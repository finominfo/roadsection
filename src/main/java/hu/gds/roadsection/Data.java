package hu.gds.roadsection;

public class Data {

    private final String roadName;
    private final int runCode;
    private final int allInMeter;
    private final int meter;
    private final int kMeter;
    private final String factoryCode;
    private final long distance;

    public Data(String roadName, int runCode, int allInMeter, String factoryCode, long distance) {
        this.roadName = roadName;
        this.runCode = runCode;
        this.allInMeter = allInMeter;
        this.meter = allInMeter % 1000;
        this.kMeter = allInMeter / 1000;
        this.factoryCode = factoryCode;
        this.distance = distance;
    }

    public String getRoadName() {
        return roadName;
    }

    public int getRunCode() {
        return runCode;
    }

    public int getAllInMeter() {
        return allInMeter;
    }

    public int getMeter() {
        return meter;
    }

    public int getkMeter() {
        return kMeter;
    }

    public String getFactoryCode() {
        return factoryCode;
    }

    public long getDistance() {
        return distance;
    }
}
