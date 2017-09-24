package com.example.remch.wifianalysis;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

public class AngelService extends Service {
    private boolean running = false;
    public AngelService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (!MainActivity.isMyServiceRunning(AngelService.this, MyService.class)) {
                        Log.d("Angel", "MyService died, respawn");
                        Intent mIntent = new Intent(AngelService.this, MyService.class);
                        startService(mIntent);
                    }
                    try {
                        Thread.sleep(1000 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        running = false;
        super.onDestroy();
    }
}
