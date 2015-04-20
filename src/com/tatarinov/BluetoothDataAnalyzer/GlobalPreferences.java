package com.tatarinov.BluetoothDataAnalyzer;

import java.util.zip.DeflaterOutputStream;

import com.tatarinov.BluetoothDataAnalyzer.Graph.DiscriminatorTypes;
import com.tatarinov.BluetoothDataAnalyzer.Graph.GraphWorkMode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class GlobalPreferences {
	private static GlobalPreferences instance = null;
	
	private static final  boolean kIsDebug = false;
	
	public static final String kPreferencesFileName ="BluetoothDataAnalyzerPrefrences";
	//  Properties
	public class PreferencesNames {
		public static final String kChartWindowWidth = "chart_window_size_ms";
		public static final String kChartFilterLength = "filter_length";
		public static final String kToolsScreenBrightness = "tools_screen_brightness";	
		public static final String kDynamicThresholdDelta = "dynamic_threshold_time_delta";
	}	
	// -------------------------------------------------------------
	
	// Defaults
	public static final Integer kChartWindowWidthDefault = 2000;
	public static final Integer kChartFilterLengthDefault = 4;
	public static final Integer kSensitivityDefault = 50;
	public static final Integer kThresholdDefault = 10;
	public static final Integer kDynamicThresholdDeltaDefault = 1;		
	// -------------------------------------------------------------          

    private SharedPreferences mSharedPreferences;
    
    private String mDeviceConnectAddress;         
        
    private DiscriminatorTypes mDiscriminatorType;    
    private double mWindowSize;
    private int mThreshold;
    private int mDynamicThresholdDelta;
    private int mSensitivity;    
    private int mFilterLength;
    private GraphWorkMode mGraphWorkMode;
    
    private boolean mIsPreferencesWereChanged = false;
    
    private OnSharedPreferenceChangeListener mSharedPreferencesListener;
    
    public boolean isPreferencesWereChanged(){
    	if (this.mIsPreferencesWereChanged){
    		this.mIsPreferencesWereChanged = false;
    		this.loadPreferences();
    		return true;
    	}
    	return false;
    }
      
    public GlobalPreferences(Context context){
        this.mSharedPreferences  = context.getSharedPreferences(kPreferencesFileName, Context.MODE_PRIVATE);
                              
        this.loadPreferences();
        
    	this.mSharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {        
	        @Override
	        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {	        	
	        	sharedPreferenceChanged(prefs, key);
	        }
	    };
	    this.mSharedPreferences
	    	.registerOnSharedPreferenceChangeListener(this.mSharedPreferencesListener);
    }
    
    private void sharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {       	
    	this.mIsPreferencesWereChanged = true;    	    	                        
    }
        
    public static GlobalPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalPreferences(context);
        }
        return instance;
    }
    
    public void setGpsPoints(String value){
    	SharedPreferences.Editor prefEditor = mSharedPreferences.edit();
    	prefEditor.putString("gps_points", value);	
    	prefEditor.commit();
    }
    
    public String getGpsPoints(){
    	return mSharedPreferences.getString("gps_points", "");    	
    }
    
    public void loadPreferences() {
        if(this.mSharedPreferences == null)
            return;               
                                      
        this.mDiscriminatorType = DiscriminatorTypes.values()[Integer.parseInt(mSharedPreferences.getString("discriminator_type", "0"))];
        this.mGraphWorkMode = GraphWorkMode.values()[Integer.parseInt(mSharedPreferences.getString("static_mode2", "0"))];                        
        this.mThreshold = Integer.parseInt(mSharedPreferences.getString("threshold", String.valueOf(kThresholdDefault)));
        this.mSensitivity = Integer.parseInt(mSharedPreferences.getString("sensitivity", String.valueOf(kSensitivityDefault)));
        this.mDeviceConnectAddress = mSharedPreferences.getString("device_connect_address", "");
        
        this.mWindowSize = (float)getInteger(PreferencesNames.kChartWindowWidth, kChartWindowWidthDefault)/1000f;
        this.mFilterLength = getInteger(PreferencesNames.kChartFilterLength, kChartFilterLengthDefault);
        this.mDynamicThresholdDelta = getInteger(PreferencesNames.kDynamicThresholdDelta, kDynamicThresholdDeltaDefault);                                               
    }
    
    private int getInteger(String key, int defaultValue){
    	try {
    		defaultValue = Integer.parseInt(this.mSharedPreferences.getString(key, String.valueOf(defaultValue)));
    	} catch(Exception e){        
        }  
    	return defaultValue;
    }

    public void savePreferences(){
        SharedPreferences.Editor prefEditor = mSharedPreferences.edit();
        prefEditor.putString("discriminator_type", String.valueOf(mDiscriminatorType.ordinal()));
        prefEditor.putString("static_mode2", String.valueOf(mGraphWorkMode.ordinal()));                      
        prefEditor.putString("threshold", String.valueOf(mThreshold));
        prefEditor.putString("sensitivity", String.valueOf(mSensitivity));        
        prefEditor.putString("device_connect_address", mDeviceConnectAddress);
        prefEditor.apply();
    }
    
    public boolean isFlashlight() {
    	return this.mSharedPreferences.getBoolean("tools_flashlight", false);        
    }   
    
    public float getScreenBrightness() {
    	return this.mSharedPreferences.getFloat(PreferencesNames.kToolsScreenBrightness, 1);    	       
    } 

    public boolean isEnableSounds() {
    	return this.mSharedPreferences.getBoolean("sounds_is_enable", true);        
    }
    
    public boolean isEnableVibrating() {
    	return this.mSharedPreferences.getBoolean("sounds_is_vibration_on", false);        
    }  
    
    public boolean isFillAreas() {
    	return this.mSharedPreferences.getBoolean("chart_is_fill_areas", false);    	
    }
    
    public void setGraphWorkMode(GraphWorkMode mode){
    	this.mGraphWorkMode = mode;
    }
    
    public GraphWorkMode getGraphWorkMode(){
    	return this.mGraphWorkMode;
    }   
    
    public double getWindowSize(){
    	return this.mWindowSize;
    }
    
    public int getFilterLength(){
    	return this.mFilterLength;
    }
    
    public void setFilterLength(int filterLength){
    	this.mFilterLength = filterLength;    	
    }
    
    public int getDynamicThresholdDelta() {
        return this.mDynamicThresholdDelta+1;
    }
    
    public int getThreshold() {
        return this.mThreshold;
    }
    
    public void setThreshold(int newValueThreshold) {
    	this.mThreshold = newValueThreshold;
    }   
    
    public DiscriminatorTypes getDiscriminatorType(){
    	return this.mDiscriminatorType;
    }
    
    public void setDiscriminatorType(DiscriminatorTypes discriminatorType){
    	this.mDiscriminatorType = discriminatorType;
    }
    
    public int getSensitivity() {
        return this.mSensitivity;
    }

    public void setSensitivity(int newValueSensitivity) {
    	this.mSensitivity = newValueSensitivity;
    }
    
    public boolean isEnableFiltration() {
    	return this.mSharedPreferences.getBoolean("chart_is_filter_on", true);        
    }

    public boolean isEnableAutoConnect() {
        return this.mSharedPreferences.getBoolean("connection_is_auto", true);               
    } 

    public boolean isAutoReconnect() {
    	return this.mSharedPreferences.getBoolean("connection_is_autoreconnect", true);        
    }

    public void setDeviceConnectAddress(String deviceConnectAddress) {
    	this.mDeviceConnectAddress = deviceConnectAddress;
    }
    
    public String getDeviceConnectAddress() {
        return this.mDeviceConnectAddress;
    }

    public boolean isDebug() {
        return kIsDebug;
    }
}
