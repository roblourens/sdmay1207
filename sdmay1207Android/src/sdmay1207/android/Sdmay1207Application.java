package sdmay1207.android;

import java.util.Random;

import sdmay1207.ais.NodeController;
import android.app.Application;
import android.util.Log;

public class Sdmay1207Application extends Application
{
    public NodeController nc = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        
        Log.d("application", "app onCreate");

        Random r = new Random();
        int nodeNumber = r.nextInt(245) + 10; // reserve the single-digit
                                              // ones

        String dataDir = getApplicationContext().getFilesDir().getParent();
        nc = new NodeController(nodeNumber, dataDir);
        System.out.println("Node #: " + nodeNumber);
    }
}
