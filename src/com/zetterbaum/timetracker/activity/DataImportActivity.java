package com.zetterbaum.timetracker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.zetterbaum.timetracker.Constants;
import com.zetterbaum.timetracker.FileUtilities;
import com.zetterbaum.timetracker.R;
import com.zetterbaum.timetracker.database.TimeSliceCategoryDBAdapter;
import com.zetterbaum.timetracker.database.TimeSliceDBAdapter;
import com.zetterbaum.timetracker.model.TimeSlice;
import com.zetterbaum.timetracker.model.TimeSliceCategory;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataImportActivity extends Activity {
    private static final String TAG = "EZTimeImport";
    private File mPath;
    private static final long ONE_DAY = 1000L * 60L * 60L * 24L;

    private enum DataSource {EXPORT, TIME_SHEET}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_import);

        FileUtilities fileUtilities = new FileUtilities(this);
        mPath = fileUtilities.getExportDir();
        String[] fileList = loadFileList();
        final Spinner spinner = (Spinner) findViewById(R.id.spinner_data_import_source);
        if (fileList == null || fileList.length == 0) {
            Log.i(TAG, "No files found to import in " + mPath);
            Toast.makeText(getApplicationContext(), "No files found to import in SD card directory: /" + fileUtilities.getExportLocation(), Toast.LENGTH_LONG).show();
            finish();
        } else {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                    android.R.layout.simple_spinner_item, fileList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
        TextView tvImportDirMessage = (TextView) findViewById(R.id.tvCurrentImportDirectory);
        tvImportDirMessage.setText("Current import directory is /" + fileUtilities.getExportLocation() + ". This can be changed from the Settings screen.");
        Button cancelButton = (Button) findViewById(R.id.button_data_import_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button importButton = (Button) findViewById(R.id.button_data_import);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importData(spinner.getSelectedItem().toString());
            }
        });

    }

    private String[] loadFileList() {
        String[] fileList = null;
        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return !sel.isDirectory();
                }
            };
            fileList = mPath.list(filter);
        }

        return fileList;
    }

