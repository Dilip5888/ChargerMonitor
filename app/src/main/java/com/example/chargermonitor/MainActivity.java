package com.example.chargermonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView statusText;
    long lastChargeStartTime = 0;
    boolean wasCharging = false;

    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            long currentTime = System.currentTimeMillis();

            // Debug info
            Toast.makeText(context,
                    "Charging: " + isCharging,
                    Toast.LENGTH_SHORT).show();

            if (isCharging) {
                lastChargeStartTime = currentTime;
                wasCharging = true;
                statusText.setText("✅ Charging...");
            } else {
                if (wasCharging) {
                    long duration = currentTime - lastChargeStartTime;

                    if (duration < 5000) {
                        statusText.setText("⚠️ Brief charging detected! Switch may be OFF.");
                        Toast.makeText(context,
                                "Charging started but stopped quickly. Did you forget the switch?",
                                Toast.LENGTH_LONG).show();
                    } else {
                        statusText.setText("🔋 Not charging.");
                    }

                    wasCharging = false;
                } else {
                    statusText.setText("🔋 Not charging.");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryReceiver);
    }
}
