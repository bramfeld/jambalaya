/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserEvent;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.UserEvent;
import ca.uvic.csr.shrimp.util.CollectionUtils;

/**
 * This class produces a dialog that allows you to choose your
 * controls for the system.
 *
 * @author Casey Best
 * @date Jan 29, 2001
 */
public class UserControlOptions extends JPanel implements ShrimpOptions {

	private final Icon blankIcon;

	private JButton cancelButton;
	private JButton okButton;
	private boolean okPressed;
	private JButton btnEditEvent;
	private JButton btnAddEvent;
	private JButton btnRestoreEvent;
	private JButton btnRemoveEvent;
	private JTextArea descriptionText;
	private JButton btnDefaults;

	private Vector userActions;
	private Vector originalActions;

	private JList lstActions;
	private DefaultListModel userControlsModel;
	private JList lstEvents;
	private DefaultListModel eventsModel;
	private Frame parent;
	private ShrimpApplication application;

	public UserControlOptions(ShrimpApplication application) {
		super(new BorderLayout());
		this.parent = application.getParentFrame();
		this.application = application;
		this.blankIcon = ResourceHandler.getIcon("icon_blank.gif");
		loadUserActions();

		//arrange actions in alphabetical order
		for (int i = 0; i < userActions.size()-1; i++) {
			for (int j = 0; j < userActions.size()-1-i; j++) {
				UserAction userAction1 = (UserAction)userActions.elementAt(j);
				UserAction userAction2 = (UserAction)userActions.elementAt(j+1);
		    	if (userAction2.getActionName().compareToIgnoreCase(userAction1.getActionName()) < 0) {  // compare the two neighbors
		    		UserAction tmp = userAction1;
		      		userActions.setElementAt(userAction2, j);
		      		userActions.setElementAt(tmp, j+1);
		  		}
		  	}
		}

		//create a panel of controls
		add(getPanelOfControls(), BorderLayout.CENTER);

		populateUserControlsList(0);
	}

	/**
	 * Loads the user actions from the {@link ShrimpApplication}.
	 * Also makes a copy of the user actions these actions in case the user cancels.
	 */
	private void loadUserActions() {
		this.userActions = application.getUserActions();
		// sort the user actions alphabetically by action name
		Collections.sort(userActions);
		this.originalActions = new Vector(userActions.size());
		for (Iterator iterator = this.userActions.iterator(); iterator.hasNext();) {
			UserAction userAction = (UserAction) iterator.next();
			UserAction userActionClone = (UserAction) userAction.clone();
			originalActions.add(userActionClone);
		}
	}

	/**
	 * Clears the user controls list and the triggers list.
	 * Then loads all the user controls.
	 */
	private void populateUserControlsList(int selectedIndex) {
		eventsModel.clear();

		ActionManager mgr = application.getActionManager();

		//populate the list
		userControlsModel.clear();

		for (Iterator iterator = userActions.iterator(); iterator.hasNext();) {
			UserAction tmpUserAction = (UserAction) iterator.next();
			userControlsModel.addElement(tmpUserAction);

			Action a = mgr.getAction(tmpUserAction.getActionName());
			if (a instanceof UserAction) {
				UserAction real = (UserAction) a;
				if (real.getIcon() != null) {
					tmpUserAction.setIcon(real.getIcon());
				}
			}
		}
		lstActions.revalidate();
		// prime the selection
		if (userControlsModel.getSize() > selectedIndex) {
			lstActions.setSelectedIndex(selectedIndex);
		}
	}

