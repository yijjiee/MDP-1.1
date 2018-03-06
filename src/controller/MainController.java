package controller;

import java.util.ArrayList;
import java.util.List;

import interfaces.MapChangedInterface;
import models.algo.Exploration;
import models.map.CellState;
import models.map.MapModel;
import models.robot.Robot;
import models.robot.RobotState;

public class MainController {
	private List<MapChangedInterface> mapListeners;
	private MapModel map;
	private MapModel cachedMap;
	private Robot robot;
	private Exploration exploration;
	private double timeLimit;
	private double coverageLimit;
	
	
	public MainController() {
		mapListeners = new ArrayList<>();
		map = new MapModel();
		robot = new Robot(1, 1, RobotState.SIMULATION);
	}

	public MapModel getMap() {
		return map;
	}
	
	public MapModel getCachedMap() {
		return cachedMap;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void setCoverageLimit(int coverageLimit) {
		this.coverageLimit = coverageLimit;
	}
	
	public void explore() {
		// 1) Cache old map state
		cachedMap = map;
		
		// 2) Create new map state with all unexplored regions
		map = new MapModel();
		
		for (int row = 0; row < MapModel.MAP_ROWS; row++) {
			for (int col = 0; col < MapModel.MAP_COLS; col++) {
				if (row < 3 && col < 3 || row > 16 && col > 11)
					map.setCellState(row, col, CellState.NORMAL);
				else
					map.setCellState(row, col, CellState.UNEXPLORED);
			}
		}
		robot.sense(cachedMap, map);
		exploration = new Exploration(map, robot, timeLimit, coverageLimit);
		exploration.startExploration();
		
		for(MapChangedInterface listener: mapListeners)
			listener.onMapChanged();
	}
	
	public boolean addMapChangedListener(MapChangedInterface listener) {
		return mapListeners.add(listener);
	}
	
	public boolean removeMapChangedListener(MapChangedInterface listener) {
		return mapListeners.remove(listener);
	}
}
