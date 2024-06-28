package com.example.batteryguardian.Broadcast;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.batteryguardian.R;

public class ChargingOverlayManager {
    private WindowManager windowManager;
    private View overlayView;

    public ChargingOverlayManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void showOverlay(Context context) {
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
        ImageView overlayImage = overlayView.findViewById(R.id.overlay_image);
        overlayImage.setBackgroundResource(R.drawable.charging_animation);
        AnimationDrawable chargingAnimation = (AnimationDrawable) overlayImage.getBackground();
        chargingAnimation.start();
        TextView overlayTextView = overlayView.findViewById(R.id.overlay_textview);
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideOverlay();
            }
        });
    }

    public void hideOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }
    private int getWindowType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;
    }
}
