/*
 Copyright 2003 University of Chicago.  All rights reserved.
 The Web100 Network Diagnostic Tool (NDT) is distributed subject to
 the following license conditions:
 SOFTWARE LICENSE AGREEMENT
 Software: Web100 Network Diagnostic Tool (NDT)

 1. The "Software", below, refers to the Web100 Network Diagnostic Tool (NDT)
 (in either source code, or binary form and accompanying documentation). Each
 licensee is addressed as "you" or "Licensee."

 2. The copyright holder shown above hereby grants Licensee a royalty-free
 nonexclusive license, subject to the limitations stated herein and U.S. Government
 license rights.

 3. You may modify and make a copy or copies of the Software for use within your
 organization, if you meet the following conditions:
 a. Copies in source code must include the copyright notice and this Software
 License Agreement.
 b. Copies in binary form must include the copyright notice and this Software
 License Agreement in the documentation and/or other materials provided with the copy.

 4. You may make a copy, or modify a copy or copies of the Software or any
 portion of it, thus forming a work based on the Software, and distribute copies
 outside your organization, if you meet all of the following conditions:
 a. Copies in source code must include the copyright notice and this
 Software License Agreement;
 b. Copies in binary form must include the copyright notice and this
 Software License Agreement in the documentation and/or other materials
 provided with the copy;
 c. Modified copies and works based on the Software must carry prominent
 notices stating that you changed specified portions of the Software.

 5. Portions of the Software resulted from work developed under a U.S. Government
 contract and are subject to the following license: the Government is granted
 for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable
 worldwide license in this computer software to reproduce, prepare derivative
 works, and perform publicly and display publicly.

 6. WARRANTY DISCLAIMER. THE SOFTWARE IS SUPPLIED "AS IS" WITHOUT WARRANTY
 OF ANY KIND. THE COPYRIGHT HOLDER, THE UNITED STATES, THE UNITED STATES
 DEPARTMENT OF ENERGY, AND THEIR EMPLOYEES: (1) DISCLAIM ANY WARRANTIES,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE OR NON-INFRINGEMENT,
 (2) DO NOT ASSUME ANY LEGAL LIABILITY OR RESPONSIBILITY FOR THE ACCURACY,
 COMPLETENESS, OR USEFULNESS OF THE SOFTWARE, (3) DO NOT REPRESENT THAT USE
 OF THE SOFTWARE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS, (4) DO NOT WARRANT
 THAT THE SOFTWARE WILL FUNCTION UNINTERRUPTED, THAT IT IS ERROR-FREE OR THAT
 ANY ERRORS WILL BE CORRECTED.

 7. LIMITATION OF LIABILITY. IN NO EVENT WILL THE COPYRIGHT HOLDER, THE
 UNITED STATES, THE UNITED STATES DEPARTMENT OF ENERGY, OR THEIR EMPLOYEES:
 BE LIABLE FOR ANY INDIRECT, INCIDENTAL, CONSEQUENTIAL, SPECIAL OR PUNITIVE
 DAMAGES OF ANY KIND OR NATURE, INCLUDING BUT NOT LIMITED TO LOSS OF PROFITS
 OR LOSS OF DATA, FOR ANY REASON WHATSOEVER, WHETHER SUCH LIABILITY IS ASSERTED
 ON THE BASIS OF CONTRACT, TORT (INCLUDING NEGLIGENCE OR STRICT LIABILITY), OR
 OTHERWISE, EVEN IF ANY OF SAID PARTIES HAS BEEN WARNED OF THE POSSIBILITY OF
 SUCH LOSS OR DAMAGES.
 The Software was developed at least in part by the University of Chicago,
 as Operator of Argonne National Laboratory (http://miranda.ctd.anl.gov:7123/).
 */

/*
Modified work: The original source code (NdtTests.java) comes from the NDT Android app
               that is available from http://code.google.com/p/ndt/.
               It's modified for the CalSPEED Android app by California
               State University Monterey Bay (CSUMB) on April 29, 2013.
*/


package gov.ca.cpuc.fieldtest.android;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import gov.ca.cpuc.fieldtest.android.AndroidUiServices.TextOutputAdapter;
import gov.ca.cpuc.fieldtest.android.TesterFragment.LatLong;
import gov.ca.cpuc.fieldtest.android.TesterFragment.NetworkLatLong;


class StandardTest implements Runnable {
    private TextOutputAdapter statistics, results, summary;
	private int runStatus;
    private String textResults;

    private final String server1;//west server
	private final String server2;//east server
	private final AndroidUiServices uiServices;
    private final String deviceId;
	private final Date date;

    private ExecCommandLine command;
	private LatLong testLatLong;
	private NetworkLatLong networkLatLong;
	private ProcessPing pingConnectivity;
	private ProcessPing pingStatsEast;
	private ProcessPing pingStatsWest;
    private static Double mosValue;
    private static Double mosValueEast;
    private ArrayList<ProcessIperf> tcpResultsWest;
    private ArrayList<ProcessIperf> tcpResultsEast;
    private ArrayList<ProcessIperf> udpResultsWest;
	private ArrayList<ProcessIperf> udpResultsEast;
    private Double saveLastNetworkLat;
	private Double saveLastNetworkLong;
	private Double saveLastGPSLat;
	private Double saveLastGPSLong;
    private String networkType;
    private String iperfDirectory;
    private String tcpPort;
	private NetworkAndDeviceInformation ndi;
    private StringBuilder testStatus;
    private ConfigurationTable configTable;
    private TestConfig prelimConfig;
    private TestConfig weakSignalConfig;
    private TestConfig westTcpConfig;
    private TestConfig eastTcpConfig;
    private TestConfig westUdpOneSecConfig;
    private TestConfig eastUdpOneSecConfig;
    private TestConfig westUdpFiveSecConfig;
    private TestConfig eastUdpFiveSecConfig;

    private static final String UP = "UP";
	private static final String DOWN = "DOWN";
    private static final String WEST = "WEST";
    private static final String EAST = "EAST";
    private final String TCP_WEST = "TCP_WEST";
    private final String TCP_EAST = "TCP_EAST";
    private final String UDP_WEST = "UDP_WEST";
    private final String UDP_EAST = "UDP_EAST";
    private final String PING_WEST = "PING_WEST";
    private final String PING_EAST = "PING_EAST";
    private final String PING_CHECK = "PING_CHECK";
    private final String SIGNAL_INFO = "SIGNAL_INFO";
    private final String PRELIM_WEST = "PRELIM_WEST";
    private final String WEAK_SIGNAL_WEST = "WEAK_SIGNAL_WEST";

    public String ip = "";
    private static Map<String, Double> WestUp = new HashMap<>();
	private static Map<String, Double> WestDown = new HashMap<>();
	private static Map<String, Double> EastUp = new HashMap<>();
	private static Map<String, Double> EastDown = new HashMap<>();
    private static String[] vid_summary;
    private static String[] vid_conference_summary;
    private static String[] vid_voip_summary;

    private static final String STREAMING = "streaming";
    private static final String CONFERENCE = "conference";
    private static final String VOIP = "voip";

