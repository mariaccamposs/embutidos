package com.grupo5.parkingspaces;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.31.112:5001";
    private static final String PARKING_INFO_URL = BASE_URL + "/parkingInfo";
    private static final String RESERVE_URL = BASE_URL + "/reserve";
    private static final String CANCEL_URL = BASE_URL + "/cancel";
    private static final String FIRE_URL = BASE_URL + "/fireStatus";

    private static final int CALL_SERVER_INTERVAL = 500;
    private static final int CALL_SERVER_FIRE_INTERVAL = 1500;
    private Handler handler;
    private Runnable runnableParkingStatus;
    private Runnable runnableFireStatus;
    private boolean firePopUpSent = false;
    private RequestQueue requestQueue;

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnableParkingStatus, CALL_SERVER_INTERVAL);
        handler.postDelayed(runnableFireStatus, CALL_SERVER_FIRE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableParkingStatus);
        handler.removeCallbacks(runnableFireStatus);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        Button parkingButton1 = findViewById(R.id.button_parking1);
        Button parkingButton2 = findViewById(R.id.button_parking2);

        Map<Integer, ParkingSpot> parkingSpacesMap = new HashMap<>();

        ParkingSpot parkingSpot1 = new ParkingSpot(1, State.OCCUPIED, "0", parkingButton1);
        ParkingSpot parkingSpot2 = new ParkingSpot(2, State.OCCUPIED, "0", parkingButton2);

        parkingSpacesMap.put(parkingSpot1.getId(), parkingSpot1);
        parkingSpacesMap.put(parkingSpot2.getId(), parkingSpot2);

        handler = new Handler(Looper.getMainLooper());

        runnableParkingStatus = new Runnable() {
            @Override
            public void run() {
                // Call the method to update parking spots UI
                updateParkingSpotsUI(parkingSpacesMap);

                // Schedule the next execution after a specified delay
                handler.postDelayed(this, CALL_SERVER_INTERVAL);
            }
        };

        runnableFireStatus = new Runnable() {
            @Override
            public void run() {
                checkFireState();

                handler.postDelayed(this, CALL_SERVER_FIRE_INTERVAL);
            }
        };

    }

    private void checkFireState() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, FIRE_URL, null,
                response -> {
                    try {
                        // Parse the JSON response and get the "result" field
                        String result = response.getString("fire");

                        if (result.equals("true")) {
                            if (!firePopUpSent) {
                                showPopupMessage("FIRE");
                                firePopUpSent = true;
                            }
                        } else {
                            firePopUpSent = false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void updateParkingSpotsUI(Map<Integer, ParkingSpot> parkingSpacesMap) {
        getParkingInfo(parkingSpacesMap);

        parkingSpacesMap.forEach((k, v) ->
        {
            State state = v.getState();
            Button button = v.getButton();

            switch (state) {
                case OCCUPIED:
                    button.setEnabled(false);
                    button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_red_300)));
                    break;
                case RESERVED:
                    button.setText(v.getLicencePlate());
                    button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_yellow_300)));
                    button.setEnabled(true);
                    break;
                default:

                    switch (v.getId()) {
                        case 1:
                            button.setText("Parking Space 1");
                            break;
                        case 2:
                            button.setText("Parking Space 2");
                            break;
                        default:
                            break;
                    }
                    button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_green_300)));
                    button.setEnabled(true);
            }

            button.setOnClickListener(view -> {
                switch (state) {
                    case EMPTY:
                        showReserveDialog(v);
                        break;
                    case RESERVED:
                        showReleaseDialog(v);
                        break;
                    case OCCUPIED:
                        break;
                }
            });
        });
    }

    private void showReserveDialog(ParkingSpot v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Reservation");
        dialogBuilder.setMessage("Enter license plate and PIN:");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reserve, null);
        dialogBuilder.setView(dialogView);
        final ReserveForm reserveForm = new ReserveForm(dialogView);

        dialogBuilder.setPositiveButton("Reserve", (dialog, which) -> {
            String licensePlate = reserveForm.getLicensePlate();
            String pin = reserveForm.getPin();

            // Perform reservation logic with the entered data
            // Send the reservation request to the server
            sendReservationRequest(v.getId(), licensePlate, pin);
        });

        dialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void sendReservationRequest(int id, String licensePlate, String pin) {

        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("id", id);
            requestBody.put("license_plate", licensePlate);
            requestBody.put("secret_code", pin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RESERVE_URL, requestBody,
                response -> {
                    // Handle successful reservation response
                },
                error -> Log.e("ReservationError", error.toString())) {
        };

        requestQueue.add(request);
    }

    private void getParkingInfo(Map<Integer, ParkingSpot> parkingSpacesMap) {

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, PARKING_INFO_URL, null,
                response -> {
                    try {

                        for (int i = 0; i < 2; i++) {

                            JSONObject parking = response.getJSONObject(i);

                            int id = parking.getInt("id");
                            ParkingSpot cur = parkingSpacesMap.get(id);

                            if (!parking.isNull("state")) {
                                String state = parking.getString("state");
                                assert cur != null;
                                cur.setState(State.convertFromString(state));
                            }

                            if (!parking.isNull("licensePlate")) {
                                String licensePlate = parking.getString("licensePlate");
                                assert cur != null;
                                cur.setLicencePlate(licensePlate);
                            }

                            parkingSpacesMap.replace(id, cur);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("ParkingInfoError", error.toString()));

        requestQueue.add(request);
    }

    private void sendSpotReleaseRequest(int id, String pin) {

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", id);
            requestBody.put("secret_code", pin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, CANCEL_URL, requestBody,
                response -> {
                    // Handle successful spot release response
                },
                error -> {
                    if (error instanceof ClientError && error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        // Error with status code 400 (Bad Request) is received
                        showErrorPopUp("WRONG PIN");
                    } else {
                        Log.e("ReleaseError", error.toString());
                    }
                });

        requestQueue.add(request);
    }

    private void showReleaseDialog(final ParkingSpot parkingSpot) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Release Spot");
        dialogBuilder.setMessage("Enter PIN to release the spot:");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_release, null);
        dialogBuilder.setView(dialogView);

        final ReleaseForm releaseForm = new ReleaseForm(dialogView);

        dialogBuilder.setPositiveButton("Release", (dialog, which) -> {
            String pin = releaseForm.getPin();

            // Perform spot release logic with the entered PIN
            // Send the spot release request to the server
            sendSpotReleaseRequest(parkingSpot.getId(), pin);
        });

        dialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showPopupMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, id) -> firePopUpSent = false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showErrorPopUp(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }


}
