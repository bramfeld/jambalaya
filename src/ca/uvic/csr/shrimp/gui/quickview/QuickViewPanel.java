/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * A panel containing buttons to invoke quick views.
 * These buttons can also be drag & drop enabled. Dragging and dropping on the
 * button will cause a new working set to be brought into Shrimp and the
 * appropriate quick view applied.

 * @author Rob Lintern, Chris Callendar
 */
public class QuickViewPanel implements QuickViewListener {

	private static final int WIDTH = 56;
	private static final int SCROLLBAR_WIDTH = 10;

    private JPanel pnlQuickView;
    private JPanel pnlButtons;
    private TransferHandler transferHandler;
    private QuickViewManager manager;
    //private ShrimpProject project;
    private JScrollPane scrollPane;
    private HashMap buttons;

    public QuickViewPanel(ShrimpProject project, TransferHandler transferHandler) {
        super();
        //this.project = project;
        this.transferHandler = transferHandler;
        this.manager = project.getQuickViewManager();
        this.buttons = new HashMap();

        createGUI();
    }

    private void createGUI() {
        pnlQuickView = new JPanel(new BorderLayout(0, 0));
        pnlButtons = new TransparentPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(ResourceHandler.getIcon("icon_label_quick_views.gif"));
        pnlQuickView.add(lbl, BorderLayout.NORTH);
		scrollPane = new JScrollPane(pnlButtons, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBorder(null);
		scrollPane.getViewport().setOpaque(false);
		pnlQuickView.add(scrollPane, BorderLayout.CENTER);
		reloadQuickViewButtons(null);
		manager.addQuickViewListenener(this);

		Action customAction = new AbstractAction("Customize the quick views for this project",
										ResourceHandler.getIcon("icon_quick_view_custom_views.gif")) {
        	public void actionPerformed(ActionEvent e) {
        		ActionManager actionManager = ApplicationAccessor.getApplication().getActionManager();
        		Action qvAction = actionManager.getAction(ShrimpConstants.TOOL_QUICK_VIEWS, ShrimpConstants.MENU_TOOLS);
        		if (qvAction != null) {
        			qvAction.actionPerformed(e);
        		}
        	}
        };

		JButton btn = createQuickViewButton(customAction, null);
		pnlQuickView.add(btn, BorderLayout.SOUTH);

		// set the maximum widths for all the containers
		pnlButtons.setMaximumSize(new Dimension(WIDTH, 2000));
		scrollPane.setMaximumSize(new Dimension(WIDTH, 2000));
		pnlQuickView.setMaximumSize(new Dimension(WIDTH, 2000));
		// use a narrower scrollbar - less overlapping with the buttons
		JScrollBar bar = scrollPane.getVerticalScrollBar();
		bar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, bar.getPreferredSize().height));
		Dimension psize = pnlQuickView.getPreferredSize();
		pnlQuickView.setPreferredSize(new Dimension(WIDTH, (psize.height > 0 ? psize.height : 600)));
    }

    public void quickViewsChanged(QuickViewEvent evt) {
    	reloadQuickViewButtons(evt);
    	if ((pnlButtons != null) && pnlButtons.isVisible()) {
    		pnlButtons.revalidate();
    		pnlButtons.repaint();
    	}
    }

    public JPanel getQuickViewPanel() {
        return pnlQuickView;
    }

    private void reloadQuickViewButtons(QuickViewEvent evt) {
    	if ((evt == null) || (evt.hasAddedActions() || evt.hasRemovedActions())) {
	    	pnlButtons.removeAll();
	    	buttons = new HashMap();
	    	QuickViewAction[] actions = manager.getSortedQuickViews();
			for (int i = 0; i < actions.length; i++) {
	            QuickViewAction action = actions[i];
            	JButton btn = createQuickViewButton(action, transferHandler);
            	boolean visible = action.isDisplay() && manager.isValid(action);
				btn.setVisible(visible);
            	pnlButtons.add(btn);
	    		buttons.put(action, btn);
	        }
    	} else if ((evt != null) && evt.hasChangedActions()) {
    		// just update the tooltip and icon
    		for (Iterator iter = evt.getChangedActions().iterator(); iter.hasNext(); ) {
    			QuickViewAction action = (QuickViewAction) iter.next();
    			if (buttons.containsKey(action)) {
	    			JButton btn = (JButton) buttons.get(action);
	    			btn.setToolTipText(action.getToolTip());
	    			btn.setIcon(action.getIcon());
	            	boolean visible = action.isDisplay() && manager.isValid(action);
    				btn.setVisible(visible);
    			}
    		}
    	}
    }

    /**
     * Creates a quick view button and adds the transfer handler if not null.
     * This method does not add the button to a panel, it just returns it.
     * @param action the action for the button
     * @param handler the transfer handler, can be null
     * @return the created button
     */
    private JButton createQuickViewButton(Action action, TransferHandler handler) {
	    JButton btnQuickView = new JButton(action);
	    btnQuickView.setToolTipText(btnQuickView.getText());
	    if (btnQuickView.getIcon() != null) {
	        // if the button has an icon then just show it without text
	        btnQuickView.setText(null);
	        btnQuickView.setIconTextGap(0);
	    }
        btnQuickView.setMargin(new Insets(1,1,1,1));
        btnQuickView.setPreferredSize(new Dimension(WIDTH, WIDTH));
        btnQuickView.setBackground(Color.WHITE);
        if (handler != null) {
            btnQuickView.setTransferHandler(handler);
        }
        return btnQuickView;
    }

}
