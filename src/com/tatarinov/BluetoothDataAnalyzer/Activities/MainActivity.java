package com.tatarinov.BluetoothDataAnalyzer.Activities;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.tatarinov.BluetoothDataAnalyzer.CameraHelper;
import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;
import com.tatarinov.BluetoothDataAnalyzer.IntentRequestCodes;
import com.tatarinov.BluetoothDataAnalyzer.SoundGenerator;
import com.tatarinov.BluetoothDataAnalyzer.Bluetooth.BluetoothConnectionState;
import com.tatarinov.BluetoothDataAnalyzer.Bluetooth.BluetoothMessages;
import com.tatarinov.BluetoothDataAnalyzer.Bluetooth.BluetoothService;
import com.tatarinov.BluetoothDataAnalyzer.UI.GraphView;
import com.tatarinov.BluetoothDataAnalyzer.UI.ToggleImageButtonWrapper;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsLocationService;
import com.tatarinov.BluetoothDataAnalyzer.Graph.DiscriminatorTypes;
import com.tatarinov.BluetoothDataAnalyzer.Graph.GraphWorkMode;
import com.tatarinov.BluetoothDataAnalyzer.Graph.Point;
import com.tatarinov.BluetoothDataAnalyzer.UI.NumberPickerWrapper;
import com.tatarinov.BluetoothDataAnalyzer.R;

public class MainActivity extends Activity {    
    public static final String kDeviceName = "device_name";
    public static final String kToast = "toast";    
    private static final String kTag = MainActivity.class.getSimpleName();
    
    private GlobalPreferences mPreferences;    
    
    private CameraHelper mCameraHelper;
    private NumberPickerWrapper mThresholdPicker;
    private NumberPickerWrapper mSensitivityPicker;
    private NumberPickerWrapper mSoundVolumePicker;
    private ToggleImageButtonWrapper mDiscriminatorToggler;
    private ToggleImageButtonWrapper mStaticDynamicToggler;    
    private PopupMenu mPopupMenu;
      
    private Toast mToastObject;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
    private String mConnectedDeviceInfo = null;     
    
    private GraphView mGraphView;    
    
    private int mZeroValue;   
    private int mThresholdZeroCounter;    
    
    private long mStartTime;
    private int mLastRawLevelValue;
    private int mLimitOverflow;
    
    private Timer mTimer;
    private TimerTask mTimerTask;
    
    private AudioManager mAudioManager;
        
    private TextView mBatteryChargeTextView;
    private TextView mSensorChargeTextView;
    private static TextView mCurrentValueTextView;
    
    private int soundPickerVolume;
    
    public static void setCurrentValue(double val){
    	mCurrentValueTextView.setText(String.format("Current value: %d", (int)val));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// cache preferences helper class
    	this.mPreferences = GlobalPreferences.getInstance(getApplicationContext());
    	if (this.mPreferences.isDebug()) {
            Log.d(kTag, "OnCreate method");
        }    	    
    	
    	super.onCreate(savedInstanceState);    	
    	    	
    	this.mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    	// set window params
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);      
        setContentView(R.layout.main);

    	this.mStartTime = (new java.util.Date()).getTime();
    	
    	PreferencesActivity.setBrightness(this, this.mPreferences.getScreenBrightness());
    	SoundGenerator.create(this);        
        this.mCameraHelper = new CameraHelper(getBaseContext());
    	
        this.createUserInterface();        

