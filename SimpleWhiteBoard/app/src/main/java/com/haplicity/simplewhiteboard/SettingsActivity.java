package com.haplicity.simplewhiteboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    private int drawingColor;
    private final Context context = this;
    private DrawView drawView;
    private ImageButton currentColor;
    private Button mSaveButton, mBackButton, mClearButton;

    //grabs the color from the pressed button and passes it to drawView
    public void changeColor(View view) {
        if(view != currentColor) {
            ImageButton imageView = (ImageButton)view;
            String color = view.getTag().toString();

            MainActivity.drawView.setColor(color);

            imageView.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.paint_selected));
            currentColor.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.paint));
            currentColor = (ImageButton)view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        drawingColor = getIntent().getIntExtra(MainActivity.sendColor, 0);
        drawView = MainActivity.drawView;

        LinearLayout colorLayout1 = (LinearLayout)findViewById(R.id.colors_layout_1);
        LinearLayout colorLayout2 = (LinearLayout)findViewById(R.id.colors_layout_2);

        //iterate through the top row of the color palette to find the current color; if the colors match, then select that color
        for (int i = 0; i < colorLayout1.getChildCount(); i++) {
            if (Color.parseColor(colorLayout1.getChildAt(i).getTag().toString()) == drawingColor) {
                currentColor = (ImageButton) colorLayout1.getChildAt(i);
                currentColor.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.paint_selected));
            }
        }

        //iterate through the bottom row of the color palette to find the current color; if the colros match, then select that color
        for (int i = 0; i < colorLayout2.getChildCount(); i++) {
            if (Color.parseColor(colorLayout2.getChildAt(i).getTag().toString()) == drawingColor) {
                currentColor = (ImageButton) colorLayout2.getChildAt(i);
                currentColor.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.paint_selected));
            }
        }

        //tells drawView to clear the canvas
        mClearButton = (Button)findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder newAlert = new AlertDialog.Builder(context);
                newAlert.setTitle("New Canvas");
                newAlert.setMessage("Are you sure you want to clear the screen?");
                newAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        drawView.clearCanvas();
                        dialog.dismiss();
                    }
                });
                newAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                newAlert.show();
            }
        });

        //saves current canvas to the Gallery
        mSaveButton = (Button)findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder newAlert = new AlertDialog.Builder(context);
                newAlert.setTitle("Save Canvas");
                newAlert.setMessage("Do you want to save the drawing to your Gallery?");
                newAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        drawView.setDrawingCacheEnabled(true);
                        String savedImage = MediaStore.Images.Media.insertImage(getContentResolver(), drawView.getDrawingCache(), UUID.randomUUID().toString()+".png","drawing");
                        if (savedImage != null) {
                            Toast newToast = Toast.makeText(context, "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                            newToast.show();
                        } else {
                            Toast newToast = Toast.makeText(context, "Error! image could not be saved.", Toast.LENGTH_SHORT);
                            newToast.show();
                        }

                        drawView.destroyDrawingCache();
                        dialog.dismiss();
                    }
                });
                newAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                newAlert.show();
            }
        });

        //ends this activity, returning to the MainActivity
        mBackButton = (Button)findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
            Intent intent = new Intent(SettingsActivity.this, AboutMeActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
