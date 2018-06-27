package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Element;

import controller.Functions;
import model.Resource;
import model.Settings;
import model.Storage;
import model.Tools;
import model.Storage.ImageFactory;
import model.Storage.Parser;
import view.Components.Ruler;

public class Dialogs {
	
	public static boolean DEBUG = false;
	
	public static class CustomDialog extends JDialog {
		public CustomDialog() {
			setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/icon.jpg")));
			setResizable(false);
			setAlwaysOnTop(true); 
		}
		protected void publish() {
			pack();
	    	setVisible(true);
	    	setLocationRelativeTo(null);
		}
	}
	
	public static class CreateImageDialog extends CustomDialog {
		private String[] formats = {"VGA", "HD", "FHD", "4K"};
		private ResFormat[] values = {new ResFormat(640,480), new ResFormat(1280, 720), new ResFormat(1920, 1080), new ResFormat(4096, 2160)};
		public CreateImageDialog() {
			setTitle("Create New Image");
			
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(5,5,5,5));
			GridLayout gl = new GridLayout(0,1);
			gl.setVgap(5);
			panel.setLayout(gl);
			
			JPanel namePanel = new JPanel();
			namePanel.setLayout(new GridLayout(0,2));
			JLabel nameLabel = new JLabel("Image Title:", SwingConstants.CENTER);
			JTextField nameInput = new JTextField();
			nameInput.setText("New Image");
			namePanel.add(nameLabel);
			namePanel.add(nameInput);
			
			JPanel widthPanel = new JPanel();
			widthPanel.setLayout(new GridLayout(0,2));
			JLabel widthLabel = new JLabel("Width:", SwingConstants.CENTER);
			JSpinner widthInput = new JSpinner();
			SpinnerModel model = new SpinnerNumberModel(1280, 1, 100000, 1);  
			widthInput.setModel(model);
			
			JPanel heightPanel = new JPanel();
			heightPanel.setLayout(new GridLayout(0,2));
			JLabel heightLabel = new JLabel("Height:", SwingConstants.CENTER);
			JSpinner heightInput = new JSpinner();
			model = new SpinnerNumberModel(720, 1, 100000, 1);
			heightInput.setModel(model);
			
			widthPanel.add(widthLabel);
			widthPanel.add(widthInput);
			heightPanel.add(heightLabel);
			heightPanel.add(heightInput);
			
			
			JLabel presetLabel = new JLabel("Format Presets:", JLabel.CENTER);
			JPanel presetButtons = new JPanel();
			presetButtons.setLayout(new GridLayout(1,0));
			for (int i = 0; i < formats.length; i++) {
				int index = i;
				JButton button = new JButton(formats[index]);
				button.addActionListener(e -> {
					widthInput.setValue(values[index].width);
					heightInput.setValue(values[index].height);
				});
				presetButtons.add(button);
			}
			
