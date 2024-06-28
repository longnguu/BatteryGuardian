package com.example.batteryguardian.Views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.batteryguardian.Adapter.AnimationChargingAdapter;
import com.example.batteryguardian.MainActivity;
import com.example.batteryguardian.Model.AnimationCharging;
import com.example.batteryguardian.R;

import java.util.ArrayList;
import java.util.List;

public class ChangeAnimationActivity extends AppCompatActivity {
    ImageView btn_back;
    RecyclerView recyclerView;
    AnimationChargingAdapter animationChargingAdapter;
    List<AnimationCharging> animationChargings = new ArrayList<>(); ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_animation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitUI();
    }

    private void InitUI() {
        btn_back = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.recyclerView);
        loadDataAnimation();
        animationChargingAdapter = new AnimationChargingAdapter(animationChargings, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(animationChargingAdapter);
        btn_back.setOnClickListener(v -> {
            startActivity(new Intent(ChangeAnimationActivity.this, MainActivity.class));
            finish();
        });
    }

    private void loadDataAnimation() {
        animationChargings.add(new AnimationCharging.Builder()
                .id(1)
                .type(0)
                .src(R.raw.animation_splash)
                .build());
        animationChargings.add(new AnimationCharging.Builder()
                .id(2)
                .type(0)
                .src(R.raw.animation_water)
                .build());
        animationChargings.add(new AnimationCharging.Builder()
                .id(3)
                .type(0)
                .src(R.raw.animation_1)
                .build());
        animationChargings.add(new AnimationCharging.Builder()
                .id(3)
                .type(0)
                .src(R.raw.animation_2)
                .build());
    }
}