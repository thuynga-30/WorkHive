<?php
    require_once 'connectdb.php';
    header('Content-Type: application/json; charset=utf-8');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type');


    ob_clean(); // Xóa sạch output đệm trước đó
    ob_start();

    session_start();
    $method = $_SERVER['REQUEST_METHOD'];

    // $action = isset($_GET['action']) ? $_GET['action'] : '';

switch ($method) {
    case 'POST':
        create_evaluation($pdo);
        break;
   
    case 'GET':
        getEvaluation($pdo);
        break;
        
    default:
        echo json_encode(['success' => false, 'message' => 'Invalid request']);
}
function getEvaluation($pdo){
    $group_id = $_GET["group_id"]?? null;
     if (!$group_id) {
        echo json_encode(['success' => false, 'message' => ' Group ID are required']);
        return;
    }

    $stmt = $pdo->prepare("SELECT * FROM evaluations WHERE group_id = ? ");
    $stmt->execute([$group_id]);
    $evaluations = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($evaluations) {
        echo json_encode(['success' => true, 'evaluations' => $evaluations]);
    } else {
        echo json_encode(['success' => false, 'message' => 'No evaluation found']);
    }
}
function create_evaluation($pdo){
      $group_id = $_GET["group_id"]?? null;
     if (!$group_id) {
        echo json_encode(['success' => false, 'message' => ' Group ID are required']);
        return;
    }
    $stmt = $pdo->prepare("SELECT user_name FROM group_members WHERE group_id = ?");
    $stmt->execute([$group_id]);
    $members = $stmt->fetchAll(PDO::FETCH_COLUMN);

    if (!$members) {
        echo json_encode(['success' => false, 'message' => 'Không có thành viên trong nhóm']);
        return;
    }

    foreach ($members as $user_name) {
        $stmt = $pdo->prepare("
            SELECT COUNT(*) AS total_subtasks,
                   SUM(CASE 
                        WHEN status = 'Done' AND due_date >= created_at THEN 1 
                        ELSE 0 
                   END) AS ontime_subtasks
            FROM tasks
            WHERE assigned_to = ? AND group_id = ? AND parent_task_id IS NOT NULL
        ");
        $stmt->execute([$user_name, $group_id]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);

        $total = $result['total_subtasks'] ?? 0;
        $ontime = $result['ontime_subtasks'] ?? 0;

        $rate_percent = ($total > 0) ? ($ontime / $total) : 0;

        if ($rate_percent >= 0.8) {
            $rating = 'Good';
        } elseif ($rate_percent >= 0.5) {
            $rating = 'Quite Good';
        } else {
            $rating = 'Average';
        }

        $stmt = $pdo->prepare("
            INSERT INTO evaluations (group_id, evaluated_user, total_subtasks, ontime_subtasks, rating, evaluated_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            ON DUPLICATE KEY UPDATE
                total_subtasks = VALUES(total_subtasks),
                ontime_subtasks = VALUES(ontime_subtasks),
                rating = VALUES(rating),
                evaluated_at = NOW()
        ");
        $stmt->execute([$group_id, $user_name, $total, $ontime, $rating]);
    }

    echo json_encode(['success' => true, 'message' => 'Đánh giá các thành viên hoàn tất']);
}

?>