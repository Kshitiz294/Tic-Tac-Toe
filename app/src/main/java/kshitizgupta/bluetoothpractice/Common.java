package kshitizgupta.bluetoothpractice;


import android.app.Application;
import java.util.UUID;

public class Common extends Application{
    public static final String UID= "12345";
    public static UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static boolean IS_SERVER;
    public static boolean IS_CLIENT;
    public static boolean IS_CONNECTED;

    @Override
    public void onCreate() {
        super.onCreate();
        IS_CLIENT=false;
        IS_SERVER=false;
        IS_CONNECTED=false;
    }

}
