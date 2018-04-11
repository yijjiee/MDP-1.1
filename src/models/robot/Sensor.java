package models.robot;

import java.util.HashSet;
import java.util.Set;

import models.map.CellState;
import models.map.MapModel;

public class Sensor {
	private int range;
	private int row;
	private int col;
	private Direction sensorFace;
	private static Set<int[]> moveHistory;
	
	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	public static void addPos(int row, int col) {
		moveHistory.add(new int[] { row, col });
	}

	public Direction getSensorFace() {
		return sensorFace;
	}

	public void setSensorFace(Direction sensorFace) {
		this.sensorFace = sensorFace;
	}
	
	public Sensor(int range, int row, int col, Direction sensorFace) {
		this.range = range;
		this.row = row;
		this.col = col;
		this.sensorFace = sensorFace;
		moveHistory = new HashSet<>();
	}
	
	public void setSensor(int row, int col, Direction sensorFace) {
		this.row = row;
		this.col = col;
		this.sensorFace = sensorFace;
	}
	
	/***
	 * For Simulation Robot
	 * @param cachedMap
	 * @param realMap
	 */
	public void sense(MapModel cachedMap, MapModel realMap) {
		switch(sensorFace) {
			case NORTH:
				getObstacle(cachedMap, realMap, 1, 0); break;
			case SOUTH:
				getObstacle(cachedMap, realMap, -1, 0); break;
			case EAST:
				getObstacle(cachedMap, realMap, 0, 1); break;	
			case WEST:
				getObstacle(cachedMap, realMap, 0, -1); break;
		}
	}
	
	/***
	 * For Physical Robot
	 * @param realMap
	 */
	public void sense(MapModel realMap) {
		switch(sensorFace) {	
			case NORTH:
				getObstacle(realMap, 1, 0); break;
			case SOUTH:
				getObstacle(realMap, -1, 0); break;
			case EAST:
				getObstacle(realMap, 0, 1); break;	
			case WEST:
				getObstacle(realMap, 0, -1); break;
		}
	}
	
	/**
	 * Simulation Robot
	 * @param cachedMap
	 * @param map
	 * @param rowInc
	 * @param colInc
	 * @return
	 */
	public int getObstacle(MapModel cachedMap, MapModel map, int rowInc, int colInc) {
		for (int i = 0; i < range; i++) {
			int x = col + (colInc*i);
			int y = row + (rowInc*i);
			
			if (x < 0 || x > 14 || y < 0 || y > 19)
				return - 1;
			
			map.setCellState(y, x, CellState.NORMAL);
			
			if (cachedMap.getCellState(y, x) == CellState.OBSTACLE) {
				map.setCellState(y, x, CellState.OBSTACLE);
				return i;
			} else if (cachedMap.getCellState(y, x) == CellState.WAYPOINT) {
				map.setCellState(y, x, CellState.WAYPOINT);
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Physical Robot
	 * @param map
	 * @param rowInc
	 * @param colInc
	 * @return
	 */
	public void getObstacle(MapModel map, int rowInc, int colInc) {
		if (range == -1) {
			int x = col + Math.abs(range*colInc);
			int y = row + Math.abs(range*rowInc);
			
			if ((x >= 0 && x <= 14) && (y >= 0 && y <= 19))
				map.setCellState(y, x, CellState.NORMAL);
		} else {
			for (int i = 1; i <= range; i++) {
				int x = col + (colInc*i);
				int y = row + (rowInc*i);
				
				if (x < 0 || x > 14 || y < 0 || y > 19)
					continue;
				
				map.setCellState(y, x, CellState.NORMAL);
	
				if (moveHistory.contains(new int[] { row, col }))
					continue;
				
				if (i == range) {
					map.setCellState(y, x, CellState.OBSTACLE);
				}
			}
		}
	}
}
