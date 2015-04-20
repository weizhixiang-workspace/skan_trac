package com.tatarinov.BluetoothDataAnalyzer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class CameraHelper {
	private Camera mCamera;     
	private Parameters mCameraParameters;
	 	
	private boolean mIsFlashlightAvailable;
	 
	public void turnFlashlightOn(){
		if (!this.mIsFlashlightAvailable)
			return;
		
		try {    	
			this.mCamera = Camera.open();
	        this.mCameraParameters = this.mCamera.getParameters();
	        	
	        this.mCameraParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
	        this.mCamera.setParameters(this.mCameraParameters);
	        this.mCamera.startPreview();	        
	    } catch (Exception ex){    		
	   	}    
	}
	 
	public void turnFlashlightOff(){
		if (this.mCamera != null) {    		     		
			this.mCameraParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
	    	this.mCamera.setParameters(this.mCameraParameters);
	    	this.mCamera.stopPreview();   	    	    		
	        this.mCamera.release();
	        this.mCamera = null;
		}
	}
	    
	 public CameraHelper(Context context){
		 PackageManager pm = context.getPackageManager();                                
	     this.mIsFlashlightAvailable =  pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	 }
}
