package com.rootwatchparty.railwayapi;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {

    private EditText searchUrlEditText, tripNumberEditText;
    private Button submitButton;
    private ProgressBar progressBar;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        searchUrlEditText = findViewById(R.id.search_url);
        tripNumberEditText = findViewById(R.id.trip_number);
        progressBar = findViewById(R.id.progress_bar);
        messageTextView = findViewById(R.id.messageTextView);
        submitButton = findViewById(R.id.submit_button);

        // Handle Android 13+ permission
        requestNotificationPermission();

        submitButton.setOnClickListener(v -> checkPermissionAndHandleService());

        initServiceUI();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                    .permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (!allGranted) {
                            Toast.makeText(this, "Please grant notification permission.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkPermissionAndHandleService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                    .permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            handleServiceStartStop();
                        } else {
                            Toast.makeText(this, "Please grant notification permission.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            handleServiceStartStop();
        }
    }

    private void handleServiceStartStop() {
        if (isServiceRunning(TrainFetchService.class)) {
            stopTrainFetchService(); // No need to check URL/trip number when stopping the service
        } else {
            String searchUrl = searchUrlEditText.getText().toString().trim();
            String tripNumber = tripNumberEditText.getText().toString().trim();

            if (searchUrl.isEmpty() || tripNumber.isEmpty()) {
                Toast.makeText(this, "Please enter all the fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            startTrainFetchService(searchUrl, tripNumber); // Check fields only when starting the service
        }
        initServiceUI();
    }

    private void startTrainFetchService(String searchUrl, String tripNumber) {
        Intent serviceIntent = new Intent(this, TrainFetchService.class);
        serviceIntent.putExtra("search_url", searchUrl);
        serviceIntent.putExtra("trip_id", tripNumber);
        startService(serviceIntent);
        Toast.makeText(this, "Service Started.", Toast.LENGTH_SHORT).show();
    }

    private void stopTrainFetchService() {
        Intent serviceIntent = new Intent(this, TrainFetchService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Service Stopped.", Toast.LENGTH_SHORT).show();
    }

    private void initServiceUI() {
        boolean isRunning = isServiceRunning(TrainFetchService.class);
        submitButton.setText(isRunning ? "Stop Service" : "Start Service");
        searchUrlEditText.setVisibility(isRunning ? View.GONE : View.VISIBLE);
        tripNumberEditText.setVisibility(isRunning ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        messageTextView.setText(isRunning ? "Service is running..." : "Service is not running...");
        messageTextView.setTextColor(getColor(isRunning ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        initServiceUI();
    }
}
