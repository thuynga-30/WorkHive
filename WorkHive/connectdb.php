<?php
// connect dtb
$host ='localhost';
$username = 'root';
$password = '';
$database = 'workhive';
$dsn = "mysql:host=$host;dbname=$database; charset=utf8mb4";
$option=[
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES => false,
];
try{
$pdo = new PDO($dsn, $username, $password, $option);
} catch (\PDOException $e){
    throw new \PDOException($e->getMessage(), (int)$e->getCode());
}
?>
