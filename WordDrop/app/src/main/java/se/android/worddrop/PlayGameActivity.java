package se.android.worddrop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Play game screen where the user tries to make as many words as possible
 * on the grid
 * @author Shruti Rachh, Soumya Achar, Pratik Sanghvi
 */
public class PlayGameActivity extends AppCompatActivity implements View.OnTouchListener{

    private static final String DEBUG_TAG = "PlayGameActivity";
    Grid grid;
    private Set<String> btnIdsSwipped=new HashSet<String>();
    private int gridWidth;
    private int gridRowsCount;
    private int gridColumnsCount;
    private int lastButtonId = 0;
    private String wordFormed = "";
    private int validWordsCount = 0;
    private WordTrie myTrie;
    private Map swipedButtonsColorMap;
    private Set swippedButtonsResourceIds;
    private GridLayout gridLayout;
    private Context context;
    private int score;
    private int userId;
    boolean isGestureMove = false;
    int remainingMoves;
    PowerUpManager powerup;
    private String gameMode;
    TextView tvGameMode, tvMovesOrTime, tvSwippedWord;
    Timer timer;
    int remainingGameTime;
    boolean pauseTimer=false;
    int colorPowerUpFlag = 0;
    int shrinkLetterFlag = 0;
    int movesCount = 0;
    private Set<String> deleteColorButtonSet =new HashSet<String>();
    private List<String> swipedWords = new ArrayList<String>();

    /**
     * Creates the layout for play game screen
     * @param savedInstanceState saves the state of application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playgame);

        //Initializing dictionary
        myTrie = new WordTrie();
        context=getApplicationContext();
        myTrie.buildTrie(context,myTrie);

        //Setting game layout and its dimensions relative to screen size so that it works for tablets as well
        gridLayout=(GridLayout)findViewById(R.id.gridview);
        gridRowsCount=gridLayout.getRowCount();
        gridColumnsCount=gridLayout.getColumnCount();
        grid=new Grid(gridRowsCount,gridColumnsCount);
        Tile tile[][]=grid.populateGrid();
        int[] dimensions=getScreenSize();
        gridWidth=(int)(dimensions[0]-(0.05*dimensions[0]));
        setDimensions((View) gridLayout, gridWidth, gridWidth);
        setGridCells(tile, gridWidth);
        setGameTabsDimensions(dimensions);
        setPowerupTabsDimensions(dimensions);

        //Initializing the Powerup controller object.
        powerup=new PowerUpManager(this);
        Intent ModeSelectionIntent = getIntent();
        userId = ModeSelectionIntent.getIntExtra("USER_ID", 0);
        powerup.setUserId(userId);
        setTxtViewPowerupCount();

        //Variable to track buttons which were swipped so that it's color can be reverted if word is not valid.
        swipedButtonsColorMap = new HashMap();
        swippedButtonsResourceIds = new HashSet();

        //Code to initiate game based on GameMode.
        Intent thisActivityIntent = getIntent();
        gameMode = thisActivityIntent.getStringExtra("GAME_MODE");

        tvGameMode = (TextView)findViewById(R.id.textView2);
        tvMovesOrTime = (TextView)findViewById(R.id.textView4);
        tvSwippedWord = (TextView)findViewById(R.id.tvSwippedWord);

        //Setting textviews based on gamemode.
        if(gameMode.equals("TIMED")){
            startTimerCountdown(91);
            tvGameMode.setText(getString(R.string.timer));
        }
        else if(gameMode.equals("MOVES")){
            remainingMoves = 30;
            tvGameMode.setText(getString(R.string.moves));
            tvMovesOrTime.setText(remainingMoves + "");
        }
        else if(gameMode.equals("ENDLESS")){
            tvGameMode.setText(getString(R.string.moves));
            tvMovesOrTime.setText(movesCount + "");
        }

    }

    /**
     * Pauses the timer in case of timed mode when a game is temporarily paused
     */
    @Override
    protected void onStop(){
        super.onStop();
        if(gameMode.equals("TIMED")) {
            pauseTimer=true;
        }
    }

