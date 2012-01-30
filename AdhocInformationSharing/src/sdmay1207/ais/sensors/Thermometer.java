package sdmay1207.ais.sensors;

public class Thermometer extends Sensor
{
    @Override
    public Object getReading()
    {
        // do magic here
        return 15;
    }

    @Override
    public String getUnits()
    {
        return "F";
    }
}
