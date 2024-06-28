package com.example.batteryguardian;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.batteryguardian.Broadcast.BatteryReceiver;
import com.example.batteryguardian.Broadcast.ChargingOverlayManager;
import com.example.batteryguardian.Broadcast.OverlayReceiver;
import com.example.batteryguardian.Model.BatteryInfo;
import com.example.batteryguardian.Service.ChargingService;
import com.example.batteryguardian.ViewModel.BatteryViewModel;
import com.example.batteryguardian.Views.AppSettingActivity;
import com.example.batteryguardian.Views.BatteryInforActivity;
import com.example.batteryguardian.Views.BatterySettingActivity;
import com.example.batteryguardian.Views.ChangeAnimationActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION = 2;
    private BatteryViewModel batteryViewModel;
    private BatteryReceiver batteryReceiver;
    private LottieAnimationView lottieAnimationView;
    OverlayReceiver overlayReceiver;
    CardView cardView1,cardView2,cardView3,cardView4;
    Animation anim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitUI();
        requestNotificationPermission();
        InitFunction();
        startChargingService();
    }

    private void InitFunction() {
        cardView1 = findViewById(R.id.cardView2);
        cardView2 = findViewById(R.id.cardView3);
        cardView3 = findViewById(R.id.cardView4);
        cardView4 = findViewById(R.id.cardView5);
        cardView1.setOnClickListener(v -> {
            animationPress(v);
            Intent intent = new Intent(MainActivity.this, ChangeAnimationActivity.class);
            startActivity(intent);
        });
        cardView2.setOnClickListener(v -> {
            animationPress(v);
            Intent intent = new Intent(MainActivity.this, BatteryInforActivity.class);
            startActivity(intent);
        });
        cardView3.setOnClickListener(v -> {
            animationPress(v);
            Intent intent = new Intent(MainActivity.this, BatterySettingActivity.class);
            startActivity(intent);
        });
        cardView4.setOnClickListener(v -> {
            animationPress(v);
            Intent intent = new Intent(MainActivity.this, AppSettingActivity.class);
            startActivity(intent);
        });
    }

    private void animationPress(View v) {
        v.animate()
                .scaleX(0.8f) // Giảm kích thước X
                .scaleY(0.8f) // Giảm kích thước Y
                .setDuration(150) // Thời gian animation (chậm lại)
                .withEndAction(() -> {
                    v.animate()
                            .scaleX(1f) // Trở về kích thước ban đầu X
                            .scaleY(1f) // Trở về kích thước ban đầu Y
                            .setDuration(150);
                });
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
            } else {
                requestNotificationPermission();
            }
        } else {
            requestNotificationPermission();
        }
    }
    private void InitUI() {
        anim = AnimationUtils.loadAnimation(this, R.anim.scale_press);

        overlayReceiver = new OverlayReceiver();
        batteryViewModel = new ViewModelProvider(this).get(BatteryViewModel.class);
        batteryReceiver = new BatteryReceiver(batteryViewModel);

        lottieAnimationView = findViewById(R.id.main_animationView);
        batteryViewModel.getBatteryInfo().observe(this, new Observer<BatteryInfo>() {
            @Override
            public void onChanged(BatteryInfo batteryInfo) {
                if (batteryInfo != null) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lottieAnimationView.getLayoutParams();
                    int marginTop = (int) -(batteryInfo.getBatteryPct() * 5) - 100;
//                    marginTop = (int) -(0 * 5)-100;
                    params.setMargins(-300, marginTop, -300, -300);
                    lottieAnimationView.setLayoutParams(params);
                    TextView batteryPctTextView = findViewById(R.id.main_info);
                    if (batteryInfo.getBatteryPct() < 20) {
                        batteryPctTextView.setTextColor(getResources().getColor(R.color.red));
                    } else if(batteryInfo.getBatteryPct()<80){
                        batteryPctTextView.setTextColor(getResources().getColor(R.color.yellow));}
                    else {
                        batteryPctTextView.setTextColor(getResources().getColor(R.color.black));
                    }
                    String charging = getResources().getString(R.string.battery_status_charging);
                    if (charging.equals(batteryInfo.getStatus())) {
                        batteryPctTextView.setText(
                                batteryInfo.getBatteryPct() + "%\n" +
                                        batteryInfo.getStatus() + "(" + batteryInfo.getTempCelsius() + "°C)\n" +
                                        batteryInfo.getCapacity() + "mAh"
                        );
                    } else {
                        batteryPctTextView.setText(batteryInfo.getBatteryPct() + "%");
                    }

                    if (charging.equals(batteryInfo.getStatus())) {
                        lottieAnimationView.playAnimation();
                        lottieAnimationView.loop(true);
                    } else {
                        lottieAnimationView.cancelAnimation();
                    }
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    requestNotificationPermission();
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION);
        } else {
            startChargingService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION) {
            startChargingService();
        }
    }
    private void startChargingService() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("show", false)) {
            Intent serviceIntent = new Intent(this, ChargingService.class);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryReceiver);
    }
}