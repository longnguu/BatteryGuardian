package com.example.batteryguardian.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.batteryguardian.Model.BatteryInfo;

public class BatteryViewModel extends ViewModel {
    private MutableLiveData<BatteryInfo> batteryInfoLiveData = new MutableLiveData<>();

    public LiveData<BatteryInfo> getBatteryInfo() {
        return batteryInfoLiveData;
    }

    public void setBatteryInfo(BatteryInfo batteryInfo) {
        batteryInfoLiveData.setValue(batteryInfo);
    }
}

