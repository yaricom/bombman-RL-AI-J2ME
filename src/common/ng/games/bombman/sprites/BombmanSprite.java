/**
 * $Id: BombmanSprite.java 2 2005-06-30 12:47:28Z yaric $
 */
package ng.games.bombman.sprites;

import ng.games.bombman.GameConfig;
import ng.games.bombman.GameEngine;

/**
 * Subclass for bombman sprites managed by player and by AI.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public abstract class BombmanSprite
{
  /** Constant to define player bombman type */
  public static final int TYPE_PLAYER = 1;
  /** Constant to define AI bombman type */
  public static final int TYPE_AI = 2;

  /** The left X coordinate of sprite relative to the board */
  public int x;
  /** The top Y coordinate of sprite relative to the board */
  public int y;
  /** Column where player is situated */
  public int col;
  /** Row where player is situated */
  public int row;

  /** Next moving direction of this sprite */
  protected int nextMovingDirection;
  /** Current moving direction of this sprite */
  protected int movingDirection;

  /** Current state of sprite */
  protected byte state;
  /** Undefined state */
  public static final byte STATE_UNDEFINED = 0;
  /** Active state */
  public static final byte STATE_ACTIVE = 1;
  /** Start death animation state */
  public static final byte STATE_START_DEATH_ANIMATION = 2;
  /** Death animation state */
  public static final byte STATE_DEATH_ANIMATION = 3;
  /** Start jiga dancing state */
  public static final byte STATE_START_WIN_ANIMATION = 4;
  /** Jiga dancing state */
  public static final byte STATE_WIN_ANIMATION = 5;
  /** Freeze state */
  public static final byte STATE_FREEZE = 6;

  /** Game engine instance */
  protected final GameEngine engine;
  /** Current frame index to use */
  protected byte frameIndex;

  /** Flag to indicate that this sprite is in acceleration mode */
  protected boolean accelerated;
  /** Flag to indicate that this sprite is in extra range explosion mode */
  protected boolean extraRange;

  /** Bombs holded in bombman's pocket */
  public byte bombsInPocket;
  /** Capacity of bombman pocket (how many bombs can be here simulatneously) */
  public byte maxBombsInPocket;

  /** Current moving speed */
  protected int movingSpeed;
  /** The last time when speed up bonus was earned */
  protected int speedUpTime;

  /** Last time */
  private int lastTime;
  /** Number of passed animation cycles */
  private int winAnimationCycles;

  /**
   * Constructs new instance.
   * @param engine the game engine.
   */
  public BombmanSprite( GameEngine engine )
  {
    this.engine = engine;
  }

  /**
   * Returns type of this sprite.
   * @return type of this sprite.
   */
  public abstract int getType();

  /**
   * Returns <code>true</code> if bombman is alive.
   * @return <code>true</code> if bombman is alive.
   */
  public boolean isAlive()
  {
    return this.state == STATE_ACTIVE;
  }

  /**
   * Signals that bombman dying.
   */
  public final void die()
  {
    this.setState( STATE_START_DEATH_ANIMATION );
  }

  /**
   * Signal that bombman becomes winner.
   */
  public final void winner()
  {
    this.setState( STATE_START_WIN_ANIMATION );
  }

  /**
   * Signal that bombman should freeze
   */
  public final void freeze()
  {
    this.setState( STATE_FREEZE );
  }

  /**
   * Signals that bombman was born and becomes active.
   */
  public final void born()
  {
    this.setState( STATE_ACTIVE );
  }

  /**
   * Set state to the player.
   * @param state the state of sprite.
   */
  private final void setState( byte state )
  {
    this.state = state;
    if( this.state == STATE_ACTIVE )
      this.frameIndex = 3;
  }

  /**
   * Method to reset player status.
   */
  public void reset()
  {
    this.movingDirection = GameEngine.STOP;
    this.nextMovingDirection = GameEngine.STOP;

    this.accelerate( false );
    this.extraRange = false;
    this.bombsInPocket = 1;
    this.maxBombsInPocket = 1;
    this.speedUpTime = 0;
    this.lastTime = 0;
    this.col = 0;
    this.row = 0;
    this.setState( STATE_UNDEFINED );
  }

  /**
   * Invoked to signal that bomb droped by bombman have been finished explosion.
   */
  public void bombHaveBeenExploded()
  {
    this.bombsInPocket++;
  }

  /**
   * Invoked to signal that one bomb is picked up.
   */
  public void increaseBombPocket()
  {
    this.maxBombsInPocket++;
    if( this.maxBombsInPocket > GameConfig.maxBombsInPocket )
      this.maxBombsInPocket = (byte)GameConfig.maxBombsInPocket;
    else
      this.bombsInPocket++;// increase, because there is still a place here
  }

  /**
   * Invoked to signal that speed bonus was earned.
   * @param time the current game time.
   */
  public void speedUp( int time )
  {
    this.speedUpTime = time;
    this.accelerate( true );
  }

  /**
   * Invoked to signal that damage range extended.
   */
  public void extendDamageRange()
  {
    this.extraRange = true;
  }

  /**
   * Returns current explosion range for bombs droped by this bombman.
   * @return current explosion range for bombs droped by this bombman.
   */
  public int getExplosionRange()
  {
    return extraRange ? GameConfig.extendedEplosionRange :
        GameConfig.normalEplosionRange;
  }

  /**
   * Method to update player state in active state.
   * @param time the current game time.
   */
  public void update( int time )
  {
    switch( this.state ){

      case STATE_ACTIVE:
        // calculate position at the board
        col = x / GameConfig.tileWidth;
        row = y / GameConfig.tileHeight;
        break;

      case STATE_START_DEATH_ANIMATION:
        this.lastTime = time;
        this.frameIndex = 0;
        this.state = STATE_DEATH_ANIMATION;
        break;

      case STATE_DEATH_ANIMATION:
        if( time - this.lastTime > GameConfig.bombmanAnimationInterval ){
          this.lastTime = time;
          this.frameIndex++;
          if( this.frameIndex == GameConfig.bombmanDeathAnimationFrames ){
            this.reset();
            this.processDeath();
          }
        }
        break;

      case STATE_START_WIN_ANIMATION:
        this.lastTime = time;
        this.frameIndex = 0;
        this.winAnimationCycles = 0;
        this.state = STATE_WIN_ANIMATION;
        break;

      case STATE_WIN_ANIMATION:
        if( time - this.lastTime > GameConfig.bombmanAnimationInterval ){
          this.lastTime = time;
          this.frameIndex++;
          if( this.frameIndex == GameConfig.bombmanWinAnimationFrames ){
            this.winAnimationCycles++;
            this.frameIndex = 0;
          }
          if( this.winAnimationCycles == GameConfig.bombmanWinAnimationCycles ){
            this.reset();
            processWin();
          }
        }
        break;
    }
  }

  /**
   * Should be implemented by subclasses in order to provide logic of death.
   */
  protected abstract void processDeath();

  /**
   * Should be implemented by subclasses in order to provide logic of win.
   */
  protected abstract void processWin();

  /**
   * Accelerates movement of this sprite.
   * @param accelerate if <code>true</code> than movement should be accelerated,
   * otherwise normal speed should be setuped.
   */
  protected final void accelerate( boolean accelerate )
  {
    this.accelerated = accelerate;
    this.movingSpeed = accelerate ? GameConfig.bombmanAcceleratedMoveOffset :
                       GameConfig.bombmanMoveOffset;
  }

  /**
   * Check whether speed should be decreased in case it was accelerated previously
   * and bonus time has expired.
   * @param time the current game time.
   * @return <code>true</code> if speed was decreased to normal due to this
   * method execution.
   */
  protected final boolean checkSpeed( int time )
  {
    if( this.movingSpeed == GameConfig.bombmanAcceleratedMoveOffset &&
        time - this.speedUpTime >= GameConfig.bombmanAccelerationTime ){
      this.speedUpTime = 0;
      this.accelerate( false );
      return true;
    }
    else
      return false;
  }

  /**
   * Returns <code>true</code> if player's sprite can move to the specified position.
   * @param col the column to move.
   * @param row the row to move.
   * @return <code>true</code> if player's sprite can move to the specified position.
   */
  protected boolean canMoveTo( int col, int row )
  {
    if( col <= 0 || col >= this.engine.colNumber ||
        row <= 0 || row >= this.engine.rowsNumber )
      return false;
    else{
      byte content = engine.getCell( col, row );
      return content != GameEngine.HARD_WALL_PLACE &&
          content != GameEngine.SOFT_WALL_PLACE &&
          content != GameEngine.BOMB_PLACE;
    }
  }

  /**
   * Invoked to signal that bomb can be droped if any drop event scheduled.
   * @return <code>true</code> if bomb was successfully dropped.
   */
  protected final boolean tryToDropBomb()
  {
    if( this.engine.dropBomb( this, this.col, this.row ) ){
      // bomb was dropped so reset counters
      this.bombsInPocket--;
      // reset flags
      this.extraRange = false;
      return true;
    }
    return false;
  }

  /**
   * Moves player sprite relative to the game board.
   * @param dx the horizontal shift.
   * @param dy the vertical shift.
   */
  protected final void move( int dx, int dy )
  {
    this.x += dx;
    this.y += dy;
  }
}