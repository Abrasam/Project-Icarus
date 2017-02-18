<html>
    <head>
        <title>Configure Payload</title>
        <link rel="stylesheet" type="text/css" href="style.css">
    </head>
    <body style="font-family:Arial;">
        <div class="nav">
            <center>
                <h1><a href="/">Project Icarus</a></h1>
                <h5>A 2-way communications system for High Altitude Ballooning. Works with Habitat.</h5>
                <h5>By Sam Sully (@Sullore or jakeio on #highaltitude).</h5>
            </center>
        </div>
        <div class="content">
            <?php
            if (count($_POST) < 8 || (is_null($_POST["callsign"]) || $_POST["txfrequency"] == 0 || $_POST["rxfrequency"] == 0 || $_POST["spreading"] > 12 || $_POST["spreading"] < 6)) {
                die("Please supply all values within valid ranges.");
            }
            $conn = new mysqli("localhost","root","OlympiaRPG","icarus");
            if ($conn->connect_error) {
                die("Connection to MySQL server failed. Bad things a-happening!");
            }
            $callsign = $_POST["callsign"];
            $txfreq = $_POST["txfrequency"];
	    $rxfreq = $_POST["rxfrequency"];
	    $txbandwidth = $_POST["txbandwidth"];
            $rxbandwidth = $_POST["rxbandwidth"];
            $sf = $_POST["spreading"];
            $coding = $_POST["coding"];
            $explicit = $_POST["header"];
            $stmt = $conn->prepare("INSERT INTO payload (callsign,txfrequency,txbandwidth,spreading_factor,coding,explicit,created_at,rxfrequency,rxbandwidth) VALUES (?,?,?,?,?,?,NOW(),?,?)");
            $stmt->bind_param("sddiiidd",$callsign,$txfreq,$txbandwidth,$sf,$coding,$explicit,$rxfreq,$rxbandwidth);
            $stmt->execute();

            echo "Successfully added to payload database!";
            $stmt->close();
            $conn->close();
            ?>
        </div>
    </body>
</html>
