package at.gtec.extendixdrone;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.ARSDK;

import java.util.ArrayList;
import java.util.List;

import at.gtec.droneapi.ParrotConnectionStates;
import at.gtec.droneapi.ParrotDrone;
import at.gtec.droneapi.ParrotEventListener;
import at.gtec.droneapi.ParrotFlyingStates;
import at.gtec.droneapi.ParrotVideoView;
import at.gtec.extendixitemreceiver.ExtendiXItemReceiver;

public class extendixUI extends Activity implements View.OnClickListener {

    //region Constant Members...

    private static String TxtConnectionState = "Connection State: ";
    private static String TxtFlightState = "Flight State: ";
    private static String TxtBatteryLevel = "Battery Level: ";
    private static String TxtConnect = "Connect";
    private static String TxtDisconnect = "Disconnect";
    private static String TxtStart = "Start";
    private static String TxtStop = "Stop";
    private static String ExtendiXPrefix = "KEY_";
    private static byte DeltaTilt=5;
    private static byte DeltaPan=5;
    private static int ManeuverDuration = 2000;
    private static byte ManeuverSpeedVerySlow = 10;
    private static byte ManeuverSpeedSlow = 20;
    private static byte ManeuverSpeedFast = 40;
    private static byte ManeuverSpeedMax = 100;
    //endregion

    //region Private Members...
    /**
     * The current UI
     */
    private extendixUI _currentUI;
    /**
     * The variable referring to the spinner in the user interface.
     */
    private Spinner _spnAvailableDevices = null;
    /**
     * List with the received commands.
     */
    private ListView _lvLogBox = null;
    /**
     * Editable box containing the port number.
     */
    private EditText _etPort = null;
    /**
     * Label containing the current battery level.
     */
    private TextView _txtBatteryLevel = null;
    /**
     * Label containing the current connection state.
     */
    private TextView _txtConnectionState = null;
    /**
     * Label containing the current connection state.
     */
    private TextView _txtFlyingState = null;
    /**
     * The button starting or sopping the intendix receiver.
     */
    private Button _btnReceiver = null;
    /**
     * The button used for connecting or disconnecting the drone.
     */
    private Button _btnDroneConnect = null;
    /**
     * The button stopping all motors of the drone.
     */
    private Button _btnEmergencyStop = null;
    /**
     * The video view containing the video of the drone.
     */
    private ParrotVideoView _pvvVideo = null;
    /**
     * The object referring to the CommandsDrone library.
     */
    private DroneCommands _droneCommands = null;
    /**
     * The object referring to the ExtendiXItemReceiver library.
     */
    private ExtendiXItemReceiver _receiver =null;
    /**
     * The object referring to the ParrotDrone library.
     */
    private  ParrotDrone _drone=null;
    /**
     * List containing available device names.
     */
    private List<String> _availableDevices = null;
    /**
     * Value containing information whether the drone is connected or not.
     */
    private boolean _connectionState = false;
    /**
     * Value containing information whether the receiver is started or not.
     */
    private boolean _receivingState = false;
    /**
     * The List containing entrys for the message log.
     */
    private List<String> _messageLogList = null;
    /**
     * The current tilt value.
     */
    private byte _tiltValue = 0;
    /**
     * The current pan value.
     */
    private byte _panValue = 0;

    private Button _btnTakePicture = null;
    private Button _btnTakeVideo = null;

    private Button _btnTakeOff = null;
    private Button _btnLand = null;

    private Button _btnForward = null;
    private Button _btnRotateRight = null;
    private Button _btnRotateLeft = null;
    private Button _btnShake = null;

    private EditText _etRtSpeed = null;
    private EditText _etRtDuration = null;
    private EditText _etFwSpeed = null;
    private EditText _etFwDuration = null;

    //endregion

    //region Interfaces...

