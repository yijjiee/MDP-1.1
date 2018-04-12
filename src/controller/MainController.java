package controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import controller.comms.CommsController;
import interfaces.MapChangedInterface;
import javafx.application.Platform;
import models.algo.Exploration;
import models.algo.FastestPath;
import models.comms.CommsModel;
import models.map.Cell;
import models.map.CellState;
import models.map.MapModel;
import models.robot.Direction;
import models.robot.Movement;
import models.robot.Position;
import models.robot.Robot;
import models.robot.RobotState;
import ui.Main;

public class MainController {
	private List<MapChangedInterface> mapListeners;
	
	private MapModel map;
	private MapModel cachedMap;
	private Robot robot;
	private Exploration exploration;
	private FastestPath fastestPathModel;
	private Position wayPoint;
	
	private CommsController commsMgr;
	
	private double timeLimit;
	private double coverageLimit;
	
	private Stack<Cell> fastestPath;
	private LinkedList<Movement> movements;
	private Timer timer;
	
	private String mdf1;
	private String mdf2;
	
	private boolean simulation = false;
	
	public MainController() {
		mapListeners = new ArrayList<>();
		map = new MapModel();
		robot = new Robot(1, 1, RobotState.SIMULATION);
		if (!simulation) {
			commsMgr = new CommsController();
			Thread t = new Thread(new Runnable() {
				public void run() {
					commsMgr.startConnection();
					String msg = null;
					if (commsMgr.isConnected()) {
						commsMgr.sendMessage("setrobot:1,1,1/", CommsModel.MSG_TO_ANDROID);
						msg = commsMgr.startRecvMsg();
					}
					
					if (msg.equals(CommsModel.MSG_TO_ANDROID + "se")) {
						robot.setState(RobotState.PHYSICAL);
						explore();
						msg = commsMgr.startRecvMsg();
					}
					
					if (msg.contains("#wp:")) {
						msg = msg.substring(4, msg.length());
						String [] wpPos = msg.split(",");
						map.setCellState(Integer.valueOf(wpPos[0]), Integer.valueOf(wpPos[1]), CellState.WAYPOINT);
						wayPoint = new Position(Integer.valueOf(wpPos[0]), Integer.valueOf(wpPos[1]));
					}
					
					msg = commsMgr.startRecvMsg();
					
					if (msg.equals(CommsModel.MSG_TO_ANDROID + "sfp")) {
						runFastestPath();
					}
				}
			});
			t.start();
		}
		
		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
				map.setCellState(i, j, CellState.NORMAL);
			}
		}
	}
	
	public boolean getSimulation() {
		return simulation;
	}
	
	public void setWayPoint(Position wayPoint) {
		this.wayPoint = wayPoint;
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
	
	public CommsController getCommsMgr() {
		return commsMgr;
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
			String rtnMsg = null;
			
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
//			if (!rtnMsg.equals(null)) {
//				System.out.println(rtnMsg);
//				String [] sensorsData = rtnMsg.split(";");
//				robot.getNL().setRange(Integer.valueOf(sensorsData[0]));
//				robot.getNC().setRange(Integer.valueOf(sensorsData[1]));
//				robot.getNR().setRange(Integer.valueOf(sensorsData[2]));
//				robot.getET().setRange(Integer.valueOf(sensorsData[3]));
//				robot.getWB().setRange(Integer.valueOf(sensorsData[4]));
//				robot.getWT().setRange(Integer.valueOf(sensorsData[5]));
//			}
//			robot.sense(null, map);
			
			for (int row = 0; row < MapModel.MAP_ROWS; row++) {
				for (int col = 0; col < MapModel.MAP_COLS; col++) {
					if (row < 3 && col < 3 || row > 16 && col > 11)
						map.setCellState(row, col, CellState.NORMAL);
					else
						map.setCellState(row, col, CellState.UNEXPLORED);
				}
			}
			commsMgr.sendMessage("setrobot:" + robot.getCol() + "," + robot.getRow() + "," + rDir + "/", CommsModel.MSG_TO_ANDROID);
			commsMgr.sendMessage("mdf1:" + mdf1 + "/", CommsModel.MSG_TO_ANDROID);
			commsMgr.sendMessage("mdf2:" + mdf2 + "/", CommsModel.MSG_TO_ANDROID);

			//Pre-exploration
			//For Android
			
			//For Arduino
			commsMgr.sendMessage("s", CommsModel.MSG_TO_BOT);
			rtnMsg = commsMgr.startRecvMsg();
			
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
			exploration = new Exploration(map, robot, 360, 100);
			exploration.setCommsMgr(commsMgr);
			exploration.startExploration();
			
//			rtnMsg = commsMgr.startRecvMsg();
//			System.out.println(rtnMsg);
//			if (rtnMsg.equals("#sfp"))
//				startFastestPath();
		}
	}
	
	public void runFastestPath() {
		if (commsMgr != null) {
			movements = new LinkedList<Movement>();
			String moveStr = "";
			if (wayPoint != null) {
				fastestPathModel = new FastestPath(robot, wayPoint.getRow(), wayPoint.getCol(), transformMap(map));
				fastestPath = fastestPathModel.startFastestPath();
				Direction rDir = convertToDirection(fastestPath);
				while (!movements.isEmpty()) {
					switch(movements.pop()) {
						case TURNLEFT: moveStr += "l"; robot.move(Movement.TURNLEFT); break;
						case TURNRIGHT: moveStr += "r"; robot.move(Movement.TURNRIGHT); break;
						case FORWARD: moveStr += "f"; robot.move(Movement.FORWARD); break;
						case BACKWARD: moveStr += "b"; robot.move(Movement.BACKWARD); break;
					}
				}
				System.out.println(moveStr);
//				robot.setRobotPos(wayPoint.getRow(), wayPoint.getCol());
//				robot.setRobotDir(rDir);
//				robot.updateSensorsLocation();
				movements.clear();
				fastestPathModel = new FastestPath(robot, MapModel.END_ROW, MapModel.END_COL, transformMap(map));
				fastestPath = fastestPathModel.startFastestPath();
				rDir = convertToDirection(fastestPath);
				while (!movements.isEmpty()) {
					switch(movements.pop()) {
						case TURNLEFT: moveStr += "l"; robot.move(Movement.TURNLEFT); break;
						case TURNRIGHT: moveStr += "r"; robot.move(Movement.TURNRIGHT); break;
						case FORWARD: moveStr += "f"; robot.move(Movement.FORWARD); break;
						case BACKWARD: moveStr += "b"; robot.move(Movement.BACKWARD); break;
					}
				}
				if (rDir == Direction.NORTH)
					moveStr += "w";
				else if (rDir == Direction.EAST)
					moveStr += "u";
				
				commsMgr.sendMessage(moveStr, CommsModel.MSG_TO_BOT);
			} else {
				movements.clear();
				fastestPathModel = new FastestPath(robot, MapModel.END_ROW, MapModel.END_COL, transformMap(map));
				fastestPath = fastestPathModel.startFastestPath();
				convertToDirection(fastestPath);
				while (!movements.isEmpty()) {
					switch(movements.pop()) {
						case TURNLEFT: moveStr += "l"; break;
						case TURNRIGHT: moveStr += "r"; break;
						case FORWARD: moveStr += "f"; break;
						case BACKWARD: moveStr += "b"; break;
					}
				}
				System.out.println(moveStr);
				commsMgr.sendMessage(moveStr, CommsModel.MSG_TO_BOT);
			}
			
		} else {
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
				if (i == 0 && (map.getCellState(i, j) == CellState.OBSTACLE || map.getCellState(i,j) == CellState.UNEXPLORED)) {
					tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j - 1, CellState.OBSTACLE);
				} else if (i == 19 && (map.getCellState(i, j) == CellState.OBSTACLE || map.getCellState(i,j) == CellState.UNEXPLORED)) {
					if (j < 15)
						tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
					if (j > 0)
						tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
					if (j < 15)
						tempMap.setCellState(i - 1, j + 1, CellState.OBSTACLE);
					if (j > 0)
						tempMap.setCellState(i - 1, j - 1, CellState.OBSTACLE);
				} else if (j == 0 && i > 0 && i < 19 && (map.getCellState(i, j) == CellState.OBSTACLE || map.getCellState(i,j) == CellState.UNEXPLORED)) {
					tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j + 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j + 1, CellState.OBSTACLE);
				} else if (j == 14 && i > 0 && i < 19 && (map.getCellState(i, j) == CellState.OBSTACLE || map.getCellState(i,j) == CellState.UNEXPLORED)) {
					tempMap.setCellState(i + 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j, CellState.OBSTACLE);
					tempMap.setCellState(i, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i + 1, j - 1, CellState.OBSTACLE);
					tempMap.setCellState(i - 1, j - 1, CellState.OBSTACLE);
				} else {
					if ((map.getCellState(i, j) == CellState.OBSTACLE || map.getCellState(i,j) == CellState.UNEXPLORED)) {
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
	
	private Direction convertToDirection(Stack<Cell> path) {
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
		return robotDir;
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
