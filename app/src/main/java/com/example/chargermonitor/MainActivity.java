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

    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            boolean isPlugged = chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                    chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            Toast.makeText(context,
                    "Plugged: " + isPlugged + ", Charging: " + isCharging,
                    Toast.LENGTH_SHORT).show();


            if (isPlugged && !isCharging) {
                statusText.setText("⚠️ Plugged in, but NOT charging!");
                Toast.makeText(context, "Charger is plugged but switch may be OFF!", Toast.LENGTH_LONG).show();
            } else if (isCharging) {
                statusText.setText("✅ Charging...");
            } else {
                statusText.setText("🔋 Not connected to charger.");
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
