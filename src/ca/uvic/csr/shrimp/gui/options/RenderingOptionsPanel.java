/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.MagnifyZoomHandler;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNormalZoomHandler;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * This Dialog lets the user choose rendering options,
 * such as the speed the screen will move at when zooming.
 *
 * @author YiLing Lu, Rob Lintern, Chris Callendar
 * @date May 07, 2001
 */
public class RenderingOptionsPanel extends JPanel implements ShrimpOptions {

	protected String toolName;
	protected ShrimpProject project;

	protected JSlider magnifySlider;
	protected JSlider zoomSlider;
	private JRadioButton rdoStaticRendQualLow;
	private JRadioButton rdoStaticRendQualHigh;

	private JRadioButton rdoDynamicRendQualLow;
	private JRadioButton rdoDynamicRendQualHigh;
	private ButtonGroup grpDynamicRendQual;

	private int staticRendQual;
	private int dynamicRendQual;

	public int magnifySelectedSpeed;
	public int zoomSelectedSpeed;

	private final int LABEL_WIDTH = 170;

	/**
	 * Constructor for RenderingOptionsPanel
	 * @param toolName The name of the tool to apply the rendering options to.
	 */
	public RenderingOptionsPanel(String toolName, ShrimpProject project) {
		this.toolName = toolName;
		this.project = project;

		loadInitialValues();
		initialize();
		setInitialValues();
	}

	private void loadInitialValues() {
		staticRendQual = PPaintContext.HIGH_QUALITY_RENDERING;
		dynamicRendQual = PPaintContext.HIGH_QUALITY_RENDERING;
		try {
			DisplayBean displayBean = (DisplayBean) project.getTool(toolName).getBean(ShrimpTool.DISPLAY_BEAN);
			staticRendQual = displayBean.getStaticRenderingQuality();
			dynamicRendQual = displayBean.getDynamicRenderingQuality();
		} catch (BeanNotFoundException e1) {
		} catch (ShrimpToolNotFoundException e1) {
		}
		zoomSelectedSpeed = PNormalZoomHandler.getZoomSpeed();
	}

	private void setInitialValues() {
		switch (staticRendQual) {
			case PPaintContext.LOW_QUALITY_RENDERING :
				rdoStaticRendQualLow.setSelected(true);
				break;
			case PPaintContext.HIGH_QUALITY_RENDERING :
				rdoStaticRendQualHigh.setSelected(true);
				break;

			default :
				rdoStaticRendQualHigh.setSelected(true);
				break;
		}
		switch (dynamicRendQual) {
			case PPaintContext.LOW_QUALITY_RENDERING :
				rdoDynamicRendQualLow.setSelected(true);
				break;
			case PPaintContext.HIGH_QUALITY_RENDERING :
				rdoDynamicRendQualHigh.setSelected(true);
				break;
			default :
				rdoDynamicRendQualHigh.setSelected(true);
				break;
		}
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.add(createStaticPanel());
		top.add(createDynamicPanel());
		top.add(createMagnifyPanel());
		top.add(createZoomPanel());
		add(top, BorderLayout.NORTH);
	}

