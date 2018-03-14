package controller.comms;

import models.comms.CommsModel;

public class CommsController {
	private CommsModel commsModel;
	
	public CommsController() {
		commsModel = CommsModel.getCommsModel();
	}
	
	public void startConnection() {
		commsModel.openConnection();
		
		System.out.println(commsModel.isConnected());
	}
	
	public void sendMessage(String msg, String receiver) {
		if (commsModel.isConnected()) {
			String outputMsg = "";
			
			if (receiver.equals(CommsModel.MSG_TO_BOT)) {
            	outputMsg = msg + "\r\n";
            } else {
            	outputMsg = receiver + msg + "\n";
            }
			
			commsModel.sendMsg(outputMsg);
		}
	}
	
	public String startRecvMsg() {
		if (commsModel.isConnected()) {
			return commsModel.recvMsg();
		}
		return null;
	}
}
