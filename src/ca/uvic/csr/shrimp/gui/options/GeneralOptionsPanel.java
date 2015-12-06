/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.options;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.eclipse.mylar.zest.layouts.algorithms.MotionLayoutAlgorithm;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.MotionLayout;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.adapter.PanelModeChangeAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.usercontrols.ToggleLongToolTipAdapter;
import ca.uvic.csr.shrimp.util.JIntegerTextField;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Displays a panel of general options
 *
 * @author Jeff Michaud, Chris Callendar
 */
public class GeneralOptionsPanel extends JPanel implements ShrimpOptions {

	private static final int DEFAULT_WIDTH = 400;
	private static final int ROW_HEIGHT = 24;

	private Properties properties;
	private Hashtable checkBoxes;

	private JCheckBox chkbxManyChildren;
	private JIntegerTextField txtManyChildren;
	private JLabel lblAnimThreshold;
	private JIntegerTextField txtAnimThreshold;
	private JCheckBox chkbxAnim;
	private JCheckBox chkbxBorderWidth;
	private JPanel checkBoxPanel;

	private JSpinner spinnerDisplacement;
	private JSpinner spinnerRadius;
	private JSpinner spinnerSpeed;
	private JSpinner spinnerTime;
	private JSpinner spinnerBorderWidth;
	private JCheckBox chkbxMotion;
	private ShrimpProject project;
	private DisplayBean displayBean;
	private ShrimpView shrimpView;

    /** Creates new form GeneralOptions */
    public GeneralOptionsPanel(ShrimpProject project, DisplayBean displayBean,
    		ShrimpView shrimpView) {
    	this.project = project;
    	this.displayBean = displayBean;
    	this.shrimpView = shrimpView;
		properties = ApplicationAccessor.getProperties();
        initComponents();
    }

    private void initComponents() {
    	checkBoxes = new Hashtable();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // too many children warning
		initTooManyChildrenOptions();
		initAnimationOptions();
		initMotionOptions();
		initBorderWidthOption();
		initCheckBoxOptions();
    }

	private JLabel createSpacer(int width) {
    	JLabel lbl = new JLabel(" ");
    	Dimension dim = new Dimension(width, 10);
		lbl.setMinimumSize(dim);
		lbl.setPreferredSize(dim);
    	lbl.setMaximumSize(dim);
    	return lbl;
	}

