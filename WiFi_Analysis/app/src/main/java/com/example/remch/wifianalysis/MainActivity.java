package com.example.remch.wifianalysis;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import netTools.Pinger;
import netTools.model.Device;


public class MainActivity extends AppCompatActivity {

    TextView tv;
    SharedPreferences sp;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Message newMsg = new Message();
                    msg.what = 0;
                    handler.sendMessageDelayed(newMsg, 5000);
                    refresh();
                    return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.text);
        sp = getSharedPreferences("basic", MODE_PRIVATE);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();
                start();
                tv.setText("服务已开启");
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                stop();
            }
        });
    }

    private void start(){
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    private void stop(){
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.cancel(pi);

        sp.edit().putLong("time", 0).commit();

        tv.setText("服务已关闭");
    }

    private void refresh(){
        long lastTime = sp.getLong("time", 0);
        long connections = sp.getInt("connections", 0);
        long diff = System.currentTimeMillis() - lastTime;
        double minutes = ((double)diff / 1000 / 60);
        Log.d("Main", "The diff between current and the last time is " + minutes + " min");
        String close = "服务已关闭. " + minutes;
        String open = "服务已开启. last time: " + minutes + "min, connections:" + connections;
        if (!isMyServiceRunning(this, MyService.class)){
            tv.setText(close);
        }else{
            tv.setText(open);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        handler.sendEmptyMessage(0);
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onDestroy() {
        // Destroy
        super.onDestroy();
    }

    public static String intToIp(int i) {

        return ((i >> 24 ) & 0xFF ) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ( i & 0xFF) ;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//    private static class AsyncScan extends AsyncTask<NetDeviceAdapter, Void, List<Device>> {
//
//        private NetDeviceAdapter adapter;
//        private AppCompatDialog mDialog;
//
//
//        public AsyncScan(AppCompatDialog dialog, String string) {
//            super();
//            this.mDialog = dialog;
//        }
//
//        @Override
//        protected List<Device> doInBackground(NetDeviceAdapter... voids) {
//
//
//            String ipString = getLocalIpv4Address();
//
//
//            if (ipString == null){
//                return new ArrayList<Device>(1);
//            }
//            int lastdot = ipString.lastIndexOf(".");
//            ipString = ipString.substring(0, lastdot);
//
//            List<Device> addresses = Pinger.getDevicesOnNetwork(ipString);
//            adapter = voids[0];
//            return addresses;
//        }
//
//        @Override
//        protected void onPostExecute(List<Device> inetAddresses) {
//            super.onPostExecute(inetAddresses);
//            adapter.setAddresses(inetAddresses);
//            adapter.notifyDataSetChanged();
//            mDialog.cancel();
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//
//        }
//
//
//        public static String getLocalIpv4Address(){
//            try {
//                String ipv4;
//                List<NetworkInterface>  nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
//                if(nilist.size() > 0){
//                    for (NetworkInterface ni: nilist){
//                        List<InetAddress>  ialist = Collections.list(ni.getInetAddresses());
//                        if(ialist.size()>0){
//                            for (InetAddress address: ialist){
//                                String ipv4 = address.getHostAddress();
//                                if (!address.isLoopbackAddress() && address instanceof Inet4Address){
//                                    return ipv4;
//                                }
//                            }
//                        }
//
//                    }
//                }
//
//            } catch (SocketException ex) {
//
//            }
//            return "";
//        }
//
//
//    }
}
