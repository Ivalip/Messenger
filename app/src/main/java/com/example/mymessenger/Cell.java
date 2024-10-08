package com.example.mymessenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cell {
    Long CumulativeDelay = Long.MAX_VALUE;
    String PreviousCell = "";
    ArrayList<String> Path = new ArrayList<String>();
    Boolean isSettled = false;
    String MyUuid;
    Map<String, Long> connectios = new HashMap<>();

    public Cell(String myUuid) {
        MyUuid = myUuid;
    }

    void addConnection(String node, Long delay){
        connectios.put(node, delay);
    }

    void startCellInit(){
        this.CumulativeDelay = 0L;
    }
}
