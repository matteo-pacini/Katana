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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class KatanaAsyncTask extends AsyncTask<Object, Integer, Integer> {

	public static enum KatanaTaskType {

		SPLIT, JOIN

	}

	///////////////
	// VARIABLES //
	///////////////
	
	private final KatanaActivity activity;
	private final int notificationID;
	private final KatanaTaskType taskType;
	private boolean queued = false;

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public KatanaAsyncTask(KatanaActivity activity, int notificationID, KatanaTaskType taskType) {

		this.activity = activity;
		this.notificationID = notificationID;
		this.taskType = taskType;

	}

	// ///////////
	// GETTERS //
	// ///////////

	public int getNotificationID() {
		return notificationID;
	}

	public KatanaTaskType getTaskType() {
		return taskType;
	}

	public boolean isQueued() {
		return queued;
	}

	public String getWorkingFile() {
		return activity.getWorkingFile();
	}

	/////////////
	// METHODS //
	/////////////

	@SuppressLint("NewApi")
	@Override
	protected void onPreExecute() {

		Notification n = null;

		if (Build.VERSION.SDK_INT >= 16) {

			n = new Notification.Builder(activity)
					.setContentTitle(activity.getWorkingFile())
					.setContentText(
						taskType.equals(KatanaTaskType.SPLIT) ? 
							(KatanaActivity.TASKS.isEmpty() ? 
								activity.getResources().getString(R.string.notif_split_running):
								activity.getResources().getString(R.string.notif_split_queued)): 
							(KatanaActivity.TASKS.isEmpty() ?
								activity.getResources().getString(R.string.notif_join_running):
								activity.getResources().getString(R.string.notif_join_queued)))
					.setSmallIcon(R.drawable.ic_launcher)
					.setOngoing(true)
					.setSound(Uri.parse("android.resource://com.matteopacini.katana/"+ R.raw.hokshrt4))
					.build();

		} else {

			n = new NotificationCompat.Builder(activity)
					.setContentTitle(activity.getWorkingFile())
					.setContentText(
						taskType.equals(KatanaTaskType.SPLIT) ? 
							(KatanaActivity.TASKS.isEmpty() ? 
								activity.getResources().getString(R.string.notif_split_running):
								activity.getResources().getString(R.string.notif_split_queued)): 
							(KatanaActivity.TASKS.isEmpty() ?
								activity.getResources().getString(R.string.notif_join_running):
								activity.getResources().getString(R.string.notif_join_queued)))
					.setSmallIcon(R.drawable.ic_launcher)
					.setOngoing(true)
					.setSound(Uri.parse("android.resource://com.matteopacini.katana/"+ R.raw.hokshrt4))
					.build();

		}

		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(notificationID, n);

		if (!KatanaActivity.TASKS.isEmpty()) {
			queued = true;
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onProgressUpdate(Integer... values) {

		if (queued) {
			
			Notification n = null;
			
			Intent cancelIntent = new Intent(activity, KatanaActivity.class);
			cancelIntent.setAction("com.matteopacini.katana.stoptask");
			cancelIntent.putExtra("workingFile", getWorkingFile());
			
			PendingIntent pendingIntent = PendingIntent.getActivity(activity, notificationID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			if (Build.VERSION.SDK_INT >= 16) {
				
				Intent stopTaskIntent = new Intent("katana.action.stoptask");
				stopTaskIntent.putExtra("workingFile", getWorkingFile());

				n = new Notification.Builder(activity)
						.setContentTitle(activity.getWorkingFile())
						.setContentText(
							taskType.equals(KatanaTaskType.SPLIT) ? 
								activity.getResources().getString(R.string.notif_split_running):
								activity.getResources().getString(R.string.notif_join_running))
						.setSmallIcon(R.drawable.ic_launcher).setOngoing(true)
						.setContentIntent(pendingIntent)
						.build();

			} else {

				n = new NotificationCompat.Builder(activity)
						.setContentTitle(activity.getWorkingFile())
						.setContentText(
							taskType.equals(KatanaTaskType.SPLIT) ? 
								activity.getResources().getString(R.string.notif_split_running):
								activity.getResources().getString(R.string.notif_join_running))
						.setSmallIcon(R.drawable.ic_launcher).setOngoing(true)
						.setContentIntent(pendingIntent)
						.build();
			}

			NotificationManager notificationManager = 
					(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.notify(notificationID, n);

			queued = false;
		}

	}

	@Override
	protected Integer doInBackground(Object... args) {

		publishProgress(0);

		if (taskType.equals(KatanaTaskType.SPLIT)) {

			return split(
					(String) args[0],
					(String) args[1],
					(String) args[2],
					(Long) args[3],
					(Long) args[4],
					(Boolean) args[5]
			);

		} else {

			return join(
					(String[]) args[0], 
					(String) args[1]
			);
			
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onPostExecute(Integer result) {

		Notification n = null;

		if (Build.VERSION.SDK_INT >= 16) {

			n = new Notification.Builder(activity)
					.setContentTitle(activity.getWorkingFile())
					.setContentText(
						taskType.equals(KatanaTaskType.SPLIT) ? 
							activity.getResources().getString(R.string.notif_split_end):
							activity.getResources().getString(R.string.notif_join_end)
					)
					.setSmallIcon(R.drawable.ic_launcher)
					.setSound(Uri.parse("android.resource://com.matteopacini.katana/"+ R.raw.hokshrt2))
					.build();

		} else {

			n = new NotificationCompat.Builder(activity)
					.setContentTitle(activity.getWorkingFile())
					.setContentText(
						taskType.equals(KatanaTaskType.SPLIT) ? 
							activity.getResources().getString(R.string.notif_split_end):
							activity.getResources().getString(R.string.notif_join_end)
					)
					.setSmallIcon(R.drawable.ic_launcher)
					.setSound(Uri.parse("android.resource://com.matteopacini.katana/"+ R.raw.hokshrt2))
					.build();

		}

		NotificationManager notificationManager = 
				(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(notificationID, n);
		
		if (taskType.equals(KatanaTaskType.JOIN)) {
		
			Toast.makeText(activity, String.format(activity.getResources().getString(R.string.toast_join_completed),getWorkingFile()), Toast.LENGTH_LONG).show();
					
		} else {
			
			Toast.makeText(activity, String.format(activity.getResources().getString(R.string.toast_split_completed),getWorkingFile()), Toast.LENGTH_LONG).show();
			
		}

		/////////////////
		// REMOVE TASK //
		/////////////////
		
		int index = 0;

		for (KatanaAsyncTask t : KatanaActivity.TASKS) {

			if (t.getWorkingFile().equals(this.getWorkingFile())) {

				break;

			} else {
				
				index++;
			}
		}

		KatanaActivity.TASKS.remove(index);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCancelled(Integer result) {

		Notification n = null;

		if (Build.VERSION.SDK_INT >= 16) {

			n = new Notification.Builder(activity)
					.setContentTitle(activity.getWorkingFile())
					.setContentText(
						taskType.equals(KatanaTaskType.SPLIT) ? 
							activity.getResources().getString(R.string.notif_split_cancel):
							activity.getResources().getString(R.string.notif_join_cancel)
					)
					.setSmallIcon(R.drawable.ic_launcher)
					.build();

		} else {

			n = new NotificationCompat.Builder(activity)
					.setContentTitle(activity.getWorkingFile())
					.setContentText(
						taskType.equals(KatanaTaskType.SPLIT) ? 
							activity.getResources().getString(R.string.notif_split_cancel):
							activity.getResources().getString(R.string.notif_join_cancel)
					)
					.setSmallIcon(R.drawable.ic_launcher)
					.build();
		}

		NotificationManager notificationManager = 
				(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(notificationID, n);
	}

	private native static int split(String inputFile, String inputFileBasename,String outputDirectory, long fileSize, long splitSize, boolean preserve);
	private native static int join(String[] inputFiles, String originalFile);
}
