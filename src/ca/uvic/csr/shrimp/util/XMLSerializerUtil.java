/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.io.OutputStream;

import org.w3c.dom.Document;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


/**
 * An alternate way of serializing a {@link Document} to XML.
 * In Java 6 using the Xerces Transformer class causes an AbstractMethodError,
 * so this is a workaround.
 *
 * @author Chris Callendar
 * @date 2008-12-01
 */
public class XMLSerializerUtil {

	/**
	 * An alternate way of serializing to XML.
	 * In Java 6 using the Xerces Transformer class causes an AbstractMethodError,
	 * so this is a workaround.
	 *
	 * @param document
	 * @param file
	 */
	public static void serialize(Document document, OutputStream outputStream) {
		try {
			XMLSerializer serializer = new XMLSerializer();
			serializer.setOutputByteStream(outputStream);
			OutputFormat format = new OutputFormat(document);
			format.setIndenting(false);
			serializer.setOutputFormat(format);
			serializer.serialize(document);
		} catch (Throwable t) {
			System.err.println(ApplicationAccessor.getAppName() + " - alternate xml serialization failed.");
			t.printStackTrace();
		}
	}

}
