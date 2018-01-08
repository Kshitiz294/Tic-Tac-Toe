package kshitizgupta.bluetoothpractice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    public BluetoothSocket socket;
    public static String NAME= "kshitizgupta.bluetoothpractice";

    public final Handler mHandler;
    public AcceptThread(Handler handler) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        BluetoothAdapter adapter= BluetoothAdapter.getDefaultAdapter();
        mHandler=handler;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, Common.MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
//                manageConnectedSocket(socket);
                mHandler.sendEmptyMessage(0);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    break;
                }
                break;
            }

        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}