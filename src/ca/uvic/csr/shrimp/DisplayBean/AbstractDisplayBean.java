/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.Component;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.event.DisplayFilterRequestListener;
import ca.uvic.csr.shrimp.DisplayBean.event.MagnifyEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.NavigationListener;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyListener;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.LifelineGroupingManager;
import ca.uvic.csr.shrimp.DisplayBean.layout.MethodExecGroupingManager;
import ca.uvic.csr.shrimp.DisplayBean.layout.StructuralGroupingManager;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.ExpressViewConfigurator;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewAction;
import ca.uvic.csr.shrimp.util.DoubleDimension;
import ca.uvic.csr.shrimp.util.GeometryUtils;

/**
 *
 * @author Rob Lintern
 */
public abstract class AbstractDisplayBean implements DisplayBean {

	// private final static boolean DEBUG = false;

	protected static final String DEFAULT_LABEL_MODE = DisplayConstants.LABEL_MODE_FIXED;
	private static final int DEFAULT_LABEL_LEVELS = 2;
	private static final boolean DEFAULT_LABEL_FADE_OUT = false;
	private static final boolean DEFAULT_LABEL_BACKGROUND_OPAQUE = false;
	private static final boolean DEFAULT_LONG_TOOLTIPS = false;
	private static final boolean DEFAULT_SHOW_ARC_LABELS = false;
	private static final boolean DEFAULT_INVERTED = false;

	private boolean tooltipEnabled;

	/* The Shrimp Event listeners */
	protected Vector filterRequestListeners;

	protected Vector customizedPanelListeners;

	protected Vector navigationListeners;

	/** The nodes and/or relationships currently focused on */
	protected ShrimpDisplayObject currentFocusedOnObject = null;

	/** The level of the most recently focused on node */
	// protected int currentLevel = 0;
	protected boolean displayBeanEnabled = true;

	/** The adapter used to catch all events from the display */
	protected ShrimpDisplayInputManager displayInputManager;

	/** The layouts available to this display bean */
	protected Hashtable layouts;

	/** The default display mode of labels. */
	protected String defaultLabelMode = DEFAULT_LABEL_MODE;

	/** The font for labels */
	protected Font labelFont;

	/** The number of levels of labels to show */
	protected int labelLevels = DEFAULT_LABEL_LEVELS;

	/** Whether labels fade out by level */
	protected boolean labelFadeOut = DEFAULT_LABEL_FADE_OUT;

	/** Whether label backgrounds are opaque */
	protected boolean labelBackgroundOpaque = DEFAULT_LABEL_BACKGROUND_OPAQUE;

	/** Whether or not to use long tool tips */
	protected boolean useLongTooltips = DEFAULT_LONG_TOOLTIPS;

	/** The child-parent relationships used for nesting. */
	protected String[] cprels;

	/** The bridge between the data and this display */
	protected DataDisplayBridge dataDisplayBridge;

	/** Whether we are using composite relationships */
	protected boolean useComposites;

	/** Whether or not the arcs are to be rendered with arrow heads */
	protected boolean usingArrowHeads;

	/** The arc styles in this display bean. */
	protected Map arcStyles;
	protected Vector arcStylesOrdered;

	/** The node shapes in this display bean */
	protected Map nodeShapes;
	protected Vector nodeShapesOrdered;

	/** The label styles in this display bean. */
	private Vector labelStyles;

	private Vector borderStyles;

	private MagnifyZoomHandler magnifyHandler;

	private boolean inverted = DEFAULT_INVERTED;

	protected boolean showArcLabels = DEFAULT_SHOW_ARC_LABELS;

	private double defaultArcWeight = ShrimpArc.DEFAULT_ARC_WEIGHT;

	/**
	 * @tag Shrimp(sequence)
	 */
	private boolean nodeEdgeMovementAllowed = true;

	private boolean switchLabelling;

	private int switchNum;

	protected String lastLayoutMode; // last layout used

	private ShrimpProject project;

	// initialized in the child classes
	protected StructuralGroupingManager groupingManager;
	protected LifelineGroupingManager lifeLineGroupingManager;
	protected MethodExecGroupingManager methodExecGroupingManager;

	/**
	 * @param cprels
	 * @param project
	 */
	public AbstractDisplayBean(String[] cprels, DataDisplayBridge dataDisplayBridge, ShrimpProject project) {
		this.project = project;
		this.cprels = cprels;
		this.dataDisplayBridge = dataDisplayBridge;

		defaultLabelMode = DEFAULT_LABEL_MODE;
		magnifyHandler = new MagnifyZoomHandler(this);
		lastLayoutMode = LayoutConstants.LAYOUT_GRID_BY_ALPHA;
		layouts = new Hashtable();

		filterRequestListeners = new Vector();
		customizedPanelListeners = new Vector();
		navigationListeners = new Vector();

		arcStyles = new HashMap();
		arcStylesOrdered = new Vector();
		addArcStyle(new StraightSolidLineArcStyle(), true);
		nodeShapes = new HashMap();
		nodeShapesOrdered = new Vector();
		addNodeShape(new RectangleNodeShape(), true);

		//@tag  Shrimp(LabelStyles) : add the supported label styles
		labelStyles = new Vector();
		labelStyles.add(DisplayConstants.LABEL_STYLE_FULL);
		labelStyles.add(DisplayConstants.LABEL_STYLE_ELIDE_LEFT);
		labelStyles.add(DisplayConstants.LABEL_STYLE_ELIDE_RIGHT);
		labelStyles.add(DisplayConstants.LABEL_STYLE_HIDE);

		borderStyles = NodeBorder.getStyles();

		displayBeanEnabled = false;

		JLabel dummyLabel = new JLabel("Hello World");
		labelFont = new Font(dummyLabel.getFont().getName(),
				DisplayConstants.DEFAULT_FONT_STYLE,
				DisplayConstants.DEFAULT_FONT_SIZE);

		usingArrowHeads = true;
		
		// @tag Shrimp.grouping : initialize the one and only structual grouping manager
		groupingManager = new StructuralGroupingManager(project, this);
		lifeLineGroupingManager = new LifelineGroupingManager(project, this);
		methodExecGroupingManager = new MethodExecGroupingManager(project, this);
	}

	/**
	 * @see DisplayBean#addLayout(ca.uvic.csr.shrimp.DisplayBean.layout.Layout)
	 */
	public void addLayout(Layout layout) {
		layouts.put(layout.getName(), layout);
	}

	/**
	 * @see DisplayBean#hasLayout(String)
	 */
	public boolean hasLayout(String layoutName) {
		return layouts.containsKey(layoutName);
	}

	/**
	 *
	 * @see DisplayBean#getLayouts()
	 */
	public Vector getLayouts() {
		return new Vector(layouts.values());
	}

	/**
	 * @see DisplayBean#setUsingArrowHeads(boolean)
	 */
	public void setUsingArrowHeads(boolean usingArrowHeads) {
		Vector arcs = getAllArcs();
		for (int i = 0; i < arcs.size(); i++) {
			String arrowHeadStyle = getDataDisplayBridge().getArrowHeadStyle(
					(ShrimpArc) arcs.elementAt(i));
			((ShrimpArc) arcs.elementAt(i)).setUsingArrowHead(usingArrowHeads, arrowHeadStyle);
		}
		this.usingArrowHeads = usingArrowHeads;
	}

	/**
	 *
	 * @see DisplayBean#getUsingArrowHeads()
	 */
	public boolean getUsingArrowHeads() {
		return usingArrowHeads;
	}

	public void setDefaultArcWeight(double defaultArcWeight) {
		if (this.defaultArcWeight != defaultArcWeight) {
			double oldWeight = this.defaultArcWeight;
			this.defaultArcWeight = defaultArcWeight;
			getAllArcs();
			for (Iterator iter = getAllArcs().iterator(); iter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				if (arc.getWeight() == oldWeight) {
					arc.setWeight(defaultArcWeight);
				}
			}
		}
		repaint();
	}

	public double getDefaultArcWeight() {
		return defaultArcWeight;
	}

	/**
	 * Sets whether or not the display should use long tooltips.
	 */
	public void setUseLongTooltips(boolean useLongTooltips) {
		this.useLongTooltips = useLongTooltips;
	}

	/**
	 * Returns whether or not the display should use long tooltips.
	 */
	public boolean getUseLongTooltips() {
		return useLongTooltips;
	}

	/**
	 * @see DisplayBean#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		displayInputManager.setActive(enabled);
		setToolTipEnabled(enabled);
		displayBeanEnabled = enabled;
	}

	/**
	 * @see DisplayBean#isEnabled()
	 */
	public boolean isEnabled() {
		return displayBeanEnabled;
	}

	/**
	 *
	 * @see DisplayBean#setUsingComposites(boolean)
	 */
	public void setUsingComposites(boolean useComposites) {
		this.useComposites = useComposites;
	}

	/**
	 *
	 * @see DisplayBean#getUsingComposites()
	 */
	public boolean getUsingComposites() {
		return useComposites;
	}

