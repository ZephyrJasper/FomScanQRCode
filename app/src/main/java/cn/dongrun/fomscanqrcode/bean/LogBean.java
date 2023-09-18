package cn.dongrun.fomscanqrcode.bean;

/**
 * @author Zephyrus, 596991713@qq.com
 * @since 2023-8-15
 */
public class LogBean {
    private String scanTime;
    private int scanCount;
    private int requestSuccessCnt;
    private String qrUUID;
    private int qrIndex;
    private int qrAllNum;
    private double rss;
    private double pss;
    private double jvm;


    public LogBean() {
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public int getScanCount() {
        return scanCount;
    }

    public void setScanCount(int scanCount) {
        this.scanCount = scanCount;
    }



    public int getRequestSuccessCnt() {
        return requestSuccessCnt;
    }

    public void setRequestSuccessCnt(int requestSuccessCnt) {
        this.requestSuccessCnt = requestSuccessCnt;
    }


    public double getRss() {
        return rss;
    }

    public void setRss(double rss) {
        this.rss = rss;
    }

    public double getPss() {
        return pss;
    }

    public void setPss(double pss) {
        this.pss = pss;
    }

    public double getJvm() {
        return jvm;
    }

    public void setJvm(double jvm) {
        this.jvm = jvm;
    }

    public String getQrUUID() {
        return qrUUID;
    }

    public void setQrUUID(String qrUUID) {
        this.qrUUID = qrUUID;
    }

    public int getQrIndex() {
        return qrIndex;
    }

    public void setQrIndex(int qrIndex) {
        this.qrIndex = qrIndex;
    }

    public int getQrAllNum() {
        return qrAllNum;
    }

    public void setQrAllNum(int qrAllNum) {
        this.qrAllNum = qrAllNum;
    }
}
