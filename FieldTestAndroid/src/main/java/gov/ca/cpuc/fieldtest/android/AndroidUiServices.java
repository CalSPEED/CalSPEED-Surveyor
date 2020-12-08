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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Implementation of UiServices for Android.
 */
class AndroidUiServices implements UiServices {
    private Message message;
    private Handler handler;
    private Context context;
    private boolean userStop;


    /**
     * Constructor to get the handler from Android's UI Thread.
     *
     * @param handler handler from main activity for dispatching messages
     */
    AndroidUiServices(Context context, Handler handler) {
        this.handler = handler;
        this.context = context;
        this.userStop = false;
    }

    /**
     * Adapter from JTextArea#append to UiServices#appendString.
     */

    static class TextOutputAdapter {
        private final int viewId;
        private final UiServices uiServices;

        /**
         * @param viewId UiServices constant to pass to appendString
         */
        TextOutputAdapter(UiServices uiServices, int viewId) {
            this.viewId = viewId;
            this.uiServices = uiServices;
        }

        void append(String s) {
            uiServices.appendString(s, viewId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendString(String str, int objectId) {
        switch (objectId) {
            case MAIN_VIEW:
                message = handler.obtainMessage(Constants.THREAD_MAIN_APPEND, str);
                handler.sendMessage(message);
                break;
            case STAT_VIEW:
                message = handler.obtainMessage(Constants.THREAD_STAT_APPEND, str);
                handler.sendMessage(message);
                break;
            case DIAG_VIEW:
                // Diagnosis view is redirected to Statistics view on Android.
                message = handler.obtainMessage(Constants.THREAD_STAT_APPEND, str);
                handler.sendMessage(message);
                break;
            case SUMMARY_VIEW:
                message = handler.obtainMessage(Constants.THREAD_SUMMARY_APPEND, str);
                handler.sendMessage(message);
                break;
            case DEBUG_VIEW:
                // We don't have diagnosis view here, just ignore this action.
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementProgress(Integer increment) {
        message = handler.obtainMessage(Constants.THREAD_ADD_PROGRESS);
        Bundle messageData = new Bundle();
        messageData.putInt("increment", increment);
        message.setData(messageData);
        handler.sendMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printLatLong() {
        message = handler.obtainMessage(Constants.THREAD_LAT_LONG_APPEND);
        handler.sendMessage(message);
    }

    void goodGpsSignal() {
        message = handler.obtainMessage(Constants.THREAD_GOOD_GPS_SIGNAL);
        handler.sendMessage(message);
    }

    void noGpsSignal() {
        message = handler.obtainMessage(Constants.THREAD_NO_GPS_SIGNAL);
        handler.sendMessage(message);
    }

    @Override
    public void onBeginTest() {
        message = handler.obtainMessage(Constants.THREAD_BEGIN_TEST);
        handler.sendMessage(message);
        userStop = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEndTest() {
        message = handler.obtainMessage(Constants.THREAD_END_TEST);
        handler.sendMessage(message);
    }

    void onTestInterrupt() {
        setCurrentTask();
        message = handler.obtainMessage(Constants.THREAD_TEST_INTERRUPTED);
        handler.sendMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFailure(String errorMessage) {
        String str = "\n" + context.getString(R.string.fail_tip) + "\n";
        appendString("\n" + errorMessage, UiServices.MAIN_VIEW);
        appendString("\n" + errorMessage, STAT_VIEW);
        appendString(str, UiServices.MAIN_VIEW);
        appendString(str, STAT_VIEW);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(String str) {
        Log.e("debug", str);
    }
    //Sets handle of current executing process

    /**
     * {@inheritDoc}
     */

    void setProcessHandle(Process process) {
        message = handler.obtainMessage(Constants.THREAD_SET_PROCESS_HANDLE, process);
        handler.sendMessage(message);
    }
    //Unimplemented (and unnecessary Applet-specific) methods below

    /**
     * {@inheritDoc}
     */

    void clearProcessHandle() {
        message = handler.obtainMessage(Constants.THREAD_CLEAR_PROCESS_HANDLE);
        handler.sendMessage(message);
    }

    // Unimplemented (and unnecessary Applet-specific) methods below

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(String status) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatusPanel(String status) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoginSent() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPacketQueuingDetected() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wantToStop() {
        return userStop;
    }


    @Override
    public void setVariable(String name, int value) {
    }

    @Override
    public void setVariable(String name, double value) {
    }

    @Override
    public void setVariable(String name, Object value) {
    }

    void printWifiID() {
        message = handler.obtainMessage(Constants.THREAD_PRINT_BSSID_SSID);
        handler.sendMessage(message);
    }

    void updateLatLong(Double lat, Double lon) {
        message = handler.obtainMessage(Constants.THREAD_UPDATE_LATLONG);
        Message newMessage = new Message();
        newMessage.copyFrom(message);
        Bundle messageData = new Bundle();
        messageData.putDouble("lat", lat);
        messageData.putDouble("lon", lon);
        newMessage.setData(messageData);
        try {
            handler.sendMessage(newMessage);
        } catch (IllegalStateException e) {
            Log.e("HandleMessage", e.getMessage());
        }
    }

    void testSuccess(int imageCode, String results) {
        message = handler.obtainMessage(Constants.THREAD_ADD_TO_RESULTS_LIST);
        Bundle messageData = new Bundle();
        messageData.putInt("imageCode", imageCode);
        messageData.putString("results", results);
        message.setData(messageData);
        handler.sendMessage(message);
    }

    void updateTestResult(int updatedImageCode, String testResults) {
        message = handler.obtainMessage(Constants.THREAD_UPDATE_RESULTS_LIST);
        Bundle messageData = new Bundle();
        messageData.putInt("imageCode", updatedImageCode);
        messageData.putString("results", testResults);
        message.setData(messageData);
        handler.sendMessage(message);
    }

    void resultsSaved() {
        message = handler.obtainMessage(Constants.THREAD_RESULTS_SAVED);
        handler.sendMessage(message);
    }

    void resultsNotSaved() {
        message = handler.obtainMessage(Constants.THREAD_RESULTS_NOT_SAVED);
        handler.sendMessage(message);
    }

    void resultsUploaded() {
        message = handler.obtainMessage(Constants.THREAD_RESULTS_UPLOADED);
        handler.sendMessage(message);
    }

    void resultsNotUploaded() {
        message = handler.obtainMessage(Constants.THREAD_RESULTS_NOT_UPLOADED);
        handler.sendMessage(message);
    }

    void attemptingToUpload() {
        message = handler.obtainMessage(Constants.THREAD_RESULTS_ATTEMPT_UPLOAD);
        handler.sendMessage(message);
    }

    void setStatusText(String text) {
        message = handler.obtainMessage(Constants.THREAD_SET_STATUS_TEXT);
        Bundle messageData = new Bundle();
        messageData.putString("text", text);
        message.setData(messageData);
        handler.sendMessage(message);
    }

    void setResults(int constant, String text, String number, boolean redText,
                    boolean numbersHidden) {
        message = handler.obtainMessage(constant);
        Bundle messageData = new Bundle();
        messageData.putString("text", text);
        messageData.putString("number", number.replace(",", "."));
        messageData.putBoolean("redText", redText);
        messageData.putBoolean("numbersHidden", numbersHidden);
        message.setData(messageData);
        handler.sendMessage(message);
    }

    void setCurrentTask() {
    }

    void phaseComplete() {
        message = handler.obtainMessage(Constants.FINISH_PHASE);
        handler.sendMessage(message);
    }

    void startUploadTimer() {
        message = handler.obtainMessage(Constants.THREAD_START_UPLOAD_TIMER);
        handler.sendMessage(message);
    }

    void stopUploadTimer() {
        message = handler.obtainMessage(Constants.THREAD_STOP_UPLOAD_TIMER);
        handler.sendMessage(message);
    }

    void startDownloadTimer() {
        message = handler.obtainMessage(Constants.THREAD_START_DOWNLOAD_TIMER);
        handler.sendMessage(message);
    }

    void stopDownloadTimer() {
        message = handler.obtainMessage(Constants.THREAD_STOP_DOWNLOAD_TIMER);
        handler.sendMessage(message);
    }

    void updateNetworkInfo() {
        if (handler.hasMessages(Constants.THREAD_UPDATE_NETWORK_INFO)) {
            handler.removeMessages(Constants.THREAD_UPDATE_NETWORK_INFO);
        }
        message = handler.obtainMessage(Constants.THREAD_UPDATE_NETWORK_INFO);
        handler.sendMessage(message);
    }

}
