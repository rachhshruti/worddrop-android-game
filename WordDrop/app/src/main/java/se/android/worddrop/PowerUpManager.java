package se.android.worddrop;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/**
 * This class contains methods that implements the powerups: scramble, delete color and
 * shrink letter
 * @author Shruti Rachh, Soumya Achar
 */
public class PowerUpManager {

    private int userId;
    private int shrinkLetterCnt;
    private int deleteColorCnt;
    private int scrambleCnt;
    private int totalScore;
    private Random random=new Random();
    private List<Tile> tilesArr;
    private int validWordsCnt = 0;
    private DBHelper dbHelper;

    /**
     * Initializes DBHelper to connect with the database
     * @param context used to open or create a database
     */
    public PowerUpManager(Context context)
    {
        dbHelper = new DBHelper(context);
    }

    /**
     * Enum that stores the count of each of the powerups
     * @author Shruti Rachh
     */
    public static enum PowerupType {

        SCRAMBLE("SCRAMBLES_NUM",0),
        DELETE_COLOR("DELETECOLORS_NUM",1),
        SHRINK_LETTER("SHRINKS_NUM",2);

        private String stringValue;
        private int intValue;
        
        private PowerupType(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    /**
     * Closes the database connection
     */
    protected void finalize() throws Throwable{
        try{
            dbHelper.closeConnections();}
        catch (Throwable t)
        {
            Log.d("PowerUpManager", t.getMessage());
        }
        finally {
            super.finalize();
        }

    }

    /**
     * Getter method for user id
     * @return user id
     */
    public int getUserId(){
        return userId;
    }

    /**
     * Setter method for user id
     * @param userId id of user to be set
     */
    public void setUserId(int userId){
        this.userId=userId;
    }

    /**
     * Gets the shrink letter powerup count
     * @return shrink letter powerup count
     */
    public int getShrinkLetterCnt(){
        shrinkLetterCnt=getPowerupCount(PowerupType.SHRINK_LETTER.toString());
        return shrinkLetterCnt;
    }

    /**
     * Sets the shrink letter powerup count
     * @param shrinkLetterCnt count to be set
     */
    public void setShrinkLetterCnt(int shrinkLetterCnt){
        updatePowerupCount(PowerupType.SHRINK_LETTER.toString(),shrinkLetterCnt);
        this.shrinkLetterCnt=shrinkLetterCnt;
    }

    /**
     * Gets the delete color powerup count
     * @return delete color powerup count
     */
    public int getDeleteColorCnt(){
        deleteColorCnt=getPowerupCount(PowerupType.DELETE_COLOR.toString());
        return deleteColorCnt;
    }

    /**
     * Sets delete color powerup count
     * @param deleteColorCnt count to be set
     */
    public void setDeleteColorCnt(int deleteColorCnt){
        updatePowerupCount(PowerupType.DELETE_COLOR.toString(),deleteColorCnt);
        this.deleteColorCnt=deleteColorCnt;
    }

    /**
     * Gets scramble powerup count
     * @return scramble powerup count
     */
    public int getScrambleCnt(){
        scrambleCnt=getPowerupCount(PowerupType.SCRAMBLE.toString());
        return scrambleCnt;
    }

    /**
     * Sets the scramble powerup count
     * @param scrambleCnt count to be set
     */
    public void setScrambleCnt(int scrambleCnt){
        updatePowerupCount(PowerupType.SCRAMBLE.toString(),scrambleCnt);
        this.scrambleCnt=scrambleCnt;
    }

    /**
     * Gets the total score of the user using user id
     * @return
     */
    public int getTotalScore(){
        totalScore = dbHelper.getTotalScore(userId);
        return totalScore;
    }

    /**
     * Sets the total score in database using user id
     * @param totalScore total score to be updated
     */
    public void setTotalScore(int totalScore){
        dbHelper.updateTotalScore(totalScore,userId);
        this.totalScore =totalScore;
    }

    /**
     * Gets the powerup count based on the type of powerup
     * @param powerupType scramble, delete color or shrink letter
     * @return powerup count
     */
    private int getPowerupCount(String powerupType){
       return dbHelper.getPowerupCount(powerupType,userId);
    }

    /**
     * Update powerup count in the database based on the type of powerup
     * @param powerupType scramble, delete color or shrink letter
     * @param count powerup count to be updated in database
     */
    private  void updatePowerupCount(String powerupType,int count) {
        dbHelper.updatePowerUpCount(count,powerupType,userId);
    }

    /**
     * Shuffles the grid such that some valid words are formed
     * @param grid game grid containing letters
     * @param trie word trie object to check for possible words 
     * that can be formed in the grid
     * @return Tile object array of the grid after scramble
     */
    public Tile[][] scramble(Grid grid, WordTrie trie) {
        Tile[][] tile = grid.getTile();
        tilesArr = convertGridToArray(tile);
        List<Integer> randInd = generateRandomIndArr(tile);
        validWordsCnt=0;
        List<Tile> wordTiles = findWords(trie);
        if(wordTiles!=null && !wordTiles.isEmpty()){
            int size = wordTiles.size();
            int cnt = 0;
            int x=0,y=0;
            List<Integer> adjCells = new ArrayList<Integer>();
            
            //Places the tiles that form words in adjacent positions on the grid
            for (int i = 0; i < validWordsCnt; i++) {
                int randomNum = random.nextInt(randInd.size());
                for (int j = 0; j < size / validWordsCnt; j++) {
                    if (j == 0) {
                        int ranPos = randInd.get(randomNum);
                        x = ranPos / tile.length;
                        y = ranPos % tile.length;
                        randInd.remove(randomNum);
                    } else {
                        randomNum = random.nextInt(adjCells.size());
                        int ranPos = adjCells.get(randomNum);
                        x = ranPos / tile.length;
                        y = ranPos % tile.length;
                        adjCells.remove(randomNum);
                        int index = randInd.indexOf(ranPos);
                        randInd.remove(index);
                    }
                    tile[x][y] = wordTiles.get(cnt++);
                    adjCells = getAdjacentCells(x, y, tile.length, randInd);
                }
            }
        }

        //Places the remaining tiles in random positions
        for (int i = 0; i < tilesArr.size(); i++) {
            int randomNum = random.nextInt(randInd.size());
            int ranPos = randInd.get(randomNum);
            tile[ranPos / tile.length][ranPos % tile.length] = tilesArr.get(i);
            randInd.remove(randomNum);
        }
        grid.setTile(tile);
        return tile;
    }

    /**
     * Gets the adjacent cells given a grid coordinate to place the word in adjacent positions
     * Uses a random index so as to have different adjacent orientations for a word each time
     * @param gridx row index of grid
     * @param gridy column index of grid
     * @param gridSize size of the grid
     * @param randInd random index
     * @return list of adjacent indices in the grid where the word can be placed
     */
    private List<Integer> getAdjacentCells(int gridx, int gridy, int gridSize,List<Integer> randInd) {
        Point neighbor[] = new Point[4];
        neighbor[0]=new Point((gridx-1),gridy);
        neighbor[1]=new Point((gridx+1),gridy);
        neighbor[2]=new Point(gridx,(gridy-1));
        neighbor[3]=new Point(gridx,(gridy+1));
        List<Integer> adjCell = new ArrayList<Integer>();
        for (int i = 0; i < neighbor.length; i++){
            int val=(neighbor[i].x*gridSize)+neighbor[i].y;
            if(neighbor[i].x>=0 && neighbor[i].y>=0 && neighbor[i].x<gridSize && neighbor[i].y<gridSize && randInd.contains(val)){
                adjCell.add(val);
            }
        }
        return adjCell;
    }

    /**
     * Find words on the grid so that when it is scrambled, few words can be placed in
     * adjacent positions
     * @param trie word trie object used to find words
     * @return list of words if found, else null
     */
    private List<Tile> findWords(WordTrie trie){
        if(tilesArr!=null && trie!=null) {
            List<Tile> words = new ArrayList<Tile>();
            outer:
            for (int i = 0; i < tilesArr.size(); i++) {
                String tmp = "";
                for (int j = 0; j < tilesArr.size(); j++) {
                    if (j != i) {
                        tmp = "" + tilesArr.get(i).getLetter() + tilesArr.get(j).getLetter();
                        if (trie.findPossibleWords(tmp.toLowerCase()) == 1) {
                            if (j==tilesArr.size()-1){
                                return null;
                            }
                            continue;
                        }

                    } else {
                        if (j==tilesArr.size()-1){
                            return null;
                        }
                        continue;
                    }
                    for (int k = 0; k < tilesArr.size(); k++) {
                        if (k != i && k != j) {
                            tmp = tmp + tilesArr.get(k).getLetter();
                            if (trie.searchWord(tmp.toLowerCase())) {
                                words.add(tilesArr.get(i));
                                words.add(tilesArr.get(j));
                                words.add(tilesArr.get(k));
                                tilesArr.remove(i);
                                tilesArr.remove(j);
                                tilesArr.remove(k);
                                validWordsCnt++;
                            }
                        }
                        if (validWordsCnt == 2) {
                            break outer;
                        }
                    }
                }
            }
            return words;
        }
        return null;
    }

    /**
     * Used by scramble method to shuffle the tiles in the grid
     * @param tile tile matrix in the grid which stores letters and colors information
     * @return list of indices generated randomly
     */
    private List<Integer> generateRandomIndArr(Tile[][] tile){
        List<Integer> randInd=new ArrayList<Integer>();
        for(int i=0;i<(tile.length*tile[0].length);i++){
            randInd.add(i);
        }
        return randInd;
    }

    /**
     * Converts grid into array, used by scramble method
     * @param tile tile matrix in the grid
     * @return tile array
     */
    private List<Tile> convertGridToArray(Tile[][] tile){
        List<Tile> tilesArr=new ArrayList<Tile>();
        for(int i=0;i<tile.length;i++){
            for(int j=0;j<tile[i].length;j++){
                tilesArr.add(tile[i][j]);
            }
        }
        return tilesArr;
    }
	
    /**
     * Delete color power implemented which deletes all the cells in the
     * grid having the same color as the selected cell
     * @param changeTile tile matrix of the grid
     * @param button tile that is pressed
     * @return set of tiles to be deleted
     */
	public Set deleteColor (Tile changeTile [][], String button) {
       try {
           Thread.sleep(400);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       Set<String> deleteColorButtonSet =new HashSet<String>();

       // Gets all the buttons with Tile colors of button
       int row = button.charAt(button.length()-2)-'0';
       int column = button.charAt(button.length()-1)-'0';
       int tileColorSelected = changeTile[row][column].getTileColor();
       for ( int i = 0 ; i<5 ; i++){
           for ( int j =0; j<5 ; j++)
           {
               if (changeTile[i][j].getTileColor() ==tileColorSelected) {
                   String addButton = "Button" + i + j;
                   deleteColorButtonSet.add(addButton);
               }
           }

       }
       return deleteColorButtonSet;
   }
}