	public Vector getUserActions() {
		return userActions;
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#cancel()
	 */
	public void cancel() {
		//return actions to their originals
		userActions.clear();
		userActions.addAll(originalActions);
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#ok()
	 */
	public void ok() {
		application.fireUserControlsChangedEvent();

		//update the shrimp input adapter with any new user actions
		Properties props = application.getProperties();

		for (Iterator iterator = userActions.iterator(); iterator.hasNext();) {
			UserAction userAction = (UserAction) iterator.next();
			//shrimpInputAdapter.addUserAction(userAction);
			Vector userEvents = userAction.getUserEvents();
			props.setProperty(DefaultProjectAction.userActionToPropertiesKeyString(userAction),
					DefaultUserEvent.userEventsToPropertiesString(userEvents));
		}
	}

	private void updateRestoreButton() {
		if (lstActions.getSelectedIndices().length > 0) {
			//populate event list with the events associated with this action.
			UserAction userAction = (UserAction) lstActions.getSelectedValue();
			boolean canRestore = !CollectionUtils.haveSameElements(userAction.getUserEvents(), userAction.getDefaultUserEvents());
			btnRestoreEvent.setEnabled(canRestore);
		} else {
			btnRestoreEvent.setEnabled(false);
		}
	}

	private JPanel getPanelOfControls() {
		JPanel pnlControls = new JPanel(new BorderLayout());
		JPanel pnlMain = new JPanel(new GridLayout(1, 2, 5, 0));
		JPanel pnlActions = new JPanel(new BorderLayout());
		JPanel pnlEvents = new JPanel(new BorderLayout());

		//pnlControls.setPreferredSize(new Dimension(500, 300));
		pnlControls.add(pnlMain, BorderLayout.CENTER);
		pnlMain.add(pnlActions);
		pnlMain.add(pnlEvents);

		userControlsModel = new DefaultListModel();
		lstActions = new JList(userControlsModel);

		// render the action icon too
		lstActions.setCellRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof UserAction) {
					UserAction ua = (UserAction) value;
					label.setIcon(ua.getIcon() != null ? ua.getIcon() : blankIcon);
				}
				return label;
			}
		});
		lstActions.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				listSelectionChanged();
			}
		});

		JScrollPane scrollActions = new JScrollPane(lstActions);
		scrollActions.setPreferredSize(new Dimension(200, 100));

		eventsModel = new DefaultListModel();
		lstEvents = new JList(eventsModel);
		lstEvents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstEvents.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnEditEvent.setEnabled(lstEvents.getSelectedValues().length > 0);
				btnRemoveEvent.setEnabled(lstEvents.getSelectedValues().length > 0);
			}
		});

		JScrollPane scrollEvents = new JScrollPane(lstEvents);
		scrollEvents.setPreferredSize(new Dimension(200, 100));

		JPanel pnlButtons = new JPanel(new GridLayout(6, 1, 0, 2));
		pnlButtons.setMaximumSize(new Dimension(100, 154));
		btnAddEvent = new JButton(new AbstractAction("Add...") {
			public void actionPerformed(ActionEvent e) {
				showCaptureEventDialog(null);
				updateRestoreButton();
			}
		});
		btnAddEvent.setToolTipText("Add a new trigger");
		btnAddEvent.setEnabled(true);

		btnEditEvent = new JButton(new AbstractAction("Edit...") {
			public void actionPerformed(ActionEvent e) {
				showCaptureEventDialog((UserEvent)lstEvents.getSelectedValue());
				updateRestoreButton();
			}
		});
		btnEditEvent.setToolTipText("Edit the selected trigger");
		btnEditEvent.setEnabled(false);

		btnRemoveEvent = new JButton(new AbstractAction("Remove") {
			public void actionPerformed(ActionEvent e) {
				removeSelectedEvents();
			}
		});
		btnRemoveEvent.setToolTipText("Remove the selected trigger");
		btnRemoveEvent.setEnabled(false);

		btnRestoreEvent = new JButton(new AbstractAction("Restore") {
			public void actionPerformed(ActionEvent e) {
				restoreUserEvents();
			}
		});
		btnRestoreEvent.setToolTipText("Restore the original trigger key(s)");
		btnRestoreEvent.setEnabled(false);

		btnDefaults = new JButton(new AbstractAction("Reset All") {
			public void actionPerformed(ActionEvent e) {
				resetAllUserControls();
			}
		});
		btnDefaults.setToolTipText("Resets the shortcut key for every user control back to the default");

		pnlButtons.add(btnAddEvent);
		pnlButtons.add(btnEditEvent);
		pnlButtons.add(btnRestoreEvent);
		pnlButtons.add(btnRemoveEvent);
		pnlButtons.add(Box.createVerticalGlue());
		pnlButtons.add(btnDefaults);

		pnlActions.add(new JLabel("Actions: "), BorderLayout.NORTH);
		pnlActions.add(scrollActions, BorderLayout.CENTER);

		pnlEvents.add(new JLabel("Triggers: "), BorderLayout.NORTH);
		pnlEvents.add(scrollEvents, BorderLayout.CENTER);

		JPanel pnlEast = new JPanel();
		pnlEast.setLayout(new BoxLayout (pnlEast, BoxLayout.Y_AXIS));
		pnlEast.add(Box.createVerticalGlue());
		pnlEast.add(pnlButtons);
		pnlEast.add(Box.createVerticalGlue());
		pnlControls.add(pnlEast, BorderLayout.EAST);

		JPanel descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.setBorder(BorderFactory.createTitledBorder(" Description "));
		descriptionPanel.setPreferredSize(new Dimension(400, 90));
		JScrollPane scroll = new JScrollPane(getDescriptionText(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		getDescriptionText().setBackground(descriptionPanel.getBackground());
		descriptionPanel.add(scroll, BorderLayout.CENTER);

		pnlControls.add(descriptionPanel, BorderLayout.SOUTH);
		pnlControls.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return pnlControls;
	}

	private JTextArea getDescriptionText() {
		if (descriptionText == null) {
			descriptionText = new JTextArea();
			descriptionText.setWrapStyleWord(true);
			descriptionText.setLineWrap(true);
			descriptionText.setEditable(false);
			descriptionText.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		}
		return descriptionText;
	}

	private void listSelectionChanged() {
		if (lstActions.getSelectedIndices().length > 0) {
			//populate event list with the events associated with this action.
			UserAction userAction = (UserAction) lstActions.getSelectedValue();
			Vector userEvents = userAction.getUserEvents();
			eventsModel.clear();
			for (Iterator iterator = userEvents.iterator(); iterator.hasNext();) {
				UserEvent userEvent = (UserEvent) iterator.next();
				eventsModel.addElement(userEvent);
			}
			getDescriptionText().setText(userAction.getToolTip());
			getDescriptionText().setToolTipText(userAction.getToolTip());
			getDescriptionText().setCaretPosition(0);
		}
		updateRestoreButton();
	}

	private void restoreUserEvents() {
		UserAction userAction = (UserAction) lstActions.getSelectedValue();
		userAction.setUserEvents(new Vector(userAction.getDefaultUserEvents()));
		// re-select action to update triggers and enablements
		lstActions.setSelectedIndices(new int[0]);
		lstActions.setSelectedValue(userAction, true);
	}

	private void removeSelectedEvents() {
		UserEvent userEvent = (UserEvent) lstEvents.getSelectedValue();
		((UserAction)lstActions.getSelectedValue()).removeUserEvent(userEvent);
		eventsModel.removeElement(userEvent);
		updateRestoreButton();
	}

	private void resetAllUserControls() {
		String msg = "Are you sure you want to reset all the user controls back to the defaults?";
		int choice = JOptionPane.showConfirmDialog(this, msg, "Reset All?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (JOptionPane.YES_OPTION == choice) {
			// save the current selection index
			int selectedIndex = lstActions.getSelectedIndex();
			// reload the default user controls in the application
			ApplicationAccessor.getApplication().setDefaultUserControls(false);
			// load the user actions into this class
			loadUserActions();
			// fires the user controls changed event and saves the controls to the properties file
			ok();
			// reload all the controls into the list
			populateUserControlsList(selectedIndex);
		}
	}

	private void showCaptureEventDialog(UserEvent eventToEdit) {
		UserAction userAction  = (UserAction) lstActions.getSelectedValue();
		EventCaptureDialog dlgEventCapture = new EventCaptureDialog(eventToEdit);
		dlgEventCapture.setVisible(true);
		if (!dlgEventCapture.accepted()) {
			return;
		}

		UserEvent capturedEvent = dlgEventCapture.getCapturedEvent();
		if (capturedEvent == null) {
			return;
		}

		//check if this temp event is associated with any other actions already
		Vector conflictingActions = new Vector();
		for (int i = 0; i < userActions.size(); i++) {
			UserAction existingAction = (UserAction) userActions.elementAt(i);
			Vector userEvents = existingAction.getUserEvents();
			for (int j = 0; j < userEvents.size(); j++) {
				UserEvent existingEvent = (UserEvent) userEvents.elementAt(j);
				if (capturedEvent.equals(existingEvent) && existingEvent.getAction() != userAction) {
					conflictingActions.add(existingAction);
				}
			}
		}
		boolean conflict = conflictingActions.size() > 0;
		boolean accepted = true;
		if (conflict) {
			String message = "\"" + capturedEvent + "\" is already used by the following action(s):";
			for (Iterator iterator = conflictingActions.iterator(); iterator.hasNext();) {
				UserAction existingAction = (UserAction) iterator.next();
				message += "\n     '" + existingAction + "'";
			}
			message += "\n\nDo you want to associate this event with the '" + userAction.getActionName() + "' action anyway?";
			accepted = (JOptionPane.showConfirmDialog(UserControlOptions.this, message) == JOptionPane.YES_OPTION);
		}

		if (accepted) {
			if (eventToEdit == null) {
				// this is a new event for this action
				eventToEdit = new DefaultUserEvent(userAction);
				userAction.addUserEvent(eventToEdit);
			}
			eventToEdit.setControlRequired(capturedEvent.isControlRequired());
			eventToEdit.setAltRequired(capturedEvent.isAltRequired());
			eventToEdit.setShiftRequired(capturedEvent.isShiftRequired());
			eventToEdit.setMouseActivated(capturedEvent.isMouseActivated());
			eventToEdit.setKeyOrButton(capturedEvent.getKeyOrButton());

			//update list box
			Vector userEvents = userAction.getUserEvents();
			eventsModel.clear();
			for (Iterator iterator = userEvents.iterator(); iterator.hasNext(); ) {
				UserEvent tmpUserEvent = (UserEvent) iterator.next();
				eventsModel.addElement(tmpUserEvent);
			}
		}
	}

	public void setCancelEnabled(boolean enabled) {
		cancelButton.setEnabled(enabled);
	}

	public boolean accepted() {
		return okPressed;
	}

	public class CaptureEventListener implements KeyListener, MouseListener, MouseWheelListener {

		private JLabel eventLabel;
		private UserAction userAction;
		private UserEvent capturedEvent;
		private KeyEvent currentKeyPressedEvent = null;

		private boolean shiftIsDown = false;
		private boolean ctrlIsDown = false;
		private boolean altIsDown = false;
		private boolean mouseActivated = false;
		private int keyOrButton;

		public CaptureEventListener(UserAction userAction, JLabel eventLabel) {
			this.eventLabel = eventLabel;
			this.userAction = userAction;
		}

		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e) {
			if ((currentKeyPressedEvent != null) && (e.getModifiers() == currentKeyPressedEvent.getModifiers()) &&
				(e.getKeyCode() == currentKeyPressedEvent.getKeyCode())) {
				return;
			}

			currentKeyPressedEvent = e;
			//System.out.println ("keyPressed");
			ctrlIsDown = e.isControlDown();
			altIsDown = e.isAltDown();
			shiftIsDown = e.isShiftDown();
			keyOrButton = e.getKeyCode();
			mouseActivated = false;
			//e.consume();
			//setAction();
			String s = KeyEvent.getKeyModifiersText(e.getModifiers());
			if (keyOrButton != KeyEvent.VK_CONTROL &&
				keyOrButton != KeyEvent.VK_ALT &&
				keyOrButton != KeyEvent.VK_SHIFT) {
					if (s.length() >0) {
						s += "+";
					}
					s += KeyEvent.getKeyText(e.getKeyCode());
			}
			eventLabel.setText(s);
		}

		public void keyReleased(KeyEvent e) {
			//System.out.println ("keyReleased");
			ctrlIsDown = e.isControlDown();
			altIsDown = e.isAltDown();
			shiftIsDown = e.isShiftDown();
			keyOrButton = e.getKeyCode();
			mouseActivated = false;
			//e.consume();
			// key can't be just a modifier
			if (keyOrButton != KeyEvent.VK_CONTROL && keyOrButton != KeyEvent.VK_ALT && keyOrButton != KeyEvent.VK_SHIFT) {
				createCapturedEvent();
			}
			currentKeyPressedEvent = null;
		}


		public void mouseClicked(MouseEvent e) {
			int clickCount = e.getClickCount();
			mouseActivated = true;

			if (SwingUtilities.isLeftMouseButton(e) && clickCount == 1) {
				keyOrButton = UserEvent.LEFT_MOUSE_BUTTON;
			} else if (SwingUtilities.isRightMouseButton(e) && clickCount == 1) {
				keyOrButton = UserEvent.RIGHT_MOUSE_BUTTON;
				//JOptionPane.showMessageDialog(UserControlOptions.this, "Sorry, the right mouse button is reserved for popup menus.");
				//return;
			}
			else if (SwingUtilities.isMiddleMouseButton(e) && clickCount == 1) {
				keyOrButton = UserEvent.MIDDLE_MOUSE_BUTTON;
			} else if (SwingUtilities.isLeftMouseButton(e) && clickCount > 1) {
				keyOrButton = UserEvent.DOUBLE_CLICK__LEFT_MOUSE_BUTTON;
			} else if (SwingUtilities.isRightMouseButton(e) && clickCount > 1) {
				keyOrButton = UserEvent.DOUBLE_CLICK__RIGHT_MOUSE_BUTTON;
			} else if (SwingUtilities.isMiddleMouseButton(e) && clickCount > 1) {
				keyOrButton = UserEvent.DOUBLE_CLICK__MIDDLE_MOUSE_BUTTON;
			}
			//e.consume();
			createCapturedEvent();
		}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseWheelMoved(MouseWheelEvent e) {
			// @tag Shrimp.MouseWheel
			mouseActivated = true;
			keyOrButton = (e.getWheelRotation() > 0 ? UserEvent.MOUSE_WHEEL_DOWN : UserEvent.MOUSE_WHEEL_UP);
			createCapturedEvent();
		}

		protected UserEvent getCapturedEvent() {
			return capturedEvent;
		}

		private void createCapturedEvent() {
			// create a temporary event to compare to other events
			capturedEvent = new DefaultUserEvent(userAction);
			capturedEvent.setControlRequired(ctrlIsDown);
			capturedEvent.setAltRequired(altIsDown);
			capturedEvent.setShiftRequired(shiftIsDown);
			capturedEvent.setMouseActivated(mouseActivated);
			capturedEvent.setKeyOrButton(keyOrButton);
			eventLabel.setText(capturedEvent.toString());
			okButton.setEnabled(true);
		}
	}

	/**
	 * Dialog that captures mouse and keyboard events
	 */
	public class EventCaptureDialog extends JDialog {
		private boolean okPressed = false;
		private CaptureEventListener cel;
		private JPanel pnlEventCapture;

		public EventCaptureDialog(UserEvent eventToEdit) {
			super (parent, "Capture Event", true);
			// add the button panel
			JPanel pnlOkCancel = new JPanel();

			// the ok button
			okPressed = false;
			okButton = new JButton("Accept");
			okButton.setEnabled(false);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					okPressed = true;
					dispose();
				}
			});
			pnlOkCancel.add(okButton);

			// the cancel button
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					okPressed = false;
					dispose();
				}
			});
			pnlOkCancel.add(cancelButton);

			pnlEventCapture = new JPanel();
			UserAction userAction  = (UserAction) lstActions.getSelectedValue();
			JLabel lblMessage = new JLabel("Push desired key or mouse button combination.");
			lblMessage.setPreferredSize(new Dimension (280, 20));
			JLabel lblEventText = new JLabel();
			lblEventText.setBorder(BorderFactory.createEtchedBorder());
			lblEventText.setPreferredSize(new Dimension (280, 20));
			if (eventToEdit != null) {
				lblEventText.setText(eventToEdit.toString());
				okButton.setEnabled(true);
			}
			pnlEventCapture.setPreferredSize(new Dimension (300,55));
			pnlEventCapture.add(lblMessage, BorderLayout.NORTH);
			pnlEventCapture.add(lblEventText, BorderLayout.SOUTH);

			cel = new CaptureEventListener(userAction, lblEventText);
			pnlEventCapture.addKeyListener(cel);
			this.addKeyListener(cel);
			this.addMouseListener(cel);
			this.addMouseWheelListener(cel);

			getContentPane().add(pnlOkCancel, BorderLayout.SOUTH);
			getContentPane().add(pnlEventCapture, BorderLayout.NORTH);

			this.pack();

			// put in center of screen
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension screen = toolkit.getScreenSize();
			int x = (int)(screen.getWidth() - getWidth()) / 2;
			int y = (int)(screen.getHeight() - getHeight()) / 2;
			setLocation(x,y);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					EventCaptureDialog.this.requestFocus();
				}
			});

		}
		public UserEvent getCapturedEvent() {
			return cel.getCapturedEvent();
		}

		public boolean accepted() {
			return okPressed;
		}

	}

}