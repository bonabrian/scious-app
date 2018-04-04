package id.bonabrian.scious.source.dao;

import java.util.List;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Measurements {
    private String id;
    private String user_id;
    private String stress_level;
    private double sdnn;
    private double mean_hr;
    private double mean_rr;
    private String time;

    public Measurements(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public String getStressLevel() {
        return stress_level;
    }

    public void setStressLevel(String stressLevel) {
        this.stress_level = stressLevel;
    }

    public double getSdnn() {
        return sdnn;
    }

    public void setSdnn(double sdnn) {
        this.sdnn = sdnn;
    }

    public double getMeanHR() {
        return mean_hr;
    }

    public void setMeanHR(double meanHR) {
        this.mean_hr = meanHR;
    }

    public double getMeanRR() {
        return mean_rr;
    }

    public void setMeanRR(double meanRR) {
        this.mean_rr = meanRR;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public class ListMeasurements extends BaseModel {
        private List<Measurements> measurements;

        public List<Measurements> getMeasurements() {
            return measurements;
        }

        public void setMeasurements(List<Measurements> measurements) {
            this.measurements = measurements;
        }
    }
}
