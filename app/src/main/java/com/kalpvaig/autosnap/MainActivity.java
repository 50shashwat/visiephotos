package com.kalpvaig.autosnap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidhiddencamera.HiddenCameraFragment;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private HiddenCameraFragment mHiddenCameraFragment;
    private ProgressBar progressBar;
    private Button stopButton;
    private int toggle =0;
    private Timer timer;
    private TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            startService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        stopButton = (Button) findViewById(R.id.stop);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.SYSTEM_ALERT_WINDOW)!= PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.SYSTEM_ALERT_WINDOW
                },1001);

        }
        else {

            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "AutoSnap");

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("AutoSnap", "Oops! Failed create "
                            + "AutoSnap" + " directory");
                }
            }
            timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, 0, 15000);

        }

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle++;
                if (toggle%2==1)
                    stop();
                else
                    start();
            }
        });
    }

    private void startService() {
        if (mHiddenCameraFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mHiddenCameraFragment)
                    .commit();
            mHiddenCameraFragment = null;
        }
        startService(new Intent(MainActivity.this, DemoCamService.class));
    }

    public void start() {
        if(timer != null) {
            return;
        }
        stopButton.setText("Stop Snapping");
        progressBar.setVisibility(View.VISIBLE);
        if (timer==null) {
            timer = new Timer();
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    startService();
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 15000);
        }
    }

    public void stop() {
        timerTask.cancel();
        timer.cancel();
        timer = null;
        stopButton.setText("Start Snapping");
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1001: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    File mediaStorageDir = new File(
                            Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "AutoSnap");

                    // Create the storage directory if it does not exist
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("AutoSnap", "Oops! Failed create "
                                    + "AutoSnap" + " directory");
                        }
                    }

                    start();

                } else {
                    Toast.makeText(this, "This application requires all permissions", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

}
