package com.sdmay1207.sensors;

import sdmay1207.ais.sensors.Battery;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatterySensor extends Battery
{

    private IntentFilter ifilter;
    private Context c;

    public BatterySensor(Context c)
    {
        this.c = c;
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    }

    @Override
    public BatteryStatus getReading()
    {
        Intent batteryStatus = c.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;

        return new BatteryStatus(batteryPct*100, isCharging);
    }

    @Override
    public String getUnits()
    {
        return "%";
    }

}
