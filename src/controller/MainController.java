package controller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import interfaces.MapChangedInterface;
import models.map.CellState;
import models.map.Map;
import models.robot.Movement;
import models.robot.Robot;
import models.robot.RobotState;
import models.robot.Sensor;

public class MainController {
	private List<MapChangedInterface> listeners;
	private Map map;
	private Map cachedMap;
	private Robot robot;
	
	public MainController() {
		listeners = new ArrayList<>();
		map = new Map();
		robot = new Robot(1, 1, RobotState.SIMULATION);
	}
	
	public Map getMap() {
		return map;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public void explore() {
		// 1) Cache old map state
		cachedMap = map;
		
		// 2) Create new map state with all unexplored regions
		map = new Map();
		
		for (int row = 0; row < Map.MAP_ROWS; row++) {
			for (int col = 0; col < Map.MAP_COLS; col++) {
				if (row < 3 && col < 3 || row > 16 && col > 11)
					map.setCellState(row, col, CellState.NORMAL);
				else
					map.setCellState(row, col, CellState.UNEXPLORED);
			}
		}
		
		if (robot.getState() == RobotState.SIMULATION) {
			Hashtable<Sensor, Integer> sensorNval = robot.sense(cachedMap, map);
			for (Sensor sensor: sensorNval.keySet()) {
			}
		}
		
		for(MapChangedInterface listener: listeners)
			listener.onMapChanged();
	}
	
	public boolean addMapChangedListener(MapChangedInterface listener) {
		return listeners.add(listener);
	}
	
	public boolean removeMapChangedListener(MapChangedInterface listener) {
		return listeners.remove(listener);
	}
 	
}
