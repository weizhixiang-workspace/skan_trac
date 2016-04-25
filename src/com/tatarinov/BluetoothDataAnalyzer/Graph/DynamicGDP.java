package com.tatarinov.BluetoothDataAnalyzer.Graph;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.graphics.Color;
import android.util.Log;

import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;
import com.tatarinov.BluetoothDataAnalyzer.SoundGenerator;
import com.tatarinov.BluetoothDataAnalyzer.UI.GraphView;

public class DynamicGDP extends GraphDataProcessor {

	private XYSeries mBlackData;
	private XYSeries mColoredData;	
		
	private double mPeakStartTime;
	private Point mPeakValue = new Point(0, 0);
	private double mBuildPeakvalue;
	private double mLocalMax;		
	private double mPrevValue;
	private int mDirection;
	private int mClearCounter;	
	private int mSoundGenerateCounter;
	
	private double mZeroValue;
	private List<Double> mZeroBuffer = new ArrayList<Double>();
	
	private List<Point> mInBuffer = new ArrayList<Point>();	
		
	public DynamicGDP(GraphView view) {
		super(view);	
		this.mBlackData = new XYSeries("BlackLevel"); 
		this.mColoredData = new XYSeries("ColoredLevel");			
	}
	
	private void zeroDetect(Point point, double thr){
		final int kZeroBufferLength = 20;
		
		if (this.mDirection != 0) {			
			this.mZeroBuffer.clear();
			return;
		}			
		
		if (this.mZeroBuffer.size() < kZeroBufferLength) {
			this.mZeroBuffer.add(point.getY());
			return;
		}
		
		this.mZeroBuffer.remove(0);
		this.mZeroBuffer.add(point.getY());
		
		double zero = 0;
		double min = Integer.MAX_VALUE;
		double max = Integer.MIN_VALUE;
				
		for (double v : this.mZeroBuffer){			
			zero += v;
			if (min > v)
				min = v;							
			if (max < v)
				max = v;					
		}	
		
		if (Math.abs(max-min) > thr)
			return;			
				
		this.mZeroValue = zero/kZeroBufferLength;		
	}
	
	private void doDiscrimination(Point point, GlobalPreferences prefs){
		DiscriminatorTypes currentType = prefs.getDiscriminatorType();
		if (currentType == DiscriminatorTypes.All)
			return;
		
		double data = point.getY(); 
		boolean ignoreNegative = currentType == DiscriminatorTypes.Colored;    	
    	if (ignoreNegative){
    		if (data < 0)
    			data = 0;
    	} else {
    		if (data > 0)
    			data = 0;
    	}
    	point.setY(data);		
	}
	
	@Override
	public void addPoint(Point point) {
		GlobalPreferences prefs = GlobalPreferences.getInstance(null);
		double thr = prefs.getThreshold();
		
		zeroDetect(point, thr);
		point.setY(point.getY()-this.mZeroValue);
		doDiscrimination(point, prefs);		
		
		if (this.mDirection != 0){
			this.mPoints.add(point);			
		} else {								
			this.mInBuffer.add(point);
			
			if (this.mInBuffer.size() >= prefs.getDynamicThresholdDelta()){
				double min = Integer.MAX_VALUE;
				double max = Integer.MIN_VALUE;
				double minTime = 0;
				double maxTime = 0;				
				
				for (Point p : this.mInBuffer){
					double val = p.getY();
					if (min > val){
						min = val;
						minTime = p.getX();
					}
					if (max < val){
						max = val;
						maxTime = p.getX();
					}						
				}									
				this.mInBuffer.remove(0);
				
				int direction = maxTime > minTime ? 1 : -1;
				
				if (direction > 0){	
					if (max < thr/2){
						return;
					}
				} else {
					if (min > -thr/2){
						return;
					}
				}							
				
				if (direction != 0){
					double p = Math.abs(max-min);								
					if (p >= thr){
						this.mDirection = direction;
						this.mPeakStartTime = point.getX();    			
						this.mPeakValue = new Point(this.mPeakStartTime, 0);
						this.mPrevValue = point.getY();
						this.mLocalMax = this.mPrevValue;
						this.mClearCounter = 0;		    					    		
						this.mPoints.add(point);
						this.mInBuffer.clear();						
						//this.startSound();
					}
				}
			}			
		}							
	}	
	
	public void init(){
		XYMultipleSeriesRenderer renderer = this.mView.getSeriesRenderer();
		XYMultipleSeriesDataset dataset = this.mView.getDataset();			
		
		int count = dataset.getSeriesCount();
		while (count-- > 0){
			dataset.removeSeries(0);
		}		
		renderer.removeAllRenderers();
		
		dataset.addSeries(0, this.mBlackData);
		dataset.addSeries(1, this.mColoredData);
		
		renderer.addSeriesRenderer(0, createXYSeriesRenderer(Color.BLACK, Color.GRAY));		
		renderer.addSeriesRenderer(1, createXYSeriesRenderer(Color.RED, Color.rgb(225, 177, 33)));
	}	
	
