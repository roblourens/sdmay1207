package sdmay1207.ais.etc;

import java.lang.reflect.Array;

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
}
