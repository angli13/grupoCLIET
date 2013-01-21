package grupo.cliet.pack;

import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


public class GrupoCLIETActivity extends Activity {
			private static final String twit = "twit";
			private ServicioBase s;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        setContentView(R.layout.main);
        LlenarTabla("pazlagunera", 20);
        ArrayList<Tweet> tweets = getTweets();
        ListView listView = (ListView) findViewById(R.id.ListViewId);
        listView.setAdapter(new UserItemAdapter(this, R.layout.listitem, tweets));
         

}
    private ServiceConnection mConnection = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder binder) {
			// TODO Auto-generated method stub
    		s = ((ServicioBase.MyBinder)binder).getService();
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
	public void LlenarTabla(String searchTerm, int page) {
	//public ArrayList<Tweet> getTweets(String searchTerm, int page) {
		String searchUrl = "http://search.twitter.com/search.json?q=from:"+searchTerm+"&rpp="+page+"&include_entities=true&result_type=recent";

		//ArrayList<Tweet> tweetstemp = new ArrayList<Tweet>();
		
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
        /*if (fila.moveToFirst())
        {
        	Tweet tweet = new Tweet(fila.getString(0) ,fila.getString(1),
        			fila.getString(2),fila.getString(3),fila.getString(4));
			tweets.add(tweet);           
        }
        else
            Toast.makeText(this, "BD vacia", Toast.LENGTH_SHORT).show();
       */ 
        bd.close(); 
        fila.close();
        return tweets;

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
	}
