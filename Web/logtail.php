<html>
<head>
<meta http-equiv="refresh" content="2">
</head>
<body>
<?php
$file = fopen("log.txt", "r") or die("No file!");
$data = fread($file, filesize("log.txt"));
fclose($file);
$exploded = explode("\n",$data);
for ($i = count($exploded) -1; $i > count($exploded)-50; $i--) {
    if ($i < 0) {
        break;
    }
    echo $exploded[$i] . "<br>";
}
?>
</body>
</html>
