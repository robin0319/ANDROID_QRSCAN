package com.example.qrcodescanner;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.internal.view.SupportMenu;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class ScanBarCodeActivity<mHandler> extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    static int SUMMER = 1;
    static int WINTER = 2;
    final String BACK_TYRE_SIZE = "back_tyre_size";
    final String BRAND_NAME = "brand_name";
    final String FRONT_TYRE_SIZE = "front_tyre_size";
    final String LB_DETAILS = "lb_details";
    final String LB_DIMENSION = "lb_dimension";
    final String LF_DETAILS = "lf_details";
    final String LF_DIMENSION = "lf_dimension";
    final String RB_DETAILS = "rb_details";
    final String RB_DIMENSION = "rb_dimension";
    final String RF_DETAILS = "rf_details";
    final String RF_DIMENSION = "rf_dimension";
    final String SEASON = "season";
    final String SUMMER_S = "summer";
    final String WINTER_S = "winter";
    private BarcodeDetector barcodeDetector;
    Button btnAction;
    private CameraSource cameraSource;
    int count;
    String employeeid;
    String intentData = "";
    String location = "";
    int lastSeason = SUMMER;
    PopupWindow mPopupWindow;
    int mStatusCode;
    RequestQueue network_queue;
    AtomicBoolean popupshown;
    SendCase send_case = SendCase.CAR;
    SharedPreferences srpf;
    SharedPreferences.Editor srpfEd;
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    String tyre_id;

    public enum SendCase {
        CAR,
        TYRE,
        ORG
    }

    public void createtyresInfoUpdatePopup() {
        final HashMap hashMap = new HashMap();
        final View inflate = LayoutInflater.from(this).inflate(R.layout.tyre_info_input_popup, (ViewGroup) null);
        updateTyreDetailsFromServer(inflate);
        final RadioGroup radioGroup = (RadioGroup) inflate.findViewById(R.id.buttonGroup);
        ((Button) inflate.findViewById(R.id.update_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                hashMap.put("rf_dimension", ((EditText) inflate.findViewById(R.id.rf_dimension)).getText().toString());
                hashMap.put("rf_details", ((EditText) inflate.findViewById(R.id.rf_details)).getText().toString());
                hashMap.put("rb_dimension", ((EditText) inflate.findViewById(R.id.rb_dimension)).getText().toString());
                hashMap.put("rb_details", ((EditText) inflate.findViewById(R.id.rb_details)).getText().toString());
                hashMap.put("lf_dimension", ((EditText) inflate.findViewById(R.id.lf_dimension)).getText().toString());
                hashMap.put("lf_details", ((EditText) inflate.findViewById(R.id.lf_details)).getText().toString());
                hashMap.put("lb_dimension", ((EditText) inflate.findViewById(R.id.lb_dimension)).getText().toString());
                hashMap.put("lb_details", ((EditText) inflate.findViewById(R.id.lb_details)).getText().toString());
                hashMap.put("front_tyre_size", ((EditText) inflate.findViewById(R.id.frontwheel_size_input)).getText().toString());
                hashMap.put("back_tyre_size", ((EditText) inflate.findViewById(R.id.backwheel_size_input)).getText().toString());
                hashMap.put("brand_name", ((EditText) inflate.findViewById(R.id.brandane__input)).getText().toString());
                if (radioGroup.getCheckedRadioButtonId() == R.id.summer_button) {
                    hashMap.put("season", "summer");
                } else {
                    hashMap.put("season", "winter");
                }
                updatetyreSpecification(hashMap);
                mPopupWindow.dismiss();
                popupshown.set(false);
            }
        });
        mPopupWindow = new PopupWindow(inflate, -2, -2);
        mPopupWindow.showAtLocation(surfaceView, 17, 0, 0);
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        ((Button) inflate.findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                txtBarcodeValue.setTextColor(-16711936);
                txtBarcodeValue.setText("Read again");
                mPopupWindow.dismiss();
                try {
                    Toast.makeText(getApplicationContext(), "Restarting Detector", Toast.LENGTH_SHORT).show();
                    popupshown.set(false);
                } catch (Exception e) {
                    Log.e("Popup close", "Failed Restaring the camera source" + e.toString());
                }
                Log.e("Popup", "Inside button cancel, tried closing");
            }
        });
    }

    //updateTyreDetailsFromServer
    public void updateTyreDetailsFromServer(final View view) {
        String str = "http://autobutler.no/dekkhotell/employee/qr_tyre_details.php?job=query&tyre_id=" + tyre_id;
        network_queue.add(new JsonObjectRequest(0, str, null, new Response.Listener<JSONObject>() {

            public void onResponse(final JSONObject jSONObject) {
                try {
                    if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("1")) {
                        runOnUiThread(new Runnable() {

                            public void run() {
                                EditText editText = (EditText) view.findViewById(R.id.rf_details);
                                EditText editText2 = (EditText) view.findViewById(R.id.rf_dimension);
                                EditText editText3 = (EditText) view.findViewById(R.id.lf_details);
                                EditText editText4 = (EditText) view.findViewById(R.id.lf_dimension);
                                EditText editText5 = (EditText) view.findViewById(R.id.rb_details);
                                EditText editText6 = (EditText) view.findViewById(R.id.rb_dimension);
                                EditText editText7 = (EditText) view.findViewById(R.id.lb_details);
                                EditText editText8 = (EditText) view.findViewById(R.id.lb_dimension);
                                EditText editText9 = (EditText) view.findViewById(R.id.frontwheel_size_input);
                                EditText editText10 = (EditText) view.findViewById(R.id.backwheel_size_input);
                                EditText editText11 = (EditText) view.findViewById(R.id.brandane__input);
                                RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.buttonGroup);
                                try {
                                    editText.setText(jSONObject.getString("rf_details"));
                                    editText2.setText(jSONObject.getString("rf_dimension"));
                                    editText3.setText(jSONObject.getString("lf_details"));
                                    editText4.setText(jSONObject.getString("lf_dimension"));
                                    editText5.setText(jSONObject.getString("rb_details"));
                                    editText6.setText(jSONObject.getString("rb_dimension"));
                                    editText7.setText(jSONObject.getString("lb_details"));
                                    editText8.setText(jSONObject.getString("lb_dimension"));
                                    editText9.setText(jSONObject.getString("front_tyre_size"));
                                    editText10.setText(jSONObject.getString("back_tyre_size"));
                                    editText11.setText(jSONObject.getString("brand_name"));
                                    if (jSONObject.getString("season").equals("summer")) {
                                        radioGroup.check(R.id.summer_button);
                                    } else if (jSONObject.getString("season").equals("winter")) {
                                        radioGroup.check(R.id.winter_button);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ScanBarCodeActivity.this, "Couldnt fetch tyre details from server", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override 
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(ScanBarCodeActivity.this, "Failed while fetching tyre details from server", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    //update tyre specification
    public void updatetyreSpecification(final Map<String, String> map) {
        String str = "http://autobutler.no/dekkhotell/employee/qr_tyre_details.php?job=update&tyre_id=" + tyre_id + "&regNr=" + intentData + "&location=" + location;
        for (String str2 : map.keySet()) {
            str = str + "&" + str2 + "=" + map.get(str2);
        }
        Toast.makeText(this, "Updating the car specification", Toast.LENGTH_SHORT).show();

        network_queue.add(new JsonObjectRequest(0, str, null, new Response.Listener<JSONObject>() {

            public void onResponse(JSONObject jSONObject) {
                try {
                    if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("1")) {
                        Toast.makeText(ScanBarCodeActivity.this, "Successfully Updated the tyre Details", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(ScanBarCodeActivity.this, "Failed Updating Tyre Details to Server", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override 
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(ScanBarCodeActivity.this, "Error on updating to the server", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override 
            public Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        });
    }

    //Location QrCode popup
    public void createCarDeliveryConfirmPopup() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Location Selected. Please scan reg nr");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        popupshown.set(false);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    //change season
    public void createTyrePickupConfirmPopup() {
        Toast.makeText(this, "TYRE SELECTED", Toast.LENGTH_LONG).show();
        View inflate = LayoutInflater.from(this).inflate(R.layout.general_popup_withseason, (ViewGroup) null);
        final RadioGroup radioGroup = (RadioGroup) inflate.findViewById(R.id.buttonGroup);
        if (lastSeason == SUMMER) {
            radioGroup.check(R.id.summer_button);
        } else {
            radioGroup.check(R.id.winter_button);
        }
        ((Button) inflate.findViewById(R.id.submit_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.summer_button) {
                    lastSeason = ScanBarCodeActivity.SUMMER;
                } else {
                    lastSeason = ScanBarCodeActivity.WINTER;
                }
                srpfEd.putInt("last_season", lastSeason);
                mPopupWindow.dismiss();
                popupshown.set(false);
                submit_tyre_details(lastSeason);
            }
        });
        ((Button) inflate.findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mPopupWindow.dismiss();
                txtBarcodeValue.setTextColor(-16711936);
                txtBarcodeValue.setText("Read again");
                try {
                    Toast.makeText(getApplicationContext(), "Restarting Detector", Toast.LENGTH_SHORT).show();
                    popupshown.set(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mPopupWindow = new PopupWindow(inflate, -2, -2);
        mPopupWindow.showAtLocation(surfaceView, 17, 0, 0);
    }

    //update or tyreinfo update popup
    public void createTyreUpdateORPickupPopup() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.updation_or_regular_job_selector_popup, (ViewGroup) null);
        ((RadioGroup) inflate.findViewById(R.id.buttonGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == -1) {
                    return;
                }
                if (i == R.id.regularButton) {
                    mPopupWindow.dismiss();
                    createTyrePickupConfirmPopup();
                } else if (i == R.id.updateButton) {
                    mPopupWindow.dismiss();
                    createtyresInfoUpdatePopup();
                }
            }
        });
        mPopupWindow = new PopupWindow(inflate, -2, -2);
        mPopupWindow.showAtLocation(surfaceView, 17, 0, 0);
    }

    //org popup
    public void createOrgDeliveryConfirmPopup() {
        Toast.makeText(this, "ORG SELECTED", Toast.LENGTH_LONG).show();
        View inflate = LayoutInflater.from(this).inflate(R.layout.general_popup, (ViewGroup) null);
        ((Button) inflate.findViewById(R.id.submit_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mPopupWindow.dismiss();
                popupshown.set(false);
                submit_org_details();
            }
        });
        ((Button) inflate.findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mPopupWindow.dismiss();
                txtBarcodeValue.setTextColor(-16711936);
                txtBarcodeValue.setText("Read again");
                try {
                    Toast.makeText(getApplicationContext(), "Restarting Detector", Toast.LENGTH_SHORT).show();
                    popupshown.set(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mPopupWindow = new PopupWindow(inflate, -2, -2);
        mPopupWindow.showAtLocation(surfaceView, 17, 0, 0);
    }

    public static class SwitchClass {
        static final  int[] switchMap = new int[SendCase.values().length];

        static {
            try {
                switchMap[SendCase.TYRE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                switchMap[SendCase.CAR.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                switchMap[SendCase.ORG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    //Create popup when scanning QrCode
    public void create_popup() {
        int i = SwitchClass.switchMap[send_case.ordinal()];
        if (i == 1) {
            createTyreUpdateORPickupPopup();
        } else if (i == 2) {
            createCarDeliveryConfirmPopup();
        } else if (i == 3) {
            createOrgDeliveryConfirmPopup();
        }
    }

    
    //Org Details
    private void submit_org_details() {
        txtBarcodeValue.setTextColor(-16776961);
        txtBarcodeValue.setText("Updating Details");
        String str = "http://autobutler.no/dekkhotell/employee/qr_mass_tyre_delivery.php?method=update&employeeid=" + employeeid + "&orgNr=" + intentData;

        network_queue.add(new JsonObjectRequest(0, str, null, new Response.Listener<JSONObject>() {

            public void onResponse(JSONObject jSONObject) {
                try {
                    if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("1")) {
                        String str = (String) jSONObject.get("data");
                        txtBarcodeValue.setTextColor(-16711936);
                        txtBarcodeValue.setText("Successfully Updated,Read again");
                    } else if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("0")) {
                        Toast.makeText(getApplicationContext(), "Tyre Not in list", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(-16711936);
                        txtBarcodeValue.setText("Mass delivery update failed");
                    }
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override 
            public void onErrorResponse(VolleyError volleyError) {
                try {
                    if (volleyError.networkResponse.statusCode == 303) {
                        Toast.makeText(getApplicationContext(), "Error while updating", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                        txtBarcodeValue.setText("Failed to update");
                    }
                } catch (NullPointerException unused) {
                    Toast.makeText(getApplicationContext(), "Update failed, Please check internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }));
    }

    
    
    private void submit_car_details() {
        txtBarcodeValue.setTextColor(-16776961);
        txtBarcodeValue.setText("Updating Details");
        String str = "http://autobutler.no/dekkhotell/employee/qr_update_car_delivered.php?employeeid=" + employeeid + "&reg_nr=" + intentData;

        network_queue.add(new JsonObjectRequest(0, str, null, new Response.Listener<JSONObject>() {

            public void onResponse(JSONObject jSONObject) {
                try {
                    if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("1")) {
                        Toast.makeText(getApplicationContext(), "Car details updated", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(-16711936);
                        txtBarcodeValue.setText("Successfully Updated, Read Again");
                    } else if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("0")) {
                        Toast.makeText(getApplicationContext(), "Car Not in list", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(-16711936);
                        txtBarcodeValue.setText("Couldn't Update, Read Again");
                    }
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override 
            public void onErrorResponse(VolleyError volleyError) {
                try {
                    if (volleyError.networkResponse.statusCode == 403) {
                        Toast.makeText(getApplicationContext(), "Error while updating", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                        txtBarcodeValue.setText("Failed to update");
                    }
                } catch (NullPointerException unused) {
                    Toast.makeText(getApplicationContext(), "Update failed, Please check internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }));
    }

    
    //season update
    private void submit_tyre_details(int i) {
        txtBarcodeValue.setTextColor(-16776961);
        txtBarcodeValue.setText("Updating Details");
        String str;
        if (i == SUMMER) {
            str = "http://autobutler.no/dekkhotell/employee/qr_update_tyre.php?season=SUMMER&reg_nr=" + intentData + "&tyre_id=" + tyre_id + "&location=" + location;
        }else {
            str = "http://autobutler.no/dekkhotell/employee/qr_update_tyre.php?season=WINTER&reg_nr=" + intentData + "&tyre_id=" + tyre_id + "&location=" + location;
        }
        location = "";

        network_queue.add(new JsonObjectRequest(Request.Method.GET, str, null, new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                try {
                    if (response.get(NotificationCompat.CATEGORY_STATUS).equals("1")) {
                        Toast.makeText(getApplicationContext(), "Tyre details updated", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(-16711936);
                        txtBarcodeValue.setText("Successfully Updated, Read Again");
                    } else if (response.get(NotificationCompat.CATEGORY_STATUS).equals("0")) {
                        Toast.makeText(getApplicationContext(), "Tyre Not in list", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(-16711936);
                        txtBarcodeValue.setText("Couldn't Update, Read Again");
                    }
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse.statusCode == 303) {
                        Toast.makeText(getApplicationContext(), "Error while updating", Toast.LENGTH_SHORT).show();
                        txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                        txtBarcodeValue.setText("Failed to update");
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Update failed, Please check internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        }));

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    
    @Override 
    public void onCreate(Bundle bundle) {
        Log.i(toString(), "Activity created");
        super.onCreate(bundle);
        popupshown = new AtomicBoolean(false);
        setContentView((int) R.layout.activity_scan_bar_code);
        network_queue = Volley.newRequestQueue(this);
        initViews();
    }

    //Menu Bar
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.logout) {
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                        .clearApplicationUserData();
            }
            Toast.makeText(getApplicationContext(), "Logout success", Toast.LENGTH_SHORT).show();
            srpfEd.clear();
            srpfEd.commit();
            startActivity(new Intent(getBaseContext(), LoginPageActivity.class));
            finish();
            return true;

        }else if (menuItem.getItemId() == R.id.addTyre){
            startActivity(new Intent(getBaseContext(), AddTyreActivity.class));
        }
        return super.onOptionsItemSelected(menuItem);

    }

    private void initViews() {
        srpf = getSharedPreferences("QRAPP PREFERENCES", 0);
        srpfEd = srpf.edit();
        employeeid = srpf.getString("user_id", "0");
        lastSeason = srpf.getInt("last_season", SUMMER);
        txtBarcodeValue = (TextView) findViewById(R.id.txtBarcodeValue);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        count = 0;
    }

    private void initialiseDetectorsAndSources() {
        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(0).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080).setAutoFocusEnabled(true).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            }

            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanBarCodeActivity.this, "android.permission.CAMERA") == 0) {
                        cameraSource.start(surfaceView.getHolder());
                        return;
                    }
                    ActivityCompat.requestPermissions(ScanBarCodeActivity.this, new String[]{"android.permission.CAMERA"}, ScanBarCodeActivity.REQUEST_CAMERA_PERMISSION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override 
            public void release() {
            }

            @Override 
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> detectedItems = detections.getDetectedItems();
                if (detectedItems.size() != 0) {
                    if (verify_contents(detectedItems.valueAt(0).displayValue)) {
                        count ++;
                        if (!popupshown.get()) {
                            popupshown.set(true);
                            runOnUiThread(new Runnable() {

                                public void run() {
                                    create_popup();
                                }
                            });
                        }
                    }
                }
            }

            private boolean verify_contents(String str) {
                String[] split = str.split(":", -1);
                if (split.length < 3) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                            txtBarcodeValue.setText("WRONG READ");
                        }
                    });
                    return false;
                }

                if (split.length > 5){
                    location = split[4];
                }
                if (!split[0].replaceAll("\\s", "").equals("AUTOBUTLER")) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                            txtBarcodeValue.setText("WRONG READ");
                        }
                    });
                    return false;
                } else if (split[1].replaceAll("\\s", "").equals("CAR")) {
                    final String str2 = split[2];
                    runOnUiThread(new Runnable() {

                        public void run() {
                            txtBarcodeValue.setTextColor(-16711936);
                            TextView textView = txtBarcodeValue;
                            textView.setText("READ CAR : " + str2);
                        }
                    });
                    intentData = str2;
                    send_case = SendCase.CAR;
                    return true;
                } else if (split[1].replaceAll("\\s", "").equals("TYRE")) {
                    final String str3 = split[2];
                    runOnUiThread(new Runnable() {

                        public void run() {
                            txtBarcodeValue.setTextColor(-16711936);
                            TextView textView = txtBarcodeValue;
                            textView.setText("READ Tyre : " + str3);
                        }
                    });

                    intentData = str3;
                    try {
                        tyre_id = split[3];
                    } catch (Exception unused) {
                        Toast.makeText(getApplicationContext(), "The QR code is old model", Toast.LENGTH_SHORT).show();
                    }
                    send_case = SendCase.TYRE;
                    return true;
                } else if (!split[1].replaceAll("\\s", "").equals("ORG")) {
                    return false;
                } else {
                    final String str4 = split[2];
                    runOnUiThread(new Runnable() {

                        public void run() {
                            String str = "http://autobutler.no/dekkhotell/employee/qr_mass_tyre_delivery.php?method=query&employeeid=" + employeeid + "&orgNr=" + str4;

                            network_queue.add(new JsonObjectRequest(0, str, null, new Response.Listener<JSONObject>() {

                                public void onResponse(JSONObject jSONObject) {
                                    try {
                                        Log.i("Inside response", (String) jSONObject.get(NotificationCompat.CATEGORY_STATUS));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("1")) {
                                            if (jSONObject.get("data").equals("0")) {
                                                txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                                                txtBarcodeValue.setText("NO Tyres for delivery");
                                                return;
                                            }
                                            txtBarcodeValue.setTextColor(-16711936);
                                            TextView textView = txtBarcodeValue;
                                            textView.setText("DELIVER " + ((String) jSONObject.get("data")) + " TYRES?");
                                        } else if (jSONObject.get(NotificationCompat.CATEGORY_STATUS).equals("0")) {
                                            Toast.makeText(getApplicationContext(), "Tyre Not in list", Toast.LENGTH_SHORT).show();
                                            txtBarcodeValue.setTextColor(-16711936);
                                            txtBarcodeValue.setText("RECEIVED ZERO DELIVERY ITEMS");
                                        }
                                    } catch (JSONException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override 
                                public void onErrorResponse(VolleyError volleyError) {
                                    try {
                                        if (volleyError.networkResponse.statusCode == 303) {
                                            Toast.makeText(getApplicationContext(), "Error while updating", Toast.LENGTH_SHORT);
                                            txtBarcodeValue.setTextColor(SupportMenu.CATEGORY_MASK);
                                            txtBarcodeValue.setText("Failed to update");
                                        }
                                    } catch (NullPointerException unused) {
                                        Toast.makeText(getApplicationContext(), "Update failed, Please check internet connection", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }));
                        }
                    });
                    intentData = str4;
                    send_case = SendCase.ORG;
                    return true;
                }
            }
        });
    }

    
    @Override 
    public void onPause() {
        super.onPause();
        cameraSource.release();
    }

    
    @Override 
    public void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}