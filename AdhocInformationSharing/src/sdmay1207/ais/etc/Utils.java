package sdmay1207.ais.etc;

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
}
