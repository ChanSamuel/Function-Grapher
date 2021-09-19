package expPlotter;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Scanner;
import sGUI.SimpleGUI;
import sGUI.Shape;

public class ExpressionPlotter {
		SimpleGUI GUI;
		Shape axises;
		
		private int gridSize = 500;
		private int center = gridSize/2;
		
		private final double ZOOM_FACTOR = 2;
		private double scale = 1;
		
	public ExpressionPlotter() {
		createAndSetupGUI();
	}
	
	public void createAndSetupGUI() {
		GUI = SimpleGUI.setupGUI();
		GUI.setFrameName("Expression Plotter");
		
		ArrayList<Shape> shapeSequence = new ArrayList<Shape>();
		
		// Y-axis
		shapeSequence.add(GUI.createLineShape(center, gridSize, center, 0));
		// X-axis
		shapeSequence.add(GUI.createLineShape(0, center, gridSize, center));
		
		axises = Shape.layer(shapeSequence);
		GUI.drawShape(axises, 0, 0);
		
		GUI.addButton("Scale up", 20, () -> zoomIn());
		GUI.addButton("Scale down", 10, () -> zoomOut());
		
		GUI.addTextFieldListener(() -> this.run());
	}
	
	public void zoomIn() {
		scale /= ZOOM_FACTOR;
		run();
	}
	
	public void zoomOut() {
		scale *= ZOOM_FACTOR;
		run();
	}
	
	public void updateAxis() {
		// Clear the draw area and redraw the axis with updated labels.
		GUI.clear();
		GUI.drawShape(axises, 0, 0);
		
		// Update axis marks and number labels.
		for (int i = 0; i <= gridSize; i+=50) {
			double label = (i-center)*scale;
			label = (double)Math.round(label * 1e3d) / 1e3d;
			
			// X-axis
			GUI.drawLine(i, center + 2, i, center - 2);
			GUI.drawString(String.valueOf(label), i, center + 20, Color.black);
			
			// Y-axis
			GUI.drawLine(center+2, i, center-2, i);
			GUI.drawString(String.valueOf(-label), center + 20, i, Color.black);
		}
	}
	
	public void run() {
		String exp;
		exp = GUI.getTextFieldTxt();
		exp = exp.replaceAll(" ", ""); // Strip space characters
		Scanner scan = new Scanner(exp);
		scan.useDelimiter("");
		try {
			// Clear the last iteration's chosen variable.
			Parser.recognizedVar = null;
			
			ParseNode root = parseExp(scan);
			GUI.println(exp);
			
			// Clear the points from last iteration and update axis labels.
			updateAxis();
			
			// Plot the points relative to the center.
			double previousY = 0;
			for (int x =- center;x <= gridSize; x++) {
				double y = root.evaluate(x*scale) / scale;
				
				
				if (center-y > gridSize || x+center > gridSize) { // Don't draw beyond the axises.
					previousY = y;
					continue;
				}
				
				
				if (x != -center) {
					GUI.drawLine(x+center, center-y, x+center - 1, center-previousY);
				} else {
					GUI.drawLine(x+center, center-y, x+center, center-y);
				}
				previousY = y;
				
			}
			
		} catch (ParseFailedException e) {
			GUI.println(e.getMessage());
		}
		scan.close();
	}
	
	private static ParseNode parseExp(Scanner s) {
		return new ExpNode().parse(s);
	}

	public static void main(String[] args) {
		ExpressionPlotter expPlotter = new ExpressionPlotter();
	}

}
