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
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            boolean isPlugged = chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                    chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            // Show debug toast
            Toast.makeText(context,
                    "isPlugged: " + isPlugged + ", isCharging: " + isCharging,
                    Toast.LENGTH_SHORT).show();

            long currentTime = System.currentTimeMillis();

            if (isCharging) {
                lastChargeStartTime = currentTime;
                wasCharging = true;
                statusText.setText("✅ Charging...");
            } else if (isPlugged && !isCharging) {
                if (wasCharging) {
                    long timeSinceCharge = currentTime - lastChargeStartTime;

                    if (timeSinceCharge < 5000) {
                        statusText.setText("⚠️ Brief charge detected! Power switch may be OFF.");
                        Toast.makeText(context,
                                "Brief charging detected and stopped. Did you forget to turn ON the switch?",
                                Toast.LENGTH_LONG).show();
                    } else {
                        statusText.setText("🔌 Plugged in, but not charging.");
                    }

                    wasCharging = false;
                } else {
                    statusText.setText("🔌 Plugged in, but not charging.");
                }
            } else {
                statusText.setText("🔋 Not connected to charger.");
                wasCharging = false;
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
