package controller;

import model.Resource;
import model.Settings;
import model.Storage;
import view.Application;
import view.Components.Splash;

public class Initializer {
	
	public static void main(String[] args) {
		new Initializer();
	}
	
	private Initializer() {
		Resource.getInstance();
		Settings.getInstance();
		Storage.getInstance();
		Splash splash = null;
		if (Settings.getInstance().getSerializedSettings().SPLASH) {
			splash = new Splash();
			splash.progressBar.setValue(33);
			splash.progressBar.setString("Setting Phasers to Stun");
		}
		if (!Settings.getInstance().getSerializedSettings().filePath.equals("") && Settings.getInstance().getSerializedSettings().PERSIST) 
			Storage.getInstance().initWithDocument(Settings.getInstance().getSerializedSettings().filePath);
		if (splash != null) {
			splash.progressBar.setValue(66);
			splash.progressBar.setString("Blowing up the Death Star");
		}
		Application.getInstance();
		if (splash != null) {
			splash.progressBar.setValue(100);
			splash.dispose();
		}
	}
	
}
