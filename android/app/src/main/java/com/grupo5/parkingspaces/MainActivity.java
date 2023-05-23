package com.grupo5.parkingspaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;

    private final String reserveUrl = "http://192.168.31.112:5001/reserve";
    private final String parkingInfoUrl = "http://192.168.31.112:5001/parkingInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        // Disable the buttons initially
        button1.setEnabled(false);
        button2.setEnabled(false);

        // Retrieve parking info and enable buttons if space is available
        retrieveParkingInfo();
    }

    public void reserveParkingSpace1(View view) {
        showReservationDialog("1");
    }

    public void reserveParkingSpace2(View view) {
        showReservationDialog("2");
    }

    private void retrieveParkingInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(parkingInfoUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleParkingInfoResponse(response.toString());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleParkingInfoResponse(String response) {
        try {
            JSONArray parkingInfoArray = new JSONArray(response);

            for (int i = 0; i < parkingInfoArray.length(); i++) {
                JSONObject parkingSpace = parkingInfoArray.getJSONObject(i);
                int id = parkingSpace.getInt("id");
                String state = parkingSpace.getString("state");

                if (id == 1 && state.equals("Empty")) {
                    button1.setEnabled(true);
                } else if (id == 2 && state.equals("Empty")) {
                    button2.setEnabled(true);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showReservationDialog(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reservation");
        builder.setMessage("Enter your name and PIN");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        final EditText nameInput = dialogView.findViewById(R.id.nameInput);
        final EditText pinInput = dialogView.findViewById(R.id.pinInput);

        builder.setView(dialogView);
        builder.setPositiveButton("Reserve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = nameInput.getText().toString();
                String pin = pinInput.getText().toString();
                if (!name.isEmpty() && !pin.isEmpty()) {
                    reserveParkingSpace(id, name, pin);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter your name and PIN", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void reserveParkingSpace(final String id, final String name, final String pin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(reserveUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("id", id);
                    jsonParams.put("secret_code", pin);

                    connection.getOutputStream().write(jsonParams.toString().getBytes());

                    int responseCode = connection.getResponseCode();
                    connection.disconnect();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                Toast.makeText(MainActivity.this, "Parking space reserved successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to reserve parking space.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
