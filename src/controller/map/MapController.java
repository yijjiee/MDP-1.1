/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package controller.map;

import controller.MainController;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import models.map.CellState;
import models.map.Map;
import ui.Main;

public class MapController {
	private Main application;
	private MainController mainMgr;
	private Pane[][] cells;
	
	public MapController(Main application, MainController mainMgr) {
		this.application = application;
		this.mainMgr = mainMgr;
		this.cells = new Pane[Map.MAP_ROWS][Map.MAP_COLS];
		
		// Add Listeners
		mainMgr.addMapChangedListener(this::initialize);
		mainMgr.getMap().addListeners(this::onMapStateChanged);
		application.getExploreBtn().setOnMouseClicked(this::onExploreClicked);
		mainMgr.addMapChangedListener(this::onMapChanged);
		mainMgr.getRobot().addListeners(this::onRobotPosChanged);
		
		initialize();
	}
	
	public void initialize() {
		for(Node node: application.getMapPane().getChildren()) {
			((Pane)node).setOnMouseClicked(this::onCellClicked);
			int row = GridPane.getRowIndex(node);
			int col = GridPane.getColumnIndex(node);
			cells[row][col] = (Pane) node;
			
			onMapStateChanged(Map.MAP_ROWS - row - 1, col);
		}
	}
	
	public void onMapChanged() {
		mainMgr.getCachedMap().removeListeners(this::onMapStateChanged);
		mainMgr.getMap().addListeners(this::onMapStateChanged);
	}

	public void onMapStateChanged(int row, int col) {
		CellState cellState = mainMgr.getMap().getCellState(row, col);
		
		if (cellState == CellState.UNEXPLORED) {
			cells[Map.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, #c0c0c0;");
		}
		else if(cellState == CellState.OBSTACLE) {
			cells[Map.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, black;");
		}
		else if(cellState == CellState.NORMAL) {
			cells[Map.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, white;");
		}
		else if (cellState == CellState.WAYPOINT) {
			cells[Map.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, red;");
		}
	}
	
	public void onCellClicked(MouseEvent event) {
		Map map = mainMgr.getMap();
		
		if (application.getExecBox().getValue().equals(Main.KEY_SIMULATION)) {
			if (event.getButton() == MouseButton.PRIMARY) {
				String cellInput = application.getMapInputBox().getValue();
				if (cellInput.equals(Main.KEY_OBSTACLE)) {
					Pane cell = (Pane)event.getSource();
					int row = Map.MAP_ROWS - GridPane.getRowIndex(cell) - 1;
					int col = GridPane.getColumnIndex(cell);
					if(map.getCellState(row, col) == CellState.OBSTACLE)
						map.setCellState(row, col, CellState.NORMAL);
					else
						map.setCellState(row, col, CellState.OBSTACLE);
				} 
				else if (cellInput.equals(Main.KEY_WAYPOINT)) {
					Pane cell = (Pane)event.getSource();
					int row = Map.MAP_ROWS - GridPane.getRowIndex(cell) - 1;
					int col = GridPane.getColumnIndex(cell);
					if(map.getCellState(row, col) == CellState.WAYPOINT)
						map.setCellState(row, col, CellState.NORMAL);
					else
						map.setCellState(row, col, CellState.WAYPOINT);
				}
			}
		}
	}
	
	public void onExploreClicked(MouseEvent event) {
		mainMgr.explore();
		application.getExploreBtn().setDisable(true);
	}
	
	public void onRobotPosChanged() {
		double y = (Map.MAP_ROWS - mainMgr.getRobot().getRow() - 1) * 39 + 29.5;
		double x = (mainMgr.getRobot().getCol() + 1) * 39;
		
		Circle robot = application.getRobotPane();
		robot.setLayoutX(x);
		robot.setLayoutY(y);

		mainMgr.getRobot().sense(mainMgr.getCachedMap(), mainMgr.getMap());
	}
}
