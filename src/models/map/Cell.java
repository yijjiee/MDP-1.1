/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package models.map;

public class Cell {
	private int row;
	private int col;
	private CellState state;
	
	public Cell(int row, int col) {
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

	public CellState getState() {
		return state;
	}

	public void setState(CellState state) {
		this.state = state;
	}
}
