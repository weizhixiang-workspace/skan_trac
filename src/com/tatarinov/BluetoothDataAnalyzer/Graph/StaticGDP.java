package com.tatarinov.BluetoothDataAnalyzer.Graph;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import android.graphics.Color;
import com.tatarinov.BluetoothDataAnalyzer.UI.GraphView;

public class StaticGDP extends GraphDataProcessor {
	private GraphDataSeria mBlackData;
	private GraphDataSeria mColoredData;
	private int mLastPointsCount;	
	
	public StaticGDP(GraphView view) {
		super(view);	
		
		this.mBlackData = new GraphDataSeria("BlackLevel"); 
		this.mColoredData = new GraphDataSeria("ColoredLevel");		
	}
		 
	public void init(){
		XYMultipleSeriesRenderer renderer = this.mView.getSeriesRenderer();
		XYMultipleSeriesDataset dataset = this.mView.getDataset();
		
		int count = dataset.getSeriesCount();
		while (count-- > 0){
			dataset.removeSeries(0);
		}		
		renderer.removeAllRenderers();
				
		dataset.addSeries(0, this.mColoredData.getData());
		dataset.addSeries(1, this.mBlackData.getData());
		
		renderer.addSeriesRenderer(0, createXYSeriesRenderer(Color.RED, Color.rgb(225, 177, 33)));
		renderer.addSeriesRenderer(1, createXYSeriesRenderer(Color.BLACK, Color.GRAY));		
	}	

	public boolean timeProcess(){					
    	int pointsCount = this.mPoints.size();    	    	       
    	if (pointsCount == mLastPointsCount)
    		return false;    	        
	
    	// 1. Добавляем новые точки
		for (int i = mLastPointsCount; i < pointsCount; ++i){
			Point point = this.mPoints.get(i);			
			if (point.getY() > 0){
				this.mBlackData.add(point.getX(), 0);
				this.mColoredData.add(point.getX(), point.getY());				
			} else {
				this.mColoredData.add(point.getX(), 0);
				this.mBlackData.add(point.getX(), -point.getY());							
			}						
		}
		
		// 2. Удаляем старые значения	
		if (this.mPoints.size() > 0){
			double windowSize = mView.getWindowSize();
			double lastX = this.mPoints.get(this.mPoints.size()-1).getX();				
			double xMin = lastX - windowSize -  Math.min(windowSize/5, 5);						
			
			for (int i = 0; i< this.mPoints.size(); ++i){    		
				Point point = this.mPoints.get(i);
				if (point.getX() >= xMin)
					break;
												
				this.mPoints.remove(i--);			
			}	
			
			for (int i = 0; i< this.mBlackData.getItemCount(); ++i){
				double val = this.mBlackData.getX(i);				
				if (val >= xMin)
					break;
												
				this.mBlackData.remove(i--);			
			}	
			
			for (int i = 0; i< this.mColoredData.getItemCount(); ++i){
				double val = this.mColoredData.getX(i);				
				if (val >= xMin)
					break;
								
				this.mColoredData.remove(i);						
			}	
		}	
		
		this.mLastPointsCount = this.mPoints.size();
    	return true;
	}

}
