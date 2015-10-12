package com.haplicity.simplewhiteboard;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Haplicity on 10/9/2015.
 */
public class Coordinates {

    //JSON data to send to server
    private JSONArray coordinates;

    //constructor
    public Coordinates() {
        coordinates = new JSONArray();
    }

    //adds an item to the array
    public void addItem(JSONObject obj) {
        coordinates.put(obj);
    }

    //clears the array
    public void clearCoord() {
        coordinates = new JSONArray();
    }

    public JSONArray getCoord() {
        return coordinates;
    }
}
