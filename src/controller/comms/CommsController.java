package controller.comms;

import controller.MainController;
import models.comms.CommsModel;
import models.map.CellState;
import models.map.MapModel;

public class CommsController {
	private MainController mainMgr;
	private CommsModel commsModel;
	
	private MapModel map;
	
	public CommsController(MainController mainMgr) {
		this.mainMgr = mainMgr;
		
		commsModel = CommsModel.getCommsModel();
		map = mainMgr.getMap();
	}
	
	public void startExplore() {
		commsModel.openConnection();
		
		for (int row = 0; row < MapModel.MAP_ROWS; row++) {
			for (int col = 0; col < MapModel.MAP_COLS; col++) {
				if (row < 3 && col < 3 || row > 16 && col > 11)
					map.setCellState(row, col, CellState.NORMAL);
				else
					map.setCellState(row, col, CellState.UNEXPLORED);
			}
		}
		
		System.out.println(commsModel.isConnected());
		
		commsModel.sendMsg("hello", "#");
		System.out.println(commsModel.recvMsg());
	}
}
