package sdmay1207.ais.sensors;

import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.sensors.SensorInterface.SensorType;

// Implemented on USB device and Android
public abstract class GPS extends Sensor
{
    public GPS()
    {
        super(SensorType.GPS);
    }

    @Override
    public abstract Location getReading();

    public class Location
    {
        public double latitude;

        public double longitude;
        
        @Override
        public int hashCode()
        {
            long hash = 0;
            hash += Double.doubleToLongBits(latitude)*37;
            hash += Double.doubleToLongBits(longitude)*37;
            
            // problem?
            return (int)hash;
        }
        
        public String toString()
        {
            return Utils.join(",", ""+latitude, ""+longitude);
        }
    }
}
