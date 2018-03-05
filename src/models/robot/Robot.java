/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 *		Robot is a 3 x 3 cell where robot center is considered as the space taken.
 */

package models.robot;

import models.map.Map;

public class Robot {
	private int row;
	private int col;
	private Direction robotDir;
	
	public static final int SENSOR_SRLR = 1;
	public static final int SENSOR_SRHR = 2;
	public static final int SENSOR_LRLR = 3;
	public static final int SENSOR_LRHR = 4;
	
	/* Sensors and data can be get from ARDUINO below
	 *	Speed
	 *	Sensors:
	 *		Placement of sensors
	 */
	private boolean finish;
	private RobotState state;
	
	public Robot(int row, int col, RobotState state) {
		this.row = row;
		this.col = col;
		this.state = state;
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
	
	public void move(Movement move) {
		switch(robotDir) {
			case NORTH:
				switch(move) {
					case FORWARD:
						row++; break;
					case BACKWARD:
						row--; break;
					case TURNLEFT:
						robotDir = Direction.WEST; break;
					case TURNRIGHT:
						robotDir = Direction.EAST; break;
				} break;
			case SOUTH:
				switch(move) {
					case FORWARD:
						row--; break;
					case BACKWARD:
						row++; break;
					case TURNLEFT:
						robotDir = Direction.EAST; break;
					case TURNRIGHT:
						robotDir = Direction.WEST; break;
				} break;
			case EAST:
				switch(move) {
					case FORWARD:
						col++; break;
					case BACKWARD:
						col--; break;
					case TURNLEFT:
						robotDir = Direction.NORTH; break;
					case TURNRIGHT:
						robotDir = Direction.SOUTH; break;
				} break;
			case WEST:
				switch(move) {
					case FORWARD:
						col--; break;
					case BACKWARD:
						col++; break;
					case TURNLEFT:
						robotDir = Direction.SOUTH; break;
					case TURNRIGHT:
						robotDir = Direction.NORTH; break;
				} break;
		}
		
		if (row == Map.END_ROW && col == Map.END_COL)
			finish = true;
	}
}
