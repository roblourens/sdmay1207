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
     * Runs the system command
     * 
     * @return For Android, returns 0 if successful. Otherwise, returns the
     *         console output
     */
    public static String sysCommand(String command)
    {
        System.out.println(command);
        
        if (isAndroidSystem())
            return runCommand(command) + "";

        String result = "";
        try
        {
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

        System.out.println("Result: " + result);
        return result;
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
}
