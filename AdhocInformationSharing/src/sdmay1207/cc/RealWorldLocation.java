package sdmay1207.cc;

import sdmay1207.ais.etc.Utils;
import sdmay1207.ais.sensors.GPS.Location;

public class RealWorldLocation
{
    // If two Locations are within DELTA meters, consider them equal
    public static final double DELTA = 20;
    
    public Location loc;
    
    public RealWorldLocation(Location loc)
    {
        this.loc = loc;
    }
    
    public boolean withinDeltaOf(RealWorldLocation other)
    {
        return withinDeltaOf(other.loc);
    }

    public boolean withinDeltaOf(Location p)
    {
        return distanceTo(p) <= DELTA;
    }

    // in meters
    public double distanceTo(RealWorldLocation other)
    {
        return distanceTo(other.loc);
    }

    // in meters
    public double distanceTo(Location p)
    {
        return Utils.distance(this.loc, p);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RealWorldLocation))
            return false;

        RealWorldLocation rw = (RealWorldLocation) o;
        return loc.equals(rw.loc);
    }

    public String toString()
    {
        return loc.toString();
    }
}
