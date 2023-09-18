package cn.dongrun.fomscanqrcode.bean;

import androidx.annotation.NonNull;

import java.util.Objects;
/**
 * @since  2023-8-15
 * @author Zephyrus,596991713@qq.com
 */
public class QrData implements Comparable<QrData> {
    private Integer qrIndex;
    private String qrData;

    public QrData() {
    }

    public QrData(Integer qrIndex, String qrData) {
        this.qrIndex = qrIndex;
        this.qrData = qrData;
    }

    public Integer getQrIndex() {
        return qrIndex;
    }

    public void setQrIndex(Integer qrIndex) {
        this.qrIndex = qrIndex;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }

    @Override
    public int compareTo(QrData Data) {
        return this.qrIndex - Data.qrIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "QrData{" +
                "qrIndex=" + qrIndex +
                ", qrData='" + qrData + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        QrData other = (QrData) obj;
        return Objects.equals(qrIndex, other.qrIndex) && Objects.equals(qrData, other.qrData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qrIndex, qrData);
    }
}