    /**
     * Cancels the timer in case of timed mode when game is restarted
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(gameMode.equals("TIMED")) {
            timer.cancel();
        }
    }

    /**
     * Resumes the timer when the game is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        setTxtViewPowerupCount();
        if(gameMode.equals("TIMED")) {
            pauseTimer=false;
        }
    }

    /**
     * Creates play game menu
     * @param menu
     * @return true when successfully created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playgame_menu, menu);
        return true;
    }

    /**
     * Checks what option is selected on the action bar 
     * @param item
     * @return true when the action is successfully completed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.mi_newgame) {
            showRestartGameAlert();
            return true;
        }
        else if (id == R.id.mi_quitgame) {
            showQuitGameAlert();
            return true;
        }
        else if (id == R.id.mi_exitapplication) {
            showExitGameAlert();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets the game tabs dimensions relative to the screen size
     * @param screenDimensions
     */
    private void setGameTabsDimensions(int[] screenDimensions) {
        LinearLayout gameTabs = (LinearLayout) findViewById(R.id.gameDetails);
        int gameTabsWidth = screenDimensions[0];
        int gameTabsHeight = (int) (screenDimensions[1] * 0.07);
        setDimensions((View) gameTabs, gameTabsWidth, gameTabsHeight);
        int no = 1;
        for (int j = 0; j < gameTabs.getChildCount(); j++) {
            TextView txtView = (TextView)gameTabs.getChildAt(j);
            setDimensions((View) txtView, gameTabsWidth / 4, gameTabsHeight);
            txtView.setGravity(Gravity.CENTER);
            no++;
        }
    }

    /**
     * Sets powerup tabs dimensions relative to the screen size
     * @param screenDimensions
     */
    private void setPowerupTabsDimensions(int[] screenDimensions) {
        GridLayout gameTabs=(GridLayout)findViewById(R.id.powerups);
        int gameTabsWidth=screenDimensions[0];
        int gameTabsHeight=(int)(screenDimensions[1]*0.08);
        setDimensions((View) gameTabs, gameTabsWidth, gameTabsHeight);
        for(int i=0;i<gameTabs.getRowCount();i++){
            for(int j=0;j<gameTabs.getColumnCount();j++){
                String powerupId="";
                if(i==0)
                    powerupId = "PowerUp" + j+"Cnt";
                else
                    powerupId="PowerUp"+j;
                int resourceId = getResources().getIdentifier(powerupId, "id", getPackageName());
                TextView txtView = (TextView) findViewById(resourceId);
                setDimensions((View)txtView,gameTabsWidth/3,gameTabsHeight/2);
                txtView.setGravity(Gravity.CENTER);
            }
        }
    }

    /**
     * Sets grid dimensions relative to screen size
     * @param view grid view
     * @param width screen width
     * @param height screen height
     */
    private void setDimensions(View view,int width,int height){
        ViewGroup.LayoutParams layoutParams=view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height=height;
        view.setLayoutParams(layoutParams);
    }

    /**
     * Sets the remaining powerup count texts for display on the screen 
     */
    private void setTxtViewPowerupCount()
    {
        TextView tvScramble = (TextView) findViewById(se.android.worddrop.R.id.PowerUp0Cnt);
        tvScramble.setText(Integer.toString(powerup.getScrambleCnt()));

        TextView tvDeleteColor = (TextView) findViewById(se.android.worddrop.R.id.PowerUp1Cnt);
        tvDeleteColor.setText(Integer.toString(powerup.getDeleteColorCnt()));

        TextView tvShrinkLetter = (TextView) findViewById(se.android.worddrop.R.id.PowerUp2Cnt);
        tvShrinkLetter.setText(Integer.toString(powerup.getShrinkLetterCnt()));
    }

