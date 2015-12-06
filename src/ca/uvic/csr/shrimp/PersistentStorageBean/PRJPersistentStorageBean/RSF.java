/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.io.IOException;
import java.io.StreamTokenizer;

/**
 * Common code for the various flavours of RSF.
 * @author Derek Rayside
 */

public abstract class RSF {

	/** the storage bean */
	protected PRJPersistentStorageBean storageBean;

	/** The .prj file containing the desired data */
	protected PRJFile prjFile;

	/** Simple constructor to make reflective instantiation easier. */
	public RSF() {
		super();
	}

	protected void setStorageBean(final PRJPersistentStorageBean r) {
		this.storageBean = r;
	}

	protected void setPRJFile(final PRJFile f) {
		this.prjFile = f;
	}

	/**
	 * Reads the relational information from the .rsf file and
	 * calls appropriate method to interpret that data.
	 * @see RSFFileInterpreter#processNodeDatd(String)
	 * @see RSFFileInterpreter#processArcData(String)
	 */
	protected abstract void extract() throws IOException;

	// need to configure the syntax of this tokenizer properly
	// the big issue is that we want to parse id's as strings instead of numbers

	protected static void setSyntax(StreamTokenizer t) {
		t.resetSyntax();
		// standard syntax; see StreamTokenizer()
		t.quoteChar('"'); // character 34
		t.wordChars('a', 'z'); // characters 97 through 122
		t.wordChars('A', 'Z'); // characters 65 through 90
		t.wordChars(128 + 32, 255);
		t.whitespaceChars(0, ' '); // characters 0 through 32
		// our custom syntax; 
		// there are no distinguished characters except whitespace
		t.wordChars('0', '9'); // characters 48 through 57
		t.wordChars('!', '!'); // character 33
		t.wordChars('$', '/'); // characters 36 through 47
		t.wordChars(':', '@'); // characters 58 through 64
		t.wordChars('[', '`'); // characters 91 through 96
		t.commentChar('#'); // character 35
		t.wordChars('{', '~'); // characters 123 through 126
		// some further additions
		t.eolIsSignificant(true);
		t.slashStarComments(true);
		t.slashSlashComments(true);
	}

}
