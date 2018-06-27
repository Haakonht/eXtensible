package model;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

import org.w3c.dom.Element;

import model.Settings.SerializedSettings.ToolData;
import model.Storage.Parser;
import view.Application;

public class Tools {
	
	public static boolean DEBUG = false;
	public static boolean CHANGED = false;
	
	public enum Tool { 
		SELECTOR("Selector"), BITMAP("Bitmap"), PENCIL("Pencil"), ERASER("Eraser"), LINE("Line"), OVAL("Oval"), TRIANGLE("Triangle"), RECTANGLE("Rectangle"), TEXT("Text"), MOVE("Shape Mover"), RESIZE("Shape Resizer"), ROTATE("Shape Rotator"), CURVE("Line Curver"); 
		private String type;
		private Tool(String type) { this.type = type; }
		public String getType() { return type; }
	}
	
	private Element pencil = null;
	private BufferedImage bitmap = null;
	private String bitmapName = "";
	private int imageIndex, layerIndex, weight = 5;
	private Tool activeTool = Tool.RECTANGLE;
	private Color outline = Color.BLACK, fill = Color.WHITE;
	private GradientPaint outlineGradient, fillGradient;
	private Font font = new Font("Times New Roman", Font.PLAIN, 14);
	private Double zoom = 1.00;
	
	public Tools(int imageIndex) {
		this.imageIndex = imageIndex;
		layerIndex = 0;
	}
	
	public Tools(int imageIndex, ToolData toolData) {
		this.imageIndex = imageIndex;
		layerIndex = toolData.layerIndex;
		activeTool = toolData.tool;
		weight = toolData.weight;
		font = toolData.font;
		outline = toolData.outline;
		fill = toolData.fill;
		zoom = toolData.zoom;
	}
	
	public void useTool(int shapeIndex, ShapeData shapeData, Point point) {
		if (activeTool == Tool.ERASER) {
			return;
		} else if (activeTool == Tool.MOVE || activeTool == Tool.RESIZE || activeTool == Tool.ROTATE || activeTool == Tool.CURVE) {
			editShape(shapeIndex, point);
		} else if (activeTool == Tool.SELECTOR) {
			selectShape(point);
		} else if (activeTool == Tool.TEXT) {
			createText(shapeData);
		} else if (activeTool == Tool.PENCIL) {
			createPencil(point);
		} else if (activeTool == Tool.LINE) {
			createLine(shapeData.location, shapeData.ctrl, point);
		} else if (activeTool == Tool.BITMAP) {
			createBitmap(shapeData);
		} else {
			createShape(shapeData);
		}
		CHANGED = true;
	}
	
	private void createLine(Point start, Point ctrl, Point end) {
		Element shape = Storage.getInstance().getDocument().createElement(activeTool.getType());
		shape.setAttribute("selected", Parser.createBoolean(false));
		start.x = (int) (start.x / zoom); start.y = (int) (start.y / zoom);
		shape.setAttribute("start", Parser.createPoint(start));	                        
		ctrl.x = (int) (ctrl.x / zoom); ctrl.y = (int) (ctrl.y / zoom);
		shape.setAttribute("ctrl", Parser.createPoint(ctrl));
		end.x = (int) (end.x / zoom); end.y = (int) (end.y / zoom);
		shape.setAttribute("end", Parser.createPoint(end));
		shape.setAttribute("weight", "" + weight);
		shape.setAttribute("color", Parser.createColor(outline));
		if (shape != null) Storage.getInstance().getLayer(imageIndex, layerIndex).appendChild(shape);
		if (Tools.DEBUG) System.out.println("TOOLS - Creating Shape In Image Index: " + imageIndex + " Layer Index: " + layerIndex);
		Application.getInstance().repaintCanvas();
		Application.getInstance().reloadMenu();
		Application.getInstance().reloadProject();
	}
	
