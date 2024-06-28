package com.example.batteryguardian.Views;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.batteryguardian.Broadcast.BatteryReceiver;
import com.example.batteryguardian.MainActivity;
import com.example.batteryguardian.R;
import com.example.batteryguardian.ViewModel.BatteryViewModel;

public class BatteryInforActivity extends AppCompatActivity {
    private BatteryViewModel batteryViewModel;
    private BatteryReceiver batteryReceiver;
    TextView tvBatteryLevel,tvBatteryStatus,tvBatteryHealth,tvBatteryTechnology,tvBatteryTemperature,tvBatteryVoltage,tvBatteryCapacity;
    ImageView btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_battery_infor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitUI();
    }

    private void InitUI() {
        btn_back = findViewById(R.id.btn_back);
        tvBatteryLevel = findViewById(R.id.main_tv_percent);
        tvBatteryStatus = findViewById(R.id.main_tv_status);
        tvBatteryHealth = findViewById(R.id.main_tv_health);
        tvBatteryTechnology = findViewById(R.id.main_tv_technology);
        tvBatteryTemperature = findViewById(R.id.main_tv_temperature);
        tvBatteryVoltage = findViewById(R.id.main_tv_voltage);
        tvBatteryCapacity = findViewById(R.id.main_tv_capacity);

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(BatteryInforActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        batteryViewModel = new ViewModelProvider(this).get(BatteryViewModel.class);
        batteryReceiver = new BatteryReceiver(batteryViewModel);

        batteryViewModel.getBatteryInfo().observe(this, batteryInfo -> {
            tvBatteryLevel.setText(batteryInfo.getBatteryPct() + " %");
            tvBatteryStatus.setText(batteryInfo.getStatus());
            tvBatteryHealth.setText(batteryInfo.getHealth());
            tvBatteryTechnology.setText(batteryInfo.getTechnology());
            tvBatteryTemperature.setText(batteryInfo.getTempCelsius() + " °C");
            tvBatteryVoltage.setText(batteryInfo.getVoltage());
            tvBatteryCapacity.setText(batteryInfo.getCapacity() + " mAh");
        });
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