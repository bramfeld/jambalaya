/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;


/**
 * Holds the result of a query.
 * Indicates if the query was cancelled, or if an error occurred.
 * If the query completed successfully then the number of matching
 * nodes as well as the arcs and neighbours are saved.
 *
 * @author Chris Callendar
 * @date 6-Dec-06
 */
public class SearchResult {

	private int nodes;
	private int arcs;
	private int neighbors;
	private boolean error;
	private String message;
	private boolean cancelled;

	public SearchResult() {
		nodes = 0;
		arcs = 0;
		neighbors = 0;
		error = false;
		message = "";
		cancelled = false;
	}

	public boolean hasResults() {
		return (nodes > 0);
	}

	public String toString() {
		if (isCancelled()) {
			return "Query cancelled ";
		} else if (isError()) {
			return getMessage();
		} else if (hasResults()) {
			String str = "Found " + nodes + (nodes == 1 ? " node" : " nodes");
			if (arcs > 0) {
				str += " and " + arcs + (arcs == 1 ? " arc" : " arcs");
			}
			return str;
		}
		return "Sorry, no matching nodes found ";
	}

	public int getArcs() {
		return arcs;
	}

	public void setArcs(int arcs) {
		this.arcs = arcs;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public int getNodes() {
		return nodes;
	}

	public void setNodes(int matchingNodes) {
		this.nodes = matchingNodes;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessage(String message, boolean error, boolean cancelled) {
		this.message = message;
		setError(error);
		setCancelled(cancelled);
	}

	public int getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(int neighbors) {
		this.neighbors = neighbors;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static SearchResult cancel(String message) {
		SearchResult result = new SearchResult();
		result.setCancelled(true);
		result.setMessage(message);
		return result;
	}

	public static SearchResult error(String message) {
		SearchResult result = new SearchResult();
		result.setError(true);
		result.setMessage(message);
		return result;
	}

}
