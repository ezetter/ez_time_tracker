package com.zetterbaum.timetracker.report;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.zetterbaum.timetracker.FileUtilities;
import com.zetterbaum.timetracker.R;

import java.io.Writer;

public class SDDataExporter {
    private Writer writer;
    private Context context;
    ReportOutput output;
    private String defaultName;

    private SDDataExporter() {

    }

    private void buildSaveFileDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.choose_file_name);
        dialog.setTitle("Choose a File Name:");
        final Button saveButton = (Button) dialog.findViewById(R.id.choose_file_name_save_button);
        final Button cancelButton = (Button) dialog
                .findViewById(R.id.choose_file_name_cancel_button);
        final EditText fileNameField = (EditText) dialog
                .findViewById(R.id.choose_file_name_edit_field);
        fileNameField.setText(defaultName);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                export(fileNameField.getText().toString());
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public static void exportToSD(String defaultName, Context context, ReportOutput output) {
        SDDataExporter exporter = new SDDataExporter();
        exporter.context = context;
        exporter.defaultName = defaultName;
        exporter.output = output;
        exporter.buildSaveFileDialog();
    }

    void export(String fileName) {
//		try {
//			String outputPath = open(fileName);
//			saveData();
        FileUtilities fileUtilities = new FileUtilities(context);
        boolean success = fileUtilities.write(fileName, output.getOutput());
//        if (success) {
//            Toast.makeText(context.getApplicationContext(),
//                    "Report successfully saved to: " + fileUtilities.getExportLocation(), Toast.LENGTH_LONG).show();
//        }
//		} catch (IOException e) {
//			Log.w("EZTimeTracker", e.getMessage(), e);
//			Toast.makeText(context.getApplicationContext(),
//					e.getMessage() + " Unable to export to SD.", Toast.LENGTH_LONG).show();
//		}
    }

//	private String open(String fileName) throws IOException {
//		File root = Environment.getExternalStorageDirectory();
//        FileUtilities fileUtilities = new FileUtilities(context);
//		File outDir = fileUtilities.getExportDirAndMakeIfNecessary();
////                = new File(root.getAbsolutePath() + File.report_separator + "EZ_time_tracker");
//		if (!outDir.isDirectory()) {
//			outDir.mkdir();
//		}
//		if (!outDir.isDirectory()) {
//			throw new IOException(
//					"Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
//		}
//		File outputFile = new File(outDir, fileName);
//		writer = new BufferedWriter(new FileWriter(outputFile));
//		return outputFile.getAbsolutePath();
//	}

//	private void saveData() throws IOException {
//		writer.write(output.getOutput());
//		writer.close();
//	}

}
