package com.tatarinov.BluetoothDataAnalyzer.UI;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class ToggleImageButtonWrapper {
	public interface StateChangeListener {
	    public void onStateChange(int newState);
	}
	
	private ImageButton mButton;
	
	private int mState;
	
	private int[] mStatesRes;
	private int mPressRes;
	
	private List<StateChangeListener> mStateChangeListeners = new ArrayList<StateChangeListener>();
	
	public int getState(){
		return this.mState;		
	}
	
	public void setState(int index){		
		this.mState = index;
		this.mButton.setImageResource(mStatesRes[index]);
		this.onStateChangeEventCall();
	}
	
	private void onStateChangeEventCall(){
		for (StateChangeListener listener : mStateChangeListeners){
			listener.onStateChange(mState);
		}
	}
	
	public void setStateChangeListener(StateChangeListener listener){
		mStateChangeListeners.add(listener);			
	}
	
	public ToggleImageButtonWrapper(ImageButton button, int[] statesRes, int pressRes){
		this.mButton = button;
		this.mStatesRes = statesRes;
		this.mPressRes = pressRes;
		
        this.mButton.setOnTouchListener(new View.OnTouchListener() {
        	@Override
        	public boolean onTouch(View v, MotionEvent event) {
        		switch (event.getAction()) {
    		    case MotionEvent.ACTION_DOWN:       		    	
    		    	int max = mStatesRes.length;    		    	    		    
    				if (++mState == max){
    					mState = 0;
    				}
    				if (mPressRes > 0){
    					mButton.setImageResource(mPressRes);
    				} else {
    					mButton.setImageResource(mStatesRes[mState]);
    				}
    		        break;
    		    case MotionEvent.ACTION_UP:    		    	
    		    	if (mPressRes > 0){
    		    		mButton.setImageResource(mStatesRes[mState]);
    		    	}    
    		    	onStateChangeEventCall();
    		        v.performClick();
    		        break;
    		    default:
    		        break;
    		    }
    		    return true;     
            }
        });	
	}
}
