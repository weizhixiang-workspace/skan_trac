package com.tatarinov.BluetoothDataAnalyzer.GPS;

import java.util.List;

import com.tatarinov.BluetoothDataAnalyzer.R;
import com.tatarinov.BluetoothDataAnalyzer.Activities.GpsPointsListActivity;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsStorage.GPSPoint;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class GpsPointsListArrayAdapter extends ArrayAdapter<String> {
	private GpsPointsListActivity mContext;
	private int mSelection = -1;	
	
    public GpsPointsListArrayAdapter(GpsPointsListActivity context) {
    	super(context, 0);
    	this.mContext = context;
    	this.addPoints();    	
    }
    
    public void itemClick(View view, int position, boolean selected) {        	    	
    	ViewHolder viewHolder; 
    	viewHolder = (ViewHolder) view.getTag();   
    	if (selected) {    		
    		viewHolder.findButton.setVisibility(View.VISIBLE);
        	viewHolder.editButton.setVisibility(View.VISIBLE);
        	view.setBackgroundColor(Color.rgb(133, 133, 133));
        	this.mSelection = position;
    	} else {
    		viewHolder.findButton.setVisibility(View.INVISIBLE);
        	viewHolder.editButton.setVisibility(View.INVISIBLE);
        	view.setBackgroundColor(Color.TRANSPARENT);
    	}   	
    }
    
    static class ViewHolder {        
        public TextView textView;
        public Button findButton;
        public Button editButton;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {    	
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               
        ViewHolder holder;
        View rowView = convertView;
        
        if (rowView == null){
        	rowView = inflater.inflate(R.layout.gps_list_element, parent, false);
        	
        	holder = new ViewHolder();
            holder.textView = (TextView) rowView.findViewById(R.id.gpsPointLabel);
            holder.findButton = (Button) rowView.findViewById(R.id.showInMapButton);
            holder.editButton = (Button) rowView.findViewById(R.id.editGpsPointButton);
            
            holder.findButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					mContext.showInNavi(mSelection);					
				}
			});
            
            holder.editButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {					
					mContext.editPoint(mSelection);									
				}
			});
            
            rowView.setTag(holder);
        	 
        } else {
        	holder = (ViewHolder) rowView.getTag();
        }
        holder.textView.setText(GpsPointsStorage.getInstance().getPoints().get(position).id);              
        this.itemClick(rowView, position, position == this.mSelection || this.mSelection == -1);                
    	return rowView;
    }
    
    public void updateData(){    	
    	if (getCount() != GpsPointsStorage.getInstance().getPoints().size()){
    		super.clear();
        	this.mSelection = -1;
        	this.addPoints();
    	} else {
    		notifyDataSetChanged();
    	}    	    	    	    	    	
    }
    
    private void addPoints(){
    	List<GPSPoint> points = GpsPointsStorage.getInstance().getPoints();
    	for (GPSPoint point : points) {
    		this.add(point.id);
    	}    	
    	this.notifyDataSetChanged();
    }
}
