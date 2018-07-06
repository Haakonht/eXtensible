package view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import controller.Canvas;
import controller.Functions;
import model.Resource.ImageImporter;
import model.Settings.SerializedSettings.ToolData;
import model.Storage;
import model.Storage.CustomCellEditor;
import model.Storage.Parser;
import model.Tools.Tool;
import view.Components.Ruler.RulerScroll;
import view.Dialogs.ColorDialog;
import view.Dialogs.LayerDialog;

public class Components {
	
	public static boolean DEBUG = false;
	
	public static void setFixedSize(Component component, Dimension dimension) {
		component.setMinimumSize(dimension);
		component.setPreferredSize(dimension);
		component.setMaximumSize(dimension);
	}
	
	public static Object transparencyCheck(Object object) {
		if (object instanceof Color) {
			Color color = (Color) object;
			if (color.getAlpha() == 0) {
				return Color.LIGHT_GRAY;
			}
			return (Color) object;
		} else {
			GradientPaint gradient = (GradientPaint) object;
			if (gradient.getColor1().getAlpha() == 0 || gradient.getColor2().getAlpha() == 0) {
				return new GradientPaint(gradient.getPoint1(), Color.LIGHT_GRAY, gradient.getPoint2(), Color.LIGHT_GRAY);
			}
			return (GradientPaint) object;
		}
	}
	
	public static Dimension getScaledDimension(Dimension imageSize, Dimension boundary) {
	    double widthRatio = boundary.getWidth() / imageSize.getWidth();
	    double heightRatio = boundary.getHeight() / imageSize.getHeight();
	    double ratio = Math.min(widthRatio, heightRatio);

	    return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
	}
	
	public static Color getContrastColor(Color color) {
		if (color.equals(Color.LIGHT_GRAY)) {
			return Color.RED;
		} else {
			double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
			return y >= 128 ? Color.black : Color.white;
		}
	}
	
	public static class Container extends JInternalFrame {
		public Container(String title, int width, int height, Component component) {
			setTitle(title);
			setBorder(null);
			setFrameIcon(null);
			setIconifiable(false);
			setMaximizable(false);
			add(component);
			setVisible(true);
			pack();
		}
		
		public Container(String title, int width, int height) {
			setTitle(title);
			setPreferredSize(new Dimension(width, height));
			setBorder(null);
			setFrameIcon(null);
			setIconifiable(false);
			setMaximizable(false);
			
			BasicInternalFrameUI ui = (BasicInternalFrameUI) getUI();
			Component northPane = ui.getNorthPane();
			MouseMotionListener[] motionListeners = (MouseMotionListener[]) northPane.getListeners(MouseMotionListener.class);

			for (MouseMotionListener listener: motionListeners) {
				northPane.removeMouseMotionListener(listener);
			}
			
			if (title.equals("Canvas") || title.equals("Status") || title.equals("Application Menu")) setClosable(true);
			setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
			setVisible(true);
		}
	}
	