    /**
     * The extendiX item receiver event listener.
     */
    private ExtendiXItemReceiver.OnItemReceivedListener _extendiXEventListener = new ExtendiXItemReceiver.OnItemReceivedListener() {
        @Override
        public void OnItemUnknownUDPReceived(String message) {
            AddToLogBox("Unknown UDP message received.");
        }

        @Override
        public void OnItemUnknownIntendix(String message) {
            AddToLogBox("Unknown intendiX command received. Check if the command starts with '" + ExtendiXPrefix + "'.");
        }

        @Override
        public void OnItemReceived(String message) {
            try
            {
                switch (message) {
                    case "KEY_TAKE_OFF":
                        DroneCommands.DroneTakeOff(_drone);
                        break;
                    case "KEY_LAND":
                        DroneCommands.DroneLand(_drone);
                        break;
                    case "KEY_LEFT": {
                        DroneCommands.DroneTurn(_drone, (byte)-ManeuverSpeedSlow, ManeuverDuration);
                        break;
                    }
                    case "KEY_LEFT_FAST": {
                        DroneCommands.DroneTurn(_drone, (byte)-ManeuverSpeedFast, ManeuverDuration);
                        break;
                    }
                    case "KEY_RIGHT": {
                        DroneCommands.DroneTurn(_drone, ManeuverSpeedSlow, ManeuverDuration);
                        break;
                    }
                    case "KEY_RIGHT_FAST": {
                        DroneCommands.DroneTurn(_drone, ManeuverSpeedFast, ManeuverDuration);
                        break;
                    }
                    case "KEY_FORWARD": {
                        DroneCommands.DroneMove(_drone, getForwardSpeed(), getForwardDuration());
                        break;
                    }
                    case "KEY_FORWARD_FAST": {
                        DroneCommands.DroneMove(_drone, getForwardSpeed(), getForwardDuration());
                        break;
                    }
                    case "KEY_BACK": {
                        DroneCommands.DroneMove(_drone, (byte)-ManeuverSpeedSlow, ManeuverDuration);
                        break;
                    }
                    case "KEY_BACK_FAST": {
                        DroneCommands.DroneMove(_drone, (byte)-ManeuverSpeedFast, ManeuverDuration);
                        break;
                    }
                    case "KEY_TURN_LEFT": {
                        DroneCommands.DroneRotate(_drone, (byte)-getRotationSpeed(), getRotationDuration());
                        break;
                    }
                    case "KEY_TURN_RIGHT": {
                        DroneCommands.DroneRotate(_drone, getRotationSpeed(), getRotationDuration());
                        break;
                    }
                    case "KEY_TAKE_PICTURE": {
                        DroneCommands.DroneTakePicture(_drone);
                        break;
                    }
                    case "KEY_START_VIDEO": {
                        DroneCommands.DroneVideoStream(_drone);
                        break;
                    }
                    case "KEY_STOP_VIDEO": {
                        DroneCommands.DroneVideoStopStream(_drone);
                        break;
                    }
                    case "KEY_CAMERA_UP": {
                        _tiltValue+=DeltaTilt;
                        DroneCommands.DroneChangeCameraOrientation(_drone,_tiltValue,_panValue);
                        break;
                    }
                    case "KEY_CAMERA_DOWN": {
                        _tiltValue-=DeltaTilt;
                        DroneCommands.DroneChangeCameraOrientation(_drone,_tiltValue,_panValue);
                        break;
                    }
                    case "KEY_CAMERA_LEFT":{
                        _panValue-=DeltaPan;
                        DroneCommands.DroneChangeCameraOrientation(_drone,_tiltValue,_panValue);
                        break;
                    }
                    case "KEY_CAMERA_RIGHT":{
                        _panValue+=DeltaPan;
                        DroneCommands.DroneChangeCameraOrientation(_drone,_tiltValue,_panValue);
                        break;
                    }
                    case "KEY_CAMERA_POSITION_RESET":{
                        _panValue=0;
                        _tiltValue=0;
                        DroneCommands.DroneChangeCameraOrientation(_drone,_tiltValue,_panValue);
                        break;
                    }
                    case "KEY_SHAKE":{
                        shake();
                        break;
                    }
                    default:
                        message = "Unknown Command.";
                        break;
                }

                AddToLogBox(message);
            }
            catch (Exception ex)
            {
                AddToLogBox("Could not execute command.");
            }
        }
    };

