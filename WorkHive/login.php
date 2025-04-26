<?php
require_once 'connectdb.php';
header('Content-Type: application/json');
// session_start();

$input = file_get_contents('php://input');
$data = json_decode($input, true);

$loginInput = trim($data['email'] ?? ''); // dùng chung cho email hoặc username
$password = $data['password'] ?? '';

if (empty($loginInput) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Email/Username and password are required']);
    exit;
}

try {
    $stmt = $pdo->prepare("
        SELECT name, email, user_name, password 
        FROM users 
        WHERE email = :email OR user_name = :user_name
    ");
    $stmt->execute([
        ':email' => $loginInput,
        ':user_name' => $loginInput
    ]);

    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user || !password_verify($password, $user['password'])) {
        echo json_encode(['success' => false, 'message' => 'Invalid email/user_name or password']);
        exit;
    }
    

    echo json_encode([
        'success' => true,
        'message' => 'Login successful',
        'user' => [
            'name' => $user['name'],
            'email' => $user['email'],
            'user_name' => $user['user_name']
        ]
    ]);
    
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
