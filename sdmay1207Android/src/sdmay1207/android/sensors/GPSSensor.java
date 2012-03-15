package sdmay1207.android.sensors;

import sdmay1207.ais.sensors.GPS;
import android.content.Context;
import android.location.LocationManager;

public class GPSSensor extends GPS
{

    private LocationManager locManager;

    /*
     * Subscribes to the LocationManager updater to receive GPS outputs
     */
    public GPSSensor(Context c)
    {
        locManager = (LocationManager) c
                .getSystemService(Context.LOCATION_SERVICE);
    }

    /*
     * Reads latest output and returns a new AISLocation object found in the GPS
     * interface in AIS
     */
    @Override
    public GPS.Location getReading()
    {
        android.location.Location location = locManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
        {
            return new GPS.Location(location.getLatitude(),
                    location.getLongitude());
        } else
        {
            return new GPS.Location(0.0, 0.0);
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
}
