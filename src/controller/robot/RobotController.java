package controller.robot;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import models.map.Map;
import models.robot.Robot;
import ui.Main;

public class RobotController {
	private Main application;
	private Map map;
	private Robot robot;
	
	public RobotController(Main application, Map map, Robot robot) {
		this.application = application;
		this.map = map;
	}
}
