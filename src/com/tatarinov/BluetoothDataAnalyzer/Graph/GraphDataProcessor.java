package com.tatarinov.BluetoothDataAnalyzer.Graph;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;
import com.tatarinov.BluetoothDataAnalyzer.UI.GraphView;

public class GraphDataProcessor {		
	
	protected List<Point> mPoints = new ArrayList<Point>();	
	
	protected GraphView mView;			
	
	public GraphDataProcessor(GraphView view){	
		this.mView = view;		
	}	
	
	public void init(){		
	}	
	
	protected XYSeriesRenderer createXYSeriesRenderer(int lineColor, int fillColor) {
		XYSeriesRenderer chartRenderer = new XYSeriesRenderer();
	    chartRenderer.setColor(lineColor);
	    chartRenderer.setPointStyle(PointStyle.POINT);
	    chartRenderer.setFillPoints(false);
	    chartRenderer.setLineWidth(8);
	    chartRenderer.setDisplayChartValues(false);        
	    FillOutsideLine fill = new FillOutsideLine(this.mView.isFillAreas() 
	    		? FillOutsideLine.Type.BOUNDS_ABOVE : FillOutsideLine.Type.NONE);
	    fill.setColor(fillColor);
	    chartRenderer.addFillOutsideLine(fill);               
	    return chartRenderer;
	} 
	
	public void addPoint(Point point){ 
		this.mPoints.add(point);
	}
	
	public boolean timeProcess(){		
		return false;    	
	}
	
	public void clear(){		
	 	this.mPoints.clear();    	
	}	
	
	public List<Point> getPoints(){
		return this.mPoints;
	}
}
