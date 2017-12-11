package se.android.worddrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Stores information related to the game grid containing set of random letters with colors
 * and contains logic for refilling the grid when words are formed
 * @author Shruti Rachh
 * @see Tile
 */
public class Grid {
    public Tile[][] tile;
    private Tile t=new Tile();

    /**
     * Initializes the grid with Tile objects given the rows and columns.
     * Tile object stores the information such as letter and color
     * @param rows the number of rows in the grid
     * @param columns the number of columns in the grid
     */
    public Grid(int rows,int columns){
        tile=new Tile[rows][columns];
    }

    /**
     * Sets the tile object matrix
     * @param tile tile object matrix
     */
    public void setTile(Tile[][] tile)
    {
        this.tile = tile;
    }
    
    /**
     * Gets the tile object matrix
     * @return tile object matrix
     */
    public Tile[][] getTile(){
        return tile;
    }
    
    /**
     * Populates the grid object with the new set of letters and colors each time 
     * user starts a new game
     * @return the tile object matrix which stores the set of letters and colors of the grid
     */
    public Tile[][] populateGrid(){
        for(int i=0;i<tile.length;i++){
            for(int j=0;j<tile[i].length;j++){
                tile[i][j]=new Tile(t.getRandomLetter(),t.getRandomColor());
            }
        }
        return tile;
    }

    /**
     * Refills the grid from the top once a word is formed by first bringing down any of the letters
     * above the word and then filling it with new letters 
     * @param btnIdsSwipped the ids of the letters that were swiped to form the word
     * @return refilled tile matrix object
     */
    public Tile[][] refillGrid(Set<String> btnIdsSwipped){
        if(tile!=null){

            List<String> lst=new ArrayList<String>();
            lst.addAll(btnIdsSwipped);
            Collections.sort(lst);
            Iterator<String> it=lst.iterator();
            while(it. hasNext()){
                String btnId=it.next();
                int len=btnId.length();
                int x=Character.getNumericValue(btnId.charAt(len-2));
                int y=Character.getNumericValue(btnId.charAt(len-1));
                int i;
                for(i=x;i>0;i--){
                    tile[i][y]=tile[i-1][y];
                    String id="button"+i+y;
                    System.out.println("Button id in grid: "+id);
                }
                tile[i][y]=new Tile(t.getRandomLetter(),t.getRandomColor());
            }
        }
        return tile;
    }
}
