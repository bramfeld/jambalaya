/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.CollapsiblePanel;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.HyperlinkAdapter;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * The filter tool displays the currently applied {@link Filter}s.
 * At the moment it only shows the filters found in the display {@link FilterBean}, and does
 * not show any of the filters from the data {@link FilterBean}.
 * <br>
 * It allows the user to temporarily disable filters and then later re-apply them.
 * <br>
 * Also allows the user to remove some of the filtered attribute values individually.
 * <br>
 * 
 * @see FilterBean
 * @see Filter
 * @author Chris Callendar
 */
public class FilterTool extends AbstractShrimpTool implements FilterChangedListener {

	private static final Color CURRENT_FG = Color.black;
	private static final Color CURRENT_BG = Color.white;
	private static final Color REMOVED_FG = new Color(128, 128, 128);
	private static final Color REMOVED_BG = Color.white;
	private static final Dimension FILTER_ROW_SINGLE = new Dimension(2000, 28);
	private static final Dimension BUTTON = new Dimension(24, 24);

	private JPanel gui;
    private GradientPanel gradientPanel;
    private CollapsiblePanel appliedCollapsePanel;
    private JPanel appliedFiltersPanel;
    private CollapsiblePanel disabledCollapsePanel;
    private JPanel disabledFiltersPanel;
    private List disabledFilters;

    public FilterTool(ShrimpProject project) {
        super(ShrimpProject.FILTER_TOOL, project);
        this.disabledFilters = new ArrayList();
        createGUI();
    }

