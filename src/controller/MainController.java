package controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import controller.comms.CommsController;
import interfaces.MapChangedInterface;
import models.algo.Exploration;
import models.algo.FastestPath;
import models.comms.CommsModel;
import models.map.Cell;
import models.map.CellState;
import models.map.MapModel;
import models.robot.Direction;
import models.robot.Movement;
import models.robot.Robot;
import models.robot.RobotState;

public class MainController {
	private List<MapChangedInterface> mapListeners;
	
	private MapModel map;
	private MapModel cachedMap;
	private Robot robot;
	private Exploration exploration;
	private FastestPath fastestPathModel;
	
	private CommsController commsMgr;
	
	private double timeLimit;
	private double coverageLimit;
	
	private Stack<Cell> fastestPath;
	private LinkedList<Movement> movements;
	private Timer timer;
	
	private String mdf1;
	private String mdf2;
	
	public MainController() {
		mapListeners = new ArrayList<>();
		map = new MapModel();
		robot = new Robot(1, 1, RobotState.SIMULATION);
		commsMgr = new CommsController();
		commsMgr.startConnection();
		
		
		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
				map.setCellState(i, j, CellState.NORMAL);
			}
		}
	}

	public void setMdf1(String mdf1) {
		this.mdf1 = mdf1;
	}

	public void setMdf2(String mdf2) {
		this.mdf2 = mdf2;
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
		if (robot.getState() == RobotState.SIMULATION) {
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
			if (robot.getCol() != 1 || robot.getRow() != 1)
				fastestPathModel = new FastestPath(robot, MapModel.START_ROW, MapModel.START_COL, transformMap(map));

			for(MapChangedInterface listener: mapListeners)
				listener.onMapChanged();
		} else {
			if (commsMgr.isConnected()) {
//			Runnable task = new Runnable() {
//				public void run() {
//					String rtnMsg = commsMgr.startRecvMsg();
//					
//					
//					if (!rtnMsg.equals(null)) {
//						System.out.println(rtnMsg);
//						String [] sensorsData = rtnMsg.split(";");
//						robot.getNL().setRange(Integer.valueOf(sensorsData[0]));
//						robot.getNC().setRange(Integer.valueOf(sensorsData[1]));
//						robot.getNR().setRange(Integer.valueOf(sensorsData[2]));
//						robot.getET().setRange(Integer.valueOf(sensorsData[3]));
//						robot.getEB().setRange(Integer.valueOf(sensorsData[4]));
//						robot.getWT().setRange(Integer.valueOf(sensorsData[5]));
//					}
//					robot.sense(null, map);
//				}
//			};
//			
//			Thread t = new Thread(task);
//			t.start();
			
				for (int row = 0; row < MapModel.MAP_ROWS; row++) {
					for (int col = 0; col < MapModel.MAP_COLS; col++) {
						if (row < 3 && col < 3 || row > 16 && col > 11)
							map.setCellState(row, col, CellState.NORMAL);
						else
							map.setCellState(row, col, CellState.UNEXPLORED);
					}
				}
				
				//Pre-exploration
				//For Android
				String rDir = "";
				switch(robot.getRobotDir()) {
					case NORTH:
						rDir = "1";
						break;
					case SOUTH:
						rDir = "2";
						break;
					case EAST:
						rDir = "3";
						break;
					case WEST:
						rDir = "4";
						break;
				}
				commsMgr.sendMessage("mdf1;" + mdf1, CommsModel.MSG_TO_ANDROID);
				commsMgr.sendMessage("mdf2;" + mdf2, CommsModel.MSG_TO_ANDROID);
				commsMgr.sendMessage("rposdir;" + robot.getRow() + ";" + robot.getCol() + ";" + rDir, CommsModel.MSG_TO_ANDROID);
				
				//For Arduino
				commsMgr.sendMessage("S", CommsModel.MSG_TO_BOT);
				String rtnMsg = commsMgr.startRecvMsg();
				
				
				if (!rtnMsg.equals(null)) {
					System.out.println(rtnMsg);
					String [] sensorsData = rtnMsg.split(";");
					robot.getNL().setRange(Integer.valueOf(sensorsData[0]));
					robot.getNC().setRange(Integer.valueOf(sensorsData[1]));
					robot.getNR().setRange(Integer.valueOf(sensorsData[2]));
					robot.getET().setRange(Integer.valueOf(sensorsData[3]));
					robot.getWB().setRange(Integer.valueOf(sensorsData[4]));
					robot.getWT().setRange(Integer.valueOf(sensorsData[5]));
				}
				
				robot.sense(null, map);
				exploration = new Exploration(map, robot, timeLimit, coverageLimit);
				exploration.setCommsMgr(commsMgr);
				exploration.startExploration();
			}
		}
	}
	
	public void runFastestPath() {
		movements = new LinkedList<Movement>();
		timer = new Timer();
		Cell wayPoint = null;
		for (int row = 0; row < MapModel.MAP_ROWS; row++) {
			for (int col = 0; col < MapModel.MAP_COLS; col++) {
				if (map.getCellState(row, col) == CellState.WAYPOINT)
					wayPoint = map.getCell(row, col);
			}
		}
		
		if (wayPoint != null)
			fastestPathModel = new FastestPath(robot, wayPoint.getRow(), wayPoint.getCol(), transformMap(map));
		else
			fastestPathModel = new FastestPath(robot, MapModel.END_ROW, MapModel.END_COL, transformMap(map));
		fastestPath = fastestPathModel.startFastestPath();
		convertToDirection(fastestPath);
		timer.schedule(moveToPoint, 500, 150);
	}
	
	private final TimerTask moveToPoint = new TimerTask() {
		public void run() {
			if (robot.getRow() == fastestPathModel.getEndRow() && robot.getCol() == fastestPathModel.getEndCol()) {
				timer.cancel();
				if (robot.getRow() != MapModel.END_ROW || robot.getCol() != MapModel.END_COL) {
					timer = new Timer();
					movements.clear();
					fastestPathModel = new FastestPath(robot, MapModel.END_ROW, MapModel.END_COL, transformMap(map));
					fastestPath = fastestPathModel.startFastestPath();
					convertToDirection(fastestPath);
					timer.schedule(moveToEnd, 250, 150);
				}
			} else
				robot.move(movements.pop());
		}
	};
	
	private final TimerTask moveToEnd = new TimerTask() {
		public void run() {
			if (robot.getRow() == fastestPathModel.getEndRow() && robot.getCol() == fastestPathModel.getEndCol())
				timer.cancel();
			else
				robot.move(movements.pop());
		}
	};
	
	public boolean addMapChangedListener(MapChangedInterface listener) {
		return mapListeners.add(listener);
	}
	
	public boolean removeMapChangedListener(MapChangedInterface listener) {
		return mapListeners.remove(listener);
	}
	
	public static MapModel transformMap(MapModel map) {
		MapModel tempMap = new MapModel();
		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
				tempMap.setCellState(i, j, map.getCellState(i, j));
			}
		}

		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
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
			}
		}
		return tempMap;
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
}
