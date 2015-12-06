/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui.ActionManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import ca.uvic.csr.shrimp.usercontrols.ShrimpActionListener;
import ca.uvic.csr.shrimp.usercontrols.UserAction;


/**
 * This purpose of this class is to provide consistent
 * menus throughout SHriMP and to seperate actions from their GUI location/representation.
 * Actions are added to this Action Manager with a path of the form "menu1/submenu1/submenu2" along
 * with a "group" to add the action to and a preferred position within the group.
 * This action manager converts actions and their paths into appropriate swing menus using the
 * createMenus(...) method as follows:
 *  - menu1 will be a JMenu.
 *  - submenu1 will be a JMenu and a submenu of menu1
 *  - submenu2 will be a JMenu and a submenu of submenu1
 * Within menus, groups will be divided by JSeperators.
 * If no group is specified, the action is simply placed in the "DEFAULT_GROUP"
 * It is possible to create more submenus by adding more names to the path.
 *
 * Ex. addAction (anAction, "Layout/Grid/", "By Alphabetical", "", 1)
 * A menuItem with an action of "anAction" will be placed as the first menu item in
 * the "Grid" submenu, off of the "Layout" menu.
 * If an action is added to a non-existent menu or group then the menu or
 * group will be created.
 *
 * An action is uniquely identified by its name and its path.
 *
 * If you wish to add or remove many actions to or form  the action manager without many events being thrown
 * do the following:
 * <pre>
 * 		boolean firingEvents = actionManager.getFiringEvents();
 * 		actionManager.setFiringEvents(false);
 * 		...
 * 		"add or remove many actions here"
 * 		...
 * 		actionManager.setFiringEvents (firingEvents);
 *
 * </pre>
 * When setFiringEvents is eventually set to true, single ActionsAdded or ActionsRemoved
 * events will be thrown.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class ActionManager {

	//private static final String PATH_KEY = "path";
	//private static final String GROUP_KEY = "group";
	//private static final String POSITION_KEY = "position";

	private static final String DEFAULT_GROUP = "default_group";

	private ActionNode rootNode;
	private Map actionToPopupListener; //key is an action, value is a popup listener

	private Vector actionManagerListeners;
	private Map/*<String, List>*/ actionListeners;	// maps action name to a list of listeners
	private ShrimpActionListener relayListener = new ShrimpActionListener() {
		public void actionStarting(ShrimpAction action) {
			fireActionStartingOrFinished(action, true);
		}
		public void actionFinished(ShrimpAction action) {
			fireActionStartingOrFinished(action, false);
		}
	};
	private boolean firingEvents = true;
	private Vector delayedAddedActions = new Vector();
	private Vector delayedRemovedActions = new Vector();
	private Vector delayedModifiedEvents = new Vector();

	private Hashtable actionSets = new Hashtable();
	private CheckBoxPopupMenuAdapter checkBoxPopupMenuAdapter = new CheckBoxPopupMenuAdapter();

	public static final int IGNORE_PARENT = 0;
	public static final int DISABLE_PARENT = 1;
	public static final int REMOVE_PARENT = 2;

	/** Creates a new Action Manager */
	public ActionManager () {
		rootNode = new ActionNode();
		actionToPopupListener = new HashMap();
		actionManagerListeners = new Vector();
		actionListeners = new HashMap/*<String, List>*/();
	}

	protected void fireActionStartingOrFinished(ShrimpAction action, boolean starting) {
		if (actionListeners.containsKey(action.getActionName())) {
			List listeners = (List) actionListeners.get(action.getActionName());
			for (Iterator iter = listeners.iterator(); iter.hasNext();) {
				ShrimpActionListener sal = (ShrimpActionListener) iter.next();
				if (starting) {
					sal.actionStarting(action);
				} else {
					sal.actionFinished(action);
				}
			}
		}
	}

	/**
	 * Adds a listener for any actions that have the given actionName.
	 * @param actionName the name of the action to listener for
	 * @param listener the listener which will get notified if the action with actionName starts/finishes
	 */
	public void addActionListener(String actionName, ShrimpActionListener listener) {
		if ((actionName != null) && (listener != null)) {
			if (!actionListeners.containsKey(actionName)) {
				actionListeners.put(actionName, new ArrayList());
			}
			List listeners = (List) actionListeners.get(actionName);
			listeners.add(listener);
		}
	}

	public void removeActionListener(ShrimpActionListener listener) {
		for (Iterator iter = actionListeners.keySet().iterator(); iter.hasNext();) {
			String actionName = (String) iter.next();
			List listeners = (List) actionListeners.get(actionName);
			if (listeners.remove(listener)) {
				break;
			}
		}
	}

	/**
	 * Merges the passed in action manager with this action manager.
	 */
	public void mergeActionManager(ActionManager actionManagerToMerge) {
		// add all actions from the passed-in action manager to this action manager
		// also include any popup listeners
		Map popupListenersToMerge = actionManagerToMerge.actionToPopupListener;
		Enumeration e = actionManagerToMerge.rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			ActionNode node = (ActionNode) e.nextElement();
			Action action = node.getAction();
			String path = node.getPathString();
			String group = node.getGroup();
			int position = node.getPosition();
			addAction(action, path, group, position);
			if (popupListenersToMerge.get(action) != null) {
				addPopupListener((String)action.getValue(Action.NAME), path, (PopupMenuListener) popupListenersToMerge.get(action));
			}
		}
	}

	/** Debugging purposes. */
	public void printAllActions() {
		System.out.println("'name', 'path', 'group', 'position'");
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			ActionNode node = (ActionNode)e.nextElement();
			System.out.println(node.toString());
		}
	}

	/** Debugging purposes. */
	public void printAllActions(String path) {
		System.out.println("'name', 'path', 'group', 'position'");
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			ActionNode node = (ActionNode) e.nextElement();
			if (path.equals(node.path)) {
				System.out.println(node.toString());
			}
		}
	}

	/**
	 * Sets whether or not this action manager should fire events
	 */
	public void setFiringEvents(boolean firingEvents) {
		this.firingEvents = firingEvents;
		if (firingEvents) {
			//fire any delayed events
			if (!delayedAddedActions.isEmpty()) {
				ActionsAddedEvent aae = new ActionsAddedEvent (delayedAddedActions);
				fireActionsAddedEvent(aae);
				delayedAddedActions = new Vector();
			}

			if (!delayedRemovedActions.isEmpty()) {
				ActionsRemovedEvent are = new ActionsRemovedEvent (delayedRemovedActions);
				fireActionsRemovedEvent(are);
				delayedRemovedActions = new Vector();
			}

			if (!delayedModifiedEvents.isEmpty()) {
				ActionsModifiedEvent ame = new ActionsModifiedEvent (delayedModifiedEvents);
				fireActionsModifiedEvent(ame);
				delayedModifiedEvents = new Vector();
			}
		}
	}

	/**
	 * Returns whether or not this action manager is firing action manager events
	 */
	public boolean getFiringEvents() {
		return firingEvents;
	}

	/**
	 * Links a popup menu listener to a particular action. Any menus that
	 * have this action will be given this listener
	 */
	public void addPopupListener(String actionName, String path, PopupMenuListener pml) {
		Action action = getAction(actionName, path);
		if (action != null) {
			actionToPopupListener.put(action, pml);
		}
	}

	/**
	 * Removes the link between an action and a popup menu listener
	 */
	public void removePopupListener(String actionName, String path, PopupMenuListener pml) {
		Action action = getAction (actionName, path);
		if (action != null) {
			actionToPopupListener.remove(action);
		}
	}

	// DEBUG
	public Map getActionToPopupListenerMap() {
		return actionToPopupListener;
	}


	/**
	 *  Enable/disable the action with the given name, at the specified path.
	 */
	public void setActionEnabled(String actionName, String path, boolean enabled) {
		ActionNode node = getActionNode(actionName, path);
		setActionEnabled(node, enabled);
	}

	private void setActionEnabled(ActionNode node, boolean enabled) {
		if(node != null) {
			Action action = node.getAction();
			action.setEnabled(enabled);

			Enumeration children = node.children();
			while (children.hasMoreElements()) {
				ActionNode child = (ActionNode) children.nextElement();

				setActionEnabled(child, enabled);
			}
		}
	}

	/**
	 * Add an action to this action manager.
	 * Menus and menuItems would be alphabetically ordered, first by their groups and then names.
	 * Note: If an action already exists with
	 * the same name and path, it will be overwritten and a ActionModifiedEvent will be thrown.
	 * @param action 	The action to add. This action must have a name.
	 * @param path 	Where to put this action.
	 * @param group 	The group to put this action in. Groups within the same menu will be
	 * 					seperated by JSeperators. If the group is the empty string, the action will
	 * 					be placed in the "DEFAULT_GROUP" group.
	 * @param position	Preferred position within a group. "1" is first, "2" is second, etc.
	 */
	public void addAction(Action action, String path, String group, int position) {
		String name = (String)action.getValue(Action.NAME);
		if (name == null || name.equals("")) {
			(new Exception ("an action must be given a name")).printStackTrace();
			return;
		}

		if (action instanceof UserAction) {
			UserAction userAction = (UserAction) action;
			userAction.addActionListener(relayListener);
		}

		//check if an action with this name and path already exists
		ActionNode node = getActionNode(name, path);
		if (node != null) {
			Action oldAction = node.getAction();
			node.setAction(action);
			node.setPosition(position);
			// fire a modified event
			ActionModifiedEvent ame = new ActionModifiedEvent(oldAction, action);
			if (firingEvents) {
				Vector modifiedEvents = new Vector();
				modifiedEvents.add(ame);
				ActionsModifiedEvent asme = new ActionsModifiedEvent(modifiedEvents);
				fireActionsModifiedEvent(asme);
			} else {
				delayedModifiedEvents.add(ame);
			}
		} else {
			if (group.equals("")) {
				group = DEFAULT_GROUP;
			}
			node = new ActionNode(action, path, group, position);
			addActionRecursive(node, path, rootNode);

			// fire an event
			if (firingEvents) {
				Vector addedActions = new Vector();
				addedActions.add(action);
				ActionsAddedEvent aae = new ActionsAddedEvent(addedActions);
				fireActionsAddedEvent(aae);
			} else {
				delayedAddedActions.add(action);
			}
		}
	}

	private void addActionRecursive(ActionNode toAddNode, String pathToGo, ActionNode currentParentNode) {
		//base case: path is empty
		StringTokenizer pathTokenizer = new StringTokenizer (pathToGo, "/");
		if (pathTokenizer.countTokens() == 0) {
			// there's no more path to look for, so add this action as a child of currentNode
			// Add at the end of its group (if there is one)

			// make a copy of this nodes children
			// remove all children and add new node and children back in the right order
			Vector childrenCopy = new Vector ();
			Enumeration children = currentParentNode.children();
			while (children.hasMoreElements()) {
				childrenCopy.add(children.nextElement());
			}

			currentParentNode.removeAllChildren();
			childrenCopy.add(toAddNode);
			Collections.sort(childrenCopy);
			for (Iterator iter = childrenCopy.iterator(); iter.hasNext();) {
				ActionNode childNode = (ActionNode) iter.next();
				currentParentNode.add(childNode);
			}

		} else if (pathTokenizer.countTokens() > 0) {
			// find the child of the current node that corresponds to the next element in the path.
			// if there isn't such a child, then create one
			final String pathToken = pathTokenizer.nextToken();
			String remainingPath = "";
			while (pathTokenizer.hasMoreTokens()) {
				remainingPath += pathTokenizer.nextToken() + "/";
			}
			Enumeration children = currentParentNode.children();
			ActionNode nodeOnPath = null;
			while (nodeOnPath == null && children.hasMoreElements()) {
				ActionNode childNode = (ActionNode) children.nextElement();
				Action childAction = childNode.getAction();
				String childActionName = (String) childAction.getValue(Action.NAME);
				if (childActionName.equals(pathToken)) {
					nodeOnPath = childNode;
				}
			}
			if (nodeOnPath == null) {
				Action emptyAction = new DefaultShrimpAction() {
					public String toString() {
						return pathToken;
					}
				};
				emptyAction.putValue(Action.NAME, pathToken);
				String path = getPathString(currentParentNode) + "/" + pathToken;
				nodeOnPath = new ActionNode (emptyAction,  path, DEFAULT_GROUP, 99);
				currentParentNode.add(nodeOnPath);
			}
			addActionRecursive(toAddNode, remainingPath, nodeOnPath);
		}
	}

	/**
	 * Removes an action from this action manager. If the action cannot be found nothing
	 * will happen.
	 * @param action The action to remove.
	 * @param handleParent	ActionManager.IGNORE_PARENT - does nothing with parent
							ActionManager.DISABLE_PARENT - disables parent if has no more children
							ActionManager.REMOVE_PARENT - removes parent if has no more children
	 */
	public void removeAction(Action action, int handleParent) {
		String actionName = (String) action.getValue(Action.NAME);
		Vector nodes = getActionNodes (actionName);
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			ActionNode actionNode = (ActionNode) iter.next();
			removeActionNode(actionNode, handleParent);
		}
	}

	/**
	 * Removes an action from this action manager. If the action cannot be found nothing
	 * will happen.
	 * @param actionName The name of the action to be removed.
	 * @param path The path of the action to be removed.
	 * @param handleParent	ActionManager.IGNORE_PARENT - does nothing with parent
							ActionManager.DISABLE_PARENT - disables parent if has no more children
							ActionManager.REMOVE_PARENT - removes parent if has no more children
	 */
	public void removeAction(String actionName, String path, int handleParent) {
		ActionNode node = getActionNode(actionName, path);
		if (node != null) {
			removeActionNode(node, handleParent);
		}
	}

	/**
	 * Removes an action from this action manager. If the action cannot be found nothing
	 * will happen.
	 * Added second signature to handle path issues. NE
	 * @param actionName The name of the action to be removed.
	 * @param handleParent	ActionManager.IGNORE_PARENT - does nothing with parent
							ActionManager.DISABLE_PARENT - disables parent if has no more children
							ActionManager.REMOVE_PARENT - removes parent if has no more children
	 */
	public boolean removeAction(String actionName, int handleParent) {
		ActionNode node = null;
		node = getActionNode(actionName);
		if (node != null) {
			removeActionNode(node, handleParent);
			return true;
		}
		return false;
	}

	/**
	 * Removes all the actions for the given path.
	 * @param path	the path of the actions to remove
	 * @param handleParent	ActionManager.IGNORE_PARENT - does nothing with parent
							ActionManager.DISABLE_PARENT - disables parent if has no more children
							ActionManager.REMOVE_PARENT - removes parent if has no more children
	 */
	public void removeActions(String path, int handleParent) {
		if (path == null) {
			return;
		}

		Enumeration e = rootNode.breadthFirstEnumeration();
		LinkedList toRemove = new LinkedList();
		while (e.hasMoreElements()) {
			ActionNode node = (ActionNode) e.nextElement();
			if (node != rootNode) {
				String path1 = node.getPathString();
				if (path.equals(path1)) {
					toRemove.add(node);
				}
			}
		}
		for (Iterator iter = toRemove.listIterator(0); iter.hasNext(); ) {
			removeActionNode((ActionNode)iter.next(), handleParent);
		}

		// now remove the parent node (same name as the path)
		ActionNode node = getActionNode(path, "");
		if (node != null) {
			removeActionNode(node, handleParent);
		}
	}

	/**
	 * Removes a node from the tree and fires an action removed event.
	 * Note: If the parent of the removed node has no more children then the parent
	 * is either: ignored, removed, or disabled depeding on handleParent.
	 * If the parent is removed, the parent's parent (and so on) will be removed
	 * as well if it has no more children.
	 */
	private void removeActionNode(ActionNode node, int handleParent) {
		ActionNode parentNode = (ActionNode) node.getParent();
		node.removeFromParent();
		Action action = node.getAction();

		// System.out.println("Removing action: " + node.toString());

		if (action instanceof UserAction) {
			UserAction userAction = (UserAction) action;
			userAction.removeActionListener(relayListener);
		}

		// remove link between this action and popup listener
		if (actionToPopupListener.containsKey(action)) {
			actionToPopupListener.remove(action);
		}

		// fire an event
		if (firingEvents) {
			Vector removedActions = new Vector();
			removedActions.add(action);
			ActionsRemovedEvent are = new ActionsRemovedEvent(removedActions);
			fireActionsRemovedEvent(are);
		} else {
			delayedRemovedActions.add(action);
		}
		//figure out what to do with the parent
		if (parentNode != null && parentNode.getChildCount() == 0) {
			switch (handleParent) {
				case IGNORE_PARENT :
					// do nothing
					break;
				case DISABLE_PARENT :
					(parentNode.getAction()).setEnabled(false);
					break;
				case REMOVE_PARENT :
					removeActionNode(parentNode, handleParent);
					break;
				default :
					// do nothing
					break;
			}
		}
	}

	/**
	 * Checks if the given action exists.
	 * @param action
	 * @return boolean
	 */
	public boolean hasAction(Action action) {
		if (action != null) {
			String actionName = (String) action.getValue(Action.NAME);
			Vector nodes = getActionNodes(actionName);
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {
				ActionNode actionNode = (ActionNode) iter.next();
				Action act = actionNode.getAction();
				if (action.equals(act)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the action with name "actionName". Returns null if the action is not found.
	 * Ex. getAction ("do it!", "menu1/menu2") will look for the "do it!" action in the
	 * submenu "menu2" off of "menu1"
	 * @param actionName The action to get.
	 * @param path Where to find this action
	 */
	public Action getAction(String actionName, String path) {
		Action action = null;
		ActionNode node = getActionNode(actionName, path);
		if (node != null) {
			action = node.getAction();
		}
		return action;
	}

	/**
	 * Gets all the actions with the given path.
	 * @param path	the path of the actions to find
	 * @return Collection of Action objects which have the given path
	 */
	public Collection getActions(String path) {
		if (path == null) {
			return Collections.EMPTY_LIST;
		}
		Enumeration e = rootNode.breadthFirstEnumeration();
		LinkedList actions = new LinkedList();
		while (e.hasMoreElements()) {
			ActionNode node = (ActionNode) e.nextElement();
			if (node != rootNode) {
				String path1 = node.getPathString();
				if (path.equals(path1)) {
					actions.add(node.getAction());
				}
			}
		}
		return actions;
	}

	/**
	 * Get the action with name "actionName". Returns null if the action is not found.
	 * Ex. getAction ("do it!") will look for the "do it!" action anywhere.
	 * NE - not sure what will happen if finds multiple ones.
	 * @param actionName The action to get.
	 */
	public Action getAction(String actionName) {
		Action action = null;
		ActionNode node = getActionNode(actionName);
		if (node != null) {
			action = node.getAction();
		}
		return action;
	}

	/**
	 * Returns the node in the tree that contains the action with
	 * the given name.  Getting rid of path assumption for uniqueness
	 * NE added
	 */
	private ActionNode getActionNode(String actionName) {
		ActionNode node = null;
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (node == null && e.hasMoreElements()) {
			ActionNode tmpNode = (ActionNode) e.nextElement();
			if (tmpNode != rootNode) {
				Action tmpAction = tmpNode.getAction();
				String tmpActionName = (String) tmpAction.getValue(Action.NAME);
				//String tmpPath = getPathString(tmpNode);
				if (tmpActionName.equals(actionName)) {
					node = tmpNode;
				}
			}
		}
		return node;
	}
	/**
	 * Returns the nodes in the tree that contains the action with
	 * the given name.
	 */
	private Vector getActionNodes(String actionName) {
		Vector nodes = new Vector();
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			ActionNode tmpNode = (ActionNode) e.nextElement();
			if (tmpNode != rootNode) {
				Action tmpAction =  tmpNode.getAction();
				String tmpActionName = (String) tmpAction.getValue(Action.NAME);
				if (tmpActionName.equals(actionName)) {
					nodes.add(tmpNode);
				}
			}
		}
		return nodes;
	}

	/**
	 * Returns the node in the tree that contains the action with
	 * the given name and path.
	 */
	private ActionNode getActionNode(String actionName, String path) {
		ActionNode node = null;
		Enumeration e = rootNode.breadthFirstEnumeration();
		while (node == null && e.hasMoreElements()) {
			ActionNode tmpNode = (ActionNode) e.nextElement();
			if (tmpNode != rootNode) {
				Action tmpAction = tmpNode.getAction();
				String tmpActionName = (String) tmpAction.getValue(Action.NAME);
				String tmpPath = getPathString(tmpNode);
				if (tmpActionName.equals(actionName) && tmpPath.equals(path)) {
					node = tmpNode;
				}
			}
		}
		return node;
	}

	/**
	 * Returns a string that represents the path of the given node from the root
	 */
	private String getPathString(ActionNode node) {
		String nodePathString = "";
		TreeNode [] nodePath = node.getPath();
		for (int i = 0; i < nodePath.length - 1; i++) {
			ActionNode pathNode = (ActionNode) nodePath[i];
			if (pathNode != rootNode) {
				Action pathAction = pathNode.getAction();
				String pathActionName = (String) pathAction.getValue(Action.NAME);
				nodePathString += pathActionName;
				if (i < nodePath.length - 2) {
					nodePathString += "/";
				}
			}
		}
		return nodePathString;
	}

	/**
	 *  Clear all actions from this action manager.
	 */
	public void clear() {
		rootNode.removeAllChildren();
		actionToPopupListener.clear();
		actionManagerListeners.clear();
		actionListeners.clear();
	}

	/**
	 * Returns a JMenu that corresponds to the actions
	 * that this action manager contains and assocites them with the menuSetName.
	 *
	 * @param menuSetName this name identifies a particular set of menus.
	 * @return JMenu the created menu.
	 */
	public JMenu createMenus(String menuSetName) {
		cleanupExistingMenu(menuSetName);

		// create new menus
		Vector menus = new Vector();
		JMenu menu = (JMenu) createMenusRecursive(rootNode);
		menus.add(menu);

		//store menu so that it can be properly cleaned up later
		actionSets.put(menuSetName, menus);

		return menu;
	}

	public void cleanupExistingMenu(String menuSetName){
		Vector menus = (Vector) actionSets.get(menuSetName);

		// if previous menus exist, do necessary cleanup before creating a new batch of menus
		if (menus != null) {
			for (Iterator iter = menus.iterator(); iter.hasNext();) {
				JMenuItem element = (JMenuItem) iter.next();
				if(element instanceof JMenu) {
					((JMenu) element).getPopupMenu().removePopupMenuListener(checkBoxPopupMenuAdapter);
				}
				element.setAction(null);
				element.removeAll();
			}
		}
	}

	/**
	 * Creates a menu item with the specified action.
	 * If the action is an instance of CheckBoxAction then a
	 * JCheckBoxMenuItem will be returned.
	 */
	private JMenuItem createMenuItem(final Action action) {
		JMenuItem menuItem = null;
		if (action instanceof CheckBoxAction) {
			CheckBoxAction chkAction = (CheckBoxAction) action;
			menuItem = new JCheckBoxMenuItem();
			((JCheckBoxMenuItem)menuItem).setSelected(chkAction.isChecked());
			ButtonGroup buttonGroup = chkAction.getButtonGroup();
			if (buttonGroup != null) {
				buttonGroup.add(menuItem);
			}
		} else {
			menuItem = new JMenuItem();
		}
		menuItem.setText((String)action.getValue(Action.NAME));
		menuItem.setAction(action);
		return menuItem;
	}

	public void repopulatePopUpMenu(String actionName, String path, JPopupMenu popup, String menuSetName) {
		popup.removeAll();
		ActionNode menuNode = getActionNode(actionName, path);
		cleanupExistingMenu(menuSetName);
		if (menuNode != null) {
			Vector forCleanUp = new Vector();
			JMenuItem menuItem = createMenusRecursive(menuNode);
			forCleanUp.add(menuItem);
			if (menuItem instanceof JMenu) {
				Component [] components = ((JMenu)menuItem).getMenuComponents();
				for (int i = 0; i < components.length; i++) {
					Component component = components[i];
					popup.add(component);
				}
			}

			//store menu so that it can be properly cleaned up later
			actionSets.put(menuSetName, forCleanUp);
		}
	}

	/**
	 * @param currentNode	The node we are currently examining.
	 */
	private JMenuItem createMenusRecursive(ActionNode currentNode) {
		Action action;
		String currentNodeActionName;

		// Assign dummy stuff to the rootnode, it won't be displayed
		if (currentNode == rootNode) {
			action = new DefaultShrimpAction();
			currentNodeActionName = "";
		} else {
			action = currentNode.getAction();
			currentNodeActionName = (String) action.getValue(Action.NAME);
		}

		// if this node is a leaf then create a JMenuItem (but only if its not a child of the root)
		// if this node has children create a JMenu, and add to it
		if (currentNode != rootNode && currentNode.getChildCount() == 0) {
			JMenuItem menuItem = createMenuItem(action);
			return menuItem;
		}

		JMenu menu = new JMenu(currentNodeActionName);
		menu.setAction(action);

		if (actionToPopupListener.get(action) != null) {
			menu.getPopupMenu().addPopupMenuListener((PopupMenuListener) actionToPopupListener.get(action));
		}

		menu.getPopupMenu().addPopupMenuListener(checkBoxPopupMenuAdapter);
		String currentGroup = null;
		for (Enumeration e = currentNode.children(); e.hasMoreElements(); ) {
			ActionNode childNode = (ActionNode) e.nextElement();
			String group = childNode.getGroup();
			// if this node is the first in a group (but not the first sibling)
			// put a JSeperator before it
			if (currentGroup != null && !group.equals(currentGroup)) {
				menu.addSeparator();
			}
			currentGroup = group;
			if ((childNode != null) && (childNode.getAction() != null)) {
				JMenuItem menuItem = createMenusRecursive(childNode);
				menu.add(menuItem);
			}
		}
		return menu;
	}

	public void addActionManagerListener(ActionManagerListener mml) {
		if (!actionManagerListeners.contains(mml)) {
			actionManagerListeners.add(mml);
		}
	}

	public void removeActionManagerListener(ActionManagerListener mml) {
		if (actionManagerListeners.contains(mml)) {
			actionManagerListeners.remove(mml);
		}
	}

	protected void fireActionsAddedEvent(ActionsAddedEvent aae) {
		for (Iterator iterator = actionManagerListeners.iterator(); iterator.hasNext();) {
			ActionManagerListener mml = (ActionManagerListener) iterator.next();
			mml.actionsAdded(aae);
		}
	}

	protected void fireActionsRemovedEvent(ActionsRemovedEvent are) {
		for (Iterator iterator = actionManagerListeners.iterator(); iterator.hasNext();) {
			ActionManagerListener mml = (ActionManagerListener) iterator.next();
			mml.actionsRemoved(are);
		}
	}

	protected void fireActionsModifiedEvent(ActionsModifiedEvent ame) {
		for (Iterator iterator = actionManagerListeners.iterator(); iterator.hasNext();) {
			ActionManagerListener mml = (ActionManagerListener) iterator.next();
			mml.actionsModified(ame);
		}
	}

	/**
	 * A popup menu listener that ensures that any checkbox menu items
	 * are checked or unchecked according to the values of their
	 * checkbox actions.
	 */
	private class CheckBoxPopupMenuAdapter implements PopupMenuListener {
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
 		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
		}
		public void popupMenuWillBecomeVisible (PopupMenuEvent e) {
			JPopupMenu popup = (JPopupMenu) e.getSource();
			MenuElement [] elements = popup.getSubElements();
			for (int i = 0; i < elements.length; i++) {
				MenuElement menuElement = elements[i];
				if (menuElement instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) menuElement;
					CheckBoxAction action = (CheckBoxAction) menuItem.getAction();
					menuItem.setSelected(action.isChecked());
				}
			}
		}
	}


	/**
	 * A node to be stored in our tree of actions.
	 */
	private class ActionNode extends DefaultMutableTreeNode implements Comparable {

		private Action action;
		private String path;
		private String group;
		private int position;

		private ActionNode() {
			super();
		}

		private ActionNode(Action action, String path, String group, int position) {
			super();
			this.action = action;
			this.path = path;
			this.group = group;
			this.position = position;
		}

		public String toString() {
			String name = (action != null ? (String)action.getValue(Action.NAME) : "null");
			return name + ", " + path + ", " + group + ", " + position;
		}

		private Action getAction() {
			return action;
		}

		private void setAction(Action action) {
			this.action = action;
		}

		private String getPathString() {
			return path;
		}

		private String getGroup() {
			return group;
		}

		private int getPosition() {
			return position;
		}

		private void setPosition(int position) {
			this.position = position;
		}

		/**
		 * Compares the actions of two nodes in the "action tree." Actions are sorted
		 * first by group name, then by their preferred position.
		 */
		public int compareTo(Object o) {
			ActionNode node1 = this;
			ActionNode node2 = (ActionNode) o;
			String group1 = node1.getGroup();
			int position1 = node1.getPosition();
			String group2 = node2.getGroup();
			int position2 = node1.getPosition();

			// order by group name first then by position
			if (group1.compareTo(group2) < 0) {
				return -1;
			} else if (group1.compareTo(group2) > 0) {
				return 1;
			} else {
				return position1 - position2;
			}
		}



		/**
		 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
		 */
		public Object getUserObject() {
			(new Exception ("dont use this!")).printStackTrace();
			return null;
		}

		/**
		 * @see javax.swing.tree.DefaultMutableTreeNode#setUserObject(java.lang.Object)
		 */
		public void setUserObject(Object arg0) {
			(new Exception ("dont use this!")).printStackTrace();
		}


		/**
		 * @see javax.swing.tree.DefaultMutableTreeNode#getUserObject()
		 */
		public Object setUserObject() {
			(new Exception ("dont use this!")).printStackTrace();
			return null;
		}

	}

	/**
	 * @return str - string representation of all the actions.
	 */
	public String toString() {
		String str = "Elements in menu: (depthfirst)";
		ActionNode node = null;
		Enumeration e = rootNode.depthFirstEnumeration();
		while (e.hasMoreElements()) {
			node = (ActionNode) e.nextElement();
			str += "\n\t" + node.toString();
		}
		return str;
	}

}