    /**
     * Gets the phone screen size
     * @return
     */
    private int[] getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int dimensions[]=new int[2];
        dimensions[0] = size.x;
        dimensions[1] = size.y;
        return dimensions;
    }

    /**
     * Sets the grid cells with the letters and colors based on the grid dimensions
     * @param tile tile matrix which stores letters and colors information
     * @param gridDimension dimensions of the grid
     */
    private void setGridCells(Tile tile[][],int gridDimension){
        for(int i=0;i<tile.length;i++) {
            for (int j = 0; j < tile[i].length; j++) {
                String btnId = "button" + i + j;
                int resourceId = getResources().getIdentifier(btnId, "id", getPackageName());
                Button gridBtn = (Button) findViewById(resourceId);
                int cellDimension=(gridDimension/5)-10;
                setDimensions((View) gridBtn, cellDimension, cellDimension);
                gridBtn.setBackgroundColor(tile[i][j].getTileColor());
                char[] letter={tile[i][j].getLetter()};
                gridBtn.setText(letter, 0, 1);
                gridBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
                gridBtn.setOnTouchListener(PlayGameActivity.this);
            }
        }
        if(gridLayout!=null){
            gridLayout.invalidate();
        }
    }

    /**
     * Checks if the tile object button is touched so as to detect the swipes that user
     * makes to form words
     * @param v view
     * @param event motion event to capture in what direction the user swiped
     * @return true when the tile object button is touched
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        TextView powerUpHelp=(TextView)findViewById(R.id.txtPowerupHelp);
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                //Checks whether the user has clicked on delete color powerup
                if (colorPowerUpFlag == 1){
                    int colorID = intersectedOrNot(event.getRawX(), event.getRawY());
                    if(colorID != -1)
                    {
                        powerUpHelp.setText("");
                        deleteColorButtonSet = powerup.deleteColor(this.grid.tile, btnIdsSwipped.iterator().next().toString());
                        Tile tilex[][]=grid.refillGrid(deleteColorButtonSet);
                        setGridCells(tilex, gridWidth);
                    }
                    colorPowerUpFlag = 0;
                    swipedButtonsColorMap = new HashMap();
                    int cntPowerCount = powerup.getDeleteColorCnt();
                    powerup.setDeleteColorCnt(cntPowerCount - 1);
                    TextView tvDeleteColor = (TextView) findViewById(se.android.worddrop.R.id.PowerUp1Cnt);
                    tvDeleteColor.setText(Integer.toString(cntPowerCount - 1));
                }
            
                //Checks whether the user has clicked on shrink letter powerup
                if (shrinkLetterFlag == 1) {
                    int shrinkID = intersectedOrNot(event.getRawX(), event.getRawY());
                    if(shrinkID != -1)
                    {
                        powerUpHelp.setText("");
                        String shrinkButton = btnIdsSwipped.iterator().next().toString();
                        Tile tilex[][]=grid.refillGrid(btnIdsSwipped);
                        setGridCells(tilex, gridWidth);
                    }
                    shrinkLetterFlag = 0;
                    swipedButtonsColorMap = new HashMap();
                    int  cntPowerCount = powerup.getShrinkLetterCnt();
                    powerup.setShrinkLetterCnt(cntPowerCount - 1);
                    TextView tvShrinkLetter = (TextView) findViewById(se.android.worddrop.R.id.PowerUp2Cnt);
                    tvShrinkLetter.setText(Integer.toString(cntPowerCount - 1));
                }
                return true;
            case (MotionEvent.ACTION_MOVE) :
                if(gameMode.equals("MOVES")){
                    isGestureMove = true;
                }
                int elementId = intersectedOrNot(event.getRawX(), event.getRawY());
                if(elementId != -1)
                {
                    Log.d(DEBUG_TAG, "Action was DOWN and it DID intersect");
                    Button btnSensed = (Button)findViewById(elementId);
                    String letter = btnSensed.getText().toString();
                    wordFormed = wordFormed + letter;
                }
                else{
                    Log.d(DEBUG_TAG, "Action was DOWN and it DID NOT intersect");
                }
                return true;
            case (MotionEvent.ACTION_UP) :
                //Checks whether the user has clicked on delete color powerup
                if (colorPowerUpFlag == 1){
                    int colorID = intersectedOrNot(event.getRawX(), event.getRawY());
                    if(colorID != -1)
                    {
                        powerUpHelp.setText("");
                        deleteColorButtonSet = powerup.deleteColor(this.grid.tile, btnIdsSwipped.iterator().next().toString());
                        Tile tilex[][]=grid.refillGrid(deleteColorButtonSet);
                        setGridCells(tilex, gridWidth);
                    }
                    colorPowerUpFlag = 0;
                    swipedButtonsColorMap = new HashMap();
                    int cntPowerCount = powerup.getDeleteColorCnt();
                    powerup.setDeleteColorCnt(cntPowerCount - 1);
                    TextView tvDeleteColor = (TextView) findViewById(se.android.worddrop.R.id.PowerUp1Cnt);
                    tvDeleteColor.setText(Integer.toString(cntPowerCount - 1));
                    return true;
                }
                //Checks whether the user has clicked on shrink letter powerup
                if (shrinkLetterFlag == 1) {
                    int shrinkID = intersectedOrNot(event.getRawX(), event.getRawY());
                    if(shrinkID != -1)
                    {
                        powerUpHelp.setText("");
                        String shrinkButton = btnIdsSwipped.iterator().next().toString();
                        Tile tilex[][]=grid.refillGrid(btnIdsSwipped);
                        setGridCells(tilex, gridWidth);
                    }
                    shrinkLetterFlag = 0;
                    swipedButtonsColorMap = new HashMap();
                    int  cntPowerCount = powerup.getShrinkLetterCnt();
                    powerup.setShrinkLetterCnt(cntPowerCount - 1);
                    TextView tvShrinkLetter = (TextView) findViewById(se.android.worddrop.R.id.PowerUp2Cnt);
                    tvShrinkLetter.setText(Integer.toString(cntPowerCount - 1));
                    return true;
                }
                lastButtonId = 0;
                tvSwippedWord.setText(wordFormed);
                movesCount = movesCount + 1;
                
                //Searches for the swipped word in the wordlist
                boolean isWordValid = myTrie.searchWord(wordFormed.toLowerCase());

                if(!isWordValid){
                    rollbackButtonColors();
                }else{
                    if(grid!=null){
                        Tile t[][]=grid.refillGrid(btnIdsSwipped);
                        setGridCells(t,gridWidth);
                        validWordsCount = validWordsCount + 1;
                        
                        // Show a list of swipped words
                        swipedWords.add(wordFormed.toLowerCase());  
                        updateScore(btnIdsSwipped.size());
                    }
                }
                wordFormed = "";
                swippedButtonsResourceIds = new HashSet();
                swipedButtonsColorMap=new HashMap();
                btnIdsSwipped=new HashSet<String>();
                if(gameMode.equals("MOVES")){
                    if(isGestureMove){
                        remainingMoves = remainingMoves - 1;
                        isGestureMove = false;
                        tvMovesOrTime.setText(remainingMoves + "");
                        if (remainingMoves == 0){
                            callGameOverActivity();
                        }
                    }
                }
                if(gameMode.equals("ENDLESS")){
                    tvMovesOrTime.setText(movesCount + "");
                }
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
    
    /**
     * Updates the score depending on the the number of letters that were
     * swiped to form a word
     * @param noOfLettersSwipped number of letters swiped in a word
     */
    private void updateScore(int noOfLettersSwipped){

        TextView scoreValTxt=(TextView)findViewById(R.id.textView3);
        score=Integer.parseInt(scoreValTxt.getText().toString());
        score=score+(noOfLettersSwipped*10);
        scoreValTxt.setText(""+score);
    }

    /**
     * Gets the id of the grid cell that was touched or swiped
     * @param x row of grid
     * @param y column of grid
     * @return id of grid cell
     */
    private int intersectedOrNot(float x, float y) {
        for (int i = 0; i < gridRowsCount; i++) {
            for (int j = 0; j < gridColumnsCount; j++) {
                String btnId = "button" + i + j;
                int resourceId = getResources().getIdentifier(btnId, "id", getPackageName());
                Button gridBtn = (Button) findViewById(resourceId);
                int[] location = new int[2];
                gridBtn.getLocationInWindow(location);
                int left = location[0];
                int top = location[1];
                int right = left + gridBtn.getWidth();
                int bottom = top + gridBtn.getHeight();
                if (x >= (left + 10) && x <= (right - 10) && y >= (top + 10) && y <= (bottom - 10)){
                    if(lastButtonId != resourceId && !swippedButtonsResourceIds.contains(resourceId)){
                        lastButtonId = resourceId;
                        swippedButtonsResourceIds.add(resourceId);
                        btnIdsSwipped.add(btnId);
                        ColorDrawable draw = (ColorDrawable)gridBtn.getBackground();
                        swipedButtonsColorMap.put(resourceId, draw.getColor());
                        gridBtn.setBackgroundColor(Color.parseColor("#FFFF00"));
                        return resourceId;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Reverts the tile colors to the previous color if the word swiped is incorrect
     */
    private void rollbackButtonColors(){
        Iterator<Map.Entry<Integer, Integer>> iterator = swipedButtonsColorMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Integer, Integer> entry = iterator.next();
            Button gridBtn = (Button) findViewById(entry.getKey());
            gridBtn.setBackgroundColor(entry.getValue());
        }
    }

    /**
     * Starts the timer count down
     * @param seconds number of seconds to be set
     */
    void startTimerCountdown(final int seconds){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int countDownValue = seconds;

            public void run() {
                if (!pauseTimer) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMovesOrTime.setText(countDownValue + "");
                        }
                    });
                    System.out.println("Remaining game time: " + countDownValue--);
                    remainingGameTime = countDownValue;
                    if (countDownValue == 0) {
                        timer.cancel();
                        callGameOverActivity();
                    }
                }
            }
        }, 0, 1000);
    }

    /**
     * Stops the timer
     */
    void stopTimer()
    {
        pauseTimer=true;
    }

    /**
     * Calls game over activity when game ends and sends the score, user id and game mode just
     * in case user decides to play again in the same game mode
     */
    void callGameOverActivity(){
        Intent moveToGameOver = new Intent(PlayGameActivity.this, GameOverActivity.class);
        moveToGameOver.putExtra("Score", score);
        moveToGameOver.putExtra("USER_ID", userId);
        moveToGameOver.putExtra("GAME_MODE", gameMode);
        startActivity(moveToGameOver);
        finish();
    }

    /**
     * Show words button click event which displays list of words already swiped by the user
     * @param view show words button
     */
    public void btnShowWordsClick(View view) {
        System.out.println("Number of words formed "+swipedWords.size());
        showSwippedWords(swipedWords);
    }

    /**
     * Scramble powerup button click event which scrambles the grid to help user finds some
     * words when they are stuck
     * @param view scramble button
     */
    public void btnScrambleClick(View view) {
        int cntPowerCount = powerup.getScrambleCnt();
        if (cntPowerCount > 0) {
            showScrambleConfirmation(cntPowerCount);
        } else {
            if(gameMode.equals("TIMED")) {
                stopTimer();
            }
            showPowerUpAlert();
        }
    }

    /**
     * Delete color powerup button click event which deletes all the grid cells that have same
     * color as the selected cell
     * @param view delete color button
     */
    public void btnDeleteColorClick(View view) {
        int cntPowerCount = powerup.getDeleteColorCnt();
        if(cntPowerCount > 0) {
            TextView powerUpHelp=(TextView)findViewById(R.id.txtPowerupHelp);
            powerUpHelp.setText(getString(R.string.deleteColorHint));
            Toast.makeText(context,getString(R.string.selectColor),Toast.LENGTH_SHORT).show();
            colorPowerUpFlag = 1;
        }
        else
        {
            if(gameMode.equals("TIMED")) {
                stopTimer();
            }
            showPowerUpAlert();
        }
    }

    /**
     * Shrink letter powerup button click event which disappears the selected cell
     * @param view shrink letter button
     */
    public void btnShrinkLetterClick(View view) {
        int cntPowerCount = powerup.getShrinkLetterCnt();
        if(cntPowerCount > 0) {
            TextView powerUpHelp=(TextView)findViewById(R.id.txtPowerupHelp);
            powerUpHelp.setText(getString(R.string.shrinkLetterHint));
            Toast.makeText(context,getString(R.string.selectLetter),Toast.LENGTH_SHORT).show();
            shrinkLetterFlag = 1;
        }
        else
        {
            if(gameMode.equals("TIMED")) {
                stopTimer();
            }
            showPowerUpAlert();
        }
    }

    /**
     * Shows powerup alert when there are not sufficient powerups left to use and gives an option
     * to purchase powerup based on the total score of the user
     */
    private void showPowerUpAlert()
    {
        pauseTimer = true;
        AlertDialog alertDialog = new AlertDialog.Builder(PlayGameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.powerUpAlert));
        alertDialog.setMessage(getString(R.string.powerUpAlertMsg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Buy),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent moveToPurchsePowerup = new Intent(PlayGameActivity.this, PurchasePowerUp.class);
                        moveToPurchsePowerup.putExtra("USER_ID", userId);
                        PlayGameActivity.this.startActivity(moveToPurchsePowerup);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.Cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pauseTimer = false;
                    }
                });
        alertDialog.show();
    }

    /**
     * Asks confirmation when user uses scramble powerup so as to prevent accidental
     * scramble of the grid
     * @param cntPowerCount scramble powerup count
     */
    private void showScrambleConfirmation(final int cntPowerCount)
    {
        pauseTimer = true;
        AlertDialog alertDialog = new AlertDialog.Builder(PlayGameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.scramble));
        alertDialog.setMessage(getString(R.string.scrambleMsg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Tile[][] tile = powerup.scramble(grid, myTrie);
                        setGridCells(tile, gridWidth);
                        powerup.setScrambleCnt(cntPowerCount - 1);
                        TextView tvScramble = (TextView) findViewById(se.android.worddrop.R.id.PowerUp0Cnt);
                        tvScramble.setText(Integer.toString(cntPowerCount - 1));
                        pauseTimer = false;
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.No),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pauseTimer = false;
                    }
                });
        alertDialog.show();
    }

    /**
     * Shows quit game confirmation alert
     */
    private void showQuitGameAlert()
    {
        pauseTimer = true;
        AlertDialog alertDialog = new AlertDialog.Builder(PlayGameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.quitGameConfirm));
        alertDialog.setMessage(getString(R.string.quitGameConfirmMsg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        callGameOverActivity();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.No),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pauseTimer=false;
                    }
                });
        alertDialog.show();
    }

    /**
     * Shows exit game confirmation alert
     */
    private void showExitGameAlert()
    {
        pauseTimer = true;
        AlertDialog alertDialog = new AlertDialog.Builder(PlayGameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.exitGameConfirm));
        alertDialog.setMessage(getString(R.string.exitGameConfirmMsg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent exitAppIntent = new Intent(PlayGameActivity.this, UserManagementActivity.class);
                        exitAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        exitAppIntent.putExtra("EXIT_CALL", true);
                        startActivity(exitAppIntent);
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.No),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pauseTimer=false;
                    }
                });
        alertDialog.show();
    }

    /**
     * Shows restart game confirmation alert
     */
    private void showRestartGameAlert()
    {
        pauseTimer=true;
        AlertDialog alertDialog = new AlertDialog.Builder(PlayGameActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.restartGameConfirm));
        alertDialog.setMessage(getString(R.string.restartGameConfirmMsg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent timedModeIntent = new Intent(PlayGameActivity.this, PlayGameActivity.class);
                        timedModeIntent.putExtra("GAME_MODE", gameMode);
                        Intent playerMgrIntent = getIntent();
                        timedModeIntent.putExtra("USER_ID", playerMgrIntent.getIntExtra("USER_ID", 0));
                        startActivity(timedModeIntent);
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.No),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pauseTimer=false;
                    }
                });
        alertDialog.show();
    }

    /**
     * Shows list of swipped words when the show words button is clicked
     * @param lstSwippedWords list of swiped words
     */
    private void showSwippedWords(List<String> lstSwippedWords)
    {
        pauseTimer=true;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlayGameActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(getString(R.string.swippedWordsLst));
        alertDialog.setItems(lstSwippedWords.toArray(new CharSequence[lstSwippedWords.size()]), null);
        alertDialog.setNegativeButton(getString(R.string.Ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pauseTimer = false;
                    }
                });
        AlertDialog alert=alertDialog.create();
        alert.show();
        int[] screenSize=getScreenSize();
        System.out.println("width "+screenSize[0]+"\nheight "+screenSize[1]);
        alert.getWindow().setLayout(screenSize[0] - 200, screenSize[1]/2);
        alert.getWindow().setGravity(Gravity.CENTER);
    }
}
