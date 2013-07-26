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

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;
import static org.acra.ReportField.USER_EMAIL;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "",
				mailTo = "ispeakprogramming@gmail.com",
				mode = ReportingInteractionMode.TOAST,
				resToastText = R.string.toast_crash_text,
				customReportContent = {USER_EMAIL, USER_APP_START_DATE, USER_CRASH_DATE, APP_VERSION_NAME, APP_VERSION_CODE, ANDROID_VERSION,
									   BUILD, BRAND, SHARED_PREFERENCES, STACK_TRACE}
				)
public class KatanaApplication extends Application {

	static {
		System.loadLibrary("KatanaNative");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}
