package kshitizgupta.bluetoothpractice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Button same_device=(Button)findViewById(R.id.same_screen);
        Button over_bluetooth=(Button)findViewById(R.id.over_bluetooth);

        same_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OptionsActivity.this,SinglePlayerActivity.class);
                startActivity(intent);
            }
        });

        over_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OptionsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