	private void createBitmap(ShapeData shapeData) {
		Element image = Storage.getInstance().getDocument().createElement(activeTool.getType());
		Element bits = Storage.getInstance().getDocument().createElement("Bitmap");
		String imageData = Parser.createBitmap(bitmap);
		image.setAttribute("path", bitmapName);
		bits.setAttribute("path", bitmapName);
		image.setAttribute("selected", Parser.createBoolean(false));
		shapeData.location.x = (int) (shapeData.location.x / zoom); shapeData.location.y = (int) (shapeData.location.y / zoom);
		double width = Math.abs(shapeData.width / zoom); double height = Math.abs(shapeData.height / zoom);
		image.setAttribute("location", Parser.createPoint(shapeData.location));
		image.setAttribute("width", "" + width);
		image.setAttribute("height", "" + height);
		image.setAttribute("rotation", "" + shapeData.rotation);
		bits.setTextContent(imageData);
		if (image != null && bits != null) {
			Storage.getInstance().getLayer(imageIndex, layerIndex).appendChild(image);
			Boolean exists = false;
			for (int i = 0; i < Storage.getInstance().getBitmaps().getChildNodes().getLength(); i++) {
				Element oldData = (Element) Storage.getInstance().getBitmaps().getChildNodes().item(i);
				if (oldData.getTextContent().equals(imageData)) exists = true;
			}
			if (!exists) Storage.getInstance().getBitmaps().appendChild(bits);
		}
		bitmap = null;
		bitmapName = "";
		activeTool = Tool.SELECTOR;
		Application.getInstance().repaintCanvas();
		Application.getInstance().reloadMenu();
		Application.getInstance().reloadProject();
	}
	
	private void createShape(ShapeData shapeData) {
		Element shape = Storage.getInstance().getDocument().createElement(activeTool.getType());
		shape.setAttribute("selected", Parser.createBoolean(false));
		shapeData.location.x = (int) (shapeData.location.x / zoom); shapeData.location.y = (int) (shapeData.location.y / zoom);
		shape.setAttribute("location", Parser.createPoint(shapeData.location));	                        
		shape.setAttribute("width", "" + Math.abs(shapeData.width / zoom));
		shape.setAttribute("height", "" + Math.abs(shapeData.height / zoom));
		shape.setAttribute("weight", "" + weight);
		shape.setAttribute("outline", Parser.createColor(outline));
		shape.setAttribute("fill", Parser.createColor(fill));
		shape.setAttribute("rotation", "" + shapeData.rotation);
		if (shape != null) Storage.getInstance().getLayer(imageIndex, layerIndex).appendChild(shape);
		if (Tools.DEBUG) System.out.println("TOOLS - Creating Shape In Image Index: " + imageIndex + " Layer Index: " + layerIndex);
		Application.getInstance().repaintCanvas();
		Application.getInstance().reloadMenu();
		Application.getInstance().reloadProject();
	}
	
	private void createPencil(Point coords) {
		coords.x = (int) (coords.x / zoom); coords.y = (int) (coords.y / zoom);
		Element point = Storage.getInstance().getDocument().createElement("Point");
		point.setAttribute("location", Parser.createPoint(coords));
		if (pencil != null) pencil.appendChild(point);
	}

	private void editShape(int shapeIndex, Point point) {
		point.x = (int) (point.x / zoom); point.y = (int) (point.y / zoom);
		Element shape = Storage.getInstance().getShape(imageIndex, layerIndex, shapeIndex);
		if (activeTool == Tool.MOVE) shape.setAttribute("location", Parser.createPoint(new Point(point.x - (int) ((Double.parseDouble(shape.getAttribute("width")) / 2)), point.y - (int) ((Double.parseDouble(shape.getAttribute("height")) / 2)))));
		else if (activeTool == Tool.CURVE) shape.setAttribute("ctrl", Parser.createPoint(point));
		else if (activeTool == Tool.ROTATE) {
			Point origin = Parser.parsePoint(shape.getAttribute("location"));
			origin.x = (int) (origin.x / zoom); origin.y = (int) (origin.y / zoom);
			int width  = (int) (Double.parseDouble(shape.getAttribute("width")) / zoom);
			Point trans = new Point(origin.x + width, origin.y);
			int diff = trans.x - point.x + trans.y - point.y;
			if (diff < 179) {
				if (diff > -179) {
					shape.setAttribute("rotation", "" + diff);
				}
			}	
		}
		else if (activeTool == Tool.RESIZE) {
			Point location = Parser.parsePoint(shape.getAttribute("location"));
			shape.setAttribute("width", "" + Math.negateExact(location.x - point.x));
			shape.setAttribute("height", "" + Math.negateExact(location.y - point.y));
		}
		Application.getInstance().repaintCanvas();
		CHANGED = true;
	}

	private void createText(ShapeData shapeData) {
		Element shape = Storage.getInstance().getDocument().createElement("Text");
		shape.setAttribute("selected", Parser.createBoolean(true));
		shapeData.location.x = (int) (shapeData.location.x / zoom); shapeData.location.y = (int) (shapeData.location.y / zoom);
		shape.setAttribute("location", Parser.createPoint(shapeData.location));
		shape.setAttribute("width", "" + shapeData.width / zoom);
		shape.setAttribute("height", "" + shapeData.height / zoom);
		shape.setAttribute("rotation", "" + shapeData.rotation);
		shape.setAttribute("color", Parser.createColor(outline));
		shape.setAttribute("font", Parser.createFont(font));
		shape.setAttribute("text", "");
		if (shape != null) Storage.getInstance().getLayer(imageIndex, layerIndex).appendChild(shape);
		if (Tools.DEBUG) System.out.println("TOOLS - Creating Shape In Image Index: " + imageIndex + " Layer Index: " + layerIndex);
		Application.getInstance().repaintCanvas();
		Application.getInstance().reloadProject();
	}
	
