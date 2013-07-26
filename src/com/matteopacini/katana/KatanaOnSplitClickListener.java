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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.matteopacini.katana.KatanaAsyncTask.KatanaTaskType;

public class KatanaOnSplitClickListener implements View.OnClickListener {
	
	private KatanaActivity context;
	
	private EditText splitSizeEditText;
	private EditText outputDirectoryEditText;
	private File workingFile;
	private long workingFileSize;

	public KatanaOnSplitClickListener(KatanaActivity context, EditText splitSizeEditText, EditText outputDirectoryEditText, File workingFile, long workingFileSize)
	{
		this.context = context;
		this.splitSizeEditText = splitSizeEditText;
		this.outputDirectoryEditText = outputDirectoryEditText;
		this.workingFile = workingFile;
		this.workingFileSize = workingFileSize;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onClick(View v) {
		
		String splitSizeString = splitSizeEditText.getText().toString().toLowerCase();
		
		StringBuilder numbers = new StringBuilder();
		StringBuilder letters = new StringBuilder();
										
		if(splitSizeString.equals(""))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.bit_error);
			builder.setMessage(context.getResources().getString(R.string.err_split_size_field_empty));
			builder.setNeutralButton("OK", null);
			builder.create().show();
			return;
		}
		
		for(int i=0; i<splitSizeString.length(); i++)
		{
			if (Character.isDigit(splitSizeString.charAt(i)))
			{
				numbers.append(splitSizeString.charAt(i));
				
			} else if (Character.isLetter(splitSizeString.charAt(i)))
			{
				letters.append(splitSizeString.charAt(i));
			}
		}
						
		try {
			
			Long.parseLong(numbers.toString());
			
		} catch (NumberFormatException ex) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.bit_error);
			builder.setMessage(context.getResources().getString(R.string.err_split_size_field_no_numbers));
			builder.setNeutralButton("OK", null);
			builder.create().show();
			return;
			
		}
						
		if (!letters.toString().equals("kb") && !letters.toString().equals("mb") && !letters.toString().equals("gb"))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.bit_error);
			builder.setMessage(
					String.format(
							context.getResources().getString(R.string.err_split_size_field_invalid_unit),
							letters.toString()
					)
			);
			builder.setNeutralButton("OK", null);
			builder.create().show();
			return;
		}
		
		long _splitSize = Long.parseLong(numbers.toString());
		
		if (letters.toString().equals("kb")) _splitSize *= KatanaActivity.KILOBYTE;
		else if (letters.toString().equals("mb")) _splitSize *= KatanaActivity.MEGABYTE;
		else if (letters.toString().equals("gb")) _splitSize *= KatanaActivity.GIGABYTE;
		
		final long splitSize = _splitSize;
		
		if (splitSize < KatanaActivity.MIN_SPLIT_SIZE || splitSize >= workingFileSize)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.bit_error);
			builder.setMessage(
					String.format(
							context.getResources().getString(R.string.err_split_size_field_invalid_split_size),
							KatanaActivity.MIN_SPLIT_SIZE/KatanaActivity.KILOBYTE
					)
			);
			builder.setNeutralButton("OK", null);
			builder.create().show();
			return;
		}
		
		int pieces = (int)Math.ceil(workingFileSize/(double)splitSize);
						
		if (pieces > KatanaActivity.MAX_PIECES)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.bit_error);
			builder.setMessage(
					String.format(
							context.getResources().getString(R.string.err_split_size_field_too_many_pieces),
							pieces,
							KatanaActivity.MAX_PIECES
					)
			);
			builder.setNeutralButton("OK", null);
			builder.create().show();
			return;
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
						context.getResources().getString(R.string.msg_split_confirm),
						pieces
				)
		);
		builder.setNegativeButton("No", null);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
																
				KatanaAsyncTask task = new KatanaAsyncTask(context, KatanaActivity.generateUniqueNotificationID(), KatanaTaskType.SPLIT);
				
				for(KatanaAsyncTask t : KatanaActivity.TASKS)
				{
					if (t.getWorkingFile().equals(context.getWorkingFile()))
					{
						
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setCancelable(false);
						builder.setTitle(R.string.bit_error);
						builder.setMessage(context.getResources().getString(R.string.err_already_splitting_file));
						builder.setNeutralButton("OK", null);
						builder.create().show();
						return;
					}
				}
				
				
				if (KatanaActivity.TASKS.isEmpty()) {
					Toast.makeText(context, String.format(context.getResources().getString(R.string.toast_split_running), workingFile.getName()), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, String.format(context.getResources().getString(R.string.toast_split_queued), workingFile.getName()), Toast.LENGTH_LONG).show();
				}
				
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				
				KatanaActivity.TASKS.add(
						(KatanaAsyncTask) 
						task.execute(
						workingFile.getAbsolutePath(),
						workingFile.getName(),
						outputDirectory.getAbsolutePath(),
						workingFileSize,
						splitSize,
						preferences.getBoolean("com.matteopacini.katana.preserve_original_file", true)
				));
				
				context.finish();
										
			} //Fine dialog "onClick"
			
		}); //Fine dialog "onClickListener"
		
		builder.create().show();
		
	}

}
