<?php
require_once 'connectdb.php';
if (!isset($_SESSION['user_name'])) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    return;
}
$userName = $_SESSION['user_name'];

try {
    // Lấy nhóm đã tạo
    $stmt1 = $pdo->prepare("SELECT * FROM groups WHERE created_by = ?");
    $stmt1->execute([$userName]);
    $createdGroups = $stmt1->fetchAll(PDO::FETCH_ASSOC);

    // Lấy nhóm đã tham gia (ngoại trừ nhóm đã tạo để tránh trùng lặp)
    $stmt2 = $pdo->prepare("
        SELECT g.* 
        FROM group_members gm
        JOIN groups g ON gm.group_id = g.group_id
        WHERE gm.user_name = ?
          AND g.created_by != ?
    ");
    $stmt2->execute([$userName, $userName]);
    $joinedGroups = $stmt2->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'success' => true,
        'created_groups' => $createdGroups,
        'joined_groups' => $joinedGroups
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}

?>