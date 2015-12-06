/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.gui.TreeMapDialog;
import edu.umd.cs.treemap.BinaryTreeLayout;
import edu.umd.cs.treemap.MapItem;
import edu.umd.cs.treemap.MapLayout;
import edu.umd.cs.treemap.MapModel;
import edu.umd.cs.treemap.Mappable;
import edu.umd.cs.treemap.OrderedTreemap;
import edu.umd.cs.treemap.Rect;
import edu.umd.cs.treemap.SimpleMapModel;
import edu.umd.cs.treemap.SquarifiedLayout;
import edu.umd.cs.treemap.StripTreemap;

/**
 * This class manages the application of one of several treemap algorithms to a set of nodes.
 *
 * @author Jeff Michaud, Rob Lintern
 */
public class TreeMapLayout implements Layout {

	public final static String LAYOUT_STRIP = "Strip";
	public final static String LAYOUT_SQUARIFIED = "Squarified";
	public final static String LAYOUT_BINARY = "Binary";
	public final static String LAYOUT_ORDERED = "Ordered";
	//public final static String NUM_RELATIONSHIPS = "Number of Relationships";
    public static final String[] algorithmFields = {LAYOUT_STRIP, LAYOUT_SQUARIFIED, LAYOUT_BINARY, LAYOUT_ORDERED};
    //public static int NUM_EXTRA_FIELDS = 3;
    //private static final double MIN_NODE_SIZE = 5.0;
    public static final int DEFAULT_BORDER_SIZE = 3;


	//This is the minimum ratio bewteen width and height that we will set the node to be
	//even if it means the layout doesn't look pretty
	//public final static int maximumRatio = 3;

	public String[] orderFields;
	public String[] sizeFields;

	private MapLayout mapLayout = new OrderedTreemap();
	private String sizeField;
	private String orderField;
	private int border;
	private Comparator comparator;
	private String mapLayoutName;

	private MapModel map;
	private DisplayBean displayBean;

    public TreeMapLayout() {
        this(null);
    }

	public TreeMapLayout(DisplayBean displayBean) {
		this.displayBean = displayBean;

		//Defaults:
		setMapLayoutName(TreeMapLayout.LAYOUT_ORDERED);

        if (displayBean != null) {
            setBorderField(displayBean != null ? displayBean.getFontHeightOnCanvas(displayBean.getLabelFont()) : DEFAULT_BORDER_SIZE);
        }
        setOrderField(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS);
        setSizeField(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS);
	}

	/**
	 * Not required for this type of layout
	 */
	public void resetLayout() {
	}

	public boolean includeArc(ShrimpArc arc) {
		return true;
	}

	public void setArcTypes(Collection arcTypes) {
	}

	public void setMapLayoutName(String algorithm) {
		this.mapLayoutName = algorithm;
	}

	public String getMapLayoutName() {
		return mapLayoutName;
	}

	public void setOrderField(String inOrderField) {
		orderField = inOrderField;

	}
	public String getOrderField(){
		return orderField;
	}

	public void setSizeField(String inSizeField) {
		sizeField = inSizeField;
	}

	public String getSizeField() {
		return sizeField;
	}

	public void setBorderField(int inBorder) {
		border = inBorder;
	}

	public int getBorderField() {
		return border;
	}

	public String[] getAlgorithmFields() {
		return algorithmFields;
	}

	public String[] getSizeFields() {
		return sizeFields;
	}