	private void createGUI() {
		gui = new TransparentPanel(new BorderLayout());
		gradientPanel = new GradientPanel(new BorderLayout(0, 10));
		final JScrollPane scroll = new JScrollPane(gradientPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		gui.add(scroll, BorderLayout.CENTER);

        gradientPanel.setEmptyBorder(10);
        appliedFiltersPanel = new TransparentPanel();
        appliedFiltersPanel.setLayout(new BoxLayout(appliedFiltersPanel, BoxLayout.Y_AXIS));
        appliedCollapsePanel = new CollapsiblePanel("Applied Filters", appliedFiltersPanel) {
        	public Dimension getPreferredSize() {
        		int width = gui.getWidth() - (scroll.getVerticalScrollBar().isVisible() ? 38 : 20);
				return new Dimension(width, super.getPreferredSize().height);
        	}
        };
		gradientPanel.add(appliedCollapsePanel, BorderLayout.NORTH);

        disabledFiltersPanel = new TransparentPanel();
        disabledFiltersPanel.setLayout(new BoxLayout(disabledFiltersPanel, BoxLayout.Y_AXIS));
        disabledCollapsePanel = new CollapsiblePanel("Disabled Filters", disabledFiltersPanel) {
	    	public Dimension getPreferredSize() {
	    		int width = gui.getWidth() - (scroll.getVerticalScrollBar().isVisible() ? 38 : 20);
				return new Dimension(width, super.getPreferredSize().height);
	    	}
	    };
		gradientPanel.add(disabledCollapsePanel, BorderLayout.CENTER);

		gui.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				gradientPanel.revalidate();
			}
		});
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
    	removeFilterChangedListener();
    	clear();
    }

	private void removeFilterChangedListener() {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				filterBean.removeFilterChangedListener(this);
				//FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
		        //dataFilterBean.removeFilterChangedListener(this);
			} catch (BeanNotFoundException e) {
			} catch (ShrimpToolNotFoundException e) {
			}
    	}
	}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#clear()
     */
    public void clear() {
    	appliedFiltersPanel.removeAll();
    	disabledFiltersPanel.removeAll();
    	disabledFilters.clear();
    }

    /**
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
     */
    public void refresh() {
        if (project == null) {
            return;
        }

        try {
        	// Display FilterBean
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			FilterBean displayFilterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
            displayFilterBean.addFilterChangedListener(this);
            refreshAppliedFilters(displayFilterBean);
            refreshDisabledFilters(displayFilterBean);

        	// Data FilterBean
            //FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
            //dataFilterBean.addFilterChangedListener(this);
            //refreshAppliedFilters(dataFilterBean);
            //refreshDisabledFilters(dataFilterBean);
        } catch (BeanNotFoundException e) {
		} catch (ShrimpToolNotFoundException e) {
        }
        gradientPanel.revalidate();
    }

	private void refreshDisabledFilters(final FilterBean filterBean) {
		disabledFiltersPanel.removeAll();
		for (Iterator iter = disabledFilters.iterator(); iter.hasNext(); ) {
         	final Filter filter = (Filter) iter.next();
    		JPanel pnl =  createPanelForFilter(filter, filterBean, REMOVED_BG, REMOVED_FG, false);
         	disabledFiltersPanel.add(pnl);
         	addDisabledFiltersButtons(filterBean, filter, pnl);
        }
		disabledFiltersPanel.revalidate();
	}

	private void addDisabledFiltersButtons(final FilterBean filterBean, final Filter filter, JPanel pnl) {
		JPanel btnPanel = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		btnPanel.setMaximumSize(FILTER_ROW_SINGLE);
		JButton btn = new JButton(new AbstractAction("", ResourceHandler.getIcon("icon_new2.gif")) {
			public void actionPerformed(ActionEvent e) {
				addFilter(filter, filterBean);
			}
		});
		btn.setToolTipText("Apply this filter (it will appear in the list above)");
		btn.setPreferredSize(BUTTON);
		btnPanel.add(btn);

		btn = new JButton(new AbstractAction("", ResourceHandler.getIcon("icon_delete.gif")) {
			public void actionPerformed(ActionEvent e) {
				disabledFilters.remove(filter);
				refreshDisabledFilters(filterBean);
			}
		});
		btn.setToolTipText("Discard this filter");
		btn.setPreferredSize(BUTTON);
		btnPanel.add(btn);
		pnl.add(btnPanel, BorderLayout.EAST);
	}

	private void refreshAppliedFilters(final FilterBean filterBean) {
		appliedFiltersPanel.removeAll();
        Vector filters = filterBean.getFilters();
        for (Iterator iter = filters.iterator(); iter.hasNext(); ) {
        	final Filter filter = (Filter) iter.next();
        	// remove duplicate filters
        	if (disabledFilters.contains(filter)) {
        		disabledFilters.remove(filter);
        	}
        	JPanel pnl = createPanelForFilter(filter, filterBean, CURRENT_BG, CURRENT_FG, true);
        	appliedFiltersPanel.add(pnl);
    		addAppliedFiltersButtons(filterBean, filter, pnl);
        }
        appliedFiltersPanel.revalidate();
	}

	private void addAppliedFiltersButtons(final FilterBean filterBean, final Filter filter, JPanel pnl) {
		JButton btn = new JButton(new AbstractAction("", ResourceHandler.getIcon("icon_delete.gif")) {
			public void actionPerformed(ActionEvent e) {
				removeFilter(filter, filterBean);
			}
		});
		btn.setToolTipText("Disable this filter (it will appear in the list below)");
		btn.setPreferredSize(BUTTON);

		JPanel btnPanel = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		btnPanel.setMaximumSize(FILTER_ROW_SINGLE);
		btnPanel.add(btn);
		pnl.add(btnPanel, BorderLayout.EAST);
	}

	private JPanel createPanelForFilter(final Filter filter, final FilterBean filterBean, Color bg, Color fg, boolean addRemoveLink) {
		String filterType = filter.getFilterType();
		String targetType = filter.getTargetType();
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBackground(bg);
		pnl.setBorder(BorderFactory.createEtchedBorder());

		JLabel lbl = new JLabel(" " + filterType + " (" + targetType + ") ");
		lbl.setForeground(fg);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));

		if (filter instanceof AttributeFilter) {
			final AttributeFilter attrFilter = (AttributeFilter)filter;
			String text = getStringValueFromAttributeFilter(attrFilter, addRemoveLink);
			final JEditorPane area = new JEditorPane("text/html", text);
			if (addRemoveLink) {
				area.addHyperlinkListener(new HyperlinkAdapter() {
					public void hyperlinkActivated(URL url) {
						if (url != null) {
							area.removeHyperlinkListener(this);
							if (filter instanceof NominalAttributeFilter) {
								NominalAttributeFilter naf = (NominalAttributeFilter) filter;
								// get the id from the url, strip the leading http://
								String str = url.toString().substring("http://".length());
								Long id = new Long(str);
								Collection filteredValues = naf.getFilteredValuesReference();
								int count = filteredValues.size();
								if (filteredValues.contains(id)) {
									filterBean.addRemoveSingleNominalAttrValue(attrFilter.getAttributeName(), attrFilter.getAttributeType(), filter.getTargetType(), id, false);
									// if it was the only one, remove the filter too
									if (count == 1) {
										try {
											filterBean.removeFilter(filter);
										} catch (FilterNotFoundException e) {}
									}
								}
							}
						}
					}
				});
			}
			area.setEditable(false);
			area.setForeground(fg);
			area.setFont(pnl.getFont());
			area.setBackground(pnl.getBackground());
			area.setBorder(BorderFactory.createEmptyBorder(0, 3, 2, 3));
			//JScrollPane scroll = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			//scroll.setBorder(null);
			JPanel innerPanel = new TransparentPanel(new BorderLayout());
			innerPanel.add(lbl, BorderLayout.NORTH);
			innerPanel.add(area, BorderLayout.CENTER);
			pnl.add(innerPanel, BorderLayout.CENTER);
			//pnl.setMaximumSize(FILTER_ROW_MULTI);
		} else {
			pnl.add(lbl, BorderLayout.CENTER);
			pnl.setMaximumSize(FILTER_ROW_SINGLE);
		}
		return pnl;
	}

	private String getStringValueFromAttributeFilter(AttributeFilter filter, boolean addRemoveLink) {
		String str = "attribute: <font color='#336699'>" + filter.getAttributeName() + "</font><br/>";
		if (filter instanceof NominalAttributeFilter) {
			NominalAttributeFilter nomFilter = (NominalAttributeFilter) filter;
			StringBuffer s = new StringBuffer();
			Collection filteredValues = nomFilter.getFilteredValuesReference();
			for (Iterator iter = filteredValues.iterator(); iter.hasNext();) {
				Object filteredValue = iter.next();
				String value = filteredValue.toString();
				s.append("<li>  - " + value);
				
				// special case - if the artifact id is being filtered, then display the
				// artifact name too, makes it more readable
				if (AttributeConstants.NOM_ATTR_ARTIFACT_ID.equals(filter.getAttributeName())) {
					try {
						long id = Long.parseLong(value, 10);
						DataBean databean = (DataBean)project.getBean(ShrimpProject.DATA_BEAN);
						Artifact art = databean.getArtifact(id);
						if (art != null) {
							s.append(" [" + art.getName() + "]");
						}
					} catch (Exception e) {
					}
				} else if (AttributeConstants.NOM_ATTR_REL_ID.equals(filter.getAttributeName())) {
					try {
						long id = Long.parseLong(value, 10);
						DataBean databean = (DataBean)project.getBean(ShrimpProject.DATA_BEAN);
						Relationship rel = databean.getRelationship(id);
						if (rel != null) {
							s.append(" [" + rel.getName() + "]");
						}
					} catch (Exception e) {
					}
				}
				if (addRemoveLink) {
					// add the remove hyperlink - we need to use the url format for this to work
					s.append("  <a href=\"http://" + value + "\">x</a></li>");
				}
			}
			int size = s.length();
			if (size > 0) {
				str += "filtered value" + (size == 1 ? ": " : "s: ") + s.toString();
			}
		} else if (filter instanceof OrdinalAttributeFilter) {
			OrdinalAttributeFilter ordFilter = (OrdinalAttributeFilter) filter;
			str += "min: " + ordFilter.getMinOfUnfiltered().toString() +
				  ", max: " + ordFilter.getMaxOfUnfiltered().toString();
		}
		return str;
	}

    public void filterChanged(FilterChangedEvent fce) {
    	refresh();
    }

    private void removeFilter(Filter filter, FilterBean filterBean) {
    	disabledFilters.add(filter);
    	try {
			filterBean.removeFilter(filter);
		} catch (FilterNotFoundException e) {
		}
    }

    private void addFilter(Filter filter, FilterBean filterBean) {
    	disabledFilters.remove(filter);
		filterBean.addFilter(filter);
    }

}