/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 * 
 * Created on Oct 7, 2002
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * @author Nasir Rather
 *
 * DoubleSlider implements a custom slider user-interface with two sliders.
 * The two sliders are two bounds (lower and upper) and the bar represents 
 * the range between the sliders.
 */
public class DoubleSlider extends JComponent {
	// These contants below control the placements and dimensions
	// some of 'em are dependent on others to maintain the general look

	public static final int MINIMUM_INTERVAL_SIZE = 50;
	
	final int TRACK_PADDING_X = 5;
	final int TRACK_HEIGHT = 20;

	final int SCALE_PADDING_X = 7;
	final int SCALE_PADDING_Y = 5;

	final int SCROLLY_WIDTH = 16;
	final int SCROLLY_HEIGHT = 16;

	final int LEFT_SCROLLY_Y = 0; //TRACK_Y - SCROLLY_HEIGHT + SCALE_PADDING_Y;

	final int TRACK_Y = SCROLLY_HEIGHT - SCALE_PADDING_Y; // 30;
	final int RIGHT_SCROLLY_Y = TRACK_Y + TRACK_HEIGHT - SCALE_PADDING_Y;

	final int BAR_Y = RIGHT_SCROLLY_Y + SCROLLY_HEIGHT + 2;
	final int BAR_HEIGHT = 10;

	final int TICK_WIDTH = 1;
	final int TICK_HEIGHT = TRACK_HEIGHT - (2 * SCALE_PADDING_Y);

	// other constants
	private final int SCROLLY_X_INCREMENT = TRACK_PADDING_X + SCALE_PADDING_X - (SCROLLY_WIDTH / 2);
	private final int TOTAL_MINIMUM_WIDTH = TRACK_PADDING_X +  + TRACK_PADDING_X;
	private final int TOTAL_MINIMUM_HEIGHT = BAR_Y + BAR_HEIGHT;

	// This scrolly controls the lower bound
	Scrolly lowerScrolly;

	// This scrolly controls the upper bound
	Scrolly upperScrolly;

	// bar represents the range between the scrollies, hence should move witht the scrollies
	Bar bar;

	// track is what the scrollies move along, doesn't necessarily have to contain the scale
	Track track;

	// Contains references to the listeners
	Vector listeners = new Vector();

	// These are the minimum and the maximum user set values the slider can have
	int minimum;
	int maximum;
	int deviation;
	
	// This represents the step, interms of the value, a slider takes
	int step;

	// current value of the lowerScrolly
	int lowerBound;
	private int lowerScrollyValue;

	// current value of the upperScrolly
	int upperBound;
	private int upperScrollyValue;

	// The with of the scale, this restricts the movement of the scrollies also
	int scaleWidth;

	// gap between the ticks
	int tickInterval = 1; //  NOTE: To prevent divide by zero error, tickInternal cannot be 0
	int numTicks;
	int numTicksOptomized;

	// this tells you the position at which the mouse was clicked last time
	private int recordedMouseX;
	
	//the dataset associated with the slider.
	Vector data;
	
	Vector colors;

	public DoubleSlider(Vector data, int preferedStep, Vector colors) {
		this.data = data;
		this.colors = colors;
		int min = 0;
		int max = data.size() - 1;
		deviation = min;
		minimum = 0;
		maximum = max - deviation;
		this.step = preferedStep;

		numTicks = (maximum - minimum) / preferedStep + 1;
		
		lowerBound = minimum;
		lowerScrollyValue = minimum;
		upperBound = maximum;
		upperScrollyValue = maximum;

		initialize();
	}

	/**
	 * returns the dataset that associates with the slider
	 */
	public Vector getDataset() {
		return data;
	}
	
	/**
	 * @see javax.swing.JComponent#paint(Graphics)
	 */
	public void paint(Graphics g) {
		super.paint(g);

		// set the size of the track
		track.setSize(getWidth() - TRACK_PADDING_X - TRACK_PADDING_X, TRACK_HEIGHT);
		
	}

