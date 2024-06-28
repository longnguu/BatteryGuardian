package com.example.batteryguardian.Views;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.batteryguardian.MainActivity;
import com.example.batteryguardian.R;
import com.example.batteryguardian.Service.ChargingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppSettingActivity extends AppCompatActivity {
    Spinner spinner_duration,spinner_turnoff;
    TextView changeLanguage;
    Switch aSwitch;
    ImageView back;
    SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitUI();
    }

    private void createDataSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.duration_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_duration.setAdapter(adapter);
        ArrayAdapter<CharSequence> adapterTurnoff = ArrayAdapter.createFromResource(this,
                R.array.turnoff_animation, android.R.layout.simple_spinner_item);
        adapterTurnoff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_turnoff.setAdapter(adapterTurnoff);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show = sharedPreferences.getBoolean("show", false);
        int duration = sharedPreferences.getInt("duration", 5);
        int turnoff = sharedPreferences.getInt("turnoff", 1);
        aSwitch.setChecked(show);
        spinner_duration.setSelection(getPositionDuration(duration));
        spinner_turnoff.setSelection(turnoff-1);
    }

    private void InitUI() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        back = findViewById(R.id.btn_back);
        spinner_duration = findViewById(R.id.spinner_duration);
        spinner_turnoff = findViewById(R.id.spinner_turnoff);
        aSwitch = findViewById(R.id.switch1);
        changeLanguage = findViewById(R.id.tv_language);
        String savedLanguage = sharedPreferences.getString("language", "vi");
        if ("en".equals(savedLanguage)) {
            changeLanguage.setText("English");
        } else {
            changeLanguage.setText("Tiếng việt");
        }
        changeLanguage.setOnClickListener(v -> {
            showLanguageSelectionDialog();
        });
        createDataSpinner();

        back.setOnClickListener(v -> {
            startActivity(new Intent(AppSettingActivity.this, MainActivity.class));
            finish();
        });
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkOverlayPermission();
                spinner_duration.setEnabled(true);
                spinner_turnoff.setEnabled(true);
                if (!isServiceRunning(ChargingService.class)) {
                    Intent serviceIntent = new Intent(AppSettingActivity.this, ChargingService.class);
                    startService(serviceIntent);
                }
            } else {
                spinner_duration.setEnabled(false);
                spinner_turnoff.setEnabled(false);
                if (isServiceRunning(ChargingService.class)) {
                    Intent serviceIntent = new Intent(this, ChargingService.class);
                    stopService(serviceIntent);
                }
            }
            int durationPosition = spinner_duration.getSelectedItemPosition();
            int duration = getDurationFromSpinner(durationPosition);

            int turnoffPosition = spinner_turnoff.getSelectedItemPosition();
            int turnoff = turnoffPosition+1;

            if (isChecked) {
                savePreferences(true,duration,turnoff);
            } else {
                savePreferences(false,duration,turnoff);
                sharedPreferences.edit().putBoolean("show", false).apply();
            }
        });
        spinner_duration.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                int durationPosition = spinner_duration.getSelectedItemPosition();
                int duration = getDurationFromSpinner(durationPosition);

                int turnoffPosition = spinner_turnoff.getSelectedItemPosition();
                int turnoff = turnoffPosition+1;

                if (aSwitch.isChecked()) {
                    savePreferences(true,duration,turnoff);
                } else {
                    savePreferences(false,duration,turnoff);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {

            }
        });
        spinner_turnoff.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                int durationPosition = spinner_duration.getSelectedItemPosition();
                int duration = getDurationFromSpinner(durationPosition);

                int turnoffPosition = spinner_turnoff.getSelectedItemPosition();
                int turnoff = turnoffPosition+1;

                if (aSwitch.isChecked()) {
                    savePreferences(true,duration,turnoff);
                } else {
                    savePreferences(false,duration,turnoff);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {

            }
        });
    }

    private int getDurationFromSpinner(int durationPosition) {
        switch (durationPosition) {
            case 0:
                return 5;
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 0;
            default:
                return 5;
        }
    }
    private int getPositionDuration(int duration) {
        switch (duration)
        {
            case 5:
                return 0;
            case 10:
                return 1;
            case 15:
                return 2;
            case 0:
                return 3;
            default:
                return 0;
        }
    }

    private void savePreferences(boolean show,int duration,int turnoff) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("show",show);
        editor.putInt("duration", duration);
        editor.putInt("turnoff", turnoff);
        editor.apply();
    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    aSwitch.setChecked(true);
                }else{
                    aSwitch.setChecked(false);
                }
            }
        }
    }
    private void switchLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    private void showLanguageSelectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_language_selection, null);

        RadioGroup radioGroupLanguages = dialogView.findViewById(R.id.radioGroupLanguages);
        RadioButton radioButtonEnglish = dialogView.findViewById(R.id.radioButtonEnglish);
        RadioButton radioButtonVietnamese = dialogView.findViewById(R.id.radioButtonVietnamese);

        // Load the saved language preference
        String savedLanguage = sharedPreferences.getString("language", "vi");
        if ("en".equals(savedLanguage)) {
            radioButtonEnglish.setChecked(true);
        } else {
            radioButtonVietnamese.setChecked(true);
        }

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(AppSettingActivity.this.getResources().getString(R.string.select_language) + ":");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save the selected language
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (radioButtonEnglish.isChecked()) {
                    editor.putString("language", "en");
                    changeLanguage.setText("English");
                } else {
                    editor.putString("language", "vi");
                    changeLanguage.setText("Tiếng việt");
                }
                editor.apply();
                applyLanguage();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void applyLanguage() {
        String language = sharedPreferences.getString("language", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        recreate();
    }

}