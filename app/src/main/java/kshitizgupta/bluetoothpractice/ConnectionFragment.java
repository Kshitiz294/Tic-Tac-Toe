package kshitizgupta.bluetoothpractice;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ConnectionFragment extends Fragment {

    public static final int REQUEST_CODE_ENABLE= 10;
    public static final int REQUEST_CODE_DISCOVERY= 11;

    ListView listView;
    ArrayList<BluetoothDevice> list;
    BluetoothAdapter adapter;
    Button search,discovery;
    ArrayAdapter<String> arrayAdapter;

    TextView progress_textview;
    ProgressBar bar;

    Handler handler;
    Runnable runnable= new Runnable() {
        @Override
        public void run() {
            dialog.dismiss();
            Toast.makeText(getActivity(),"Connection Failed",Toast.LENGTH_SHORT).show();
        }
    };

    ProgressDialog dialog;

    CountDownTimer timer= new CountDownTimer(30000,1000) {
        @Override
        public void onTick(long l) {
            progress_textview.setText("" + (l/1000));
        }

        @Override
        public void onFinish() {
            bar.setVisibility(View.GONE);
            progress_textview.setVisibility(View.GONE);
            search.setEnabled(true);
            discovery.setEnabled(true);
            search.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            discovery.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        }
    };

    public interface ConnectionManager{
        void ClientConnection(BluetoothDevice bluetoothDevice);
        void ServerConnection();
    }

    private final BroadcastReceiver receiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                list.add(device);
                arrayAdapter.add(device.getName()+ " - "+device.getAddress());
            }
        }
    };



    public ConnectionFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(receiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.connection_layout,container,false);
        list= new ArrayList<>();

        adapter= BluetoothAdapter.getDefaultAdapter();

        progress_textview= (TextView)view.findViewById(R.id.progress_textview);
        bar= (ProgressBar)view.findViewById(R.id.progress_bar);

        if(adapter == null){
            Toast.makeText(getActivity(),"BlueTooth is not supported on your device",Toast.LENGTH_SHORT).show();
        }else{
            if(!adapter.isEnabled()){
                Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent,REQUEST_CODE_ENABLE);
            }
        }

        arrayAdapter= new ArrayAdapter<String>(getActivity(), R.layout.list_item,R.id.list_item_textView);

        listView= (ListView)view.findViewById(R.id.listView);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device= list.get(i);
                ((ConnectionManager)getActivity()).ClientConnection(device);
                dialog= ProgressDialog.show(getActivity(),"","Requesting Connection");

                handler= new Handler();
                handler.postDelayed(runnable,12000);
            }
        });


        search = (Button) view.findViewById(R.id.search);
        discovery= (Button) view.findViewById(R.id.discovery);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(View.VISIBLE);
                Set<BluetoothDevice> devices= adapter.getBondedDevices();

                if(devices.size() >0){
                    for (BluetoothDevice device:devices){
                        list.add(device);
                        arrayAdapter.add(device.getName()+ " - "+ device.getAddress());
                    }
                }
                adapter.startDiscovery();
            }
        });

        discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,30);
                startActivityForResult(intent,REQUEST_CODE_DISCOVERY);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_CODE_ENABLE){
            if(resultCode== getActivity().RESULT_OK){
                Toast.makeText(getActivity(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(),"Bluetooth not enabled",Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode==REQUEST_CODE_DISCOVERY){
            if(resultCode== 30){
                Toast.makeText(getActivity(),"Bluetooth Discovery enabled for 30 seconds",Toast.LENGTH_SHORT).show();
                bar.setVisibility(View.VISIBLE);
                bar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_ATOP);
                progress_textview.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                search.setEnabled(false);
                discovery.setEnabled(false);
                search.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                discovery.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                timer.start();
                ((ConnectionManager)getActivity()).ServerConnection();
            }else{
                Toast.makeText(getActivity(),"Bluetooth Discovery not enabled",Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cancelDiscovery();
        getActivity().unregisterReceiver(receiver);
        timer.cancel();
    }

}
