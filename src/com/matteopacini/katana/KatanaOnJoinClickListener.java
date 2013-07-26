////////////////////////////////////////////////////////////////////////////
//                                                                        //
//  This file is part of Katana.                                          //
//                                                                        //
//  Katana is free software: you can redistribute it and/or modify        //
//  it under the terms of the GNU General Public License as published by  //
//  the Free Software Foundation, either version 3 of the License, or     //
//  any later version.                                                    //
//                                                                        //
//  Katana is distributed in the hope that it will be useful,             //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of        //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
//  GNU General Public License for more details.                          //
//                                                                        //
//  You should have received a copy of the GNU General Public License     //
//  along with Katana. If not, see <http://www.gnu.org/licenses/>.        //
//                                                                        //
////////////////////////////////////////////////////////////////////////////

package com.matteopacini.katana;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.matteopacini.katana.KatanaAsyncTask.KatanaTaskType;

public class KatanaOnJoinClickListener implements View.OnClickListener {
	
	private KatanaActivity context;
	
	private EditText outputDirectoryEditText;
	private File workingFile;

	public KatanaOnJoinClickListener(KatanaActivity context, EditText outputDirectoryEditText, File workingFile)
	{
		this.context = context;
		this.outputDirectoryEditText = outputDirectoryEditText;
		this.workingFile = workingFile;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onClick(View v) {
		
		int lastIndexOfDot = workingFile.getName().lastIndexOf('.');
		
		final ArrayList<File> pieces = new ArrayList<File>();
		final String originalFile = workingFile.getName().substring(0,lastIndexOfDot);
		
		//File parts.
		
		for(File f : workingFile.getParentFile().listFiles()) 
		{
			if (!f.isDirectory() && f.getName().matches("^.+\\.[0-9]{3}$") && f.getName().contains(originalFile))
			{
				pieces.add(f);
			}
		}
		
		//Checking for missing file parts.
		
		for(int i=1; i<=pieces.size();i++)
		{
			String actualPiece = String.format("%s.%03d", originalFile, i);
			boolean found = false;
			
			
			for(File p : pieces)
			{
				if (p.getName().equals(actualPiece))
				{
					found = true;
				}
			}
			
			if (!found) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setCancelable(false);
				builder.setTitle(R.string.bit_error);
				builder.setMessage(
						String.format(
								context.getResources().getString(R.string.err_join_missing_file),
								actualPiece
						)
				);
				builder.setNeutralButton("OK", null);
				builder.create().show();
				return;
				
			}
		}
		
		final File outputDirectory = new File(outputDirectoryEditText.getText().toString());
		
		if (!outputDirectory.exists())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.bit_error);
			builder.setMessage(
					String.format(
							context.getResources().getString(R.string.err_output_directory_field_no_such_dir),
							outputDirectory.getAbsolutePath()
					)
			);
			builder.setNeutralButton("OK", null);
			builder.create().show();
			return;
		}
							
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle(R.string.bit_confirm);
		builder.setMessage(
				String.format(
						context.getResources().getString(R.string.msg_join_confirm),
						pieces
				)
		);
		builder.setNegativeButton("No", null);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
		
				//Fix per il workingFile
				workingFile = new File(workingFile.getParent(), originalFile);
		
				KatanaAsyncTask task = new KatanaAsyncTask(context,KatanaActivity.generateUniqueNotificationID(),KatanaTaskType.JOIN);
		
				for(KatanaAsyncTask t : KatanaActivity.TASKS)
				{
					if (t.getWorkingFile().equals(context.getWorkingFile()))
					{
				
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setCancelable(false);
						builder.setTitle(R.string.bit_error);
						builder.setMessage(context.getResources().getString(R.string.err_already_joining_file));
						builder.setNeutralButton("OK", null);
						builder.create().show();
						return;
					}
				}
		
				if (KatanaActivity.TASKS.isEmpty()) {
					Toast.makeText(context, String.format(context.getResources().getString(R.string.toast_join_running), workingFile.getName()), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, String.format(context.getResources().getString(R.string.toast_join_queued), workingFile.getName()), Toast.LENGTH_LONG).show();
				}
		
				String[] piecesArray = new String[pieces.size()];
		
				for(int i=0; i<pieces.size(); i++) {
			
					piecesArray[i] = pieces.get(i).getAbsolutePath();
				}
				
				KatanaActivity.TASKS.add(
						(KatanaAsyncTask) 
						task.execute(
						piecesArray,
						workingFile.getAbsolutePath()
				));
		
				context.finish();
				
			} //Fine dialog "onClick"
			
		}); //Fine dialog "onClickListener"
		
		builder.create().show();
		
	}

}
