package android.TextMessenger.model;

import adhoc.aodv.Node;
import android.location.LocationManager;

public class ClassConstants {
	private Node node;
	private LocationManager locationManager;
	private static final ClassConstants INSTANCE = new ClassConstants();
	 
	   // Private constructor prevents instantiation from other classes
	   private ClassConstants() {}
	 
	   public static ClassConstants getInstance() {
	      return INSTANCE;
	   }
	   
	   
	   public void setNode(Node node){
		   this.node = node;
	   }
	   
	   public Node getNode(){
		   return node;
	   }


	   public void setLocationManager(LocationManager locationManager){
		   this.locationManager = locationManager;
	   }
	   
	   public LocationManager getLocationManager(){
		   return locationManager;
	   }
}
