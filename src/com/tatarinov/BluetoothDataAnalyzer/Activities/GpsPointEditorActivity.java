package com.tatarinov.BluetoothDataAnalyzer.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tatarinov.BluetoothDataAnalyzer.R;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsStorage;
import com.tatarinov.BluetoothDataAnalyzer.GPS.GpsPointsStorage.GPSPoint;

public class GpsPointEditorActivity extends Activity {
	private GPSPoint mPoint;
	private int mPointIndex;
	private EditText mEdit;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gps_point_editor);
		
		this.mEdit = (EditText)findViewById(R.id.nameEditor);
		
		Button applyButton = (Button)findViewById(R.id.applyButton);	     
		applyButton.setOnClickListener(new View.OnClickListener() {
			@Override
		    public void onClick(View view) {
				mPoint.id = mEdit.getText().toString();
			}
		});
		
		Button deleteButton = (Button)findViewById(R.id.deleteButton);	     
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
		    public void onClick(View view) {
				GpsPointsStorage.getInstance().removePoint(mPointIndex);
				setResult(RESULT_OK, getIntent());
				finish();
			}
		});				
	}
	
	public void onResume() {
		super.onResume();		
		
		Bundle b = getIntent().getExtras();
		this.mPointIndex = b.getInt("point_index");		
		this.mPoint = GpsPointsStorage.getInstance().getPoints().get(this.mPointIndex);
		
		TextView lgValue = (TextView)findViewById(R.id.longitudeValue);
		lgValue.setText(String.valueOf(this.mPoint.location.getLongitude()));
		
		TextView laValue = (TextView)findViewById(R.id.latitudeValue);
		laValue.setText(String.valueOf(this.mPoint.location.getLatitude()));			
		
		this.mEdit.setText(mPoint.id);			
	}
	
	public void onStop() {		
		super.onStop();		
	}
}
