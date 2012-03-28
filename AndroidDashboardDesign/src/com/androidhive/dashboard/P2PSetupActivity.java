package com.androidhive.dashboard;

import java.util.Arrays;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.sensors.GPS.Location;
import sdmay1207.ais.sensors.SensorInterface.SensorType;
import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidhive.dashboard.R;

public class P2PSetupActivity extends Activity
{
    private MapView mapView;

    // 0: first point (from)
    // 1: second point (to)
    // 2: rally point
    private int settingPoint = 0;
    private Location[] selectedLocations = new Location[3];
    NodeController nc;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p2psetup);

        // set title
        ((TextView) findViewById(R.id.setupTitle))
                .setText("Tap to set the point to retrieve video from");

        // setup mapview
        mapView = (MapView) findViewById(R.id.setupMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15);
        mapView.getController().setCenter(new GeoPoint(42.024443, -93.656141));

        nc = ((DashboardApplication) getApplication()).nc;

        // set OK button listener
        ((Button) findViewById(R.id.setupOkButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        settingNextPoint();
                    }
                });

        ((Button) findViewById(R.id.setupCurLocButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Location curLoc = (Location) nc.sensorInterface
                                .getReading(SensorType.GPS);
                        selectedLocations[settingPoint] = curLoc;
                        syncOverlays();
                    }
                });

        final GestureDetector gd = new GestureDetector(
                new MapSingleTouchListener());
        mapView.setOnTouchListener(new OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                return gd.onTouchEvent(event);
            }
        });
    }

    private void settingNextPoint()
    {
        if (settingPoint == 0)
        {
            ((TextView) P2PSetupActivity.this.findViewById(R.id.setupTitle))
                    .setText("Tap to set the point to send video to");

            settingPoint = 1;
        } else if (settingPoint == 1)
        {
            ((TextView) P2PSetupActivity.this.findViewById(R.id.setupTitle))
                    .setText("Tap to set the point to rally at in case of failure");

            settingPoint = 2;
        } else if (settingPoint == 2)
        {
            Toast.makeText(
                    this,
                    "Initializing point-to-point task! Sending commands to all nodes in the network.",
                    3);

            System.out.println(Arrays.toString(selectedLocations));
            nc.p2pCmdr.initiateP2PTask(selectedLocations[0],
                    selectedLocations[1], selectedLocations[2], 1000000);
        }
    }

    private void syncOverlays()
    {
        mapView.getOverlays().clear();

        for (Location loc : selectedLocations)
        {
            if (loc != null)
            {
                SimpleLocationOverlay overlay = new SimpleLocationOverlay(
                        P2PSetupActivity.this);
                overlay.setLocation(new GeoPoint(loc.latitude, loc.longitude));
                mapView.getOverlays().add(overlay);
            }
        }

        mapView.postInvalidate();
    }

    private class MapSingleTouchListener extends SimpleOnGestureListener
    {
        public boolean onSingleTapUp(MotionEvent e)
        {
            IGeoPoint igp = mapView.getProjection().fromPixels(
                    e.getX(), e.getY());
            Location loc = new Location(igp.getLatitudeE6()
                    / Math.pow(10, 6), igp.getLongitudeE6()
                    / Math.pow(10, 6));
            selectedLocations[settingPoint] = loc;

            syncOverlays();
            return super.onSingleTapUp(e);
        };
    }
}
