package com.example.mymessenger;

import java.util.List;

public class DataFormater {
    public static String formater(String time) {
        Long inttime = Long.parseLong(time);
        inttime = inttime / 1000;
        long seconds = inttime % 60;
        inttime = inttime / 60;
        long day = inttime / 1440;
        long hour = (inttime  % 1440) / 60;
        long minutes = (inttime % 1440) % 60;
        return day + ":" + String.format("%2s", hour).replace(' ', '0') + ":" +
                String.format("%2s", minutes).replace(' ', '0') + ":" +
                String.format("%2s", seconds).replace(' ', '0');
    }

    public static String bd_formater(String time){
        String[] times = time.split(":");
        return times[1] + ":" + times[2];
    }
}
