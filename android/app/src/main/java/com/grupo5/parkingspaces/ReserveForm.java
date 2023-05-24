package com.grupo5.parkingspaces;

import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ReserveForm {

    private final TextInputEditText licensePlateEditText;
    private final TextInputEditText pinEditText;

    public ReserveForm(View view) {
        this.licensePlateEditText = view.findViewById(R.id.edit_text_license_plate);
        this.pinEditText = view.findViewById(R.id.edit_text_pin);
    }

    public String getLicensePlate() {
        return Objects.requireNonNull(licensePlateEditText.getText()).toString();
    }

    public String getPin() {
        return Objects.requireNonNull(pinEditText.getText()).toString();
    }
}
