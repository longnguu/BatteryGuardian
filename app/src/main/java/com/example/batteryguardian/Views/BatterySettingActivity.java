package com.example.batteryguardian.Views;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.batteryguardian.R;

public class BatterySettingActivity extends AppCompatActivity {
    Switch aSwitch;
    SeekBar seekBarTop, seekBarBottom;
    TextView tvTop, tvBottom;
    LinearLayout linearLayout;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_battery_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitUI();
    }

    private void InitUI() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        aSwitch = findViewById(R.id.switch1);
        seekBarTop = findViewById(R.id.power_top);
        seekBarBottom = findViewById(R.id.power_bottom);
        tvTop = findViewById(R.id.seekbar_value);
        tvBottom = findViewById(R.id.seekbar_value1);
        linearLayout = findViewById(R.id.linear_layout_protect);


        boolean protectBattery = sharedPreferences.getBoolean("protectBattery", false);
        aSwitch.setChecked(protectBattery);
        if (protectBattery){
            linearLayout.setVisibility(View.VISIBLE);
        }else{
            linearLayout.setVisibility(View.GONE);
        }

        int savedProgress = sharedPreferences.getInt("percentTop", 100);
        seekBarTop.setProgress(savedProgress);
        tvTop.setText(String.valueOf(savedProgress));
        int savedProgress1 = sharedPreferences.getInt("percentBottom", 0);
        seekBarBottom.setProgress(savedProgress1);
        tvBottom.setText(String.valueOf(savedProgress1));

        seekBarTop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTop.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("percentTop", seekBar.getProgress());
                editor.apply();

            }
        });
        seekBarBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBottom.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("percentBottom", seekBar.getProgress());
                editor.apply();

            }
        });
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(aSwitch.isChecked()){
                linearLayout.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putBoolean("protectBattery", true);
                editor.apply();
            }else{
                linearLayout.setVisibility(View.GONE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putBoolean("protectBattery", false);
                editor.apply();
            }
        });
    }
}