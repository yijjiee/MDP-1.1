package models.algo;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import models.map.CellState;
import models.map.MapModel;
import models.robot.Movement;
import models.robot.Robot;
import models.robot.RobotState;
import models.robot.Sensor;

public class Exploration {
	private MapModel map;
	private Robot robot;
	private Sensor NL;
	private Sensor NC;
	private Sensor NR;
	private Sensor WT;
	private Sensor EC;
	private double timeLimit;
	private double coverageLimit;
	private int exploredCells;

	private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
	
	public Exploration(MapModel map, Robot robot, double timeLimit, double coverageLimit) {
		this.map = map;
		this.robot = robot;
		this.timeLimit = timeLimit;
		this.coverageLimit = coverageLimit;
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
			WT = robot.getWT();
			EC = robot.getEC();
		}

		final ScheduledFuture<?> scheduler = exec.scheduleAtFixedRate(move, 1, 250, TimeUnit.MILLISECONDS);
		
		exec.schedule(new Runnable() {
	       public void run() { scheduler.cancel(true); }
	     }, (long) timeLimit, TimeUnit.SECONDS);
		
		System.out.println("Exploration Started.");
	}
	
	final Runnable move = new Runnable() { public void run() {
		if (exploredCells < (coverageLimit/100)*300) {
			getExploredCells();
			doNextMove();
		} else {
			exec.shutdown();
			System.out.println("Exploration Ended.");
			// Fastest path back to starting point
		}
	}};
	
	private void getExploredCells() {
		exploredCells = 0;
		for (int row = 0; row < MapModel.MAP_ROWS; row++) {
			for (int col = 0; col < MapModel.MAP_COLS; col++) {
				if (map.getCellState(row, col) != CellState.UNEXPLORED)
					exploredCells++;
			}
		}
	}
	
	private void doNextMove() {
		if (!checkLeftObstacle()) {
			System.out.println("Robot is turning left.");
			robot.move(Movement.TURNLEFT);
			System.out.println("Robot is moving forward.");
			robot.move(Movement.FORWARD);
		} else if (checkForwardObstacle()) {
			System.out.println("Robot is turning right.");
				robot.move(Movement.TURNRIGHT);
		} else {
			System.out.println("Robot is moving forward.");
			robot.move(Movement.FORWARD);
		}
	}
	
	private boolean checkForwardObstacle() {
		switch (robot.getRobotDir()) {
			case NORTH:
				if (map.getCellState(NL.getRow() + 1, NL.getCol()) == CellState.OBSTACLE || map.getCellState(NC.getRow() + 1, NC.getCol()) == CellState.OBSTACLE || map.getCellState(NR.getRow() + 1, NR.getCol()) == CellState.OBSTACLE)
					return true;
				break;
			case SOUTH:
				if (map.getCellState(NL.getRow() - 1, NL.getCol()) == CellState.OBSTACLE || map.getCellState(NC.getRow() - 1, NC.getCol()) == CellState.OBSTACLE || map.getCellState(NR.getRow() - 1, NR.getCol()) == CellState.OBSTACLE)
					return true;
				break;
			case EAST:
				if (map.getCellState(NL.getRow(), NL.getCol() + 1) == CellState.OBSTACLE || map.getCellState(NC.getRow(), NC.getCol() + 1) == CellState.OBSTACLE || map.getCellState(NR.getRow(), NR.getCol() + 1) == CellState.OBSTACLE)
					return true;
				break;
			case WEST:
				if (map.getCellState(NL.getRow(), NL.getCol() - 1) == CellState.OBSTACLE || map.getCellState(NC.getRow(), NC.getCol() - 1) == CellState.OBSTACLE || map.getCellState(NR.getRow(), NR.getCol() - 1) == CellState.OBSTACLE)
					return true;
				break;
		}
		
		return false;
	}
	
	private boolean checkLeftObstacle() {
		switch (robot.getRobotDir()) {
			case NORTH:
				if (map.getCellState(NL.getRow(), NL.getCol() - 1) == CellState.OBSTACLE || map.getCellState(NL.getRow() - 1, NL.getCol() - 1) == CellState.OBSTACLE || map.getCellState(NL.getRow() - 2, NL.getCol() - 1) == CellState.OBSTACLE)
					return true;
				break;
			case SOUTH:
				if (map.getCellState(NL.getRow(), NL.getCol() + 1) == CellState.OBSTACLE || map.getCellState(NL.getRow() + 1, NL.getCol() + 1) == CellState.OBSTACLE || map.getCellState(NL.getRow() + 2, NL.getCol() + 1) == CellState.OBSTACLE)
					return true;
				break;
			case EAST:
				if (map.getCellState(NL.getRow() + 1, NL.getCol() - 2) == CellState.OBSTACLE || map.getCellState(NL.getRow() + 1, NL.getCol() - 1) == CellState.OBSTACLE || map.getCellState(NL.getRow() + 1, NL.getCol()) == CellState.OBSTACLE)
					return true;
				break;
			case WEST:
				if (map.getCellState(NL.getRow() - 1, NL.getCol() + 2) == CellState.OBSTACLE || map.getCellState(NL.getRow() - 1, NL.getCol() + 1) == CellState.OBSTACLE || map.getCellState(NL.getRow() - 1, NL.getCol()) == CellState.OBSTACLE)
					return true;
				break;
		}
		
		return false;
	}
}
