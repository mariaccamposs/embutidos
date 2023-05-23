package com.grupo5.parkingspaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://172.17.23.253:5001";
    private static final String PARKING_INFO_URL = BASE_URL + "/parkingInfo";
    private static final String RESERVE_URL = BASE_URL + "/reserve";
    private static final String CANCEL_URL = BASE_URL + "/cancel";

    private RequestQueue requestQueue;

    private Button parkingButton1;
    private Button parkingButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        parkingButton1 = findViewById(R.id.button_parking1);
        parkingButton2 = findViewById(R.id.button_parking2);

        parkingButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveParkingSpot(1);
            }
        });

        parkingButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveParkingSpot(2);
            }
        });

        getParkingInfo();
    }


    private void reserveParkingSpot(final int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Reservation");
        dialogBuilder.setMessage("Enter license plate and PIN:");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reserve, null);
        dialogBuilder.setView(dialogView);

        final LicensePinForm licensePinForm = new LicensePinForm(dialogView);

        dialogBuilder.setPositiveButton("Reserve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String licensePlate = licensePinForm.getLicensePlate();
                String pin = licensePinForm.getPin();

                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("id", id);
                    requestBody.put("license_plate", licensePlate);
                    requestBody.put("secret_code", pin);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RESERVE_URL, requestBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Handle successful reservation
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("ReservationError", error.toString());
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                requestQueue.add(request);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void getParkingInfo() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, PARKING_INFO_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<ParkingSpot> parkingSpots = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject parking = response.getJSONObject(i);
                                int id = parking.getInt("id");
                                String state = parking.getString("state");
                                String licensePlate = parking.getString("licensePlate");

                                ParkingSpot spot = new ParkingSpot(id, state, licensePlate);
                                parkingSpots.add(spot);
                            }

                            // Process the parking spots list as needed
                            updateParkingSpotsUI(parkingSpots);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ParkingInfoError", error.toString());
                    }
                });

        requestQueue.add(request);
    }

    private void updateParkingSpotsUI(List<ParkingSpot> parkingSpots) {
        for (final ParkingSpot spot : parkingSpots) {
            final int id = spot.getId();
            String state = spot.getState();
            String licensePlate = spot.getLicensePlate();

            Button parkingButton;
            if (id == 1) {
                parkingButton = parkingButton1;
            } else if (id == 2) {
                parkingButton = parkingButton2;
            } else {
                continue;
            }

            parkingButton.setText("Parking Space " + id);

            if (state.equals("Empty")) {
                parkingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showReserveDialog(id);
                    }
                });
            } else if (state.equals("Occupied")) {
                parkingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showReleaseDialog(id);
                    }
                });
            }
        }
    }

    private void showReserveDialog(final int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Reservation");
        dialogBuilder.setMessage("Enter license plate and PIN:");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reserve, null);
        dialogBuilder.setView(dialogView);

        final LicensePinForm licensePinForm = new LicensePinForm(dialogView);

        dialogBuilder.setPositiveButton("Reserve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String licensePlate = licensePinForm.getLicensePlate();
                String pin = licensePinForm.getPin();

                // Perform reservation logic with the entered data
                // Send the reservation request to the server
                sendReservationRequest(id, licensePlate, pin);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showReleaseDialog(final int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Release Spot");
        dialogBuilder.setMessage("Enter PIN to release the spot:");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_release, null);
        dialogBuilder.setView(dialogView);

        final PinForm pinForm = new PinForm(dialogView);

        dialogBuilder.setPositiveButton("Release", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pin = pinForm.getPin();

                // Perform spot release logic with the entered PIN
                // Send the spot release request to the server
                sendSpotReleaseRequest(id, pin);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void sendReservationRequest(int id, String licensePlate, String pin) {
        String url = this.RESERVE_URL;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", id);
            requestBody.put("license_plate", licensePlate);
            requestBody.put("secret_code", pin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful reservation response
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ReservationError", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void sendSpotReleaseRequest(int id, String pin) {
        String url = this.CANCEL_URL;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", id);
            requestBody.put("secret_code", pin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful spot release response
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ReleaseError", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }


    private class ParkingSpot {
        private int id;
        private String state;
        private String licensePlate;

        public ParkingSpot(int id, String state, String licensePlate) {
            this.id = id;
            this.state = state;
            this.licensePlate = licensePlate;
        }

        public int getId() {
            return id;
        }

        public String getState() {
            return state;
        }

        public String getLicensePlate() {
            return licensePlate;
        }
    }


    private void releaseParkingSpot(final int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Release Spot");
        dialogBuilder.setMessage("Enter PIN to release the spot:");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_release, null);
        dialogBuilder.setView(dialogView);

        final PinForm pinForm = new PinForm(dialogView);

        dialogBuilder.setPositiveButton("Release", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pin = pinForm.getPin();

                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("id", id);
                    requestBody.put("secret_code", pin);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, CANCEL_URL, requestBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Handle successful spot release
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("ReleaseError", error.toString());
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                requestQueue.add(request);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private static class LicensePinForm {
        private final View rootView;
        private final TextInputEditText licensePlateEditText;
        private final TextInputEditText pinEditText;

        public LicensePinForm(View rootView) {
            this.rootView = rootView;
            this.licensePlateEditText = rootView.findViewById(R.id.edit_text_license_plate);
            this.pinEditText = rootView.findViewById(R.id.edit_text_pin);
        }

        public String getLicensePlate() {
            return licensePlateEditText.getText().toString();
        }

        public String getPin() {
            return pinEditText.getText().toString();
        }
    }

    private static class PinForm {
        private final View rootView;
        private final TextInputEditText pinEditText;

        public PinForm(View rootView) {
            this.rootView = rootView;
            this.pinEditText = rootView.findViewById(R.id.edit_text_pin);
        }

        public String getPin() {
            return pinEditText.getText().toString();
        }
    }
}
