package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.Timer;

import controller.Canvas;
import model.Settings;
import model.Storage;
import view.Components.AppMenu;
import view.Components.Container;
import view.Components.CustomTabbedPane;
import view.Components.Drawing;
import view.Components.ProjectMenu;
import view.Components.Ruler;
import view.Components.ScrollWrapper;
import view.Components.Status;
import view.Components.ToolMenu;
import view.Components.Window;
import view.Dialogs.StartupDialog;

public class Application {

	public static boolean DEBUG = false;
	
	public static Application getInstance() {
        return ResourceLoader.APPLICATION;
	}

	private static final class ResourceLoader {
		private static final Application APPLICATION = new Application();
	}
	
	private Window window;
	private Container toolBar;
	private Container toolMenu;
	private Container statusBar;
	private Container projectMenu;
	private Container canvas;
	private CustomTabbedPane tabs;
	private Status statusInfo;
	private AppMenu applicationMenu;
	
	private Timer updater;
	private int autoSave = 0;
	
	private Application() {
		window = new Window("eXtensible", 1700, 900);
		
		toolBar = new Container("Application Menu", window.getWidth(), 100);
		statusBar = new Container("Status", window.getWidth(), 50);
		toolMenu = new Container("Tool Menu", 280, window.getHeight());
		toolMenu.setVisible(false);
		projectMenu = new Container("Project Menu", 250, window.getHeight());
		projectMenu.setVisible(false);
		canvas = new Container("Canvas", window.getWidth(), window.getHeight());
		
		applicationMenu = new AppMenu();
		statusInfo = new Status();
		if (Settings.getInstance().getSerializedSettings() != null && !Settings.getInstance().getSerializedSettings().toolData.isEmpty()) 
			 tabs = new CustomTabbedPane(Settings.getInstance().getSerializedSettings().toolData);
		else tabs = new CustomTabbedPane();
		
		toolBar.add(applicationMenu);
		canvas.add(tabs);
		statusBar.add(statusInfo);
		
		if (Settings.getInstance().getSerializedSettings() != null) {
			canvas.setVisible(Settings.getInstance().getSerializedSettings().canvasVisible);
			statusBar.setVisible(Settings.getInstance().getSerializedSettings().statusBarVisible);
			toolBar.setVisible(Settings.getInstance().getSerializedSettings().toolBarVisible);
		}
		
		window.desktop.add(toolBar, BorderLayout.NORTH);
		window.desktop.add(statusBar, BorderLayout.SOUTH);
		window.desktop.add(toolMenu, BorderLayout.WEST);
		window.desktop.add(projectMenu, BorderLayout.EAST);
		window.desktop.add(canvas, BorderLayout.CENTER);
		
		window.revalidate();
		window.repaint();
		
		updater = new Timer(50, e -> {
			if (Settings.getInstance().getSerializedSettings().AUTOSAVE) { 
				autoSave++;
				if (autoSave > Settings.getInstance().getSerializedSettings().autoSaveTimer / updater.getDelay()) {
					Storage.getInstance().autoSave(); autoSave = 0; 
				}
			}
			tabs.update(Storage.getInstance().getImageCount());
			applicationMenu.enableCheck(getCanvas());
			if (getCanvas() != null) statusInfo.setStatus(getCanvas());
		});
		if (Settings.getInstance().getSerializedSettings() != null) updater.setDelay(Settings.getInstance().getSerializedSettings().updateTimer);
		updater.start();
		
		if (!Settings.getInstance().getSerializedSettings().PERSIST) new StartupDialog();
	}
	
	public void toggleCanvas() { canvas.setVisible(!canvas.isVisible()); }
	public void toggleStatus() { statusBar.setVisible(!statusBar.isVisible()); }
	public void toggleToolBar() { toolBar.setVisible(!toolBar.isVisible()); }
	
	public void reloadMenu() {
		toggleMenu("");
		toggleMenu(AppMenu.menuCommand);
	}
	public void toggleMenu(String command) {
		if (command.equals("")) {
			toolMenu.setVisible(false);
		} else {
			toolMenu.getContentPane().removeAll();
			toolMenu.add(new ToolMenu(command));
			toolMenu.setTitle(command);
			toolMenu.setVisible(true);
		}
	}
	public void reloadProject() {
		toggleProject("");
		toggleProject(AppMenu.projectCommand);
	}
	public void toggleProject(String command) {
		if (command.equals("")) {
			projectMenu.setVisible(false);
		} else {
			projectMenu.getContentPane().removeAll();
			projectMenu.add(new ProjectMenu(command));
			projectMenu.setTitle(command);
			projectMenu.setVisible(true);
		}
	}
	
	public void repaintCanvas() {
		for (Component component : canvas.getComponents()) {
			component.repaint();
			if (Application.DEBUG) System.out.println("!! - CANVAS REPAINT - !!");
		}
	}
	public void repaintMenu() { toolMenu.revalidate(); toolMenu.repaint(); }
	
	public Window getWindow() { return window; }
	public Canvas getCanvas() { 
		if (tabs.getTabCount() < 1) return null;	
		else return ((Drawing)((ScrollWrapper) tabs.getSelectedComponent()).getContent()).getCanvas(); 
	}
	public Ruler getRuler(int orientation) {
		if (tabs.getTabCount() < 1) return null;
		else return ((Ruler)((ScrollWrapper) tabs.getSelectedComponent()).getRuler(orientation));
	}
	public Container getToolBarContainer() { return toolBar; }
	public Container getCanvasContainer() { return canvas; }
	public Container getStatusContainer() { return statusBar; }
	public Drawing getDrawing() { return ((Drawing)((ScrollWrapper) tabs.getSelectedComponent()).getContent()); }
	public Drawing getDrawing(int imageIndex) { return ((Drawing)((ScrollWrapper) tabs.getComponentAt(imageIndex)).getContent()); }
	public void setTabIndex(int index) { tabs.setSelectedIndex(index); }
	public int getTabIndex() { return tabs.getSelectedIndex(); }
	public int getTabCount() { return tabs.getTabCount(); }
	public Toolkit getToolkit() { return window.getToolkit(); }
	public Timer getUpdater() { return updater; }

}
