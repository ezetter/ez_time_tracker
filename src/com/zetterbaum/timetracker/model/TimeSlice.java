package com.zetterbaum.timetracker.model;

import android.text.format.DateFormat;
import com.zetterbaum.timetracker.DateTimeFormatter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeSlice implements Serializable {
	private static final long serialVersionUID = 6586305797483181442L;

	private long rowId;

	private long startTime = 0;

	private long endTime = 0;

	private TimeSliceCategory category;

	private String notes;

	private static Calendar calendar = new GregorianCalendar();

	public static final long MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;

	public static final int IS_NEW_TIMESLICE = -1;

	public long getRowId() {
		return rowId;
	}

	public void setRowId(long rowId) {
		this.rowId = rowId;
	}

	public long getDurationInMilliseconds() {
		return endTime - startTime;
	}

	public String getStartDateStr() {
		if (startTime == 0) {
			return "";
		} else {
			return DateFormat.format("E, MMMM dd, yyyy", startTime).toString();
		}
	}

	public String getStartMonthStr() {
		if (startTime == 0) {
			return "";
		} else {
			return DateFormat.format("MMMM, yyyy", startTime).toString();
		}
	}

	public String getStartWeekStr() {
		if (startTime == 0) {
			return "";
		} else {
			long firstDayOfWeekDate = startTime;
			CharSequence dayOfWeek = DateFormat.format("E", startTime);
			if (dayOfWeek.equals("Mon")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY;
			} else if (dayOfWeek.equals("Tue")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 2;
			} else if (dayOfWeek.equals("Wed")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 3;
			} else if (dayOfWeek.equals("Thu")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 4;
			} else if (dayOfWeek.equals("Fri")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 5;
			} else if (dayOfWeek.equals("Sat")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 6;
			}
			return "Week of " + DateFormat.format("MMMM dd, yyyy", firstDayOfWeekDate).toString();
		}
	}

	public int getStartTimeComponent(int componentId) {
		calendar.setTimeInMillis(startTime);
		return calendar.get(componentId);
	}

	public void setStartTimeComponent(int componentId, int value) {
		calendar.setTimeInMillis(startTime);
		calendar.set(componentId, value);
		startTime = calendar.getTimeInMillis();
	}

	public int getEndTimeComponent(int componentId) {
		calendar.setTimeInMillis(endTime);
		return calendar.get(componentId);
	}

	public void setEndTimeComponent(int componentId, int value) {
		calendar.setTimeInMillis(endTime);
		calendar.set(componentId, value);
		endTime = calendar.getTimeInMillis();
	}

	public String getStartTimeStr() {
		if (startTime == 0) {
			return "";
		} else {
			return DateTimeFormatter.formatTimePerCurrentSettings(startTime);
		}
	}

	public String getEndTimeStr() {
		if (startTime == 0) {
			return "";
		} else {
			return DateTimeFormatter.formatTimePerCurrentSettings(endTime);
		}
	}

	public TimeSliceCategory getCategory() {
		return category;
	}

	public void setCategory(TimeSliceCategory category) {
		this.category = category;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getTitleWithDuration() {
		return getCategory().getCategoryName() + ": " + getStartTimeStr() + " - " + getEndTimeStr()
				+ " (" + DateTimeFormatter.hrColMinColSec(getDurationInMilliseconds(), true) + ")";
	}

	public String getTitle() {
		return getCategory().getCategoryName() + ": " + getStartTimeStr() + " - " + getEndTimeStr();
	}

	public String getNotes() {
		if (notes != null) {
			return notes;
		} else {
			return "";
		}
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

}
