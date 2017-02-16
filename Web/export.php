<?php
$conn = new mysqli("localhost","root","OlympiaRPG","icarus");
if ($conn->connect_error) {
    die("Connection to MySQL server failed. Bad things a-happening!");
}
$start = $_POST["start"];
$end = $_POST["end"];
$callsign = $_POST["callsign"];
$stmt = $conn->prepare("SELECT packet.raw FROM packet INNER JOIN payload ON payload.payload_id = packet.payload_id WHERE payload.callsign = ? AND packet.time < ? AND packet.time > ?");
$stmt->bind_param("sss",$callsign,$end,$start);
$stmt->bind_result($result);
$stmt->execute();
while ($stmt->fetch()) {
    printf($result . "<br>");
}
?>
