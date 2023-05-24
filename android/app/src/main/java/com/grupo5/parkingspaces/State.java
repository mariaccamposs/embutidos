package com.grupo5.parkingspaces;

public enum State {
    EMPTY,
    OCCUPIED,
    RESERVED;

    public static State convertFromString(String state) {
        switch (state) {
            case "Empty":
                return State.EMPTY;
            case "Reserved":
                return State.RESERVED;
            default:
                return State.OCCUPIED;
        }
    }
}



