package se.android.worddrop;

import android.graphics.Color;

import java.util.Random;

/**
 * Stores information about the letter and color for each cell in the game grid
 * @author Shruti Rachh
 * @see Grid
 */
public class Tile {
    private char letter;
    private int tileColor;
    private Random rand=new Random();
    private static final String[] allowedTileColors={"#0002FD","#F28740","#68F441","#F14E41","#9D4F76","#FFCD00"};
    private static int letterFreq[]=new int[26];
    private static int total=0;
    
    /**
     * Initializes different frequencies for different set of letters
     * so as to ensure some words can be formed
     */
    public Tile(){
        initializeLetterFreq();
    }
    
    /**
     * Initializes the letter and color for the tile object
     * @param letter alphabet letter (A-Z)
     * @param tileColor index for color in allowedTileColors array
     */
    public Tile(char letter,int tileColor){
        this.letter=letter;
        this.tileColor=tileColor;
    }
    
    /**
     * Initializes different frequencies for different set of letters
     * so as to ensure some words can be formed
     * Example, giving more frequency for vowels as compared to consonants, especially,
     * uncommon letters such as 'z','q','v'
     */
    public void initializeLetterFreq(){
        total =0;
        for(int i=0;i<letterFreq.length;i++){
            if(i==0 || i==4 || i==8 || i==14 ){
                letterFreq[i]=70;
            }else if(i>=21 || i==16 || i==9|| i==20){
                letterFreq[i]=5;
            }else{
                letterFreq[i]=30;
            }
            total+=letterFreq[i];
        }
    }
    
    /**
     * Gets a random letter based on the frequencies assigned to different letters
     * @return letter character (A-Z)
     */
    public char getRandomLetter(){
        int randNum = rand.nextInt(total+1)+1;
        int it=0,count=0;
        while(count<randNum && it<letterFreq.length){
            count+=letterFreq[it++];
        }
        char letter=(char)((it-1)+65);
        return letter;
    }
    
    /**
     * Gets a random color from the array of allowedTileColors
     * @return the index for color in the allowedTileColors array
     */
    public int getRandomColor(){
        int randNum = rand.nextInt(allowedTileColors.length);
        tileColor=Color.parseColor(allowedTileColors[randNum]);
        return tileColor;
    }
    
    /**
     * Setter for letter variable
     * @param letter (A-Z)
     */
    public void setLetter(char letter){
        this.letter=letter;
    }
    
    /**
     * Getter for letter
     * @return letter (A-Z)
     */
    public char getLetter(){
        return letter;
    }
    
    /**
     * Setter for tile color
     * @param tileColor index for color in allowedTileColors array
     */
    public void setTileColor(int tileColor){
        this.tileColor=tileColor;
    }
    
    /**
     * Getter for tile color
     * @return index for color in allowedTileColors array
     */
    public int getTileColor(){
        return tileColor;
    }
}
