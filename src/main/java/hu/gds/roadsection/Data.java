package hu.gds.roadsection;

public class Data {

    private final String roadName;
    private final int runCode;
    private final int distanceInMeter;
    private final String factoryCode;

    public Data(String roadName, int runCode, int distanceInMeter, String factoryCode) {
        this.roadName = roadName;
        this.runCode = runCode;
        this.distanceInMeter = distanceInMeter;
        this.factoryCode = factoryCode;
    }

    public String getRoadName() {
        return roadName;
    }

    public int getRunCode() {
        return runCode;
    }

    public int getDistanceInMeter() {
        return distanceInMeter;
    }

    public String getFactoryCode() {
        return factoryCode;
    }
}