	/**
	 * returns the value pointed by the lowerScrolly/leftScrolly.
	 * 
	 * @return int lowerBound
	 */
	public int getLowerBound() {
		return lowerBound + deviation;
	}
	
	private int getInternalLowerBound() {
		return lowerBound;
	}
	
	/**
	 * returns the value pointed by the upperScrolly/rightScrolly.
	 * 
	 * @return int upperBound
	 */
	public int getUpperBound() {
		return upperBound + deviation;

	}
	
	private int getInternalUpperBound() {
		return upperBound;
	}
	

	/**
	 * Returns the minimum value for the bound.
	 */
	public int getMinimum() {
		return minimum;
	}

	/**
	 * Returns the maximum value for the bound.
	 */
	public int getMaximum() {
		return maximum;
	}

	/**
	 * Returns the step, value of each step a scrolly takes.
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Sets the value pointed by the upperScrolly/rightScrolly.
	 * 
	 * @param bound
	 */
	public void setUpperBound(int bound) {
		bound -= deviation;
		
		setUpperScrollyValue(bound);
		upperBound = bound;
		fireUpperBoundChanged();
	}

	/**
	 * Sets the value pointed by the lowerScrolly/leftScrolly.
	 * 
	 * @param bound
	 */
	public void setLowerBound(int bound) {
		bound -= deviation;
		
		setLowerScrollyValue(bound);
		lowerBound = bound;
		fireLowerBoundChanged();
	}

