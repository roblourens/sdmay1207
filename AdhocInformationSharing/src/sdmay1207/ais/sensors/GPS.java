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

    public static class Location
    {
        public double latitude;

        public double longitude;
        
        /**
         * Instantiates a Location from its toString output
         */
        public Location(String locString)
        {
            String[] coords = locString.split(",");
            latitude = Double.parseDouble(coords[0]);
            longitude = Double.parseDouble(coords[1]);
        }
        
        public Location(double latitude, double longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        // in meters
        public double distanceTo(Location p)
        {
            return Utils.distance(this, p);
        }

        @Override
        public int hashCode()
        {
            long hash = 0;
            hash += Double.doubleToLongBits(latitude) * 37;
            hash += Double.doubleToLongBits(longitude) * 37;

            // problem?
            return (int) hash;
        }

        // This is the data sent in the Heartbeat
        public String toString()
        {
            return String.format("%f,%f", latitude, longitude);
            //return String.format("(%f, %f)", latitude, longitude);
        }

        public boolean equals(Object o)
        {
            if (!(o instanceof Location))
                return false;

            Location p = (Location) o;
            return (latitude == p.latitude && longitude == p.longitude);
        }
    }
}
