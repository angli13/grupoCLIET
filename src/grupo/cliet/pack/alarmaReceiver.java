package grupo.cliet.pack;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class alarmaReceiver extends BroadcastReceiver {
	private static final String APP_TAG = "grupo.cliet.pack";
 
	private static final String EXEC_INTERVAL = "1800001";
 
	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		Log.d(APP_TAG, "alarmaReceiver.onReceive() called");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String storedPreference = prefs.getString("intervalo_actualizacion", EXEC_INTERVAL);
		int intervalo = Integer.parseInt(storedPreference);
		Log.d("preferencias", String.valueOf(storedPreference));
		AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctx, alarmaeventoReceiver.class); // explicit
        						// intent
		PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.SECOND, (intervalo/1000));
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				now.getTimeInMillis(), intervalo, intentExecuted);
		Log.d(APP_TAG, "alarma creada");
	}
 
}
