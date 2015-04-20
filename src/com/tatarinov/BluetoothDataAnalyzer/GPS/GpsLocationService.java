package com.tatarinov.BluetoothDataAnalyzer.GPS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GpsLocationService {
	private static GpsLocationService instance;
	private LocationManager mLocationManager;
	private CustomLocationListener mLocationListener;
	private CustomGpsListener mGpsListener = new CustomGpsListener();
	private List<IGetLocationCallback> mGetCallbacks = new ArrayList<IGetLocationCallback>();
	private List<ILocationChangeListener> mChangeCallbacks = new ArrayList<ILocationChangeListener>();
	
	public interface  IGetLocationCallback {
		public void onGetLocation(Location location);		
	}	
	
	public interface  ILocationChangeListener {
		public void onLocationChange(Location location);			
		
		public void onGPSStatusChanged(String status);
	}	
	
	private class CustomGpsListener implements Listener {
		private GpsStatus mGpsStatus;
		public String mStatus = "";	
				
	    private int iterableSize(Iterable<?> it) {
	    	  if (it instanceof Collection)
	    	    return ((Collection<?>)it).size();
	   
	    	  int i = 0;
	    	  for (Object obj : it) i++;
	    	  return i;
	    }

		@Override
		public void onGpsStatusChanged(int event) {    
	    	switch (event){	    		    			    		
	    	case GpsStatus.GPS_EVENT_FIRST_FIX:
	    	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	    		mGpsStatus = mLocationManager.getGpsStatus(mGpsStatus);	    		
	    		mStatus = String.format("GPS: %d(%d)", iterableSize(mGpsStatus.getSatellites()), mGpsStatus.getMaxSatellites());	    		
	    		break;
	    		
	    	case GpsStatus.GPS_EVENT_STARTED:
	    		mStatus = "GPS started..";
	    		break;
	    		
	    	case GpsStatus.GPS_EVENT_STOPPED:
	    		mStatus = "GPS stopped..";
	    		break;
	    		
	    	default:
	    		mStatus = "";
	    	}
	    		   
	    	for (ILocationChangeListener cb : mChangeCallbacks){
				cb.onGPSStatusChanged(mStatus);			
			}	    	
		}		
	}
	
	private class CustomLocationListener implements LocationListener {
	
	    @Override
	    public void onLocationChanged(Location loc) {		    	
	    	changeLastLocation(loc);    		    
	    }

	    @Override
	    public void onProviderDisabled(String provider) {}	    		   

	    @Override
	    public void onProviderEnabled(String provider) {}
	   
	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	}	
	
	public static GpsLocationService getInstance(Activity activity){
		if (instance == null){
			instance = new GpsLocationService(activity);
		}
		return instance;		
	}	

	public void startListenUpdates() {
		if (this.mLocationListener != null)
			return;
		
		this.mLocationListener = new CustomLocationListener();
		this.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
				1000, 2, this.mLocationListener);	
		this.mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
				5000, 20, this.mLocationListener);
		this.mLocationManager.addGpsStatusListener(mGpsListener);					
	}
	
	public void stopListenUpdates() {		
		if (this.mLocationListener == null)
			return;
		
		this.mLocationManager.removeUpdates(this.mLocationListener);	
		this.mLocationManager.removeGpsStatusListener(mGpsListener);
		this.mGetCallbacks.clear();
		this.mLocationListener = null;
	}
	
	public void addChangeListener(ILocationChangeListener listener){
		this.mChangeCallbacks.add(listener);
		listener.onGPSStatusChanged(mGpsListener.mStatus);
	}
	
	public void removeChangeListener(ILocationChangeListener listener) {
		this.mChangeCallbacks.remove(listener);
	}
	
	private void changeLastLocation(Location location){
		for (ILocationChangeListener cb : this.mChangeCallbacks){
			cb.onLocationChange(location);			
		}		
		
		if (this.mGetCallbacks.size() > 0){
			for (IGetLocationCallback cb : this.mGetCallbacks){
				cb.onGetLocation(location);
			}
			this.mGetCallbacks.clear();
		}
	}
	
	public boolean isGPSEnabled(){
		return this.mLocationManager != null;
	}
	
	public void getLastLocation(IGetLocationCallback callback){		
		Location lastKnownLocation = this.mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null){			
			callback.onGetLocation(lastKnownLocation);
			return;
		}		
		this.mGetCallbacks.add(callback);
	}	

	private GpsLocationService(Activity activity){
		this.mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);					
	}
}
