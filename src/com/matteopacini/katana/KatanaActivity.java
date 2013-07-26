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
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import org.acra.ACRA;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.matteopacini.katana.KatanaAsyncTask.KatanaTaskType;

public class KatanaActivity extends Activity {
	
	public static ArrayList<KatanaAsyncTask> TASKS = new ArrayList<KatanaAsyncTask>();
			
	///////////////
	// CONSTANTS //
	///////////////
	
	public static final long KILOBYTE = 1024;
	public static final long MEGABYTE = 1048576;
	public static final long GIGABYTE = 1073741824;
	
	public static final long MIN_FILE_SIZE = (2*KILOBYTE);
	public static final long MIN_SPLIT_SIZE = (MIN_FILE_SIZE/2);
	public static final long MAX_PIECES = 999;
	
	///////////////
	// VARIABLES //
	///////////////
			
	private KatanaTaskType taskType = KatanaTaskType.SPLIT;
	private File workingFile;
	private long workingFileSize;
	private SharedPreferences preferences;
	
	////////
	// UI //
	////////
	
	private TextView fileNameTextView;
	private TextView fileSizeTextView;
	private EditText splitSizeEditText;
	private EditText outputDirectoryEditText;
	private Button browseButton;
	private Button splitButton;
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getWorkingFile() { return workingFile.getName(); }
	
