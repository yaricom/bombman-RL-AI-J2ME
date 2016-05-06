/**
 * $Id: BombmanGameController.java 223 2005-07-14 15:42:03Z yaric $
 */
package ng.games.bombman;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import ng.games.bombman.sprites.*;
import ng.games.bombman.screens.BombmanScreen;

/**
 * Controls game rendering and updates logic. Draws all in-game menus as well as
 * forces game engine to update itself when appropriate.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class BombmanGameController
{
  /** Parent game instance */
  public final BombmanGame game;
  /** Game engine */
  private final GameEngine engine;

  /** Game controller state flag */
  private byte state;
  /** Game controller state to be set next after current state processing */
  private byte nextState;
  /** Undefined state */
  private static final byte STATE_UNDEFINED = 0;
  /** Load level specific data state */
  public static final byte STATE_LOAD_LEVEL_DATA = 1;
  /** Show level intro fade in state */
  public static final byte STATE_LEVEL_INTRO_FADE_IN = 2;
  /** Show level intro state */
  public static final byte STATE_LEVEL_INTRO = 3;
  /** Show level intro fade out state */
  public static final byte STATE_LEVEL_INTRO_FADE_OUT = 4;

  /** Show game state */
  public static final byte STATE_ACTIVE_GAME = 5;
  /** Pause menu state */
  public static final byte STATE_PAUSE_MENU = 6;
  /** Resume game state */
  public static final byte STATE_RESUME_GAME = 7;
  /** Starts level without graphics resources loading state */
  public static final byte STATE_START_LEVEL_WITHOUT_RES_LOADING = 9;

  /** Show game results with high score state */
  public static final byte STATE_SHOW_GAME_RESULTS_HIGH_SCORE = 10;
  /** Show game results with ordinary score state */
  public static final byte STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE = 11;
  /** Show game loose state */
  public static final byte STATE_SHOW_GAME_LOOSE = 12;
  /** Show enter name screen */
  public static final byte STATE_ENTER_NAME = 13;
  /** Save entered name and show top scores screen */
  public static final byte STATE_SAVE_NAME_EXIT_TO_TOP_SCORES = 14;

  /** Exit to the main menu state */
  public static final byte STATE_EXIT_TO_MAIN_MENU = 15;
  /** Exit game state */
  public static final byte STATE_EXIT_GAME = 16;

  /** Flag to indicate that this screen in the valid state now */
  private boolean valid;

  /** Time passed from start of current game */
  private int time;
  /** Last time when some event occurs */
  private int lastTime;
  /** Number of passed ticks */
  private int ticks;
  /** Current game score */
  private int score;

  /** Repaint definitions. Holds definitions which screen areas should be repainted */
  private int repaintDefs;
  /** Constants to define total repaint flag */
  private static final int REPAINT_TOTAL = 1;
  /** Constants to define repaint generic active area */
  private static final int REPAINT_GENERIC_ACTIVE = 1 << 1;

  /** Box used to draw level caption animation */
  private int levelTextBoxX, levelTextBoxWidth, levelTextBoxHalfWidth;
  /** Array to hold screen images */
  private Image[] screenImages;
  /** Level caption */
  private String levelCaption;

  /** Index of main/settings menu current selection  */
  private int menuIndex;

  /** Array to hold indices of user name letters in letters array */
  private int[] userNameIndexes = new int[ 4 ];
  /** Current user name letter indice */
  private int currentUserNameIndex;
  /** Letters to input user name */
  private static final String[] INPUT_LETTERS = new String[] {
       "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
       "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", " "};
  /** The array to hold width of all input letters */
  private static final byte[] inputLettersWidth = new byte[ INPUT_LETTERS.length ];
  /** Maximal width of name letter from provided set */
  private final int maxLetterWidth;
  /** Left x coordinate for enter name field */
  private final int enterNameX;


  private int hideNotifyCount;

  /**
   * Constructs new game controller.
   * @param parent the parent game instance.
   */
  public BombmanGameController( BombmanGame parent )
  {
    this.game = parent;
    this.engine = new GameEngine( this );

    // calculate maximal width of letter
    int tmp = 0;
    game.textManager.setFont( TextManager.FONT_TEXT );
    for( int i = INPUT_LETTERS.length - 1; i >= 0 ; i-- ){
      inputLettersWidth[ i ] = (byte)game.textManager.stringWidth( INPUT_LETTERS[ i ] );
      if( inputLettersWidth[ i ] > tmp )
        tmp = inputLettersWidth[ i ];
    }
    this.maxLetterWidth = tmp;
    this.enterNameX = ( GameConfig.gameMenuTextAreaWidth -
                      this.maxLetterWidth * 4 ) / 2;
  }

  /**
   * Method to update game control logic.
   * @param wasteTime time which was spent on previous game cycle.
   */
  public final void update( int wasteTime )
  {
    // update time
    this.time += wasteTime;

    switch( state ){
      case STATE_UNDEFINED:
        this.state = this.nextState;
        break;

      case STATE_LOAD_LEVEL_DATA:
        if( valid ){
          if( this.engine.loadLevel() ){
            // engine preloads all images and is ready for next screen processing
            this.invalidate();
            this.nextState = STATE_LEVEL_INTRO_FADE_IN;
          }
          else
            this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
        }
        else{
          // get background images
          this.prepareScreenImages();
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_LEVEL_INTRO_FADE_IN:
        if( valid ){
          // animate level caption with various moving speed
          if( this.levelTextBoxX + this.levelTextBoxHalfWidth <
              GameConfig.gameMenuAreaWidth / 2 )
            this.moveLevelTextBox();
          else{
            this.nextState = STATE_LEVEL_INTRO;
            this.invalidate();
          }
        }
        else{
          // get background images
          this.prepareScreenImages();
          this.prepareLevelCaption();

          game.textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
          this.levelTextBoxWidth = game.textManager.stringWidth( this.levelCaption ) +
                                   ( GameConfig.gameMenuLevelBoxTextXOffset << 1 );
          this.levelTextBoxHalfWidth = this.levelTextBoxWidth >> 1;
          this.levelTextBoxX = GameConfig.gameMenuAreaX - this.levelTextBoxWidth;

          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_LEVEL_INTRO:
        if( !valid ){
          // get background images
          this.prepareScreenImages();

          this.levelTextBoxX = GameConfig.gameMenuAreaX +
                               GameConfig.gameMenuAreaWidth / 2 -
                               this.levelTextBoxHalfWidth;

          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.addMenuCommands();
          this.valid = true;
        }
        break;

      case STATE_LEVEL_INTRO_FADE_OUT:
        if( valid ){
          // animate level caption with various moving speed
          if( this.levelTextBoxX <= GameConfig.gameMenuAreaX +
              GameConfig.gameMenuAreaWidth )
            this.moveLevelTextBox();
          else{
            // start game
            this.nextState = STATE_ACTIVE_GAME;
            this.invalidate();
          }
        }
        else{
          // get background images
          this.prepareScreenImages();

          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.valid = true;
        }
        break;

      case STATE_ACTIVE_GAME:
        if( this.valid )
          this.engine.update( wasteTime );
        else{
          this.releasePrivate();
          this.engine.startLevel();
          // add apropriate command
          this.addMenuCommands();
          // hide commands area
          game.canvas.hideCommands( true );
          this.valid = true;
        }
        break;

      case STATE_PAUSE_MENU:
        if( !valid ){
          game.menuItems = new String[ 4 ];
          game.menuItems[ 0 ] = game.dao.getResource( StringItems.RESUME,
                                Settings.lang );
          game.menuItems[ 1 ] = game.dao.getResource( StringItems.RESTART,
                                Settings.lang );
          game.menuItems[ 2 ] = game.dao.getResource( StringItems.MAIN_MENU,
                                Settings.lang );
          game.menuItems[ 3 ] = game.dao.getResource( StringItems.EXIT_GAME,
                                Settings.lang );

          // add apropriate command
          this.addMenuCommands();
          this.valid = true;
        }
        break;

      case STATE_RESUME_GAME:
        if( !valid ){
          this.engine.resume();
          this.state = STATE_ACTIVE_GAME;
          // hide commands area
          game.canvas.hideCommands( true );
          // add apropriate command
          this.addMenuCommands();
          this.valid = true;
        }
        break;

      case STATE_START_LEVEL_WITHOUT_RES_LOADING:
        this.engine.resetLevelData();
        // simply change state
        this.state = STATE_LEVEL_INTRO_FADE_IN;
        break;

      case STATE_SHOW_GAME_LOOSE:
        if( !valid ){
          // get background images
          this.prepareScreenImages();
          game.scrollableText = game.textManager.breakIntoLines(
                                game.dao.getResource(
                                StringItems.S_ITEM_PLAY_AGAIN, Settings.lang ),
                                GameConfig.gameMenuTextAreaWidth,
                                TextManager.FONT_TEXT | TextManager.FONT_TYPE_WHITE );
          // add apropriate command
          this.addMenuCommands();
          this.valid = true;
        }
        break;

      case STATE_SHOW_GAME_RESULTS_HIGH_SCORE:
      case STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE:
        if( !valid ){
          // get background images
          this.prepareScreenImages();
          StringBuffer tmp = new StringBuffer( game.dao.getResource(
                             StringItems.S_ITEM_YOUR_SCORE, Settings.lang ) );
          tmp.append( " " ).append( this.score )
              .append( "\n" ).append( game.dao.getResource(
              this.state == STATE_SHOW_GAME_RESULTS_HIGH_SCORE ?
              StringItems.S_ITEM_STORE_SCORE : StringItems.S_ITEM_PLAY_AGAIN,
              Settings.lang ) );
          game.scrollableText = game.textManager.breakIntoLines(
                                tmp.toString(), GameConfig.gameMenuTextAreaWidth,
                                TextManager.FONT_TEXT | TextManager.FONT_TYPE_WHITE );

          // add apropriate command
          this.addMenuCommands();
          this.valid = true;
        }
        break;

      case STATE_ENTER_NAME:
        if( !valid ){
          // get background images
          this.prepareScreenImages();
          game.scrollableText = game.textManager.breakIntoLines(
                                game.dao.getResource(
                                StringItems.S_ITEM_ENTER_NAME, Settings.lang ),
                                GameConfig.gameMenuAreaWidth -
                                GameConfig.gameMenuTextPadding * 2,
                                TextManager.FONT_TEXT | TextManager.FONT_TYPE_WHITE );
          this.fillUserNameIndexes();
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          // add apropriate command
          this.addMenuCommands();
          this.valid = true;
        }
        else if( time - this.lastTime >= GameConfig.gameScreenEnterNameBlinkDelay ){
          // mark to repaint to emulate carret blinking
          this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
          this.lastTime = time;
          this.ticks++;
        }
        break;

      case STATE_SAVE_NAME_EXIT_TO_TOP_SCORES:
        Settings.addScore( this.getUserName(), this.score );
        Settings.storeSettings();
        // release resources
        this.releaseAll();
        game.requestTopScoresScreen();
        break;

      case STATE_EXIT_TO_MAIN_MENU:
        this.engine.resetLevelData();
        // release resources
        this.releaseAll();
        game.requestMainMenu();
        break;

      case STATE_EXIT_GAME:
        // release resources
        this.releaseAll();
        game.exitGame();
        break;
    }
  }

  /**
   * Method to render current game.
   * @param g The Graphics context.
   */
  public void paint( Graphics g )
  {
    if( !valid )
      return; // nothing to do

    switch( this.state ){
      case STATE_LOAD_LEVEL_DATA:
        this.drawScreenBackground( g );
        this.drawLoadLevelScreen( g );
        break;

      case STATE_LEVEL_INTRO_FADE_IN:
      case STATE_LEVEL_INTRO:
      case STATE_LEVEL_INTRO_FADE_OUT:
        this.drawScreenBackground( g );
        this.drawLevelCaption( g );
        break;

      case STATE_ACTIVE_GAME:
        this.engine.paint( g );
        break;

      case STATE_PAUSE_MENU:
        this.drawPauseMenu( g );
        break;

      case STATE_SHOW_GAME_LOOSE:
      case STATE_SHOW_GAME_RESULTS_HIGH_SCORE:
      case STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE:
        this.drawScreenBackground( g );
        if( this.time - lastTime >= GameConfig.scrollingDelay &&
            ( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 ||
            ( this.repaintDefs & REPAINT_TOTAL ) != 0 ) ){
          this.lastTime = this.time;
          // clear area
          this.drawMenuArea( GameConfig.gameMenuAreaX, GameConfig.gameMenuAreaY,
              GameConfig.gameMenuAreaWidth, GameConfig.gameMenuAreaHeight, g );

          game.textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_WHITE );
          int h = GameConfig.gameMenuTextLines * game.textManager.charHeight;
          game.scrollPosition += game.scrollStep * game.textManager.charHeight;
          int textHeight = game.scrollableText.length * game.textManager.charHeight;
          // draw scrollable text
          game.scrollPosition = game.drawFixedScrollableText(
                                game.scrollableText,
                                GameConfig.gameMenuTextAreaX,
                                GameConfig.gameMenuTextAreaY,
                                GameConfig.gameMenuTextAreaWidth, h,
                                GameConfig.scrollTextAligment, g,
                                game.scrollPosition, game.textManager.charHeight,
                                textHeight );
          // draw scroll bar
          game.drawScrollBar( game.scrollPosition,
              textHeight - h, GameConfig.gameMenuScrollMarkX,
              GameConfig.gameMenuScrollBarY,
              GameConfig.gameMenuScrollBarHeight, g );
        }
        break;

      case STATE_ENTER_NAME:
        this.drawScreenBackground( g );
        this.drawEnterName( g );
        break;

    }
    // reset total repaint flag
    if( ( this.repaintDefs & REPAINT_TOTAL ) != 0 )
      this.repaintDefs ^= REPAINT_TOTAL;
  }

  /**
   * Invoked to signal that game engine should perform start game sequence.
   */
  public void start()
  {
    this.nextState = STATE_LOAD_LEVEL_DATA;
    this.invalidate();
    hideNotifyCount = 0;
  }

  /**
   * Invoked to signal that positive command was pressed by user.
   */
  public void positiveCommand()
  {
    switch( this.state  ){
      case STATE_LEVEL_INTRO:
        this.nextState = STATE_LEVEL_INTRO_FADE_OUT;
        this.removeAllCommands();
        break;

      case STATE_PAUSE_MENU:
        String item = game.menuItems[ this.menuIndex ];
        if( item.equals( game.dao.getResource( StringItems.RESUME,
            Settings.lang ) ) ){
          this.nextState = STATE_RESUME_GAME;
        }
        else if( item.equals( game.dao.getResource( StringItems.RESTART,
            Settings.lang ) ) ){
          this.engine.resetLevelData();
          this.nextState = STATE_ACTIVE_GAME;
        }
        else if( item.equals( game.dao.getResource( StringItems.MAIN_MENU,
            Settings.lang ) ) )
          this.nextState = STATE_EXIT_TO_MAIN_MENU;
        else if( item.equals( game.dao.getResource( StringItems.EXIT_GAME,
            Settings.lang ) ) )
          this.nextState = STATE_EXIT_GAME;
        break;

      case STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE:
      case STATE_SHOW_GAME_LOOSE:
        // start game from begining
        this.nextState = STATE_START_LEVEL_WITHOUT_RES_LOADING;
        break;

      case STATE_SHOW_GAME_RESULTS_HIGH_SCORE:
        this.nextState = STATE_ENTER_NAME;
        break;

      case STATE_ENTER_NAME:
        this.nextState = STATE_SAVE_NAME_EXIT_TO_TOP_SCORES;
        break;
    }
    this.invalidate();
  }

  /**
   * Invoked to signal that negative command was pressed by user.
   */
  public void negativeCommand()
  {
    switch( this.state  ){
      case STATE_LEVEL_INTRO:
        this.nextState = STATE_EXIT_TO_MAIN_MENU;
        break;

      case STATE_ACTIVE_GAME:
        this.pause();
        break;

      case STATE_SHOW_GAME_LOOSE:
      case STATE_SHOW_GAME_RESULTS_HIGH_SCORE:
      case STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE:
        this.nextState = STATE_EXIT_TO_MAIN_MENU;
        break;
    }
    this.invalidate();
  }

  /**
   * Called when a key is pressed.
   * @param actionCode The game action code.
   */
  public void actionKeyPressed( int actionCode )
  {
    switch( this.state ){
      case STATE_ACTIVE_GAME:
        this.engine.actionKeyPressed( actionCode );
        break;

      case STATE_PAUSE_MENU:
        int index = this.menuIndex;
        if( actionCode == BombmanScreen.UP ){
          if( index == 0 )
            index = game.menuItems.length - 1;
          else
            index--;
          this.setMenuIndex( index );
        }
        else if( actionCode == BombmanScreen.DOWN ){
          index = ++index % game.menuItems.length;
          this.setMenuIndex( index );
        }
        break;

      case STATE_SHOW_GAME_LOOSE:
        if( actionCode == BombmanScreen.UP )
          game.scrollStep = ( byte ) - 1;
        else if( actionCode == BombmanScreen.DOWN )
          game.scrollStep = 1;
        this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
        break;

      case STATE_ENTER_NAME:
        this.proceedEnterNameAction( actionCode );
        break;
    }
  }

  /**
   * Proceed with key presses on enter name facility.
   * @param action the action code.
   */
  private final void proceedEnterNameAction( int action )
  {
    switch( action ){
      case BombmanScreen.UP:
        if( this.userNameIndexes[ this.currentUserNameIndex ] == 0 )
          this.userNameIndexes[ this.currentUserNameIndex ] = INPUT_LETTERS.
              length - 1;
        else
          this.userNameIndexes[ this.currentUserNameIndex ]--;
        break;
      case BombmanScreen.DOWN:
        this.userNameIndexes[ this.currentUserNameIndex ] =
            ( this.userNameIndexes[ this.currentUserNameIndex ] +
            1 ) % INPUT_LETTERS.length;
        break;
      case BombmanScreen.LEFT:
        if( this.currentUserNameIndex == 0 )
          this.currentUserNameIndex = this.userNameIndexes.length - 1;
        else
          this.currentUserNameIndex--;
        break;
      case BombmanScreen.RIGHT:
        this.currentUserNameIndex =
            ( this.currentUserNameIndex + 1 ) % this.userNameIndexes.length;
        break;
    }
    this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
  }

  /**
   * Method to set new menu index.
   * @param index the new menu index.
   */
  private final void setMenuIndex( int index )
  {
    // reset currently selected
    this.menuIndex = index;
    // mark to repaint
    this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
  }

  /**
   * Called when a key is released.
   * @param actionCode The game action code.
   */
  public void actionKeyReleased( int actionCode )
  {
    switch( state ){
      case STATE_ACTIVE_GAME:
        this.engine.actionKeyReleased( actionCode );
        break;

      case STATE_SHOW_GAME_LOOSE:
        game.scrollStep = 0;
        if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
          this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
        break;
    }
  }

  /**
   * Invoked to signal that game engine becomes invisible due to handset internal
   * mechanics.
   */
  public void hideNotify()
  {
    hideNotifyCount++;
    if( this.state == STATE_UNDEFINED )
      return;// nothing to do
    else if( this.state == STATE_ACTIVE_GAME )
      this.pause();
    else
      this.nextState = this.state;
    this.invalidate();
  }

  /**
   * Invoked to signal that game should enter into paused mode
   */
  private final void pause()
  {
    this.nextState = STATE_PAUSE_MENU;
    this.engine.pause();
  }

  /**
   * Release all resources acquired by this controller.
   */
  private final void releasePrivate()
  {
    this.screenImages = null;
    this.levelCaption = null;
  }

  /**
   * Release all resources acquired by this controller and game engine. Should
   * be invoked before leaving to the main menu or during hideNotify() invokation.
   */
  private final void releaseAll()
  {
    this.time = 0;
    this.releasePrivate();
    this.engine.releaseResources();
    this.removeAllCommands();
  }

  /**
   * Invoked by game engine to signal that next level should be started.
   * @param score the current game score
   */
  public final void prepareNextLevel( int score )
  {
    if( ++Settings.level < GameConfig.levels ){
      this.nextState = STATE_START_LEVEL_WITHOUT_RES_LOADING;
      Settings.score = score;
    }
    else{
      this.score = score;
      this.nextState = Settings.isHighScore( this.score ) ?
                       STATE_SHOW_GAME_RESULTS_HIGH_SCORE :
                       STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE;
      // reset level to signal that game is complete
      Settings.level = 0;
      Settings.score = 0;
    }

    // store settings to persist current progress
    Settings.storeSettings();
    this.invalidate();
    // remove pause command
    game.canvas.removeCommand( game.pauseCommand );
  }

  /**
   * Invoked by game engine to signal that loose game.
   */
  public final void gameLoose()
  {
    this.nextState = STATE_SHOW_GAME_LOOSE;
    Settings.level = 0;
    Settings.score = 0;
    // store settings to reset any progress
    Settings.storeSettings();
    this.invalidate();
  }

  /**
   * Moves level text box in accordance with its current position.
   */
  private final void moveLevelTextBox()
  {
    int offset = Math.abs( this.levelTextBoxHalfWidth -
                 this.levelTextBoxWidth / 2 - this.levelTextBoxX ) / 4;
    if( offset < 1 ){
      offset = 1;
    }
    this.levelTextBoxX += offset;
    this.repaintDefs |= REPAINT_GENERIC_ACTIVE;
  }

  /**
   * Prepares screen images by loading them if needed.
   */
  private final void prepareScreenImages()
  {
    if( this.screenImages == null )
      this.screenImages = game.dao.getMenuImages();
  }

  /**
   * Prepares level caption.
   */
  private final void prepareLevelCaption()
  {
    StringBuffer text = new StringBuffer( game.dao.getResource(
                        StringItems.S_ITEM_LEVEL, Settings.lang ) );
    text.append( " " ).append( Settings.level + 1 );
    this.levelCaption = text.toString();
  }

  /**
   * Returns user name comprised of letters, which was input by user.
   * @return user name comprised of letters, which was input by user.
   */
  private String getUserName()
  {
    StringBuffer res = new StringBuffer( 4 );
    for( int i = 0; i < 4; i++ )
      res.append( INPUT_LETTERS[ this.userNameIndexes[ i ] ] );

    return res.toString();
  }

  /**
   * Fills array of user name indices using already stored user name if exists.
   * Otherwise the first alphabet letters will be used.
   */
  private void fillUserNameIndexes()
  {
    StringBuffer tmp = new StringBuffer( 4 );
    if( Settings.userName.length() > 0 )
      tmp.append( Settings.userName );
    else
      tmp.append( INPUT_LETTERS[ 0 ] ).append( INPUT_LETTERS[ 0 ] )
      .append( INPUT_LETTERS[ 0 ] ).append( INPUT_LETTERS[ 0 ] );
    char chr;
    for( int i = 3, j; i >= 0; i-- ){
      chr = tmp.charAt( i );
      for( j = INPUT_LETTERS.length - 1; j >= 0 ; j-- ){
        if( INPUT_LETTERS[ j ].charAt( 0 ) == chr )
          this.userNameIndexes[ i ] = j;
      }
    }
  }

  /**
   * Draws enter name screen.
   * @param g the graphics context to draw on.
   */
  private final void drawEnterName( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_GENERIC_ACTIVE | REPAINT_TOTAL ) ) != 0 ){
      // clear area
      this.drawMenuArea( GameConfig.gameMenuAreaX, GameConfig.gameMenuAreaY,
          GameConfig.gameMenuAreaWidth, GameConfig.gameMenuAreaHeight, g );

      int x, y = GameConfig.gameMenuTextAreaY, w;
      game.textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
      // draw title
      // Please enter your name
      for( int i = 0; i < game.scrollableText.length; i++ ){
        w = game.textManager.stringWidth( game.scrollableText[ i ] );
        x = ( GameConfig.screenWidth - w ) / 2;
        game.textManager.drawText( game.scrollableText[ i ], x, y, true, g );
        y += game.textManager.charHeight;
      }

      // draw enter name facility
      int h = game.textManager.charHeight + 4;
      x = this.enterNameX;
      y = GameConfig.gameScreenEnterNameY - h;
      w = 0;
      for( int i = 0; i < 4; i++ ){
        w = inputLettersWidth[ this.userNameIndexes[ i ] ];
        if( i == this.currentUserNameIndex && ticks % 3 == 0 ){
          g.setColor( GameConfig.gameScreenEnterNameCarretColor );
          g.fillRect( x, y, this.maxLetterWidth + 4, h );
        }
        game.textManager.drawText( INPUT_LETTERS[ this.userNameIndexes[ i ] ],
            x + 2 + ( this.maxLetterWidth - w ) / 2, y + 2, true, g );
        x += this.maxLetterWidth + GameConfig.gameScreenEnterNameInterwal;
      }

      if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
        this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
    }
  }

  /**
   * Draws level caption.
   * @param g the graphics context to draw on.
   */
  private final void drawLevelCaption( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_GENERIC_ACTIVE | REPAINT_TOTAL ) ) != 0 ){
      // clear area
      this.drawMenuArea( GameConfig.gameMenuAreaX, GameConfig.gameMenuAreaY,
        GameConfig.gameMenuAreaWidth, GameConfig.gameMenuAreaHeight, g );

      // draw text box
      g.setClip( GameConfig.gameMenuAreaX, GameConfig.gameMenuAreaY,
          GameConfig.gameMenuAreaWidth, GameConfig.gameMenuAreaHeight );
      g.setColor( GameConfig.mainMenuBackground );
      g.fillRect( this.levelTextBoxX, GameConfig.gameMenuLevelBoxY,
          this.levelTextBoxWidth, GameConfig.gameMenuLevelBoxHeight );
      g.setColor( GameConfig.mainMenuForeground );
      g.drawRect( this.levelTextBoxX, GameConfig.gameMenuLevelBoxY,
          this.levelTextBoxWidth - 1, GameConfig.gameMenuLevelBoxHeight - 1 );

      // draw text
      game.textManager.setFont( TextManager.FONT_TEXT | TextManager.FONT_TYPE_BLACK );
      game.textManager.drawText( this.levelCaption,
          this.levelTextBoxX + GameConfig.gameMenuLevelBoxTextXOffset,
          GameConfig.gameMenuLevelBoxY + GameConfig.gameMenuLevelBoxTextYOffset,
          true, g );

      if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
        this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
    }
  }

  /**
   * Draws pause menu.
   * @param g the graphics context to draw on.
   */
  private final void drawPauseMenu( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_GENERIC_ACTIVE | REPAINT_TOTAL ) ) != 0 ){
      // draw background
      this.engine.paint( g );

      // clear menu area
      this.drawMenuArea( GameConfig.gameScreenPauseMenuX,
          GameConfig.gameScreenPauseMenuY, GameConfig.gameScreenPauseMenuWidth,
          GameConfig.gameScreenPauseMenuHeight, g );

      game.textManager.setFont( TextManager.FONT_MENU | TextManager.FONT_TYPE_BLACK );
      int y = GameConfig.gameScreenPauseMenuOptionY;
      for( int i = 0, x, w, py; i < game.menuItems.length; i++ ){
        w = game.textManager.stringWidth( game.menuItems[ i ] );
        x = ( GameConfig.screenWidth - w ) / 2;
        // draw caption
        g.setClip( x, y, w, game.textManager.charHeight );
        game.textManager.drawText( game.menuItems[ i ], x, y, true, g );
        // draw pointer if appropriate
        if( this.menuIndex == i ){
          // draw left pointer
          x -= GameConfig.gameScreenPauseMenuPointerXOffset + GameConfig.tileWidth;
          py = y + GameConfig.gameScreenPauseMenuPointerYOffset;
          g.setClip( x, py, GameConfig.tileWidth, GameConfig.tileHeight );
          g.drawImage( game.commonImages[ Images.TILES ],
              x - GameConfig.tileWidth * 7, py, Graphics.TOP | Graphics.LEFT );
          // draw right pointer
          x += GameConfig.gameScreenPauseMenuPointerXOffset + GameConfig.tileWidth +
              w + GameConfig.gameScreenPauseMenuPointerXOffset;
          g.setClip( x, py, GameConfig.tileWidth, GameConfig.tileHeight );
          g.drawImage( game.commonImages[ Images.TILES ],
              x - GameConfig.tileWidth * 7, py, Graphics.TOP | Graphics.LEFT );
        }
        y += game.textManager.charHeight + GameConfig.gameScreenPauseMenuInterval;
      }

      if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
        this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
    }
    // clear commands area in full screen mode when paused
    if( GameConfig.fullScreen ){
      g.setClip( 0, GameConfig.gameScreenActiveHeight,
          GameConfig.screenWidth, GameConfig.gameScreenIndicatorsAreaHeight );
      g.setColor( GameConfig.gameBackgroundColor );
      g.fillRect( 0, GameConfig.gameScreenActiveHeight,
          GameConfig.screenWidth, GameConfig.gameScreenIndicatorsAreaHeight );
    }
  }

  /**
   * Draws screen background for menu screens.
   * @param g the graphics context to draw on.
   */
  private final void drawScreenBackground( Graphics g )
  {
    if( ( this.repaintDefs & REPAINT_TOTAL ) != 0 ){
      game.drawScreenFill( this.screenImages[ 0 ], g );
      // draw title
      g.drawImage( this.screenImages[ 1 ], GameConfig.gameMenuTitleX,
          GameConfig.gameMenuTitleY, Graphics.TOP | Graphics.LEFT );
    }
  }

  /**
   * Method to draw loading level data screen.
   * @param g the graphics context to draw on.
   */
  private final void drawLoadLevelScreen( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_GENERIC_ACTIVE | REPAINT_TOTAL ) ) != 0 ){
      // clear area
      this.drawMenuArea( GameConfig.gameMenuAreaX, GameConfig.gameMenuAreaY,
        GameConfig.gameMenuAreaWidth, GameConfig.gameMenuAreaHeight, g );

      // draw caption
      String text = game.dao.getResource( StringItems.S_ITEM_LOADING,
                    Settings.lang );
      int x = GameConfig.gameMenuAreaX +
              ( GameConfig.gameMenuAreaWidth -
              game.textManager.stringWidth( text ) ) / 2;
      int y = GameConfig.gameMenuAreaY +
              ( GameConfig.gameMenuAreaHeight -
              game.textManager.charHeight ) / 2;
      game.textManager.drawText( text, x, y, false, g );
      // draw progress bar
      y += this.game.textManager.charHeight + GameConfig.gameMenuLoadBarYOffset;
      game.drawProgressBar( GameConfig.gameMenuLoadBarX, y,
          GameConfig.gameMenuLoadBarWidth, GameConfig.gameMenuLoadBarHeight,
          this.engine.progress, GameEngine.stepsToLoad,
          GameConfig.gameMenuLoadBarColor, g );

      if( ( this.repaintDefs & REPAINT_GENERIC_ACTIVE ) != 0 )
        this.repaintDefs ^= REPAINT_GENERIC_ACTIVE;
    }
  }

  /**
   * Draws menu area.
   * @param x the left X coordinate of area.
   * @param y the top Y coordinate of area.
   * @param w the width of area.
   * @param h the height of area.
   * @param g the graphics context to draw on.
   */
  private final void drawMenuArea( int x, int y, int w, int h, Graphics g )
  {
    g.setClip( x, y, w, h );
    g.setColor( GameConfig.mainMenuBackground );
    g.fillRect( x, y, w, h );
    g.setColor( GameConfig.mainMenuForeground );
    g.drawRect( x, y, w - 1, h - 1 );
  }

  /**
   * Method to release menu resources.
   */
  private void invalidate()
  {
    this.state = STATE_UNDEFINED;
    this.valid = false;
    this.menuIndex = 0;
    game.menuItems = null;
    game.scrollableText = null;
    game.scrollPosition = 0;
    game.scrollStep = 0;
    this.lastTime = 0;
    this.ticks = 0;
    this.currentUserNameIndex = 0;

    // set repaint flag
    this.repaintDefs = REPAINT_TOTAL;
    // marks to show commands
    game.canvas.hideCommands( false );
  }

  /**
   * Method to add appropriate commands in accordance with current game state.
   */
  private final void addMenuCommands()
  {
    // remove previously added commands
    this.removeAllCommands();

    // add negative commands in accordance with state
    switch( state ){
      case STATE_PAUSE_MENU:
      case STATE_ENTER_NAME:
        // don't add anything
        break;

      case STATE_ACTIVE_GAME:
        game.canvas.addCommand( game.pauseCommand );
        break;

      case STATE_SHOW_GAME_LOOSE:
      case STATE_SHOW_GAME_RESULTS_HIGH_SCORE:
      case STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE:
        game.canvas.addCommand( game.noCommand );
        break;

      default:
        game.canvas.addCommand( game.backCommand );
    }

    // add positive commands
    Command tmp = this.getCurrentPositiveCommand();
    if( tmp != null )
      game.canvas.addCommand( tmp );
  }

  /**
   * Returns current positive command in accordance with menu state.
   * @return current positive command in accordance with menu state.
   */
  protected final Command getCurrentPositiveCommand()
  {
    Command tmp = null;
    switch( this.state )
    {
      case STATE_LEVEL_INTRO:
        tmp = game.startCommand;
        break;

      case STATE_PAUSE_MENU:
        tmp = game.selectCommand;
        break;

      case STATE_SHOW_GAME_LOOSE:
      case STATE_SHOW_GAME_RESULTS_HIGH_SCORE:
      case STATE_SHOW_GAME_RESULTS_ORDINARY_SCORE:
        tmp = game.yesCommand;
        break;

      case STATE_ENTER_NAME:
        tmp = game.saveCommand;
        break;

    }
    return tmp;
  }

  /**
   * Method to remove all menu commands from canvas.
   */
  private final void removeAllCommands()
  {
    game.canvas.removeCommand( game.backCommand );
    game.canvas.removeCommand( game.pauseCommand );
    game.canvas.removeCommand( game.noCommand );

    game.canvas.removeCommand( game.startCommand );
    game.canvas.removeCommand( game.selectCommand );
    game.canvas.removeCommand( game.yesCommand );
    game.canvas.removeCommand( game.saveCommand );
  }
}