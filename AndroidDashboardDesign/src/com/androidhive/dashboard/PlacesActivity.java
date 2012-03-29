package com.androidhive.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Node;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidhive.dashboard.R;

public class PlacesActivity extends Activity implements Observer
{
    /** Called when the activity is first created. */
    MapView mapView;

    private NodeController nc;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_layout);

        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15);
        mapView.getController()
                .setCenter(new GeoPoint(42.024443, -93.656141));

        nc = ((DashboardApplication) getApplication()).nc;
        nc.addNetworkObserver(this);

        // Set button listeners
        final Context c = this;
        ((Button) findViewById(R.id.showNodeListButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startActivity(new Intent(c, NodeListActivity.class));
                    }
                });

        ((Button) findViewById(R.id.initP2PButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startActivity(new Intent(c, P2PSetupActivity.class));
                    }
                });

        ((Button) findViewById(R.id.stopButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        nc.stop();
                        finish();
                    }
                });

        updateMapObjects();
    }
    
    // http://code.google.com/p/osmdroid/issues/detail?id=267
    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.getTileProvider().clearTileCache();
    }

    private void updateMapObjects()
    {
        mapView.getOverlays().clear();
        int me= 0;
        Collection<Node> nodes = nc.getNodesInNetwork().values();
        List<OverlayItem> items = new ArrayList<OverlayItem>();
        for (Node n : nodes)
        {
            if (n.lastLocation != null)
            {
                items.add(new OverlayItem(""+n.nodeNum, "title", "desc", new GeoPoint(n.lastLocation.latitude,
                        n.lastLocation.longitude)));
            }
            
        }
       
        final Context c = this;
        ItemizedOverlay<OverlayItem> overlay = new ItemizedOverlayWithFocus<OverlayItem>(this, items,
                new OnItemGestureListener<OverlayItem>(){
                    public boolean onItemLongPress(int arg0, OverlayItem arg1)
                    {
                        return false;
                    }

                    public boolean onItemSingleTapUp(int i, OverlayItem item)
                    {
                        int nodeNum = Integer.parseInt(item.getUid());
                        Intent intent = new Intent(c, NodeDetailsActivity.class);
                        intent.putExtra(NodeDetailsActivity.NODE_NUM_KEY, nodeNum);
                        startActivity(intent);
                        
                        System.out.println("tapped node # " + item.getUid());
                        return true;
                    }
                });
       
        mapView.getOverlays().add(overlay);
        mapView.postInvalidate();
    }
    
    private void updateTextMessageView(final String message)
    {
        runOnUiThread(new Runnable() {
            public void run() {
            	TextView msg= ((TextView) findViewById(R.id.recvdMessages));
            	msg.setText(message);
            	//msg.setHeight(10);
            };
        });
    }

    public void update(Observable observable, Object obj)
    {
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
        case RecvdTextMessage:
            updateTextMessageView(netEvent.data.toString());
            break;
        }
    }
}
