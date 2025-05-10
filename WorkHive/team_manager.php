<?php
require_once 'connectdb.php';
// require_once 'notification_manager.php';
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

switch ($method){
    case 'POST':
        if (isset($_GET['add_member']) && $_GET['add_member'] == '1'){
            add_members($pdo,$data);
        
        }elseif (isset($_GET['remove_user']) && $_GET['remove_user'] == '1') {
            remove_user_from_group($pdo, $data);
        } elseif (isset($_GET['delete_group']) && $_GET['delete_group'] == '1') {
            delete_group($pdo,$data);
        }else{
            create_groups($pdo,$data);
        }
        break;
        case 'GET':
            if (isset($_GET['group_id']) && !isset($_GET['get_group_by_id'])) {
                $data = ['group_id' => $_GET['group_id']];
                getMembersOfGroup($pdo, $data);
            } else if (isset($_GET['get_group_by_id']) && isset($_GET['group_id'])) {
                $data = ['group_id' => $_GET['group_id']];
                get_group_by_id($pdo, $data);
            } else {
                get_groups($pdo);
            }
            break;
        
        
    case 'PUT' :
        update_group($pdo,$data);
        break;
    
        
}
function get_group_by_id($pdo, $data){
    $group_id = $data['group_id'] ?? null;

    if (!$group_id || !is_numeric($group_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 
        'message' => 'Invalid Group ID']);
        return;
    }

    try {
        $stmt = $pdo->prepare("SELECT * FROM groups WHERE group_id = ?");
        $stmt->execute([$group_id]);
        $group = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$group) {
            http_response_code(404);
            echo json_encode(['success' => false,
             'message' => 'Group not found']);
            return;
        }

        http_response_code(200);
        echo json_encode([
            'success' => true,
            'message' => 'Load Group success',
            'group' => $group
        ]);
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 
        'message' => 'Database error: ' . $e->getMessage()]);
    }
}
function getMembersOfGroup($pdo, $data) {
    $group_id = $data['group_id'] ?? null;
    if (!$group_id || !is_numeric($group_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Invalid Group ID']);
        return;
    }

    try {
        $stmt = $pdo->prepare("SELECT user_name, role FROM group_members WHERE group_id = ?");
        $stmt->execute([$group_id]);

        $members = [];
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $members[] = [
                'user_name' => $row['user_name'],
                'role' => $row['role']
            ];
        }

        echo json_encode([
            'success' => true,
            'message' => 'Get group success',
            'members' => $members
        ]);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function delete_group($pdo, $data) {
    $headers = apache_request_headers();
    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }
    $userName = $headers['Authorization'];

    $group_id = $data['group_id'] ?? null;

    if (!$group_id || !is_numeric($group_id)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Invalid Group ID']);
        return;
    }

    try {
        // Kiểm tra xem người dùng có phải là leader không
        $stmt = $pdo->prepare("SELECT * FROM groups WHERE group_id = ? AND created_by = ?");
        $stmt->execute([$group_id,$userName]);
        if ($stmt->rowCount() === 0) {
            http_response_code(403);
            echo json_encode(['success' => false, 'message' => 'Only the creator of the group can delete it']);
            return;
        }


        // Xóa nhóm khỏi bảng `groups` và `group_members`
        $pdo->beginTransaction();
        $stmt = $pdo->prepare("DELETE FROM group_members WHERE group_id = ?");
        $stmt->execute([$group_id]);

        $stmt = $pdo->prepare("DELETE FROM groups WHERE group_id = ?");
        $stmt->execute([$group_id]);

        // Commit giao dịch
        $pdo->commit();
        http_response_code(200);

        echo json_encode(['success' => true, 'message' => 'Group deleted successfully']);
    } catch (PDOException $e) {
        $pdo->rollBack();
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}
function get_groups($pdo){
    $headers = apache_request_headers();
    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $userName = $headers['Authorization'];

    $createdGroups = [];
    $joinedGroups = [];

    $stmt = $pdo->prepare("SELECT * FROM groups WHERE created_by = ?");
    $stmt->execute([$userName]);
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $createdGroups[] = [
            'group_id' => (int)$row['group_id'],
            'name' => $row['name'],
            'description' => $row['description'],
            'created_by' => $row['created_by'],
            
        ];
    }

    $stmt2 = $pdo->prepare("
        SELECT g.* 
        FROM group_members gm 
        JOIN groups g ON gm.group_id = g.group_id 
        WHERE gm.user_name = ? AND gm.role= 'Member'
    ");
    $stmt2->execute([$userName]);
    while ($row = $stmt2->fetch(PDO::FETCH_ASSOC)) {
        $joinedGroups[] = [
            'group_id' => (int)$row['group_id'],
            'name' => $row['name'],
            'description' => $row['description'],
            'created_by' => $row['created_by'],
        ];
    }

    echo json_encode([
        'success' => true,
        'message' => 'Get group success',
        'created_groups' => $createdGroups,
        'joined_groups' => $joinedGroups
    ]);
}

function create_groups($pdo,$data){
    $headers = apache_request_headers();

    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $userName = $headers['Authorization'];
    $name = trim($data['name'] ?? '');
    $description = trim($data['description'] ?? '');
    $created_by = $userName;

    if (empty($name) || empty($description)){
        echo json_encode(['success' => false, 'message' => 'Please fill all fields']);
        exit;
    }
    try{

        $sql = "INSERT INTO groups (name,description,created_by)
        VALUES (:name, :description, :created_by)";
        $stmt = $pdo->prepare($sql);
        $stmt->execute(
            [':name'=> $name,
            ':description'=>$description,
            ':created_by'=>$created_by
        ]);
        $group_id = $pdo->lastInsertId();

        // Add creator to the group_members table with role 'leader'
        $stmt = $pdo->prepare("INSERT INTO group_members (group_id, user_name, role) VALUES (:group_id, :user_name, 'leader')");
        $stmt->execute([ ':group_id' => $group_id, ':user_name' => $created_by ]);

        echo json_encode(['success' => true, 'message' => 'Group created']);

    }catch( PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function add_members($pdo,$data){
    $headers = apache_request_headers();
    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $userName = $headers['Authorization'];

    $group_id = $data['group_id'] ?? null;
    $user_name = trim($data['user_name']?? '');
    
    if (!$group_id || !$user_name) {
        echo json_encode(['success' => false, 'message' => 'Missing group_id or user_name']);
        return;
    }
    try {
    // Kiểm tra nhóm có tồn tại không
    $stmt = $pdo->prepare("SELECT * FROM groups WHERE group_id = ?");
    $stmt->execute([$group_id]);
    if ($stmt->rowCount() === 0) {
        echo json_encode(['success' => false, 'message' => 'Group not found']);
        return;
    }
      // Kiểm tra người dùng có tồn tại không
      $stmt = $pdo->prepare("SELECT * FROM users WHERE user_name = ?");
      $stmt->execute([$user_name]);
      if ($stmt->rowCount() === 0) {
          echo json_encode(['success' => false, 'message' => 'User not found']);
          return;
      }
    // Kiểm tra người dùng có phải là leader không (người tạo nhóm)
        $stmt = $pdo->prepare("SELECT * FROM group_members WHERE group_id = ? AND user_name = ? AND role = 'leader'");
        $stmt->execute([$group_id, $userName]);
        if ($stmt->rowCount() === 0) {
            echo json_encode(['success' => false, 'message' => 'Only the leader can add members']);
            return;
        }
      // Thêm thành viên với role 'member'
      $stmt = $pdo->prepare("INSERT INTO group_members (group_id, user_name, role)
          VALUES (?,?, 'member')");
      $stmt->execute([
           $group_id,$user_name
      ]);
      $stmt1 = $pdo->prepare("SELECT name FROM  groups  
                           WHERE group_id = ?");
        $stmt1->execute([$group_id]);
        $info = $stmt1->fetch();
        if ($info) {
            $name= $info['name'];
        
        sendNotification($pdo, $user_name, "Bạn vừa được thêm vào nhóm $name");
        }
      echo json_encode(['success' => true, 'message' => 'Member added to group']);

  } catch (PDOException $e) {
      if ($e->getCode() == 23000) {
          echo json_encode(['success' => false, 'message' => 'User already in group']);
      } else {
          echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
      }
  }
}
function remove_user_from_group($pdo, $data) {


    $group_id = $data['group_id'] ?? null;
    $user_name = $data['user_name'] ?? null;
    // $userName =$headers['Authorization'];
    if (!$group_id || !$user_name) {
        echo json_encode(['success' => false, 'message' => 'Group ID and User Name are required']);
        return;
    }

    try {
        
        $stmt = $pdo->prepare("SELECT * FROM group_members WHERE group_id = ? AND user_name = ?");
        $stmt->execute([ $group_id,  $user_name]);
        

        if ($stmt->rowCount() === 0) {
            echo json_encode(['success' => false, 'message' => 'User is not a member of the group']);
            return;
        }

        // Xóa người dùng khỏi nhóm
        
        $stmt = $pdo->prepare("DELETE FROM group_members WHERE group_id = ? AND user_name = ?");
        $stmt->execute([ $group_id, $user_name]);
        $stmt = $pdo->prepare("DELETE FROM tasks WHERE group_id = ? AND assigned_to = ?");
        $stmt->execute([ $group_id, $user_name]);
        $stmt1 = $pdo->prepare("SELECT name FROM  groups  
                           WHERE group_id = ?");
        $stmt1->execute([$group_id]);
        $info = $stmt1->fetch();
        if ($info) {
            $name= $info['name'];
        
        sendNotification($pdo, $user_name, "Bạn vừa bị xóa ra khỏi nhóm $name");
    }

        echo json_encode(['success' => true, 'message' => 'User removed from group successfully']);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}
function update_group($pdo,$data) {
   $headers = apache_request_headers();
    
    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Unauthorized']);
        return;
    }

    $group_id = $data['group_id'] ?? null;

    $userName = $headers['Authorization'];
    try {
        // Kiểm tra nhóm có tồn tại không
        $stmt = $pdo->prepare("SELECT * FROM groups WHERE group_id = ?");
        $stmt->execute([$group_id]);
        if ($stmt->rowCount() === 0) {
            echo json_encode(['success' => false, 'message' => 'Group not found']);
            return;
        }
         
        // Kiểm tra người dùng có phải là leader không (người tạo nhóm)
            $stmt = $pdo->prepare("SELECT * FROM group_members WHERE group_id = ? AND user_name = ? AND role = 'leader'");
            $stmt->execute([$group_id, $userName]);
            if ($stmt->rowCount() === 0) {
                echo json_encode(['success' => false, 'message' => 'Only the leader can add members']);
                return;
            }
            
            $stmt = $pdo->prepare("UPDATE groups SET name = ?, description = ? WHERE group_id = ?");
            $stmt->execute([$data['name'], $data['description'] ?? null, $group_id]);
            echo json_encode(['success' => true, 'message' => 'Group updated']);
            

        } catch (PDOException $e) {
            if ($e->getCode() == 23000) {
                echo json_encode(['success' => false, 'message' => 'User already in group']);
            } else {
                echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
            }
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

?>
