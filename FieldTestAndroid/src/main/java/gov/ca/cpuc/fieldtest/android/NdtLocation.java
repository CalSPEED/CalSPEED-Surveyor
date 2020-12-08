/* Original work: Copyright 2009 Google Inc. All Rights Reserved.
 
   Modified work: The original source code (AndroidNdt.java) comes from the NDT Android app
                  that is available from http://code.google.com/p/ndt/.
                  It's modified for the CalSPEED Android app by California 
                  State University Monterey Bay (CSUMB) on April 29, 2013.

Copyright (c) 2020, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package gov.ca.cpuc.fieldtest.android;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import gov.ca.cpuc.fieldtest.android.TesterFragment.LatLong;
import gov.ca.cpuc.fieldtest.android.TesterFragment.NetworkLatLong;

/**
 * Handle the location related functions and listeners.
 */
class NdtLocation implements LocationListener {
    /**
     * Location variable, publicly accessible to provide access to geographic data.
     */
    private static final String BACKGROUND = "BACKGROUND";
    private static final String FOREGROUND = "FOREGROUND";
    Location location;
    private Location networkLocation;
    LocationManager locationManager;
    Boolean gpsEnabled;
    Boolean networkEnabled;
    Location NetworkLastKnownLocation;
    Location GPSLastKnownLocation;
    private Criteria criteria;
    private NetworkLatLong networkLatLong;
    private LatLong latLongptr;


    /**
     * Passes context to this class to initialize members.
     *
     * @param context context which is currently running
     */
    NdtLocation(Context context, NetworkLatLong networkLatLong, LatLong latLongptr) {
        this.networkLatLong = networkLatLong;
        this.latLongptr = latLongptr;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        location = new Location(LocationManager.GPS_PROVIDER);
        networkLocation = new Location(LocationManager.NETWORK_PROVIDER);
        networkLocation.setLatitude(0.0);
        networkLocation.setLongitude(0.0);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String bestProvider = locationManager.getBestProvider(criteria, true);
        Log.d("LocationManager", "Best provider is:" + bestProvider);
    }

