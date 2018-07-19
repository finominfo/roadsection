package hu.gds.roadsection;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedSizeCompactData {

//    kszam – a közút száma (M1, M3, stb. is)
//    pkod – pályakód (0=nincsenek pályák; 1, 2 – bal, jobb pályák)
//    wgs84… – értelemszerű
//    km – km szelvény
//    dist – méter szelvény
//    uzemm – illetékes üzemmérnökség kódja (Vác=135)

    private static final int KEY_MULTIPLIER = 100_000;
    private static final int FACTORY_CODE_SIZE = 3;
    private static final int CODE_FOR_M = 0x40_00_00_00;
    private final AtomicInteger currentSize = new AtomicInteger(0);

    private final int[] roadName;
    private final byte[] runCode;
    private final long[] wgs84;
    private final int[] meter;
    private final byte[] factoryCode;

    public FixedSizeCompactData(int maxSize) {
        roadName = new int[maxSize];
        runCode = new byte[maxSize];
        wgs84 = new long[maxSize];
        meter = new int[maxSize];
        factoryCode = new byte[maxSize * FACTORY_CODE_SIZE];
    }

    public void add(String[] split) {
        final int position = currentSize.getAndIncrement();

        if (split[0].toUpperCase().startsWith("M")) {
            roadName[position] = CODE_FOR_M | Integer.valueOf(split[0].substring(1));
        } else {
            roadName[position] = Integer.valueOf(split[0]);
        }

        runCode[position] = Byte.valueOf(split[1]);

        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        long high = Long.valueOf(df.format(Double.valueOf(split[2]) * KEY_MULTIPLIER));
        long low = Long.valueOf(df.format(Double.valueOf(split[3]) * KEY_MULTIPLIER));
        wgs84[position] = (high << 32) | low;

        meter[position] = Integer.valueOf(split[4]) * 1000 + Integer.valueOf(split[5]);

        if (split[6].length() != FACTORY_CODE_SIZE) {
            throw new RuntimeException("Problem with FACTORY_CODE_SIZE");
        }
        System.arraycopy(split[6].getBytes(), 0, factoryCode, position * FACTORY_CODE_SIZE, FACTORY_CODE_SIZE);
    }

    public Data get(double wgs84Latitude, double wgs84Longitude) {
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        long latitude = Long.valueOf(df.format(wgs84Latitude * KEY_MULTIPLIER));
        long longitude = Long.valueOf(df.format(wgs84Longitude * KEY_MULTIPLIER));
        long minimumDistance = Long.MAX_VALUE;
        int minimumPosition = 0;
        for (int i = 0; i < currentSize.get(); i ++) {
            long currentLatitude = wgs84[i] >>> 32;
            long currentLongitude = wgs84[i] & 0xFF_FF_FF_FFL;
            long diffLatitude = latitude - currentLatitude;
            long diffLongitude = longitude - currentLongitude;
            long distance = diffLatitude * diffLatitude + diffLongitude * diffLongitude;
            if (distance < minimumDistance) {
                minimumDistance = distance;
                minimumPosition = i;
            }
        }
        String retRoadName = (roadName[minimumPosition] & CODE_FOR_M) == CODE_FOR_M
                ? "M" + (roadName[minimumPosition] ^ CODE_FOR_M)
                : "" + roadName[minimumPosition];
        String retFactoryCode = new String(factoryCode, minimumPosition * FACTORY_CODE_SIZE, FACTORY_CODE_SIZE);
        return new Data(retRoadName, runCode[minimumPosition], meter[minimumPosition], retFactoryCode, minimumDistance);
    }

    public int getSize() {
        return currentSize.get();
    }

}
