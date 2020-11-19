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

import gov.ca.cpuc.fieldtest.android.AndroidUiServices.TextOutputAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.lang.InterruptedException;



class ExecCommandLine {

    private ProcessIperf iperfTest;
    private ProcessPing pingTest;
    private String commandline;
    private Process process;
    Boolean commandTimedOut;
    private long timeout;
    private TextOutputAdapter display;
    private AndroidUiServices uiServices;

    ExecCommandLine(String line, long timeout, TextOutputAdapter display, ProcessIperf iperfTest,
                    ProcessPing pingTest, AndroidUiServices uiServices) {
        this.iperfTest = iperfTest;
        this.pingTest = pingTest;
        this.commandline = line;
        this.commandTimedOut = false;
        this.timeout = timeout;
        this.display = display;
        this.uiServices = uiServices;
    }

    String runIperfCommand() throws TimeoutException, InterruptedException {
        int read;
        char[] buffer = new char[4096];
        StringBuilder output = new StringBuilder();
        StringBuilder line = new StringBuilder();
        StringBuilder lineerror = new StringBuilder();
        StringBuilder outputerror = new StringBuilder();
        int readerror;
        char[] buffererror = new char[4096];
        Timer timer = new Timer();
        try {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                commandTimedOut = true;
                if (iperfTest.getTestType().equals(Constants.TCP)) {
                    iperfTest.setMessage("Last line: " + display.toString());
                    if (!iperfTest.finishedUploadTest) {
                        iperfTest.setMessage("Upload test not finished");
                    }
                }
                Log.i(this.getClass().getName(), "Timed out");
                this.cancel();
                process.destroy();
                }
            };
            timer.schedule(task, timeout);
            if (iperfTest.getTestType().equals(Constants.TCP)) {
                uiServices.startUploadTimer();
            }

            Log.d("CommandLine", commandline);
            this.process = Runtime.getRuntime().exec(commandline);
            uiServices.setProcessHandle(this.process);
            output.append("\nIperf command line:");
            output.append(commandline);
            output.append("\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            BufferedReader readererror = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            try {
                while (((read = reader.read(buffer)) > 0)) {
                    output.append(buffer, 0, read);
                    line.delete(0, line.length());
                    line.append(buffer, 0, read);
                    if (this.display != null) {
                        this.display.append(line.toString());
                    }
                    if (iperfTest != null & !commandTimedOut) {
                        iperfTest.processOutput(line.toString() + "\n");
                        iperfTest.processErrorOutput(line.toString());
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        process.destroy();
                        reader.close();
                        readererror.close();
                        timer.cancel();
                        throw new InterruptedException();
                    }
                }
                while (((readerror = readererror.read(buffererror)) > 0)) {
                    outputerror.append(buffererror, 0, readerror);
                    lineerror.delete(0, lineerror.length());
                    lineerror.append(buffererror, 0, readerror);
                    if (this.display != null) {
                        this.display.append(lineerror.toString());
                    }
                    if (iperfTest != null) {
                        iperfTest.processErrorOutput(lineerror.toString()
                                + "\n");
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        process.destroy();
                        reader.close();
                        readererror.close();
                        timer.cancel();
                        throw new InterruptedException();
                    }
                }
                Log.d(this.getClass().getName(), "End of while loop");
                process.waitFor();
                Log.d(this.getClass().getName(), "Waited for");
                timer.cancel();
                reader.close();
                readererror.close();
                return (output.toString() + outputerror.toString());
            } catch (IllegalThreadStateException ex) {
                Log.e("ERROR", ex.getMessage());
                return(output.toString()+outputerror.toString());
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage());
                return(output.toString()+outputerror.toString());
            } catch (InterruptedException e) {
                Log.e("ERROR", e.getMessage());
                process.destroy();
                reader.close();
                readererror.close();
                timer.cancel();
                throw new InterruptedException();
            }catch (Exception e){
                Log.e("ERROR", e.getMessage());
                return(output.toString()+outputerror.toString());
            }
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
            return(output.toString()+outputerror.toString());
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
            return(output.toString()+outputerror.toString());
        }
    }

    String runCommand() throws InterruptedException {
        BufferedReader reader;
        BufferedReader readererror;
        StringBuilder output = new StringBuilder();
        Timer timer = new Timer();
        try {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    commandTimedOut = true;
                    if(pingTest != null){
                        pingTest.success = false;
                    }
                    this.cancel();
                    process.destroy();
                }
            };
            timer.schedule(task, timeout);
            this.process = Runtime.getRuntime().exec(commandline);
            uiServices.setProcessHandle(this.process);
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            readererror = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (pingTest != null) {
                    pingTest.ProcessOutput(line + "\n", "Phone");
                }
                if (Thread.currentThread().isInterrupted()) {
                    timer.cancel();
                    process.destroy();
                    reader.close();
                    readererror.close();
                    throw new InterruptedException();
                }
            }
            while ((line = readererror.readLine()) != null) {
                output.append(line);
                output.append("\n");
                if (Thread.currentThread().isInterrupted()) {
                    timer.cancel();
                    process.destroy();
                    reader.close();
                    readererror.close();
                    throw new InterruptedException();
                }
            }
            process.waitFor();
            timer.cancel();
            reader.close();
            readererror.close();

            int exval = process.exitValue();
            if (exval != 0) {
                output.append("\nbad exit value--error executing command: ");
                output.append(commandline);
            }
        } catch (IOException e) {
            timer.cancel();
            output.append(e.getMessage());
        } catch (IllegalThreadStateException ex) {
            timer.cancel();
            output.append("\nerror executing command: ");
            output.append(commandline);
            output.append(ex.getMessage());
        } catch (InterruptedException e) {
            process.destroy();
            timer.cancel();
            process.destroy();
            throw new InterruptedException();
        } catch (Exception e){
            timer.cancel();
            output.append(e.getMessage());
        }
        return output.toString();
    }

}
