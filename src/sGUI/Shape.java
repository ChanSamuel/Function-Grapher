package sGUI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Shape contains a name, position, and methods to draw it on the graphics pane.
 * There several children of Shape included in this java file:
 * Rectangle, Oval, Line, ImageShape, and StringShape.
 * 
 * Position:
 * Shape contains the x1 and y1 fields which dictate where the Shape will be placed on the graphics pane.
 * For the shapes: Rectangle, Oval, ImageShape, and StringShape, this x1 and y1 will be the center of the shape.
 * For the shapes: Line, and the shapes returned by Shape.layer(...), x1 and y1 will be (respectively): 
 * One of the points on either end of the line, and the top left-corner of the custom shape.
 * 
 * Behaviour with SimpleGUI:
 * When it is time to draw this Shape on the graphics pane, SimpleGUI will check whether this Shape is a
 * fill shape or not using Shape.isFillShape(...). If this is true, then SimpleGUI will call Shape.fill(...).
 * If this is false, then Shape.draw(...) will be called.
 * 
 * The return value of Shape.isFillShape(...) controls whether Shape.draw(...) or Shape.fill(...) will be used to draw this
 * Shape on the graphics pane.
 * 
 * @author Sam Chan
 */
public abstract class Shape {
	
	/**
	 * Name of this Shape.
	 */
	protected String name;
	
	/**
	 * Horizontal position of this Shape on the graphics pane.
	 * See this class' javadoc for more details on what this means.
	 */
	protected int x1;
	
	/**
	 * Vertical position of this Shape on the graphics pane.
	 * See this class' javadoc for more details on what this means.
	 */
	protected int y1;
	
	/**
	 * Construct a Shape.
	 * @param name
	 * @param x1 : horizontal position of this Shape on the graphics pane.
	 * @param y1 : vertical position of this Shape on the graphics pane.
	 */
	public Shape(String name, int x1, int y1) {
		this.name = name;
		this.x1 = x1;
		this.y1 = y1;
	}
	
	/**
	 * The purpose of draw(...) is to draw a non-filled version of the shape.
	 * 
	 * When it is time to draw this Shape on the graphics pane, SimpleGUI will check whether this Shape is a
	 * fill shape or not using Shape.isFillShape(). If this is true, then SimpleGUI will call Shape.fill(), if not, 
	 * then Shape.draw() will be called.
	 * 
	 * @param g
	 */
	public abstract void draw(Graphics2D g);
	
	/**
	 * Returns whether or not this Shape is a 'fill shape' or not.
	 * Fill shapes are drawn on the graphics pane using this Shape's Shape.fill(...) method.
	 * This is in contrast to 'non fill shapes', which are drawn with Shape.draw(...).
	 * 
	 * The return value of this method controls whether Shape.draw() or Shape.fill() will be used to draw this
	 * Shape on the graphics pane.
	 * 
	 * @return whether this Shape is a fill shape or not.
	 */
	public boolean isFillShape() {return false;}
	
	/**
	 * Sets the position of this Shape.
	 * @param x1
	 * @param y1
	 */
	public void setPosition(int x1, int y1) {
		this.x1 = x1;
		this.y1 = y1;
	}
	
	
	/**
	 * The purpose of fill(...) is to draw a filled version of the shape drawn by Shape.draw(...).
	 * 
	 * When it is time to draw this Shape on the graphics pane, SimpleGUI will check whether this Shape is a
	 * fill shape or not using Shape.isFillShape(). If this is true, then SimpleGUI will call Shape.fill(), if not, 
	 * then Shape.draw() will be called.
	 * 
	 * If this method is left unimplemented then by default this method calls Shape.draw().
	 * 
	 * @param g
	 */
	public void fill (Graphics2D g) {draw(g);}
	
	/* **************************
	 * Accessor methods for Shape
	 * **************************
	 */
	
	/**
	 * Returns the name of this Shape.
	 * @return Shape name.
	 */
	public String getShapeName() {return name;}
	
	/**
	 * Returns the x1 coordinate for this Shape.
	 * See this class' Javadoc for more information about coordinates.
	 * @return x1
	 */
	public int x1() {return x1;}
	/**
	 * Returns the y1 coordinate for this Shape.
	 * See this class' Javadoc for more information about coordinates.
	 * @return y1
	 */
	public int y1() {return y1;}
	
