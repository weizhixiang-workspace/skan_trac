package com.tatarinov.BluetoothDataAnalyzer.Graph.Filters;

import java.util.ArrayList;
import java.util.List;

import com.tatarinov.BluetoothDataAnalyzer.Graph.GraphDataProcessor;

public class LinearFilter extends Filter {
//	protected String getSeriesName(){
//		return "LinearLevel";
//	}
//		
//	private int mLastPointsCount = 0;
//	private final int kSubDivPartCount = 3;
//	private List<Point> mFilterValues = new ArrayList<Point>();
//	
//	protected LinearFilter(List<Point> points) {
//		super(points);		
//	}
//	
//	public FilterTypes getFilterType(){
//		return FilterTypes.Linear;
//	}
//	
//	public boolean timeProcess(){
//		int pointsCount = this.mPoints.size();
//		int filterValuesCount = this.mFilterValues.size();
//		if (pointsCount == this.mLastPointsCount)
//			return false;			
//		
//		// Фильтруем значения
//		this.linearInterpolation();		
//		
//		// Добавляем новые, фильтрованные значения
//		for (int i = filterValuesCount; i < this.mFilterValues.size(); ++i){
//			Point point = this.mFilterValues.get(i);    			    			    		
//			this.mDataSeries.add(point.getX(), point.getY());    			
//		}
//				
//		// Удаляем старые значения	
//		if (this.mPoints.size() > 0){
//			double lastX = this.mPoints.get(pointsCount - 1).getX();				
//			double xMin = lastX - this.mWindowSize -  Math.min(this.mWindowSize/5, 5);					
//			for (int i = 0; i< pointsCount; ++i){    		
//				Point point = this.mPoints.get(i);
//				if (point.getX() >= xMin)
//					break;
//										
//				this.mPoints.remove(i--);			
//			}	
//			for (int i = 0; i< this.mFilterValues.size(); ++i){    		
//				Point point = this.mFilterValues.get(i);
//				if (point.getX() >= xMin)
//					break;
//				
//				this.mDataSeries.remove(i);
//				this.mFilterValues.remove(i--);			
//			}
//		}
//	
//		this.mLastPointsCount = this.mPoints.size();
//		return true;		
//	}
//	
//	private void linearInterpolation(){
//		List<Point> noneFilterValues = this.mPoints;
//		List<Point> filterValues = this.mFilterValues;
//		
//		int j = this.mLastPointsCount-1;	
//		for (; j < noneFilterValues.size()-1; ++j) {
//			if (j < 0)
//				continue;
//			Point p0 = noneFilterValues.get(j);
//			Point p1 = noneFilterValues.get(j+1);
//						
//			double yShift = (p1.getY() - p0.getY())/kSubDivPartCount;
//			double xShift = (p1.getX() - p0.getX())/kSubDivPartCount;
//			for (int i = 0; i < kSubDivPartCount; ++i){				
//				filterValues.add(new Point(p0.getX() + xShift * i, p0.getY() + yShift * i));
//			}
//		}					
//		filterValues.add(noneFilterValues.get(j));
//	}
}