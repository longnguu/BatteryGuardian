package com.example.batteryguardian.Service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.batteryguardian.R;

public class ChargingService extends Service {
    private final String CHANNEL_ID = "ChargingServiceChannel";
    private BroadcastReceiver receiver;
    private WindowManager windowManager;
    private View overlayView;
    private Handler handler = new Handler();
    private float batteryPct = 0.0f;
    LottieAnimationView lottieAnimationView;
    SharedPreferences sharedPreferences;
    int percentTop,percentBottom,duration,turnoff;
    boolean sendNotificationTop = false;
    boolean sendNotificationBottom = false;
    boolean protectBattery = false;
    boolean show = true;
    private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && STOP_SERVICE_ACTION.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadPreferences();
        // Register SharedPreferences change listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);

        createNotificationChannel();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case Intent.ACTION_POWER_CONNECTED:
                            showOverlay();
                            break;
                        case Intent.ACTION_POWER_DISCONNECTED:
                            hideOverlay();
                            break;
                    }
                    if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                        Toast.makeText(context, "Power connected", Toast.LENGTH_SHORT).show();
                    } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                        Toast.makeText(context, "Power disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(receiver, filter);

        Intent stopServiceIntent = new Intent(this, ChargingService.class);
        stopServiceIntent.setAction(STOP_SERVICE_ACTION);

        PendingIntent stopServicePendingIntent = PendingIntent.getService(
                this,
                0,
                stopServiceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon_app)
                .setContentText("Battery Guardian is running")
                .addAction(R.drawable.icon_stop, "Stop Service", stopServicePendingIntent);

        startForeground(101, notificationBuilder.build());
    }

    private void loadPreferences() {
        duration = sharedPreferences.getInt("duration", 5); // default to 5 seconds
        turnoff = sharedPreferences.getInt("turnoff", 1);   // default to 1 (single click)
        percentTop = sharedPreferences.getInt("percentTop", 100); // default to 100%
        percentBottom = sharedPreferences.getInt("percentBottom", 100); // default to 100%
        protectBattery = sharedPreferences.getBoolean("protectBattery", false); // default to false
        show = sharedPreferences.getBoolean("show", false); // default to false

    }

    private void createBatteryReceiver() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPct = level * 100 / (float) scale;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Charging Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefListener);
        hideOverlay();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showOverlay() {
        if (overlayView != null) {
            return;
        }
        sendNotificationTop = false;
        sendNotificationBottom = false;
        batteryPct = getCurrentBatteryLevel();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_charge_overlay, null);
        createBatteryReceiver();

        RelativeLayout overlayLayout = overlayView.findViewById(R.id.viewOverlay);
        lottieAnimationView = overlayView.findViewById(R.id.overlay_image);
        int type = sharedPreferences.getInt("type", 0);
        int src = sharedPreferences.getInt("src", -1);
        if (type==0 && src!=-1){
            lottieAnimationView.setAnimation(src);
        }
        int flag = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        windowManager.addView(overlayView, params);

        final TextView overlayTextView = overlayView.findViewById(R.id.overlay_textview);
        updateBatteryStatus(overlayTextView);

        if (turnoff == 1) {
            overlayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideOverlay();
                }
            });
        } else if (turnoff == 2) {
            overlayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setClickable(false);
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setClickable(true);
                        }
                    }, 200);
                }
            });
            overlayView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(overlayView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        hideOverlay();
                        return super.onDoubleTap(e);
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        if (duration!=0){
            overlayView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideOverlay();
                }
            }, duration * 1000);
        }
    }
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            loadPreferences();
            if (overlayView != null) {
                hideOverlay();
                showOverlay();
            }
        }
    };


    private void hideOverlay() {
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    private void updateBatteryStatus(final TextView overlayTextView) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                if (protectBattery){
                    if (batteryPct >= percentTop && !sendNotificationTop) {
                        sendNotification("Hãy rút sạc","Đã đến mức pin cài đặt");
                    }
                    if (batteryPct <= percentBottom && !sendNotificationBottom) {
                        sendNotification("Hãy sạc","Đã đến mức pin cài đặt");
                    }
                }
                if (!show){
                    stopForeground(true);
                    stopSelf();
                }
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                overlayTextView.setText(String.format("Battery Level: %.2f%%\nCharging: %b", batteryPct, isCharging));
                handler.postDelayed(this, 1000); // Update every second
            }
        });
    }

    private void sendNotification(String title,String message) {
        sendNotificationTop = true;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Uri soundUri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.sound_notification);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_app)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private float getCurrentBatteryLevel() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level * 100 / (float) scale;
    }
}
