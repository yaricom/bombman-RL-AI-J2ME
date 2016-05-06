/**
 * $Id: StringItems.java 2 2005-06-30 12:47:28Z yaric $
 */
package ng.games.bombman;

/**
 * Holder for string items indices.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public interface StringItems
{
  //
  // Main menu items
  //
  public static final int CONTINUE = 0;
  public static final int NEW_GAME = 1;
  public static final int TOP_SCORES = 2;
  public static final int OPTIONS = 3;
  public static final int HELP = 4;
  public static final int ABOUT = 5;

  //
  // Options menu
  //
  public static final int LANGUAGE = 6;
  public static final int MUSIC = 7;
  public static final int SOUND = 8;

  //
  // Pause menu
  //
  public static final int RESUME = 9;
  public static final int RESTART = 10;
  public static final int MAIN_MENU = 11;
  public static final int EXIT_GAME = 12;

  //
  // Commands
  //
  public static final int SELECT_COMMAND = 13;
  public static final int YES_COMMAND = 14;
  public static final int CHANGE_COMMAND = 15;
  public static final int START_COMMAND = 16;
  public static final int SAVE_COMMAND = 17;

  public static final int PAUSE_COMMAND = 18;
  public static final int OK_COMMAND = 19;
  public static final int NO_COMMAND = 20;
  public static final int BACK_COMMAND = 21;
  public static final int EXIT_COMMAND = 22;

  //
  // String items
  //
  public static final int S_ITEM_LOADING = 23;
  public static final int S_ITEM_PRESS_ANY_KEY = 24;
  public static final int S_ITEM_NEW_GAME_PROMPT = 25;
  public static final int S_ITEM_LEVEL = 26;
  public static final int S_ITEM_YOU_WIN = 27;
  public static final int S_ITEM_YOU_LOOSE = 28;
  public static final int S_ITEM_PLAY_AGAIN = 29;
  public static final int S_ITEM_YOUR_SCORE = 30;
  public static final int S_ITEM_STORE_SCORE = 31;
  public static final int S_ITEM_ENTER_NAME = 32;
}