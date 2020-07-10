package com.vik0t0r.bluebridge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

public class Bridge extends Service {
    private static final String TAG = "BridgeService";
    private BluetoothDevice mDevice;
    private BluetoothAdapter mAdapter;
    ServerThread s = null;


    NotificationManager manager;
    String Channelid = "MainNotification";
    public Bridge() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(Channelid, "Foreground Service", NotificationManager.IMPORTANCE_DEFAULT));
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        // Retrieve Bluetooth device from Intent bundle
        Bundle b = i.getExtras();
        mDevice = (BluetoothDevice) b.getParcelable("BluetoothDevice");

        // make the service foreground
        Toast.makeText(this,"Service Start Command",Toast.LENGTH_SHORT).show();

        Bundle extras = i.getExtras();


        // Create tap intent
        Intent tapintent = new Intent(this, MainActivity.class);
        tapintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapendingIntent = PendingIntent.getActivity(this, 0, tapintent, 0);


        //notification creation
        Notification notification =
                new Notification.Builder(this, Channelid)
                        .setSmallIcon(R.drawable.ic_baseline_bluetooth_searching_24)
                        .setContentTitle("Connected to:")
                        .setContentText(mDevice.getName() + " | " + mDevice.getAddress())
                        .setContentIntent(tapendingIntent)
                        .build();

        startForeground(14000, notification);

        // Start main thread
        //TODO start any other needed server
        if (s == null) {
            s = new ServerThread(8022, mDevice, UUID.fromString("f331dead-1234-4321-9999-785340612afe"));
            s.start();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        s.interrupt();
    }


private class ServerThread extends Thread {
        Boolean interrupted = false;

        private BluetoothDevice mDevice;
        private InetAddress ServerIP;
        private int ServerPort;
        private ServerSocket ServerListenerSocket;
        private UUID mBtUUID;
        private ProxyServer ps;

        private Socket socket1 = null;
        private BluetoothSocket socketBT = null;

        public ServerThread(int port, BluetoothDevice device , UUID BtUUID){
            ServerPort = port;
            mDevice = device;
            mBtUUID = BtUUID;

        }


        @Override
        public void run(){
            try {
                ServerIP = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }



            while (true){
                try{
                    Log.d(TAG, "initializing sockets");
                    if (interrupted){
                        break;
                    }
                    sleep(1000);
                    ServerListenerSocket = new ServerSocket(ServerPort, 0, ServerIP);
                    if (ServerListenerSocket != null){
                        break;
                    }

                }
            catch (IOException | InterruptedException e){
                e.printStackTrace();
            }}


            try {
                while(true){
                    Log.d(TAG,"Loop init");
                    if (interrupted){
                        break;
                    }
                    socketBT = mDevice.createRfcommSocketToServiceRecord(mBtUUID);
                    socket1 = ServerListenerSocket.accept();
                    socketBT.connect();
                    ps = new ProxyServer(socketBT, socket1);
                    ps.start();
                    ps.join();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

    public void interrupt(){
            interrupted = true;

        if (ServerListenerSocket != null){
            try {
                ServerListenerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            if (socket1 != null){
                try {
                    socket1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socketBT != null) {
                try {
                    socketBT.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null){
            ps.stop();
            }
    }

}
}