/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;
import org.eclipse.mylar.zest.layouts.dataStructures.BendPoint;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RoundedRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedCanvas;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.AttributeFilter;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.XMLSerializerUtil;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PAffineTransform;

/**
 * This is one snap shot of a Shrimp Window
 * @see SnapShot
 *
 * @author Casey Best
 * @date Oct 12, 2001
 */
public class ShrimpSnapShot implements SnapShot {

	private static final String NO_COMMENT_ENTERED = "[no comment entered]";
	private static final String DISPLAY_FILTERS = "displayFilters";
    private static final String DATA_FILTERS = "dataFilters";
    private static final String NAME_ATTR = "name";
    private static final String VALUE_ATTR = "value";
	private static final String SCRIPT_FILE_NAME = "snapshot_scripts.js";
	private static boolean includeImage = true;

	private static final DecimalFormat DEC_FORMAT = new DecimalFormat("0.##");

	private SelectorBean selectorBean;
	private AttrToVisVarBean attrToVisVarBean;
	private double currentScale;

	// the DOM tree structure fro the snapShot
	private Document snapShotDOM;
	private Document htmlDOM;

	private Image previewShot;
	private String userComment = "";

	// this flags tells if a change was made to this snapshot
	private boolean snapShotChanged = false;

	private ViewTool view;
	private ShrimpProject project;
    private DataBean dataBean;
    private FilterBean dataFilterBean;
    private DisplayBean displayBean;
    private FilterBean displayFilterBean;
	private Dimension previewShotSize;

	private static final String EMAIL_PROPS_KEY = "filmstip.email.to";
	public static String USER_NAME;
	static {
	    try {
	        USER_NAME = System.getProperty("user.name");
	    } catch (SecurityException e){
	        USER_NAME = "unknown";
	    }
	}
	public static String USER_EMAIL = "";
	private String timeStamp;

	// referenced in FilmStrip->load()
	public ShrimpSnapShot(Node snapShotRootNode, Component parent) {
		// Create a DOM Document for this snapShot
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			snapShotDOM = builder.newDocument(); // Create from whole cloth
			snapShotDOM.appendChild(snapShotDOM.importNode(snapShotRootNode, true));
			// imports the sub-tree below the root node including the node itself
			loadImageAndComment(parent);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public ShrimpSnapShot(ShrimpProject project, File file, Component parent) {
		this(project, file);
		loadImageAndComment(parent);
	}

	public ShrimpSnapShot(ShrimpProject project, File file) {
		this.project = project;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			snapShotDOM = builder.parse(file);
		} catch (FileNotFoundException e) {
			// Sometimes the applet still throws a FNFE...
		} catch (Throwable t) {
			System.err.println("ShrimpSnapShot error: " + file);
			//t.printStackTrace();
		}
	}

	/**
	 * Note: The view is passed only to create the snapshot.  It's not necessarily the view the snapshot is loaded back onto.
	 */
	public ShrimpSnapShot(ShrimpProject project, ViewTool view, String comment) {
		this.view = view;
		this.project = project;
		this.userComment = comment;

		Date date = new Date();
		timeStamp = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z").format(date);

//		if (USER_EMAIL == null) {
//			try {
//				String host = InetAddress.getLocalHost().getCanonicalHostName();
//				//String dns = host.substring(host.indexOf(".")+1);
//				USER_EMAIL = USER_NAME.concat("@").concat(host);
//			} catch(UnknownHostException ex){
//				ex.printStackTrace();
//			}
//		}

		USER_EMAIL = getEmailAddressFromProperties(project);

        loadBeans(project, view);
		if (displayBean == null) {
			return;
		}
		// calculate the preview shot
		int width = (int) ((Dimension)displayBean.getCanvasDimension()).getWidth();
		int height = (int) ((Dimension)displayBean.getCanvasDimension()).getHeight();
		if ((width <= 0) || (height <= 0)) {
			return;
		}

		PCanvas canvas = ((PNestedDisplayBean)displayBean).getPCanvas();
		previewShot = canvas.createImage(width, height);

		// Special case - in Jambalaya we make an image from the canvas before the canvas is disposed
		if ((previewShot == null) && (canvas instanceof PNestedCanvas)) {
			System.out.println("Got the previewShot from the canvas!");
			PNestedCanvas pnCanvas = (PNestedCanvas)canvas;
			previewShot = pnCanvas.getCanvasImage();
			pnCanvas.clearCanvasImage();	// not needed anymore
		}
		if (previewShot != null) {
			Graphics g = previewShot.getGraphics();
			canvas.paint(g);

			// scale the preview shot to thumbnail size
			// NOTE: this is necessary for faster painting later on
			double wProp = (double) IMAGE_WIDTH / (double) width;
			double hProp = (double) IMAGE_HEIGHT / (double) height;
			double prop = (wProp > hProp) ? hProp : wProp;
			width = (int) (prop * width);
			height = (int) (prop * height);
			previewShotSize = new Dimension(width, height);
			previewShot = previewShot.getScaledInstance(width, height, Image.SCALE_SMOOTH);

			//previewShot = previewShot.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
		} else {
			// use an empty image
			System.err.println("Couldn't create preview image");
			previewShot = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
			previewShotSize = new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT);
		}

