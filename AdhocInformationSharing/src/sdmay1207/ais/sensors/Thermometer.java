package sdmay1207.ais.sensors;

import sdmay1207.ais.sensors.SensorInterface.SensorType;

public class Thermometer extends Sensor
{
    public Thermometer()
    {
        super(SensorType.Thermometer);
    }
    
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
