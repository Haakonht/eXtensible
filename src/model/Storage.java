package model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import controller.Functions;
import view.Application;
import view.Components.Drawing;

public class Storage {
	
	public static boolean DEBUG = false;
	
	public static Storage getInstance() {
		return ResourceLoader.STORAGE;
	}

	private static final class ResourceLoader {
		private static final Storage STORAGE = new Storage();
	}

	private int elementCount;
	private Document XML;
	private Element project, images, bitmaps;
	private ArrayList<ArrayList<Element>> redoList;
	private ArrayList<Element> copyList;
	private String filePath = "";
	
	private Storage() {
		DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFac.newDocumentBuilder();
			XML = dBuilder.newDocument();
			project = XML.createElement("Project");
			images = XML.createElement("Images");
			bitmaps = XML.createElement("Bitmaps");
			XML.appendChild(project);
			project.appendChild(images);
			project.appendChild(bitmaps);
			redoList = new ArrayList<ArrayList<Element>>();
			copyList = new ArrayList<Element>();
			elementCount = getElementCount();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void autoSave() {
		if (elementCount != getElementCount() || Tools.CHANGED) {
			if (!filePath.equals("")) {
				System.out.println("STORAGE - Autosaving");
				saveDocument();
			}
			elementCount = getElementCount();
		}
	}
	
	public Document getDocument() { return XML; }
	public Element getImages() { return images; }
	public Element getImage(int imageIndex) { return (Element) images.getChildNodes().item(imageIndex); }
	public Element getLayer(int imageIndex, int layerIndex) { return (Element) getImage(imageIndex).getChildNodes().item(layerIndex); }
	public Element getShape(int imageIndex, int layerIndex, int shapeIndex) { return (Element) getLayer(imageIndex, layerIndex).getChildNodes().item(shapeIndex); }
	public String getFilePath() { return filePath; }
	public Element getBitmaps() { return bitmaps; }
	public Element getBitmap(String path) {
		for (int i = 0; i < bitmaps.getChildNodes().getLength(); i++) {
			Element bitmap = (Element) bitmaps.getChildNodes().item(i);
			if (bitmap.getAttribute("path").equals(path)) return bitmap;
		}
		return null;
	}
	
	public void createRedoList() { redoList.add(new ArrayList<Element>()); }
	public void removeRedoList(int imageIndex) { redoList.remove(imageIndex); }
	public Boolean isRedoEmpty(int imageIndex) { 
		if (redoList.get(imageIndex) == null) return false;
		return redoList.get(imageIndex).isEmpty(); 
	}
	public int getImageCount() { return images.getChildNodes().getLength(); }
	public int getLayerCount(int imageIndex) { return getImage(imageIndex).getChildNodes().getLength(); }
	public Boolean isCopyEmpty() { return copyList.isEmpty(); }
	public Element getRedo(int imageIndex) { 
		if (!redoList.get(imageIndex).isEmpty()) return redoList.get(imageIndex).get(redoList.get(imageIndex).size() - 1); 		
		else return null;
	}
	public String getLastAddedShapeName(int imageIndex) {
		String string = "";
		for (int i = 0; i < getImage(imageIndex).getChildNodes().getLength(); i++) {
			Element layer = getLayer(imageIndex, i);
			for (int s = 0; s < layer.getChildNodes().getLength(); s++) {
				Element shape = getShape(imageIndex, i, s);
				string = shape.getTagName();
			}
		}
		return string;
	}

	public void initWithDocument(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			try {
				this.filePath = filePath;
		    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				XML = stripTextNodes(dBuilder.parse(file));
				project = XML.getDocumentElement();
				images = (Element) project.getFirstChild();
				bitmaps = (Element) project.getLastChild();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void openDocument() {
		JFileChooser chooser = new JFileChooser();
	    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "eXtensible Markup Language (.xml)", "xml"));
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.setDialogTitle("Open Image");
	    int returnVal = chooser.showOpenDialog(Application.getInstance().getWindow());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				filePath = chooser.getSelectedFile().getAbsolutePath();
		    	File file = new File(filePath);
		    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				XML = stripTextNodes(dBuilder.parse(file));
				project = XML.getDocumentElement();
				images = (Element) project.getFirstChild();
				bitmaps = (Element) project.getLastChild();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
	    } 
	}	
	public void saveDocument() {
		if (!filePath.equals("")) {
			try {
    			Transformer transformer = TransformerFactory.newInstance().newTransformer();
				DOMSource source = new DOMSource(XML);
				StreamResult result = new StreamResult(new File(filePath));
				transformer.transform(source, result);
				Tools.CHANGED = false;
    		} catch (TransformerFactoryConfigurationError | TransformerException e) {
    			e.printStackTrace();
    		}
		} else {
			saveDocumentAs();
		}
	}
	public void saveDocumentAs() {
		JFileChooser chooser = new JFileChooser();
	    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "eXtensible Markup Language (.xml)", "xml"));
	    chooser.addChoosableFileFilter(new FileNameExtensionFilter( "PNG Image File (.png)", "png"));
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.setApproveButtonText("Save");
	    chooser.setDialogTitle("Save Image");
	    int returnVal = chooser.showOpenDialog(Application.getInstance().getWindow());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	filePath = chooser.getSelectedFile().getAbsolutePath();
	    	if (!chooser.getSelectedFile().getAbsolutePath().endsWith(".png") || !chooser.getSelectedFile().getAbsolutePath().endsWith(".xml")) {
	    		FileFilter chosenFilter = chooser.getFileFilter();
	    		filePath = chooser.getSelectedFile().getAbsolutePath() + "." + ((FileNameExtensionFilter) chosenFilter).getExtensions()[0];
	    	}
	    	if (filePath.endsWith(".png")) {
	    		Functions.routeFunction("Clear Grid");
	    		Drawing drawing = Application.getInstance().getDrawing();
	    		BufferedImage im = new BufferedImage(drawing.getWidth(), drawing.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    		drawing.paint(im.getGraphics());
	    		try {
	    			ImageIO.write(im, "PNG", new File(filePath));
	    		} catch (IOException e) {
	    			System.out.println("Couldn't write to file");
	    		}
	    	} else {
	    		saveDocument();
	    	}
	    }
	}
	public void debugDocument() {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(XML);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			System.out.println(xmlString);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		} 	
	}
	public String getXMLText() {
		String xmlString = "";
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(XML);
			transformer.transform(source, result);
			xmlString = result.getWriter().toString();
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		} 	
		return xmlString.toString();
	}
	
	public Document stripTextNodes(Document document) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
			NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < emptyTextNodes.getLength(); i++) {
				Node emptyTextNode = emptyTextNodes.item(i);
				emptyTextNode.getParentNode().removeChild(emptyTextNode);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return document;
	}
	
	public int getImageIndex(String title) {
		int index = -1;
		for (int i = 0; i < images.getChildNodes().getLength(); i++) {
			Element element = getImage(i);
			if (element.getAttribute("title").equals(title)) {
				index = i;
			}
		}
		return index;
	}
	
	public int getElementCount() {
		int count = 0;
		for (int i = 0; i < getImages().getChildNodes().getLength(); i++) {
			for (int l = 0; l < getImage(i).getChildNodes().getLength(); l++) {
				for (int s = 0; s < getLayer(i,l).getChildNodes().getLength(); s++) {
					count++;
				}
				count++;
			}
			count++;
		}
		return count;
	}
	
	public void deselectAll() {
		for (int i = 0; i < getImages().getChildNodes().getLength(); i++) {
			for (int l = 0; l < getImage(i).getChildNodes().getLength(); l++) {
				for (int s = 0; s < getLayer(i,l).getChildNodes().getLength(); s++) {
					Element shape = getShape(i,l,s);
					shape.setAttribute("selected", Parser.createBoolean(false));
				}
			}
		}
	}
	
	public boolean contains(Element shape, Point point) {
		if (shape.getTagName().equals("Pencil")) return false;
		boolean contains = false;
		Tools tools  = Application.getInstance().getCanvas().getTools();
		Shape testShape = null;
		int width = 0, height = 0;
		if (shape.getTagName().equals("Line")) {
			Point start = Parser.parsePoint(shape.getAttribute("start"));
			Point end = Parser.parsePoint(shape.getAttribute("end"));
			start.x = (int) (start.x * tools.getZoom()); start.y = (int) (start.y * tools.getZoom());
			end.x = (int) (end.x * tools.getZoom()); end.y = (int) (end.y * tools.getZoom());
			testShape = new Line2D.Double(start, end);
		} else {
			Point location = Parser.parsePoint(shape.getAttribute("location"));
			location.x = (int) (location.x * tools.getZoom()); location.y = (int) (location.y * tools.getZoom());
			width = (int) (Double.parseDouble(shape.getAttribute("width")) * tools.getZoom()); height = (int) (Double.parseDouble(shape.getAttribute("height")) * tools.getZoom());
			if (shape.getTagName().equals("Rectangle") || shape.getTagName().equals("Text") || shape.getTagName().equals("Bitmap")) testShape = new Rectangle2D.Double(location.x, location.y, width, height);
			else if (shape.getTagName().equals("Oval")) testShape = new Ellipse2D.Double(location.x, location.y, width, height);
			else if (shape.getTagName().equals("Triangle")) testShape = tools.createTriangle(location, new Point(location.x + width, location.y + height));
		}	
		if (testShape != null) {
			if (testShape instanceof Line2D) {
				if (testShape.intersects(point.x - 2, point.y - 2, 4, 4)) {
					contains = true;
				}
			} else {
				int rotation = Integer.parseInt(shape.getAttribute("rotation"));
				AffineTransform transform = new AffineTransform();
		        transform.rotate(Math.toRadians(rotation), testShape.getBounds().x + (width / 2), testShape.getBounds().y + (height / 2));
		        Shape rotatedShape = transform.createTransformedShape(testShape);
				if (rotatedShape.contains(point)) contains = true; 
			}
		}
		return contains;
	}
	
	public Element getTopSelectedShape(Point point) {
		Element top = null;
		Tools tools = Application.getInstance().getCanvas().getTools();
		int imageIndex = tools.getImageIndex();
		for (int l = 0; l < getImage(imageIndex).getChildNodes().getLength(); l++) {
			for (int s = 0; s < getLayer(imageIndex, l).getChildNodes().getLength(); s++) {
				Element shape = getShape(imageIndex, l, s);
				if (Parser.parseBoolean(shape.getAttribute("selected"))) {
					if (contains(shape, point)) {
						top = shape;
					}
				}
			}	
		}
		return top;
	}
	
	public void removePencil(Rectangle rect, int imageIndex, int layerIndex) {
		for (int i = 0; i < getLayer(imageIndex, layerIndex).getChildNodes().getLength(); i++) {
			if (getShape(imageIndex, layerIndex, i).getTagName().equals("Pencil")) {
				Element pencilArray = getShape(imageIndex, layerIndex, i);
				for (int p = 0; p < pencilArray.getChildNodes().getLength(); p++) {
					Element pencil = (Element) pencilArray.getChildNodes().item(p);
					if (rect.contains(Parser.parsePoint(pencil.getAttribute("location")))) {
						pencilArray.removeChild(pencil);
					}
				}
			}
		}
		for (int i = 0; i < getLayer(imageIndex, layerIndex).getChildNodes().getLength(); i++) {
			if (getShape(imageIndex, layerIndex, i).getTagName().equals("Pencil")) {
				Element pencilArray = getShape(imageIndex, layerIndex, i);
				if (pencilArray.getChildNodes().getLength() == 0) {
					getLayer(imageIndex, layerIndex).removeChild(pencilArray);
				}
			}
		}
	}
	
	public String getImageTitle(int imageIndex) {
		Element image = getImage(imageIndex);
		return image.getAttribute("title");
	}
	
	public Dimension getImageSize(int imageIndex) {
		int width = Integer.parseInt(getImage(imageIndex).getAttribute("width"));
		int height = Integer.parseInt(getImage(imageIndex).getAttribute("height"));
		return new Dimension(width, height);
	}
	
	public void removeImageByTitle(String title) {
		for (int i = 0; i < images.getChildNodes().getLength(); i++) {
			Element image = getImage(i);
			if (image.getAttribute("title").equals(title)) {
				System.out.println("STORAGE - Removing Image With Title " + title);
				images.removeChild(image);
			}
		}
	}
	
	public void removeLayerByTitle(String imageTitle, String layerTitle) {
		for (int i = 0; i < getImage(getImageIndex(imageTitle)).getChildNodes().getLength(); i++) {
			Element layer = (Element) getImage(getImageIndex(imageTitle)).getChildNodes().item(i);
			if (layer.getAttribute("title").equals(layerTitle)) {
				if (Storage.DEBUG) System.out.println("STORAGE - Removing Image With Title " + getImage(getImageIndex(imageTitle)).getTagName());
				getImage(getImageIndex(imageTitle)).removeChild(layer);
			}
		}
	}
	
	public void removeImageByIndex(int imageIndex) {
		if (Storage.DEBUG) System.out.println("STORAGE - Removing Image With Index " + imageIndex);
		images.removeChild(images.getChildNodes().item(imageIndex));
	}
	
	public void removeLayerByIndex(int imageIndex, int layerIndex) {
		if (Storage.DEBUG) System.out.println("STORAGE - Removing Layer Index " + layerIndex + " Of Image With Index " + imageIndex);
		Element layer = getLayer(imageIndex, layerIndex);
		images.getChildNodes().item(imageIndex).removeChild(layer);
	}
	
	public void changeFontSize(Element text, int newSize) {
		Font f = Parser.parseFont(text.getAttribute("font"));
		text.setAttribute("font", Parser.createFont(new Font(f.getFontName(), f.getStyle(), newSize)));
	}
	
	public void copy(boolean cut) {
		if (Application.getInstance().getCanvas() != null) {
			copyList.clear();
			Tools tools = Application.getInstance().getCanvas().getTools();
			for (int l = 0; l < getImage(tools.getImageIndex()).getChildNodes().getLength(); l++) {
				Element layer = getLayer(tools.getImageIndex(), l);
				for (int s = 0; s < layer.getChildNodes().getLength(); s++) {
					Element shape = getShape(tools.getImageIndex(), l, s);
					if (Parser.parseBoolean(shape.getAttribute("selected"))) {
						copyList.add(shape);
						if (cut) layer.removeChild(shape);
					}
				}
			}
		}
	}
	
	public void paste() {
		if (Application.getInstance().getCanvas() != null) {
			Tools tools = Application.getInstance().getCanvas().getTools();
			for (int i = 0; i < copyList.size(); i++) {
				getLayer(tools.getImageIndex(), tools.getLayerIndex()).appendChild(cloneShape(copyList.get(i)));
			}
			copyList.clear();
		}
	}
	
	public void undo() {
		if (Application.getInstance().getCanvas() != null) {
			Tools tools = Application.getInstance().getCanvas().getTools();
			if (getLayer(tools.getImageIndex(), tools.getLayerIndex()).getChildNodes().getLength() < 1) return;
			Element shape = getShape(tools.getImageIndex(), tools.getLayerIndex(), getLayer(tools.getImageIndex(), tools.getLayerIndex()).getChildNodes().getLength() - 1);
			getLayer(tools.getImageIndex(), tools.getLayerIndex()).removeChild(shape);
			redoList.get(tools.getImageIndex()).add(shape);
		}
	}
	
	public void redo() {
		if (Application.getInstance().getCanvas() != null) {
			Tools tools = Application.getInstance().getCanvas().getTools();
			ArrayList<Element> redo = redoList.get(tools.getImageIndex());
			getLayer(tools.getImageIndex(), tools.getLayerIndex()).appendChild(redo.get(redo.size() - 1));
			redo.remove(redo.size() - 1);
		}
	}
	
	public void layerUp() {
		if (Application.getInstance().getCanvas() != null) {
			Tools tools = Application.getInstance().getCanvas().getTools();
			if (getLayer(tools.getImageIndex(), tools.getLayerIndex() + 1) != null) {
				Element target = getLayer(tools.getImageIndex(), tools.getLayerIndex() + 1);
				Element layer = getLayer(tools.getImageIndex(), tools.getLayerIndex());
				getImage(tools.getImageIndex()).removeChild(target);
				getImage(tools.getImageIndex()).insertBefore(target, layer);
			}
		}
	}
	
	public void layerDown() {
		if (Application.getInstance().getCanvas() != null) {
			Tools tools = Application.getInstance().getCanvas().getTools();
			if (getLayer(tools.getImageIndex(), tools.getLayerIndex() - 1) != null) {
				Element target = getLayer(tools.getImageIndex(), tools.getLayerIndex() - 1);
				Element layer = getLayer(tools.getImageIndex(), tools.getLayerIndex());
				getImage(tools.getImageIndex()).removeChild(layer);
				getImage(tools.getImageIndex()).insertBefore(layer, target);
			}
		}
	}
	
	public void deleteSelected() {
		if (Application.getInstance().getCanvas() != null) {
			Tools tools = Application.getInstance().getCanvas().getTools();
			for (int l = 0; l < getImage(tools.getImageIndex()).getChildNodes().getLength(); l++) {
				Element layer = getLayer(tools.getImageIndex(), l);
				for (int s = 0; s < layer.getChildNodes().getLength(); s++) {
					Element shape = getShape(tools.getImageIndex(), l, s);
					if (Parser.parseBoolean(shape.getAttribute("selected"))) {
						if (shape.getTagName().equals("Bitmap")) {
							if (bitmapCheck(shape.getAttribute("path"))) {
								Storage.getInstance().getBitmaps().removeChild(Storage.getInstance().getBitmap(shape.getAttribute("path")));
							}
						}
						layer.removeChild(shape);
					}
				}
			}
		}
	}
	
	public Boolean bitmapCheck(String path) {
		Boolean usedOnce = false;
		int occurance = 0;
		if (Application.getInstance().getCanvas() != null) {
			for (int i = 0; i < getImages().getChildNodes().getLength(); i++) {
				for (int l = 0; l < getImage(i).getChildNodes().getLength(); l++) {
					Element layer = getLayer(i, l);
					for (int s = 0; s < layer.getChildNodes().getLength(); s++) {
						Element shape = getShape(i, l, s);
						if (shape.getTagName().equals("Bitmap")) {
							if (shape.getAttribute("path").equals(path)) occurance++;
						}
					}
				}
			}	
		}
		if (occurance < 2) usedOnce = true;
		return usedOnce;
	}
	
	private Element cloneShape(Element shape) {
		Element newShape = XML.createElement(shape.getTagName());
		NamedNodeMap attributes = shape.getAttributes();
	    for (int i = 0; i < attributes.getLength(); i++) {
	        Attr node = (Attr) attributes.item(i);
	        newShape.setAttribute(node.getName(), node.getValue());
	    }
	    return newShape;
	}
	
	public static class Parser {
		public static boolean DEBUG = false;
		public static String createBoolean(boolean bool) {
			if (Parser.DEBUG) System.out.println("STORAGE - Creating Boolean");
			if (bool) return "true";
			else return "false";
		}
		public static Boolean parseBoolean(String bool) {
			if (Parser.DEBUG) System.out.println("STORAGE - Parsing Boolean");
			if (bool.equals("true")) return true;
			else return false;
		}
		public static String createBitmap(BufferedImage image) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "png", baos);
				baos.flush();
				String encodedImage = DatatypeConverter.printBase64Binary(baos.toByteArray());
				baos.close(); 
				return encodedImage;
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return "error";
		}
		public static BufferedImage parseBitmap(String image) {
			byte[] imageData = DatatypeConverter.parseBase64Binary(image);
			try {
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
				return img;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		public static String createPoint(Point point) { 
			if (Parser.DEBUG) System.out.println("STORAGE - Creating Point");
			return point.x + ";" + point.y; 
		}
		public static Point parsePoint(String point) {
			if (Parser.DEBUG) System.out.println("STORAGE - Parsing Point");
			String result[] = point.split(";");
			return new Point((int)(Double.parseDouble(result[0])), (int)(Double.parseDouble(result[1])));
		}
		public static String createFont(Font font) {
			if (Parser.DEBUG) System.out.println("STORAGE - Parsing Font");
			return font.getFontName() + ";" + font.getStyle() + ";" + font.getSize();
		}
		public static Font parseFont(String font) {
			if (Parser.DEBUG) System.out.println("STORAGE - Creating Font");
			String[] result = font.split(";");
			return new Font(result[0], Integer.parseInt(result[1]), Integer.parseInt(result[2]));
		}
		public static int getFontSize(String font) {
			String[] result = font.split(";");
			return Integer.parseInt(result[2]);
		}
		public static String createColor(Color color) {
			if (Parser.DEBUG) System.out.println("STORAGE - Creating Color");
			return color.getAlpha() + ";" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue(); 
		}
		public static String createColor(GradientPaint gradient) {
			if (Parser.DEBUG) System.out.println("STORAGE - Creating Gradient");
			return "gradient(" + gradient.getPoint1().getX() + ";" + gradient.getPoint1().getY() + ";" + gradient.getColor1().getRed() + ";" + gradient.getColor1().getGreen() + ";" + gradient.getColor1().getBlue() + ";" + gradient.getColor1().getAlpha() + ";" + gradient.getPoint2().getX() + ";" + gradient.getPoint2().getY() + ";" + gradient.getColor2().getRed() + ";" + gradient.getColor2().getGreen() + ";" + gradient.getColor2().getBlue() + ";" + gradient.getColor2().getAlpha() + ")";
		}
		public static Object parseColor(String color) {
			if (color.startsWith("gradient")) {
				return parseGradient(color);
			}
			else {
				if (Parser.DEBUG) System.out.println("STORAGE - Parsing Color");
				String result[] = color.split(";");
				return new Color(Integer.parseInt(result[1]), Integer.parseInt(result[2]), Integer.parseInt(result[3]), Integer.parseInt(result[0]));
			}
	
		}
		public static GradientPaint parseGradient(String color) {
			if (Parser.DEBUG) System.out.println("STORAGE - Parsing Gradient");
			String gradient = color.substring(9, color.length() - 1);
			String[] result = gradient.split(";");
			return new GradientPaint(new Point2D.Double(Double.parseDouble(result[0]), Double.parseDouble(result[1])), new Color(Integer.parseInt(result[2]), Integer.parseInt(result[3]), Integer.parseInt(result[4]), Integer.parseInt(result[5])), new Point2D.Double(Double.parseDouble(result[6]), Double.parseDouble(result[7])), new Color(Integer.parseInt(result[8]), Integer.parseInt(result[9]), Integer.parseInt(result[10]), Integer.parseInt(result[11])));
		}
	}
	
	public static class CustomCellEditor extends DefaultTreeCellEditor {

		public CustomCellEditor(JTree tree, DefaultTreeCellRenderer cellRenderer) {
			super(tree, cellRenderer);
			tree.getModel();
		}
		
	}
	
	public static class ImageFactory {
		public static boolean DEBUG = false;
		public static void createImage(String title, int width, int height, Color background) {
			Element image = Storage.getInstance().getDocument().createElement("Image");
			image.setAttribute("title", title);
			image.setAttribute("width", "" + width);
			image.setAttribute("height", "" + height);
			image.setAttribute("background", Parser.createColor(background));
			Element layer = Storage.getInstance().getDocument().createElement("Layer");
			layer.setAttribute("title", "Background");
			layer.setAttribute("visible", Parser.createBoolean(true));
			image.appendChild(layer);
			Storage.getInstance().getImages().appendChild(image);
			if (ImageFactory.DEBUG) System.out.println("STORAGE - Creating Image");
		}
		public static void createLayer(int imageIndex, String title, boolean visible) {
			Element layer = Storage.getInstance().getDocument().createElement("Layer");
			layer.setAttribute("title", title);
			layer.setAttribute("visible", Parser.createBoolean(visible));
			Storage.getInstance().getImage(imageIndex).appendChild(layer);
			if (ImageFactory.DEBUG) System.out.println("STORAGE - Adding Layer To Image Index: " + imageIndex);
		}
	}

}
