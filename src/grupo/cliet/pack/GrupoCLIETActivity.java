package grupo.cliet.pack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.simple.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;


public class GrupoCLIETActivity extends SherlockActivity {
			private ServicioBase s;
			private PendingIntent pendingIntent;
			

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        doBindService();
        Log.d("doBindService", "conexion");
        //editarbarra();
        Log.d("editarbarra", "barra de titulo");
        setContentView(R.layout.main);
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
        if (item.getItemId() == R.id.configuracion) {
            startActivity(new Intent(this, Preferencias.class));
        }
        return true;
	}





	private ServiceConnection mConnection = new ServiceConnection(){

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
    	
    	
    }
    


    
 public void actualizar(View view){
	 new CrearArray().execute();
    }   

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
					image.setImageResource(R.drawable.grupo);
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
	
	
	
	public void iniciarAlarma(){
		Intent myIntent = new Intent(GrupoCLIETActivity.this, ServicioBase.class);
		   pendingIntent = PendingIntent.getService(GrupoCLIETActivity.this, 0, myIntent, 0);

		            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

		            Calendar calendar = Calendar.getInstance();
		            calendar.setTimeInMillis(System.currentTimeMillis());
		            calendar.add(Calendar.SECOND, 15);
		            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), calendar.getTimeInMillis(), pendingIntent);
		            Log.d("alarma", "activada");
	}
	
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
		@Override
		protected UserItemAdapter doInBackground(ArrayList<Tweet>... params) {
			ArrayList<Tweet> twits = params[0];
			UserItemAdapter adaptador= new UserItemAdapter(GrupoCLIETActivity.this, R.layout.listitem, twits);
			return adaptador;
		}

		@Override
		protected void onPostExecute(UserItemAdapter result) {
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
    	
    	@Override
		protected ArrayList<Tweet> doInBackground(Void... params) {
			ArrayList<Tweet> tweets = getTweets();
			return tweets;
		}

		@Override
		protected void onPostExecute(ArrayList<Tweet> result) {
			new PopularTabla().execute(result);
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

	}

