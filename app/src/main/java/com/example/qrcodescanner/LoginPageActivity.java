package com.example.qrcodescanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginPageActivity extends AppCompatActivity {
    Button logbutton;
    int mstatusCode;
    RequestQueue network_queue;
    SharedPreferences.Editor shared_pref_editor;
    SharedPreferences srPref;
    EditText text_nam;
    EditText text_pas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        setViews();
        network_queue = Volley.newRequestQueue(this);
    }

    private void setViews() {
        logbutton = (Button) findViewById(R.id.log_in_button);
        srPref = getSharedPreferences("QRAPP PREFERENCES", 0);
        text_nam = (EditText) findViewById(R.id.text_name);
        text_pas = (EditText) findViewById(R.id.text_pass);
        shared_pref_editor = srPref.edit();
        if (srPref.contains("user_name") && srPref.contains("password")) {
            Log.e("App", srPref.getString("user_name", ""));
            Log.e("App", srPref.getString("password", ""));
            Intent intent = new Intent(getBaseContext(), ScanBarCodeActivity.class);
            intent.putExtra("user_name", srPref.getString("user_name", ""));
            intent.putExtra("password", srPref.getString("password", ""));
            startActivity(new Intent(getApplicationContext(), ScanBarCodeActivity.class));
            finish();
        }
        logbutton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                verif_user_name_password(text_nam.getText().toString(), text_pas.getText().toString());
            }
        });
    }

    private void verif_user_name_password(String a, String b) {
        Toast.makeText(getApplicationContext(), "Logging IN", Toast.LENGTH_SHORT).show();
        String url = "http://autobutler.no/dekkhotell/employee/index.php";
        Log.e("url",url);

        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                shared_pref_editor.putString("user_name", text_nam.getText().toString());
                shared_pref_editor.putString("password", text_pas.getText().toString());
                shared_pref_editor.commit();
                Log.e("App", srPref.getString("user_name", ""));
                Intent intent = new Intent(getBaseContext(), ScanBarCodeActivity.class);
                intent.putExtra("user_name", srPref.getString("user_name", ""));
                intent.putExtra("password", srPref.getString("password", ""));
                startActivity(new Intent(getApplicationContext(), ScanBarCodeActivity.class));
                finish();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse.statusCode == 403) {
                        runOnUiThread(new Runnable() {

                            public void run() {
                                Log.i("Status code", "Near toast");
                                Context applicationContext = getApplicationContext();
                                Toast.makeText(applicationContext, "Login Failed, " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Login Failed, Please check internet connection ", Toast.LENGTH_SHORT).show();
                }
                Context applicationContext = getApplicationContext();
                Toast.makeText(applicationContext, "Login Failed, " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("submit", a);
                params.put("username", a);
                params.put("pass", b);
                return params;
            }
        };
        network_queue.add(request);
    }
}
