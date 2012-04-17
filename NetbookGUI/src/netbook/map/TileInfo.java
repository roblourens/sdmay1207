package netbook.map;

import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

public class TileInfo extends TileFactoryInfo{

	static String baseUrl = "file:dataDir/mapTiles";
	static int max = 18;
	

	public TileInfo(){
		super(
	        0, //min level
	        5, //max allowed level
	        max, // max level
	        256, //tile size
	        true, true, // x/y orientation is normal
	        baseUrl, // base url
	        "x","y","z" // url args for x, y & z
	        );
	}
	
	public String getTileUrl(int x, int y, int zoom) {
		//System.out.print("Getting Tile "+zoom+":"+x+":"+y);
		
		int zVal = max-zoom;
		
		//System.out.println("From: "+this.baseURL+"/"+zVal+"/"+x+"/"+y+".png.tile");
		return this.baseURL+"/"+zVal+"/"+x+"/"+y+".png.tile";

	}
	
}
