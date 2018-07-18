package hu.gds.roadsection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class RoadResolver {
    private final static int KEY_MULTIPLIER = 10_000;
    private final static String CSV = "./split.csv";
    private final static String SPLIT = ";";
    private final Map<Integer, CompactData> roadSections = new HashMap<>();
    private volatile long resultValue = 0;


    public RoadResolver() {
        BufferedReader br = null;
        DecimalFormat df = new DecimalFormat("#");
        try {
            df.setRoundingMode(RoundingMode.HALF_UP);
            br = new BufferedReader(new FileReader(new File(CSV)));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] split = line.split(SPLIT);
                int key = CRC16.get(df.format(Double.valueOf(split[2]) * KEY_MULTIPLIER) + df.format(Double.valueOf(split[3]) * KEY_MULTIPLIER));
                CompactData compactData = roadSections.get(key);
                if (compactData == null) {
                    compactData = new CompactData();
                    roadSections.put(key, compactData);
                }
                compactData.add(split);
            }
            br.close();
            System.out.println(roadSections.size());
            int count = 0;
            for (CompactData compactData : roadSections.values()) {
                count += compactData.getSize();
            }
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new RoadResolver();
    }
}