	private JPanel createStaticPanel() {
		// rendering quality
		rdoStaticRendQualLow = new JRadioButton("Low");
		rdoStaticRendQualHigh = new JRadioButton("High");
		ButtonGroup grpStaticRendQual = new ButtonGroup();
		grpStaticRendQual.add(rdoStaticRendQualLow);
		grpStaticRendQual.add(rdoStaticRendQualHigh);

		ActionListener rdoStaticActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				staticRendQual = (rdoStaticRendQualLow.isSelected() ? PPaintContext.LOW_QUALITY_RENDERING :
					PPaintContext.HIGH_QUALITY_RENDERING);
			}
		};
		rdoStaticRendQualLow.addActionListener(rdoStaticActionListener);
		rdoStaticRendQualHigh.addActionListener(rdoStaticActionListener);

		JLabel lbl = new JLabel("Static Rendering Quality: ");
		lbl.setPreferredSize(new Dimension(LABEL_WIDTH, 16));

		JPanel pnlStatic = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlStatic.setBorder(BorderFactory.createEtchedBorder());
		pnlStatic.add(lbl);
		pnlStatic.add(rdoStaticRendQualLow);
		pnlStatic.add(rdoStaticRendQualHigh);
		return pnlStatic;
	}

	private JPanel createDynamicPanel() {
		// interacting rendering quality
		rdoDynamicRendQualLow = new JRadioButton("Low");
		rdoDynamicRendQualHigh = new JRadioButton("High");
		grpDynamicRendQual = new ButtonGroup();
		grpDynamicRendQual.add(rdoDynamicRendQualLow);
		grpDynamicRendQual.add(rdoDynamicRendQualHigh);

		ActionListener rdoDynamicActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dynamicRendQual = (rdoDynamicRendQualLow.isSelected() ? PPaintContext.LOW_QUALITY_RENDERING :
						PPaintContext.HIGH_QUALITY_RENDERING);
			}
		};
		rdoDynamicRendQualLow.addActionListener(rdoDynamicActionListener);
		rdoDynamicRendQualHigh.addActionListener(rdoDynamicActionListener);

		JLabel lbl = new JLabel("Dynamic Rendering Quality: ");
		lbl.setPreferredSize(new Dimension(LABEL_WIDTH, 16));

		JPanel pnlDynamic = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlDynamic.setBorder(BorderFactory.createEtchedBorder());
		pnlDynamic.add(lbl);
		pnlDynamic.add(rdoDynamicRendQualLow);
		pnlDynamic.add(rdoDynamicRendQualHigh);
		return pnlDynamic;
	}

	private JPanel createZoomPanel() {
		Dictionary map = new Hashtable();
		map.put(new Integer(0), new JLabel("0"));
		map.put(new Integer(25), new JLabel("25"));
		map.put(new Integer(50), new JLabel("50"));
		map.put(new Integer(75), new JLabel("75"));
		map.put(new Integer(100), new JLabel("100"));

		// set up the zoom slider
		zoomSlider = new JSlider(JSlider.HORIZONTAL, PNormalZoomHandler.MIN_ZOOM_PAUSE,
				PNormalZoomHandler.MAX_ZOOM_PAUSE, zoomSelectedSpeed);
		zoomSlider.setMajorTickSpacing(25);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setLabelTable(map);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					zoomSelectedSpeed = source.getValue();
				}
			}
		});
		JLabel lbl = new JLabel("Zoom Speed (delay):  ");
		lbl.setPreferredSize(new Dimension(LABEL_WIDTH, 16));

		JPanel zoomSliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		zoomSliderPanel.setBorder(BorderFactory.createEtchedBorder());
		zoomSliderPanel.add(lbl);
		zoomSliderPanel.add(new JLabel("Fast"));
		zoomSliderPanel.add(zoomSlider);
		zoomSliderPanel.add(new JLabel("Slow"));
		return zoomSliderPanel;
	}

	private JPanel createMagnifyPanel() {
		Dictionary labelsMap = new Hashtable();
		labelsMap.put(new Integer(0), new JLabel("0"));
		labelsMap.put(new Integer(1000), new JLabel("1"));
		labelsMap.put(new Integer(2000), new JLabel("2"));
		labelsMap.put(new Integer(3000), new JLabel("3"));
		labelsMap.put(new Integer(4000), new JLabel("4"));
		labelsMap.put(new Integer(5000), new JLabel("5"));

		// setup the magnify slider
		magnifySelectedSpeed = MagnifyZoomHandler.getAnimationTime();
		magnifySlider = new JSlider(JSlider.HORIZONTAL, MagnifyZoomHandler.MIN_ANIMATION_TIME,
				MagnifyZoomHandler.MAX_ANIMATION_TIME, magnifySelectedSpeed);
		magnifySlider.setMajorTickSpacing(1000);
		magnifySlider.setPaintTicks(true);
		magnifySlider.setPaintLabels(true);
		magnifySlider.setLabelTable(labelsMap);
		magnifySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					magnifySelectedSpeed = source.getValue();
				}
			}
		});

		JLabel lbl = new JLabel("Magnify Speed (in seconds):  ");
		lbl.setPreferredSize(new Dimension(LABEL_WIDTH, 16));

		JPanel magnifySliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		magnifySliderPanel.setBorder(BorderFactory.createEtchedBorder());
		magnifySliderPanel.add(lbl);
		magnifySliderPanel.add(new JLabel("Fast"));
		magnifySliderPanel.add(magnifySlider);
		magnifySliderPanel.add(new JLabel("Slow"));
		return magnifySliderPanel;
	}

	/**
	 * @see ShrimpOptions#ok()
	 */
	public void ok() {
		MagnifyZoomHandler.setAnimationTime(magnifySelectedSpeed);
		PNormalZoomHandler.setZoomSpeed(zoomSelectedSpeed);
		try {
			DisplayBean displayBean = (DisplayBean) project.getTool(toolName).getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.setStaticRenderingQuality(staticRendQual);
			displayBean.setDynamicRenderingQuality(dynamicRendQual);
		} catch (BeanNotFoundException e1) {
		} catch (ShrimpToolNotFoundException e1) {
		}
	}

	/**
	 * @see ShrimpOptions#cancel()
	 */
	public void cancel() {
	}

}
