from http.server import HTTPServer,BaseHTTPRequestHandler
from time import strftime
import requests as r
import base64,hashlib
class TestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        path = self.path
        self.send_response(200)
        self.send_header('Content-type','text/html')
        self.end_headers()
        if path[1:] == "wibble":
            self.wfile.write("wobble".encode())
        else:
            self.wfile.write("wibble with me?".encode())
    def do_PUT(self):
        path = self.path
        length = int(self.headers['content-length'])
        data = self.rfile.read(length).decode("iso-8859-1")
        if path == "/telemetryUpload":
            ###############################
            # Database logging goes here. #
            ###############################
            b64 = (base64.b64encode(data.encode()))
            sha256 = hashlib.sha256(b64).hexdigest()
            b64 = b64.decode()
            now = strftime("%Y-%0m-%0dT%H:%M:%SZ")
            json = "{\"data\": {\"_raw\": \"%s\"},\"receivers\": {\"%s\": {\"time_created\": \"%s\",\"time_uploaded\": \"%s\"}}}" % (b64, "SAMPI", now, now)
            headers = {"Accept" : "application/json", "Content-Type" : "application/json", "charsets" : "utf-8"}
            try:
                res = r.put("http://habitat.habhub.org/habitat/_design/payload_telemetry/_update/add_listener/"+sha256, headers=headers, data=json)
            except:
                print("Unable to reach habitat.")
            self.send_response(201)
            self.send_header('Content-type','text/html')
            self.end_headers()
            self.wfile.write("Telemetry received.".encode())

server = HTTPServer(("",8080), TestHandler)
try:
    server.serve_forever()
except KeyboardInterrupt:
    server.server_close()
