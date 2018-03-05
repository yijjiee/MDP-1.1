package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import interfaces.MapChangedInterface;
import models.map.CellState;
import models.map.Map;
import models.robot.Movement;
import models.robot.Robot;
import models.robot.RobotState;

public class MainController {
	private List<MapChangedInterface> mapListeners;
	private Map map;
	private Map cachedMap;
	private Robot robot;
	
	private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
	
	public MainController() {
		mapListeners = new ArrayList<>();
		map = new Map();
		robot = new Robot(1, 1, RobotState.SIMULATION);
	}
	
	public Map getMap() {
		return map;
	}
	
	public Map getCachedMap() {
		return cachedMap;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public void explore() {
		// 1) Cache old map state
		cachedMap = map;
		
		// 2) Create new map state with all unexplored regions
		map = new Map();
		
		for (int row = 0; row < Map.MAP_ROWS; row++) {
			for (int col = 0; col < Map.MAP_COLS; col++) {
				if (row < 3 && col < 3 || row > 16 && col > 11)
					map.setCellState(row, col, CellState.NORMAL);
				else
					map.setCellState(row, col, CellState.UNEXPLORED);
			}
		}
		robot.sense(cachedMap, map);
		
		if (robot.getState() == RobotState.SIMULATION) {
			final Runnable moveForward = new Runnable() { public void run() {
				robot.move(Movement.FORWARD);
			}};
			final ScheduledFuture<?> beeperHandle =
		       exec.scheduleAtFixedRate(moveForward, 1, 1, TimeUnit.SECONDS);
		     exec.schedule(new Runnable() {
		       public void run() { beeperHandle.cancel(true); }
		     }, 60, TimeUnit.SECONDS);
		}
		
		for(MapChangedInterface listener: mapListeners)
			listener.onMapChanged();
	}
	
	public boolean addMapChangedListener(MapChangedInterface listener) {
		return mapListeners.add(listener);
	}
	
	public boolean removeMapChangedListener(MapChangedInterface listener) {
		return mapListeners.remove(listener);
	}	
}
