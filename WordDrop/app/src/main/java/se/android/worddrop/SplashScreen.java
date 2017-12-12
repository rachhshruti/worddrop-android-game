package se.android.worddrop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Splash screen when the application opens up
 * @author Suman Gaonkar
 */
public class SplashScreen extends Activity {

	/**
	 * Creates the layout for splash screen and sets the timer for it's appearance
	 * @param savedInstanceState saves the state of application
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(se.android.worddrop.R.layout.splash);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(SplashScreen.this,UserManagementActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

    /**
     * Auto-generated method stub
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}