    private ArrayList<Metric> allMetrics = new ArrayList<>();
    private HashSet<Integer> threadSet = new HashSet<>();
    private Map<Integer, String> threadDirection = new HashMap<>();
    private static HashMap<Integer, ArrayList<Metric>> metricsByThread = new HashMap<>();


    /*
	 * Initializes the network test thread.
	 *
	 * @param host hostname of the test server
	 *
	 * @param uiServices object for UI interaction
	 *
	 * @param networkType indicates the type of network, e.g. 3G, Wifi, Wired,
	 * etc.
	 * StandardTest(uiServices, rawData, deviceId, date,
                applicationFilesDir, connectionType, ndi, networkLatLong, myLatLong, s1, s2,
                TCPPort, UDPPort);
	 */
    StandardTest(AndroidUiServices uiServices, String textResults, String DeviceId, Date date,
                 String applicationFilesDir, String networkName, NetworkAndDeviceInformation ndi,
                 NetworkLatLong networkLatLong, LatLong testLatLong, String server1, String server2,
                 String TCPPort, String UDPPort) {
		this.server1 = server1;
		this.server2 = server2;
		this.uiServices = uiServices;
        this.deviceId = DeviceId;
		this.date = date;
        saveLastNetworkLat = 0.0;
		saveLastNetworkLong = 0.0;
		saveLastGPSLat = 0.0;
		saveLastGPSLong = 0.0;
		this.textResults = textResults;
        this.command = null;
		this.networkLatLong = networkLatLong;
		this.networkLatLong.getLatitudeLongitude(networkLatLong);
		this.testLatLong = testLatLong;
		this.testLatLong.getLatitudeLongitude(testLatLong);
        this.ndi = ndi;
        this.iperfDirectory = applicationFilesDir;
        this.tcpPort = TCPPort;
        this.networkType = networkName;
        this.pingConnectivity = null;
        this.configTable = new ConfigurationTable();
        this.weakSignalConfig = new TcpTestConfig(applicationFilesDir, server1, TCPPort,
                Constants.WEAK_SIGNAL_WINDOW_SIZE, Constants.WEAK_SIGNAL_THREAD_NUMBER,
                Constants.WEAK_SIGNAL_TEST_TIME);
        this.prelimConfig = new TcpTestConfig(applicationFilesDir, server1, TCPPort,
                Constants.PRELIM_WINDOW_SIZE, Constants.PRELIM_THREAD_NUMBER);
        this.westTcpConfig = new TcpTestConfig(applicationFilesDir, server1, TCPPort,
                Constants.DEFAULT_WINDOW_SIZE, Constants.DEFAULT_THREAD_NUMBER);
        this.eastTcpConfig = new TcpTestConfig(applicationFilesDir, server2, TCPPort,
                Constants.DEFAULT_WINDOW_SIZE, Constants.DEFAULT_THREAD_NUMBER);
        this.westUdpOneSecConfig = new UdpTestConfig(applicationFilesDir, server1, UDPPort,
                Constants.DEFAULT_UDP_LENGTH, Constants.DEFAULT_UDP_BANDWIDTH, 1);
        this.eastUdpOneSecConfig = new UdpTestConfig(applicationFilesDir, server2, UDPPort,
                Constants.DEFAULT_UDP_LENGTH, Constants.DEFAULT_UDP_BANDWIDTH, 1);
        this.westUdpFiveSecConfig = new UdpTestConfig(applicationFilesDir, server1, UDPPort,
                Constants.DEFAULT_UDP_LENGTH, Constants.DEFAULT_UDP_BANDWIDTH, 5);
        this.eastUdpFiveSecConfig = new UdpTestConfig(applicationFilesDir, server2, UDPPort,
                Constants.DEFAULT_UDP_LENGTH, Constants.DEFAULT_UDP_BANDWIDTH, 5);
        setupResultsObjects();
		statistics = new TextOutputAdapter(uiServices, UiServices.STAT_VIEW);
		results = new TextOutputAdapter(uiServices, UiServices.MAIN_VIEW);
		summary = new TextOutputAdapter(uiServices, UiServices.SUMMARY_VIEW);
	}

    private void setupResultsObjects() {
        pingStatsEast = new ProcessPing("Latency",uiServices);
        pingStatsWest = new ProcessPing("Latency",uiServices);
        tcpResultsWest = new ArrayList<>();
        tcpResultsEast = new ArrayList<>();
        udpResultsWest = new ArrayList<>();
        udpResultsEast = new ArrayList<>();
    }

    String[] getVideoCalc() {
        return getVideo(STREAMING);
    }

    String[] getVideoConferenceCalc() {
        return getVideo(CONFERENCE);
    }

    private String[] getVideo(String type) {
        String[] vidSummary = null;
        switch (type) {
            case STREAMING:
                vidSummary = vid_summary;
                break;
            case CONFERENCE:
                vidSummary = vid_conference_summary;
                break;
            case VOIP:
                vidSummary = vid_voip_summary;
                break;
        }
        if (vidSummary == null) {
            vidSummary = calcVideo(type);
        }
        if (vidSummary[2].equals("")) {
            vidSummary[2] = "N/A";
        }
        return vidSummary;
    }

    private void printLatLong() {
        String lat = "N/A";
        String lon ="N/A";
		testLatLong.getLatitudeLongitude(testLatLong);
        Double gpsLat = testLatLong.latitude;
        Double gpsLong = testLatLong.longitude;
        networkLatLong.getLatitudeLongitude(networkLatLong);
        Double networkLat = networkLatLong.latitude;
        Double networkLong = networkLatLong.longitude;
        if (gpsLat != 0.0 || gpsLong != 0.0) {
            lat = Double.toString(gpsLat);
            lon = Double.toString(gpsLong);
            uiServices.updateLatLong(gpsLat, gpsLong);
        } else if (networkLat != 0.0 || networkLong != 0.0) {
            lat = Double.toString(networkLat);
            lon = Double.toString(networkLong);
            uiServices.updateLatLong(networkLat, networkLong);
		}
        showResults("\nLatitude:" + lat);
        showResults("\nLongitude:" + lon);
		sumNewDistances();
    }

	private void sumNewDistances(){
		Location oldGPSLocation = new Location("");
		oldGPSLocation.setLatitude(saveLastGPSLat);
		oldGPSLocation.setLongitude(saveLastGPSLong);
		Location oldNetworkLocation = new Location("");
		oldNetworkLocation.setLatitude(saveLastNetworkLat);
		oldNetworkLocation.setLongitude(saveLastNetworkLong);
		if (testLatLong.valid){
			if ((saveLastGPSLat != 0) && (saveLastGPSLong != 0)){
				Location newGPSLocation = new Location("");
				newGPSLocation.setLatitude(testLatLong.latitude);
				newGPSLocation.setLongitude(testLatLong.longitude);
            }
			saveLastGPSLat = testLatLong.latitude;
			saveLastGPSLong = testLatLong.longitude;
		}
		if (networkLatLong.valid){
			if ((saveLastNetworkLat != 0)&& (saveLastNetworkLong != 0)){
				Location newNetworkLocation = new Location("");
				newNetworkLocation.setLatitude(networkLatLong.latitude);
				newNetworkLocation.setLongitude(networkLatLong.longitude);
            }
			saveLastNetworkLat = networkLatLong.latitude;
			saveLastNetworkLong = networkLatLong.longitude;
		}

	}

