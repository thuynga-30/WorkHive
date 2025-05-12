<?php
require_once "connectdb.php";
// require "notification_manager.php";
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');


ob_clean(); // Xóa sạch output đệm trước đó
ob_start();

session_start();
$method = $_SERVER['REQUEST_METHOD'];
if ($method == 'POST' || $method == 'PUT') {
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);

    if (!is_array($data)) {
        echo json_encode(['success' => false, 'message' => 'Invalid JSON']);
        exit;
    }
    
} 
    $action = isset($_GET['action']) ? $_GET['action'] : '';


switch ($method) {
    case 'POST':
        if ($action === 'create') create_task($pdo, $data);
        elseif ($action ==='createSubTask')  createSubTask($pdo,$data);
        else if ($action === 'delete') delete_SubTask($pdo, $data);
        else if ($action === 'remove') remove_task($pdo,$data);
        break;
    case 'PUT':
        if ($action === 'update') update_task($pdo, $data);
        else if ($action === 'status') update_status($pdo, $data);
        break;
    case 'GET':
        if ($action === 'group') get_tasks_by_group($pdo);
            else if ($action === 'user') get_tasks_by_user($pdo);
                else if ($action === 'stats_group') getTaskProgress($pdo);
                     else if ($action ==='subtask')  get_subtask_by_group($pdo);
                        else if ($action ==='check_over') checkOverdueTasksAndNotify($pdo);
        break;
        
    default:
        echo json_encode(['success' => false, 'message' => 'Invalid request']);
}
function createSubTask($pdo, $data) {
    $parent_id = $data['task_id'] ?? null;
    $title = $data['title'] ?? '';
    $description = $data['description'] ?? '';
    $assigned_to = $data['assigned_to'] ?? null;
    $group_id = $data['group_id'] ?? null;
    $due_date = $data['due_date'] ?? '';

    if (!$parent_id || !$title || !$group_id || !$due_date) {
        echo json_encode(['success' => false, 'message' => 'Missing required fields']);
        return;
    }

    try {
        if ($assigned_to) {
            $checkUser = $pdo->prepare("
                SELECT * FROM group_members 
                WHERE user_name = ? 
                AND group_id = ?
            ");
            $checkUser->execute([$assigned_to, $group_id]);
            $user = $checkUser->fetch(PDO::FETCH_ASSOC);

            if (!$user) {
                echo json_encode(['success' => false, 'message' => 'Assigned user does not exist in the group']);
                return;
            }
        }
        $stmt = $pdo->prepare("INSERT INTO tasks (parent_task_id, title, description, assigned_to, group_id, due_date) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->execute([$parent_id, $title, $description, $assigned_to, $group_id, $due_date]);
        sendNotification($pdo, $assigned_to, "Bạn vừa được giao một công việc mới: $title.");

        echo json_encode(['success' => true, 'message' => 'Subtask created']);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}
function sendNotification($pdo, $user_name, $message) {
    try {
        if (empty($user_name) || empty($message)) {
            error_log("Notification Error: Missing user_name or message");
            return false;
        }

        $stmt = $pdo->prepare("INSERT INTO notifications (user_name, content) VALUES (?, ?)");
        if ($stmt->execute([$user_name, $message])) {
            return true;
        } else {
            error_log("Notification Error: Failed to insert notification");
            return false;
        }
    } catch (PDOException $e) {
        error_log("Notification Error: " . $e->getMessage());
        return false;
    }
}

function update_status($pdo, $data) {
    $task_id = $data['task_id'] ?? null;
    $status = $data['status'] ?? '';

    if (!$task_id || !in_array($status, ['Pending', 'Doing', 'Done'	])) {
        echo json_encode(['success' => false, 'message' => 'Invalid status or missing task_id']);
        return;
    }

    $stmt = $pdo->prepare("UPDATE tasks SET status = ? WHERE task_id = ?");
    $stmt->execute([$status, $task_id]);
    if ($status == 'Done') {
    // Gửi cho leader hoặc assigned
    $stmt = $pdo->prepare("SELECT t.title, t.assigned_to, g.created_by FROM tasks t 
                           JOIN groups g ON t.group_id = g.group_id 
                           WHERE t.task_id = ?");
    $stmt->execute([$task_id]);
    $info = $stmt->fetch();

    if ($info) {
        $task_title = $info['title'];
        $recipient = $info['created_by']; 
        sendNotification($pdo, $recipient, "$task_title đã hoàn thành");
    }
}

    echo json_encode(['success' => true, 'message' => 'Status updated']);
}

function checkOverdueTasksAndNotify($pdo) {
    $stmt = $pdo->prepare("SELECT task_id, title, assigned_to, group_id, due_date 
                           FROM tasks 
                           WHERE due_date < NOW() 
                             AND status != 'Done'
                             AND overdue_notified = FALSE");
    $stmt->execute();
    $tasks = $stmt->fetchAll();

    foreach ($tasks as $task) {
        $recipient = $task['assigned_to'];
        $group_id = $task['group_id'];
    if (empty($recipient)) continue; 
        $task_title = $task['title'];
        $due_date = date('d/m/Y', strtotime($task['due_date']));
        $message = "Công việc \"{$task_title}\" giao cho {$recipient} đã quá hạn vào ngày {$due_date}";

         $leaderStmt = $pdo->prepare("
                SELECT user_name 
                FROM group_members 
                WHERE group_id = ? AND role = 'LEADER'
            ");
            $leaderStmt->execute([$group_id]);
            $leader = $leaderStmt->fetch(PDO::FETCH_ASSOC);

            if ($leader) {
                $leader_name = $leader['user_name'];
                sendNotification($pdo, $leader_name, $message);
            }
        // Đánh dấu đã gửi thông báo
        $update = $pdo->prepare("UPDATE tasks SET overdue_notified = TRUE WHERE task_id = ?");
        $update->execute([$task['task_id']]);
    }
}
function getTaskProgress($pdo) {
    $parent_id = $_GET['task_id'] ?? null;
    if (!$parent_id) {
        echo json_encode(['success' => false, 'message' => 'Group ID required']);
        return;
    }

    try {
        // Lấy tổng số subtask
        $stmt = $pdo->prepare("SELECT status FROM tasks WHERE parent_task_id  = ?");
        $stmt->execute([$parent_id]);

       
        $subtasks = $stmt->fetchAll(PDO::FETCH_COLUMN);
        $total = count($subtasks);
        $done = count(array_filter($subtasks, fn($status) => strtolower($status) === 'done'));

        $progress = $total > 0 ? round(($done / $total) * 100) : 0;

        echo json_encode([
            'success' => true,
            'progress_percent' => $progress
        ]);
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'DB error']);
    }
}
function get_tasks_by_group($pdo) {
    $group_id = $_GET['group_id'] ?? null;

    if (!$group_id) {
        echo json_encode(['success' => false, 'message' => ' Group ID are required']);
        return;
    }

    $stmt = $pdo->prepare("SELECT * FROM tasks WHERE group_id = ? AND parent_task_id  IS NULL");
    $stmt->execute([$group_id]);
    $tasks = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($tasks) {
        echo json_encode(['success' => true, 'tasks' => $tasks]);
    } else {
        echo json_encode(['success' => false, 'message' => 'No tasks found']);
    }

}
function get_subtask_by_group($pdo){
    $group_id = $_GET['group_id'] ?? null;
    $parent_id = $_GET['task_id'] ?? null;
    if (!$group_id || !$parent_id) {
        echo json_encode(['success' => false, 'message' => 'Group ID and Task ID are required']);
        return; 
    }
    $stmt = $pdo->prepare("SELECT * FROM tasks WHERE group_id = ? AND parent_task_id  IS NOT NULL AND parent_task_id = ? ");
    $stmt->execute([$group_id,$parent_id]);
    $tasks = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($tasks) {
        echo json_encode(['success' => true, 'tasks' => $tasks]);
    } else {
        echo json_encode(['success' => false, 'message' => 'No tasks found']);
    }
}
function remove_task($pdo, $data) {
    $task_id = $data['task_id'] ?? null;
    $group_id = $data['group_id'] ?? null;

    if (!$task_id || !$group_id) {
        echo json_encode(['success' => false, 'message' => 'Task ID and Group ID required']);
        exit;
    }

    // Xóa tất cả task con của task tổng
    $stmt = $pdo->prepare("DELETE FROM tasks WHERE parent_task_id = ? AND group_id = ?");
    $stmt->execute([$task_id, $group_id]);

    // Xóa luôn task tổng
    $stmt = $pdo->prepare("DELETE FROM tasks WHERE task_id = ? AND group_id = ?");
    $stmt->execute([$task_id, $group_id]);

    echo json_encode(['success' => true, 'message' => 'Task and its sub-tasks deleted']);
    exit;
}

function create_task($pdo, $data) {
    $title = $data['title'] ?? '';
    $description = $data['description'] ?? '';
    $group_id = $data['group_id'] ?? null;
    $due_date = $data['due_date'] ?? '';

    if (!$title ) {
        echo json_encode(['success' => false, 'message' => 'Missing title fields']);
        return;
    }
    if (!$group_id ) {
        echo json_encode(['success' => false, 'message' => 'Missing groupid fields']);
        return;
    }
    if (!$due_date ) {
        echo json_encode(['success' => false, 'message' => 'Missing day fields']);
        return;
    }
    try{
    $stmt = $pdo->prepare("INSERT INTO tasks (title, description, group_id, due_date,status) VALUES (?, ?, ?, ?,'Doing')");
    $stmt->execute([$title, $description, $group_id, $due_date]);

    echo json_encode(['success' => true, 'message' => 'Task created']);
    }  catch(PDOException $e){
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);

    }
}



function delete_SubTask($pdo, $data) {
    $title = $data['title'] ?? null;
    if (!$title) {
        echo json_encode(['success' => false, 'message' => 'Task ID required']);
        return;
    }

    $stmt = $pdo->prepare("DELETE FROM tasks WHERE title = ?");
    $stmt->execute([$title]);
    echo json_encode(['success' => true, 'message' => 'Task deleted']);
}

function update_task($pdo, $data) {
    $task_id = $data['task_id'] ?? null;
    $title = $data['title'] ?? '';
    $description = $data['description'] ?? '';
    $due_date = $data["due_date"] ?? '';

    if (!$task_id || !$title) {
        echo json_encode(['success' => false, 'message' => 'Missing task_id or title']);
        return;
    }

    $stmt = $pdo->prepare("UPDATE tasks SET title = ?, description = ?, due_date =? WHERE task_id = ?");
    $stmt->execute([$title, $description,$due_date, $task_id]);
    echo json_encode(['success' => true, 'message' => 'Task updated']);
}

function get_tasks_by_user($pdo) {
    $headers = apache_request_headers();

    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $user_name = $headers['Authorization'];
    // $user_name = $_GET['user_name'] ?? null;
    if (!$user_name) {
        echo json_encode(['success' => false, 'message' => 'User Name required']);
        return;
    }

    $stmt = $pdo->prepare("SELECT * FROM tasks WHERE assigned_to = ? AND parent_task_id IS NOT NULL");
    $stmt->execute([$user_name]);
    $tasks = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(['success' => true, 'tasks' => $tasks]);
}




?>