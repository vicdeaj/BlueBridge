package com.vik0t0r.bluebridge;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int BLUECODE = 888;
    private BluetoothAdapter Ba = null;
    private BluetoothDevice mDevice = null;
    SharedPreferences sharedPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ba = BluetoothAdapter.getDefaultAdapter();

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        String deviceMac = sharedPref.getString("BluetoothDevice","");
        if (!deviceMac.isEmpty()){
            mDevice = Ba.getRemoteDevice(deviceMac);
            Button SelectButton = (Button) findViewById(R.id.button3);
            SelectButton.setText(mDevice.getName());

        }


        // Check if bluetooth is disposable
        if (Ba == null){
            Log.i(TAG,"Bluetooth not disposable");
            ErrorDialog(true,"Bluetooth is not available on this device");
        } else{
            //Activate Bluetooth if it isn't
            if(!Ba.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BLUECODE);
            }
        }
    }


    public void startService(View v){
        if (mDevice != null) {
            Ba.cancelDiscovery();
            Intent i = new Intent(v.getContext(), Bridge.class);
            Bundle b = new Bundle();
            b.putParcelable("BluetoothDevice",mDevice);
            i.putExtras(b);
            startService(i);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select a device first");
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }
    public void stopService(View v){
        stopService(new Intent(v.getContext(), Bridge.class));
    }
    public void Select(View view){
        final List<BluetoothDevice> pairedDevices = new ArrayList<BluetoothDevice>(Ba.getBondedDevices());
        final String[] macs = new String[pairedDevices.size()];

        if (pairedDevices.size() > 0){
            Log.d(TAG, "paired devices > 0");
            //CREATE ARRAY SO CAN ACCES BY Index
            //final String[] macs= pairedDevices.keySet().toArray(new String[pairedDevices.size()]);
            int i = 0;
            for (BluetoothDevice device : pairedDevices){
                macs[i] = device.getName() + " \n   " + device.getAddress();
                i = i +1;
            }

            //Generate AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose device to connect");
            builder.setItems(macs, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDevice = pairedDevices.get(which);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("BluetoothDevice",mDevice.getAddress());
                    editor.apply();
                    //Set text to match mac
                    Button SelectButton = (Button) findViewById(R.id.button3);
                    SelectButton.setText(mDevice.getName());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            ErrorDialog(false,"No paired Devices detected");
        }

    }

    private void ErrorDialog(final Boolean kill, String msg){
        //helper function for spawning error dialogs
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.error_dialog);
        dialog.setCancelable(false);

        // set the custom dialog components - text and button
        //TextView dialogtitle = (TextView) dialog.findViewById(R.id.txtDiaTitle);
        TextView dialogmsg = (TextView) dialog.findViewById(R.id.txtDiaMsg);
        dialogmsg.setText(msg);
        Button dialogButton = (Button) dialog.findViewById(R.id.btnOk);

        // if button is clicked, kill activity
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (kill){
                    finish();
                }}
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("resultcode",String.valueOf(resultCode));
        Log.d("requestcode",String.valueOf(requestCode));
        //be a fucking pesado
        if ( (requestCode == BLUECODE) & (resultCode == 0)){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUECODE);
        }
    }
}