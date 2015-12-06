/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import ca.uvic.csr.shrimp.util.EscapeDialog;

/**
 * A dialog box for displaying the progress of a task. It displays a subtitle and a note (usually an indication of progress made.)
 */
public class ProgressDialog extends EscapeDialog {

	private static final int TITLE_FONT_SIZE = 14;
	private static final int OTHER_FONT_SIZE = 12;

	private static JLabel lblSubtitle = new JLabel("");
	private static JButton btnCancel = new JButton("Cancel");
	private static JLabel lblNote = new JLabel("");
	private static JPanel pnlContent = new JPanel();
	private static JLabel lblTitle = new JLabel("");

	private static boolean isCancelled = false;
	private static int netShowCount = 0; // how many times asked to show minus how many times asked to hide

	private static boolean doNotShow = false;

	private static ProgressDialog progressDialog;

	/**
	 * @param parent The parent of this dialog.
	 * @param subtitle An initial subtitle.
	 * @param note An initial note.
	 */
	private ProgressDialog(Frame frame, String title) {
		super(frame, title, false);
		setUndecorated(true);
		lblTitle.setSize(new Dimension(360, 20));
		lblTitle.setLocation(new Point(20, 20));
		lblTitle.setVisible(true);

		Font bigFont = new Font(lblTitle.getFont().getName(), Font.BOLD, TITLE_FONT_SIZE);
		Font smallFont = new Font(lblTitle.getFont().getName(), Font.PLAIN, OTHER_FONT_SIZE);
		lblTitle.setFont(bigFont);
		lblTitle.setText(title);

		btnCancel.setSize(new Dimension(90, 30));
		btnCancel.setLocation(new Point(135, 120));
		btnCancel.setVisible(true);

		lblSubtitle.setSize(new Dimension(360, 20));
		lblSubtitle.setLocation(new Point(20, 50));
		lblSubtitle.setVisible(true);
		lblSubtitle.setFont(smallFont);

		lblNote.setSize(new Dimension(360, 20));
		lblNote.setLocation(new Point(20, 80));
		lblNote.setVisible(true);
		lblNote.setFont(smallFont);

		pnlContent.setLayout(null);
		pnlContent.add(lblTitle);
		pnlContent.add(lblSubtitle);
		pnlContent.add(lblNote);
		pnlContent.add(btnCancel);

		pnlContent.setPreferredSize(new Dimension(360, 160));
		pnlContent.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		getContentPane().add(pnlContent);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				btnCancel.doClick();
			}
		});

		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblNote.setText("Cancelling...");
				isCancelled = true;
				// hide this dialog after 1 second
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
							if (netShowCount <= 0) {
								netShowCount = 1;
							}
							tryHideProgress();
						} catch (Exception ignore) {
						}
					}
				}).start();
			}
		});

		pack();

		// put in center of screen
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) (screen.getWidth() - this.getWidth()) / 2;
		int y = (int) (screen.getHeight() - this.getHeight()) / 2;
		setLocation(x, y);

		lblNote.repaint();
		lblSubtitle.repaint();
		progressDialog = this;
	}

	protected void performEscape(KeyEvent e) {
		disposeProgressDialog();
		super.performEscape(e);
	}

	public static void disposeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dispose();
		}
	}

	/**
	 * Creates a progress dialog with the given frame as its parent, and with the given title.
	 * @param frame
	 * @param title
	 */
	public static void createProgressDialog(Frame frame, String title) {
		if (progressDialog != null) {
			progressDialog.dispose();
		}
		progressDialog = new ProgressDialog(frame, title);
	}

	/**
	 * Clears the progress dialog's note and subtitle. Also resets dialog as "not cancelled"
	 */
	private static void reset() {
		lblNote.setText("");
		lblSubtitle.setText("");
		isCancelled = false;
	}

	private static long lastSetNoteTime = System.currentTimeMillis();
	private static long SET_NOTE_INTERVAL = 300; // milliseconds

	/**
	 * Sets the note of this progress dialog Repaints dialog immediately, if forcePaint is true, or if a small amount of time has passed since last
	 * setting of note Nothing will be done if the progress dialog is not visible.
	 *
	 * @param note
	 * @param forcePaint Whether or not to update the note immediately, regardless of time elapsed since last setNote
	 */
	public static void setNote(String note, boolean forcePaint) {
		if (doNotShow) {
			return;
		}
		if (!progressDialog.isVisible()) {
			Exception e = new Exception("Warning: Progress dialog not visible when setting note!");
			StackTraceElement[] stackTrace = e.getStackTrace();
			System.err.println("\n" + e.getMessage());
			if (stackTrace.length > 1) {
				System.err.println(stackTrace[1]);
			}
			if (stackTrace.length > 2) {
				System.err.println(stackTrace[2]);
			}
			return;
		}
		// we don't want to set the note too often, or it slows everything down
		long currentTime = System.currentTimeMillis();
		long interval = currentTime - lastSetNoteTime;
		if (forcePaint || interval > SET_NOTE_INTERVAL) {
			lblNote.setText(note);
			if (SwingUtilities.isEventDispatchThread()) {
				pnlContent.paintImmediately(pnlContent.getBounds()); // paint immediateley so we can at least see some progress
			}
			lastSetNoteTime = System.currentTimeMillis();
		}
	}

	/**
	 * Sets the note of this progress dialog, and repaints dialog immediately. Nothing will be done if the progress dialog is not visible.
	 *
	 * @param note
	 */
	public static void setNote(String note) {
		setNote(note, false);
	}

	/**
	 * Returns the current note on the progress dialog.
	 */
	public static String getNote() {
		return lblNote.getText();
	}

	/**
	 * Sets the subtitle of this progress dialog, and repaints dialog immediately. Nothing will be done if the progress dialog is not visible.
	 * @param subtitle
	 */
	public static void setSubtitle(String subtitle) {
		if (doNotShow) {
			return;
		}
		if (!progressDialog.isVisible()) {
			return;
		}
		lblSubtitle.setText(subtitle);
		if (SwingUtilities.isEventDispatchThread()) {
			pnlContent.paintImmediately(pnlContent.getBounds()); // paint immediateley so we can at least see some progress
		}
	}

	/**
	 * Return the current subtitle on the progress dialog
	 */
	public static String getSubtitle() {
		return lblSubtitle.getText();
	}

	private static Border oldBorder;
	private static Color oldColor;
	private static boolean pressed = false;

	private static long lastCheckedTime = System.currentTimeMillis();
	private static long CHECK_CANCELLED_INTERVAL = 200; // milliseconds

	/**
	 * Returns true if cancel button has been pushed. Returrns false if the ProgressDialog is not visible
	 */
	public static boolean isCancelled() {
		if (progressDialog == null) {
			return false;
		}
		if (doNotShow) {
			return false;
		}
		if (!progressDialog.isVisible()) {
			return false;
		}
		if (isCancelled) {
			return true;
		}

		// see if we've done this very recently because we don't want to do it too often
		long now = System.currentTimeMillis();
		if ((now - lastCheckedTime) > CHECK_CANCELLED_INTERVAL) {
			// check through the queue of posted events to see if the user has
			// clicked on the cancel button
			if (SwingUtilities.isEventDispatchThread()) {
				List postedEvents = new ArrayList();
				while (Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() != null) {
					try {
						AWTEvent nextEvent = Toolkit.getDefaultToolkit().getSystemEventQueue().getNextEvent();
						postedEvents.add(nextEvent);
						if (nextEvent instanceof MouseEvent) {
							MouseEvent mouseEvent = (MouseEvent) nextEvent;
							if (mouseEvent.getClickCount() == 1 && mouseEvent.getComponent().equals(progressDialog)) {
								if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED) {
									if (mouseIsOverCancelButton(mouseEvent)) {
										Dimension size = btnCancel.getSize();
										btnCancel.setBorder(oldBorder);
										btnCancel.setBackground(oldColor);
										btnCancel.paintImmediately(new Rectangle(0, 0, size.width, size.height));
										pressed = false;
										isCancelled = true;
										System.out.println("cancelled");
									}
								} else if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED && !pressed) {
									if (mouseIsOverCancelButton(mouseEvent)) {
										oldBorder = btnCancel.getBorder();
										oldColor = btnCancel.getBackground();
										Color darkerColor = oldColor.darker();
										btnCancel.setBackground(darkerColor);
										btnCancel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
										Dimension size = btnCancel.getSize();
										btnCancel.paintImmediately(new Rectangle(0, 0, size.width, size.height));
										pressed = true;
									}
								}
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				for (Iterator iter = postedEvents.iterator(); iter.hasNext();) {
					AWTEvent event = (AWTEvent) iter.next();
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
				}
			}
		}
		return isCancelled;
	}

	private static boolean mouseIsOverCancelButton(MouseEvent mouseEvent) {
		Rectangle btnBounds = btnCancel.getBounds();
		Insets insets = progressDialog.getInsets();
		Rectangle absBtnBounds = new Rectangle(insets.left + btnBounds.x, insets.top + btnBounds.y, btnBounds.width, btnBounds.height);
		return absBtnBounds.contains(mouseEvent.getPoint());
	}

	/**
	 * Shows the progress dialog if not already visible.
	 */
	public static void showProgress() {
		if (doNotShow) {
			return;
		}

		netShowCount++;
		if (!progressDialog.isVisible() && netShowCount > 0) {
			isCancelled = false; // dialog cannot be consider cancelled if just put on screen
			progressDialog.setVisible(true);
		}
	}

	/**
	 * Hides the progress dialog if this method called the same number of times or more than {@link #showProgress} If the progress dialog is hidden,
	 * it will consider "not cancelled" and the note and subtitle will be cleared.
	 * @see #reset()
	 */
	public static void tryHideProgress() {
		if (progressDialog == null) {
			return;
		}
		if (doNotShow) {
			return;
		}

		netShowCount--;
		if (netShowCount < 0) {
			System.err.println("Progress Dialog is being hidden more times that shown!");
			(new Exception("")).printStackTrace();
			netShowCount = 0;
		}
		if (progressDialog.isVisible() && netShowCount == 0) {
			progressDialog.setVisible(false);
			reset(); // dialog cannot be considered cancelled if it is hidden
		}
	}

	public static void setDoNotShow(boolean doNotShow) {
		ProgressDialog.doNotShow = doNotShow;
	}

	public static boolean isDoNotShow() {
		return ProgressDialog.doNotShow;
	}

	/** for testing * */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		JButton button = new JButton("push this");
		frame.getContentPane().add(button);
		ProgressDialog.createProgressDialog(frame, "Never ending progress! ...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runTest();
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.setSize(200, 200);
		frame.setVisible(true);
	}

	static int count = 0;

	private static void runTest() {
		// final String[] prog = {"|", "/", "--", "\\", "|", "/", "--", "\\"};
		final String[] prog = { "   .", "  . ", " .  ", ".   ", " .  ", "  . ", "   ." };
		count = 0;
		ProgressDialog.reset();
		ProgressDialog.showProgress();
		ProgressDialog.setSubtitle("subtitle");
		int index = 0;
		while (!ProgressDialog.isCancelled()) {
			if (count % 1000 == 0) {
				ProgressDialog.setSubtitle("" + count);
			}
			ProgressDialog.setNote(prog[index++ % prog.length]);
			// try {
			// Thread.sleep(0);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			count++;
		}
		ProgressDialog.tryHideProgress();
	}

}