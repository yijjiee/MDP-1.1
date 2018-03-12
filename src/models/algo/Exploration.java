package models.algo;

import java.util.LinkedList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import controller.MainController;
import controller.comms.CommsController;
import models.map.Cell;
import models.map.CellState;
import models.map.MapModel;
import models.robot.Direction;
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
	private Sensor ET;
	private Sensor EB;
	private double timeLimit;
	private double coverageLimit;
	private int exploredCells;
	private FastestPath fastestPathModel;
	private LinkedList<Movement> movements;
	private CommsController commsMgr;
	
	private Timer timer;
	
	private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
	
	public Exploration(MapModel map, Robot robot, double timeLimit, double coverageLimit) {
		this.map = map;
		this.robot = robot;
		this.timeLimit = timeLimit;
		this.coverageLimit = coverageLimit;
		movements = new LinkedList<Movement>();
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
			NL = robot.getNL();
			NC = robot.getNC();
			NR = robot.getNR();
			WT = robot.getWT();
			ET = robot.getET();
			EB = robot.getEB();
			
			
		} else if (robot.getState() == RobotState.SIMULATION) {
			NL = robot.getNL();
			NC = robot.getNC();
			NR = robot.getNR();
			WT = robot.getWT();
			ET = robot.getET();
			EB = robot.getEB();

			
			timer = new Timer();
			timer.schedule(explore, 500, 150);
		}		
		System.out.println("Exploration Started.");
	}
	
	private final TimerTask explore = new TimerTask() {
		public void run() {
			getExploredCells();
			if (robot.getRow() == MapModel.START_ROW && robot.getCol() == MapModel.START_COL) {
				if (exploredCells > 100) {
					timer.cancel();
				}
				else {
					doNextMove();
				}
			} else {
				 if (exploredCells < (coverageLimit/100)*300) {
					 doNextMove();
				 } else {
					timer.cancel();
					timer = new Timer();
					startFastestPath();
					timer.schedule(moveToStart, 0, 150);
					System.out.println("Exploration ended.");
				}
			}
		}
	};
	
	private final TimerTask moveToStart = new TimerTask() {
		public void run() {
			if (robot.getRow() == 1 && robot.getCol() == 1)
				timer.cancel();
			else {
				robot.move(movements.pop());
			}
		}
	};
	
	private void startFastestPath() {
		fastestPathModel = new FastestPath(robot, MapModel.START_ROW, MapModel.START_COL, MainController.transformMap(map));
		Stack<Cell> fastestPath = fastestPathModel.startFastestPath();
		convertToDirection(fastestPath);
	}
	
	private void convertToDirection(Stack<Cell> path) {
		int robotRow = robot.getRow();
		int robotCol = robot.getCol();
		Direction robotDir = robot.getRobotDir();
		int rowDiff = robotRow - path.peek().getRow();
		int colDiff = robotCol - path.peek().getCol();
		
		robotDir = getMovementFromPos(rowDiff, colDiff, robotDir);

		while (path.size() > 1) {
			Cell tempCell = path.pop();
			rowDiff = tempCell.getRow() - path.peek().getRow();
			colDiff = tempCell.getCol() - path.peek().getCol();
			robotDir = getMovementFromPos(rowDiff, colDiff, robotDir);
		}
		System.out.println("End of Converting Path");
	}
	
	private Direction getMovementFromPos(int rowDiff, int colDiff, Direction robotDir) {
		switch (rowDiff) {
			case -1:
				switch (robotDir) {
					case NORTH:
						break;
					case SOUTH:
						movements.add(Movement.TURNLEFT);
						movements.add(Movement.TURNLEFT);
						break;
					case EAST:
						movements.add(Movement.TURNLEFT);
						break;
					case WEST:
						movements.add(Movement.TURNRIGHT);
						break;
					default: break;
				}
				movements.add(Movement.FORWARD);
				return Direction.NORTH;
			case 1:
				switch (robotDir) {
					case NORTH:
						movements.add(Movement.TURNLEFT);
						movements.add(Movement.TURNLEFT);
						break;
					case SOUTH:
						break;
					case EAST:
						movements.add(Movement.TURNRIGHT);
						break;
					case WEST:
						movements.add(Movement.TURNLEFT);
						break;
					default: break;
				}
				movements.add(Movement.FORWARD);
				return Direction.SOUTH;
		}
	
		switch (colDiff) {
			default: break;
			case -1:
				switch (robotDir) {
					case NORTH:
						movements.add(Movement.TURNRIGHT);
						break;
					case SOUTH:
						movements.add(Movement.TURNLEFT);
						break;
					case EAST:
						break;
					case WEST:
						movements.add(Movement.TURNLEFT);
						movements.add(Movement.TURNLEFT);
						break;
					default: break;
				}
				movements.add(Movement.FORWARD);
				return Direction.EAST;
			case 1:
				switch (robotDir) {
					case NORTH:
						movements.add(Movement.TURNLEFT);
						break;
					case SOUTH:
						movements.add(Movement.TURNRIGHT);
						break;
					case EAST:
						movements.add(Movement.TURNLEFT);
						movements.add(Movement.TURNLEFT);
						break;
					case WEST:
						break;
					default: break;
				}
				movements.add(Movement.FORWARD);
				return Direction.WEST;
		}
		return null;
	}
	
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
			robot.move(Movement.TURNLEFT);
			robot.move(Movement.FORWARD);
		} else if (checkForwardObstacle()) {
			robot.move(Movement.TURNRIGHT);
		} else {
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
