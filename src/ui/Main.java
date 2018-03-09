/*
 *		Author: Wong Yijie
 *		Last edited: 22 February 2018
 */

package ui;
	
import controller.MainController;
import controller.comms.CommsController;
import controller.map.MapController;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	private static Main application;
	private GridPane mapPane;
	private VBox menuPane;
	private Pane robotPane;
	
	private ComboBox<String> execBox;
	private ComboBox<String> mapInputBox;
	private TextField tlField;
	private TextField clField;
	private Button exploreBtn;
	private Button fastestPathBtn;
	private Button loadMap;
	private Button saveMap;
	
	private TextArea mdf1;
	private TextArea mdf2;
	
	public final static String KEY_SIMULATION = "SIMULATION";
	public final static String KEY_PHYSICAL = "PHYSICAL";
	public final static String KEY_OBSTACLE = "OBSTACLE";
	public final static String KEY_WAYPOINT = "WAYPOINT";
	
	public Main() {
		application = this;
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Pane root = new Pane();
			Scene scene = new Scene(root,1000,800);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			mapPane = initializeMap();
			menuPane = initializeMenu();
			robotPane = initializeRobot();
			Pane endPointPane = initializeEndPoint();
			
			root.getChildren().addAll(mapPane, menuPane, endPointPane,  robotPane);
			primaryStage.setScene(scene);
			
			Main application = Main.getCurrentApplication();
			MainController mainMgr = new MainController();
			CommsController commsMgr = new CommsController(mainMgr);
			MapController mapMgr = new MapController(application, primaryStage, mainMgr, commsMgr);
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public GridPane getMapPane() {
		return mapPane;
	}
	
	public VBox getMenuPane() {
		return menuPane;
	}
	
	public Pane getRobotPane() {
		return robotPane;
	}

	public ComboBox<String> getExecBox() {
		return execBox;
	}
	
	public Button getExploreBtn() {
		return exploreBtn;
	}
	
	public Button getFastestPathBtn() {
		return fastestPathBtn;
	}
	
	public Button getLoadMapBtn() {
		return loadMap;
	}
	
	public Button getSaveMapBtn() {
		return saveMap;
	}
	
	public TextField getTimeLimitField() {
		return tlField;
	}
	
	public TextField getCoverageLimitField() {
		return clField;
	}

	public ComboBox<String> getMapInputBox() {
		return mapInputBox;
	}
	
	public TextArea getMdf1() {
		return mdf1;
	}
	
	public TextArea getMdf2() {
		return mdf2;
	}
	
	public static Main getCurrentApplication() {
		return application;
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	private GridPane initializeMap() {
		GridPane mapPane = new GridPane();
		mapPane.setPrefWidth(400);
		mapPane.setLayoutX(20);
		mapPane.setLayoutY(10);
		
		for (int i = 0 ; i < 15; i++) {
            ColumnConstraints col = new ColumnConstraints(39);
            mapPane.getColumnConstraints().add(col);
        }
		
		for (int i = 0; i < 20; i++) {
			 RowConstraints row = new RowConstraints(39);
			 mapPane.getRowConstraints().add(row);
		}
		
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 20; j++) {
				Pane cell = new Pane();
				
				cell.getStyleClass().add("cell");
				if (i == 0) {
                    cell.getStyleClass().add("first-column");
                }
                if (j == 0) {
                    cell.getStyleClass().add("first-row");
                }
    			mapPane.add(cell, i, j);
			}
		}
		
		return mapPane;
	}
	
	private Pane initializeRobot() {
		robotPane = new StackPane();
		robotPane.setLayoutX(20);
		robotPane.setLayoutY(672.5);
		
		Circle robot = new Circle();
		robot.setRadius(58.5);
		robot.setFill(Color.BLACK);
		
		Circle robotHead = new Circle();
		robotHead.setRadius(15);
		robotHead.setFill(Color.RED);
		
		robotPane.getChildren().addAll(robot, robotHead);
		StackPane.setAlignment(robotHead, Pos.TOP_CENTER);
		
		return robotPane;
	}

	private Pane initializeEndPoint() {
		Label label = new Label("EndPoint");
		label.setTextFill(Color.WHITE);
		label.setFont(new Font(25));
		label.setLayoutX(10);
		label.setLayoutY(40);
		
		Rectangle bg = new Rectangle();
		bg.setWidth(116);
		bg.setHeight(115);
		bg.setFill(Color.GREY);
		
		Pane endPoint = new Pane();
		endPoint.setLayoutX(488);
		endPoint.setLayoutY(11);
		endPoint.getChildren().addAll(bg, label);
		
		return endPoint;
	}
	
	private VBox initializeMenu() {
		VBox menuPane = new VBox();
		
		menuPane.setSpacing(20);
		menuPane.setLayoutX(620);
		menuPane.setLayoutY(10);
		menuPane.setPrefWidth(375);
		menuPane.setPrefHeight(780);
		menuPane.getStyleClass().add("menu");
		
		menuPane.setOnMouseClicked(e -> {
			menuPane.requestFocus();
		});
		
		// Load/Save Map Buttons
		HBox lsMap = new HBox();
		loadMap = new Button("Load Map");
		loadMap.setFont(new Font(14));
		saveMap = new Button("Save Map");
		saveMap.setFont(new Font(14));
		lsMap.setSpacing(20);
		lsMap.getChildren().addAll(loadMap, saveMap);
		
		// Execution Mode
		HBox execPane = new HBox();
		Label execLabel = new Label("Execution Mode:");
		execLabel.setFont(new Font(16));
		execBox = new ComboBox<String>();
		execBox.getStyleClass().addAll("combo-box, list-cell, combo-box-popup");
		execBox.getItems().addAll(KEY_SIMULATION, KEY_PHYSICAL);
		execBox.setValue(KEY_SIMULATION);
		execPane.setSpacing(10);
		execPane.getChildren().addAll(execLabel, execBox);
		
		// Obstacles / Waypoint
		HBox owPane = new HBox();
		Label owLabel = new Label("Map Input:");
		owLabel.setFont(new Font(16));
		mapInputBox = new ComboBox<String>();
		mapInputBox.getStyleClass().addAll("combo-box, list-cell, combo-box-popup");
		mapInputBox.getItems().addAll(KEY_OBSTACLE, KEY_WAYPOINT);
		mapInputBox.setValue(KEY_OBSTACLE);
		owPane.setSpacing(10);
		owPane.getChildren().addAll(owLabel, mapInputBox);
		
		// Start Coord Label
		Label scLabel = new Label("Start Coordinates (x, y):");
		scLabel.setFont(new Font(16));
		
		/*// Start Coord
		HBox scBox = new HBox();
		TextField scX = new TextField();
		scX.setPrefWidth(40);
		scX.setPromptText("X");
		TextField scY = new TextField();
		scY.setPrefWidth(40);
		scY.setPromptText("Y");
		scBox.setSpacing(20);
		scBox.getChildren().addAll(scX, scY);
		
		// End Coord Label
		Label ecLabel = new Label("End Coordinates (x, y):");
		ecLabel.setFont(new Font(16));
		
		// End Coord
		HBox ecBox = new HBox();
		TextField ecX = new TextField();
		ecX.setPrefWidth(40);
		ecX.setPromptText("X");
		TextField ecY = new TextField();
		ecY.setPrefWidth(40);
		ecY.setPromptText("Y");
		ecBox.setSpacing(20);
		ecBox.getChildren().addAll(ecX, ecY);
		*/
		
		// MDF1 Label
		Label mdf1Label = new Label("MDF1:");
		mdf1Label.setFont(new Font(16));
		
		// MDF1
		mdf1 = new TextArea();
		mdf1.setFont(new Font(14));
		mdf1.setPrefHeight(80);
		mdf1.setText("00000000000000000000000000000000000000");
		mdf1.setWrapText(true);
		mdf1.setEditable(false);
		
		// MDF1 Label
		Label mdf2Label = new Label("MDF2:");
		mdf2Label.setFont(new Font(16));
		
		// MDF1
		mdf2 = new TextArea();
		mdf2.setFont(new Font(14));
		mdf2.setPrefHeight(80);
		mdf2.setText("00000000000000");
		mdf2.setWrapText(true);
		mdf2.setEditable(false);
		
		// Time limit
		HBox tlBox = new HBox();
		Label tlLabel = new Label("Time Limit (secs):");
		tlLabel.setFont(new Font(16));
		tlField = new TextField();
		tlField.setText("360");
		tlField.setFont(new Font(13));
		tlField.setPrefWidth(70);
		tlBox.setSpacing(10);
		tlBox.getChildren().addAll(tlLabel, tlField);
		
		// Coverage Limit
		HBox clBox = new HBox();
		Label clLabel = new Label("Coverage Limit (%):");
		clLabel.setFont(new Font(16));
		clField = new TextField();
		clField.setText("100");
		clField.setFont(new Font(13));
		clField.setPrefWidth(70);
		clBox.setSpacing(10);
		clBox.getChildren().addAll(clLabel, clField);
		
		// Exploration / Fastest Path Button
		HBox startBox = new HBox();
		exploreBtn = new Button("Explore");
		exploreBtn.setFont(new Font(16));
		fastestPathBtn = new Button("Fastest Path");
		fastestPathBtn.setFont(new Font(16));
		startBox.setSpacing(20);
		startBox.getChildren().addAll(exploreBtn, fastestPathBtn);
		
		menuPane.getChildren().addAll(lsMap, execPane, owPane, /*scLabel, scBox, ecLabel, ecBox,*/ mdf1Label, mdf1, mdf2Label, mdf2, tlBox, clBox, startBox);
		
		return menuPane;
	}
}