    /**
     * The event listener of the drone.
     */
    private ParrotEventListener _droneEventListener = new ParrotEventListener()
    {
        @Override
        public void OnDevicesAvailable(List<String> devices)
        {
            _availableDevices = devices;
            if(devices != null && devices.size()>0)
            {
                ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(_currentUI, android.R.layout.simple_spinner_item, _availableDevices);
                _spnAvailableDevices.setAdapter(itemsAdapter);

                _btnDroneConnect.setEnabled(true);
            }
        }

        @Override
        public void OnConnectionStateChanged(ParrotConnectionStates parrotConnectionStates) {

            switch (parrotConnectionStates) {
                case CONNECTED:
                    _txtConnectionState.setText(TxtConnectionState + "\nConnected");
                    _connectionState = true;

                    _btnEmergencyStop.setEnabled(true);
                    _txtBatteryLevel.setVisibility(View.VISIBLE);
                    break;
                case CONNECTING:
                    _txtConnectionState.setText(TxtConnectionState + "\nConnecting");
                    _connectionState = false;
                    break;
                case DISCONNECTING:
                    _txtConnectionState.setText(TxtConnectionState + "\nDisconnecting");
                    _connectionState = false;
                    break;
                case DISCONNECTED:
                    _txtConnectionState.setText(TxtConnectionState + "\nDisconnected");
                    _connectionState = false;
                    break;
                case UNKNOWN:
                    _txtConnectionState.setText(TxtConnectionState + "\nUnknown connection state");
                    _connectionState = false;
                    break;
                default:
                    _txtConnectionState.setText(TxtConnectionState + "\nUnknown connection state");
                    _connectionState = false;
                    break;
            }
        }

        @Override
        public void OnFlyingStateChanged(ParrotFlyingStates parrotFlyingStates) {
            switch (parrotFlyingStates)
            {
                case UNKNOWN:
                    _txtFlyingState.setText(TxtFlightState + "\nUnknown flight state");
                    break;
                case LANDED:
                    _txtFlyingState.setText(TxtFlightState + "\nLanded");
                    break;
                case TAKING_OFF:
                    _txtFlyingState.setText(TxtFlightState + "\nTaking off");
                    break;
                case HOVERING:
                    _txtFlyingState.setText(TxtFlightState + "\nHovering");
                    break;
                case FLYING:
                    _txtFlyingState.setText(TxtFlightState + "\nFlying");
                    break;
                case LANDING:
                    _txtFlyingState.setText(TxtFlightState + "\nLanding");
                    break;
                case EMERGENCY:
                    _txtFlyingState.setText(TxtFlightState + "\nEmergyncy");
                    break;
                case USERTAKEOFF:
                    _txtFlyingState.setText(TxtFlightState + "\nUser take off");
                    break;
                case RAMPING:
                    _txtFlyingState.setText(TxtFlightState + "\nMotor Ramping");
                    break;
                case EMERGENCY_LANDING:
                    _txtFlyingState.setText(TxtFlightState + "\nEmergency Landing");
                    break;
            }
        }

        @Override
        public void OnBatteryLevelChanged(int i) {
            _txtBatteryLevel.setText(TxtBatteryLevel + "\n" + Integer.toString(i));
        }
    };

