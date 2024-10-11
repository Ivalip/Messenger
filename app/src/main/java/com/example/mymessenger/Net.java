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
        Log.d("GRAPH", NET.graphToString(this.graph));
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
            Log.d("BUILD", "graphKeys: "+graphKeys);
            for (int i = 0; i < graphKeys.size(); i++) {
                if(!graphKeys.get(i).equals(MyUuid)) {
                    Log.d("BUILD", "sendingTO: " + graphKeys.get(i));
                    NET.sendGraph(graphKeys.get(i));
                }
            } // send graph
            Log.d("GRAPH", NET.graphToString(graph));
        }}, 15000);
    }

    void construct(String message) {
        if (message.contains("REBOUND")){
            message = message.substring(message.indexOf("REBOUND")+7);
        }
        Log.d("GRAPHMSG", message);
        ArrayList<String> nodes = new ArrayList<>(Arrays.asList(message.split(",")));
        String newel = Arrays.asList(nodes.get(nodes.size()-1).split("\\|")).get(0);
        nodes.remove(nodes.size()-1);
        nodes.add(newel);
        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(message.split("\\|")));
        Log.d("GRAPHMSG", tmp.subList(1, tmp.size()).toString());
        ArrayList<Long> delays = tmp.subList(1, tmp.size()).stream()
                .map(Long::parseLong).collect(Collectors.toCollection(ArrayList::new));
        Log.d("GRAPHRAW", "NODES: " + nodes.toString() + "\nDELAYS: " + delays.toString());
        while(!delays.isEmpty()) {
            String node = nodes.get(nodes.size()-1);
            nodes.remove(nodes.size()-1);
            String node2 = nodes.get(nodes.size()-1);
            Long delay = delays.get(0);
            delays.remove(0);
            if(graph.get(node) == null) {
                Cell cell = new Cell(node);
                graph.put(node, cell);
            }
            if(!graph.get(node).connectios.containsKey(node2)) {
                graph.get(node).addConnection(node2, delay);
            }
            if(graph.get(node2) == null) {
                Cell cell = new Cell(node2);
                graph.put(node2, cell);
            }
            if(!graph.get(node2).connectios.containsKey(node)){
                graph.get(node2).addConnection(node, delay);
            }
        }
        Log.d("MARK", "connection() finished");
    }

    void Dejkstra(String startcell){
        Log.d("MARK", "Dejkstra() started");
        graph.get(startcell).startCellInit();
        Log.d("Dejkstra", "Startcell: "+startcell);
        ArrayList<Cell> unsettledCells = new ArrayList<>();
        Cell currentCell = graph.get(startcell);
        do {
            Log.d("Dejkstra", "CurrentCell: "+currentCell.MyUuid);
            ArrayList<String> connectedCells = new ArrayList<>(currentCell.connectios.keySet());
            Log.d("Dejkstra", "ConnectedCells: "+connectedCells.toString());
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
                unsettledCells.remove(currentCell);
            }
            Log.d("Dejkstra", "CurrentCellStatus: "+graph.get(currentCell.MyUuid).isSettled);
            if(!unsettledCells.isEmpty()) {
                currentCell = getMinCell(unsettledCells);
            }// getting closet unsettled cell
        } while(!unsettledCells.isEmpty());
        this.buildRoutes();
        Log.d("MARK", "Dejkstra() finished");
    }

    void buildRoutes(){
        Log.d("MARK", "buildRoutes() started");
        ArrayList<String> graphKeys = new ArrayList<>(graph.keySet());
        for (int i = 0; i < graphKeys.size(); i++) {
            Cell currentCell = graph.get(graphKeys.get(i));
            Cell tmpCell = currentCell;
            while (!tmpCell.PreviousCell.isEmpty()){
                currentCell.Path.add(0, tmpCell.MyUuid);
                tmpCell = graph.get(tmpCell.PreviousCell);
            }
        }
        Log.d("MARK", "buildRoutes() finished");
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
        String receiverID = String.join(",", graph.get(target).Path);
        Log.d("SendGRAPH", "reciever: "+receiverID);
        try {
            notificationService.sendMessage("GRAPH" + graphToString(graph), receiverID, MyUuid, "SYS");
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