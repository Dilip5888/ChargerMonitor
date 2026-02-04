package com.example.chargermonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView statusText;

    // State tracking
    private enum ChargeState {
        NOT_CHARGING,
        OBSERVING,
        STABLE_CHARGING,
        PULSE_DETECTED
    }

    private ChargeState currentState = ChargeState.NOT_CHARGING;
    private long chargingStartTime = 0;
    private static final long STABILITY_WINDOW_MS = 1500; // capacitor discharge window

    private Handler handler = new Handler();
    private Runnable stabilityCheckRunnable;

    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging =
                    status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

            // 🔌 CHARGING DETECTED
            if (isCharging) {

                // Start observation if NOT already charging or observing
                if (currentState == ChargeState.NOT_CHARGING ||
                        currentState == ChargeState.PULSE_DETECTED) {

                    currentState = ChargeState.OBSERVING;
                    chargingStartTime = System.currentTimeMillis();
                    statusText.setText("⚡ Power detected… verifying source");

                    // Schedule stability check
                    stabilityCheckRunnable = () -> {
                        // If we reach here and still charging, it's stable
                        if (currentState == ChargeState.OBSERVING) {
                            currentState = ChargeState.STABLE_CHARGING;
                            statusText.setText("✅ Real charger detected\nCharging stable");
                        }
                    };

                    handler.postDelayed(stabilityCheckRunnable, STABILITY_WINDOW_MS);
                }
                // If already stable, do nothing (don't restart observation)
                else if (currentState == ChargeState.STABLE_CHARGING) {
                    // Keep showing stable message
                }
            }

            // 🔋 NOT CHARGING
            else {

                // If we were observing and charging stopped early → capacitor pulse
                if (currentState == ChargeState.OBSERVING) {
                    handler.removeCallbacks(stabilityCheckRunnable);
                    currentState = ChargeState.PULSE_DETECTED;

                    long duration = System.currentTimeMillis() - chargingStartTime;
                    statusText.setText(
                            "⚠️ Connected but NOT charging\n" +
                                    "(capacitor pulse detected - " + duration + "ms)");
                }
                // If was stable and now stopped → normal unplug
                else if (currentState == ChargeState.STABLE_CHARGING) {
                    currentState = ChargeState.NOT_CHARGING;
                    statusText.setText("🔋 Not charging");
                }
                // If pulse was already detected, keep showing it
                else if (currentState == ChargeState.PULSE_DETECTED) {
                    // Keep the pulse message until next plug-in
                }
                // Default not charging
                else {
                    currentState = ChargeState.NOT_CHARGING;
                    statusText.setText("🔋 Not charging");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        statusText.setText("🔋 Waiting for charger...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(
                batteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryReceiver);
        handler.removeCallbacksAndMessages(null);
    }
}