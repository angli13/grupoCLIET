package grupo.cliet.pack;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;



public class GrupoCLIETActivity extends SherlockActivity {
		//private ServicioBase s;
		//private PendingIntent pendingIntent;
			private static final String CUENTA = 	"Rojolaguna";
			private static final String CUENTA2 = 	"pazlagunera";
			private String NUMERODETWEETS = "20";
			

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  	   boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
	    if (firstrun){
	    	new ObteneryLlenar().execute(CUENTA,CUENTA2,NUMERODETWEETS);
	    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
	        .edit()
	        .putBoolean("firstrun", false)
	        .commit();
	    }
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
       // doBindService();
        
        setContentView(R.layout.main);
        Log.d("antes de comparar", "listo");
        new compararTweets().execute(CUENTA,CUENTA2);
        
        Intent intent=new Intent(this, alarmaReceiver.class);
        getApplicationContext().sendBroadcast(intent);

}




	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.layout.mainmenu, menu);
        return true;
	}




	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == R.id.actualizar) {
        	new compararTweets().execute(CUENTA,CUENTA2);
        }
        if (item.getItemId() == R.id.configuracion) {
            startActivity(new Intent(this, Preferencias.class));
        }
        return true;
	}





	/*private ServiceConnection mConnection = new ServiceConnection(){

		public void onServiceConnected(ComponentName name, IBinder binder) {
			// TODO Auto-generated method stub
		    		s = ((ServicioBase.MyBinder)binder).getService(); 
		    		new CrearArray().execute();
		    		Log.d("Servicio", "conectado");	
		}
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
					s=null;
			Log.d("Servicio", "desconectado");	
		}

    };
    void doBindService(){
    	bindService(new Intent(this, ServicioBase.class), 
    			mConnection,Context.BIND_AUTO_CREATE);
    	
    	
    }*/


            @Override
            protected void onStart() {
                super.onStart();
            }

	 public class UserItemAdapter extends ArrayAdapter<Tweet> {
		private ArrayList<Tweet> tweets;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<Tweet> tweets) {
			super(context, textViewResourceId, tweets);
			this.tweets = tweets;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.listitem, null);
			}

			Tweet tweet = tweets.get(position);
			if (tweet != null) {
				TextView username = (TextView) v.findViewById(R.id.username);
				TextView message = (TextView) v.findViewById(R.id.message);
				TextView date = (TextView) v.findViewById(R.id.date);
				ImageView image = (ImageView) v.findViewById(R.id.avatar);

				if (username != null) {
					username.setText(tweet.username);
				}
				if(message != null) {
					message.setText(tweet.message);
				}
				if(date != null) {
					date.setText(cambiarFecha(tweet.fecha));
				}
				
				if(image != null) {
					if (tweet.username.equals("codigo rojo laguna")){
					image.setImageResource(R.drawable.crojo);}
					else{image.setImageResource(R.drawable.grupo);}
					//image.setImageBitmap(getBitmap(tweet.image_url));
				}
			}
			return v;
		}}
	
	
	/*public Bitmap getBitmap(String bitmapUrl) {
		try {
			URL url = new URL(bitmapUrl);
			return BitmapFactory.decodeStream(url.openConnection() .getInputStream()); 
		}
		catch(Exception ex) {return null;}
	}*/
	
	
	public class Tweet {
		public String id;
		public String username;
		public String message;
		public String image_url;
		public String fecha;
		
		
		public Tweet(String id, String username, String message, String url, String fecha) {
			this.id = id;
			this.username = username;
			this.message = message;
			this.image_url = url;
			this.fecha = fecha;		
		}
	}

    public class PopularTabla extends AsyncTask<ArrayList<Tweet>, Integer, UserItemAdapter>{
        private ProgressDialog dialog;
      int myProgress;
    	@Override
		protected void onPreExecute() {
            dialog = ProgressDialog.show(GrupoCLIETActivity.this, "", "Cargando... Por favor espera", true);
          myProgress = 0;
		}

		@Override
		protected UserItemAdapter doInBackground(ArrayList<Tweet>... params) {
			ArrayList<Tweet> twits = params[0];
			UserItemAdapter adaptador= new UserItemAdapter(GrupoCLIETActivity.this, R.layout.listitem, twits);
			return adaptador;
		}

		@Override
		protected void onPostExecute(UserItemAdapter result) {
			dialog.dismiss();
			ListView listView = (ListView) findViewById(R.id.ListViewId);
	        listView.setAdapter(result);
		}
    	
    }

    public class CrearArray extends AsyncTask<Void, Integer, ArrayList<Tweet>>{
		
    	public ArrayList<Tweet> getTweets() {
    		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
    		AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(GrupoCLIETActivity.this, "administracion", null, 1);
            SQLiteDatabase bd=admin.getWritableDatabase();
            Cursor fila=bd.rawQuery("select *  from tweets",null);
            while(fila.moveToNext()) {
                tweets.add(new Tweet(fila.getString(fila.getColumnIndex("id")),
                		fila.getString(fila.getColumnIndex("usuario")),
                		fila.getString(fila.getColumnIndex("tweet")),
                		fila.getString(fila.getColumnIndex("imagen")),
                		fila.getString(fila.getColumnIndex("fecha"))));
               Log.d("id en BD", fila.getString(fila.getColumnIndex("_ID"))) ;
             }
            bd.close(); 
            fila.close();
            return tweets;

        }
        private ProgressDialog dialog;
        int myProgress;
      	@Override
  		protected void onPreExecute() {
              dialog = ProgressDialog.show(GrupoCLIETActivity.this, "", "Cargando... Por favor espera", true);
            myProgress = 0;
  		}
    	@Override
		protected ArrayList<Tweet> doInBackground(Void... params) {
			try{
    		ArrayList<Tweet> tweets = getTweets();
    		return tweets;
			}catch (Exception ex){
    		Log.d("array", "problema en array");
			return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<Tweet> result) {
			new PopularTabla().execute(result);
			dialog.dismiss();
		}
    	
    }
    public String cambiarFecha(String fecha){
    	fecha = fecha.replace(",", "");
    	Locale local = new Locale("es","MX");
    final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyy HH:mm:ss", local);
    final SimpleDateFormat parser = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss Z", Locale.US);
    String fechanueva = null;
	try {
		fechanueva = formatter.format(parser.parse(fecha));
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return fechanueva;
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
				this.cancel(true);
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(GrupoCLIETActivity.this, "administracion", null, 1);
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
	        dialog.dismiss();
	        new CrearArray().execute();
		}

		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(),
                  "Problema de Conexión", Toast.LENGTH_SHORT).show();
		}  
        private ProgressDialog dialog;
        int myProgress;
      	@Override
  		protected void onPreExecute() {
              dialog = ProgressDialog.show(GrupoCLIETActivity.this, "", "Actualizando... Por favor espera", true);
            myProgress = 0;
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
					this.cancel(true);
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
					Log.d("valor de id", id);
				}
				AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(GrupoCLIETActivity.this, "administracion", null, 1);
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
				new CrearArray().execute();
				dialog.dismiss();
			} else {
			Log.v("",""+"tweet nuevo, ejecutando consulta y llenando BD"); 
				new ObteneryLlenar().execute(CUENTA,CUENTA2,NUMERODETWEETS);
				dialog.dismiss();
				Log.d("llenar", "BD actualizada");
				}
			}
			 private ProgressDialog dialog;
		        int myProgress;
		      	@Override
		  		protected void onPreExecute() {
		              dialog = ProgressDialog.show(GrupoCLIETActivity.this, "", "Actualizando... Por favor espera", true);
		            myProgress = 0;
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

