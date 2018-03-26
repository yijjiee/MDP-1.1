/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package controller.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.comms.CommsModel;
import models.map.CellState;
import models.map.MapDescriptorFormat;
import models.map.MapModel;
import models.robot.RobotState;
import ui.Main;

public class MapController {
	private Main application;
	private Stage primaryStage;
	private MainController mainMgr;
	private Pane[][] cells;
	
	public MapController(Main application, Stage primaryStage, MainController mainMgr) {
		this.application = application;
		this.primaryStage = primaryStage;
		this.mainMgr = mainMgr;
		this.cells = new Pane[MapModel.MAP_ROWS][MapModel.MAP_COLS];
		
		// Add Listeners
		mainMgr.addMapChangedListener(this::initialize);
		mainMgr.getMap().addListeners(this::onMapStateChanged);
		application.getExploreBtn().setOnMouseClicked(this::onExploreClicked);
		application.getFastestPathBtn().setOnMouseClicked(this::onFastestPathClicked);
		application.getLoadMapBtn().setOnMouseClicked(this::onLoadMapClicked);
		application.getSaveMapBtn().setOnMouseClicked(this::onSaveMapClicked);
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
		
		String mdf1 = toString(MapDescriptorFormat.MDF1);
		String mdf2 = toString(MapDescriptorFormat.MDF2);
		mainMgr.setMdf1(mdf1);
		mainMgr.setMdf2(mdf2);
		
//		if (mainMgr.getCommsMgr() != null && mainMgr.getCommsMgr().isConnected()) {
//			mainMgr.getCommsMgr().sendMessage("mdf1:" + mdf1 + "/", CommsModel.MSG_TO_ANDROID);
//			mainMgr.getCommsMgr().sendMessage("mdf2:" + mdf2 + "/", CommsModel.MSG_TO_ANDROID);
//		}
		
		Platform.runLater(() -> pane.setStyle(stylesheet));
		Platform.runLater(() -> application.getMdf1().setText(mdf1));
		Platform.runLater(() -> application.getMdf2().setText(mdf2));
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

		if (application.getExecBox().getValue().equals(Main.KEY_SIMULATION))
			mainMgr.getRobot().setState(RobotState.SIMULATION);
		else
			mainMgr.getRobot().setState(RobotState.PHYSICAL);
		
		mainMgr.explore();
	}
	
	public void onLoadMapClicked(MouseEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MDF files", "*.mdf1");
		fileChooser.setTitle("Load Map");
		fileChooser.getExtensionFilters().add(extFilter);
		File file = fileChooser.showOpenDialog(primaryStage);
		String mdf1Line = "" , mdf2Line = "";
		if (file != null) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String readerLine;
				while ((readerLine = reader.readLine()) != null)
					mdf1Line += readerLine;
				
				reader.close();
				
				file = new File(file.getAbsolutePath().substring(0, file.toString().lastIndexOf(".")).concat(".mdf2"));
				reader = new BufferedReader(new FileReader(file));
				readerLine = "";
				while ((readerLine = reader.readLine()) != null)
					mdf2Line += readerLine;
				
				reader.close();
			} catch(IOException e) {
				System.out.println("Loading file error: IOException");
			}
		}
		
		if (mdf1Line != "" && mdf2Line != "") {
			application.getMdf1().setText(mdf1Line);
			application.getMdf2().setText(mdf2Line);
			
			String mdf1bin = new BigInteger(mdf1Line, 16).toString(2);
			String mdf2bin = new BigInteger(mdf2Line, 16).toString(2);
			
			mdf1bin = mdf1bin.substring(2, mdf1bin.length() - 2);
	        mdf2bin = String.join("", Collections.nCopies(mdf2Line.length() * 4 - mdf2bin.length(), "0")) + mdf2bin;
	        
	        int mdf1Counter = 0;
	        int mdf2Counter = 0;
	        
	        for (int y = 0; y < MapModel.MAP_ROWS; y++) {
	            for (int x = 0; x < MapModel.MAP_COLS; x++) {
	                if (mdf1bin.substring(mdf1Counter, mdf1Counter + 1).equals("0")) {
	                    mainMgr.getMap().setCellState(y, x, CellState.UNEXPLORED);
	                } else {
	                    if (mdf2bin.substring(mdf2Counter, mdf2Counter + 1).equals("1")) {
	                        mainMgr.getMap().setCellState(y, x, CellState.OBSTACLE);
	                    }
	                    else {
	                    	mainMgr.getMap().setCellState(y, x, CellState.NORMAL);
	                    }
	
	                    mdf2Counter++;
	                }
	                mdf1Counter++;
	            }
	        }
		}
	}
	
	public void onSaveMapClicked(MouseEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MDF files", "*.mdf1");
		fileChooser.setTitle("Save Map");
		fileChooser.getExtensionFilters().add(extFilter);
		File file = fileChooser.showSaveDialog(primaryStage);
		if (file != null) {
			try {
				FileWriter fw = new FileWriter(file);
				fw.write(application.getMdf1().getText());
				fw.flush();
				fw.close();
				file = new File(file.getAbsolutePath().substring(0, file.toString().lastIndexOf(".")).concat(".mdf2"));
				fw = new FileWriter(file);
				fw.write(application.getMdf2().getText());
				fw.flush();
				fw.close();
			} catch (IOException e) {
				System.out.println("Saving file error: IOException");
			}
		}
	}
	
	public void onFastestPathClicked(MouseEvent event) {
		mainMgr.runFastestPath();
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
		mainMgr.getRobot().sense(null, mainMgr.getMap());
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