	/////////////
	// METHODS //
	/////////////
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);

	    if (requestCode == 666) {
	        if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
	            outputDirectoryEditText.setText(data
	                .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
	        } 
	    }
		
	}
	
	@Override
	public void onBackPressed() {
				
		boolean userVoted = preferences.getBoolean("com.matteopacini.katana.uservoted", false);
		
		if (!userVoted) {
		
			AlertDialog.Builder rateDialogBuilder = new AlertDialog.Builder(this)
			.setTitle(
					String.format(getResources().getString(R.string.rate_dialog_title), getResources().getString(R.string.app_name))
			)
		    .setIcon(R.drawable.ic_launcher)
		    .setMessage(
		    		String.format(getResources().getString(R.string.rate_dialog_message), getResources().getString(R.string.app_name))
		    )
		    .setPositiveButton(getResources().getString(R.string.rate_dialog_btn_rate_it), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.matteopacini.katana")));
					preferences.edit().putBoolean("com.matteopacini.katana.uservoted", true).commit();
					
				}
				
			})
		    .setNegativeButton(getResources().getString(R.string.rate_dialog_btn_no_thanks), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				
					finish();
					
				}
			});
			
			rateDialogBuilder.create().show();
		
		} else {
			
			super.onBackPressed();
			
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_katana);
				
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		///////////////////////
		// PREFERENCES CHECK //
		///////////////////////
				
		
		boolean tutorialForNormalScreenShown = preferences.getBoolean("katana.normalscreentutorial", false);
		
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL &&
				!tutorialForNormalScreenShown)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			
			builder.setTitle(R.string.bit_tutorial);
			builder.setMessage(
					getResources().getString(R.string.tutorial_normal_screen)
			);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					
					SharedPreferences.Editor editor = preferences.edit();
					editor.putBoolean("katana.normalscreentutorial", true);
					editor.commit();
					
				}
			});
			builder.create().show();
		}
		
		//////////////////
		// INTENT CHECK //
		//////////////////
		
		Intent intent = getIntent();
		
		//////////////////////////////////////////////
		// INTENT: com.matteopacini.katana.stoptask //
		//////////////////////////////////////////////
		
		if (intent.getAction().equals("com.matteopacini.katana.stoptask")) {
		
			int index = 0;
			
			for(KatanaAsyncTask t : TASKS) {
				
				if (t.getWorkingFile().equals(intent.getExtras().getString("workingFile")))
				{
					break;
					
				} else {
					
					index++;
					
				}
				
			}
			
			TASKS.get(index).cancel(true);
			TASKS.remove(index);
			finish();
			
		}
		
		////////////////////////////////////////
		// INTENT: android.intent.action.VIEW //
		////////////////////////////////////////
				
		//File size check.
		
		try {
			
			workingFile = new File(new URI(intent.getDataString()));
			workingFileSize = workingFile.length();
						
			if (workingFileSize < MIN_FILE_SIZE) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				
				builder.setTitle(R.string.bit_error);
				builder.setMessage(
						String.format(
								getResources().getString(R.string.err_file_is_too_small),
								workingFile.getName(),
								MIN_FILE_SIZE/KILOBYTE
						)
				);
				builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						KatanaActivity.this.finish();
						
					}
				});
				
				builder.create().show();
												
			} //Fine "if"
			
		} catch (Exception e) {
			
			ACRA.getErrorReporter().handleException(e);
			
		}
		
		//////////////////
		// SPLIT OR JOIN //
		//////////////////
		
		if (workingFile.getName().matches("^.+\\.[0-9]{3}$"))
		{
			taskType = KatanaTaskType.JOIN;
		}
		
		//////////////////////
		// UI CONFIGURATION //
		//////////////////////
		
		fileNameTextView = (TextView) findViewById(R.id.fileNameTextView);
		fileSizeTextView = (TextView) findViewById(R.id.fileSizeTextView);
		splitSizeEditText = (EditText) findViewById(R.id.splitSizeEditText);
		outputDirectoryEditText = (EditText)findViewById(R.id.outputDirectoryEditText);
		browseButton = (Button)findViewById(R.id.browseButton);
		splitButton = (Button) findViewById(R.id.splitButton);
		
		outputDirectoryEditText.setEnabled(false);
		
		//File name.
		
		fileNameTextView.setText(String.format(getResources().getString(R.string.textview_file_name),workingFile.getName()));
		
		//File size.
			
		if (workingFileSize >= GIGABYTE) {
			
			fileSizeTextView.setText(String.format(getResources().getString(R.string.textview_file_size),(workingFileSize/(float)GIGABYTE),"GB"));
			
		} else if (workingFileSize >= MEGABYTE) {
			
			fileSizeTextView.setText(String.format(getResources().getString(R.string.textview_file_size),(workingFileSize/(float)MEGABYTE),"MB"));
			
		} else {
			
			fileSizeTextView.setText(String.format(getResources().getString(R.string.textview_file_size),(workingFileSize/(float)KILOBYTE),"KB"));
		}
		
		//Output directory.
		
		outputDirectoryEditText.setText(workingFile.getParent());
		
		browseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				final Intent chooserIntent = new Intent(KatanaActivity.this, DirectoryChooserActivity.class);
				startActivityForResult(chooserIntent, 666);

			}
		});
		
		///////////
		// SPLIT //
		///////////
		
		splitButton.setOnClickListener(
				new KatanaOnSplitClickListener(KatanaActivity.this, 
				splitSizeEditText, 
				outputDirectoryEditText, 
				workingFile, 
				workingFileSize));
		
		//////////
		// JOIN //
		//////////
		
		if (taskType.equals(KatanaTaskType.JOIN))
		{
			splitSizeEditText.setEnabled(false);
			splitButton.setText(getResources().getString(R.string.bit_join));
			
			splitButton.setOnClickListener(new KatanaOnJoinClickListener(KatanaActivity.this, outputDirectoryEditText, workingFile)); 
			
		} //Fine "if"
		
	}
	
	public static int generateUniqueNotificationID()
	{
		boolean found = false;
		int id = new Random().nextInt();
		
		if (TASKS.isEmpty()) return id;
		
		while(true) {
		
			for(KatanaAsyncTask task: TASKS) {
			
				if (task.getNotificationID() == id) {
					found = true;
				}
				
			}
			
			if (found) {
				
				id = new Random().nextInt();
				found = false;
				
			} else {
				
				break;
			}
		}
		
		return id;
	}
}
