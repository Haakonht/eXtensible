package controller;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Resource;
import model.Storage;
import model.Tools;
import model.Tools.Tool;
import view.Application;
import view.Components.Drawing;
import view.Components.Ruler;
import view.Dialogs.AboutDialog;
import view.Dialogs.BitmapDialog;
import view.Dialogs.CloseDialog;
import view.Dialogs.ColorDialog;
import view.Dialogs.CreateImageDialog;
import view.Dialogs.FontDialog;
import view.Dialogs.GridDialog;
import view.Dialogs.LayerDialog;
import view.Dialogs.PreferencesDialog;
import view.Dialogs.ResizeDialog;

public class Functions {
	
	private static int autoSave = 0;
	
	public static void routeFunction(String command) {
		if (command.equals("New Image")) {
			new CreateImageDialog();
		} else if (command.equals("Open Image")) {
			Storage.getInstance().openDocument();
		} else if (command.equals("Save Image")) {
			Storage.getInstance().saveDocument();
		} else if (command.equals("Save Image As")) {
			Storage.getInstance().saveDocumentAs();
		} else if (command.equals("Print")) {
			Drawing component = Application.getInstance().getDrawing();
			PrinterJob pjob = PrinterJob.getPrinterJob();
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			PageFormat postformat = pjob.pageDialog(aset);
			if (postformat != null) {
			    pjob.setPrintable(new Printer(component), postformat);
			    if (pjob.printDialog()) {
			        try {
						pjob.print();
					} catch (PrinterException e) {
						e.printStackTrace();
					}
			    }
			}
		} else if (command.equals("Exit")) { 
			if (Tools.CHANGED) new CloseDialog("You have unsaved changes.. ","Are you sure you want to close the application?");
			else new CloseDialog("Are you sure you want to close the application?");
		} else if (command.equals("Selector")) {
			Application.getInstance().getCanvas().getTools().setTool(Tool.SELECTOR);
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (command.equals("Pencil")) {
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Application.getInstance().getCanvas().getTools().setTool(Tool.PENCIL);
		} else if (command.equals("Eraser")) {
			Application.getInstance().getDrawing().setCursor(Resource.getInstance().getEraser());
			Application.getInstance().getCanvas().getTools().setTool(Tool.ERASER);
		} else if (command.equals("Line")) {
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Application.getInstance().getCanvas().getTools().setTool(Tool.LINE);
		} else if (command.equals("Oval")) {
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Application.getInstance().getCanvas().getTools().setTool(Tool.OVAL);
		} else if (command.equals("Triangle")) {
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Application.getInstance().getCanvas().getTools().setTool(Tool.TRIANGLE);
		} else if (command.equals("Rectangle")) {
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Application.getInstance().getCanvas().getTools().setTool(Tool.RECTANGLE);
		} else if (command.equals("Bitmap")) {
			new BitmapDialog();
		} else if (command.equals("Import Bitmap")) {
			JFileChooser chooser = new JFileChooser();
			chooser.addChoosableFileFilter(new FileNameExtensionFilter( "Supported Image Formats (jpg, jpeg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"));
		    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "JPEG Image (.jpeg)", "jpg" ,"jpeg"));
		    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "PNG Image (.png)", "png"));
		    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "GIF Image (.gif)", "gif"));
		    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "Bitmap Image (.bmp)", "bmp"));
		    chooser.setAcceptAllFileFilterUsed(false);
		    chooser.setApproveButtonText("Select");
		    chooser.setDialogTitle("Select Bitmap");
		    int returnVal = chooser.showOpenDialog(Application.getInstance().getWindow());
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	File selectedFile = chooser.getSelectedFile();
	            String path = selectedFile.getAbsolutePath();
	    		Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		    	Application.getInstance().getCanvas().getTools().setBitmap(Resource.getInstance().getImage(path), selectedFile.getName());
		    	Application.getInstance().getCanvas().getTools().setTool(Tools.Tool.BITMAP);
		    }
		} else if (command.equals("Text")) {
			Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			Application.getInstance().getCanvas().getTools().setTool(Tool.TEXT);
		} else if (command.equals("Set Outline")) {
			new ColorDialog().showColorChooser("Outline");
		} else if (command.equals("Set Fill")) {
			new ColorDialog().showColorChooser("Fill");
		} else if (command.equals("Set Background")) {
			new ColorDialog().showColorChooser("Background");
		} else if (command.equals("Resize Canvas")) {
			new ResizeDialog(false);
		} else if (command.equals("Resize Image")) {
			new ResizeDialog(true);
		} else if (command.equals("Application Menu")) {
			Application.getInstance().toggleToolBar();
		} else if (command.equals("Canvas")) {
			Application.getInstance().toggleCanvas();
		} else if (command.equals("Status")) {
			Application.getInstance().toggleStatus();
		} else if (command.equals("Set Font")) {
			new FontDialog();
		} else if (command.equals("Preferences")) {
			new PreferencesDialog();
		} else if (command.equals("Undo")) {
			Storage.getInstance().undo();
			reloadUI();
		} else if (command.equals("Redo")) {
			Storage.getInstance().redo();
			reloadUI();
		} else if (command.equals("Copy")) {
			Storage.getInstance().copy(false);
			Application.getInstance().reloadMenu();
		} else if (command.equals("Cut")) {
			Storage.getInstance().copy(true);
			reloadUI();
		} else if (command.equals("Paste")) {
			Storage.getInstance().paste();
			reloadUI();
		} else if (command.equals("Add Layer")) {
			new LayerDialog();
		} else if (command.equals("Remove Layer")) {
			Storage.getInstance().removeLayerByIndex(Application.getInstance().getCanvas().getTools().getImageIndex(), Application.getInstance().getCanvas().getTools().getLayerIndex());
			reloadUI();
		} else if (command.equals("Layer Down")) {
			Storage.getInstance().layerUp();
			reloadUI();
		} else if (command.equals("Layer Up")) {
			Storage.getInstance().layerDown();
			reloadUI();
		} else if (command.equals("Delete Selected")) {
			Storage.getInstance().deleteSelected();
			reloadUI();
		} else if (command.equals("Zoom +")) {
			if (Application.getInstance().getCanvas().getTools().getZoom() < 5.00) Application.getInstance().getCanvas().getTools().setZoom(Application.getInstance().getCanvas().getTools().getZoom() + 0.25);
			reloadUI();
		} else if (command.equals("Zoom -")) {
			if (Application.getInstance().getCanvas().getTools().getZoom() > 0.25) Application.getInstance().getCanvas().getTools().setZoom(Application.getInstance().getCanvas().getTools().getZoom() - 0.25);
			reloadUI();
		} else if (command.equals("Reset Zoom")) {
			Application.getInstance().getCanvas().getTools().setZoom(1.00);
			reloadUI();
		} else if (command.equals("About")) {
			new AboutDialog();
		} else if (command.equals("Deselect All")) {
			Storage.getInstance().deselectAll();
			reloadUI();
		} else if (command.equals("Grid")) {
			new GridDialog();
		} else if (command.equals("Clear Grid")) {
			Application.getInstance().getRuler(Ruler.HORIZONTAL).clearGrid();
			Application.getInstance().getRuler(Ruler.VERTICAL).clearGrid();
			Application.getInstance().repaintCanvas();
		}
	} 
	
	private static void reloadUI() {
		Application.getInstance().reloadMenu();
		Application.getInstance().reloadProject();
		Application.getInstance().repaintCanvas();
	}
	
	private static class Printer implements Printable {
	    final Component comp;
	    public Printer(Component comp){
	        this.comp = comp;
	    }

	    @Override
	    public int print(Graphics g, PageFormat format, int page_index) 
	            throws PrinterException {
	        if (page_index > 0) {
	            return Printable.NO_SUCH_PAGE;
	        }
	        Dimension dim = comp.getSize();
	        double cHeight = dim.getHeight();
	        double cWidth = dim.getWidth();

	        double pHeight = format.getImageableHeight();
	        double pWidth = format.getImageableWidth();

	        double pXStart = format.getImageableX();
	        double pYStart = format.getImageableY();

	        double xRatio = pWidth / cWidth;
	        double yRatio = pHeight / cHeight;

	        Graphics2D g2 = (Graphics2D) g;
	        g2.translate(pXStart, pYStart);
	        g2.scale(xRatio, yRatio);
	        comp.paint(g2);

	        return Printable.PAGE_EXISTS;
	    }
	}

}
