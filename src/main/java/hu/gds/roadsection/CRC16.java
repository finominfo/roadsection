package hu.gds.roadsection;

public class CRC16 {

        public static int get(String input) {
            byte[] bytes = input.getBytes();
            int value = 0;
            for (byte byte1 : bytes) {
                int a = (int) byte1;
                int b;
                for (int count = 7; count >= 0; count--) {
                    a = a << 1;
                    b = (a >>> 8) & 1;
                    if ((value & 0x8000) != 0) {
                        value = ((value << 1) + b) ^ 0x1021;
                    } else {
                        value = (value << 1) + b;
                    }
                }
                value = value & 0xffff;
            }
            return value;
        }
    }
