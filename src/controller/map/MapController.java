/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package controller.map;

import controller.MainController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import models.map.CellState;
import models.map.MapModel;
import ui.Main;

public class MapController {
	private Main application;
	private MainController mainMgr;
	private Pane[][] cells;
	
	public MapController(Main application, MainController mainMgr) {
		this.application = application;
		this.mainMgr = mainMgr;
		this.cells = new Pane[MapModel.MAP_ROWS][MapModel.MAP_COLS];
		
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
						
			onMapStateChanged(MapModel.MAP_ROWS - row - 1, col);
		}
	}
	
	public void onMapChanged() {
		mainMgr.getCachedMap().removeListeners(this::onMapStateChanged);
		mainMgr.getMap().addListeners(this::onMapStateChanged);
	}

	public void onMapStateChanged(int row, int col) {
		CellState cellState = mainMgr.getMap().getCellState(row, col);
		
		if (cellState == CellState.UNEXPLORED) {
			cells[MapModel.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, #c0c0c0;");
		}
		else if(cellState == CellState.OBSTACLE) {
			cells[MapModel.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, black;");
		}
		else if(cellState == CellState.NORMAL) {
			cells[MapModel.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, white;");
		}
		else if (cellState == CellState.WAYPOINT) {
			cells[MapModel.MAP_ROWS - row - 1][col].setStyle("-fx-background-color: black, red;");
		}
	}
	
	public void onCellClicked(MouseEvent event) {
		MapModel map = mainMgr.getMap();
		
		if (!application.getExploreBtn().isDisabled()) {
			if (application.getExecBox().getValue().equals(Main.KEY_SIMULATION)) {
				if (event.getButton() == MouseButton.PRIMARY) {
					String cellInput = application.getMapInputBox().getValue();
					if (cellInput.equals(Main.KEY_OBSTACLE)) {
						Pane cell = (Pane)event.getSource();
						int row = MapModel.MAP_ROWS - GridPane.getRowIndex(cell) - 1;
						int col = GridPane.getColumnIndex(cell);
						if(map.getCellState(row, col) == CellState.OBSTACLE)
							map.setCellState(row, col, CellState.NORMAL);
						else
							map.setCellState(row, col, CellState.OBSTACLE);
					} 
					else if (cellInput.equals(Main.KEY_WAYPOINT)) {
						Pane cell = (Pane)event.getSource();
						int row = MapModel.MAP_ROWS - GridPane.getRowIndex(cell) - 1;
						int col = GridPane.getColumnIndex(cell);
						if(map.getCellState(row, col) == CellState.WAYPOINT)
							map.setCellState(row, col, CellState.NORMAL);
						else
							map.setCellState(row, col, CellState.WAYPOINT);
					}
				}
			}
		}
	}
	
	public void onExploreClicked(MouseEvent event) {
		mainMgr.setCoverageLimit(Integer.valueOf(application.getCoverageLimitField().getText()));
		mainMgr.setTimeLimit(Integer.valueOf(application.getTimeLimitField().getText()));
		mainMgr.explore();
		application.getExploreBtn().setDisable(true);
	}
	
	public void onRobotPosChanged() {
		double y = (MapModel.MAP_ROWS - mainMgr.getRobot().getRow() - 1) * 39 - 29.5;
		double x = (mainMgr.getRobot().getCol() - 1) * 39 + 20;
		
		StackPane robot = (StackPane) application.getRobotPane();
		robot.setLayoutX(x);
		robot.setLayoutY(y);
		Circle robotHead = (Circle) robot.getChildren().get(1);
		
		switch(mainMgr.getRobot().getRobotDir()) {
			case NORTH:
				StackPane.setAlignment(robotHead, Pos.TOP_CENTER);
				break;
			case SOUTH:
				StackPane.setAlignment(robotHead, Pos.BOTTOM_CENTER);
				break;
			case EAST:
				StackPane.setAlignment(robotHead, Pos.CENTER_RIGHT);
				break;
			case WEST:
				StackPane.setAlignment(robotHead, Pos.CENTER_LEFT);
				break;
		}

		mainMgr.getRobot().sense(mainMgr.getCachedMap(), mainMgr.getMap());
	}
}
