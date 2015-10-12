package com.haplicity.simplewhiteboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {

    public static final String PREF_NAME = "MyPrefs";
    public static final String LAST_COLOR = "PrevColor";
    public static final String sendColor = "com.haplicity.simplewhiteboard.COLOR";
    public static DrawView drawView;
    public static Socket mSocket;
    {
        try {
            //connect to my server
            mSocket = IO.socket("https://vast-plateau-9604.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }

    private Button mDrawButton, mEraseButton, mSettingsButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawView)findViewById(R.id.draw_view);

        //loads last used color from SharedPreferences
        preferences = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int lastColor = preferences.getInt(LAST_COLOR, 0);
        if (lastColor != 0) {
            drawView.setColor(lastColor);
        }

        mDrawButton = (Button)findViewById(R.id.draw_button);
        mDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop erasing
                drawView.setErasing(false);
            }
        });

        mEraseButton = (Button)findViewById(R.id.erase_button);
        mEraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start erasing
                drawView.setErasing(true);
            }
        });

        mSettingsButton = (Button)findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to SettingsActivity and pass current drawing color
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra(sendColor, drawView.getColor());
                startActivity(intent);
            }
        });

        //run drawView's onUpdate when we receive a 'drawing' event
        mSocket.on("drawing", drawView.onUpdate);
        //connect to the server
        mSocket.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //saves current color to SharedPreferences
        preferences.edit().putInt(LAST_COLOR, drawView.getColor()).apply();

        //disconnect from the server
        mSocket.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about_me) {
            Intent intent = new Intent(MainActivity.this, AboutMeActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}