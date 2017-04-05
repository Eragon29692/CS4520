package edu.neu.madcourse.duyvu.communication.fcm;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import edu.neu.madcourse.duyvu.R;


public class FCMActivity extends AppCompatActivity {

    private static final String TAG = FCMActivity.class.getSimpleName();

    // Please add the server key from your firebase console in the follwoing format "key=<serverKey>"
    private static final String SERVER_KEY = "key=AAAA1Kag1ys:APA91bE-nqz-wP59douQ8urLOBESoc_qlcPhyRV8mcxKhwi6l5JfpEL2pn2MeVt00Wc_IJrkJpYtqBc7EObhj27FjURFe6JjSfVE-Ni0b9cs5a5U9tHlri5EMBCyohvv68U5xl4TKUl0";

    // This is the client registration token
    private static final String CLIENT_REGISTRATION_TOKEN = "c1lD9Ilud4k:APA91bEDA3Dcp_MUzR0c6kUW77LICg_nu9SLvhtWmkDzIZVzmoD22BolzmeMmR9Y5ptD6sChiE3iegUCUSE2mQXheeKU1Vx_1M935ftK53RlfRwqvhm_l-CtqaYxNPToF2rrYg9Gpsdc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_fcm);


        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = "toekn: " + token;
                Log.d(TAG, msg);
                Toast.makeText(FCMActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pushNotification(View type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pushNotification();
            }
        }).start();
    }

    private void pushNotification() {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
        try {
            jNotification.put("title", "Google I/O 2016");
            jNotification.put("body", "Firebase Cloud Messaging (App)");
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");
            jNotification.put("click_action", "OPEN_ACTIVITY_1");

            // If sending to a single client
            jPayload.put("to", CLIENT_REGISTRATION_TOKEN);

            /*
            // If sending to multiple clients (must be more than 1 and less than 1000)
            JSONArray ja = new JSONArray();
            ja.put(CLIENT_REGISTRATION_TOKEN);
            // Add Other client tokens
            ja.put(FirebaseInstanceId.getInstance().getToken());
            jPayload.put("registration_ids", ja);
            */

            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: " + resp);
                    Toast.makeText(FCMActivity.this,resp,Toast.LENGTH_LONG);
                }
            });
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }
}
