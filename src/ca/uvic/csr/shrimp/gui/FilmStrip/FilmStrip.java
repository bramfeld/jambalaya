/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.adapter.LoadFilmStripAdapter;
import ca.uvic.csr.shrimp.adapter.SaveFilmStripAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.SnapShotAdapter;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * This is the holder of snap shots taken.  These snapshots
 * are displayed as miniture pictures.  Each snapshot can
 * be invoked to layout the current shrimp view to match
 * the layout stored in the snapshot.  An annotation can
 * be added to each snapshot.
 *
 * @author Casey Best, Chris Callendar
 * @date Oct 3, 2001
 */
public class FilmStrip extends AbstractShrimpTool {

	public final static String FILMSTIP_DIRECTORY_KEY = "filmstrip directory";

	private JRootPane rootPane;
	private SnapShotsPanel snapShotsPanel;

	// name of the file the filmstrip is saved to
	private String fileName;

	private Frame parentFrame;
	private LoadFilmStripAdapter loadFilmStripAdapter;
	private SaveFilmStripAdapter saveFilmStripAdapter;
	private SnapShotAdapter snapShotAdapter;
	private EmailFilmStripAdapter emailAdapter;

	private JButton clearButton;

	public FilmStrip(ShrimpProject project) {
		super(ShrimpApplication.FILMSTRIP, project);
		this.parentFrame = ApplicationAccessor.getParentFrame();

		this.loadFilmStripAdapter = new LoadFilmStripAdapter(this);
		this.saveFilmStripAdapter = new SaveFilmStripAdapter(this, parentFrame);
		this.snapShotAdapter = new SnapShotAdapter(project, ShrimpProject.SHRIMP_VIEW);
		this.snapShotsPanel = new SnapShotsPanel(this, parentFrame);

		this.rootPane = new JRootPane();
		rootPane.getContentPane().setLayout(new BorderLayout());
		rootPane.getContentPane().add(createToolBar(), BorderLayout.NORTH);
		rootPane.getContentPane().add(snapShotsPanel, BorderLayout.CENTER);
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.addSeparator(new Dimension(15, 15));
		toolbar.add(createToolBarButton(loadFilmStripAdapter));
		toolbar.add(createToolBarButton(saveFilmStripAdapter));
		toolbar.addSeparator(new Dimension(15, 15));
		toolbar.add(createToolBarButton(snapShotAdapter));
		if (EmailFilmStripAdapter.canEmail()) {
			toolbar.addSeparator(new Dimension(15, 15));
			this.emailAdapter = new EmailFilmStripAdapter("Email", ResourceHandler.getIcon("icon_feedback.gif"), getProject(), this);
			toolbar.add(createToolBarButton(emailAdapter));
		}
		
		toolbar.addSeparator(new Dimension(15, 15));
		this.clearButton = new JButton(new DefaultShrimpAction(" Clear ") {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		clearButton.setToolTipText("Clear the filmstrip - removes all the snapshots");
		toolbar.add(clearButton);
		return toolbar;
	}

	private JButton createToolBarButton(ShrimpAction action) {
		JButton button = new JButton(action);
		button.setText(null);
		button.setToolTipText(action.getToolTip());
		//button.setMargin(new Insets(0,0,1,1));
		return button;
	}

	public SnapShotsPanel getSnapShotPanel() {
		return snapShotsPanel;
	}

	public Component getGUI() {
		return rootPane;
	}

	// cleans up the filmstrip by telling all the snapshots to cleanup their memory use
	public void disposeTool() {
		saveTemporaryFilmstrip(getProject());
		ShrimpSnapShot.saveEmailAddressToProperties(getProject(), ShrimpSnapShot.USER_EMAIL);
		clear();
	}

	public void refresh() {
		// what to do here?
		// clearing causes all the snapshots to be lost...
		//clear();
		ShrimpProject p = getProject();
		setProject(p);
		
		// need to update the adapter to use the current project
		snapShotAdapter.setProject(p);
		loadFilmStripAdapter.setEnabled(p != null);
		snapShotsChanged();	// updates the save button
	}

	public void clear() {
		snapShotsPanel.removeAllSnapShots();
	}

	/**
	 * @return false only if the user chose the "cancel" option
	 */
	public boolean saveIfNeeded() {
		boolean okay = true;
		if (snapShotsPanel.hasChanged() && (snapShotsPanel.getSnapShots().size() > 0)) {
            int selectedOption = JOptionPane.showConfirmDialog(null, "Would you like to save the changes to the filmstrip?", ApplicationAccessor.getApplication().getName(), JOptionPane.YES_NO_CANCEL_OPTION);
			// if yes was answered, show the save filmstrip prompt
			if (selectedOption == JOptionPane.YES_OPTION) {
				if (this.fileName == null) {
					SaveFilmStripAdapter saveFilmStrip = new SaveFilmStripAdapter(this, rootPane.getParent());
					saveFilmStrip.save();
				} else {
					this.save(this.fileName);
				}
			} else if (selectedOption == JOptionPane.CANCEL_OPTION) {
				okay = false;
			}
		}
		return okay;
	}

	public void setProject(ShrimpProject project) {
		if (!ShrimpUtils.equals(this.project, project)) {
			// save the current filmstrip
			// we don't need to save if the new project is null - already been saved in disposeTool()
			if (snapShotsPanel.hasChanged() && (project != null) && (this.project != null)) {
				saveTemporaryFilmstrip(this.project);
			}

			super.setProject(project);

			// load an existing filmstrip
			loadTemporaryFilmstrip();
		}
	}
	
	public void loadTemporaryFilmstrip() {
		loadTemporaryFilmstrip(getProject());
	}

	private void saveTemporaryFilmstrip(ShrimpProject project) {
		if (snapShotsPanel.hasChanged()) {
			File file = getTemporaryFilmstripFile(project);
			if (file != null) {
				if (file.exists()) {
					file.delete();
				}
				if ((snapShotsPanel.getSnapShotCount() > 0)) {
					save(file.getAbsolutePath());
					//System.out.println("Temporaray filmstrip saved for " + project.getTitle());
					//System.out.println("Saved to " + file);
				}
			}
		}
	}

	private void loadTemporaryFilmstrip(ShrimpProject project) {
		File file = getTemporaryFilmstripFile(project);
		if (file != null) {
			if (file.exists()) {
				load(file.getAbsolutePath());
				//System.out.println("Temporaray filmstrip loaded for " + project.getTitle());
			}
		}
	}

	private File getTemporaryFilmstripFile(ShrimpProject project) {
		File file = null;
		if (project != null) {
			URI uri = project.getProjectURI();
			if (uri != null) {
				try {
					file = ResourceHandler.toFile(uri);
					String path = file.getAbsolutePath();
					int dot = path.lastIndexOf(".");
					if (dot != -1) {
						path = path.substring(0, dot);
					}
					path += ".filmstrip";
					file = new File(path);
				} catch (Throwable t) {
					t.printStackTrace();
					file = null;
				}
			}
		}
		return file;
	}

	public void applyLayout(SnapShot snapShot) {
		if ((project != null) && (snapShot != null)) {
			Cursor cursor = getGUI().getCursor();
			getGUI().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				ShrimpView view = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				snapShot.revertViewToSnapShotState(project, view);
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			} finally {
				getGUI().setCursor (cursor);
			}
		}
	}

	/**
	 * Saves the status of the filmstrip to a file.
	 */
	public void save(String fileName) {
		try {
			// create a new DOM tree for the filmstrip
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			// FilmStrip will be the root of the DOM tree
			Element root = document.createElement("FilmStrip");

			// get DOM subtrees for each snapShot - traverse the vector in reverse directon so that when you load the filmstrip it is in the right order
			Vector snapShots = snapShotsPanel.getSnapShots();
			for (int i = snapShots.size() - 1; i >= 0; i--) {
				ShrimpSnapShot snapShot = (ShrimpSnapShot) snapShots.get(i);
				root.appendChild(document.importNode(snapShot.getSnapShotDOMSubTree(), true));
			}

			// attach the root to the tree
			document.appendChild(root);

			// Use a Transformer for output (it is not actually transforming anything but this is the suggest way)
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			// indenting causes problems when reading back in - it adds text nodes
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(fileName));
			transformer.transform(source, result);

			snapShotsPanel.setChanged(false);

			// tell each snapShot the changes have been saved
			for (int i = 0; i < snapShots.size(); i++) {
				ShrimpSnapShot snapShot = (ShrimpSnapShot) snapShots.get(i);
				snapShot.changesSaved();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadFilmStrip() {
		//Create and display a FileChooser
		//Get the user selected file
		JFileChooser chooser = new JFileChooser();
		ShrimpFileFilter filter = new ShrimpFileFilter();
		filter.addExtension("filmstrip");
		filter.setDescription("Shrimp Filmstrip");
		chooser.setFileFilter(filter);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setDialogTitle("Load Filmstrip");
		chooser.setApproveButtonText("Load");
		String filmStripDir = System.getProperty("user.dir");
		Properties props = ApplicationAccessor.getProperties();
		filmStripDir = props.getProperty(FILMSTIP_DIRECTORY_KEY);
		if (filmStripDir == null) {
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		} else {
			chooser.setCurrentDirectory(new File(filmStripDir));
		}

		int state = chooser.showOpenDialog(parentFrame);
		if (!(state == JFileChooser.CANCEL_OPTION)) {
			File file = chooser.getSelectedFile();

			if (!file.exists()) {
				JOptionPane.showMessageDialog(parentFrame, "File with the name " + file.getPath() + "does not exist.");
			} else if (state == JFileChooser.APPROVE_OPTION) {
				// make sure it has the right extension
				String fileName = file.getPath();
				if (!fileName.endsWith(".filmstrip")) {
					fileName += ".filmstrip";
				}
				props.setProperty(FILMSTIP_DIRECTORY_KEY, file.getParent());
				load(fileName);
			}
		}
	}

	/**
	 * Loads the info from the file into the film strip.
	 */
	private void load(String fileName) {
		this.fileName = fileName;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(fileName));

			snapShotsPanel.removeAllSnapShots();
			// get all snapShots from the DOM tree and add them to filmStrip
			NodeList svgNodes = document.getElementsByTagName("svg");
			Vector shots = new Vector();
			for (int i = 0; i < svgNodes.getLength(); i++) {
				Node svgNode = svgNodes.item(i);
				NamedNodeMap attributes = svgNode.getAttributes();
				Node nameNode = attributes.getNamedItem("name");
				if (nameNode != null && nameNode.getNodeValue().equals("snapshot")) {
					SnapShot snapShot = new ShrimpSnapShot(svgNode, rootPane);
					shots.add(snapShot);
				}
			}
			snapShotsPanel.addSnapShots(shots);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		snapShotsPanel.setChanged(false); // this filmstrip was just loaded
	}

	public void addSnapShot(ShrimpSnapShot snapShot) {
		getSnapShotPanel().addSnapShot(snapShot);
	}

	public void snapShotsChanged() {
		boolean enabled = (getProject() != null) && (getSnapShotPanel().getSnapShotCount() > 0);
		saveFilmStripAdapter.setEnabled(enabled);
		if (emailAdapter != null) {
			emailAdapter.setEnabled(enabled);
		}
		clearButton.setEnabled(enabled);
	}

}