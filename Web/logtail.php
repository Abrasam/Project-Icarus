<html>
<head>
<!--The below tag makes the page refresh every 2 seconds. This ensures the log is always up-to-date.-->
<meta http-equiv="refresh" content="2">
</head>
<body>
<?php
//Open the log file.
$file = fopen("log.txt", "r") or die("No file!");
//Read the log.
$data = fread($file, filesize("log.txt"));
fclose($file);
//Split the log by newline.
$exploded = explode("\n",$data);
//The below for loop outputs each line separately followed by a <br> (line break).
for ($i = count($exploded) -1; $i > count($exploded)-50; $i--) {
    if ($i < 0) {
        break;
    }
    echo $exploded[$i] . "<br>";
}
?>
</body>
</html>
