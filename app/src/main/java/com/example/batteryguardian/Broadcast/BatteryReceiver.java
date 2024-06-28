package com.example.batteryguardian.Broadcast;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.example.batteryguardian.Model.BatteryInfo;
import com.example.batteryguardian.R;
import com.example.batteryguardian.Service.ChargingService;
import com.example.batteryguardian.ViewModel.BatteryViewModel;

public class BatteryReceiver extends BroadcastReceiver {
    private BatteryViewModel batteryViewModel;
    Context context;
    public BatteryReceiver(BatteryViewModel batteryViewModel) {
        this.batteryViewModel = batteryViewModel;
    }
    public BatteryReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        String voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) + " mV";
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
        String healthString = getHealthString(health);
        float batteryPct = level / (float)scale * 100;
        String statusString = getStatusString(status);
        float tempCelsius = temperature / 10f;
        int capacity = getBatteryCapacity(context);
        BatteryInfo batteryInfo = new BatteryInfo(batteryPct, statusString, capacity, tempCelsius);
        batteryInfo.setVoltage(voltage);
        batteryInfo.setTechnology(technology);
        batteryInfo.setHealth(healthString);
        batteryViewModel.setBatteryInfo(batteryInfo);
        if (status == BatteryManager.BATTERY_STATUS_FULL) {
            sendFullChargeNotification(context);
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return context.getResources().getString(R.string.battery_status_charging);
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return context.getResources().getString(R.string.battery_status_discharging);
            case BatteryManager.BATTERY_STATUS_FULL:
                return context.getResources().getString(R.string.battery_status_full);
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return context.getResources().getString(R.string.battery_status_not_charging);
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                return context.getResources().getString(R.string.battery_status_unknown);
        }
    }
    private String getHealthString(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                return context.getResources().getString(R.string.battery_health_cold);
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return context.getResources().getString(R.string.battery_health_dead);
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return context.getResources().getString(R.string.battery_health_good);
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return context.getResources().getString(R.string.battery_health_over_voltage);
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return context.getResources().getString(R.string.battery_health_overheat);
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return context.getResources().getString(R.string.battery_health_unspecified_failure);
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            default:
                return context.getResources().getString(R.string.battery_health_unknown);
        }
    }


    private int getBatteryCapacity(Context context) {
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
            batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) batteryCapacity;
    }

    private void sendFullChargeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "battery_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Battery Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Battery Full")
                .setContentText("Your battery is fully charged.")
                .setSmallIcon(R.drawable.icon_charge)
                .build();
        notificationManager.notify(1, notification);
    }
}
