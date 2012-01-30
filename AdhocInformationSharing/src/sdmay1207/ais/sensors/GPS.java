package sdmay1207.ais.sensors;

// Implemented on USB device and Android
public abstract class GPS extends Sensor
{
    @Override
    public abstract GPSReading getReading();

    public class GPSReading
    {
        public float latitude;

        public float longitude;
    }
}
