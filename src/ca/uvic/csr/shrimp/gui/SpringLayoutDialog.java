/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.mylar.zest.layouts.algorithms.SpringLayoutAlgorithm;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * A dialog box for changing options of a {@link SpringLayoutAlgorithm} layout.
 */
public class SpringLayoutDialog extends JDialog {

	private JPanel jPanel = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel6 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel9 = new JLabel();
	private JTextField tfldStrain = new JTextField();
	private JLabel jLabel10 = new JLabel();
	private JTextField tfldLength = new JTextField();
	private JTextField tfldGravitation = new JTextField();
	private JTextField tfldMove = new JTextField();
	private JButton cancelBtn = new JButton();
	private JCheckBox chkRandomize = new JCheckBox();
	private JLabel jLabel11 = new JLabel();
	private JTextField tfldIterations = new JTextField();
	private JButton okBtn = new JButton();

	private SpringLayoutAlgorithm springLayoutAlgorithm;
	private boolean cancelled = false;


	/**
	 * @param spring The springLayoutAlgorithm layout associated with this dialog.
	 */
	public SpringLayoutDialog(SpringLayoutAlgorithm spring) {
		super(ApplicationAccessor.getParentFrame(), "Spring Layout Settings", true);
		this.springLayoutAlgorithm = spring;

		jPanel.setSize(new java.awt.Dimension(324, 392));
		jPanel.setLocation(new java.awt.Point(32, 32));
		jPanel.setVisible(true);
		jPanel.setLayout(null);
		jPanel.add(jLabel1);
		jPanel.add(jLabel4);
		jPanel.add(jLabel6);
		jPanel.add(jLabel5);
		jPanel.add(jLabel2);
		jPanel.add(jLabel9);
		jPanel.add(tfldStrain);
		jPanel.add(jLabel10);
		jPanel.add(tfldLength);
		jPanel.add(tfldGravitation);
		jPanel.add(tfldMove);
		jPanel.add(cancelBtn);
		jPanel.add(chkRandomize);
		jPanel.add(jLabel11);
		jPanel.add(tfldIterations);
		jPanel.add(okBtn);

		jLabel1.setSize(new Dimension(50, 20));
		jLabel1.setLocation(new Point(40, 22));
		jLabel1.setVisible(true);
		jLabel1.setText("strain:");
		jLabel1.setHorizontalAlignment(JLabel.RIGHT);
		jLabel1.setFont(new Font("Dialog", 0, 12));

		jLabel4.setSize(new Dimension(80, 22));
		jLabel4.setLocation(new Point(10, 44));
		jLabel4.setVisible(true);
		jLabel4.setText("springLayoutAlgorithm length:");
		jLabel4.setHorizontalAlignment(JLabel.RIGHT);
		jLabel4.setFont(new Font("Dialog", 0, 12));

		jLabel6.setSize(new Dimension(286, 20));
		jLabel6.setLocation(new Point(10, 88));
		jLabel6.setVisible(true);
		jLabel6.setText("Repulsive Force = gravitation/(distance^2)");

		jLabel5.setSize(new Dimension(306, 20));
		jLabel5.setLocation(new Point(10, 0));
		jLabel5.setVisible(true);
		jLabel5.setText("Attractive Force = strain * log (distance/springLayoutAlgorithm length) ");

		jLabel2.setSize(new Dimension(246, 20));
		jLabel2.setLocation(new Point(10, 158));
		jLabel2.setVisible(true);
		jLabel2.setText("Movement = move * total force");

		jLabel9.setSize(new Dimension(80, 20));
		jLabel9.setLocation(new Point(10, 110));
		jLabel9.setVisible(true);
		jLabel9.setText("gravitation:");
		jLabel9.setHorizontalAlignment(JLabel.RIGHT);
		jLabel9.setFont(new Font("Dialog", 0, 12));

		tfldStrain.setSize(new Dimension(200, 20));
		tfldStrain.setLocation(new Point(96, 22));
		tfldStrain.setVisible(true);

		jLabel10.setSize(new Dimension(60, 20));
		jLabel10.setLocation(new Point(30, 180));
		jLabel10.setVisible(true);
		jLabel10.setText("move:");
		jLabel10.setHorizontalAlignment(JLabel.RIGHT);
		jLabel10.setFont(new Font("Dialog", 0, 12));

		tfldLength.setSize(new Dimension(200, 20));
		tfldLength.setLocation(new Point(96, 44));
		tfldLength.setVisible(true);

		tfldGravitation.setSize(new Dimension(200, 20));
		tfldGravitation.setLocation(new Point(96, 110));
		tfldGravitation.setVisible(true);

		tfldMove.setSize(new Dimension(200, 20));
		tfldMove.setLocation(new Point(96, 180));
		tfldMove.setVisible(true);

		cancelBtn.setSize(new Dimension(76, 24));
		cancelBtn.setLocation(new Point(168, 356));
		cancelBtn.setVisible(true);
		cancelBtn.setText("Cancel");
		cancelBtn.setFont(new Font("Dialog", 0, 12));

		chkRandomize.setSize(new Dimension(172, 16));
		chkRandomize.setLocation(new Point(12, 268));
		chkRandomize.setVisible(true);
		chkRandomize.setText("Initial random placement");
		chkRandomize.setFont(new Font("Dialog", 0, 12));
		chkRandomize.setSelected(true);

		jLabel11.setSize(new Dimension(58, 20));
		jLabel11.setLocation(new Point(12, 228));
		jLabel11.setVisible(true);
		jLabel11.setText("Iterations:");
		jLabel11.setHorizontalAlignment(JLabel.TRAILING);

		tfldIterations.setSize(new Dimension(60, 20));
		tfldIterations.setLocation(new Point(76, 228));
		tfldIterations.setVisible(true);

		okBtn.setSize(new Dimension(76, 24));
		okBtn.setLocation(new Point(80, 356));
		okBtn.setVisible(true);
		okBtn.setText("OK");
		okBtn.setFont(new Font("Dialog", 0, 12));

		jPanel.setPreferredSize(new Dimension(320, 400));
		getContentPane().setLayout(new BorderLayout ());
		getContentPane().add(jPanel, BorderLayout.CENTER);

		tfldStrain.setText("" + spring.getSpringStrain());
		tfldLength.setText("" + spring.getSpringLength());
		tfldGravitation.setText("" + spring.getSpringGravitation());
		tfldMove.setText("" + spring.getSpringMove());
		tfldIterations.setText("" + spring.getIterations());
		chkRandomize.setSelected(spring.getRandom());

		okBtn.addActionListener ( new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				springLayoutAlgorithm.setSpringStrain((Double.valueOf(tfldStrain.getText())).doubleValue());
				springLayoutAlgorithm.setSpringLength((Double.valueOf(tfldLength.getText())).doubleValue());
				springLayoutAlgorithm.setSpringGravitation((Double.valueOf(tfldGravitation.getText())).doubleValue());
				springLayoutAlgorithm.setSpringMove((Double.valueOf(tfldMove.getText())).doubleValue());
				springLayoutAlgorithm.setIterations((Integer.valueOf(tfldIterations.getText())).intValue());
				springLayoutAlgorithm.setRandom(chkRandomize.isSelected());
				cancelled = false;
				dispose();
			}
		});


		cancelBtn.addActionListener ( new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				cancelled = true;
				dispose();
			}
		});

		pack();

		// put in center of screen
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(screen.getWidth() - this.getWidth()) / 2;
		int y = (int)(screen.getHeight() - this.getHeight()) / 2;
		setLocation (x,y);

		setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
	    addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	cancelled = true;
	        	dispose();
	        }
	    });
		setVisible(true);
	}

	/**
	 * Returns true if user has pushed cancel button.
	 */
	public boolean isCancelled () {
		return cancelled;
	}


}