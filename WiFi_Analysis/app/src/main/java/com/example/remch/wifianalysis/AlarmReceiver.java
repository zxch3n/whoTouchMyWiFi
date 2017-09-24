package com.example.remch.wifianalysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Remch on 2017/9/17.
 */

public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Receiver", "receive!");
        Intent newIntent = new Intent(context, MyService.class);
        context.startService(newIntent);
    }
}
