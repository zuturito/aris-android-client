package edu.uoregon.casls.aris_android.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import edu.uoregon.casls.aris_android.Utilities.AppConfig;

/**
 * Created by smorison on 11/5/15.
 */
public class PollTriggerService extends IntentService {
	private boolean runTimer = true;

	public PollTriggerService() {
		super("PollTriggerTimerService");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int i = 1;
//		Log.d(AppConfig.LOGTAG, getClass().getSimpleName() + "OnHandleIntent Called. ");
		while (runTimer) {
			sendUpdateMessage(i++);
			try {
				Thread.sleep(1000); // 1 sec loop
			} catch (Exception e) {
				Log.d(AppConfig.LOGTAG, getClass().getSimpleName() + "SendError: " + e.getMessage());
			}
		}
	}

	private void sendUpdateMessage(int pct) {
//		Log.d(AppConfig.LOGTAG, getClass().getSimpleName() + "Broadcasting update message: " + pct);
		Intent intent = new Intent(AppConfig.TRIGGER_POLLER_SVC_ACTION);
		intent.putExtra(AppConfig.COMMAND, AppConfig.POLLTIMER_CYCLE_PASS);
		intent.putExtra(AppConfig.DATA, pct);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void sendResultMessage(String data) {
//		Log.d(AppConfig.LOGTAG, getClass().getSimpleName() + "Broadcasting result message: " + data);
		Intent intent = new Intent(AppConfig.TRIGGER_POLLER_SVC_ACTION);
		intent.putExtra(AppConfig.COMMAND, AppConfig.POLLTIMER_RESULT);
		intent.putExtra(AppConfig.DATA, data);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		runTimer = false;
		sendResultMessage("PollTriggerTimerService Has Been Stopped");
		super.onDestroy();
	}
}