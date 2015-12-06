/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * String Matcher class.
 *
 * @author Jingwei Wu
 */
public class Matcher {

	/**
	 * A tag indicating the search pattern is a string
	 * not a regular expression.
	 */
	public static final int CONTAINS_MODE = 0;

	/**
	 * A tag indicating the search pattern is a string and must match exactly.
	 */
	public static final int EXACT_MATCH_MODE = 1;

	/**
	 * A tag indicating the search pattern is a string and must match the start of the string.
	 */
	public static final int STARTS_WITH_MODE = 2;

	/**
	 * A tag indicating the search pattern is a string and must match the end of the string.
	 */
	public static final int ENDS_WITH_MODE = 3;

	/**
	 * A tag indicating the search pattern is a regular
	 * expression not a string.
	 */
	public static final int REGEXP_MODE = 4;

	/**
	 * Tests if the specfied string <code>s</code> is an exact match of the specified search
	 * <code>pattern</code>, which could be either a regular expression or an exact string.
	 * @param s the string to be tested.
	 * @param pattern the search pattern.
	 * @param mode the mode for this testing.
	 * It has to be one of: {@link #CONTAINS_MODE}, {@link #REGEXP_MODE}, or {@link #EXACT_MATCH_MODE}.
	 * @param ignoreCase indicating if this test should ignore case.
	 * If it is <code>true</code>, the test will be case insensitive.
	 */
	public static boolean match(String s, String pattern, int mode, boolean ignoreCase) {
		// No need to do the search.
		if (s == null || pattern == null) {
			return false;
		}

		String s1 = ignoreCase ? s.toLowerCase() : s;
		String s2 = ignoreCase ? pattern.toLowerCase() : pattern;
		switch (mode) {
			case CONTAINS_MODE :
				return s1.indexOf(s2) != -1;
			case EXACT_MATCH_MODE :
				return s1.equals(s2);
			case STARTS_WITH_MODE :
				return s1.startsWith(s2);
			case ENDS_WITH_MODE :
				return s1.endsWith(s2);
			case REGEXP_MODE :
				try {
			        Pattern regexPattern = ignoreCase ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
			        java.util.regex.Matcher regexMatcher = regexPattern.matcher(s);
			        return regexMatcher.matches();
				} catch (PatternSyntaxException e) {
	    		    String message = "Sorry, the regular expression entered is not a valid pattern.\n\n" + e.getMessage();
	    		    JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), message,
	    		    		ApplicationAccessor.getAppName(), JOptionPane.ERROR_MESSAGE);
	                e.printStackTrace();
	            }
		}
		return false;
	}

}
