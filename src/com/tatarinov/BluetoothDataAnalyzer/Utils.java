package com.tatarinov.BluetoothDataAnalyzer;

import java.util.ArrayList;

public class Utils {
    public static int getCRC(ArrayList<Integer> data) {
        int crc = 0;

        if(data.size() == 4) {
            return crc ^ data.get(0) ^ data.get(1) ^ data.get(2) ^ data.get(3);
        }else {
            return crc;
        }
    }
}
