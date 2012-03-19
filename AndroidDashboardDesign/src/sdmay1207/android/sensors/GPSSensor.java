package sdmay1207.android.sensors;

import sdmay1207.ais.sensors.GPS;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSSensor extends GPS implements LocationListener
{

    private LocationManager locManager;

    /*
     * Subscribes to the LocationManager updater to receive GPS outputs
     */
    public GPSSensor(Context c)
    {
        locManager = (LocationManager) c
                .getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
    }

    /*
     * Reads latest output and returns a new Location object
     */
    @Override
    public Location getReading()
    {
        android.location.Location location = locManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
        {
            return new Location(location.getLatitude(),
                    location.getLongitude());
        } else
        {
            return new Location(0.0, 0.0);
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

    //@Override
    public void onLocationChanged(android.location.Location location)
    {
    }

    //@Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    //@Override
    public void onProviderEnabled(String provider)
    {
    }

   // @Override
    public void onProviderDisabled(String provider)
    {
    }
}
