package com.tatarinov.BluetoothDataAnalyzer.UI;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;
import com.tatarinov.BluetoothDataAnalyzer.Graph.DynamicGDP;
import com.tatarinov.BluetoothDataAnalyzer.Graph.GraphDataProcessor;
import com.tatarinov.BluetoothDataAnalyzer.Graph.GraphWorkMode;
import com.tatarinov.BluetoothDataAnalyzer.Graph.Point;
import com.tatarinov.BluetoothDataAnalyzer.Graph.StaticGDP;

public class GraphView {		
	private static final int kDisplayValueHeight = 5;    
	
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mSeriesRenderer;
    private GraphicalView mChart;
    private GraphDataProcessor mGraphData;
    
    private boolean mIsFillAreas;    
    private double mWindowSize;      
    private GraphWorkMode mWorkMode;
    
    private boolean mIsNeedRefresh;      
           
    public void addValue(Point point){
    	this.mGraphData.addPoint(point);    	
    }
    
    public void setFillAreas(boolean fill){  
    	if (this.mIsFillAreas == fill)
    		return;
    	this.mIsFillAreas = fill;
    	
    	this.mGraphData.init();    
    	this.refresh();		
    }
    
    public boolean isFillAreas(){
    	return this.mIsFillAreas;
    }
    
    public XYMultipleSeriesDataset getDataset(){
    	return this.mDataset;
    }
    
    public XYMultipleSeriesRenderer getSeriesRenderer(){
    	return this.mSeriesRenderer;
    }

    public void setMode(GraphWorkMode workMode){      	
    	this.mWorkMode = workMode;    	
    	if (workMode == GraphWorkMode.Static){    				
			this.mGraphData = new StaticGDP(this);	
			this.mWindowSize = GlobalPreferences.getInstance(null).getWindowSize();			
    	} else {
    		this.mGraphData = new DynamicGDP(this);
    	}    	    	
    	this.mGraphData.init();    	 
		this.refresh();
    }
    
    public GraphView(){
    	this.mDataset = new XYMultipleSeriesDataset();
    	this.mSeriesRenderer= createXYMultipleSeriesRenderer();    	
    	this.mGraphData = new GraphDataProcessor(this);	    			    							
    }   
        
    public void setWindowSize(double value){    	    	    	    	        	    	    	  
    	this.mWindowSize = value;
    	this.mSeriesRenderer.setXAxisMin(this.mSeriesRenderer.getXAxisMax()-value);    	    	   
    }
    
    public double getWindowSize(){
    	return this.mWindowSize;
    }
    
    public void clear(){    	
    	this.mGraphData.clear();
    	this.refresh();
    }          
    
    public void refresh(){
    	this.mIsNeedRefresh = true;    	
    }
    
    public void timeProcess(){    	
    	// Процессим данные
    	if (this.mGraphData.timeProcess()){
    		this.mIsNeedRefresh = true;
    	}    	
    	
    	// Если были обновления, то изменяем и перерисовываем график
    	if (this.mIsNeedRefresh){
    		this.mIsNeedRefresh = false;
    		
    		if (this.mWorkMode == GraphWorkMode.Static){
    			this.processStaticMode();
    		} else {
    			this.processDynamicMode();
    		}    		    		       	
    		
    		this.mChart.repaint();  	    		    		
    	}
    }
    
    private void processDynamicMode(){
    	DynamicGDP gdp = (DynamicGDP)this.mGraphData;
    	
    	// Устанавливаем в качестве титула текущее максимальное значение
    	int maxPeakValue = (int)gdp.getPeakValue();    	
    	this.mSeriesRenderer.setChartTitle(String.format("Max value: %d", maxPeakValue));
    	
    	if (this.mSeriesRenderer.getXAxisMax() != 2)
    		this.mSeriesRenderer.setXAxisMax(2);	    
    	if (this.mSeriesRenderer.getXAxisMin() != 0)    		
    		this.mSeriesRenderer.setXAxisMin(0);    	
    			    	    	    
    	maxPeakValue = Math.abs(maxPeakValue);
    	double yMax = 0;    	    	 
    	if (maxPeakValue < 10) {
    		yMax = 30;    		    		    	
    	} else if (maxPeakValue < 100) {
    		double part = 0.33 * (maxPeakValue-10.0)/90.0 +0.33;    		
    		yMax = maxPeakValue * 1/part;
    	} else if (maxPeakValue < 1000) {
    		double part = 0.33 * (maxPeakValue-100.0)/900.0 +0.66;    		
    		yMax = maxPeakValue * 1/part;
    	} else {
    		this.updateYLimits();   
    		return;
    	}    	
    	if (this.mSeriesRenderer.getYAxisMin() != 0)
    		this.mSeriesRenderer.setYAxisMin(0);   
    	this.mSeriesRenderer.setYAxisMax(yMax);    	    	
    }
    
