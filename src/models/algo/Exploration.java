package models.algo;

import models.map.CellState;
import models.map.Map;
import models.robot.Movement;
import models.robot.Robot;
import models.robot.RobotState;
import models.robot.Sensor;

public class Exploration {
	private Map map;
	private Robot robot;
	private Sensor NL;
	private Sensor NC;
	private Sensor NR;
	private Sensor WC;
	private Sensor EC;
	private int unexploredCells;
	
	public Exploration(Map map, Robot robot) {
		this.map = map;
		this.robot = robot;
		
		/*
		 * Coverage Limit & Time Limit 
		 */
	}
	
	public void startExploration() {
		if (robot.getState() == RobotState.PHYSICAL) {
			/*
			 * Exploration with Physical Robot
			 * Details to get from Sensors transmitted from RPI
			 */
		} else if (robot.getState() == RobotState.SIMULATION) {
			NL = robot.getNL();
			NC = robot.getNC();
			NR = robot.getNR();
			WC = robot.getWC();
			EC = robot.getEC();
		}
		
		System.out.println("Exploration started.");
		getUnexploredCells();
	
		do {
			doNextMove();
			
		} while (unexploredCells > 0);
	}
	
	private void getUnexploredCells() {
		for (int row = 0; row < Map.MAP_ROWS; row++) {
			for (int col = 0; col < Map.MAP_COLS; col++) {
				if (map.getCellState(row, col) == CellState.UNEXPLORED)
					unexploredCells++;
			}
		}
	}
	
	private void doNextMove() {
		if (!checkForwardObstacle())
			robot.move(Movement.FORWARD);
	}
	
	private boolean checkForwardObstacle() {
		if (NL.getRow() >= 0 && NL.getRow() < 19 && NL.getCol() >= 0 && NL.getCol() < 14) {
			if (map.getCellState(NL.getRow() + 1, NL.getCol()) == CellState.OBSTACLE || map.getCellState(NC.getRow() + 1, NC.getCol()) == CellState.OBSTACLE || map.getCellState(NR.getRow() + 1, NR.getCol()) == CellState.OBSTACLE)
			return true;
		else
			return false;
		}
		return true;
	}
}
