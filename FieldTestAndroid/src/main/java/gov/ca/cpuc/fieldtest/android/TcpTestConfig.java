package gov.ca.cpuc.fieldtest.android;

/**
 * Created by joshuaahn on 6/7/19.
 */

public class TcpTestConfig extends TestConfig {
    private String windowSize;
    private Integer threadNumber;

    TcpTestConfig(String iperfPath, String serverIp, String port, String windowSizeFormat,
                  String windowSize, Integer threadNumber, Integer testTime,
                  Integer testInterval) {
        super(iperfPath, serverIp, port, windowSizeFormat, testTime, testInterval);
        this.windowSize = windowSize;
        this.threadNumber = threadNumber;
    }

    TcpTestConfig(String iperfPath, String serverIp, String port,
                  String windowSize, Integer threadNumber, Integer testTime) {
        super(iperfPath, serverIp, port, testTime);
        this.windowSize = windowSize;
        this.threadNumber = threadNumber;
    }

    TcpTestConfig(String iperfPath, String serverIp, String port,
                  String windowSize, Integer threadNumber) {
        super(iperfPath, serverIp, port);
        this.windowSize = windowSize;
        this.threadNumber = threadNumber;
    }

    String createIperfCommandLine() {
        if (!this.windowSize.contains("k")) {
            this.windowSize = String.format("%sk", this.windowSize);
        }
        String iperf = this.iperfPath + Constants.IPERF_VERSION;
        String cl = String.format(Constants.TCP_COMMAND, this.serverIp, this.port, iperf,
                this.windowSize, this.threadNumber, this.format, this.testTime, this.testInterval);
        System.out.println(cl);
        return String.format(Constants.TCP_COMMAND, this.serverIp, this.port, iperf,
                this.windowSize, this.threadNumber, this.format, this.testTime, this.testInterval);
    }

    String getWindowSize() {
        return this.windowSize;
    }

    Integer getThreadNumber() {
        return this.threadNumber;
    }

    void setThreadNumber(Integer threadNum) {
        this.threadNumber = threadNum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(this.serverIp);
        sb.append("\nPort: ").append(this.port);
        sb.append("\nWindow size: ").append(this.windowSize);
        sb.append("\nNumber of threads: ").append(this.threadNumber);
        String fullFormatName;
        switch (this.format) {
            case "k":
                fullFormatName = "Kilobits";
                break;
            case "K":
                fullFormatName = "Kilobytes";
                break;
            case "m":
                fullFormatName = "Megabits";
                break;
            case "M":
                fullFormatName = "Megabytes";
                break;
            default:
                fullFormatName = "Kilobits";
                break;
        }
        sb.append("\nWindow size format: ").append(fullFormatName);
        sb.append("\nTest time length: ").append(this.testTime);
        sb.append("\nTest time interveral: ").append(this.testInterval);
        return sb.toString();
    }

}
