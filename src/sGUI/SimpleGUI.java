package sGUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

public class SimpleGUI {
	private JFrame frame;
	private JPanel buttonPanel;
	private DrawArea graphicsPane;
	private TextField textField;
	private TextArea textArea;
	
	private Color col;
	private int lineWidth = 1;
	
	private boolean inputRecieved;
	private Thread callingThread;
	
	private SimpleGUI() {
		frame = new JFrame();
		buttonPanel = new JPanel();
		graphicsPane = new DrawArea(this);
		textField = new TextField(30);
		textArea = new TextArea(40,30);
		JPanel lhsPanel = new JPanel(); // this is the entire left-hand-side panel
		JPanel textPanel = new JPanel(); // this is the entire right-hand-side panel
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		col = Color.black;
		
		callingThread = Thread.currentThread();
		
		// Everytime the user presses enter in the textField inputRecieved is true, and we notify all waiting 
		// on the callingThread.
		textField.addActionListener((e) -> {
			inputRecieved = true;
			synchronized (callingThread) {callingThread.notifyAll();}
		});
		
		// Adding the text boxes to the textPanel
		textArea.setEditable(false);
		textPanel.add(textField);
		textPanel.add(textAreaScrollPane);
		// Adding lhs components to lhsPanel
		lhsPanel.add(buttonPanel);
		lhsPanel.add(Box.createVerticalGlue());
		
		// Layouts for all the components
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		lhsPanel.setLayout(new BoxLayout(lhsPanel, BoxLayout.Y_AXIS));
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		
		// Set the preferred size for the graphicsPane and let the other panels work around that
		graphicsPane.setPreferredSize(new Dimension(600,600));
		
		lhsPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		
		// Configuring the frame
		frame.setLocation(400, 100); // At some point fix this to be in proportion to screen size rather than constant
		frame.getContentPane().add(lhsPanel);
		frame.getContentPane().add(graphicsPane);
		frame.getContentPane().add(textPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	/*
	 * Concurrency: Whoever owns this object's intrinsic lock can call:
	 * askUserInput(...), getTextFieldTxt(...), sleep(...). 
	 */
	
	/**
	 * Sets the title of the GUI window
	 */
	public void setFrameName(String name) {
		frame.setTitle(name);
	}
	
	public void addTextFieldListener(ButtonFunction f) {
		textField.addActionListener(e -> f.func());
	}
	
	/**
	 * Adds a button with some name and some vertical distance (in pixels) from the component above it
	 */
	public void addButton(String name, int verticalSpacing, ButtonFunction f) {
		JButton button = new JButton();
		button.addActionListener(e -> f.func());
		button.setText(name);
		
		// constant spacing between component before
		buttonPanel.add(Box.createRigidArea(new Dimension(0, verticalSpacing)));
		buttonPanel.add(button);
	}
	
	/**
	 * Clears the graphics pane
	 */
	public void clear() {
		graphicsPane.clearShapes(); // clears the list of shapes to be painted
		// Then, adds the background rectangle to that list of shapes
		redraw();
		// paintComponent will end up setting the color of the Graphics object to the background col, so we must set it back.
		graphicsPane.getGraphics().setColor(col);
	}
	
	/* 
	 * *********************
	 * SHAPE DRAWING METHODS
	 * *********************
	 */
	
	/**
	 * Draws a line from (x1, y1) to (x2, y2)
	 */
	public Shape drawLine(double x1, double y1, double x2, double y2) {
		Shape s = new Line((int)(x1+0.5), (int)(y1+0.5), (int)(x2+0.5), (int)(y2+0.5), col);
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	/**
	 * Draws a rectangle where (x,y) is the top-left corner of the rectangle
	 */
	public Shape drawRect(double x, double y, double width, double height) {
		Shape s = new Rectangle((int) (x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), col, null, false);
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	/**
	 * Draws an oval with a bounding rectangle with left-hand corner at (x,y)
	 */
	public Shape drawOval(double x, double y, double width, double height) {
		Shape s = new Oval((int)(x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), col, null, false);
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	/**
	 * Draws an image where (x,y) is the top-left corner of the image.
	 */
	public Shape drawImg(File imgFile, double x, double y) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(imgFile);
		} catch (IOException e) {
			throw new Error("Failed to read image file!", e);
		}
		Shape s = new ImageShape(img, (int)(x+0.5), (int)(y+0.5));
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	/**
	 * Draws an image where (x,y) is the top-left corner of the image.
	 */
	public Shape drawImg(URL u, double x, double y) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(u);
		} catch (IOException e) {
			throw new Error("Failed to read image file!", e);
		}
		Shape s = new ImageShape(img, (int)(x+0.5), (int)(y+0.5));
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	public Shape drawString(String str, double x, double y, Color col) {
		Shape s = new StringShape(str, (int)(x+0.5), (int)(y+0.5), col);
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	public Shape drawShape(Shape custom, double x, double y) {
		custom.setPosition((int)(x+0.5), (int)(y+0.5));
		graphicsPane.addShape(custom);
		redraw();
		return custom;
	}
	
	public Shape fillRect(double x, double y, double width, double height) {
		Shape s = new Rectangle((int) (x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), null, col, true);
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	public Shape fillOval(double x, double y, double width, double height) {
		Shape s = new Oval((int)(x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), null, col, true);
		graphicsPane.addShape(s);
		redraw();
		return s;
	}
	
	/**
	 * Deletes the shape from the draw area if it exists.
	 * If this method appears to do nothing on the draw area, then the shape likely does not exist on the draw area.
	 */
	public void deleteShape(Shape s) {
		graphicsPane.removeShape(s);
		redraw();
	}
	
	public void setColor(Color col) {
		this.col = col;
	}
	
	/**
	 * Refreshes the graphics area
	 */
	public void redraw() {
		graphicsPane.repaint();
	}
	
	/*
	 * *********************
	 * SHAPE FACTORY METHODS
	 * *********************
	 */
	
	/**
	 * Returns a line Shape from (x1, y1) to (x2, y2)
	 */
	public Shape createLineShape(double x1, double y1, double x2, double y2) {
		Shape s = new Line((int)(x1+0.5), (int)(y1+0.5), (int)(x2+0.5), (int)(y2+0.5), col);
		return s;
	}
	/**
	 * Returns a rectangle Shape where (x,y) is the top-left corner of the rectangle
	 */
	public Shape createRectShape(double x, double y, double width, double height) {
		Shape s = new Rectangle((int) (x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), col, null, false);
		return s;
	}
	
	/**
	 * Returns an oval Shape with a bounding rectangle with left-hand corner at (x,y)
	 */
	public Shape createOvalShape(double x, double y, double width, double height) {
		Shape s = new Oval((int)(x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), col, null, false);
		return s;
	}
	
	/**
	 * Returns an image Shape
	 */
	public Shape createImgShape(File imgFile, double x, double y) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(imgFile);
		} catch (IOException e) {
			throw new Error("Failed to read image file!", e);
		}
		Shape s = new ImageShape(img, (int)(x+0.5), (int)(y+0.5));
		return s;
	}
	
	public Shape createStringShape(String str, double x, double y, Color col) {
		Shape s = new StringShape(str, (int)(x+0.5), (int)(y+0.5), col);
		return s;
	}
	
	public Shape createFilledRectShape(double x, double y, double width, double height) {
		Shape s = new Rectangle((int) (x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), null, col, true);
		return s;
	}
	
	public Shape createFilledOvalShape(double x, double y, double width, double height) {
		Shape s = new Oval((int)(x+0.5), (int)(y+0.5), (int)(width+0.5), (int)(height+0.5), null, col, true);
		return s;
	}
	
	/*
	 * ******************
	 * TEXT PANEL METHODS
	 * ******************
	 */
	
	/**
	 * Asks a question for the user and then waits for the input from the textField.
	 * Once the user has pressed enter in the text field, this method will return the text 
	 * the user has typed in the text field.
	 * @param question 
	 * @return text input
	 */
	public synchronized String askUserInput(String question) {
		inputRecieved = false; // has to be false since we haven't asked the question yet!
		callingThread = Thread.currentThread();
		println(question);
		try {
			synchronized (callingThread) {
				while (!inputRecieved) {
					callingThread.wait();
				}
			}
		} catch (InterruptedException e) {
			throw new Error("GUI was interrupted whilst asking user input", e);
		}
		return getTextFieldTxt();
	}
	
	/**
	 * Gets the input from the text field
	 * @return text field text.
	 */
	public synchronized String getTextFieldTxt() {
		return textField.getText();
	}
	
	/**
	 * Prints to the text area on a new line 
	 * @param txt 
	 */
	public void println(String txt) {
		textArea.append(txt + "\n");
	}
	
	/* *******************
	 * GETTERS AND SETTERS
	 * *******************
	 */
	
	/* 
	 * TODO:
	 * Consider having different locks for the methods for getting/setting line width
	 * and getting/setting textfield text. 
	 */
	
	/**
	 * Gets the line width field of this SimpleGUI object.
	 * In turn, this method is called by the DrawArea object which uses the return value to set
	 * the Graphics2D stroke width.
	 * @return line width
	 */
	public int getLineWidth() {
		return lineWidth;
	}
	
	/**
	 * Sets the line width field of this SimpleGUI object. 
	 * @param width
	 */
	public void setLineWidth(int width) {
		lineWidth = width;
	}
	
	/**
	 * Sleeps the calling thread for x milliseconds.
	 */
	public synchronized void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new Error("Sleep method interrupted!", e);
		}
	}
	
	/**
	 * Sets up the GUI by creating a SimpleGUI object and configuring it to default settings like having a 'clear' button.
	 * Returns the aforementioned SimpleGUI object
	 */
	public static SimpleGUI setupGUI() {
		SimpleGUI guiObj = new SimpleGUI();
		guiObj.setFrameName("GUI");
		guiObj.addButton("Clear", 0, () -> guiObj.clear());
		return guiObj;
	}
	
	public static void main(String[] args) {
        setupGUI();
    }

}

