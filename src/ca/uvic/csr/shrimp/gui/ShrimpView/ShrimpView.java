/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.eclipse.mylar.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.mylar.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.mylar.zest.layouts.algorithms.VerticalLayoutAlgorithm;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CurvedDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CurvedSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShapes;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.event.ModeChangeEvent;
import ca.uvic.csr.shrimp.DisplayBean.layout.ForceDirectedLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.HierarchicalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.HorizontalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.HorizontalTreeLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.MotionLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.OrthogonalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.RadialLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SequenceLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.ShrimpCascadeLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.ShrimpGridLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.ShrimpSpringLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SugiyamaLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.TreeMapLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.VerticalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.VerticalTreeLayout;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.ScriptingBean.DefaultMainViewScriptingBean;
import ca.uvic.csr.shrimp.ScriptingBean.ScriptingBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplicationAdapter;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplicationEvent;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectAdapter;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectListener;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractViewTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.AnnotationPanelActionAdapter;
import ca.uvic.csr.shrimp.adapter.AttrToVisVarChangeAdapter;
import ca.uvic.csr.shrimp.adapter.ChangeCPRelAdapter;
import ca.uvic.csr.shrimp.adapter.CodeURLRequestAdapter;
import ca.uvic.csr.shrimp.adapter.DataTypesChangeAdapter;
import ca.uvic.csr.shrimp.adapter.DisplayFilterChangedAdapter;
import ca.uvic.csr.shrimp.adapter.DisplayFilterRequestAdapter;
import ca.uvic.csr.shrimp.adapter.DocumentsPanelActionAdapter;
import ca.uvic.csr.shrimp.adapter.JavaDocURLRequestAdapter;
import ca.uvic.csr.shrimp.adapter.OpenShrimpOptionsAdapter;
import ca.uvic.csr.shrimp.adapter.ProtegeDoubleClickAdapter;
import ca.uvic.csr.shrimp.adapter.RelationshipAddAdapter;
import ca.uvic.csr.shrimp.adapter.ResetProjectAdapter;
import ca.uvic.csr.shrimp.adapter.SelectedArcsChangeAdapter;
import ca.uvic.csr.shrimp.adapter.SelectedNodesChangeAdapter;
import ca.uvic.csr.shrimp.adapter.ShrimpInputAdapter;
import ca.uvic.csr.shrimp.adapter.ZoomModeChangeAdapter;
import ca.uvic.csr.shrimp.adapter.mouse.MouseHighlightArcAdapter;
import ca.uvic.csr.shrimp.adapter.mouse.MouseSelectAndMoveAdapter;
import ca.uvic.csr.shrimp.adapter.mouse.ShrimpMouseWrapperAdapter;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManagerListener;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsAddedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsModifiedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsRemovedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ToolBarConfigurator;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.AttachDocumentToNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.AttachURLToNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeLabelIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeOverlayIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.CloseSelectedNodesAdapter;
import ca.uvic.csr.shrimp.usercontrols.CollapseAllDescendentsAdapter;
import ca.uvic.csr.shrimp.usercontrols.FilterSelectedArcsAdapter;
import ca.uvic.csr.shrimp.usercontrols.FilterSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.FisheyeInAdapter;
import ca.uvic.csr.shrimp.usercontrols.FisheyeOutAdapter;
import ca.uvic.csr.shrimp.usercontrols.FocusOnHomeAdapter;
import ca.uvic.csr.shrimp.usercontrols.GroupSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.LayoutModeChangeAdapter;
import ca.uvic.csr.shrimp.usercontrols.MagnifyInAdapter;
import ca.uvic.csr.shrimp.usercontrols.MagnifyOutAdapter;
import ca.uvic.csr.shrimp.usercontrols.NavigateToArcDestAdapter;
import ca.uvic.csr.shrimp.usercontrols.NavigateToArcSourceAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenAllAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenCloseNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenSelectedNodesAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanEastAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanNorthAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanSouthAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanWestAdapter;
import ca.uvic.csr.shrimp.usercontrols.PruneSubgraphAdapter;
import ca.uvic.csr.shrimp.usercontrols.RedoActionAdapter;
import ca.uvic.csr.shrimp.usercontrols.RemoveNodeLabelIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.RemoveNodeOverlayIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.RenameSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectAllChildrenAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectAllDescendantsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectConnectedNodesAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectInverseSiblingsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SnapShotAdapter;
import ca.uvic.csr.shrimp.usercontrols.SpaceInvadersAdapter;
import ca.uvic.csr.shrimp.usercontrols.UndoActionAdapter;
import ca.uvic.csr.shrimp.usercontrols.UnfilterAllArcsByIdAdapter;
import ca.uvic.csr.shrimp.usercontrols.UnfilterAllByIdAdapter;
import ca.uvic.csr.shrimp.usercontrols.UngroupSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.ViewDocumentsAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomInAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomInZoomModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutZoomModeAdapter;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.NodeAttributeComparator;
import ca.uvic.csr.shrimp.util.NodeNameComparator;
import ca.uvic.csr.shrimp.util.NodeNumChildrenComparator;
import ca.uvic.csr.shrimp.util.NodeNumRelsComparator;
import ca.uvic.csr.shrimp.util.NodeTypeComparator;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * Class ShrimpView serves as the connector of all the beans and adapters.
 * It joins the project together into a single view.
 *
 * @author many
 */
public class ShrimpView extends AbstractViewTool implements ViewTool {

	// constants for saved label Properties
 	public static final String SV_LABEL_FONT_KEY = "SV Label Font";
 	public static final String SV_LABEL_MODE_KEY = "SV Label Mode";
 	public static final String SV_LABEL_FADE_OUT_KEY = "SV Label Fade Out";
 	public static final String SV_LABEL_BACKGROUND_OPAQUE_KEY = "SV Label Background Opaque";
 	public static final String SV_LABEL_LEVELS_KEY = "SV Label Levels";
 	public static final String SV_SHOW_ARC_LABELS_KEY = "SV Show Arc Labels";
 	public static final String SV_SWITCH_LABEL_MODE_KEY = "SV Switch Label Mode";
 	public static final String SV_SWITCH_LABEL_MODE_NUM_KEY = "SV Switch Label Mode Number";

	public static final String STATIC_RENDER_QUALITY_KEY = "static render quality";
	public static final String DYNAMIC_RENDER_QUALITY_KEY = "dynamic render quality";

	public static final String MOUSE_MODE_PROPERTY_KEY = "shrimp_mouse_mode";
	public static final String MOUSE_MODE_DEFAULT = DisplayConstants.MOUSE_MODE_SELECT;

	// cursors
	// *** Note: The mac likes a 16x16 image for the cursor, or else the cursor will be invisible
	private static final boolean isMac = (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1);
	public static final Cursor CURSOR_SELECT = createCursor((isMac ? "cursor_select_16.gif" : "cursor_select_32.gif"), DisplayConstants.MOUSE_MODE_SELECT);
	public static final Cursor CURSOR_ZOOM_IN = createCursor((isMac ? "cursor_zoom_in_16.gif" : "cursor_zoom_in_32.gif"), DisplayConstants.MOUSE_MODE_ZOOM_IN);
	public static final Cursor CURSOR_ZOOM_OUT = createCursor((isMac ? "cursor_zoom_out_16.gif" : "cursor_zoom_out_32.gif"), DisplayConstants.MOUSE_MODE_ZOOM_OUT);
	private static Cursor createCursor(String cursorImage, String name) {
		return Toolkit.getDefaultToolkit().createCustomCursor(ResourceHandler.getResourceImage(cursorImage), new Point(0, 0), name);
	}

	// Adapters used to tie the bean together
	//private DataChangeAdapter dataChangeAdapter;
	private DataTypesChangeListener dataTypesChangeAdapter;
	private RelationshipAddAdapter relationshipAddAdapter;

	private ShrimpInputAdapter shrimpInputAdapter;
	// @tag Shrimp.Collapse
	private OpenCloseNodeAdapter openCloseNodeAdapter;
	private MouseSelectAndMoveAdapter mouseSelectAndMoveAdapter;
	private ShrimpMouseWrapperAdapter shrimpMouseWrapperAdapter;
	private MouseHighlightArcAdapter mouseHighlightArcAdapter;
	private DisplayFilterChangedAdapter filterChangedAdapter;
	private SnapShotAdapter snapShotAdapter;
	private SelectedNodesChangeAdapter selectedNodesChangeAdapter;
	private SelectedArcsChangeAdapter selectedArcsChangeAdapter;

	//---- DisplayBean adapters
	private AttrToVisVarChangeAdapter attrToVisVarChangeAdapter;
	private DisplayFilterRequestAdapter filterRequestAdapter;
	private CodeURLRequestAdapter codeUrlRequestAdapter;
	private JavaDocURLRequestAdapter javaDocUrlRequestAdapter;
	private ProtegeDoubleClickAdapter protegeDoubleClickAdapter;
	private AnnotationPanelActionAdapter annotationPanelActionAdapter;
	private DocumentsPanelActionAdapter documentsPanelActionAdapter;
	private ChangeCPRelAdapter changeCPRelAdapter;
	private ResetProjectAdapter resetProjectAdapter;
	private PanEastAdapter panEastAdapter;
	private PanWestAdapter panWestAdapter;
	private PanNorthAdapter panNorthAdapter;
	private PanSouthAdapter panSouthAdapter;
	private ZoomInZoomModeAdapter zoomInAdapter;
	private ZoomOutZoomModeAdapter zoomOutAdapter;
	private ZoomInAnyModeAdapter zoomInAnyModeAdapter;
	private ZoomOutAnyModeAdapter zoomOutAnyModeAdapter;
	private MagnifyInAdapter magnifyInAdapter;
	private MagnifyOutAdapter magnifyOutAdapter;
	private FisheyeInAdapter fisheyeInAdapter;
	private FisheyeOutAdapter fisheyeOutAdapter;

	//---- Menu adapters
	private FocusOnHomeAdapter focusOnHomeAdapter;
	private LayoutModeChangeAdapter layoutChildrenAdapter;
	private LayoutModeChangeAdapter layoutSelectedAdapter;
	private ZoomModeChangeAdapter zoomModeChangeAdapter;
	private RedoActionAdapter redoActionAdapter;
	private UndoActionAdapter undoActionAdapter;
	//private CutCopySelectedArtifactAdapter cutSelectedArtifactAdapter;
	//private CutCopySelectedArtifactAdapter copySelectedArtifactAdapter;
	//private PasteCurrentArtifactAdapter pasteCurrentArtifactAdapter;
	private PruneSubgraphAdapter pruneSubgraphAdapter;
	private FilterSelectedArtifactsAdapter hideSelectedArtifactsAdapter;
	private UnfilterAllByIdAdapter unfilterAllByIdAdapter;
	// @tag Shrimp.grouping
	private GroupSelectedArtifactsAdapter groupSelectedArtifactsAdapter;
	private RenameSelectedArtifactsAdapter renameSelectedNodeAdapter;
	private UngroupSelectedArtifactsAdapter ungroupSelectedArtifactsAdapter;
	//private OpenSearchAdapterOld openSearchAdapter;

