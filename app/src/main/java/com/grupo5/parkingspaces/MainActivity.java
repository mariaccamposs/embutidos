package com.grupo5.parkingspaces;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;

    private String reserveUrl = "http://192.168.31.112:5001/reserve";
    private String parkingInfoUrl = "http://192.168.31.112:5001/parkingInfo";

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
        new RetrieveParkingInfoTask().execute();
    }

    public void reserveParkingSpace1(View view) {
        String id = "1";
        String secretCode = "600";
        new ReserveParkingSpaceTask().execute(id, secretCode);
    }

    public void reserveParkingSpace2(View view) {
        String id = "2";
        String secretCode = "600";
        new ReserveParkingSpaceTask().execute(id, secretCode);
    }

    private class RetrieveParkingInfoTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(parkingInfoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String response) {
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
    }

    private class ReserveParkingSpaceTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String id = params[0];
                String secretCode = params[1];

                URL url = new URL(reserveUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("id", id);
                jsonParams.put("secret_code", secretCode);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(jsonParams.toString());
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                connection.disconnect();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Parking space reserved successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to reserve parking space.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