		// Store the necessary data in memory as a DOM tree
		createSVGDOM();
		produceHTML();

	}

	public static void saveEmailAddressToProperties(ShrimpProject project, String to) {
		if (project != null) {
			Properties props = project.getProperties();
			props.setProperty(EMAIL_PROPS_KEY, to);
		}
	}

	public static String getEmailAddressFromProperties(ShrimpProject project) {
		if (project != null) {
			Properties props = project.getProperties();
			ShrimpSnapShot.USER_EMAIL = props.getProperty(EMAIL_PROPS_KEY, ShrimpSnapShot.USER_EMAIL);
		}
		return ShrimpSnapShot.USER_EMAIL;
	}

	/**
	 * Takes a snapshot and saves it to a file.
	 */
	public static void takeSnapShot(ShrimpProject project, ShrimpView shrimpView) {
		try {
			String showLoadPreviousDialogStr = ApplicationAccessor.getProperty(
					ShrimpApplication.SHOW_RETURN_TO_PREVIOUS_DIALOG_KEY, "true");
			if ("true".equalsIgnoreCase(showLoadPreviousDialogStr)) {
				ShrimpSnapShot snapShot = new ShrimpSnapShot(project, shrimpView, null);
				snapShot.save(getSnapShotName(project));
			}
		} catch (Exception ex) {
			System.err.println(ApplicationAccessor.getAppName() + " - Warning! Couldn't create snapshot.");
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the snapshot name from the project.
	 * @return the snapshot filename
	 */
	public static String getSnapShotName(ShrimpProject project) {
		URI projectURI = project.getProjectURI();
		String projectName = new String (projectURI.toString());
		int index = projectURI.toString().lastIndexOf('/');
		if (index != -1) {
		    projectName = projectName.substring(index + 1);
		}
		String scheme = projectURI.getScheme();
		String path = projectURI.getPath();
		String fileName = (scheme != null && path != null && scheme.toLowerCase().equals("file") ? projectURI.getPath() : projectName) + ".svg";
		return fileName;
	}

	/**
	 * @param project
	 * @param view
	 * @throws BeanNotFoundException
	 */
	private void loadBeans(ShrimpProject project, ViewTool view) {
		try {
			this.dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			this.dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
			this.displayBean = (DisplayBean) view.getBean(ShrimpTool.DISPLAY_BEAN);
			this.selectorBean = (SelectorBean) view.getBean(ShrimpTool.SELECTOR_BEAN);
			this.displayFilterBean = (FilterBean) view.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			this.attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}

	/**
	 * Returns the root of its DOM.
	 */
	public Node getSnapShotDOMSubTree() {
		return snapShotDOM.getDocumentElement();
	}

	/**
	 * Returns the userComment associated with this snapshot
	 */
	public String getComment() {
		return userComment;
	}

	public void setComment(String c) {
		userComment = c;
	}

	public String toString() {
		String comment = getComment();
		if (comment == null || comment.length()== 0) {
			comment = NO_COMMENT_ENTERED;
		}
		return "ShrimpSnapShot: " + comment;
	}

	/**
	 * Returns the flag that indicates if this snapshot was changed
	 */
	public boolean hasChanged() {
		return snapShotChanged;
	}

	/**
	 * Presents the userComment to the user for modifications
	 * @return true if the comment changed
	 */
	public boolean promptUserToChangeComment(Frame parentOfDialog) {
		boolean changed = false;
		String message = "Edit the message attached to this snapshot:";
		CommentDialog dialog = new CommentDialog(parentOfDialog, userComment, message);
		if (dialog.showDialog() == JOptionPane.OK_OPTION) {
			changed = changeComment(dialog.getComment());
		}
		return changed;
	}

	/**
	 * Change the userComment on this snapshot to the given string
	 * @param newComment The userComment to put on this snapshot.  The old userComment is erased.
	 * @return true if the comment changed
	 */
	public boolean changeComment(String newComment) {
		// if Comment did not change do nothing
		if (this.getComment().equals(newComment)) {
			return false;
		}
		setComment(newComment);

		try {
			// check if there is any comment there first, if not then add a new node for the new userComment
			/* This is what the xml looks like:
			 * <g name="info">
			 *   <text name="userComment">comment/text>
			 * </g>
			 */
			NodeList gs = snapShotDOM.getElementsByTagName("g");
			for (int i = 0; i < gs.getLength(); i++) {
				Node n = gs.item(i);
				if (n instanceof Element) {
					Element el = (Element) n;
					if ("info".equals(el.getAttribute(NAME_ATTR))) {
						boolean found = false;
						NodeList textNodes = el.getElementsByTagName("text");
						for (int j = 0; j < textNodes.getLength(); j++) {
							Node tn = textNodes.item(j);
							Node nameItem = tn.getAttributes().getNamedItem(NAME_ATTR);
							if (nameItem != null) {
								String name = nameItem.getNodeValue();
								if ("userComment".equals(name)) {
									found = true;
									if (tn.getFirstChild() != null) {
										tn.getFirstChild().setNodeValue(newComment);
									} else {
										tn.appendChild(snapShotDOM.createTextNode(newComment));
									}
									break;
								}
							}
						}
						if (!found) {
							Element tn = snapShotDOM.createElement("text");
							tn.setAttribute(NAME_ATTR, "userComment");
							tn.appendChild(snapShotDOM.createTextNode(newComment));
							el.appendChild(tn);
						}
						break;
					}
				}
			}
		} catch (DOMException e) {
			e.printStackTrace();
		}

		snapShotChanged = true; // the snapshot has changed
		return true;
	}

	/**
	 * Returns a small image which is a preview of this snapshot.
	 */
	public Image getPreviewShot() {
		return previewShot;
	}

	/**
	 * Returns the size of the preview shot
	 */
	public Dimension getPreviewShotSize() {
		return previewShotSize;
	}

	/**
	 * Reverts the view back to this snapshot.  For example, the display in shrimp will revert to the
	 * same layout as it was in when this snapshot was taken.
	 *
	 * @param view The view to make look like this snapshot
	 */
	public void revertViewToSnapShotState(ShrimpProject project, ViewTool view) {
		this.project = project;
		this.view = view;

		loadBeans(project, view);
		loadImageAndComment(ApplicationAccessor.getParentFrame());
		loadView();
	}

	/**
	 * Saves this snapShot to the given filename
	 */
	public void save(String fileName) {
		try {
            FileOutputStream svgFileOutputStream = new FileOutputStream(fileName);
            save(svgFileOutputStream);
            svgFileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) { // thrown when shrimp is an applet
            //e.printStackTrace();
        }
	}

	public void saveDOMs(String svgFileName, String htmlFileName){
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			// indenting causes problems when reading - adds text nodes
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(snapShotDOM);
			StreamResult result = new StreamResult(new File (svgFileName));
			transformer.transform(source, result);

			Element el = (Element)(htmlDOM.getElementsByTagName("iframe").item(0));
			int pos = svgFileName.lastIndexOf("\\");
			String fname = svgFileName.substring(pos+1) ;
			el.setAttributeNS(null,"src",fname);

			DOMSource htmlSource = new DOMSource(htmlDOM);
			StreamResult htmlResult = new StreamResult(new File(htmlFileName));
			transformer.transform(htmlSource, htmlResult);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Saves this snapShot to the given output stream
	 */
	public void save(FileOutputStream fileOutputStream) {
		try {
			try {
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				// indenting causes problems when reading - adds text nodes
				//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(snapShotDOM);
				StreamResult result = new StreamResult(fileOutputStream);
				// @tag Shrimp.Java6 : this sometimes throws an AbstractMethodError in Java 1.6
				transformer.transform(source, result);
			} catch (AbstractMethodError e) {
				// use an alternate xml serialization method
				XMLSerializerUtil.serialize(snapShotDOM, fileOutputStream);
			}
			snapShotChanged = false; // the changes have been saved
		} catch (Throwable t) {
			System.err.println(ApplicationAccessor.getAppName() + "- Error saving snapshot: " + t.getMessage());
			t.printStackTrace();
		}
	}

	/**
	 * Cleans up any resources this snap shot was using.  You can
	 * assume that this snap shot won't be used once this method
	 * is called.
	 */
	public void cleanUp() {
		if (previewShot != null) {
			previewShot.flush();
		}
	}

	/**
	 *  Call this method if the snapShot was saved externally (Eg, filmstrip imported the tree structure and saved it).
	 */
	public void changesSaved() {
		snapShotChanged = false;
	}

	private static String transformToMatrixString(AffineTransform tx) {
		double[] matrix = new double[6];
		tx.getMatrix(matrix);
		String txStr = "matrix(" + matrix[0] + "," + matrix[1] + "," + matrix[2] + "," + matrix[3] + "," + matrix[4] + "," + matrix[5] + ")";
		return txStr;
	}

	private static String colourToRBGString(Color color) {
		String rgb = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
		return rgb;
	}

	private void createSVGDOM() {
		try {
		    DOMImplementation impl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
			Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			String svgNS = "http://www.w3.org/2000/svg"; //
			snapShotDOM = impl.createDocument(svgNS, "svg", null);
			Element svgNode = snapShotDOM.getDocumentElement();

			svgNode.setAttributeNS(null, "width", "" + ((Dimension)displayBean.getCanvasDimension()).getWidth());
			svgNode.setAttributeNS(null, "height", "" + ((Dimension)displayBean.getCanvasDimension()).getHeight()+150);
			boolean jsFileExists = ResourceHandler.getFileURL("scripts/" + SCRIPT_FILE_NAME) != null;

			if (jsFileExists) {
				svgNode.setAttributeNS(null, "onload", "LoadHandler(evt)");
				svgNode.setAttributeNS(null, "onzoom", "zoomHandler(evt)");
			}
			svgNode.setAttribute(NAME_ATTR, "snapshot");

			// TODO testing out ways to save attribute and visual variable map
			Attribute attribute;

			attribute = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_COLOR);
			Element attributeMap = snapShotDOM.createElementNS(svgNS, "AttributeMap");
			svgNode.appendChild(attributeMap);
			attributeMap.setAttributeNS(null, NAME_ATTR, attribute.getName());
			attributeMap.setAttributeNS(null, VALUE_ATTR, VisVarConstants.VIS_VAR_NODE_COLOR);


			//create arrowhead defs - one for each colour ... is there a better way to do this?
			/*
			<defs>
				<marker id="Triangle"
				  viewBox="0 0 10 10" refX="0" refY="5"
				  markerUnits="strokeWidth"
				  markerWidth="4" markerHeight="3"
				  orient="auto">
				  <path d="M 0 0 L 10 5 L 0 10 z" />
				</marker>
			  </defs>
			*/ {
				Element defsNode = snapShotDOM.createElementNS(svgNS, "defs");
				svgNode.appendChild(defsNode);
				Vector arcs = displayBean.getAllArcs();
				Set arcColours = new HashSet();
				for (Iterator iter = arcs.iterator(); iter.hasNext();) {
					ShrimpArc arc = (ShrimpArc) iter.next();
					Color colour = arc.getColor();
					arcColours.add(colour);
				}
				for (Iterator iter = arcColours.iterator(); iter.hasNext();) {
					Color colour = (Color) iter.next();
					String markerID = "arrowHead" + colour.getRGB();
					Element markerNode = snapShotDOM.createElementNS(svgNS, "marker");
					defsNode.appendChild(markerNode);

					markerNode.setAttributeNS(null, "id", markerID);
					markerNode.setAttributeNS(null, "viewBox", "0 0 10 10");
					markerNode.setAttributeNS(null, "refX", "10");
					markerNode.setAttributeNS(null, "refY", "5");
					markerNode.setAttributeNS(null, "markerUnits", "strokeWidth");
					markerNode.setAttributeNS(null, "markerWidth", "8");
					markerNode.setAttributeNS(null, "markerHeight", "6");
					markerNode.setAttributeNS(null, "orient", "auto");

					Element markerPathNode = snapShotDOM.createElement("path");
					markerNode.appendChild(markerPathNode);
					markerPathNode.setAttributeNS(null, "d", "M 0 0 L 10 5 L 0 10 z");
					markerPathNode.setAttributeNS(null, "fill", colourToRBGString(colour));
				}
			}

			// <g id="root" onkeydown="keydownHandler(evt)" onkeyup="keyupHandler(evt)" transform = "camera transform">
			PCamera camera = ((PNestedDisplayBean)displayBean).getPCanvas().getCamera();
			double[] matrix = new double[6];
			camera.getViewTransform().getMatrix(matrix);
			currentScale = camera.getViewScale();
			String txStr = "matrix(" + matrix[0] + "," + matrix[1] + "," + matrix[2] + "," + matrix[3] + "," + matrix[4] + "," + matrix[5] + ")";
			Element rootGNode = snapShotDOM.createElementNS(svgNS, "g");
			rootGNode.setAttributeNS(null, "id", "displayBean");

			rootGNode.setAttributeNS(null, "transform", txStr);

			svgNode.appendChild(rootGNode);

			// not to include the image to reduce file size.
			if (includeImage && (previewShot != null)) {
				Element imageNode = snapShotDOM.createElementNS(svgNS, "image");
				rootGNode.appendChild(imageNode);
				imageNode.setAttributeNS(null, "visibility", "hidden");
				// add image data
				String imageStr = createImageString(previewShot, getPreviewShotSize());
				imageNode.appendChild(snapShotDOM.createCDATASection(imageStr));
			}

			// add nodes and labels
			//<g id="nodes_and_labels">

			Element nodesNode = snapShotDOM.createElementNS(svgNS, "g");
			nodesNode.setAttributeNS(null, "id", "nodes_and_labels");
			rootGNode.appendChild(nodesNode);

			/* for each node ...
			 <rect	id="node id"
			 			transform="node transformation"
			 			fill = "node background colour"
			  			stroke = "node shape stoke colour"
			  			stroke-width = "node shape stroke width"
			  			width = "node preferred width"
			  			height = "node preferred height"
			  			onmousemove = "mousemoveHandler(evt)>
			  		<title>"node tooltip"</title>
			  </rect>
			*/
			Vector nodes = new Vector(displayBean.getVisibleNodes());
			// sort nodes in reverse order, so that children are drawn on top of parents
			Collections.sort(nodes, Collections.reverseOrder());
			for (int i = 0; i < nodes.size(); i++) {
				ShrimpNode sn = (ShrimpNode) nodes.elementAt(i);
				Element rectNode = snapShotDOM.createElementNS(svgNS, "rect");
				nodesNode.appendChild(rectNode);

				if ((sn instanceof PShrimpNode) && ((PShrimpNode)sn).getCusomizedPanelImage() != null) {
				    Image image = ((PShrimpNode)sn).getCusomizedPanelImage();
					Element imageNode = snapShotDOM.createElementNS(svgNS, "image");
					rectNode.appendChild(imageNode);
					imageNode.setAttributeNS(null, "visibility", "hidden");
					// add image data
					Dimension dim = new Dimension(image.getWidth(view.getGUI()), image.getHeight(view.getGUI()));
					String imageStr = createImageString(image, dim);
					imageNode.appendChild(snapShotDOM.createCDATASection(imageStr));
				}

				// get tranformation of the node
				AffineTransform nodeTrans = ((PShrimpNode)sn).getLocalToGlobalTransform(new PAffineTransform());
				Rectangle2D.Double snBounds = sn.getOuterBounds();
				Dimension prefSize = new Dimension ((int)snBounds.getWidth(), (int)snBounds.getHeight());

				Color colour = sn.getColor();
				boolean visible = displayBean.isVisible(sn);
				String visibility = (visible) ? "visible" : "hidden";
				String toolTip = (String) sn.getArtifact().getAttribute("tooltip");
				if (toolTip == null) {
					toolTip = sn.getArtifact().getName() + " (" + sn.getArtifact().getType() + ")";
				}
				String artifactID = sn.getArtifact().getExternalIdString();
				NodeShape nodeShape = sn.getNodeShape();
				if (nodeShape instanceof RoundedRectangleNodeShape) {
					RoundRectangle2D.Double roundedRect = (RoundRectangle2D.Double)nodeShape.getShape(sn.getOuterBounds());
					rectNode.setAttributeNS(null, "rx", String.valueOf(roundedRect.getArcWidth()/2.0));
					rectNode.setAttributeNS(null, "ry", String.valueOf(roundedRect.getArcHeight()/2.0));
				}

				rectNode.setAttributeNS(null, "id", artifactID);
				rectNode.setAttributeNS(null, "transform", transformToMatrixString(nodeTrans));
				rectNode.setAttributeNS(null, "fill", colourToRBGString(colour));
				if (!selectedNodes.contains(sn)) {
					Color strokeColor = selectedNodes.contains(sn) ? NodeBorder.DEFAULT_HIGHLIGHT_COLOR : NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR;
					rectNode.setAttributeNS(null, "stroke", colourToRBGString(strokeColor));
					rectNode.setAttributeNS(null, "stroke-width", "2");
				} else {
					rectNode.setAttributeNS(null, "stroke", "blue");
					rectNode.setAttributeNS(null, "stroke-width", "8");
				}
				rectNode.setAttributeNS(null, "width", "" + prefSize.getWidth());
				rectNode.setAttributeNS(null, "height", "" + prefSize.getHeight());
				rectNode.setAttributeNS(null, "visibility", visibility);

				if (jsFileExists) {
					rectNode.setAttributeNS(null, "onmousemove", "mousemoveHandler(evt)");
				}
				rectNode.setAttributeNS(null, "panelMode", displayBean.getPanelMode(sn)); // **** this is not an svg tag!, not sure where to put it

				Element transformationNode = snapShotDOM.createElement("transform");
				rectNode.appendChild(transformationNode);

				AffineTransform transform = (AffineTransform) displayBean.getTransformOf(sn);
				double[] transformMatrix = new double[6];
				transform.getMatrix(transformMatrix);

				transformationNode.setAttribute("x", String.valueOf(transformMatrix[0]));
				transformationNode.setAttribute("y", String.valueOf(transformMatrix[1]));
				transformationNode.setAttribute("width", String.valueOf(transformMatrix[2]));
				transformationNode.setAttribute("height", String.valueOf(transformMatrix[3]));
				transformationNode.setAttribute("scaleX", String.valueOf(transformMatrix[4]));
				transformationNode.setAttribute("scaleY", String.valueOf(transformMatrix[5]));

				// parent nodes
				ShrimpNode parentNode = sn.getParentShrimpNode();
				while (parentNode != null) {
					Element parentIdNode = snapShotDOM.createElement("parentId");
					rectNode.appendChild(parentIdNode);
					String parentID = "" + parentNode.getArtifact().getID();
					parentIdNode.appendChild(snapShotDOM.createTextNode(parentID));

					parentNode = parentNode.getParentShrimpNode();
				}

				// focus state
				Element focusNode = snapShotDOM.createElement("focus");
				rectNode.appendChild(focusNode);
				Vector currentlyFocusedOnObjects = displayBean.getCurrentFocusedOnObjects();

				String focus;
				if (currentlyFocusedOnObjects.contains(sn)) {
					focus = "true";
				} else {
					focus = "false";
				}
				focusNode.appendChild(snapShotDOM.createTextNode(focus));

				Element titleNode = snapShotDOM.createElementNS(svgNS, "title");
				titleNode.appendChild(snapShotDOM.createTextNode(toolTip));
				rectNode.appendChild(titleNode);

				// add node labels
				if (sn.isVisible()) {
					Element textNode = snapShotDOM.createElementNS(svgNS, "text");
					nodesNode.appendChild(textNode);
					Rectangle2D.Double bounds = null;
					Font labelFont = (Font) displayBean.getLabelFont();
					String labelStr = sn.getName();
					AffineTransform labelTrans = null;
					bounds = sn.getLabelBounds();
					labelTrans = nodeTrans;
					textNode.setAttributeNS(null, "x", "" + (bounds.getX() + bounds.getWidth()/2.0));
					textNode.setAttributeNS(null, "y", "" + (bounds.getHeight() - bounds.getY()));
					textNode.setAttributeNS(null, "font-family", labelFont.getFamily());
					textNode.setAttributeNS(null, "font-size", "" + labelFont.getSize());
					textNode.setAttributeNS(null, "text-anchor", "middle");
					textNode.setAttributeNS(null, "transform", transformToMatrixString(labelTrans));
					textNode.setAttributeNS(null, "fill", "black");
					if (jsFileExists) {
						textNode.setAttributeNS(null, "onmousemove", "mousemoveHandler(evt)");
					}
					textNode.appendChild(snapShotDOM.createTextNode(labelStr));

					Element labelTitleNode = snapShotDOM.createElementNS(svgNS, "title");
					labelTitleNode.appendChild(snapShotDOM.createTextNode(toolTip));
					textNode.appendChild(labelTitleNode);
				}
			}

			//add arcs
			Element arcsNode = snapShotDOM.createElementNS(svgNS, "g");
			arcsNode.setAttributeNS(null, "id", "arcs");
			rootGNode.appendChild(arcsNode);

			Vector arcs = displayBean.getAllArcs();
			for (Iterator iter = arcs.iterator(); iter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				boolean visible = displayBean.isVisible(arc);
				if (!visible) {
					continue;
				}
				// for now we will just have straight arcs
				Point2D.Double srcPoint = arc.getSrcTerminal().getArcAttachPoint();
				Point2D.Double destPoint = arc.getDestTerminal().getArcAttachPoint();
				Element lineNode = snapShotDOM.createElementNS(svgNS, "line");
				arcsNode.appendChild(lineNode);

				lineNode.setAttributeNS(null, "x1", "" + srcPoint.x);
				lineNode.setAttributeNS(null, "y1", "" + srcPoint.y);
				lineNode.setAttributeNS(null, "x2", "" + destPoint.x);
				lineNode.setAttributeNS(null, "y2", "" + destPoint.y);

				// @tag Shrimp.BendPoints : save bendpoints in snapshot?
				if (arc.hasBendPoints()) {
					//lineNode.setAttributeNS(null, "bendPoints", bendPointsToString(arc.getBendPoints()));
				}

				Color colour = arc.getColor();
				String visibility = (visible) ? "visible" : "hidden";
				String markerID = "arrowHead" + colour.getRGB();

				lineNode.setAttributeNS(null, "stroke", colourToRBGString(colour));
				lineNode.setAttributeNS(null, "stroke-width", "1");
				lineNode.setAttributeNS(null, "visibility", visibility);
				lineNode.setAttributeNS(null, "marker-end", "url(#" + markerID + ")");
				if (jsFileExists) {
					lineNode.setAttributeNS(null, "onmousemove", "mousemoveHandler(evt)");
				}

				String toolTip = arc instanceof ShrimpCompositeArc ? ((ShrimpCompositeArc)arc).getArcCount() + "" : arc.getRelationship().getName();
				Element titleNode = snapShotDOM.createElementNS(svgNS, "title");
				titleNode.appendChild(snapShotDOM.createTextNode(toolTip));
				lineNode.appendChild(titleNode);
			}

			appendNonDisplayNodes(svgNode);
			appendScriptNode(svgNode);
		} catch (Throwable e) {
			System.err.println("problem creating SVG DOM SnapShot");
			e.printStackTrace();
		}
	}

	/**
	 * Converts bendpoints into a string for storing as an attribute.
	 * @param bendPoints
	 * @return a String like "double,double,boolean|double,double,boolean"
	 * where the doubles are the x,y values and the boolean is if it is a control point
	 */
	protected String bendPointsToString(LayoutBendPoint[] bendPoints) {
		StringBuffer buffer = new StringBuffer();
		if (bendPoints != null) {
			for (int i = 0; i < bendPoints.length; i++) {
				LayoutBendPoint bp = bendPoints[i];
				if (i > 0) {
					buffer.append("|");
				}
				buffer.append(DEC_FORMAT.format(bp.getX()));
				buffer.append(",");
				buffer.append(DEC_FORMAT.format(bp.getY()));
				buffer.append(",");
				buffer.append(""+bp.getIsControlPoint());
			}
		}
		return buffer.toString();
	}

	/**
	 * Parse the given attribute string into an array of {@link LayoutBendPoint} objects.
	 * The format is expected to be like: "double,double,boolean|double,double,boolean"
	 * where doubles are numbers and booleans will be the "true" or "false" string.
	 * @param str
	 * @return
	 */
	protected LayoutBendPoint[] bendPointsFromString(String str) {
		LayoutBendPoint[] bendPoints = new LayoutBendPoint[0];
		if ((str != null) && (str.length() > 0)) {
			String[] bps = str.split("|");
			ArrayList list = new ArrayList(bps.length);
			for (int i = 0; i < bps.length; i++) {
				String[] tri = bps[i].split(",");
				if (tri.length == 3) {
					try {
						double x = Double.parseDouble(tri[0]);
						double y = Double.parseDouble(tri[1]);
						boolean cp = "true".equalsIgnoreCase(tri[2]);
						list.add(new BendPoint(x, y, cp));
					} catch (NumberFormatException ex) {
					}
				}
			}
			bendPoints = (LayoutBendPoint[]) list.toArray(new LayoutBendPoint[list.size()]);
		}
		return bendPoints;
	}

	// appends javascripts inside of a <script> tag
	private void appendScriptNode(Element parentNode) {
		// add javascripts from file
		String fileContents = ResourceHandler.getFileContents("scripts/" + SCRIPT_FILE_NAME);
		Element scriptLoadHandlerNode = snapShotDOM.createElement("script");
		parentNode.appendChild(scriptLoadHandlerNode);
		CDATASection cDataScript = snapShotDOM.createCDATASection(fileContents);
		scriptLoadHandlerNode.appendChild(cDataScript);
	}

	private void appendNonDisplayNodes(Element parentNode) {
		Element nonSVGNode = snapShotDOM.createElementNS(null, "g");
		nonSVGNode.setAttribute("visibility", "hidden");
		nonSVGNode.setAttribute(NAME_ATTR, "non_display");
		parentNode.appendChild(nonSVGNode);

		// add "meta" data
		appendInfoNode(nonSVGNode);

		// add displayBean cprels
		Element cprelNode = snapShotDOM.createElementNS(null, "text");
		cprelNode.setAttribute(NAME_ATTR, "cprels");
		String[] cprels = displayBean.getCprels();
		String cprelsString = "";
		for (int i = 0; i < cprels.length; i++) {
			String cprel = cprels[i];
			cprelsString += cprel;
			if (i < cprels.length -1)  {
				cprelsString += "|";
			}
		}
		cprelNode.appendChild(snapShotDOM.createTextNode(cprelsString));
		nonSVGNode.appendChild(cprelNode);

		// add currently focused on node id
		Vector focusedOnObjects = displayBean.getCurrentFocusedOnObjects();
		long focusedNodeID = -1;
		if (focusedOnObjects.size() == 1) {
			ShrimpNode sn = (ShrimpNode) focusedOnObjects.firstElement();
			focusedNodeID = sn.getID();
		}
		Element focusNode = snapShotDOM.createElementNS(null, "text");
		focusNode.setAttribute(NAME_ATTR, "focusedNodeID");
		focusNode.appendChild(snapShotDOM.createTextNode("" + focusedNodeID));

		Rectangle2D.Double screenCoordinates = (Rectangle2D.Double) displayBean.getScreenCoordinates();

		Element screenCoordNode = snapShotDOM.createElementNS(null, "text");
		screenCoordNode.setAttribute(NAME_ATTR, "screenCoord");

		screenCoordNode.setAttribute("x", String.valueOf(screenCoordinates.getX()));
		screenCoordNode.setAttribute("y", String.valueOf(screenCoordinates.getY()));
		screenCoordNode.setAttribute("width", String.valueOf(screenCoordinates.getWidth()));
		screenCoordNode.setAttribute("height", String.valueOf(screenCoordinates.getHeight()));

		nonSVGNode.appendChild(focusNode);
		nonSVGNode.appendChild(screenCoordNode);

		// add filters
		appendFilterNodes(nonSVGNode, displayFilterBean, DISPLAY_FILTERS);
		appendFilterNodes(nonSVGNode, dataFilterBean, DATA_FILTERS);
	}

	// write the display filters
	private void appendFilterNodes(Element parentNode, FilterBean filterBean, String tag) {
		Element allFiltersNode = snapShotDOM.createElement("g");
		allFiltersNode.setAttribute("id", tag);
		allFiltersNode.setAttribute(NAME_ATTR, tag);
		parentNode.appendChild(allFiltersNode);

		Vector allFilters = filterBean.getFilters();
		int i = 0;
		for (Iterator iter = allFilters.iterator(); iter.hasNext();) {
			Filter filter = (Filter) iter.next();
			if (filter instanceof AttributeFilter) {
				String filterStr = ((AttributeFilter)filter).toString();
				Element filterNode = snapShotDOM.createElement("text");
				filterNode.setAttribute("id", "filter_" + i);
				filterNode.setAttribute(NAME_ATTR, "filter_" + i);
				filterNode.appendChild(snapShotDOM.createTextNode(filterStr));
				allFiltersNode.appendChild(filterNode);
				i++;
			}
		}
	}

	/** add the project information
	 * The infor node is supposed to be appended as a child node for a snapshot node.
	 * <svg id="snapshot_1">
	 *      <g>... ...</g>
	 *      <info>
	 *              <userName>..</userName>
	 *              <prjName>..</prjName>
	 *              <timeStamp>..</timeStamp>
	 *      </info>
	 * </svg>
	 * @param parent
	 */
	private void appendInfoNode(Element parent) {
		Element infoNode = snapShotDOM.createElement("g");
		infoNode.setAttribute("visibility", "hidden");
		infoNode.setAttribute(NAME_ATTR, "info");
		Element usrNode = snapShotDOM.createElement("text");
		usrNode.setAttribute(NAME_ATTR, "userName");
		Element prjNode = snapShotDOM.createElement("text");
		prjNode.setAttribute(NAME_ATTR, "prjName");
		Element timeNode = snapShotDOM.createElement("text");
		timeNode.setAttribute(NAME_ATTR, "timeStamp");

		Element commentNode = snapShotDOM.createElement("text");
		commentNode.setAttribute(NAME_ATTR, "userComment");
		if (userComment == null || userComment.length() == 0) {
			userComment = NO_COMMENT_ENTERED;
		}

		Element currentScaleNode = snapShotDOM.createElement("text");
		currentScaleNode.setAttribute("id", "currentScale");
		currentScaleNode.appendChild(snapShotDOM.createTextNode(String.valueOf(currentScale)));

		infoNode.appendChild(usrNode);
		infoNode.appendChild(prjNode);
		infoNode.appendChild(timeNode);
		infoNode.appendChild(commentNode);
		infoNode.appendChild(currentScaleNode);

		parent.appendChild(infoNode);

		usrNode.appendChild(snapShotDOM.createTextNode(USER_NAME));
		timeNode.appendChild(snapShotDOM.createTextNode(timeStamp));
		prjNode.appendChild(snapShotDOM.createTextNode(project.getTitle()));
		commentNode.appendChild(snapShotDOM.createTextNode(userComment));
	}

	/**
	 * Loads the view from the DOM tree.
	 */
	private void loadView() {
		//Cursor cursor = ApplicationAccessor.getApplication().getCursor();
		boolean displayVisible = displayBean.isVisible();

		//init the beans we will need later
		ActionHistoryBean actionHistoryBean = null;
		try {
			actionHistoryBean = (ActionHistoryBean) view.getBean(ShrimpTool.ACTION_HISTORY_BEAN);
		} catch (BeanNotFoundException e1) {
			System.err.println("ActionHistoryBean not found");
			e1.printStackTrace();
			return;
		}

		boolean filterBeanFiringEvents = displayFilterBean.isFiringEvents();

		ApplicationAccessor.waitCursor();
		try {
			displayBean.setVisible(false);
			displayFilterBean.setFiringEvents(false);

			// TODO: testing out ways to save attribute and visual variable map
			NodeList list = snapShotDOM.getElementsByTagName("AttributeMap");
			for (int i = 0; i < list.getLength(); i++) {
				Element el = (Element) list.item(i);
				String name = el.getAttribute(NAME_ATTR);
				String value = el.getAttribute(VALUE_ATTR);

				attrToVisVarBean.mapAttrToVisVar(name, value);
			}

			// read the cprel and make sure the user wants to apply this
			String savedCprelsString = "";
			list = snapShotDOM.getElementsByTagName("text");
			for (int i = list.getLength() - 1; i >= 0; i--) {
				Element el = (Element) (list.item(i));
				if ((el.getAttribute(NAME_ATTR).indexOf("cprels")) != -1) {
					if (el.getChildNodes().getLength() > 0) {
						savedCprelsString = el.getChildNodes().item(0).getNodeValue();
					}
					break;
				}
			}

			StringTokenizer st = new StringTokenizer(savedCprelsString, "|");
			List cprelsOfSnapShot = new ArrayList(st.countTokens());
			//int index = 0;
			while (st.hasMoreTokens()) {
				String cprel = st.nextToken();
				cprelsOfSnapShot.add(cprel);
			}

			boolean cprelOK = false;
			if (view instanceof ShrimpView) {
				cprelOK = checkCprels(cprelsOfSnapShot);
			}

			if (!cprelOK) {
				ApplicationAccessor.getApplication().defaultCursor();
				displayBean.setVisible(displayVisible);
				return;
			}

			dataBean.setDataIsDirty();
            loadFilters(displayFilterBean, DISPLAY_FILTERS);
            loadFilters(dataFilterBean, DATA_FILTERS);
			project.refresh();
			//shrimpView.addDefaultRootNodes();

			// focus on the root
			//displayBean.focusOnExtents();

			// reset history bean
			actionHistoryBean.clearHistory();
			actionHistoryBean.clearFuture();

			// create a vector for the focused on objects
			Vector focusedOnObjects = new Vector();

			NodeList nodesNlabelsNodeList = snapShotDOM.getElementsByTagName("g");
			Vector nodesInDisplay = displayBean.getAllNodes();
			for (int i = 0; i < nodesNlabelsNodeList.getLength(); i++) {
				Element el = (Element) (nodesNlabelsNodeList.item(i));

				if ((el.getAttributeNode("id") != null) && (el.getAttributeNode("id").getValue().equalsIgnoreCase("displayBean"))) {
					NodeList displayBeanNodes = el.getChildNodes();
					for (int j = 0; j < displayBeanNodes.getLength(); j++) {
						Element displayBeanEl = (Element) displayBeanNodes.item(j);
						if ((displayBeanEl.getAttributeNode("id") != null) && (displayBeanEl.getAttributeNode("id").getValue().equalsIgnoreCase("nodes_and_labels"))) {
							NodeList artifactsNodeList = displayBeanEl.getElementsByTagName("rect");
							for (int k = 0; k < artifactsNodeList.getLength(); k++) {
								Element artifactNode = (Element) artifactsNodeList.item(k);
								String id = artifactNode.getAttributeNode("id").getValue();
								Object artifactId = dataBean.getArtifactExternalIDFromString(id);

								if (artifactId != null) {
									String panelMode = artifactNode.getAttributeNode("panelMode").getValue();
									Boolean visible = new Boolean((artifactNode.getAttributeNode("visibility").getValue()).equalsIgnoreCase("visible"));

									// get chain of parents
									Vector parentIDs = new Vector();
									NodeList parentIDNodeList = artifactNode.getElementsByTagName("parentId");
									for (int l = 0; l < parentIDNodeList.getLength(); l++) {
										String parentID = parentIDNodeList.item(l).getFirstChild().getNodeValue();
										parentIDs.addElement(parentID);
									}

									// get the position of the node
									double[] matrix = new double[6];
									NodeList transformNode = artifactNode.getElementsByTagName("transform");
									Element transEl = (Element) transformNode.item(0);

									matrix[0] = Double.parseDouble(transEl.getAttributeNode("x").getValue());
									matrix[1] = Double.parseDouble(transEl.getAttributeNode("y").getValue());
									matrix[2] = Double.parseDouble(transEl.getAttributeNode("width").getValue());
									matrix[3] = Double.parseDouble(transEl.getAttributeNode("height").getValue());
									matrix[4] = Double.parseDouble(transEl.getAttributeNode("scaleX").getValue());
									matrix[5] = Double.parseDouble(transEl.getAttributeNode("scaleY").getValue());

									int preferredWidth = (int) Double.parseDouble(artifactNode.getAttributeNode("width").getValue());
									int preferredHeight = (int) Double.parseDouble(artifactNode.getAttributeNode("height").getValue());

									// now position nodes in display - assume the parent is always added before the child
									Artifact artifact = dataBean.findArtifactByExternalId(artifactId);

									ShrimpNode sn = null;
									if (artifact != null) {
									    sn = displayBean.getDataDisplayBridge().getShrimpNode(artifact, parentIDs, true);
									}
									if (sn != null) {
										if (!nodesInDisplay.contains(sn)) {
											displayBean.addObject(sn);
										}

										// update the preferred size of this node, if needed
										Rectangle2D.Double snBounds = sn.getOuterBounds();
										sn.setOuterBounds(new Rectangle2D.Double(snBounds.x, snBounds.y, preferredWidth, preferredHeight));

										displayBean.setVisible(sn, visible.booleanValue(), true);

										//restore the node's image if it had one
										NodeList imageNodes = artifactNode.getElementsByTagName("image");
										if (imageNodes.getLength() == 1) {
											Element imageEl = (Element) imageNodes.item(0);
											Node firstImageTagChild = imageEl.getFirstChild();
											if (firstImageTagChild != null) {
												String imageStr = firstImageTagChild.getNodeValue();
												if (imageStr != null && imageStr.length() > 0) {
													byte[] bytes = convertImageStringToBytes(imageStr);
													ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
													JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(inputStream);
													Image image = decoder.decodeAsBufferedImage();
													inputStream.close();
													if (image != null) {
													    if (sn instanceof PShrimpNode) {
													        ((PShrimpNode)sn).setCustomizedPanelImage(image);
													    }
													}
												}
											}
										}

										displayBean.setPanelMode(sn, panelMode);
										// why twice? Rob Sept 29, 2003
										displayBean.setTransformOf(sn, new AffineTransform(matrix));
										displayBean.setTransformOf(sn, new AffineTransform(matrix));


										// record whether this artifact was focused on
										NodeList els = artifactNode.getElementsByTagName("focus");
										if (els.item(0).getFirstChild().getNodeValue().equalsIgnoreCase("true") && sn != null) {
											focusedOnObjects.add(sn);
										}
									} else {
									    System.err.println("ShrimpSnapShot.loadView: no node for, artifact id=" + artifactId);
									}
								}
							}
						}
					}
					break;
				}
			}

			try {
				project.getApplication().getTool(ShrimpApplication.ARC_FILTER).refresh();
			} catch (Exception e) {
			}
			try {
				project.getApplication().getTool(ShrimpApplication.NODE_FILTER).refresh();
			} catch (Exception e) {
			}


			Rectangle2D.Double coords = null;
			// get the screen coordinates
			NodeList gList = snapShotDOM.getElementsByTagName("text");
			for (int i = 0; i < gList.getLength(); i++) {
				Element el = (Element) gList.item(i);
				if (el.getAttribute(NAME_ATTR).equalsIgnoreCase("screenCoord")) {
					double x = Double.parseDouble(el.getAttribute("x"));
					double y = Double.parseDouble(el.getAttribute("y"));
					double width = Double.parseDouble(el.getAttribute("width"));
					double height = Double.parseDouble(el.getAttribute("height"));
					coords = new Rectangle2D.Double(x, y, width, height);
					break;
				}
			}

			// Pan only if the screencoordinates are not visible
			Rectangle2D bounds = (Rectangle2D) displayBean.getScreenCoordinates();
			if (!bounds.getBounds2D().equals(coords)) {
				Vector screenCoordinates = new Vector(1);
				screenCoordinates.addElement(coords);
				displayBean.focusOnCoordinates(screenCoordinates, true);
			}
			view.navigateToObject(focusedOnObjects);
			displayBean.setVisible(displayVisible);
		} catch (Exception e) {
			System.err.println("Problem loading view: " + e.getMessage());
			e.printStackTrace();
		} finally {
			ApplicationAccessor.defaultCursor();
			displayBean.setVisible(displayVisible);
			displayFilterBean.setFiringEvents(filterBeanFiringEvents);
		}
	}

	private boolean checkCprels(List cprelsOfSnapShot)  {
		boolean cprelOK;
		boolean changeCprel = false;
		List cprelsOfCurrentView = Arrays.asList(((ShrimpView)view).getCprels());

		// if the current hierarchy of the view doesn't match the hierachy stored in the snapshot then
		// check that hierarchy in snapshot is valid for the current project, and if so
		// prompt the user to change
		if (!CollectionUtils.haveSameElements(cprelsOfSnapShot, cprelsOfCurrentView)) {
			Vector allCprelTypes = dataBean.getHierarchicalRelationshipTypes(true, true);
			boolean snapShotCprelIsValid = allCprelTypes.containsAll(cprelsOfSnapShot) || (cprelsOfSnapShot.size() == 0);
			if (snapShotCprelIsValid) {
				Object[] options = { "OK", "Cancel" };
				int optionPicked =
					JOptionPane.showOptionDialog(
						(view != null ? view.getGUI() : null),
						"Warning!  The snapshot's hierarchy doesn't match the current view.\nDo you wish to change the current view's hierarchy?",
						"Warning - Hierarchy Change",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[0]);
				if (optionPicked == JOptionPane.OK_OPTION) {
					cprelOK = true;
					changeCprel = true;
				} else {// doesn't want to proceed.
					cprelOK = false;
				}
			} else {
				String msg = "The current view doesn't support the hierarchy in the snapshot!";
				JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), msg,
						"Hierarchies Not Compatible", JOptionPane.ERROR_MESSAGE);
				cprelOK = false;
			}
		} else {
			cprelOK = true;
		}

		if (cprelOK && changeCprel) {
		    String[] newCprels = new String[cprelsOfSnapShot.size()];
		    cprelsOfSnapShot.toArray(newCprels);
			((ShrimpView)view).setCprels(newCprels, false, false);
		}
		return cprelOK;
	}

	private void loadFilters(FilterBean filterBean, String tag) {
		// read the display filters
		Vector filtersToAdd = new Vector();
		NodeList allGNodes = snapShotDOM.getElementsByTagName("g");
		for (int i = 0; i < allGNodes.getLength(); i++) {
			Element gNode = (Element) allGNodes.item(i);

			if ((gNode.getAttribute(NAME_ATTR) != null) && (gNode.getAttribute(NAME_ATTR).equalsIgnoreCase(tag))) {
				NodeList filterNodes = gNode.getChildNodes();
				for (int j = 0; j < filterNodes.getLength(); j++) {
					Node filterNode = filterNodes.item(j);
					Node text = filterNode.getFirstChild();
					if (text != null) {
						String filterStr = text.getNodeValue();
						Filter newFilter = AttributeFilter.stringToFilter(filterStr);
						if (newFilter != null) {
	                        filtersToAdd.add(newFilter);
						}
					}
				}
				break; // should be only one "filters" tags of given type
			}
		}

		// reset the filterbeans
		Vector currentDisplayFilters = (Vector)filterBean.getFilters().clone();
		try {
            filterBean.removeFilters(currentDisplayFilters);
        } catch (FilterNotFoundException e) {
            e.printStackTrace();
        }
        filterBean.addFilters(filtersToAdd);

	}

	private String createImageString(Image image, Dimension desiredDimension) throws Throwable {
		StringBuffer convertedData = new StringBuffer();

		// add the image to it
		int width = (int) desiredDimension.getWidth();
		int height = (int) desiredDimension.getHeight();

		// create a buffered image
		Image newImage = image.getScaledInstance(width, height, Image.SCALE_FAST);
		BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		bImage.getGraphics().drawImage(newImage, 0, 0, view.getGUI());

		// create a byte array for the image
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(bImage, "jpeg", stream);
		byte[] rawData = stream.toByteArray();
		stream.flush();	// make sure all bytes are written
		bImage.flush();	// release resources
		newImage.flush();	// release resources

		// convert the byte array into a string
		for (int i = 0; i < rawData.length; i++) {
			// left character
			convertedData.append (hexLookUp[(0xf0 & rawData[i]) >>> 4]);
			// right character - together they make a 16-bit number: "ff" or "4a", etc.
			convertedData.append (hexLookUp[(0x0f & rawData[i])]);
		}

		return convertedData.toString();
	}

	/**
	 * This is a short cut to speed up the conversion from image data to hex strings.
	 */
	private static final char[] hexLookUp = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * Literally converts the hex value in the two characters, into a byte.
	 * For example, "f0" would become 11110000.  c1 would be 'f' and c2 would be '0'
	 * @param c1 The high half of the new byte
	 * @param c2 The low half of the new byte
	 * @return The byte created from the two chars.
	 */
	private byte convertCharsToByte(char c1, char c2) {
		int high = convertCharToBits(c1);
		int low = convertCharToBits(c2);
		return (byte)((high << 4) | low);
	}

	/**
	 * Converts the given char into half of a byte.  This method is used by
	 * convertCharsToByte to create each half of the byte.
	 * @param c The character to convert
	 * @return The character in bits.
	 */
	private int convertCharToBits(char c) {
		int result = 0;
		c = Character.toLowerCase(c);
		if (c >= '0' && c <= '9') {
			result = c - '0';
		} else {
			result = c - 'a' + 0xa;
		}
		return result;
	}

	/**
	 * Convert the given string into an array of bytes.  Each two characters
	 * in the string represent a single byte.  For example, "f0" represents
	 * 11110000.
	 * Note: The array of bytes can be reformed into an image using
	 *       JPEGCodec.createJPEGDecoder (new ByteArrayInputStream (byte[]))
	 * @param imageString The image in string form
	 * @return the image in byte[] form
	 */
	private byte[] convertImageStringToBytes(String imageString) {
		byte[] rawData = new byte[imageString.length()/2];
		for (int i = 0; i < rawData.length; i++) {
			rawData[i] = convertCharsToByte(imageString.charAt(i*2), imageString.charAt (i*2+1));
		}
		return rawData;
	}

	/**
	* Loads the image and the userComment.
	* NOTE: This does not take shrimpview to the stored view, it just loads the picture and the userComment.
	*/
	private void loadImageAndComment(Component parent) {
		if (snapShotDOM == null) {
			return;
		}

		try {
			// Load the image
			NodeList imageTags = snapShotDOM.getElementsByTagName("image");
			String imageStr = null;
			if (imageTags.getLength() > 0) {
				Node firstImageTag = imageTags.item(0);
				if (firstImageTag != null) {
					Node firstImageTagChild = firstImageTag.getFirstChild();
					if (firstImageTagChild != null) {
						imageStr = firstImageTagChild.getNodeValue();
					}
				}
			}

			if (imageStr != null) {
				byte[] bytes = convertImageStringToBytes(imageStr);
				ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
				JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(inputStream);
				previewShot = decoder.decodeAsBufferedImage();
				inputStream.close();
				previewShotSize = new Dimension(previewShot.getWidth(parent), previewShot.getHeight(parent));
			} else {
				previewShot = parent.createImage(100,100); // should create a blank image
				previewShotSize = new Dimension(100, 100);
			}
		} catch (Exception e) {
			previewShot = parent.createImage(100,100); // should create a blank image
			previewShotSize = new Dimension(100, 100);
			e.printStackTrace();
		}

		// get the userComment
		NodeList textNodes = snapShotDOM.getElementsByTagName("text");
		for (int i = 0; i < textNodes.getLength(); i++) {
			Node textNode = textNodes.item(i);
			NamedNodeMap attributes = textNode.getAttributes();
			Node nameAttr = attributes.getNamedItem(NAME_ATTR);
			if (nameAttr != null && nameAttr.getNodeValue().equals("userComment")) {
				userComment = textNode.getFirstChild().getNodeValue();
				break;
			}
		}
	}

	public Document getSnapShotDOM() {
		return snapShotDOM;
	}

	private void produceHTML() {
	    try {
		    DOMImplementation impl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
			String htmlNS = "http://www.w3.org/2000/";
			htmlDOM = impl.createDocument(htmlNS, "html", null);

			Element htmlNode = htmlDOM.getDocumentElement();
			Element head = htmlDOM.createElement("head");
			Element title = htmlDOM.createElement("title");
			Element meta = htmlDOM.createElement("meta");
			Element body = htmlDOM.createElement("body");
			Element iframe = htmlDOM.createElement("iframe");

			meta.setAttributeNS(null,"http-equiv","Content-Type");
			meta.setAttributeNS(null,"content","text/html");
			title.appendChild(htmlDOM.createTextNode(project.getTitle() + " | Snapshot"));

			iframe.setAttributeNS(null,"align","left");
			iframe.setAttributeNS(null,"width","1000");
			iframe.setAttributeNS(null,"height","800");
			iframe.setAttributeNS(null,"scrolling","auto");
			iframe.setAttributeNS(null,"frameborder","10");
			iframe.setAttributeNS(null,"marginwidth","10");
			iframe.setAttributeNS(null,"marginheight","10");
			// only for keeping the browser happy.
			iframe.appendChild(htmlDOM.createTextNode(" "));

			htmlNode.appendChild(head);
			head.appendChild(title);
			head.appendChild(meta);

			htmlNode.appendChild(body);
			body.appendChild(iframe);

			Element generalInfo = htmlDOM.createElement("p");
			Element comment = htmlDOM.createElement("p");

			Element prj = htmlDOM.createElement("i");
			Element br1 = htmlDOM.createElement("br");
			Element creator = htmlDOM.createElement("i");
			Element a = htmlDOM.createElement("a");
			Element br2 = htmlDOM.createElement("br");
			Element time = htmlDOM.createElement("i");

			generalInfo.appendChild(prj);
			generalInfo.appendChild(br1);
			generalInfo.appendChild(creator);
			generalInfo.appendChild(br2);
			generalInfo.appendChild(time);

			body.appendChild(generalInfo);

			Element commentTitle = htmlDOM.createElement("Strong");
			Element br3 = htmlDOM.createElement("br");
			body.appendChild(comment);

			String timeInfo = "Created on: ".concat(timeStamp);
			String prjName = "Project name: ".concat(project.getTitle());
			if (USER_EMAIL != null) {
                String mailto = "mailto:".concat(USER_EMAIL);
                a.setAttributeNS(null, "href", mailto);
			}

			a.appendChild(htmlDOM.createTextNode(USER_NAME));

			String userName = "Created by: ";
			prj.appendChild(htmlDOM.createTextNode(prjName));
			creator.appendChild(htmlDOM.createTextNode(userName));
			creator.appendChild(a);
			time.appendChild(htmlDOM.createTextNode(timeInfo));

			commentTitle.appendChild(htmlDOM.createTextNode("Comment:"));
			comment.appendChild(commentTitle);
			comment.appendChild(br3);
			comment.appendChild(htmlDOM.createTextNode(this.getComment()));
	    } catch (Exception e) {
	       System.err.println("ShrimpSnapShot: problem creating html");
	       e.printStackTrace();
	    }
	}

	public Document getHtmlDOM() {
		return htmlDOM;
	}

	public static String askForComments() {
		String comment = null;
		Frame parentFrame = ApplicationAccessor.getParentFrame();
		SnapshotDialog dialog = new SnapshotDialog(parentFrame, true);
		int rv = dialog.showDialog(parentFrame);
		if (rv == SnapshotDialog.OK_OPTION) {
			comment = dialog.getComment();
		}
		return comment;
	}

}