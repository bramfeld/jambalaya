/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.FileOpenCommandAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.FilmStrip.ShrimpSnapShot;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;

/**
 * @author Yiling Lu
 */
public class ImportSVGCommandAdapter extends DefaultProjectAction {

	public static String ACTION_NAME = "Import HTML/SVG Snapshot";

	private String toolName;
	private String fileName;
	private File lastDir = null;

	public ImportSVGCommandAdapter(ShrimpProject project, String toolName) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_import.gif"), project);
		this.toolName = toolName;
		mustStartAndStop = false;
		lastDir = new File(System.getProperty("user.dir"));
	}

	public void actionPerformed(ActionEvent event) {
		openFile();
	}

	private void openFile() {
		//Create and display a FileChooser
		//Get the user selected file
		JFileChooser chooser = new JFileChooser();
		ShrimpFileFilter filter = new ShrimpFileFilter();
		filter.addExtension("svg");
		filter.setDescription("SVG File");
		chooser.setFileFilter(filter);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setDialogTitle("Import HTML/SVG Snapshot");
		chooser.setApproveButtonText("Load");
		chooser.setCurrentDirectory(lastDir);

		int state = chooser.showOpenDialog(ApplicationAccessor.getParentFrame());
		if (state != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// make sure it has the right extension
		File file = chooser.getSelectedFile();
		lastDir = file.getParentFile();
		fileName = file.getPath();

		// for now, requiring a project in active state
		// In the future, if the project is null, then
		// 1. pass the svg file, find out the project name
		// 2. load the project of that name, it can be done by asking the user the specify the location of the
		//    project file or to load the project file from a well-known place.(through http connection, or JDBC or...)
		if (!hasProject()) {
			Map info = parseSVGFile(fileName);
			info.get("prjName").toString();

			Object[] options = { "Yes", "No" };
			String msg = "This snapshot requires a project with name: " + info.get("prjName").toString()
					+ " or its equivalent open in the current working space" + "\n" + "Do you want to open this project now?";

			int n = JOptionPane.showOptionDialog(null, msg, "Choose a project file to open",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == JOptionPane.YES_OPTION) {
				ActionManager actionManager = ApplicationAccessor.getApplication().getActionManager();
				FileOpenCommandAdapter openAction = (FileOpenCommandAdapter)
						actionManager.getAction(ShrimpConstants.ACTION_NAME_OPEN, ShrimpConstants.MENU_FILE);
				openAction.startAction(); // this will update the project
			} else if (n == JOptionPane.NO_OPTION) {
				return;

			} else {
				return;
			}
		}

		ViewTool view = getViewTool(toolName);
		if (view != null) {
			ShrimpSnapShot snapShot = new ShrimpSnapShot(getProject(), file);
			snapShot.revertViewToSnapShotState(getProject(), view);
		}
	}

	public void startAction() {
		actionPerformed(new ActionEvent("", 0, ""));
	}

	private Map parseSVGFile(String filePath) {
		HashMap info = new HashMap();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(filePath);

			NodeList list = document.getElementsByTagName("text");
			for (int i = list.getLength() - 1; i >= 0; i--) {
				Element el = (Element) (list.item(i));
				if ((el.getAttribute("name").indexOf("cprel")) != -1) {
					info.put(el.getAttribute("name"), el.getChildNodes().item(0).getNodeValue());
					break;
				}
			}

			// get all snapShots from the DOM tree and add them to filmStrip
			NodeList gNode = document.getElementsByTagName("g");
			for (int i = 0; i < gNode.getLength(); i++) {
				Element el = (Element) gNode.item(i);
				if ((el.getAttributeNode("name") != null) && (el.getAttributeNode("name").getValue().equalsIgnoreCase("info"))) {
					NodeList infoNodes = el.getChildNodes();
					for (int j = 0; j < infoNodes.getLength(); j++) {
						Element infoEl = (Element) infoNodes.item(j);
						info.put(infoEl.getAttributeNode("name").getValue(), infoNodes.item(j).getFirstChild().getNodeValue());
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return info;
	}

}
