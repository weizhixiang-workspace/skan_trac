package com.tatarinov.BluetoothDataAnalyzer.Activities;

import net.jayschwa.android.preference.SliderPreference;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;
import com.tatarinov.BluetoothDataAnalyzer.R;

public class PreferencesActivity extends PreferenceActivity {
	private OnSharedPreferenceChangeListener mSharedPreferencesListener;
	
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(GlobalPreferences.kPreferencesFileName);
        addPreferencesFromResource(R.xml.preferences); 
                
        this.setSummary(GlobalPreferences.PreferencesNames.kChartWindowWidth);
        this.setSliderValue(GlobalPreferences.PreferencesNames.kToolsScreenBrightness);
        this.setSummary(GlobalPreferences.PreferencesNames.kChartFilterLength);
        this.setSummary(GlobalPreferences.PreferencesNames.kDynamicThresholdDelta);        
                
    	this.mSharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {        
	        @Override
	        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {	        		        	        		        
	        	if (key.equals(GlobalPreferences.PreferencesNames.kChartWindowWidth)){
	        		validateInput(prefs, key, GlobalPreferences.kChartWindowWidthDefault, 1000, Integer.MAX_VALUE);	        		     		       
	        	}
	        	if (key.equals(GlobalPreferences.PreferencesNames.kToolsScreenBrightness)){	        		
	        		setBrightness(setSliderValue(key));	        			        			        		      
	        	}	        	
	        	if (key.equals(GlobalPreferences.PreferencesNames.kChartFilterLength)){
	        		validateInput(prefs, key, GlobalPreferences.kChartFilterLengthDefault, 3, 20);	        		
	        	}
	        	if (key.equals(GlobalPreferences.PreferencesNames.kDynamicThresholdDelta)){
	        		validateInput(prefs, key, GlobalPreferences.kDynamicThresholdDeltaDefault, 1, 100);	        		
	        	}
	        }
	    };
	    preferenceManager.getSharedPreferences()
	    	.registerOnSharedPreferenceChangeListener(this.mSharedPreferencesListener);	    	   
    }
    
    private void validateInput(SharedPreferences prefs, String field, int defValue, int min, int max){
    	int val = defValue;
        try {
        	val = Integer.parseInt(prefs.getString(field, String.valueOf(defValue)));
        } catch(Exception e){        
        }            
        boolean validateFailed = false;
        if (val < min){
        	val = min;
        	validateFailed = true;
        }
        if (val > max){
        	val = max;
        	validateFailed = true;
        }        
        if (validateFailed){
        	prefs.edit().putString(field, String.valueOf(val)).apply();        	
        } 
        setSummary(field);
    }
        
    @SuppressWarnings("deprecation")
	public float setSliderValue(String key){
    	float val = getPreferenceManager().getSharedPreferences().getFloat(key, 1);    					
		SliderPreference pref = (SliderPreference)findPreference(key);					
		pref.setSummary(String.valueOf((int) (val * 100)));
		return val;
    }
    
    private void setBrightness(float val){
    	setBrightness(this, val);
    }
    
    public static void setBrightness(Activity activity, float val){
    	WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = val;
		activity.getWindow().setAttributes(lp);	
    }
    
    @SuppressWarnings("deprecation")
	private void setSummary(String key){
    	Preference pref = findPreference(key);	
    	if (pref instanceof EditTextPreference){
    		pref.setSummary(((EditTextPreference)pref).getText());
    	}
    }
}
