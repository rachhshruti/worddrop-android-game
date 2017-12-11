package se.android.worddrop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;

/**
 * Game mode selection screen
 * Different game modes to select from:
 * 1. Timer
 * 2. Moves
 * 3. Endless
 * Also, there is a powerups button which lets the user purchase powerups
 * based on their total score
 * @author Pratik Sanghvi
 */
public class ModeSelectionActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnMoves, btnEndless;

    /**
     * Creates the layout for game mode selection screen
     * @param savedInstanceState saves the state of application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modeselection);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnNext = (Button) findViewById(R.id.btnTimed);
        btnNext.setOnClickListener(this);

        btnMoves = (Button) findViewById(R.id.btnMoves);
        btnMoves.setOnClickListener(this);

        btnEndless = (Button) findViewById(R.id.btnEndless);
        btnEndless.setOnClickListener(this);

    }

    /**
     * Creates mode selection menu
     * @param menu
     * @return true when successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.modeselection_menu, menu);
        return true;
    }

    /**
     * Checks if exit game is clicked on the action bar
     * @param item
     * @return true when game is sucessfully exited
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.mi_exitApplicationModeSelection)
        {
            Intent exitAppIntent = new Intent(this, UserManagementActivity.class);
            exitAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            exitAppIntent.putExtra("EXIT_CALL", true);
            startActivity(exitAppIntent);
            finish();
            return true;
        }
        if(id ==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks which game mode button is clicked based on which a new game will be created
     * @param v game mode button that is clicked
     */
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnTimed:{
                callPlayGameActivity("TIMED");
                break;
            }
            case R.id.btnMoves:{

                callPlayGameActivity("MOVES");
                break;
            }
            case R.id.btnEndless:{
                callPlayGameActivity("ENDLESS");
                break;
            }
        }
    }

    /**
     * Powerups button click event which takes the user to purchase powerups screen 
     * where the user can purchase powerups based on their total score 
     * @param view the powerups button that is clicked
     */
    public void btnPowerupsClick(View view) {
        Intent powerupIntent = new Intent(this,PurchasePowerUp.class);
        Intent playerMgrIntent = getIntent();
        powerupIntent.putExtra("USER_ID", playerMgrIntent.getIntExtra("USER_ID", 0));
        startActivity(powerupIntent);
    }

    /**
     * Calls play game activity which displays the new game to user based on the mode
     * that is selected
     * @param gameMode mode of game either timer, moves or endless
     */
    void callPlayGameActivity(String gameMode){
        Intent timedModeIntent = new Intent(ModeSelectionActivity.this, PlayGameActivity.class);
        timedModeIntent.putExtra("GAME_MODE", gameMode);
        Intent playerMgrIntent = getIntent();
        timedModeIntent.putExtra("USER_ID", playerMgrIntent.getIntExtra("USER_ID", 0));
        startActivity(timedModeIntent);
    }

}

