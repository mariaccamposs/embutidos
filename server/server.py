from http.server import BaseHTTPRequestHandler, HTTPServer
from ParkingSpace import ParkingSpace, ParkingSpaceEncoder
import json
from urllib.parse import urlparse, parse_qs
import threading
import serial,time

host = "0.0.0.0" # Permite conexões de outros devices na mesma rede ao contrário de localhost
port = 5001

numberOfSpaces = 2
parkingSpaces = []
arduino = serial.Serial("COM4", 9600, timeout=1);

class thread(threading.Thread):
    def __init__(self, thread_name, thread_ID):
        threading.Thread.__init__(self)
        self.thread_name = thread_name
        self.thread_ID = thread_ID
    def run(self):
        print(str(self.thread_name) +" "+ str(self.thread_ID));
        print('Running. Press CTRL-C to exit.')
        time.sleep(0.1) #wait for serial to open
        if arduino.isOpen():
            print("{} connected!".format(arduino.port))
            try:
                while True:
                    #pcmd=input("Enter command : ")
                # arduino.write(cmd.encode())
                    time.sleep(0.1) #wait for arduino to answer
                    while arduino.inWaiting()==0: pass
                    if  arduino.inWaiting()>0: 
                        answer=arduino.readline()
                        answer=answer.decode('utf-8');
                        #print(answer)
                        parque = 1
                        if "1" in answer:
                            parque = 1
                        else:
                            parque = 2
                        if "Ocupado" in answer:
                            parkingSpaces[parque].stateToOccuped()
                            if parkingSpaces[parque].isOccuped():
                                print("Parking "+ str(parque) +" Ocupied\n");
                            else:
                                print("Error setting parking 1 to Ocupied")
                        elif "Livre" in answer:
                            parkingSpaces[parque].stateToEmpty()
                            if parkingSpaces[parque].isEmpty():
                                print("Parking "+ str(parque) +" Empty\n");
                            else:
                                print("Error setting parking 1 to Empty")
                        arduino.flushInput() #remove data after reading
            except KeyboardInterrupt:
                print("KeyboardInterrupt has been caught.")

class Server(BaseHTTPRequestHandler):
    

    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def _send_parking_info(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(serializeParkingSpaces().encode('utf-8'))

    def _send_space_info(self,spaceId):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(serializeParkingSpaces(space=spaceId).encode('utf-8'))

    def do_GET(self):
        url = urlparse(self.path)
        if url.path == "/parkingInfo":
            self._send_parking_info()
            return
        elif url.path== "/parkingSpaceInfo":
            spaceId = int(parse_qs(url.query)['id'][0])
            self._send_space_info(spaceId)
            return 
        else:
            self.send_error(404)
        print("GET request \nPath: %s\nHeaders: %s\n" % (str(self.path),str(self.headers)))
        self._set_response()
        self.wfile.write("GET request for {}".format(self.path).encode('utf-8'))
    

    def _reserve_space(self):
        content_length= int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        data = json.loads(post_data)

        id_space = int(data['id'])
        license_plate = data['license_plate']
        secret_code = data['secret_code']
        #notification adicionar depois
        
        if (parkingSpaces[id_space].reserveSpace(license_plate,secret_code)):
            print("Reserved %d with success\n" % id_space)
            #send_command(str(id_space)+"reserved")
            arduino.write(str(id_space).encode())
            #while arduino.inWaiting()==0: pass
            #answer=arduino.readline()
            #answer=answer.decode('utf-8');
            #print(answer)
            self._set_response()
        else:
            self.send_error(400,"Place not available for reservation")
    def send_command(command):
        arduino.write(command.encode())  # Send the command to the Arduino
        response = arduino.readline().decode().strip()  # Read the response from Arduino
        print("Received response:", response)
    def _cancel_reservation(self):
        content_length= int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        data = json.loads(post_data)

        id_space = int(data['id'])
        secret_code = data['secret_code']
        
        if (parkingSpaces[id_space].cancelReservation(secret_code)):
            print("Cancelled %d with success\n" % id_space)
            self._set_response()
        else:
            if not parkingSpaces[id_space].isReserved():
                self.send_error(400,"Cant cancel a place that isn't reserved")
            else:
                self.send_error(400,"Wrong code")

    # Usado para reservar um lugar
    # Json com id do lugar mais matricula mais booleano se quer receber notificações 
    def do_POST(self):
        if self.path == "/reserve":
            self._reserve_space()
            return
        elif self.path == "/cancel":
            self._cancel_reservation()
            return
        else:
            self.send_error(404)
            return
        #print("POST request \nPath: %s\nHeaders: %s\n" % (str(self.path),str(self.headers)))
        #self._set_response()
        #self.wfile.write("GET request for {}".format(self.path).encode('utf-8'))



def initializeParkingSpaces(n):
    parkingSpaces.append(None)
    for i in range(1,n+1):
        p = ParkingSpace(i)
        parkingSpaces.append(p)

def serializeParkingSpaces(space=None):
    if (space != None):
        #if (space > 0 and space <= numberOfSpaces)
        serializedMessage = json.dumps(parkingSpaces[space],cls=ParkingSpaceEncoder)
    else:
        serializedMessage = json.dumps(parkingSpaces[1:],cls=ParkingSpaceEncoder) # 0 é Null
    return serializedMessage

if __name__ == "__main__":
    thread1 = thread("Thread-Arduino", 1)
    thread1.start()
    webServer = HTTPServer( (host,port), Server)
    print("Server started at http://%s:%s" % (host,port))

    initializeParkingSpaces(numberOfSpaces)

    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Server stopped")
