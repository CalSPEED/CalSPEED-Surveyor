/*
Copyright (c) 2013, California State University Monterey Bay (CSUMB).
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

import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class ProcessIperf {
    private final String testType;
    private boolean success;
    private final boolean isFinished;
    private Float phase1UploadResult;
    private Float phase1DownloadResult;
    private Integer udpState;
    private Double udpLoss;
    private Double udpJitter;
    private Float uploadSpeed;
    private String uploadMessage;
    private Float downloadSpeed;
    private String downloadMessage;
    private Float[] rollingUploadSpeed;
    private Integer[] rollingUploadCount;
    private Boolean[] rollingUploadDone;
    private Float[] rollingDownloadSpeed;
    private Integer[] rollingDownloadCount;
    private Boolean[] rollingDownloadDone;
    private ArrayList<Float> downloadSpeedValues;
    Boolean finishedUploadTest;

    private Integer[] iperfThreadState;
    private Set<Integer> threadNumSet = new HashSet<>();
    private Set<Integer> uploadSet = new HashSet<>();
    private Set<Integer> downloadSet = new HashSet<>();
    private HashMap<Integer, Integer> doneThreads = new HashMap<>();
    private AndroidUiServices uiServices;
    private Map<Integer, Integer> threadNumMap = new HashMap<>();
    private ArrayList<Float> uploadValues = new ArrayList<>();
    private ArrayList<Float> downloadValues = new ArrayList<>();
    private Integer threadNumber;
    private String message = "";
    private String errorMessage;
    private String logger = this.getClass().getName();


    private ProcessIperf(AndroidUiServices uiServices, String testType) {
        this.testType = testType;
        this.success = false;
        this.uiServices = uiServices;
        this.threadNumber = Constants.DEFAULT_THREAD_NUMBER;
        isFinished = false;
        this.errorMessage = "";
    }

    ProcessIperf(AndroidUiServices uiServices, String testType, UdpTestConfig testConfig) {
        this(uiServices, testType);
        this.threadNumber = Constants.DEFAULT_THREAD_NUMBER;
        this.udpLoss = 0.0;
        this.udpJitter = 0.0;
        this.udpState = 0;
    }

    ProcessIperf(AndroidUiServices uiServices, String testType, TcpTestConfig testConfig) {
        this(uiServices, testType);
        this.threadNumber = testConfig.getThreadNumber();
        finishedUploadTest = false;
        rollingUploadSpeed = new Float[testConfig.getThreadNumber()];
        rollingUploadCount = new Integer[testConfig.getThreadNumber()];
        rollingUploadDone = new Boolean[testConfig.getThreadNumber()];
        rollingDownloadSpeed = new Float[testConfig.getThreadNumber()];
        rollingDownloadCount = new Integer[testConfig.getThreadNumber()];
        rollingDownloadDone = new Boolean[testConfig.getThreadNumber()];
        downloadSpeedValues = new ArrayList<>();
        this.iperfThreadState = new Integer[testConfig.getThreadNumber()];
        for (int i = 0; i < iperfThreadState.length; i++) {
            iperfThreadState[i] = 0;
        }
        for (int i = 0; i < testConfig.getThreadNumber(); i++) {
            rollingUploadSpeed[i] = 0.0f;
            rollingUploadCount[i] = 0;
            rollingUploadDone[i] = false;
            rollingDownloadSpeed[i] = 0.0f;
            rollingDownloadCount[i] = 0;
            rollingDownloadDone[i] = false;
        }
        this.uploadSpeed = 0.0f;
        this.downloadSpeed = 0.0f;
        phase1UploadResult = 0.0f;
        phase1DownloadResult = 0.0f;
        uploadMessage = "Upload Speed";
        downloadMessage = "Download Speed";
    }

    String getTestType() {
        return this.testType;
    }

    Double getLoss() {
        return this.udpLoss;
    }

    Double getJitter() {
        return this.udpJitter;
    }

    int getState() {
        return this.udpState;
    }

    Float getDownloadSpeed() {
        return this.downloadSpeed;
    }

    ArrayList<Float> getRollingDownloadValues() {
        return this.downloadSpeedValues;
    }

    Float getUploadSpeed() {
        return this.uploadSpeed;
    }

    String getErrorMessage() {
        return this.errorMessage;
    }

    boolean getSuccess() {
        return success;
    }

    void setMessage(String message) {
        this.message += "\n" + message;
    }

    String getMessage() {
        return this.message;
    }

    private static String formatFloatString(String value) {
        Float flAverage = Float.valueOf(value);
        NumberFormat numberFormat = new DecimalFormat("#.0");
        return (numberFormat.format(flAverage));
    }

    private Boolean allDownloadThreadsDone() {
        Boolean allDone = true;
        for (int i = 0; i < this.threadNumber; i++) {
            Log.v("AllDownloadDone", String.valueOf(Collections.singletonList(threadNumSet)));
            for (Integer threadNum : threadNumSet) {
                if (threadNumMap.get(threadNum) == null) {
                    break;
                } else {
                    if (i == threadNumMap.get(threadNum)) {
                        Log.v("AllDownloadDone", String.format("Thread index %d is thread num %d - value is %b", i, threadNum, rollingDownloadDone[i]));
                    }
                }
            }
            if (!rollingDownloadDone[i]) {
                allDone = false;
                break;
            }
        }
        return (allDone);
    }

    private Boolean allUploadThreadsDone() {
        Boolean allDone = true;
        for (int i = 0; i < this.threadNumber; i++) {
            if (!rollingUploadDone[i]) {
                allDone = false;
                break;
            }
        }
        return (allDone);
    }

    private void setIperfUploadSpeedOnly() {
        if (allUploadThreadsDone()) {
            uiServices.stopUploadTimer();
            finishedUploadTest = true;
            Log.d("setIperfUploadSpeedOnly", "in SetIPerfUploadSpeedOnly uploadspeed=" +
                    uploadSpeed.toString());
            uiServices.startDownloadTimer();
        }
    }

    private void setIperfDownloadSpeed() {
        if (allDownloadThreadsDone()) {
            uiServices.stopDownloadTimer();
            uiServices.setResults(Constants.THREAD_WRITE_DOWNLOAD_DATA, downloadMessage,
                    formatFloatString(downloadSpeed.toString()), true, true);
            Log.d("setIperfDownloadSpeed", "in setIperfDownloadSpeed downloadspeed=" +
                    uploadSpeed.toString());
        }
    }

    private void parseUDPLine(String line) {
        int indexStart;
        int indexEnd;
        switch (udpState) {
            case 0:
                indexStart = line.indexOf("Server Report");
                if (indexStart != -1) {
                    Log.d("UDP State", "Found Server Report in line, incrementing state to 1");
                    udpState = 1;
                }
                break;
            case 1:
                indexStart = line.indexOf("/sec");
                if (indexStart != -1) {
                    indexEnd = line.indexOf("ms", indexStart);
                    if (indexEnd != -1) {
                        udpJitter = Double.parseDouble(line.substring(indexStart + 5, indexEnd - 1));
                        indexStart = line.indexOf("(");
                        indexEnd = line.indexOf(")", indexStart);
                        udpLoss = Double.parseDouble(line.substring(indexStart + 1, indexEnd - 1));
                        Log.d("udp_loss", "udp udpLoss: " + udpLoss);
                        MOSCalculation.addUDPLoss(udpLoss);
                        Log.d("UDP State", "Found udp loss, incrementing state to 2");
                        udpState = 2;
                    }
                }
                break;
            default:
                break;
        }
    }

    private Float getTCPBitsPerSec(String line) {
        int indexStart;
        int indexEnd;
        float currentSpeed = 0.0f;

        indexStart = line.indexOf("KBytes");
        if (indexStart != -1) {
            indexEnd = line.indexOf("Kbits/sec");
            String upSpeed = line.substring(indexStart + 7, indexEnd);
            try {
                currentSpeed = Float.parseFloat(upSpeed);
                if (currentSpeed > Constants.IPERF_BIG_NUMBER_ERROR) {
                    currentSpeed = 0.0f;
                }
                return (currentSpeed);
            } catch (Exception e) {
                return (0.0f);
            }
        } else {
            return (currentSpeed);
        }
    }

    private void displayUploadRollingAverage() {
        Float rollingAvg = phase1UploadResult + uploadSpeed; // phase1UploadResult is
        // 0 for phase 1
        try {
            for (int i = 0; i < this.threadNumber; i++) {
                if (!rollingUploadDone[i]) {
                    if (rollingUploadCount[i] != 0) {
                        rollingAvg += rollingUploadSpeed[i] / rollingUploadCount[i];
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DisplayUploadRolling", e.toString());
        }
        if (isFinished) {
            rollingAvg = rollingAvg / 2;
            if (rollingAvg != 0.0f) {
                Log.v("RollingUpload", "rolling average is: " + String.valueOf(rollingAvg));
                uiServices.setResults(Constants.THREAD_WRITE_UPLOAD_DATA, uploadMessage,
                        ProcessIperf.formatFloatString(rollingAvg.toString()), false, false);
            }
        }
    }

    private void displayDownloadRollingAverage() {
        Float rollingAvg = phase1DownloadResult + downloadSpeed;
        try {
            for (int i = 0; i < this.threadNumber; i++) {
                if (!rollingDownloadDone[i]) {
                    if (rollingDownloadCount[i] != 0) {
                        rollingAvg += rollingDownloadSpeed[i] / rollingDownloadCount[i];
                    }
                }
            }
        } catch (Exception e) {
            Log.e("displayDownloadRolling", e.toString());
        }
        if (isFinished) {
            rollingAvg = rollingAvg / 2;
            uiServices.stopDownloadTimer();
            Log.v("RollingDownload", "rolling average is: " + String.valueOf(rollingAvg));
            uiServices.setResults(Constants.THREAD_WRITE_DOWNLOAD_DATA, downloadMessage,
                    ProcessIperf.formatFloatString(rollingAvg.toString()), false, false);
        }
    }

    /**
     * Parse the TCP line and display rolling upload and download values.
     * For each thread, it keeps track of where the thread is in the TCP test. Whether
     * is it in the upload or download, it uses four states to determine what part of
     * the test this particular thread is on.
     * Case 0: Thread is initiated. All threads start with 0.0- to show that this is
     * the first value. Once established, it will move to 1, which is the upload case
     * Case 1: The thread is in upload test. If the thread sees a 0.0- again, it means it
     * is seeing the last value, the sum value, i.e. '0.0-10.2 secs'. This means we can
     * move on to the download. Otherwise, keep adding the value and display the average
     * Case 2: Thread is initiated for download. Like Case 0, this will get the thread
     * ready for the download portion
     * Case 3: Thread is in download test. If the test sees a 0.0, it is the final line
     * value for that thread. It display the rolling download value.
     *
     * @param line:      The line form Iperf with the thread number, time, and throughput
     * @param threadNum: the threadNum to parse
     */
    private void parseTCPLine(String line, Integer threadNum) {
        Integer threadIndex = threadNumMap.get(threadNum);
        if (threadIndex == null) {
            return;
        }
        if ((threadIndex < 0) || (threadIndex > this.threadNumber)) {
            Log.w("parseTCPLine", "Bad thread index = " + threadIndex.toString() + "\n" + line);
            return;
        }
        try {
            if (line.contains(" 0.0- 0.0")) { // ignore error line from iperf
                return;
            }
            Log.v("parseTCPLine", String.format("Case: %s for thread map { %d: %d }",
                    iperfThreadState[threadIndex], threadNum, threadIndex));
            switch (iperfThreadState[threadIndex]) {
                case 0:
                    if (line.contains(" 0.0-")) {
                        iperfThreadState[threadIndex] = 1;
                        rollingUploadSpeed[threadIndex] += getTCPBitsPerSec(line);
                        rollingUploadCount[threadIndex]++;
                        displayUploadRollingAverage();
                    }
                    break;
                case 1:
                    if (line.contains(" 0.0-")) {
                        Log.d("parseTCPLine", String.format("Upload TCP value for thread %d at " +
                                "index %d is %f", threadNum, threadIndex, getTCPBitsPerSec(line)));
                        uploadValues.add(getTCPBitsPerSec(line));
                        uploadSpeed += getTCPBitsPerSec(line);
                        iperfThreadState[threadIndex] = 2;
                        rollingUploadDone[threadIndex] = true;
                        setIperfUploadSpeedOnly();
                        doneThreads.put(threadNum, threadIndex);
                        Log.d("getThreadNum", String.format("Adding %s to done threads. " +
                                        "thread set value is: %s", threadNum,
                                iperfThreadState[threadIndex]));
                    } else {
                        if (!finishedUploadTest) {
                            rollingUploadSpeed[threadIndex] += getTCPBitsPerSec(line);
                            rollingUploadCount[threadIndex]++;
                            displayUploadRollingAverage();
                        }
                    }
                    break;
                case 2:
                    if (line.contains(" 0.0-")) {
                        rollingDownloadDone[threadIndex] = false;
                        iperfThreadState[threadIndex] = 3;
                        Log.d("parseTCPLine", String.format("Advancing index %d " +
                                "for thread [%d] to 3", threadIndex, threadNum));
                        downloadSpeedValues.add(getTCPBitsPerSec(line));
                        rollingDownloadSpeed[threadIndex] += getTCPBitsPerSec(line);
                        rollingDownloadCount[threadIndex]++;
                        displayDownloadRollingAverage();
                    }
                    break;
                case 3:
                    if (line.contains(" 0.0-")) {
                        Log.d("parseTCPLine", String.format("Download TCP value for thread %d " +
                                        "at index %d is %f", threadNum, threadIndex,
                                getTCPBitsPerSec(line)));
                        downloadValues.add(getTCPBitsPerSec(line));
                        downloadSpeed += getTCPBitsPerSec(line);
                        iperfThreadState[threadIndex] = 2;
                        rollingDownloadDone[threadIndex] = true;
                        setIperfDownloadSpeed();
                    } else {
                        downloadSpeedValues.add(getTCPBitsPerSec(line));
                        rollingDownloadSpeed[threadIndex] += getTCPBitsPerSec(line);
                        rollingDownloadCount[threadIndex]++;
                        displayDownloadRollingAverage();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e("parseTCPLine", "Exception threadnum =" + threadIndex.toString());
        }
    }

    final void processOutput(String input) {
        Integer startIndex = 0;
        Integer endIndex;
        Integer threadNum;
        String line;
        while (startIndex < input.length()) {
            endIndex = input.indexOf("\n", startIndex);
            line = input.substring(startIndex, endIndex);
            if (endIndex != -1) {
                if (testType.equals(Constants.UDP)) {
                    parseUDPLine(line);
                } else if (testType.equals(Constants.TCP)) {
                    threadNum = getThreadNum(line);
                    if (threadNum != -1) {
                        parseTCPLine(line, threadNum);
                    }
                }
                startIndex = endIndex + 1;
            } else {
                if (testType.equals(Constants.UDP)) {
                    parseUDPLine(line);
                } else if (testType.equals(Constants.TCP)) {
                    threadNum = getThreadNum(line);
                    if (threadNum != -1) {
                        parseTCPLine(line, threadNum);
                    }
                }
                startIndex = input.length();
            }
        }
    }

    private Integer getThreadNum(String line) {
        int indexStart;
        int indexEnd;
        int threadNum = -1;
        String threadNumStr;
        try {
            indexStart = line.indexOf("[");
            if ((indexStart != -1) && (!line.contains("[SUM]"))) {
                indexEnd = line.indexOf("]");
                if (indexEnd != -1) {
                    threadNumStr = line.substring(indexStart + 2, indexEnd);
                    threadNumStr = threadNumStr.replaceAll("^\\s+", "");
                    threadNum = Integer.parseInt(threadNumStr);
                    if (line.contains("local")) {
                        threadNumSet.add(threadNum);
                        if (uploadSet.contains(threadNum)) {
                            downloadSet.add(threadNum);
                        } else {
                            if (uploadSet.size() >= this.threadNumber) {
                                downloadSet.add(threadNum);
                            } else {
                                uploadSet.add(threadNum);
                            }
                        }
                        if ((uploadSet.size() == this.threadNumber) && (downloadSet.isEmpty())) {
                            Log.d("getThreadNum", "Upload set is exactly " +
                                    String.valueOf(this.threadNumber) + " with no download " +
                                    "threads yet");
                            int mapIndex = 0;
                            for (Integer threadNumber : uploadSet) {
                                threadNumMap.put(threadNumber, mapIndex);
                                mapIndex++;
                            }
                        }
                        if (!doneThreads.isEmpty()) {
                            Set<Integer> threadNumberSet = doneThreads.keySet();
                            for (Integer thread : threadNumberSet) {
                                if (downloadSet.contains(threadNumber)) {
                                    doneThreads.remove(thread);
                                    Log.d("getThreadNum", String.format("Download set contains " +
                                            "%s, removing %s", thread, threadNumber));
                                }
                            }
                            threadNumberSet = doneThreads.keySet();
                            for (Integer thread : threadNumberSet) {
                                int replacedThreadNum = doneThreads.get(thread);
                                threadNumMap.put(threadNum, replacedThreadNum);
                                doneThreads.remove(thread);
                                Log.d("getThreadNum", String.format("Reassigning upload thread " +
                                        "%s, to download thread %s", thread, threadNum));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return (-1);
        }
        return (threadNum);
    }

    final void processErrorOutput(String input) {
        Integer startIndex = 0;
        Integer endIndex;
        String line;
        while (startIndex < input.length()) {
            endIndex = input.indexOf("\n", startIndex);
            if (endIndex == -1)
                endIndex = input.length();
            line = input.substring(startIndex, endIndex);
            switch (testType) {
                case Constants.UDP:
                    if (line.matches(".*did not receive ack.*")
                            || line.matches(".*error:.*")) {
                        this.errorMessage += String.format("[UDP] Error: did not receive ack or error:\n%s",
                                line);
                    }
                    this.errorMessage += String.format("[UDP] There was a UDP error:\n%s", line);
                    break;
                case Constants.TCP:
                    if (line.matches(".*failed.*") || line.matches(".*error:.*")) {
                        downloadMessage = "Download Incomplete";
                        if (!finishedUploadTest) {
                            uploadMessage = "Upload Incomplete";
                        }
                        this.errorMessage += String.format("[TCP] There was a error or failure:\n%s",
                                line);
                    }
                    this.errorMessage += String.format("[TCP] There was a TCP error:\n%s", line);
                    break;
                default:
                    this.errorMessage += String.format("There is an unknown error:\n%s", line);
                    break;
            }
            startIndex = endIndex + 1;
        }
    }

    void finishResults() {
        boolean isCompleted = true;
        if (this.getTestType().equals(Constants.TCP)) {
            if (!allDownloadThreadsDone()) {
                this.success = false;
                Log.i(this.logger, "Download threads are incomplete");
                for (Integer threadnum : threadNumMap.keySet()) {
                    Integer threadIndex = threadNumMap.get(threadnum);
                    Log.d("IncompleteThreads", String.format("Thread [%d], index: %d, status: %b",
                            threadnum, threadIndex, rollingDownloadDone[threadIndex]));
                }
                isCompleted = false;
            }
            if (!allUploadThreadsDone()) {
                Log.i(this.logger, "Upload threads are incomplete");
                isCompleted = false;
                this.success = false;
            }
            if (uploadSpeed == 0.0f || downloadSpeed == 0.0f) {
                Log.d(this.logger, "Have zero values for upload and download");
                Log.d(this.logger, String.format("rolling download speeds: %s\nrolling upload speeds: %s",
                        Arrays.toString(rollingDownloadSpeed),
                        Arrays.toString(rollingUploadSpeed)));
                Log.d(this.logger, String.format("download speeds: %s\nupload speeds: %s",
                        Collections.singleton(downloadValues),
                        Collections.singleton(uploadValues)));
                Log.d(this.logger, String.format("final download speed: %f\nfinal upload speeds: %f",
                        downloadSpeed, uploadSpeed));
                Log.i(this.logger, "Upload speed or download speed is zero 0.0");
                isCompleted = false;
            } else {
                Log.d(this.logger, "Upload speed and download speed are non-zero");
                uiServices.stopUploadTimer();
                if (downloadSpeed != 0.0f) {
                    Log.d(this.logger, "Success. Both upload and download speeds are non-zero");
                    this.success = true;
                    setIperfDownloadSpeed();
                } else {
                    Log.d(this.logger, "Download speed is 0.0. Calling setupIperfUploadSpeedOnly()");
                    setIperfUploadSpeedOnly();
                }

            }
        } else if (this.getTestType().equals(Constants.UDP)) {
            if (this.udpState != 2 && testType != null) {
                Log.i(this.logger, "UDP state is not 2. It is : " + udpState);
                isCompleted = false;
            } else {
                this.success = true;
            }
        }
        if (!isCompleted) {
            Log.w(this.logger, "Something wasn't finished properly");
        }
    }
}
