package com.tatarinov.BluetoothDataAnalyzer.UI;

import java.util.ArrayList;
import java.util.List;

import com.tatarinov.BluetoothDataAnalyzer.R;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NumberPickerWrapper {
	public interface ValueChangedListener {
	    public void onValueChange(int value, boolean increase);
	}
	
	private TextView mText;
	
	private List<ValueChangedListener> mValueChangedListeners = new ArrayList<ValueChangedListener>();
	
	private ImageButton mPlusButton;
	private ImageButton mMinusButton;
	
	private int mCurrentValue;
	private int mMaxValue;
	private int mMinValue;	
	
	private int mPlusId;
	private int mMinusId;
	private int mPressCounter;
	private int mPressButtonId = -1;
	
	public NumberPickerWrapper(int current, int max, int min){
		this.mCurrentValue = current;
		this.mMaxValue = max;
		this.mMinValue = min;
	}
	
	public void timeProcess(){
		if (this.mPressButtonId != -1 && ++this.mPressCounter > 20){
			this.mPressCounter = 10;
			this.OnDoLastAction();
		}
	}
	
	private void OnDoLastAction(){
		int prevValue = mCurrentValue;
		if (this.mPressButtonId == this.mPlusId){
			mCurrentValue++;
		} else if (this.mPressButtonId == this.mMinusId){			
			mCurrentValue--;			
		}		
		checkForLimitOverflow();
		if (prevValue != mCurrentValue){
			callChangeValueEvent(prevValue < mCurrentValue);
		}
	}
	
	public int getValue(){
		return this.mCurrentValue;
	}
	
	public void setValue(int val){
		this.mCurrentValue = val;
		this.checkForLimitOverflow();
	}
	
	public void init(Activity mainActivity, int plusButtonId, int minusButtonId, int textViewId){
		this.mPlusButton = (ImageButton)mainActivity.findViewById(plusButtonId);
		this.mMinusButton = (ImageButton)mainActivity.findViewById(minusButtonId);
		this.mText = (TextView)mainActivity.findViewById(textViewId);		
		
		this.mPlusId = plusButtonId;
		this.mMinusId = minusButtonId;
		
		LinearLayout root = (LinearLayout)this.mPlusButton.getParent();		
		root.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean isPlusClick = event.getY() < v.getHeight()/2;							
				
				switch (event.getAction()) {
			    case MotionEvent.ACTION_DOWN:			    	
			    	if (isPlusClick){
			    		mPlusButton.setImageResource(R.drawable.button_plus_press);
			    		mPressButtonId = mPlusId;
			    	} else {
			    		mMinusButton.setImageResource(R.drawable.button_minus_press);
			    		mPressButtonId = mMinusId;
			    	}			    		
			    	mPressCounter = 0;
				    OnDoLastAction();				    				    
			        break;
			        
			    case MotionEvent.ACTION_UP:
			    	if (isPlusClick){
			    		mPlusButton.setImageResource(R.drawable.button_plus_normal);			    		
			    	} else {
			    		mMinusButton.setImageResource(R.drawable.button_minus_normal);			    		
			    	}			
			    	mPressButtonId = -1; 
			        v.performClick();
			        break;
			    default:
			        break;
			    }
			    return true;			
			}		
		});
			
		this.checkForLimitOverflow();		
	}
	
	public void setChangeValueListener(ValueChangedListener listener){
		this.mValueChangedListeners.add(listener);
	}
	
	private void callChangeValueEvent(boolean increase){
		for (ValueChangedListener listener : mValueChangedListeners) {    
			listener.onValueChange(this.getValue(), increase);
		}
	}
	
	private void checkForLimitOverflow(){
		if (this.mCurrentValue <= this.mMinValue) {
			this.mCurrentValue = this.mMinValue;
			this.mMinusButton.setEnabled(false);
			this.mMinusButton.setAlpha(0.5f);
			
		} else if (!this.mMinusButton.isEnabled()){
			this.mMinusButton.setEnabled(true);
			this.mMinusButton.setAlpha(1f);
		}
		
		if (this.mCurrentValue >= this.mMaxValue){
			this.mCurrentValue = this.mMaxValue;
			this.mPlusButton.setEnabled(false);
			this.mPlusButton.setAlpha(0.5f);
			
		}  else if (!this.mPlusButton.isEnabled()){
			this.mPlusButton.setEnabled(true);
			this.mPlusButton.setAlpha(1f);
		}
		this.mText.setText(String.valueOf(this.mCurrentValue));
	}
}
