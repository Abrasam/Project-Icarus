<html>
    <head>
        <title>Configure Payload</title>
        <link rel="stylesheet" type="text/css" href="style.css">
    </head>
    <body style="font-family:Arial;">
        <div class="nav">
            <center>
                <h1>Project Icarus</h1>
                <h5>A 2-way communications system for High Altitude Ballooning. Works with Habitat.</h5>
                <h5>By Sam Sully (@Sullore or jakeio on #highaltitude).</h5>
            </center>
        </div>
        <div class="content">
            <?php
            if (count($_POST) < 6 || (is_null($_POST["callsign"]) || $_POST["frequency"] == 0 || $_POST["spreading"] > 12 || $_POST["spreading"] < 6)) {
                die("Please supply all values.");
            }
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

            echo "Successfully added to payload database!";
            $stmt->close();
            $conn->close();
            ?>
        </div>
    </body>
</html>