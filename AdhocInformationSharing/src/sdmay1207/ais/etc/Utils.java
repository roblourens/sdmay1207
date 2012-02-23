package sdmay1207.ais.etc;

import java.lang.reflect.Array;

import sdmay1207.ais.sensors.GPS.Location;

public class Utils
{
    public static String join(String delimiter, String... sArr)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sArr.length; i++)
        {
            builder.append(sArr[i]);

            if (i != sArr.length - 1)
                builder.append(delimiter);
        }
        return builder.toString();
    }

    public static int getNodeNumberFromIP(String ip)
    {
        return Integer.parseInt(ip.split("\\.")[3]);
    }

    // Arrays.copyOfRange is not implemented in Android
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayCopy(T[] original, int from, int to)
    {
        T[] newArr = (T[]) Array.newInstance(original[0].getClass(), to - from);

        for (int i = from; i < to; i++)
            newArr[i - from] = original[i];

        return newArr;
    }

    // Haversine formula in meters
    public static double distance(Location p1, Location p2)
    {
        double dlong = Math.toRadians(p1.longitude - p2.longitude);
        double dlat = Math.toRadians(p1.latitude - p2.latitude);
        double a = Math.pow(Math.sin(dlat / 2.0), 2)
                + Math.cos(Math.toRadians(p1.latitude))
                * Math.cos(Math.toRadians(p2.latitude))
                * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367 * c;

        return d*1000;
    }
    
    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}