package grupo.cliet.pack;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class Preferencias extends PreferenceActivity {
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Intent intent=new Intent(this, alarmaReceiver.class);
        getApplicationContext().sendBroadcast(intent);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState); 
        
        addPreferencesFromResource(R.xml.preferencias);        
    }
    
    
}

