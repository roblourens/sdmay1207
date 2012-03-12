package sdmay1207.ais.etc;

public abstract class Repeater implements Runnable
{
    // http://stackoverflow.com/questions/106591/do-you-ever-use-the-volatile-keyword-in-java
    volatile boolean keepRunning = true;

    @Override
    public void run()
    {
        while (keepRunning)
        {
            this.runOnce();
        }
    }
    
    public void start()
    {
        new Thread(this).start();
    }

    public void stop()
    {
        keepRunning = false;
    }

    protected void runOnce()
    {

    }
    
    public static abstract class TimedRepeater extends Repeater
    {
        private long interval;
        
        public TimedRepeater(long millis)
        {
            interval = millis;
        }
        
        @Override
        public void run()
        {
            while (keepRunning)
            {
                this.runOnce();
                Utils.sleep(interval);
            }
        }
    }
}