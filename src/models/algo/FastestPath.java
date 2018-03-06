package models.algo;

import java.util.Map;
import java.util.Set;

import models.map.MapModel;
import models.robot.Robot;

public class FastestPath {
	private Set closedSet;
	private Set openSet;
	private Map cameFrom;
	private Map gScore;
	private Map fScore;
	private boolean wayPoint;
	private Robot robot;
	
	public FastestPath(Robot robot) {
		this.robot = robot;
	}
	
	public void startFastestPath() {
		while (!openSet.isEmpty()) {	
			if (robot.getRow() == MapModel.END_ROW && robot.getCol() == MapModel.END_COL)
				reconstruct_path(cameFrom, robot.getRow(), robot.getCol());
		}
	}
	
	public void reconstruct_path(Map cameFrom, int row, int col) {
		
	}
}
