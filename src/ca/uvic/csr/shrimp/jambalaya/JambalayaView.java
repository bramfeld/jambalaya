/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.Component;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.util.Collection;
import java.util.Set;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.workspace.WorkspaceFrame;
import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.core.ui.view.ViewsPane;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;

/**
 * The tab plugin for Protege.
 * The tab holds the {@link JambalayaApplication} and the one and only {@link JambalayaProject}.
 * The main GUI components (cls tree, instance list, {@link ShrimpView}) are
 * held in the {@link JambalayaContentPane}.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class JambalayaView extends AbstractOWLClassViewComponent implements IJambalayaContainer {

	private static JambalayaView instance = null;
	private JambalayaApplication jambalayaApplication = null;
	private JambalayaProject jambalayaProject = null;
	private JambalayaContentPane content = null;
	private HierarchyListener visibilityListener = null;
	private OWLOntologyChangeListener frameListener = null;
	// this Jambalaya's menus
	private JMenu jambalayaMenu = null;
	private boolean shown = false;
	private boolean disposed = false;

	public void initialiseClassView() throws Exception {
		if (instance == null)
			instance = this;

		// create the application
		jambalayaApplication = new JambalayaApplication(getOWLModelManager(), getOWLEditorKit());

		// get the Protege frame
		Frame mainFrame = (Frame) SwingUtilities.windowForComponent(this);
		jambalayaApplication.setParentFrame(mainFrame);

		// create the GUI widgets
		content = new JambalayaContentPane(this, getOWLEditorKit());
		jambalayaMenu = new JMenu(new DefaultShrimpAction(JambalayaApplication.APPLICATION_TITLE,
																			ResourceHandler.getIcon("icon_jambalaya.gif")));
		content.initialize();
		getView().add(content.getComponent());

		// create the project
		ProgressDialog.createProgressDialog(mainFrame, jambalayaApplication.getName() + " Loading");
		jambalayaApplication.waitCursor();
		try {
			ProgressDialog.showProgress();
			ProgressDialog.setSubtitle("Creating Jambalaya view ...");
			ProgressDialog.setNote("");

			if (jambalayaProject == null)
				 jambalayaProject = jambalayaApplication.createJambalayaProject(getProject());
			content.createShrimpView(jambalayaProject);

			// Tell all listeners this Jambalaya is ready
			jambalayaApplication.fireApplicationStartedEvent();
			jambalayaApplication.fireProjectActivatedEvent(jambalayaProject);

			createMenus();

			ProgressDialog.tryHideProgress();
		} finally {
			jambalayaApplication.defaultCursor();
		}
	
		addJambalayaMenu();
		jambalayaApplication.fireApplicationActivatedEvent();
		shown = true;
		
		// Add a listener to detect when this Jambalaya is hidden or shown.
		visibilityListener = new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (getView().isShowing()) {
						addJambalayaMenu();
						jambalayaApplication.fireApplicationActivatedEvent();
						shown = true;
					} else {
						removeJambalayaMenu();
						jambalayaApplication.fireApplicationDeactivatedEvent();
						// save the properties here because we want to remember if the tool dialogs were open
						jambalayaApplication.saveProperties();
						shown = false;
					}
				}
			}
		};
		getView().addHierarchyListener(visibilityListener);
		
		// Add a listener to detect any changes in the active ontology
		frameListener = new OWLOntologyChangeListener() {
			public void ontologiesChanged(List changes) {
				if (jambalayaProject != null) {
					try {
						Object dataBean = jambalayaProject.getBean(ShrimpProject.DATA_BEAN);
						if (dataBean != null && dataBean instanceof ProtegeDataBean)
							((ProtegeDataBean) dataBean).setDefaultRootClses();
					} catch (Exception e) {
					}
				}
				// put an asterisk in the name of the jambalaya view to indicate that it needs to be refreshed
				setCaption("*");
			}
		};
		getOWLModelManager().addOntologyChangeListener(frameListener);
	}

	protected OWLClass updateView(OWLClass owlClass) {
		return owlClass;
	}

	public void disposeView() {
		disposed = true;

		getOWLModelManager().removeOntologyChangeListener(frameListener);
		getView().removeHierarchyListener(visibilityListener);
		
		if (shown) {
			removeJambalayaMenu();
			jambalayaApplication.fireApplicationDeactivatedEvent();
			// save the properties here because we want to remember if the tool dialogs were open
			jambalayaApplication.saveProperties();
			shown = false;
		}
		
		content.takeSnapShot();
		content.dispose();
		jambalayaApplication.close();
		ProgressDialog.disposeProgressDialog();

		content = null;
		jambalayaProject = null;
		jambalayaApplication = null;
		
		if (instance == this)
			instance = null;
	}
	
	public void createMenus() {
		if (jambalayaApplication != null) {
			jambalayaMenu.removeAll();

			// add the menus from the action manager
			JMenu menu = jambalayaApplication.getActionManager().createMenus("JambalayaMenus");
			Component[] menuComponents = menu.getMenuComponents();

			// Need a better way to remove menu items we do not want in a particular menu
			for (int i = 0; i < menuComponents.length; i++) {
				if (menuComponents[i] instanceof JMenu) {
					JMenu menuItem = (JMenu) menuComponents[i];
					if (!menuItem.getText().equalsIgnoreCase("Arc")) {
						jambalayaMenu.add(menuItem);
					}
				} else {
					jambalayaMenu.add(menuComponents[i]);
				}
			}
		}
	}

	private void addJambalayaMenu() {
		WorkspaceFrame frame = ProtegeManager.getInstance().getFrame(getOWLWorkspace());
		if (frame != null) {
			JMenuBar mainWindowMenuBar = frame.getJMenuBar();
			if (mainWindowMenuBar != null) {
				if (mainWindowMenuBar.isAncestorOf(jambalayaMenu))
					mainWindowMenuBar.remove(jambalayaMenu);
				int n = mainWindowMenuBar.getMenuCount();
				JMenu hlp = (n > 1 ? mainWindowMenuBar.getMenu(n - 1) : null);
				if (hlp != null)
					mainWindowMenuBar.remove(hlp);
				mainWindowMenuBar.add(jambalayaMenu);
				if (hlp != null)
					mainWindowMenuBar.add(hlp);
				mainWindowMenuBar.revalidate();
				mainWindowMenuBar.repaint();
			}
		}
	}

	private void removeJambalayaMenu() {
		WorkspaceFrame frame = ProtegeManager.getInstance().getFrame(getOWLWorkspace());
		if (frame != null) {
			JMenuBar mainWindowMenuBar = frame.getJMenuBar();
			if (mainWindowMenuBar != null) {
				if (mainWindowMenuBar.isAncestorOf(jambalayaMenu)) {
					mainWindowMenuBar.remove(jambalayaMenu);
					mainWindowMenuBar.revalidate();
					mainWindowMenuBar.repaint();
				}
			}
		}
	}

	public void setCaption(String s)
	{
		String cpt = jambalayaApplication.getName() + " " + s;
		setHeaderText(cpt);
		/*
		JTabbedPane tabbedPane  = (JTabbedPane) view.getParent();
		tabbedPane.setTitleAt(tabbedPane.indexOfComponent(view), cpt);
		*/
	}

	public void setRootClses(Set rootClses) {
		content.setRootClses(rootClses);
	}

	public OWLOntology getProject() {
		OWLModelManager mm = getOWLModelManager();
		if (mm != null)
			return mm.getActiveOntology();
		return null;
	}

	public JambalayaProject getJambalayaProject() {
		return jambalayaProject;
	}

	/**
	 * Used by the Jambalaya applets and by the ShowInQueryViewAction classes.
	 */
	public void setJambalayaProject(JambalayaProject jambalayaProject) {
		this.jambalayaProject = jambalayaProject;
	}

	public JambalayaApplication getJambalayaApplication() {
		return jambalayaApplication;
	}

	public JambalayaContentPane getContentPane() {
		return content;
	}

	public static JambalayaView globalInstance() {
		return instance;
	}
	 
}

