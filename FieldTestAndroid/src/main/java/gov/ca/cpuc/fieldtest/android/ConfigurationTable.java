/*
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by joshuaahn on 9/19/19.
 *
 * This class is creates a dictionary of
 */

class ConfigurationTable {
    private Map<String, Map<Integer, Map<String, Integer>>> configTable;
    private Map<String, Map<String, Integer>> defaultConfigTable;
    private Map<String, Map<String, Integer>> prelimTable;
    private Map<String, Integer> weakSignalPrelimTable;

    // WEAK SIGNAL DETECTION
    final String WEAK_SIGNAL_PRELIM_VALUE = String.format("%s:1,%s:32,%s:5",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);
    // REGULAR PRELIM TESTING
    String MOBILE_PRELIM_VALUE = "%s:%d,%s:256,%s:%d";
    final String MOBILE_PRELIM_VALUE_10 = String.format(MOBILE_PRELIM_VALUE, Constants.THREAD_NUMBER,
            Constants.PRELIM_THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME, 10);
    final String MOBILE_PRELIM_VALUE_20 = String.format(MOBILE_PRELIM_VALUE, Constants.THREAD_NUMBER,
            Constants.PRELIM_THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME, 20);
    final String MOBILE_PRELIM_VALUE_30 = String.format(MOBILE_PRELIM_VALUE, Constants.THREAD_NUMBER,
            Constants.PRELIM_THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME, 30);
    final String MOBILE_PRELIM_VALUE_60 = String.format(MOBILE_PRELIM_VALUE, Constants.THREAD_NUMBER,
            Constants.PRELIM_THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME, 60);
    final String WIFI_PRELIM_VALUE = String.format("%s:%d,%s:256,%s:%d", Constants.THREAD_NUMBER,
            Constants.PRELIM_THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME,
            Constants.PRELIM_TEST_TIME);

    // TCP CONFIGURATION VALUES
    final Integer MOBILE_LEVEL_ONE_KEY = 10000;
    final Integer MOBILE_LEVEL_TWO_KEY = 100000;
    final Integer MOBILE_LEVEL_THREE_KEY = 250000;
    final Integer MOBILE_LEVEL_FOUR_KEY = Integer.MAX_VALUE;
    final String MOBILE_LEVEL_ONE_VALUE = String.format("%s:1,%s:256,%s:20",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);
    final String MOBILE_LEVEL_TWO_VALUE = String.format("%s:4,%s:256,%s:20",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);
    final String MOBILE_LEVEL_THREE_VALUE = String.format("%s:8,%s:256,%s:20",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);
    final String MOBILE_LEVEL_FOUR_VALUE = String.format("%s:8,%s:512,%s:20",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);

    final Integer WIFI_LEVEL_ONE_KEY = 10000;
    final Integer WIFI_LEVEL_TWO_KEY = 100000;
    final Integer WIFI_LEVEL_THREE_KEY = 250000;
    final String WIFI_LEVEL_ONE_VALUE = String.format("%s:1,%s:256,%s:20", Constants.THREAD_NUMBER,
            Constants.WINDOW_SIZE, Constants.TEST_TIME);
    final String WIFI_LEVEL_TWO_VALUE = String.format("%s:4,%s:256,%s:20", Constants.THREAD_NUMBER,
            Constants.WINDOW_SIZE, Constants.TEST_TIME);
    final String WIFI_LEVEL_THREE_VALUE = String.format("%s:8,%s:512,%s:20",
            Constants.THREAD_NUMBER, Constants.WINDOW_SIZE, Constants.TEST_TIME);
    final String WIFI_LEVEL_FOUR_VALUE = String.format("%s:8,%s:512,%s:20", Constants.THREAD_NUMBER,
            Constants.WINDOW_SIZE, Constants.TEST_TIME);

    // DEFAULT VALUES
    final String MOBILE_DEFAULT_VALUE = String.format("%s:%d,%s:256,%s:%d", Constants.THREAD_NUMBER,
            Constants.DEFAULT_THREAD_NUMBER, Constants.WINDOW_SIZE,
            Constants.TEST_TIME, Constants.DEFAULT_TEST_TIME);
    final String WIFI_DEFAULT_VALUE = String.format("%s:2,%s:256,%s:20", Constants.THREAD_NUMBER,
            Constants.WINDOW_SIZE, Constants.TEST_TIME);


    ConfigurationTable() {
        createWeakSignalPrelimTable();
        createPrelimTable();
        createTcpTable();
        createDefaultsTable();
    }