    //endregion

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnConnect: {
                    if(_connectionState)
                    {
                        _drone.Close();
                        _btnDroneConnect.setText(TxtConnect);
                        AddToLogBox("Disconnected from drone.");
                    }
                    else
                    {
                        String deviceName = _spnAvailableDevices.getSelectedItem().toString();
                        _drone.Open(deviceName);
                        _btnDroneConnect.setText(TxtDisconnect);
                        AddToLogBox("Connected to " + deviceName + ".");
                    }
                    break;
                }
                case R.id.btnEmergencyStop: {
                    _drone.EmergencyLanding();
                    break;
                }
                case R.id.btnReceiver: {
                    if(_receivingState)
                    {
                        _receiver.EndReceiving();
                        _receivingState = false;
                        _btnReceiver.setText(TxtStart);
                        AddToLogBox("Stopped listening for intendiX messages.");
                    }
                    else
                    {
                        String portTemp = _etPort.getText().toString();
                        _receiver.BeginReceiving(Integer.parseInt(portTemp));
                        _receivingState = true;
                        _btnReceiver.setText(TxtStop);
                        AddToLogBox("Listening for intendiX messages on port " + portTemp +".");
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (Exception ex) {
            ShowToast(ex.getMessage());
        }
    }

    /**
     * The entry point of the main program.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extendix_ui);
        _currentUI = this;

        //freeze orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //initialize drone application
        ARSDK.loadSDKLibs();

        //get UI elements
        _etPort = (EditText)findViewById(R.id.etPort);
        _spnAvailableDevices = (Spinner) findViewById(R.id.spnDevices);
        _lvLogBox = (ListView) findViewById(R.id.lvLog);
        _txtBatteryLevel = (TextView)findViewById(R.id.txtBatteryLevel);
        _txtConnectionState= (TextView)findViewById(R.id.txtConnectionState);
        _txtFlyingState = (TextView)findViewById(R.id.txtFlyingState);
        _btnReceiver= (Button) findViewById(R.id.btnReceiver);
        _btnDroneConnect=  (Button) findViewById(R.id.btnConnect);
        _btnEmergencyStop = (Button)findViewById(R.id.btnEmergencyStop);
        _pvvVideo = (ParrotVideoView)findViewById(R.id.pvvVideo);
        _btnTakePicture = (Button)findViewById(R.id.btnTakePicture);
        _btnTakeVideo = (Button)findViewById(R.id.btnTakeVideo);

        _btnTakeOff = (Button)findViewById(R.id.btnTakeOff);
        _btnLand = (Button)findViewById(R.id.btnLand);

        _btnRotateLeft = (Button)findViewById(R.id.btnRotateLeft);
        _btnRotateRight = (Button)findViewById(R.id.btnRotateRight);
        _btnForward = (Button)findViewById(R.id.btnForward);
        _btnShake = (Button)findViewById(R.id.btnShake);

        _etRtSpeed = (EditText)findViewById(R.id.etRotationSpeed);
        _etRtDuration = (EditText)findViewById(R.id.etRotationDuration);
        _etFwSpeed = (EditText)findViewById(R.id.etForwardSpeed);
        _etFwDuration = (EditText)findViewById(R.id.etForwardDuration);

        _btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneTakePicture(_drone);
                ShowToast("Take!!");
            }
        });

        _btnTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneVideoStream(_drone);
                ShowToast("Take!!");
            }
        });

        _btnTakeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneTakeOff(_drone);
                ShowToast("TakeOff");
            }
        });

        _btnLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneLand(_drone);
                ShowToast("Land");
            }
        });

        _btnShake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // roneCommands.Shake(_drone, (byte)70, 300, 4);
                shake();
                ShowToast("shake");
            }
        });

        // Rotation and Forward

        _btnRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneRotate(_drone, getRotationSpeed(), getRotationDuration());
                ShowToast("Rotate Right");
            }
        });

        _btnRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneRotate(_drone, (byte)-getRotationSpeed(), getRotationDuration());
                ShowToast("Rotate Left");
            }
        });

        _btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneCommands.DroneMove(_drone, getForwardSpeed(), getForwardDuration());
                ShowToast("Forward");
            }
        });

        //add button events
        _btnDroneConnect.setOnClickListener(this);
        _btnEmergencyStop.setOnClickListener(this);
        _btnReceiver.setOnClickListener(this);

        //set initial values for ui elements
        _btnDroneConnect.setEnabled(false);
        _btnEmergencyStop.setEnabled(false);
        _txtBatteryLevel.setVisibility(View.INVISIBLE);
        _txtFlyingState.setText(TxtFlightState + "\nLanded");
        _txtConnectionState.setText(TxtConnectionState + "\nDisconnected");
        _btnReceiver.setText(TxtStart);
        _btnDroneConnect.setText(TxtConnect);

        //initialize device list
        List<String> deviceTmp = new ArrayList<>();
        deviceTmp.add("Any device available");
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,deviceTmp);
        _spnAvailableDevices.setAdapter(itemsAdapter);

        //initialize received commands list
        _messageLogList = new ArrayList<>();

        //Try to initialize extendiX item receiver and attach eventlistener
        try {
            _receiver = new ExtendiXItemReceiver(ExtendiXPrefix , getApplicationContext());
            _receiver.AttachEventListener(_extendiXEventListener);
        } catch (Exception e) {
            ShowToast("Could not initialize connection with intendiX. Error:" + e.getMessage());
        }

        //Try to initialize the drone
        try{
            _drone= new ParrotDrone(getApplicationContext(),_pvvVideo);
            _drone.AttachEventListener(_droneEventListener);
        } catch (Exception e){
            ShowToast("Could not initialize connection with the drone. Error:" + e.getMessage());
        }

        //Try to initialize the drone
        try{
            _drone.StartScanningForDevices();
        } catch (Exception e){
            ShowToast("Could not start scanning for devices. Error:" + e.getMessage());
        }
    }

    private void shake() {
        Handler mHandler = new Handler();
        final byte speed = 80;
        final int duration = 500;
        final int margin = 200;
        final int count = 10;
        final Runnable rotateRight = new Runnable() {
            @Override
            public void run() {
                DroneCommands.DroneRotate(_drone, speed, duration);
            }
        };
        final Runnable rotateLeft = new Runnable() {
            @Override
            public void run() {
                DroneCommands.DroneRotate(_drone, (byte)-speed, duration);
            }
        };

        for (int i=0; i<count; i++) {
            mHandler.postDelayed(rotateRight, (duration+margin)*(i*2));
            mHandler.postDelayed(rotateLeft, (duration+margin)*(i*2 + 1));
        }
    }

    private void swing() {
        Handler mHandler = new Handler();
        final byte speed = 40;
        final int duration = 700;
        final int margin = 200;
        final int count = 10;
        final Runnable moveRight = new Runnable() {
            @Override
            public void run() {
                DroneCommands.DroneMove(_drone, speed, duration);
            }
        };
        final Runnable moveLeft = new Runnable() {
            @Override
            public void run() {
                DroneCommands.DroneMove(_drone, (byte)-speed, duration);
            }
        };
        final Runnable land = new Runnable() {
            @Override
            public void run() {
                DroneCommands.DroneLand(_drone);
            }
        };

        for (int i=0; i<count; i++) {
            mHandler.postDelayed(moveRight, (duration+margin)*(i*2));
            mHandler.postDelayed(moveLeft, (duration+margin)*(i*2 + 1));
        }
        mHandler.postDelayed(land, (duration+margin)*(count*2));
    }

    private byte getRotationSpeed() {
        return (byte)Integer.parseInt(_etRtSpeed.getText().toString());
    }

    private int getRotationDuration() {
        return Integer.parseInt(_etRtDuration.getText().toString());
    }

    private byte getForwardSpeed() {
        return (byte)Integer.parseInt(_etFwSpeed.getText().toString());
    }

    private int getForwardDuration() {
        return Integer.parseInt(_etFwDuration.getText().toString());
    }

    private void AddToLogBox(String message)
    {
        _messageLogList.add(message);
        ArrayAdapter mAdapter = new ArrayAdapter(_currentUI,android.R.layout.simple_list_item_1, _messageLogList);
        _lvLogBox.setAdapter(mAdapter);
        _lvLogBox.setSelection(_lvLogBox.getCount() - 1);
    }

    /**
     * Creates a toast and displays it.
     * @param message The message to display.
     */
    private void ShowToast(String message)
    {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
