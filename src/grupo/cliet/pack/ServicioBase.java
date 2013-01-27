package grupo.cliet.pack;



import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ServicioBase extends Service {
	private final IBinder mBinder = new MyBinder();
	private static final int MY_NOTIFICATION_ID=1;
	private static final String CUENTA2 = "pazlagunera";
	private NotificationManager notificationManager;
	private Notification myNotification;
	private static final String CUENTA = 	"Rojolaguna";
	private String NUMERODETWEETS = "20";
	private String twit;
	@Override
	public IBinder onBind(Intent arg0) {
		new ObteneryLlenar().execute(CUENTA,CUENTA2,NUMERODETWEETS);
		//new compararTweets().execute(CUENTA);
		return mBinder;
	}
	public class MyBinder extends Binder{
		ServicioBase getService() {
			return ServicioBase.this;
		}
		}
	@Override
	public void onCreate() {
		super.onCreate();

		}
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		new compararTweets().execute(CUENTA,CUENTA2);
		return START_NOT_STICKY;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	/*	if (timer != null) {
			timer.cancel();
		}
		Log.d("timer", "timer terminado");*/
	}
	

	

	
	public void createNotification() {
		  notificationManager =
				    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				   myNotification = new Notification(R.drawable.ic_launcher,
						   twit,
				     System.currentTimeMillis());
				   Context context = getApplicationContext();
				   String notificationTitle = "Alerta Ciudadana";
				   String notificationText = twit;
				   Intent myIntent = new Intent(this, GrupoCLIETActivity.class);
				   PendingIntent pendingIntent
				     = PendingIntent.getActivity(ServicioBase.this,
				       0, myIntent,
				       Intent.FLAG_ACTIVITY_NEW_TASK);
				   myNotification.defaults |= Notification.DEFAULT_SOUND;
				   myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				   myNotification.setLatestEventInfo(context,
				      notificationTitle,
				      notificationText,
				      pendingIntent);
				   notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
				  
				  }

	  
	  
	  public class ObteneryLlenar extends AsyncTask<String, Integer, JSONArray >{

		@Override
		protected JSONArray doInBackground(String... searchTerm) {
			try{
			String searchUrl = "http://search.twitter.com/search.json?q=from%3a"+searchTerm[0]+"+OR+from%3a"+searchTerm[1]+"&rpp="+searchTerm[2]+"&include_entities=true&result_type=recent";

			HttpClient client = new  DefaultHttpClient();
			HttpGet get = new HttpGet(searchUrl);
		      
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			String responseBody = null;
			try{
				responseBody = client.execute(get, responseHandler);
			}catch(Exception ex) {
				ex.printStackTrace();
				Toast.makeText(getApplicationContext(),
	                    "Problema de Conexión", Toast.LENGTH_SHORT).show();
			}

			JSONObject jsonObject = null;
			JSONParser parser=new JSONParser();
			
			try {
				Object obj = parser.parse(responseBody);
				jsonObject=(JSONObject)obj;
				
			}catch(Exception ex){
				Log.v("TESTservicio","Exception: " + ex.getMessage());
			}
			
			JSONArray arr = null;
			
			try {
				Object j = jsonObject.get("results");
				arr = (JSONArray)j;
			}catch(Exception ex){
				Log.v("TESTser","Exception: " + ex.getMessage());
			}
			return arr;
			}catch (Exception error){
				Log.v("Error","Exception: " + error.getMessage());
				cancel(true);
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(ServicioBase.this, "administracion", null, 1);
	        SQLiteDatabase bd=admin.getWritableDatabase();
	        bd.execSQL("drop table if exists tweets");
	        admin.onCreate(bd);
	        ContentValues registro = new ContentValues();
	        for(Object t : result) {
				registro.put("id", ((JSONObject)t).get("id_str").toString());
		        registro.put("usuario", ((JSONObject)t).get("from_user_name").toString());
		        registro.put("tweet",((JSONObject)t).get("text").toString());
		        registro.put("imagen",((JSONObject)t).get("profile_image_url").toString() ); 
		        registro.put("fecha",((JSONObject)t).get("created_at").toString()) ; 	
	            bd.insert("tweets", null, registro);
	            Log.d("TWEET", "TWEET guardado "+(((JSONObject)t).get("id_str").toString()));
	        }
	        bd.close();
	        admin.close();	
		}

		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(),
                    "Problema de Conexión", Toast.LENGTH_SHORT).show();
		}  
	  }
	  
	  public class compararTweets extends AsyncTask<String, Integer, JSONArray>{

		@Override
		protected JSONArray doInBackground(String... params) {
			try{
			String searchUrl = "http://search.twitter.com/search.json?q=from%3a"+params[0]+"+OR+from%3a"+params[1]+"&rpp=1&include_entities=true&result_type=recent";

			HttpClient client = new  DefaultHttpClient();
			HttpGet get = new HttpGet(searchUrl);
		      
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			String responseBody = null;
			try{
				responseBody = client.execute(get, responseHandler);
			}catch(Exception ex) {
				ex.printStackTrace();
				Toast.makeText(getApplicationContext(),
	                    "Problema de Conexión", Toast.LENGTH_SHORT).show();
				cancel(true);
			}

			JSONObject jsonObject = null;
			JSONParser parser=new JSONParser();
			
			try {
				Object obj = parser.parse(responseBody);
				jsonObject=(JSONObject)obj;
				
			}catch(Exception ex){
				Log.v("TESTservicio","Exception: " + ex.getMessage());
				cancel(true);
			}
			
			JSONArray arr = null;
			
			try {
				Object j = jsonObject.get("results");
				arr = (JSONArray)j;
			}catch(Exception ex){
				Log.v("TESTser","Exception: " + ex.getMessage());
				cancel(true);
			}
			return arr;
			}catch (Exception error){
				Log.v("Error","Exception: " + error.getMessage());
				cancel(true);
				return null;
			}
			
		}

		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(),
                    "Problema de Conexión", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			String id ="vacio";
			for(Object t : result) { 
				id =((JSONObject)t).get("id_str").toString();
				twit =((JSONObject)t).get("text").toString();
				Log.d("valor de id", id);
			}
			AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(ServicioBase.this, "administracion", null, 1);
	        SQLiteDatabase bd=admin.getReadableDatabase();
	        Cursor fila=bd.rawQuery("select id from tweets where _ID='1'",null);
	        String idenBD = null;
	        while(fila.moveToNext()) {
	         idenBD = fila.getString(fila.getColumnIndex("id"));
	        }
	        fila.close();
	        bd.close();
	        admin.close();
	        Log.d("valor de id", id);
	        Log.d("valor de id en BD", idenBD);
	        long ljson = Long.parseLong(id);
	        long lbd = Long.parseLong(idenBD);
	        Log.v("valor",Boolean.toString(tweetboolean(ljson,lbd)));
			if (tweetboolean(ljson,lbd)){
			Log.d("Llenar", "No hay ningun tweet nuevo");
		} else {
		Log.v("",""+"tweet nuevo, ejecutando consulta y llenando BD"); 
			new ObteneryLlenar().execute(CUENTA,CUENTA2,NUMERODETWEETS);
			Log.d("llenar", "BD actualizada");
			createNotification();
			}
		  
	  }
        public Boolean tweetboolean(long id1, long id2){
        	if (id1==id2){
        		return true;
        	} else{
        		return false;
        	}
        }
	}
}

