package com.grupo5.parkingspaces;

import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ReleaseForm {
    private final TextInputEditText pinEditText;

    public ReleaseForm(View rootView) {
        this.pinEditText = rootView.findViewById(R.id.edit_text_pin);
    }

    public String getPin() {
        return Objects.requireNonNull(pinEditText.getText()).toString();
    }
}