    private void addGpsListener() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.addGpsStatusListener(onGpsStatusChange);
                locationManager.requestLocationUpdates(1000, 500, criteria, this, null);
                Log.v("LocationManager", "Adding GPS to location manager updates");
                gpsEnabled = true;
            } else {
                Log.d("LocationManager", "GPS location is not set to enabled");
                gpsEnabled = false;
            }
        } catch (SecurityException e) {
            Log.e("NdtLocation", "Failed to addGpsListener: " + e.getMessage());
            gpsEnabled = false;
        }
    }

    private void removeGPSStatusListener() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.removeGpsStatusListener(onGpsStatusChange);
            gpsEnabled = false;
        }
    }

    private void addNetworkListener() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500,
                        0, this);
                Log.v("LocationManager", "Adding network to location manager updates");
                networkEnabled = true;
            } else {
                Log.d("LocationManager", "Network location is not set to enabled");
                networkEnabled = false;
            }
        } catch (SecurityException e) {
            Log.e("NdtLocation", "Failed to addNetworkListener: " + e.getMessage());
            networkEnabled = false;
        }
    }

    private void removeNetworkListenerUpdates() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.removeUpdates(this);
            networkEnabled = false;
        }
    }

    private final GpsStatus.Listener onGpsStatusChange = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    latLongptr.updateLatitudeLongitude();
                    Log.v("GpsStatus.Listener", "GPS starting...\n");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.v("GpsStatus.Listener", "GPS first fix \n");
                    latLongptr.updateLatitudeLongitude();
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    location = null;
                    latLongptr.updateLatitudeLongitude();
                    Log.v("GpsStatus.Listener", "GPS stopped.\n");
                    break;
            }
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            networkLatLong.updateNetworkLatitudeLongitude(location);
            networkLocation.set(location);
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            latLongptr.setLatitudeLongitude(location.getLatitude(), location.getLongitude(), true);
            latLongptr.updateLatitudeLongitude();
            location.set(location);
        }
        Log.v("LocationChange", String.format("New %s location: %f, %f. Accuracy %f",
                location.getProvider(), location.getLatitude(), location.getLongitude(),
                location.getAccuracy()));
        this.location = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            removeGPSStatusListener();
            location = null;
            latLongptr.updateLatitudeLongitude();
        }
        if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            removeNetworkListenerUpdates();
            networkLocation = null;
            networkLatLong = null;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            addGpsListener();
        }
        if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            addNetworkListener();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("LocationProvider", "Status Changed: Out of Service");
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    removeGPSStatusListener();
                    location = null;
                    latLongptr.updateLatitudeLongitude();
                }
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    removeNetworkListenerUpdates();
                    networkLocation = null;
                    networkLatLong = null;
                }
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("LocationProvider", "Status Changed: Temporarily Unavailable");
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    removeGPSStatusListener();
                    location = null;
                    latLongptr.updateLatitudeLongitude();
                }
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    removeNetworkListenerUpdates();
                    networkLocation = null;
                    networkLatLong = null;
                }
                break;
            case LocationProvider.AVAILABLE:
                Log.v("LocationProvider", "Status Changed: Available");
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    addGpsListener();
                }
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    addNetworkListener();
                }
                break;
        }
    }

    /**
     * Stops requesting the location update.
     */
    void stopListen() {
        removeGPSStatusListener();
        removeNetworkListenerUpdates();
    }

    /**
     * Begins to request the location update.
     */
    void startListen() {
        addNetworkListener();
        addGpsListener();
    }

    /**
     * Change network listening
     */
    void listenInForeground() {
        changeListener(FOREGROUND);
    }

    void listenInBackground() {
        changeListener(BACKGROUND);
    }

    private void changeListener(String type) {
        final int GPS_FOREGROUND = 1000;
        final int GPS_BACKGROUND = 20000;
        final int NETWORK_FOREGROUND = 2000;
        final int NETWORK_BACKGROUND = 15000;
        final int PASSIVE = 10000;
        try {
            if (locationManager != null) {
                if ((locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) &&
                        (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                    Log.d("LocationManager", "Removing location updates");
                    locationManager.removeUpdates(this);
                    switch (type) {
                        case FOREGROUND:
                            Log.v("LocationManager", String.format("Changing GPS to " +
                                    "foreground updating every %d seconds", GPS_FOREGROUND));
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    NETWORK_FOREGROUND, 0, this);
                            Log.v("LocationManager", String.format("Changing network to " +
                                    "foreground updating every %d seconds", NETWORK_FOREGROUND));
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                    GPS_FOREGROUND, 0, this);
                            Log.d("LocationManager", "Changing to location manager to foreground updates");
                            networkEnabled = true;
                            break;
                        case BACKGROUND:
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    NETWORK_BACKGROUND, 0, this);
                            Log.v("LocationManager", String.format("Changing GPS to " +
                                    "background updating every %d seconds", GPS_BACKGROUND));
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                    GPS_BACKGROUND, 0, this);
                            Log.v("LocationManager", String.format("Changing network to " +
                                    "background updating every %d seconds", NETWORK_BACKGROUND));
                            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                                    PASSIVE, 0, this);
                            Log.v("LocationManager", String.format("Changing passive to " +
                                    "background updating every %d seconds", PASSIVE));
                            Log.d("LocationManager", "Changing to location manager to background updates");
                            networkEnabled = true;
                            break;
                        default:
                            Log.i("LocationManager", "Invalid option");
                            break;
                    }
                } else {
                    networkEnabled = false;
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i("LocationProvider", "GPS provider not enabled");
                    }
                    if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        Log.i("LocationProvider", "Network provider not enabled");
                    }
                }
            } else {
                Log.w("LocationManager", "Location Manager is null");
                networkEnabled = false;
            }
        } catch (SecurityException e) {
            Log.e("NdtLocation", "Failed to addNetworkListener: " + e.getMessage());
            networkEnabled = false;
        }
    }
}
