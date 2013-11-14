package com.charles.taskmantest.geofence;

import com.google.android.gms.location.Geofence;

/**
 * Created by charles on 10/9/13.
 */
public class SimpleGeoFence {
    // Instance variables
            private final String mId;
            private final double mLatitude;
            private final double mLongitude;
            private final double mRadius;
            private long mExpirationDuration;
            private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude Latitude of the Geofence's center.
         * @param longitude Longitude of the Geofence's center.
         * @param radius Radius of the geofence circle.
         * @param expiration Geofence expiration duration
         * @param transition Type of Geofence transition.
         */
        public SimpleGeoFence(
                String geofenceId,
                double latitude,
                double longitude,
                double radius,
                long expiration,
                int transition) {
            // Set the instance fields from the constructor
            this.mId = geofenceId;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            this.mExpirationDuration = expiration;
            this.mTransitionType = transition;
        }
        // Instance field getters
        public String getId() {
            return mId;
        }
        public double getLatitude() {
            return mLatitude;
        }
        public double getLongitude() {
            return mLongitude;
        }
        public double getRadius() {
            return mRadius;
        }
        public long getExpirationDuration() {
            return mExpirationDuration;
        }
        public int getTransitionType() {
            return mTransitionType;
        }
        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence() {
            // Build a new Geofence object
            float rad = (float)getRadius();
            return new Geofence.Builder()
                    .setRequestId(getId())
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(getLatitude(), getLongitude(), rad)
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        }
}
