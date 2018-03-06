package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import interfaces.MapChangedInterface;
import models.algo.Exploration;
import models.algo.FastestPath;
import models.map.Cell;
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
	private FastestPath fastestPathModel;
	private double timeLimit;
	private double coverageLimit;
	private Stack<Cell> fastestPath;
	
	
	public MainController() {
		mapListeners = new ArrayList<>();
		map = new MapModel();
		robot = new Robot(1, 1, RobotState.SIMULATION);
		
		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
				map.setCellState(i, j, CellState.NORMAL);
			}
		}
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
	
	public void runFastestPath() {
		fastestPathModel = new FastestPath(robot, MapModel.END_ROW, MapModel.END_COL, transformMap());
		fastestPath = fastestPathModel.startFastestPath();
		
		for (Cell cell : fastestPath) {
			System.out.println("Row: " + cell.getRow() + " Column: " + cell.getCol());
		}
	}
	
	public boolean addMapChangedListener(MapChangedInterface listener) {
		return mapListeners.add(listener);
	}
	
	public boolean removeMapChangedListener(MapChangedInterface listener) {
		return mapListeners.remove(listener);
	}
	
	public MapModel transformMap() {
		MapModel tempMap = new MapModel();
		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
				tempMap.setCellState(i, j, map.getCellState(i, j));
			}
		}

		for (int i = 0; i < MapModel.MAP_ROWS - 1; i++) {
			for (int j = 0; j < MapModel.MAP_COLS - 1; j++) {
				if (i == 0 && map.getCellState(i, j) == CellState.OBSTACLE) {
					tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j - 1, CellState.OBSTACLE);
				} else if (i == 19 && map.getCellState(i, j) == CellState.OBSTACLE) {
					tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j - 1, CellState.OBSTACLE);
				} else if (j == 0 && i > 0 && i < 19 && map.getCellState(i, j) == CellState.OBSTACLE) {
					tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j + 1, CellState.OBSTACLE);
				} else if (j == 14 && i > 0 && i < 19 && map.getCellState(i, j) == CellState.OBSTACLE) {
					tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j - 1, CellState.OBSTACLE);
				} else {
					if (map.getCellState(i, j) == CellState.OBSTACLE) {
						tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
						tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
						tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
						tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
						tempMap.setCellState(i + 1, j - 1, CellState.OBSTACLE);
						tempMap.setCellState(i - 1, j - 1, CellState.OBSTACLE);
						tempMap.setCellState(i + 1, j + 1, CellState.OBSTACLE);
						tempMap.setCellState(i - 1, j + 1, CellState.OBSTACLE);
					}
				}
				
				System.out.print(tempMap.getCellState(i, j) + " ");
			}
			System.out.println();
		}
		return tempMap;
	}
}