	/**
	 * @see DisplayBean#setVisible(java.lang.Object,
	 *      boolean, boolean)
	 */
	public void setVisible(Object obj, boolean visible, boolean assignDefaultPosition) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				setVisible(iter.next(), visible, assignDefaultPosition);
			}
			return;
		}

		// make sure that this object can be set visible
		if (visible) {
			if (obj instanceof ShrimpNode) {
				// can't set node visible if its parent not visible, or parent
				// is not in children mode,
				// or the artifact associated with this node is filtered
				ShrimpNode sn = (ShrimpNode) obj;
				ShrimpNode parentNode = sn.getParentShrimpNode();
				if (parentNode != null) {
					visible = isVisible(parentNode) &&
						parentNode.getPanelMode().equals(PanelModeConstants.CHILDREN);
				}
				visible = visible && !isFiltered(sn.getArtifact());
			} else if (obj instanceof ShrimpArc) {
				// can't set arc visible if its end nodes are not in the display
				ShrimpArc arc = (ShrimpArc) obj;
				ShrimpNode srcNode = arc.getSrcNode();
				ShrimpNode destNode = arc.getDestNode();
				boolean srcNodeInDisplay = nodeIsInDisplay(srcNode);
				boolean destNodeInDisplay = nodeIsInDisplay(destNode);
				if (!srcNodeInDisplay || !isVisible(srcNode)
						|| !destNodeInDisplay || !isVisible(destNode)
						|| isFiltered(arc.getRelationship())) {
					visible = false;
				}
			} else if (obj instanceof ShrimpLabel) {
				// cant set label visible if its node is not visible
				ShrimpLabel label = (ShrimpLabel) obj;
				if (!isVisible(label.getLabeledObject())) {
					visible = false;
				}
			}
		}

		if (obj instanceof ShrimpDisplayObject) {
			setDisplayObjectVisible((ShrimpDisplayObject) obj, visible, assignDefaultPosition);
		}
	}

	// TODO need good way to determine if a node is in the display
	protected abstract boolean nodeIsInDisplay(ShrimpNode node);

	protected void setDisplayObjectVisible(ShrimpDisplayObject sdo, boolean visible, boolean assignDefaultPosition) {
		// now check if it's a node or arc or label
		if (sdo instanceof ShrimpNode) {
			ShrimpNode sn = (ShrimpNode) sdo;
			sn.setVisible(visible);
			if (visible && !sn.getHasBeenTransformedOnce() && assignDefaultPosition) {
				setNewNodePosition(sn);
			}

			// update visibility of arcs attached to this node
			Vector arcs = getDataDisplayBridge().getShrimpArcs(sn);
			for (Iterator iter = arcs.iterator(); iter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				arc.updateVisibility();
				ShrimpLabel sl = getDataDisplayBridge().getShrimpArcLabel(arc, false);
				if (sl != null) {
					sl.updateVisibility();
				}
			}

			// If this ShrimpNode has a label in the tree then update it's visibility
			ShrimpLabel sl = getDataDisplayBridge().getShrimpNodeLabel(sn, false);
			if (sl != null) {
				sl.updateVisibility();
			}

			// set the existing children to the same visibility.
			Vector children = getDataDisplayBridge().getChildNodes(sn);
			setVisible(children, visible, assignDefaultPosition);
		} else if (sdo instanceof ShrimpArc) {
			// If this ShrimpArc has a label in the tree then update it's visibility
			ShrimpLabel sl = getDataDisplayBridge().getShrimpArcLabel((ShrimpArc) sdo, false);
			if (sl != null) {
				sl.updateVisibility();
			}
		}
	}

	protected abstract void setNewNodePosition(ShrimpNode sn);

	/**
	 * @see DisplayBean#getVisibleObjects()
	 */
	public Vector getVisibleObjects() {
		Vector visibleArcs = getVisibleArcs();
		Vector visibleNodes = getVisibleNodes();
		Vector visibleLabels = getVisibleLabels();

		Vector visibleObjects = new Vector(visibleArcs.size()
				+ visibleNodes.size() + visibleLabels.size());
		visibleObjects.addAll(visibleArcs);
		visibleObjects.addAll(visibleNodes);
		visibleObjects.addAll(visibleLabels);

		return visibleObjects;
	}

	/**
	 * @see DisplayBean#getInvisibleObjects()
	 */
	public Vector getInvisibleObjects() {
		Vector arcs = getAllArcs();
		Vector nodes = getAllNodes();
		Vector labels = getAllLabels();

		Vector objects = new Vector(arcs.size() + nodes.size() + labels.size());

		for (int i = 0; i < arcs.size(); i++) {
			ShrimpArc arc = (ShrimpArc) arcs.get(i);
			if (!arc.isVisible()) {
				objects.add(arc);
			}
		}

		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.get(i);
			if (!node.isVisible()) {
				objects.add(node);
			}
		}

		for (int i = 0; i < labels.size(); i++) {
			ShrimpLabel label = (ShrimpLabel) labels.get(i);
			if (!label.isVisible()) {
				objects.add(label);
			}
		}

		objects.trimToSize();
		return objects;
	}

	/**
	 * @see DisplayBean#isVisible(Object)
	 */
	public boolean isVisible(Object obj) {
		boolean visible = false;
		if (obj instanceof Collection) {
			visible = true;
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext() && visible;) {
				visible = isVisible(iter.next());
			}
		} else if (obj instanceof ShrimpDisplayObject) {
			visible = ((ShrimpDisplayObject) obj).isVisible();
		}

		return visible;
	}

	/**
	 * @see DisplayBean#addObject(java.lang.Object)
	 */
	public void addObject(Object obj) {
		// loop through vector
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				addObject(iter.next());
			}
		} else if (obj instanceof ShrimpNode) {
			ShrimpNode node = (ShrimpNode) obj;
			if (isFiltered(node.getArtifact())) {
				node.setVisible(false); // make sure that filtered nodes are
										// invisible by default
			}
			addShrimpNode(node);

			// @tag Shrimp.labelling
			// If labels are fixed or scaled by level, add a new sticky label
			// for this node to the display
			// If labels are scaled by node size, add a label directly to the
			// visual pane of this node
			if (defaultLabelMode.equals(DisplayConstants.LABEL_MODE_FIXED) ||
				defaultLabelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)) {
				ShrimpLabel sl = dataDisplayBridge.getShrimpNodeLabel(node,	true);
				addShrimpLabel(sl);
			}

			// add the terminals to the display, as new node's sibling
			for (Iterator iter = node.getTerminals().iterator(); iter.hasNext();) {
				ShrimpTerminal terminal = (ShrimpTerminal) iter.next();
				addShrimpTerminal(terminal);
			}

			// add the arcs that this node participates in
			Vector arcs = dataDisplayBridge.getShrimpArcs(node, true, false);
			for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iterator.next();
				addShrimpArc(arc);
			}

		} else if (obj instanceof ShrimpArc) {
			// this is a node that represents a relationship
			addShrimpArc((ShrimpArc) obj);
		} else if (obj instanceof ShrimpLabel) {
			// this is a node that represents an artifact's label
			addShrimpLabel((ShrimpLabel) obj);
		} else if (obj instanceof ShrimpTerminal) {
			addShrimpTerminal((ShrimpTerminal) obj);
		}
	}

	// arranges all arcs going between the same nodes as this arc so that they
	// do not overlap
	public void arrangeArcs(ShrimpNode srcNode, ShrimpNode destNode) {
		Vector arcsFromDestToSrc = getDataDisplayBridge().getShrimpArcs(destNode, srcNode);
		boolean isArcInOtherDirection = false;
		for (Iterator iterator = arcsFromDestToSrc.iterator(); iterator.hasNext();) {
			ShrimpArc arcFromDestToSrc = (ShrimpArc) iterator.next();
			if (arcFromDestToSrc.isVisible()) {
				isArcInOtherDirection = true;
				break;
			}
		}

		int curveFactor = 0;
		if (isArcInOtherDirection) {
			curveFactor++;
		}

		Vector arcsFromSrcToDest = getDataDisplayBridge().getShrimpArcs(srcNode, destNode);
		isArcInOtherDirection = false;
		for (Iterator iterator = arcsFromSrcToDest.iterator(); iterator.hasNext();) {
			ShrimpArc arcFromSrcToDest = (ShrimpArc) iterator.next();
			if (arcFromSrcToDest.isVisible()) {
				arcFromSrcToDest.setCurveFactor(curveFactor++);
				isArcInOtherDirection = true;
			}
		}

		curveFactor = 0;
		if (isArcInOtherDirection) {
			curveFactor++;
		}

		for (Iterator iterator = arcsFromDestToSrc.iterator(); iterator.hasNext();) {
			ShrimpArc arcFromDestToSrc = (ShrimpArc) iterator.next();
			if (arcFromDestToSrc.isVisible()) {
				arcFromDestToSrc.setCurveFactor(curveFactor++);
			}
		}
	}

	protected abstract void addShrimpNode(ShrimpNode node);

	protected abstract void addShrimpArc(ShrimpArc arc);

	protected abstract void addShrimpLabel(ShrimpLabel label);

	protected abstract void addShrimpTerminal(ShrimpTerminal terminal);

	/**
	 * @see DisplayBean#removeObject(java.lang.Object)
	 */
	public void removeObject(Object obj) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				removeObject(iter.next());
			}
		} else if (obj instanceof ShrimpNode) {
			removeShrimpNode((ShrimpNode) obj);
		} else if (obj instanceof ShrimpArc) {
			removeShrimpArc((ShrimpArc) obj);
		} else if (obj instanceof ShrimpLabel) {
			removeShrimpLabel((ShrimpLabel) obj);
		}
	}

	protected abstract void removeShrimpNode(ShrimpNode node);

	protected abstract void removeShrimpArc(ShrimpArc arc);

	protected abstract void removeShrimpLabel(ShrimpLabel label);

	public void addNavigationListener(NavigationListener listener) {
		navigationListeners.add(listener);
	}

	public void removeNavigationListener(NavigationListener listener) {
		navigationListeners.remove(listener);
	}

	/**
	 * Informs any navigation listeners that a magnify event has occurred.
	 */
	public void fireAfterMagnifyEvent(Object fromObject, Object toObject) {
		MagnifyEvent e = new MagnifyEvent(fromObject, toObject);
		for (int i = 0; i < navigationListeners.size(); i++) {
			((NavigationListener) navigationListeners.elementAt(i)).afterMagnify(e);
		}
	}

	/**
	 * Informs any navigation listeners that a magnify event is about to occur.
	 */
	public void fireBeforeMagnifyEvent(Object fromObject, Object toObject) {
		MagnifyEvent e = new MagnifyEvent(fromObject, toObject);
		for (int i = 0; i < navigationListeners.size(); i++) {
			((NavigationListener) navigationListeners.elementAt(i)).beforeMagnify(e);
		}
	}

	/**
	 * @see DisplayBean#setPanelMode(java.lang.Object,
	 *      java.lang.String)
	 */
	public void setPanelMode(Object obj, String newMode) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				setPanelMode(iter.next(), newMode);

			}
		} else if (obj instanceof ShrimpNode) {
			ShrimpNode sn = (ShrimpNode) obj;
			String currentMode = sn.getPanelMode();

			if (shouldSetPanelMode(sn, newMode, currentMode)) {
				setPanelMode(sn, newMode, currentMode);
			} else {
				// System.out.println("cant set panel mode of " + obj + " to " + newMode);
			}
		}
	}

	private void setPanelMode(ShrimpNode sn, String newMode, String currentMode) {
		sn.setPanelMode(newMode);
		if (newMode.equals(PanelModeConstants.CHILDREN) &&
			!currentMode.equals(PanelModeConstants.CHILDREN)) {
			dataDisplayBridge.getCompositeArcsManager().addCompositeArcsOfChildren(sn);
		}

		if (!newMode.equals(PanelModeConstants.CHILDREN) &&
			currentMode.equals(PanelModeConstants.CHILDREN)) {
			dataDisplayBridge.getCompositeArcsManager().collapseCompositeArcs(sn);
		}
	}

	private boolean shouldSetPanelMode(ShrimpNode node, String newMode, String currentMode) {
		if (PanelModeConstants.CHILDREN.equals(newMode) && !PanelModeConstants.CHILDREN.equals(currentMode)) {
			int numVisibleChildren = getDataDisplayBridge().getVisibleChildNodeCount(node);
			int numInvisibleChildren = getDataDisplayBridge().getInvisibleChildNodeCount(node);
			int numTotalChildren = numVisibleChildren + numInvisibleChildren;
			if (numVisibleChildren == 0) {
				return false;
			}

			Properties properties = ApplicationAccessor.getProperties();
			String showWarningStr = properties.getProperty(PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING,
															DEFAULT_SHOW_MANY_CHILDREN_WARNING);
			boolean showWarning = (new Boolean(showWarningStr)).booleanValue();
			if (showWarning) {
				String thresholdStr = properties.getProperty(PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING_THRESHOLD,
															DEFAULT_SHOW_MANY_CHILDREN_WARNING_THRESHOLD);
				int threshold = (new Integer(thresholdStr)).intValue();
				if (numTotalChildren > threshold) {
					Component parentComp = ApplicationAccessor.getParentFrame();
					StringBuffer msg = new StringBuffer();
					msg.append("Are you sure you want to open \"");
					msg.append(node.getName());
					msg.append("?\"\n\nThere will be ");
					msg.append(numTotalChildren);
					msg.append(" children of \"");
					msg.append(node.getName());
					msg.append("!\"\n");
					msg.append(numVisibleChildren + " will be visible and ");
					msg.append(numInvisibleChildren + " will be filtered.");
					msg.append("\nIt may take some time to create them all.");
					msg.append("\n\n(Note: This warning can be configured in 'Tools > Options')");
					int result = JOptionPane.showConfirmDialog(parentComp, msg.toString(),
							ApplicationAccessor.getAppName(), JOptionPane.YES_NO_OPTION);
					return (result == JOptionPane.OK_OPTION);
				}
				return true;
			}
		}
		return true;
	}

	/**
	 * @see DisplayBean#getPanelMode(java.lang.Object)
	 */
	public String getPanelMode(Object obj) {
		if (obj instanceof ShrimpNode) {
			return ((ShrimpNode) obj).getPanelMode();
		}
		return null;
	}

	/**
	 * @see DisplayBean#setDefaultLabelMode(java.lang.String)
	 */
	public void setDefaultLabelMode(String newMode) {
		if (newMode != null) {
			defaultLabelMode = newMode; // set label mode before doing anything else
			setLabelMode(getAllNodes(), newMode);
		} else {
			System.err.println("AbstractDisplayBean: warning - can't set default label mode to null!");
		}
	}

	/**
	 * @see DisplayBean#setLabelMode(Object, String)
	 * @tag Shrimp.labelling
	 */
	public void setLabelMode(Object object, String newMode) {
		if (object instanceof Collection) {
			for (Iterator iter = ((Collection) object).iterator(); iter.hasNext();) {
				setLabelMode(iter.next(), newMode);
			}
		} else if (object instanceof ShrimpNode) {
			ShrimpNode node = (ShrimpNode) object;
			String oldMode = node.getLabelMode();
			if (oldMode.equals(newMode)) {
				// do nothing
			} else if (DisplayConstants.isLabelOnNode(oldMode) && !DisplayConstants.isLabelOnNode(newMode)) {
				// when switching from labels on nodes to sticky labels
				// hide the labels on the nodes and add sticky labels to the display
				node.setLabelMode(newMode);
				// create a new ShrimpLabel for this node and add it to the display
				ShrimpLabel sl = dataDisplayBridge.getShrimpNodeLabel(node, true);
				addObject(sl);
			} else if (!DisplayConstants.isLabelOnNode(oldMode) && DisplayConstants.isLabelOnNode(newMode)) {
				// when switching from sticky labels to labels on nodes
				// show the labels on the nodes and remove sticky labels from the display
				node.setLabelMode(newMode);
				// remove sticky label from the display
				ShrimpLabel sl = dataDisplayBridge.getShrimpNodeLabel(node, false);
				if (sl != null) {
					removeObject(sl);
				}
			// Switching between on-label modes
			} else if (DisplayConstants.isLabelOnNode(oldMode) && DisplayConstants.isLabelOnNode(newMode)) {
				node.setLabelMode(newMode);
				// Switching between above-label modes
			} else if (!DisplayConstants.isLabelOnNode(oldMode) && !DisplayConstants.isLabelOnNode(newMode)) {
				// when switching from one type of sticky label to another, just update the existing label
				node.setLabelMode(newMode);
				ShrimpNodeLabel label = dataDisplayBridge.getShrimpNodeLabel(node, false);
				if (label != null) {
					label.displayObjectPositionChanged();
					label.updateVisibility();
				}
			}
		}
	}

	/**
	 * @see DisplayBean#getLabelMode(java.lang.Object)
	 */
	public String getLabelMode(Object object) {
		String labelMode = "";
		if (object instanceof ShrimpNode) {
			labelMode = ((ShrimpNode) object).getLabelMode();
		}
		return labelMode;
	}

	/**
	 * @see DisplayBean#getDefaultLabelMode()
	 */
	public String getDefaultLabelMode() {
		return defaultLabelMode;
	}

	/**
	 * @see DisplayBean#setLabelFont(Object)
	 */
	public void setLabelFont(Object newFont) {
		labelFont = (Font) newFont;
		setLabelFont(getAllNodes(), (Font) newFont);
		setLabelFont(getAllLabels(), (Font) newFont);
	}

	/**
	 * Change the font of labels of the passed in object.
	 *
	 * @param newFont
	 *            The new label font
	 */
	private void setLabelFont(Object obj, Font newFont) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				setLabelFont(iter.next(), newFont);
			}
		} else {
			if (obj instanceof ShrimpNode) {
				ShrimpNode sn = (ShrimpNode) obj;
				sn.setLabelFont(newFont);
			} else if (obj instanceof ShrimpLabel) {
				ShrimpLabel ssl = (ShrimpLabel) obj;
				ssl.setFont(newFont);
			}
		}
	}

	/**
	 * @see DisplayBean#getLabelFont()
	 */
	public Object getLabelFont() {
		return labelFont;
	}

    /**
     * Change whether we should switch labelling mode.
     *
     *@param switchLabelling Whether labels fade out by level.
     */
    public void setSwitchLabelling(boolean labelling) {
    	this.switchLabelling = labelling;
    }

    /**
     * Return whether we should switch labelling mode.
     */
    public boolean getSwitchLabelling() {
    	return switchLabelling;
    }

   /**
     * Change the number of nodes at which labelling switches to on node
     *
     *@param num the number of nodes to switch labelling mode at
     */
    public void setSwitchAtNum(int num) {
    	this.switchNum = num;
    }

    /**
     * Returns the number of nodes at which labelling switches to on node
     */
    public int getSwitchAtNum() {
    	return switchNum;
    }


	/**
	 * @see DisplayBean#setLabelLevels(int)
	 */
	public void setLabelLevels(int numLevels) {
		labelLevels = numLevels;
		setLabelLevels(getAllLabels(), numLevels);
	}

	private void setLabelLevels(Object obj, int numLevels) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				setLabelLevels(iter.next(), numLevels);
			}
		} else {
			if (obj instanceof ShrimpLabel) {
				if (defaultLabelMode.equals(DisplayConstants.LABEL_MODE_FIXED) ||
					defaultLabelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)) {
					ShrimpLabel ssl = (ShrimpLabel) obj;
					ssl.updateVisibility();
				}
			}
		}
	}

	/**
	 * @see DisplayBean#getLabelLevels()
	 */
	public int getLabelLevels() {
		return labelLevels;
	}

	/**
	 * @see DisplayBean#setLabelFadeOut(boolean)
	 */
	public void setLabelFadeOut(boolean fadeOut) {
		labelFadeOut = fadeOut;
		setLabelFadeOut(getAllLabels(), fadeOut);
	}

	private void setLabelFadeOut(Object obj, boolean fadeOut) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection) obj).iterator(); iter.hasNext();) {
				setLabelFadeOut(iter.next(), fadeOut);
			}
		} else {
			if (obj instanceof ShrimpLabel) {
				if (defaultLabelMode.equals(DisplayConstants.LABEL_MODE_FIXED) ||
					defaultLabelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)) {
					ShrimpLabel sl = (ShrimpLabel) obj;
					sl.updateVisibility();
				}
			}
		}
	}

	/**
	 * @see DisplayBean#getLabelFadeOut()
	 */
	public boolean getLabelFadeOut() {
		return labelFadeOut;
	}

	/**
	 * @see DisplayBean#getLabelBackgroundOpaque()
	 */
	public boolean getLabelBackgroundOpaque() {
		return labelBackgroundOpaque;
	}

	/**
	 * @see DisplayBean#setLabelBackgroundOpaque(boolean)
	 */
	public void setLabelBackgroundOpaque(boolean labelOpaque) {
		this.labelBackgroundOpaque = labelOpaque;
		for (Iterator iter = getAllLabels().iterator(); iter.hasNext();) {
			ShrimpLabel label = (ShrimpLabel) iter.next();
			if (label instanceof ShrimpNodeLabel) {
				label.setBackgroundOpaque(labelOpaque);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see DisplayBean#setShowArcLabels(boolean)
	 */
	public void setShowArcLabels(boolean showArcLabels) {
		if (this.showArcLabels == showArcLabels) {
			return;
		}
		this.showArcLabels = showArcLabels;
		if (showArcLabels) {
			Vector arcs = getAllArcs();
			for (Iterator iter = arcs.iterator(); iter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				ShrimpArcLabel arcLabel = dataDisplayBridge.getShrimpArcLabel(
						arc, true);
				if (arcLabel != null) {
					addObject(arcLabel);
				}
			}
		} else {
			Vector arcLabels = dataDisplayBridge.getShrimpArcLabels();
			removeObject(arcLabels);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see DisplayBean#getShowArcLabels()
	 */
	public boolean getShowArcLabels() {
		return showArcLabels;
	}

	/**
	 * @see DisplayBean#isToolTipEnabled()
	 */
	public boolean isToolTipEnabled() {
		return tooltipEnabled;
	}

	/**
	 * @see DisplayBean#setToolTipEnabled(boolean)
	 */
	public void setToolTipEnabled(boolean enabled) {
		tooltipEnabled = enabled;
	}

	/**
	 * @see DisplayBean#clear()
	 */
	public void clear() {
		currentFocusedOnObject = null;
	}

	/**
	 * @see DisplayBean#setLayoutMode(Object, String, boolean, boolean)
	 */
	public void setLayoutMode(Object obj, final String newMode, boolean showLayoutDialog, final boolean animate) {
		Layout layout = getLayout(newMode);
		if (layout == null) {
			System.err.println("no layout for " + newMode);
			return;
		}

		if (!(obj instanceof Vector)) {
			(new Exception("Cant set layout mode of: " + obj + ".  Expecting a Vector.")).printStackTrace();
			return;
		}

		Vector nodes = (Vector) obj;
		if (nodes.isEmpty()) {
			return;
		}

		layout.resetLayout();
		this.lastLayoutMode = newMode; // save in case someone needs to know

		// It is possible that we are applying a layout to node at many different levels,
		// could be parents and children mixed, so ...
		// Arrange nodes, first by level then by parentNodeID
		// Use TreeMap, because then levels will be automatically sorted
		//
		// TreeMap("level" -> Map("parentNodeID" -> Vector(node)))
		TreeMap levelMap = new TreeMap();
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			ShrimpNode parentNode = node.getParentShrimpNode();
			if (parentNode == null) {
				continue; // ignore roots
			}
			String key = "" + node.getLevel();

			Map parentIDToChildrenMap = (HashMap) levelMap.get(key);
			if (parentIDToChildrenMap == null) {
				parentIDToChildrenMap = new HashMap();
				levelMap.put(key, parentIDToChildrenMap);
			}

			key = "" + parentNode.getID();
			Vector childrenOfThisParent = (Vector) parentIDToChildrenMap.get(key);
			if (childrenOfThisParent == null) {
				childrenOfThisParent = new Vector();
				parentIDToChildrenMap.put(key, childrenOfThisParent);
			}
			childrenOfThisParent.add(node);
		}

		// it is possible that some of the nodes are root nodes (no parents)
		// and so they must be laid out first
		Vector includedRootNodes = new Vector();
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			ShrimpNode parentNode = node.getParentShrimpNode();
			if (parentNode == null && node.isVisible()) {
				includedRootNodes.add(node);
			}
		}
		if (!includedRootNodes.isEmpty()) {
			Vector excludedRoots = dataDisplayBridge.getRootNodes();
			excludedRoots.removeAll(includedRootNodes);
			performLayout(includedRootNodes, getDisplayBounds(), excludedRoots, showLayoutDialog, animate, layout);
		}

		// Now that we have nodes arranged, just go though the maps
		for (Iterator levelsIter = levelMap.values().iterator(); levelsIter.hasNext();) {
			Map parentIDToChildrenMap = (Map) levelsIter.next();
			for (Iterator siblingIter = parentIDToChildrenMap.values().iterator(); siblingIter.hasNext();) {
				// siblingsToLayout are nodes at one level and from one parent node
				Vector siblingsToLayout = (Vector) siblingIter.next();
				PShrimpNode firstSiblingToLayout = (PShrimpNode) siblingsToLayout.firstElement();
				ShrimpNode parentNode = firstSiblingToLayout.getParentShrimpNode();
				Vector otherSiblings = dataDisplayBridge.getChildNodes(parentNode);
				otherSiblings.removeAll(siblingsToLayout);
				Vector nodesToExclude = new Vector(); // these nodes are to be excluded from the layout
				for (Iterator iter = otherSiblings.iterator(); iter.hasNext();) {
					ShrimpNode otherSibling = (ShrimpNode) iter.next();
					if (otherSibling.isVisible()) {
						nodesToExclude.add(otherSibling);
					}
				}
				Rectangle2D.Double bounds = getNestedLayoutBounds(parentNode);
				performLayout(siblingsToLayout, bounds, nodesToExclude, showLayoutDialog, animate, layout);
				// Automatically open subgraphs
				if (siblingsToLayout.size() == 1 && dataDisplayBridge.getChildNodesCount(firstSiblingToLayout) > 0) {
					openNode(firstSiblingToLayout);
				}
			}
		}
	}

	/**
	 * This method gets the bounds for laying out the children of the given {@link ShrimpNode}.
	 * It adds a small amount of padding at the top and bottom of the inner bounds
	 * for the labels.
	 * @param parentNode
	 * @return the bounds for laying out the parentNode's children
	 */
	protected Rectangle2D.Double getNestedLayoutBounds(ShrimpNode parentNode) {
		Rectangle2D.Double bounds = parentNode.getInnerBounds();
		// @tag Shrimp.LayoutPadding : this is an attempt to get a constant amount of padding above the child nodes
		double shift = 0.05 * bounds.height;
		double scale = parentNode.getMagnification();
		if (scale > 0) {
			shift *= scale;
		}
		return new Rectangle2D.Double(bounds.x, bounds.y + shift, bounds.width, bounds.height - shift);
	}

	protected abstract Rectangle2D.Double getDisplayBounds();

	private void performLayout(Vector nodesToInclude, Rectangle2D.Double bounds, Vector nodesToExclude,
			boolean showLayoutDialog, boolean animate, Layout layout) {
		// change the cursor
		ApplicationAccessor.waitCursor();
		try {
			Layout layoutToApply = selectLayout(layout, ((ShrimpNode)nodesToInclude.get(0)).getParentShrimpNode());
			layoutToApply.setupAndApplyLayout(nodesToInclude, bounds, nodesToExclude, showLayoutDialog, animate, true);
		} catch (Exception e) {
			System.err.println("There was a problem performing layout: " + layout);
			e.printStackTrace();
		} finally {
			ApplicationAccessor.defaultCursor();
		}
		showLayoutDialog = false;
	}

	/**
	 * Select the layout that applies to this particular subgraph, using
	 * the default layout if none specified
	 * @param layout
	 * @param parentShrimpNode
	 * @return
	 */
	private Layout selectLayout(Layout layout, ShrimpNode node) {
		Layout layoutToApply = layout;
		// If this is the default mode, check for graph override
		if (layoutToApply.getName().equals(getDefaultLayoutMode())) {
			if (node != null) {
				Artifact artifact = node.getArtifact();
				String viewType = (String)artifact.getAttribute("view_type");
				if (viewType != null && !viewType.equals("")) {
					if (viewType.equals("CallGraph")) {
						layoutToApply = getLayout(LayoutConstants.LAYOUT_HIERARCHICAL);
						setDefaultLabelMode(DisplayConstants.LABEL_MODE_WRAP_TO_NODE);
						updateLabelModeDisplay();
		 			}
					else if (viewType.equals("ControlFlow")) {
						layoutToApply = configureBasedOnQuickView(JavaDomainConstants.JAVA_QUICK_VIEW_CONTROL_FLOW_GRAPH);
					}
				}
			}
		}
		return layoutToApply;
	}

	private String getDefaultLayoutMode() {
		return ApplicationAccessor.getProperty(PROPERTY_KEY__DEFAULT_LAYOUT_MODE, PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE);
	}

	/**
	 * Update the label mode drop list
	 */
	private void updateLabelModeDisplay() {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView)project.getTool(ShrimpProject.SHRIMP_VIEW);
				SelectorBean selectorBean = (SelectorBean)shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
				selectorBean.setSelected(DisplayConstants.LABEL_MODE, getDefaultLabelMode());
			} catch (ShrimpToolNotFoundException e) {
				// ignore
			} catch (BeanNotFoundException e) {
				// ignore
			}
		}
	}

	/**
	 * Configure the bean based on the specified quick view name, returning
	 * the layout that is associated with this quick view
	 * @param viewName
	 * @return
	 */
	private Layout configureBasedOnQuickView(String viewName) {
		if (project != null) {
			QuickViewAction action = project.getQuickViewManager().getQuickViewAction(viewName);
			ExpressViewConfigurator config = action.getConfigurator();
			setDefaultLabelMode(config.getLabelMode());
			updateLabelModeDisplay();
			return getLayout(config.getLayoutMode());
		} else {
			return getLayout(lastLayoutMode);
		}
	}

	/**
	 * Returns the layout with the given name. If there isn't a layout, null is
	 * returned Note: Only layouts added to this bean will be returned
	 */
	public Layout getLayout(String name) {
		return (Layout) layouts.get(name);
	}

	/**
	 * @see DisplayBean#startMovingWithMouse(Collection)
	 */
	public void startMovingWithMouse(Collection objs) {
		if (currentFocusedOnObject != null && currentFocusedOnObject instanceof ShrimpNode) {
			((ShrimpNode) currentFocusedOnObject).setHasFocus(false);
			// currentFocusedOnObject = null;
		}
	}

	/**
	 * @see DisplayBean#stopMovingWithMouse()
	 */
	public void stopMovingWithMouse() {
		if (currentFocusedOnObject != null && currentFocusedOnObject instanceof ShrimpNode) {
			// ((ShrimpNode) currentFocusedOnObject).setIsFocusedOn(true);
		}
	}

	/**
	 * @see DisplayBean#setPositionsAndSizes(Vector, Vector, Vector, boolean)
	 */
	public void setPositionsAndSizes(Vector objects, Vector positions, Vector dimensions, boolean animate) {
		setPositionsAndSizes(objects, new Vector(), positions, dimensions, animate, true);
	}

	/**
	 * @see DisplayBean#setPositionsAndSizes(Vector, Vector, Vector, boolean)
	 */
	public void setPositionsAndSizes(Vector objects, Vector relationships, Vector positions, Vector dimensions, boolean animate) {
		if (animate) {
			// do not animate if too many nodes
			String animationThresholdStr = ApplicationAccessor.getProperty(PROPERTY_KEY__ANIMATION_THRESHOLD, "" + DEFAULT_ANIMATION_THRESHOLD);
			int animationThreshold = Integer.valueOf(animationThresholdStr).intValue();
			animate = (objects.size() < animationThreshold);
		}
		setPositionsAndSizes(objects, relationships, positions, dimensions, animate, true);
	}

	protected void setPositionsAndSizes(Vector objects, Vector positions, Vector dimensions, boolean animate, boolean fixDescendents) {
		setPositionsAndSizes(objects, new Vector(), positions, dimensions, animate, fixDescendents);
	}

	protected void setPositionsAndSizes(Vector objects, Vector relationships, Vector positions, Vector dimensions,
										boolean animate, boolean fixDescendents) {
		if (objects.isEmpty()) {
			return;
		}
		if ((objects.size() != positions.size()) || (objects.size() != dimensions.size()) || (positions.size() != dimensions.size())) {
			// (new Exception("vectors are different sizes!")).printStackTrace();
			System.err.println("AbstractDisplayBean.setPositionsAndSizes(): vectors are different sizes!");
			return;
		}
		// animate = objects.size() < 100; // don't animate if too many objects
		final List nodesToBeMoved = new ArrayList();
		final List transformsToUse = new ArrayList();

		setBoundsAndCalcTransforms(objects, positions, dimensions, fixDescendents, nodesToBeMoved, transformsToUse, false);

		// finally, transform all these nodes, firePositionChangedEvent=false, because it will be done more efficiently next
		setTransformsOfNodes(nodesToBeMoved, transformsToUse, false, animate);

		// if movements were animated, then arcs and labels etc should have been updating continuously
		// otherwise, we need to update them now
		if (!(animate && isAnimatingLayouts())) {
			// all nodes affected by the move including the passed in nodes and their descendents (if fixDescendents is true)
			Set allNodesAffected = new HashSet();
			for (Iterator iter = objects.iterator(); iter.hasNext(); ) {
				ShrimpNode node = (ShrimpNode) iter.next();
				allNodesAffected.add(node);
				if (fixDescendents) {
					allNodesAffected.addAll(dataDisplayBridge.getDescendentNodes(node, false));
				}
			}
			// Create a set of all arcs that are affected by the movements of nodes
			// This is done last to prevent arcs from being updated more than once
			// Also, tell any labels of these nodes that the node position has changed
			// a rough estimate of the number of arcs
			Set allArcs = new HashSet(nodesToBeMoved.size() * 5);
			for (Iterator iterator = allNodesAffected.iterator(); iterator.hasNext(); ) {
				ShrimpNode node = (ShrimpNode) iterator.next();
				if (node.isVisible()) {
					ShrimpNodeLabel label = dataDisplayBridge.getShrimpNodeLabel(node, false);
					if (label != null) {
						label.displayObjectPositionChanged();
					}
					allArcs.addAll(dataDisplayBridge.getShrimpArcs(node));
				}
			}
			for (Iterator iter = allArcs.iterator(); iter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				arc.displayObjectPositionChanged();
			}
		}
	}

	/**
	 * Set the bounds of given nodes and calculates the transforms to be applied
	 * to nodes based on the given inputs.
	 * Note: object[i] will be placed at position[i] and resized to dimension[i]
	 *
	 * @param nodes
	 *            The nodes to move and resize.
	 * @param positions
	 *            The new positions of centers of the nodes with respect to the parent's
	 *            coordinate system. This list must contain java.awt.Point2D objects.
	 * @param dimensions
	 *            The new dimensions of the nodes with respect to the parent's coordinate
	 *            system. This list must contain java.awt.Dimension2D objects.
	 * @param fixDescendents
	 *            Whether or not to move and resize the descendents of the
	 *            passed in nodes.
	 * @param nodesToBeMoved
	 *            A list of all nodes to be moved (may include descendents if
	 *            fixDescendents is true)
	 * @param transformsToUse
	 *            A list of the transforms to be applied to nodesToBeMoved.
	 */
	private void setBoundsAndCalcTransforms(List nodes, List positions, List dimensions, boolean fixDescendents,
											List nodesToBeMoved, List transformsToUse, boolean animate) {
		if (nodes.size() != positions.size() || nodes.size() != dimensions.size() || positions.size() != dimensions.size()) {
			// System.err.println("vectors are different sizes!");
			return;
		}

		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode sn = (ShrimpNode) nodes.get(i);
			Point2D newPositionWRTParent = (Point2D) positions.get(i);
			Dimension2D newDimensionWRTParent = (Dimension2D) dimensions.get(i);

			// Determine some reasonable dimensions for the node's local coordinate system.
			// NOTE: we select a height of DEFAULT_NODE_DIMENSION to keep things a bit simpler.
			double newLocalHeight = ShrimpNode.DEFAULT_NODE_DIMENSION;
			double whRatio = newDimensionWRTParent.getWidth() / newDimensionWRTParent.getHeight();
			double newLocalWidth = ShrimpNode.DEFAULT_NODE_DIMENSION * whRatio;

			Double outerBounds = sn.getOuterBounds();
			double newLocalX = outerBounds.x; // keep the same
			double newLocalY = outerBounds.y; // keep the same

			// save old bounds with repsect to parent node's coordinate system
			// Rectangle2D.Double oldInnerBoundsWRTParent = GeometryUtils.transform(sn.getInnerBounds(), getTransformOf(sn));
			Rectangle2D.Double oldInnerBounds = sn.getInnerBounds();

			// set new bounds of node (local coordinate system)
			Rectangle2D.Double bounds = new Rectangle2D.Double(newLocalX, newLocalY, newLocalWidth, newLocalHeight);
			if (animate) {
				setBoundsOfShrimpNodeWithAnimation(sn, bounds);
			} else {
				sn.setOuterBounds(bounds);
			}

			// Set the transform that relates the position and size of the local node
			// to its position within the the parent's coordinate system
			double scaleX = newDimensionWRTParent.getWidth() / newLocalWidth;
			double scaleY = newDimensionWRTParent.getHeight() / newLocalHeight;
			// Set the coordinates of the upper left corner (position was specified for centre point)
			double translateX = newPositionWRTParent.getX()	- newDimensionWRTParent.getWidth() / 2.0;
			double translateY = newPositionWRTParent.getY()	- newDimensionWRTParent.getHeight() / 2.0;
			AffineTransform tx = new AffineTransform();
			tx.translate(translateX, translateY);
			if (Math.abs(scaleX - scaleY) > 0.01) {
				System.err.println("scaleX and scaleY not same");
			}
			// we always want a node to be scaled the same in the x and y directions
			tx.scale(scaleX, scaleX);

			if (!nodesToBeMoved.contains(sn)) {
				nodesToBeMoved.add(sn);
				transformsToUse.add(tx);
			}

			// This node's inner bounds contain the descendent nodes
			Rectangle2D.Double newInnerBounds = (Rectangle2D.Double) sn.getInnerBounds().clone();
			if (fixDescendents) {
				fixDescendentTransforms(sn, oldInnerBounds, newInnerBounds, nodesToBeMoved, transformsToUse, animate);
			}
		}
	}

	/**
	 * Moves and resizes the descendents of the passed in node if they do not fit in its new bounds.
	 */
	private void fixDescendentTransforms(ShrimpNode sn, Rectangle2D.Double oldInnerBounds, Rectangle2D.Double newInnerBounds,
										 List nodesToBeMoved, List transformsToUse, boolean animate) {
		double widthDiff = Math .abs(oldInnerBounds.width - newInnerBounds.width);
		double heightDiff = Math.abs(oldInnerBounds.height - newInnerBounds.height);

		// Now that we have changed the size of sn, we must check that its children still
		// fit inside of it if we've made it a different proportion
		Vector children = dataDisplayBridge.getChildNodes(sn);
		if (children.isEmpty()) {
			return;
		}

		if (widthDiff < 0.001 && heightDiff < 0.001) {
			return;
		}

		double minX = java.lang.Double.MAX_VALUE;
		double minY = java.lang.Double.MAX_VALUE;
		double maxX = java.lang.Double.MIN_VALUE;
		double maxY = java.lang.Double.MIN_VALUE;

		// determine position and size of a bounding box that will enclose all grandchildren
		for (Iterator childrenIter = children.iterator(); childrenIter.hasNext(); ) {
			ShrimpNode child = (ShrimpNode) childrenIter.next();
			if (child != null) {
				Rectangle2D.Double boundsWRTParent = GeometryUtils.transform(child.getOuterBounds(),
															(AffineTransform) getTransformOf(child));
				if (boundsWRTParent.getX() < minX) {
					minX = boundsWRTParent.getX();
				}
				if (boundsWRTParent.getY() < minY) {
					minY = boundsWRTParent.getY();
				}
				if (boundsWRTParent.getX() + boundsWRTParent.getWidth() > maxX) {
					maxX = boundsWRTParent.getX() + boundsWRTParent.getWidth();
				}
				if (boundsWRTParent.getY() + boundsWRTParent.getHeight() > maxY) {
					maxY = boundsWRTParent.getY() + boundsWRTParent.getHeight();
				}
			}
		}

		// Calculate size of a bounding box for all grandchildren
		double totalWidth = maxX - minX;
		double totalHeight = maxY - minY;

		// leave a bit of space for labels if the label is off node
		double fontHeight = 0;
		// @tag Shrimp.fitToNodeLabelling
		if (!DisplayConstants.isLabelOnNode(getDefaultLabelMode())) {
			fontHeight = getFontHeightOnCanvas(getLabelFont());
		}

		// Calculate the proportions of the bounding box to the
		// size of the new inner bounds of the parent
		double widthScale = newInnerBounds.getWidth() / totalWidth;
		double heightScale = (newInnerBounds.getHeight() - fontHeight) / totalHeight;
		// Select smallest scale:horizontal or vertical
		double chosenScale = (widthScale <= heightScale) ? widthScale : heightScale;

		// Reduce scale a bit for a bit of a cushion
		chosenScale = chosenScale * 0.90;

		// Calculate the vertical and horizonotal adjustment needed to center the children
		double xAdjust = (newInnerBounds.getWidth() - totalWidth * chosenScale) / 2.0 - minX * chosenScale;
		double yAdjust = ((newInnerBounds.getHeight() - fontHeight) - totalHeight * chosenScale)
								/ 2.0 - minY * chosenScale + fontHeight / 2.0;

		// Gather information on each child's new size and position
		// and then recursively recall this function
		Vector childPositions = new Vector();
		Vector childDimensions = new Vector();
		for (Iterator childIter = children.iterator(); childIter.hasNext(); ) {
			ShrimpNode child = (ShrimpNode) childIter.next();
			Rectangle2D.Double boundsWRTParent = GeometryUtils.transform(child.getOuterBounds(),
													(AffineTransform) getTransformOf(child));
			Dimension2D childDimension = new DoubleDimension();
			childDimension.setSize(boundsWRTParent.width * chosenScale, boundsWRTParent.height * chosenScale);
			childDimensions.add(childDimension);
			Point2D.Double childPosition = new Point2D.Double(
					(boundsWRTParent.x + boundsWRTParent.width / 2.0) * chosenScale + xAdjust,
					(boundsWRTParent.y + boundsWRTParent.height / 2.0) * chosenScale + yAdjust);
			childPositions.add(childPosition);
		}

		setBoundsAndCalcTransforms(children, childPositions, childDimensions, true, nodesToBeMoved, transformsToUse, animate);
	}

	/**
	 * @see DisplayBean#isAnimatingLayouts()
	 */
	public boolean isAnimatingLayouts() {
		Properties props = ApplicationAccessor.getProperties();
		String usingAnimation = props.getProperty(PROPERTY_KEY__USE_ANIMATION, "" + DEFAULT_USING_ANIMATION);
		return Boolean.valueOf(usingAnimation).booleanValue();
	}

	protected abstract void setBoundsOfShrimpNodeWithAnimation(ShrimpNode node, Rectangle2D.Double newBounds);

	/**
	 * @see DisplayBean#setTransformOf(Vector, Vector)
	 */
	public void setTransformOf(Vector objects, Vector transforms) {
		for (int i = 0; i < objects.size(); i++) {
			Object obj = objects.elementAt(i);
			AffineTransform transform = (AffineTransform) transforms.elementAt(i);
			setTransformOf(obj, transform);
		}
	}

	/**
	 * @see DisplayBean#setTransformOf(Object, AffineTransform)
	 */
	public void setTransformOf(Object obj, Object transform) {
		setTransformOf(obj, transform, true, false);
	}

	/**
	 * @see DisplayBean#setTransformOf(java.lang.Object,
	 *      java.awt.geom.AffineTransform, boolean, boolean)
	 */
	public void setTransformOf(Object obj, Object transform, boolean firePositionChangedEvent, boolean animate) {
		if (currentFocusedOnObject != null && currentFocusedOnObject instanceof ShrimpNode) {
			((ShrimpNode) currentFocusedOnObject).setHasFocus(false);
		}
		if (obj instanceof ShrimpNode) {
			setTransformOfNode((ShrimpNode) obj, (AffineTransform) transform, firePositionChangedEvent, animate);
		}
	}

	/**
	 * Move the given node to a location by translating and scaling the artifact
	 * from 0,0 using the given transformation.
	 * @param node The node to move
	 * @param transform The tranformation to use on the node
	 */
	protected void setTransformOfNode(final ShrimpNode node, AffineTransform at,
			boolean firePositionChangedEvent, boolean animate) {
		List nodes = new ArrayList(1);
		nodes.add(node);
		List transforms = new ArrayList(1);
		transforms.add(at);
		setTransformsOfNodes(nodes, transforms, firePositionChangedEvent, animate);
	}

	protected void setTransformsOfNodes(List nodes, List transforms,
			boolean firePositionChangedEvent, boolean animate) {
		if (animate && isAnimatingLayouts()) {
			setTransformsOfNodesWithAnimation(nodes, transforms);
		} else {
			setTransformsOfNodesWithoutAnimation(nodes, transforms, firePositionChangedEvent);
		}
	}

	protected abstract void setTransformsOfNodesWithAnimation(List nodes, List transforms);

	protected void setTransformsOfNodesWithoutAnimation(List nodes, List transforms, boolean firePositionChangedEvent) {
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.get(i);
			AffineTransform destTransform = (AffineTransform) transforms.get(i);
			AffineTransform srcTransform = (AffineTransform) getTransformOf(node);
			if (srcTransform.equals(destTransform)) {
				continue;
			}
			setTransformOfNode(node, destTransform);
			node.setHasBeenTransformedOnce(true);
			node.recomputeCentrePoint();
			if (firePositionChangedEvent) {
				node.firePositionChangedEvent();
			}
		}
	}

	protected abstract void setTransformOfNode(ShrimpNode node, AffineTransform at);

	/**
	 * @see DisplayBean#focusOn(Object)
	 */
	public void focusOn(Object object) {
		// In this implementation, we look only at the first node in the Vector
		// passsed in. In other implementations, we might choose to focus on
		// more than one node at a time.
		if (object instanceof Vector) {
			Vector v = (Vector) object;
			if (v.size() > 0) {
				focusOn(v.firstElement());
			}
		} else {
			if (object instanceof ShrimpNode) {
				final ShrimpNode sn = (ShrimpNode) object;
				focusOnNode(sn, null);
				// open the node if it's not already opened
				if (!isNodeOpen(sn)) {
					openNode(sn);
				}
			} else if (object instanceof ShrimpArc) {
				ShrimpArc sa = (ShrimpArc) object;
				magnifyHandler.focusOn(sa);
				currentFocusedOnObject = sa;
			}
		}
	}

	/**
	 * @see DisplayBean#focusOnNode(ShrimpNode, String)
	 */
	public void focusOnNode(final ShrimpNode sn, String panelMode) {
		ShrimpDisplayObject oldObj = currentFocusedOnObject;
		if (oldObj != null && oldObj instanceof ShrimpNode) {
			ShrimpNode oldSn = (ShrimpNode) oldObj;
			currentFocusedOnObject = null;
			oldSn.setHasFocus(false);
		}

		fireBeforeMagnifyEvent(oldObj, sn);

		// need to hide the handles before zooming - otherwise they get scaled
		// they will appear again after zooming when setIsHighlighted(true) is called
		if (sn != null) {
			((PShrimpNode)sn).removeBoundsHandlesFrom();
		}

		magnifyHandler.focusOn(sn);
		currentFocusedOnObject = sn;
		if (sn != null) {
			sn.setHasFocus(true);
			sn.setIsHighlighted(true);
		}

		// set the panel mode only after we've finished focusing on sn
		if (panelMode != null) {
			setPanelMode(sn, panelMode);
		}

		fireAfterMagnifyEvent(oldObj, sn);
	}

	/**
	 * Returns whether or not the node is open.
	 * @param node The node to check
	 */
	public boolean isNodeOpen(ShrimpNode node) {
		return !node.getPanelMode().equals(PanelModeConstants.CLOSED);
	}

	/**
	 * @see DisplayBean#getCurrentFocusedOnObjects()
	 */
	public Vector getCurrentFocusedOnObjects() {
		Vector currentFocusedOnObjects = new Vector();
		if (currentFocusedOnObject != null) {
			currentFocusedOnObjects.add(currentFocusedOnObject);
		}
		return currentFocusedOnObjects;
	}

	/**
	 * @see DisplayBean#addShrimpMouseListener(ShrimpMouseListener)
	 */
	public void addShrimpMouseListener(ShrimpMouseListener sml) {
		displayInputManager.addShrimpMouseListener(sml);
	}

	/**
	 * @see DisplayBean#addShrimpKeyListener(ShrimpKeyListener)
	 */
	public void addShrimpKeyListener(ShrimpKeyListener skl) {
		displayInputManager.addShrimpKeyListener(skl);
	}

	/**
	 * @see DisplayBean#removeShrimpMouseListener(ShrimpMouseListener)
	 */
	public void removeShrimpMouseListener(ShrimpMouseListener sml) {
		displayInputManager.removeShrimpMouseListener(sml);
	}

	/**
	 * @see DisplayBean#removeShrimpKeyListener(ShrimpKeyListener)
	 */
	public void removeShrimpKeyListener(ShrimpKeyListener skl) {
		displayInputManager.removeShrimpKeyListener(skl);
	}

	/**
	 * @see DisplayBean#addFilterRequestListener(DisplayFilterRequestListener)
	 */
	public void addFilterRequestListener(DisplayFilterRequestListener frl) {
		filterRequestListeners.addElement(frl);
	}

	/**
	 * @see DisplayBean#removeFilterRequestListener(DisplayFilterRequestListener)
	 */
	public void removeFilterRequestListener(DisplayFilterRequestListener frl) {
		filterRequestListeners.removeElement(frl);
	}

	/**
	 * @see DisplayBean#highlight(Object, boolean)
	 */
	public void highlight(Object object, boolean on) {
		if (object instanceof Collection) {
			for (Iterator iter = ((Collection) object).iterator(); iter.hasNext();) {
				highlight(iter.next(), on);
			}
		} else if (object instanceof ShrimpNode) {
			highlightNode((ShrimpNode) object, on);
		} else if (object instanceof ShrimpArc) {
			highlightArc((ShrimpArc) object, on);
		}
	}

	/**
	 * Highlights the given node
	 * @param node The node to highlight
	 */
	private void highlightNode(ShrimpNode sn, boolean on) {
		sn.setIsHighlighted(on);
		if (on && currentFocusedOnObject != null && sn.equals(currentFocusedOnObject)) {
			sn.setHasFocus(true);
		}
	}

	/**
	 * Highlights/Unhighlights the given arc
	 * @param arc The arc to modify
	 * @param on Whether or not to highligh (false = unhighlight)
	 */
	protected void highlightArc(ShrimpArc arc, boolean on) {
		arc.setHighlighted(on);
		this.repaint(); // TODO is this really necessary?
	}

	/**
	 * @see DisplayBean#getDataDisplayBridge()
	 */
	public DataDisplayBridge getDataDisplayBridge() {
		return dataDisplayBridge;
	}

	/**
	 * @see DisplayBean#getPathBetweenObjects(Object, Object)
	 */
	public Vector getPathBetweenObjects(Object source, Object dest) {
		ShrimpNode commonSrcNode = null;
		ShrimpNode commonDestNode = null;

		// Find common source
		if (source == null) {
			// do nothing
		} else if (source instanceof Vector) {
			Vector sourceVec = (Vector) ((Vector) source).clone();
			switchArcsToNodes(sourceVec);
			commonSrcNode = getClosestCommonAncestor(sourceVec);
		} else if (source instanceof ShrimpArc) {
			return new Vector();
		} else if (source instanceof ShrimpNode) {
			commonSrcNode = (ShrimpNode) source;
		} else if (source instanceof ShrimpNodeLabel) {
			commonSrcNode = (ShrimpNode) ((ShrimpLabel) source)
					.getLabeledObject();
		} else if (dest instanceof ShrimpTerminal) {
			commonSrcNode = ((ShrimpTerminal) source).getShrimpNode();
		} else {
			System.err.println("DisplayBean.getPathBetweenObjects can't handle this source object type!: " + source);
		}

		// Find common dest
		if (dest == null) {
			// do nothing
		} else if (dest instanceof Vector) {
			Vector destVec = (Vector) ((Vector) dest).clone();
			switchArcsToNodes(destVec);
			commonDestNode = getClosestCommonAncestor(destVec);
		} else if (dest instanceof ShrimpArc) {
			return new Vector();
		} else if (dest instanceof ShrimpNode) {
			commonDestNode = (ShrimpNode) dest;
		} else if (dest instanceof ShrimpNodeLabel) {
			commonDestNode = (ShrimpNode) ((ShrimpLabel) dest)
					.getLabeledObject();
		} else if (dest instanceof ShrimpTerminal) {
			commonDestNode = ((ShrimpTerminal) dest).getShrimpNode();
		} else {
			System.err.println("DisplayBean.getPathBetweenObjects can't handle this dest object type!: " + dest);
		}

		// Determine the path
		Vector path = getPathBetweenNodes(commonSrcNode, commonDestNode);
		if (!path.isEmpty()) {
			// make sure the source and dest are in the path
			if (source instanceof Vector) {
				// if the source was a vector containing arcs, these arcs will
				// not be
				// in the path yet so we add them here.
				Vector srcVec = (Vector) source;
				for (Iterator iter = srcVec.iterator(); iter.hasNext();) {
					Object element = iter.next();
					if (element instanceof ShrimpArc) {
						path.insertElementAt(element, 0);
					}
				}
			} else if (source instanceof ShrimpArc) {
				path.insertElementAt(source, 0);
			}

			if (dest instanceof Vector) {
				// if the dest was a vector containing arcs, these arcs will not
				// be
				// in the path yet so we add them here.
				Vector destVec = (Vector) dest;
				for (Iterator iter = destVec.iterator(); iter.hasNext();) {
					Object element = iter.next();
					if (element instanceof ShrimpArc) {
						path.insertElementAt(element, path.size());
					}
				}
			} else if (dest instanceof ShrimpArc) {
				path.insertElementAt(dest, path.size());
			}
		}
		return path;
	}

	/**
	 * Converts all arcs in the given vector of arcs and nodes into nodes only
	 * by removing the arcs and adding their endpoints.
	 */
	private void switchArcsToNodes(Vector nodesAndArcs) {
		Vector relNodes = new Vector();
		for (int i = 0; i < nodesAndArcs.size(); i++) {
			if (nodesAndArcs.elementAt(i) instanceof ShrimpArc) {
				ShrimpArc arc = (ShrimpArc) nodesAndArcs.elementAt(i);
				relNodes.add(arc.getSrcNode());
				relNodes.add(arc.getDestNode());
				nodesAndArcs.removeElementAt(i);
				i--;
			}
		}
		for (int i = 0; i < relNodes.size(); i++) {
			nodesAndArcs.add(relNodes.elementAt(i));
		}
	}

	/**
	 * Returns the closest path between two nodes. If the srcNode and destNode
	 * have a common ancestor then the path goes from srcNode to the common
	 * ancestor, then from the common ancestor to destNode. If there is no
	 * common ancestor then the path will go from the srcNode up to its top
	 * level ancestor (a root) then over to destNode's top level ancestor
	 * (another root) and then back down to the destNode
	 * @param srcNode The node to start the path at.
	 * @param destNode The node to end the path at.
	 */
	private Vector getPathBetweenNodes(ShrimpNode srcNode, ShrimpNode destNode) {
		Vector path = new Vector();
		Vector nodes = new Vector();

		// if only the destination node known then make that the only thing on
		// the path
		if (srcNode == null) {
			if (destNode != null) {
				path.add(destNode);
			}
			return path;
		}

		nodes.addElement(srcNode);
		nodes.addElement(destNode);
		ShrimpNode commonAncestor = getClosestCommonAncestor(nodes);

		if (commonAncestor != null) {

			// find the path from the source node to the common ancestor
			int index = 0;
			ShrimpNode nodeOnPath = srcNode.getParentShrimpNode();
			path.insertElementAt(srcNode, index++); // must include source art
			if (!commonAncestor.equals(srcNode)) {
				while (nodeOnPath != null) {
					path.insertElementAt(nodeOnPath, index++);
					if (nodeOnPath.equals(commonAncestor)) {
						nodeOnPath = null;
					} else {
						nodeOnPath = nodeOnPath.getParentShrimpNode();
					}
				}
			}

			// find the path from the common ancestor to the destination
			nodeOnPath = destNode;
			if (!commonAncestor.equals(destNode)) {
				while (nodeOnPath != null) {
					if (nodeOnPath.equals(commonAncestor)) {
						nodeOnPath = null;
					} else {
						path.insertElementAt(nodeOnPath, index);
						nodeOnPath = nodeOnPath.getParentShrimpNode();
					}
				}
			}
		} else {
			if (srcNode != null) {
				path.add(srcNode);
				Vector pathFromSrcToRoot = dataDisplayBridge
						.getFirstPathToRoot(srcNode);
				path.addAll(pathFromSrcToRoot);
			}
			if (destNode != null) {
				Vector pathFromRootToDest = dataDisplayBridge
						.getFirstPathToRoot(destNode);
				Collections.reverse(pathFromRootToDest);
				path.addAll(pathFromRootToDest);
				path.add(destNode);
			}
		}

		return path;
	}

	/**
	 * Returns the first common node from all of the given nodes' ancestor
	 * trees. In other words, this method will get all of the ancestors for each
	 * node passed in. The ancestors will then be searched for the lowest (ie.
	 * closest to the passed in nodes) ancestor in each tree. If there isn't a
	 * common node between the given nodes, null is returned.
	 *
	 * Note: If one node is an ancestor of the others, it will be returned
	 *
	 * @param nodes The nodes to be searched
	 */
	public ShrimpNode getClosestCommonAncestor(Vector nodes) {
		ShrimpNode commonAncestor = null;
		// if 0 elements return nothing
		if (nodes.isEmpty()) {
			commonAncestor = null;
		} else if (nodes.size() == 1) {
			// the closest common ancestor is itself
			commonAncestor = (ShrimpNode) nodes.firstElement();
		} else {
			// start with the assumption that all ancestors of the first node are common
			ShrimpNode firstNode = (ShrimpNode) nodes.firstElement();
			Set commonAncestors = new HashSet(dataDisplayBridge.getAncestorNodes(firstNode));
			commonAncestors.add(firstNode);
			for (int i = 1; i < nodes.size(); i++) {
				ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
				Set ancestors = new HashSet(dataDisplayBridge.getAncestorNodes(node));
				ancestors.add(node);
				commonAncestors.retainAll(ancestors);
			}

			// from all the found common ancestors, get the one lowest down in the tree
			// TODO figure out if this is the overall closest ancestor (ie. has
			// shortest cumulative distance from every node)
			for (Iterator iter = commonAncestors.iterator(); iter.hasNext();) {
				ShrimpNode ancestor = (ShrimpNode) iter.next();
				if (commonAncestor == null || ancestor.getLevel() > commonAncestor.getLevel()) {
					commonAncestor = ancestor;
				}
			}
		}
		return commonAncestor;

	}

	/**
	 * @see DisplayBean#isFiltered(Object)
	 */
	public boolean isFiltered(Object object) {
		boolean isFiltered = false;
		for (Iterator iter = filterRequestListeners.iterator(); iter.hasNext() && !isFiltered; ) {
			DisplayFilterRequestListener listener = (DisplayFilterRequestListener) iter.next();
			isFiltered = listener.isFiltered(object);
		}
		return isFiltered;
	}

	/**
	 * @see DisplayBean#openNode(ShrimpNode)
	 */
	public void openNode(ShrimpNode node) {
		if (isNodeOpen(node)) {
			return;
		}
		String defaultMode = getDefaultNodeMode(node);
		if (defaultMode != null) {
			try {
				setPanelMode(node, defaultMode);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private String getDefaultNodeMode(ShrimpNode node) {
		String defaultMode = null;
		String[] modes = node.getArtifact().getDefaultPanelModeOrder();
		if (modes.length > 0) {
			defaultMode = modes[0];
			// if the first mode is "children" and there are no children then
			// open the node with the second mode specified (if there is one)
			if (PanelModeConstants.CHILDREN.equals(defaultMode) &&
				getDataDisplayBridge().getVisibleChildNodeCount(node) == 0) {
				if (modes.length > 1) {
					defaultMode = modes[1];
				}
			}
		}
		return defaultMode;
	}

	/**
	 * @see DisplayBean#closeNode(ShrimpNode)
	 */
	public void closeNode(ShrimpNode node) {
		if (!isNodeOpen(node)) {
			return;
		}

		ApplicationAccessor.waitCursor();
		try {
			setPanelMode(node, PanelModeConstants.CLOSED);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			ApplicationAccessor.defaultCursor();
		}
	}

	/**
	 * Open or close a node
	 * @param art The node to open/close
	 * @param rememberChildrenPositions Whether or not to remember the children's last known layout
	 */
	protected void openCloseShrimpNode(ShrimpNode node,  boolean rememberChildrenPositions) {
	}

	/**
	 * @see DisplayBean#getAllNodes()
	 */
	public Vector getAllNodes() {
		Vector allNodes = dataDisplayBridge.getShrimpNodes();
		for (Iterator iter = allNodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			if (!node.isInDisplay()) {
				// System.err.println("node not in display: " + node);
				iter.remove();
			}
		}
		return allNodes;
	}

	/**
	 * @see DisplayBean#getVisibleNodes()
	 */
	public Vector getVisibleNodes() {
		Vector nodes = getAllNodes();

		Vector visibleNodes = new Vector(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.get(i);
			if (node.isVisible()) {
				visibleNodes.add(node);
			}
		}
		visibleNodes.trimToSize();
		return (Vector) visibleNodes.clone();
	}

	/**
	 * @see DisplayBean#getAllArcs()
	 */
	public Vector getAllArcs() {
		Vector allArcs = dataDisplayBridge.getShrimpArcs();
		for (Iterator iter = allArcs.iterator(); iter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) iter.next();
			if (!arc.isInDisplay()) {
				// System.err.println("arc not in display: " + arc);
				iter.remove();
			}
		}
		return allArcs;
	}

	/**
	 * @see DisplayBean#getVisibleArcs()
	 */
	public Vector getVisibleArcs() {
		Vector arcs = getAllArcs();

		Vector visibleArcs = new Vector(arcs.size());
		for (int i = 0; i < arcs.size(); i++) {
			ShrimpArc arc = (ShrimpArc) arcs.get(i);
			if (arc.isVisible()) {
				visibleArcs.add(arc);
			}
		}
		visibleArcs.trimToSize();
		return (Vector) visibleArcs.clone();
	}

	/**
	 * @see DisplayBean#getAllLabels()
	 */
	public Vector getAllLabels() {
		Vector allLabels = dataDisplayBridge.getShrimpLabels();
		for (Iterator iter = allLabels.iterator(); iter.hasNext();) {
			ShrimpLabel label = (ShrimpLabel) iter.next();
			if (!label.isInDisplay()) {
				// System.err.println("label not in display: " + label);
				iter.remove();
			}
		}
		return allLabels;
	}

	/**
	 * @see DisplayBean#getVisibleLabels()
	 */
	public Vector getVisibleLabels() {
		Vector labels = getAllLabels();

		Vector visibleLabels = new Vector(labels.size());
		for (int i = 0; i < labels.size(); i++) {
			ShrimpLabel label = (ShrimpLabel) labels.get(i);
			if (label.isVisible()) {
				visibleLabels.add(label);
			}
		}
		visibleLabels.trimToSize();
		return (Vector) visibleLabels.clone();
	}

	/**
	 * @see DisplayBean#setCprels(String[])
	 */
	public void setCprels(String[] cprels) {
		this.cprels = cprels;
	}

	/**
	 * @see DisplayBean#getCprels()
	 */
	public String[] getCprels() {
		return cprels;
	}

	/**
	 * @see DisplayBean#setInverted(boolean)
	 */
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	/**
	 * @see DisplayBean#isInverted()
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * Note that the {@link StraightSolidLineArcStyle} is added by default.
	 * @see DisplayBean#addArcStyle(ArcStyle, boolean)
	 */
	public void addArcStyle(ArcStyle arcStyle, boolean makeDefault) {
		arcStyles.put(arcStyle.getName(), arcStyle);
		if (makeDefault) {
			arcStyles.put("Default", arcStyle);
			// make sure it doesn't exist, then add to the front of the list
			arcStylesOrdered.remove(arcStyle);
			arcStylesOrdered.add(0, arcStyle);
		} else {
			if (!arcStylesOrdered.contains(arcStyle)) {
				arcStylesOrdered.add(arcStyle);
			}
		}
	}

	/**
	 * Note that the StraightSolidLineArcStyle can't be removed.
	 * @see DisplayBean#removeArcStyle(ArcStyle)
	 */
	public void removeArcStyle(ArcStyle arcStyle) {
		if (!(arcStyle instanceof StraightSolidLineArcStyle)) {
			arcStyles.remove(arcStyle.getName());
			arcStylesOrdered.remove(arcStyle);
		}
	}

	/**
	 * Returns whether or not this display can display a certain arc style
	 */
	protected boolean hasArcStyle(String arcStyleName) {
		return arcStyles.get(arcStyleName) != null;
	}

	/** Returns the arcstyle with the given name */
	public ArcStyle getArcStyle(String arcStyleName) {
		if (arcStyles.get(arcStyleName) != null) {
			return (ArcStyle) arcStyles.get(arcStyleName);
		}
		return (ArcStyle) arcStyles.get("Default");
	}

	/** Returns all the arc styles currently in this display bean. */
	public Vector getArcStyles() {
		//Set sortedArcStyles = new TreeSet(arcStylesOrdered);	// sort?
		return new Vector(arcStylesOrdered);
	}

	/**
	 * @see DisplayBean#addNodeShape(NodeShape, boolean)
	 */
	public void addNodeShape(NodeShape nodeShape, boolean makeDefault) {
		nodeShapes.put(nodeShape.getName(), nodeShape);
		if (makeDefault) {
			nodeShapes.put("Default", nodeShape);
			// make sure it doesn't exist, then add it to the front of the list
			nodeShapesOrdered.remove(nodeShape);
			nodeShapesOrdered.add(0, nodeShape);
		} else {
			// make sure it doesn't exist
			if (!nodeShapesOrdered.contains(nodeShape)) {
				nodeShapesOrdered.add(nodeShape);
			}
		}
	}

	/**
	 * @see DisplayBean#removeNodeShape(NodeShape)
	 */
	public void removeNodeShape(NodeShape nodeShape) {
		if (!(nodeShape instanceof RectangleNodeShape)) {
			nodeShapes.remove(nodeShape.getName());
			nodeShapesOrdered.remove(nodeShape);
		}
	}

	/**
	 * @param nodeShapeName
	 * @return Whehter or not this displayBean has the given node shape.
	 */
	protected boolean hasNodeShape(String nodeShapeName) {
		return nodeShapes.get(nodeShapeName) != null;
	}

	/**
	 * @see DisplayBean#getNodeShape(java.lang.String)
	 */
	public NodeShape getNodeShape(String nodeShapeName) {
		if (nodeShapes.get(nodeShapeName) != null) {
			return (NodeShape) nodeShapes.get(nodeShapeName);
		}
		return (NodeShape) nodeShapes.get("Default");
	}

	/**
	 * These shapes will be in the order that they were added to this display bean.
	 * @see DisplayBean#getNodeShapes()
	 */
	public Vector getNodeShapes() {
		// Set sortedNodeStyles = new TreeSet(nodeStylesOrdered.values());	// sort?
		return new Vector(nodeShapesOrdered);
	}

	/**
	 * @see DisplayBean#getLabelStyles()
	 */
	public Vector getLabelStyles() {
		return new Vector(labelStyles);
	}

	public Vector getBorderStyles() {
		return borderStyles;
	}

	/**
     * @tag Shrimp.sequence : Necessary to prevent node and edge dragging in certain layouts.
     * @return boolean
     */
    public boolean isNodeEdgeMovementAllowed() {
		return nodeEdgeMovementAllowed;
    }

    /**
     * @tag Shrimp.sequence : Necessary to prevent node and edge dragging in certain layouts.
     */
    public void setNodeEdgeMovement(boolean movementAllowed) {
    	this.nodeEdgeMovementAllowed = movementAllowed;
    }

	/**
	 * Refresh the layout, possibly using animation.
	 */
	public void refreshLayout(Vector targets, boolean animate) {
		Vector nodes;
		// Create a node list based on the siblings of the first selected node
		if (targets.isEmpty()) {
			nodes = getDataDisplayBridge().getRootNodes();
		} else {
			HashSet siblings = new HashSet();
			for (int i = 0; i < targets.size(); i++) {
				ShrimpNode node = (ShrimpNode) targets.get(i);
				ShrimpNode parentNode = node.getParentShrimpNode();
				if (parentNode == null) {
					siblings.addAll(dataDisplayBridge.getRootNodes());
				} else {
					siblings.addAll(dataDisplayBridge.getChildNodes(parentNode));
				}
			}
			nodes = new Vector(siblings);
		}
		Collections.sort(nodes);
		setLayoutMode(nodes, getLastLayoutMode(), false, animate);
		requestFocus(); // give focus to the canvas
	}

	/**
	 * Refresh the layout using animation.
	 */
	public void refreshLayout(Vector targets) {
		refreshLayout(targets, true);
	}

	/**
	 * Refresh the layout with animation.
	 */
	public void refreshLayout() {
		refreshLayout(true);
	}

	public void refreshLayout(boolean animate) {
		refreshLayout(new Vector(), animate);
	}

	/**
	 * @return the last layout mode.
	 */
	public String getLastLayoutMode() {
		return lastLayoutMode;
	}

	public StructuralGroupingManager getStructuralGroupingManager() {
		return groupingManager;
	}

	public LifelineGroupingManager getLifelineGroupingManager() {
		return lifeLineGroupingManager;
	}

	public MethodExecGroupingManager getMethodExecGroupingManager() {
		return methodExecGroupingManager;
	}
}