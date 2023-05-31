import json
from json import JSONEncoder

#Falta lógica de transições
class ParkingSpace:
    def __init__(self,id):
        self.id = id
        self.state = "Empty" #Pode ser Empty, Reserved, Occupied
        self.licensePlate = None
        self.secretCode = None
        self.fire = False

    def isEmpty(self):
        return self.state == "Empty"

    def isReserved(self):
        return self.state == "Reserved"

    def isOccupied(self):
        return self.state == "Occupied"

    def fireTrue(self):
        self.fire = True

    def fireFalse(self):
        self.fire = False

    def fireState(self):
        if (self.fire):
            return True
        return False

    def stateToEmpty(self):
        self.state = "Empty"

    def stateToReserved(self):
        self.state = "Reserved"

    def stateToOccupied(self):
        self.state = "Occupied"

    def reserveSpace(self,license_plate,secret_code):
        if (self.isEmpty()):
            self.stateToReserved()
            self.licensePlate = license_plate
            self.secretCode = secret_code
            return True
        else:
            print("Cant reserve because state isn't empty")
            return False

    def cancelReservation(self,code):
        if not self.isReserved():
            print("Place isnt reserved so cant be cancelled")
            return False
        else:
            if (code != self.secretCode):
                print("Wrong code")
                return False
            else:
                self.stateToEmpty()
                self.licensePlate = None
                self.secretCode = None
                print("Cancelled with success")
                return True

class ParkingSpaceEncoder(JSONEncoder):
    def default(self,o):
        d = o.__dict__.copy()
        del d['secretCode']
        return d
