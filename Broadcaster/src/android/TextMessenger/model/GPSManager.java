package android.TextMessenger.model;

import java.text.DecimalFormat;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSManager implements LocationListener {
	
	private LocationManager locManager;
	
	public GPSManager(){
		locManager = ClassConstants.getInstance().getLocationManager();
	}

	public String getLocation() {
		Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location!=null){
			Double latitude = location.getLatitude();
			Double longitude = location.getLongitude();
			
	        DecimalFormat df = new DecimalFormat("#.####");
			return "Lat: "+df.format(latitude)+",Long: "+df.format(longitude);
		}
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
