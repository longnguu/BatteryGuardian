package com.example.batteryguardian.Broadcast;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.batteryguardian.R;

public class OverlayReceiver extends BroadcastReceiver {
    private WindowManager windowManager;
    private View overlayView;
    private boolean isCharging = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        float batteryPct = level / (float)scale * 100;
        float tempCelsius = temperature / 10f;
        if (status == BatteryManager.BATTERY_STATUS_FULL) {
            sendFullChargeNotification(context);
        }
    }

    private void hideOverlay() {
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }
    private void showOverlay(Context context) {
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(context).inflate(R.layout.layout_charge_overlay, null);
        RelativeLayout overlayLayout = overlayView.findViewById(R.id.viewOverlay);
        int flag = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        overlayLayout.setSystemUiVisibility(flag);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.RGBA_8888
        );
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        windowManager.addView(overlayView, params);
        TextView overlayTextView = overlayView.findViewById(R.id.overlay_textview);
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideOverlay();
            }
        });

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
