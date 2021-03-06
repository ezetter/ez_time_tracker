package com.zetterbaum.timetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import com.zetterbaum.timetracker.model.TimeSlice;
import com.zetterbaum.timetracker.model.TimeSliceCategory;

import java.util.ArrayList;
import java.util.List;

public class TimeSliceDBAdapter {
	private static TimeSliceDBAdapter timeSliceDBAdapterSingleton;
	private final TimeSliceCategoryDBAdapter categoryDBAdapter;

	public TimeSliceDBAdapter(Context context) {
		DatabaseInstance.open();
		categoryDBAdapter = new TimeSliceCategoryDBAdapter(context);
	}

	public static TimeSliceDBAdapter getTimeSliceDBAdapter(Context context) {
		DatabaseInstance.open();
		if(timeSliceDBAdapterSingleton == null) {
			timeSliceDBAdapterSingleton = new TimeSliceDBAdapter(context);
		}
		return timeSliceDBAdapterSingleton;
	}

	public long createTimeSlice(final TimeSlice timeSlice) {
		return DatabaseInstance.getDb().insert(DatabaseHelper.TIME_SLICE_TABLE,
				null, timeSliceContentValuesList(timeSlice));
	}

	public long updateTimeSlice(final TimeSlice timeSlice) {
		return DatabaseInstance.getDb().update(DatabaseHelper.TIME_SLICE_TABLE,
				timeSliceContentValuesList(timeSlice),"_id = " + timeSlice.getRowId(), null);
	}

	public boolean delete(final long rowId) {
        return DatabaseInstance.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE,  "_id=" + rowId, null) > 0;
    }

	public boolean deleteForDateRange(long startDate, long endDate) {
		return DatabaseInstance.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE,
				"start_time>=" + startDate + " and start_time <=" + endDate, null) > 0;
	}

	public boolean deleteAll() {
		return DatabaseInstance.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE, null, null) > 0;
	}

    public TimeSlice fetchByRowID(final long rowId) throws SQLException {
		if (!DatabaseInstance.getDb().isOpen()) {
			DatabaseInstance.open();
		}
		Cursor cur = DatabaseInstance.getDb().query(true,
				DatabaseHelper.TIME_SLICE_TABLE, columnList(),
				"_id=" + rowId, null, null, null, null, null);
		if (cur != null) {
			cur.moveToFirst();
		}
		return fillTimeSliceFromCursor(cur);
	}

    public boolean categoryHasTimeSlices(final TimeSliceCategory category) throws SQLException {
		Cursor cur = DatabaseInstance.getDb().query(true,
				DatabaseHelper.TIME_SLICE_TABLE, columnList(),
				"category_id=" + category.getRowId(), null, null, null, null, null);
		if (cur.moveToNext()) {
			return true;
		}
		return false;
	}

    public List<TimeSlice> fetchAllTimeSlices() {
		List<TimeSlice> result = new ArrayList<TimeSlice>();
		Cursor cur = DatabaseInstance.getDb().query(
				DatabaseHelper.TIME_SLICE_TABLE, columnList(), null, null,
				null, null, null);
		while (cur.moveToNext()) {
			TimeSlice ts = this.fillTimeSliceFromCursor(cur);
			result.add(ts);
		}
		return result;
	}

	public List<TimeSlice> fetchTimeSlicesByDateRange(long startDate, long endDate) {
		List<TimeSlice> result = new ArrayList<TimeSlice>();
		Cursor cur = DatabaseInstance.getDb().query(
				DatabaseHelper.TIME_SLICE_TABLE, columnList(),
				"start_time >= " + startDate + " and end_time <= " + endDate
				, null,
				null, null, "start_time");
		while (cur.moveToNext()) {
			TimeSlice ts = this.fillTimeSliceFromCursor(cur);
			result.add(ts);
		}
		return result;
	}

	private TimeSlice fillTimeSliceFromCursor(Cursor cur) {
		TimeSlice ts = new TimeSlice();
		ts.setRowId(cur.getInt(cur.getColumnIndexOrThrow("_id")));
		ts.setStartTime(cur
				.getLong(cur.getColumnIndexOrThrow("start_time")));
		ts.setEndTime(cur.getLong(cur.getColumnIndexOrThrow("end_time")));
		ts.setCategory(categoryDBAdapter.fetchByRowID(cur.getInt(cur
				.getColumnIndexOrThrow("category_id"))));
		ts.setNotes(cur.getString(cur.getColumnIndexOrThrow("notes")));
		return ts;
	}

	private String[] columnList() {
		List<String> columns = new ArrayList<String>();
		columns.add("_id");
		columns.add("category_id");
		columns.add("start_time");
		columns.add("end_time");
		columns.add("notes");
		return columns.toArray(new String[0]);
	}

	private ContentValues timeSliceContentValuesList(final TimeSlice timeSlice) {
		ContentValues values = new ContentValues();
		values.put("category_id", timeSlice.getCategory().getRowId());
		values.put("start_time", timeSlice.getStartTime());
		values.put("end_time", timeSlice.getEndTime());
		values.put("notes", timeSlice.getNotes());
		return values;
	}

}
