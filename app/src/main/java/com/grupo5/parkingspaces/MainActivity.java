package com.grupo5.parkingspaces;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView park1Status;
    private TextView park2Status;
    private Button park1Button;
    private Button park2Button;

    private boolean isPark1Occupied;
    private boolean isPark2Occupied;

    private String park1User;
    private String park2User;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        park1Status = findViewById(R.id.park1Status);
        park2Status = findViewById(R.id.park2Status);
        park1Button = findViewById(R.id.park1Button);
        park2Button = findViewById(R.id.park2Button);

        // Set initial status
        updateParkStatus();

        park1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPark1Occupied) {
                    // Park 1 is occupied, so release it
                    isPark1Occupied = false;
                    park1User = null;
                } else {
                    // Park 1 is free, so reserve it
                    showReservationDialog(1);
                }
                updateParkStatus();
            }
        });

        park2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPark2Occupied) {
                    // Park 2 is occupied, so release it
                    isPark2Occupied = false;
                    park2User = null;
                } else {
                    // Park 2 is free, so reserve it
                    showReservationDialog(2);
                }
                updateParkStatus();
            }
        });
    }

    private void showReservationDialog(final int parkNumber) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reservation, null);
        dialogBuilder.setView(dialogView);

        final EditText nameEditText = dialogView.findViewById(R.id.nameEditText);

        dialogBuilder.setTitle("Park " + parkNumber + " Reservation");
        dialogBuilder.setPositiveButton("Reserve", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String userName = nameEditText.getText().toString();
                if (!userName.isEmpty()) {
                    if (parkNumber == 1) {
                        isPark1Occupied = true;
                        park1User = userName;
                    } else if (parkNumber == 2) {
                        isPark2Occupied = true;
                        park2User = userName;
                    }
                    updateParkStatus();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateParkStatus() {
        if (isPark1Occupied) {
            park1Status.setText("Occupied by " + park1User);
            park1Button.setText("Release Park 1");
        } else {
            park1Status.setText("Free");
            park1Button.setText("Reserve Park 1");
        }

        if (isPark2Occupied) {
            park2Status.setText("Occupied by " + park2User);
            park2Button.setText("Release Park 2");
        } else {
            park2Status.setText("Free");
            park2Button.setText("Reserve Park 2");
        }
    }
}
