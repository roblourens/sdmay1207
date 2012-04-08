package sdmay1207.ais.sensors;

import sdmay1207.ais.Device;
import sdmay1207.ais.etc.Repeater.TimedRepeater;

public class NetbookGPS extends GPS
{
    private Location lastLocation = new Location(0, 0);

    // see note on runOnce
    private Object lastLocationLock = new Object();

    public NetbookGPS()
    {
        new GPSRepeater().start();
    }

    @Override
    public Location getReading()
    {
        synchronized (lastLocationLock)
        {
            return lastLocation;
        }
    }

    @Override
    public String getUnits()
    {
        return "";
    }

    private class GPSRepeater extends TimedRepeater
    {
        public GPSRepeater()
        {
            super(2000);
        }

        @Override
        protected void runOnce()
        {
            String nmeaData = Device.sysCommand("cat /dev/ttyUSB0", 2);

            for (String s : nmeaData.split("\n"))
            {
                if (s.startsWith("$GPGGA"))
                {
                    String[] args = s.split(",");
                    if (args.length < 6)
                    {
                        System.out.println("Got invalid GPGGA line: " + s);
                        return;
                    }

                    String latStr = args[2];
                    String latHemisphere = args[3];
                    String lonStr = args[4];
                    String lonHemisphere = args[5];

                    // match Android GPS convention instead of hemisphere
                    // letters, and the USB stick returns 100ths of degrees for
                    // some reason (4201.4634 N etc)
                    double lat = 0, lon = 0;
                    try
                    {
                        lat = degreesFromGPSString(latStr)
                                * (latHemisphere.equals("S") ? -1 : 1);
                        lon = degreesFromGPSString(lonStr)
                                * (lonHemisphere.equals("W") ? -1 : 1);
                    } catch (NumberFormatException nfe)
                    {
                        System.err
                                .println("Bad lat or lon format from USB GPS");
                        nfe.printStackTrace();
                    }

                    // Maybe can't use lastLocation as the lock object since we
                    // put it towards a new Object in the synchronized block?
                    // not sure whether that would actually be a problem or not
                    synchronized (lastLocationLock)
                    {
                        lastLocation = new Location(lat, lon);
                    }

                    return;
                }
            }

            System.out.println("No GPGGA in data from GPS");
        }

        private double degreesFromGPSString(String str)
        {
            int decimalPos = str.indexOf('.');
            double latDegrees = Double.parseDouble(str.substring(0, decimalPos-2));
            double latMinutes = Double.parseDouble(str.substring(decimalPos-2));
            return latDegrees + latMinutes / 60;
        }
    }
}
