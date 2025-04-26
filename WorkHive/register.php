<?php
require_once 'connectdb.php';
header('Content-Type: application/json');

// Đọc dữ liệu đầu vào
$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (!is_array($data)) {
    echo json_encode(['success' => false, 'message' => 'Invalid JSON']);
    exit;
}

$user_name = trim($data['user_name']?? '');
$name = trim($data['name'] ?? '');
$email = trim($data['email'] ?? '');
$password = $data['password'] ?? '';

// Kiểm tra đầu vào hợp lệ
if ( empty($user_name) || empty($name) || empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Please fill all fields']);
    exit;
}
if (!preg_match('/^[a-zA-Z0-9_]+$/', $user_name)) {
    echo json_encode(['success' => false, 'message' => 'Username can only contain letters, numbers, and underscores']);
    exit;
}

// định dạng email
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(['success' => false, 'message' => 'Invalid email format']);
    exit;
}

if (strlen($password) < 6) {
    echo json_encode(['success' => false, 'message' => 'Password must be at least 6 characters']);
    exit;
}

try {
    // Kiểm tra email có tồn tại chưa
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE email = :email");
    $stmt->execute([':email' => $email]);
    if ($stmt->fetchColumn() > 0) {
        echo json_encode(['success' => false, 'message' => 'Email already exists']);
        exit;
    }
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE user_name = :user_name");
    $stmt->execute([':user_name' => $user_name]);
    if ($stmt->fetchColumn() > 0) {
        echo json_encode(['success' => false, 'message' => 'UserName already exists']);
        exit;
    }
    // Chèn dữ liệu vào database
    $stmt = $pdo->prepare("INSERT INTO users (user_name,name, email, password) VALUES (:user_name,:name, :email, :password)");
    $stmt->execute([
        ':user_name' => $user_name,
        ':name' => $name,
        ':email' => $email,
        ':password' => password_hash($password, PASSWORD_DEFAULT),
    ]);

    echo json_encode(['success' => true, 'message' => 'Registration successful']);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>
