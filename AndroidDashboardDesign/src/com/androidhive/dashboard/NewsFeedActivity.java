package com.androidhive.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.LocationListenerProxy;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidhive.dashboard.R;

public class NewsFeedActivity extends Activity {
    /** Called when the activity is first created. */
	private List<OverlayItem> wayPointItems = new ArrayList<OverlayItem>();
	MapView mapView;
	
	MyLocationOverlay mlay;
	PathOverlay pathOverlay;
	SimpleLocationOverlay myLocationOverlay;
	ResourceProxy rp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager locmanager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider= LocationManager.GPS_PROVIDER;
        Location location= locmanager.getLastKnownLocation(provider);
        
       
        setContentView(R.layout.news_feed_layout);

       mapView= (MapView) findViewById(R.id.map1);
       
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15);
        
        mapView.getController().setCenter(new GeoPoint( 42.0347222, -93.6197222));
        
        createOverlays();
    }
    private void createOverlays() {
		pathOverlay = new PathOverlay(Color.BLUE, this);
		pathOverlay.addPoint(new GeoPoint( 42.0347222, -93.6197222));
		pathOverlay.addPoint(new GeoPoint( 42.0357221, -93.6197222));
		pathOverlay.addPoint(new GeoPoint( 42.0347222, -93.6207221));
		//pathOverlay.addPoint(new GeoPoint( 42.0347222, -93.6197222));
		mapView.getOverlays().add(pathOverlay);
		
		PathOverlay mpathOverlay = new PathOverlay(Color.RED, this);
		mpathOverlay.addPoint(new GeoPoint( 42.0347222, -93.6197222));
		mpathOverlay.addPoint(new GeoPoint( 42.0337221, -93.6197222));
		mpathOverlay.addPoint(new GeoPoint( 42.0347222, -93.6187221));
		//pathOverlay.addPoint(new GeoPoint( 42.0347222, -93.6197222));
		mapView.getOverlays().add(mpathOverlay);
		
		mlay= new MyLocationOverlay(this,mapView);
		mlay.enableCompass();
		mlay.enableMyLocation();
		LocationListenerProxy llp= mlay.mLocationListener;
		//llp.startListening(pListener, pUpdateTime, pUpdateDistance)
		mapView.getOverlays().add(mlay);
		if(mlay.getMyLocation()!=null)
		{
			System.out.println("H"+mlay.getMyLocation().getLatitudeE6());
			mapView.getController().setCenter(new GeoPoint( mlay.getMyLocation().getLatitudeE6(), mlay.getMyLocation().getLongitudeE6()));
		}
		
		myLocationOverlay = new SimpleLocationOverlay(this);
		myLocationOverlay.setLocation(new GeoPoint( 42.0347222, -93.6197222));
		mapView.getOverlays().add(myLocationOverlay);
		
		ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(mScaleBarOverlay);
        mapView.postInvalidate();
		}
}
