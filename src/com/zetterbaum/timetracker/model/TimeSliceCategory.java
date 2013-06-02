package com.zetterbaum.timetracker.model;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.zetterbaum.timetracker.database.TimeSliceCategoryDBAdapter;

import java.io.Serializable;

public class TimeSliceCategory implements Serializable, Comparable<TimeSliceCategory>{
	private static final long serialVersionUID = 4899523432240132519L;

	private long rowId;

	private String categoryName;

	private String description;


	public long getRowId() {
		return rowId;
	}

	public void setRowId(long rowId) {
		this.rowId = rowId;
	}

	public String getCategoryName() {
		if(categoryName == null) {
			return "N/A";
		}
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		if(description == null) {
			return "";
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return categoryName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categoryName == null) ? 0 : categoryName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeSliceCategory other = (TimeSliceCategory) obj;
		if (categoryName == null) {
			if (other.categoryName != null)
				return false;
		} else if (!categoryName.equals(other.categoryName))
			return false;
		return true;
	}

	public static ArrayAdapter<TimeSliceCategory> getCategoryAdapter(Context context) {
		TimeSliceCategoryDBAdapter timeSliceCategoryDBAdapter = new TimeSliceCategoryDBAdapter(context);

		TimeSliceCategory[] timeSliceCategories = timeSliceCategoryDBAdapter
				.fetchAllTimeSliceCategories()
				.toArray(new TimeSliceCategory[0]);
		return new ArrayAdapter<TimeSliceCategory>(
				context, android.R.layout.simple_spinner_item, timeSliceCategories);

	}

    public static TimeSliceCategory getTimeSliceCategoryForName(Context context, String name) {
        TimeSliceCategoryDBAdapter timeSliceCategoryDBAdapter = new TimeSliceCategoryDBAdapter(context);
        return timeSliceCategoryDBAdapter.fetchByName(name);
    }

	@Override
	public int compareTo(TimeSliceCategory anotherTimeSliceCategory) {
		return this.categoryName.compareTo(anotherTimeSliceCategory.categoryName);
	}


}
