package com.zetterbaum.timetracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.*;
import com.zetterbaum.timetracker.DateTimeFormatter;
import com.zetterbaum.timetracker.R;
import com.zetterbaum.timetracker.database.DatabaseInstance;
import com.zetterbaum.timetracker.database.TimeSliceDBAdapter;
import com.zetterbaum.timetracker.model.TimeSlice;
import com.zetterbaum.timetracker.report.ReportInterface;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyright 2010 Eric Zetterbaum ezetter@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
public class TimeSheetReportActivity extends Activity implements ReportInterface {
	private static final int EDIT_MENU_ID = Menu.FIRST;
	private static final int DELETE_MENU_ID = Menu.FIRST + 1;
	private static final int ADD_MENU_ID = Menu.FIRST + 2;
	private static final int SHOW_DESC_MENU_ID = Menu.FIRST + 3;
	private TimeSliceDBAdapter mTimeSliceDBAdapter;
	private LinearLayout mMainLayout;
	private final Map<View, Long> mRowToSliceRowIdMap = new HashMap<View, Long>();
	private long mChosenRowId;
	private ReportFramework mReportFramework;
	private List<TextView> mReportViewList;
	private String mDateSelectedForAdd;
	private boolean mShowNotes = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        DatabaseInstance.initialize(this);
		DatabaseInstance.open();
		mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		mReportFramework = new ReportFramework(this, this);
		if (savedInstanceState != null) {
			mReportFramework.setStartDateRange(savedInstanceState.getLong("StartDateRange"));
			mReportFramework.setEndDateRange(savedInstanceState.getLong("EndDateRange"));
		}
		loadDataIntoReport();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		DatabaseInstance.open();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("StartDateRange", mReportFramework.getStartDateRange());
		outState.putLong("EndDateRange", mReportFramework.getEndDateRange());
	}

	public void loadDataIntoReport() {
		initScrollview();
		String lastStartDate = "";
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(mReportFramework.getEndDateRange()));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		long endDate = c.getTimeInMillis();

		List<TimeSlice> timeSlices = mTimeSliceDBAdapter.fetchTimeSlicesByDateRange(
				mReportFramework.getStartDateRange(), endDate);
		int count = 0;
		for (TimeSlice aSlice : timeSlices) {
			if (!lastStartDate.equals(aSlice.getStartDateStr())) {
				lastStartDate = aSlice.getStartDateStr();
				addDateHeaderLine(lastStartDate);
			}
			addTimeSliceLine(aSlice);
			count++;
		}
		initialScrollToEnd();
	}

	private void initScrollview() {
		setContentView(mReportFramework.buildViews());
		mMainLayout = new LinearLayout(this);
		mMainLayout.setOrientation(LinearLayout.VERTICAL);
		mReportFramework.getLinearScroller().addView(mMainLayout);
		mReportViewList = mReportFramework.initializeTextViewsForExportList();

    }

	/**
	 * Apparently one needs to wait for the UI to "settle" before the scroll
	 * will work. post seems to do the trick.
	 */
	private void initialScrollToEnd() {
		mReportFramework.getLinearScroller().getScrollView().post(new Runnable() {
			public void run() {
				mReportFramework.getLinearScroller().getScrollView().fullScroll(
						ScrollView.FOCUS_DOWN);
			}
		});
	}

	private void addTimeSliceLine(TimeSlice aSlice) {
		TextView sliceReportLine = new TextView(this);
		mRowToSliceRowIdMap.put(sliceReportLine, aSlice.getRowId());
		StringBuilder sliceReportText = new StringBuilder();
		sliceReportText.append("  ").append(aSlice.getTitleWithDuration());
		int lineOneEnd = sliceReportText.length();
		boolean showNotes = (mShowNotes && aSlice.getNotes() != null && aSlice.getNotes().length() > 0);
		if (showNotes) {
			sliceReportText.append("\n").append("    ").append(aSlice.getNotes());
		}
		sliceReportLine.setText(sliceReportText.toString(), TextView.BufferType.SPANNABLE);
		if (showNotes) {
			Spannable str = (Spannable) sliceReportLine.getText();
			str.setSpan(new ForegroundColorSpan(android.graphics.Color.GRAY), lineOneEnd, str
					.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		registerForContextMenu(sliceReportLine);
		sliceReportLine.setTag("Detail");
		mMainLayout.addView(sliceReportLine);
        mReportFramework.addSeparator(mMainLayout);

        mReportViewList.add(sliceReportLine);
	}

    private TextView addDateHeaderLine(String dateText) {
		TextView startDateLine = new TextView(this);
		startDateLine.setText(dateText);
		startDateLine.setTextColor(Color.GREEN);
		registerForContextMenu(startDateLine);
		startDateLine.setTag("Header");
		mMainLayout.addView(startDateLine);
        mReportFramework.addSeparator(mMainLayout);
        mReportViewList.add(startDateLine);
		startDateLine.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					((TextView) view).setTextColor(Color.rgb(0, 128, 0));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					((TextView) view).setTextColor(Color.GREEN);
				}
				return false;
			}
		});
		return startDateLine;
	}

	private void showTimeSliceEditDialog(long rowId, long date) {
		Intent i = new Intent(this, TimeSliceEditActivity.class);
		i.putExtra("row_id", rowId);
		i.putExtra("date", date);
		startActivityForResult(i, 1);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getTag().equals("Detail")) {
			menu.add(0, EDIT_MENU_ID, 0, getString(R.string.menu_text_edit));
			menu.add(0, DELETE_MENU_ID, 0, getString(R.string.menu_text_delete));
			mChosenRowId = mRowToSliceRowIdMap.get(v);
		} else if (v.getTag().equals("Header")) {
			menu.add(0, ADD_MENU_ID, 0, getString(R.string.menu_text_add_new_activity));
			mDateSelectedForAdd = (String) ((TextView) v).getText();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			Bundle data = intent.getBundleExtra("data");
			if (data != null) {
				TimeSlice updatedTimeSlice = (TimeSlice) data.getSerializable("time_slice");
				if (updatedTimeSlice.getRowId() == TimeSlice.IS_NEW_TIMESLICE) {
					mTimeSliceDBAdapter.createTimeSlice(updatedTimeSlice);
				} else {
					mTimeSliceDBAdapter.updateTimeSlice(updatedTimeSlice);
				}
				loadDataIntoReport();
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_MENU_ID:
			showTimeSliceEditDialog(mChosenRowId, 0);
			return true;
		case DELETE_MENU_ID:
			buildDeleteDialog();
			return true;
		case ADD_MENU_ID:
			DateFormat format = new SimpleDateFormat("E, MMMM dd, yyyy");
			try {
				showTimeSliceEditDialog(TimeSlice.IS_NEW_TIMESLICE, format.parse(
						mDateSelectedForAdd).getTime());
			} catch (ParseException e) {
				// This WILL parse.
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void buildDeleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TimeSlice ts = TimeSliceDBAdapter.getTimeSliceDBAdapter(this).fetchByRowID(mChosenRowId);
		builder.setTitle(ts.getTitle());
		builder.setMessage(getString(R.string.delete_interval_confirmation)).setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mTimeSliceDBAdapter.delete(mChosenRowId);
						loadDataIntoReport();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(0, ADD_MENU_ID, 0, getString(R.string.menu_text_add_new_activity));
		if (mShowNotes) {
			menu.add(0, SHOW_DESC_MENU_ID, 0, getString(R.string.menu_text_hide_notes));
		} else {
			menu.add(0, SHOW_DESC_MENU_ID, 0, getString(R.string.menu_text_show_notes));
		}
		mReportFramework.onPrepareOptionsMenu(menu);

		return result;
	}

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			final Calendar c = DateTimeFormatter.getCalendar(year, monthOfYear, dayOfMonth);

			showTimeSliceEditDialog(TimeSlice.IS_NEW_TIMESLICE, c.getTimeInMillis());
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_MENU_ID:
			Calendar c = Calendar.getInstance();
			DatePickerDialog dialog = new DatePickerDialog(this, mDateSetListener, c
					.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			dialog.setTitle(getString(R.string.choose_date));
			dialog.show();
			break;
		case SHOW_DESC_MENU_ID:
			if (mShowNotes) {
				mShowNotes = false;
			} else {
				mShowNotes = true;
			}
			loadDataIntoReport();
			break;
		default:
			mReportFramework.setReportType(ReportFramework.ReportTypes.TIMESHEET);
			mReportFramework.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		mReportFramework.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return mReportFramework.onCreateDialog(id);
	}

}