    private OpenAllAdapter openAllAdapter;
	//private AddNodeToSelectedNodesAdapter addArtifactToSelectedArtifactsAdapter;
	//private DeleteSelectedArtifactsAdapter deleteSelectedArtifactsAdapter;

	private CloseSelectedNodesAdapter closeSelectedNodesAdapter;
	private CloseSelectedNodesAdapter showClosedAdapter;
	//private CloseSelectedNodesAdapter closeArtifactsAndRememberAdapter;
	private OpenSelectedNodesAdapter expandSelectedNodesAdapter;
	private OpenSelectedNodesAdapter showChildrenOfSelectedNodesAdapter;
	private CollapseAllDescendentsAdapter collapseAllDescendentsAdapter;
	private SelectAllChildrenAdapter selectAllChildrenAdapter;
	private SelectAllDescendantsAdapter selectAllDescendentsAdapter;
	private SelectInverseSiblingsAdapter selectInverseSiblingsAdapter;
	private SelectConnectedNodesAdapter selectOutgoingNodesAdapter;
	private SelectConnectedNodesAdapter selectIncomingNodesAdapter;
	private SelectConnectedNodesAdapter selectIncomingAndOutgoingNodesAdapter;
	//private ShowConnectedNodesAdapter showOutgoingNodesAdapter;
	//private ShowConnectedNodesAdapter showIncomingNodesAdapter;
	//private ShowConnectedNodesAdapter showIncomingAndOutgoingNodesAdapter;

	private CheckBoxAction magnifyModeAction; //changes to magnify mode
	private CheckBoxAction fisheyeModeAction; //changes to fisheye mode
	private CheckBoxAction zoomModeAction; //changes to zoom mode

	// @tag Shrimp.DocumentManager
	private ViewDocumentsAdapter viewDocumentsAdapter;
	private AttachDocumentToNodeAdapter attachDocumentToNodeAdapter;

	// @tag Shrimp.ChangeNodeIcon
	private ChangeNodeLabelIconAdapter changeLabelIconAdapter;
	private ChangeNodeOverlayIconAdapter changeOverlayIconAdapter;

	private HashMap menuActions = new HashMap();

	// menus and their adapters
	private JPopupMenu nodePopupMenu;
	private JPopupMenu arcPopupMenu;
	private ArcPopupListener arcPopupListener;
	private NodePopupListener nodePopupListener;
	private ActionManagerListener nodeActionManagerListener;
	private ActionManagerListener arcActionManagerListener;
	private NavigatePopupMenuAdapter navigatePopupMenuAdapter;
	//private ToolsPopupMenuAdapter toolsPopupMenuAdapter;

	//project adapters
	private ShrimpProjectListener shrimpProjectListener;

	private Properties appProps;

	private Vector shrimpViewListeners = new Vector();

	// The current child parent relationships being used in this view
	protected String[] cprels = new String[0];

	/** Copy buffer for artifacts */
	protected Vector artifactCopyBuffer;
	protected String cprelForCopyBuffer;

	///** The user control adapters */
	protected Vector userControls;

	private JRootPane gui;
	private JToolBar toolBar;
	private QuickSearchPanel quickSearchPanel;

	private ToolBarConfigurator configurator;
	private String mouseMode = MOUSE_MODE_DEFAULT;

	private DataBean dataBean;
    private boolean inverted;

	private ShrimpViewConfiguration config; // configuration for use as an embedded application

	public ShrimpView(ShrimpProject project, ShrimpViewConfiguration config) {
		super(ShrimpProject.SHRIMP_VIEW, project);
		this.config = config;

		try {
			this.dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			this.cprels = dataBean.getDefaultCprels();
            this.inverted = dataBean.getDefaultCprelsInverted();
		} catch (BeanNotFoundException e) {
			// do nothing
		}
	}

	public void init() {
		initDataStructures();
		initLayoutsAndDisplayStyles();
		initAdapters();
		if (config.enableUserControls) {
			initUserControls();
		}
		initDisplay();
		initShrimpListeners();
	}

