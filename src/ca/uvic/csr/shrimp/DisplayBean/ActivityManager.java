/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivityScheduler;

/**
 * Schedules and managers activities (listens for completion).
 * @author Rob Lintern
 * @author Chris Callendar
 */
public class ActivityManager implements PActivity.PActivityDelegate {
	private boolean activitiesFinished;
	private HashSet activitiesSet;
	
	/** Schedules one activity. */
	public ActivityManager(PActivityScheduler scheduler, PActivity activity) {
		activitiesFinished = false;
		activitiesSet = new HashSet(1);
		activitiesSet.add(activity);
		activity.setDelegate(this);
		scheduler.addActivity(activity, true);
	}

	/** Holds multiple activities which have already been added to a scheduler. */
	public ActivityManager(Collection activities) {
		this(null, activities);
	}
	
	/**
	 * Schedules multiple activities.  If the scheduler is null the activities will
	 * not be scheduled.
	 * @param scheduler the scheduler, can be null
	 */
	public ActivityManager(PActivityScheduler scheduler, Collection activities) {
		activitiesFinished = (activities.size() == 0);
		activitiesSet = new HashSet(activities.size());
		activitiesSet.addAll(activities);
		for (Iterator iter = activities.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof PActivity) {
				PActivity activity = (PActivity)obj;
				activity.setDelegate(this);
				if (scheduler != null) {
					scheduler.addActivity(activity, true);
				}
			}
		}
	}
	
	public void activityStarted(PActivity activity) {
		activitiesFinished = false;
		if (!activitiesSet.contains(activity)) {
			activitiesSet.add(activity);
		}
	}

	public void activityStepped(PActivity activity) {}

	public void activityFinished(PActivity activity) {
		boolean removed = activitiesSet.remove(activity);
		// if all activities have finished then we are done
		if (removed && (activitiesSet.size() == 0)) {
			activitiesFinished = true;
		}
	}

	/** If all the activities have finished. */
	public boolean isFinished () {
		return activitiesFinished;
	}
	
	public void setFinished(boolean activitiesFinished) {
		this.activitiesFinished = activitiesFinished;
	}
	
	/**
	 * Sleeps until all the activities have finished.
	 */
	public void waitUntilFinished() {
		while (!isFinished()) {
			try {
				Thread.sleep(20);	// default Piccolo animation step
			} catch (InterruptedException ignore) {}
		}
	}
	
}
