package controller;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import model.Resource;
import model.Settings.SerializedSettings.ToolData;
import model.Storage;
import model.Tools;
import model.Tools.ShapeData;
import model.Tools.Tool;
import view.Application;
import view.Components.Selector;
import view.Components.ShapeContext;
import view.Components.ToolContext;

public class Canvas implements MouseListener, MouseMotionListener {

	private Tools tools;
	private boolean selectorOperation = false;
	private Point origin = null;
	private Point shadow = null;
	private Point position = null;
	private Tool rememberTool = null;
	private double renderTime = 0.00;
	
	private ArrayList<Selector> selectors;
	private int activeSelectorIndex = -1;
	
	public Canvas(String title) {
		Storage.getInstance().createRedoList();
		tools = new Tools(Storage.getInstance().getImageIndex(title));
		selectors = new ArrayList<Selector>();
	}
	
	public Canvas(String title, ToolData toolData) {
		Storage.getInstance().createRedoList();
		tools = new Tools(Storage.getInstance().getImageIndex(title), toolData);
		selectors = new ArrayList<Selector>();
	}
	
	private void selectorOperationStart(MouseEvent e) {
		if (!selectors.isEmpty()) {
			for (int i = 0; i < selectors.size(); i++) {
				Selector selector = selectors.get(i);
				if (selector.moveButton != null) {
					if (selector.moveButton.contains(e.getX(), e.getY())) {
						if (rememberTool == null) {
							rememberTool = tools.getTool();
							tools.setTool(Tool.MOVE);
							selectorOperation = true;
							activeSelectorIndex = selector.getShapeIndex();
							Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						}
					} else if (selector.resizeButton.contains(e.getX(), e.getY())) {
						if (rememberTool == null) {
							rememberTool = tools.getTool();
							tools.setTool(Tool.RESIZE);
							selectorOperation = true;
							activeSelectorIndex = selector.getShapeIndex();
							Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
						}
					} else if (selector.rotateButton.contains(e.getX(), e.getY())) {
						if (rememberTool == null) {
							rememberTool = tools.getTool();
							tools.setTool(Tool.ROTATE);
							selectorOperation = true;
							activeSelectorIndex = selector.getShapeIndex();
							Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						}
					} 
				}
				else {
					if (selector.curveButton.contains(e.getX(), e.getY())) {
						if (rememberTool == null) {
							rememberTool = tools.getTool();
							tools.setTool(Tool.CURVE);
							selectorOperation = true;
							activeSelectorIndex = selector.getShapeIndex();
							Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						}
					}
				}
			}				
		}
	}
	private void selectorOperationEnd() {
		if (rememberTool != null) {
			tools.setTool(rememberTool);
			rememberTool = null;
			selectorOperation = false;
			if (tools.getTool() == Tool.SELECTOR) Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			else if (tools.getTool() == Tool.ERASER) Application.getInstance().getDrawing().setCursor(Resource.getInstance().getEraser());
			else Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			activeSelectorIndex = -1;
			selectors.clear();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			doPop(e);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			Storage.getInstance().debugDocument();
		} else {
			if (tools.getTool() == Tool.PENCIL) tools.createPencil();
			selectorOperationStart(e);
			origin = e.getPoint();
			shadow = null;
		}
		if (Application.DEBUG) System.out.println("CANVAS - Mouse Pressed");
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (tools.getTool() == Tool.PENCIL) tools.setPencil();
			if (tools.getTool() == Tool.SELECTOR) tools.useTool(0, null, e.getPoint());
			if (shadow != null && tools.getTool() != Tool.LINE) {
				int x = Math.min(origin.x, shadow.x);
				int y = Math.min(origin.y, shadow.y); 
				tools.useTool(activeSelectorIndex, new ShapeData(new Point(x,y), new Point((e.getPoint().x + origin.x) / 2, (e.getPoint().y + origin.y) / 2), Math.abs(origin.x - e.getPoint().x), Math.abs(origin.y - e.getPoint().y), 0), e.getPoint());
			} else if (tools.getTool() == Tool.LINE) {
				tools.useTool(activeSelectorIndex, new ShapeData(origin, new Point((e.getPoint().x + origin.x) / 2, (e.getPoint().y + origin.y) / 2), Math.negateExact(origin.x - e.getPoint().x), Math.negateExact(origin.y - e.getPoint().y), 0), e.getPoint());
			}
			selectorOperationEnd();
			origin = null;
			shadow = null;
		}
		if (Application.DEBUG) System.out.println("CANVAS - Mouse Released");
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		position = e.getPoint();
		if (selectorOperation) tools.useTool(activeSelectorIndex, null, e.getPoint());
		if (tools.getTool() == Tool.PENCIL) tools.useTool(0, null, e.getPoint());
		if (tools.getTool() == Tool.ERASER) tools.removePencil(e.getPoint());
		shadow = e.getPoint();
		Application.getInstance().repaintCanvas();
	}

	private void doPop(MouseEvent e){
		if (tools.getTool() == Tool.SELECTOR || tools.getTool() == Tool.TEXT ||  tools.getTool() == Tool.ERASER) {
			if (Storage.getInstance().getTopSelectedShape(e.getPoint()) != null) {
				ShapeContext shapeContext = new ShapeContext(Storage.getInstance().getTopSelectedShape(e.getPoint()));
				shapeContext.show(e.getComponent(), e.getX(), e.getY());
			}
		} else {
			 ToolContext toolContext = new ToolContext(this);
			 toolContext.show(e.getComponent(), e.getX(), e.getY()); 
		}    
	 }
	
	public Tools getTools() { return tools; }
	public Point getShadow() { return shadow; }
	public Point getOrigin() { return origin; }
	public Point getPosition() { return position; }
	public double getRendertime() { return renderTime; }
	public void setPosition(Point position) { this.position = position; }
	public void addSelector(Selector selector) { selectors.add(selector); }
	public Selector getSelector(int selectorIndex) { return selectors.get(selectorIndex); }
	public int getSelectorCount() { return selectors.size(); }
	public void setRendertime(Double renderTime) { this.renderTime = renderTime; }
	
	@Override
	public void mouseMoved(MouseEvent e) {
		position = e.getPoint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

}