			JPanel buttonPanel = new JPanel();
			gl = new GridLayout(0, 2);
			buttonPanel.setLayout(gl);
			JButton confirm = new JButton("Create");
			confirm.addActionListener(e -> {
				if (!nameInput.getText().equals("")) {
					ImageFactory.createImage(nameInput.getText(), (Integer) widthInput.getValue(), (Integer) heightInput.getValue(), Color.WHITE);
					dispose();
					if (Dialogs.DEBUG) System.out.println("CREATE IMAGE DIALOG - Creating Image");
				} else {
					dispose();
					if (Dialogs.DEBUG) System.out.println("CREATE IMAGE DIALOG - Disposing Dialog (No Title Entered)");
				}	
			});
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
				if (Dialogs.DEBUG) System.out.println("CREATE IMAGE DIALOG - Disposing Dialog (Cancel)");
			});
			
			buttonPanel.add(confirm);
			buttonPanel.add(cancel);
			panel.add(namePanel);
			panel.add(widthPanel);
			panel.add(heightPanel);
			panel.add(presetLabel);
			panel.add(presetButtons);
			panel.add(buttonPanel);
			
			add(panel);
			getRootPane().setDefaultButton(confirm);
			publish();
		}
		private class ResFormat {
			public int width, height;
			public ResFormat(int width, int height) {
				this.width = width; this.height = height;
			}
		}
	}
	
	public static class ColorDialog extends CustomDialog {
		private JColorChooser chooser;
		private JPanel contentPanel;
		private String title;	
		private Element shape = null;
		public ColorDialog() {	
			super();
			contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));	
			chooser = new JColorChooser();
	        AbstractColorChooserPanel[] panels = chooser.getChooserPanels(); 
	        for (AbstractColorChooserPanel panel : panels) {
	        	if (panel.getDisplayName().equals("RGB")) {
	        		panel.setBorder(new TitledBorder(panel.getDisplayName()));
	        		contentPanel.add(panel, BorderLayout.CENTER);
	        	} else if (panel.getDisplayName().equals("Swatches")) {
	        		panel.setBorder(new TitledBorder(panel.getDisplayName()));
	        		contentPanel.add(panel, BorderLayout.NORTH);
	        	}
	        }    
	        contentPanel.add(generateButtons(), BorderLayout.SOUTH);      
	        add(contentPanel);
		}
		public void showColorChooser(String title) {
			this.title = title;
			setTitle("Select " + title + " Color");
	        setVisible(true);   
	        publish();
		}
		public void showColorChanger(String title, Element shape) {
			this.title = title;
			this.shape = shape;
			setTitle("Change " + title + " Color");
	        setVisible(true);   
	        publish();
		}
		private JPanel generateButtons() {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1,0));
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			JButton ok = new JButton("Ok");
			ok.addActionListener(e-> {
				if (title.equals("Background")) Application.getInstance().getCanvas().getTools().setBackground(chooser.getColor());
				else if (title.equals("Outline")) {
					if (shape == null) Application.getInstance().getCanvas().getTools().setOutline(chooser.getColor());
					else {
						if (shape.getTagName().equals("Line") || shape.getTagName().equals("Text")) shape.setAttribute("color", Parser.createColor(chooser.getColor()));
						else shape.setAttribute("outline", Parser.createColor(chooser.getColor()));
					}
				}
				else {
					if (shape == null) Application.getInstance().getCanvas().getTools().setFill(chooser.getColor());
					else shape.setAttribute("fill", Parser.createColor(chooser.getColor()));
				}
				Application.getInstance().toggleMenu("Tools");
				Application.getInstance().repaintMenu();
				dispose();
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttonPanel.add(ok);
			buttonPanel.add(cancel);
			getRootPane().setDefaultButton(ok);
			return buttonPanel;
		}
	}

	public static class ResizeDialog extends CustomDialog {
		public ResizeDialog(Boolean resizeContents) {
			if (resizeContents) setTitle("Resize Image");
			else setTitle("Resize Canvas");
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new GridLayout(0,1));
			JLabel current = new JLabel("Current canvas size: " + Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("width") + " x " + Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("height"), JLabel.CENTER);
			contentPanel.add(current);
			JPanel widthPanel = new JPanel();
			widthPanel.setLayout(new GridLayout(0,2));
			JLabel widthLabel = new JLabel("Width:", SwingConstants.CENTER);
			JSpinner widthInput = new JSpinner();
			SpinnerModel model = new SpinnerNumberModel(Integer.parseInt(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("width")), 0, 100000, 1);  
			widthInput.setModel(model);
			widthPanel.add(widthLabel);
			widthPanel.add(widthInput);
			contentPanel.add(widthPanel);
			JPanel heightPanel = new JPanel();
			heightPanel.setLayout(new GridLayout(0,2));
			JLabel heightLabel = new JLabel("Height:", SwingConstants.CENTER);
			JSpinner heightInput = new JSpinner();
			model = new SpinnerNumberModel(Integer.parseInt(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("height")), 0, 100000, 1);
			heightInput.setModel(model);
			heightPanel.add(heightLabel);
			heightPanel.add(heightInput);
			contentPanel.add(heightPanel);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(0, 2));
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			JButton confirm = new JButton("Resize");
			confirm.addActionListener(e -> {
				int heightValue = (Integer) heightInput.getValue();
				int widthValue = (Integer) widthInput.getValue();
				if (resizeContents) {
					if (heightValue != 0 && widthValue != 0) {
						int width = Integer.parseInt(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("width"));
						int height = Integer.parseInt(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("height"));
						Resource.getInstance().resizeImage(Application.getInstance().getCanvas().getTools().getImageIndex(), new Dimension(width, height), new Dimension(widthValue, heightValue));
						Application.getInstance().repaintCanvas();
						dispose();
					}
				} else {
					if (heightValue != 0 && widthValue != 0) {
						Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).setAttribute("width", "" + widthValue);
						Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).setAttribute("height", "" + heightValue);
						Application.getInstance().repaintCanvas();
						dispose();
					}
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttonPanel.add(confirm);
			buttonPanel.add(cancel);
			contentPanel.add(buttonPanel);
			add(contentPanel);
			getRootPane().setDefaultButton(confirm);
			publish();
		}
	}

	public static class StartupDialog extends CustomDialog {
		public StartupDialog() {
			setTitle("Startup Wizard");	
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new GridLayout(0,1));
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.add(infoPanel());
			contentPanel.add(buttonPanel());
			add(contentPanel);
			publish();
		}
		private JPanel infoPanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0,1));
			JLabel welcome = new JLabel("eXtensible - Alpha Version 2.1", JLabel.CENTER);
			JLabel info = new JLabel("This application requires an XML project file, you can either create a new one or load a previously saved one", JLabel.CENTER);
			panel.add(welcome);
			panel.add(info);
			return panel;
		}
		private JPanel buttonPanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0,1));
			panel.setBorder(new EmptyBorder(5,0,0,0));
			JButton newProject = new JButton("Start a new project");
			newProject.addActionListener(e -> {
				dispose();
				new CreateImageDialog();
			});
			JButton openProject = new JButton("Open existing project");
			openProject.addActionListener(e -> {
				dispose();
				Storage.getInstance().openDocument();
			});
			panel.add(newProject);
			panel.add(openProject);
			return panel;
		}		
	}

	public static class FontDialog extends CustomDialog {
		  protected Font resultFont;
		  protected String resultName;
		  protected int resultSize;
		  protected boolean isBold;
		  protected boolean isItalic;
		  protected String displayText = "Qwerty Yuiop";
		  protected JList<String> fontNameChoice, fontSizeChoice;	 
		  protected DefaultListModel<String> fontName, fontSize;
		  protected JCheckBox bold, italic;
		  protected JLabel previewArea;
		  protected String fontSizes[] = { "8", "10", "11", "12", "14", "16", "18", "20", "24", "30", "36", "40", "48", "60", "72", "84", "90", "104", "120"};
		  protected static final int DEFAULT_SIZE = 3;
		  public FontDialog() {
		    setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		    setTitle("Select Font");
		    
		    JPanel top = new JPanel();
		    top.setBorder(new EmptyBorder(5,5,5,5));
		    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));

		    fontNameChoice = new JList<String>();
		    JScrollPane name = new JScrollPane(fontNameChoice);
		    fontNameChoice.setVisibleRowCount(8);
		    top.add(name);
		   
		    String fontList[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		    fontName = new DefaultListModel<String>();  
		    
		    for (int i = 0; i < fontList.length; i++)
		      fontName.addElement(fontList[i]);
		    fontNameChoice.setModel(fontName);
		    fontNameChoice.setSelectedIndex(1);

		    fontSizeChoice = new JList<String>();
		    JScrollPane size = new JScrollPane(fontSizeChoice);
		    size.setMinimumSize(new Dimension(size.getHeight(), 50));
		    fontSizeChoice.setVisibleRowCount(8);
		    
		    JPanel spacer = new JPanel();
		    spacer.setMaximumSize(new Dimension(0,5));
		    top.add(spacer);
		    top.add(size);
		    
		    fontSize = new DefaultListModel<String>();

		    for (int i = 0; i < fontSizes.length; i++) 
		    	fontSize.addElement(fontSizes[i]);
		    fontSizeChoice.setModel(fontSize);
		    fontSizeChoice.setSelectedIndex(DEFAULT_SIZE);
		    	
		    add(top);

		    JPanel attrs = new JPanel();
		    add(attrs);
		    attrs.setBorder(new EmptyBorder(5,80,10,0));
		    attrs.setLayout(new GridLayout(0, 2));
		    attrs.add(bold = new JCheckBox("Bold", false), JCheckBox.CENTER);
		    attrs.add(italic = new JCheckBox("Italic", false), JCheckBox.CENTER);

		    JPanel previewPanel = new JPanel();
		    previewPanel.setLayout(new BorderLayout());
		    previewPanel.setPreferredSize(new Dimension(200, 100));
		    previewPanel.setBackground(Color.DARK_GRAY);
		    
		    previewArea = new JLabel(displayText, JLabel.CENTER);
		    previewArea.setForeground(Color.WHITE);
		    previewPanel.add(previewArea, BorderLayout.CENTER);
		    add(previewPanel);

		    JPanel bot = new JPanel();
		    bot.setBorder(new EmptyBorder(10,5,5,5));
		    bot.setLayout(new GridLayout(1,0));

		    JButton okButton = new JButton("Ok");
		    bot.add(okButton);
		    okButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        previewFont();
		        Application.getInstance().getCanvas().getTools().setFont(getSelectedFont());
		        dispose();
		        setVisible(false);
		      }
		    });

		    JButton pvButton = new JButton("Preview");
		    bot.add(pvButton);
		    pvButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        previewFont();
		      }
		    });

		    JButton canButton = new JButton("Cancel");
		    bot.add(canButton);
		    canButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        dispose();
		        setVisible(false);
		      }
		    });
		    add(bot);
		    previewFont(); 
		    getRootPane().setDefaultButton(okButton);
		    publish();
		  }
		  protected void previewFont() {
		    resultName = fontNameChoice.getSelectedValue();
		    String resultSizeName = fontSizeChoice.getSelectedValue();
		    int resultSize = Integer.parseInt(resultSizeName);
		    isBold = bold.isSelected();
		    isItalic = italic.isSelected();
		    int attrs = Font.PLAIN;
		    if (isBold)
		      attrs = Font.BOLD;
		    if (isItalic)
		      attrs |= Font.ITALIC;
		    resultFont = new Font(resultName, attrs, resultSize);
		    previewArea.setFont(resultFont);
		  }
		  public String getSelectedName() {
		    return resultName;
		  }
		  public int getSelectedSize() {
		    return resultSize;
		  }
		  public Font getSelectedFont() {
		    return resultFont;
		  }
	}

	public static class PreferencesDialog extends CustomDialog {
		private JCheckBox toolbarCheckbox, canvasCheckbox, statusCheckbox, autosaveCheckbox, persistCheckbox, splashCheckbox;
		private JSpinner updateInput, autoSaveInput;
		private JTabbedPane tabbedPane;
		private GridLayout layout;
		public PreferencesDialog() {
			setTitle("Preferences");
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			layout = new GridLayout(0,2);
			layout.setHgap(5);
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Window", windowSettings());
			tabbedPane.addTab("System", systemSettings());
			contentPanel.add(tabbedPane, BorderLayout.CENTER);
			contentPanel.add(generateButtons());
			add(contentPanel);
			publish();
		}
		private JPanel windowSettings() {
			JPanel window = new JPanel();
			window.setBorder(new EmptyBorder(5,5,5,5));
			window.setLayout(layout);
			JLabel splashLabel = new JLabel("Show Splash Screen");
			splashCheckbox = new JCheckBox();
			splashCheckbox.setSelected(Settings.getInstance().getSerializedSettings().SPLASH);
			window.add(splashLabel);
			window.add(splashCheckbox);
			JLabel toolbarLabel = new JLabel("Toolbar Visible");
			toolbarCheckbox = new JCheckBox();
			toolbarCheckbox.setSelected(Application.getInstance().getToolBarContainer().isVisible());
			window.add(toolbarLabel);
			window.add(toolbarCheckbox);
			JLabel canvasLabel = new JLabel("Canvas Visible");
			canvasCheckbox = new JCheckBox();
			canvasCheckbox.setSelected(Application.getInstance().getCanvasContainer().isVisible());
			window.add(canvasLabel);
			window.add(canvasCheckbox);
			JLabel statusLabel = new JLabel("Status Bar Visible");
			statusCheckbox = new JCheckBox();
			statusCheckbox.setSelected(Application.getInstance().getStatusContainer().isVisible());
			window.add(statusLabel);
			window.add(statusCheckbox);
			return window;
		}
		private JPanel systemSettings() {
			JPanel system = new JPanel();
			system.setBorder(new EmptyBorder(5,5,5,5));
			system.setLayout(layout);
			JLabel updateRate = new JLabel("Set System Update Rate:");
			updateInput = new JSpinner();
			SpinnerNumberModel model = new SpinnerNumberModel(Application.getInstance().getUpdater().getDelay(), 50, 1000, 50);
			updateInput.setModel(model);
			system.add(updateRate);
			system.add(updateInput);
			JLabel persistLabel = new JLabel("Persist data between sessions");
			persistCheckbox = new JCheckBox();
			persistCheckbox.setSelected(Settings.getInstance().getSerializedSettings().PERSIST);
			system.add(persistLabel);
			system.add(persistCheckbox);
			JLabel autoSaveRate = new JLabel("Set Auto Save Timer: (Seconds)");
			autoSaveInput = new JSpinner();
			SpinnerNumberModel model2 = new SpinnerNumberModel(Settings.getInstance().getSerializedSettings().autoSaveTimer / 1000, 1, 60, 1);
			autoSaveInput.setModel(model2);
			system.add(autoSaveRate);
			system.add(autoSaveInput);
			JLabel autosaveLabel = new JLabel("Enable auto save");
			autosaveCheckbox = new JCheckBox();
			autosaveCheckbox.setSelected(Settings.getInstance().getSerializedSettings().AUTOSAVE);
			system.add(autosaveLabel);
			system.add(autosaveCheckbox);
			return system;
		}
		private JPanel generateButtons() {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			buttonPanel.setLayout(new GridLayout(1,0));
			JButton ok = new JButton("Ok");
			ok.addActionListener(e -> {
				Application.getInstance().getToolBarContainer().setVisible(toolbarCheckbox.isSelected());
				Application.getInstance().getCanvasContainer().setVisible(canvasCheckbox.isSelected());
				Application.getInstance().getStatusContainer().setVisible(statusCheckbox.isSelected());
				Settings.getInstance().getSerializedSettings().SPLASH = splashCheckbox.isSelected();
				Settings.getInstance().getSerializedSettings().PERSIST = persistCheckbox.isSelected();
				Settings.getInstance().getSerializedSettings().AUTOSAVE = autosaveCheckbox.isSelected();
				Settings.getInstance().getSerializedSettings().autoSaveTimer = ((int)autoSaveInput.getValue()) * 1000;
				Application.getInstance().getUpdater().setDelay((int)updateInput.getValue());
				dispose();
			});
			buttonPanel.add(ok);
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttonPanel.add(cancel);
			getRootPane().setDefaultButton(ok);
			return buttonPanel;
		}
		
	}

	public static class CloseDialog extends CustomDialog {
		public CloseDialog(String text, String message) {
			setTitle("Exit");
			add(messageBox(text, message), BorderLayout.CENTER);
			add(generateButtons(), BorderLayout.SOUTH);
			publish();
		}
		public CloseDialog(String text) {
			setTitle("Exit");
			add(textBox(text), BorderLayout.CENTER);
			add(generateButtons(), BorderLayout.SOUTH);
			publish();
		}
		private JPanel generateButtons() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1,0));
			panel.setBorder(new EmptyBorder(5,5,5,5));
			JButton ok = new JButton("Ok");
			ok.addActionListener(e -> {
				Settings.getInstance().serializeSettings();
				System.exit(0);
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			panel.add(ok);
			panel.add(cancel);
			getRootPane().setDefaultButton(ok);
			return panel;
		}
		private JPanel messageBox(String text, String message) {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0,1));
			panel.setBorder(new EmptyBorder(10, 5, 5, 5));
			JLabel sit = new JLabel(text, JLabel.CENTER);
			JLabel rep = new JLabel(message, JLabel.CENTER);
			panel.add(sit);
			panel.add(rep);
			return panel;
		}
		private JPanel textBox(String text) {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0,1));
			panel.setBorder(new EmptyBorder(10, 5, 5, 5));
			JLabel sit = new JLabel(text, JLabel.CENTER);
			panel.add(sit);
			return panel;
		}
		
	}

	public static class LayerDialog extends CustomDialog {
		public LayerDialog() {
			setTitle("Add Layer");
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(5,5,5,5));
			panel.setLayout(new GridLayout(0,1));
			JLabel help = new JLabel("Enter Description:");
			help.setHorizontalAlignment(JLabel.CENTER);
			JTextField jtf = new JTextField();
			jtf.setText("New Layer");
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			buttonPanel.setLayout(new GridLayout(1,0));
			JButton button = new JButton("Ok");
			button.addActionListener(e -> {
				if (!jtf.getText().equals("")) ImageFactory.createLayer(Application.getInstance().getCanvas().getTools().getImageIndex(), jtf.getText(), true);
				Application.getInstance().reloadProject();
				dispose();
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttonPanel.add(button);
			buttonPanel.add(cancel);
			panel.add(help);
			panel.add(jtf);
			panel.add(buttonPanel);
			add(panel);
			getRootPane().setDefaultButton(button);
			publish();
		}
		public LayerDialog(int imageIndex, int layerIndex) {
			setTitle("Rename Layer");
			Element layer = Storage.getInstance().getLayer(imageIndex, layerIndex);
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(5,5,5,5));
			panel.setLayout(new GridLayout(0,1));
			JLabel help = new JLabel("Enter Description:");
			help.setHorizontalAlignment(JLabel.CENTER);
			JTextField jtf = new JTextField();
			jtf.setText(layer.getAttribute("title"));
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			buttonPanel.setLayout(new GridLayout(1,0));
			JButton button = new JButton("Ok");
			button.addActionListener(e -> {
				if (!jtf.getText().equals("")) {
					layer.setAttribute("title", jtf.getText());
					Application.getInstance().reloadProject();
				}
				dispose();
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttonPanel.add(button);
			buttonPanel.add(cancel);
			panel.add(help);
			panel.add(jtf);
			panel.add(buttonPanel);
			add(panel);
			getRootPane().setDefaultButton(button);
			publish();
		}
	}

	public static class AboutDialog extends CustomDialog {
		public AboutDialog() {
			setTitle("About eXtensible");
			
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			
			JPanel aboutPanel = new JPanel();
			aboutPanel.setLayout(new GridLayout(0,1));
			aboutPanel.add(new JLabel("Version: eXtensible Release Candidate 1", JLabel.CENTER), BorderLayout.NORTH);
			aboutPanel.add(new JLabel("Developed by: \n Håkon Torgersen", JLabel.CENTER), BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			buttonPanel.setLayout(new GridLayout(1,1));
			JButton close = new JButton("Ok");
			close.addActionListener(e -> {
				dispose();
			});			
			buttonPanel.add(close);
			contentPanel.add(aboutPanel);
			contentPanel.add(buttonPanel);
			
			add(contentPanel);
			getRootPane().setDefaultButton(close);
			publish();
		}
	}

	public static class GridDialog extends CustomDialog {
		public GridDialog() {
			setTitle("Create Grid");
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new GridLayout(0,1));
			JPanel widthPanel = new JPanel();
			widthPanel.setLayout(new GridLayout(0,2));
			JLabel widthLabel = new JLabel("Columns:", SwingConstants.CENTER);
			JSpinner widthInput = new JSpinner();
			SpinnerModel model = new SpinnerNumberModel(1, 1, 1000,  1);  
			widthInput.setModel(model);
			widthPanel.add(widthLabel);
			widthPanel.add(widthInput);
			contentPanel.add(widthPanel);
			JPanel heightPanel = new JPanel();
			heightPanel.setLayout(new GridLayout(0,2));
			JLabel heightLabel = new JLabel("Rows:", SwingConstants.CENTER);
			JSpinner heightInput = new JSpinner();
			model = new SpinnerNumberModel(1, 1, 1000, 1);
			heightInput.setModel(model);
			heightPanel.add(heightLabel);
			heightPanel.add(heightInput);
			contentPanel.add(heightPanel);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(0, 2));
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			JButton confirm = new JButton("Create Grid");
			confirm.addActionListener(e -> {
				Application.getInstance().getRuler(Ruler.HORIZONTAL).clearGrid();
				Application.getInstance().getRuler(Ruler.VERTICAL).clearGrid();
				int heightValue = (Integer) heightInput.getValue();
				int widthValue = (Integer) widthInput.getValue();
				if (heightValue != 0 && widthValue != 0) {
					int width = Integer.parseInt(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("width"));
					int height = Integer.parseInt(Storage.getInstance().getImage(Application.getInstance().getCanvas().getTools().getImageIndex()).getAttribute("height"));
					for (int i = 0; i < widthValue; i++) {
						int columnWidth = width / widthValue; 
						Application.getInstance().getRuler(Ruler.HORIZONTAL).addGrid(columnWidth * i);
					}
					for (int i = 0; i < heightValue; i++) {
						int rowHeight = height / heightValue; 
						Application.getInstance().getRuler(Ruler.VERTICAL).addGrid(rowHeight * i);
					}
					Application.getInstance().repaintCanvas();
					dispose();
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttonPanel.add(confirm);
			buttonPanel.add(cancel);
			contentPanel.add(buttonPanel);
			add(contentPanel);
			getRootPane().setDefaultButton(confirm);
			publish();
		}
	}

	public static class BitmapDialog extends CustomDialog implements ListSelectionListener {
		private JList bitmapList;
		private JButton image;
		private ImageIcon icon;
		public BitmapDialog() {
			setTitle("Select Bitmap");
			setAlwaysOnTop(false);
			JPanel contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5,5,5,5));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			JPanel listPanel = new JPanel();
			listPanel.setPreferredSize(new Dimension(400,200));
			listPanel.setLayout(new GridLayout(0,2));
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (int i = 0; i < Storage.getInstance().getBitmaps().getChildNodes().getLength(); i++) {
				Element bitmap = (Element) Storage.getInstance().getBitmaps().getChildNodes().item(i);
				model.addElement(bitmap.getAttribute("path"));
			}
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			bitmapList = new JList(model);
			scrollPane.setViewportView(bitmapList);
			bitmapList.setBackground(listPanel.getBackground());
			bitmapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			image = new JButton("Click to Select Bitmap");
			image.setHorizontalTextPosition(JButton.CENTER);
			image.setVerticalTextPosition(JButton.BOTTOM);
			image.addActionListener(e -> {
				String path = (String) bitmapList.getSelectedValue();
				Application.getInstance().getDrawing().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			    Application.getInstance().getCanvas().getTools().setBitmap(Parser.parseBitmap(Storage.getInstance().getBitmap(path).getTextContent()), path);
			    Application.getInstance().getCanvas().getTools().setTool(Tools.Tool.BITMAP);
			    dispose();
			});
			bitmapList.addListSelectionListener(this);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,0,0,0));
			buttonPanel.setLayout(new GridLayout(0,2));
			JButton cancelBtn = new JButton("Cancel");
			cancelBtn.addActionListener(e -> {
				dispose();
			});
			JButton importBtn = new JButton("Import Bitmap");
			importBtn.addActionListener(e -> {
				dispose();
				Functions.routeFunction("Import Bitmap");
			});
			listPanel.add(scrollPane);
			listPanel.add(image);
			buttonPanel.add(importBtn);
			buttonPanel.add(cancelBtn);
			contentPanel.add(listPanel);
			contentPanel.add(buttonPanel);
			add(contentPanel);
			publish();
			if (!model.isEmpty()) {	
				bitmapList.setSelectedIndex(0);
				icon =  new ImageIcon(Resource.getInstance().resizeBitmap(Parser.parseBitmap(Storage.getInstance().getBitmap(model.get(0)).getTextContent()), image.getWidth() - 20, image.getHeight() - 35));
				image.setIcon(icon);
			}
			else {
				image.setText("Import a Bitmap to Project");
				image.setEnabled(false);
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				String path = bitmapList.getSelectedValue().toString();
				icon = new ImageIcon(Resource.getInstance().resizeBitmap(Parser.parseBitmap(Storage.getInstance().getBitmap(path).getTextContent()), image.getWidth() - 20, image.getHeight() - 35));
				image.setIcon(icon);
			}
			
		}
	}
}
	

