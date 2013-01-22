package grupo.cliet.pack;

import java.util.Timer;
import java.util.TimerTask;

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
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServicioBase extends Service {
	private Timer timer = new Timer();
	private static final long UPDATE_INTERVAL=15000;
	private final IBinder mBinder = new MyBinder();
	private static final int MY_NOTIFICATION_ID=1;
	private NotificationManager notificationManager;
	private Notification myNotification;

	@Override
	public IBinder onBind(Intent arg0) {
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
	/*	timer.scheduleAtFixedRate((new TimerTask(){
		public void run(){ 
			if (TweetNuevo()){
				Log.d("Llenar", "No hay ningun tweet nuevo");
			} else {
				Log.v("",""+TweetNuevo());
				LlenarBD(ExtraerTwitter("pazlagunera",20));
				Log.d("llenar", "BD actualizada");
				createNotification();}
		}}}),0,UPDATE_INTERVAL);
		Log.d("timer", "timer iniciado"); */
		}
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		if (TweetNuevo()){
			Log.d("Llenar", "No hay ningun tweet nuevo");
		} else {
			Log.v("",""+TweetNuevo());
			LlenarBD(ExtraerTwitter("pazlagunera",20));
			Log.d("llenar", "BD actualizada");
			createNotification();}
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	/*	if (timer != null) {
			timer.cancel();
		}
		Log.d("timer", "timer terminado");*/
	}
	
	//Crea el JSONArray que contiene el codigo XML extraido directo de Twitter
	//searchTerm es el usuario de Twitter del cual se extraen los tweets
	//page es el numero de tweets a extraer
	public JSONArray ExtraerTwitter(String searchTerm, int page){
		String searchUrl = "http://search.twitter.com/search.json?q=from:"+searchTerm+"&rpp="+page+"&include_entities=true&result_type=recent";

		HttpClient client = new  DefaultHttpClient();
		HttpGet get = new HttpGet(searchUrl);
	      
		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String responseBody = null;
		try{
			responseBody = client.execute(get, responseHandler);
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		JSONObject jsonObject = null;
		JSONParser parser=new JSONParser();
		
		try {
			Object obj = parser.parse(responseBody);
			jsonObject=(JSONObject)obj;
			
		}catch(Exception ex){
			Log.v("TEST","Exception: " + ex.getMessage());
		}
		
		JSONArray arr = null;
		
		try {
			Object j = jsonObject.get("results");
			arr = (JSONArray)j;
		}catch(Exception ex){
			Log.v("TEST","Exception: " + ex.getMessage());
		}
		return arr;
		
	}
	
	//Llena la base de datos TWEETS con el JSONArray indicado 
	public void LlenarBD(JSONArray arr){
		AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd=admin.getWritableDatabase();
        bd.execSQL("drop table if exists tweets");
        admin.onCreate(bd);
        ContentValues registro = new ContentValues();
        for(Object t : arr) {
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
	
	//Revisa el ultimo tweet en Twitter y el ultimo en la BD
	//entrega un valor verdadero si no coinciden, falso si sigue siendo el mismo
	public boolean TweetNuevo(){
		JSONArray arr = ExtraerTwitter("pazlagunera",1);
		String id =null;
		for(Object t : arr) { id =((JSONObject)t).get("id_str").toString();}
		AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase bd=admin.getReadableDatabase();
        Cursor fila=bd.rawQuery("select id from tweets where _ID='1'",null);
        String idenBD = null;
        while(fila.moveToNext()) {
         idenBD = fila.getString(fila.getColumnIndex("id"));}
        fila.close();
        bd.close();
        admin.close();
        Log.d("valor de id", id);
        Log.d("valor de id", idenBD);
        if  (id==idenBD) {
        return false;
        } else {
		return true;}	
	}
	
	public void createNotification() {
		  notificationManager =
				    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				   myNotification = new Notification(R.drawable.icon,
						   UltimoTweet(),
				     System.currentTimeMillis());
				   Context context = getApplicationContext();
				   String notificationTitle = "Alerta Ciudadana";
				   String notificationText = UltimoTweet();
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

	  
	  public String UltimoTweet(){
			AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(this, "administracion", null, 1);
	        SQLiteDatabase bd=admin.getReadableDatabase();
	        Cursor fila=bd.rawQuery("select tweet from tweets where _ID='1'",null);
	        String idenBD = null;
	        while(fila.moveToNext()) {
	         idenBD = fila.getString(fila.getColumnIndex("tweet"));}
	        fila.close();
	        bd.close();
	        admin.close();
	        return idenBD;
	  }
	}

