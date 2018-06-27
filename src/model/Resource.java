package model;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import model.Storage.Parser;
import model.Tools.Tool;
import view.Application;

public class Resource {

	private static Resource instance = null;
	
	public static Resource getInstance() {
		if (instance == null) instance = new Resource();
		return instance;
	}
	
	private Document stringLibrary;
	
	private Resource() {
		setLAF();
	}
	
	private void setLAF() {
		UIManager.put("PopupMenu.background", new Color(0));
		UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder());
		UIManager.put("Tree.expandedIcon",  new ImageIcon(getClass().getResource("/icons/treeMinus.gif")));
		UIManager.put("Tree.collapsedIcon", new ImageIcon(getClass().getResource("/icons/treePlus.gif")));
	}
	
	public BufferedImage getImage(String path) {
    	try {
			return ImageIO.read(new FileInputStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public BufferedImage getImageFile(File file) {
    	try {
			return ImageIO.read(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public BufferedImage resizeBitmap(BufferedImage original, double width, double height) {
		  int w = original.getWidth();  
		  int h = original.getHeight();  
		  BufferedImage scaled = new BufferedImage((int)width, (int)height, original.getType());  
		  Graphics2D g = scaled.createGraphics();
		  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);  
		  g.drawImage(original, 0, 0, scaled.getWidth(), scaled.getHeight(), 0, 0, w, h, null);  
		  g.dispose();  
		  return scaled;  
	}
	
	public void resizeImage(int imageIndex, Dimension oldSize, Dimension newSize) {
		Element image = Storage.getInstance().getImage(imageIndex);
		double width = oldSize.getWidth() / newSize.getWidth();
		double height = oldSize.getHeight() / newSize.getHeight();
		image.setAttribute("width", "" + newSize.width);
		image.setAttribute("height", "" + newSize.height);
		for (int layerIndex = 0; layerIndex < image.getChildNodes().getLength(); layerIndex++) {
			Element layer = Storage.getInstance().getLayer(imageIndex, layerIndex);
			for (int shapeIndex = 0; shapeIndex < layer.getChildNodes().getLength(); shapeIndex++) {
				Element shape = Storage.getInstance().getShape(imageIndex, layerIndex, shapeIndex);
				if (!shape.getAttribute("weight").equals("")) {
					double thickness = Double.parseDouble(shape.getAttribute("weight"));
					shape.setAttribute("weight", "" + (int)(thickness / Math.max(width, height)));
				}
				if (shape.getTagName().equals("Line")) {
					Point start = Parser.parsePoint(shape.getAttribute("start"));
					Point ctrl = Parser.parsePoint(shape.getAttribute("ctrl"));
					Point end = Parser.parsePoint(shape.getAttribute("end"));
					start.x = (int) (start.x / width); start.y = (int) (start.y / height);
					ctrl.x = (int) (ctrl.x / width); ctrl.y = (int) (ctrl.y / height);
					end.x = (int) (end.x / width); end.y = (int) (end.y / height);
					shape.setAttribute("start", Parser.createPoint(start));
					shape.setAttribute("ctrl", Parser.createPoint(ctrl));
					shape.setAttribute("end", Parser.createPoint(end));
				} else if (shape.getTagName().equals("Oval") || shape.getTagName().equals("Triangle") || shape.getTagName().equals("Rectangle") || shape.getTagName().equals("Bitmap") || shape.getTagName().equals("Text")) {
					Point location = Parser.parsePoint(shape.getAttribute("location"));
					location.x = (int) (location.x / width); location.y = (int) (location.y / height);
					shape.setAttribute("location", Parser.createPoint(location));
					double shapeWidth = Double.parseDouble(shape.getAttribute("width")) / width;
					double shapeHeight = Double.parseDouble(shape.getAttribute("height")) / height;
					shape.setAttribute("width", "" + shapeWidth);
					shape.setAttribute("height", "" + shapeHeight);
				} else if (shape.getTagName().equals("Pencil")) {
					for (int p = 0; p < shape.getChildNodes().getLength(); p++) {
						Element point = (Element) shape.getChildNodes().item(p);
						Point location = Parser.parsePoint(point.getAttribute("location"));
						location.x = (int) (location.x / width); location.y = (int) (location.y / height);
						point.setAttribute("location", Parser.createPoint(location));
					}
				}
			}
		}
	}
	
	public String loadString(String id) {
		String string = "Failed to load string resource";
		for (int i = 0; i < stringLibrary.getDocumentElement().getChildNodes().getLength(); i++) {
			if (stringLibrary.getDocumentElement().getChildNodes().item(i).getNodeName().equals(id)) {
				string = stringLibrary.getDocumentElement().getChildNodes().item(i).getTextContent();
			}
		}
		return string;
	}
	
	public Cursor getEraser() {
		Toolkit toolkit = Application.getInstance().getToolkit();
		Image image = toolkit.getImage(getClass().getResource("/icons/eraser.gif"));
	    Point hotspot = new Point(8,8);
	    Cursor cursor = toolkit.createCustomCursor(image, hotspot, "eraser");
	    return cursor;
	}
	
	public static class ImageImporter extends TransferHandler {
		@Override 
		public boolean canImport(JComponent component, DataFlavor[] flavors) {
			return true;
	    }
	    @Override 
	    public boolean importData(JComponent component, Transferable transferable) {
	    	try {
	    		Image img = null;
	    		for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
	    			if (DataFlavor.imageFlavor.equals(flavor)) {
	    				Object o = transferable.getTransferData(DataFlavor.imageFlavor);
	    				if (o instanceof Image) {
	    					Object ob = transferable.getTransferData(DataFlavor.javaFileListFlavor);
		    				if (ob instanceof List) {
		    					List slist = (List) ob;
		    					for (Object f : slist) {
		    						if (f instanceof File) {
		    							File file = (File) f;
		    							importImage(((File) f).getName(), (Image)o);	
		    						}	
		    					}	
		    				}
	    				}
	    			}
	    			if (DataFlavor.javaFileListFlavor.equals(flavor)) {
	    				Object o = transferable.getTransferData(DataFlavor.javaFileListFlavor);
	    				if (o instanceof List) {
	    					List list = (List) o;
	    					for (Object f : list) {
	    						if (f instanceof File) {
	    							File file = (File) f;
	    							System.out.println(file);
	    							if (!file.getName().endsWith(".bmp")) {
	    								importImage(file);
	    								return true;
	    							} 
	    						}
	    					}
	    				}
	    			}
	    		}
	        } catch (Exception ex) {
	          ex.printStackTrace();
	        }
	        return false;
	    }
	    @Override 
	    public int getSourceActions(JComponent component) {
	    	return COPY;
	    }
	    private void importImage(File file) {
	    	String bitmapName = file.getName();
	       	BufferedImage bitmap = Resource.getInstance().getImageFile(file);
	       	Application.getInstance().getCanvas().getTools().setBitmap(bitmap, bitmapName);
	       	Application.getInstance().getCanvas().getTools().setTool(Tool.BITMAP);
		}
		private void importImage(String name, Image image) {
			String bitmapName = name;
	       	BufferedImage bitmap = (BufferedImage) image;
	       	Application.getInstance().getCanvas().getTools().setBitmap(bitmap, bitmapName);
	       	Application.getInstance().getCanvas().getTools().setTool(Tool.BITMAP);
		}
	}
}

