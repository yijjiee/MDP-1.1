package models.comms;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommsModel {
	public static final String MSG_TO_ANDROID = "#";
	public static final String MSG_TO_BOT = "~";
	
	private static CommsModel commsMgr;
	private static Socket conn;
	
	private BufferedWriter writer;
	private BufferedReader reader;
	
	private CommsModel() {}
	
	public static CommsModel getCommsModel() {
		if (commsMgr == null)
			commsMgr = new CommsModel();
		return commsMgr;
	}
	
	public void openConnection() {
        System.out.println("Opening Connection.");

        try {
            String HOST = "192.168.2.1";
            int PORT = 99;
            conn = new Socket(HOST, PORT);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            System.out.println("Connection Established.");

            return;
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostException - Opening Connection");
        } catch (IOException e) {
            System.out.println("IOException - Opening Connection");
        } catch (Exception e) {
            System.out.println("Exception - Opening Connection");
            System.out.println(e.toString());
        }

        System.out.println("Failed to start connection.");
    }
	
	public void closeConnection() {
        System.out.println("Closing Connection.");

        try {
            reader.close();

            if (conn != null) {
                conn.close();
                conn = null;
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.out.println("IOException - Closing Connection");
        } catch (NullPointerException e) {
            System.out.println("NullPointerException - Closing Connection");
        } catch (Exception e) {
            System.out.println("Exception - Closing Connection");
            System.out.println(e.toString());
        }
    }

	public void sendMsg(String msg) {
        try {
            writer.write(msg);
            writer.flush();
        } catch (IOException e) {
            System.out.println("IOException - Sending Message");
        } catch (Exception e) {
            System.out.println("Exception - Sending Message");
            System.out.println(e.toString());
        }
    }
	
	public String recvMsg() {
        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();
            
            System.out.println("Receiving message: " + input);
            
            if (input != null && input.length() > 0) {
                sb.append(input);
                return sb.toString();
            }
        } catch (IOException e) {
            System.out.println("IOException - Receiving Message");
        } catch (Exception e) {
            System.out.println("Exception - Receiving Message");
            System.out.println(e.toString());
        }

        return null;
    }

    public boolean isConnected() {
        return conn.isConnected();
    }
}
