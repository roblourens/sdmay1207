package sdmay1207.ais.sensors;

import sdmay1207.ais.sensors.SensorInterface.SensorType;

public abstract class Sensor
{
    private SensorType type;

    // or something
    public abstract Object getReading();

    public abstract String getUnits();

    public Sensor(SensorType type)
    {
        this.type = type;
    }

    public SensorType getType()
    {
        return type;
    }
}
