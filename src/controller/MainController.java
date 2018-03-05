package controller;

import java.util.ArrayList;
import java.util.List;

import interfaces.MapChangedInterface;
import models.map.CellState;
import models.map.Map;
import models.robot.Robot;
import models.robot.RobotState;

public class MainController {
	private List<MapChangedInterface> listeners;
	private Map map;
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
