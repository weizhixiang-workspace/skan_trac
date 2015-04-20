package com.tatarinov.BluetoothDataAnalyzer.Activities;

import com.tatarinov.BluetoothDataAnalyzer.R;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsLocationService;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsLocationService.ILocationChangeListener;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsStorage;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsStorage.GPSPoint;

import android.app.Activity;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GpsPointNaviActivity extends Activity  {
	private GPSPoint mPoint;	
	private GpsLocationService mService;
	private ChangeLocationListener mListener = new ChangeLocationListener();	
	private CustomAnimationListener mAnimationListener = new CustomAnimationListener();
	private ImageView mArrow;
	private TextView mStatusLabel;
	private int mArrowHeight;
	private int mArrowWidth;
	private GeomagneticField geoField;	
	
	private int mCurrentAnimValue;
	private int mNextAnimValue = -1;
	
	private class ChangeLocationListener implements ILocationChangeListener {
		public void onLocationChange(Location location) {
			geoField = new GeomagneticField(
					Double.valueOf(location.getLatitude()).floatValue(),
				    Double.valueOf(location.getLongitude()).floatValue(),
				    Double.valueOf(location.getAltitude()).floatValue(),
				    System.currentTimeMillis()
			);			
			float bearing = location.bearingTo(mPoint.location);
			float heading = geoField.getDeclination();			
			int rot = (int) (mCurrentAnimValue - heading + bearing);			
			if (rot < 0){
				rot = 360 + rot;
			}
			rot %= 360;
			rotate(rot);			
		}
				
		public void onGPSStatusChanged(String status){
			mStatusLabel.setText(status);			
		}
	}	
	
	private class CustomAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {}

		@Override
		public void onAnimationEnd(Animation animation) {			
			if (mNextAnimValue >= 0){
				rotate(mNextAnimValue);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {}	
	}

	private void rotate(int dir) {		
		Animation anim = mArrow.getAnimation();
		if (anim != null && !anim.hasEnded()){
			mNextAnimValue = dir;
			return;
		}
		
		anim = new RotateAnimation(mCurrentAnimValue, dir, 
				mArrowWidth/2, mArrowHeight/2);

		anim.setDuration((int)(10 * Math.abs(dir-mCurrentAnimValue)));       
		anim.setRepeatCount(0);       
		anim.setRepeatMode(Animation.REVERSE); 	   
		anim.setFillAfter(true);	
		anim.setAnimationListener(mAnimationListener);
	    mArrow.startAnimation(anim);
	    mCurrentAnimValue = dir;
	    mNextAnimValue = -1;	    
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gps_point_navi);		
		
		this.mService = GpsLocationService.getInstance(null);	
		this.mArrow = (ImageView) findViewById(R.id.compassArrowImage);
		this.mStatusLabel = (TextView) findViewById(R.id.statusLabel);
		
		ViewTreeObserver vto = this.mArrow.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
		    public boolean onPreDraw() {
		    	mArrow.getViewTreeObserver().removeOnPreDrawListener(this);
		    	mArrowHeight = mArrow.getMeasuredHeight();
		    	mArrowWidth = mArrow.getMeasuredWidth();		    	
		        return true;
		    }
		});
	}
	
	public void onResume() {
		super.onResume();				        	     
		this.mService.addChangeListener(this.mListener);		
		
		Bundle b = getIntent().getExtras();
		int index = b.getInt("point_index");		
		this.mPoint = GpsPointsStorage.getInstance().getPoints().get(index);
	}
	
	public void onStop() {		
		super.onStop();		
		this.mService.removeChangeListener(this.mListener);
	}
}
