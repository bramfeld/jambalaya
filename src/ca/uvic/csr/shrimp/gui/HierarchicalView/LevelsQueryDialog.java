/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.HierarchicalView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeArtifact;

/**
 * @author Neil Ernst
 *
 * A Dialog which queries the user for the depth to expand a tree
 */
public class LevelsQueryDialog extends JDialog implements ChangeListener {

	private JSlider slider;
	private JLabel valLbl;
	private JLabel msgLbl;
	private JButton okBtn;
	private static final int DEFAULT_VALUE = 3;
	private int levels = -99;
	private int min = 1;
	private int max = -1;
	
    
    public LevelsQueryDialog(Frame frame, Vector selectedNodes, DisplayBean displayBean) {   
    	super(frame, "Select # of Levels", true);
    	min = 1;
    	max = -1;
    	getMaxDepth(selectedNodes, displayBean); //try to set maximum
    	if (max == -1) {
    	    
    	} else {
    		levels = Math.min(DEFAULT_VALUE, max);
    		initComponents();
    	}
    }
    
    /**
	 * Set this shiznat up
	 */
	private void initComponents() {
		JPanel jPanel = new JPanel();
		jPanel.setSize(new java.awt.Dimension(275, 150));
		jPanel.setLocation(new java.awt.Point(32, 32));
		jPanel.setVisible(true);
		jPanel.setLayout(new BorderLayout());
		
		slider = new JSlider(min, max, Math.min(DEFAULT_VALUE, max));
    	slider.setPaintTicks(true);
    	slider.setPaintLabels(true);
    	slider.setMajorTickSpacing(1);
    	slider.setSnapToTicks(true);
    	slider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
    	slider.addChangeListener(this);
    	jPanel.add(slider, BorderLayout.CENTER);
    	
    	valLbl = new JLabel(" current value: " + new Integer(DEFAULT_VALUE).toString());
    	jPanel.add(valLbl, BorderLayout.EAST);
    	
        msgLbl = new JLabel("To what depth should the tree be expanded below this node?");
        jPanel.add(msgLbl, BorderLayout.NORTH);
        
        okBtn = new JButton("Accept");
        okBtn.setPreferredSize(new Dimension(70, 30));
        okBtn.setMaximumSize(new Dimension(70,30));
        okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				close();
			}
        });
        jPanel.add(okBtn, BorderLayout.SOUTH);
        
        getContentPane().add(jPanel);
		jPanel.setPreferredSize(new java.awt.Dimension(275, 150));
        pack();
        repaint();

        // put in center of screen
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(screen.getWidth() - this.getWidth()) / 2;
		int y = (int)(screen.getHeight() - this.getHeight()) / 2;
		setLocation (x,y);
	
		setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
	    addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	dispose();
	        }
	    });
	    
	    setVisible(true);
	}

	/**
     * Listen to the slider
     * @param e
     */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        levels = source.getValue();
        valLbl.setText(" current value: " + new Integer(levels).toString());
    }
    
    public void getMaxDepth (Vector selectedNodes, DisplayBean displayBean) {
    	if (selectedNodes == null) return;
    	max = -1;
    	for (int i = 0; i < selectedNodes.size(); i++) {
            ShrimpNode node = (ShrimpNode) selectedNodes.elementAt(i);
        	Artifact art = node.getArtifact();
        	int maxForThisNode = -1;
        	if (art instanceof ProtegeArtifact) {
        		ProtegeArtifact protegeArt = (ProtegeArtifact) art;
                maxForThisNode = protegeArt.getTreeDepth();
        	} else if (art != null) {
        	    maxForThisNode = findMaxDepthDefault (node, displayBean);
        	} 
    	    if (maxForThisNode > max) { //we want the largest maximum
    		    max = maxForThisNode;
    		} 
        }
    }
    
    private int findMaxDepthDefault (ShrimpNode node, DisplayBean displayBean) {
        int depth = -1;
        Vector descendents = displayBean.getDataDisplayBridge().getDescendentNodes(node, true);
        for (Iterator iter = descendents.iterator(); iter.hasNext();) {
            ShrimpNode descendentNode = (ShrimpNode) iter.next();
            int depthOfThisDesc = node.getLevel() - descendentNode.getLevel();
            if (depthOfThisDesc > depth) {
                depth = depthOfThisDesc;
            }
        }
        return depth;
    }
    

	/**
	 * @return the number of levels deep to open a tree
	 */
	public int getLevels() {
		return levels;
	}
	
	private void close () {
    	setVisible(false);
		dispose();
	}
	
}
