package com.example.remch.wifianalysis;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import netTools.Pinger;
import netTools.model.Device;

public class MyService extends Service {
    private static final int GRAY_SERVICE_ID = 9999;
    // TODO 增加取消功能
    // TODO 增加获取本地IP功能
    // TODO 增加设定IP功能
    public static int timeout = 1000 * 60;
    boolean running = false;
    File save_file;
    SharedPreferences sharedPreferences;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
        save_file = new File(Environment.getExternalStorageDirectory(), "wifi_status.txt");
        sharedPreferences = getSharedPreferences("basic", MODE_PRIVATE);
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running) {
                    startForeground(GRAY_SERVICE_ID, new Notification());
                    if (!MainActivity.isMyServiceRunning(MyService.this, AngelService.class)){
                        Log.d("MyService", "Angel died, respawn");
                        Intent intent = new Intent(MyService.this, AngelService.class);
                        startService(intent);
                    }
                    Log.d("Service", "I am alive.");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new RunTest()).start();
        sendBroadcast();
        sharedPreferences.edit().putLong("time", System.currentTimeMillis()).apply();
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendBroadcast(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int timeout = 60 * 1000 * 10;
        long triggerAtTime = SystemClock.elapsedRealtime() + timeout;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
    }

    private void closeAlarm(){
    }

    @Override
    public void onDestroy() {
        running = false;
        Log.d("Service", "onDestroy");
        Intent intent = new Intent(this, AngelService.class);
        startService(intent);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class RunTest implements Runnable{

        @Override
        public void run() {
            Log.d("Service", "Start Scanning");
            try {
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(save_file, true)
                        )
                );
                String time = Long.toString(System.currentTimeMillis() / 1000);
                List<Device> devices = Pinger.getDevicesOnNetwork("192.168.101");
                StringBuilder sb = new StringBuilder();
                for (Device device : devices) {
                    String line = device.toString() + ' ' + time;
                    Log.d("Service", line);
                    sb.append(line);
                    sb.append('\n');
                }

                bw.write(sb.toString());
                bw.close();
                Log.d("Service", "Ping info saved");
                sharedPreferences.edit().putInt("connections", devices.size()).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
