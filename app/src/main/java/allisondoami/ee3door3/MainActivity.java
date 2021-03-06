package allisondoami.ee3door3;

import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity implements SensorEventListener {
    float[] history = new float[2];
    int[] direction = {5};
    Toast correctPassword;
    Toast incorrectPassword;
    Toast showNotification;
    Toast leftMove;
    Toast rightMove;
    Toast btConnected_toast;
    Boolean sensorOn = false;
    Button setPasswordButton;
    Button enterPasswordButton;
    int savedPassword[] = {3, 3, 3, 3};
    FragmentManager fm = getFragmentManager();
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;

    int entries[] = {0, 0, 0, 0};
    int entryNum = 0;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket socket;
    OutputStream outputStream;
    InputStream inputStream;

    public enum REQUEST_CODES {
        REQUEST_ENABLE_BT(1);

        private final int value;

        //set all enum values to ints
        REQUEST_CODES(int value) {
            this.value = value;
        }
        public final int getInt() { //getter
            return value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        setContentView(R.layout.activity_main);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Locate the button in activity_main.xml
        setPasswordButton = (Button) findViewById(R.id.button);
        enterPasswordButton = (Button) findViewById(R.id.button2);

        //Toast
        // Context context = this.getContext();
        CharSequence text = "Correct Password, Door Opened";
        CharSequence text2 = "Incorrect Password";
        CharSequence text3 = "Please enter password again";
        CharSequence text4 = "You moved right";
        CharSequence text5 = "You moved left";
        CharSequence btConnected_text = "You are connected!";
        int duration = Toast.LENGTH_SHORT;

        correctPassword = Toast.makeText(this, text, duration);
        incorrectPassword = Toast.makeText(this, text2, duration);
        showNotification = Toast.makeText(this,text3, duration);
        rightMove = Toast.makeText(this, text4, duration);
        leftMove = Toast.makeText(this, text5, duration);
        btConnected_toast = Toast.makeText(this, btConnected_text, duration);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast no_bt_toast = Toast.makeText(this, "Bluetooth is not supported on this device.", Toast.LENGTH_LONG);
            no_bt_toast.show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CODES.REQUEST_ENABLE_BT.getInt());
        }
        //set up the device list so we can populate it with bluetooth devices we find.
        ArrayList<String> listItems = new ArrayList<>();
        ArrayAdapter<String> adapter;

        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if(iterator.getAddress().equals(Globals.DOOR_ADDRESS)){
                    try {
                        socket = iterator.createRfcommSocketToServiceRecord(Globals.BT_UUID);
                        socket.connect();
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        btConnected_toast.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Capture button clicks
        setPasswordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MyDialogFragment alertdFragment = new MyDialogFragment();
                // Show Alert DialogFragment
                alertdFragment.show(fm, "Set Password");
            }
        });


        enterPasswordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            sensorOn = true;
            entries[entryNum] = direction[0];
            entryNum++;
            byte write_buffer[] = {1}; 
            //entries is full. check for correctness.
            if(entryNum >= 4) {
                entryNum = 0;
                boolean valid = true;
                for(int i = 0; i < 4; i++) {
                    if(entries[i] != savedPassword[i]) {
                        valid = false;
                        write_buffer[0] = 0;
                    }
                }
                if(valid) {
                    correctPassword.show();
                } else {
                    incorrectPassword.show();
                }

                try {
                    outputStream.write(write_buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } //end if(entries are full)
            } //end onclick callback function
        }); //end onclick listener setup
    } //end oncreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODES.REQUEST_ENABLE_BT.getInt()) {
            //bluetooth was enabled
            if(resultCode == RESULT_OK) {
                Log.v("Bluetooth", "Bluetooth enabled.");
            }
            else {
                Log.v("Bluetooth", "Bluetooth not enabled.");
                Toast tempToast = Toast.makeText(this, "Please Enable Bluetooth.", Toast.LENGTH_LONG);
                tempToast.show();
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.v("control flow", "onsensorchanged called");
        float xChange = history[0] - event.values[0];
        history[0] = event.values[0];
        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > 1000) {
            lastUpdate = curTime;
            if (xChange > 2) {
                direction[0] = 0;
                leftMove.show();
                Log.v("Dir", "Left");
            } else if (xChange < -2) {
                direction[0] = 1;
                rightMove.show();
                Log.v("Dir", "Right");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void savePassword(int passedInPassword[]) {
        for (int i = 0; i < 4; i++) {
            savedPassword[i] = passedInPassword[i];
            String p = "" + savedPassword[i];
            Log.v("SavedPassword", p);
        }
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}

