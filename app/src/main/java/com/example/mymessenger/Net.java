package com.example.mymessenger;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;


public class Net {
    String MyUuid;
    NotificationService notificationService;
    String sender;
    final Net NET = this;
    Map<String, Long> cells = new HashMap<>();
    Map<String, Cell> graph = new HashMap<>();



    public Net(NotificationService notificationService, String MyUuid) {
        this.MyUuid = MyUuid;
        this.notificationService = notificationService;
    }

    public Net(NotificationService notificationService, String MyUuid, String graph) {
        this.MyUuid = MyUuid;
        this.notificationService = notificationService;
        while (!graph.isEmpty()){
            String cellId = graph.substring(0, graph.indexOf("{"));
            Cell cell = new Cell(cellId);
            String connections = graph.substring(graph.indexOf("{")+1, graph.indexOf("}"));
            while(!connections.isEmpty()){
                String cell2 = connections.substring(connections.indexOf("[") + 1, connections.indexOf(","));
                Long delay = Long.parseLong(connections.substring(connections.indexOf(",") + 1, connections.indexOf("]")));
                cell.addConnection(cell2, delay);
                connections = connections.substring(connections.indexOf("]") + 1);
            }
            this.graph.put(cellId, cell);
            graph = graph.substring(graph.indexOf("}") + 1);
        }// graph parsing
        this.Dejkstra(MyUuid);
    }

    void build() throws IOException {
        notificationService.sendMessage(MyUuid, "", MyUuid, "SYS");
        new Timer().schedule(new TimerTask() { @Override public void run() {
            if(graph.get(MyUuid) == null){
                Cell def = new Cell(MyUuid);
                graph.put(MyUuid, def);
            }
            NET.Dejkstra(MyUuid); // deikstra
            ArrayList<String> graphKeys = new ArrayList<>(graph.keySet());
            for (int i = 0; i < graphKeys.size(); i++) {
                NET.sendGraph(graphKeys.get(i));
            } // send graph
            Log.d("GRAPH", NET.graphToString(graph));
        }}, 15000);
    }

    void construct(String message) {
        ArrayList<String> nodes = new ArrayList<>(Arrays.asList(message.split(",")));
        nodes.remove(nodes.size()-1);
        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(message.split("\\|")));
        ArrayList<Long> delays = tmp.subList(0, tmp.size()-1).stream()
                .map(Long::parseLong).collect(Collectors.toCollection(ArrayList::new));
        while(!delays.isEmpty() && nodes.size()>1) {
            String node = nodes.get(nodes.size()-1);
            nodes.remove(nodes.size()-1);
            String node2 = nodes.get(nodes.size()-1);
            Long delay = delays.get(0);
            delays.remove(0);
            if(graph.get(node) == null){
                Cell cell = new Cell(node);
                graph.put(node, cell);
            }
            if(!graph.get(node).connectios.containsKey(node2)) {
                graph.get(node).addConnection(node2, delay);
            }
            if(graph.get(node2) == null){
                Cell cell = new Cell(node);
                graph.put(node, cell);
            }
            if(!graph.get(node2).connectios.containsKey(node)){
                graph.get(node2).addConnection(node, delay);
            }
        }
    }

    void Dejkstra(String startcell){
        graph.get(startcell).startCellInit();
        ArrayList<Cell> unsettledCells = new ArrayList<>();
        Cell currentCell = graph.get(startcell);
        do {
            ArrayList<String> connectedCells = new ArrayList<>(currentCell.connectios.keySet());
            for (int i = 0; i < connectedCells.size(); ++i) {
                Cell node = graph.get(connectedCells.get(i));
                if(!node.isSettled) {
                    Long delay = currentCell.connectios.get(node.MyUuid);
                    Long newDelay = delay + currentCell.CumulativeDelay;
                    if (node.CumulativeDelay > newDelay) {
                        node.CumulativeDelay = newDelay;
                        node.PreviousCell = currentCell.MyUuid;
                    }
                    unsettledCells.add(node);
                }
            }
            currentCell.isSettled = true;
            if(!unsettledCells.isEmpty()) {
                currentCell = getMinCell(unsettledCells);
            }// getting closet unsettled cell
        } while(!unsettledCells.isEmpty());
        this.buildRoutes();
    }

    void buildRoutes(){
        ArrayList<String> graphKeys = new ArrayList<>(graph.keySet());
        for (int i = 0; i < graphKeys.size(); i++) {
            Cell currentCell = graph.get(graphKeys.get(i));
            Cell tmpCell = currentCell;
            while (!tmpCell.PreviousCell.isEmpty()){
                currentCell.Path.add(0, tmpCell.MyUuid);
                tmpCell = graph.get(tmpCell.PreviousCell);
            }
        }
    }

    Cell getMinCell(ArrayList<Cell> unsettledCells){
        Cell cell = unsettledCells.get(0);
        for(int i = 0; i < unsettledCells.size(); ++i){
            if(unsettledCells.get(i).CumulativeDelay < cell.CumulativeDelay){
                cell = unsettledCells.get(i);
            }
        }
        return cell;
    }

    void sendGraph(String target){
        String recieverID = String.join(",", graph.get(target).Path);
        try {
            notificationService.sendMessage("GRAPH" + graphToString(graph), recieverID, MyUuid, "SYS");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    String graphToString(Map<String, Cell> graph){
        String str = "";
        ArrayList<String> graphKeys = new ArrayList<>(graph.keySet());
        for (int i = 0; i < graphKeys.size(); i++) {
            Cell cell = graph.get(graphKeys.get(i));
            str += cell.MyUuid + "{";
            ArrayList<String> connectionKeys = new ArrayList<>(cell.connectios.keySet());
            for (int j = 0; j < connectionKeys.size(); j++) {
                str += "[" + connectionKeys.get(j) + "," + cell.connectios.get(connectionKeys.get(j)) + "]";
            }
            str += "}";
        }
        return str;
    }
}