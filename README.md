# Parking Spaces

The aim is to manage a parking lot.

The objective is to have automatic detection of cars in parking spaces, with a light indication of occupied (red) or free (green). This occupation is to be visualized also on a smartphone. The detection of occupancy should be done by pressure and by infrared (different sensors in different spots).

The system will incorporate a fire alarm (based on temperature and/or light). When a fire is detected the smartphone should be warned and a alarm should sound on the parking site.

On the smartphone it should be possible to reserve a place for parking. This is to be indicated by a different light.

A central system should show the occupancy of the parking site with the license plate for cars that have reserved places (this will be entered in the smartphone).

## Requirements:

- support more than one smartphone
    syncing of reservations between smartphones should be in less than 1 sec
- detection of car presence should be in less than 1 sec since arrival
- detection of car absence should be in less than 2 sec since departure
- indication of change in occupancy in smartphone should be in less than 2 sec 
    - same for fire warning
- fire detection should be in less than 1 sec
- indication on parking space (light on) for reservation should be in less than 1 sec

## Demo
A demo video is available at [youtube](https://youtu.be/LWcwwDz1TOc).

