package models.algo;

import java.util.ArrayList;
import java.util.Stack;

import models.map.Cell;
import models.map.CellState;
import models.map.MapModel;
import models.robot.Direction;
import models.robot.Robot;

public class FastestPath {
	private ArrayList<Cell> closedSet;
	private ArrayList<Cell> openSet;
	private Cell currentCell;
	private ArrayList<Cell> neighbours;
	private Cell [][] cameFrom;
	private double [][] gScore;
	private double [][] fScore;
	private Robot robot;
	private MapModel map;
	private Direction robotDir;
	private int endRow;
	private int endCol;
	
	/**
	 * 
	 * @param robot
	 * @param map
	 */
	public FastestPath(Robot robot, int endRow, int endCol, MapModel map) {
		this.map = map;
		this.robot = robot;
		this.endRow = endRow;
		this.endCol = endCol;
		robotDir = robot.getRobotDir();

		closedSet = new ArrayList<Cell>();
		openSet = new ArrayList<Cell>();
		currentCell = map.getCell(robot.getRow(), robot.getCol());
		gScore = new double[MapModel.MAP_ROWS][MapModel.MAP_COLS];
		fScore = new double[MapModel.MAP_ROWS][MapModel.MAP_COLS];
		cameFrom = new Cell[MapModel.MAP_ROWS][MapModel.MAP_COLS];
		
		for (int i = 0; i < MapModel.MAP_ROWS; i++) {
			for (int j = 0; j < MapModel.MAP_COLS; j++) {
				gScore[i][j] = Double.MAX_VALUE;
				fScore[i][j] = Double.MAX_VALUE;
			}
		}
		
		gScore[currentCell.getRow()][currentCell.getCol()] = 0;
		fScore[currentCell.getRow()][currentCell.getCol()] = getDistance(robot.getRow(), robot.getCol(), endRow, endCol);
		openSet.add(currentCell);		
	}
	
	public int getEndRow() {
		return endRow;
	}

	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	public int getEndCol() {
		return endCol;
	}

	public void setEndCol(int endCol) {
		this.endCol = endCol;
	}

	public Stack<Cell> startFastestPath() {
		while (!openSet.isEmpty()) {
			int lowestIndex = getLowestScore();
			currentCell = openSet.get(lowestIndex);
			openSet.remove(openSet.get(lowestIndex));
			
			if (currentCell.getRow() == endRow && currentCell.getCol() == endCol) {
				System.out.println("Solution Found.");
				return reconstruct_path(cameFrom, robot.getRow(), robot.getCol());
			}
			
			closedSet.add(currentCell);
			
			neighbours = getNeighbours(currentCell.getRow(), currentCell.getCol(), map);
			for (int i = 0; i < neighbours.size(); i++) {
				Cell neighbour = neighbours.get(i);
				int nRow = neighbour.getRow();
				int nCol = neighbour.getCol();
				int rowDiff = nRow - robot.getRow();
				int colDiff = nCol - robot.getCol();
				double turnCost = 0;
				
				if (closedSet.contains(neighbour))
					continue;
				
				if (openSet.lastIndexOf(neighbour) == -1)
					openSet.add(neighbour);
				
				turnCost = calculateTurnCost(rowDiff, colDiff);
				
				double tempGscore = gScore[currentCell.getRow()][currentCell.getCol()] + turnCost;
				
				if (tempGscore >= gScore[nRow][nCol])
					continue;
				else {
					cameFrom[nRow][nCol] = currentCell;
					gScore[nRow][nCol] = tempGscore;
					fScore[nRow][nCol] = gScore[nRow][nCol] + getDistance(nRow, nCol, endRow, endCol);
				}	
			}
		}
		System.out.println("No solution found!");
		return null;
	}
	
	private double calculateTurnCost(int rowDiff, int colDiff) {
		double turnCost = 0;
		switch (rowDiff) {
			case 1:
				switch (robotDir) {
					case NORTH:
						break;
					case SOUTH:
						turnCost = 20;
						break;
					case EAST:
						turnCost = 10;
						break;
					case WEST:
						turnCost = 10;
						break;
				}
				return turnCost;
			case -1:
				switch (robotDir) {
					case NORTH:
						turnCost = 20;
						break;
					case SOUTH:
						break;
					case EAST:
						turnCost = 10;
						break;
					case WEST:
						turnCost = 10;
						break;
				}
				return turnCost;
		}
		
		switch (colDiff) {
			case 1:
				switch (robotDir) {
					case NORTH:
						turnCost = 10;
						break;
					case SOUTH:
						turnCost = 10;
						break;
					case EAST:
						break;
					case WEST:
						turnCost = 20;
						break;
				}
				return turnCost;
			case -1:
				switch (robotDir) {
					case NORTH:
						turnCost = 10;
						break;
					case SOUTH:
						turnCost = 10;
						break;
					case EAST:
						turnCost = 20;
						break;
					case WEST:
						break;
				}
				return turnCost;
		}
		return turnCost;
	}
	
	private int getLowestScore() {
		double lowestScore = Double.MAX_VALUE;
		int lowestIndex = -1;
		
		for(int i = 0; i < openSet.size(); i++) {
			int row = openSet.get(i).getRow();
			int col = openSet.get(i).getCol();
			
			if(fScore[row][col] < lowestScore){
				lowestScore = fScore[row][col];
				lowestIndex = i;
			}
		}
		return lowestIndex;
	}

	private Stack<Cell> reconstruct_path(Cell[][] cameFrom, int row, int col) {
		Stack<Cell> path = new Stack<Cell>();
	    while (cameFrom[currentCell.getRow()][currentCell.getCol()] != null) {
			path.push(currentCell);
			currentCell = cameFrom[currentCell.getRow()][currentCell.getCol()];
		}
	    return path;
	}
	
	private ArrayList<Cell> getNeighbours(int row, int col, MapModel map) {
		neighbours = new ArrayList<Cell>();
		Cell northNeighbour = map.getCell(row + 1, col);
		Cell southNeighbour = map.getCell(row - 1, col);
		Cell eastNeighbour = map.getCell(row, col + 1);
		Cell westNeighbour = map.getCell(row, col - 1);
		
		if (northNeighbour.getRow() > 0 && northNeighbour.getCol() > 0 && northNeighbour.getRow() < 19 && northNeighbour.getCol() < 14 && northNeighbour.getState() != CellState.OBSTACLE)
			neighbours.add(northNeighbour);
		if (southNeighbour.getRow() > 0 && southNeighbour.getCol() > 0 && southNeighbour.getRow() < 19 && southNeighbour.getCol() < 14 && southNeighbour.getState() != CellState.OBSTACLE)
			neighbours.add(southNeighbour);
		if (eastNeighbour.getRow() > 0 && eastNeighbour.getCol() > 0 && eastNeighbour.getRow() < 19 && eastNeighbour.getCol() < 14 && eastNeighbour.getState() != CellState.OBSTACLE)
			neighbours.add(eastNeighbour);
		if (westNeighbour.getRow() > 0 && westNeighbour.getCol() > 0 && westNeighbour.getRow() < 19 && westNeighbour.getCol() < 14 && westNeighbour.getState() != CellState.OBSTACLE)
			neighbours.add(westNeighbour);
		
		return neighbours;
	}
	
	private double getDistance(int startRow, int startCol, int endRow, int endCol) {
		return Math.abs(startRow-endRow) + Math.abs(startCol-endCol);
	}
}
