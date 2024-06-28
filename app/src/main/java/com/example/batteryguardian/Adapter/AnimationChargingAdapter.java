package com.example.batteryguardian.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.batteryguardian.Model.AnimationCharging;
import com.example.batteryguardian.R;

import java.util.List;

public class AnimationChargingAdapter extends RecyclerView.Adapter<AnimationChargingAdapter.ViewHolder>{
    List<AnimationCharging> animationChargings;
    Context context;

    public AnimationChargingAdapter(List<AnimationCharging> animationChargings, Context context) {
        this.animationChargings = animationChargings;
        this.context = context;
    }

    @NonNull
    @Override
    public AnimationChargingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_animation_charging, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimationChargingAdapter.ViewHolder holder, int position) {
        AnimationCharging animationCharging = animationChargings.get(position);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int positionAnimation = sharedPreferences.getInt("positionAnimation", 0);
        int type = sharedPreferences.getInt("type", 0);
        int src = sharedPreferences.getInt("src", 0);
        Log.d("androidruntime", "onBindViewHolder: "+src + " "+positionAnimation + " "+type);
        if (animationCharging.getType()==0){
            holder.lottieAnimationView.setAnimation(animationCharging.getSrc());
            holder.lottieAnimationView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
        }
        if (positionAnimation == position){
            holder.checkedImageView.setVisibility(View.VISIBLE);
        }else{
            holder.checkedImageView.setVisibility(View.GONE);
        }
        holder.constraintLayout.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1).scaleY(1).setDuration(100);
            });
            editor.putInt("src", Integer.valueOf(animationCharging.getSrc()));
            editor.putInt("positionAnimation", position);
            editor.putInt("type", animationCharging.getType());
            editor.apply();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        if (animationChargings != null){
            return animationChargings.size();
        }else{
            return 0;
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        LottieAnimationView lottieAnimationView;
        ImageView imageView,checkedImageView;
        ConstraintLayout constraintLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lottieAnimationView = itemView.findViewById(R.id.animationView);
            imageView = itemView.findViewById(R.id.imageView);
            constraintLayout = itemView.findViewById(R.id.layout_animation_charing_main);
            checkedImageView = itemView.findViewById(R.id.checkedImageView);
        }
    }
}
