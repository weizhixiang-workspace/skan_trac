package com.tatarinov.BluetoothDataAnalyzer.Activities;

import com.tatarinov.BluetoothDataAnalyzer.R;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsLocationService;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsLocationService.IGetLocationCallback;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsLocationService.ILocationChangeListener;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsListArrayAdapter;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsStorage;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class GpsPointsListActivity extends ListActivity {
	private GpsLocationService mLocationService;
	private TextView mStatusLabel;
	private GetLocationCallback mGetCallback = new GetLocationCallback();	
	private ChangeLocationListener mListener = new ChangeLocationListener();
	
	private class GetLocationCallback implements IGetLocationCallback {
		public void onGetLocation(Location location){
			addCurrentLocation(location);
		}
	}
	
	private class ChangeLocationListener implements ILocationChangeListener {
		public void onLocationChange(Location location) {		
		}
				
		public void onGPSStatusChanged(String status){
			mStatusLabel.setText(status);			
		}
	}	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mLocationService = GpsLocationService.getInstance(this);		

	    setListAdapter(new GpsPointsListArrayAdapter(this));		
	    setContentView(R.layout.gps_list);	   
	    
		this.mStatusLabel = (TextView) findViewById(R.id.statusLabel);
	     
	    Button addButton = (Button)findViewById(R.id.gpsAddPointButton);	     
	    addButton.setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {	    		 
	    		mLocationService.getLastLocation(mGetCallback);	    		 	    		 
	    	}
	    });	    	   
	}
	
	public void onResume() {
		super.onResume();		
		mLocationService.startListenUpdates();
		mLocationService.addChangeListener(this.mListener);
	}
	
	public void onStop() {
		super.onStop();	
		mLocationService.removeChangeListener(this.mListener);
		GpsPointsStorage.getInstance().save();
	}
	
	public void showInNavi(int pointIndex) {	
		Intent intent = new Intent(this, GpsPointNaviActivity.class);
		
		Bundle b = new Bundle();
		b.putInt("point_index", pointIndex);
		intent.putExtras(b);
		
		startActivity(intent);		
	}
	
	public void editPoint(int pointIndex) {
		Intent intent = new Intent(this, GpsPointEditorActivity.class);
		
		Bundle b = new Bundle();
		b.putInt("point_index", pointIndex);
		intent.putExtras(b);
		
		startActivityForResult(intent, 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);	    
	    GpsPointsListArrayAdapter adapter = (GpsPointsListArrayAdapter)getListAdapter();	    	    
	    adapter.updateData();	    
	}
	 
	private void addCurrentLocation(Location location) {		
		String id = String.format("lg:%.3f lat: %.3f", location.getLongitude(), location.getLatitude());		 
		GpsPointsStorage.getInstance().addLocation(id, location);
		GpsPointsListArrayAdapter adapter = (GpsPointsListArrayAdapter)getListAdapter();
		adapter.add(id);
	}
	 
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {		
		GpsPointsListArrayAdapter adapter = (GpsPointsListArrayAdapter)getListAdapter();		
		adapter.itemClick(v, position, true);
		adapter.notifyDataSetChanged();
	}
}
