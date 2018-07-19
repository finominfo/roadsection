package hu.gds.roadsection;

public class Data {

    private final String roadName;
    private final int runCode;
    private final int distanceInMeter;
    private final String factoryCode;
    private final long distance;

    public Data(String roadName, int runCode, int distanceInMeter, String factoryCode, long distance) {
        this.roadName = roadName;
        this.runCode = runCode;
        this.distanceInMeter = distanceInMeter;
        this.factoryCode = factoryCode;
        this.distance = distance;
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

    public long getDistance() {
        return distance;
    }
}
