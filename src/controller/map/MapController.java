/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package controller.map;

import java.math.BigInteger;
import java.util.Collections;

import controller.MainController;
import controller.comms.CommsController;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import models.map.CellState;
import models.map.MapDescriptorFormat;
import models.map.MapModel;
import models.robot.RobotState;
import ui.Main;

public class MapController {
	private Main application;
	private MainController mainMgr;
	private CommsController commsMgr;
	private Pane[][] cells;
	
	public MapController(Main application, MainController mainMgr, CommsController commsMgr) {
		this.application = application;
		this.mainMgr = mainMgr;
		this.commsMgr = commsMgr;
		this.cells = new Pane[MapModel.MAP_ROWS][MapModel.MAP_COLS];
		
		// Add Listeners
		mainMgr.addMapChangedListener(this::initialize);
		mainMgr.getMap().addListeners(this::onMapStateChanged);
		application.getExploreBtn().setOnMouseClicked(this::onExploreClicked);
		application.getFastestPathBtn().setOnMouseClicked(this::onFastestPathClicked);
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
		String stylesheet;
		Pane pane = cells[MapModel.MAP_ROWS - row - 1][col];
		CellState cellState = mainMgr.getMap().getCellState(row, col);
		
		if (cellState == CellState.UNEXPLORED) 
			stylesheet = "-fx-background-color: black, #c0c0c0;";
		else if(cellState == CellState.OBSTACLE) 
			stylesheet = "-fx-background-color: black, black;";
		else if(cellState == CellState.NORMAL) 
			stylesheet = "-fx-background-color: black, white;";
		else
			stylesheet = "-fx-background-color: black, red;";
		
		Platform.runLater(() -> pane.setStyle(stylesheet));
		Platform.runLater(() -> application.getMdf1().setText(toString(MapDescriptorFormat.MDF1)));
		Platform.runLater(() -> application.getMdf2().setText(toString(MapDescriptorFormat.MDF2)));
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
		if (mainMgr.getRobot().getState() == RobotState.SIMULATION) {
			mainMgr.explore();
		} else {
			commsMgr.startExplore();
		}
		application.getExploreBtn().setDisable(true);
	}
	
	public void onFastestPathClicked(MouseEvent event) {
		mainMgr.runFastestPath();
		application.getFastestPathBtn().setDisable(true);
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
	
	public String toString(MapDescriptorFormat format) {
		MapModel map = mainMgr.getMap();
        String descriptor = new String();
        int bitcount = 0;
        
        for (int row = 0; row < MapModel.MAP_ROWS ; row++) {
            for (int col = 0; col < MapModel.MAP_COLS; col++) {
                boolean explored = map.getCellState(row, col) != CellState.UNEXPLORED;

                if (format == MapDescriptorFormat.MDF1) {
                    descriptor += explored ? "1" : "0";
                } else if (explored) {
                    descriptor += map.getCellState(row, col) == CellState.OBSTACLE ? "1" : "0";
                    bitcount++;
                }
            }
        }

        if (format == MapDescriptorFormat.MDF1) {
            descriptor = "11" + descriptor + "11";
        } else {
            bitcount = bitcount % 8;
            if (bitcount > 0) {
                descriptor += String.join("", Collections.nCopies(8 - bitcount, "0"));
            }
        }
        
        int expectedlength = descriptor.length() / 4;
        String hexstring = new BigInteger(descriptor, 2).toString(16).toUpperCase();

        if (expectedlength > hexstring.length()) 
            hexstring = String.join("", Collections.nCopies(expectedlength - hexstring.length(), "0")) + hexstring;
        
        return hexstring;
    }
}
