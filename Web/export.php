<?php
//Open new database connection.
$conn = new mysqli("localhost","root","OlympiaRPG","icarus");
if ($conn->connect_error) {
    //End program if database failed to load.
    die("Connection to MySQL server failed. Bad things a-happening!");
}
//Acquire data from HTTP POST.
$start = $_POST["start"];
$end = $_POST["end"];
$callsign = $_POST["callsign"];
//Prepare SQL statement, this is cross-table parametrised SQL using an INNER JOIN.
$stmt = $conn->prepare("SELECT packet.raw FROM packet INNER JOIN payload ON payload.payload_id = packet.payload_id WHERE payload.callsign = ? AND packet.time < ? AND packet.time > ?");
$stmt->bind_param("sss",$callsign,$end,$start);
$stmt->bind_result($result);
$stmt->execute();
//Output CSV data.
printf("type,data<br>");
while ($stmt->fetch()) {
    $data = explode(",", explode("*", $result)[0]);
    printf($data[2] . "," . $data[3] . "<br>");
}
?>
