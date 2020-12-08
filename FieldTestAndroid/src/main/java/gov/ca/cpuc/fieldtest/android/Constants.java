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

import java.util.HashMap;

/**
 * Definition for constant values .
 */
public class Constants {

    static final String CROWDSOURCE_DATASERVERS[] = {
    };

    static final String FIELDTEST_SERVER_NAMES[] = {
    };

    static final String FIELDTEST_SERVER_HOSTS[] = {
    };

    static final String FIELDTEST_DATASERVERS[] = {
    };

    static final String FIELDTEST_USERNAMES[] = {
    };

    static final int FIELDTEST_OFFICIAL = 0;
    static final int FIELDTEST_DEVELOPMENT = 1;

    static final HashMap PW = new HashMap<String, String>() {{
    }};


    public static final String ports[] = {};

    /**
     * SMOOTHING NUMBER BETWEEN 0 AND 1 FOR LOW PASS FILTERING
     */
    static final float SMOOTH = 0.15f;

    /* Preference static variables */
    static final int MODE_PRIVATE = 0;

    /**
     * Maximum test steps for ProgressBar setting.
     */
    static final int TEST_STEPS = 20;

    static final String privacyPolicyURL = "http://www.cpuc.ca.gov/General.aspx?id=1778";

    static final float METERS_TO_FEET = 3.28084f;

    static final int THREAD_MAIN_APPEND = 0;
    static final int THREAD_STAT_APPEND = 1;
    static final int THREAD_BEGIN_TEST = 2;
    static final int THREAD_END_TEST = 3;
    static final int THREAD_ADD_PROGRESS = 4;
    static final int THREAD_SUMMARY_APPEND = 5;
    static final int THREAD_LAT_LONG_APPEND = 6;
    static final int THREAD_SET_PROCESS_HANDLE = 7;
    static final int THREAD_CLEAR_PROCESS_HANDLE = 8;
    static final int THREAD_GOOD_GPS_SIGNAL = 9;
    static final int THREAD_NO_GPS_SIGNAL = 10;
    static final int THREAD_UPDATE_LATLONG = 11;
    static final int THREAD_RESULTS_SAVED = 12;
    static final int THREAD_RESULTS_NOT_SAVED = 13;
    static final int THREAD_RESULTS_UPLOADED = 14;
    static final int THREAD_RESULTS_NOT_UPLOADED = 15;
    static final int THREAD_RESULTS_ATTEMPT_UPLOAD = 16;
    public static final int THREAD_ADD_TO_RESULTS_LIST = 17;
    public static final int THREAD_UPDATE_RESULTS_LIST = 18;
    public static final int THREAD_TEST_TIMED_OUT = 19;
    static final int THREAD_TEST_INTERRUPTED = 20;
    static final int THREAD_SET_STATUS_TEXT = 21;
    static final int THREAD_PRINT_BSSID_SSID = 22;
    static final int THREAD_NO_MOBILE_CONNECTION = 23;
    static final int THREAD_GOT_MOBILE_CONNECTION = 24;
    public static final int THREAD_START_ANIMATION = 25;
    public static final int THREAD_STOP_ANIMATION = 26;
    static final int THREAD_WRITE_UPLOAD_DATA = 27;
    static final int THREAD_WRITE_DOWNLOAD_DATA = 28;
    static final int THREAD_WRITE_LATENCY_DATA = 29;
    static final int THREAD_WRITE_JITTER_DATA = 30;
    static final int THREAD_CONNECTIVITY_FAIL = 31;
    static final int FINISH_PHASE = 32;
    static final int THREAD_START_UPLOAD_TIMER = 33;
    static final int THREAD_STOP_UPLOAD_TIMER = 34;
    static final int THREAD_START_DOWNLOAD_TIMER = 35;
    static final int THREAD_STOP_DOWNLOAD_TIMER = 36;
    static final int THREAD_UPDATE_DOWNLOAD_NUMBER = 37;
    static final int THREAD_UPDATE_UPLOAD_NUMBER = 38;
    static final int THREAD_SET_DOWNLOAD_NUMBER = 39;
    static final int THREAD_SET_DOWNLOAD_NUMBER_STOP_TIMER = 40;
    static final int THREAD_SET_UPLOAD_NUMBER = 41;
    static final int THREAD_SET_UPLOAD_NUMBER_STOP_TIMER = 42;
    static final int THREAD_WRITE_MOS_DATA = 43;
    static final int THREAD_SET_MOS_VALUE = 44;
    static final int THREAD_UPDATE_NETWORK_INFO = 45;
    static final int THREAD_UPLOAD_ALL_FILES = 46;
    static final int THREAD_DISPLAY_TCP = 47;
    static final int THREAD_DISPLAY_UDP = 48;
    static final int THREAD_DISPLAY_PING = 49;
    static final int THREAD_HIDE_TEST_DISPLAY = 50;
    static final int THREAD_GET_SIGNAL_STRENGTH = 51;
    static final int THREAD_WAIT_FOR_GPS_SIGNAL = 52;

