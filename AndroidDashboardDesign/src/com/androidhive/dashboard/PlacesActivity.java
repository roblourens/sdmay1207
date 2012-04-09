package com.androidhive.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Node;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidhive.dashboard.R;

import com.androidhive.dashboard.DashboardApplication.Notification;

public class PlacesActivity extends Activity implements Observer
{
    /** Called when the activity is first created. */
    private MapView mapView;
    private TextView notificationView;

    private NodeController nc;
    boolean isStarted = false;
    private DashboardApplication da;

    private final int MAX_LINES_OF_NOTIFICATION_TEXT = 4;
    private final int MENU_ID_KILL = 7;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_layout);

        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setClickable(true);
        mapView.setKeepScreenOn(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15);
        mapView.getController().setCenter(new GeoPoint(42.024443, -93.656141));

        da = ((DashboardApplication) getApplication());
        nc = da.nc;
        notificationView = ((TextView) findViewById(R.id.notifications));
        notificationView.setLines(MAX_LINES_OF_NOTIFICATION_TEXT);

        // Set button listeners
        ((Button) findViewById(R.id.startStopButton))
        .setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (!isStarted)
                {
                    ((Button) findViewById(R.id.startStopButton))
                            .setText("Starting...");
                    ((Button) findViewById(R.id.startStopButton))
                            .setEnabled(false);
                    new StartupTask().execute();
                    
                    isStarted = true;
                    
                } else
                {
                    da.stop();
                    isStarted = false;

                    ((Button) findViewById(R.id.startStopButton))
                            .setText("Start");
                    //Disable network buttons
                    ((Button) findViewById(R.id.showNodeListButton))
                    .setEnabled(false);
                    ((Button) findViewById(R.id.initP2PButton))
                    .setEnabled(false);
                    ((Button) findViewById(R.id.directNeighborsButton))
                    .setEnabled(false);
                   
                    
                }
            }
        });
        
                	
        ((Button) findViewById(R.id.showNodeListButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent i = new Intent(PlacesActivity.this,
                                NodeListActivity.class);
                        i.putExtra(NodeListActivity.DIRECT_NEIGHBORS_ONLY_KEY,
                                false);
                        startActivity(i);
                    }
                });

        ((Button) findViewById(R.id.initP2PButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        if (isStarted)
                            startActivity(new Intent(PlacesActivity.this,
                                    P2PSetupActivity.class));
                        else
                            Toast.makeText(
                                    PlacesActivity.this,
                                    "You need to join the network before issuing a Point-to-Point command",
                                    5).show();
                    }
                });

        ((Button) findViewById(R.id.directNeighborsButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent i = new Intent(PlacesActivity.this,
                                NodeListActivity.class);
                        i.putExtra(NodeListActivity.DIRECT_NEIGHBORS_ONLY_KEY,
                                true);
                        startActivity(i);
                    }
                });

        notificationView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(PlacesActivity.this,
                        NotificationActivity.class));
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        updateMapObjects();
        updateNotificationView();

        // make sure we are observing when added
        da.nm.addObserver(this);
        nc.addNetworkObserver(this);
    }

    // http://code.google.com/p/osmdroid/issues/detail?id=267
    @Override
    protected void onPause()
    {
        super.onPause();
        System.out.println("onPause");
        mapView.getTileProvider().clearTileCache();

        // make sure refs are removed when we might die
        da.nm.deleteObserver(this);
        nc.removeNetworkObserver(this);
    }

    private void updateMapObjects()
    {
        mapView.getOverlays().clear();
        Collection<Node> nodes = nc.getNodesInNetwork().values();
        List<OverlayItem> items = new ArrayList<OverlayItem>();

        for (Node n : nodes)
        {
            if (n.lastLocation != null)
            {
                OverlayItem o1 = new OverlayItem("" + n.nodeNum, "title",
                        "desc", new GeoPoint(n.lastLocation.latitude,
                                n.lastLocation.longitude));
                if (n.nodeNum == nc.getMe().nodeNum)
                	o1.setMarker(writeOnDrawable(R.drawable.my_person,
                            ("" + n.nodeNum)));
                else if (!(nc.getNodesInNetwork().containsKey(n.nodeNum)))
                	o1.setMarker(writeOnDrawable(R.drawable.disappear_person,
                            ("" + n.nodeNum)));
                else
                	o1.setMarker(writeOnDrawable(R.drawable.other_person,
                        ("" + n.nodeNum)));
                items.add(o1);
            }
        }

        // now add from notifications
        // add notification map overlays
        for (Notification n : da.nm.notifications)
        {
            OverlayItem oi = n.getOverlayItem();
            if (oi != null)
                items.add(oi);
        }

        ItemizedOverlay<OverlayItem> overlay = new ItemizedIconOverlay<OverlayItem>(
                this, items, new OnItemGestureListener<OverlayItem>()
                {
                    public boolean onItemLongPress(int arg0, OverlayItem arg1)
                    {
                        return false;
                    }

                    public boolean onItemSingleTapUp(int i, OverlayItem item)
                    {
                        if (item.getUid().equals("notification"))
                            return false;

                        int nodeNum = Integer.parseInt(item.getUid());
                        Intent intent = new Intent(PlacesActivity.this,
                                NodeDetailsActivity.class);
                        intent.putExtra(NodeDetailsActivity.NODE_NUM_KEY,
                                nodeNum);
                        startActivity(intent);

                        System.out.println("tapped node # " + item.getUid());
                        return true;
                    }
                });

        mapView.getOverlays().add(overlay);
        mapView.postInvalidate();
    }

    // don't need to know what the new one is, just that there is a new
    // Notification.
    // display as many as we can, pulling from the NotificationManager
    private void updateNotificationView()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                notificationView.setText("");
                for (int i = 0; i < da.nm.notifications.size()
                        && i < MAX_LINES_OF_NOTIFICATION_TEXT; i++)
                {
                    String newText = "";
                    if (i != 0)
                        newText = "\n";

                    int index = da.nm.notifications.size() - i - 1;
                    Notification n = da.nm.notifications.get(index);
                    newText += "+ " + n.shortDisplayString();
                    notificationView.setText(notificationView.getText()
                            + newText);
                }
            };
        });
    }

    public void update(Observable observable, Object obj)
    {
        if (observable == da.nm)
        {
            updateNotificationView();
            return;
        }

        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event)
        {
        case RecvdHeartbeat:
            updateMapObjects();
            break;
        case NodeJoined:
            updateMapObjects();
            break;
        case NodeLeft:
            updateMapObjects();
            break;
        case SentHeartbeat:
            // in case we move/find position and are the only node
            updateMapObjects();
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, MENU_ID_KILL, Menu.NONE, "Kill");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == MENU_ID_KILL)
            System.out.println(1 / 0);

        return super.onOptionsItemSelected(item);
    }

    public BitmapDrawable writeOnDrawable(int drawableId, String text)
    {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 0, bm.getHeight() / 2, paint);

        return new BitmapDrawable(bm);
    }

    private class StartupTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            da.gps.start();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            da.start();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            ((Button) findViewById(R.id.startStopButton)).setText("Stop");
            //Enable buttons on screen
            ((Button) findViewById(R.id.startStopButton)).setEnabled(true);
            ((Button) findViewById(R.id.showNodeListButton)).setEnabled(true);
            ((Button) findViewById(R.id.initP2PButton)).setEnabled(true);
            ((Button) findViewById(R.id.directNeighborsButton)).setEnabled(true);
            
            isStarted = true;
        }
    }
}