	/**
	 * Takes in a list of shapes 'shapeSequence' and layers the shapes on top of eachother.
	 * 
	 * The first element in shapeSequence goes on the first layer. The last element goes on the last layer.
	 * Shapes on later layers will draw over those on earlier layers.
	 * 
	 * Example:
	 * If we construct a Rectangle shape object with the x1,y1 parameters (100,100) then pass it into layer(List<Shape>), 
	 * The returned custom shape when drawn at (0,0) will draw the Rectangle at (100,100).
	 * When drawn at (0,10), the Rectangle is drawn at (100, 110).
	 * 
	 * The returned custom shape's fill() and draw() methods will do the same thing.
	 * 
	 * @param shapeSequence
	 * @return A custom shape.
	 */
	public static Shape layer(List<Shape> shapeSequence) {
		// First we create a shallow copy of shapeSequence
		ArrayList<Shape> shapeSequenceCopy = new ArrayList<Shape>(shapeSequence);
		// Now we create a map of where all the shapes should be drawn relative to (0,0) which is the default position and customShape's center
		Map<Shape, int[]> positionsTemplate = new HashMap<Shape, int[]>();
		for (Shape s : shapeSequenceCopy) {
			int[] pos = {s.x1(), s.y1()};
			positionsTemplate.put(s, pos);
		}
		
		// Next we create the object.
		Shape customShape = new Shape("CUSTOM", 0, 0) {
			
			// By default, Shape.fill() calls draw(), so we don't need to implement Shape.fill().
			
			@Override
			public void draw(Graphics2D g) {
				for (Shape s : shapeSequenceCopy) {
					s.setPosition(positionsTemplate.get(s)[0] + this.x1(), positionsTemplate.get(s)[1] + this.y1());
					if (s.isFillShape()) {s.fill(g);}
					else {s.draw(g);}
				}
			}
			
			@Override
			public boolean isFillShape() {
				return true; // Our customShape is always a fill by default.
			}

		};
		
		return customShape;
	}
}

final class Rectangle extends Shape{
	private int width, height;
	private Color fillCol, lineCol;
	private boolean fillShape;
	
	/**
	 * Construct a Rectangle of a given position and size.
	 * The parameters x1, y1 specify the coordinates of the center of this Rectangle.
	 * 
	 * If parameter fillShape is true, then fillCol must not be null and lineCol should ideally be set to null.
	 * Otherwise, lineCol must not be null and fillCol should ideally be set to null.
	 * However, nothing bad will happen if we do not ideally set the other to null.
	 * 
	 * @param x1 : Horizontal coordinate of the center of this Rectangle.
	 * @param y1 : Vertical coordinate of the center of this Rectangle.
	 * @param width : Width of this Rectangle.
	 * @param height : Height of this Rectangle.
	 * @param lineCol : Colour of the border of this non-filled Rectangle.
	 * @param fillCol : Colour of the fill of this filled Rectangle.
	 * @param fillShape : Whether this Rectangle will be filled or not.
	 */
	public Rectangle(int x1, int y1, int width, int height, Color lineCol, Color fillCol, boolean fillShape) {
		super("Rectangle", x1, y1);
		
		if (fillShape) {
			if (fillCol == null) throw new Error("fillCol cannot be null if fillShape is true!");
			this.fillCol = fillCol;
		} else {
			if (lineCol == null) throw new Error("lineCol cannot be null if fillShape is false!");
			this.lineCol = lineCol;
		}
		
		this.fillShape = fillShape;
		this.width = width;
		this.height = height;
		
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(lineCol);
		g.drawRect(x1, y1, width, height);
	}
	
	@Override
	public void fill (Graphics2D g) {
		g.setColor(fillCol);
		g.fillRect(x1, y1, width, height);
	}
	
	@Override
	public String getShapeName() {return name;}
	@Override
	public boolean isFillShape() {return fillShape;}
}

final class Oval extends Shape{
	private int width, height;
	private Color fillCol, lineCol;
	private boolean fillShape;
	
	public Oval(int x1, int y1, int width, int height, Color lineCol, Color fillCol, boolean fillShape) {
		super("Oval", x1, y1);
		this.fillShape = fillShape;
		this.width = width;
		this.height = height;
		this.lineCol = lineCol;
		this.fillCol = fillCol;
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(lineCol);
		g.drawOval(x1, y1, width, height);
	}
	
	@Override
	public void fill (Graphics2D g) {
		g.setColor(fillCol);
		g.fillOval(x1, y1, width, height);
	}
	
	@Override
	public String getShapeName() {return name;}
	@Override
	public boolean isFillShape() {return fillShape;}
}

final class Line extends Shape{
	private Color lineCol;
	private final int LENGTH, HEIGHT; // The length of the Horizontal component, and height of the vertical component
	
	public Line(int x1, int y1, int x2, int y2, Color lineCol) {
		super("Line", x1, y1);
		LENGTH = x2 - x1;
		HEIGHT = y2 - y1;
		
		this.lineCol = lineCol;
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(lineCol);
		g.drawLine(x1, y1, x1 + LENGTH, y1 + HEIGHT);
	}

}

final class ImageShape extends Shape{
	private Image img;
	
	public ImageShape(Image img, int x1, int y1) {
		super("Image", x1, y1);
		this.img = img;
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.drawImage(img, x1, y1, null);
	}
	
}

final class StringShape extends Shape{
	private String str;
	private Color lineCol;
	
	public StringShape(String str, int x1, int y1, Color col) {
		super("StringShape", x1, y1);
		this.str = str;
		this.lineCol = col;
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(lineCol);
		g.drawString(str, x1, y1);
	}

}
