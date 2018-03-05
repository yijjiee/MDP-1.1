/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 *		Robot is a 3 x 3 cell where robot center is considered as the space taken.
 */

package models.robot;

import java.util.ArrayList;
import java.util.List;

import interfaces.RobotInterface;
import models.map.Map;

public class Robot {
	private int row;
	private int col;
	private Direction robotDir;
	
	public static final int SENSOR_SR = 4;
	public static final int SENSOR_LR = 6;
	
	/* Sensors and data can be get from ARDUINO below
	 *	Speed
	 *	Sensors:*/
	private Sensor NL, NC, NR, WC, EC;
	
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
		WC = new Sensor(SENSOR_SR, row, col - 1, Direction.WEST);
		EC = new Sensor(SENSOR_LR, row + 1, col + 1, Direction.EAST);
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

	public Sensor getWC() {
		return WC;
	}

	public Sensor getEC() {
		return EC;
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
		
		if (row == Map.END_ROW && col == Map.END_COL)
			finish = true;
	}
	
	public void sense(Map cachedMap, Map map) {
		NL.sense(cachedMap, map);
		NC.sense(cachedMap, map);
		NR.sense(cachedMap, map);
		WC.sense(cachedMap, map);
		EC.sense(cachedMap, map);
	}
	
	public void addListeners(RobotInterface listener) {
		listeners.add(listener);
	}
	
	public void notifyChange() {
		for (RobotInterface listener : listeners)
			listener.onRobotMove();
	}
	
	public void updateSensorsLocation() {
		if (robotDir == Direction.NORTH) {
			NL.setSensor(row + 1, col - 1, Direction.NORTH);
			NC.setSensor(row + 1, col, Direction.NORTH);
			NR.setSensor(row + 1, col + 1, Direction.NORTH);
			WC.setSensor(row, col - 1, Direction.WEST);
			EC.setSensor(row + 1, col + 1, Direction.EAST);
		} else if (robotDir == Direction.SOUTH) {
			NL.setSensor(row - 1, col + 1, Direction.SOUTH);
			NC.setSensor(row - 1, col, Direction.SOUTH);
			NR.setSensor(row - 1, col - 1, Direction.SOUTH);
			WC.setSensor(row, col + 1, Direction.EAST);
			EC.setSensor(row - 1, col - 1, Direction.WEST);
		} else if (robotDir == Direction.EAST) {
			NL.setSensor(row + 1, col + 1, Direction.EAST);
			NC.setSensor(row, col + 1, Direction.EAST);
			NR.setSensor(row - 1, col + 1, Direction.EAST);
			WC.setSensor(row + 1, col, Direction.NORTH);
			EC.setSensor(row - 1, col + 1, Direction.SOUTH);
		} else {
			NL.setSensor(row - 1, col - 1, Direction.WEST);
			NC.setSensor(row, col - 1, Direction.WEST);
			NR.setSensor(row + 1, col - 1, Direction.WEST);
			WC.setSensor(row - 1, col, Direction.SOUTH);
			EC.setSensor(row + 1, col - 1, Direction.NORTH);
		}
	}
}
