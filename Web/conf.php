<?php
$conn = new mysqli("localhost","root","OlympiaRPG","icarus");
if ($conn->connect_error) {
    die("Connection to MySQL server failed. Bad things a-happening!");
}
$callsign = $_POST["callsign"];
$freq = $_POST["frequency"];
$bandwidth = $_POST["bandwidth"];
$sf = $_POST["spreading"];
$coding = $_POST["coding"];
$explicit = $_POST["header"];
$stmt = $conn->prepare("INSERT INTO payload (callsign,frequency,bandwidth,spreading_factor,coding,explicit,created_at) VALUES (?,?,?,?,?,?,NOW())");
$stmt->bind_param("sddiii",$callsign,$freq,$bandwidth,$sf,$coding,$explicit);
$stmt->execute();

echo "Done!";
$stmt->close();
$conn->close();
?>