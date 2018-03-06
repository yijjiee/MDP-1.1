package models.robot;

import models.map.CellState;
import models.map.MapModel;

public class Sensor {
	private int range;
	private int row;
	private int col;
	private Direction sensorFace;
	
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
	}
	
	public void setSensor(int row, int col, Direction sensorFace) {
		this.row = row;
		this.col = col;
		this.sensorFace = sensorFace;
	}
	
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
			}
		}
		return -1;
	}
}
