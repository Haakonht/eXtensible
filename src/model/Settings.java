package model;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import model.Settings.SerializedSettings.ToolData;
import model.Tools.Tool;
import view.Application;

public class Settings {

	private static Settings instance = null;
	
	public static Settings getInstance() {
		if (instance == null) instance = new Settings();
		return instance;
	}
	
	private String FILE_NAME = "res/settings.ini";
	
	private SerializedSettings serializedSettings;
	public SerializedSettings getSerializedSettings() { 
		if (serializedSettings != null) {
			return serializedSettings;
		} else {
			return null;
		}	
	}
	
	private Settings() {
		serializedSettings = null;
		File f = new File(FILE_NAME);
		if(f.exists()) { 
			try{
				FileInputStream fis = null;
				fis = new FileInputStream(FILE_NAME);
				@SuppressWarnings("resource")
				ObjectInputStream ois = new ObjectInputStream(fis);
				serializedSettings = (SerializedSettings) ois.readObject();
			}
			catch(IOException | ClassNotFoundException ioe){
				System.out.println("File not found");
		    }
			System.out.println("Deserialized settings!");
		} 
		else serializedSettings = new SerializedSettings();	
	}
	
	public void serializeSettings() {
		ArrayList<ToolData> toolData = new ArrayList<ToolData>();
		for (int i = 0; i < Storage.getInstance().getImageCount(); i++) {
			Tools tools = Application.getInstance().getDrawing(i).getCanvas().getTools();	
			toolData.add(new ToolData(tools.getTool(), tools.getLayerIndex(), tools.getWeight(), tools.getFont(), tools.getOutline(), tools.getFill(), tools.getZoom()));
		}
		serializedSettings.toolData = toolData;
		serializedSettings.filePath = Storage.getInstance().getFilePath();
		serializedSettings.toolBarVisible = Application.getInstance().getToolBarContainer().isVisible();
		serializedSettings.statusBarVisible = Application.getInstance().getStatusContainer().isVisible();
		serializedSettings.canvasVisible = Application.getInstance().getCanvasContainer().isVisible();
		try{
			FileOutputStream fos = null;
			fos = new FileOutputStream(FILE_NAME);
			ObjectOutputStream oos= new ObjectOutputStream(fos);
			oos.writeObject(serializedSettings);
			oos.close();
			fos.close();
		}
		catch(IOException ioe){
            ioe.printStackTrace();
        }
	}
	
	public static class SerializedSettings implements Serializable {
		public Boolean toolBarVisible, statusBarVisible, canvasVisible, PERSIST, AUTOSAVE, SPLASH;
		public int updateTimer, autoSaveTimer;
		public String filePath;
		public ArrayList<ToolData> toolData;
		public SerializedSettings() {
			filePath = "";
			toolBarVisible = true;
			statusBarVisible = true;
			canvasVisible = true;
			PERSIST = false;
			AUTOSAVE = false;
			SPLASH = true;
			updateTimer = 50;
			autoSaveTimer = 10000;
			toolData = new ArrayList<ToolData>();
			toolData.add(new ToolData(Tool.RECTANGLE, 0, 10, new Font("Times New Roman", Font.PLAIN, 14), Color.BLACK, Color.WHITE, 1.00));
		}

		public static class ToolData implements Serializable {
			public Tool tool;
			public int layerIndex, weight;
			public Font font;
			public Color outline, fill;
			public Double zoom;
			public ToolData(Tool tool,  int layerIndex, int weight, Font font, Color outline, Color fill, Double zoom) {
				this.tool = tool; this.layerIndex = layerIndex; this.weight = weight; this.font = font; this.outline = outline; this.fill = fill; this.zoom = zoom;
			}
		}
	}
		
	public static class ServerSettings implements Serializable {
		private ObjectOutputStream output;
		private ObjectInputStream input;
		private String serverIP;
		private Socket connection;
		
		public ServerSettings(String host) {
			serverIP = host;
		}
		
		public void connect() {
			try {
				connection = new Socket(InetAddress.getByName(serverIP), 6789);
				output = new ObjectOutputStream(connection.getOutputStream());
				output.flush();
				input = new ObjectInputStream(connection.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void dispose() {
			try {
				output.close();
				input.close();
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
	

