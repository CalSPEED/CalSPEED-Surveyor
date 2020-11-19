/* Original work: Copyright 2009 Google Inc. All Rights Reserved.
 
   Modified work: The original source code (AndroidNdt.java) comes from the NDT Android app
                  that is available from http://code.google.com/p/ndt/.
                  It's modified for the CalSPEED Android app by California 
                  State University Monterey Bay (CSUMB) on April 29, 2013.
*/

package gov.ca.cpuc.fieldtest.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static gov.ca.cpuc.fieldtest.android.BuildConfig.VERSION_NAME;


/**
 * UI Thread and Entry Point of mobile client.
 */
public class TesterFragment extends SherlockFragment {
    private String rawData;
    String postTestText;
    private Button buttonStandardTest;
    private ProgressBar progressBar;
    private TextView topText;
    private TextView textViewMain;
    private PowerManager.WakeLock wakeLock;
    private int networkType;
    private NetworkInfo networkInfo;
    private AndroidUiServices uiServices;
    private NdtLocation ndtLocation;
    private AssetManager assetManager;
    private String applicationFilesDir;
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;
    private boolean isAborted;
    private String mobileInfo;
    private String telephoneInfo;
    private String serverInfo;
    private String signalStrengthPower;
    private Date date;
    private boolean hasLatLong;
    private Double startLatitude;
    private Double startLongitude;
    private String longitude;
    private String latitude;
    private Context context;
    private String deviceId;
    Boolean usingUploadButton = false;
    Boolean validLocation = false;
    private LatLong myLatLong;
    private NetworkLatLong networkLatLong;
    private ProgressBar gpsDialog;
    private String Provider;
    private String TCPPort;
    private String UDPPort;
    private String locationCode = null;
    private ArrayList<ResultsItem> testResults;
    private ResultsViewer resultsViewer;
    private TextView latView;
    private TextView networkView;
    private TextView technologyView;
    private TextView longView;
    private LinearLayout testResultView;
    private ImageView testIcon;
    private ImageView uploadedIcon;
    private TextView locationCodeView;
    private ListView list;
    private Float smoothUpload;
    private Timer UploadTimer;
    private Timer DownloadTimer;
    private TimerTask uploadTask;
    private TimerTask downloadTask;
    private HistoryDatabaseHandler db;
    private NetworkAndDeviceInformation ndi;
    private StandardTest currentTest;
    private Thread testThread;
    private Process currentProcess;

    public TesterFragment() {
    }


