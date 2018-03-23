/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 *		Robot is a 3 x 3 cell where robot center is considered as the space taken.
 */

package models.robot;

import java.util.ArrayList;
import java.util.List;

import interfaces.RobotInterface;
import javafx.application.Platform;
import models.map.CellState;
import models.map.MapModel;

public class Robot {
	private int row;
	private int col;
	private Direction robotDir;
	
	public static final int SENSOR_SR = 4;
	public static final int SENSOR_LR = 6;
	
	/* Sensors and data can be get from ARDUINO below
	 *	Speed
	 *	Sensors:*/
	private Sensor NL, NC, NR, ET, WB, WT;
	
	private boolean finish;
	private RobotState state;
	
	private List<RobotInterface> listeners = new ArrayList<RobotInterface>();
	
	public Robot(int row, int col, RobotState state) {
		this.row = row;
		this.col = col;
		this.state = state;
		robotDir = Direction.NORTH;
		
		NL = new Sensor(SENSOR_SR, row + 1, col - 1, Direction.NORTH);
		NC = new Sensor(SENSOR_SR, row + 1, col, Direction.NORTH);
		NR = new Sensor(SENSOR_SR, row + 1, col + 1, Direction.NORTH);
		ET = new Sensor(SENSOR_LR, row + 1, col + 1, Direction.EAST);
		WB = new Sensor(SENSOR_SR, row - 1, col - 1, Direction.WEST);
		WT = new Sensor(SENSOR_SR, row + 1, col - 1, Direction.WEST);
		
//		robotDir = Direction.NORTH;
//		
//		NL = new Sensor(SENSOR_SR, row + 1, col - 1, Direction.NORTH);
//		NC = new Sensor(SENSOR_SR, row + 1, col, Direction.NORTH);
//		NR = new Sensor(SENSOR_SR, row + 1, col + 1, Direction.NORTH);
//		ET = new Sensor(SENSOR_LR, row + 1, col + 1, Direction.EAST);
//		WB = new Sensor(SENSOR_SR, row - 1, col - 1, Direction.WEST);
//		WT = new Sensor(SENSOR_SR, row + 1, col - 1, Direction.WEST);
	}

	public void setRobotPos(int row, int col) {
		this.row = row;
		this.col = col;
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
	
	public RobotState getState() {
		return state;
	}
	
	public void setState(RobotState state) {
		this.state = state;
	}
	
	public Direction getRobotDir() {
		return robotDir;
	}
	
	public Sensor getNL() {
		return NL;
	}

	public Sensor getNC() {
		return NC;
	}

	public Sensor getNR() {
		return NR;
	}

	public Sensor getET() {
		return ET;
	}
	
	public Sensor getWB() {
		return WB;
	}

	public Sensor getWT() {
		return WT;
	}
	
	public void move(Movement move) {
		switch(robotDir) {
			case NORTH:
				switch(move) {
					case FORWARD:
						row += 1; break;
					case BACKWARD:
						row -= 1; break;
					case TURNLEFT:
						robotDir = Direction.WEST; break;
					case TURNRIGHT:
						robotDir = Direction.EAST; break;
				} break;
			case SOUTH:
				switch(move) {
					case FORWARD:
						row -= 1; break;
					case BACKWARD:
						row += 1; break;
					case TURNLEFT:
						robotDir = Direction.EAST; break;
					case TURNRIGHT:
						robotDir = Direction.WEST; break;
				} break;
			case EAST:
				switch(move) {
					case FORWARD:
						col += 1; break;
					case BACKWARD:
						col -= 1; break;
					case TURNLEFT:
						robotDir = Direction.NORTH; break;
					case TURNRIGHT:
						robotDir = Direction.SOUTH; break;
				} break;
			case WEST:
				switch(move) {
					case FORWARD:
						col -= 1; break;
					case BACKWARD:
						col += 1; break;
					case TURNLEFT:
						robotDir = Direction.SOUTH; break;
					case TURNRIGHT:
						robotDir = Direction.NORTH; break;
				} break;
		}

		updateSensorsLocation();
		notifyChange();
		
		if (row == MapModel.END_ROW && col == MapModel.END_COL)
			finish = true;
	}
	
	public void sense(MapModel cachedMap, MapModel map) {
		if (cachedMap != null) {
			NL.sense(cachedMap, map);
			NC.sense(cachedMap, map);
			NR.sense(cachedMap, map);
			ET.sense(cachedMap, map);
			WB.sense(cachedMap, map);
			WT.sense(cachedMap, map);
		} else {
			NL.sense(map);
			NC.sense(map);
			NR.sense(map);
			ET.sense(map);
			WB.sense(map);
			WT.sense(map);
		}
	}
	
	public void addListeners(RobotInterface listener) {
		listeners.add(listener);
	}
	
	public void notifyChange() {
		for (RobotInterface listener : listeners)
			Platform.runLater(() -> listener.onRobotMove());
	}
	
	public void updateSensorsLocation() {
		if (robotDir == Direction.NORTH) {
			NL.setSensor(row + 1, col - 1, Direction.NORTH);
			NC.setSensor(row + 1, col, Direction.NORTH);
			NR.setSensor(row + 1, col + 1, Direction.NORTH);
			ET.setSensor(row + 1, col + 1, Direction.EAST);
			WB.setSensor(row - 1, col - 1, Direction.WEST);
			WT.setSensor(row + 1, col - 1, Direction.WEST);
		} else if (robotDir == Direction.SOUTH) {
			NL.setSensor(row - 1, col + 1, Direction.SOUTH);
			NC.setSensor(row - 1, col, Direction.SOUTH);
			NR.setSensor(row - 1, col - 1, Direction.SOUTH);
			ET.setSensor(row - 1, col - 1, Direction.WEST);
			WB.setSensor(row + 1, col + 1, Direction.EAST);
			WT.setSensor(row - 1, col + 1, Direction.EAST);
		} else if (robotDir == Direction.EAST) {
			NL.setSensor(row + 1, col + 1, Direction.EAST);
			NC.setSensor(row, col + 1, Direction.EAST);
			NR.setSensor(row - 1, col + 1, Direction.EAST);
			ET.setSensor(row - 1, col + 1, Direction.SOUTH);
			WB.setSensor(row + 1, col - 1, Direction.NORTH);
			WT.setSensor(row + 1, col + 1, Direction.NORTH);
		} else {
			NL.setSensor(row - 1, col - 1, Direction.WEST);
			NC.setSensor(row, col - 1, Direction.WEST);
			NR.setSensor(row + 1, col - 1, Direction.WEST);
			ET.setSensor(row + 1, col - 1, Direction.NORTH);
			WB.setSensor(row - 1, col + 1, Direction.SOUTH);
			WT.setSensor(row - 1, col - 1, Direction.SOUTH);			
		}
	}
}