	public void run() {
		try {
			uiServices.onBeginTest();
			MOSCalculation.clearData();
            String checkConnectionMessage = "\nChecking Connectivity.....\n";
            handleMessage(checkConnectionMessage);
            uiServices.incrementProgress(1);
            uiServices.testSuccess(Constants.BLANK_CHECKBOX, "Connectivity Test");
			for (int i = 0; i < 3; i++) {
                testStatus = new StringBuilder("Connectivity Check");
                changeDisplayText(testStatus.toString());
				runStatus = pingTest(String.format(Constants.PING_COMMAND, server1, "4"),
                        null, pingConnectivity, true, PING_CHECK);
				if ((runStatus != Constants.PING_100_PERCENT_LOSS)
						&& (runStatus != Constants.RUNCOMMAND_INTERRUPT)) {
					break;
				}
				waiting(3);
			}
            if (runStatus == Constants.PING_100_PERCENT_LOSS) {
                uiServices.updateTestResult(Constants.RED_CHECKBOX, "Connectivity Check Failed");
                String pingConnectFailMessage = "\nConnectivity Test Failed--Exiting Test.\n";
                handleMessage(pingConnectFailMessage);
				runStatus = Constants.PING_CONNECTIVITY_FAIL;
				uiServices.setStatusText("No Network Connection.");
				Log.i("INFO", "No network connection 1");
                saveAllResults(false, false);
			} else if (runStatus == Constants.RUNCOMMAND_INTERRUPT) {
                uiServices.updateTestResult(Constants.RED_CHECKBOX, "Connectivity Check Failed");
                String connectTestFailMessage = "\nConnectivity Test Failed--Exiting Test.\n";
                handleMessage(connectTestFailMessage);
				runStatus = Constants.PING_CONNECTIVITY_FAIL;
			} else {
                Log.d("run prelim", "running prelims");
                runPreliminaryTests();
                printLatLong();
                handleMessage(Constants.TEST_ONE_MESSAGE);
                ProcessIperf tcpWestOne = new ProcessIperf(uiServices, Constants.TCP,
                        (TcpTestConfig) this.westTcpConfig);
                tcpTest(this.westTcpConfig.createIperfCommandLine(), tcpWestOne, WEST, TCP_WEST);
                tcpResultsWest.add(tcpWestOne);
                uiServices.incrementProgress(2);
				printLatLong();
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_TWO_MESSAGE);
                ProcessIperf tcpEastOne = new ProcessIperf(uiServices, Constants.TCP,
                        (TcpTestConfig) this.westTcpConfig);
                tcpTest(this.eastTcpConfig.createIperfCommandLine(), tcpEastOne, EAST, TCP_EAST);
                tcpResultsEast.add(tcpEastOne);
                printLatLong();
                uiServices.incrementProgress(2);

                handleMessage(Constants.TEST_THREE_MESSAGE);
                pingTest(String.format(Constants.PING_COMMAND, server2, "10"), null, pingStatsEast,
                        false, PING_EAST);
                printLatLong();
                uiServices.incrementProgress(2);
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_FOUR_MESSAGE);
                ProcessIperf tcpWestTwo = new ProcessIperf(uiServices, Constants.TCP,
                        (TcpTestConfig) this.westTcpConfig);
                tcpTest(this.westTcpConfig.createIperfCommandLine(), tcpWestTwo, WEST, TCP_WEST);
                tcpResultsWest.add(tcpWestTwo);
                uiServices.incrementProgress(2);
                printLatLong();
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_FIVE_MESSAGE);
                ProcessIperf tcpEastTwo = new ProcessIperf(uiServices, Constants.TCP,
                        (TcpTestConfig) this.westTcpConfig);
                tcpTest(this.eastTcpConfig.createIperfCommandLine(), tcpEastTwo, EAST, TCP_EAST);
                tcpResultsEast.add(tcpEastTwo);
                printLatLong();
                uiServices.incrementProgress(2);
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_SIX_MESSAGE);
                pingTest(String.format(Constants.PING_COMMAND, server1, "10"),
                        null, pingStatsWest, false, PING_WEST);
				printLatLong();
                uiServices.incrementProgress(2);

				handleMessage(Constants.TEST_SEVEN_MESSAGE);
                ProcessIperf udpWestOneSec = new ProcessIperf(uiServices, Constants.UDP,
                        (UdpTestConfig) this.eastUdpOneSecConfig);
				udpTest(this.westUdpOneSecConfig.createIperfCommandLine(),
                        udpWestOneSec, 3, UDP_WEST);
                udpResultsWest.add(udpWestOneSec);
                uiServices.incrementProgress(2);
				printLatLong();
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_EIGHT_MESSAGE);
                ProcessIperf udpEastOneSec = new ProcessIperf(uiServices, Constants.UDP,
                        (UdpTestConfig) this.eastUdpOneSecConfig);
				udpTest(this.eastUdpOneSecConfig.createIperfCommandLine(),
                        udpEastOneSec, 3, UDP_EAST);
                udpResultsEast.add(udpEastOneSec);
				printLatLong();
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_NINE_MESSAGE);
                ProcessIperf udpWestFiveSec = new ProcessIperf(uiServices, Constants.UDP,
                        (UdpTestConfig) this.eastUdpOneSecConfig);
                udpTest(this.westUdpFiveSecConfig.createIperfCommandLine(),
                        udpWestFiveSec, 1, UDP_WEST);
                udpResultsWest.add(udpWestFiveSec);
                printLatLong();
                uiServices.incrementProgress(2);
                uiServices.phaseComplete();

                handleMessage(Constants.TEST_TEN_MESSAGE);
                ProcessIperf udpEastFiveSec = new ProcessIperf(uiServices, Constants.UDP,
                        (UdpTestConfig) this.eastUdpOneSecConfig);
                udpTest(this.eastUdpFiveSecConfig.createIperfCommandLine(),
                        udpEastFiveSec, 1, UDP_EAST);
                udpResultsEast.add(udpEastFiveSec);
                printLatLong();
                uiServices.incrementProgress(2);

                handleMessage(Constants.TEST_ELEVEN_MESSAGE);
                captureSignalInfo();
                uiServices.incrementProgress(2);

                printLatLong();
                verifyResults();
                calculateSpeed();
                vid_summary = calcVideo(STREAMING);
                vid_conference_summary = calcVideo(CONFERENCE);
                vid_voip_summary = calcVideo(VOIP);
                Log.d("VideoMetrics", "VidSummary: " + Arrays.toString(vid_summary));
                Log.d("VideoMetrics", "VidConf: " + Arrays.toString(vid_conference_summary));
                Log.d("VideoMetrics", "Voip: " + Arrays.toString(vid_voip_summary));

                uiServices.incrementProgress(2);
				runStatus = saveAllResults(false, false);
			}
			// Finish the Test
			if (runStatus == Constants.RUNCOMMAND_INTERRUPT){
				uiServices.setStatusText("Test Interrupted.");
				uiServices.onTestInterrupt();
			}else if (runStatus == Constants.RUNCOMMAND_FAIL){
                uiServices.incrementProgress(1);
				//uiServices.setStatusText("Results Not Processed.");
			}else if (runStatus == Constants.PING_CONNECTIVITY_FAIL){
                uiServices.incrementProgress(20);
				uiServices.setStatusText("No Network Connection.");
				Log.i("INFO", "No network connection 2");
			} else {
				uiServices.incrementProgress(1);
				Log.d("RUN", "Test successful and completed");
				uiServices.setStatusText("Test Complete.");
			}

		} catch (InterruptedException e) {
            Log.d("Quit", "Thread was interrupted");
            Thread.currentThread().interrupt();
            uiServices.testSuccess(Constants.RED_CHECKBOX, "Quitting Operations...");
			printToSummary("\nStandard Test Interrupted...\n");
			uiServices.setStatusText("Test Interrupted");
			uiServices.onTestInterrupt();
			return;
		}
		uiServices.onEndTest();
	}

	private void runPreliminaryTests() {
        String prelimNotes = "\n..................................................................";
        prelimNotes += "\nPRELIM TEST NOTES\n\n";
        Log.d("CONFIGURATION", this.configTable.toString());
        int weakSignalAverage = 0;
        Log.i("NetworkType", this.networkType);
        if (this.networkType.equals(Constants.MOBILE)) {
            Map<String, Integer> weakSigPrelimTable = configTable.getWeakSignalPrelimTable();
            Log.d("PRELIM_TABLE", weakSigPrelimTable.toString());
            String weakSignalWindowSize = String.format("%dk",
                    weakSigPrelimTable.get(Constants.WINDOW_SIZE));
            this.weakSignalConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                    weakSignalWindowSize, weakSigPrelimTable.get(Constants.THREAD_NUMBER),
                    weakSigPrelimTable.get(Constants.TEST_TIME));
            prelimNotes += "Weak signal test configuration:";
            prelimNotes += configPrinter(weakSigPrelimTable);

            ProcessIperf weakSignalTest = new ProcessIperf(uiServices, Constants.TCP,
                    (TcpTestConfig) this.weakSignalConfig);
            Log.d("PRELIM_CMD_LINE", this.weakSignalConfig.createIperfCommandLine());
            try {
                prelimNotes += prelimTcpTest(this.weakSignalConfig.createIperfCommandLine(),
                        weakSignalTest, WEAK_SIGNAL_WEST, Constants.IPERF_WEAK_SIGNAL_TCP_TIMEOUT);
            } catch (Exception e) {
                Log.e("RunPrelim", e.toString());
            }
            prelimNotes += "Weak signal speed values are: \n";
            prelimNotes += Collections.singleton(weakSignalTest.getRollingDownloadValues());
            weakSignalAverage = determineQuality(weakSignalTest);
            prelimNotes += "\nWeak signal speed result is: ";
            prelimNotes += weakSignalAverage;
        }
        String tcpConfigResult = "";
        if (weakSignalAverage > 750 || (this.networkType.equals(Constants.WIFI))) {
            Map<String, Integer> prelimTable = configTable.getPrelimTable().get(Constants.MOBILE);
            String prelimWindowSize = String.format("%dk",
                    prelimTable.get(Constants.WINDOW_SIZE));
            this.prelimConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                    prelimWindowSize, prelimTable.get(Constants.THREAD_NUMBER),
                    prelimTable.get(Constants.TEST_TIME));
            prelimNotes += "\n\nPrelim test configuration:";
            prelimNotes += configPrinter(prelimTable);
            Log.d("PRELIM_CMD_LINE", this.prelimConfig.createIperfCommandLine());
            try {
                ProcessIperf tcpPrelim = new ProcessIperf(uiServices, Constants.TCP,
                        (TcpTestConfig) this.prelimConfig);
                long prelimTimeout = (long) prelimTable.get(Constants.TEST_TIME) * 3000;
                Log.d("PrelimTestTime", String.valueOf(prelimTimeout));
                prelimNotes += prelimTcpTest(this.prelimConfig.createIperfCommandLine(),
                        tcpPrelim, PRELIM_WEST, Constants.IPERF_PRELIM_TCP_TIMEOUT);
                prelimNotes += "Download speed values are: \n";
                prelimNotes += Collections.singleton(tcpPrelim.getRollingDownloadValues());
                if (tcpPrelim.getSuccess()) {
                    prelimNotes += "\nDownload speed result is: ";
                    prelimNotes += tcpPrelim.getDownloadSpeed();
                    tcpConfigResult += "\n\nIperf TCP Test params:";
                    tcpConfigResult += setTcpConfig(tcpPrelim.getDownloadSpeed());
                } else {
                    prelimNotes += "\nProbe test result failed, using default configuration.";
                    setDefaultTcpConfig();
                }
            } catch (Exception e) {
                Log.e("RunPrelim", e.toString());
            }
        } else {
            setDefaultTcpConfig();
        }
        prelimNotes += tcpConfigResult;
        prelimNotes += "\n..................................................................\n\n";
        textResults += prelimNotes;
    }

    private int determineQuality(ProcessIperf weakSignalTest) {
        StringBuilder prelimStatus = new StringBuilder();
        prelimStatus.append(getTestType(WEAK_SIGNAL_WEST)).append("\n");
        if (!weakSignalTest.getSuccess()) {
            prelimStatus.append("Iperf test failed");
            uiServices.updateTestResult(Constants.RED_CHECKBOX, prelimStatus.toString());
            return 0;
        } else{
            ArrayList<Float> downloadSpeeds = weakSignalTest.getRollingDownloadValues();
            Log.d("downloadspeeds", String.valueOf(Collections.singletonList(downloadSpeeds)));
            if (downloadSpeeds.size() < 2) {
                prelimStatus.append("Only less than two speeds found");
                uiServices.updateTestResult(Constants.RED_CHECKBOX, prelimStatus.toString());
                return 0;
            }
            downloadSpeeds.remove(0);
            downloadSpeeds.remove(0);
            Log.d("downloadspeeds", String.valueOf(Collections.singletonList(downloadSpeeds)));
            int sum = 0;
            for (Float downloadSpeed : downloadSpeeds) {
                sum += Math.floor(downloadSpeed);
            }
            int average = sum / downloadSpeeds.size();
            prelimStatus.append(String.format("Download speed: %d Kb/s", average));
            uiServices.updateTestResult(Constants.GREEN_CHECKBOX, prelimStatus.toString());
            return average;
        }
    }

    private void verifyResults() {
        for (ProcessIperf tcpIperfTest: tcpResultsWest) {
            tcpIperfTest.finishResults();
        }
        for (ProcessIperf tcpIperfTest: tcpResultsEast) {
            tcpIperfTest.finishResults();
        }
        for (ProcessIperf udpIperfTest: udpResultsWest) {
            Log.d("Verify", "west udp states are: " + String.valueOf(udpIperfTest.getState()));
            udpIperfTest.finishResults();
        }
        for (ProcessIperf udpIperfTest: udpResultsEast) {
            Log.d("Verify", "east udp states are: " + String.valueOf(udpIperfTest.getState()));
            udpIperfTest.finishResults();
        }
    }


    private void changeDisplayText(String type) {
        uiServices.setStatusText(getTestType(type));
    }

    private String getTestType(String type) {
        String testType;
        switch (type) {
            case TCP_WEST:
                testType = "California TCP Test";
                break;
            case TCP_EAST:
                testType =  "Virginia TCP Test";
                break;
            case UDP_WEST:
                testType = "California UDP Test";
                break;
            case UDP_EAST:
                testType = "Virginia UDP Test";
                break;
            case PING_WEST:
                testType = "California Ping Test";
                break;
            case PING_EAST:
                testType = "Virginia Ping Test";
                break;
            case PING_CHECK:
                testType = "Checking Connectivity";
                break;
            case SIGNAL_INFO:
                testType = "Getting Signal Information";
                break;
            case PRELIM_WEST:
                testType = "Probe Test";
                break;
            case WEAK_SIGNAL_WEST:
                testType = "Weak Signal Detection Test";
                break;
            default:
                testType = "";
        }
        return testType;
    }

	Integer saveAllResults(boolean isAborted, boolean uploadOnly) {
        Log.v("Save", "saving results twice?");
		uiServices.attemptingToUpload();
		Integer returnStatus = Constants.RUNCOMMAND_SUCCESS;
        if (isAborted) {
            showResults("\nQuitting Operations...\n");
        }
        try {
            showResults("\nSaving Results to sdcard...\n");
            statistics.append("\nSaving Results to sdcard...\n");
            printToSummary("\nSaving Results...\n");
            SaveResults localResult = new SaveResults(results, summary,
                    textResults, deviceId, date);
            if (!uploadOnly) {
                String status = localResult.saveResultsLocally();
                Log.d("SaveResults", "Save result status: " + status);

                if (status.indexOf("successfully") > 0) {
                    printToSummary("File successfully saved.\n");
                    uiServices.resultsSaved();
                    statistics.append("File successfully saved.\n");
                } else {
                    printToSummary(status);
                    statistics.append(status);
                    uiServices.resultsNotSaved();
                    uiServices.setStatusText("Results Not Saved.");
                    returnStatus = Constants.RUNCOMMAND_FAIL;
                }
            }

            printToSummary("\nAttempting Upload to Server...\n");
            statistics.append("\nAttempting Upload to Server...\n");

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    printToSummary("Upload Timeout. \n");
                    uiServices.setStatusText("Test Complete");
                    if (Constants.DEBUG)
                        Log.v("LAWDebug", "Upload Timeout");
                    runStatus = Constants.THREAD_RESULTS_NOT_UPLOADED;
                    uiServices.resultsNotUploaded();
                    uiServices.onEndTest();
                    this.cancel();
                }
            };
            timer.schedule(task, 60000);
            try {
                if (localResult.uploadAllFiles(summary, results)) {
                    Log.d("SaveAllFiles", "Upload successful");
                    timer.cancel();
                    printToSummary("All Files successfully uploaded.\n");
                    statistics.append("All Files successfully uploaded.\n");
                    uiServices.resultsUploaded();
                    localResult.clearErrorMessage();
                } else {
                    timer.cancel();
                    printToSummary("Upload Failed. Try again later.\n");
                    statistics.append("Upload Failed. Try again later.\n");
                    uiServices.resultsNotUploaded();
                    uiServices.setStatusText("Test Complete.");
                    if (Constants.DEBUG)
                        Log.v("debug", "Upload Failed");
                    statistics.append(localResult.getErrorMessage());
                    localResult.clearErrorMessage();
                    returnStatus = Constants.RUNCOMMAND_FAIL;
                }
            } catch (Exception ce) {
                Log.w("WARN", "Can't upload files: " + ce.getMessage());
                uiServices.resultsNotUploaded();
            }

		} catch (InterruptedException e) {
			Log.e("SavingAllResults", e.getMessage());
			uiServices.resultsNotUploaded();
		}
		return(returnStatus);
	}


    private void handleMessage(String message) {
        Log.d("Messenger", message);
        printToSummary(message);
        statistics.append(message);
        showResults(message);
    }

	private void printToSummary(String message) {
		summary.append(message);
	}

	private void showResults(String message) {
		results.append(message);
		textResults += message;
	}

    private String prelimTcpTest(String commandline, ProcessIperf iperfTest, String testName,
                              long timeout) {
        String resultOutput = null;
        Log.v(testName, commandline);
        testStatus = new StringBuilder();
        changeDisplayText(testName);
        testStatus.append(getTestType(testName)).append("\n");
        uiServices.testSuccess(Constants.BLANK_CHECKBOX, testStatus.toString());
        try {
            Log.v(testName, "Test timeout value: " + String.valueOf(timeout));
            command = new ExecCommandLine(commandline,
                    timeout, results, iperfTest, null, uiServices);
            resultOutput = command.runIperfCommand();
            if (command.commandTimedOut) {
                String message = "\nIperf timed out after " + timeout / 1000 + " seconds.\n";
                Log.d(testName, iperfTest.getMessage());
                testStatus.append(message);
                uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
                return message;
            }
        } catch (InterruptedException e) {
            Log.i(testName, "Interrupt Exception");
            testStatus.append("Test interrupted");
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            return null;

        } catch (TimeoutException e) {
            String message = "\nIperf timed out after " + timeout / 1000 + " seconds.\n";
            Log.i(testName, message);
            testStatus.append(message);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            return null;
        }
        finally {
            iperfTest.finishResults();
        }
        if (Thread.currentThread().isInterrupted()) {
            Log.i(testName, "Interrupt Exception");
            testStatus.append("Test interrupted");
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            return null;
        }
        if (iperfTest.getSuccess()) {
            testStatus.append(String.format("Download speed: %d Kb/s",
                    Math.round(iperfTest.getDownloadSpeed())));
            uiServices.updateTestResult(Constants.GREEN_CHECKBOX, testStatus.toString());
        } else {
            testStatus.append("Iperf test unsuccessful");
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            Log.d(testName, "Iperf test unsuccessful");
            Log.d(testName, iperfTest.getErrorMessage());
        }
        uiServices.clearProcessHandle();
        return resultOutput;
    }

    private String setTcpConfig(Float downloadSpeed) {
        Map<String, Map<Integer, Map<String, Integer>>> table = configTable.getTable();
        Map<Integer, Map<String, Integer>> networkTable = table.get(this.networkType);
        Map<String, Integer> tcpConfig = null;
        for (Object key: networkTable.keySet()) {
            if (downloadSpeed < Float.valueOf((Integer) key)) {
                tcpConfig = networkTable.get(key);
                Log.d("TCP_TEST_CONFIG", configPrinter(tcpConfig));
                break;
            }
        }
        if (tcpConfig != null) {
            String windowSizeFormat = String.format("%dk", tcpConfig.get(Constants.WINDOW_SIZE));
            this.westTcpConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                    windowSizeFormat, tcpConfig.get(Constants.THREAD_NUMBER),
                    tcpConfig.get(Constants.TEST_TIME));
            this.eastTcpConfig = new TcpTestConfig(this.iperfDirectory, server2, this.tcpPort,
                    windowSizeFormat, tcpConfig.get(Constants.THREAD_NUMBER),
                    tcpConfig.get(Constants.TEST_TIME));
            return configPrinter(tcpConfig);
        } else {
            setDefaultTcpConfig();
            Map<String, Map<String, Integer>> defaultsTable = configTable.getDefaultsTable();
            Map<String, Integer> defaultConfig = defaultsTable.get(this.networkType);
            return configPrinter(defaultConfig);
        }
    }

    private String setDefaultTcpConfig() {
        Map<String, Map<String, Integer>> defaultsTable = configTable.getDefaultsTable();
        Map<String, Integer> defaultConfig = defaultsTable.get(this.networkType);
        Log.d("DefaultConfig", String.format("Using default config for TCP tests:\n %s",
                configPrinter(defaultConfig)));
        String windowSizeFormat =
                String.format("%dk", defaultConfig.get(Constants.WINDOW_SIZE));
        this.westTcpConfig = new TcpTestConfig(this.iperfDirectory, server1, this.tcpPort,
                windowSizeFormat, defaultConfig.get(Constants.THREAD_NUMBER),
                defaultConfig.get(Constants.TEST_TIME));
        this.eastTcpConfig = new TcpTestConfig(this.iperfDirectory, server2, this.tcpPort,
                windowSizeFormat, defaultConfig.get(Constants.THREAD_NUMBER),
                defaultConfig.get(Constants.TEST_TIME));
        return configPrinter(defaultConfig);
    }

    private String configPrinter(Map<String, Integer> config) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n{\n");
        for (String key: config.keySet()) {
            sb.append("    ").append(key).append(": ");
            if (key.equals("windowSize") ){
                sb.append(String.valueOf(config.get(key) * 2));
            } else {
                sb.append(String.valueOf(config.get(key)));
            }
            sb.append("\n");
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    private String mapToString(Map<String, Integer> config) {
        StringBuilder sb = new StringBuilder();
        for (String key: config.keySet()) {
            sb.append(key).append(": ").append(String.valueOf(config.get(key)));
            sb.append(", ");
        }
        return sb.toString();
    }

    private int tcpTest(String commandline, ProcessIperf iperfTest, String server, String testName)
            throws InterruptedException {
        Log.v(testName, commandline);
        testStatus = new StringBuilder();
        testStatus.append(getTestType(testName));
        testStatus.append("\n");
        changeDisplayText(testName);
        uiServices.testSuccess(Constants.BLANK_CHECKBOX, testStatus.toString());
        try {
            Log.v(testName, "Test timeout value: " + String.valueOf(Constants.IPERF_TCP_TIMEOUT));
            command = new ExecCommandLine(commandline,
                    Constants.IPERF_TCP_TIMEOUT, results, iperfTest, null, uiServices);
            String commandOutput = command.runIperfCommand();
            textResults += commandOutput;
            parseOutput(textResults, server);
            statistics.append("\n" + commandOutput);
            if (command.commandTimedOut) {
                String message = "\nIperf timed out after "
                        + Constants.IPERF_TCP_TIMEOUT / 1000 + " seconds.\n";
                handleMessage(message);
                if (!iperfTest.finishedUploadTest){
                    iperfTest.setMessage("Upload test not finished");
                }
                testStatus.append(Constants.FAILED_TCP_LINE);
                uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
                uiServices.clearProcessHandle();
				return Constants.RUNCOMMAND_FAIL;
            }
        } catch (InterruptedException e) {
            Log.d("Quit", "Interrupting: " + testName);
            testStatus.append(Constants.FAILED_TCP_LINE);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            uiServices.clearProcessHandle();
            throw new InterruptedException();

        } catch (TimeoutException e) {
            String message = "\nIperf timed out after " + Constants.IPERF_TCP_TIMEOUT / 1000
                    + " seconds.\n";
            handleMessage(message);
            uiServices.clearProcessHandle();
            testStatus.append(Constants.FAILED_TCP_LINE);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            return Constants.RUNCOMMAND_FAIL;
        }
        finally {
			iperfTest.finishResults();
		}
		if (Thread.currentThread().isInterrupted()) {
            Log.d("Quit", "Interruption called from end: " + testName);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            throw new InterruptedException();
        }
        if (iperfTest.getSuccess()) {
			testStatus.append(String.format(Constants.SUCCESS_TCP_LINE,
					iperfTest.getUploadSpeed(),
					iperfTest.getDownloadSpeed()));
			uiServices.updateTestResult(Constants.GREEN_CHECKBOX, testStatus.toString());
		} else {
			testStatus.append(Constants.FAILED_TCP_LINE);
			uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
		}
        uiServices.clearProcessHandle();
        return Constants.RUNCOMMAND_SUCCESS;
    }

    private int pingTest(String commandline, ProcessIperf iperfTest, ProcessPing pingTest,
                         boolean isPingCheck, String testName) throws InterruptedException {
        Log.v("ping_command", commandline);
        String commandOutput;
        changeDisplayText(testName);
        if (!isPingCheck) {
            uiServices.testSuccess(Constants.BLANK_CHECKBOX, "Ping Test");
        }
        try {
            ExecCommandLine command = new ExecCommandLine(commandline, Constants.PING_TIMEOUT,
                    results, iperfTest, pingTest, uiServices);
            commandOutput = command.runCommand();
            showResults("\n" + commandOutput);
            statistics.append("\n" + commandOutput);
            if (command.commandTimedOut) {
                String message = "\nPing timed out after "
                        + Constants.PING_TIMEOUT / 1000 + " seconds.\n";
                handleMessage(message);
                if (!isPingCheck) {
                    pingTest.SetPingFail("Test Timed Out.");
                    uiServices.updateTestResult(Constants.RED_CHECKBOX, "Ping Test Failed");
                }
                return Constants.RUNCOMMAND_FAIL;
            }
        } catch (InterruptedException e) {
            if (!isPingCheck) {
                uiServices.updateTestResult(Constants.RED_CHECKBOX, "Ping Test Failed");
            } else {
                uiServices.updateTestResult(Constants.RED_CHECKBOX, "Connectivity Test Failed");
            }
            Log.d("Quit", "Interrupting: " + testName);
            throw new InterruptedException();
        } finally {
            if (!isPingCheck) {
                pingTest.setPingStatus();
            }
        }
        if (isPingCheck) {
            if ((commandOutput.contains(Constants.PING_100_PERCENT))
                    || (!commandOutput.contains("rtt min"))) {
                return (Constants.PING_100_PERCENT_LOSS);
            } else {
                uiServices.updateTestResult(Constants.GREEN_CHECKBOX, "Connectivity Test Success");
                return (Constants.RUNCOMMAND_SUCCESS);
            }
        } else {
            if (Thread.currentThread().isInterrupted()) {
                Log.d("Quit", "Interruption called from end: " + testName);
                uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
                throw new InterruptedException();
            }

            if (pingTest.success) {
				String testStatus = getTestType(testName) +
						"\n" +
						String.format(Constants.SUCCESS_PING_LINE,
								pingTest.getAverage(), pingTest.getLoss());
				uiServices.updateTestResult(Constants.GREEN_CHECKBOX, testStatus);
			} else {
				uiServices.updateTestResult(Constants.RED_CHECKBOX, Constants.FAILED_PING_LINE);
			}
            return (Constants.RUNCOMMAND_SUCCESS);
        }
    }

	private int udpTest(String commandline, ProcessIperf iperfTest, int numTests, String testName)
            throws InterruptedException {
        Log.v("udp_command", commandline);
        changeDisplayText(testName);
        testStatus = new StringBuilder();
        testStatus.append(getTestType(testName));
        testStatus.append("\n");
        uiServices.testSuccess(Constants.BLANK_CHECKBOX, testStatus.toString());
        try {
            for (int i = 0; i < numTests; i++) {
                if (numTests > 1) {
                    String startMessage = "\nStarting UDP 1 second Test #" + (i + 1) + "\n";
                    handleMessage(startMessage);
                }
                command = new ExecCommandLine(commandline,
                        Constants.IPERF_UDP_TIMEOUT, results, iperfTest, null, uiServices);
                String commandOutput = command.runIperfCommand();
                textResults += commandOutput;
                statistics.append("\n" + commandOutput);
                if (command.commandTimedOut) {
                    String message = "\nIperf timed out after " + Constants.IPERF_UDP_TIMEOUT / 1000
                            + " seconds.\n";
                    handleMessage(message);
                    iperfTest.setMessage("UDP test timed out");
                    testStatus.append(Constants.FAILED_UDP_LINE);
                    uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
                }
                waiting(1);
            }
        } catch (InterruptedException e) {
            Log.d("Quit", "Interrupting: " + testName);
            testStatus.append(Constants.FAILED_UDP_LINE);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            uiServices.clearProcessHandle();
            throw new InterruptedException();

        } catch (TimeoutException e) {
            results.append("\n" + e);
            statistics.append("\n" + e);
            uiServices.clearProcessHandle();
            testStatus.append(Constants.FAILED_UDP_LINE);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            return (Constants.RUNCOMMAND_FAIL);
        } finally {
            command = null;
            iperfTest.finishResults();
        }
        if (Thread.currentThread().isInterrupted()) {
            Log.d("Quit", "Interruption called from end: " + testName);
            uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
            throw new InterruptedException();
        }
        if (iperfTest.getSuccess()) {
			testStatus.append(String.format(Constants.SUCCESS_UDP_LINE, iperfTest.getJitter(),
                    iperfTest.getLoss()));
			uiServices.updateTestResult(Constants.GREEN_CHECKBOX, testStatus.toString());
		} else {
			testStatus.append(Constants.FAILED_UDP_LINE);
			uiServices.updateTestResult(Constants.RED_CHECKBOX, testStatus.toString());
		}
		return Constants.RUNCOMMAND_SUCCESS;
	}

	private void captureSignalInfo() {
        changeDisplayText(SIGNAL_INFO);
        showResults(String.format("Signal Strength: %d dB\n", ndi.getSignalStrength()));
        showResults(String.format("Signal Noise Ratio: %d\n", ndi.getSnr()));
        showResults(String.format("Location Area Code: %d\n", ndi.getLocationAreaCode()));
        showResults(String.format("Cell Tower ID: %d\n", ndi.getCellTowerId()));
    }

	private static void waiting(int n) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}

    /**
     * Video Metric Calculations and relevant methods.
     * method to send commandOutput to for parsing.
     */
    private void parseOutput(String commandOutput, String server) {
        List<String> rawData = Arrays.asList(commandOutput.split("\n"));
        for (int k = 0; k < rawData.size();k++) {
            addLineToMetric(rawData.get(k), server);
        }
        Collections.sort(allMetrics, new MetricComparator());
        for (Integer thread: threadSet) {
            metricsByThread.put(thread, new ArrayList<Metric>());
        }
    }


    /**
     * Put all the output into a List. This method filters through each line
     * and finds the thread number, speed, and time range. Given how many
     * times the thread number appears, the direction changes from up to down
     * and vice versa.
     * This skips other lines and lines that contain [SUM]
     * @param line A line from the raw data output
     * @param server Either the East or West server passed from the runCommand method
     */
    private void addLineToMetric(String line, String server) {
        List<String> elements = Arrays.asList(line.split("\\s+"));
        int speed, time, threadID;
        if ((elements.size() >= 8) && elements.contains("[")) {
            threadID = elements.indexOf("[") + 1;
            Integer validThreadID = Integer.valueOf(elements.get(threadID).replace("]", ""));
            if (elements.contains("local") && elements.contains("port")) {
                changeDirection(validThreadID);
            }
            if (elements.contains("Kbits/sec")) {
                speed = elements.indexOf("Kbits/sec") - 1; //find index before speed measurement
                time = elements.indexOf("sec") - 1; //find index before "sec"
                String timeValue = elements.get(time);
                String timeRangeFirst, timeRangeSecond;
                if (timeValue.contains("-")) {
                    timeRangeFirst = timeValue.split("-")[0];
                    timeRangeSecond = timeValue.split("-")[1];
                } else {
                    timeRangeFirst = elements.get(elements.indexOf("sec") - 2).replace("-", "");
                    timeRangeSecond = timeValue;
                }
                if (speed >= 0 && time >= 0
                        && elements.indexOf("[SUM]") == -1
                        && isValidTime(timeRangeFirst, timeRangeSecond)) {
                    String validSpeed = elements.get(speed);
                    String validTime = formatTimeForMetric(elements.get(time));
                    Metric validMetric = new Metric(validThreadID,
                            Double.valueOf(validSpeed), validTime,
                            threadDirection.get(validThreadID), server);
                    threadSet.add(validThreadID);
                    allMetrics.add(validMetric);
                }
            }
        }
    }

    private void changeDirection(Integer threadNumber) {
        if (threadDirection.get(threadNumber) == null) {
            threadDirection.put(threadNumber, UP);
        } else if (threadDirection.get(threadNumber).equals(UP)) {
            threadDirection.put(threadNumber, DOWN);
        } else if (threadDirection.get(threadNumber).equals(DOWN)) {
            threadDirection.put(threadNumber, UP);
        }
    }

    private void calculateSpeed() {
        for (Metric metric: allMetrics) {
            Log.v("MetricsDEBUG", metric.toString());
            Map<String, Double> mapToUpdate = null;
            if (metric.getServer().equals(WEST)) {
                if (metric.getDirection().equals(UP)) {
                    mapToUpdate = WestUp;
                } else if (metric.getDirection().equals(DOWN)) {
                    mapToUpdate = WestDown;
                }
            } else if (metric.getServer().equals(EAST)) {
                if (metric.getDirection().equals(UP)) {
                    mapToUpdate = EastUp;
                } else if (metric.getDirection().equals(DOWN)) {
                    mapToUpdate = EastDown;
                }
            }
            if (mapToUpdate == null) {
                Log.e("CalculateSpeed", "Didn't find a valid metric: " + metric.toString());
                throw new NullPointerException();
            }
            String time = metric.getTimeRange();
            if (Double.parseDouble(time) < 11.0) {
                Double speed = metric.getSpeed();
                Double oldSpeed = mapToUpdate.get(time);
                if (oldSpeed == null) {
                    oldSpeed = 0.0;
                }
                mapToUpdate.put(time, oldSpeed + speed);

                ArrayList<Metric> valuesForThread = metricsByThread.get(metric.getThreadID());
                valuesForThread.add(metric);
            }
        }
    }

    private boolean isValidTime(String timeOne, String timeTwo) {
        Log.v("ValidatingTime", "Comparing times: " + timeOne + " and " + timeTwo);
        if (timeOne == null || timeTwo == null) {
            Log.e("ValidatingTime", "Got invalid times");
            return false;
        } else {
            if ((timeTwo.length() == 3) && timeTwo.contains("0") && timeOne.contains("0")) {
                return true;
            } else {
                Double firstTime = Double.valueOf(timeOne);
                Double secondTime = Double.valueOf(timeTwo);
                return secondTime - firstTime == 1.0;
            }
        }
    }

    private String formatTimeForMetric(String rawTime) {
        if (rawTime.contains("-")) {
            return rawTime.split("-")[1];
        } else {
            return rawTime;
        }
    }


    static void resetScores(){
		Map<String, Double> mp = WestUp;
		Iterator it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			mp.put((String) pair.getKey(), 0.0);
			it.remove(); // avoids a ConcurrentModificationException
		}
		mp = WestDown;
		it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			mp.put((String) pair.getKey(), 0.0);
			it.remove(); // avoids a ConcurrentModificationException
		}
		mp = EastUp;
		it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			mp.put((String) pair.getKey(), 0.0);
			it.remove(); // avoids a ConcurrentModificationException
		}
		mp = EastDown;
		it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			mp.put((String) pair.getKey(), 0.0);
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

    private static String[] calcVideo(String type) {
        switch (type) {
            case STREAMING:
                return calcVideo();
            case CONFERENCE:
                return calcConference();
            case VOIP:
                return calcVoip();
            default:
                return new String[]{"", "", ""};
        }
    }


    private static String[] calcVideo() {
        int downHD, downSD, downLS, upHD, upSD, upLS;
        downHD = downSD = downLS = upHD = upSD = upLS = 0;
        vid_summary = new String[3];
        Map mp = WestDown;
        Iterator it = mp.entrySet().iterator();
        int counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry) it.next();
            if ((Double) pair.getValue() > 2500) {
                downHD++;
            } else if ((Double) pair.getValue() > 700)
                downSD++;
            else
                downLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        mp = WestUp;
        it = mp.entrySet().iterator();
        counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry) it.next();
            if ((Double) pair.getValue() > 2500)
                upHD++;
            else if ((Double) pair.getValue() > 700)
                upSD++;
            else
                upLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        String videoDetails = "West [Down] HD: " + downHD
                + ", SD: " + downSD
                + ", LS: " + downLS;
        vid_summary[0] = videoDetails;
        vid_summary[1] = "West [Up] HD: " + upHD
                + ", SD: " + upSD
                + ", LS : " + upLS;
        Log.d("TestStreaming", "Download | HD: " + downHD + ", SD: " + downSD + ", LD: " + downLS);
        if (mosValue == null || mosValue <= 0) {
            vid_summary[2] = "N/A";
        } else if (!hasTenValues(WEST, DOWN)) {
            vid_summary[2] = "N/A";
        } else if (downHD >= 9) {
            //return "High Definition";
            vid_summary[2] = "HD";
        } else if ((downHD + downSD) >= 9) {
            //return "Standard Definition";
            vid_summary[2] = "SD";
        } else if (((downHD + downSD + downLS) >= 10)) {
            //return "Low Service";
            vid_summary[2] = "LD";
        } else {
            vid_summary[2] = "N/A";
        }
        Log.d("TestStreaming", "Final Result is: " +  vid_summary[2]);
        return vid_summary;
    }

    private static String[] calcConference(){
        int upHD, upSD, upLS, downHD, downSD, downLS;
        upHD = upSD = upLS = downHD = downSD = downLS = 0;
        vid_conference_summary = new String[3];
        Map<String,Double> mp = EastUp;
        Iterator it = mp.entrySet().iterator();
        int counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry)it.next();
            if ((Double)pair.getValue() > 2500)
                upHD++;
            else if ((Double)pair.getValue() > 700)
                upSD++;
            else
                upLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        mp = EastDown;
        it = mp.entrySet().iterator();
        counter = 0;
        while (it.hasNext()) {
            if (counter == 10) {
                break;
            }
            Map.Entry pair = (Map.Entry)it.next();
            if ((Double)pair.getValue() > 2500)
                downHD++;
            else if ((Double)pair.getValue() > 700)
                downSD++;
            else
                downLS++;
            counter++;
            it.remove(); // avoids a ConcurrentModificationException
        }

        String conferenceDetails = "East [Up] HD: " + upHD
                + ", SD: " + upSD
                + ", LS: " + upLS;
        vid_conference_summary[0] = conferenceDetails;
        vid_conference_summary[1] = "East [Down] HD: " + downHD
                + ", SD: " + downSD
                + ", LS: " + downLS;
        Log.d("TestConference", "Download | HD: " + downHD + ", SD: " + downSD + ", LD: "
                + downLS + "\nUpload | HD: " + upHD + ", SD: " + upSD + ", LD: " + upLS);
        if (mosValue == null || mosValue <= 0) {
            vid_conference_summary[2] = "N/A";
        } else if (!hasTenValues(EAST, DOWN) && !hasTenValues(EAST, UP)) {
            vid_conference_summary[2] = "N/A";
        } else if (mosValue < 4.0) {
            vid_conference_summary[2] = "LD";
        } else if (downHD >= 9 && upHD >= 9) {
            vid_conference_summary[2] = "HD";
        } else if (upHD + upSD >= 9 && downHD + downSD >= 9) {
            vid_conference_summary[2] = "SD";
        } else if (upHD + upSD + upLS >= 10 && downHD + downSD + downLS >= 10) {
            vid_conference_summary[2] = "LD";
        } else {
            vid_conference_summary[2] = "N/A";
        }
        Log.d("TestConference", "Final Result is: " +  vid_conference_summary[2]);
        return vid_conference_summary;
    }

    /**
     * Translate the Voice over IP value based on the MOS value from the East server.
     * @return String based on the MOS value range
     */
    private static String[] calcVoip(){
        if (mosValueEast == null || mosValueEast <= 0.0) {
            vid_voip_summary = new String[]{"", "", "N/A"};
        } else if (mosValueEast < 3.0) {
            vid_voip_summary = new String[]{"", "", "Poor"};
        } else if (mosValueEast < 4.0) {
            vid_voip_summary = new String[]{"", "", "Fair"};
        } else {
            vid_voip_summary = new String[]{"", "", "Good"};
        }
        Log.d("TestVoIP", "Final Result is: " +  vid_voip_summary[2] + " with MOS: "
                + mosValueEast);
        return vid_voip_summary;
    }

    private static boolean hasTenValues(String server, String direction) {
        for (Map.Entry<Integer, ArrayList<Metric>> entry: metricsByThread.entrySet()) {
            ArrayList<Metric> matchingMetrics = new ArrayList<>();
            for (Metric metric: entry.getValue()) {
                if ((metric.getServer()).equals(server)
                        && metric.getDirection().equals(direction)) {
                    matchingMetrics.add(metric);
                }
            }
            Log.v("TenValues", "Thread: " + entry.getKey() + " | Size: " + matchingMetrics.size());
            if (matchingMetrics.size() < 10) {
                Log.i("TenValues", "Found an entry less than 10\nThread: " + entry.getKey()
                        + " Size: " + matchingMetrics.size());
                return false;
            }
        }
        return true;
    }

}