	private void initDataStructures() {
		// init Data Structures
		appProps = ApplicationAccessor.getProperties();

		// init the gui
		gui = new JRootPane();
		gui.getContentPane().setLayout(new BorderLayout());
		try {
			final DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);

			JPanel mainPanel = new JPanel(new BorderLayout());
			gui.getContentPane().add(mainPanel, BorderLayout.CENTER);

			if (config.showQuickSearchPanel) {
				// @tag Shrimp.QuickSearch : add the quick search panel to ShrimpView
				quickSearchPanel = new QuickSearchPanel(new QuickSearchHelper(this));
				mainPanel.add(quickSearchPanel, BorderLayout.NORTH);
			}

			//@tag Shrimp.Scrollbars: do we want scrollbars on the main shrimp view?
			String showScrollbars = appProps.getProperty(ShrimpApplication.USE_SCROLLPANE, "true");
			boolean showScrollBars = "true".equalsIgnoreCase(showScrollbars);
			PCanvas canvas = ((PNestedDisplayBean)displayBean).getPCanvas();
			if (showScrollBars) {
				// Hides any custom panels on scroll
				AdjustmentListener scrollListener = new AdjustmentListener() {
					//Gets called whenever the scrollbar value changes, either by the user or programmatically.
			        public void adjustmentValueChanged(AdjustmentEvent evt) {
						if ((displayBean != null) && (displayBean.getCurrentFocusedOnObjects().size() > 0)) {
							Object obj = displayBean.getCurrentFocusedOnObjects().get(0);
							if (obj instanceof PShrimpNode) {
								// for some reason the customized panel doesn't move when a scroll happens
								// so we have to do it ourselves.  This keeps the panel centered on the node
								((PShrimpNode)obj).centerCustomizedPanelOnNode(true);
							}
							// This doesn't work because the scrollbars positions change on layout
							// and this hides the customized panel before it gets shown
							//displayBean.focusOnNode(null, null);
						}
			        }
				};
				PScrollPane pScrollPane = new PScrollPane(canvas);
				pScrollPane.getHorizontalScrollBar().addAdjustmentListener(scrollListener);
				pScrollPane.getVerticalScrollBar().addAdjustmentListener(scrollListener);
				mainPanel.add(pScrollPane, BorderLayout.CENTER);
			} else {
				mainPanel.add(canvas, BorderLayout.CENTER);
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void createShrimpViewBeansForProject() {
		FilterBean filterBean;
		DisplayBean displayBean;
		SelectorBean selectorBean;
		ActionHistoryBean actionHistoryBean;
		//PersistentStorageBean persistentStorageBean;

		//ScriptingTool sm = new ScriptingTool();
		//project.addTool(ShrimpProject.SCRIPTING_TOOL, sm);

		//---- DATA DISPLAY BRIDGE
		DataDisplayBridge dataDisplayBridge = new DataDisplayBridge(this);

    	//---- DISPLAY BEAN
		displayBean = new PNestedDisplayBean(project, cprels, dataDisplayBridge);
        displayBean.setInverted(inverted);
		addBean(ShrimpTool.DISPLAY_BEAN, displayBean);

		//---- SELECTOR BEAN
		selectorBean = new SelectorBean();
		selectorBean.setSelected(DisplayConstants.ZOOM_MODE, DisplayConstants.MAGNIFY);
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector());
		addBean(ShrimpTool.SELECTOR_BEAN, selectorBean);

		//---- ACTIONHISTORY BEAN
		// create the secondary Beans and init
		actionHistoryBean = new ActionHistoryBean();
		addBean(ShrimpTool.ACTION_HISTORY_BEAN, actionHistoryBean);


		//---- FILTER BEAN
		filterBean = new FilterBean();
		addBean(ShrimpTool.DISPLAY_FILTER_BEAN, filterBean);

		//---- PERSISTENT STORAGE BEAN
		//persistentStorageBean = new PRJPersistentStorageBean();  //used for saving views
		//addBean(ShrimpProject.PERSISTENT_STORAGE_BEAN, persistentStorageBean);
	}

	private void initLayoutsAndDisplayStyles() {

		try {
			//Retrieve the beans necessary
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);

			Layout layout = new ShrimpGridLayout(displayBean, NodeNameComparator.NODE_NAME_COMPARATOR, LayoutConstants.LAYOUT_GRID_BY_ALPHA, new GridLayoutAlgorithm());
			displayBean.addLayout(layout);
			layout = new ShrimpGridLayout(displayBean, new NodeNumChildrenComparator(displayBean), LayoutConstants.LAYOUT_GRID_BY_NUM_CHILDREN, new GridLayoutAlgorithm());
			displayBean.addLayout(layout);
			// @tag Shrimp(SortByRelationships) : only sorting on visible relationships now
			layout = new ShrimpGridLayout(displayBean, new NodeNumRelsComparator(displayBean), LayoutConstants.LAYOUT_GRID_BY_NUM_RELS, new GridLayoutAlgorithm());
			displayBean.addLayout(layout);
			layout = new ShrimpGridLayout(displayBean, NodeTypeComparator.NODE_TYPE_COMPARATOR, LayoutConstants.LAYOUT_GRID_BY_TYPE, new GridLayoutAlgorithm());
			displayBean.addLayout(layout);
			layout = new ShrimpGridLayout(displayBean, NodeAttributeComparator.NODE_ATTRIBUTE_COMPARATOR, LayoutConstants.LAYOUT_GRID_BY_ATTRIBUTE, new GridLayoutAlgorithm());
			displayBean.addLayout(layout);
			layout = new ShrimpGridLayout(displayBean, NodeNameComparator.NODE_NAME_COMPARATOR, LayoutConstants.LAYOUT_HORIZONTAL, new HorizontalLayoutAlgorithm());
			displayBean.addLayout(layout);
			layout = new ShrimpGridLayout(displayBean, NodeNameComparator.NODE_NAME_COMPARATOR, LayoutConstants.LAYOUT_VERTICAL, new VerticalLayoutAlgorithm());
			displayBean.addLayout(layout);
			layout = new VerticalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_VERTICAL, false);
			displayBean.addLayout(layout);
			layout = new HorizontalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_HORIZONTAL, false);
			displayBean.addLayout(layout);
			layout = new VerticalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_VERTICAL_INVERTED, true);
			displayBean.addLayout(layout);
			layout = new HorizontalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_HORIZONTAL_INVERTED, true);
			displayBean.addLayout(layout);

			layout = new ShrimpSpringLayout(displayBean);
			displayBean.addLayout(layout);

			layout = new RadialLayout(displayBean, LayoutConstants.LAYOUT_RADIAL, false);
			displayBean.addLayout(layout);
			layout = new RadialLayout(displayBean, LayoutConstants.LAYOUT_RADIAL_INVERTED, true);
			displayBean.addLayout(layout);

			if (OrthogonalLayout.isLoaded()) {
				layout = new HierarchicalLayout(displayBean, LayoutConstants.LAYOUT_HIERARCHICAL);
				displayBean.addLayout(layout);
				layout = new OrthogonalLayout(displayBean, LayoutConstants.LAYOUT_ORTHOGONAL);
				displayBean.addLayout(layout);
			}

			//@tag Shrimp(sequence)
			try {
				layout = new SequenceLayout(displayBean, LayoutConstants.LAYOUT_SEQUENCE);
				displayBean.addLayout(layout);
			} catch (NoClassDefFoundError ignore) {
			}

			// @tag Shrimp(sugiyama)
			if (SugiyamaLayout.isInstalled()) {
				layout = new SugiyamaLayout(displayBean, LayoutConstants.LAYOUT_SUGIYAMA);
				displayBean.addLayout(layout);
			}

			layout = new TreeMapLayout(displayBean);
			displayBean.addLayout(layout);

			layout = new ShrimpCascadeLayout(displayBean);
			displayBean.addLayout(layout);
			layout = new HorizontalLayout(displayBean, false);	// overlapping
			displayBean.addLayout(layout);
			layout = new HorizontalLayout(displayBean, true);	// no overlapping
			displayBean.addLayout(layout);
			layout = new VerticalLayout(displayBean);
			displayBean.addLayout(layout);

			layout = new MotionLayout(displayBean, LayoutConstants.LAYOUT_MOTION);
			displayBean.addLayout(layout);

			//layout = new ShrimpUMLLayout(displayBean);
			//displayBean.addLayout(layout);
			//layout = new TGLayout(displayBean);
			//displayBean.addLayout(layout);

			if (ForceDirectedLayout.isPrefuseInstalled()) {
				//ensure that the prefuse_force.jar file exists
				layout = new ForceDirectedLayout(displayBean);
				displayBean.addLayout(layout);
			}

			// add the arc styles
			displayBean.addArcStyle(new StraightSolidLineArcStyle(), true);
			displayBean.addArcStyle(new StraightDottedLineArcStyle(), false);
			displayBean.addArcStyle(new CurvedSolidLineArcStyle(), false);
			displayBean.addArcStyle(new CurvedDottedLineArcStyle(), false);
			//displayBean.addArcStyle(new CompositeArcStyle(), false);

			// @tag Shrimp.NodeShapes : this is where the node shapes are initialized
			NodeShape[] shapes = NodeShapes.getNodeShapes();
			for (int i = 0; i < shapes.length; i++) {
				displayBean.addNodeShape(shapes[i], false);
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initAdapters() {
		//Event adapters
		snapShotAdapter = new SnapShotAdapter(project, ShrimpProject.SHRIMP_VIEW);
		layoutChildrenAdapter = new LayoutModeChangeAdapter(this, LayoutModeChangeAdapter.APPLY_TO_CHILDREN_OF_SELECTED);
		layoutSelectedAdapter = new LayoutModeChangeAdapter(this, LayoutModeChangeAdapter.APPLY_TO_SELECTED);
		zoomModeChangeAdapter = new ZoomModeChangeAdapter(this);
		redoActionAdapter = new RedoActionAdapter(this, project.getActionManager());
		undoActionAdapter = new UndoActionAdapter(this, project.getActionManager());
		changeCPRelAdapter = new ChangeCPRelAdapter(project);
		resetProjectAdapter = new ResetProjectAdapter(project);

		//Node adapters
		openAllAdapter = new OpenAllAdapter(project, this);
		//addArtifactToSelectedArtifactsAdapter = new AddNodeToSelectedNodesAdapter();
		//deleteSelectedArtifactsAdapter = new DeleteSelectedArtifactsAdapter(this);
		closeSelectedNodesAdapter = new CloseSelectedNodesAdapter(ShrimpConstants.ACTION_NAME_COLLAPSE, this);
		showClosedAdapter = new CloseSelectedNodesAdapter(PanelModeConstants.CLOSED, this);
		expandSelectedNodesAdapter = new OpenSelectedNodesAdapter(ShrimpConstants.ACTION_NAME_EXPAND, project, this, false);
		showChildrenOfSelectedNodesAdapter = new OpenSelectedNodesAdapter(PanelModeConstants.CHILDREN, ResourceHandler.getIcon("icon_panel_mode_children.gif"), project, this, false);
		collapseAllDescendentsAdapter = new CollapseAllDescendentsAdapter(project, this);

		// @tag Shrimp.grouping
		groupSelectedArtifactsAdapter = new GroupSelectedArtifactsAdapter(this);
		renameSelectedNodeAdapter = new RenameSelectedArtifactsAdapter(project, this);
		ungroupSelectedArtifactsAdapter = new UngroupSelectedArtifactsAdapter(this);

		// @tag Shrimp.FilterAllDestinationNodes
		pruneSubgraphAdapter = new PruneSubgraphAdapter(this);
		hideSelectedArtifactsAdapter = new FilterSelectedArtifactsAdapter(this);
		unfilterAllByIdAdapter = new UnfilterAllByIdAdapter(this);

		// @tag Shrimp.DocumentManager : initialize ShrimpView adapter
		attachDocumentToNodeAdapter = new AttachDocumentToNodeAdapter(this);
		viewDocumentsAdapter = new ViewDocumentsAdapter(this);

		// @tag Shrimp.ChangeNodeIcon
		changeLabelIconAdapter = new ChangeNodeLabelIconAdapter(this);
		changeOverlayIconAdapter = new ChangeNodeOverlayIconAdapter(this);

		focusOnHomeAdapter = new FocusOnHomeAdapter(this);

		//---- DataBean adapters
		dataTypesChangeAdapter = new DataTypesChangeAdapter(project, this);
		relationshipAddAdapter = new RelationshipAddAdapter(this);

		//---- DisplayBean adapters
		shrimpInputAdapter = new ShrimpInputAdapter(this);
		selectAllChildrenAdapter = new SelectAllChildrenAdapter(this);
		selectAllDescendentsAdapter = new SelectAllDescendantsAdapter(this);
		selectInverseSiblingsAdapter = new SelectInverseSiblingsAdapter(this);
		selectIncomingAndOutgoingNodesAdapter = new SelectConnectedNodesAdapter(project, this, DisplayConstants.INCOMING_AND_OUTGOING);
		selectIncomingNodesAdapter = new SelectConnectedNodesAdapter(project, this, DisplayConstants.INCOMING);
		selectOutgoingNodesAdapter = new SelectConnectedNodesAdapter(project, this, DisplayConstants.OUTGOING);
		//showIncomingAndOutgoingNodesAdapter = new ShowConnectedNodesAdapter(project, this, DisplayConstants.INCOMING_AND_OUTGOING);
		//showIncomingNodesAdapter = new ShowConnectedNodesAdapter(project, this, DisplayConstants.INCOMING);
		//showOutgoingNodesAdapter = new ShowConnectedNodesAdapter(project, this, DisplayConstants.OUTGOING);
		panEastAdapter = new PanEastAdapter(this);
		panWestAdapter = new PanWestAdapter(this);
		panNorthAdapter = new PanNorthAdapter(this);
		panSouthAdapter = new PanSouthAdapter(this);

		// @tag Shrimp.ZoomModes
		fisheyeInAdapter = new FisheyeInAdapter(this);
		magnifyInAdapter = new MagnifyInAdapter(this);
		zoomInAdapter = new ZoomInZoomModeAdapter(this);
		zoomInAnyModeAdapter = new ZoomInAnyModeAdapter(this);
		zoomInAnyModeAdapter.addZoomMode(DisplayConstants.FISHEYE, fisheyeInAdapter);
		zoomInAnyModeAdapter.addZoomMode(DisplayConstants.MAGNIFY, magnifyInAdapter);
		zoomInAnyModeAdapter.addZoomMode(DisplayConstants.ZOOM, zoomInAdapter);

		fisheyeOutAdapter = new FisheyeOutAdapter(this);
		magnifyOutAdapter = new MagnifyOutAdapter(this);
		zoomOutAdapter = new ZoomOutZoomModeAdapter(this);
		zoomOutAnyModeAdapter = new ZoomOutAnyModeAdapter(this);
		zoomOutAnyModeAdapter.addZoomMode(DisplayConstants.FISHEYE, fisheyeOutAdapter);
		zoomOutAnyModeAdapter.addZoomMode(DisplayConstants.MAGNIFY, magnifyOutAdapter);
		zoomOutAnyModeAdapter.addZoomMode(DisplayConstants.ZOOM, zoomOutAdapter);

		openCloseNodeAdapter = new OpenCloseNodeAdapter(project, this);
		//OpenCompositeRelationshipAdapter ocra = new OpenCompositeRelationshipAdapter(displayBean, selectorBean, actionHistoryBean);
		mouseSelectAndMoveAdapter = new MouseSelectAndMoveAdapter(this);
		mouseHighlightArcAdapter = new MouseHighlightArcAdapter(this);
		filterRequestAdapter = new DisplayFilterRequestAdapter(this);
		attrToVisVarChangeAdapter = new AttrToVisVarChangeAdapter(project, this);
		codeUrlRequestAdapter = new CodeURLRequestAdapter(this);
		javaDocUrlRequestAdapter = new JavaDocURLRequestAdapter(this);
		protegeDoubleClickAdapter = new ProtegeDoubleClickAdapter(this);
		annotationPanelActionAdapter = new AnnotationPanelActionAdapter(project.getProperties());
		documentsPanelActionAdapter = new DocumentsPanelActionAdapter(project.getProperties());
		shrimpMouseWrapperAdapter = new ShrimpMouseWrapperAdapter(this, mouseSelectAndMoveAdapter,
				zoomInAnyModeAdapter, zoomOutAnyModeAdapter);

		//--- FilterBean adapters
		filterChangedAdapter = new DisplayFilterChangedAdapter(this);

		//---- SelectorBean adapters
		ActionManager mgr = (actionManager != null ? actionManager : project.getActionManager());
		selectedNodesChangeAdapter = new SelectedNodesChangeAdapter(this, mgr);
		selectedArcsChangeAdapter = new SelectedArcsChangeAdapter(this, mgr);

		try {
			//scripting adapters registered with ScriptingBean
			ScriptingBean sb = (ScriptingBean) project.getBean(ShrimpProject.SCRIPTING_BEAN);
			DefaultMainViewScriptingBean svsb = sb.getShrimpViewScriptingBean();

			svsb.setRedoActionAdapter(redoActionAdapter);
			svsb.setUndoActionAdapter(undoActionAdapter);

			svsb.setLayoutChildrenAdapter(layoutChildrenAdapter);
			svsb.setLayoutSelectedAdapter(layoutSelectedAdapter);

			svsb.setOpenAllAdapter(openAllAdapter);
			svsb.setCloseSelectedNodesAdapter(closeSelectedNodesAdapter);
			svsb.setOpenSelectedNodesAdapter(showChildrenOfSelectedNodesAdapter);
			svsb.setCollapseAllDescendentsAdapter(collapseAllDescendentsAdapter);
			svsb.setFilterSelectedArtifactsAdapter(hideSelectedArtifactsAdapter);
			svsb.setPruneSubgraphAdapter(pruneSubgraphAdapter);
			svsb.setUnfilterAllByIdAdapter(unfilterAllByIdAdapter);
			// @tag Shrimp.grouping
			svsb.setGroupSelectedArtifactsAdapter(groupSelectedArtifactsAdapter);
			svsb.setRenameSelectedNodeAdapter(renameSelectedNodeAdapter);
			svsb.setUngroupSelectedArtifactsAdapter(ungroupSelectedArtifactsAdapter);
			svsb.setFocusOnHomeAdapter(focusOnHomeAdapter);

			svsb.setSelectAllChildrenAdapter(selectAllChildrenAdapter);
			svsb.setSelectAllDescendentsAdapter(selectAllDescendentsAdapter);
			svsb.setSelectInverseSiblingsAdapter(selectInverseSiblingsAdapter);
			svsb.setSelectConnectedNodesAdapter(selectIncomingAndOutgoingNodesAdapter);

			svsb.setAttachDocumentToNodeAdapter(attachDocumentToNodeAdapter);
			svsb.setViewDocumentsAdapter(viewDocumentsAdapter);
			svsb.setChangeNodeLabelIconAdapter(changeLabelIconAdapter);
			svsb.setChangeNodeOverlayIconAdapter(changeOverlayIconAdapter);

			svsb.setSnapShotAdapter(snapShotAdapter);
			svsb.setZoomModeChangeAdapter(zoomModeChangeAdapter);
		} catch (BeanNotFoundException e) {
			//e.printStackTrace();
		} catch (NoClassDefFoundError e) {

		}
	}

	/**
	 * Sets all the adapters that have (or should have) keyboard controls
	 */
	private void  initUserControls() {
		//--- user controls
		userControls = new Vector();

		userControls.add(snapShotAdapter);
		userControls.add(selectAllChildrenAdapter);
		userControls.add(selectAllDescendentsAdapter);
		userControls.add(selectIncomingAndOutgoingNodesAdapter);
		userControls.add(selectIncomingNodesAdapter);
		userControls.add(selectOutgoingNodesAdapter);
		//userControls.add(showIncomingAndOutgoingNodesAdapter);
		//userControls.add(showIncomingNodesAdapter);
		//userControls.add(showOutgoingNodesAdapter);
		userControls.add(panEastAdapter);
		userControls.add(panWestAdapter);
		userControls.add(panNorthAdapter);
		userControls.add(panSouthAdapter);
		userControls.add(zoomInAdapter);
		userControls.add(zoomOutAdapter);
		userControls.add(zoomInAnyModeAdapter);
		userControls.add(zoomOutAnyModeAdapter);
		userControls.add(magnifyInAdapter);
		userControls.add(magnifyOutAdapter);
		userControls.add(fisheyeInAdapter);
		userControls.add(fisheyeOutAdapter);
		userControls.add(openCloseNodeAdapter);
		userControls.add(openAllAdapter);
		userControls.add(hideSelectedArtifactsAdapter);

		//@tag Shrimp.grouping
		userControls.add(groupSelectedArtifactsAdapter);
		userControls.add(renameSelectedNodeAdapter);
		userControls.add(ungroupSelectedArtifactsAdapter);

		// @tag Shrimp.FilterAllDestinationNodes
		userControls.add(pruneSubgraphAdapter);
		userControls.add(unfilterAllByIdAdapter);
		userControls.add(focusOnHomeAdapter);
		userControls.add(undoActionAdapter);
		userControls.add(redoActionAdapter);

		// @tag Shrimp.DocumentManager
		userControls.add(attachDocumentToNodeAdapter);
		userControls.add(viewDocumentsAdapter);

		// @tag Shrimp.ChangeNodeIcon
		userControls.add(changeLabelIconAdapter);
		userControls.add(changeOverlayIconAdapter);

		// @tag Shrimp.SpaceInvaders
		userControls.add(new SpaceInvadersAdapter(project, this));

		loadControlPreferences(userControls);
		project.getApplication().addApplicationListener(new ShrimpApplicationAdapter() {
			public void userControlsChanged(ShrimpApplicationEvent event) {
				loadControlPreferences(userControls);
			}
		});
	}


	/**
	 * Does any work necessary to tie adapters to project specific beans.  This should be called
	 * when after all the adapters are initialized (shrimp tool initialization).
	 */
	public void initShrimpViewBeansForProject() {

		try {
			//Retrieve all the beans
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			ActionHistoryBean actionHistoryBean = (ActionHistoryBean) getBean(ShrimpTool.ACTION_HISTORY_BEAN);
			FilterBean filterBean = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);
			AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);

			//---- DataBean Listeners
			//dataBean.addDataChangeListener (dataChangeAdapter);
			dataBean.addDataTypesChangeListener(dataTypesChangeAdapter);
			dataBean.addRelationshipAddListener(relationshipAddAdapter);

			//---- ActionhistoryBean Listeners
			actionHistoryBean.addActionHistoryListener(undoActionAdapter);
			actionHistoryBean.addActionHistoryListener(redoActionAdapter);

			//---- FilterBean Listeners
			filterBean.addFilterChangedListener(filterChangedAdapter);

			//---- DisplayBean Listeners
			displayBean.addShrimpMouseListener(shrimpInputAdapter);
			displayBean.addShrimpKeyListener(shrimpInputAdapter);
			displayBean.addShrimpKeyListener(mouseSelectAndMoveAdapter);
			displayBean.addShrimpMouseListener(mouseHighlightArcAdapter);
			displayBean.addFilterRequestListener(filterRequestAdapter);
			displayBean.addShrimpMouseListener(shrimpMouseWrapperAdapter);

			//-- AttrToVisVarBean Listeners
			attrToVisVarBean.addVisVarValuesChangeListener(attrToVisVarChangeAdapter);

			//---- SelectorBean Listeners
			selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, selectedNodesChangeAdapter);
			selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_ARCS, selectedArcsChangeAdapter);

			displayBean.getDataDisplayBridge().refresh();

			//---- DataDisplayBridge Listeners
			Vector customizedPanelListeners = new Vector(5);
			customizedPanelListeners.add(codeUrlRequestAdapter);
			customizedPanelListeners.add(javaDocUrlRequestAdapter);
			customizedPanelListeners.add(protegeDoubleClickAdapter);
			customizedPanelListeners.add(annotationPanelActionAdapter);
			customizedPanelListeners.add(documentsPanelActionAdapter);
			displayBean.getDataDisplayBridge().addCustomizedPanelListeners(customizedPanelListeners);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the node labels
	 */
	private void initDisplay() {
		try {
			//Retrieve all the beans
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);

			// check the ApplicationAccessor.getApplication() properties for font settings
			String fontStr = appProps.getProperty(SV_LABEL_FONT_KEY);
			if (fontStr != null) {
				StringTokenizer tokenizer = new StringTokenizer (fontStr, "!");
				String fontName = tokenizer.nextToken();
				String fontStyleStr = tokenizer.nextToken();
				int fontStyle = Integer.parseInt (fontStyleStr);
				String fontSizeStr = tokenizer.nextToken();
				int fontSize = Integer.parseInt (fontSizeStr);
				Font font = new Font (fontName, fontStyle, fontSize);
				if (!font.equals(displayBean.getLabelFont())) {
					displayBean.setLabelFont(font);
				}
			}

			// label mode
			// if hierarchy is none then scale labels by node size to speed things up.
			if (cprels.length > 0) {//equals(DisplayConstants.NO_HIERARCHY)) {
				String labelMode = appProps.getProperty(SV_LABEL_MODE_KEY);
				if (labelMode != null && !labelMode.equals(displayBean.getDefaultLabelMode())) {
					displayBean.setDefaultLabelMode(labelMode);
				}
			} else {
				displayBean.setDefaultLabelMode(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL);
			}

			// levels of labels
			String labelLevelsStr = appProps.getProperty(SV_LABEL_LEVELS_KEY);
			if (labelLevelsStr != null) {
				int labelLevels = Integer.parseInt(labelLevelsStr);
				if (labelLevels != displayBean.getLabelLevels()) {
					displayBean.setLabelLevels(labelLevels);
				}
			}

			// label fade out
			String labelFadeOutStr = appProps.getProperty(SV_LABEL_FADE_OUT_KEY);
			if (labelFadeOutStr != null) {
				boolean labelFadeOut = Boolean.valueOf(labelFadeOutStr).booleanValue();
				if (labelFadeOut != displayBean.getLabelFadeOut()) {
					displayBean.setLabelFadeOut(labelFadeOut);
				}
			}

			// label background opaque
			String labelOpaqueStr = appProps.getProperty(SV_LABEL_BACKGROUND_OPAQUE_KEY);
			if (labelOpaqueStr != null) {
				boolean labelOpaque = Boolean.valueOf(labelOpaqueStr).booleanValue();
				if (labelOpaque != displayBean.getLabelBackgroundOpaque()) {
					displayBean.setLabelBackgroundOpaque(labelOpaque);
				}
			} else {
				displayBean.setLabelBackgroundOpaque(false);
			}

			// arc labels
			String showArcLabelsStr = appProps.getProperty(SV_SHOW_ARC_LABELS_KEY);
		    Boolean showArcLabels = Boolean.valueOf(showArcLabelsStr);
		    displayBean.setShowArcLabels(showArcLabels.booleanValue());

			// switch node labelling mode
			String switchLabelModeStr = appProps.getProperty(SV_SWITCH_LABEL_MODE_KEY);
		    Boolean switchLabelMode = Boolean.valueOf(switchLabelModeStr);
		    displayBean.setSwitchLabelling(switchLabelMode.booleanValue());
			String switchAtNumStr = appProps.getProperty(SV_SWITCH_LABEL_MODE_NUM_KEY);
			if (switchAtNumStr != null) {
				int switchAtNum = Integer.parseInt (switchAtNumStr);
				if (switchAtNum != displayBean.getSwitchAtNum()) {
					displayBean.setSwitchAtNum(switchAtNum);
				}
			}

			//static rendering quality
			String staticRendQualStr = appProps.getProperty(STATIC_RENDER_QUALITY_KEY, "" + displayBean.getDefaultStaticRenderingQuality());
			int staticRendQual = Integer.parseInt(staticRendQualStr);
			if (staticRendQual != displayBean.getStaticRenderingQuality()) {
				displayBean.setStaticRenderingQuality(staticRendQual);
			}

			//dynamic rendering quality
			String dynamicRendQualStr = appProps.getProperty(DYNAMIC_RENDER_QUALITY_KEY, "" + displayBean.getDefaultDynamicRenderingQuality());
			int dynamicRendQual = Integer.parseInt(dynamicRendQualStr);
			if (dynamicRendQual != displayBean.getDynamicRenderingQuality()) {
				displayBean.setDynamicRenderingQuality(dynamicRendQual);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adds listeners so that ShrimpView knows when a project (or the application)
	 * opens and closes and can react accordingly.
	 */
	private void initShrimpListeners() {
        // create menus associated with this view
        shrimpProjectListener = new ShrimpProjectAdapter() {
			public void projectActivated(ShrimpProjectEvent event) {
        		createShrimpViewActions();
        		createNodePopupMenu();
        		createArcPopupMenu();
        		if (config.createToolBar) {
        			createToolBar();
        		}
        		if (config.showModePanel) {
        			createModePanel();
        		}

				gui.validate();

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// set the default mouseMode
						setMouseMode(ApplicationAccessor.getProperties().getProperty(MOUSE_MODE_PROPERTY_KEY,
								ShrimpView.MOUSE_MODE_DEFAULT));
					}
				});
			}

			public void projectClosing(ShrimpProjectEvent event) {
				event.getProject().removeProjectListener(shrimpProjectListener);
			}
		};
		project.addProjectListener(shrimpProjectListener);

	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool#getGUI()
	 */
	public Component getGUI() {
		return this.gui;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool#disposeTool()
	 */
	public void disposeTool() {
		saveShrimpViewSettings();

		gui.getContentPane().removeAll();

		//Remove ShrimpView Actions/Menus
		removeShrimpViewActions();
		removeNodePopupMenu();
		removeArcPopupMenu();
		shrimpInputAdapter.clearUserActions();

		//Remove Project listeners
		if (project != null) {
			project.removeProjectListener(shrimpProjectListener);
		}
		shrimpProjectListener = null;

		//Now remove all the adapters as listeners to the beans
		try {
			//Retrieve all the beans
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			ActionHistoryBean actionHistoryBean = (ActionHistoryBean) getBean(ShrimpTool.ACTION_HISTORY_BEAN);
			FilterBean filterBean = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);

			//---- DataBean Listeners
			//dataBean.removeDataChangeListener(dataChangeAdapter);
			if (project != null) {
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				dataBean.removeDataTypesChangeListener(dataTypesChangeAdapter);
				dataBean.removeRelationshipAddListener(relationshipAddAdapter);
			}

			//---- ActionhistoryBean Listeners
			actionHistoryBean.removeActionHistoryListener(undoActionAdapter);
			actionHistoryBean.removeActionHistoryListener(redoActionAdapter);

			//---- FilterBean Listeners
			filterBean.removeFilterChangedListener(filterChangedAdapter);

			//remove all filters
			Vector filters = (Vector)filterBean.getFilters().clone();
			try {
				filterBean.removeFilters(filters);
			} catch (FilterNotFoundException e) {}

			//---- DisplayBean Listeners
			displayBean.removeShrimpMouseListener(shrimpInputAdapter);
			displayBean.removeShrimpKeyListener(shrimpInputAdapter);
			displayBean.removeShrimpKeyListener(mouseSelectAndMoveAdapter);
			displayBean.removeShrimpMouseListener(mouseHighlightArcAdapter);
			displayBean.removeFilterRequestListener(filterRequestAdapter);
			displayBean.removeShrimpMouseListener(shrimpMouseWrapperAdapter);

			//---- DataDisplayBridge Listeners
			Vector customizedPanelListeners = new Vector(5);
			customizedPanelListeners.add(codeUrlRequestAdapter);
			customizedPanelListeners.add(javaDocUrlRequestAdapter);
			customizedPanelListeners.add(protegeDoubleClickAdapter);
			customizedPanelListeners.add(annotationPanelActionAdapter);
			customizedPanelListeners.add(documentsPanelActionAdapter);
			displayBean.getDataDisplayBridge().removeCustomizedPanelListeners(customizedPanelListeners);

			//---- SelectorBean Listeners
			selectorBean.removePropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, selectedNodesChangeAdapter);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves any ShrimpView settings into the application properties
	 */
	private void saveShrimpViewSettings() {
		Properties properties = ApplicationAccessor.getProperties();
		try {
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			Font font = (Font) displayBean.getLabelFont();
			properties.setProperty(SV_LABEL_FONT_KEY, font.getName() + "!" + font.getStyle() + "!" + font.getSize());
			properties.setProperty(SV_LABEL_MODE_KEY, displayBean.getDefaultLabelMode());
			properties.setProperty(SV_LABEL_FADE_OUT_KEY, "" + displayBean.getLabelFadeOut());
			properties.setProperty(SV_LABEL_BACKGROUND_OPAQUE_KEY, "" + displayBean.getLabelBackgroundOpaque());
			properties.setProperty(SV_LABEL_LEVELS_KEY, "" + displayBean.getLabelLevels());
			properties.setProperty(SV_SHOW_ARC_LABELS_KEY, "" + displayBean.getShowArcLabels());
			properties.setProperty(SV_SWITCH_LABEL_MODE_KEY , "" + displayBean.getSwitchLabelling());
			properties.setProperty(SV_SWITCH_LABEL_MODE_NUM_KEY , "" + displayBean.getSwitchAtNum());
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

    /**
     * Adds the default root nodes to the display. These will correspond to
     * the root artifacts based on the current cprels.  The root nodes will not be opened.
     * @param layoutAndFocusExtents Whether or not to layout and focus on the newly added roots nodes.
     */
	public void addDefaultRootNodes(boolean layoutAndFocusExtents) {
		ApplicationAccessor.waitCursor();
		String subTitle = "";
		try {
			ProgressDialog.showProgress();
			subTitle = ProgressDialog.getSubtitle();
			ProgressDialog.setSubtitle("Adding root nodes to Main View ...");
		    DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
		    // tell the display bean to display the roots
	        ProgressDialog.setNote("Creating root node(s)...");
			Collection rootNodes = displayBean.getDataDisplayBridge().createDefaultRootNodes();
			for (Iterator iter = rootNodes.iterator(); iter.hasNext();) {
	            ShrimpNode rootNode = (ShrimpNode) iter.next();
	            //ProgressDialog.setNote("Adding " + rootNode.getName() + " to Main View...");
	            displayBean.addObject(rootNode);
	        }
			displayBean.setVisible(rootNodes, true, true);
            if (layoutAndFocusExtents) {
    	        ProgressDialog.setNote("Laying out root node(s)...");
    			String defaultLayoutMode = ApplicationAccessor.getProperties().getProperty(
    					DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE,
    					DisplayBean.PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE);
    			displayBean.setLayoutMode(rootNodes, defaultLayoutMode, false, false);
    	        ProgressDialog.setNote("Focusing on root node(s)...");
    			displayBean.focusOnExtents(false);
            }
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} finally {
			// change to the old cursor
		    ProgressDialog.setSubtitle(subTitle);
		    ProgressDialog.tryHideProgress();
			ApplicationAccessor.defaultCursor();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
	 */
	public void refresh() {
		ApplicationAccessor.waitCursor();
		try {
			ProgressDialog.showProgress();
			String subTitle = ProgressDialog.getSubtitle();
			ProgressDialog.setSubtitle("Refreshing Main View ...");

		    clear();

			// change to the old cursor
		    ProgressDialog.setSubtitle(subTitle);
		    ProgressDialog.tryHideProgress();
		} finally {
			ApplicationAccessor.defaultCursor();
		}
	}

	/**
	 * Reloads the toolbar actions.
	 */
	public void reloadToolBar() {
		if ((gui != null) && (toolBar != null)) {
			gui.getContentPane().remove(toolBar);
			createToolBar();
		}
	}

	/**
	 * @return JToolBar the ShrimpView toolbar
	 */
	public JToolBar getToolBar() {
		return toolBar;
	}

	/**
	 * @return the {@link QuickSearchPanel}, might be null
	 */
	public QuickSearchPanel getQuickSearchPanel() {
		return quickSearchPanel;
	}

	/**
	 * Creates the bottom mode panel.
	 * This contains the hierarchy button, node labels combo, arc labels checkbox, navigation combo.
	 * @see ShrimpViewModePanel
	 */
	private void createModePanel() {
		try {
			ShrimpViewModePanel modePanel = new ShrimpViewModePanel(this, changeCPRelAdapter,
				magnifyModeAction, fisheyeModeAction, zoomModeAction);
			gui.getContentPane().add(modePanel, BorderLayout.SOUTH);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createToolBar() {
		Collection extraActions = new ArrayList();
		// @tag Shrimp.QuickSearch : If the quick search isn't showing then add a toolbar button to display it
		if (quickSearchPanel != null && !quickSearchPanel.isVisibleInProperties()) {
			DefaultShrimpAction action = new DefaultShrimpAction(ResourceHandler.getIcon("icon_quick_search.gif"), "Show the quick search panel") {
				public void actionPerformed(ActionEvent e) {
					quickSearchPanel.showSearchPanel(true);
					reloadToolBar();
				}
			};
			extraActions.add(action);
		}

		actionManager = project.getActionManager();
		if (configurator == null) {
			configurator = new ToolBarConfigurator(this, actionManager);
		}
		toolBar = configurator.createToolBar(extraActions);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(getOutputLabel());
		gui.getContentPane().add(toolBar, BorderLayout.NORTH);
		gui.getContentPane().validate();
	}

	private void createShrimpViewActions() {
    	try {
			//Retrieve all the beans
			final DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			final SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);

			ActionManager actionManager = project.getActionManager();

	    	// stop actionManager events
	    	boolean firingEvents = actionManager.getFiringEvents();
	    	actionManager.setFiringEvents(false);

			// ***************** EDIT MENU *******************

			//Edit->Change CP Relationship
			addShrimpViewAction(changeCPRelAdapter, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_A, 1);

			addShrimpViewAction(resetProjectAdapter, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_A, 2);

			Action editAction = actionManager.getAction(ShrimpConstants.MENU_EDIT, "");
            editAction.setEnabled(true);
			// ********************* EDIT MENU ************************


			// ********************  NODE MENU ************************
			// note: custom panel items are added as the menu is opened

			// OPEN_CUSTOM_PANEL_BUTTON_NAME Menu

			// Node->Arrange Children
			ShrimpAction action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_ARRANGE_CHILDREN);
			action.setEnabled(true);	// false or true?
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 1);

			// Node->Arrange Selected Items
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_ARRANGE_SELECTED_ITEMS);
			action.setEnabled(false);
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 2);

			// Node->Arrange Children->Grid
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_GRID);
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, ShrimpConstants.GROUP_A, 1);

			//Node->Arrange Selected Nodes->Grid
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_GRID);
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_ARRANGE_SELECTED_ITEMS, ShrimpConstants.GROUP_A, 1);

			ArrayList layouts = new ArrayList(20);
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_GRID_ALPHABETICAL, "icon_grid_layout_alphabetical.gif", LayoutConstants.LAYOUT_GRID_BY_ALPHA, "false", "/" + ShrimpConstants.ACTION_NAME_GRID, ShrimpConstants.GROUP_A, "1"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_GRID_BY_CHILDREN, "icon_grid_layout_num_children.gif", LayoutConstants.LAYOUT_GRID_BY_NUM_CHILDREN, "false", "/" + ShrimpConstants.ACTION_NAME_GRID, ShrimpConstants.GROUP_A, "2"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_GRID_BY_RELATIONSHIPS, "icon_grid_layout_num_rels.gif", LayoutConstants.LAYOUT_GRID_BY_NUM_RELS, "false", "/" + ShrimpConstants.ACTION_NAME_GRID, ShrimpConstants.GROUP_A, "3"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_GRID_BY_TYPE, "icon_grid_layout_type.gif", LayoutConstants.LAYOUT_GRID_BY_TYPE, "false", "/" + ShrimpConstants.ACTION_NAME_GRID, ShrimpConstants.GROUP_A, "4"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_GRID_BY_ATTRIBUTE, "icon_grid_layout_attribute.gif", LayoutConstants.LAYOUT_GRID_BY_ATTRIBUTE, "true", "/" + ShrimpConstants.ACTION_NAME_GRID, ShrimpConstants.GROUP_A, "5"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_RADIAL_LAYOUT, "icon_radial_layout.gif", LayoutConstants.LAYOUT_RADIAL, "false", "", ShrimpConstants.GROUP_A, "1"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_RADIAL_LAYOUT_INVERTED, "icon_radial_layout.gif", LayoutConstants.LAYOUT_RADIAL_INVERTED, "false", "", ShrimpConstants.GROUP_A, "2"});
			if (OrthogonalLayout.isLoaded()) {
				layouts.add(new String[] { ShrimpConstants.ACTION_NAME_HIERACHICAL_LAYOUT, "icon_hierarchical_layout.gif", LayoutConstants.LAYOUT_HIERARCHICAL, "false", "", ShrimpConstants.GROUP_A, "3"});
				layouts.add(new String[] { ShrimpConstants.ACTION_NAME_ORTHOGONAL_LAYOUT, "icon_controlflow_layout.gif", LayoutConstants.LAYOUT_ORTHOGONAL, "false", "", ShrimpConstants.GROUP_A, "4"});
			}
			// @tag Shrimp(sugiyama)
			if (SugiyamaLayout.isInstalled()) {
				layouts.add(new String[] { ShrimpConstants.ACTION_NAME_SUGIYAMA_LAYOUT, "icon_sugiyama_layout.gif", LayoutConstants.LAYOUT_SUGIYAMA, "false", "", ShrimpConstants.GROUP_A, "5"});
			}

			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_SPRING_LAYOUT, "icon_spring_layout.gif", LayoutConstants.LAYOUT_SPRING, "false", "", ShrimpConstants.GROUP_A, "6"});
			//layouts.add(new String[] { ShrimpConstants.ACTION_NAME_SPRING_LAYOUT_ADVANCED", "icon_spring_layout_advanced.gif", LayoutConstants.LAYOUT_SPRING, "true", "", ShrimpConstants.GROUP_A, "4"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_VERTICAL, "icon_tree_layout_vertical.gif", LayoutConstants.LAYOUT_TREE_VERTICAL, "false", "", ShrimpConstants.GROUP_A, "5"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_HORIZONTAL, "icon_tree_layout_horizontal.gif", LayoutConstants.LAYOUT_TREE_HORIZONTAL, "false", "", ShrimpConstants.GROUP_A, "7"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_VERTICAL_INVERTED, "icon_tree_layout_vertical_inverted.gif", LayoutConstants.LAYOUT_TREE_VERTICAL_INVERTED, "false", "", ShrimpConstants.GROUP_A, "8"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_HORIZONTAL_INVERTED, "icon_tree_layout_horizontal_inverted.gif", LayoutConstants.LAYOUT_TREE_HORIZONTAL_INVERTED, "false", "", ShrimpConstants.GROUP_A, "9"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREEMAP_LAYOUT, "icon_treemap_layout.gif", LayoutConstants.LAYOUT_TREEMAP, "true", "", ShrimpConstants.GROUP_A, "10"});
			//layouts.add(new String[] { ShrimpConstants.ACTION_NAME_UML_LAYOUT, "icon_uml_layout.gif", DisplayConstants.LAYOUT_UML, "false", "", ShrimpConstants.GROUP_B, "1"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_HORIZONTAL_LAYOUT, "", LayoutConstants.LAYOUT_HORIZONTAL, "false", "", ShrimpConstants.GROUP_B, "2"});
			// @tag Shrimp(HorizontalLayout(NoOverlap))
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_HORIZONTAL_LAYOUT_NO_OVERLAP, "", LayoutConstants.LAYOUT_HORIZONTAL_NO_OVERLAP, "false", "", ShrimpConstants.GROUP_B, "2"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_VERTICAL_LAYOUT, "", LayoutConstants.LAYOUT_VERTICAL, "false", "", ShrimpConstants.GROUP_B, "3"});
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_CASCADE_LAYOUT, "", "Cascade", "false", "", ShrimpConstants.GROUP_B, "4"});
			//layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TOUCHGRAPH_LAYOUT, "icon_touchgraph_layout.gif", LayoutConstants.LAYOUT_TOUCHGRAPH, "false", "", ShrimpConstants.GROUP_A, "9"});
			if (ForceDirectedLayout.isPrefuseInstalled()) {
				layouts.add(new String[] { ShrimpConstants.ACTION_NAME_FORCE_DIRECTED_LAYOUT, "icon_force_directed_layout.gif", LayoutConstants.LAYOUT_FORCE_DIRECTED, "false", "", ShrimpConstants.GROUP_A, "10"});
			}

			for (Iterator iter = layouts.iterator(); iter.hasNext(); ) {
	            final String[] params = (String[]) iter.next();
	            final String actionName = params[0];
	            final Icon icon = ResourceHandler.getIcon(params[1]);
	            final String layoutMode = params[2];
	            final boolean showLayoutDialog = Boolean.valueOf(params[3]).booleanValue();
	            final String parentMenuName = params[4];
	            final String menuGroupName = params[5];
	            final int menuPosition = Integer.parseInt(params[6]);

	    		ShrimpAction layoutChildrenAction = new DefaultShrimpAction(actionName, icon) {
	    			public void actionPerformed(ActionEvent e) {
	    			    layoutChildrenAdapter.changeLayout(layoutMode, showLayoutDialog);
	    			}
	    		};
				addShrimpViewAction(layoutChildrenAction, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN + parentMenuName, menuGroupName, menuPosition);

				ShrimpAction layoutSelectedAction = new DefaultShrimpAction(actionName, icon) {
	    			public void actionPerformed(ActionEvent e) {
	    			    layoutSelectedAdapter.changeLayout(layoutMode, showLayoutDialog);
	    			}
	    		};
	    		layoutSelectedAction.setEnabled(false);
				addShrimpViewAction(layoutSelectedAction, ShrimpConstants.MENU_NODE_ARRANGE_SELECTED_ITEMS + parentMenuName, menuGroupName, menuPosition);
	        }


			// Node->Select
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_SELECT);
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 3);

			//Node ->Select-> None
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_NONE) {
				public void actionPerformed (ActionEvent e) {
					selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
				}
			};
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_A, 1);

			//Node ->Select->Inverse Siblings
			selectInverseSiblingsAdapter.setEnabled(false);
			addShrimpViewAction(selectInverseSiblingsAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_A, 2);

			selectAllChildrenAdapter.setEnabled(false);
			addShrimpViewAction(selectAllChildrenAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_A, 3);

			//Node -> Select->All Descendents
			selectAllDescendentsAdapter.setEnabled(false);
			addShrimpViewAction(selectAllDescendentsAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_A, 4);

			//Node -> Select->Outgoing Arcs
			//selectOutgoingArcsAdapter.setEnabled(false);
			//addShrimpViewAction(selectOutgoingArcsAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_B, 1);

			//Node -> Select->Incoming Arcs
			//selectIncomingArcsAdapter.setEnabled(false);
			//addShrimpViewAction(selectIncomingArcsAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_B, 2);

			//Node -> Select->Incoming and Outgoing Arcs
			//selectIncomingAndOutgoingArcsAdapter.setEnabled(false);
			//addShrimpViewAction(selectIncomingAndOutgoingArcsAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_B, 3);
			// End - "Select" Menu

			//Node -> Select->Incoming and Outgoing Nodes
			selectIncomingAndOutgoingNodesAdapter.setEnabled(false);
			addShrimpViewAction(selectIncomingAndOutgoingNodesAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_B, 4);

			selectIncomingNodesAdapter.setEnabled(false);
			addShrimpViewAction(selectIncomingNodesAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_B, 5);

			selectOutgoingNodesAdapter.setEnabled(false);
			addShrimpViewAction(selectOutgoingNodesAdapter, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_B, 6);

			// Node->Labels
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_LABELS);
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE, "Group - Labels", 3);

			//Node ->Labels-> Fixed
			action = new DefaultShrimpAction(DisplayConstants.LABEL_MODE_FIXED) {
				public void actionPerformed(ActionEvent e) {
					displayBean.setLabelMode(selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES), DisplayConstants.LABEL_MODE_FIXED);
				}
			};
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_LABELS, ShrimpConstants.GROUP_A, 1);

			//Node ->Labels-> by level
			action = new DefaultShrimpAction(ShrimpConstants.SCALE_BY_LEVEL) {
				public void actionPerformed (ActionEvent e) {
					displayBean.setLabelMode(selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES), DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL);
				}
			};
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_LABELS, ShrimpConstants.GROUP_A, 2);

			//Node ->Labels-> by node size
			action = new DefaultShrimpAction(DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE) {
				public void actionPerformed (ActionEvent e) {
					displayBean.setLabelMode(selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES), DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
				}
			};
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_LABELS, ShrimpConstants.GROUP_A, 3);

			// @tag shrimp(fitToNodeLabelling)
			//Node ->Labels-> Fit to node
			action = new DefaultShrimpAction(DisplayConstants.LABEL_MODE_FIT_TO_NODE) {
				public void actionPerformed(ActionEvent e) {
					displayBean.setLabelMode(selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES), DisplayConstants.LABEL_MODE_FIT_TO_NODE);
				}
			};
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_LABELS, ShrimpConstants.GROUP_A, 4);

			// @tag shrimp(fitToNodeLabelling)
			//Node ->Labels-> Wrap to node
			action = new DefaultShrimpAction(DisplayConstants.LABEL_MODE_WRAP_TO_NODE) {
				public void actionPerformed(ActionEvent e) {
					displayBean.setLabelMode(selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES), DisplayConstants.LABEL_MODE_WRAP_TO_NODE);
				}
			};
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE_LABELS, ShrimpConstants.GROUP_A, 5);

			//action = new ShortestPathAction(this, project);
			//addShrimpViewAction (action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_G, 1);
			//show connected
			//showIncomingAndOutgoingNodesAdapter.setEnabled(false);
			//addShrimpViewAction(showIncomingAndOutgoingNodesAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 1);
			//showIncomingNodesAdapter.setEnabled(false);
			//addShrimpViewAction(showIncomingNodesAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 2);
			//showOutgoingNodesAdapter.setEnabled(false);
			//addShrimpViewAction(showOutgoingNodesAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 3);

			// Node->Show
			action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_SHOW);
			addShrimpViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 6);
			// Node->Show->Closed
			addShrimpViewAction(showClosedAdapter, ShrimpConstants.MENU_NODE_SHOW, ShrimpConstants.GROUP_A, 1);

			// Node->Show->Show Children
			addShrimpViewAction(showChildrenOfSelectedNodesAdapter, ShrimpConstants.MENU_NODE_SHOW, ShrimpConstants.GROUP_A, 2);

			// Node->Expand
			addShrimpViewAction(expandSelectedNodesAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 1);

			// Node->Expand All Descendants
			addShrimpViewAction(openAllAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 2);

			// Node->Collapse
			addShrimpViewAction(closeSelectedNodesAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 3);

			//Node->Collapse all desendants
			addShrimpViewAction(collapseAllDescendentsAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 4);

			// Node->Group Selected Nodes  @tag Shrimp.grouping
			addShrimpViewAction(groupSelectedArtifactsAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 1);
			// Node->Ungroup Selected Nodes  @tag Shrimp.grouping
			addShrimpViewAction(ungroupSelectedArtifactsAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 2);
			// Node->Rename Selected Node  @tag Shrimp.grouping
			addShrimpViewAction(renameSelectedNodeAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 3);

			// Node->Hide
			addShrimpViewAction(hideSelectedArtifactsAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 1);
			// Node->Prune
			addShrimpViewAction(pruneSubgraphAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 2);
			// Node->Unfilter All Nodes
			addShrimpViewAction(unfilterAllByIdAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 3);

			// @tag Shrimp.filterArcs
			FilterSelectedArcsAdapter filterSelectedArcsAdapter = new FilterSelectedArcsAdapter(this);
			// @tag Shrimp.unfilterAllArcs
			UnfilterAllArcsByIdAdapter unfilterAdapter = new UnfilterAllArcsByIdAdapter(this, filterSelectedArcsAdapter);

			// Node->Unfilter All Arcs
			addShrimpViewAction(unfilterAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 4);

			// @tag Shrimp.DocumentManager
			// Node->Attach Document
			addShrimpViewAction(attachDocumentToNodeAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_E, 0);
			// Node->Attach URL
			addShrimpViewAction(new AttachURLToNodeAdapter(this), ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_E, 1);
			// Node->View Documents
			addShrimpViewAction(viewDocumentsAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_E, 2);

			// @tag Shrimp.ChangeNodeIcon
			// Node->Change Node Label Icon
			addShrimpViewAction(changeLabelIconAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_F, 0);
			// Node->Remove Node Label Icon
			addShrimpViewAction(new RemoveNodeLabelIconAdapter(this), ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_F, 1);
			// Node->Change Node Overlay Icon
			addShrimpViewAction(changeOverlayIconAdapter, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_F, 2);
			// Node->Remove Node Overlay Icon
			addShrimpViewAction(new RemoveNodeOverlayIconAdapter(this), ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_F, 3);


            Action nodeAction = actionManager.getAction(ShrimpConstants.MENU_NODE, "");
            nodeAction.setEnabled(true);
			// ********************  NODE MENU ************************

			// ******************** ARC MENU **************************
			// Arc -> Go To Source Node
			addShrimpViewAction(new NavigateToArcSourceAdapter(this), ShrimpConstants.MENU_ARC, ShrimpConstants.GROUP_A, 1);
			// Arc -> Go To Destination Node
			addShrimpViewAction(new NavigateToArcDestAdapter(this), ShrimpConstants.MENU_ARC, ShrimpConstants.GROUP_A, 2);

			// Arc -> Hide Arc
			addShrimpViewAction(filterSelectedArcsAdapter, ShrimpConstants.MENU_ARC, ShrimpConstants.GROUP_B, 1);
			// Arc -> Show All Hidden Arcs  @tag Shrimp.unfilterAllArcs
			addShrimpViewAction(unfilterAdapter, ShrimpConstants.MENU_ARC, ShrimpConstants.GROUP_B, 2);

            Action arcAction = actionManager.getAction(ShrimpConstants.MENU_ARC, "");
            arcAction.setEnabled(false);
			//  ******************** ARC MENU *************************

			// ********************* NAVIGATE MENU ********************
			navigatePopupMenuAdapter = new NavigatePopupMenuAdapter(this);
			actionManager.addPopupListener(ShrimpConstants.MENU_NAVIGATE, "", navigatePopupMenuAdapter);

			//Navigate -> Forward
			redoActionAdapter.setEnabled(false);
			addShrimpViewAction(redoActionAdapter, ShrimpConstants.MENU_NAVIGATE, ShrimpConstants.GROUP_A, 1);

			//Navigate -> Back
			undoActionAdapter.setEnabled(false);
			addShrimpViewAction(undoActionAdapter, ShrimpConstants.MENU_NAVIGATE, ShrimpConstants.GROUP_A, 2);

			//Navigate -> Home
			addShrimpViewAction(focusOnHomeAdapter, ShrimpConstants.MENU_NAVIGATE, ShrimpConstants.GROUP_A, 3);

			//Navigate -> Zoom Mode
			ButtonGroup navigate = new ButtonGroup();
			zoomModeAction = new CheckBoxAction(ShrimpConstants.ACTION_NAME_ZOOM_MODE, navigate) {
				public void startAction () {
					zoomModeChangeAdapter.changeZoomMode (new ModeChangeEvent(DisplayConstants.ZOOM, displayBean, null));
					getNavigateMethod();
				}
			};
			zoomModeAction.setChecked(false);
			addShrimpViewAction(zoomModeAction, ShrimpConstants.MENU_NAVIGATE, ShrimpConstants.GROUP_B, 1);

			//Navigate -> Magnify Mode
			magnifyModeAction = new CheckBoxAction(ShrimpConstants.ACTION_NAME_MAGNIFY_MODE, navigate) {
				public void startAction () {
					zoomModeChangeAdapter.changeZoomMode (new ModeChangeEvent(DisplayConstants.MAGNIFY, displayBean, null));
					getNavigateMethod();
				}
			};
			magnifyModeAction.setChecked(true);
			addShrimpViewAction(magnifyModeAction, ShrimpConstants.MENU_NAVIGATE, ShrimpConstants.GROUP_B, 2);

			//Navigate -> Fisheye Mode
			fisheyeModeAction = new CheckBoxAction(ShrimpConstants.ACTION_NAME_FISHEYE_MODE, navigate) {
				public void startAction () {
					zoomModeChangeAdapter.changeZoomMode (new ModeChangeEvent(DisplayConstants.FISHEYE, displayBean, null));
					getNavigateMethod();
				}
			};
			fisheyeModeAction.setChecked(false);
			addShrimpViewAction(fisheyeModeAction, ShrimpConstants.MENU_NAVIGATE, ShrimpConstants.GROUP_B, 3);


			final ButtonGroup mouseModeButtonGroup = new ButtonGroup();
			final CheckBoxAction selectAction = new CheckBoxAction(ShrimpConstants.ACTION_NAME_SELECT_TOOL, mouseModeButtonGroup) {
				public void startAction() {
					setMouseMode(DisplayConstants.MOUSE_MODE_SELECT);
				}
			};
			selectAction.putValue(Action.SMALL_ICON, ResourceHandler.getIcon("icon_select.gif"));
			addShrimpViewAction(selectAction, ShrimpConstants.MENU_NAVIGATE, "mouseTools", 1);

			final CheckBoxAction zoomInAction = new CheckBoxAction(ShrimpConstants.ACTION_NAME_ZOOM_IN_TOOL, mouseModeButtonGroup) {
				public void startAction() {
					setMouseMode(DisplayConstants.MOUSE_MODE_ZOOM_IN);
				}
			};
			zoomInAction.putValue(Action.SMALL_ICON, ResourceHandler.getIcon("icon_zoom_in.gif"));
			addShrimpViewAction(zoomInAction, ShrimpConstants.MENU_NAVIGATE, "mouseTools", 1);

			final CheckBoxAction zoomOutAction = new CheckBoxAction(ShrimpConstants.ACTION_NAME_ZOOM_OUT_TOOL, mouseModeButtonGroup) {
				public void startAction() {
					setMouseMode(DisplayConstants.MOUSE_MODE_ZOOM_OUT);
				}
			};
			zoomOutAction.putValue(Action.SMALL_ICON, ResourceHandler.getIcon("icon_zoom_out.gif"));
			addShrimpViewAction(zoomOutAction, ShrimpConstants.MENU_NAVIGATE, "mouseTools", 1);

			addShrimpViewListener(new ShrimpViewAdapter() {
				public void shrimpViewMouseModeChanged(ShrimpViewMouseModeChangedEvent event) {
					String mode = event.getMouseMode();
					if(mode.equals(DisplayConstants.MOUSE_MODE_SELECT)) {
						selectAction.setChecked(true);
						zoomInAction.setChecked(false);
						zoomOutAction.setChecked(false);
					}
					else if(mode.equals(DisplayConstants.MOUSE_MODE_ZOOM_IN)) {
						selectAction.setChecked(false);
						zoomInAction.setChecked(true);
						zoomOutAction.setChecked(false);
					}
					else if(mode.equals(DisplayConstants.MOUSE_MODE_ZOOM_OUT)) {
						selectAction.setChecked(false);
						zoomInAction.setChecked(false);
						zoomOutAction.setChecked(true);
					}
				}
			});

            Action navAction = actionManager.getAction(ShrimpConstants.MENU_NAVIGATE, "");
            navAction.setEnabled(true);
			// ********************* NAVIGAGE MENU ********************

			// ********************* TOOLS MENU ***********************
    		addShrimpViewAction(new OpenShrimpOptionsAdapter(ShrimpConstants.ACTION_NAME_OPTIONS, project), ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_E, 1);

			addShrimpViewAction(snapShotAdapter, ShrimpConstants.MENU_TOOLS, "Snapshot", 1);

            Action toolsAction = actionManager.getAction(ShrimpConstants.MENU_TOOLS, "");
            toolsAction.setEnabled(true);
            // ********************* TOOLS MENU ***********************


			// restore actionManager events
			actionManager.setFiringEvents(firingEvents);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void addShrimpViewAction(ShrimpAction action, String parentMenu, String groupName, int position) {
		project.getActionManager().addAction(action, parentMenu, groupName, position);
		if (!menuActions.containsKey(parentMenu)) {
			menuActions.put(parentMenu, new Vector());
		}
		Vector actions = (Vector) menuActions.get(parentMenu);
		if (!actions.contains(action.getActionName())) {
			actions.add(action.getActionName());
		}
	}

	// this should be invoked in disposeTool
	private void removeShrimpViewActions() {
		if (project != null) {
	    	ActionManager actionManager = project.getActionManager();

	    	// stop actionManager events
	    	boolean firingEvents = actionManager.getFiringEvents();
	    	actionManager.setFiringEvents(false);

			for (Iterator iterator = menuActions.keySet().iterator(); iterator.hasNext();) {
				String menu = (String) iterator.next();
				//System.out.println("Removing " + menu);
				Vector actions = (Vector) menuActions.get(menu);
				while (actions.size() > 0) {
					String actionName = (String) actions.remove(0);
					//System.out.println("Removing " + actionName);
					actionManager.removeAction(actionName, menu, ActionManager.DISABLE_PARENT);
				}
			}

			// restore actionManager events
			actionManager.setFiringEvents(firingEvents);
		}
	}

	private void createNodePopupMenu() {
		try {
			//Retrieve all the beans
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);

			nodePopupMenu = new JPopupMenu();
			nodePopupListener = new NodePopupListener(nodePopupMenu, this);
			if (displayBean != null) {
				((PNestedDisplayBean)displayBean).getPCanvas().addMouseListener(nodePopupListener);
			}
			//add a action manager listener so that the popup can repopulated if the menus change
			nodeActionManagerListener = new ActionManagerListener() {
				public void actionsAdded(ActionsAddedEvent event) {
					nodePopupListener.repopulateNodePopupMenu();
				}
				public void actionsRemoved(ActionsRemovedEvent event) {
					nodePopupListener.repopulateNodePopupMenu();
				}
				public void actionsModified(ActionsModifiedEvent event) {
					nodePopupListener.repopulateNodePopupMenu();
				}
			};
			project.getActionManager().addActionManagerListener(nodeActionManagerListener);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void removeNodePopupMenu() {
		try {
			//Retrieve all the beans
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			if (project != null) {
				project.getActionManager().removeActionManagerListener(nodeActionManagerListener);
			} else {
				System.out.println("Can't remove action manager listener!");
			}
			if (displayBean != null) {
				((PNestedDisplayBean)displayBean).getPCanvas().removeMouseListener(nodePopupListener);
				// remove any actions added actions from the node menu
				nodePopupListener.removeAddedActions();
			}
			if (nodePopupMenu != null) {
				nodePopupMenu.removeAll();
				nodePopupMenu = null;
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createArcPopupMenu() {
		try {
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);

			arcPopupMenu = new JPopupMenu();
			arcPopupListener = new ArcPopupListener(arcPopupMenu, this);
			((PNestedDisplayBean)displayBean).getPCanvas().addMouseListener(arcPopupListener);
			//add a action manager listener so that the popup can repopulated if the menus change
			arcActionManagerListener = new ActionManagerListener() {
				public void actionsAdded(ActionsAddedEvent event) {
					arcPopupListener.repopulateArcPopupMenu();
				}
				public void actionsRemoved(ActionsRemovedEvent event) {
					arcPopupListener.repopulateArcPopupMenu();
				}
				public void actionsModified(ActionsModifiedEvent event) {
					arcPopupListener.repopulateArcPopupMenu();
				}
			};
			project.getActionManager().addActionManagerListener(arcActionManagerListener);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void removeArcPopupMenu() {
		try {
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			if (project != null) {
				project.getActionManager().removeActionManagerListener(arcActionManagerListener);
			} else {
				System.err.println("Warning - can't remove arc action manager listener");
			}
			if (displayBean != null) {
				((PNestedDisplayBean)displayBean).getPCanvas().removeMouseListener(arcPopupListener);
			}
			if (arcPopupMenu != null) {
				arcPopupMenu.removeAll();
				arcPopupMenu = null;
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a user action to the system
	 */
	public void addUserControl(UserAction userAction) {
		if (!userControls.contains(userAction)) {
			userControls.add(userAction);
			shrimpInputAdapter.addUserAction(userAction);
		}
	}

	public String getMouseMode() {
		return mouseMode;
	}

	public String[] getCprels() {
		return cprels;
	}

	/** loads the control preferences from the file */
	private void loadControlPreferences(Vector userActions) {
		for (Iterator iterator = userActions.iterator(); iterator.hasNext();) {
			UserAction userAction = (UserAction) iterator.next();
			userAction.setUserEvents(ApplicationAccessor.getApplication().getUserEvents(userAction));
		}
		shrimpInputAdapter.clearUserActions();
		for (int i = 0; i < userActions.size(); i++) {
			UserAction action = (UserAction) userActions.elementAt(i);
			shrimpInputAdapter.addUserAction(action);
		}
	}

	public void setMouseMode(String mode) {
		// set the cursor according to the mode
		Cursor cursor = null;
		if (DisplayConstants.MOUSE_MODE_SELECT.equals(mode)) {
			cursor = CURSOR_SELECT;
		} else if (DisplayConstants.MOUSE_MODE_ZOOM_IN.equals(mode)) {
			cursor = CURSOR_ZOOM_IN;
		} else if (DisplayConstants.MOUSE_MODE_ZOOM_OUT.equals(mode)) {
			cursor = CURSOR_ZOOM_OUT;
		} else {
			return; // illegal mouse mode, do nothing
		}

        try {
            PNestedDisplayBean nestedDisplayBean = (PNestedDisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			nestedDisplayBean.getPCanvas().setCursor(cursor);
            // @tag Shrimp(Cursors) : request focus to ensure the cursor changes
            nestedDisplayBean.requestFocus();
        } catch (BeanNotFoundException e) {
            e.printStackTrace();
        }

		//set and store the new mode
		ApplicationAccessor.getProperties().setProperty(MOUSE_MODE_PROPERTY_KEY, mode);
		mouseMode = mode;
		fireMouseModeChangedEvent(mode);
	}

	public void setCprels(String[] newCprels, boolean inverted, boolean shouldRefresh) {
	    if (CollectionUtils.haveSameElements(newCprels, getCprels()) && isInverted() == inverted) {
			return; // don't bother setting the cprels if the new ones are the same as the old
		}

		ApplicationAccessor.waitCursor();
	    try {
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			boolean dataBeanFiringEvents = dataBean.isFiringEvents();
			dataBean.setFiringEvents(false);
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);

			displayBean.setCprels(newCprels);
			displayBean.setInverted(inverted);

			//remove everything from the display
			if (shouldRefresh) {
			    refresh();
			}

			this.cprels = newCprels;
			this.inverted = inverted;
			fireCprelChangedEvent(newCprels);

			// change to the old cursor
			dataBean.setFiringEvents(dataBeanFiringEvents);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} finally {
			ApplicationAccessor.defaultCursor();
		}
	}

	public boolean isInverted() {
	    return inverted;
	}

	/**
	 * Causes this view to navigate in steps to a specific object.
	 */
	public void navigateToObject(Object destObject) {
		try {
			//Retrieve all the beans
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);

			Vector currentObjects = (Vector) displayBean.getCurrentFocusedOnObjects().clone();
			Object srcObject = null;
			if (!currentObjects.isEmpty() && !(currentObjects.elementAt(0) instanceof ShrimpArc)) {
			    srcObject = currentObjects;
			}

			Vector path = displayBean.getPathBetweenObjects(srcObject, destObject);
			if (!path.isEmpty()) {
				// check to see if any of the arts on the path are filtered
				for (int i = 1; i < path.size(); i++) {
					ShrimpNode nodeOnPath = (ShrimpNode) path.elementAt(i);
					if (displayBean.isFiltered(nodeOnPath.getArtifact())) {
						String msg = "Can't reach destination!  The destination, or a node on the path to it, is filtered.";
						JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), msg,
								"Shrimp Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				//create a temporary magnify-in adapter to do the work.
				selectorBean.setSelected (SelectorBeanConstants.TARGET_OBJECT, destObject);
				Object oldZoomMode = selectorBean.getSelected(DisplayConstants.ZOOM_MODE);
				selectorBean.setSelected(DisplayConstants.ZOOM_MODE, DisplayConstants.MAGNIFY);

				MagnifyInAdapter tempMIA = new MagnifyInAdapter(this);
				tempMIA.startAction();
				selectorBean.setSelected(DisplayConstants.ZOOM_MODE, oldZoomMode);
			}
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void setCopyBuffer(Vector artifacts, String cprel) {
		this.artifactCopyBuffer = artifacts;
		this.cprelForCopyBuffer = cprel;
	}

	public Vector getCopyBuffer() {
		return artifactCopyBuffer;
	}

	public String getCprelForCopyBuffer() {
		return cprelForCopyBuffer;
	}

	public void addShrimpViewListener(ShrimpViewListener shrimpViewListener) {
		if(!shrimpViewListeners.contains(shrimpViewListener)) {
			shrimpViewListeners.add(shrimpViewListener);
		}
	}

	public void removeShrimpViewListener(ShrimpViewListener shrimpViewListener) {
		shrimpViewListeners.remove(shrimpViewListener);
	}

	protected void fireCprelChangedEvent(String[] cprels) {
		Vector cloneListeners = (Vector) shrimpViewListeners.clone();

		for(int i=0; i<cloneListeners.size(); i++) {
			((ShrimpViewListener) cloneListeners.get(i)).shrimpViewCprelsChanged(new ShrimpViewCprelsChangedEvent(this, cprels));
		}
	}

	protected void fireMouseModeChangedEvent(String mouseMode) {
		Vector cloneListeners = (Vector) shrimpViewListeners.clone();

		for(int i=0; i<cloneListeners.size(); i++) {
			((ShrimpViewListener) cloneListeners.get(i)).shrimpViewMouseModeChanged(new ShrimpViewMouseModeChangedEvent(this, mouseMode));
		}
	}

	private void getNavigateMethod() {
		try {
			ActionHistoryBean actionHistoryBean = (ActionHistoryBean) getBean(ShrimpTool.ACTION_HISTORY_BEAN);
			SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);

			ActionManager actionManager = ShrimpView.this.project.getActionManager();
			String mode = (String) selectorBean.getSelected(DisplayConstants.ZOOM_MODE);
			try {
				CheckBoxAction chkZoom = (CheckBoxAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_ZOOM_MODE, ShrimpConstants.MENU_NAVIGATE);
				CheckBoxAction chkMagnify = (CheckBoxAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_MAGNIFY_MODE, ShrimpConstants.MENU_NAVIGATE);
				CheckBoxAction chkFisheye = (CheckBoxAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_FISHEYE_MODE, ShrimpConstants.MENU_NAVIGATE);
				chkZoom.setChecked(mode.equals(DisplayConstants.ZOOM));
				chkMagnify.setChecked(mode.equals(DisplayConstants.MAGNIFY));
				chkFisheye.setChecked(mode.equals(DisplayConstants.FISHEYE));

				actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_BACK, ShrimpConstants.MENU_NAVIGATE, actionHistoryBean.undoIsPossible());
				actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_FORWARD, ShrimpConstants.MENU_NAVIGATE, actionHistoryBean.redoIsPossible());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

    public void clear() {
		// change to a busy cursor
		ApplicationAccessor.waitCursor();
		try {
		    //clear the selector bean
			SelectorBean selectorBean = (SelectorBean)getBean(ShrimpTool.SELECTOR_BEAN);
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector());
			selectorBean.clearSelected(SelectorBeanConstants.TARGET_OBJECT);

			//remove everything from the display and data-display bridge
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.clear();
			displayBean.getDataDisplayBridge().refresh();

			// clear the quick search
			if (quickSearchPanel != null) {
				quickSearchPanel.getQuickSearchHelper().clearSearch();
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} finally {
			// change to the old cursor
			ApplicationAccessor.defaultCursor();
		}
    }

}