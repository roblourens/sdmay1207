package sdmay1207.ais;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.TextMessenger.view.Connect;

// Maybe there could be a config file for each device with the things here and
// available sensors, etc.
public class Device
{
    // Android-only
    // What happens if the library isn't loaded?
    private static int runCommand(String command)
    {
        return Connect.runCommand(command);
    }

    private static String dataDir;

    public static void setDataDir(String dataDir)
    {
        Device.dataDir = dataDir;
    }

    public static String getDataDir()
    {
        return Device.dataDir;
    }
    
    /**
     * Executes command with timeout in seconds (timeout only for netbook)
     * @param timeout -1 : no timeout
     * @return
     */
    public static String sysCommand(String command, int timeout)
    {
        if (isAndroidSystem())
            return runCommand(command) + "";

        String result = "";
        try
        {
            if (timeout != -1)
                command = "timeout " + timeout + " " + command;
            
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                result += line + "\n";
        } catch (IOException e1)
        {
            e1.printStackTrace();
            return result;
        } catch (InterruptedException e2)
        {
            e2.printStackTrace();
            return result;
        }

        System.out.println(command);
        //System.out.println("Result: " + result);
        return result;
    }

    /**
     * Runs the system command
     * 
     * @return For Android, returns 0 if successful. Otherwise, returns the
     *         console output
     */
    public static String sysCommand(String command)
    {
        return sysCommand(command, -1);
    }

    private static boolean isAndroid;
    private static boolean detectedOS = false;

    public static boolean isAndroidSystem()
    {
        if (!detectedOS)
        {
            isAndroid = new File("/sdcard").exists();
            detectedOS = true;
        }

        return isAndroid;
    }

    // cached name
    private static String wlanInterfaceName = null;

    public static String wlanInterfaceName()
    {
        if (!isAndroidSystem())
        {
            if (wlanInterfaceName == null)
            {
                String iwconfig = Device.sysCommand("iwconfig");
                for (String line : iwconfig.split("\n"))
                    if (line.contains("802.11"))
                    {
                        wlanInterfaceName = line.split("\\s+")[0];
                        break;
                    }
            }

            return wlanInterfaceName;
        } else
            return "tiwlan0";
    }
    
    public static String doAndroidHardStop()
    {
        String basePath;
        if (new File("/data/data/android.tether").exists())
            basePath = "/data/data/android.tether";
        else if (new File("/data/data/com.googlecode.android.wifi.tether").exists())
            basePath = "/data/data/com.googlecode.android.wifi.tether";
        else
        {
            System.err.println("Something is wrong - is the wifi tether installed?");
            return "fail";
        }
        
        return Device.sysCommand("su -c \""+basePath+"/bin/tether stop 1\"");
    }
}