    static final int GREEN_CHECKBOX = 0;
    static final int RED_CHECKBOX = 1;
    static final int BLANK_CHECKBOX = 2;

    static final Integer DEFAULT_THREAD_NUMBER = 1;
    static final String DEFAULT_WINDOW_SIZE = "256";
    static final Integer DEFAULT_UDP_LENGTH = 220;
    static final String DEFAULT_UDP_BANDWIDTH = "88k";
    static final String DEFAULT_WINDOW_SIZE_FORMAT = "k";
    static final Integer DEFAULT_TEST_INTERVAL = 1;
    static final Integer DEFAULT_TEST_TIME = 20;
    static final String PRELIM_WINDOW_SIZE = "64k";
    static final Integer PRELIM_THREAD_NUMBER = 1;
    static final Integer PRELIM_TEST_TIME = 10;
    static final String WEAK_SIGNAL_WINDOW_SIZE = "32k";
    static final Integer WEAK_SIGNAL_THREAD_NUMBER = 1;
    static final Integer WEAK_SIGNAL_TEST_TIME = 5;
    static Integer IPERF_TCP_THREADS = 4;
    static Integer IPERF_TCP_LOWEST_THREAD_NUM = 5;
    static final Integer NUM_ONE_SEC_UDP_TESTS_PER_SERVER = 3;
    static final Integer NUM_FIVE_SEC_UDP_TESTS_PER_SERVER = 1;
    static final Integer NUM_TCP_TESTS_PER_SERVER = 2;
    static final Double IPERF_BIG_NUMBER_ERROR = 9999999999.99; //iperf error puts big number in kbytes/sec data

    static final String THREAD_NUMBER = "threadNumber";
    static final String WINDOW_SIZE = "windowSize";
    static final String TEST_TIME = "testTime";

    static final boolean DEBUG_MOS = false;
    static final boolean DEBUG = false;
    static final boolean UploadDebug = false;
    static final boolean DownloadDebug = false;

    static final long IPERF_WEAK_SIGNAL_TCP_TIMEOUT = 20000;
    static final int IPERF_PRELIM_TCP_TIMEOUT = 40000;
    static final int IPERF_TCP_TIMEOUT = 90000;
    static final int IPERF_UDP_TIMEOUT = 60000;
    static final int PING_TIMEOUT = 30000;

    static final int MIN_LOCATION = 1000;
    static final int MAX_LOCATION = 9999;

    static final int RUNCOMMAND_SUCCESS = 0;
    static final int RUNCOMMAND_FAIL = 1;
    static final int RUNCOMMAND_INTERRUPT = 2;
    static final String PING_100_PERCENT = "100% packet udpLoss";
    static final int PING_100_PERCENT_LOSS = 3;
    static final int PING_CONNECTIVITY_FAIL = 4;

