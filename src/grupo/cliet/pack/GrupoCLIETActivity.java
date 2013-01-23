package grupo.cliet.pack;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class GrupoCLIETActivity extends Activity {
			private ServicioBase s;
			private PendingIntent pendingIntent;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        doBindService();
        
        Log.d("doBindService", "conexion");
        editarbarra();
        Log.d("editarbarra", "barra de titulo");
        //setContentView(R.layout.main);
       // ArrayList<Tweet> tweets = getTweets();
        //ListView listView = (ListView) findViewById(R.id.ListViewId);
        //listView.setAdapter(new UserItemAdapter(this, R.layout.listitem, tweets));
        Intent intent=new Intent(this, alarmaReceiver.class);
        getApplicationContext().sendBroadcast(intent);


}
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, 0, 0, "Preferencias");
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) { 	
		case 0:
			startActivity(new Intent(this, Preferencias.class));
			return true;
		}
		return false;
	}
	
	private ServiceConnection mConnection = new ServiceConnection(){

		public void onServiceConnected(ComponentName name, IBinder binder) {
			// TODO Auto-generated method stub
		    		s = ((ServicioBase.MyBinder)binder).getService();
		    		new Thread(){
		    			public void run(){
		    				s.LlenarBD(s.ExtraerTwitter("pazlagunera", 20));
		    		}}.start();
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
	ArrayList<Tweet> tweets = getTweets();
    ListView listView = (ListView) findViewById(R.id.ListViewId);
    listView.setAdapter(new UserItemAdapter(this, R.layout.listitem, tweets));
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
					date.setText(tweet.fecha);
				}
				
				if(image != null) {
					image.setImageBitmap(getBitmap(tweet.image_url));
				}
			}
			return v;
		}}
	

	public Bitmap getBitmap(String bitmapUrl) {
		try {
			URL url = new URL(bitmapUrl);
			return BitmapFactory.decodeStream(url.openConnection() .getInputStream()); 
		}
		catch(Exception ex) {return null;}
	}
	
	public ArrayList<Tweet> getTweets() {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		AdminSQLiteOpenHelper admin=new AdminSQLiteOpenHelper(this, "administracion", null, 1);
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
	public void editarbarra(){
    final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.main);
    if ( customTitleSupported ) {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }

   /* final TextView myTitleText = (TextView) findViewById(R.id.myTitle);
    if ( myTitleText != null ) {
        myTitleText.setText("NEW TITLE");}*/
    }


	}

