package gr.gkortsaridis.arduinobluetoothmanager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "bluetooth1";
    private static String address = "00:13:02:25:92:35";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    Button five;
    Button four;
    Button one;
    private OutputStream outStream = null;
    Button three;
    Button two;
    Button zero;
    Button six,seven;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.zero = (Button) findViewById(R.id.zeroBtn);
        this.one = (Button) findViewById(R.id.oneBtn);
        this.two = (Button) findViewById(R.id.twoBtn);
        this.three = (Button) findViewById(R.id.threeBtn);
        this.four = (Button) findViewById(R.id.fourBtn);
        this.five = (Button) findViewById(R.id.fiveBtn);
        this.six = (Button) findViewById(R.id.sixBtn);
        this.seven = (Button) findViewById(R.id.sevenBtn);
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    public void zero(View v) {
        sendData("0");
        buttons();
    }

    public void one(View v) {
        sendData("1");
        buttons();
    }

    public void two(View v) {
        sendData("2");
        buttons();
    }

    public void three(View v) {
        sendData("3");
        buttons();
    }

    public void four(View v) {
        sendData("4");
        buttons();
    }

    public void five(View v) {
        sendData("5");
        buttons();
    }

    public void six(View v) {
        sendData("6");
        buttons();
    }

    public void seven(View v) {
        sendData("7");
        buttons();
    }

    private void buttons() {
        this.zero.setEnabled(false);
        this.one.setEnabled(false);
        this.two.setEnabled(false);
        this.three.setEnabled(false);
        this.four.setEnabled(false);
        this.five.setEnabled(false);
        this.six.setEnabled(false);
        this.seven.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                MainActivity.this.zero.setEnabled(true);
                MainActivity.this.one.setEnabled(true);
                MainActivity.this.two.setEnabled(true);
                MainActivity.this.three.setEnabled(true);
                MainActivity.this.four.setEnabled(true);
                MainActivity.this.five.setEnabled(true);
                MainActivity.this.six.setEnabled(true);
                MainActivity.this.seven.setEnabled(true);
            }
        }, 5000);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (VERSION.SDK_INT >= 10) {
            try {
                return (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class}).invoke(device, new Object[]{MY_UUID});
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    public void onResume() {
        super.onResume();
        try {
            this.btSocket = createBluetoothSocket(this.btAdapter.getRemoteDevice(address));
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }
        this.btAdapter.cancelDiscovery();
        try {
            this.btSocket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                this.btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
        Log.d(TAG, "...Create Socket...");
        try {
            this.outStream = this.btSocket.getOutputStream();
        } catch (IOException e3) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e3.getMessage() + ".");
        }
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "...In onPause()...");
        if (this.outStream != null) {
            try {
                this.outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }
        try {
            this.btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        if (this.btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else if (this.btAdapter.isEnabled()) {
            Log.d(TAG, "...Bluetooth ON...");
        } else {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        Log.d(TAG, "...Send data: " + message + "...");
        try {
            this.outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00")) {
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            }
            errorExit("Fatal Error", msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n");
        }
    }
}