    static final String TEST_ONE_MESSAGE = "\nStarting Test 1: Iperf TCP West....\n";
    static final String TEST_TWO_MESSAGE = "\nStarting Test 2: Iperf TCP East....\n";
    static final String TEST_THREE_MESSAGE = "\nStarting Test 3: Ping East....\n";
    static final String TEST_FOUR_MESSAGE = "\nStarting Test 4: Iperf TCP West....\n";
    static final String TEST_FIVE_MESSAGE = "\nStarting Test 5: Iperf TCP East....\n";
    static final String TEST_SIX_MESSAGE = "\nStarting Test 6: Ping West....\n";
    static final String TEST_SEVEN_MESSAGE =
            "\nStarting Test 7: 3 Iperf West UDP 1 second tests....\n";
    static final String TEST_EIGHT_MESSAGE =
            "\nStarting Test 8: 3 Iperf East UDP 1 second tests....\n";
    static final String TEST_NINE_MESSAGE =
            "\nStarting Test 9: One Iperf West UDP 5 second test....\n";
    static final String TEST_TEN_MESSAGE =
            "\nStarting Test 10: One Iperf East UDP 5 second test....\n";
    static final String TEST_ELEVEN_MESSAGE =
            "\nStarting Test 11: Capturing Signal Strength....\n";
    static final String UDP = "UDP";
    static final String TCP = "TCP";
    static final String PING = "PING";
    public static final String CONNECTION_TIMED_OUT = "Timed Out";
    public static final String TCP_CONNECTION_FAIL = "TCP Connection Failed";
    public static final String UDP_CONNECTION_FAIL = "UDP Connection Failed";
    public static final String TEST_INTERRUPTED = "Test Interrupted";
    static final String FAILED_TCP_LINE = "TCP Test Failed";
    static final String FAILED_UDP_LINE = "UDP Test Failed";
    static final String FAILED_PING_LINE = "Ping Test Failed";
    static final String SUCCESS_TCP_LINE = "UP: %1$s Kb/s; DOWN: %2$s Kb/s";
    static final String SUCCESS_UDP_LINE = "Jitter: %1$s ms; Loss: %2$s %%";
    static final String SUCCESS_PING_LINE = "Latency: %1$s ms; Loss: %2$s %%";

    static String IPERF_VERSION = "/iperfT";
    static String UDP_COMMAND = "%3$s -c %1$s -u -l %4$d -b %5$s -i %8$d -t %7$d -f %6$s -p %2$s";
    static String TCP_COMMAND = "%3$s -c %1$s -e -w %4$s -P %5$d -i %8$d -t %7$d -f %6$s -p %2$s";
    static String PING_COMMAND = "ping -c %2$s %1$s";

    static String MOBILE = "MOBILE";
    static String WIFI = "WIFI";

    // Network type from call TelephonyManager.getNetworkType()
    static final String NETWORK_TYPE[][] = {
            {"7", "1xRTT"},
            {"4", "CDMA"},
            {"2", "EDGE"},
            {"14", "EHRPD"},
            {"5", "EVDO REV 0"},
            {"6", "EVDO REV A"},
            {"12", "EVDO REV B"},
            {"1", "GPRS"},
            {"8", "HSDPA"},
            {"10", "HSPA"},
            {"15", "HSPA+"},
            {"9", "HSUPA"},
            {"11", "IDEN"},
            {"13", "LTE"},
            {"3", "UMTS"},
            {"0", "UNKNOWN"},
            {"16", "GSM"},
            {"17", "IWLAN"},
            {"18", "TD_SCDMA"},
            {"20", "NR_5G"}
    };

    /**
     * URL for reverse geocoding and arcGIS data gathering
     */
    public static final String ARCGIS_GEOMETRYSERVER = "";
    public static final int IN_SECRET = 0;
    public static final int OUT_SECRET = 0;

    public static final String ARCGIS_MAPSERVER = "";


    private Constants() {
        // private constructor to make sure it can't be instantiated
    }
}