	public static class Window extends JFrame {
		private KeyboardFocusManager manager;
		public JDesktopPane desktop;
		public Window(String title, int width, int height) {
			setTitle(title);
			setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/icon.jpg")));
			setSize(width, height);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					Functions.routeFunction("Exit");
				}
			});
			manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			manager.addKeyEventDispatcher(new KeyDispatcher());
			desktop = new JDesktopPane();
			desktop.setBorder(new LineBorder(Color.DARK_GRAY, 10));
			BorderLayout layout = new BorderLayout();
			layout.setHgap(5); layout.setVgap(5);
			desktop.setLayout(layout);
			desktop.setBackground(Color.DARK_GRAY);
			add(desktop, BorderLayout.CENTER);
			setVisible(true);
		}
		private class KeyDispatcher implements KeyEventDispatcher {
		    public boolean dispatchKeyEvent(KeyEvent e) {
		    	if(e.getID() == KeyEvent.KEY_PRESSED) {
		    		if ((e.getKeyChar() == KeyEvent.VK_1) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    		if (AppMenu.menuCommand == "File") AppMenu.menuCommand = "";
			    		else AppMenu.menuCommand = "File";
			    		Application.getInstance().toggleMenu(AppMenu.menuCommand);
			    	} else if ((e.getKeyCode() == KeyEvent.VK_N) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
		    			Functions.routeFunction("New Image");
		    		} else if ((e.getKeyCode() == KeyEvent.VK_O) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
		    			Functions.routeFunction("Open Image");
		    		} else if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
		    			Functions.routeFunction("Save Image");
		    		} else if ((e.getKeyCode() == KeyEvent.VK_F4) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
		    			Functions.routeFunction("Exit");
		    		} 
		    		if (Application.getInstance().getCanvas() != null) {
			    		if ((e.getKeyCode() == KeyEvent.VK_2) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			if (AppMenu.menuCommand == "Edit") AppMenu.menuCommand = "";
			    			else AppMenu.menuCommand = "Edit";
			    			Application.getInstance().toggleMenu(AppMenu.menuCommand);
			    		} else if ((e.getKeyCode() == KeyEvent.VK_3) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			if (AppMenu.menuCommand == "Tools") AppMenu.menuCommand = "";
			    			else AppMenu.menuCommand = "Tools";
			    			Application.getInstance().toggleMenu(AppMenu.menuCommand);
			    		} else if ((e.getKeyCode() == KeyEvent.VK_4) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			if (AppMenu.menuCommand == "View") AppMenu.menuCommand = "";
			    			else AppMenu.menuCommand = "View";
			    			Application.getInstance().toggleMenu(AppMenu.menuCommand);
			    		} else if ((e.getKeyChar() == KeyEvent.VK_1) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
			    			if (AppMenu.projectCommand == "XML") AppMenu.projectCommand = "";
			    			else AppMenu.projectCommand = "XML";
			    			Application.getInstance().toggleProject(AppMenu.projectCommand);
			    		} else if ((e.getKeyCode() == KeyEvent.VK_2) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
			    			if (AppMenu.projectCommand == "Images") AppMenu.projectCommand = "";
			    			else AppMenu.projectCommand = "Images";
			    			Application.getInstance().toggleProject(AppMenu.projectCommand);
			    		} else if ((e.getKeyCode() == KeyEvent.VK_3) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
			    			if (AppMenu.projectCommand == "Layers") AppMenu.projectCommand = "";
			    			else AppMenu.projectCommand = "Layers";
			    			Application.getInstance().toggleProject(AppMenu.projectCommand);
			    		} else if ((e.getKeyCode() == KeyEvent.VK_4) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
			    			if (AppMenu.projectCommand == "Shapes") AppMenu.projectCommand = "";
			    			else AppMenu.projectCommand = "Shapes";
			    			Application.getInstance().toggleProject(AppMenu.projectCommand);
			    		} else if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			Functions.routeFunction("Undo");
			    		} else if ((e.getKeyCode() == KeyEvent.VK_Y) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			Functions.routeFunction("Redo");
			    		} else if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			Functions.routeFunction("Copy");
			    		} else if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			Functions.routeFunction("Paste");
			    		} else if ((e.getKeyCode() == KeyEvent.VK_X) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			Functions.routeFunction("Cut");
			    		} else if ((e.getKeyCode() == KeyEvent.VK_L) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			    			Functions.routeFunction("Add Layer");
			    		} else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && Application.getInstance().getCanvas().getTools().getTool() != Tool.TEXT) {
			    			Functions.routeFunction("Deselect All");
			    		}
			    	}
		    	}
		    	return false;
		    }
		}
	}

	public static class AppMenu extends JPanel {
		private boolean enabled = true;
		public static String menuCommand = "";
		public static String projectCommand = "";
		private JPanel menu, project;
		private String[] menuButtons = { "File", "Edit", "Tools", "View"};
		private String[] projectButtons = {"XML", "Images", "Layers", "Shapes"};
		public AppMenu() {
			GridLayout layout = new GridLayout(1,0);
			setLayout(layout);
			setBorder(new EmptyBorder(5,5,5,5));
			menu = new JPanel();
			menu.setLayout(layout);
			int i = 1;
			for (String string : menuButtons) {
				JButton button = new JButton("<html>" + string + "<br/><font size=-3> (Ctrl + " + i + ")</font></html>");
				button.addActionListener(e -> {
					if (menuCommand.equals(string)) menuCommand = "";
					else menuCommand = string;
					Application.getInstance().toggleMenu(menuCommand);
				});
				addButtonIcon(button, string, 25);
				menu.add(button);
				i++;
			}
			add(menu);
			add(new JPanel());
			project = new JPanel();
			project.setLayout(layout);
			project.add(new JLabel());
			i = 1;
			for (String string : projectButtons) {
				JButton button = new JButton("<html>" + string + "<br/><font size=-3> (Alt + " + i + ")</font></html>");
				button.addActionListener(e -> {
					if (projectCommand.equals(string)) projectCommand = "";
					else projectCommand = string;
					Application.getInstance().toggleProject(projectCommand);
				});
				addButtonIcon(button, string, 25);
				project.add(button);
				i++;
			}
			add(project);
		}
		public void enableCheck(Canvas canvas) {
			if (canvas == null && enabled) {
				for (int i = 1; i < menu.getComponentCount(); i++) {
					JButton button = (JButton) menu.getComponent(i);
					button.setEnabled(false);
				}
				for (int i = 1; i < project.getComponentCount(); i++) {
					JButton button = (JButton) project.getComponent(i);
					button.setEnabled(false);	
				}
				enabled = false;
			} else if (canvas != null && !enabled) {
				for (int i = 1; i < menu.getComponentCount(); i++) {
					JButton button = (JButton) menu.getComponent(i);
					button.setEnabled(true);
				}
				for (int i = 1; i < project.getComponentCount(); i++) {
					JButton button = (JButton) project.getComponent(i);
					button.setEnabled(true);
				}
				enabled = true;
			}
		}
		private void addButtonIcon(JButton btn, String fileName, int size) {
			try {
				Image img = ImageIO.read(getClass().getResource("/icons/" + fileName + ".png"));
			    Image icon = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
				btn.setIcon(new ImageIcon(icon));
			} catch (Exception ex) {
			    System.out.println(ex);
			}
		}
	}
	
	public static class ToolMenu extends JPanel {
		private String[] fileButtons = {"New Image", "PLACEHOLDER", "Open Image", "Save Image", "Save Image As", "PLACEHOLDER", "Print", "PLACEHOLDER", "PLACEHOLDER", "Exit"};
		private String[] fileHotkeys = {"Ctrl+N", "", "Ctrl+O", "Ctrl+S", "Ctrl+Shift+S", "", "Ctrl+P", "", "", "Alt+F4" };
		private String[] editButtons = {"Undo", "Redo", "PLACEHOLDER", "Cut", "Copy", "Paste", "PLACEHOLDER", "RESIZES", "PLACEHOLDER", "ZOOM_CONTROLS"};
		private String[] toolsButtons = {"Selector", "PENCIL", "Line", "Oval", "Triangle", "Rectangle", "Bitmap", "TEXT_TOOL", "WEIGHT_SLIDER", "COLOR_BUTTONS"};
		private String[] viewButtons = {"Application Menu", "Canvas", "Status", "GRID", "PLACEHOLDER", "PLACEHOLDER", "PLACEHOLDER", "PLACEHOLDER", "Preferences", "About"};
		public ToolMenu(String command) {
			setBorder(new EmptyBorder(5,5,5,5));
			setLayout(new GridLayout(0,1));
			if (command.equals("File")) {
				generateMenu(fillPlaceholders(fileButtons), command);
			} else if (command.equals("Edit")) {
				generateMenu(fillPlaceholders(editButtons), command);
			} else if (command.equals("Tools")) {
				generateMenu(toolsButtons, command);
			} else if (command.equals("View")) {
				generateMenu(fillPlaceholders(viewButtons), command);
			}
		}
		private void generateMenu(String[] array, String command) {
			for (String string : array) {
				if (string.equals("PENCIL")) {
					JPanel panel = new JPanel();
					panel.setLayout(new GridLayout(1,0));
					JButton pencil = new JButton("Pencil");
					JButton eraser = new JButton("Eraser");
					pencil.addActionListener(e -> {
						Functions.routeFunction(pencil.getText());
					});
					eraser.addActionListener(e -> {
						Functions.routeFunction(eraser.getText());
					});
					panel.add(pencil);
					panel.add(eraser);
					add(panel);
				} else if (string.equals("RESIZES")) {
					JPanel panel = new JPanel();
					panel.setLayout(new GridLayout(1,0));
					JButton resizeCanvas = new JButton("Resize Canvas");
					JButton resizeImage = new JButton("Resize Image");
					resizeCanvas.addActionListener(e -> {
						Functions.routeFunction(resizeCanvas.getText());
					});
					resizeImage.addActionListener(e -> {
						Functions.routeFunction(resizeImage.getText());
					});
					panel.add(resizeCanvas);
					panel.add(resizeImage);
					add(panel);
				} else if (string.equals("ZOOM_CONTROLS")) {
					JPanel zoomPanel = new JPanel();
					zoomPanel.setLayout(new BorderLayout());
					JButton out = new JButton("Zoom -");
					out.addActionListener(e -> {
						Functions.routeFunction("Zoom -");
					});
					JButton resetZoom = new JButton("Zoom (" + Application.getInstance().getCanvas().getTools().getZoom() + ")");
					resetZoom.addActionListener(e -> {
						Functions.routeFunction("Reset Zoom");
					});
					JButton in = new JButton("Zoom +");
					in.addActionListener(e -> {
						Functions.routeFunction("Zoom +");
					});
					zoomPanel.add(out, BorderLayout.WEST);
					zoomPanel.add(resetZoom, BorderLayout.CENTER);
					zoomPanel.add(in, BorderLayout.EAST);
					add(zoomPanel);
				} else if (string.equals("GRID")) {
					JPanel gridPanel = new JPanel();
					gridPanel.setLayout(new GridLayout(0,2));
					JButton grid = new JButton("Grid");
					grid.addActionListener(e -> {
						Functions.routeFunction(grid.getText());
					});
					JButton clearGrid = new JButton("Clear Grid");
					clearGrid.addActionListener(e -> {
						Functions.routeFunction(clearGrid.getText());
					});
					gridPanel.add(grid);
					gridPanel.add(clearGrid);
					add(gridPanel);
				} else if (string.equals("WEIGHT_SLIDER")) {
					JPanel sliderPanel = new JPanel();
					sliderPanel.setBorder(new EmptyBorder(8,0,0,0));
					sliderPanel.setLayout(new BorderLayout());
					JLabel label = new JLabel("Outline Thickness:", JLabel.CENTER);
					int weight = 5;
					if (Application.getInstance().getCanvas() != null) weight = Application.getInstance().getCanvas().getTools().getWeight();
					JSlider slider = new JSlider(1, 20, weight);
					slider.setMinorTickSpacing(1);
					slider.setPaintTicks(true);
					slider.setSnapToTicks(true);
					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
				    table.put(1, new JLabel("1"));
				    table.put(5, new JLabel("5"));
				    table.put(10, new JLabel("10"));
				    table.put(15, new JLabel("15"));
				    table.put(20, new JLabel("20"));
				    slider.setLabelTable(table);
				    slider.setPaintLabels(true);
			        slider.addChangeListener(new ChangeListener() {
			            public void stateChanged(ChangeEvent event) {
			            	Application.getInstance().getCanvas().getTools().setWeight(slider.getValue());
			            }
			        });	
					sliderPanel.add(label, BorderLayout.NORTH);
					sliderPanel.add(slider, BorderLayout.CENTER);
					add(sliderPanel);
				} else if (string.equals("PLACEHOLDER")) {
					JLabel placeholder = new JLabel();
					add(placeholder);
				} else if (string.equals("COLOR_BUTTONS")) {
					JPanel colorPanel = new JPanel();
					colorPanel.setLayout(new GridLayout(1,0));			
					ColorButton outline = new ColorButton("Outline");
					if (Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()) instanceof Color) outline.setBackground((Color) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()));
					else outline.setBackground((GradientPaint) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()));
					outline.addActionListener(e -> {
						Functions.routeFunction("Set Outline");
					});
					ColorButton fill = new ColorButton("Fill");
					if (Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getFill()) instanceof Color) fill.setBackground((Color) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getFill()));
					else fill.setBackground((GradientPaint) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getFill()));
					fill.addActionListener(e -> {
						Functions.routeFunction("Set Fill");
					});
					ColorButton background = new ColorButton("Background");
					if (Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getBackground()) instanceof Color) background.setBackground((Color) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getBackground()));
					else background.setBackground((GradientPaint) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getBackground()));
					background.addActionListener(e -> {
						Functions.routeFunction("Set Background");
					});
					colorPanel.add(outline);
					colorPanel.add(fill);
					colorPanel.add(background);
					add(colorPanel);
				} else if (string.equals("TEXT_TOOL")) {
					JPanel textPanel = new JPanel();
					textPanel.setLayout(new GridLayout(1,0));
					JButton button = new JButton("Text");
					button.addActionListener(e -> {
						Functions.routeFunction("Text");
					});
					JButton font = new JButton("Set Font");
					font.addActionListener(e -> {
						Functions.routeFunction("Set Font");
					});
					textPanel.add(button);
					textPanel.add(font);
					add(textPanel);
				} else {
					JButton button = new JButton(string);
					button.setToolTipText("Hotkey " + string);
					if (string.equals("Undo")) {
						if (Application.getInstance().getCanvas() != null) {
							int imageIndex = Application.getInstance().getTabIndex();
							String lastShape = Storage.getInstance().getLastAddedShapeName(imageIndex);
							button.setText("Undo " + lastShape);
							if (lastShape.equals("")) button.setEnabled(false);
						}
					}
					if (string.equals("Redo")) { 
						if (Application.getInstance().getCanvas() != null) {
							button.setEnabled(!Storage.getInstance().isRedoEmpty(Application.getInstance().getCanvas().getTools().getImageIndex())); 
							if (Storage.getInstance().getRedo(Application.getInstance().getCanvas().getTools().getImageIndex()) != null) {
								button.setText("Redo " + Storage.getInstance().getRedo(Application.getInstance().getCanvas().getTools().getImageIndex()).getTagName());
							}
						}
					}
					if (string.equals("Paste")) { button.setEnabled(!Storage.getInstance().isCopyEmpty()); }
					if (string.equals("Save Image") || string.equals("Save Image As") || string.equals("Print")) { if (Application.getInstance().getTabCount() == 0) button.setEnabled(false); }
					button.addActionListener(e -> {
						Functions.routeFunction(string);
					});
					add(button);
				}
			}
		}
		private String[] fillPlaceholders(String[] buttons) {
			String[] array = new String[toolsButtons.length];
			for (int i = 0; i < buttons.length; i++) {
				array[i] = buttons[i];
			}
			for (int i = buttons.length; i < toolsButtons.length; i++) {
				array[i] = "PLACEHOLDER";
			}
			return array;
		}
	}
	
	public static class ProjectMenu extends JPanel {
		public static TreePath lastPath;
		private String[] layerButtons = {"Add Layer", "Layer Up", "Remove Layer", "Layer Down"};
		private String[] shapeButtons = {"Delete Selected"};
		public ProjectMenu(String command) {
			setLayout(new BorderLayout());
			if (command.equals("XML")) {
				JScrollPane jsp = new JScrollPane(createXMLPage());
				jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				add(createXMLButtons(command), BorderLayout.NORTH);
				add(jsp, BorderLayout.CENTER);
			} else if (command.equals("Source")) { 
				JScrollPane jsp = new JScrollPane(createTextArea());
				jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				add(createXMLButtons(command), BorderLayout.NORTH);
				add(jsp, BorderLayout.CENTER);
			} else if (command.equals("Images")) {
				JScrollPane jsp = new JScrollPane(createImagePage());
				jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				add(jsp, BorderLayout.CENTER);
			} else if (command.equals("Layers")) {
				JScrollPane jsp = new JScrollPane(createLayerPage());
				jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				add(createLayerButtons(layerButtons), BorderLayout.NORTH);
				add(jsp, BorderLayout.CENTER);
			} else if (command.equals("Shapes")) {
				JScrollPane jsp = new JScrollPane(createShapePage());
				jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				add(createShapeButtons(shapeButtons), BorderLayout.NORTH);
				add(jsp, BorderLayout.CENTER);
			}
		}
		private JPanel createTextArea() {
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());
			JTextArea jta = new JTextArea();
			jta.setEditable(false);
			jta.setText(Storage.getInstance().getXMLText());
			jta.setBackground(Color.DARK_GRAY);
			jta.setForeground(Color.WHITE);
			contentPanel.add(jta);
			return contentPanel;
		}
		private JPanel createXMLPage() {
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new GridLayout());
			int layers = Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getChildNodes().getLength();
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()));
			for (int l = 0; l < Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getChildNodes().getLength(); l++) {
				ArrayList<String> usedBitmapsInLayer = new ArrayList<String>();
				DefaultMutableTreeNode layerNode = new DefaultMutableTreeNode(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getChildNodes().item(l));
				for (int s = 0; s < Storage.getInstance().getLayer(Application.getInstance().getCanvas().getTools().getImageIndex(), l).getChildNodes().getLength(); s++) {
					 DefaultMutableTreeNode shapeNode = new DefaultMutableTreeNode(Storage.getInstance().getShape(Application.getInstance().getCanvas().getTools().getImageIndex(), l, s));
					 Element shape = Storage.getInstance().getShape(Application.getInstance().getCanvas().getTools().getImageIndex(), l, s);
					 NamedNodeMap attributes = shape.getAttributes();
					 int numAttrs = attributes.getLength();
					 for (int i = 0; i < numAttrs; i++) {
			            Attr attr = (Attr) attributes.item(i);
			            if (attr.getNodeName().equals("path")) usedBitmapsInLayer.add(attr.getValue());
			            String[] values = {attr.getNodeName(), attr.getValue()};
			            DefaultMutableTreeNode attrNode = new DefaultMutableTreeNode(values);
			            shapeNode.add(attrNode);
					 }
					 layerNode.add(shapeNode);
				}
				root.add(layerNode);
				if (!usedBitmapsInLayer.isEmpty()) {
					DefaultMutableTreeNode bitmapsNode = new DefaultMutableTreeNode(Storage.getInstance().getBitmaps());
					for (String s : usedBitmapsInLayer) {
						DefaultMutableTreeNode bitmapNode = new DefaultMutableTreeNode(Storage.getInstance().getBitmap(s));
						Element bitmap = Storage.getInstance().getBitmap(s);
						 NamedNodeMap attributes = bitmap.getAttributes();
						 int numAttrs = attributes.getLength();
						 for (int i = 0; i < numAttrs; i++) {
				            Attr attr = (Attr) attributes.item(i); 
				            String[] values = {attr.getNodeName(), attr.getValue()};
				            DefaultMutableTreeNode attrNode = new DefaultMutableTreeNode(values);
				            bitmapNode.add(attrNode);
						 }
						 String[] value = {"Data", bitmap.getTextContent()};
						 DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(value);
						 bitmapNode.add(dataNode);
						 bitmapsNode.add(bitmapNode);
					}
					root.add(bitmapsNode);
				}
			}
			JTree tree = new JTree(root);
			DefaultTreeCellRenderer ccr = new CustomCellRenderer();
			tree.setCellRenderer(ccr);
			tree.setCellEditor(new CustomCellEditor(tree, ccr));
			tree.setOpaque(false);
			if (lastPath != null) tree.expandPath(lastPath);
	        contentPanel.add(tree);
			return contentPanel;
		}
		private JPanel createXMLButtons(String command) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
			buttonPanel.setLayout(new GridLayout(1,0));
			if (command.equals("Source")) {
				JButton button = new JButton("Close XML");
				button.addActionListener(e -> {
					Application.getInstance().reloadProject();
				});
				buttonPanel.add(button);
			} else {
				JButton button = new JButton("View XML");
				button.addActionListener(e -> {
					Application.getInstance().toggleProject("Source");
				});
				buttonPanel.add(button);
			}
			return buttonPanel;
		}
		private JPanel createImagePage() {
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			for (int i = 0; i < Storage.getInstance().getImageCount(); i++) {
				JLabel title = new JLabel(Storage.getInstance().getImageTitle(i), JLabel.CENTER);
				title.setOpaque(true);
				title.setBackground(Color.DARK_GRAY);
				title.setForeground(Color.WHITE);
				title.setFont(title.getFont().deriveFont(13f));
				Components.setFixedSize(title, new Dimension(222, 30));
				Drawing drawing = Application.getInstance().getDrawing(i);
				BufferedImage image = new BufferedImage(drawing.getWidth(), drawing.getHeight(), BufferedImage.TYPE_INT_ARGB);
				drawing.paint(image.getGraphics());
				ImageIcon icon = new ImageIcon((Image)image.getScaledInstance(222, -1, Image.SCALE_DEFAULT));
				JLabel label = new JLabel("Image Preview");
				label.setForeground(Color.WHITE);
				label.setHorizontalTextPosition(JLabel.CENTER);
				label.setIcon(icon);		
				label.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
				JButton button = new JButton("Select");
				if (Application.getInstance().getTabIndex() == i) button.setEnabled(false);
				Components.setFixedSize(button, new Dimension(222, 30));
				int index = i;
				button.addActionListener(e -> {
					Application.getInstance().setTabIndex(index);
				});
				JLabel spacer = new JLabel();
				Components.setFixedSize(spacer, new Dimension(222, 5));
				contentPanel.add(title);
				contentPanel.add(label);
				contentPanel.add(button);
				contentPanel.add(spacer);
			}
			return contentPanel;
		}
		private JPanel createLayerPage() {
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			if (Application.getInstance().getTabCount() > 0) {
				int imageIndex = Application.getInstance().getCanvas().getTools().getImageIndex();
				ButtonGroup btnGrp = new ButtonGroup();
				for (int i = 0; i < Storage.getInstance().getLayerCount(imageIndex); i++) {
					Element layer = Storage.getInstance().getLayer(imageIndex, i); int index = i;
					JPanel layerPanel = new JPanel();
					layerPanel.setBorder(new LineBorder(Color.DARK_GRAY, 1));
					layerPanel.setLayout(new BorderLayout());
					JPanel checkboxPanel = new JPanel();
					checkboxPanel.setBackground(Color.DARK_GRAY);
					checkboxPanel.setLayout(new FlowLayout());
					JLabel checkboxLabel = new JLabel("   Visible:");
					checkboxLabel.setForeground(Color.WHITE);
					JCheckBox checkBox = new JCheckBox();
					checkBox.setBackground(Color.DARK_GRAY);
					checkBox.setSelected(Parser.parseBoolean(layer.getAttribute("visible")));
					checkBox.addActionListener(e -> {
						Storage.getInstance().getLayer(imageIndex, index).setAttribute("visible", Parser.createBoolean(checkBox.isSelected()));
						Application.getInstance().repaintCanvas();
					});
					checkboxPanel.add(checkboxLabel);
					checkboxPanel.add(checkBox);
					JToggleButton toggleButton = new JToggleButton(layer.getAttribute("title"));
					if (index == Application.getInstance().getCanvas().getTools().getLayerIndex()) toggleButton.setSelected(true);
					toggleButton.addActionListener(e -> {
						Application.getInstance().getCanvas().getTools().setLayer(index);
					});
					toggleButton.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) { 
							if (e.getClickCount() == 2) {
								new LayerDialog(imageIndex, index);
							}
				        } 
					});
					btnGrp.add(toggleButton);
					layerPanel.add(checkboxPanel, BorderLayout.EAST);
					layerPanel.add(toggleButton, BorderLayout.CENTER);
					setFixedSize(layerPanel, new Dimension(222, 35));
					contentPanel.add(layerPanel);
					JLabel spacer = new JLabel();
					setFixedSize(spacer, new Dimension(222,5));
					contentPanel.add(spacer);
				}
			}
			return contentPanel;
		}
		private JPanel createLayerButtons(String[] array) {
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new GridLayout(2,2));
			for (int i = 0; i < array.length; i++) {
				String text = array[i];
				JButton button = new JButton(text);
				button.addActionListener(e -> {
					Functions.routeFunction(text);
				});
				contentPanel.add(button);
			}
			return contentPanel;
		}
		private JPanel createShapePage() {
			int imageIndex = Application.getInstance().getCanvas().getTools().getImageIndex();
			int layerIndex = Application.getInstance().getCanvas().getTools().getLayerIndex();
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			for (int i = 0; i < Storage.getInstance().getLayer(imageIndex, layerIndex).getChildNodes().getLength(); i++) {
				Element shape = Storage.getInstance().getShape(imageIndex, layerIndex, i); int index = i;
				JPanel shapePanel = new JPanel();
				shapePanel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (Parser.parseBoolean(Storage.getInstance().getShape(imageIndex, layerIndex, index).getAttribute("selected"))) Storage.getInstance().getShape(imageIndex, layerIndex, index).setAttribute("selected", Parser.createBoolean(false));
						else Storage.getInstance().getShape(imageIndex, layerIndex, index).setAttribute("selected", Parser.createBoolean(true));
						Application.getInstance().reloadProject();
						Application.getInstance().repaintCanvas();
				    }
				});
				if (shape.getTagName().equals("Pencil") || shape.getTagName().equals("Line")) {
					Color color = (Color) Parser.parseColor(shape.getAttribute("color"));
					shapePanel.setBackground(color);
					shapePanel.setBorder(new LineBorder(color, 5));

					JLabel label = new JLabel(shape.getTagName());
					if (shape.getTagName().equals("Line")) {
						if (Parser.parseBoolean(shape.getAttribute("selected"))) shapePanel.setBorder(BorderFactory.createDashedBorder(Color.WHITE, 5, 2, 1, false));
					}
					label.setHorizontalAlignment(JLabel.CENTER);
					label.setBackground(Components.getContrastColor(shapePanel.getBackground()));
					shapePanel.add(label);
				} else if (shape.getTagName().equals("Text")) {
					Color color = (Color) Parser.parseColor(shape.getAttribute("color"));
					JLabel label = new JLabel(shape.getTagName() + " " + shape.getAttribute("text"));
					if (shape.getAttribute("text").length() > 5) label.setText(shape.getTagName() + " = " + shape.getAttribute("text").substring(0,5) + "..");
					label.setHorizontalAlignment(JLabel.CENTER);
					label.setForeground(color);
					shapePanel.setBackground(getContrastColor(color));
					shapePanel.setBorder(new LineBorder(shapePanel.getBackground(), 5));
					if (Parser.parseBoolean(shape.getAttribute("selected"))) shapePanel.setBorder(BorderFactory.createDashedBorder(shapePanel.getForeground(), 5, 2, 1, false));
					shapePanel.add(label);
				} else if (shape.getTagName().equals("Bitmap")) {
					JLabel label = new JLabel(shape.getTagName() + " " + shape.getAttribute("path").substring(0,5));
					label.setHorizontalAlignment(JLabel.CENTER);
					shapePanel.setBorder(new LineBorder(shapePanel.getBackground(), 5));
					if (Parser.parseBoolean(shape.getAttribute("selected"))) shapePanel.setBorder(BorderFactory.createDashedBorder(shapePanel.getForeground(), 5, 2, 1, false));
					shapePanel.add(label);
				} else {
					Color outline = (Color) Parser.parseColor(shape.getAttribute("outline"));
					Color fill = (Color) Parser.parseColor(shape.getAttribute("fill"));
					shapePanel.setBackground(fill);
					shapePanel.setBorder(new LineBorder(outline, 5));
					if (Parser.parseBoolean(shape.getAttribute("selected"))) shapePanel.setBorder(BorderFactory.createDashedBorder(outline, 5, 2, 1, false));
					JLabel label = new JLabel(shape.getTagName());
					label.setHorizontalAlignment(JLabel.CENTER);
					label.setBackground(Components.getContrastColor(shapePanel.getBackground()));
					label.setOpaque(false);
					shapePanel.add(label);
				}
				setFixedSize(shapePanel, new Dimension(222, 35));
				contentPanel.add(shapePanel);
				JLabel spacer = new JLabel();
				setFixedSize(spacer, new Dimension(222, 5));
				contentPanel.add(spacer);
			}
			return contentPanel;
		}
		private JPanel createShapeButtons(String[] array) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
			buttonPanel.setLayout(new GridLayout(1,0));
			for (int i = 0; i < array.length; i++) {
				String text = array[i];
				JButton button = new JButton(text);
				button.addActionListener(e -> {
					Functions.routeFunction(text);
				});
				buttonPanel.add(button);
			}
			return buttonPanel;
		}
	}

	public static class CustomCellRenderer extends DefaultTreeCellRenderer {
		private JPanel renderer;  
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selectedCell, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			  Component returnValue = null;
			  renderer = new JPanel();
			  renderer.setLayout(new GridLayout(0,1,0,5));
			  if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
				  Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				  if (userObject instanceof Element && ((Element) userObject).getTagName().equals("Image")) {
					  Element image = (Element) userObject;
					  JLabel title = new JLabel(image.getAttribute("title"));
					  title.setFont(title.getFont().deriveFont(18f));
					  renderer.add(title);
					  if (selectedCell) {
						  ProjectMenu.lastPath = tree.getSelectionPath();
						  renderer.setBackground(Color.DARK_GRAY);
						  title.setForeground(Color.WHITE);
					  }
					  renderer.setEnabled(true);
					  returnValue = renderer;
		      } else if (userObject instanceof Element && ((Element) userObject).getTagName().equals("Layer")) {
		    	  Element layer = (Element) userObject;
		    	  JLabel title = new JLabel(layer.getAttribute("title"));
		    	  title.setFont(title.getFont().deriveFont(16f));
				  renderer.add(title);
				  if (selectedCell) {
					ProjectMenu.lastPath = tree.getSelectionPath();
				  	renderer.setBackground(Color.DARK_GRAY);
				  	title.setForeground(Color.WHITE);
				  }
		    	  renderer.setEnabled(true);
		    	  returnValue = renderer;
		      } else if (userObject instanceof Element) {
		    	  Element element = (Element) userObject;
		    	  JLabel title = new JLabel(element.getTagName());
		    	  title.setFont(title.getFont().deriveFont(14f));
		    	  renderer.add(title);
		    	  if (selectedCell) {
		    		  ProjectMenu.lastPath = tree.getSelectionPath();
		    		  renderer.setBackground(Color.DARK_GRAY);
		    		  title.setForeground(Color.WHITE);
		    	  }
		    	  renderer.setEnabled(true);
		    	  returnValue = renderer;
		      } else if (userObject instanceof String[]) {
		    	  if (selectedCell) ProjectMenu.lastPath = tree.getSelectionPath();
		    	  String[] data = (String[]) userObject;
		    	  JPanel dataPanel = new JPanel();
		    	  dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.X_AXIS));
		    	  String typeText = data[0].substring(0, 1).toUpperCase() + data[0].substring(1) + ":";
		    	  JLabel type = new JLabel(typeText);
		    	  Components.setFixedSize(type, new Dimension(60, 20));
		    	  dataPanel.add(type);
		    	  if (typeText.equals("Outline:") || typeText.equals("Fill:")) {
		    		  JLabel colorLabel = new JLabel();
		    		  colorLabel.setOpaque(true);
		    		  colorLabel.setBackground((Color)Parser.parseColor(data[1]));
		    		  Components.setFixedSize(colorLabel, new Dimension(100,20));
		    		  dataPanel.add(colorLabel);
		    	  } else if (typeText.equals("Selected:")) { 
		    		  JLabel selectedLabel = new JLabel();
		    		  if (data[1].equals("true")) selectedLabel.setText("Yes");
		    		  else selectedLabel.setText("No");
		    		  selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		    		  Components.setFixedSize(selectedLabel, new Dimension(100,20));
		    		  dataPanel.add(selectedLabel);
		    	  } else {
		    		  JTextField textField = new JTextField(data[1]);
			    	  textField.setHorizontalAlignment(SwingConstants.RIGHT);
			    	  Components.setFixedSize(textField, new Dimension(100,20));
			    	  dataPanel.add(textField);
		    	  }
		    	  renderer.add(dataPanel);
		    	  renderer.setEnabled(true);
		    	  returnValue = renderer;
		      }
		    }
		    if (returnValue == null) {
		      returnValue = getTreeCellRendererComponent(tree, value, selectedCell, expanded, leaf, row, hasFocus);
		    }
		    return returnValue;
		  }
	}
	
	public static class ScrollWrapper extends JScrollPane {
		private Ruler columnView, rowView;
		private Component content;
		public Component getContent() { return content; }
		public ScrollWrapper(Component component) {
			this.content = component;
			JPanel wrapperPanel = new JPanel();
			wrapperPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			wrapperPanel.setBackground(Color.LIGHT_GRAY);
			wrapperPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 50, 50, Color.LIGHT_GRAY));
			if (component instanceof Drawing) {
				JLabel[] corners = new JLabel[3];
				    for (int i = 0; i < corners.length; i++) {
				      corners[i] = new JLabel();
				      corners[i].setBackground(Color.LIGHT_GRAY);
				      if (i > 0) {
				    	  corners[i].setBackground(Color.LIGHT_GRAY.brighter());
				    	  corners[i].setBorder(new LineBorder(Color.LIGHT_GRAY.darker(), 1));
				      }
				      corners[i].setOpaque(true);
				}
				setCorner(JScrollPane.UPPER_LEFT_CORNER, corners[0]);
				setCorner(JScrollPane.LOWER_LEFT_CORNER, corners[1]);
				setCorner(JScrollPane.UPPER_RIGHT_CORNER, corners[2]);
				component.addPropertyChangeListener("preferredSize", new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						Dimension dim = ((Drawing) component).getZoomedSize();
						columnView.setPreferredSize(new Dimension(dim.width, 50));
						rowView.setPreferredSize(new Dimension(50, dim.height));
						columnView.revalidate(); columnView.repaint();
						rowView.revalidate(); rowView.repaint();
					}
				});
				columnView = new Ruler(Ruler.HORIZONTAL, component.getPreferredSize());
				rowView = new Ruler(Ruler.VERTICAL, component.getPreferredSize());
				RulerScroll rc = new RulerScroll(columnView);
				RulerScroll rr = new RulerScroll(rowView);
				getHorizontalScrollBar().addAdjustmentListener(e -> {
					if (e.getValue() <= component.getSize().width) rc.getHorizontalScrollBar().setValue(e.getValue());
				});
				getVerticalScrollBar().addAdjustmentListener(e -> {
					if (e.getValue() <= component.getSize().height) rr.getVerticalScrollBar().setValue(e.getValue());
				});
				setColumnHeaderView(rc);
				setRowHeaderView(rr);
			}
			wrapperPanel.add(component);
			if (component instanceof Drawing) setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			else setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
			setViewportView(wrapperPanel);
			setVisible(true);	
		}
		public Ruler getRuler(int orientation) {
			if (orientation == Ruler.HORIZONTAL) return columnView;
			else if (orientation == Ruler.VERTICAL) return rowView;
			else return null;
		}
	}
	
	public static class CustomTabbedPane extends JTabbedPane implements ChangeListener {
	    private int lastCount;
	    private ArrayList<ToolData> toolData = null;
	    public CustomTabbedPane() {
	    	super();
	    	addChangeListener(this);
	    }
		public CustomTabbedPane(ArrayList<ToolData> toolData) {
			super();
			addChangeListener(this);
			this.toolData = toolData;
		}
		public void addCloseableTab(int index, Component component) {
	    	String title = Storage.getInstance().getImageTitle(index);
	    	addTab(title, component);

	        JPanel tabPanel = new JPanel();
	        tabPanel.setOpaque(false);
	        tabPanel.setLayout(null);
	        tabPanel.setPreferredSize(new Dimension(80 + (title.length() * 2),20));
	        
	        JButton button = new JButton("X");
	        button.setMargin(new Insets(0, 0, 0, 0));
	        button.setBounds(60 + (title.length() * 2),2,18,18);
	        button.addActionListener(e -> {
	        		removeTabAt(Storage.getInstance().getImageIndex(title));
	        		Storage.getInstance().removeRedoList(Storage.getInstance().getImageIndex(title));
	        		Storage.getInstance().removeImageByTitle(title);
	        });
	        
	        JLabel label = new JLabel(title, JLabel.LEFT);
	        label.setBackground(getBackground());
	        label.setBounds(5,2,50 + (title.length() * 2),18);
	        
	        tabPanel.add(label);
	        tabPanel.add(button);
	        setTabComponentAt(index, tabPanel);
	    }
	    public void update(int imageCount) {
	    	if (lastCount != imageCount) {
	    		removeAll();
	    		for (int i = 0; i < Storage.getInstance().getImages().getChildNodes().getLength(); i++) {
	    			Canvas c;
	    			if (toolData != null && i < toolData.size()) c = new Canvas(Storage.getInstance().getImageTitle(i), toolData.get(i));
	    			else c = new Canvas(Storage.getInstance().getImageTitle(i));
					Drawing d = new Drawing(c);
					ScrollWrapper sw = new ScrollWrapper(d);
					addCloseableTab(i, sw);
				}
	    		if (imageCount > lastCount) setSelectedIndex(getTabCount() - 1);
	    		lastCount = imageCount;
	    		Application.getInstance().repaintCanvas();
	    	}
	    }
		@Override
		public void stateChanged(ChangeEvent e) {
			int imageCount = Storage.getInstance().getImageCount();
			if (getTabCount() > 0 && getTabCount() == imageCount) {
				Application.getInstance().reloadMenu();
				Application.getInstance().reloadProject();
			}
		}
	}
	
	public static class Drawing extends JComponent {
		private Canvas canvas;
		public Canvas getCanvas() { return canvas; }
		public Drawing(Canvas canvas) {
			this.canvas = canvas;
			setOpaque(false);
			addMouseListener(canvas);
			addMouseMotionListener(canvas);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			setTransferHandler(new ImageImporter());
			checkSize();
		}
		@Override
		public void paintComponent(Graphics g) {
			long renderTime = System.currentTimeMillis();
			checkSize();
			HashMap<Integer, Element> paintLater = new HashMap<Integer, Element>();
			Graphics2D graphSettings = (Graphics2D)g;
			graphSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);	
			Color background = null;
			GradientPaint backgroundGradient = null;
			if (Parser.parseColor(Storage.getInstance().getImage(canvas.getTools().getImageIndex()).getAttribute("background")) instanceof Color) {
				background = (Color) Parser.parseColor(Storage.getInstance().getImage(canvas.getTools().getImageIndex()).getAttribute("background"));
			} else {
				backgroundGradient = (GradientPaint) Parser.parseColor(Storage.getInstance().getImage(canvas.getTools().getImageIndex()).getAttribute("background"));
			}
			if (background == null) graphSettings.setPaint(backgroundGradient);
			else graphSettings.setColor(background);
			int width = (int) (Integer.parseInt(Storage.getInstance().getImage(canvas.getTools().getImageIndex()).getAttribute("width")) * canvas.getTools().getZoom());
			int height = (int) (Integer.parseInt(Storage.getInstance().getImage(canvas.getTools().getImageIndex()).getAttribute("height")) * canvas.getTools().getZoom());
			graphSettings.fillRect(0, 0, width, height);
			
			if (!Application.getInstance().getRuler(Ruler.HORIZONTAL).getGrid().isEmpty()) {
				graphSettings.setColor(Color.LIGHT_GRAY);
				graphSettings.setStroke(new BasicStroke(1));
				for (Integer i : Application.getInstance().getRuler(Ruler.HORIZONTAL).getGrid()) {
					graphSettings.drawLine((int)(i * canvas.getTools().getZoom()), 0, (int)(i * canvas.getTools().getZoom()), height);
				}
			}
			if (!Application.getInstance().getRuler(Ruler.VERTICAL).getGrid().isEmpty()) {
				graphSettings.setColor(Color.LIGHT_GRAY);
				graphSettings.setStroke(new BasicStroke(1));
				for (Integer i : Application.getInstance().getRuler(Ruler.VERTICAL).getGrid()) {
					graphSettings.drawLine(0, (int)(i * canvas.getTools().getZoom()), width, (int)(i * canvas.getTools().getZoom()));
				}
			}
			
			for (int l = 0; l < Storage.getInstance().getImage(canvas.getTools().getImageIndex()).getChildNodes().getLength(); l++) {
				Element layer = Storage.getInstance().getLayer(canvas.getTools().getImageIndex(), l);
				if (layer.getAttribute("visible").equals("true")) {
					for (int s = 0; s < layer.getChildNodes().getLength(); s++) {
						Element shape = Storage.getInstance().getShape(canvas.getTools().getImageIndex(), l, s);
						if (shape.getTagName().equals("Pencil")) {
								canvas.getTools().drawPencil(shape, graphSettings);
						} else {
							if (Parser.parseBoolean(shape.getAttribute("selected"))) {
								paintLater.put(s, shape);
							}
							else {
								canvas.getTools().drawShape(shape, graphSettings);
							}
						}
					}
				}
			}
			if (canvas.getTools().getPencil() != null) canvas.getTools().drawPencil(canvas.getTools().getPencil(), graphSettings);
			//RENDER SHADOW
			if (canvas.getShadow() != null && canvas.getOrigin() != null) {
				graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f));
				graphSettings.setColor(Color.BLACK);
				graphSettings.setXORMode(Color.WHITE);
				graphSettings.setStroke(new BasicStroke((float) (canvas.getTools().getWeight() * canvas.getTools().getZoom())));
				if (canvas.getTools().getTool() == Tool.LINE) graphSettings.drawLine(canvas.getOrigin().x, canvas.getOrigin().y, canvas.getShadow().x, canvas.getShadow().y);
				else {
					int x = Math.min(canvas.getOrigin().x, canvas.getShadow().x);
					int y = Math.min(canvas.getOrigin().y, canvas.getShadow().y); 
					if (canvas.getTools().getTool() == Tool.RECTANGLE || canvas.getTools().getTool() == Tool.TEXT || canvas.getTools().getTool() == Tool.BITMAP) graphSettings.drawRect(x, y, Math.abs(canvas.getOrigin().x - canvas.getShadow().x), Math.abs(canvas.getOrigin().y - canvas.getShadow().y));
					else if (canvas.getTools().getTool() == Tool.OVAL) graphSettings.drawOval(x, y, Math.abs(canvas.getOrigin().x - canvas.getShadow().x), Math.abs(canvas.getOrigin().y - canvas.getShadow().y));
					else if (canvas.getTools().getTool() == Tool.TRIANGLE) graphSettings.draw(canvas.getTools().createTriangle(canvas.getOrigin(), canvas.getShadow()));
				}
			}
			for (Entry<Integer, Element> pair : paintLater.entrySet()) {
				Element shape = pair.getValue();
				Selector selector = new Selector(pair.getKey(), canvas, graphSettings);
				canvas.addSelector(selector);
				add(selector);
				if (shape.getTagName().equals("Text")) {
					selector.requestFocusInWindow();
					selector.addKeyListener(selector);
				}
			}
			if (Application.getInstance().getRuler(Ruler.HORIZONTAL).getMarkers() != null) {
				graphSettings.setColor(Color.DARK_GRAY);
				graphSettings.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
				for (Point markerLocation : Application.getInstance().getRuler(Ruler.HORIZONTAL).getMarkers()) {
					g.drawLine((int)(markerLocation.x * canvas.getTools().getZoom()), 0, (int)(markerLocation.x * canvas.getTools().getZoom()), height);
				}		
			}
			if (Application.getInstance().getRuler(Ruler.VERTICAL).getMarkers() != null) {
				graphSettings.setColor(Color.DARK_GRAY);
				graphSettings.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
				for (Point markerLocation : Application.getInstance().getRuler(Ruler.VERTICAL).getMarkers()) {
					g.drawLine(0, (int)(markerLocation.y * canvas.getTools().getZoom()), width,(int)(markerLocation.y * canvas.getTools().getZoom()));
				}
			}
			canvas.setRendertime((double)System.currentTimeMillis() - renderTime); 
		}
		private void checkSize() {
			if (Storage.getInstance().getImageSize(canvas.getTools().getImageIndex()) != getSize()) {
				Dimension dim = Storage.getInstance().getImageSize(canvas.getTools().getImageIndex());
				dim.width = (int) (dim.width * canvas.getTools().getZoom());
				dim.height = (int) (dim.height * canvas.getTools().getZoom());
				setMinimumSize(dim);
				setPreferredSize(dim);
				setMaximumSize(dim);
				revalidate();
			}
		}
		public Dimension getZoomedSize() {
			Dimension dim = Storage.getInstance().getImageSize(canvas.getTools().getImageIndex());
			dim.width = (int) (dim.width * canvas.getTools().getZoom());
			dim.height = (int) (dim.height * canvas.getTools().getZoom());
			return dim;
		}

	}
 	
	public static class Status extends JPanel {
		private JLabel mouseX, mouseY, weight, tool, renderTime;
		private ColorLabel outline, fill, background;
		public Status() {
			setBorder(new EmptyBorder(5,5,5,5));
			setLayout(new GridLayout(1,0));
			tool = new JLabel("Active Tool: ", JLabel.CENTER);
			add(tool);
			mouseX = new JLabel("Mouse X: ", JLabel.CENTER);
			add(mouseX);
			mouseY = new JLabel("Mouse Y: ", JLabel.CENTER);
			add(mouseY);
			weight = new JLabel("Outline Weight: ", JLabel.CENTER);
			add(weight);
			JLabel label = new JLabel("Outline color: ", JLabel.CENTER);
			outline = new ColorLabel(Color.BLACK);
			add(label);
			add(outline);
			label = new JLabel("Fill color: ", JLabel.CENTER);
			fill = new ColorLabel(Color.WHITE);
			add(label);
			add(fill);
			label = new JLabel("Background color: ", JLabel.CENTER);
			background = new ColorLabel(Color.WHITE);
			add(label);
			add(background);
			renderTime = new JLabel("Unrendered", JLabel.CENTER);
			add(renderTime);
		}
		public void setStatus(Canvas canvas) { 
			tool.setText("Active Tool: " + canvas.getTools().getTool().getType());
			if (canvas.getTools().getTool() == Tool.TEXT) {
				mouseX.setText("Font: " + canvas.getTools().getFont().getFontName());
				mouseY.setText("Font Size: " + canvas.getTools().getFont().getSize());
			} else {
				if (canvas.getPosition() != null) {
					mouseX.setText("Mouse X: " + canvas.getPosition().x);
					mouseY.setText("Mouse Y: " + canvas.getPosition().y);
				}
			}
			weight.setText("Outline Weight: " + canvas.getTools().getWeight());
			outline.setColor(canvas.getTools().getOutline());
			fill.setColor(canvas.getTools().getFill());
			renderTime.setText("<html>Render Time: <font color=gray>" + canvas.getRendertime() + "ms</font></html>");
			if (canvas.getTools().getBackground() instanceof Color) {
				background.setColor((Color) canvas.getTools().getBackground());
			} else {
				background.setColor((GradientPaint) canvas.getTools().getBackground());
			}
			repaint();
			if (Components.DEBUG) System.out.println("STATUS BAR - Updating!"); 
		}
	}
	
	public static class ColorLabel extends JLabel {
		private Color color = null;
		private GradientPaint gradient = null;
		public ColorLabel(Color color) {
			this.color = color;
			repaint();
		}
		public ColorLabel(GradientPaint gradient) {
			this.gradient = new GradientPaint(gradient.getPoint1(), gradient.getColor1(), new Point2D.Double(getWidth(), 0), gradient.getColor2());
			repaint();
		}
		public void setColor(Color color) {
			this.color = color;
			gradient = null;
		}
		public void setColor(GradientPaint gradient) {
			this.gradient = new GradientPaint(gradient.getPoint1(), gradient.getColor1(), new Point2D.Double(getWidth(), 0), gradient.getColor2());
			color = null;
		}
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D graphSettings = (Graphics2D)g;
			if (color == null) graphSettings.setPaint(gradient);
			else graphSettings.setColor(color);
			graphSettings.fillRect(0, 0, getWidth(), getHeight());
			graphSettings.setColor(Color.BLACK);
			graphSettings.drawRect(0, 0, getWidth(), getHeight());
		}
	}

	public static class ColorButton extends JButton implements MouseListener {
		private Color color = null;
		private GradientPaint gradient = null;
		public ColorButton(String text) {
			super(text);
			setContentAreaFilled(false);
			addMouseListener(this);
		}
		@Override
		public void setBackground(Color color) { 
			this.color = color; 
			gradient = null;
			setForeground(getContrastColor(color));
		}
		public void setBackground(GradientPaint gradient) { 
			this.gradient = gradient; 
			color = null;
			setForeground(getContrastColor(gradient.getColor1()));
		}
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D graphSettings = (Graphics2D)g;
			if (color == null) graphSettings.setPaint(gradient);
			else graphSettings.setColor(color);
			graphSettings.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
		public void mouseClicked(MouseEvent e) { 
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (getText().equals("Outline")) {
					Color color = Application.getInstance().getCanvas().getTools().getOutline(); 
					if (color.getAlpha() == 0) {
						Color temp = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
						Application.getInstance().getCanvas().getTools().setOutline(temp);
					} else {
						Color temp = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
						Application.getInstance().getCanvas().getTools().setOutline(temp);
					}
				} else if (getText().equals("Background")) {
					Object color = Application.getInstance().getCanvas().getTools().getBackground();
					if (color instanceof Color) {
						Color c = (Color) color;
						if (c.getAlpha() == 0) {
							Color temp = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
							Application.getInstance().getCanvas().getTools().setBackground(temp);
						} else {
							Color temp = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0);
							Application.getInstance().getCanvas().getTools().setBackground(temp);
						}	
					} 
				} else if (getText().equals("Fill")) {
					Color color = Application.getInstance().getCanvas().getTools().getFill(); 
					if (color.getAlpha() == 0) {
						Color temp = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
						Application.getInstance().getCanvas().getTools().setFill(temp);
					} else {
						Color temp = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
						Application.getInstance().getCanvas().getTools().setFill(temp);
					}	
				}
				Application.getInstance().reloadMenu();
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
	
	public static class Splash extends JWindow {
		public JProgressBar progressBar;	
		public Splash() {
			UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
			UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
			setSize(767, 513);
			setLayout(new BorderLayout());
			add(splashImage(), BorderLayout.CENTER);
			add(progressBar(), BorderLayout.SOUTH);
			setLocationRelativeTo(null);
			setVisible(true);	
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private JLabel splashImage() {
			ImageIcon gif = new ImageIcon(getClass().getResource("/icons/sketches.gif"));
			JLabel splash = new JLabel(gif);
			return splash;
		}	
		private JProgressBar progressBar() {
			progressBar = new JProgressBar();
			progressBar.setString("Modifying the Phase Variance");
			progressBar.setStringPainted(true);
			progressBar.setBorder(null);
			progressBar.setBorderPainted(false);
			progressBar.setBackground(Color.DARK_GRAY);
			progressBar.setForeground(Color.BLACK);
			return progressBar;
		}	
	}
	
	public static class Selector extends JComponent implements KeyListener {
		private Element shape;
		private Canvas canvas;
		private Point start;
		private int width, height, shapeIndex;
		public Rectangle moveButton, resizeButton, rotateButton, curveButton;
		public int getShapeIndex() { return shapeIndex; }
		@SuppressWarnings("restriction")
		public Selector(int shapeIndex, Canvas canvas, Graphics2D graphSettings) {
			this.shapeIndex = shapeIndex;
			this.canvas = canvas;
			addMouseListener(canvas);
			addMouseMotionListener(canvas);
			shape = Storage.getInstance().getShape(canvas.getTools().getImageIndex(), canvas.getTools().getLayerIndex(), shapeIndex);
			if (!shape.getTagName().equals("Line")) {
				start = Parser.parsePoint(shape.getAttribute("location"));
				start.x = (int) (start.x * canvas.getTools().getZoom()); start.y = (int) (start.y * canvas.getTools().getZoom());
				width = (int) (Double.parseDouble(shape.getAttribute("width")) * canvas.getTools().getZoom());
				height = (int) (Double.parseDouble(shape.getAttribute("height")) * canvas.getTools().getZoom()); 
			}	
			if (shape.getTagName().equals("Rectangle") || shape.getTagName().equals("Oval") || shape.getTagName().equals("Text") || shape.getTagName().equals("Bitmap")) {
				moveButton = new Rectangle(start.x + ((width / 2) - 14), start.y + ((height / 2) - 14), 28, 28);
				resizeButton = new Rectangle(start.x + width, start.y + height, 20, 20);
				rotateButton = new Rectangle(start.x + width, start.y + height - (height + 20), 24, 24);
			} else if (shape.getTagName().equals("Triangle")) { 
				Point centroid = canvas.getTools().getTriangleCentroid(start, new Point(start.x + width, start.y + height));
				moveButton = new Rectangle(centroid.x - 14, centroid.y - 14, 28, 28);
				resizeButton = new Rectangle(centroid.x + (width / 2) + 20, centroid.y + (height / 2) + 20, 20, 20);
				rotateButton = new Rectangle(centroid.x + (width / 2), centroid.y - height, 24, 24);
			} else if (shape.getTagName().equals("Line")) {
				Point ctrl = Parser.parsePoint(shape.getAttribute("ctrl"));
				curveButton = new Rectangle((int) ((ctrl.x - 10) * canvas.getTools().getZoom()), (int) ((ctrl.y - 10) * canvas.getTools().getZoom()), 20, 20);
			}
			drawSelected(graphSettings);
		}	
		private void drawSelected(Graphics2D graphSettings) {
			if (shape.getTagName().equals("Text")) canvas.getTools().drawText(shape, graphSettings);
			else canvas.getTools().drawShape(shape, graphSettings);
			graphSettings.setStroke(new BasicStroke(3));
			graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
			if (shape.getTagName().equals("Rectangle") || shape.getTagName().equals("Oval") || shape.getTagName().equals("Triangle") || shape.getTagName().equals("Text") || shape.getTagName().equals("Bitmap")) {
				graphSettings.setColor(Color.WHITE);
				graphSettings.fillRect(moveButton.x, moveButton.y, moveButton.width, moveButton.height);
				graphSettings.fillRect(resizeButton.x, resizeButton.y, resizeButton.width, resizeButton.height);
				graphSettings.fillOval(rotateButton.x, rotateButton.y, rotateButton.width, rotateButton.height);
				graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				graphSettings.setColor(Color.BLACK);
				graphSettings.drawRect(moveButton.x, moveButton.y, moveButton.width, moveButton.height);
				graphSettings.drawRect(resizeButton.x, resizeButton.y, resizeButton.width, resizeButton.height);
				graphSettings.drawOval(rotateButton.x, rotateButton.y, rotateButton.width, rotateButton.height);
			} 
			else if (shape.getTagName().equals("Line")) {
				graphSettings.setColor(Color.WHITE);
				graphSettings.fillRect(curveButton.x, curveButton.y, curveButton.width, curveButton.height);
				graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				graphSettings.setColor(Color.BLACK);
				graphSettings.drawRect(curveButton.x, curveButton.y, curveButton.width, curveButton.height);
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {
			
		}
		@Override
		public void keyReleased(KeyEvent e) {
			
		}
		@Override
		public void keyTyped(KeyEvent e) {
			if (!Parser.parseBoolean(shape.getAttribute("selected"))) {
				setFocusable(false);
				removeKeyListener(this);
				return;
			} else if (e.getKeyChar() == KeyEvent.VK_ESCAPE || e.getKeyChar() == KeyEvent.VK_ENTER) {
				setFocusable(false);
				removeKeyListener(this);
				if (shape.getAttribute("text").equals("")) Storage.getInstance().getLayer(Application.getInstance().getCanvas().getTools().getImageIndex(), Application.getInstance().getCanvas().getTools().getLayerIndex()).removeChild(shape);
				else shape.setAttribute("selected", Parser.createBoolean(false));
			} else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
				String newString = shape.getAttribute("text");
				if (newString.equals("")) return;
				shape.setAttribute("text", newString.substring(0 , newString.length() - 1));
			} else {
				shape.setAttribute("text", shape.getAttribute("text") + e.getKeyChar());
				requestFocusInWindow();
			}
			Application.getInstance().repaintCanvas();
			Application.getInstance().reloadProject();
		}
	}
	
	public static class ToolContext extends JPopupMenu {
		private JPanel contextPanel;
	    public ToolContext(Canvas canvas) {
	    	setOpaque(false);
	    	JPanel title = new JPanel();
	    	JLabel label = new JLabel(canvas.getTools().getTool().getType(), JLabel.CENTER);
			label.setFont(label.getFont().deriveFont(13f));
			title.setBackground(Color.DARK_GRAY);
			label.setForeground(Color.WHITE);
			title.add(label);
			add(title, BorderLayout.NORTH);
	        contextPanel = new JPanel();
	        contextPanel.setOpaque(false);
	        GridLayout layout = new GridLayout(0,1); layout.setHgap(10);
	        contextPanel.setLayout(layout);        
	        add(contextPanel, BorderLayout.CENTER);        
	        if (canvas.getTools().getTool() == Tool.LINE || canvas.getTools().getTool() == Tool.PENCIL) lineTool(canvas);	
	        else if (canvas.getTools().getTool() == Tool.OVAL || canvas.getTools().getTool() == Tool.TRIANGLE || canvas.getTools().getTool() == Tool.RECTANGLE) shapeTool(canvas);	        		                		        
	    }
		private void lineTool(Canvas canvas) {
			JPanel colorPanel = new JPanel();
			colorPanel.setLayout(new GridLayout(1,0));
			ColorButton color = new ColorButton("Color");
			if (Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()) instanceof Color) color.setBackground((Color) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()));
			else color.setBackground((GradientPaint) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()));
			color.addActionListener(e -> {
				Functions.routeFunction("Set Outline");
				setVisible(false);
			});
			colorPanel.add(color);
			contextPanel.add(colorPanel);
			
			JSlider slider = new JSlider(1, 20, canvas.getTools().getWeight());
			slider.setOpaque(false);
			slider.setMinorTickSpacing(1);
			slider.setPaintTicks(true);
			slider.setSnapToTicks(true);
			Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
			table.put(1, new JLabel("1"));
			table.put(5, new JLabel("5"));
			table.put(10, new JLabel("10"));
			table.put(15, new JLabel("15"));
			table.put(20, new JLabel("20"));
			slider.setLabelTable(table);
			slider.setPaintLabels(true);
			//SLIDER STROKE THICKNESS
			slider.addChangeListener(new ChangeListener() {
			    public void stateChanged(ChangeEvent event) {
			    	canvas.getTools().setWeight(slider.getValue());
			    }
			});
			contextPanel.add(slider);
			pack();
		}
		private void shapeTool(Canvas canvas) {
			JPanel colorPanel = new JPanel();
			colorPanel.setLayout(new GridLayout(1,0));
			ColorButton outline = new ColorButton("Outline");
			if (Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()) instanceof Color) outline.setBackground((Color) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()));
			else outline.setBackground((GradientPaint) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()));
			outline.addActionListener(e -> {
				Functions.routeFunction("Set Outline");
				setVisible(false);
			});
			colorPanel.add(outline);
			
			ColorButton fill = new ColorButton("Fill");
			if (Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getOutline()) instanceof Color) fill.setBackground((Color) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getFill()));
			else fill.setBackground((GradientPaint) Components.transparencyCheck(Application.getInstance().getCanvas().getTools().getFill()));
			fill.addActionListener(e -> {
				Functions.routeFunction("Set Fill");
				setVisible(false);
			});
			colorPanel.add(fill);
			contextPanel.add(colorPanel);
			
			JSlider slider = new JSlider(1, 20, canvas.getTools().getWeight());
			slider.setOpaque(false);
			slider.setMinorTickSpacing(1);
			slider.setPaintTicks(true);
			slider.setSnapToTicks(true);
			Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
			table.put(1, new JLabel("1"));
			table.put(5, new JLabel("5"));
			table.put(10, new JLabel("10"));
			table.put(15, new JLabel("15"));
			table.put(20, new JLabel("20"));
			slider.setLabelTable(table);
			slider.setPaintLabels(true);
			//SLIDER STROKE THICKNESS
			slider.addChangeListener(new ChangeListener() {
			    public void stateChanged(ChangeEvent event) {
			    	canvas.getTools().setWeight(slider.getValue());
			    }
			});
			contextPanel.add(slider);
			pack();
		}
	}

	public static class ShapeContext extends JPopupMenu {
	    public ShapeContext(Element shape) {
	    	setOpaque(false);
	    	JPanel title = new JPanel();
	    	JLabel label = new JLabel(shape.getTagName(), JLabel.CENTER);
			label.setFont(label.getFont().deriveFont(13f));
			title.setBackground(Color.DARK_GRAY);
			label.setForeground(Color.WHITE);
			title.add(label);
			add(title, BorderLayout.NORTH);
	    	JPanel contextPanel = new JPanel();
		    contextPanel.setOpaque(false);
		    GridLayout layout = new GridLayout(0,1); layout.setHgap(10);
		    contextPanel.setLayout(layout); 
	 	    
		    if (!shape.getTagName().equals("Bitmap")) {
		    	JPanel colorPanel = new JPanel();
				colorPanel.setLayout(new GridLayout(1,0));
				ColorButton outline = new ColorButton("Outline");
				if (shape.getTagName().equals("Line") || shape.getTagName().equals("Text")) {
					outline.setText("Color");
					if (Components.transparencyCheck(Parser.parseColor(shape.getAttribute("color"))) instanceof Color) outline.setBackground((Color) Components.transparencyCheck(Parser.parseColor(shape.getAttribute("color"))));
					else outline.setBackground((GradientPaint) Components.transparencyCheck(Parser.parseColor(shape.getAttribute("color"))));
				} else {
					if (Components.transparencyCheck(Parser.parseColor(shape.getAttribute("outline"))) instanceof Color) outline.setBackground((Color) Components.transparencyCheck(Parser.parseColor(shape.getAttribute("outline"))));
					else outline.setBackground((GradientPaint) Components.transparencyCheck(Parser.parseColor(shape.getAttribute("outline"))));
				}
				outline.addActionListener(e -> {
					ColorDialog cd = new ColorDialog();
					cd.showColorChanger("Outline", shape);
					setVisible(false);
				});
				colorPanel.add(outline);
				
				if (shape.getTagName().equals("Rectangle") || shape.getTagName().equals("Oval") || shape.getTagName().equals("Triangle")) {
					ColorButton fill = new ColorButton("Fill");
					if (Components.transparencyCheck(Parser.parseColor(shape.getAttribute("fill"))) instanceof Color) fill.setBackground((Color) Components.transparencyCheck(Parser.parseColor(shape.getAttribute("fill"))));
					else fill.setBackground((GradientPaint) Components.transparencyCheck(Parser.parseColor(shape.getAttribute("fill"))));
					fill.addActionListener(e -> {
						ColorDialog cd = new ColorDialog();
						cd.showColorChanger("Fill", shape);
						setVisible(false);
					});
					colorPanel.add(fill);
				} else if (shape.getTagName().equals("Text")) {
			    	JButton button = new JButton("Set Font");
			    	button.addActionListener(e -> {
			    		Functions.routeFunction("Set Font");
			    		setVisible(false);
			    	});
			    	colorPanel.add(button);
			    }
				contextPanel.add(colorPanel);
				
				JSlider slider;
				if (shape.getTagName().equals("Text")) slider =  new JSlider(1, 120, Parser.getFontSize(shape.getAttribute("font")));
				else slider = new JSlider(1, 20, Integer.parseInt(shape.getAttribute("weight")));
				slider.setBackground(Color.DARK_GRAY);
				slider.setForeground(Color.WHITE);
				slider.setMinorTickSpacing(1);
				slider.setPaintTicks(true);
				slider.setSnapToTicks(true);
				if (shape.getTagName().equals("Text")) {
					Hashtable<Integer, CustomLabel> table = new Hashtable<Integer, CustomLabel>();
					table.put(15, new CustomLabel("15", Color.WHITE));
					table.put(30, new CustomLabel("30", Color.WHITE));
					table.put(45, new CustomLabel("45", Color.WHITE));
					table.put(60, new CustomLabel("60", Color.WHITE));
					table.put(75, new CustomLabel("75", Color.WHITE));
					table.put(90, new CustomLabel("90", Color.WHITE));
					table.put(105, new CustomLabel("105", Color.WHITE));
					table.put(120, new CustomLabel("120", Color.WHITE));
					slider.setLabelTable(table);
					slider.setPaintLabels(true);
				} else {
					Hashtable<Integer, CustomLabel> table = new Hashtable<Integer, CustomLabel>();
					table.put(1, new CustomLabel("1", Color.WHITE));
					table.put(5, new CustomLabel("5", Color.WHITE));
					table.put(10, new CustomLabel("10", Color.WHITE));
					table.put(15, new CustomLabel("15", Color.WHITE));
					table.put(20, new CustomLabel("20", Color.WHITE));
					slider.setLabelTable(table);
					slider.setPaintLabels(true);
				}
				slider.addChangeListener(new ChangeListener() {
				    public void stateChanged(ChangeEvent event) {
				    	if (shape.getTagName().equals("Text")) {
				    		Storage.getInstance().changeFontSize(shape, slider.getValue());
				    	} else {
				    		shape.setAttribute("weight", "" + slider.getValue());
				    	} 	
				    	Application.getInstance().repaintCanvas();
				    }
				});
				contextPanel.add(slider);
		    }    
		    if (shape.getTagName().equals("Bitmap")) contextPanel.setPreferredSize(new Dimension(120,40));
		    JButton button = new JButton("Delete");
			button.addActionListener(e -> {
				Storage.getInstance().deleteSelected();
				Application.getInstance().repaintCanvas();
				setVisible(false);
			});
			contextPanel.add(button);
			add(contextPanel, BorderLayout.CENTER);
			pack();
	    }
	    private class CustomLabel extends JLabel {
	    	public CustomLabel(String text, Color foreground) {
	    		setText(text);
	    		setForeground(foreground);
	    	}
	    }
	}
	 
	public static class Ruler extends JComponent {
	    public static final int HORIZONTAL = 0, VERTICAL = 1;
	    private int orientation;
	    private Point point;
	    private ArrayList<Point> markerLocations = new ArrayList<Point>();
	    private ArrayList<Integer> gridLocations = new ArrayList<Integer>();
	    public Ruler(int o, Dimension component) {
	        orientation = o;
	        MouseAdapter adapter = new MouseAdapter(){
	            public void mouseClicked(MouseEvent e) {
	            	if (e.getButton() == MouseEvent.BUTTON1) {
	            		markerLocations.add(e.getPoint());
	            	}
	            	else if (e.getButton() == MouseEvent.BUTTON3) {
	            		if (!markerLocations.isEmpty()) markerLocations.remove(markerLocations.get(markerLocations.size() - 1));
	            	}
	            	Application.getInstance().repaintCanvas();
	            }
	            public void mouseMoved(MouseEvent e) {
	            	Application.getInstance().getCanvas().setPosition(e.getPoint());
	            	repaint();
	            }
	         };
	         addMouseListener(adapter);
	         addMouseMotionListener(adapter);
	        if (orientation == HORIZONTAL) {
	        	setPreferredSize(new Dimension(component.width, 50));
	        } else {
	        	setPreferredSize(new Dimension(50, component.height));
	        }
	        Timer t = new Timer(10, e -> {
	        	Point point = Application.getInstance().getCanvas().getPosition();
	        	if (this.point != point) {
	        		this.point = point;
	        		repaint();
	        	}
	        });
	        t.start();
	    }
	    @Override
	    protected void paintComponent(Graphics g) {	        
	        int height = getPreferredSize().height;
	        int width = getPreferredSize().width;
	        g.setColor(Color.LIGHT_GRAY);
	        g.fillRect(0, 0, getWidth(), getHeight());
	        g.setColor(Color.BLACK);
	        if (orientation == HORIZONTAL) {
	        	int i = 1;
	        	double zoom = Application.getInstance().getCanvas().getTools().getZoom();
	        	for (int x=0; x < width / zoom; x++) {
	        		if (x % 50 == 0) {
		   	 	     	g.drawLine((int)(x * zoom), height - 20,(int) (x * zoom), height);
		   	 	        if (i != 1) g.drawString("" + i, (int)(x * zoom) - 3, height - 30);
		   	 	        i++;
		   	 	    } else if (x % 10 == 0) {
		   	 	    	g.drawLine((int)(x * zoom), height - 10, (int)(x * zoom), height);
		   	 	    } else if (x % 2 == 0) {
		   	 	        g.drawLine((int)(x * zoom), height - 5, (int)(x * zoom), height);
		   	 	    }		 
	 	        }
	        	if (!markerLocations.isEmpty()) {
		       		g.setColor(Color.DARK_GRAY);
		       		for (Point markerLocation : markerLocations) {
		       			g.drawLine((int)(markerLocation.x * zoom), 0,(int)(markerLocation.x * zoom), height);
		       		}
		       	}
	        	if (point != null) {
		       		g.setColor(Color.RED);
		       		g.drawLine(point.x, 0, point.x, height);
		       	} 
	        }
	        else {
	        	int i = 1;
	        	double zoom = Application.getInstance().getCanvas().getTools().getZoom();
	        	 for (int y=0; y < height / zoom; y++) {
	        		if (y % 50 == 0) {         
		  	 	        g.drawLine(30,(int)(y * zoom), 50, (int)(y * zoom));
		  	 	        if (i != 1) g.drawString("" + i, 5, (int)(y * zoom) + 3);
		  	 	        i++;
	        		} else if (y % 10 == 0) {     
		  	 	        g.drawLine(40, (int)(y * zoom), 50, (int)(y * zoom));
	        		} else if (y % 2 == 0) {     
		  	 	        g.drawLine(45, (int)(y * zoom), 50, (int)(y * zoom));
	        		}	
	 	        }
	        	if (!markerLocations.isEmpty()) {
		       		g.setColor(Color.DARK_GRAY);
		       		for (Point markerLocation : markerLocations) {
		       			g.drawLine(0, (int)(markerLocation.y * zoom), width,(int)(markerLocation.y * zoom));
		       		}	
		       	}
	        	if (point != null) {
	        		g.setColor(Color.RED);
	        		g.drawLine(0, point.y, width, point.y);
	        	} 
	        }  
	    }
	    public ArrayList<Point> getMarkers() {
	    	if (!markerLocations.isEmpty()) return markerLocations;
	    	else return null;
	    }
	    public ArrayList<Integer> getGrid() { return gridLocations; }
	    public void addGrid(int point) { gridLocations.add(point); } 
	    public void clearGrid() { gridLocations.clear(); }
	    
	    public static class RulerScroll extends JScrollPane {
			public RulerScroll(Ruler ruler) {
		    	super(ruler);
				if (ruler.orientation == Ruler.HORIZONTAL) setBorder(BorderFactory.createMatteBorder(0,0,0,50, Color.LIGHT_GRAY));
				else setBorder(BorderFactory.createMatteBorder(0,0,50,0, Color.LIGHT_GRAY));
			}
		}
	}
	
}