    private void processStaticMode(){
    	List<Point> dataPoints = this.mGraphData.getPoints();  
    	
    	double xAxisMax = this.mSeriesRenderer.getXAxisMax();
    	double xAxisMin = this.mSeriesRenderer.getXAxisMin();
    	if ((xAxisMax-xAxisMin) != this.mWindowSize){
    		this.setWindowSize(this.mWindowSize);
    	}
   
		if (dataPoints.size() > 0) {
    		// Устанавливаем новый титул для графика    	    			
    		Point last = dataPoints.get(dataPoints.size()-1);    		    		
    		this.mSeriesRenderer.setChartTitle(String.format("Current value: %d", (int)last.getY()));
    		
    		// Сдвигаем ось времени на время новых значений
    		double xMax = this.mSeriesRenderer.getXAxisMax();
    		if (last.getX() > xMax){
    			double shiftX = last.getX() - xMax;    			
    			this.mSeriesRenderer.setXAxisMax(shiftX + xMax);
    			this.mSeriesRenderer.setXAxisMin(shiftX + mSeriesRenderer.getXAxisMin());    			
    		}
    		
    		// Изменяем масштаб оси Y, если нужно
    		this.updateYLimits();    		    		    		     
		}
    }
    
    public GraphicalView getView(Context context){    
        mChart = ChartFactory.getTimeChartView(context, mDataset, mSeriesRenderer, "");                       
        return mChart;
    }   
                   
    private void updateYLimits(){
		double yMax = this.mSeriesRenderer.getYAxisMax();
		double yMin = this.mSeriesRenderer.getYAxisMin();
		double yRange = yMax - yMin;
		double dataYmax = Integer.MIN_VALUE;
		double dataYmin = Integer.MAX_VALUE;
		double decreaseLimitScale = 0.65;
		double increaseLimitScale = 1.5; 
		
		for (XYSeries xySeries : this.mDataset.getSeries()){
			double max = xySeries.getMaxY();
			double min = xySeries.getMinY();    			    	
			if (max > dataYmax) {
				dataYmax = max;
			}
			if (min < dataYmin){
				dataYmin = min;
			}
		}
		
		double dataRange = dataYmax - dataYmin;			
		if ((dataYmax > yMax || dataYmin < yMin) 
				|| (dataRange == 0 && yRange != kDisplayValueHeight)
				|| (dataRange/yRange < decreaseLimitScale)){
			
			if (dataRange == 0){
	    		dataRange = kDisplayValueHeight;	
	    	} else {
	    		dataRange *= increaseLimitScale;    
	    	}   
			
			dataYmax = dataYmin + dataRange;      	
        	this.mSeriesRenderer.setYAxisMin(0);
        	this.mSeriesRenderer.setYAxisMax(dataYmax);	
		}      	   
    }

    private XYMultipleSeriesRenderer createXYMultipleSeriesRenderer() {
        XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();

        multipleRenderer.setAntialiasing(true);
        multipleRenderer.setInScroll(true);

        multipleRenderer.setChartTitle("Current value:");
        multipleRenderer.setChartTitleTextSize((float) 20.0);
        multipleRenderer.setXTitle("Seconds");
        multipleRenderer.setYTitle("Count");               

        multipleRenderer.setPanEnabled(false, false);

        multipleRenderer.setXAxisMin(0);
        multipleRenderer.setXAxisMax(2);

        multipleRenderer.setYAxisMin(0);
        multipleRenderer.setYAxisMax(kDisplayValueHeight);

        multipleRenderer.setBarSpacing(2);
        multipleRenderer.setShowGrid(false);
        multipleRenderer.setApplyBackgroundColor(true);
        multipleRenderer.setBackgroundColor(Color.WHITE);
        multipleRenderer.setMarginsColor(Color.WHITE);

        multipleRenderer.setAxesColor(Color.WHITE);
        multipleRenderer.setYLabelsColor(0, Color.BLACK);
        multipleRenderer.setXLabelsColor(Color.WHITE);
        multipleRenderer.setGridColor(Color.BLACK);
        multipleRenderer.setLabelsColor(Color.BLACK);

        multipleRenderer.setZoomEnabled(false, false);
        multipleRenderer.setShowLegend(false);
        multipleRenderer.setShowAxes(false);
        multipleRenderer.setShowLabels(true);               

        return multipleRenderer;
    }
}
