package com.zetterbaum.timetracker;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtilities {
    private final Context context;
    private final static String DEFAULT_EXPORT_LOCATION = "EZ_time_tracker";
    private final static String EXPORT_LOCATION_PREF_KEY = "external_storage_loc";

    public FileUtilities(Context context) {
        super();
        this.context = context;
    }

    public String getExportLocation() {
        return Settings.getOrInitializeSetting(context, EXPORT_LOCATION_PREF_KEY, DEFAULT_EXPORT_LOCATION);
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String pref = prefs.getString("external_storage_loc", "unset");
//        if ("unset".equals(pref)) {
//            return DEFAULT_EXPORT_LOCATION;
//        } else {
//            return pref;
//        }
    }

    public File getExportDir() {
        File root = Environment.getExternalStorageDirectory();
        return new File(root.getAbsolutePath() + File.separator + getExportLocation());
    }

    public File getExportDirAndMakeIfNecessary() throws IOException {
        File outDir = getExportDir();
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        if (!outDir.isDirectory()) {
            throw new IOException(
                    "Unable to create directory " + getExportLocation() + ". Maybe the SD card is mounted?");
        }
        return outDir;
    }

    public List<String> read(String fileName) {
        List<String> input = new ArrayList<String>();
        File dir = getExportDir();
        File inputFile = new File(dir, fileName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line = reader.readLine();
            while (line != null) {
                input.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e("EZTimeTracker", "Error reading input file.", e);
        }
        return input;
    }

    public boolean write(String fileName, String data) {
        boolean result = false;
        try {
            File outDir = getExportDirAndMakeIfNecessary();
            File outputFile = new File(outDir, fileName);
            Writer writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(data);
            result = true;
            Toast.makeText(context.getApplicationContext(),
                    "Report successfully saved to: " + outputFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
            writer.close();
        } catch (IOException e) {
            Log.w("EZTimeTracker", e.getMessage(), e);
            Toast.makeText(context, e.getMessage() + " Unable to write to external storage.",
                    Toast.LENGTH_LONG).show();
        }
        return result;
    }


}
