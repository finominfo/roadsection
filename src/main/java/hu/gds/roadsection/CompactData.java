package hu.gds.roadsection;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CompactData {

//    kszam – a közút száma (M1, M3, stb. is)
//    pkod – pályakód (0=nincsenek pályák; 1, 2 – bal, jobb pályák)
//    wgs84… – értelemszerű
//    km – km szelvény
//    dist – méter szelvény
//    uzemm – illetékes üzemmérnökség kódja (Vác=135)

    private static final int KEY_MULTIPLIER = 100_000;
    private static final double SIZE_MULTIPLIER = 1.5;
    private static final int START_SIZE = 100;
    private static final int TOO_CLOSE_DIFFERENCE = 10;
    private static final int FACTORY_CODE_SIZE = 3;
    private volatile int maxSize = START_SIZE;
    private final AtomicInteger currentSize = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();

    private volatile int[] roadName = null;
    private volatile byte[] runCode = null;
    private volatile long[] wgs84 = null;
    private volatile int[] meter = null;
    private volatile byte[] factoryCode = null;

    public CompactData() {
        roadName = new int[maxSize];
        runCode = new byte[maxSize];
        wgs84 = new long[maxSize];
        meter = new int[maxSize];
        factoryCode = new byte[maxSize * FACTORY_CODE_SIZE];
    }

    public void add(String[] split) {
        addToPosition(split, currentSize.getAndIncrement());
    }

    private void addToPosition(String[] split, final int position) {
        if (maxSize - position <= TOO_CLOSE_DIFFERENCE) {
            lock.lock();
            try {
                if (position >= maxSize) {
                    if (position == maxSize) {
                        extendSize();
                    }
                    addToPosition(split, position);
                    return;
                }
            } finally {
                lock.unlock();
            }
        }
        makeAdd(split, position);
    }

    private void extendSize() {
        int maxSizeNew = (int) (maxSize * SIZE_MULTIPLIER);

        int[] roadNameNew = new int[maxSizeNew];
        System.arraycopy(roadName, 0, roadNameNew, 0, roadName.length);
        roadName = roadNameNew;

        byte[] runCodeNew = new byte[maxSizeNew];
        System.arraycopy(runCode, 0, runCodeNew, 0, runCode.length);
        runCode = runCodeNew;

        long[] wgs84New = new long[maxSizeNew];
        System.arraycopy(wgs84, 0, wgs84New, 0, wgs84.length);
        wgs84 = wgs84New;

        int[] meterNew = new int[maxSizeNew];
        System.arraycopy(meter, 0, meterNew, 0, meter.length);
        meter = meterNew;

        byte[] factoryCodeNew = new byte[maxSizeNew * FACTORY_CODE_SIZE];
        System.arraycopy(factoryCode, 0, factoryCodeNew, 0, factoryCode.length);
        factoryCode = factoryCodeNew;

        maxSize = maxSizeNew;
    }

    private void makeAdd(String[] split, final int position) {
        if (split[0].toUpperCase().startsWith("M")) {
            roadName[position] = 0x40_00_00_00 | Integer.valueOf(split[0].substring(1));
        } else {
            roadName[position] = Integer.valueOf(split[0]);
        }

        runCode[position] = Byte.valueOf(split[1]);

        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        long high = Long.valueOf(df.format(Double.valueOf(split[2]) * KEY_MULTIPLIER));
        long low = Long.valueOf(df.format(Double.valueOf(split[3]) * KEY_MULTIPLIER));
        wgs84[position] = (high << 32) | low;

        meter[position] = Integer.valueOf(split[4] + split[5]);

        if (split[6].length() != FACTORY_CODE_SIZE) {
            throw new RuntimeException("Problem with FACTORY_CODE_SIZE");
        }
        System.arraycopy(factoryCode, position * FACTORY_CODE_SIZE, split[6].getBytes(), 0, FACTORY_CODE_SIZE);

    }

    public int getSize() {
        return currentSize.get();
    }

}
