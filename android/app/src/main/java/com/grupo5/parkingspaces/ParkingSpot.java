package com.grupo5.parkingspaces;

import android.widget.Button;

public class ParkingSpot {
    private final int id;
    private State state;
    private String licencePlate;
    private final Button button;

    public ParkingSpot(int id, State state, String licencePlate, Button button) {
        this.id = id;
        this.state = state;
        this.licencePlate = licencePlate;
        this.button = button;
    }

    public int getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }

    public Button getButton() {
        return button;
    }


}
