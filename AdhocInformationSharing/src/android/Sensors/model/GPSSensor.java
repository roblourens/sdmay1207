package android.Sensors.model;

import android.GPS.view.R;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import sdmay1207.ais.sensors.GPS;

public class GPSSensor extends GPS implements LocationListener
{
	
	private LocationManager locManager;
	
	/*
	 * Subscribes to the LocationManager updater to receive GPS outputs
	 */
	public GPSSensor(){
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
	}

	/*
	 * Reads latest output and returns a new AISLocation object found in
	 * the GPS interface in AIS
	 */
	@Override
	public AISLocation getReading() 
	{
		Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location != null)
		{
			return new AISLocation(location.getLatitude(),location.getLongitude());
		} 
		else
		{
			return new AISLocation(0.0,0.0);
		}
	}

	/*
	 * Returns units in degrees
	 */
	@Override
	public String getUnits() 
	{
		return "degrees";
	}
	
	
	//LOCATIONLISTENER METHODS Unused
	
	//@Override  
	public void onLocationChanged(AISLocation location) {
	}

	//@Override
	public void onProviderDisabled(String arg0) {}

	//@Override
	public void onProviderEnabled(String arg0) {}

	//@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	

}
