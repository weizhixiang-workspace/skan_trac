package com.tatarinov.BluetoothDataAnalyzer.GPS;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;
import android.location.Location;

public class GpsPointsStorage {
	private static GpsPointsStorage instance;
	private List<GPSPoint> mCache = new ArrayList<GPSPoint>();	
	private Gson mGson;
	
	class LocationSerializer implements JsonSerializer<Location>
	{
		@Override
		public JsonElement serialize(Location t,
				java.lang.reflect.Type arg1, JsonSerializationContext arg2) {
			
			JsonObject jo = new JsonObject();

		    jo.addProperty("mAccuracy", t.getAccuracy());
		    jo.addProperty("mAltitude", t.getAltitude());
		    jo.addProperty("mBearing", t.getBearing());		    
		    //jo.addProperty("mElapsedRealtimeNanos", t.getElapsedRealtimeNanos());		    
		    jo.addProperty("mLatitude", t.getLatitude());
		    jo.addProperty("mLongitude", t.getLongitude());
		    
		    jo.addProperty("mProvider", t.getProvider());
		    jo.addProperty("mSpeed", t.getSpeed());
		    jo.addProperty("mTime", t.getTime());
		    
		    return jo;					
		}
	}

	class LocationDeserializer implements JsonDeserializer<Location>
	{
		@Override
		public Location deserialize(JsonElement je,
			java.lang.reflect.Type t, JsonDeserializationContext arg2)
					throws JsonParseException {					
			
			JsonObject jo = je.getAsJsonObject();
		    Location l = new Location(jo.getAsJsonPrimitive("mProvider").getAsString());
		    l.setAccuracy(jo.getAsJsonPrimitive("mAccuracy").getAsFloat());		    
		    l.setAltitude(jo.getAsJsonPrimitive("mAltitude").getAsDouble());		    
		    l.setAltitude(jo.getAsJsonPrimitive("mBearing").getAsFloat());		    
		    l.setLatitude(jo.getAsJsonPrimitive("mLatitude").getAsDouble());
		    l.setLongitude(jo.getAsJsonPrimitive("mLongitude").getAsDouble());		    
		    l.setSpeed(jo.getAsJsonPrimitive("mSpeed").getAsFloat());		    
		    l.setTime(jo.getAsJsonPrimitive("mTime").getAsLong());		    		    		    			 
		    
		    return l;			
		}
	}
	
	public class GPSPoint {
		public String id;
		
		public Location location;
		
		public GPSPoint(String id, Location loc){
			this.id = id;
			this.location = loc;
		}
		
		public GPSPoint(){			
		}
	}
	
	public static GpsPointsStorage getInstance(){
		if (instance == null){
			instance = new GpsPointsStorage();
		}
		return instance;
	}
	
	private GpsPointsStorage() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Location.class, new LocationDeserializer());
		gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
		mGson = gsonBuilder.create(); 
							  	
		try {
			JsonParser parser=new JsonParser();			
			JsonArray arr= parser.parse(GlobalPreferences.getInstance(null).getGpsPoints()).getAsJsonArray();		
			for (JsonElement jsonElement : arr) {
				mCache.add(mGson.fromJson(jsonElement, GPSPoint.class));			
			}
		} catch (Exception e) {				
		}		
	}
	
	public void addLocation(String id, Location location){
		this.mCache.add(new GPSPoint(id, location));
	}	
	
	public void removePoint(int index){
		this.mCache.remove(index);
	}
	
	public List<GPSPoint> getPoints(){
		return this.mCache;
	}
	
	public void save() {				
		GlobalPreferences.getInstance(null).setGpsPoints(mGson.toJson(mCache));			
	}
}
