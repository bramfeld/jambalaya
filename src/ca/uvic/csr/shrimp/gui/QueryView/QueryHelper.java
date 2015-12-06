/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.Matcher;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.SearchBean.SearchResult;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ProgressDialog;

/**
 * Some code to help manage query parameters and perform a query.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class QueryHelper {

	private static final Color DEFAULT_LABEL_COLOR = Color.BLACK;
	private static final Color MATCH_LABEL_COLOR = new Color(224, 0, 0);
	private static final int SHOW_ALL_ANCESTORS = -1;
	private static final float MIN_TRANSPARENCY = 0.20f;
    private static final float MAX_TRANSPARENCY = 0.70f;
    private static final int DEFAULT_MAX_MATCHES = 10;
    private static final int DEFAULT_MAX_NEIGHBORS = 30;

    public static final int SEARCH_MODE_BY_NAME = 0;
    public static final int SEARCH_MODE_USE_SRC_ARTIFACTS = 1;

    public static final int STRING_MATCH_EXACT_MODE = Matcher.EXACT_MATCH_MODE;
    public static final int STRING_MATCH_CONTAINS_MODE = Matcher.CONTAINS_MODE;
    public static final int STRING_MATCH_STARTS_WITH_MODE = Matcher.STARTS_WITH_MODE;
    public static final int STRING_MATCH_ENDS_WITH_MODE = Matcher.ENDS_WITH_MODE;
    public static final int STRING_MATCH_REGEXP_MODE = Matcher.REGEXP_MODE;

    private String queryString = "";
    private Collection relTypes = new ArrayList(0);
    private Collection artTypes = new ArrayList(0);
    private boolean caseSensitive = false;
    private int stringMatchingMode = STRING_MATCH_CONTAINS_MODE;
    private int incomingLevels = 1;
    private int outgoingLevels = 1;
    private Collection srcArtifacts = Collections.EMPTY_LIST;
    private String layoutMode = LayoutConstants.LAYOUT_TREE_VERTICAL;
    private boolean changeTransparency = true;
    private int maxMatches = DEFAULT_MAX_MATCHES;
    private int maxNeighbours = DEFAULT_MAX_NEIGHBORS;
    private int searchMode = SEARCH_MODE_BY_NAME;

    private ShrimpTool tool;
    private Collection matchingNodes;

    public QueryHelper(ShrimpTool tool) {
        super();
        this.tool = tool;
        this.matchingNodes = Collections.EMPTY_LIST;
        if (tool instanceof QueryView) {
            ((QueryView)tool).setQueryHelper(this);
        }
    }

    /**
     * Returns the matching nodes from the last query.
     * @return {@link Collection} of {@link ShrimpNode} objects
     */
    public Collection getMatchingNodes() {
    	return matchingNodes;
    }

    public void clearMatchingNodes() {
        this.matchingNodes = Collections.EMPTY_LIST;
    }

    /**
     * Sets the maximum number of matches allowed before a warning is displayed.
     * Use -1 to indicate no warning.
     * @param maxMatches
     */
    public void setMaxMatches(int maxMatches) {
    	this.maxMatches = maxMatches;
    }

    /**
     * Sets the maximum number of neighbouring nodes allowed before a warning is displayed.
     * Use -1 to indicate no warning.
     * @param maxNeighbours
     */
    public void setMaxNeighbours(int maxNeighbours) {
    	this.maxNeighbours = maxNeighbours;
    }

    public void dispose() {
    	clear();
    }

    /**
     * Clears the artifact types, relationship types, source artifact name and the source artifacts.
     */
    public void clear() {
    	queryString = "";
    	clearMatchingNodes();
    	setArtifactTypes(Collections.EMPTY_LIST);
    	setRelationshipTypes(Collections.EMPTY_LIST);
    	setSrcArtifacts(Collections.EMPTY_LIST);
    }

    private boolean isDoNotShowProgress() {
    	boolean show = true;
    	if (tool instanceof QueryView) {
            show = ((QueryView)tool).isDoNotShowProgressDialog();
        }
    	return show;
    }

    public SearchResult doQuery() {
    	return doQuery(true);
    }

    public SearchResult doQuery(boolean animate) {
    	SearchResult result = new SearchResult();
    	if (tool == null) {
    		result.setError(true);
    		result.setMessage("QueryView is null");
    		return result;
    	}

    	// warn if the empty string is queried for
        queryString = queryString.trim();
		if ((queryString.length() == 0) && (searchMode == SEARCH_MODE_BY_NAME)) {
            int choice = JOptionPane.showConfirmDialog(ApplicationAccessor.getParentFrame(),
            		"Your search is empty - are you sure you want to run this query?\n" +
            		"If there are no filters set then all nodes will be returned.\nThis could take a very long time to complete.",
            		"Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.NO_OPTION) {
            	result.setCancelled(true);
            	return result;
            }
        }

		// possibly turn off the progress dialog
		boolean hideProgress = isDoNotShowProgress();
		boolean oldDoNotShowValue = ProgressDialog.isDoNotShow();
		ProgressDialog.setDoNotShow(hideProgress);

		getDisplayBean().setEnabled(false);
        ApplicationAccessor.waitCursor();
        try {
    		ProgressDialog.showProgress();
            ProgressDialog.setSubtitle("Performing query...");
            ProgressDialog.setNote("querying data...");

            DataBean dataBean = getDataBean();
            Collection rootArtifacts = new Vector();

            if (searchMode == SEARCH_MODE_BY_NAME) {
                // Run the search on the query string
	            SearchBean searchBean = getSearchBean();
	            ArtifactSearchStrategy artifactSearchStrategy = (ArtifactSearchStrategy) searchBean.getStrategy(ArtifactSearchStrategy.NAME);
	            artifactSearchStrategy.setIncremental(false);
	            artifactSearchStrategy.setCaseSensitive(caseSensitive);
	            artifactSearchStrategy.setSearchMode(stringMatchingMode);
	            Vector artifactTypesToSearch = new Vector();
	            artifactTypesToSearch.addAll(artTypes);

	            // This may take long time!
	            artifactSearchStrategy.run(dataBean.getArtifacts(true), artifactTypesToSearch, queryString);
	            rootArtifacts = artifactSearchStrategy.getSearchResults();
            } else {
                // Run the search on the src artifacts
            	rootArtifacts = srcArtifacts;
            }

            boolean alreadyWarned = false;
            int matchCount = rootArtifacts.size();
			// warn if too many results
            if ((maxMatches > 0) && (matchCount > maxMatches)) {
                int choice = JOptionPane.showConfirmDialog(ApplicationAccessor.getParentFrame(),
                		"The query returned " + matchCount + " matching nodes.\n" +
                		"It may take a long time to display these nodes, continue?",
                		"Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.NO_OPTION) {
                	result.setCancelled(true);
                	return result;
                }
                alreadyWarned = true;
            }

            if (matchCount > 0) {
            	result.setNodes(matchCount);
                String srcArtifactsFoundStr = matchCount + (matchCount == 1 ? " node" : " nodes");
                final Collection artifactsToDisplay = new HashSet();
                artifactsToDisplay.addAll(rootArtifacts);

                DisplayBean displayBean = getDisplayBean();

                // find neighbours
                Collection neighbours = new ArrayList();
                int maxLevel = Integer.MIN_VALUE;
                if (!ProgressDialog.isCancelled()) {
                	ProgressDialog.setNote("getting neighbours for " + srcArtifactsFoundStr);

                    neighbours = dataBean.getConnectedArtifacts(rootArtifacts, artTypes,
                    		relTypes, incomingLevels + 1, outgoingLevels + 1); // 1st level includes the srcArtifact
                    neighbours.removeAll(rootArtifacts); // we don't want the src artifacts to be considered neighbours
                    for (Iterator iter = neighbours.iterator(); iter.hasNext();) {
                        Artifact neighbour = (Artifact) iter.next();
                        // levels of neighbours returned range from 2 or more
                        int level = ((Integer) neighbour.getAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE)).intValue();
                        maxLevel = Math.max(maxLevel, level);
                     }

                    int neighboursSize = neighbours.size();
        			// warn if too many results
                    if (!alreadyWarned && (maxNeighbours > 0) && (neighboursSize > maxNeighbours)) {
                        int choice = JOptionPane.showConfirmDialog(ApplicationAccessor.getParentFrame(),
                        		"The query returned " + neighboursSize + " neighboring nodes.  Continue?",
                        		"Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice == JOptionPane.NO_OPTION) {
                        	result.setCancelled(true);
                        	return result;
                        }
                        alreadyWarned = true;
                    }

                    artifactsToDisplay.addAll(neighbours);
                    result.setNeighbors(neighboursSize);
                }

                // @tag Shrimp.QueryView : clearing the tool removes all nodes and arcs
                // TODO is this a good idea?  Sometimes there was a NullPointerException that happened
                // on PShrimpNode because it was disposed and then used again
                tool.clear();

                String nodesToDisplayStr = artifactsToDisplay.size() + (artifactsToDisplay.size() == 1 ? " node" : " nodes");
                Vector nodesToDisplay = new Vector();

                // remember the existing nodes - we will add new ones and remove the unused ones
                // if we cleared the tool above - this will always be empty
                //Vector existingNodes = displayBean.getAllNodes();

                // create all nodes
                if (!ProgressDialog.isCancelled()) {
                	ProgressDialog.setNote("creating " + nodesToDisplayStr + "...");
                    nodesToDisplay = displayBean.getDataDisplayBridge().getShrimpNodes(new Vector(artifactsToDisplay), true);
                }

                 // create all arcs
                Set arcsToDisplay = new HashSet();
                if (!ProgressDialog.isCancelled()) {
                	ProgressDialog.setNote("creating arcs for " + nodesToDisplayStr + "...");
                    for (Iterator iter = nodesToDisplay.iterator(); iter.hasNext();) {
                        ShrimpNode node = (ShrimpNode) iter.next();
                        Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node, true, false);
                        for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
                            ShrimpArc arc = (ShrimpArc) iterator.next();
                            if (!getFilterBean().isFiltered(arc.getRelationship())) {
                                arcsToDisplay.add(arc);
                            }
                        }
                    }
                }
                String arcsFoundStr = arcsToDisplay.size() + (arcsToDisplay.size() == 1 ? " arc" : " arcs");
                result.setArcs(arcsToDisplay.size());

                // add nodes to display
                if (!ProgressDialog.isCancelled()) {
                	ProgressDialog.setNote("adding " + nodesToDisplayStr + " and " + arcsFoundStr + " to the display...");
                    // add the resulting nodes
                    for (Iterator iter = nodesToDisplay.iterator(); iter.hasNext(); ) {
                    	ShrimpNode node = (ShrimpNode) iter.next();
                    	displayBean.addObject(node);
                    	displayBean.setVisible(node, true, true);
                    	//existingNodes.remove(node);

                    	// set the neighbors to be openable
                    	if ((node.getArtifact() != null) && neighbours.contains(node.getArtifact())) {
							node.setOpenable(true);
						}
                    }
                    // remove any remaining nodes (not part of the search result)
                    /*
                    while (existingNodes.size() > 0) {
                    	ShrimpNode node = (ShrimpNode) existingNodes.remove(0);
                    	displayBean.removeObject(node);
                    	displayBean.setVisible(node, false, false);
                    }*/
                }

                // set tranlucency of nodes, and colors of labels
                if (changeTransparency) {
                    Font defaultFont = (Font)displayBean.getLabelFont();
                    Font boldFont = defaultFont.deriveFont(Font.BOLD);
	                float alphaStep = (MAX_TRANSPARENCY - MIN_TRANSPARENCY) / (maxLevel - 1);
	                for (Iterator iter = nodesToDisplay.iterator(); iter.hasNext();) {
	                    ShrimpNode node = (ShrimpNode) iter.next();
	                    ShrimpNodeLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(node, false);
	                    Artifact artifact = node.getArtifact();
	                    float alpha = 1.0f;
	                    if (neighbours.contains(artifact)) {
	                        float level = ((Integer) artifact.getAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE)).floatValue();
	                        float numSteps = level - 2.0f;
	                        alpha = MAX_TRANSPARENCY - numSteps * alphaStep;
	                    } else {
	                        alpha = 1.0f;
	                    }
	                    node.setTransparency(alpha);
	                    if (label != null) {
	                        label.setTransparency(alpha);
	                        boolean isRoot = rootArtifacts.contains(artifact);
							label.setTextColor(isRoot ? MATCH_LABEL_COLOR : DEFAULT_LABEL_COLOR);
	                        label.setFont(isRoot ? boldFont : defaultFont);
	                    }
	                    //System.out.println(artifact.getName() + ": " + alpha);
	                }
	                for (Iterator iter = arcsToDisplay.iterator(); iter.hasNext();) {
	                    ShrimpArc arc = (ShrimpArc) iter.next();
	                    ShrimpNode srcNode = arc.getSrcNode();
	                    ShrimpNode destNode = arc.getDestNode();
	                    float srcT = srcNode.getTransparency();
	                    float destT = destNode.getTransparency();
	                    arc.setTransparency(Math.max(srcT, destT));
	                }
                }

                // get rid of the temporary "path distance" attributes so they don't show in the attributes panel
                for (Iterator iter = nodesToDisplay.iterator(); iter.hasNext();) {
                    ShrimpNode node = (ShrimpNode) iter.next();
                    node.getArtifact().setAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE, null);
                }

                Component gui = tool.getGUI();
                if (gui instanceof JComponent) {
                    ((JComponent)gui).revalidate();
                }
                gui.repaint();

                // layout nodes
                if (!ProgressDialog.isCancelled()) {
                	ProgressDialog.setNote("laying out " + nodesToDisplayStr + " and " + arcsFoundStr + "...");
                    // make sure the layout exists
                    if (!displayBean.hasLayout(layoutMode)) {
                    	System.err.println("Warning: couldn't use the '" + layoutMode + "' layout.  " +
                    			"Defaulting to " + LayoutConstants.LAYOUT_TREE_VERTICAL + ".");
                    	layoutMode = LayoutConstants.LAYOUT_TREE_VERTICAL;
                    }
                    displayBean.setLayoutMode(nodesToDisplay, layoutMode, false, animate);
                    displayBean.focusOnExtents(false);
                }

                // select the matching nodes (after layout and focus!)
                this.matchingNodes = displayBean.getDataDisplayBridge().getShrimpNodes(new Vector(rootArtifacts), true);
    			getSelectorBean().setSelected(SelectorBeanConstants.SELECTED_NODES, matchingNodes);

                if (ProgressDialog.isCancelled()) {
                    result.setCancelled(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
            		"There was a problem performing the query: \n" + e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
            result.setError(true);
            result.setMessage("Query error: " + e.getMessage());
        } finally {
            ApplicationAccessor.defaultCursor();
            ProgressDialog.tryHideProgress();
            getDisplayBean().setEnabled(true);
            ProgressDialog.setDoNotShow(oldDoNotShowValue);
        }
        return result;
    }

    public String getLayoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
    }

    public Collection getArtTypes() {
        return artTypes;
    }

    public void setArtifactTypes(Collection artTypes) {
        this.artTypes = artTypes;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public int getIncomingLevels() {
        return incomingLevels;
    }

    public int getOutgoingLevels() {
        return outgoingLevels;
    }

    public void setLevels(int incomingLevels, int outgoingLevels) {
        this.incomingLevels = Math.max(SHOW_ALL_ANCESTORS, incomingLevels);
        this.outgoingLevels = outgoingLevels;
    }

    public Collection getRelTypes() {
        return relTypes;
    }

    public void setRelationshipTypes(Collection relTypes) {
        this.relTypes = relTypes;
    }

    public int getStringMatchingMode() {
        return stringMatchingMode;
    }

    public void setStringMatchingMode(int stringMatchingMode) {
        this.stringMatchingMode = stringMatchingMode;
    }

	public int getSearchMode() {
		return searchMode;
	}

	/**
	 * @param searchMode The searchMode to set (by name, or by source artifacts).
	 */
	public void setSearchMode(int searchMode) {
		this.searchMode = searchMode;
	}

    public String getSrcArtifactName() {
        return queryString;
    }

    public void setSrcArtifactName(String srcArtifactName) {
        this.queryString = srcArtifactName;
        this.searchMode = SEARCH_MODE_BY_NAME;
    }

    public void setSrcArtifacts(Collection srcArtifacts) {
    	this.srcArtifacts = (srcArtifacts != null ? srcArtifacts : Collections.EMPTY_LIST);
        this.searchMode = SEARCH_MODE_USE_SRC_ARTIFACTS;
    }

    public Collection getSrcArtifacts() {
        return srcArtifacts;
    }

    private DisplayBean getDisplayBean() {
        DisplayBean displayBean = null;
        try {
            displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
        } catch (BeanNotFoundException e) {
            e.printStackTrace();
        }
        return displayBean;
    }

    private SelectorBean getSelectorBean() {
        SelectorBean selectorBean = null;
        try {
            selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
        } catch (BeanNotFoundException e) {
            e.printStackTrace();
        }
        return selectorBean;
    }

    private FilterBean getFilterBean() {
        FilterBean filterBean = null;
        try {
            filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
        } catch (BeanNotFoundException e) {
            e.printStackTrace();
        }
        return filterBean;
    }

    private DataBean getDataBean() {
        DataBean dataBean = null;
        ShrimpProject project = tool.getProject();
        if (project != null) {
            try {
                dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
            } catch (BeanNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        return dataBean;
    }

    private SearchBean getSearchBean() {
        SearchBean searchBean = null;
        ShrimpProject project = tool.getProject();
        if (project != null) {
            try {
                searchBean = (SearchBean) project.getBean(ShrimpProject.SEARCH_BEAN);
            } catch (BeanNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        return searchBean;
    }

    /**
     * @return Returns the changeTransparency.
     */
    public boolean isChangeTransparency() {
        return changeTransparency;
    }
    /**
     * @param changeTransparency The changeTransparency to set.
     */
    public void setChangeTransparency(boolean changeTransparency) {
        this.changeTransparency = changeTransparency;
    }

}