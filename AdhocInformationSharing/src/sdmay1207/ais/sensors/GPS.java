package sdmay1207.ais.sensors;

// Implemented on USB device and Android
public abstract class GPS extends Sensor
{
    @Override
    public abstract Location getReading();

    public class Location
    {
        public float latitude;

        public float longitude;

        @Override
        public int hashCode()
        {
            int hash = 0;
            hash += Float.floatToIntBits(latitude)*37;
            hash += Float.floatToIntBits(longitude)*37;
            return hash;
        }
    }
}