	private void initTooManyChildrenOptions() throws NumberFormatException {
		String thresholdStr = properties.getProperty(DisplayBean.PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING_THRESHOLD,
				DisplayBean.DEFAULT_SHOW_MANY_CHILDREN_WARNING_THRESHOLD);
		int threshold = 200;
		try {
			threshold = Integer.parseInt(thresholdStr);
		} catch (NumberFormatException ignore) {}
		txtManyChildren = new JIntegerTextField(threshold);
		txtManyChildren.setPreferredSize(new Dimension(36, 20));

		// too many children warning threshold enable/disable
		boolean showWarning = "true".equalsIgnoreCase(properties.getProperty(DisplayBean.PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING,
				DisplayBean.DEFAULT_SHOW_MANY_CHILDREN_WARNING));
		chkbxManyChildren = new JCheckBox("Display warning if opening a node with number of children greater than: ");
		chkbxManyChildren.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtManyChildren.setEnabled(chkbxManyChildren.isSelected());
			}
		});
		chkbxManyChildren.setSelected(showWarning);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnl.setBorder(BorderFactory.createEtchedBorder());
		pnl.setPreferredSize(new Dimension(DEFAULT_WIDTH, ROW_HEIGHT));
		pnl.add(chkbxManyChildren);
		pnl.add(txtManyChildren);

		this.add(pnl);
	}

	private void initAnimationOptions() throws NumberFormatException {
		lblAnimThreshold = new JLabel("Disable animation if number of nodes greater than: ", JLabel.LEFT);
		String thresholdStr = properties.getProperty(DisplayBean.PROPERTY_KEY__ANIMATION_THRESHOLD,
				DisplayBean.DEFAULT_ANIMATION_THRESHOLD);
		int threshold = 200;
		try {
			threshold = Integer.parseInt(thresholdStr);
		} catch (NumberFormatException ignore) {}
		txtAnimThreshold = new JIntegerTextField(threshold);
		txtAnimThreshold.setPreferredSize(new Dimension(36, 20));

		JPanel pnlAnimThreshold = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		pnlAnimThreshold.add(createSpacer(22));
		pnlAnimThreshold.add(lblAnimThreshold);
		pnlAnimThreshold.add(txtAnimThreshold);

		// animation enable/disable
		String animateStr = properties.getProperty(DisplayBean.PROPERTY_KEY__USE_ANIMATION,
													"" + DisplayBean.DEFAULT_USING_ANIMATION);
		boolean animate = "true".equalsIgnoreCase(animateStr);
		chkbxAnim = new JCheckBox("Use animation for layouts");
		chkbxAnim.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAnimThreshold.setEnabled(chkbxAnim.isSelected());
				lblAnimThreshold.setEnabled(chkbxAnim.isSelected());
			}
		});
		chkbxAnim.setSelected(animate);

		JPanel pnlAnim = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnlAnim.add(chkbxAnim);

		JPanel pnl = new JPanel(new GridLayout(2, 1, 0, 2));
		pnl.setBorder(BorderFactory.createEtchedBorder());
		pnl.setPreferredSize(new Dimension(DEFAULT_WIDTH, 2 * ROW_HEIGHT));
		pnl.add(pnlAnim);
		pnl.add(pnlAnimThreshold);

		this.add(pnl);
	}

	private void initMotionOptions() {
		// load prefs
		int displacement = MotionLayout.DEFAULT_DISPLACEMENT;
		double radius = MotionLayoutAlgorithm.DEFAULT_RADIUS;
		int speed = MotionLayout.DEFAULT_SPEED;
		long time = MotionLayoutAlgorithm.DEFAULT_TIME;
		try {
			displacement = Integer.parseInt(properties.getProperty(DisplayBean.PROPERTY_KEY__DISPLACEMENT, DisplayBean.DEFAULT_DISPLACEMENT));
		} catch (NumberFormatException ignore) {}
		try {
			radius = Double.parseDouble(properties.getProperty(DisplayBean.PROPERTY_KEY__MOTION_RADIUS, DisplayBean.DEFAULT_MOTION_RADIUS));
		} catch (NumberFormatException ignore) {}
		try {
			speed = Integer.parseInt(properties.getProperty(DisplayBean.PROPERTY_KEY__MOTION_SPEED, DisplayBean.DEFAULT_MOTION_SPEED));
		} catch (NumberFormatException ignore) {}
		try {
			time = Long.parseLong(properties.getProperty(DisplayBean.PROPERTY_KEY__MOTION_TIME, DisplayBean.DEFAULT_MOTION_TIME));
		} catch (NumberFormatException ignore) {}


		boolean useMotion = "true".equals(properties.getProperty(DisplayBean.PROPERTY_KEY__USE_MOTION, "true"));
		chkbxMotion = new JCheckBox("Use motion to highlight the neighborhoods of selected nodes by pressing the Alt key", useMotion);

		JPanel pnl1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		String ttip = "The displacement from the selected node (between 0 and 5) - the higher the displacement value the more nodes moved";
		final JLabel lbl = createLabel("Displacement: ", useMotion, ttip);
		// only allow displacements of [0 ... 5]
		spinnerDisplacement = createSpinner(displacement, 0, 5, 1, useMotion, ttip);
		pnl1.add(createSpacer(22));
		pnl1.add(lbl);
		pnl1.add(spinnerDisplacement);

		ttip = "The radius of the motion between 0.1 and 10";
		final JLabel lbl2 = createLabel("Motion radius: ", useMotion, ttip);
		// only allow radius between [0.1 ... 10]
		spinnerRadius = createSpinner(radius, 0.1, 10, 1, useMotion, ttip);
		pnl1.add(createSpacer(22));
		pnl1.add(lbl2);
		pnl1.add(spinnerRadius);

		JPanel pnl2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ttip = "The speed of the motion between 1 and 10";
		final JLabel lbl3 = createLabel("Motion speed: ", useMotion, ttip);
		// only allow speed between [0 ... 10]
		spinnerSpeed = createSpinner(speed, 0, 10, 1, useMotion, ttip);
		pnl2.add(createSpacer(22));
		pnl2.add(lbl3);
		pnl2.add(spinnerSpeed);

		ttip = "The duration of the motion in milliseconds between 100 and 10000";
		final JLabel lbl4 = createLabel("Motion time (millis): ", useMotion, ttip);
		// only allow time between [100 ... 10000]
		spinnerTime = createSpinner(time, 100, 10000, 500, useMotion, ttip);
		pnl2.add(createSpacer(22));
		pnl2.add(lbl4);
		pnl2.add(spinnerTime);

		chkbxMotion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean sel = chkbxMotion.isSelected();
				lbl.setEnabled(sel);
				spinnerDisplacement.setEnabled(sel);
				lbl2.setEnabled(sel);
				spinnerRadius.setEnabled(sel);
				lbl3.setEnabled(sel);
				spinnerSpeed.setEnabled(sel);
				lbl4.setEnabled(sel);
				spinnerTime.setEnabled(sel);
			}
		});

		JPanel pnlMotion = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnlMotion.add(chkbxMotion);

		JPanel motionPanel = new JPanel(new GridLayout(3, 1, 0, 2));
		motionPanel.setBorder(BorderFactory.createEtchedBorder());
		motionPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, 3 * ROW_HEIGHT));
		motionPanel.add(pnlMotion);
		motionPanel.add(pnl1);
		motionPanel.add(pnl2);

		this.add(motionPanel);
	}

	private void initCheckBoxOptions() {
		// NOTE: these checkbox actions ONLY get updated when the OK button clicked, not when the checkbox is clicked

		//Long Tool Tips
		ToggleLongToolTipAdapter longToolTipAdapter = new ToggleLongToolTipAdapter(project, shrimpView);
		shrimpView.addUserControl(longToolTipAdapter);

		//Arrow Heads
		CheckBoxAction arrowHeadAction = new CheckBoxAction("Use Arrow Heads", project) {
			public void startAction() {
				displayBean.setUsingArrowHeads(isChecked());
				displayBean.repaint();
			}
		};
		arrowHeadAction.setChecked(displayBean.getUsingArrowHeads());

		//Return to previous view dialog
		CheckBoxAction returnToPreviousView = new CheckBoxAction("Display 'Return to Previous View' dialog on startup") {
			public void startAction() {
				properties.setProperty(ShrimpApplication.SHOW_RETURN_TO_PREVIOUS_DIALOG_KEY, "" + isChecked());
			}
		};
		String shownStr = properties.getProperty(ShrimpApplication.SHOW_RETURN_TO_PREVIOUS_DIALOG_KEY);
		returnToPreviousView.setChecked(ShrimpUtils.parseBoolean(shownStr, true));

		//Panel mode change warning dialog
		CheckBoxAction showAllDescendentsWarning = new CheckBoxAction("Display 'Show Panel Mode Change' warning") {
			public void startAction() {
				properties.setProperty(PanelModeChangeAdapter.WARNING_KEY, "" + isChecked());
			}
		};
		shownStr = properties.getProperty(PanelModeChangeAdapter.WARNING_KEY);
		showAllDescendentsWarning.setChecked(ShrimpUtils.parseBoolean(shownStr, true));

		CheckBoxAction showScrollbars = new CheckBoxAction("Show scrollbars in the main Shrimp view (requires re-opening current project)") {
			public void startAction() {
				properties.setProperty(ShrimpApplication.USE_SCROLLPANE, "" + isChecked());
			}
		};
		shownStr = properties.getProperty(ShrimpApplication.USE_SCROLLPANE);
		showScrollbars.setChecked(ShrimpUtils.parseBoolean(shownStr, true));

		CheckBoxAction showPlusIcons = new CheckBoxAction("Show plus icons on nodes that have children") {
			public void startAction() {
				PShrimpNode.RENDER_PLUS_ICON = isChecked();
				properties.setProperty(ShrimpApplication.SHOW_PLUS_ICONS, ""+isChecked());
			}
		};
		shownStr = properties.getProperty(ShrimpApplication.SHOW_PLUS_ICONS);
		showPlusIcons.setChecked(ShrimpUtils.parseBoolean(shownStr, PShrimpNode.RENDER_PLUS_ICON));

		CheckBoxAction showAttachmentIcons = new CheckBoxAction("Show attachment icons on nodes that have attached files") {
			public void startAction() {
				PShrimpNode.RENDER_ATTACHMENT_ICON = isChecked();
				properties.setProperty(ShrimpApplication.SHOW_ATTACHMENT_ICONS, ""+isChecked());
			}
		};
		shownStr = properties.getProperty(ShrimpApplication.SHOW_ATTACHMENT_ICONS);
		showAttachmentIcons.setChecked(ShrimpUtils.parseBoolean(shownStr, PShrimpNode.RENDER_ATTACHMENT_ICON));

		CheckBoxAction showResizeHandles = new CheckBoxAction("Show node resize handles") {
			public void startAction() {
				PShrimpNode.DEFAULT_SHOW_RESIZE_HANDLES = isChecked();
				properties.setProperty(ShrimpApplication.SHOW_RESIZE_HANDLES, ""+isChecked());
			}
		};
		shownStr = properties.getProperty(ShrimpApplication.SHOW_RESIZE_HANDLES);
		showResizeHandles.setChecked(ShrimpUtils.parseBoolean(shownStr, PShrimpNode.DEFAULT_SHOW_RESIZE_HANDLES));

		CheckBoxAction openCloseDoors = new CheckBoxAction("Use animation when opening and closing nodes") {
			public void startAction() {
				PShrimpNode.DEFAULT_ANIMATE_DOORS = isChecked();
				properties.setProperty(ShrimpApplication.ANIMATE_OPEN_CLOSE_DOORS, ""+isChecked());
			}
		};
		shownStr = properties.getProperty(ShrimpApplication.ANIMATE_OPEN_CLOSE_DOORS);
		openCloseDoors.setChecked(ShrimpUtils.parseBoolean(shownStr, PShrimpNode.DEFAULT_ANIMATE_DOORS));

		checkBoxPanel = new JPanel(new GridLayout(0, 1, 5, 2));
		checkBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
									BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		addTwoCheckBoxActions(longToolTipAdapter, arrowHeadAction);
		addTwoCheckBoxActions(showPlusIcons, showResizeHandles);
		addCheckBoxAction(showAttachmentIcons);
		addCheckBoxAction(openCloseDoors);
		addCheckBoxAction(returnToPreviousView);
		addCheckBoxAction(showAllDescendentsWarning);
		addCheckBoxAction(showScrollbars);

		this.add(checkBoxPanel);
	}

	private void initBorderWidthOption() {
		String prompt = "Override default border width?";
		final int MAX_DISPLAY_BORDER_WIDTH = 10;
		chkbxBorderWidth = new JCheckBox(prompt);
		String overrideDefaultStr = properties.getProperty(
				DisplayBean.PROPERTY_KEY__OVERRIDE_DEFAULT_NODE_BORDER_WIDTH,
				DisplayBean.DEFAULT_OVERRIDE_DEFAULT_NODE_BORDER_WIDTH);
		boolean overrideDefault = "true".equalsIgnoreCase(overrideDefaultStr);
		chkbxBorderWidth.setSelected(overrideDefault);

		String currentWidthMultiplierStr = properties.getProperty(
				DisplayBean.PROPERTY_KEY__BORDER_WIDTH_MULTIPLIER,
				DisplayBean.DEFAULT_BORDER_WIDTH_MULTIPLIER);
		int currentWidthMultiplier = Integer.parseInt(DisplayBean.DEFAULT_BORDER_WIDTH_MULTIPLIER);
		try {
			currentWidthMultiplier = Integer.parseInt(currentWidthMultiplierStr);
		} catch (NumberFormatException ignore) {}

		String toolTip = "The multiplier applied to the outer and inner node border width.";
		final JLabel lbl = new JLabel("Node Border Width Multiplier: ");
		lbl.setEnabled(overrideDefault);
		lbl.setToolTipText(toolTip);
		SpinnerNumberModel model = new SpinnerNumberModel(currentWidthMultiplier, 1, MAX_DISPLAY_BORDER_WIDTH, 1);
		spinnerBorderWidth = new JSpinner(model);
		spinnerBorderWidth.setEnabled(overrideDefault);
		spinnerBorderWidth.setToolTipText(toolTip);

		chkbxBorderWidth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				properties.setProperty(DisplayBean.PROPERTY_KEY__OVERRIDE_DEFAULT_NODE_BORDER_WIDTH,
						"" + chkbxBorderWidth.isSelected());
				lbl.setEnabled(chkbxBorderWidth.isSelected());
				spinnerBorderWidth.setEnabled(chkbxBorderWidth.isSelected());
			}
		});

		JPanel pnlBorder = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnlBorder.add(chkbxBorderWidth);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnl.add(lbl);
		pnl.add(spinnerBorderWidth);

		JPanel borderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		borderPanel.setBorder(BorderFactory.createEtchedBorder());
		borderPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, ROW_HEIGHT));
		borderPanel.add(pnlBorder);
		borderPanel.add(pnl);

		this.add(borderPanel);
	}


	private JLabel createLabel(String text, boolean enabled, String ttip) {
		JLabel label = new JLabel(text + " ", JLabel.RIGHT);
		label.setEnabled(enabled);
		label.setToolTipText(ttip);
		label.setPreferredSize(new Dimension(120, 20));
		return label;
	}

	private JSpinner createSpinner(double val, double min, double max, double step, boolean enabled, String tooltip) {
		SpinnerNumberModel model = new SpinnerNumberModel(val, min, max, step);
		JSpinner spinner = new JSpinner(model);
		spinner.setEnabled(enabled);
		spinner.setToolTipText(tooltip);
		int size = Double.toString(max).length();
		int width = size * 8;
		spinner.setPreferredSize(new Dimension(Math.max(35, width), 20));
		return spinner;
	}

	private int getSpinnerIntValue(JSpinner spinner) {
		SpinnerNumberModel model = (SpinnerNumberModel)spinner.getModel();
		return model.getNumber().intValue();
	}

	private double getSpinnerDoubleValue(JSpinner spinner) {
		SpinnerNumberModel model = (SpinnerNumberModel)spinner.getModel();
		return model.getNumber().doubleValue();
	}

	private void addCheckBoxAction(CheckBoxAction action, JPanel panel) {
		JCheckBox checkBox = new JCheckBox(action.getActionName(), action.getIcon());
		checkBox.setToolTipText(action.getToolTip());
		checkBox.setSelected(action.isChecked());
		checkBoxes.put(checkBox, action);
		panel.add(checkBox);
	}

	private void addCheckBoxAction(CheckBoxAction action) {
		addCheckBoxAction(action, checkBoxPanel);
	}

	private void addTwoCheckBoxActions(CheckBoxAction action1, CheckBoxAction action2) {
		JPanel panel = new JPanel(new GridLayout(1, 2, 5, 0));
		addCheckBoxAction(action1, panel);
		addCheckBoxAction(action2, panel);
		checkBoxPanel.add(panel);
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#cancel()
	 */
	public void cancel() {
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#ok()
	 */
	public void ok() {
		// this is where all the checkbox actions are actually run
		Enumeration e = checkBoxes.keys();
		while (e.hasMoreElements()) {
			JCheckBox checkBox = (JCheckBox) e.nextElement();
			CheckBoxAction checkBoxAction = (CheckBoxAction) checkBoxes.get(checkBox);
			checkBoxAction.setChecked(checkBox.isSelected());
			checkBoxAction.startAction();
		}

		Properties props = ApplicationAccessor.getProperties();
		boolean showWarning = chkbxManyChildren.isSelected();
		props.setProperty(DisplayBean.PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING, "" + showWarning);
		int warningThreshold = txtManyChildren.getIntegerText();
		props.setProperty(DisplayBean.PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING_THRESHOLD, "" + warningThreshold);

		boolean animate = chkbxAnim.isSelected();
		props.setProperty(DisplayBean.PROPERTY_KEY__USE_ANIMATION, "" + animate);
		int animThreshold = txtAnimThreshold.getIntegerText();
		props.setProperty(DisplayBean.PROPERTY_KEY__ANIMATION_THRESHOLD, "" + animThreshold);

		boolean useMotion = chkbxMotion.isSelected();
		props.setProperty(DisplayBean.PROPERTY_KEY__USE_MOTION, "" + useMotion);
		int displ = getSpinnerIntValue(spinnerDisplacement);
		props.setProperty(DisplayBean.PROPERTY_KEY__DISPLACEMENT, Integer.toString(displ));
		double radius = getSpinnerDoubleValue(spinnerRadius);
		props.setProperty(DisplayBean.PROPERTY_KEY__MOTION_RADIUS, Double.toString(radius));
		int speed = getSpinnerIntValue(spinnerSpeed);
		props.setProperty(DisplayBean.PROPERTY_KEY__MOTION_SPEED, Integer.toString(speed));
		int time = getSpinnerIntValue(spinnerTime);
		props.setProperty(DisplayBean.PROPERTY_KEY__MOTION_TIME, Integer.toString(time));

		int borderWidthMultiplier = getSpinnerIntValue(spinnerBorderWidth);
		props.setProperty(DisplayBean.PROPERTY_KEY__BORDER_WIDTH_MULTIPLIER,
				Integer.toString(borderWidthMultiplier));


		// update the MotionLayout to use the new displacement, radius, speed, and time
		try {
			updateMotionLayout(displ, radius, speed, time);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// update the query view too
		try {
			updateMotionLayout(displ, radius, speed, time);
		} catch (Exception ignore) {
		}
	}

	private void updateMotionLayout(int displacement, double radius, int speed, long time) {
		Vector v = displayBean.getLayouts();
		for (Iterator iter = v.iterator(); iter.hasNext(); ) {
			Layout layout = (Layout)iter.next();
			if (layout instanceof MotionLayout) {
				MotionLayout motionLayout = ((MotionLayout)layout);
				motionLayout.setDisplacement(displacement);
				motionLayout.setMotionRadius(radius);
				motionLayout.setMotionSpeed(speed);	// set speed BEFORE time
				motionLayout.setMotionTime(time);
				break;	// there can't be more than one right?
			}
		}
	}
}