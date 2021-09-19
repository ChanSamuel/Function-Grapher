package sGUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class DrawArea extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private List<Shape> shapes = new ArrayList<Shape>();
	SimpleGUI GUI;
	
	public DrawArea(SimpleGUI gui) {
		this.GUI = gui;
	}
	
	/*
	 * 
	 * This implementation is not good at all for heavy graphics operations.
	 * (it repaints the entire screen if anything at all changes)
	 * 
	 * This implementation enables interacting with individual shapes that have been drawn 
	 * behind other shapes (move/removing etc)- which can be handy. 
	 * However, this means you are obliged to regularly clear the backlog of unused shapes with 
	 * repeating animations. So, it is not good for animations.
	 * 
	 */
	
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		super.paintComponent(g2D);
		// Make the background white
		Color col = g2D.getColor();
		g2D.setColor(Color.white);
		g2D.fillRect(0, 0, getWidth(), getHeight());
		g2D.setColor(col);
		g2D.setStroke(new BasicStroke(GUI.getLineWidth()));
		
		// Then draw all the shapes on top of the background
		if (shapes.isEmpty()) {return;}
		for (int i = 0; i < shapes.size(); i++) {
			Shape s = shapes.get(i);
			if (s.isFillShape()) {
				s.fill(g2D);
			} else {s.draw(g2D);}
		}
	}
	
	public void addShape(Shape s) {shapes.add(s);}
	
	public void clearShapes() {shapes.clear();}
	
	public void removeShape(Shape s) {shapes.remove(s);}
	
	
}




