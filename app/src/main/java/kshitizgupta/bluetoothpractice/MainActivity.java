package kshitizgupta.bluetoothpractice;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements ConnectionFragment.ConnectionManager,PlayFragment.MessageInterface{

    AcceptThread acceptThread;
    ConnectThread connectThread;
    ConnectedThread connectedThread;

    String PLAY_TAG= "PFTAG";
    String CONNECT_TAG= "CFTAG";

    public static final int MESSAGE_READ=0;

    Handler client_handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Common.IS_CONNECTED=true;
            Common.IS_CLIENT=true;
            Common.IS_SERVER=false;
            Toast.makeText(MainActivity.this,"Connection Established",Toast.LENGTH_SHORT).show();
            ConnectionFragment fragment= (ConnectionFragment) getFragmentManager().findFragmentByTag(CONNECT_TAG);
            if(fragment!=null){
                fragment.dialog.dismiss();
                fragment.handler.removeCallbacks(fragment.runnable);
            }
            getFragmentManager().beginTransaction().replace(R.id.frame_layout,new PlayFragment(),PLAY_TAG).commit();
            connectedThread= new ConnectedThread(connectThread.mmSocket,connection_handler);
            connectedThread.start();
        }

    };

    Handler server_handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Common.IS_CONNECTED=true;
            Common.IS_CLIENT=false;
            Common.IS_SERVER=true;
            Toast.makeText(MainActivity.this,"Connection Established",Toast.LENGTH_SHORT).show();
            getFragmentManager().beginTransaction().replace(R.id.frame_layout,new PlayFragment(),PLAY_TAG).commit();
            connectedThread= new ConnectedThread(acceptThread.socket,connection_handler);
            connectedThread.start();
        }
    };

    Handler connection_handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {

            String string="EMPTY";

            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readbuff= (byte[])msg.obj;
                    string= new String(readbuff,0,msg.arg1);
            }
            if(string.equals("XXX")){
                final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false)
                        .setMessage("Opponent left Channel")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.this.finish();
                            }
                        });
                AlertDialog dialog= builder.create();
                dialog.show();
            }else {
                PlayFragment fragment= (PlayFragment) getFragmentManager().findFragmentByTag(PLAY_TAG);
                if(fragment != null){
                    fragment.respondToMessage(string);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        PlayFragment fragment= (PlayFragment) getFragmentManager().findFragmentByTag(PLAY_TAG);
        if(fragment != null){
            final AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setMessage("Are you sure you want to leave?")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finish();
                            sendMessage("XXX");
                        }
                    });
            AlertDialog dialog= builder.create();
            dialog.show();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Common.IS_CLIENT=false;
        Common.IS_SERVER=false;
        Common.IS_CONNECTED=false;

        getFragmentManager().beginTransaction().replace(R.id.frame_layout,new ConnectionFragment(),CONNECT_TAG).commit();

    }


    @Override
    public void ClientConnection(BluetoothDevice bluetoothDevice) {
        connectThread=new ConnectThread(bluetoothDevice,client_handler);
        connectThread.start();
    }

    @Override
    public void ServerConnection() {
        acceptThread=new AcceptThread(server_handler);
        acceptThread.start();
    }


    @Override
    public void sendMessage(String string) {
        connectedThread.write(string.getBytes(Charset.forName("UTF-8")));
    }

    public void clearThreads(){
        if(connectThread != null){
            connectThread.cancel();
        }
        if(acceptThread != null){
            acceptThread.cancel();
        }
        if(connectedThread != null){
            connectedThread.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearThreads();
        Common.IS_CLIENT=false;
        Common.IS_SERVER=false;
    }
}
