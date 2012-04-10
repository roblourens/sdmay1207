package sdmay1207.ais.etc;

import java.lang.reflect.Array;
import java.util.Map;

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

        return d * 1000;
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

    // Return key for given value
    public static Object reverseMapLookup(
            Map<? extends Object, ? extends Object> map, Object value)
    {
        for (Object k : map.keySet())
            if (map.get(k).equals(value))
                return k;

        return null;
    }

    /**
     * Turns array of bytes into string. From
     * http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html
     * 
     * @param buf
     *            Array of bytes to convert to hex string
     * @return Generated hex string
     */
    public static String bytesToHexStr(byte buf[])
    {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;

        for (i = 0; i < buf.length; i++)
        {
            if (((int) buf[i] & 0xff) < 0x10)
                strbuf.append("0");

            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }

        return strbuf.toString();
    }
    
    public static byte[] hexStrToBytes(String hexStr)
    {
        byte[] bytes = new byte[hexStr.length()/2];
        
        for (int i=0; i<hexStr.length(); i+=2)
        {
            String byteStr = hexStr.substring(i, i+2);
            byte theByte = (byte) Integer.parseInt(byteStr, 16);
            bytes[i/2] = theByte;
        }
        
        return bytes;
    }
}