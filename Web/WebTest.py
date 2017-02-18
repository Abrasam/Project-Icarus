from http.server import HTTPServer,SimpleHTTPRequestHandler
from time import strftime
import requests as r
import base64,hashlib,MySQLdb,crcmod,datetime

checksum = crcmod.predefined.mkCrcFun('crc-ccitt-false')

db = MySQLdb.connect(host="localhost",user="root",passwd="OlympiaRPG",db="icarus")
cursor = db.cursor()

class TestHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        path = self.path[1:]
        cursor.execute("SELECT * FROM payload WHERE callsign=%s", (path,))
        result = cursor.fetchall()
        maxID = -1
        maxDate = datetime.datetime(1970, 1, 1)
        for row in result:
            if row[2] > maxDate:
                maxID = row[0]
                maxDate = row[2]
        if maxID == -1:
            return
        cursor.execute("SELECT callsign,txfrequency,txbandwidth,spreading_factor,coding,explicit,rxfrequency,rxbandwidth FROM payload WHERE payload_id=%s", (maxID,))
        result = cursor.fetchall()[0]
        self.send_response(200)
        self.send_header('Content-type','text/html')
        self.end_headers()
        out = result[0] + "," + str(result[1]) + "," + str(result[2]) + "," + str(result[3]) + "," + str(result[4]) + "," + str(result[5]) + "," + str(result[6]) + "," + str(result[7])
        self.wfile.write(out.encode("iso-8859-1"))
            

    def do_PUT(self):
        path = self.path
        length = int(self.headers['content-length'])
        data = self.rfile.read(length).decode("iso-8859-1")
        if path == "/telemetryUpload":
            handleTelem(data)
        elif path == "/imageUpload":
            handleSSDV(data)
        elif path == "/packetUpload":
            handlePacket(data)
        self.send_response(201)
        self.send_header('Content-type','text/html')
        self.end_headers()
        self.wfile.write("Received.".encode())

def handleTelem(data):
    b64 = (base64.b64encode(data.encode()))
    sha256 = hashlib.sha256(b64).hexdigest()
    b64 = b64.decode()
    now = strftime("%Y-%0m-%0dT%H:%M:%SZ")
    json = "{\"data\": {\"_raw\": \"%s\"},\"receivers\": {\"%s\": {\"time_created\": \"%s\",\"time_uploaded\": \"%s\"}}}" % (b64, "SAMPI", now, now)
    headers = {"Accept" : "application/json", "Content-Type" : "application/json", "charsets" : "utf-8"}
    try:
        res = r.put("http://habitat.habhub.org/habitat/_design/payload_telemetry/_update/add_listener/"+sha256, headers=headers, data=json)
        with open("log.txt", "a+") as f:
            f.write("[TELEM FWD] " + data)
    except:
        print("Unable to reach habitat.")

def handleSSDV(data):
    data = "U" + data
    b64 = base64.b64encode(bytearray(data.encode('iso-8859-1'))).decode('utf-8')
    headers = {"Accept" : "application/json", "Content-Type" : "application/json", "charsets" : "utf-8"}
    now = strftime("%Y-%0m-%0dT%H:%M:%SZ")
    upload = "{\"type\": \"packet\", \"packet\": \"%s\", \"encoding\": \"base64\", \"received\": \"%s\", \"receiver\": \"%s\"}" % (b64, now, "SAMPI")
    try:
        res = r.post("http://ssdv.habhub.org/api/v0/packets", headers=headers, data=upload, timeout=2)
        with open("log.txt", "a+") as f:
            f.write("[IMG PCKT FWD]\n") #remember to add the correct Image ID and stuff here.
    except:
        print("Unable to reach habitat.")

def handlePacket(raw):
    raw = raw.replace("\n", "\\n")
    data = raw.split("*")
    sentence = data[0].replace(">","")
    csum = data[1]
    packetData = sentence.split(",")
    callsign = packetData[0]
    print(packetData)
    cursor.execute("SELECT * FROM payload WHERE callsign=%s", (callsign,))
    result = cursor.fetchall()
    maxID = -1
    maxDate = datetime.datetime(1970, 1, 1)
    for row in result:
        if row[2] > maxDate:
            maxID = row[0]
            maxDate = row[2]
    if maxID == -1:
        return
    cursor.execute("INSERT INTO packet (payload_id,time,raw) VALUES (%s,NOW(),%s)", (maxID,raw,))
    db.commit()
    with open("log.txt", "a+") as f:
            f.write("[PCKT LOGGED]"  + raw + "\n")

server = HTTPServer(("",8080), TestHandler)

try:
    server.serve_forever()
except KeyboardInterrupt:
    server.server_close()
