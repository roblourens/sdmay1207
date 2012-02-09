package android.TextMessenger.view;

// runCommand must be called from something in this package, unless we
// modify/recompile
public class Connect
{
    public static native int runCommand(String command);

    static
    {
        System.loadLibrary("adhocsetup");
    }
}