package com.example.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AddTyreActivity extends AppCompatActivity {

    private WebView webView;
    private SharedPreferences srPref;
    private boolean alreadyLoaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tyre);
        initView();
    }

    private void initView() {
        webView = findViewById(R.id.webView);
        webView.clearCache(true);
        srPref = getSharedPreferences("QRAPP PREFERENCES", 0);
        loadUrl("http://autobutler.no/dekkhotell/employee/index.php?p=home");
    }
    private void loadUrl(String url) {
        webView.setWebViewClient(new CustomWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.loadUrl(url);
    }

    private class CustomWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String name = srPref.getString("user_name", "");
            String password = srPref.getString("password", "");
            view.evaluateJavascript("document.getElementsByName('username')[0].value = '"+ name +"';",null);
            view.evaluateJavascript("document.getElementsByName('pass')[0].value = '"+ password +"';",null);
            view.evaluateJavascript("document.getElementsByName('submit')[0].click();",null);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                if (!DetectConnection.checkInternetConnection(AddTyreActivity.this)) {
                    Toast.makeText(AddTyreActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    view.loadUrl(url);
                    Log.e("url:::", url);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (alreadyLoaded)
                return;
            alreadyLoaded = true;
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(AddTyreActivity.this);
            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static class DetectConnection {
        public static boolean checkInternetConnection(Context context) {
            try {
                ConnectivityManager con_manager = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);

                return (con_manager.getActiveNetworkInfo() != null
                        && con_manager.getActiveNetworkInfo().isAvailable()
                        && con_manager.getActiveNetworkInfo().isConnected());
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }

        }
    }
}