/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package models.map;

import java.util.ArrayList;
import java.util.List;

import interfaces.MapListener;

public class MapModel {
	public static final int MAP_ROWS = 20;
	public static final int MAP_COLS = 15;
	
	public static final int END_ROW = 18;
	public static final int END_COL = 13;
	public static final int START_ROW = 1;
	public static final int START_COL = 1;
	
	private Cell[][] grid;
	private List<MapListener> listeners = new ArrayList<MapListener>();
	

	public Cell[][] getGrid() {
		return grid;
	}
	
	public Cell getCell(int row, int col) {
		return grid[row][col];
	}
	
	public CellState getCellState(int row, int col) {
		if (row < 0 || row > 19 || col < 0 || col > 14)
			return CellState.OBSTACLE;
		return grid[row][col].getState();
	}
	
	public void setCellState(int row, int col, CellState state) {
		grid[row][col].setState(state);
		notifyChange(row, col);		
	}
	
	public MapModel() {
		grid = new Cell[MAP_ROWS][MAP_COLS];
		for (int row = 0; row < MAP_ROWS; row++) {
			for (int col = 0; col < MAP_COLS; col++) {
				grid[row][col] = new Cell(row, col);
			}
		}
	}
	
	public void addListeners(MapListener ml) {
		listeners.add(ml);
	}
	
	public void removeListeners(MapListener ml) {
		listeners.remove(ml);
	}
	
	public void notifyChange(int row, int col) {
		for (MapListener ml : listeners)
			ml.onMapStateChange(row, col);
	}
}
