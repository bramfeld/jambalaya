/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectAdapter;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.GradientPanel;

/**
 * @author Rob Lintern
 */
public class SearchTool extends AbstractShrimpTool {

    /**
     * A tabbed pane is used to hold the component UI of each search strategy.
     */
    private JTabbedPane strategiesTabbedPane = null;
    private JRootPane gui;
    private ShrimpViewSearchToolBridge bridge;

    public SearchTool(ShrimpProject project) {
        super(ShrimpProject.SEARCH_TOOL, project);

        gui = new JRootPane();
        gui.setLayout(new BorderLayout());
        bridge = new ShrimpViewSearchToolBridge();
    }

    /**
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getGUI()
     */
    public Component getGUI() {
        return gui;
    }

    /**
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#disposeTool()
     */
    public void disposeTool() {
    	bridge.removeListeners();
    	clear();
    }
    
    /**
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
     */
    public void refresh() {
        if (project == null) {
			return;
		}

        gui.removeAll();
        try {
        	DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
	        TreeSet artifactNames = new TreeSet(Collator.getInstance());
        	Vector artifacts = dataBean.getArtifacts(true);
        	for (int i = 0; i < artifacts.size(); i++) {
        		Artifact artifact = (Artifact) artifacts.get(i);
        		artifactNames.add(artifact.getName());
        	}

            final SearchBean searchBean = (SearchBean) project.getBean(ShrimpProject.SEARCH_BEAN);
            searchBean.addSearchBeanListener(new SearchBeanListener() {
                 public void strategiesChanged() {
                	 searchBean.removeSearchBeanListener(this);	// important!
                     refresh();
                 }
            });
            JPanel pnlAllStrategies = new GradientPanel(new BorderLayout());
            pnlAllStrategies.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
            								BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            strategiesTabbedPane = new JTabbedPane();
            	strategiesTabbedPane.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    selectedStrategyChanged();
                }
            });
            strategiesTabbedPane.removeAll();
            for (Iterator iter = searchBean.getStrategies().iterator(); iter.hasNext();) {
                SearchStrategy searchStrategy = (SearchStrategy) iter.next();
                strategiesTabbedPane.addTab(searchStrategy.getName(), searchStrategy.getComponentUI());
                searchStrategy.setSearchItems(artifactNames);
            }
            pnlAllStrategies.add(strategiesTabbedPane, BorderLayout.CENTER);
            gui.add(pnlAllStrategies, BorderLayout.CENTER);
            gui.revalidate();
        } catch (BeanNotFoundException e) {
            // do nothing
        }

        bridge.init();
    }

    public void setSearchText(String text) {
    	if (project != null) {
			try {
				SearchBean searchBean = (SearchBean) project.getBean(ShrimpProject.SEARCH_BEAN);
		        for (Iterator iter = searchBean.getStrategies().iterator(); iter.hasNext();) {
		            SearchStrategy searchStrategy = (SearchStrategy) iter.next();
		            searchStrategy.setSearchText(text);
		        }
			} catch (BeanNotFoundException e) {
			}
    	}
    }

    private void selectedStrategyChanged() {
        if (project == null) {
			return;
		}

        try {
            SearchBean searchBean = (SearchBean) project.getBean(ShrimpProject.SEARCH_BEAN);
	        Component selectedStrategyGUI = strategiesTabbedPane.getSelectedComponent();
	        for (Iterator iter = searchBean.getStrategies().iterator(); iter.hasNext();) {
	            final SearchStrategy strategy = (SearchStrategy) iter.next();
	            if (strategy.getComponentUI().equals(selectedStrategyGUI)) {
	                SwingUtilities.invokeLater(new Runnable() {
	                    public void run() {
	                        setSearchButtonAsDefault(strategy);
	                    }
	                });
	                break;
	            }
	        }
        } catch (BeanNotFoundException e) {
            // do nothing
        }
   }

    private void setSearchButtonAsDefault(SearchStrategy selectedStrategy) {
        JButton btnSearch = selectedStrategy.getSearchButton();
        if (btnSearch != null) {
            gui.setDefaultButton(btnSearch);
        }
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#clear()
     */
    public void clear() {
    	gui.removeAll();
    }

    class ShrimpViewSearchToolBridge extends ShrimpProjectAdapter implements PropertyChangeListener {

    	public void init() {
    		try {
    			ShrimpProject project = getProject();
    			if (project != null) {
    				project.addProjectListener(this);
    				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
    				SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
    				selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, this);
    				setSelectionToSearchTool(selectorBean);
    			}
    		} catch (Exception e) {
    		}
    	}

    	public void removeListeners() {
    		ShrimpProject project = getProject();
    		if (project != null) {
    			project.removeProjectListener(this);
    			try {
    				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
    				SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
    				selectorBean.removePropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, this);
    			} catch (BeanNotFoundException e) {
    			} catch (ShrimpToolNotFoundException e) {
    			}
    		}
    	}

    	public void projectClosing(ShrimpProjectEvent event) {
    		event.getProject().removeProjectListener(this);
    		removeListeners();
    	}

    	public void propertyChange(PropertyChangeEvent evt) {
    		Vector oldSelected = (Vector) evt.getOldValue();
    		Vector newSelected = (Vector) evt.getNewValue();
    		if (!CollectionUtils.haveSameElements(oldSelected, newSelected)) {
    			setSelectionToSearchTool(newSelected);
    		}
    	}

    	public void setSelectionToSearchTool(SelectorBean selectorBean) {
    		if (selectorBean != null) {
    			Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
    			setSelectionToSearchTool(selectedNodes);
    		}
    	}

    	private void setSelectionToSearchTool(Vector selectedNodes) {
    		// get the artifacts from the nodes
    		if (selectedNodes.size() > 0) {
    			ShrimpNode node = (ShrimpNode) selectedNodes.get(0);
        		setSearchText(node.getArtifact().getName());
    		}
    	}

    }

}