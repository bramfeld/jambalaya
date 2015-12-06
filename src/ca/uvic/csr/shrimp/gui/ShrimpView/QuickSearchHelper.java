/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.MagnifyZoomHandler;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.SearchBean.SearchResult;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.usercontrols.ExpandCollapseSubgraphAdapter;


/**
 * Performs a search and displays the results in the {@link ShrimpView} by using a {@link Filter}.
 * The filter hides all artifacts that aren't part of the search results.
 * It is a similar idea to the QueryView but doesn't add or remove any objects
 * from the {@link DisplayBean}.  It works just with whatever is displayed in the ShrimpView.
 *
 * @author Chris Callendar
 * @date 30-Mar-07
 */
public class QuickSearchHelper implements FilterChangedListener {

	private static final String SEARCH_CANCELLED = "Search cancelled";
	private static final String INVALID_SEARCH_STRING = "Invalid search string";
	private static final Color DEFAULT_LABEL_COLOR = Color.BLACK;
	private static final Color MATCH_LABEL_COLOR = new Color(224, 0, 0);

	public static final String PROP_INCOMING = "quicksearch.incoming";
	public static final String PROP_OUTGOING = "quicksearch.outgoing";

	public static final int MIN_INCOMING_LEVEL = -1;
	public static final int MIN_OUTGOING_LEVEL = 0;
	public static final int MAX_LEVEL = 3;
	public static final int DEFAULT_INCOMING_LEVEL = 1;
	public static final int DEFAULT_OUTGOING_LEVEL = 1;

	private ShrimpView shrimpView;
	private QuickSearchFilter filter;
	private Vector lastMatchingNodes;
	private int incomingLevel = DEFAULT_INCOMING_LEVEL;
	private int outgoingLevel = DEFAULT_OUTGOING_LEVEL;

	public QuickSearchHelper(ShrimpView shrimpView) {
		this.shrimpView = shrimpView;
		this.filter = new QuickSearchFilter();
		this.lastMatchingNodes = new Vector();

		// listen for changes - this is done to re-color the highlighted search results
		try {
			getFilterBean().addFilterChangedListener(this);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}

		loadLevels();
	}

	private void loadLevels() {
		Properties props = shrimpView.getProject().getProperties();
		try {
			int incoming = Integer.parseInt(props.getProperty(PROP_INCOMING, ""+DEFAULT_INCOMING_LEVEL));
			int outgoing = Integer.parseInt(props.getProperty(PROP_OUTGOING, ""+DEFAULT_OUTGOING_LEVEL));
			setLevels(incoming, outgoing);
		} catch (Exception ex) {
		}
	}

	public void saveLevels() {
		Properties props = shrimpView.getProject().getProperties();
		if ((incomingLevel == DEFAULT_INCOMING_LEVEL) && (outgoingLevel == DEFAULT_OUTGOING_LEVEL)) {
			// default values - so no point in storing
			props.remove(PROP_INCOMING);
			props.remove(PROP_OUTGOING);
		} else {
			props.setProperty(PROP_INCOMING, ""+incomingLevel);
			props.setProperty(PROP_OUTGOING, ""+outgoingLevel);
		}
	}

	/**
	 * Sets the incoming and outgoing levels.
	 */
	public void setLevels(int incoming, int outgoing) {
		this.incomingLevel = Math.max(MIN_INCOMING_LEVEL, Math.min(MAX_LEVEL, incoming));
		this.outgoingLevel = Math.max(MIN_OUTGOING_LEVEL, Math.min(MAX_LEVEL, outgoing));
	}

	public int getIncomingLevel() {
		return incomingLevel;
	}

	public int getOutgoingLevel() {
		return outgoingLevel;
	}

	public ShrimpView getShrimpView() {
		return shrimpView;
	}

	private DataBean getDataBean() throws BeanNotFoundException {
		return (DataBean) shrimpView.getProject().getBean(ShrimpProject.DATA_BEAN);
	}

	private SearchBean getSearchBean() throws BeanNotFoundException {
		return (SearchBean) shrimpView.getProject().getBean(ShrimpProject.SEARCH_BEAN);
	}

