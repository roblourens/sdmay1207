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
import sdmay1207.cc.Point2PointCommander.TooFewNodesException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

        setupForPoint(0);

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
                        if (selectedLocations[settingPoint] != null)
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

    @Override
    protected void onPause()
    {
        super.onPause();

        mapView.getTileProvider().clearTileCache();
        // mapView.destroyDrawingCache(); //?
    }

    @Override
    public void onBackPressed()
    {
        if (settingPoint == 0)
            finish();

        // remove this point and overlay
        selectedLocations[settingPoint] = null;
        syncOverlays();

        // reset UI
        settingPoint--;
        setupForPoint(settingPoint);
    }

    /**
     * Setup the UI for the point number, and complete on the last one
     * 
     * @param p
     *            Number of the point to setup for
     */
    private void setupForPoint(int p)
    {
        switch (p)
        {
        case 0:
            ((TextView) findViewById(R.id.setupTitle))
                    .setText("Tap the point to retrieve video from");
            break;
        case 1:
            ((TextView) P2PSetupActivity.this.findViewById(R.id.setupTitle))
                    .setText("Tap the point to send video to");
            break;
        case 2:
            ((TextView) P2PSetupActivity.this.findViewById(R.id.setupTitle))
                    .setText("Tap the rally point");
            break;
        default:
            break;
        }
    }

    private void settingNextPoint()
    {
        if (settingPoint == 2)
        {
            new P2PInitTask().execute();
            System.out.println(Arrays.toString(selectedLocations));
        } else
        {
            settingPoint++;
            setupForPoint(settingPoint);
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
            IGeoPoint igp = mapView.getProjection().fromPixels(e.getX(),
                    e.getY());
            Location loc = new Location(igp.getLatitudeE6() / Math.pow(10, 6),
                    igp.getLongitudeE6() / Math.pow(10, 6));

            if (settingPoint < 4)
                selectedLocations[settingPoint] = loc;

            syncOverlays();
            return super.onSingleTapUp(e);
        };
    }

    private void log(String msg)
    {
        Log.d("P2PSetupActivity", msg);
    }

    private class P2PInitTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ((Button) P2PSetupActivity.this.findViewById(R.id.setupOkButton))
                    .setText("Computing...");
            ((Button) P2PSetupActivity.this.findViewById(R.id.setupOkButton))
                    .setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                nc.p2pCmdr.initiateP2PTask(selectedLocations[0],
                        selectedLocations[1], selectedLocations[2], 100000);
            } catch (TooFewNodesException e)
            {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            ((Button) P2PSetupActivity.this.findViewById(R.id.setupOkButton))
                    .setText("Ok");
            ((Button) P2PSetupActivity.this.findViewById(R.id.setupOkButton))
                    .setEnabled(true);

            if (result)
            {
                System.out.println("p2p initiated, finishing");
                Toast.makeText(
                        P2PSetupActivity.this,
                        "Initializing point-to-point task! Sending commands to all nodes in the network.",
                        3).show();
                finish();
            } else
            {

                Toast.makeText(
                        P2PSetupActivity.this,
                        "Sorry, there are not enough nodes in the network to connect those two points.",
                        7).show();
                log("p2p too few nodes");
            }
        }
    }
}