	public void clear(){
		super.clear();		
		this.mInBuffer.clear();
		this.mBlackData.clear();
		this.mColoredData.clear();	 	
	}		
	
	public double getPeakValue(){
		return this.mBuildPeakvalue;
	}
	
	private void buildPeak(){				
		this.mBlackData.clear();
		this.mColoredData.clear();		
		
		this.mBuildPeakvalue = Math.abs(this.mPeakValue.getY())
				-GlobalPreferences.getInstance(null).getThreshold();		
		if (this.mBuildPeakvalue == 0){
			//this.mSoundGenerateCounter = 0;
			this.mDirection = 0;
			this.mBuildPeakvalue = 0;
			return;
		}
		this.mBuildPeakvalue = this.mDirection * this.mBuildPeakvalue;
		
		double peakDuration = this.mPeakValue.getX() - this.mPeakStartTime;
		if (peakDuration > 1.5)
			peakDuration = 1.5;
		else if (peakDuration < 0.5)
			peakDuration = 0.5;
		
		double startX = 1 - peakDuration/2;
		
		double b = Math.abs(this.mBuildPeakvalue);			
		double a = -b/((startX-1)*(startX-1));		
		
		XYSeries data = this.mDirection < 0 ? this.mBlackData : this.mColoredData;
		double step = 0.02;
		for (double x = startX; true; x+= step){	
			double i = x - 1;
			double y = a * i * i + b;
			if (y < 0)
				y = 0;
			data.add(x, y);
			if (x > startX && y==0)
				break;
		}		
		this.mSoundGenerateCounter = (int) (Math.signum(mPeakValue.getY()) * peakDuration * 15);	
		this.mDirection = 0;			
	}	
	
	private int getTone(int peakValue){		
		if (peakValue > 0) {
			return 1500;
		} else {
			return 500;
		}	
	}
	
	private void generateSound(){				
		if (this.mSoundGenerateCounter == 0)
			return;
		
		SoundGenerator sound = SoundGenerator.getInstance();	
		if (this.mSoundGenerateCounter > 0)
			--this.mSoundGenerateCounter;
		else
			++this.mSoundGenerateCounter;
																			
		if (this.mSoundGenerateCounter == 0){	
			sound.stop();
		} else {
			sound.playTone(getTone(this.mSoundGenerateCounter));
		}			
	}
	
	private void startSound(){
		SoundGenerator sound = SoundGenerator.getInstance();
		sound.vibrate(false);
		sound.vibrate(true);
		this.mSoundGenerateCounter = (int)Math.signum(this.mLocalMax)*15;
	}
	
	private boolean clearViewRoutine(Point point){
		boolean res = false;
		if (point == null){
			if (this.mClearCounter++ == 50){				
				if (this.mBlackData.getItemCount() > 0){
					res = true;
					this.mBlackData.clear();
				}					
				if (this.mColoredData.getItemCount() > 0){
					res = true;
					this.mColoredData.clear();
				}												
			}				
			return res;
		}		
		
		if ((int)this.mPrevValue != (int)point.getY()){
			this.mClearCounter = 0;
			return res;
		}
		
		if (this.mClearCounter++ == 50){
			res = true;
			this.mClearCounter = 0;
			if (this.mPrevValue == 0){
				if (this.mBlackData.getItemCount() > 0)
					this.mBlackData.clear();				
				if (this.mColoredData.getItemCount() > 0)
					this.mColoredData.clear(); 				
				this.mDirection = 0;
			} else if (this.mBlackData.getItemCount() == 0 && 
					this.mColoredData.getItemCount() == 0){	
				this.buildPeak();				
			}
		}		
		return res;					
	}

	public boolean timeProcess(){
		this.generateSound();
		
		int pointsCount = this.mPoints.size();    	    	       
    	if (pointsCount == 0){
    		return clearViewRoutine(null);    		
    	}
    		        							    	
    	boolean res = false;
    	for (int i=0; i < pointsCount; ++i){
    		Point point = this.mPoints.get(i);    		
    		res = clearViewRoutine(point);
    		if (this.mDirection == 0)
    			break;    		
    		    		
    		double current = (Math.signum(point.getY()) == this.mDirection ? 1 : -1) * Math.abs(point.getY());    		
    		double prev = Math.abs(this.mPrevValue);      		
    		this.mPrevValue = point.getY();
    		
    		if (current > prev){     			
    			this.mLocalMax = this.mPrevValue;
    			if (Math.abs(this.mPeakValue.getY()) < Math.abs(this.mLocalMax))
    				this.mPeakValue.setX(point.getX());    			
    			continue;
    		}
    		
    		if (Math.abs(this.mPeakValue.getY()) < Math.abs(this.mLocalMax)){
    			this.mPeakValue.setY(this.mLocalMax);
				res = true;
				this.buildPeak();
			}
    		
    		if (current <= 0){    			
    			this.mDirection = 0;
    		} 
    	}    	
    	    	    
    	this.mPoints.clear();
    	return res;    	
	}
}
