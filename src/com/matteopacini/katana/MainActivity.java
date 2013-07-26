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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class MainActivity extends PreferenceActivity {

	private SharedPreferences preferences;

	private boolean isFirstStart = true;

	// TODO: Use fragments?
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		///////////////////////
		// FIRST START ALERT //
		///////////////////////

		if ((isFirstStart = preferences.getBoolean("com.matteopacini.katana.firststart", true))) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);

			builder.setMessage(getResources().getString(R.string.alert_first_start))
				.setPositiveButton("OK", null)
				.setNegativeButton(getResources().getString(R.string.btn_dont_show_this_again),new DialogInterface.OnClickListener() {
	
					public void onClick(DialogInterface dialog,int id) {
	
						isFirstStart = false;
	
					}
	
				});

			builder.create().show();

		} // Fine "if"

	}

	@Override
	protected void onStop() {

		super.onStop();

		//////////////////////
		// SAVE PREFERENCES //
		//////////////////////
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("com.matteopacini.katana.firststart", isFirstStart);
		editor.commit();

	}

}
