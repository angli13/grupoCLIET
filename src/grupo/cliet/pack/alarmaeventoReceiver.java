package grupo.cliet.pack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class alarmaeventoReceiver extends BroadcastReceiver {
 
	@Override
	public void onReceive(final Context ctx, final Intent intent) {
		
		Log.d("servicio evento", "evento creado,llamando al servicio");
		Intent eventService = new Intent(ctx, ServicioBase.class);
		ctx.startService(eventService);
	}
 
}