    private void createTcpTable () {
        this.configTable = new HashMap<>();
        Map<Integer, Map<String, Integer>> mobileTable = new TreeMap<>();
        mobileTable.put(MOBILE_LEVEL_ONE_KEY, stringToMap(MOBILE_LEVEL_ONE_VALUE));
        mobileTable.put(MOBILE_LEVEL_TWO_KEY, stringToMap(MOBILE_LEVEL_TWO_VALUE));
        mobileTable.put(MOBILE_LEVEL_THREE_KEY, stringToMap(MOBILE_LEVEL_THREE_VALUE));
        mobileTable.put(MOBILE_LEVEL_FOUR_KEY, stringToMap(MOBILE_LEVEL_FOUR_VALUE));
        configTable.put(Constants.MOBILE, mobileTable);
        Map<Integer, Map<String, Integer>> wifiTable = new TreeMap<>();
        wifiTable.put(WIFI_LEVEL_ONE_KEY, stringToMap(WIFI_LEVEL_ONE_VALUE));
        wifiTable.put(WIFI_LEVEL_TWO_KEY, stringToMap(WIFI_LEVEL_TWO_VALUE));
        wifiTable.put(WIFI_LEVEL_THREE_KEY, stringToMap(WIFI_LEVEL_THREE_VALUE));
        Integer WIFI_LEVEL_FOUR_KEY = Integer.MAX_VALUE;
        wifiTable.put(WIFI_LEVEL_FOUR_KEY, stringToMap(WIFI_LEVEL_FOUR_VALUE));
        configTable.put(Constants.WIFI, wifiTable);
    }

    private void createDefaultsTable() {
        this.defaultConfigTable = new HashMap<>();
        defaultConfigTable.put(Constants.MOBILE, stringToMap(MOBILE_DEFAULT_VALUE));
        defaultConfigTable.put(Constants.WIFI, stringToMap(WIFI_DEFAULT_VALUE));
    }

    private void createPrelimTable() {
        prelimTable = new HashMap<>();
        prelimTable.put(Constants.MOBILE, stringToMap(MOBILE_PRELIM_VALUE_10));
        prelimTable.put(Constants.WIFI, stringToMap(WIFI_PRELIM_VALUE));
    }

    public ArrayList<Map<String, Integer>> getPrelimCalibrationValues() {
        ArrayList<Map<String, Integer>> prelimList = new ArrayList<>();
        prelimList.add(stringToMap(MOBILE_PRELIM_VALUE_10));
        prelimList.add(stringToMap(MOBILE_PRELIM_VALUE_20));
        prelimList.add(stringToMap(MOBILE_PRELIM_VALUE_30));
        prelimList.add(stringToMap(MOBILE_PRELIM_VALUE_60));
        return prelimList;
    }

    private void createWeakSignalPrelimTable() {
        this.weakSignalPrelimTable = stringToMap(WEAK_SIGNAL_PRELIM_VALUE);
    }

    private Map<String, Integer> stringToMap(String parseString) {
        Map<String, Integer> stepMap = new HashMap<>();
        String[] pairs = parseString.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            stepMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
        }
        return stepMap;
    }

    Map<String, Map<Integer, Map<String, Integer>>> getTable() {
        return this.configTable;
    }

    Map<String, Map<String, Integer>> getDefaultsTable() {
        return this.defaultConfigTable;
    }

    Map<String, Map<String, Integer>> getPrelimTable() {
        return this.prelimTable;
    }

    Map<String,Integer> getWeakSignalPrelimTable() {
        return this.weakSignalPrelimTable;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Preliminary Configuration:\n");
        for (String wsKey : this.weakSignalPrelimTable.keySet()) {
            sb.append("\t").append(wsKey).append(": ");
            sb.append(weakSignalPrelimTable.get(wsKey)).append("\n");
        }
        for (String pkey: prelimTable.keySet()) {
            Map<String, Integer> prelims = prelimTable.get(pkey);
            sb.append("\t").append(pkey).append(": ").append("\n");
            for (String prelimKey : prelims.keySet()) {
                sb.append("\t\t").append(prelimKey).append(": ");
                sb.append(prelims.get(prelimKey)).append("\n");
            }
        }
        sb.append("\nTCP Test Configuration:\n");
        for (String key: configTable.keySet()) {
            sb.append("\t").append(key).append(": ").append("\n");
            Map<Integer, Map<String, Integer>> levels = configTable.get(key);
            for (Integer i : levels.keySet()) {
                sb.append("\t\t").append(String.valueOf(i)).append(": ").append("\n");
                Map<String, Integer> configs = levels.get(i);
                for (String configType : configs.keySet()) {
                    sb.append("\t\t\t").append(configType).append(": ");
                    sb.append(String.valueOf(configs.get(configType))).append("\n");
                }
            }
        }
        sb.append("\nDefault TCP Test Configuration:\n");
        for (String key: defaultConfigTable.keySet()) {
            Map<String, Integer> configs = defaultConfigTable.get(key);
            sb.append("\t").append(key).append(": ").append("\n");
            for (String configKey : configs.keySet()) {
                sb.append("\t\t").append(configKey).append(": ");
                sb.append(String.valueOf(configs.get(configKey))).append("\n");
            }
        }
        return sb.toString();
    }
}
