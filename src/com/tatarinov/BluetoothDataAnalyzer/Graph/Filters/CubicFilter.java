package com.tatarinov.BluetoothDataAnalyzer.Graph.Filters;

import java.util.ArrayList;
import java.util.List;

import com.tatarinov.BluetoothDataAnalyzer.Graph.GraphDataProcessor;

public class CubicFilter extends Filter {
//	protected String getSeriesName(){
//		return "CubicLevel";
//	}
//	
//	private int mLastPointsCount = 0;
//	private int kSubDivPartCount = 4;
//	private List<Point> mFilterValues = new ArrayList<Point>();	
//	
//	public FilterTypes getFilterType(){
//		return FilterTypes.Cubic;
//	}
//		
//	public List<Point> getPoints(){
//		return this.mFilterValues;
//	}
//	
//	public boolean timeProcess(){
//		int filterValuesCount = this.mFilterValues.size();
//		int pointsCount = this.mPoints.size();
//		if (pointsCount == this.mLastPointsCount || !this.cubicInterpolation())
//			return false;			
//		
//		// Добавляем новые, фильтрованные значения		
//		for (int i = filterValuesCount; i < this.mFilterValues.size(); ++i){
//			Point point = this.mFilterValues.get(i);    			    			    		
//			this.mDataSeries.add(point.getX(), point.getY());    			
//		}	
//		
//		// Удаляем старые значения		
//		if (this.mPoints.size() > 0){
//			double lastX = this.mPoints.get(this.mPoints.size()-1).getX();				
//			double xMin = lastX - this.mWindowSize -  Math.min(this.mWindowSize/4, 5);					
//			for (int i = 0; i< this.mPoints.size(); ++i){    		
//				Point point = this.mPoints.get(i);
//				if (point.getX() >= xMin)
//					break;
//				
//				this.mDataSeries.remove(i);
//				this.mPoints.remove(i--);			
//			}			
//			for (int i = 0; i< this.mFilterValues.size(); ++i){    		
//				Point point = this.mFilterValues.get(i);
//				if (point.getX() >= xMin)
//					break;
//								
//				this.mFilterValues.remove(i--);			
//			}			
//		}		  				
//		
//		this.mLastPointsCount = this.mPoints.size();
//		return true;
//	}
//	
//	private boolean cubicInterpolation(){
//		List<Point> noneFilterValues = this.mPoints;
//		List<Point> filterValues = this.mFilterValues;
//		
//		if (noneFilterValues.size() < 5)
//			return false;
//
//		double subStep = 1.0/kSubDivPartCount;
//		int j = Math.max(0, this.mLastPointsCount-4);	
//		double xExtra = noneFilterValues.get(noneFilterValues.size()-1).getX()
//				-noneFilterValues.get(noneFilterValues.size()-2).getX();
//		for (; j < noneFilterValues.size()-4; ++j) {
//			Point p0 = noneFilterValues.get(j);
//			Point p1 = noneFilterValues.get(j +1);
//			Point p2 = noneFilterValues.get(j +2);
//			Point p3 = noneFilterValues.get(j +3);
//			
//			double x0 = p0.getX();
//			double xShift = (p1.getX()-x0)/kSubDivPartCount;			
//			
//			for (int i=0; i < kSubDivPartCount-1; ++i){
//				double mu = i * subStep;
//				double mu2 = mu * mu;
//				
//				double a0 = -0.5 * p0.getY() + 1.5 * p1.getY() - 1.5 * p2.getY() + 0.5 * p3.getY();
//				double a1 = p0.getY() - 2.5 * p1.getY() + 2 * p2.getY() - 0.5 * p3.getY();
//				double a2 = -0.5 * p0.getY() + 0.5 * p2.getY();
//				double a3 = p1.getY();
//				
//				double yn = a0 * mu * mu2 +a1 * mu2 +a2 * mu +a3;
//				filterValues.add(new Point(x0 + xShift * i + xExtra, yn));				
//			}			
//		}
//		return true;
//	}
//	
//	protected CubicFilter(List<Point> points){
//		super(points);
//	}
}