//    protected Dialog onCreateDialog(int id) {
//        Dialog dialog;
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        switch (id) {
//            case DIALOG_LOAD_FILE:
//                builder.setTitle("Choose file from which to import:");
//                if (mFileList == null) {
//                    Log.e(TAG, "Showing file picker before loading the file list");
//                    dialog = builder.create();
//                    return dialog;
//                }
//                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        importData(mFileList[which]);
//                    }
//                });
//                break;
//        }
//        dialog = builder.show();
//
//        return dialog;
//    }

    private void importData(String inputFileName) {
        FileUtilities fileUtilities = new FileUtilities(this);
        List<String> input = fileUtilities.read(inputFileName);

        DataSource source = DataSource.EXPORT;
        int counter = 0;
        for (String inputLine : input) {
            counter += 1;
            if (counter == 1) { //Header line
                if (inputLine.trim().startsWith(Constants.FROM_DATE.getValue())) {
                    source = DataSource.TIME_SHEET;
                }
            } else {
                try {
                    TimeSlice timeSlice;
                    if (source == DataSource.EXPORT) {
                        timeSlice = processDataExportLine(inputLine);
                    } else {
                        timeSlice = processTimeSheetExportLine(inputLine);
                    }
                    if (timeSlice != null) {
                        TimeSliceDBAdapter dbAdapter = new TimeSliceDBAdapter(this);
                        long newTimeSliceId = dbAdapter.createTimeSlice(timeSlice);
                        lastTimeSlice = timeSlice;
                        lastTimeSlice.setRowId(newTimeSliceId);
                        Log.d(TAG, "Created new timeslice with id:" + newTimeSliceId);
                    }
                } catch (ParseException e) {
                    Log.w(TAG, "Unable to parse import data: " + inputLine, e);
                    Toast.makeText(getApplicationContext(),
                            "Unable to load data: it is not a valid export file.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        Toast.makeText(getApplicationContext(),
                "Data successfully imported.", Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean isAmPm(String time) {
        return time.endsWith("am") || time.endsWith("pm");
    }

    private final SimpleDateFormat dateFormatterAmPm = new SimpleDateFormat("MM/dd/yyyy h:mmaa");
    private final SimpleDateFormat dateFormatter24hr = new SimpleDateFormat("MM/dd/yyyy kk:mm");
    private final SimpleDateFormat dateFormatterTSHeader = new SimpleDateFormat("E, MMMM dd, yyyy");

    private static Date dateCurrentlyProcessing;

    private TimeSliceCategory getTimeSliceCategory(String categoryName) {
        TimeSliceCategoryDBAdapter timeSliceCategoryDBAdapter = new TimeSliceCategoryDBAdapter(this);
        TimeSliceCategory category = timeSliceCategoryDBAdapter.fetchByName(categoryName);
        if ("N/A".equals(category.getCategoryName()) && !"N/A".equals(categoryName)) {
            category = timeSliceCategoryDBAdapter.createTimeSliceCategoryFromName(categoryName);
        }

        return category;
    }

    private TimeSlice lastTimeSlice;

    private TimeSlice processTimeSheetExportLine(String inputLine) throws ParseException {
        if (inputLine.startsWith("    ")) {
            lastTimeSlice.setNotes(inputLine.trim());
//            Log.d(TAG,"Got note: " + lastTimeSlice.getNotes() + "for timeslice " + lastTimeSlice.getRowId());
            TimeSliceDBAdapter dbAdapter = new TimeSliceDBAdapter(this);
            dbAdapter.updateTimeSlice(lastTimeSlice);
            return null;
        } else if (inputLine.startsWith("  ")) {
            TimeSlice timeSlice = new TimeSlice();
            Log.d(TAG, "Currently processing line " + inputLine);
            final Calendar c = Calendar.getInstance();
            c.setTime(dateCurrentlyProcessing);
            String[] splitString = inputLine.trim().split(":");
            timeSlice.setCategory(getTimeSliceCategory(splitString[0].trim()));
//            Log.d(TAG,"timeSlice category=" + timeSlice.getCategory());
            c.add(Calendar.HOUR, Integer.parseInt(splitString[1].trim()));
//            Log.d(TAG,"ampm:" + splitString[2].trim().substring(2));
            if (splitString[2].trim().substring(2, 4).equals("pm")) {
                c.add(Calendar.HOUR, 12);
            }
            int minuteComponent = Integer.parseInt(splitString[2].substring(0,2));
            c.add(Calendar.MINUTE, minuteComponent);
//            Log.d(TAG, "Start time: " + c.getTime());
            timeSlice.setStartTime(c.getTimeInMillis());
            splitString = inputLine.trim().split("\\(")[1].split(":");
            c.add(Calendar.HOUR, Integer.parseInt(splitString[0]));
            c.add(Calendar.MINUTE, Integer.parseInt(splitString[1]));
            String secondsStr = splitString[2].replace(")", "").trim();
            if (!"00".equals(secondsStr)) {
                c.add(Calendar.SECOND, Integer.parseInt(secondsStr));
            }
            timeSlice.setEndTime(c.getTimeInMillis());
            return timeSlice;
        } else {
            if (!inputLine.trim().startsWith(Constants.TO_DATE.getValue())) {
                dateCurrentlyProcessing = dateFormatterTSHeader.parse(inputLine);
            }
        }
        return null;
    }

    private TimeSlice processDataExportLine(String inputLine) throws ParseException {
        String[] values = inputLine.split(",");
        TimeSlice timeSlice = new TimeSlice();
        SimpleDateFormat dateFormatter = dateFormatter24hr;
        if (isAmPm(values[1])) {
            dateFormatter = dateFormatterAmPm;

        }
        String startDateStr = values[0] + " " + values[1];
        long startDate = dateFormatter.parse(startDateStr).getTime();

        timeSlice.setStartTime(startDate);
        String endDateStr = values[0] + " " + values[2];
        long endDate = dateFormatter.parse(endDateStr).getTime();
        if (endDate < startDate) {
            endDate = endDate + ONE_DAY;
        }
        timeSlice.setEndTime(endDate);
        TimeSliceCategory category = getTimeSliceCategory(values[3]);
        timeSlice.setCategory(category);
        if (values.length > 5) {
            timeSlice.setNotes(values[5]);
        }
        return timeSlice;
    }

}
