package android.GPS.view;

import android.GPS.control.ButtonListener;
import android.GPS.view.R;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class GPSActivity extends Activity implements LocationListener
{
    Button button;
    LocationManager locManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ButtonListener listener = new ButtonListener(this);
        button = (Button) findViewById(R.id.retrieveButton);
        button.setOnClickListener(listener);

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        locManager.removeUpdates(this);
    }

    public void clickConnect()
    {
        Location location = locManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
        {
            this.showLocation(location);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        System.out.println("Location changed");
    }

    @Override
    public void onProviderDisabled(String arg0)
    {
    }

    @Override
    public void onProviderEnabled(String arg0)
    {
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2)
    {
    }

    private void showLocation(Location location)
    {
        EditText gpsText = (EditText) findViewById(R.id.gpsCoords);
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        gpsText.append("> Lat: " + latitude + "\n   Long: " + longitude + "\n");
    }
}