	private void setSizeFields(Vector nodes){
		Set fields = new TreeSet(String.CASE_INSENSITIVE_ORDER);
		for (int i=0; i < nodes.size(); i++) {
			ShrimpNode n = (ShrimpNode) nodes.elementAt(i);
			Vector keys = n.getArtifact().getAttributeNames();
			for (int j=0; j < keys.size(); j++) {
				String key = (String) keys.elementAt(j);
				Object value = n.getArtifact().getAttribute(key);
				if (value instanceof Number || value instanceof Date) {
					fields.add(key);
				}
			}
		}
        addExtraAttributeNames(fields);
        removeUndesireableAttributeNames(fields);
		sizeFields = new String [ fields.size()];
        int i = 0;
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            String fieldName = (String) iter.next();
            sizeFields [i++] = fieldName;
        }
	}

	public String[] getOrderFields() {
		return orderFields;
	}

	private void addExtraAttributeNames(Set attrNames) {
        String numDescendentsStr = AttributeConstants.ORD_ATTR_NUM_DESCENDENTS;
        attrNames.add(numDescendentsStr);
        String numChildrenStr = AttributeConstants.ORD_ATTR_NUM_CHILDREN;
        attrNames.add(numChildrenStr);
        //@tag Shrimp(TreeMap): order/size by number of relationships
        String numRelStr = AttributeConstants.ORD_ATTR_NUM_RELATIONSHIPS;
        attrNames.add(numRelStr);
    }

    private void removeUndesireableAttributeNames(Set attrNames) {
        attrNames.remove(AttributeConstants.NOM_ATTR_ARTIFACT_ID);
        // remove any num descendents or num children that have cprel names tagged on the end of them
        for (Iterator iter = attrNames.iterator(); iter.hasNext();) {
            String attrName = (String) iter.next();
            if (attrName.indexOf(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS) != -1 && attrName.length() > AttributeConstants.ORD_ATTR_NUM_DESCENDENTS.length()) {
                iter.remove();
            } else if (attrName.indexOf(AttributeConstants.ORD_ATTR_NUM_CHILDREN) != -1 && attrName.length() > AttributeConstants.ORD_ATTR_NUM_CHILDREN.length()) {
                iter.remove();
            } else if (attrName.indexOf(AttributeConstants.ORD_ATTR_NUM_RELATIONSHIPS) != -1 && attrName.length() > AttributeConstants.ORD_ATTR_NUM_RELATIONSHIPS.length()) {
                iter.remove();
            }
        }
    }

	private void setOrderFields(Vector nodes) {
		Set fields = new TreeSet(String.CASE_INSENSITIVE_ORDER);
		for (int i=0; i < nodes.size(); i++) {
			ShrimpNode n = (ShrimpNode) nodes.elementAt(i);
			Vector keys = n.getArtifact().getAttributeNames();
			for (int j=0; j < keys.size(); j++) {
				String key = (String) keys.elementAt(j);
				Object value = n.getArtifact().getAttribute(key);
				if (value instanceof Comparable) {
					fields.add(key);
				}
			}
		}
        addExtraAttributeNames(fields);
        removeUndesireableAttributeNames(fields);
        orderFields = new String [ fields.size()];
        int i = 0;
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            String fieldName = (String) iter.next();
            orderFields [i++] = fieldName;
        }
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#setupAndApplyLayout(Vector, Rectangle2D.Double, Vector, boolean, boolean, boolean)
	 */
	public void setupAndApplyLayout(Vector nodes, Rectangle2D.Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate, boolean separateComponents) {
		if (showDialog) {
			setSizeFields(nodes);
			setOrderFields(nodes);

			// display treemap options to user
			TreeMapDialog tmd = new TreeMapDialog (this);
			if (tmd.isCancelled()) {
				return;
			}
		}

		// take out any nodes from the layout that don't have the correct customized data
		// the order is determined by some customized data
		// remove any nodes from the layout that don't have this customized data
        if (mapLayoutName.equals(LAYOUT_ORDERED) &&
        		!(orderField.equals(AttributeConstants.ORD_ATTR_NUM_CHILDREN) ||
        		  orderField.equals(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS) ||
        		  orderField.equals(AttributeConstants.ORD_ATTR_NUM_RELATIONSHIPS))) {
    		for (Iterator iter = ((Vector)nodes.clone()).iterator(); iter.hasNext();) {
    			ShrimpNode node = (ShrimpNode) iter.next();
    			Artifact artifact = node.getArtifact();
    			if (artifact.getAttribute(orderField) == null) {
    				nodes.remove(node);
    				nodesToExclude.add(node);
    			}
    		}
        }

		// the size is determined by some customized data
		// remove any nodes from the layout that don't have this customized data
        if (!(sizeField.equals(AttributeConstants.ORD_ATTR_NUM_CHILDREN) ||
        	  sizeField.equals(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS) ||
        	  sizeField.equals(AttributeConstants.ORD_ATTR_NUM_RELATIONSHIPS))) {
    		for (Iterator iter = ((Vector)nodes.clone()).iterator(); iter.hasNext();) {
    			ShrimpNode node = (ShrimpNode) iter.next();
    			Artifact artifact = node.getArtifact();
    			if (artifact.getAttribute(sizeField) == null) {
    				nodes.remove(node);
    				nodesToExclude.add(node);
    			}
    		}
        }


		applyLayout (nodes, nodesToExclude, bounds, animate);
	}

	private void applyLayout(Vector nodesToLayout, Vector nodesToExclude, Rectangle2D.Double bounds, boolean animate) {
		// Find all the children that are visible
		Vector children = new Vector();

		for (int i = 0; i < nodesToLayout.size(); i++) {
			if (displayBean.isVisible(nodesToLayout.elementAt(i))) {
				children.addElement(nodesToLayout.elementAt(i));
			}
		}

		// leave 10% for unconnected nodes, if any
		double conHeight = bounds.getHeight();
		double unconHeight = 0;
		if (!nodesToExclude.isEmpty()) {
			conHeight = bounds.getHeight() * 0.9;
			unconHeight = bounds.getHeight() - conHeight;
			bounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(), conHeight);
		}

		boolean layoutApplied = true;
		if (children.size() > 0) {
			layoutApplied = applyLayoutTo (children, new Vector(), bounds, animate);
			displayBean.setLabelMode(children, displayBean.getDefaultLabelMode());
		}

    	//now place any unconnected nodes in a grid
    	if (layoutApplied && !nodesToExclude.isEmpty()) {
			displayBean.setLabelMode(nodesToExclude, DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
    		//create a temporary grid layout to do the work
    		ShrimpGridLayout grid = new ShrimpGridLayout(displayBean);
			bounds.setRect(bounds.getX(), bounds.getY() + conHeight,  bounds.getWidth(), unconHeight*0.9);
			grid.setupAndApplyLayout(nodesToExclude, bounds, new Vector(), false, animate, false);
    	}

	}

	public boolean applyLayoutTo(Vector nodes, Vector arcs, Rectangle2D.Double bounds, boolean animate) {
		//Init the Layout
		if (mapLayoutName.equalsIgnoreCase(TreeMapLayout.LAYOUT_ORDERED)) {
			mapLayout = new StripTreemap();
		} else if (mapLayoutName.equalsIgnoreCase(TreeMapLayout.LAYOUT_SQUARIFIED)) {
			mapLayout = new SquarifiedLayout();
		} else if (mapLayoutName.equalsIgnoreCase(TreeMapLayout.LAYOUT_SQUARIFIED)) {
			mapLayout = new BinaryTreeLayout();
		} else {
			mapLayout = new OrderedTreemap();
		}


		//Init the Order comparator
		comparator = new AttributeValueComparator();

		// Sort the child nodes
		Collections.sort(nodes, comparator);

		//populate the map
		Mappable items[];
		map = new SimpleMapModel();

		items = new MapItem[nodes.size()];


        /*
         * Size cannot be zero otherwise the node gets a size
           and location of (0,0) on screen, so we make sure
           here that size is at least 1

           What is the best way to represent a value of zero, such as when a sizing
           by the number of children and a node has no children? Should the node really not
           show up in this case or should it just be very small...how small? Its size needs
           to be relatively small compared to its siblings.

           The simple solution here is to change any zero sizes to one eigth the value
           of the smallest size. Probably not ideal but seems ok when performing
           a treemap using number of children, or number of descendents.

        */
		double minSizeAboveZero = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
            double size = getSize(node.getArtifact());
            if (size > 0) {
                minSizeAboveZero = Math.min(minSizeAboveZero, size);
            }
        }

		for (int i = 0; i < nodes.size(); i++) {
            ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
            double size = getSize(node.getArtifact());
            if (size == 0) {
                if (minSizeAboveZero > 0 && minSizeAboveZero != Double.MAX_VALUE) {
                    size = minSizeAboveZero/8.0;
                } else {
                    size = 1.0;
                }
            }
			items[i] = new MapItem();
			items[i].setSize(size);
		}

		((SimpleMapModel)map).setItems(items);

		Rect rect = new Rect();
		double outsideBorder = 5.0;
		rect.h = bounds.height - 2.0*outsideBorder;
		rect.w = bounds.width - 2.0*outsideBorder;
		rect.x = bounds.x + outsideBorder;
		rect.y = bounds.y + outsideBorder;

		mapLayout.layout(map, rect);

		Vector positions = new Vector(nodes.size());
		Vector dimensions = new Vector(nodes.size());

		for (int i = 0; i < nodes.size(); i++) {
			// set new position/size for child
			Dimension dim = new Dimension();

			//Prevent the node from being too small
			dim.height = Math.max ((int) items[i].getBounds().h - border, 5);
			dim.width = Math.max ((int) items[i].getBounds().w - border, 5);

/*
			//Prevent the nodes from becoming too thin to be useful
			//reduce the longest dimension so that it matches maximum ratio
			int ratio = dim.height/dim.width;
			if (ratio > maximumRatio || ratio < 1/maximumRatio) {

				if (dim.height > dim.width)
					dim.height = maximumRatio * dim.width;
				else
					dim.width = maximumRatio * dim.height;
			}
*/
			//finally add the dimenison - we're done adjusting it
			dimensions.add(dim);

			double ulx = items[i].getBounds().x + 0.5*border; // upper left corner x
			double uly = items[i].getBounds().y + 0.5*border; // upper left corner y


			double centerX = ulx + dim.width / 2.0;
			double centerY = uly + dim.height / 2.0;

			if (java.lang.Double.isNaN(centerX)) {
				centerX = 0;
			}
			if (java.lang.Double.isNaN(centerY)) {
				centerY = 0;
			}

			positions.add(new Point2D.Double(centerX, centerY));
		}
		displayBean.setPositionsAndSizes(nodes, positions, dimensions, animate);

		return true;
	}

	public double getSize(Artifact art) {
        double size = 0;
        if (sizeField.equals(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS)) {
            size = getDescendentsCount(art);
        } else if (sizeField.equals(AttributeConstants.ORD_ATTR_NUM_CHILDREN)) {
            size = getChildrenCount(art);
        } else if (sizeField.equals(AttributeConstants.ORD_ATTR_NUM_RELATIONSHIPS)) {
        	size = getRelationshipsCount(art);
        } else {
    		Object attrValue = art.getAttribute(sizeField);
    		if (attrValue != null) {
    			if (attrValue instanceof Number) {
                    Number i = (Number) attrValue;
    				size = i.doubleValue();
    			} else if (attrValue instanceof Date) {
    			    size = ((Date)attrValue).getTime();
                }
    		}
        }

//        if (size < 1) {
//            size = 1;
//        }
		return size;
	}

    private int getDescendentsCount(Artifact art) {
        return displayBean.isInverted() ? art.getAncestors(displayBean.getCprels()).size() : art.getDescendentsCount(displayBean.getCprels(), true);
    }

    private int getChildrenCount(Artifact art) {
        return displayBean.isInverted() ? art.getParentsCount(displayBean.getCprels()) : art.getChildrenCount(displayBean.getCprels());
    }

    private int getRelationshipsCount(Artifact art) {
    	return art.getRelationships().size() + 5;	// why the 5?
    }

	/*
     * Allows the comparison of nodes by the values of a particular attribute.
     * If the attribute values are not comparable then node names will be used instead
     */
	private class AttributeValueComparator implements Comparator {
		public int compare(Object o1, Object o2) {
            Artifact art1 = ((ShrimpNode) (o1)).getArtifact();
            Artifact art2 = ((ShrimpNode) (o2)).getArtifact();
            if (orderField.equals(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS)) {
                return getDescendentsCount(art2) - getDescendentsCount(art1);
            } else if (orderField.equals(AttributeConstants.ORD_ATTR_NUM_CHILDREN)) {
                return getChildrenCount(art2) - getChildrenCount(art1);
            } else {
                Object attrValue1 = art1.getAttribute(orderField);
                Object attrValue2 = art2.getAttribute(orderField);

                if (attrValue1 != null && attrValue2 != null) {
                    if (attrValue1 instanceof Comparable && attrValue2 instanceof Comparable) {
                        if (attrValue1 instanceof String && attrValue2 instanceof String) {
                            String s1 = (String) attrValue1;
                            String s2 = (String) attrValue2;
                            int byComparable = s1.compareToIgnoreCase(s2);
                            if (byComparable != 0) {
                                return byComparable;
                            }

                        }
                        Comparable c1 = (Comparable) attrValue1;
                        Comparable c2 = (Comparable) attrValue2;
                        int byComparable = c2.compareTo(c1);
                        if (byComparable != 0) {
                            return byComparable;
                        }
                    }
                }

                String name1 = art1.getName();
                String name2 = art2.getName();
                return name1.toLowerCase().compareTo(name2.toLowerCase());
            }
		}
	}

	public String toString() {
		return getName() + " (" + mapLayoutName + ", " + orderField + ", " + sizeField + ")";
	}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#getName()
     */
    public String getName() {
        return LayoutConstants.LAYOUT_TREEMAP;
    }

    public MapModel getMap() {
        return map;
    }

    public void setMap(MapModel map) {
        this.map = map;
    }

    public MapLayout getMapLayout() {
        return mapLayout;
    }

    public void setMapLayout(MapLayout mapLayout) {
        this.mapLayout = mapLayout;
    }

    public Comparator getComparator() {
        return comparator;
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

}