	public void removePencil(Point coords) {
		Rectangle rect = new Rectangle(coords.x - 6, coords.y - 6, 12, 12);
		Storage.getInstance().removePencil(rect, imageIndex, layerIndex);
		Application.getInstance().repaintCanvas();
	}
	
	public void selectShape(Point point) {
		Element selectedShape = null;
		for (int i = 0; i < Storage.getInstance().getLayer(imageIndex, layerIndex).getChildNodes().getLength(); i++) {
			Element shape = Storage.getInstance().getShape(imageIndex, layerIndex, i);
			if (Storage.getInstance().contains(shape, point)) {
				selectedShape = shape;
			} else {
				Storage.getInstance().getShape(imageIndex, layerIndex, i).setAttribute("selected", Parser.createBoolean(false));
			}
		}
		if (selectedShape != null) selectedShape.setAttribute("selected", Parser.createBoolean(true));
		Application.getInstance().reloadProject();
		Application.getInstance().repaintCanvas();
	}
	
	public void drawShape(Element shape, Graphics2D graphSettings) {
		if (shape.getTagName().equals("Text")) {
			drawText(shape, graphSettings); 
			return;
		} else if (shape.getTagName().equals("Bitmap")) {
			drawBitmap(shape, graphSettings);
			return;
		}
		GradientPaint outlineGradient = null;
		Color outline = null;
		GradientPaint fillGradient = null;
		Color fill = null; 
		Point location = null;
		int width = 0, height = 0, rotation = 0, weight = (int) (Double.parseDouble(shape.getAttribute("weight")) * zoom);
		if (shape.getTagName().equals("Line")) {
			outline = (Color) Parser.parseColor(shape.getAttribute("color"));
		}
		else {
			location = Parser.parsePoint(shape.getAttribute("location"));
			location.x = (int) (location.x * zoom);
			location.y = (int) (location.y * zoom);
			width = (int) (Double.parseDouble(shape.getAttribute("width")) * zoom);
			height = (int) (Double.parseDouble(shape.getAttribute("height")) * zoom);
			if (Parser.parseColor(shape.getAttribute("outline")) instanceof Color) {
				outline = (Color) Parser.parseColor(shape.getAttribute("outline"));
			} else {
				outlineGradient = (GradientPaint) Parser.parseColor(shape.getAttribute("outline"));
			}
			if (Parser.parseColor(shape.getAttribute("fill")) instanceof Color) {
				fill = (Color) Parser.parseColor(shape.getAttribute("fill"));
			} else {
				fillGradient = (GradientPaint) Parser.parseColor(shape.getAttribute("fill"));
			}
			rotation = Integer.parseInt(shape.getAttribute("rotation"));
		}
		
		AffineTransform oldXForm = graphSettings.getTransform();
		graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		if (Parser.parseBoolean(shape.getAttribute("selected"))) graphSettings.setStroke(new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
		else graphSettings.setStroke(new BasicStroke(weight));
		if (shape.getTagName().equals("Triangle")) {
			Point centroid = getTriangleCentroid(location, new Point(location.x + width, location.y + height));
			graphSettings.rotate(Math.toRadians(rotation), centroid.x, centroid.y);
		}
		else if (!shape.getTagName().equals("Line")) {
			graphSettings.rotate(Math.toRadians(rotation), location.x + (width / 2), location.y + (height / 2));
		}
		if (shape.getTagName().equals("Line")) {
			Point start = Parser.parsePoint(shape.getAttribute("start"));
			Point ctrl = Parser.parsePoint(shape.getAttribute("ctrl"));
			Point end = Parser.parsePoint(shape.getAttribute("end"));
			start.x = (int) (start.x * zoom); start.y = (int) (start.y * zoom);
			ctrl.x = (int) (ctrl.x * zoom); ctrl.y = (int) (ctrl.y * zoom);
			end.x = (int) (end.x * zoom); end.y = (int) (end.y * zoom);
			if (outline == null) graphSettings.setPaint(outlineGradient);
			else graphSettings.setColor(outline);
				QuadCurve2D.Double curve = new QuadCurve2D.Double(start.x, start.y, ctrl.x, ctrl.y, end.x, end.y);
	            graphSettings.draw(curve);	
		} else if (shape.getTagName().equals("Rectangle")) {
			if (fill == null) graphSettings.setPaint(fillGradient);
			else graphSettings.setColor(fill);
			graphSettings.fillRect(location.x, location.y, width, height);
			if (outline == null) graphSettings.setPaint(outlineGradient);
			else graphSettings.setColor(outline);
			graphSettings.drawRect(location.x, location.y, width, height);
			graphSettings.setTransform(oldXForm);
		} else if (shape.getTagName().equals("Oval")) {
			if (fill == null) graphSettings.setPaint(fillGradient);
			else graphSettings.setColor(fill);
			graphSettings.fillOval(location.x, location.y, width, height);
			if (outline == null) graphSettings.setPaint(outlineGradient);
			else graphSettings.setColor(outline);
			graphSettings.drawOval(location.x, location.y, width, height);
			graphSettings.setTransform(oldXForm);
		} else if (shape.getTagName().equals("Triangle")) {
			Point end = new Point(location.x + width, location.y + height);
			graphSettings.setColor(fill);
			graphSettings.fill(createTriangle(location, end));	
			graphSettings.setColor(outline);
			graphSettings.draw(createTriangle(location, end));
			graphSettings.setTransform(oldXForm);
		}
		if (Tools.DEBUG) System.out.println("TOOLS - Rendering Shape: " + shape.toString());
	}
	public void drawPencil(Element pencilArray, Graphics2D graphSettings) {
		Color color = (Color) Parser.parseColor(pencilArray.getAttribute("color"));
		int weight = (int) (Double.parseDouble(pencilArray.getAttribute("weight")) * zoom);
		graphSettings.setColor(color);
		for (int i = 0; i < pencilArray.getChildNodes().getLength(); i++) {
			Element point = (Element) pencilArray.getChildNodes().item(i);
			Point location = Parser.parsePoint(point.getAttribute("location"));
			location.x = (int) (location.x * zoom);
			location.y = (int) (location.y * zoom);
			graphSettings.fillOval(location.x - (weight / 2), location.y - (weight / 2), weight, weight);
		}
	}
	public void drawText(Element shape, Graphics2D graphSettings) {
		Color color = (Color) Parser.parseColor(shape.getAttribute("color"));
		Point location = Parser.parsePoint(shape.getAttribute("location"));
		location.x = (int) (location.x * zoom);
		location.y = (int) (location.y * zoom);
		int width = (int) (Double.parseDouble(shape.getAttribute("width")) * zoom);
		int height = (int) (Double.parseDouble(shape.getAttribute("height")) * zoom);
		int weight = (int) (5 * zoom);
		int rotation = Integer.parseInt(shape.getAttribute("rotation"));
		String text = shape.getAttribute("text");
		Font style = Parser.parseFont(shape.getAttribute("font"));
		style = new Font(style.getFontName(), style.getStyle(), (int) (style.getSize() * zoom));
		int halfFont = style.getSize() / 2;
		
		AffineTransform oldXForm = graphSettings.getTransform();
		graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		graphSettings.setStroke(new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
		graphSettings.rotate(Math.toRadians(rotation), location.x + (width / 2), location.y + (height / 2));
		graphSettings.setColor(Color.BLACK);
		if (Parser.parseBoolean(shape.getAttribute("selected"))) graphSettings.drawRect(location.x, location.y, width, height);
		graphSettings.setFont(style);
		graphSettings.setColor(color);
		graphSettings.drawString(text, location.x, (location.y + halfFont) + (height / 2));
		graphSettings.setTransform(oldXForm);
	}
	public void drawBitmap(Element bitmap, Graphics2D graphSettings) {
		String path = bitmap.getAttribute("path");
		Point location = Parser.parsePoint(bitmap.getAttribute("location"));
		location.x = (int) (location.x * zoom);
		location.y = (int) (location.y * zoom);
		int width = (int) (Double.parseDouble(bitmap.getAttribute("width")) * zoom);
		int height = (int) (Double.parseDouble(bitmap.getAttribute("height")) * zoom);
		int rotation = Integer.parseInt(bitmap.getAttribute("rotation"));
		BufferedImage image = Parser.parseBitmap(Storage.getInstance().getBitmap(path).getTextContent());
		
		AffineTransform oldXForm = graphSettings.getTransform();
		graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		graphSettings.rotate(Math.toRadians(rotation), location.x + (width / 2), location.y + (height / 2));
        graphSettings.drawImage(image, location.x, location.y, width, height, null);
		if (Parser.parseBoolean(bitmap.getAttribute("selected"))) {
			graphSettings.setColor(Color.BLACK);
			graphSettings.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
			graphSettings.drawRect(location.x, location.y, width, height);
		}
		graphSettings.setTransform(oldXForm);
	}
	
	public void setBitmap(BufferedImage bitmap, String bitmapName) { this.bitmap = bitmap; this.bitmapName = bitmapName; }
	public void setZoom(Double zoom) { this.zoom = zoom; }
	public void setLayer(int layerIndex) { this.layerIndex = layerIndex; }
	public void setWeight(int weight) { this.weight = weight; }
	public void setOutline(Color outline) { this.outline = outline; }
	public void setFill(Color fill) { this.fill = fill; }
	public void setTool(Tool tool) { this.activeTool = tool; }
	public void setBackground(Color background) { 
		Storage.getInstance().getImage(imageIndex).setAttribute("background", Parser.createColor(background)); 
		Application.getInstance().repaintCanvas();
	}
	public void setBackground(GradientPaint background) {
		Storage.getInstance().getImage(imageIndex).setAttribute("background", Parser.createColor(background));
	}
	public void setFont(Font font) { this.font = font; }
	
	public void createPencil() { 
		pencil = Storage.getInstance().getDocument().createElement("Pencil"); 
		pencil.setAttribute("weight", "" + weight / zoom);
		pencil.setAttribute("color", Parser.createColor(outline));
	}
	public void setPencil() { Storage.getInstance().getLayer(imageIndex, layerIndex).appendChild(pencil); pencil = null; Application.getInstance().reloadProject(); } 
	
	public Double getZoom() { return zoom; }
	public Element getPencil() { return pencil; }
	public Font getFont() { return font; }
	public Tool getTool() { return activeTool; }
	public int getWeight() { return weight; }
	public int getImageIndex() { return imageIndex; }
	public int getLayerIndex() { return layerIndex; }
	public Color getOutline() { return outline; }
	public Color getFill() { return fill; }
	public Object getBackground() { 
		if (Parser.parseColor(Storage.getInstance().getImage(imageIndex).getAttribute("background")) instanceof Color) return (Color) Parser.parseColor(Storage.getInstance().getImage(imageIndex).getAttribute("background")); 
		else return (GradientPaint) Parser.parseColor(Storage.getInstance().getImage(imageIndex).getAttribute("background")); 
	}
	
	public Polygon createTriangle(Point start, Point end){
		int x0 = (int)start.getX();
        int y0 = (int)start.getY();
        int x1 = (int)end.getX();
        int y1 = (int)end.getY();
		
		Point2D point2b = computeTipPoint(start, end);
	    int x2b = (int)point2b.getX();
	    int y2b = (int)point2b.getY();

        int xCoordb[] = {x0, x1, x2b};
        int yCoordb[] = {y0, y1, y2b};
		return new Polygon(xCoordb, yCoordb, 3);
	}
	public Point getTriangleCentroid(Point start, Point end){
		int x0 = (int)start.getX();
        int y0 = (int)start.getY();
        int x1 = (int)end.getX();
        int y1 = (int)end.getY();
		
		Point2D point2b = computeTipPoint(start, end);
	    int x2b = (int)point2b.getX();
	    int y2b = (int)point2b.getY();

        int xCoordb[] = {x0, x1, x2b};
        int yCoordb[] = {y0, y1, y2b};
		return new Point((x0 + x1 + x2b) / 3, (y0 + y1 + y2b) / 3);
	}
	private Point2D computeTipPoint(Point2D p0, Point2D p1) {
	    double dx = p1.getX() - p0.getX();
	    double dy = p1.getY() - p0.getY();
	    double length = Math.sqrt(dx*dx+dy*dy);
	    double dirX = dx / length;
	    double dirY = dy / length;
	    double height = Math.sqrt(3)/2 * length;
	    double cx = p0.getX() + dx * 0.5;
	    double cy = p0.getY() + dy * 0.5;
	    double pDirX = -dirY;
	    double pDirY = dirX;
	    double rx = 0;
	    double ry = 0;
	    rx = cx - height * pDirX;
	    ry = cy - height * pDirY;
	    return new Point2D.Double(rx, ry);
	}
	
	public static class ShapeData {
		public int width, height, rotation;
		public Point location, ctrl;
		public ShapeData(Point location, Point ctrl, int width, int height, int rotation) {
			this.location = location; this.width = width; this.height = height; this.rotation = rotation; this.ctrl = ctrl;
			if (Tools.DEBUG) System.out.println("SHAPE CREATED - Location: " + location.toString() + " Width: " + width + " Height: " + height);
		}
	}
}
