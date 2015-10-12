package com.haplicity.simplewhiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

/**
 * Created by Haplicity on 9/27/2015.
 */
public class DrawView extends View {

    //drawing path
    private Path drawPath;

    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;

    //initial canvasColor
    private int paintColor = 0xFF660000;

    //the canvas
    private Canvas drawCanvas;

    //canvas bitmap
    private Bitmap canvasBitmap;

    //are we erasing?
    private boolean erasing = false;

    //JSON data to send to server
    private Coordinates coordinates = new Coordinates();

    public DrawView(Context context, AttributeSet attribset) {
        super(context, attribset);
        setUp();
    }

    //initialize variables and default settings
    private void setUp() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(30);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setErasing(boolean onErase) {
        erasing = onErase;
    }

    //updates the canvas with input from other users
    public Emitter.Listener onUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //get the JSON object from the data
            JSONObject data = (JSONObject) args[0];
            JSONArray array;
            try {
                //get the array from the data
                array = data.getJSONArray("path");

                //iterate through the array and draw the saved path
                for (int i = 0; i < array.length(); i++) {

                    JSONObject index = array.getJSONObject(i);

                    if (i == 0) {
                        //if they were erasing, use the eraser settings
                        if (index.getBoolean("erasing")) {
                            drawPaint.setColor(Color.WHITE);
                            drawPaint.setStrokeWidth(60);
                        } else {
                            //gets the color they used
                            drawPaint.setColor(index.getInt("color"));
                            drawPaint.setStrokeWidth(30);
                        }

                        //start drawing from their starting point
                        drawPath.moveTo(index.getInt("x"), index.getInt("y"));
                    } else {
                        //draw path using their coordinates
                        drawPath.lineTo(index.getInt("x"), index.getInt("y"));
                    }
                }

                //draw path onto canvas
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                postInvalidate();

            } catch (JSONException e) {
                return;
            }
        }
    };

    //clears the canvas
    public void clearCanvas() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    //sets canvas color
    public void setColor(String color) {
        invalidate();
        paintColor = Color.parseColor(color);
        drawPaint.setColor(paintColor);
    }

    //sets the canvas color
    public void setColor(int color) {
        invalidate();
        paintColor = color;
        drawPaint.setColor(paintColor);
    }

    public int getColor() {
        return paintColor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        //if we are erasing, use erasing settings
        if (erasing) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.setStrokeWidth(60);
        } else {
            drawPaint.setColor(paintColor);
            drawPaint.setStrokeWidth(30);
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //start drawing the path, starting from where they pressed
                drawPath.moveTo(touchX, touchY);

                //creates JSON object to store the path in, and sets its parameters
                JSONObject obj1 = new JSONObject();
                try {
                    obj1.put("x", touchX);
                    obj1.put("y", touchY);
                    obj1.put("color", paintColor);
                    obj1.put("erasing", erasing);
                } catch (JSONException e) { }

                //put the JSON object into the array to be sent to the server
                coordinates.addItem(obj1);
                break;
            case MotionEvent.ACTION_MOVE:
                //draw the path as the user moves
                drawPath.lineTo(touchX, touchY);

                //store the path data in a JSON object and push it to the array
                JSONObject obj2 = new JSONObject();
                try {
                    obj2.put("x", touchX);
                    obj2.put("y", touchY);
                    obj2.put("color", paintColor);
                } catch (JSONException e) {}

                coordinates.addItem(obj2);
                break;
            case MotionEvent.ACTION_UP:
                //draw the path onto the canvas
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();

                //send the path data to the other users
                MainActivity.mSocket.emit("draw", coordinates.getCoord());

                //reset the array
                coordinates.clearCoord();
                break;
            default:
                return false;
        }

        invalidate();

        return true;
    }

    @Override
    protected void onSizeChanged(int width, int height, int prevWidth, int prevHeight) {
        super.onSizeChanged(width, height, prevWidth, prevHeight);

        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }
}