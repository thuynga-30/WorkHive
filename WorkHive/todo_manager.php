<?php
require_once "connectdb.php";
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
    $action = isset($data['action']) ? $data['action'] : '';
} elseif ($method == 'GET') {
    $action = isset($_GET['action']) ? $_GET['action'] : '';

}
switch ($method) {
    case 'POST':
        if ($action === 'create') create_task($pdo, $data);
        elseif ($action ==='createSubTask')  createSubTask($pdo,$data);
        // else if ($action === 'assign') assign_task($pdo, $input);
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
                else if ($action === 'stats_group') get_group_progress_stats($pdo);
                     else if ($action === 'stats_user') get_user_progress_stats($pdo);
        break;
        
    default:
        echo json_encode(['success' => false, 'message' => 'Invalid request']);
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
    $stmt = $pdo->prepare("INSERT INTO tasks (title, description, group_id, due_date) VALUES (?, ?, ?, ?)");
    $stmt->execute([$title, $description, $group_id, $due_date]);

    echo json_encode(['success' => true, 'message' => 'Task created']);
    }  catch(PDOException $e){
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);

    }
}
function createSubTask($pdo, $data) {
    $parent_id = $data['task_id'] ?? null; // Quan trọng: Lấy đúng parent_id!
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

        echo json_encode(['success' => true, 'message' => 'Subtask created']);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}


function delete_SubTask($pdo, $data) {
    $task_id = $data['task_id'] ?? null;
    if (!$task_id) {
        echo json_encode(['success' => false, 'message' => 'Task ID required']);
        return;
    }

    $stmt = $pdo->prepare("DELETE FROM tasks WHERE task_id = ?");
    $stmt->execute([$task_id]);
    echo json_encode(['success' => true, 'message' => 'Task deleted']);
}
function remove_task($pdo,$data){
    $group_id = $data['group_id'] ?? null;
    if (!$group_id){
         echo json_encode(['success' => false, 'message' => 'Group ID required']);
        return;
    }
    $stmt = $pdo->prepare("DELETE FROM tasks WHERE group_id = ?");
    $stmt->execute([$group_id]);
    echo json_encode(['success' => true, 'message' => 'Task deleted']);
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
    echo json_encode(['success' => true, 'message' => 'Status updated']);
}

function update_task($pdo, $data) {
    $task_id = $data['task_id'] ?? null;
    $title = $data['title'] ?? '';
    $description = $data['description'] ?? '';

    if (!$task_id || !$title) {
        echo json_encode(['success' => false, 'message' => 'Missing task_id or title']);
        return;
    }

    $stmt = $pdo->prepare("UPDATE tasks SET title = ?, description = ? WHERE task_id = ?");
    $stmt->execute([$title, $description, $task_id]);
    echo json_encode(['success' => true, 'message' => 'Task updated']);
}

function get_tasks_by_group($pdo) {
    $group_id = $_GET['group_id'] ?? null;
    if (!$group_id) {
        echo json_encode(['success' => false, 'message' => 'Group ID required']);
        return;
    }

    $stmt = $pdo->prepare("SELECT * FROM tasks WHERE group_id = ? AND parent_id IS NULL");
    $stmt->execute([$group_id]);
    $tasks = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(['success' => true, 'tasks' => $tasks]);
}

function get_tasks_by_user($pdo) {
    $user_name = $_GET['user_name'] ?? null;
    if (!$user_name) {
        echo json_encode(['success' => false, 'message' => 'User Name required']);
        return;
    }

    $stmt = $pdo->prepare("SELECT * FROM tasks WHERE assigned_to = ? AND parent_id IS NOT NULL");
    $stmt->execute([$user_name]);
    $tasks = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(['success' => true, 'tasks' => $tasks]);
}
function get_group_progress_stats($pdo) {
    $group_id = $_GET['group_id'] ?? null;
    if (!$group_id) {
        echo json_encode(['success' => false, 'message' => 'Group ID required']);
        return;
    }

    $stmt = $pdo->prepare("
        SELECT status, COUNT(*) as total 
        FROM tasks 
        WHERE group_id = ?
        GROUP BY status
    ");
    $stmt->execute([$group_id]);
    $stats = $stmt->fetchAll(PDO::FETCH_KEY_PAIR);

    echo json_encode([
        'success' => true,
        'group_id' => $group_id,
        'stats' => [
            'Pending' => (int)($stats['Pending'] ?? 0),
            'Doing' => (int)($stats['Doing'] ?? 0),
            'Done' => (int)($stats['Done'] ?? 0)
        ]
    ]);
}
function get_user_progress_stats($pdo) {
    $user_name = $_GET['user_name'] ?? null;
    if (!$user_name) {
        echo json_encode(['success' => false, 'message' => 'User name required']);
        return;
    }

    $stmt = $pdo->prepare("
        SELECT status, COUNT(*) as total 
        FROM tasks 
        WHERE assigned_to = ?
        GROUP BY status
    ");
    $stmt->execute([$user_name]);
    $stats = $stmt->fetchAll(PDO::FETCH_KEY_PAIR);

    echo json_encode([
        'success' => true,
        'user_name' => $user_name,
        'stats' => [
            'Pending' => (int)($stats['Pending'] ?? 0),
            'Doing' => (int)($stats['Doing'] ?? 0),
            'Done' => (int)($stats['Done'] ?? 0)
        ]
    ]);
}

// function assign_task($pdo, $data) {
//     $task_id = $data['task_id'] ?? null;
//     $assigned_to = $data['assigned_to'] ?? '';

//     if (!$task_id || !$assigned_to) {
//         echo json_encode(['success' => false, 'message' => 'Missing task_id or assigned_to']);
//         return;
//     }

//     $stmt = $pdo->prepare("UPDATE tasks SET assigned_to = ? WHERE task_id = ?");
//     $stmt->execute([$assigned_to, $task_id]);
//     echo json_encode(['success' => true, 'message' => 'Task assigned']);
// }
?>