    /**
     * Initializes the activity.
     */
    @Override
    public void onStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
            waitForPermissions();
        }
        if (context == null) { // check to see if still active fragment
            setupAll();
        }
        initViews();
        db = new HistoryDatabaseHandler(getActivity());
        ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        super.onStart();
    }

    private void waitForPermissions() {
        boolean granted = false;
        while (!granted) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            if (getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            granted = true;
        }
    }

    private void requestPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        Log.d("Permissions", "Requesting GPS permission");
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        Log.d("Permissions", "Requesting phone state permission");
        if (getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        Log.d("Permissions", "Requesting write permission");
        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        Log.d("Permissions", "Requesting: " + Arrays.toString(permissions.toArray()));
        if (!permissions.isEmpty()) {
            this.getActivity().requestPermissions(
                    permissions.toArray(new String[permissions.size()]), 1);
        }
    }

    public void setupAll() {
        SharedPreferences legal = getActivity().getSharedPreferences("Legal", Context.MODE_PRIVATE);
        if (!(legal.getBoolean("privacyPolicyAccepted", false))) {
            createPrivacyPolicyAlert();
        } else {
            Log.d("Legal", "Privacy policy accepted: " +
                    String.valueOf(legal.getBoolean("privacyPolicyAccepted", false)));
        }
        try {
            String packageName = getActivity().getApplicationContext().getPackageName();
            context = getActivity().createPackageContext(packageName, 0);
            Prefs.resetGPSoverride(context);
        } catch (Exception e) {
            if (Constants.DEBUG)
                Log.v("debug", "unable to set context OnCreate");
        }
        Thread gpsCollector = new Thread(new GPSCollector());
        gpsCollector.start();

        int serverNumber = Constants.FIELDTEST_DEVELOPMENT;
        String serverName = Constants.FIELDTEST_SERVER_NAMES[serverNumber];

        // Initialize the managers
        PowerManager powerManager =
                (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Network Testing");
        this.connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        this.telephonyManager = (TelephonyManager) getActivity()
                .getSystemService(Context.TELEPHONY_SERVICE);
        this.connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        UiHandler uiHandler = new UiHandler(Looper.myLooper());
        uiServices = new AndroidUiServices(getActivity(), uiHandler);

        assetManager = getActivity().getAssets();
        applicationFilesDir = getApplicationFilesDir();
        setupIperf();
        isAborted = false;
        hasLatLong = false;

        textViewMain = getActivity().findViewById(R.id.TextViewMain);
        textViewMain.setMovementMethod(ScrollingMovementMethod.getInstance());
        textViewMain.setClickable(false);
        textViewMain.setLongClickable(false);
        String header = getString(R.string.official) + VERSION_NAME;
        textViewMain.append(header);
        textViewMain.append("\n");
        uiServices.appendString(String.format("%s\n", header), UiServices.STAT_VIEW);
        textViewMain.append(String.format("\n%s\n",
                getString(R.string.default_server_indicator, serverName)));
        date = new Date();
        textViewMain.append(String.format("%s\n", date.toString()));

        testResults = new ArrayList<>();
        resultsViewer = new ResultsViewer(context, testResults);
        list = getActivity().findViewById(R.id.resultsListView);
        list.setAdapter(resultsViewer);
        Log.d("ListView", "setting visibility to visible");
        list.setVisibility(View.VISIBLE);
        list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        rawData = "";
        postTestText = "";
        setupUploadTimer();
        setupDownloadTimer();
        getLastKnownLocationInfo();
        this.startLongitude = 0.0;
        this.startLatitude = 0.0;
        setStartLocation();
    }

    public float getCpuTemp() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = reader.readLine();
            float temp = Float.parseFloat(line) / 10.0f;
            Log.d("CPU TEMP", String.valueOf(temp));
            return temp;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    private void setStartLocation() {
        Location lastKnownLocation = null;
        if (ndtLocation.GPSLastKnownLocation != null
                && ndtLocation.NetworkLastKnownLocation != null) {
            if (ndtLocation.GPSLastKnownLocation.getTime()
                    > ndtLocation.GPSLastKnownLocation.getTime()) {
                lastKnownLocation = ndtLocation.GPSLastKnownLocation;
            } else {
                lastKnownLocation = ndtLocation.NetworkLastKnownLocation;
            }
        } else if (ndtLocation.GPSLastKnownLocation != null) {
            lastKnownLocation = ndtLocation.GPSLastKnownLocation;
        } else if (ndtLocation.NetworkLastKnownLocation != null) {
            lastKnownLocation = ndtLocation.NetworkLastKnownLocation;
        }
        if (lastKnownLocation != null) {
            Log.v("Date", String.format("Date length: %d, lastKnownLocation date length: %d",
                    Long.toString(date.getTime()).length(),
                    Long.toString(lastKnownLocation.getTime()).length()));
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss");
            String dateText = df.format(new Date(lastKnownLocation.getTime()));
            Log.v("Date", "Last known date: " + dateText);
            Log.v("Date", "Current date: " + df.format(date));

            long timeInterval = date.getTime() - lastKnownLocation.getTime();
            if (timeInterval > 600000) {
                Log.v("LastKnownLocation", "Time differential is: "
                        + Long.toString(timeInterval));
            } else {
                if (timeInterval > 180000) {
                    Log.v("LastKnownLocation", "Time differential is: "
                            + Long.toString(timeInterval));
                    Log.v("LastKnownLocation", String.format("Last LatLong: %f, %f",
                            lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                }
                if (myLatLong.location != null) {
                    if (lastKnownLocation.getTime() > myLatLong.location.getTime()) {
                        this.startLatitude = lastKnownLocation.getLatitude();
                        this.startLongitude = lastKnownLocation.getLongitude();
                    } else {
                        Log.v("LastKnownLocation", String.format("Current LatLong: %f, %f",
                                myLatLong.latitude, myLatLong.longitude));
                        this.startLatitude = myLatLong.latitude;
                        this.startLongitude = myLatLong.longitude;
                    }
                } else {
                    Log.d("LastKnownLocation", "Current location is empty");
                    this.startLatitude = lastKnownLocation.getLatitude();
                    this.startLongitude = lastKnownLocation.getLongitude();
                }
            }
        } else {
            Log.d("LastKnownLocation", "Last location is empty");
        }

        if (this.startLongitude != 0.0 && this.startLatitude != 0.0) {
            Log.d("LastKnownLocation", "We have a last known location");
            Log.d("LastKnownLocation",
                    String.format("%f, %f", this.startLatitude, this.startLongitude));
            uiServices.updateLatLong(this.startLatitude, this.startLongitude);
            myLatLong.setLatitudeLongitude(this.startLatitude, this.startLongitude, true);
            this.latitude = Double.toString(this.startLatitude);
            this.longitude = Double.toString(this.startLongitude);
        } else {
            this.longitude = "0.0";
            this.latitude = "0.0";
        }
        Log.d("StartLocation",
                String.format("%f, %f", this.startLatitude, this.startLongitude));
    }

    private class GPSCollector implements Runnable {
        GPSCollector() {
            networkLatLong = new NetworkLatLong();
            myLatLong = new LatLong();
            ndtLocation = new NdtLocation(context, networkLatLong, myLatLong);
            ndtLocation.startListen();
            if (!ndtLocation.gpsEnabled && !ndtLocation.networkEnabled) {
                createGpsDisabledAlert();
            }
        }

        @Override
        public void run() {
            Log.d("GPSCollector", "Running GPS collection on separate thread");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        if (ndtLocation != null) {
            ndtLocation.stopListen();
        }
        if (db != null) {
            db.close();
        }
        Log.v("TesterFragment", "onDestroy");
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.v("onDestroy", "Release Wake Lock onDestroy");
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (db != null) {
            db.close();
        }
        Log.v("TesterFragment", "onPause");
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.v("onPause", "Release Wake Lock onDestroy");
        }
        ndtLocation.listenInBackground();
        super.onPause();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        Log.v("TesterFragment", "onResume");
        ndtLocation.listenInForeground();
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        if (db != null) {
            db.close();
        }
        Log.v("TesterFragment", "onStop");
        super.onStop();
    }

    /**
     * Initializes the components on main view.
     */
    private void initViews() {
        buttonStandardTest = getActivity().findViewById(R.id.ButtonStandardTest);
        buttonStandardTest.setOnClickListener(new StandardTestButtonListener());
        locationCodeView = getActivity().findViewById(R.id.locationCode);
        networkView = getActivity().findViewById(R.id.Network);
        technologyView = getActivity().findViewById(R.id.Technology);
        longView = getActivity().findViewById(R.id.Long);
        latView = getActivity().findViewById(R.id.Lat);
        progressBar = getActivity().findViewById(R.id.ProgressBar);
        progressBar.setIndeterminate(false);
        topText = getActivity().findViewById(R.id.topText);
        testResultView = getActivity().findViewById(R.id.finalStatus);
        testIcon = getActivity().findViewById(R.id.testStatusIcon);
        uploadedIcon = getActivity().findViewById(R.id.uploadedStatusIcon);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tester_fragment, container, false);
    }

    private void getLastKnownLocationInfo() {
        String GPSLocationProvider = LocationManager.GPS_PROVIDER;
        String NetworkLocationProvider = LocationManager.NETWORK_PROVIDER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                getActivity().requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        Log.d("LastKnownLocation", "Calling last known locations");
        this.ndtLocation.GPSLastKnownLocation =
                ndtLocation.locationManager.getLastKnownLocation(GPSLocationProvider);
        this.ndtLocation.NetworkLastKnownLocation =
                ndtLocation.locationManager.getLastKnownLocation(NetworkLocationProvider);

    }


    /**
     * We need to determine which Iperf to use. If it is Android O
     * and higher, then we should use the NDK version.
     * Otherwise, use the cross-compiled version
     * Also need to make the Iperf file 755 executable
     */
    private void setupIperf() {
        if (Build.VERSION.SDK_INT < 26) {
            copyBinaryFile("android_iperf_2_0_2_3", "iperfM");
            Constants.IPERF_VERSION = "/iperfM";
        } else {
            copyBinaryFile("ndk_iperf", "iperfT");
            Constants.IPERF_VERSION = "/iperfT";
        }
        ExecCommandLine command = new ExecCommandLine("chmod 755 "
                + this.applicationFilesDir + Constants.IPERF_VERSION, 60000, null, null,
                null, uiServices);
        try {
            String output = command.runCommand();
            Log.d("SetupIperf", "Making iperf command executable: " + output);
        } catch (InterruptedException e) {
            Log.e("SetupIperf", e.getMessage());
        } catch (Exception e) {
            Log.e("SetupIperf", "Failed to make Iperf executable. " + e.getMessage());
        }
        printAppDirectoryInfo();
    }

    private String getApplicationFilesDir() {
        File pathForAppFiles = getActivity().getFilesDir();
        return pathForAppFiles.getAbsolutePath();
    }

    private void printAppDirectoryInfo() {
        File pathForAppFiles = getActivity().getFilesDir();
        Log.d(getClass().getSimpleName(), "Listing Files in " + pathForAppFiles.getAbsolutePath());
        String[] fileList = pathForAppFiles.list();
        File[] fileptrs = pathForAppFiles.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            Log.d(getClass().getSimpleName(), "Filename " + i + ": " + fileList[i] + " size: "
                    + fileptrs[i].length());
        }
    }

    public void copyBinaryFile(String inputFilename, String outputFilename) {
        try {
            InputStream inputFile = this.assetManager.open(inputFilename);
            FileOutputStream outputFile = getActivity().openFileOutput(outputFilename,
                    Context.MODE_PRIVATE);
            copy(inputFile, outputFile);
            inputFile.close();
            outputFile.flush();
            outputFile.close();
        } catch (IOException e) {
            Log.e("Asset File Error", e.getMessage());
        }
    }

    private static void copy(InputStream in, FileOutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int read;
        try {
            while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
            }
        } catch (EOFException e) {
            Log.e("EOF File Error", e.getMessage());
        }
    }

    private void makeLocationDialog(String title, String message) {
        validLocation = false;
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);
        alert.setMessage(message);
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        input.append("");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                validLocation = isGoodLocation(value);
                Log.d("ValidLocation", String.valueOf(validLocation));
                locationCode = value;
                locationCodeView.setText(value);
                if (!validLocation) {
                    String errorText = "Location values must be numbers \nbetween "
                            + Constants.MIN_LOCATION + " and " + Constants.MAX_LOCATION;
                    Toast.makeText(context, errorText, Toast.LENGTH_LONG).show();
                } else {
                    initTest();
                }
            }
        });
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        validLocation = false;
                    }
                });
        final AlertDialog locationDialog = alert.create();
        locationDialog.setCanceledOnTouchOutside(false);
        locationDialog.show();
    }

    private boolean isGoodLocation(String value) {
        validLocation = false;
        if ((value.length() == 4) && (value.matches("[0-9]{4}"))) {
            int locationInteger = Integer.parseInt(value);
            if ((locationInteger >= Constants.MIN_LOCATION)
                    && (locationInteger <= Constants.MAX_LOCATION)) {
                validLocation = true;
            }
        }
        return validLocation;
    }

    private class StandardTestButtonListener implements OnClickListener {
        public void onClick(View view) {
            makeLocationDialog(getString(R.string.location_title),
                    getString(R.string.location_subtitle));
            context = view.getContext();
        }
    }

    private class Uploader implements Runnable {
        Uploader() {
            super();
        }

        @Override
        public void run() {
            if (currentTest != null) {
                if (isAborted) {
                    currentTest.saveAllResults(true, false);
                } else {
                    currentTest.saveAllResults(false, true);
                }
            }
        }
    }

    private void initTest() {
        testResults.clear();
        resultsViewer.notifyDataSetChanged();
        rawData = "";
        if (this.connectivityManager == null) {
            this.connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        networkInfo = this.connectivityManager.getActiveNetworkInfo();
        Log.d("networkInfo", String.valueOf(networkInfo));
        mobileInfo = getMobileProperty();
        Log.d("mobileInfo", String.valueOf(mobileInfo));
        serverInfo = getServerInfo();
        telephoneInfo = getTelephoneProperty();
        Thread signalStrengthCollector = new Thread(new SignalStrengthCollector());
        signalStrengthCollector.start();
        if (!ndtLocation.gpsEnabled && !ndtLocation.networkEnabled) {
            createGpsDisabledAlert();
        } else {
            this.connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            usingUploadButton = false;
            if (isNetworkActive()) {
                networkType = ConnectivityManager.TYPE_MOBILE;
                this.wifiManager = (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    networkType = ConnectivityManager.TYPE_WIFI;
                    createWifiAlert();
                } else {
                    if (this.wifiManager.isWifiEnabled()) {
                        createDisableWifiAlert();
                    } else {
                        finishStartButton();
                    }
                }
            } else {
                finishStartButton();
            }
        }
    }

    public boolean isNetworkActive() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = null; // reset
        networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null;
    }

    public void finishApp() {
        ndtLocation.stopListen();
        System.exit(0);
    }

    public void finishStartButton() {
        ToggleButton indoorOutdoorToggle = getActivity()
                .findViewById(R.id.indoorOutdoorToggle);
        indoorOutdoorToggle.setVisibility(View.GONE);
        indoorOutdoorToggle.setEnabled(false);
        buttonStandardTest.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        topText.setVisibility(View.VISIBLE);
        testResultView.setVisibility(View.VISIBLE);
        testIcon.setImageResource(R.drawable.blank_checkbox);
        uploadedIcon.setImageResource(R.drawable.blank_checkbox);
        progressBar.setProgress(0);
        locationCodeView.setVisibility(View.VISIBLE);
        textViewMain.setText("");
        date = new Date();
        writeInfo();
        getLatLong();
        displayTestInfo();
    }

    private void writeInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.official));
        stringBuilder.append(VERSION_NAME);
        stringBuilder.append("\n");
        stringBuilder.append(getString(R.string.test_begins_at,
                String.format("%s\n", date.toString())));
        stringBuilder.append("\n");
        if (Prefs.getGPSoverride(context)) {
            stringBuilder.append("GPS override set by Tester.");
        }
        uiServices.appendString(stringBuilder.toString(), UiServices.MAIN_VIEW);
        uiServices.appendString(stringBuilder.toString(),
                UiServices.SUMMARY_VIEW);
        stringBuilder.append("\n\n");
        stringBuilder.append(getSystemProperty());
        stringBuilder.append("\n");
        stringBuilder.append(mobileInfo);
        stringBuilder.append("\n\n");
        stringBuilder.append(serverInfo);
        stringBuilder.append("\n");
        stringBuilder.append(telephoneInfo);
        stringBuilder.append("\n\n");
        stringBuilder.append("Location ID: ");
        stringBuilder.append(locationCode);
        stringBuilder.append("\n");
        stringBuilder.append(getString(R.string.latitude_display, this.latitude));
        stringBuilder.append("\n");
        stringBuilder.append(getString(R.string.longitude_display, this.longitude));
        uiServices.appendString(stringBuilder.toString(), UiServices.MAIN_VIEW);
        rawData += stringBuilder.toString();
    }

    private class AcquireGPS extends Thread {
        private AndroidUiServices uiServices;
        private LatLong gpsLatLong;

        AcquireGPS(AndroidUiServices uiServices) {
            this.uiServices = uiServices;
            this.gpsLatLong = new LatLong();
        }

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gpsLatLong.getLatitudeLongitude(gpsLatLong);
                if (gpsLatLong.valid) {
                    Log.d("gpsLatLong", String.valueOf(gpsLatLong.valid));
                    uiServices.goodGpsSignal();
                    break;
                }
            }
            if (!gpsLatLong.valid) {
                uiServices.noGpsSignal();
            }
        }
    }

    private void checkGpsOverride() {
        if (Prefs.getGPSoverride(context)) {
            startTest();
        } else {
            acquiringGPS();
            startTest();
        }
    }

    private void acquiringGPS() {
        Thread checkGPS = new Thread(new AcquireGPS(uiServices));
        checkGPS.start();
    }

    private void resultsSaved() {
        testIcon.setImageResource(R.drawable.green_checkbox);
    }

    private void resultsNotSaved() {
        testIcon.setImageResource(R.drawable.red_checkbox);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "Unable to save results to SD card. Please check your settings.")
                .setCancelable(false)
                .setPositiveButton("Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createGpsDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your Location service is disabled! Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable Location",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showGpsOptions();
                            }
                        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finishApp();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showGpsOptions() {
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

    private void createPrivacyPolicyAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "Have you read and agree to our terms and conditions?")
                .setCancelable(false)
                .setPositiveButton("Read",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        builder.setNegativeButton("Yes, I agree.",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences legal = getActivity().getSharedPreferences("Legal",
                                Constants.MODE_PRIVATE);
                        SharedPreferences.Editor legalEditor = legal.edit();
                        legalEditor.putBoolean("privacyPolicyAccepted", true);
                        legalEditor.apply();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        Button readButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        readButton.setOnClickListener(new PrivacyPolicyReadListener());
    }

    private class PrivacyPolicyReadListener implements OnClickListener {

        PrivacyPolicyReadListener() {
        }

        @Override
        public void onClick(View v) {
            openWebURL(Constants.privacyPolicyURL);
        }
    }

    private void createWifiAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "You're connected to Wifi!\n Would you like to use WiFi?")
                .setCancelable(false)
                .setPositiveButton("WiFi",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                uiServices.printWifiID();
                                finishStartButton();
                            }
                        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createDisableWifiAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "WiFi is turned on, but you may not be logged in. " +
                        "Please log into your WiFi network or turn it off before running CalSPEED.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Gets the system related properties.
     *
     * @return a string describing the OS and Java environment
     */
    private String getSystemProperty() {
        String osName, osArch, osVer, javaVer, javaVendor;
        osName = System.getProperty("os.name");
        osArch = System.getProperty("os.arch");
        osVer = System.getProperty("os.version");
        javaVer = System.getProperty("java.version");
        javaVendor = System.getProperty("java.vendor");
        String deviceModel = Build.MODEL;
        int sdkInt = Build.VERSION.SDK_INT;
        return "\n" +
                getString(R.string.os_line, osName, osArch, osVer) +
                "\n" +
                getString(R.string.java_line, javaVer, javaVendor) +
                "\nDevice Name: " + deviceModel  +
                "\nSDK Version Number: " + String.valueOf(sdkInt);
    }


    /**
     * Gets the mobile device related properties.
     *
     * @return a string about locationCode, network type (MOBILE or WIFI)
     */
    private String getMobileProperty() {
        StringBuilder sb = new StringBuilder();
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            Log.d(getClass().getSimpleName(), networkInfo.toString());
            sb.append(getString(R.string.network_type_indicator, networkInfo.getTypeName()));
            sb.append("\n");
        }
        if (ndtLocation.gpsEnabled && ndtLocation.location != null) {
            LatLong newLatLong = new LatLong();
            newLatLong.getLatitudeLongitude(newLatLong);
            if (newLatLong.valid) {
                Log.v(this.getClass().getSimpleName(), ndtLocation.location.toString());
                sb.append(getString(R.string.latitude_result, newLatLong.latitude));
                sb.append("\n");
                sb.append(getString(R.string.longitude_result, newLatLong.longitude));
                sb.append("\n");
            }
        } else {
            sb.append(getString(R.string.no_GPS_info, ""));
        }

        return sb.toString();
    }

    /**
     * Gets the mobile provider related properties.
     *
     * @return a string about network providers and network type
     */
    @SuppressLint("NewApi")
    private String getTelephoneProperty() {
        StringBuilder sb = new StringBuilder();
        try {
            String providerName = this.telephonyManager.getSimOperatorName();
            if (providerName == null) {
                providerName = "UNKNOWN";
            }
            Log.v("debug", providerName);
            sb.append("\n").append(getString(R.string.network_provider, providerName));
            Provider = sb.substring(18, sb.length());
        } catch (Exception e) {
            Log.e("GetTelephonyProperty", "Failed to get NETWORK PROVIDER " + e.getMessage());
            sb.append(getString(R.string.network_provider, "na"));
        }
        try {
            String operatorName = telephonyManager.getNetworkOperatorName();
            if (Provider.equalsIgnoreCase("")) {
                Provider = operatorName;
            }
            getPorts();
            if (operatorName == null) {
                operatorName = "UNKNOWN";
            }
            Log.v("debug", operatorName);
            sb.append("\n").append(getString(R.string.network_operator, operatorName));
        } catch (Exception e) {
            Log.e("GetTelephonyProperty", "Failed to get NETWORK OPERATOR " + e.getMessage());
            sb.append("\n").append(getString(R.string.network_operator, "na"));
        }
        try {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                sb = printInfoLine(R.string.connection_type, Constants.WIFI, sb);
            } else {
                String connectionType = getConnectionType();
                if (connectionType == null) {
                    connectionType = "UNKNOWN";
                }
                Log.d(getClass().getSimpleName(), connectionType);
                sb = printInfoLine(R.string.connection_type, connectionType, sb);
            }
        } catch (Exception e) {
            Log.e("GetTelephonyProperty", "Failed to get CONNECTION TYPE " + e.getMessage());
            sb.append("\n").append(getString(R.string.connection_type, "UNKNOWN"));
        }
        try {
            if (Build.VERSION.SDK_INT < 28) {
                deviceId = telephonyManager.getDeviceId();
            } else {
                deviceId = telephonyManager.getMeid();
            }
            sb.append(String.format("\nDevice ID: %s", deviceId));
        } catch (Exception e) {
            Log.e("GetTelephonyProperty", "Failed to get DEVICE ID " + e.getMessage());
            sb.append(String.format("\nDevice ID: %s", "na"));
        }
        Log.d("TelephonyProperty", sb.toString());
        return sb.toString();
    }

    private void displayTestInfo() {
        technologyView.setVisibility(View.VISIBLE);
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            networkView.setText(getString(R.string.network_display,
                    getString(R.string.wifi_display)));
            technologyView.setVisibility(View.GONE);
        } else {
            networkView.setText(getString(R.string.network_display, ndi.getNetworkOperatorName()));
            technologyView.setText(getString(R.string.tech_display, getConnectionType()));
        }
    }


    public String getServerInfo() {
        int fieldTestUser = Constants.FIELDTEST_OFFICIAL;
        String serverName = Constants.FIELDTEST_SERVER_NAMES[fieldTestUser];
        String serverHost = Constants.FIELDTEST_SERVER_HOSTS[fieldTestUser];
        return getString(R.string.server_indicator, serverName) +
                "\n" +
                getString(R.string.host_indicator, serverHost);
    }

    private void getPorts() {
        if (Provider.equalsIgnoreCase("at&t")) {
            TCPPort = Constants.ports[0];
            UDPPort = Constants.ports[1];
        } else if (Provider.equalsIgnoreCase("sprint")) {
            TCPPort = Constants.ports[2];
            UDPPort = Constants.ports[3];
        } else if (Provider.equalsIgnoreCase("t-mobile")) {
            TCPPort = Constants.ports[4];
            UDPPort = Constants.ports[5];
        } else if (Provider.equalsIgnoreCase("verizon")) {
            TCPPort = Constants.ports[6];
            UDPPort = Constants.ports[7];
        } else {
            TCPPort = Constants.ports[8];
            UDPPort = Constants.ports[9];
        }
    }

    private StringBuilder printInfoLine(int label, String variable,
                                        StringBuilder buffer) {
        if (Constants.DEBUG)
            Log.v("debug", variable);
        buffer.append("\n").append(getString(label, variable));
        return buffer;
    }

    private String getConnectionType() {
        Integer connectionIndex;
        String type = "UNKNOWN";
        final int connection = this.telephonyManager.getNetworkType();
        for (int i = 0; i < Constants.NETWORK_TYPE.length; i++) {
            connectionIndex = Integer.valueOf(Constants.NETWORK_TYPE[i][0]);
            if (connectionIndex == connection) {
                type = Constants.NETWORK_TYPE[i][1];
                break;
            }
        }
        return type;
    }

    class LatLong {
        Location oldLocation;
        Location location;
        Double latitude;
        Double longitude;
        Boolean valid;

        LatLong() {
            this.latitude = 0.0;
            this.longitude = 0.0;
            this.valid = false;
        }

        synchronized void setLatitudeLongitude(Double latitude, Double longitude, Boolean valid) {
            myLatLong.latitude = latitude;
            myLatLong.longitude = longitude;
            myLatLong.valid = valid;
        }

        synchronized Boolean getLatitudeLongitude(LatLong structLatLong) {
            structLatLong.latitude = myLatLong.latitude;
            structLatLong.longitude = myLatLong.longitude;
            structLatLong.valid = myLatLong.valid;
            return (structLatLong.valid);
        }

        void updateLatitudeLongitude() {
            if (ndtLocation.location != null) {
                this.location = ndtLocation.location;
                if (this.location.getLatitude() != 0.0 && this.location.getLongitude() != 0.0) {
                    myLatLong.setLatitudeLongitude(
                            ndtLocation.location.getLatitude(),
                            ndtLocation.location.getLongitude(), true);
                    uiServices.updateLatLong(ndtLocation.location.getLatitude(),
                            ndtLocation.location.getLongitude());
                    Log.v("UpdatingLatLong", this.latitude + ", " + this.longitude);
                    this.oldLocation = ndtLocation.location;
                }
            } else {
                myLatLong.setLatitudeLongitude(0.0, 0.0, false);
            }
        }
        void printLocation() {
            Log.i("LocationChange", String.format("Best location: Provider: %s; (%f, %f);" +
                            " Accuracy %f; Speed %f;", location.getProvider(),
                    location.getLatitude(), location.getLongitude(), location.getAccuracy(),
                    location.getSpeed()));
        }
    }


    class NetworkLatLong {
        Location location;
        Location oldLocation;
        Double latitude;
        Double longitude;
        Boolean valid;

        NetworkLatLong() {
            this.latitude = 0.0;
            this.longitude = 0.0;
            this.valid = false;
        }

        synchronized void setLatitudeLongitude(Double latitude,
                                               Double longitude, Boolean valid) {
            networkLatLong.latitude = latitude;
            networkLatLong.longitude = longitude;
            networkLatLong.valid = valid;
        }

        synchronized Boolean getLatitudeLongitude(NetworkLatLong structLatLong) {
            structLatLong.latitude = networkLatLong.latitude;
            structLatLong.longitude = networkLatLong.longitude;
            structLatLong.valid = networkLatLong.valid;
            return (structLatLong.valid);
        }

        void updateNetworkLatitudeLongitude(Location location) {
            if (location != null) {
                this.location = location;
                if (this.location.getLatitude() != 0.0 && this.location.getLongitude() != 0.0) {
                    if (oldLocation != null) {
                        Log.v("UpdateLocation", "Old network location time: " + oldLocation.getTime());
                        Log.v("UpdateLocation", "Old network location accuracy: " + oldLocation.getAccuracy());
                    }
                    Log.v("UpdateLocation", "New network location time: " + location.getTime());
                    Log.v("UpdateLocation", "New network location accuracy: " + location.getAccuracy());
                    networkLatLong.setLatitudeLongitude(
                            location.getLatitude(),
                            location.getLongitude(), true);
                    uiServices.updateLatLong(location.getLatitude(),
                            location.getLongitude());
                    Log.v("UpdatingLatLong", this.latitude + ", " + this.longitude);
                    this.oldLocation = location;
                }
            } else {
                networkLatLong.setLatitudeLongitude(0.0, 0.0, false);
            }
        }

        void printLocation() {
            Log.i("LocationChange", String.format("Best location: Network location; (%f, %f);" +
                            " Accuracy %f; Speed %f;", location.getLatitude(),
                    location.getLongitude(), location.getAccuracy(), location.getSpeed()));
        }
    }

    private void setupUploadTimer() {
        smoothUpload = 0.0f;
        UploadTimer = new Timer();
        uploadTask = new TimerTask() {
            @Override
            public void run() {
            }
        };
    }

    private void setupDownloadTimer() {
        DownloadTimer = new Timer();
        downloadTask = new TimerTask() {
            @Override
            public void run() {
            }
        };
    }

    private class UiHandler extends Handler {
        UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            String results;
            uiServices.setCurrentTask();
            try {
                switch (message.what) {
                    case Constants.THREAD_MAIN_APPEND:
                        textViewMain.append(message.obj.toString());
                        break;
                    case Constants.THREAD_STAT_APPEND:
                        rawData += message.obj.toString();
                        break;
                    case Constants.THREAD_LAT_LONG_APPEND:
                        textViewMain.append("\nLatitude: " + startLatitude);
                        textViewMain.append("\nLongitude: " + startLongitude + "\n");
                        break;
                    case Constants.THREAD_BEGIN_TEST:
                        if (Constants.DEBUG)
                            Log.v("debug", "Begin the test");
                        buttonStandardTest.setEnabled(false);
                        progressBar.setMax(Constants.TEST_STEPS);
                        if (!wakeLock.isHeld()) {
                            wakeLock.acquire();
                            if (Constants.DEBUG)
                                Log.v("debug", "wakeLock acquired");
                        }
                        break;
                    case Constants.THREAD_END_TEST:
                        Log.v("debug", "End the test");
                        textViewMain.append("\n-----End of Test------\n");
                        rawData += "\n";
                        buttonStandardTest.setEnabled(true);
                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                        buttonStandardTest.setText(getString(R.string.testAgain));
                        buttonStandardTest.setVisibility(Button.VISIBLE);
                        progressBar.setVisibility(ProgressBar.GONE);
                        buttonStandardTest.setEnabled(true);
                        String[] videoSummary = currentTest.getVideoCalc();
                        String videoClassification = videoSummary[2];
                        String[] conferenceSummary = currentTest.getVideoConferenceCalc();
                        String conferenceClassification = conferenceSummary[2];
                        Log.d("Video Calc", videoClassification);
                        Log.d("Conference", conferenceClassification);
                        networkLatLong.printLocation();
                        myLatLong.printLocation();
                        StandardTest.resetScores();
                    case Constants.THREAD_TEST_INTERRUPTED:
                        if (Constants.DEBUG)
                            Log.v("debug", "End the test");
                        textViewMain.append("\n-----End of Test------\n");
                        rawData += "\n";
                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                            if (Constants.DEBUG)
                                Log.v("debug", "wakeLock released");
                        }
                        testThread.interrupt();
                        Thread.currentThread().interrupt();
                        break;
                    case Constants.THREAD_ADD_PROGRESS:
                        final Integer increment = message.getData().getInt("increment");
                        progressBar.setProgress(progressBar.getProgress() + increment);
                        Log.v("IncrementProgress", "Increment progress: " + increment);
                        Log.v("IncrementProgress", "Total progress: " + progressBar.getProgress());
                        Log.v("IncrementProgress", "Max: " + progressBar.getMax());
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            Log.w("ProgressBar", "Exception while thread sleeping");
                        }
                        break;
                    case Constants.THREAD_SET_PROCESS_HANDLE:
                        currentProcess = (Process) message.obj;
                        break;
                    case Constants.THREAD_CLEAR_PROCESS_HANDLE:
                        break;
                    case Constants.THREAD_WAIT_FOR_GPS_SIGNAL:
                        Log.d("WAIT GPS", "Wait for GPS");
                        getLatLong();
                        break;
                    case Constants.THREAD_GOOD_GPS_SIGNAL:
                        Log.d("GOOD GPS", "Good GPS");
                        startTest();
                        break;
                    case Constants.THREAD_NO_GPS_SIGNAL:
                        Log.d("NO GPS", "No GPS");
                        gpsDialog.setVisibility(View.GONE);
                        break;
                    case Constants.THREAD_NO_MOBILE_CONNECTION:
                        break;
                    case Constants.THREAD_GOT_MOBILE_CONNECTION:
                        finishStartButton();
                        break;
                    case Constants.THREAD_UPDATE_LATLONG:
                        Double latToUpdate = message.getData().getDouble("lat");
                        Double lonToUpdate = message.getData().getDouble("lon");
                        String latitude = getLatitude();
                        String longitude = getLongitude();
                        Log.v("UpdateLatLong",
                                String.format("Old coordinates: %s, %s", latitude, longitude));
                        latitude = Double.toString(latToUpdate);
                        longitude = Double.toString(lonToUpdate);
                        Log.v("UpdateLatLong",
                                String.format("New coordinates: %s, %s", latitude, longitude));
                        if (latitude.length() > 11)
                            latitude = latitude.substring(0, 10);
                        if (longitude.length() > 11)
                            longitude = longitude.substring(0, 10);
                        if (!longitude.equals("0.0") && !latitude.equals("0.0")) {
                            setLatLong(latitude, longitude);
                            latView.setText(String.format("Lat: %s", latitude));
                            longView.setText(String.format("Long: %s", longitude));
                        }
                        getCpuTemp();
                        break;
                    case Constants.THREAD_ADD_TO_RESULTS_LIST:
                        String currentResult = message.getData().getString("results");
                        int image = message.getData().getInt("imageCode");
                        ResultsItem addResult = new ResultsItem(getCheckBox(image), currentResult);
                        if (!testResults.add(addResult)) {
                            Log.e("AddResultsItem", "Error adding new line to results line.");
                        }
                        if (resultsViewer.getCount() > 2) {
                            View item = resultsViewer.getView(0, null, list);
                            item.measure(0, 0);
                            LinearLayout.LayoutParams params =
                                    new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            (int) (3.5 * item.getMeasuredHeight()));
                            list.setLayoutParams(params);
                        }
                        resultsViewer.notifyDataSetChanged();
                        break;
                    case Constants.THREAD_UPDATE_RESULTS_LIST:
                        ResultsItem resultToUpdate = testResults.get(testResults.size() - 1);
                        Log.d("UpdateResultsList", "Updating: " + resultToUpdate.getDataText());
                        int newImage = message.getData().getInt("imageCode");
                        String resultsText = message.getData().getString("results");
                        resultToUpdate.setDataImage(getCheckBox(newImage));
                        resultToUpdate.setText(resultsText);
                        resultsViewer.notifyDataSetChanged();
                        break;
                    case Constants.THREAD_RESULTS_SAVED:
                        resultsSaved();
                        break;
                    case Constants.THREAD_RESULTS_NOT_SAVED:
                        resultsNotSaved();
                        break;
                    case Constants.THREAD_RESULTS_UPLOADED:
                        buttonStandardTest.setEnabled(true);
                        uploadedIcon.setImageResource(R.drawable.green_checkbox);
                        break;
                    case Constants.THREAD_RESULTS_NOT_UPLOADED:
                        buttonStandardTest.setEnabled(true);
                        uploadedIcon.setImageResource(R.drawable.red_checkbox);
                        break;
                    case Constants.THREAD_RESULTS_ATTEMPT_UPLOAD:
                        Log.d("AttemptUpload", "Disable start button");
                        buttonStandardTest.setEnabled(false);
                        uploadedIcon.setImageResource(R.drawable.blank_checkbox);
                        break;
                    case Constants.THREAD_SET_STATUS_TEXT:
                        results = message.getData().getString("text");
                        Log.v("Results", "RESULTS: " + results);
                        topText.setText(results);
                        break;
                    case Constants.THREAD_PRINT_BSSID_SSID:
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID();
                        String bssid = wifiInfo.getBSSID();
                        String wifiOutput = String.format("\n\nWiFi SSID: %s\nWiFi BSSID: %s\n",
                                ssid, bssid);
                        textViewMain.append(wifiOutput);
                        rawData += wifiOutput;
                        break;
                    case Constants.THREAD_WRITE_UPLOAD_DATA:
                        break;
                    case Constants.THREAD_WRITE_DOWNLOAD_DATA:
                        break;
                    case Constants.THREAD_WRITE_LATENCY_DATA:
                        break;
                    case Constants.THREAD_WRITE_JITTER_DATA:
                        break;
                    case Constants.THREAD_WRITE_MOS_DATA:
                        break;
                    case Constants.FINISH_PHASE:
                        break;
                    case Constants.THREAD_START_UPLOAD_TIMER:
                        smoothUpload = 0.0f;
                        UploadTimer.cancel();
                        UploadTimer.purge();
                        UploadTimer = null;
                        setupUploadTimer();
                        UploadTimer.scheduleAtFixedRate(uploadTask, 0, 1000);
                        break;
                    case Constants.THREAD_STOP_UPLOAD_TIMER:
                        UploadTimer.cancel();
                        UploadTimer.purge();
                        UploadTimer = null;
                        setupUploadTimer();
                        break;
                    case Constants.THREAD_UPDATE_UPLOAD_NUMBER:
                        if (Constants.UploadDebug)
                            Log.v("debug", "in handler update Upload Timer number="
                                    + smoothUpload.toString());
                        break;
                    case Constants.THREAD_SET_UPLOAD_NUMBER:
                        String num2 = message.getData().getString("number");
                        if (Constants.UploadDebug)
                            Log.v("debug", "in handler set upload number number="
                                    + num2);
                        break;
                    case Constants.THREAD_SET_UPLOAD_NUMBER_STOP_TIMER:
                        UploadTimer.cancel();
                        UploadTimer.purge();
                        UploadTimer = null;
                        String num1 = message.getData().getString("number");
                        if (Constants.UploadDebug)
                            Log.v("debug", "in handler stop Upload Timer number="
                                    + num1);
                        setupUploadTimer();
                        break;
                    case Constants.THREAD_START_DOWNLOAD_TIMER:
                        DownloadTimer.scheduleAtFixedRate(downloadTask, 0, 1000);
                        break;
                    case Constants.THREAD_STOP_DOWNLOAD_TIMER:
                        DownloadTimer.cancel();
                        DownloadTimer.purge();
                        DownloadTimer = null;
                        setupDownloadTimer();
                        break;
                    case Constants.THREAD_UPDATE_DOWNLOAD_NUMBER:
                        break;
                    case Constants.THREAD_SET_DOWNLOAD_NUMBER:
                        break;
                    case Constants.THREAD_SET_DOWNLOAD_NUMBER_STOP_TIMER:
                        DownloadTimer.cancel();
                        DownloadTimer.purge();
                        DownloadTimer = null;
                        setupDownloadTimer();
                        break;
                    case Constants.THREAD_UPDATE_NETWORK_INFO:
                        getNetworkAndDeviceInfo();
                        break;
                    case Constants.THREAD_UPLOAD_ALL_FILES:
                        Thread uploadWorker = new Thread(new Uploader());
                        uploadWorker.start();
                    case Constants.THREAD_DISPLAY_TCP:
                        break;
                    case Constants.THREAD_DISPLAY_UDP:
                        break;
                    case Constants.THREAD_DISPLAY_PING:
                        break;
                    case Constants.THREAD_HIDE_TEST_DISPLAY:
                        break;
                    case Constants.THREAD_GET_SIGNAL_STRENGTH:
                        rawData += String.format("\nSignal Strength: %s dB", signalStrengthPower);
                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    private Integer getCheckBox(int imageCode) {
        Integer imageReturnCode;
        switch (imageCode) {
            case Constants.GREEN_CHECKBOX:
                imageReturnCode = R.drawable.green_checkbox;
                break;
            case Constants.RED_CHECKBOX:
                imageReturnCode = R.drawable.red_checkbox;
                break;
            case Constants.BLANK_CHECKBOX:
                imageReturnCode = R.drawable.blank_checkbox;
                break;
            default:
                imageReturnCode = R.drawable.blank_checkbox;
                break;
        }
        return imageReturnCode;
    }

    private void startTest() {
        String s1 = Constants.FIELDTEST_SERVER_HOSTS[0];
        String s2 = Constants.FIELDTEST_SERVER_HOSTS[1];
        String networkType;
        if (networkInfo != null) {
            networkType = networkInfo.getTypeName();
        } else {
            networkType = "UNKNOWN";
        }
        StandardTest stdTest = new StandardTest(uiServices, rawData, deviceId, date,
                applicationFilesDir, networkType, ndi, networkLatLong, myLatLong, s1,
                s2, TCPPort, UDPPort);
        testThread = new Thread(stdTest);
        currentTest = stdTest;
        this.testThread.start();
    }

    String getLatitude() {
        return this.latitude;
    }

    String getLongitude() {
        return this.longitude;
    }

    private void getLatLong() {
        showAcquiringGPS();
        Log.v("GetLocation", "Starting values: " + this.latitude + ", " + this.longitude);
        (new LocationWaiter()).execute();
    }

    private void setLatLong(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private class LocationWaiter extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            hasLatLong = false;
            for (int i = 0; i < 10; i++) {
                if (Double.valueOf(latitude) != 0.0 && Double.valueOf(longitude) != 0.0) {
                    Log.v("GetLocation", latitude + ", " + longitude);
                    hasLatLong = true;
                    break;
                } else {
                    Log.v("GetLocation", "Waiting for location... ");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.e("GetInitialLocation", e.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            hideAcquiringGPS();
            if (hasLatLong) {
                Log.v("GetLocation", latitude + ", " + longitude);
                checkGpsOverride();
            } else {
                topText.setText(R.string.noGpsLocation);
                checkGpsOverride();
                resetTest();
            }
        }
    }

    private void resetTest() {
        currentProcess.destroy();
        uiServices.onEndTest();
    }

    private void showAcquiringGPS() {
        buttonStandardTest.setEnabled(false);
        topText.setText(R.string.acquiringGps);
    }

    private void hideAcquiringGPS() {
        buttonStandardTest.setEnabled(true);
        topText.setText(R.string.acquireGps);
    }

    private class SignalStrengthCollector implements Runnable {
        SignalStrengthCollector() {
            ndi = new NetworkAndDeviceInformation(uiServices,
                    telephonyManager, connectivityManager);
        }

        @Override
        public void run() {
            Log.d("SignalStrengthCollector", "All the work is done in NetworkAndDeviceInfo");
        }
    }

    private void getNetworkAndDeviceInfo() {
        if (ndi.getConnectivity()) {
            int signalPower = ndi.getSignalStrength();
            if (signalPower == 90 || signalPower <= -140) {
                this.signalStrengthPower = "N/A";
            } else {
                this.signalStrengthPower = String.valueOf(signalPower);
            }
        }
    }


}
