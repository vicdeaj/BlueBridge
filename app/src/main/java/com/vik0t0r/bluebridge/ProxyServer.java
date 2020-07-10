package com.vik0t0r.bluebridge;

import android.bluetooth.BluetoothSocket;
import android.net.IpPrefix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ProxyServer { //send everything from one socket to another
    private BlueToNonBlue BTN;
    private NonBlueToBlue NTB;
    public ProxyServer(BluetoothSocket BTSocket, Socket IPSocket) throws IOException {
        IPSocket.setSoTimeout(1);
        BTN = new BlueToNonBlue(BTSocket, IPSocket);
        NTB = new NonBlueToBlue(IPSocket, BTSocket);

    }
    public void start(){
        BTN.start();
        NTB.start();

        NTB.setup(BTN);
        BTN.setup(NTB);
    }
    public void join(){

        try {
            BTN.join();
        } catch (InterruptedException e) {
            NTB.interrupt();
            e.printStackTrace();
        }
        try {
            NTB.join();
        } catch (InterruptedException e) {
            BTN.interrupt();
            e.printStackTrace();
        }
    }

    public void stop(){
        BTN.interrupt();
        NTB.interrupt();
    }

    private class BlueToNonBlue extends Thread{
        private byte[] reply = new byte[4096];
        private Boolean Interrupted = false;
        private BluetoothSocket mbtsocket;
        private Socket mipsocket;
        private InputStream istream;
        private OutputStream ostream;
        private NonBlueToBlue OTHREAD;

        public BlueToNonBlue(BluetoothSocket btsocket, Socket ipsocket) throws IOException {
            mbtsocket = btsocket;
            mipsocket = ipsocket;
            istream = btsocket.getInputStream();
            ostream = ipsocket.getOutputStream();

        }

        public void setup(NonBlueToBlue t){
            OTHREAD = t;
        }
        public void interrupt(){
            Interrupted = true;
        }

        public void run(){

            while (true){
                try {

                    if (mipsocket.isClosed()){
                        interrupt();
                    }
                    if (!mbtsocket.isConnected() || !mipsocket.isConnected()){
                        Log.d("BTN","some socket closed");
                    }

                    if (Interrupted){
                        break;
                    }

                    int bytesRead;
                    bytesRead = istream.read(reply, 0,4096);
                    if (bytesRead == -1){
                        interrupt();
                        break;
                    }
                    ostream.write(reply, 0, bytesRead);
                } catch(SocketTimeoutException e){

                    continue;
                }
                catch (IOException e) {
                    if (e.toString().equals("java.io.IOException: bt socket closed, read return: -1")){
                        interrupt();
                    }
                    Log.d("BTN IOException",e.toString());

                e.printStackTrace();
            }}
            Log.d("BTN","finally stopping");
            OTHREAD.interrupt();
            try {
                mipsocket.close();
                mbtsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class NonBlueToBlue extends Thread{
        private byte[] reply = new byte[4096];
        private Boolean Interrupted = false;
        private BluetoothSocket mbtsocket;
        private Socket mipsocket;
        private InputStream istream;
        private OutputStream ostream;
        private BlueToNonBlue OTHREAD;

        public NonBlueToBlue(Socket ipsocket, BluetoothSocket btsocket) throws IOException {
            mbtsocket = btsocket;
            mipsocket = ipsocket;
            istream = ipsocket.getInputStream();
            ostream = btsocket.getOutputStream();
        }

        public void setup(BlueToNonBlue t){
            OTHREAD = t;
        }

        public void interrupt(){
            Interrupted = true;
        }

        public void run(){

            while (true){
                try{


                    if (!mbtsocket.isConnected() || !mipsocket.isConnected()){
                        Log.d("NTB","some socket closed");
                        interrupt();
                    }
                    if (Interrupted){
                        break; }


                    int bytesRead;
                    bytesRead = istream.read(reply, 0, 4096);
                    if (bytesRead == -1){
                        interrupt();
                        break;
                    }
                    ostream.write(reply, 0, bytesRead);

                }catch (SocketTimeoutException e){
                    continue;
                }catch (IOException e) {
                    if (e.toString().equals("java.io.IOException: bt socket closed, read return: -1")){
                        interrupt();
                    }
                    e.printStackTrace();
                }
            }
            Log.d("NTB","finally stopping");
            OTHREAD.interrupt();
            try {
                mipsocket.close();
                mbtsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
