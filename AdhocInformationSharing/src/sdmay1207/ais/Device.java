package sdmay1207.ais;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Maybe there could be a config file for each device with the things here and
// available sensors, etc.
public class Device
{
    // Android-only
    // What happens if the library isn't loaded?
    public static native int runCommand(String command);

    static
    {
        if (isAndroidSystem())
            System.loadLibrary("adhocsetup");
    }

    public static String sysCommand(String command)
    {
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
            return result;
        } catch (InterruptedException e2)
        {
            return result;
        }

        return result;
    }

    public static boolean isAndroidSystem()
    {
        // detect this somehow
        return false;
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
                        return line.split("\\s+")[0];
            }

            return wlanInterfaceName;
        } else
            return "tiwlan"; // ?
    }
}
