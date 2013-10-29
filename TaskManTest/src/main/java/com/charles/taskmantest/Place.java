package com.charles.taskmantest;

import com.charles.taskmantest.geofence.SimpleGeoFence;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by charles on 10/8/13.
 */
public class Place implements Serializable {
    @SerializedName("name")
    private String name = "";
    @SerializedName("wifi")
    private HashMap<String, Object> wifi;
    @SerializedName("bluetooth")
    private String bluetooth = "";
    @SerializedName("sms")
    private HashMap<String, Object> sms;
    @SerializedName("fence")
    private HashMap<String, Object> fence;
    @SerializedName("mute")
    private String mute = "";
    @SerializedName("airplane")
    private String airplane = "";
    @SerializedName("vibrate")
    private String vibrate = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
    Manage storing and retrieving geofences
     */
    public void setGeofence (String id, double lat, double lon, double radius, long expir, int trans) {
        fence = new HashMap<String, Object>();
        fence.put("id", id);
        fence.put("lat", new Double(lat));
        fence.put("lon", new Double(lon));
        fence.put("radius", new Float(radius));
        fence.put("expir", new Long(expir));
        fence.put("trans", new Integer(trans));
    }

    public SimpleGeoFence getGeoFence() {
        if (fence != null) {
            String id = (String)fence.get("id");
            double lat = ((Double)fence.get("lat"));
            double lon = ((Double)fence.get("lon"));
            float radius = ((Float)fence.get("radius"));
            long expir = ((Long)fence.get("expir"));
            int trans = ((Integer)fence.get("trans"));
            if (
                    lat != INVALID_FLOAT_VALUE &&
                    lon != INVALID_FLOAT_VALUE &&
                    radius != INVALID_FLOAT_VALUE &&
                    expir != INVALID_INT_VALUE &&
                    trans != INVALID_LONG_VALUE
                    ) {
                return new SimpleGeoFence (id, lat, lon, radius, expir, trans);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    /*
         * Invalid values, used to test geofence storage when
         * retrieving geofences
         */
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;

}
