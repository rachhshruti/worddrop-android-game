package se.android.worddrop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Game over screen
 * Displays the following: 
 * 1. Current game score
 * 2. Total score of user
 * 3. Powerups remaining
 * 4. Play again, home and exit buttons
 * @author Suman Gaonkar
 */
public class GameOverActivity extends Activity {

    int userId;
    PowerUpManager powerup;
    
    /**
     * Creates the layout for game over screen and gets all the needed data
     * @param savedInstanceState saves the state of application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(se.android.worddrop.R.layout.activity_gameover);
        getScoreDetails();
        powerup=new PowerUpManager(this);
        powerup.setUserId(userId);
        setTxtViewPowerupCount();
    }

    /**
     * Gets the remaining count of all the powerups for the user
     */
    private void setTxtViewPowerupCount()
    {
    	// Gets scramble powerup count
        TextView tvScramble = (TextView) findViewById(R.id.tvScrambleCnt);
        tvScramble.setText(Integer.toString(powerup.getScrambleCnt()));

        // Gets the delete color powerup count
        TextView tvDeleteColor = (TextView) findViewById(R.id.tvDeleteColorCnt);
        tvDeleteColor.setText(Integer.toString(powerup.getDeleteColorCnt()));

        // Gets the shrink letter powerup count
        TextView tvShrinkLetter = (TextView) findViewById(R.id.tvShrinkLetterCnt);
        tvShrinkLetter.setText(Integer.toString(powerup.getShrinkLetterCnt()));
    }

    /**
     * Gets the current score and total score of the user from the database
     * and displays it on game over screen
     */
    private void getScoreDetails(){
        Intent intent=getIntent();
        int score=intent.getIntExtra("Score", 0);
        userId=intent.getIntExtra("USER_ID",0);
        DBHelper helper=new DBHelper(this);
        int totalScore=helper.getTotalScore(userId);
        System.out.println("Total Score: "+totalScore);
        TextView currScore=(TextView)findViewById(R.id.currScoreVal);
        currScore.setText(""+score);
        totalScore=totalScore+score;
        TextView totScore=(TextView)findViewById(R.id.totalScoreVal);
        totScore.setText(""+totalScore);
        helper.updateTotalScore(totalScore,userId);
    }

    /**
     * Home button click event which takes the user back to home screen
     * @param view 
     */
    public void btnHomeClick(View view) {
        Intent plyermanagementIntent = new Intent(this, UserManagementActivity.class);
        startActivity(plyermanagementIntent);
        finish();
    }

    /**
     * Exit game button click event which exits the user out of the game
     * @param view
     */
    public void btnExitGameClick(View view) {
        Intent exitAppIntent = new Intent(this, UserManagementActivity.class);
        exitAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        exitAppIntent.putExtra("EXIT_CALL", true);
        startActivity(exitAppIntent);
        finish();
    }

    /**
     * Play again button click event which starts a new game for the user
     * @param view
     */
    public void btnPlayAgainClick(View view) {
        Intent playAgainIntent = new Intent(this, PlayGameActivity.class);
        Intent playerMgrIntent = getIntent();
        playAgainIntent.putExtra("USER_ID", playerMgrIntent.getIntExtra("USER_ID", 0));
        playAgainIntent.putExtra("GAME_MODE", playerMgrIntent.getStringExtra("GAME_MODE"));
        startActivity(playAgainIntent);
        finish();
    }
}
