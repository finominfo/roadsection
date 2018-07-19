package hu.gds.roadsection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoadResolver {
    private final static int KEY_MULTIPLIER = 10_000;
    private final static String CSV = "./split.csv";
    private final static String SPLIT = ";";
    private final Map<Integer, FixedSizeCompactData> roadSections = new HashMap<>();
    private final Map<Integer, AtomicInteger> temporarySizeMeasurement = new HashMap<>();


    public RoadResolver() {
        try {
            DecimalFormat df = new DecimalFormat("#");
            df.setRoundingMode(RoundingMode.HALF_UP);
            String line;
            BufferedReader br = new BufferedReader(new FileReader(new File(CSV)));
            br.readLine();
            for (int i = 0; i <= 0xffff; i++) temporarySizeMeasurement.put(i, new AtomicInteger());
            while ((line = br.readLine()) != null) {
                String[] split = line.split(SPLIT);
                int key = CRC16.get(df.format(Double.valueOf(split[2]) * KEY_MULTIPLIER) + df.format(Double.valueOf(split[3]) * KEY_MULTIPLIER));
                temporarySizeMeasurement.get(key).incrementAndGet();
            }
            br.close();
            for (int i = 0; i <= 0xffff; i++)
                roadSections.put(i, new FixedSizeCompactData(temporarySizeMeasurement.get(i).get()));
            temporarySizeMeasurement.clear();
            br = new BufferedReader(new FileReader(new File(CSV)));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] split = line.split(SPLIT);
                int key = CRC16.get(df.format(Double.valueOf(split[2]) * KEY_MULTIPLIER) + df.format(Double.valueOf(split[3]) * KEY_MULTIPLIER));
                roadSections.get(key).add(split);
            }
            br.close();
            int count = 0;
            for (FixedSizeCompactData compactData : roadSections.values()) {
                count += compactData.getSize();
            }
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Data get(double wgs84Latitude, double wgs84Longitude) {
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        double wgs84LatitudeZero = wgs84Latitude * KEY_MULTIPLIER;
        double wgs84LongitudeZero = wgs84Longitude * KEY_MULTIPLIER;
        List<Double> keyLatitudes = Arrays.asList(wgs84LatitudeZero, wgs84LatitudeZero + 1, wgs84LatitudeZero - 1);
        List<Double> keyLongitudes = Arrays.asList(wgs84LongitudeZero, wgs84LongitudeZero + 1, wgs84LongitudeZero - 1);
        List<Data> datas = new ArrayList<>();
        for (Double lat : keyLatitudes) {
            for (Double lon : keyLongitudes) {
                int key = CRC16.get(df.format(lat) + df.format(lon));
                datas.add(roadSections.get(key).get(wgs84Latitude, wgs84Longitude));
            }
        }
        long minimumDistance = Long.MAX_VALUE;
        int minimumPosition = 0;
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getDistance() < minimumDistance) {
                minimumDistance = datas.get(i).getDistance();
                minimumPosition = i;
            }
        }
        return datas.get(minimumPosition);
    }


    public static void main(String[] args) {
        RoadResolver roadResolver = new RoadResolver();
        Random random = new Random();
        int bigDistance = 0;
        int smallDistance = 0;
        for (int i = 0; i < 1_000_000; i++) {
            Data data = roadResolver.get(random.nextDouble() * 1D + 46D, random.nextDouble() * 1D + 16D);
            if (data.getDistance() > 1_000_000) {
                bigDistance++;
            } else if (data.getDistance() < 100) {
                smallDistance++;
            }
        }
        System.out.println("Big distances: " + bigDistance);
        System.out.println("Small distances: " + smallDistance);
    }
}
