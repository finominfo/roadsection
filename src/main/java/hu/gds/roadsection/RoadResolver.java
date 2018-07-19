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
            for (int i = 0; i <= 0xffff; i++)  roadSections.put(i, new FixedSizeCompactData(temporarySizeMeasurement.get(i).get()));
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
        int key = CRC16.get(df.format(wgs84Latitude * KEY_MULTIPLIER) + df.format(wgs84Longitude * KEY_MULTIPLIER));
        return roadSections.get(key).get(wgs84Latitude, wgs84Longitude);
    }


    public static void main(String[] args) {
        RoadResolver roadResolver = new RoadResolver();
        Random random = new Random();
        for (int i = 0; i < 10_000_000; i ++) {
            roadResolver.get(random.nextDouble() + 46D, random.nextDouble() + 16D);
        }
    }
}
