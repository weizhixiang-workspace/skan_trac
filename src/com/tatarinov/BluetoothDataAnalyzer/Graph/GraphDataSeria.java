package com.tatarinov.BluetoothDataAnalyzer.Graph;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.model.XYSeries;

import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;

public class GraphDataSeria {
	private XYSeries mData;
	private boolean mIsFiltraionEnabled;
	private List<Point> mBuffer = new ArrayList<Point>();
	private int mFilterLength;
	
	public GraphDataSeria(String name){
		this.mData = new XYSeries(name);
		GlobalPreferences gl = GlobalPreferences.getInstance(null);
		this.mIsFiltraionEnabled = gl.isEnableFiltration();
		this.mFilterLength = gl.getFilterLength();		
	}
	
	public XYSeries getData(){
		return this.mData;
	}	
	
	public void clear(){
		this.mBuffer.clear();
		this.mData.clear();
	}
	
	public void add(double x, double y){
		GlobalPreferences gl = GlobalPreferences.getInstance(null);
		boolean isFiltraionEnable = gl.isEnableFiltration();		
		if (this.mIsFiltraionEnabled != isFiltraionEnable){
			this.clear();
			this.mIsFiltraionEnabled = isFiltraionEnable;
		}
		
		if (this.mIsFiltraionEnabled){
			int filterLength = GlobalPreferences.getInstance(null).getFilterLength();
			if (filterLength != this.mFilterLength){
				this.clear();
				this.mFilterLength = GlobalPreferences.getInstance(null).getFilterLength();
			}
			
			int i = this.mBuffer.size();
			if (i < mFilterLength){
				this.mBuffer.add(new Point(x, y));
			} else {
				this.mBuffer.remove(0);
				this.mBuffer.add(new Point(x, y));
				double out = 0;
				for (Point p : this.mBuffer){
					out += p.getY();				
				}				
				out = out/mFilterLength;
				this.mData.add(this.mBuffer.get(this.mBuffer.size()-1).getX(), out);
			}
		} else {
			this.mData.add(x, y);
		}
	}
	
	public double getX(int index){
		return this.mData.getX(index);
	}
	
	public void remove(int index){
		this.mData.remove(index);
	}
	
	public int getItemCount(){
		return this.mData.getItemCount();	
	}
}
