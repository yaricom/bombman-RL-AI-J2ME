/**
 * $Id: GameEngine.java 223 2005-07-14 15:42:03Z yaric $
 */
package ng.games.bombman;

import java.util.Random;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import ng.games.bombman.sprites.*;
import ng.mobile.game.util.Log;

/**
 * Game engine - encapsulates all game logic.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class GameEngine
{
  /** Score for wall blowing */
  public static final int scoreWall = 1;
  /** Score for bonus earning */
  public static final int scoreBonus = 10;

  /** Constant to define stopped or undefined moving state */
  public static final int STOP = 0;

  /** Number of load steps */
  public static final int stepsToLoad = 4;

  /** Hard wall place */
  public static final byte HARD_WALL_PLACE = 0;
  /** Soft wall place */
  public static final byte SOFT_WALL_PLACE = 1;
  /** Empty place */
  public static final byte EMPTY_PLACE = 2;
  /** Shadowed empty place */
  public static final byte SHADOWED_EMPTY_PLACE = 3;
  /** Bomb bonus place */
  public static final byte BOMB_BONUS_PLACE = 4;
  /** Fire range bonus place */
  public static final byte FIRE_RANGE_BONUS_PLACE = 5;
  /** Speed bonus place */
  public static final byte SPEED_BONUS_PLACE = 6;
  /** Bomb place */
  public static final byte BOMB_PLACE = 7;
  /** Player place */
  public static final byte PLAYER_PLACE = 8;
  /** Enemy place */
  public static final byte ENEMY_PLACE = 9;

  /** Parent game controller instance */
  public final BombmanGameController parent;
  /** Player sprite */
  public final PlayerBombmanSprite player;
  /** AI bombman sprite */
  public final AIBombmanSprite aiBombman;
  /** Bomb sprites */
  public BombSprite[] bombs;
  /** Clock sprite */
  private final ClockSprite clock;
  /** Randomizer */
  private final Random randomizer;

  /** Holds current loading progress state */
  public int progress;

  /** Game engine state flag */
  private byte state;
  /** Undefined state */
  private static final byte STATE_UNDEFINED = 0;
  /** Game fade in animation */
  public static final byte STATE_GAME_FADE_IN = 1;
  /** Normal game execution state */
  public static final byte STATE_GAME = 2;
  /** Move board to display opponent win animation state */
  public static final byte STATE_CENTER_OPPONENT_ON_BOARD = 4;
  /** Display opponent win animation */
  public static final byte STATE_OPPONENT_WIN = 5;
  /** Display player win animation */
  public static final byte STATE_PLAYER_WIN = 6;
  /** Reload level state */
  public static final byte STATE_RELOAD_LEVEL = 7;
  /** Game over final animation */
  public static final byte STATE_FINAL_ANIMATION = 9;
  /** Final caption displaying */
  public static final byte STATE_FINAL_CAPTION = 10;
  /** Game over state (reset all level data and invoke game over menu) */
  public static final byte STATE_GAME_OVER = 11;

  /** Previous game state */
  private byte prevState;

  /** Danger map - used to store dangerous cell */
  private byte[][]hazzardMap;
  /** Board map, where map[ column ][ row ] */
  private byte[][]map;
  /** Board image */
  private Image boardImage;
  /** Graphics from board image */
  private Graphics boardGraphics;
  /** The number of rows on game board */
  public short rowsNumber;
  /** The number of columns on game board */
  public short colNumber;
  /** The width of board image */
  private int boardWidth;
  /** The height of board image */
  private int boardHeight;
  /** The left X coordinate of the board relative to the screen */
  public int boardX;
  /** The top Y coordinate of the board relative to the screen */
  public int boardY;

  /** Final board X coordinate when panning to show opponent animation */
  private int boardXFinal;
  /** Final board Y coordinate when panning to show opponent animation */
  private int boardYFinal;

  /** The number of bomb bonuses already present on the board */
  private int bombBonusesOnBoard;
  /** Previously placed on the board bonus */
  private byte previousBonus;

  /** The X coordinate of the player's sprite relative to the board */
  public int playerX;
  /** The Y coordinate of the player's sprite relative to the board */
  public int playerY;
  /** Flag to signal whether player win current level or not */
  private boolean playerWin;

  /** Lives left */
  private byte livesLeft;
  /** Current game score */
  private int score;
  /** Last score when lives count was increased */
  private int lastScore;
  /** Time passed from start of current game */
  private int time;
  /** Last time when some event occurs */
  private int lastTime;
  /** Flag to indicate whether engine paused */
  private boolean paused;


  /** Repaint definitions. Holds definitions which screen areas should be repainted */
  private int repaintDefs;
  /** Constant to define total repaint flag */
  private static final int REPAINT_TOTAL = 1;
  /** Constant to define repaint game board */
  private static final int REPAINT_BOARD = 1 << 1;
  /** Constant to define repaint active elements */
  private static final int REPAINT_ACTIVE = 1 << 2;
  /** Constant to define repaint lives mark area */
  private static final int REPAINT_LIVES = 1 << 3;
  /** Constant to define repaint scores mark area */
  private static final int REPAINT_SCORES = 1 << 4;
  /** Constant to define repaint bombs in pocket mark area */
  private static final int REPAINT_BOMBS = 1 << 5;
  /** Constant to define repaint final animation area */
  private static final int REPAINT_FINAL_ANIMATION = 1 << 6;

  /** Scores image */
  private Image scoresImage;
  /** Bomberman sprites */
  public Image[] bombermanImages;
  /** Clock images */
  public Image[] clockImages;
  /** Final game caption */
  private String[] finalCaption;
  /** Appearance animation segment height */
  private int animSegmentHeight;

  /**
   * Construct new instance with specified parent controller.
   * @param controller the parent game controller.
   */
  public GameEngine( BombmanGameController controller )
  {
    this.parent = controller;
    this.player = new PlayerBombmanSprite( this );
    this.aiBombman = new AIBombmanSprite( this );
    this.bombs = new BombSprite[ GameConfig.maxBombsInPocket * 2 ];
    for( int i = this.bombs.length - 1; i >= 0 ; i-- )
      this.bombs[ i ] = new BombSprite( this );

    this.clock = new ClockSprite( this );
    this.randomizer = new Random();
    this.state = STATE_UNDEFINED;
    this.resetLevelData();
  }

  /**
   * Returns next random value, which fits into specified range.
   * @param low the lowest possible value
   * @param high the high limit for value (exclusive).
   * @return next random value, which fits into specified range.
   */
  public final int getNextRandom( int low, int high )
  {
    int limit = high - low;
    return low + Math.abs( randomizer.nextInt() % limit );
  }

  /**
   * Called when a key is pressed.
   * @param actionCode The game action code.
   */
  protected void actionKeyPressed( int actionCode )
  {
    this.player.keyPressed( actionCode );
  }

  /**
   * Called when a key is released.
   * @param actionCode The game action code.
   */
  protected void actionKeyReleased( int actionCode )
  {
    this.player.keyReleased( actionCode );
  }

  /**
   * Invoked to signal that game engine should resume its execution.
   */
  protected final void resume()
  {
    this.paused = false;
    this.repaintDefs |= REPAINT_TOTAL;
  }

  /**
   * Invoked to signal that game engine should enter into paused mode
   */
  protected final void pause()
  {
    this.paused = true;
    this.repaintDefs |= REPAINT_TOTAL;
  }

  /**
   * Updates engine logic.
   * @param wasteTime time which was spent on previous game cycle.
   */
  protected final void update( int wasteTime )
  {
    if( paused )
      return; // nothing to do

    // update time
    this.time += wasteTime;
    // update game objects
    switch( this.state ){
      case STATE_GAME_FADE_IN:
        this.animSegmentHeight -= GameConfig.levelFadingStep;
        if( this.animSegmentHeight <= 0 ){
          this.animSegmentHeight = 0;// reset for subsequent reuse
          this.state = STATE_GAME;
        }
        this.repaintDefs |= REPAINT_TOTAL;// everything should be repaint
        break;

      case STATE_GAME:
        // update player
        this.player.update( this.time );
        // update opponent
        this.aiBombman.update( time );

        // update bombs
        for( int i = this.bombs.length - 1; i >= 0; i-- )
          this.bombs[ i ].update( time );

        // update clock
        this.clock.update( time );
        break;

      case STATE_CENTER_OPPONENT_ON_BOARD:
        boolean finished = false;
        // move horizontaly
        if( this.boardX > this.boardXFinal + GameConfig.boardPanningSpeed )
          this.boardX -= GameConfig.boardPanningSpeed;
        else if( this.boardX < this.boardXFinal - GameConfig.boardPanningSpeed )
          this.boardX += GameConfig.boardPanningSpeed;
        else
          finished = true;

        // move vertically
        if( this.boardY > this.boardYFinal + GameConfig.boardPanningSpeed )
          this.boardY -= GameConfig.boardPanningSpeed;
        else if( this.boardY < this.boardYFinal - GameConfig.boardPanningSpeed )
          this.boardY += GameConfig.boardPanningSpeed;
        else if( finished ){
          this.state = STATE_OPPONENT_WIN;
          this.aiBombman.winner();
        }
        break;

      case STATE_OPPONENT_WIN:
        this.aiBombman.update( time );
        break;

      case STATE_PLAYER_WIN:
        this.player.update( time );
        break;

      case STATE_RELOAD_LEVEL:
        this.resetLevelData();
        this.startLevel();
        break;

      case STATE_FINAL_ANIMATION:
        this.animSegmentHeight += GameConfig.finalAnimationStep;
        if( this.animSegmentHeight >= GameConfig.screenHeight / 2 ){
          this.state = STATE_FINAL_CAPTION;
          this.lastTime = time;
        }
        this.repaintDefs |= REPAINT_FINAL_ANIMATION;
        break;

      case STATE_FINAL_CAPTION:
        if( time - this.lastTime >= GameConfig.finalCaptionDelay )
          this.state = STATE_GAME_OVER;
        break;

      case STATE_GAME_OVER:
        if( this.playerWin ){
          this.parent.prepareNextLevel( this.score );
        }
        else
          this.parent.gameLoose();
        // reset level data
        this.resetLevelData();
        break;
    }
  }

  /**
   * Paints game engine.
   * @param g the graphics context to draw on.
   */
  protected final void paint( Graphics g )
  {
    switch( this.state ){
      case STATE_UNDEFINED:
      case STATE_RELOAD_LEVEL:
      case STATE_GAME_OVER:
        return; // nothing to do

      case STATE_GAME_FADE_IN:
        this.drawBackground( g );
        this.drawBoard( g );
        this.drawBombmans( g );
        this.drawIndicators( g );
        this.drawLevelFadeAnimation( g );
        break;

      case STATE_GAME:
        this.drawBackground( g );
        this.drawBoard( g );
        this.drawActive( g );
        this.drawBombmans( g );
        this.drawIndicators( g );
        break;

      case STATE_CENTER_OPPONENT_ON_BOARD:
      case STATE_OPPONENT_WIN:
      case STATE_PLAYER_WIN:
        this.drawBackground( g );
        this.drawBoard( g );
        this.drawBombmans( g );
        this.drawIndicators( g );
        break;

      case STATE_FINAL_ANIMATION:
        this.drawBackground( g );
        this.drawBoard( g );
        this.drawIndicators( g );
        this.drawFinalAnimation( g );
        break;

      case STATE_FINAL_CAPTION:
        this.drawFinalAnimation( g );
        break;
    }

    // reset
    if( ( this.repaintDefs & REPAINT_TOTAL ) != 0 )
      this.repaintDefs ^= REPAINT_TOTAL;
  }

  /**
   * Draws level fade in/out animation.
   * @param g the graphics context to draw on.
   */
  private final void drawLevelFadeAnimation( Graphics g )
  {
    g.setClip( 0, 0, GameConfig.screenWidth, GameConfig.screenHeight );
    g.setColor( GameConfig.gameBackgroundColor );
    int y = GameConfig.levelFadingSegmentHeight;
    while( y <= GameConfig.screenHeight ){
      g.fillRect( 0, y - this.animSegmentHeight,
          GameConfig.screenWidth, this.animSegmentHeight );
      y += GameConfig.levelFadingSegmentHeight;
    }
  }


  /**
   * Draws final animation's current frame.
   * @param g the graphics context to draw on.
   */
  private final void drawFinalAnimation( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_FINAL_ANIMATION | REPAINT_TOTAL ) ) != 0 ){
      g.setClip( 0, 0, GameConfig.screenWidth, GameConfig.screenHeight );
      g.setColor( GameConfig.gameBackgroundColor );
      g.fillRect( 0, 0, GameConfig.screenWidth, this.animSegmentHeight );
      g.fillRect( 0, GameConfig.screenHeight - this.animSegmentHeight,
          GameConfig.screenWidth, this.animSegmentHeight );

      // draw caption if appropriate
      if( this.state == STATE_FINAL_CAPTION ){
        parent.game.textManager.setFont(
            TextManager.FONT_TEXT | TextManager.FONT_TYPE_WHITE );
        int strY = ( GameConfig.screenHeight - parent.game.textManager.charHeight *
                   this.finalCaption.length ) / 2;
        for( int i = 0, strX; i < this.finalCaption.length; i++ ){
          strX = ( GameConfig.screenWidth -
                 parent.game.textManager.stringWidth( this.finalCaption[ i ] ) ) / 2;
          parent.game.textManager.drawText( this.finalCaption[ i ], strX, strY,
              false, g );
        }
      }

      if( ( this.repaintDefs & REPAINT_FINAL_ANIMATION ) != 0 )
        this.repaintDefs ^= REPAINT_FINAL_ANIMATION;
    }
  }

  /**
   * Draws screen background.
   * @param g the graphics context to draw on.
   */
  private final void drawBackground( Graphics g )
  {
    if( ( this.repaintDefs & REPAINT_TOTAL ) != 0 ){
      g.setColor( GameConfig.gameBackgroundColor );
      g.fillRect( 0, 0, GameConfig.screenWidth,
          GameConfig.gameScreenActiveHeight );

      // draw bottom icons
      g.setColor( GameConfig.gameScreenIndicatorsBackground );
      g.fillRect( 0, GameConfig.gameScreenActiveHeight,
          GameConfig.screenWidth,
          GameConfig.gameScreenIndicatorsAreaHeight );
      g.setColor( GameConfig.gameScreenIndicatorsForeground );
      g.drawRect( 0, GameConfig.gameScreenActiveHeight,
          GameConfig.screenWidth - 1,
          GameConfig.gameScreenIndicatorsAreaHeight - 1 );

      // draw live icon
      int x = GameConfig.gameScreenIndicatorHorizontalOffset;
      g.setClip( x, GameConfig.gameScreenIndicatorY,
          GameConfig.gameScreenLivesMarkWidth,
          GameConfig.gameScreenScoreNumHeight );
      g.drawImage( this.scoresImage,
          x - GameConfig.gameScreenScoreNumWidthTotal,
          GameConfig.gameScreenIndicatorY, Graphics.TOP | Graphics.LEFT );
      // draw semicolon
      x += GameConfig.gameScreenLivesMarkWidth;
      g.setClip( x, GameConfig.gameScreenIndicatorY,
          GameConfig.gameScreenScoreNumWidth,
          GameConfig.gameScreenScoreNumHeight );
      g.drawImage( this.scoresImage,
          x - GameConfig.gameScreenScoreNumWidth * 10,
          GameConfig.gameScreenIndicatorY, Graphics.TOP | Graphics.LEFT );

      // draw bomb icon
      x = GameConfig.screenWidth -
          GameConfig.gameScreenIndicatorHorizontalOffset -
          GameConfig.gameScreenScoreNumWidth -
          GameConfig.gameScreenScoreNumWidth;
      // draw semicolon
      g.setClip( x, GameConfig.gameScreenIndicatorY,
          GameConfig.gameScreenScoreNumWidth,
          GameConfig.gameScreenScoreNumHeight );
      g.drawImage( this.scoresImage,
          x - GameConfig.gameScreenScoreNumWidth * 10,
          GameConfig.gameScreenIndicatorY, Graphics.TOP | Graphics.LEFT );
      // draw icon
      x -= GameConfig.gameScreenBombsMarkWidth;
      g.setClip( x, GameConfig.gameScreenIndicatorY,
          GameConfig.gameScreenBombsMarkWidth,
          GameConfig.gameScreenScoreNumHeight );
      g.drawImage( this.scoresImage,
          x - GameConfig.gameScreenScoreNumWidthTotal -
          GameConfig.gameScreenLivesMarkWidth,
          GameConfig.gameScreenIndicatorY, Graphics.TOP | Graphics.LEFT );
    }
  }

  /**
   * Draws game board.
   * @param g the graphics context to draw on.
   */
  private final void drawBoard( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_BOARD | REPAINT_TOTAL ) ) != 0 ){
      g.setClip( 0, 0, GameConfig.screenWidth,
          GameConfig.gameScreenActiveHeight );
      // reset background
      g.setColor( GameConfig.gameBackgroundColor );
      g.fillRect( 0, 0, GameConfig.screenWidth,
          GameConfig.gameScreenActiveHeight );

      // draw board
      g.drawImage( this.boardImage, this.boardX, this.boardY,
          Graphics.TOP | Graphics.LEFT );
    }
  }

  /**
   * Draws bombmans.
   * @param g the graphics context to draw on.
   */
  private final void drawBombmans( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_ACTIVE | REPAINT_TOTAL ) ) != 0 ){
      // draw players
      this.aiBombman.paint( g );
      this.player.paint( g );
    }
  }

  /**
   * Draws active screen elements except bombmans.
   * @param g the graphics context to draw on.
   */
  private final void drawActive( Graphics g )
  {
    if( ( this.repaintDefs & ( REPAINT_ACTIVE | REPAINT_TOTAL ) ) != 0 ){
      // draw bombs
      for( int i = this.bombs.length - 1; i >= 0; i-- )
        this.bombs[ i ].paint( g );

      // draw clock
      this.clock.paint( g );
    }
  }

  /**
   * Draws indicators area
   * @param g the graphics context to draw on.
   */
  private final void drawIndicators( Graphics g )
  {
    int x;
    // draw lives
    if( ( this.repaintDefs & ( REPAINT_LIVES | REPAINT_TOTAL ) ) != 0 ){
      // draw number
      x = GameConfig.gameScreenIndicatorHorizontalOffset +
          GameConfig.gameScreenScoreNumWidth + GameConfig.gameScreenLivesMarkWidth;
      g.setClip( x, GameConfig.gameScreenIndicatorY,
          GameConfig.gameScreenScoreNumWidth, GameConfig.gameScreenScoreNumHeight );
      g.drawImage( this.scoresImage, x - GameConfig.gameScreenScoreNumWidth * this.livesLeft,
          GameConfig.gameScreenIndicatorY, Graphics.TOP | Graphics.LEFT );

      if( ( this.repaintDefs & REPAINT_LIVES ) != 0 )
        this.repaintDefs ^= REPAINT_LIVES;
    }

    // draw score
    if( ( this.repaintDefs & ( REPAINT_SCORES | REPAINT_TOTAL ) ) != 0 ){
      int divider = GameConfig.gameScreenScoreDivider;
      int scoreTmp = this.score;
      x = GameConfig.gameScreenScoreX;
      for( int j2 = 0; j2 < 5; j2++ ){
        int dx = scoreTmp / divider;
        if( dx > 9 ){
          dx = 9;
        }
        scoreTmp -= dx * divider;
        divider /= 10;
        g.setClip( x, GameConfig.gameScreenIndicatorY,
            GameConfig.gameScreenScoreNumWidth, GameConfig.gameScreenScoreNumHeight );
        g.drawImage( this.scoresImage, x - dx * GameConfig.gameScreenScoreNumWidth,
            GameConfig.gameScreenIndicatorY, Graphics.TOP | Graphics.LEFT );
        x += GameConfig.gameScreenScoreNumWidth + GameConfig.gameScreenScoreInterwal;
      }

      if( ( this.repaintDefs & REPAINT_SCORES ) != 0 )
        this.repaintDefs ^= REPAINT_SCORES;
    }

    // draw bombs
    if( ( this.repaintDefs & ( REPAINT_BOMBS | REPAINT_TOTAL ) ) != 0 ){
      // draw number
      x = GameConfig.screenWidth - GameConfig.gameScreenIndicatorHorizontalOffset -
          GameConfig.gameScreenScoreNumWidth;
      g.setClip( x, GameConfig.gameScreenIndicatorY,
          GameConfig.gameScreenScoreNumWidth, GameConfig.gameScreenScoreNumHeight );
      g.drawImage( this.scoresImage, x - GameConfig.gameScreenScoreNumWidth *
          this.player.bombsInPocket, GameConfig.gameScreenIndicatorY,
          Graphics.TOP | Graphics.LEFT );

      if( ( this.repaintDefs & REPAINT_BOMBS ) != 0 )
        this.repaintDefs ^= REPAINT_BOMBS;
    }
  }

  /**
   * Finds and return bomb placed at specified cell.
   * @param col the column where to look for bomb.
   * @param row the row where to look for bomb.
   * @return <code>BombSprite</code> at specified location or <code>null</code>.
   */
  public final BombSprite getBombAt( int col, int row )
  {
    BombSprite bomb = null;
    for( int i = this.bombs.length - 1; i >= 0; i-- ){
      bomb = bombs[ i ];
      if( bomb.col == col && bomb.row == row )
        return bomb;
    }
    return null;
  }

  /**
   * Method to drop bomb.
   * @param bombman the bombman droping this bomb.
   * @param col the column where to drop bomb.
   * @param row the row where to drop bomb.
   * @return <code>true</code> if bomb was successfully droped.
   */
  public final boolean dropBomb( BombmanSprite bombman, int col, int row )
  {
    BombSprite bomb = null;
    for( int i = this.bombs.length - 1; i >= 0 ; i-- ){
      if( !bombs[ i ].charged() )
        bomb = bombs[ i ];
    }
    // charging found bomb if any
    if( bomb != null ){
      bomb.charge( bombman, col, row );
      this.repaintDefs |=  REPAINT_BOMBS;
      return true;
    }
    else
      return false;
  }

  /**
   * Invoked to signal that bomb charged by specified bombman have been finished
   * explosion animation.
   * @param bomb the bomb.
   */
  public final void bombHaveBeenExploded( BombSprite bomb )
  {
    BombmanSprite tmp = bomb.getBombman();
    tmp.bombHaveBeenExploded();
    if( tmp.getType() == BombmanSprite.TYPE_PLAYER ){
      this.repaintDefs |= REPAINT_BOMBS;
    }
  }

  /**
   * Tries to blow up bombman at the specified position if any.
   * @param col the column of cell.
   * @param row the row of cell.
   */
  public final void blowUpBombman( int col, int row )
  {
    byte content = this.getCell( col, row );
    switch( content ){
      case PLAYER_PLACE:
        this.blowUpBombman( player );
        break;

      case ENEMY_PLACE:
        this.blowUpBombman( aiBombman );
        break;

      default:
        if( player.col == col && player.row == row )
          this.blowUpBombman( player );
        else if( aiBombman.col == col && aiBombman.row == row )
          this.blowUpBombman( aiBombman );
        break;
    }
  }

  /**
   * Blows up specified cell.
   * @param bombman the bombman charged this bomb.
   * @param col the column of cell.
   * @param row the row of cell.
   */
  public final void blowUpCell( final BombmanSprite bombman, int col, int row )
  {
    byte content = this.getCell( col, row );
    switch( content ){
      case SOFT_WALL_PLACE:
        byte type = this.getNextAvailableBonus();
        if( type != -1 ){
          // bonus found
          this.setCell( type, col, row );
          this.drawTile( type,
              col * GameConfig.tileWidth, row * GameConfig.tileHeight );
        }
        else
          this.eraseCell( col, row );
        if( bombman.getType() == BombmanSprite.TYPE_PLAYER ){
          // update score for wall blowing by player
          this.updateScore( scoreWall);
          this.repaintDefs |= REPAINT_SCORES;
        }
        break;

      case PLAYER_PLACE:
        this.blowUpBombman( this.player );
        break;

      case ENEMY_PLACE:
        this.blowUpBombman( this.aiBombman );
        break;
    }
  }

  /**
   * Updates score value.
   * @param val the score value to add to the current one.
   */
  private final void updateScore( int val )
  {
    this.score += val;
    if( this.score - this.lastScore >= 100 ){
      // bonus one life for next 100 points
      this.livesLeft++;
      this.lastScore = this.score;
      this.repaintDefs |= REPAINT_LIVES;
    }
  }

  /**
   * Performs check whether sprite will be under attack on the target cell.
   * @param col the column of cell.
   * @param row the row of cell.
   * @return <code>true</code> if target cell is dangerous.
   */
  public final boolean isCellHazardous( int col, int row )
  {
    // hazzard map has size less than board, because no need to store info about
    // borders
    return this.hazzardMap[ col ][ row ] > 0 ||
        this.getCell( col, row ) == BOMB_PLACE;
  }

  /**
   * Changes cell's hazard status.
   * @param col the column of cell.
   * @param row the row of cell.
   * @param increase if <code>true</code> than cell hazard status will be increased,
   * otherwise decreased.
   */
  public final void changeCellHazardStatus( int col, int row, boolean increase )
  {
    if( increase )
      this.hazzardMap[ col ][ row ]++;
    else if( this.hazzardMap[ col ][ row ] > 0 )
      this.hazzardMap[ col ][ row ]--;
  }

  /**
   * Invoked to blow up specified bombman if appropriate.
   * @param bombman the bombman to blow up.
   */
  private final void blowUpBombman( BombmanSprite bombman )
  {
    if( bombman.getType() == BombmanSprite.TYPE_PLAYER ){
      // blow up player
      if( player.isAlive() && aiBombman.isAlive() ){
        player.die();
        aiBombman.freeze();
      }
    }
    else{
      // blow up AI bombman
      if( player.isAlive() && aiBombman.isAlive() ){
        aiBombman.die();
        player.freeze();
      }
    }
  }

  /**
   * Invoked to signal that clock should display beeping animation.
   */
  public final void startClockBeeping()
  {
    this.clock.startBeeping();
  }

  /**
   * Method to move player left
   * @param dx the player offset along X coordinate.
   */
  public final void movePlayerLeft( int dx )
  {
    playerX -= dx;
    if( boardX < 0 && boardWidth - playerX > GameConfig.screenWidth / 2 ){
      boardX += dx;
      if( boardX > 0 )
        boardX = 0;
    }
  }

  /**
   * Method to move player sprite right.
   * @param dx the horizontal increment.
   */
  public final void movePlayerRight( int dx )
  {
    playerX += dx;
    if( boardX + boardWidth > GameConfig.screenWidth &&
        playerX > GameConfig.screenWidth / 2 ){
      boardX -= dx;
      if( boardX + boardWidth < GameConfig.screenWidth )
        boardX = GameConfig.screenWidth - boardWidth;
    }
  }

  /**
   * Method to move player sprite up.
   * @param dy the vertical increment.
   */
  public final void movePlayerUP( int dy )
  {
    playerY -= dy;
    if( boardY < 0 && boardHeight - playerY > GameConfig.gameScreenActiveHeight / 2 ){
      boardY += dy;
      if( boardY > 0 )
        boardY = 0;
    }
  }

  /**
   * Moves player sprite down.
   * @param dy the vertical offset.
   */
  public final void movePlayerDown( int dy )
  {
    playerY += dy;
    if( boardY + boardHeight > GameConfig.gameScreenActiveHeight &&
        playerY > GameConfig.gameScreenActiveHeight / 2 ){
      boardY -= dy;
      if( boardY + boardHeight < GameConfig.gameScreenActiveHeight )
        boardY = GameConfig.gameScreenActiveHeight - boardHeight;
    }
  }

  /**
   * Method to process moving of player from one position to another.
   * @param bombman the bombman to process.
   * @param prevCol the previous column where player was.
   * @param prevRow the previous row where player was.
   */
  public final void processBombmanMovement( BombmanSprite bombman,
      int prevCol, int prevRow )
  {
    byte cellContent = this.getCell( bombman.col, bombman.row );
    if( cellContent == EMPTY_PLACE || cellContent == SHADOWED_EMPTY_PLACE ){
      // erase current bombman's position
      this.eraseCell( bombman.col, bombman.row );
    }
    else{
      // apply bonus if any
      if( this.applyBonus( bombman, bombman.col, bombman.row ) &&
          bombman.getType() == BombmanSprite.TYPE_PLAYER ){
        // bonus earned by player, so update score
        this.updateScore( scoreBonus );
        parent.game.effects.pickUpBonus();
        this.repaintDefs |= REPAINT_SCORES;
      }
      this.eraseCell( bombman.col, bombman.row );
    }
    // erase previous position if bomb was not droped here
    if( this.getCell( prevCol, prevRow ) != BOMB_PLACE )
      this.eraseCell( prevCol, prevRow );

    // mark next position as bombman position
    this.setCell( bombman.getType() == BombmanSprite.TYPE_PLAYER ?
        PLAYER_PLACE : ENEMY_PLACE, bombman.col, bombman.row );
  }

  /**
   * Method to load all level data.
   * @return <code>true</code> if all data already loaded.
   */
  protected final boolean loadLevel()
  {
    switch( this.progress++ ){
      case 0:
        // reserved
        break;

      case 1:// load scores image
        this.scoresImage = parent.game.dao.getScoreImage();
        break;

      case 2:// load clock images
        this.clockImages = parent.game.dao.getClockSprites();
        break;

      case 3:// load sprites image
        this.bombermanImages = parent.game.dao.getSprites();
        break;

      default:
        parent.game.dao.cleanIntermediate();
        return true;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if soft wall is situated in the specified cell.
   * @param col the column of cell.
   * @param row the row of cell.
   * @return <code>true</code> if soft wall found on specified cell.
   */
  public final boolean isSoftWallAt( int col, int row )
  {
    if( col <= 0 || col >= this.colNumber ||
        row <= 0 || row >= this.rowsNumber )
      return false;

    if( this.getCell( col, row ) == GameEngine.SOFT_WALL_PLACE )
      return true;
    else
      return false;
  }

  /**
   * Returns index of tile living in the specified cell.
   * @param col the column of cell.
   * @param row the row of cell.
   * @return index of tile situated in specified cell or -1 if specified cell
   * doen't exist.
   */
  public final byte getCell( int col, int row )
  {
    return this.map[ col ][ row ];
  }

  /**
   * Method to set content of particullar cell.
   * @param tile the tile to set in this cell.
   * @param col the column of cell.
   * @param row the row of cell.
   */
  public final void setCell( byte tile, int col, int row )
  {
    this.map[ col ][ row ] = tile;
  }

  /**
   * Method to reset paticular board cell to the background tile.
   * @param col the column of cell.
   * @param row the row of cell.
   */
  public final void eraseCell( int col, int row )
  {
    byte top = this.getCell( col, row - 1 );
    byte type = EMPTY_PLACE;
    if( row > 0 && ( top == SOFT_WALL_PLACE || top == HARD_WALL_PLACE ) )
      type = SHADOWED_EMPTY_PLACE;
    this.map[ col ][ row ] = type;
    this.drawTile( type, col * GameConfig.tileWidth, row * GameConfig.tileHeight );
    // check if shadowed empty place is under this cell
    row++;
    if( row < this.rowsNumber && this.getCell( col, row ) == SHADOWED_EMPTY_PLACE ){
      this.map[ col ][ row ] = EMPTY_PLACE;
      this.drawTile( EMPTY_PLACE, col * GameConfig.tileWidth,
          row * GameConfig.tileHeight );
    }
  }

  /**
   * Method to draw particullar tile at the specified location.
   * @param tile the tile to draw.
   * @param x the left X coordinate of drawing location.
   * @param y the top Y coordinate of drawing location.
   */
  public final void drawTile( byte tile, int x, int y )
  {
    this.boardGraphics.setClip( x, y, GameConfig.tileWidth,
          GameConfig.tileHeight );
    if( tile <= SPEED_BONUS_PLACE ){
      x -= GameConfig.tileWidth * tile;
    }
    else{
      x -= GameConfig.tileWidth * EMPTY_PLACE;
    }
    this.boardGraphics.drawImage( parent.game.commonImages[ Images.TILES ],
        x, y, Graphics.TOP | Graphics.LEFT );
    this.repaintDefs |= REPAINT_BOARD;
  }

  /**
   * Release all stored resources, when game engine stops its execution.
   */
  protected final void releaseResources()
  {
    this.progress = 0;
    this.scoresImage = null;
    this.bombermanImages = null;
    this.clockImages = null;
  }

  /**
   * Resets level data, when current level is complete or not passed.
   */
  protected final void resetLevelData()
  {
    if( this.state != STATE_RELOAD_LEVEL )
      this.livesLeft = 3; // reset to default only when level is complete
    this.time = 0;
    this.bombBonusesOnBoard = 0;
    this.previousBonus = -1;
    this.player.reset();
    this.aiBombman.reset();
    for( int i = this.bombs.length - 1; i >= 0 ; i-- )
      this.bombs[ i ].reset();
    this.clock.stop();
    this.map = null;
    this.hazzardMap = null;

    this.animSegmentHeight = 0;
    this.lastTime = 0;
    this.playerWin = false;
    this.paused = false;
    this.score = Settings.score;
    this.lastScore = this.score;
    this.prevState = STATE_UNDEFINED;

    // release board gfx
    this.boardGraphics = null;
    this.boardImage = null;
    // release final caption image
    this.finalCaption = null;
  }

  /**
   * Invoked to signal that player have been dead. Invoked after death animation
   * has been completed.
   */
  public final void playerDead()
  {
    // calculate final board position for panning to the opponent place
    this.state = STATE_CENTER_OPPONENT_ON_BOARD;
    int boardPos[] = this.calculateBoardPosition(
                     this.aiBombman.x, this.aiBombman.y );
    this.boardXFinal = boardPos[ 0 ];
    this.boardYFinal = boardPos[ 1 ];

    // play scream sound
    parent.game.effects.scream();
  }

  /**
   * Invoked to signal that player win this match. Invoked after win animation
   * has been completed.
   */
  public final void playerWin()
  {
    this.playerWin = true;
    this.state = STATE_FINAL_ANIMATION;
    this.prepareFinalCaption( parent.game.dao.getResource(
        StringItems.S_ITEM_YOU_WIN, Settings.lang ) );
  }

  /**
   * Invoked to signal that opponent have been dead. Invoked after death animation
   * has been completed.
   */
  public final void opponentDead()
  {
    // play win sound
    parent.game.effects.winSound();

    player.winner();
    this.state = STATE_PLAYER_WIN;
  }

  /**
   * Invoked to signal that opponent win this match. Invoked after win animation
   * has been completed.
   */
  public final void opponentWin()
  {
    if( --this.livesLeft >= 0 )
      this.state = STATE_RELOAD_LEVEL;
    else{
      // game over
      this.playerWin = false;
      this.state = STATE_FINAL_ANIMATION;
      this.prepareFinalCaption( parent.game.dao.getResource(
          StringItems.S_ITEM_YOU_LOOSE, Settings.lang ) );
    }
  }

  /**
   * Prepares final caption.
   * @param caption the caption.
   */
  private final void prepareFinalCaption( String caption )
  {
    this.finalCaption = parent.game.textManager.breakIntoLines( caption,
                        GameConfig.screenWidth - 10, TextManager.FONT_TEXT );
  }

  /**
   * Prepares anything for current level start up.
   */
  protected final void startLevel()
  {
    this.createLevelData();
    this.animSegmentHeight = GameConfig.levelFadingSegmentHeight +
                             GameConfig.levelFadingStep;
    this.state = STATE_GAME_FADE_IN;
    this.repaintDefs = REPAINT_TOTAL | REPAINT_BOARD | REPAINT_ACTIVE;
  }

  /**
   * Checks whether bonus placed at the specified location and if so than
   * apply it for specified bombman.
   * @param bombman the bombman to apply bonus for.
   * @param col the column to check.
   * @param row the row to check.
   * @return <code>true</code> if bonus was applied.
   */
  private final boolean applyBonus( BombmanSprite bombman, int col, int row )
  {
    boolean res = false;
    byte content = this.getCell( col, row );
    switch( content ){
      case BOMB_BONUS_PLACE:
        bombman.increaseBombPocket();
        this.bombBonusesOnBoard--;// one bonus is earned so decrease counter
        if( bombman.getType() == BombmanSprite.TYPE_PLAYER )
          this.repaintDefs |= REPAINT_BOMBS;
        res = true;
        break;

      case FIRE_RANGE_BONUS_PLACE:
        bombman.extendDamageRange();
        res = true;
        break;

      case SPEED_BONUS_PLACE:
        bombman.speedUp( this.time );
        if( bombman.getType() == BombmanSprite.TYPE_PLAYER )
          this.clock.startCountDown();
        res = true;
        break;
    }
    return res;
  }

  /**
   * Returns next available bonus if any.
   * @return next available bonus if any.
   */
  private final byte getNextAvailableBonus()
  {
    byte type = -1;
    int rand = this.getNextRandom( 0, 100 );
    if( rand <= 5 && this.previousBonus != BOMB_BONUS_PLACE ){
      // check if any bomb bonus can be droped
      int bombs = this.bombBonusesOnBoard + this.player.maxBombsInPocket +
                  this.aiBombman.maxBombsInPocket;

      if( bombs < GameConfig.maxBombsInPocket * 2 ){
        type = BOMB_BONUS_PLACE;
        this.bombBonusesOnBoard++;
      }
    }
    else if( rand <= 20 && this.previousBonus != SPEED_BONUS_PLACE ){
      // speed bonus
      type = SPEED_BONUS_PLACE;
    }
    else if( rand <= 40 && this.previousBonus != FIRE_RANGE_BONUS_PLACE ){
      // extra fire range bonus
      type = FIRE_RANGE_BONUS_PLACE;
    }
    // remember this type to avoid sequential appearance of the same bonuses
    if( type != -1 )
      this.previousBonus = type;

    return type;
  }

  /**
   * Method to calculate initial board position in accordance with player position.
   */
  private final void calculateInitialBoardPosition()
  {
    this.playerX = this.player.x + GameConfig.tileWidth / 2;
    this.playerY = this.player.y + GameConfig.tileHeight / 2;
    // calculate horizontal board position
    int boardPos[] = this.calculateBoardPosition( playerX, playerY );
    this.boardX = boardPos[ 0 ];
    this.boardY = boardPos[ 1 ];
  }

  /**
   * Calculates board position relative to the screen in order to place sprite
   * with provided coordinates in appropriate place.
   * @param x the left X coordinate of player sprite.
   * @param y the top Y coordinate of player sprite.
   * @return coordinates of top-left corner of the board relative to the screen.
   */
  private final int[] calculateBoardPosition( int x, int y )
  {
    int[] bp = new int[ 2 ];
    // calculate horizontal board position
    if( this.boardWidth <= GameConfig.screenWidth ){
      bp[ 0 ] = ( GameConfig.screenWidth - this.boardWidth ) / 2;
    }
    else if( x <= GameConfig.screenWidth / 2 ){
      bp[ 0 ] = 0;
    }
    else if( x >= this.boardWidth - GameConfig.screenWidth / 2 ){
      bp[ 0 ] = GameConfig.screenWidth - this.boardWidth;
    }
    else{
      bp[ 0 ] = GameConfig.screenWidth / 2 - x;
    }

    // calculate vertical board position
    if( this.boardHeight <= GameConfig.gameScreenActiveHeight ){
      bp[ 1 ] = ( GameConfig.gameScreenActiveHeight - this.boardHeight ) / 2;
    }
    else if( y <= GameConfig.gameScreenActiveHeight / 2 ){
      bp[ 1 ] = 0;
    }
    else if( y >= this.boardHeight - GameConfig.gameScreenActiveHeight / 2 ){
      bp[ 1 ] = GameConfig.gameScreenActiveHeight - this.boardHeight;
    }
    else{
      bp[ 1 ] = GameConfig.gameScreenActiveHeight / 2 - y;
    }
    return bp;
  }

  /**
   * Method to read level map definition from resource file.
   * <p>
   * Where next symbols can be used to represent game objects:
   * <ul>
   * <li> Empty place - .
   * <li> Shadowed empty place - _
   * <li> Soft wall place - =
   * <li> Hard wall place - #
   * <li> Player place - 1
   * <li> Active enemy place - 2
   * </ul>
   * </p>
   *
   * @param data the array of strings with level data.
   */
  private void createLevelMap( String[]data )
  {
    int c, col = 1, row = 0;
    // read header
    this.colNumber = ( byte )data[ 0 ].length();
    this.rowsNumber = ( byte )data.length;
    this.colNumber += 2;
    this.rowsNumber += 2;
    this.hazzardMap = new byte[ this.colNumber ][ this.rowsNumber ];
    this.map = new byte[ this.colNumber ][ this.rowsNumber ];
    byte currentCell;
    for( int i = 0, j; i < this.rowsNumber - 2; i++ ){
      // start next row reading
      currentCell = -1;
      row++;
      col = 1;
      for( j = 0; j < this.colNumber - 2; j++ ){
        c = data[ i ].charAt( j );
        switch( c ){
          case '.': // empty place
            currentCell = EMPTY_PLACE;
            break;
          case '_': // shadowed empty place
            currentCell = SHADOWED_EMPTY_PLACE;
            break;
          case '=': // soft wall
            currentCell = SOFT_WALL_PLACE;
            break;
          case '#': // hard wall
            currentCell = HARD_WALL_PLACE;
            break;
          case '1': // player place
            this.player.x = col * GameConfig.tileWidth;
            this.player.y = row * GameConfig.tileHeight;
            currentCell = PLAYER_PLACE;
            break;
          case '2': // active enemy place
            this.aiBombman.x = col * GameConfig.tileWidth;
            this.aiBombman.y = row * GameConfig.tileHeight;
            currentCell = ENEMY_PLACE;
            break;

          default: // empty place
            currentCell = EMPTY_PLACE;
            break;
        }
        if( currentCell != -1 ){
          map[ col++ ][ row ] = currentCell;
        }
      }
    }
    // fill board edges
    for( byte i = 0; i < colNumber; i++ ){
      // top edge
      map[ i ][ 0 ] = HARD_WALL_PLACE;
      // bottom edge
      map[ i ][ rowsNumber - 1 ] = HARD_WALL_PLACE;
    }

    for( byte i = 0; i < rowsNumber; i++ ){
      // left edge
      map[ 0 ][ i ] = HARD_WALL_PLACE;
      // right edge
      map[ colNumber - 1 ][ i ] = HARD_WALL_PLACE;
    }
  }

  /**
   * Creates level image.
   */
  private final void createLevelImage()
  {
    this.boardWidth = GameConfig.tileWidth * this.colNumber;
    this.boardHeight = GameConfig.tileHeight * this.rowsNumber;
    this.boardImage = Image.createImage( boardWidth, boardHeight );
    this.boardGraphics = boardImage.getGraphics();
    for( int col = 0, row, xx = 0, yy; col < this.colNumber; col++ ){
      yy = 0;
      for( row = 0; row < this.rowsNumber; row++ ){
        if( map[ col ][ row ] <= ENEMY_PLACE )
          this.drawTile( map[ col ][ row ], xx, yy );
        yy += GameConfig.tileHeight;
      }
      xx += GameConfig.tileWidth;
    }
  }

  /**
   * Loads data for current level and resets all counters.
   */
  private final void createLevelData()
  {
    if( this.map == null ){
      this.createLevelMap( parent.game.dao.getLevelData( Settings.level ) );
      this.score = Settings.score;
      this.lastScore = this.score;
    }
    this.createLevelImage();
    this.calculateInitialBoardPosition();
    // reset bombmans' states
    this.player.born();
    this.aiBombman.born();
  }
}