	/**
	 * Adds the listener to this DoubleSlider.
	 * 
	 * @param listener DoubleSliderListener
	 */
	public void addDoubleSliderListener(DoubleSliderListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes the listener from this DoubleSlider.
	 * 
	 * @param listener DoubleSliderListener
	 */
	public void removeDoubleSliderListener(DoubleSliderListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	// set initial values
	// add the necessary listeners
	private void initialize() {
		// set these sizes so that this component appears properly inside 
		// layouts
		setMinimumSize(new Dimension(TOTAL_MINIMUM_WIDTH, TOTAL_MINIMUM_HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, TOTAL_MINIMUM_HEIGHT));
		setPreferredSize(new Dimension(TOTAL_MINIMUM_WIDTH, TOTAL_MINIMUM_HEIGHT));

		lowerScrolly = new Scrolly();
		upperScrolly = new Scrolly();

		lowerScrolly.setSize(SCROLLY_WIDTH, SCROLLY_HEIGHT);
		// use the upside down scrolly
		upperScrolly.setOrientation(upperScrolly.RIGHT);
		upperScrolly.setSize(SCROLLY_WIDTH, SCROLLY_HEIGHT);

		lowerScrolly.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				recordedMouseX = e.getX();
			}

			public void mouseReleased(MouseEvent e) {
				if (lowerBound != lowerScrollyValue) {
					lowerBound = lowerScrollyValue;
					fireLowerBoundChanged();
				}
			}
		});
		lowerScrolly.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				moveLowerScrolly(e.getX() - recordedMouseX);
			}
		});

		upperScrolly.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				recordedMouseX = e.getX();
			}

			public void mouseReleased(MouseEvent e) {
				if (upperBound != upperScrollyValue) {
					upperBound = upperScrollyValue;
					fireUpperBoundChanged();
				}
			}
		});
		upperScrolly.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				moveUpperScrolly(e.getX() - recordedMouseX);
			}
		});

		track = new Track();
		track.setLocation(TRACK_PADDING_X, TRACK_Y);
		//track.setBorder(BorderFactory.createLineBorder(Color.red));

		bar = new Bar();
		bar.setLocation(0, BAR_Y);
		//bar.setBackground(Color.red);

		bar.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				recordedMouseX = e.getX();
			}

			public void mouseReleased(MouseEvent e) {
				if (lowerBound != lowerScrollyValue && upperBound != upperScrollyValue) {
					lowerBound = lowerScrollyValue;
					upperBound = upperScrollyValue;
					fireRangeMoved();
				}
			}
		});
		bar.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				moveBar(e.getX() - recordedMouseX);
			}
		});

		lowerScrolly.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				resizeBar();
			}
		});
		upperScrolly.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				resizeBar();
			}
		});

		add(lowerScrolly);
		add(upperScrolly);

		add(bar);

		add(track);
	}

	private void moveLowerScrolly(int drag) {
		int x = lowerScrolly.getX() - SCROLLY_X_INCREMENT + drag;

		int times = x / Math.max(0,tickInterval);
		int remainder = x % tickInterval;

		if ((2 * remainder) >= tickInterval) {
			times++;
		}

		times = (int) (times * numTicks / (double)numTicksOptomized);
		
		setLowerScrollyValue(times);
	}

	// this changes the lowerBound
	private void setLowerScrollyValue(int value) {
		//if (!isValidValue(value)) {
		//	return;
		//}
		int newX = (int) (value * ((double) numTicksOptomized / (double) numTicks)) * tickInterval;

		newX += SCROLLY_X_INCREMENT;

		if (newX == lowerScrolly.getX()) {
			return;
		} else if (newX < SCROLLY_X_INCREMENT) {
			newX = SCROLLY_X_INCREMENT;
			value = minimum;
		} else if (newX > upperScrolly.getX()) {
			newX = upperScrolly.getX();
			value = getInternalUpperBound();
		}

		lowerScrollyValue = value;

		lowerScrolly.setLocation(newX, lowerScrolly.getY());
	}

	private void moveUpperScrolly(int drag) {
		int x = upperScrolly.getX() - SCROLLY_X_INCREMENT + drag;

		int times = x / (Math.max(0,tickInterval));
		int remainder = x % tickInterval;

		if ((2 * remainder) >= tickInterval) {
			times++;
		}
		
		times = (int) (times * numTicks / (double)numTicksOptomized);
		
		setUpperScrollyValue(times);
	}

	// this changes the upperBound
	private void setUpperScrollyValue(int value) {
		//if (!isValidValue(value)) {
		//	return;
		//}
		int newX = (int) (value * ((double) numTicksOptomized / (double) numTicks)) * tickInterval;
		
		newX += SCROLLY_X_INCREMENT;

		if (newX == upperScrolly.getX()) {
			return;
		} else if (newX > (SCROLLY_X_INCREMENT + scaleWidth)) {
			newX = SCROLLY_X_INCREMENT + scaleWidth;
			value = maximum;
		} else if (newX < lowerScrolly.getX()) {
			newX = lowerScrolly.getX();
			value = getInternalLowerBound();
		}

		upperScrollyValue = value;

		upperScrolly.setLocation(newX, upperScrolly.getY());
	}

	// moves the bar
	private void moveBar(int drag) {
		if (drag < 0) {
			int lowerScrollyX = lowerScrolly.getX();

			moveLowerScrolly(drag);
			drag = lowerScrolly.getX() - lowerScrollyX;
			moveUpperScrolly(drag);
		} else {
			int upperScrollyX = upperScrolly.getX();

			moveUpperScrolly(drag);
			drag = upperScrolly.getX() - upperScrollyX;
			moveLowerScrolly(drag);
		}
		bar.repaint();
	}

	// resizes the bar, this should be called whenever a scrolly moves
	private void resizeBar() {
		bar.setLocation(lowerScrolly.getX(), bar.getY());
		bar.setSize(upperScrolly.getX() + upperScrolly.getWidth() - lowerScrolly.getX(), bar.getHeight());
		bar.repaint();
	}

	// Fire lowerBoundChanged Event
	private void fireLowerBoundChanged() {
		Vector cloneListeners = (Vector) listeners.clone();

		for (int i = 0; i < cloneListeners.size(); i++) {
			((DoubleSliderListener) cloneListeners.get(i)).lowerBoundChanged(new DoubleSliderEvent(this));
		}
	}

	// Fire UpperBoundChanged Event
	private void fireUpperBoundChanged() {
		Vector cloneListeners = (Vector) listeners.clone();

		for (int i = 0; i < cloneListeners.size(); i++) {
			((DoubleSliderListener) cloneListeners.get(i)).upperBoundChanged(new DoubleSliderEvent(this));
		}
	}

	// Fire RangeMoved Event
	private void fireRangeMoved() {
		Vector cloneListeners = (Vector) listeners.clone();

		for (int i = 0; i < cloneListeners.size(); i++) {
			((DoubleSliderListener) cloneListeners.get(i)).rangeMoved(new DoubleSliderEvent(this));
		}
	}

	// gui for a scrolly
	class Scrolly extends JComponent {
		final int LEFT = 1;
		final int RIGHT = 2;

		int orientation = LEFT;

		public void setOrientation(int orientation) {
			this.orientation = orientation;
		}

		public void paint(Graphics g) {
			Polygon polygon = new Polygon();
			Polygon polygon2 = new Polygon();

			if (orientation == LEFT) {
				polygon.addPoint(1, 1);
				polygon.addPoint(getWidth()-1, 1);
				polygon.addPoint((getWidth() / 2), getHeight()-1);
				
				polygon2.addPoint(0, 0);
				polygon2.addPoint(getWidth(), 0);
				polygon2.addPoint((getWidth() / 2), getHeight());
			} else {
				polygon.addPoint((getWidth() / 2), 1);
				polygon.addPoint(getWidth()-1, getHeight()-1);
				polygon.addPoint(1, getHeight()-1);
				
				polygon2.addPoint((getWidth() / 2), 0);
				polygon2.addPoint(getWidth(), getHeight());
				polygon2.addPoint(0, getHeight());
			}

			g.setColor(Color.gray);
			g.fillPolygon(polygon);
			g.setColor(Color.lightGray);
			g.drawPolygon(polygon2);
		}
	}

	// gui for the track
	class Track extends JComponent {
		boolean needPositioning = true;

		public void paint(Graphics g) {
			int oldTickInterval = tickInterval;
			int oldNumTicksOptimized = numTicksOptomized;

			numTicksOptomized = numTicks;
			float divisor = (float) Math.max(1, numTicks - 1) / (float) ((getWidth() - TRACK_PADDING_X - TRACK_PADDING_X) - (2 * SCALE_PADDING_X));
			if(divisor > 0.5) {
				numTicksOptomized = numTicks / (int) ((divisor*2) + 1);
			}
			
			tickInterval = (getWidth() - (2 * SCALE_PADDING_X)) / Math.max(1, numTicksOptomized - 1);
			
//			if(tickInterval < MINIMUM_INTERVAL_SIZE) {
//				tickInterval = MINIMUM_INTERVAL_SIZE;
//				numTicksOptomized = (getWidth() - (2 * SCALE_PADDING_X))/tickInterval;
//			}
			

			int x = SCALE_PADDING_X;
			g.setColor(Color.gray);
			for (int i = 0; i < numTicksOptomized; i++) {
				g.fillRect(x, SCALE_PADDING_Y, TICK_WIDTH, TICK_HEIGHT);
				x += tickInterval;
			}

			scaleWidth = x - tickInterval - SCALE_PADDING_X;

			// Do the initial setup, this is required to set proper scrolly positions
			if (needPositioning) {
				lowerBound = minimum;
				lowerScrolly.setLocation(SCROLLY_X_INCREMENT, LEFT_SCROLLY_Y);

				upperBound = maximum;
				upperScrolly.setLocation(SCROLLY_X_INCREMENT + scaleWidth, RIGHT_SCROLLY_Y);

				bar.setSize(scaleWidth, BAR_HEIGHT);

				needPositioning = false;
			}
			else if(oldTickInterval !=  tickInterval || oldNumTicksOptimized != numTicksOptomized) {
				setLowerScrollyValue(getInternalLowerBound());
				setUpperScrollyValue(getInternalUpperBound());
			}
			
//			// This block here takes care of resizing
//			// Everytime the track's size changes, scrollies' positions are changed accordingly
//			if (oldTickInterval > tickInterval || oldNumTicksOptimized > numTicksOptomized) {
//				System.out.println("track 1");
//				setLowerScrollyValue(getInternalLowerBound());
//				setUpperScrollyValue(getInternalUpperBound());
//			} else if (oldTickInterval < tickInterval || oldNumTicksOptimized < numTicksOptomized) {
//				System.out.println("track 2");
//				setUpperScrollyValue(getInternalUpperBound());
//				setLowerScrollyValue(getInternalLowerBound());
//			}
		}
	}
	
	private class Bar extends JPanel {
		//GradientPaint paint;
		
		private Bar () {
			//updatePaint();	
		}
		
		/*
		private void updatePaint () {
			Point2D pt1 = new Point2D.Float(0,0);
			Point2D pt2 = new Point2D.Float(getWidth(), 0);
			int left = lowerScrollyValue;
			int right = upperScrollyValue;
			Color maxOrdinalColor = (Color)colors.elementAt(right);
			Color minOrdinalColor = (Color)colors.elementAt(left);
			paint = new GradientPaint (pt1, minOrdinalColor, pt2, maxOrdinalColor, false);
			repaint();
		}
		*/

		public void paint(Graphics g) {
			double h = g.getClipBounds().getHeight();
			double y = g.getClipBounds().getY();
			for (int i = lowerScrollyValue; i < upperScrollyValue + 1; i++) {
				Color color = (Color)colors.elementAt(i);	
				g.setColor(color);
				double x = (i - lowerScrollyValue) * tickInterval - 0.5*tickInterval + 0.5*SCROLLY_WIDTH;
				double w = tickInterval;
				g.fillRect((int)x, (int)y, (int)w, (int)h);
			}
		}
	}

	//NOTE: this is for test puposes, please leave it here until this class is stable
	public static void main(String[] args) {
		JFrame f = new JFrame();
		
		int count = 200;
		
		Vector v = new Vector();
		for (int i = 0; i < count; i++) {
			v.add(""+ i);
		}
		
		Vector colors = new Vector ();
		int r = 0;
		int g = 0;
		int b = 0;
		char c = 'b';
		for (int i = 0; i < count; i++) {
			switch(c) {
				case 'b':
					b += 25; if(b>250) { b=0; g+= 25; c ='g'; }
					break;
				case 'g':
					g += 25; if(g>250) { g=0; r+= 25; c ='r'; }
					break;
				case 'r':
					r += 25; if(r>250) { r=0; b+= 25; c ='b'; }
					break;
			}
			
			colors.add(new Color (r,g,b));
		}
		
		final DoubleSlider doubleSlider = new DoubleSlider(v, 1, colors);

		doubleSlider.addDoubleSliderListener(new DoubleSliderListener() {
			public void lowerBoundChanged(DoubleSliderEvent event) {
				System.out.print("\nlowerBoundChanged : ");
				System.out.println(event.getDoubleSlider().getLowerBound() + ", " + event.getDoubleSlider().getUpperBound());
				System.out.println(event.getDoubleSlider().getInternalLowerBound() + ", " + event.getDoubleSlider().getInternalUpperBound());
			}

			public void upperBoundChanged(DoubleSliderEvent event) {
				System.out.print("\nupperBoundChanged : ");
				System.out.println(event.getDoubleSlider().getLowerBound() + ", " + event.getDoubleSlider().getUpperBound());
				System.out.println(event.getDoubleSlider().getInternalLowerBound() + ", " + event.getDoubleSlider().getInternalUpperBound());
			}

			public void rangeMoved(DoubleSliderEvent event) {
				System.out.print("\nrangeMoved : ");
				System.out.println(event.getDoubleSlider().getLowerBound() + ", " + event.getDoubleSlider().getUpperBound());
				System.out.println(event.getDoubleSlider().getInternalLowerBound() + ", " + event.getDoubleSlider().getInternalUpperBound());
			}
		});
		f.getContentPane().add(doubleSlider);

		f.setSize(300, 200);
		f.setLocation(100, 150);
		f.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.setVisible(true);
		
		

		//doubleSlider.setLowerBound(10);
		//doubleSlider.setUpperBound(15);
		
	}
	
	/**
	 * @param colors
	 */
	public void setColors(Vector colors) {
		this.colors = colors;
	}

}