        // start bluetooth adapter
    	this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {              	
            this.mToastObject.setText(this.getString(R.string.bt_not_enabled_leaving));
            this.mToastObject.show();              
            finish();            
        }                             
    }                
    
    private void onTimeProcess(){
    	int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);    	    	
    	if (curVolume != soundPickerVolume){
    		soundPickerVolume = curVolume;
    		SoundGenerator.getInstance().SetMute(soundPickerVolume == 0);
    		final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);   
    		float v = curVolume;    		
    		mSoundVolumePicker.setValue((int) ((v/maxVolume)*100));						        		
    	}    	
    	this.mThresholdPicker.timeProcess();
    	this.mSensitivityPicker.timeProcess();
    	this.mSoundVolumePicker.timeProcess();    	
    	this.mGraphView.timeProcess();    
    }    

    @SuppressLint("ShowToast")
	private void createUserInterface() {
    	this.mPopupMenu = new PopupMenu(this, this.findViewById(R.id.settingsButton));
    	this.mPopupMenu.getMenuInflater().inflate(R.menu.option_menu, this.mPopupMenu.getMenu());
    	
    	this.mGraphView = new GraphView();
    	this.mGraphView.setMode(this.mPreferences.getGraphWorkMode());
    	this.mGraphView.setFillAreas(this.mPreferences.isFillAreas());    	        	    
    	
        final LinearLayout chartContainer = (LinearLayout) findViewById(R.id.graphChartContainer);                 
        chartContainer.addView(mGraphView.getView(this));        
       
        ViewTreeObserver vto = chartContainer.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onGlobalLayout() {            	            	
                chartContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        
        chartContainer.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View view) {
        		showPanels(true);
            }
        });
        
    	ImageButton btnSetZero = (ImageButton) findViewById(R.id.zeroButton);
        btnSetZero.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View view) {
        		setZeroValue();
                 
                if(mPreferences.isDebug()){
                	String toastMessage = String.format(Locale.getDefault(), 
                			"Set a new zero value: %d", mZeroValue);
             		mToastObject.setText(toastMessage);
             		mToastObject.show();
                }     
            }
        });

        ImageButton settings = (ImageButton) findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	onPreparePopupMenu();            	
            	mPopupMenu.show();            	
            }
        });
        this.mPopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
            	switch(item.getItemId()){            	            	 
                	case R.id.menu_item_scan:
                		// Launch the DeviceListActivity to see devices and do scan
                		showActivityById(R.id.menu_item_scan);                		
                		return true;

                	case  R.id.menu_item_monitor:
                		showActivityById(R.id.menu_item_monitor);                		
                		return true;
                		
                 	case  R.id.menu_item_gps:
                		showActivityById(R.id.menu_item_gps);                		
                		return true;

                	case R.id.menu_item_disconnect:
                		if (mBluetoothService != null) {                			
                			mBluetoothService.stop();                		
                			mToastObject.setText(getString(R.string.state_not_connected));
                			mToastObject.show();                    
                		}
                		return true;

                	case R.id.menu_item_settings:
                		showActivityById(R.id.menu_item_settings);                    
                		return true;
            	}            
            	return false;            
            }
        });	
        
        this.mToastObject = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
        
        this.mDiscriminatorToggler = new ToggleImageButtonWrapper((ImageButton)findViewById(R.id.colorBlackButton), 
        		new int[]{R.drawable.button_bw_normal, R.drawable.button_bw_black, R.drawable.button_bw_color}, R.drawable.button_bw_press);
        this.mDiscriminatorToggler.setState(mPreferences.getDiscriminatorType().ordinal());
        this.mDiscriminatorToggler.setStateChangeListener(new ToggleImageButtonWrapper.StateChangeListener(){
        	@Override
			public void onStateChange(int newState) {	        		
        		mPreferences.setDiscriminatorType(DiscriminatorTypes.values()[newState]);        		        	
			}
        });
        
        this.mStaticDynamicToggler = new ToggleImageButtonWrapper((ImageButton)findViewById(R.id.staticButton), 
        		new int[]{R.drawable.button_static_normal, R.drawable.button_static_press}, -1);          
        this.mStaticDynamicToggler.setState(mPreferences.getGraphWorkMode().ordinal());
        this.mStaticDynamicToggler.setStateChangeListener(new ToggleImageButtonWrapper.StateChangeListener(){
        	@Override
			public void onStateChange(int newState) {	        		
        		mPreferences.setGraphWorkMode(GraphWorkMode.values()[newState]);
        		mGraphView.setMode(mPreferences.getGraphWorkMode());
        		SoundGenerator.getInstance().stop();
			}
        });
        
        this.mThresholdPicker = new NumberPickerWrapper(this.mPreferences.getThreshold(), Integer.MAX_VALUE, 0);
        this.mThresholdPicker.init(this, R.id.thresholdPlusButton, R.id.thresholdMinusButton, R.id.thresholdText);
        this.mThresholdPicker.setChangeValueListener(new NumberPickerWrapper.ValueChangedListener() {			
			@Override
			public void onValueChange(int value, boolean i) {				
		        mPreferences.setThreshold(mThresholdPicker.getValue());		
		        
				String toastMessage = String.format(getString(R.string.threshold_changed)+": %d", value);
				mToastObject.setText(toastMessage);        		
				mToastObject.show();
			}
		});
        
        this.mSensitivityPicker = new NumberPickerWrapper(this.mPreferences.getSensitivity(), 99, 0);
        this.mSensitivityPicker.init(this, R.id.sensitivityPlusButton, R.id.sensitivityMinusButton, R.id.sensitivityText);
        this.mSensitivityPicker.setChangeValueListener(new NumberPickerWrapper.ValueChangedListener() {			
			@Override
			public void onValueChange(int value, boolean i) {		
				mPreferences.setSensitivity(mSensitivityPicker.getValue());
				
				String toastMessage = String.format(getString(R.string.sensitivity_changed)+": %d", value);
				mToastObject.setText(toastMessage);        		
				mToastObject.show();
			}
		});       
                
        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        soundPickerVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        float curVolume = soundPickerVolume;		
        if (soundPickerVolume > 0){        	        	
            SoundGenerator.getInstance().playTone(1500);
            SoundGenerator.getInstance().stop();        	
        } else {
        	SoundGenerator.getInstance().SetMute(true);
        }
                                    
        this.mSoundVolumePicker = new NumberPickerWrapper((int) ((curVolume/maxVolume)*100), 100, 0);
        this.mSoundVolumePicker.init(this, R.id.soundPlusButton, R.id.soundMinusButton, R.id.soundText);
        this.mSoundVolumePicker.setChangeValueListener(new NumberPickerWrapper.ValueChangedListener() {			
			@Override
			public void onValueChange(int value, boolean i) {
				
				if (i){
					mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM,
			                AudioManager.ADJUST_RAISE, 0);
				} else {
					mAudioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM,
			                AudioManager.ADJUST_LOWER, 0);
				}				
				float curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
				int val = (int) ((curVolume/maxVolume)*100);
				mSoundVolumePicker.setValue(val);							
			}
		});   
        
        initBatteryLevelService();
    }      
    
    private void showPanels(boolean show){
    	  LinearLayout leftPanel = (LinearLayout) findViewById(R.id.leftPanel);
    	  LinearLayout rightPanel = (LinearLayout) findViewById(R.id.rightPanel);
    	  
    	  if (show){
    		  if (leftPanel.getVisibility() != LinearLayout.VISIBLE){
    			  leftPanel.setVisibility(LinearLayout.VISIBLE);
        		  rightPanel.setVisibility(LinearLayout.VISIBLE);
    		  }    		  
    	  } else {
    		  leftPanel.setVisibility(LinearLayout.GONE);
    		  rightPanel.setVisibility(LinearLayout.GONE);
    	  }
    }

    public void setZeroValue() {
    	this.mZeroValue = this.mLastRawLevelValue;    	
    	this.mThresholdZeroCounter = 0;    	
    	this.mGraphView.clear();    	    		    	    	
    }    

    @Override
    public void onStart() {    
        super.onStart();        
        if (this.mPreferences.isDebug()) {
            Log.d(kTag, "OnStart method");
        }                    

        if (!this.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, IntentRequestCodes.REQUEST_ENABLE_BT);
        }

        if (this.mPreferences.isEnableAutoConnect() &&
                (this.mPreferences.getDeviceConnectAddress() != null &&
                        !this.mPreferences.getDeviceConnectAddress().isEmpty())) {
        	this.ConnectToDevice(this.mPreferences.getDeviceConnectAddress());
        }
    }
    
    private DiscriminatorTypes getCurrentDiscriminatorType(){
    	int state = this.mDiscriminatorToggler.getState();
    	return DiscriminatorTypes.values()[state];    	
    }
    
    @Override
    public void onStop() {
    	super.onStop();                
        this.mPreferences.savePreferences();
        
        if (this.mTimer != null){        
        	this.mTimer.cancel();
            this.mTimer = null;
        }        
        
        SoundGenerator.getInstance().stop();
        this.mCameraHelper.turnFlashlightOff();        
    }

    @Override
    public synchronized void onResume() {
        super.onResume();        
        this.mTimer = new Timer();       
        this.mTimerTask = new TimerTask() {
            @Override
            public void run() {
            	runOnUiThread(new Runnable() {
            		public void run() {              	
            			onTimeProcess();                    
                    }
            	});
            }
        };               
        this.mTimer.scheduleAtFixedRate(this.mTimerTask, 1000, 40);   
 
        if (this.mBluetoothService != null) {
            if (this.mBluetoothService.getState() == BluetoothConnectionState.STATE_NONE) {                
            	this.mBluetoothService.start();
            }
        }
        
        // change settings if pref were changed
        if (this.mPreferences.isPreferencesWereChanged()){      
        	this.mGraphView.setFillAreas(this.mPreferences.isFillAreas());        	        
        	this.mGraphView.setWindowSize(this.mPreferences.getWindowSize());        	        	       
        }
        if (this.mPreferences.isFlashlight()){
    		this.mCameraHelper.turnFlashlightOn();
    	}   
        
    	GpsLocationService.getInstance(this).stopListenUpdates();
    }  
    
    private void showActivityById(int id){
    	switch (id){
    	case R.id.menu_item_scan:
    		Intent serverIntent = new Intent(this, DeviceListActivity.class);
    		startActivityForResult(serverIntent, IntentRequestCodes.REQUEST_CONNECT_DEVICE);
    		break;
    		
    	case  R.id.menu_item_monitor:
    		Intent monitorIntent = new Intent(this, MonitorActivity.class);
    		startActivityForResult(monitorIntent, IntentRequestCodes.RESULT_MONITOR);
    		break;
    		
    	case  R.id.menu_item_gps:
    		Intent gpsActivity = new Intent(this, GpsPointsListActivity.class);    		
    		startActivity(gpsActivity);
    		break;
    		
    	case  R.id.menu_item_settings:
    	    Intent prefActivity = new Intent(this, PreferencesActivity.class);
            startActivity(prefActivity);  
    		break;
    	}    	    	       
    }    

    @Override
    public void onDestroy() {
        super.onDestroy();           
        if (this.mBluetoothService != null) {
        	this.mBluetoothService.stop();
        	this.mBluetoothService = null;
        }               
    }    
 
    private void setupBluetoothService(BluetoothDevice device) {        
    	this.mBluetoothService = new BluetoothService(this, this.mHandler, device);    	           
    }
    
    private void onBltMessageStateDisconnected(){    	 
    	try {
			 Thread.sleep(1000L);
	         ConnectToDevice(this.mPreferences.getDeviceConnectAddress());            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }    	
    }    
    
    private void generateSound(double currentValue){    	
    	if (!this.mPreferences.isEnableSounds())
    		return;      	    
    	    	
    	try {
    		if (currentValue > 0.001) {
    			SoundGenerator.getInstance().playTone(1500);    			
    			    		                       	
            } else if (currentValue < -0.001) {
            	SoundGenerator.getInstance().playTone(500);
            	
            } else {              
            	SoundGenerator.getInstance().stop();
            }
    	}catch (Exception ex){    		
    	}            
    }
    
    private int checkOverflowLimit(int rawData, int prevRawData){
    	if (this.mLimitOverflow == 0){
    		if (Math.abs(rawData - prevRawData) > 32768){
    			this.mLimitOverflow = prevRawData;
    			return prevRawData;
      	  	}    		
    		return rawData;    	
        }     	
    	if (Math.abs(rawData - this.mLimitOverflow) < 16384){
    		this.mLimitOverflow = 0;
    		return rawData;
  	  	} 
    	return this.mLimitOverflow;    	
    }
    
    private double doDiscrimination(double data){
    	if (this.mPreferences.getGraphWorkMode() == GraphWorkMode.Dynamic)
    		return data;
    	
    	DiscriminatorTypes currentType = this.getCurrentDiscriminatorType();
    	if (currentType == DiscriminatorTypes.All)
    		return data;
    	
    	boolean ignoreNegative = currentType == DiscriminatorTypes.Colored;    	
    	if (ignoreNegative){
    		if (data < 0)
    			data = 0;
    	} else {
    		if (data > 0)
    			data = 0;
    	}
    	return data;              
    }
    
    private void setSensorBatteryLevel(int level){    	
    	this.mSensorChargeTextView.setText(String.valueOf(level)+"%");    	
    }
    
    private void setBatteryLevel(int level){    	
    	this.mBatteryChargeTextView.setText(String.valueOf(level)+"%");    	
    }
    
    private void initBatteryLevelService(){
    	this.mBatteryChargeTextView = (TextView) findViewById(R.id.photoChargeText);
    	this.mSensorChargeTextView = (TextView) findViewById(R.id.sensorChargeText);
    	mCurrentValueTextView = (TextView) findViewById(R.id.currentValueLabel);
    	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {    	        	    
    	    @Override
    	    public void onReceive(Context context, Intent intent) {
    	    	int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
    	    	setBatteryLevel(level);    	    	
    	    }    	        	   
    	};

    	IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	Intent batteryStatus = registerReceiver(batteryReceiver, filter);  
    	setBatteryLevel(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
    }
    
    private void onBltMessageChargeRead(int data){
    	final float minVoltage = 7f;
    	final float maxVoltage = 9f;
    	
    	if (this.mTimer == null)
    		return;
    	
    	int level = 0;
    	float voltage = 15f * data/1024f;    	
    	if (voltage < minVoltage){
    		level = 0;	
    	} else {
    		level = (int)(100 * (voltage - minVoltage)/(maxVoltage-minVoltage));
    		if (level > 100)
        		level = 100;        	
    	}
    
    	setSensorBatteryLevel(level);    	
    }
    
    private void onBltMessageDataRead(int data){
    	if (this.mTimer == null)
    		return;    	
    	
    	int prevRawLevelValue = this.mLastRawLevelValue;
    	
    	// remember the current raw level
    	this.mLastRawLevelValue = data;
    	
    	// set zero level, if need
    	if (this.mZeroValue == 0){
    		this.setZeroValue();     	  
        }    	    	    	
    	
        // check for data limit overflow
    	data = this.checkOverflowLimit(data, prevRawLevelValue);    	    	    	 
          
        // remember current date
        java.util.Date date= new java.util.Date();
        double currentTime = (date.getTime()-mStartTime)/1000.0;       
        
        double sensitivity = 100 - this.mSensitivityPicker.getValue();
        sensitivity = (sensitivity* sensitivity*sensitivity)/10000;        
                           
        // calc the current value according to current sensitivity
        double currentValue = (data - this.mZeroValue)/ (1+sensitivity);               
        
    	// apply discriminator
        currentValue = this.doDiscrimination(currentValue);        
        
        // check if we limit the mininal threshold
        if (this.mPreferences.getGraphWorkMode() == GraphWorkMode.Static){
        	int threshold = this.mThresholdPicker.getValue();
        	if (threshold > 0){
        		int sign = Math.signum(currentValue) > 0 ? 1 : -1;
             	if (Math.abs(currentValue) > threshold){
             		currentValue -= sign * threshold;
                 	this.mThresholdZeroCounter = sign * 25;  
             	} else {
             		if (this.mThresholdZeroCounter != 0){
             			int thresholdCounterSign = Math.signum(this.mThresholdZeroCounter) > 0 ? 1 : -1;        		        	
                 		if (thresholdCounterSign != sign){
                 			this.mThresholdZeroCounter = 0;            		
                 		} else {
                 			this.mThresholdZeroCounter -= thresholdCounterSign;	
                 		}       	
             		}             		 		
             		if (this.mThresholdZeroCounter == 0){        			
                 		currentValue = 0;
             		}  else {
             			currentValue -= sign * threshold;
             			if (currentValue < 0){
             				currentValue = 0;
             			}
             		}
                }
        	}
        	generateSound(currentValue);    	                                                 
        }
        this.mGraphView.addValue(new Point(currentTime, currentValue));                                        
    }          
    
    private void onBltMessageDataWrite(int data){     	
    }
        
    @SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BluetoothMessages.MESSAGE_STATE_CHANGE:                    
                    switch (message.arg1) {
                        case BluetoothConnectionState.STATE_CONNECTED:
                            break;

                        case BluetoothConnectionState.STATE_CONNECTING:
                            break;

                        case BluetoothConnectionState.STATE_DISCONNECTED:
                        	onBltMessageStateDisconnected();                           
                            break;

                        case BluetoothConnectionState.STATE_LISTEN:
                        case BluetoothConnectionState.STATE_NONE:
                            break;
                    }

                case BluetoothMessages.MESSAGE_WRITE:
                	onBltMessageDataWrite(message.arg1);                    
                    break;

                case BluetoothMessages.MESSAGE_READ_DATA:
                	onBltMessageDataRead(message.arg1);                        
                    break;
                    
                case BluetoothMessages.MESSAGE_READ_CHARGE:
                	onBltMessageChargeRead(message.arg1);                        
                    break;

                case BluetoothMessages.MESSAGE_DEVICE_NAME:                    
                    mConnectedDeviceInfo = message.getData().getString(kDeviceName);
                    mToastObject.setText(getString(R.string.state_connected_to) +" "+ mConnectedDeviceInfo);
                    mToastObject.show();                    
                    break;

                case BluetoothMessages.MESSAGE_TOAST:                    
                    mToastObject.setText(message.getData().getString(kToast));
                    mToastObject.show();                    
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {        
        switch (requestCode) {
            case IntentRequestCodes.REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String devAddress = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    mPreferences.setDeviceConnectAddress(devAddress);
                    ConnectToDevice(devAddress);
                }
                break;
                
            case IntentRequestCodes.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                } else {
                    
                    mToastObject.setText(R.string.bt_not_enabled_leaving);
                    mToastObject.show();                    
                    finish();
                }
                break;
        }
    }

    private void onPreparePopupMenu() {    	
    	Menu menu = this.mPopupMenu.getMenu();
        MenuItem monitorItem = menu.findItem(R.id.menu_item_monitor);
        MenuItem disconnectItem = menu.findItem(R.id.menu_item_disconnect);
        MenuItem scanItem = menu.findItem(R.id.menu_item_scan);

        if (mBluetoothService != null && mBluetoothService.getState() == BluetoothConnectionState.STATE_CONNECTED) {
            disconnectItem.setVisible(true);
            scanItem.setVisible(false);
            if(mPreferences.isDebug()) {
                monitorItem.setVisible(true);
            }
        } else {
            disconnectItem.setVisible(false);
            scanItem.setVisible(true);
            monitorItem.setVisible(false);
        }        
    }

    private void ConnectToDevice(String address) {   
        if(mBluetoothService != null){
            mBluetoothService.stop();
            mBluetoothService = null;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (mBluetoothService == null) {
            setupBluetoothService(device);
        }

        mBluetoothService.connect(device);
    }       
    
    public MainActivity() {
    }
}