    private FilterBean getFilterBean() throws BeanNotFoundException {
       return (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
    }


    private SelectorBean getSelectorBean() throws BeanNotFoundException {
    	return (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
    }

    private DisplayBean getDisplayBean() throws BeanNotFoundException {
    	return (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
    }

	/**
	 * Listens for when the search results filter is removed and
	 * resets the last matching nodes back to their default label color and font.
	 */
	public void filterChanged(FilterChangedEvent fce) {
		if (fce.getRemovedFilters().contains(filter) && !lastMatchingNodes.isEmpty()) {
			try {
				// clear the colored nodes (last matches)
				colorMatchingNodes(getDisplayBean(), false);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
			lastMatchingNodes = new Vector(0);
		}
	}

	/**
	 * Removes the quick search filter.
	 */
	public void clearSearch() {
		try {
			FilterBean filterBean = getFilterBean();
			if (filterBean.contains(filter)) {
				filter.setAllowedArtifacts(Collections.EMPTY_SET);
				filter.setSearchText("");
				filterBean.removeFilter(filter);
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (FilterNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs the search on the given query string and using the given string matching mode.
	 * @param queryString the string to search - must not be empty
	 * @param stringMatchingMode the string matching mode - see
	 * @return the {@link SearchResult}
	 */
	public SearchResult performSearch(String queryString, int stringMatchingMode) {
		if (queryString.length() == 0) {
			return SearchResult.error(INVALID_SEARCH_STRING);
		}

		SearchResult result = new SearchResult();
		DisplayBean displayBean = null;
		ApplicationAccessor.waitCursor();
        try {
            ProgressDialog.showProgress();
            ProgressDialog.setSubtitle("Performing query...");
            ProgressDialog.setNote("querying data...");

            FilterBean filterBean = getFilterBean();
            DataBean dataBean = getDataBean();
            displayBean = getDisplayBean();
            displayBean.setEnabled(false);
			Vector artifactTypes = dataBean.getArtifactTypes(false, true);

            // 1. Perform the search
            Vector rootArtifacts = collectMatchingArtifacts(queryString, stringMatchingMode, artifactTypes);

            if (ProgressDialog.isCancelled()) {
    			return SearchResult.cancel(SEARCH_CANCELLED);
            }
            // warn if too many results
            int matchingNodes = rootArtifacts.size();
            final int maxMatches = 10;
            if ((maxMatches > 0) && (matchingNodes > maxMatches)) {
                String msg = "The query returned " + matchingNodes + " matching nodes.\n" +
                			"It may take a long time to display these nodes, continue?";
				int choice = JOptionPane.showConfirmDialog(ApplicationAccessor.getParentFrame(),
                		msg, "Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.NO_OPTION) {
        			return SearchResult.cancel(SEARCH_CANCELLED);
                }
            }

            result.setNodes(matchingNodes);
            if (matchingNodes > 0) {
                final Collection artifactsToDisplay = new HashSet();
                artifactsToDisplay.addAll(rootArtifacts);

                // 2. Find the neighbours
                Collection neighbours = collectNeighbours(artifactTypes, rootArtifacts);
    		    artifactsToDisplay.addAll(neighbours);
    		    result.setNeighbors(neighbours.size());

                if (ProgressDialog.isCancelled()) {
        			return SearchResult.cancel(SEARCH_CANCELLED);
                }

                // 3. If there is a nesting hierarchy then add and open all the ancestors
				if (!displayBean.isFlat()) {
	                HashSet allAncestors = collectAndOpenAncestors(displayBean, dataBean, artifactsToDisplay);
	                artifactsToDisplay.addAll(allAncestors);
                } else {
                	expandCollapsedNodes(displayBean, filterBean, artifactsToDisplay);
                }

                // 4. reset the last matched nodes to the default color and font
                colorMatchingNodes(displayBean, false);

                if (ProgressDialog.isCancelled()) {
        			return SearchResult.cancel(SEARCH_CANCELLED);
                }

                // 5. Color and the matching nodes
                lastMatchingNodes = displayBean.getDataDisplayBridge().getShrimpNodes(new Vector(rootArtifacts), true);
                colorMatchingNodes(displayBean, true);

				// 6. Apply the filter
                filter.setAllowedArtifacts(artifactsToDisplay);
                filter.setSearchText(queryString);
				filterBean.applyFilter(filter);

                if (ProgressDialog.isCancelled()) {
        			return SearchResult.cancel(SEARCH_CANCELLED);
                }

                // 7. Re-validate the gui
                JComponent gui = (JComponent) shrimpView.getGUI();
                gui.revalidate();
                gui.repaint();

                // 8. focus on the search results, or on the extents
                if (!displayBean.isFlat() && (rootArtifacts.size() >= 1)) {
					focusOnParents(displayBean, rootArtifacts);
                } else {
                	displayBean.focusOnExtents(false);
                }

                // 9. Select the matching nodes
				getSelectorBean().setSelected(SelectorBeanConstants.SELECTED_NODES, lastMatchingNodes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
            		"There was a problem performing the query: \n" + e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
            result.setMessage("Error performing search", true, false);
        } finally {
            ApplicationAccessor.defaultCursor();
            ProgressDialog.tryHideProgress();
            if (displayBean != null) {
            	displayBean.setEnabled(true);
            }
        }
        return result;
	}

	/**
	 * Focusses on the parents of the search results.
	 * This should only be done for a few search results and only in a nested view.
	 */
	private void focusOnParents(DisplayBean displayBean, Vector rootArtifacts) {
		Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes(rootArtifacts, true);
		Vector focusNodes = new Vector();
		// focus on the parents of the matching nodes
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			ShrimpNode node = (ShrimpNode) iter.next();
			focusNodes.addAll(displayBean.getDataDisplayBridge().getParentShrimpNodes(node));
		}
		if (focusNodes.size() == 0) {
			focusNodes.addAll(nodes);
		} else if (focusNodes.size() > 1) {
			System.out.println("What to focus on? " + focusNodes);
		}

		// slow the zooming down
		int time = MagnifyZoomHandler.getAnimationTime();
		MagnifyZoomHandler.setAnimationTime(3000);
		displayBean.focusOn(focusNodes);
		MagnifyZoomHandler.setAnimationTime(time);
	}

	private Vector collectMatchingArtifacts(String queryString, int matchingMode, Vector artifactTypes) throws BeanNotFoundException {
        // Run the search on the query string
        SearchBean searchBean = getSearchBean();
        ArtifactSearchStrategy strategy = (ArtifactSearchStrategy) searchBean.getStrategy(ArtifactSearchStrategy.NAME);
        strategy.setIncremental(false);
        strategy.setCaseSensitive(false);
        strategy.setSearchMode(matchingMode);
        Vector artifacts = getDataBean().getArtifacts(true);

        // This may take long time!
        strategy.run(artifacts, artifactTypes, queryString);
        return strategy.getSearchResults();
	}

	/**
	 * For the given artifact types and root artifacts it collects the neighbours
	 * and returns them.
	 */
	private Collection collectNeighbours(Vector artifactTypes, Collection rootArtifacts) throws BeanNotFoundException {
		Collection neighbours = new ArrayList();
		int maxLevel = Integer.MIN_VALUE;
		if (!ProgressDialog.isCancelled()) {
			int matchingNodes = rootArtifacts.size();
            String srcArtifactsFoundStr = matchingNodes + (matchingNodes == 1 ? " node" : " nodes");
		    ProgressDialog.setNote("getting neighbours for " + srcArtifactsFoundStr);
		    Vector relTypes = getDataBean().getRelationshipTypes(false, true);
		    // 1st level includes the srcArtifact
		    neighbours = getDataBean().getConnectedArtifacts(rootArtifacts, artifactTypes, relTypes, incomingLevel+1, outgoingLevel+1);
		    neighbours.removeAll(rootArtifacts); // we don't want the src artifacts to be considered neighbours
		    for (Iterator iter = neighbours.iterator(); iter.hasNext();) {
		        Artifact neighbour = (Artifact) iter.next();
		        // levels of neighbours returned range from 2 or more
		        int level = ((Integer) neighbour.getAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE)).intValue();
		        maxLevel = Math.max(maxLevel, level);
		     }
		}
		return neighbours;
	}

	/**
	 * Collects the ancestors artifacts and returns them.
	 * Also ensures that all the ancestor nodes are open.
	 * @return a set of {@link Artifact}s.
	 */
	private HashSet collectAndOpenAncestors(DisplayBean displayBean, DataBean dataBean, Collection artifactsToDisplay) {
		HashSet seenArtifacts = new HashSet();
		HashSet seenNodes = new HashSet();
		for (Iterator iter = artifactsToDisplay.iterator(); iter.hasNext(); ) {
			Artifact artifact = (Artifact) iter.next();
			if (!seenArtifacts.contains(artifact)) {
				seenArtifacts.add(artifact);
				// collect the ancestor artifact
				Vector ancestors = dataBean.getAncestors(artifact, displayBean.getCprels());
				seenArtifacts.addAll(ancestors);
				// get the ancestor nodes
				Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes(ancestors, true);
				// reverse the nodes - we want to start with the root node
				Collections.reverse(nodes);
				for (Iterator iter2 = nodes.iterator(); iter2.hasNext(); ) {
					ShrimpNode node = (ShrimpNode) iter2.next();
					if (!seenNodes.contains(node)) {
						seenNodes.add(node);
						// make sure the node is open so that the children are visible and laid out
						displayBean.openNode(node);
					}

				}
			}
		}
		return seenArtifacts;
	}

	private void expandCollapsedNodes(DisplayBean displayBean, FilterBean filterBean, Collection artifactsToDisplay) {
		HashSet seenArtifacts = new HashSet();
		HashSet seenNodes = new HashSet();
		Vector exclude = new Vector();
		for (Iterator iter = artifactsToDisplay.iterator(); iter.hasNext(); ) {
			Artifact artifact = (Artifact) iter.next();
			if (!seenArtifacts.contains(artifact)) {
				seenArtifacts.add(artifact);
				// get the shrimp nodes
				Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes(artifact, true);
				for (Iterator iter2 = nodes.iterator(); iter2.hasNext(); ) {
					ShrimpNode node = (ShrimpNode) iter2.next();
					if (!seenNodes.contains(node)) {
						seenNodes.add(node);
						if (node.isCollapsed()) {
							System.out.println("Expanding " + node.getName());
							ExpandCollapseSubgraphAdapter.expandCollapseNode(displayBean, filterBean, node,
									exclude, true, false, false);
							exclude.add(node);
						}
					}
				}
			}
		}
	}

	/**
	 * Iterates through the set of last matched nodes setting the label
	 * color and font.
	 * @param on if true then the label color is set to red and the font is bolded.
	 * 	Otherwise the default color (black) and plain font are used.
	 */
	protected void colorMatchingNodes(DisplayBean displayBean, boolean on) {
		Font defaultFont = (Font)displayBean.getLabelFont();
        Font boldFont = defaultFont.deriveFont(Font.BOLD);
		for (Iterator iter = lastMatchingNodes.iterator(); iter.hasNext(); ) {
			ShrimpNode node = (ShrimpNode) iter.next();
			ShrimpLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(node, false);
			if (label != null) {
				label.setFont(on ? boldFont : defaultFont);
				label.setTextColor(on ? MATCH_LABEL_COLOR : DEFAULT_LABEL_COLOR);
			}
		}
	}

	/**
	 * Search Results filter - hides all artifacts that aren't part of the search results.
	 * The results include the matched nodes and their neighbours, and all the ancestors.
	 *
	 * @author Chris Callendar
	 * @date 2-Apr-07
	 */
	class QuickSearchFilter implements Filter {

		// Collection of Artifact objects that should be displayed
		public Collection allowedArtifacts = Collections.EMPTY_SET;
		private String searchText;

		public void setAllowedArtifacts(Collection allowArtifacts) {
			this.allowedArtifacts = allowArtifacts;
		}

		public void setSearchText(String searchText) {
			this.searchText = searchText;
		}

		public String getFilterType() {
			return FilterConstants.QUICK_SEARCH_FILTER_TYPE + "\"" + searchText + "\"";
		}

		public String getTargetType() {
			return FilterConstants.ARTIFACT_FILTER_TYPE;
		}

		public boolean isFiltered(Object object) {
			boolean visible = true;
			if (object instanceof Artifact) {
				// filter any artifacts that aren't in this set
				visible = allowedArtifacts.contains(object);
			}
			return !visible;
		}

	}

}
