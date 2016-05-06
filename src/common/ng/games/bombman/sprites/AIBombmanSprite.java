/**
 * $Id: AIBombmanSprite.java 197 2005-07-08 16:00:57Z yaric $
 */
package ng.games.bombman.sprites;

import javax.microedition.lcdui.Graphics;

import ng.games.bombman.GameConfig;
import ng.games.bombman.GameEngine;
import ng.games.bombman.screens.BombmanScreen;
import ng.mobile.game.util.Log;

/**
 * Bombman sprite controled by AI.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class AIBombmanSprite extends BombmanSprite
{
  /** Empty cell weight */
  private static final int WEIGHT_CLEAN_CELL = 2000;
  /** Danger cell weight */
  private static final int WEIGHT_DANGER_CELL = -100;
  /** Side danger cell weight */
  private static final int WEIGHT_SIDE_DANGER_CELL = 100;
  /** Cell with opponent */
  private static final int WEIGHT_OPPONENT_CELL = 10;
  /** Not reachable cell weight */
  private static final int WEIGHT_BLOCKED_CELL = Integer.MIN_VALUE;

  /** Drop bomb factor for empty space */
  private static final int BOMB_FACTOR_CLEAN_CELL = 0;
  /** Drop bomb factor for soft wall */
  private static final int BOMB_FACTOR_SOFT_WALL = 100;
  /** Drop bomb factor for opponent */
  private static final int BOMB_FACTOR_OPPONENT = 1000;

  /** Stores information about current mode */
  private byte mode;
  /** Intercept player mode */
  private static final byte MODE_INTERCEPT = 1;
  /** Evade from fire mode */
  private static final byte MODE_EVADE = 2;

  /** Flag to indicate that bombman is in danger now */
  private boolean inDanger;

  /** Previous drop bomb factor */
  private int prevDropBombFactor;

  /**
   * Array to hold board analysis data in fourth directions
   * <ul>
   * <li> 0 - left
   * <li> 1 - top
   * <li> 2 - right
   * <li> 3 - bottom
   * </ul>
   */
  private final int[] boardAnalysis;
  /** Left cell index in board analysis array */
  private final static int LEFT_CELL = 0;
  /** Top cell index in board analysis array */
  private final static int TOP_CELL = 1;
  /** Right cell index in board analysis array */
  private final static int RIGHT_CELL = 2;
  /** Bottom cell index in board analysis array */
  private final static int BOTTOM_CELL = 3;

  /** Bomb instance used to simulate explosion */
  private final BombSprite testBomb;

  //
  // Loging definitions
  //
  private static final boolean logInitEvasion = Log.enabled & false;
  private static final boolean logInitInterception = Log.enabled & false;
  private static final boolean logBestEvasionDirection = Log.enabled & false;
  private static final boolean logBestDirection = Log.enabled & false;
  private static final boolean logDropBombOk = Log.enabled & false;
  private static final boolean logStartInterceptionMovement = Log.enabled & false;

  /**
   * Constructs new instance.
   * @param engine the game engine.
   */
  public AIBombmanSprite( GameEngine engine )
  {
    super( engine );
    boardAnalysis = new int[ 4 ];
    testBomb = new BombSprite( engine );
  }

  /**
   * Method to reset bombman status.
   */
  public void reset()
  {
    super.reset();

    this.inDanger = false;
    this.setMode( MODE_INTERCEPT );
    this.testBomb.reset();
  }


  /**
   * Returns type of this sprite.
   * @return type of this sprite.
   */
  public int getType()
  {
    return BombmanSprite.TYPE_AI;
  }

  /**
   * Method to update player state in active state.
   * @param time the current game time.
   */
  public final void update( int time )
  {
    // update super first
    super.update( time );

    if( this.state == STATE_ACTIVE ){
      switch( this.mode ){
        case MODE_INTERCEPT:
          if( this.movingDirection != GameEngine.STOP )
            this.finishMove( time );
          else
            this.initInterception();
          break;

        case MODE_EVADE:
          if( this.movingDirection != GameEngine.STOP )
            this.finishMove( time );
          else if( this.analyseDirectCell( col, row ) == WEIGHT_CLEAN_CELL ){
            // current cell where player stoped is clean
            this.setMode( MODE_INTERCEPT );
          }
          else
            this.initEvasion();
          break;
      }
    }
  }

  /**
   * Sets current mode.
   * @param mode the new mode value.
   */
  private final void setMode( int mode )
  {
    this.mode = (byte)mode;
    // reset mode specific values
    movingDirection = GameEngine.STOP;
    nextMovingDirection = GameEngine.STOP;
    this.prevDropBombFactor = 0;
  }

  /**
   * Performs move initialization for fire evasion.
   */
  private final void initEvasion()
  {
    if( logInitEvasion ){
      Log.log( "INIT EVASION", "nextMove: " + nextMovingDirection );
    }

    int w, w1, w2;
    // try to find best direction
    switch( nextMovingDirection ){
      case GameEngine.STOP: // undefined - look for best one
        movingDirection = this.calculateBestEvasionDirection();

        if( logInitEvasion ){
          Log.log( "INIT EVASION", "undefined - evading in: " + movingDirection );
        }
        break;

      case BombmanScreen.UP:
        // check next cell
        w = this.analyseDirectCell( col, row - 1 );
        w1 = this.analyseDirectCell( col - 1, row );// left
        w2 = this.analyseDirectCell( col + 1, row );// right
        if( w == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.UP;// move one step further
        else if( w1 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.LEFT;// move to the left
        else if( w2 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.RIGHT;// move to the right
        else if( w1 != WEIGHT_BLOCKED_CELL || w2 != WEIGHT_BLOCKED_CELL )
          movingDirection = this.calculateBestEvasionDirection();// we have cross road - try to get best direction
        else if( w != WEIGHT_BLOCKED_CELL )
          movingDirection = BombmanScreen.UP;// still can try to escape

        if( logInitEvasion ){
          Log.log( "INIT EVASION", "w: " + w + " left: " + w1 +
              " right: " + w2 + " moving: " + movingDirection );
        }
        break;

      case BombmanScreen.DOWN:
        // check next cell
        w = this.analyseDirectCell( col, row + 1 );
        w1 = this.analyseDirectCell( col - 1, row );// left
        w2 = this.analyseDirectCell( col + 1, row );// right
        if( w == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.DOWN;// move one step further
        else if( w1 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.LEFT;// move to the left
        else if( w2 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.RIGHT;// move to the right
        else if( w1 != WEIGHT_BLOCKED_CELL || w2 != WEIGHT_BLOCKED_CELL )
          movingDirection = this.calculateBestEvasionDirection();// we have cross road - try to get best direction
        else if( w != WEIGHT_BLOCKED_CELL )
          movingDirection = BombmanScreen.DOWN;// still can try to escape

        if( logInitEvasion ){
          Log.log( "INIT EVASION", "w: " + w + " left: " + w1 +
              " right: " + w2 + " moving: " + movingDirection );
        }
        break;

      case BombmanScreen.LEFT:
        // check next cell
        w = this.analyseDirectCell( col - 1, row );
        w1 = this.analyseDirectCell( col, row - 1 );// top
        w2 = this.analyseDirectCell( col, row + 1 );// bottom
        if( w == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.LEFT;// move one step further
        else if( w1 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.UP;// move to the top
        else if( w2 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.DOWN;// move to the bottom
        else if( w1 != WEIGHT_BLOCKED_CELL || w2 != WEIGHT_BLOCKED_CELL )
          movingDirection = this.calculateBestEvasionDirection();// we have cross road - try to get best direction
        else if( w != WEIGHT_BLOCKED_CELL )
          movingDirection = BombmanScreen.LEFT;// still can try to escape

        if( logInitEvasion ){
          Log.log( "INIT EVASION", "w: " + w + " top: " + w1 + " bottom: " + w2 +
              " moving: " + movingDirection );
        }
        break;

      case BombmanScreen.RIGHT:
        // check next cell
        w = this.analyseDirectCell( col + 1, row );
        w1 = this.analyseDirectCell( col, row - 1 );// top
        w2 = this.analyseDirectCell( col, row + 1 );// bottom
        if( w == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.RIGHT;// move one step further
        else if( w1 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.UP;// move to the top
        else if( w2 == WEIGHT_CLEAN_CELL )
          movingDirection = BombmanScreen.DOWN;// move to the bottom
        else if( w1 != WEIGHT_BLOCKED_CELL || w2 != WEIGHT_BLOCKED_CELL )
          movingDirection = this.calculateBestEvasionDirection();// we have cross road - try to get best direction
        else if( w != WEIGHT_BLOCKED_CELL )
          movingDirection = BombmanScreen.RIGHT;// still can try to escape

        if( logInitEvasion ){
          Log.log( "INIT EVASION", "w: " + w + " top: " + w1 + " bottom: " + w2 +
              " moving: " + movingDirection );
        }
        break;
    }

    // set appropriate frame index and move sprite
    switch( movingDirection ){
      case GameEngine.STOP:
        this.frameIndex = 3;
        break;

      case BombmanScreen.UP:
        this.frameIndex = 1;
        this.move( 0, -this.movingSpeed );
        break;

      case BombmanScreen.DOWN:
        this.frameIndex = 4;
        this.move( 0, this.movingSpeed );
        break;

      case BombmanScreen.LEFT:
        this.frameIndex = 7;
        this.move( -this.movingSpeed, 0 );
        break;

      case BombmanScreen.RIGHT:
        this.frameIndex = 10;
        this.move( this.movingSpeed, 0 );
        break;
    }
  }

  /**
   * Calculates best fire evasion path.
   * Algorythm:
   * 1.) Calculate weight of fourth directions from current location with
   * GameConfig.extendedEplosionRange depth.
   * 2.) Weight of particullar direction consist of weight of bomb if found,
   * weight of danger path and additional weight for crossroads and opponent.
   * 3.) Weight of bomb calculated by multiplying distance between bomb and
   * bomberman on time to explode. Higher bomb weight means that this bomb is
   * more safe to approach. Thus if two bombs found bombman can decide what
   * direction is more desirable to run.
   * 4.) Weight of crossroad calculated as sum of weights of side cells taking
   * into account safe this cell or under danger.
   * 5.) If opponent found than most desirable direction is one which not lead
   * to him to avoid deadends, when bombman becomes catched between his and
   * opponent bombs.
   * 6.) When weights for possible moving directions calculated bombman decides
   * where to move - direction with maximal weight will be most desirable way
   * to proceed.
   * @return moving direction to follow best fire evasion path.
   */
  private final int calculateBestEvasionDirection()
  {
    // analyse four possible run directions in order to choose the best one
    int c, r, currWeight;
    byte content;
    // flags to indicate whether traversing in specified directions is still needed
    boolean left = true, right = true, top = true, bottom = true;
    // flags to indicate whether at least one clean cell already processed in specified direction
    boolean leftClean = false, rightClean = false, topClean = false, bottomClean = false;

    for( int i = GameConfig.extendedEplosionRange - 1, shift; i >= 0; i-- ){
      shift = GameConfig.extendedEplosionRange - i;
      // check left cell
      if( left ){
        c = col - shift;
        r = row;
        currWeight = this.analyseCellHorizontalTraversing( c, r );
        if( currWeight != WEIGHT_BLOCKED_CELL ){
          if( !leftClean ){
            // increase weight only if clean cell was not found in this
            // direction previously to avoid counting several clean cells
            this.boardAnalysis[ LEFT_CELL ] += currWeight;
            // check if this cell is clean and if so than set flag indicating
            // that one clean cell already processed
            leftClean = currWeight == WEIGHT_CLEAN_CELL;
          }
        }
        else if( c >= 0 ){
          // stop any further traverse in this direction
          left = false;
          // check whether this is bomb and analyse it if so
          content = this.engine.getCell( c, r );
          if( content == GameEngine.BOMB_PLACE )
            this.boardAnalysis[ LEFT_CELL ] +=
                this.analyseBomb( this.engine.getBombAt( c, r ) );
          else if( content == GameEngine.PLAYER_PLACE ){
            // count opponent in order to decrease weight of this cell
            this.boardAnalysis[ LEFT_CELL ] -= WEIGHT_OPPONENT_CELL;
          }
        }
      }

      // check right cell
      if( right ){
        c = col + shift;
        r = row;
        currWeight = this.analyseCellHorizontalTraversing( c, r );
        if( currWeight != WEIGHT_BLOCKED_CELL ){
          if( !rightClean ){
            // increase weight only if clean cell was not found in this
            // direction previously to avoid counting several clean cells
            this.boardAnalysis[ RIGHT_CELL ] += currWeight;
            // check if this cell is clean and if so than set flag indicating
            // that one clean cell already processed
            rightClean = currWeight == WEIGHT_CLEAN_CELL;
          }
        }
        else if( c < this.engine.colNumber ){
          // stop any further traverse in this direction
          right = false;
          // check whether this is bomb and analyse it if so
          content = this.engine.getCell( c, r );
          if( content == GameEngine.BOMB_PLACE )
            this.boardAnalysis[ RIGHT_CELL ] +=
                this.analyseBomb( this.engine.getBombAt( c, r ) );
          else if( content == GameEngine.PLAYER_PLACE ){
            // count opponent in order to decrease weight of this cell
            this.boardAnalysis[ RIGHT_CELL ] -= WEIGHT_OPPONENT_CELL;
          }
        }
      }

      // check top cell
      if( top ){
        c = col;
        r = row - shift;
        currWeight = this.analyseCellVerticalTraversing( c, r );
        if( currWeight != WEIGHT_BLOCKED_CELL ){
          if( !topClean ){
            // increase weight only if clean cell was not found in this
            // direction previously to avoid counting several clean cells
            this.boardAnalysis[ TOP_CELL ] += currWeight;
            // check if this cell is clean and if so than set flag indicating
            // that one clean cell already processed
            topClean = currWeight == WEIGHT_CLEAN_CELL;
          }
        }
        else if( r >= 0 ){
          // stop any further traverse in this direction
          top = false;
          // check whether this is bomb and analyse it if so
          content = this.engine.getCell( c, r );
          if( content == GameEngine.BOMB_PLACE )
            this.boardAnalysis[ TOP_CELL ] +=
                this.analyseBomb( this.engine.getBombAt( c, r ) );
          else if( content == GameEngine.PLAYER_PLACE ){
            // count opponent in order to decrease weight of this cell
            this.boardAnalysis[ TOP_CELL ] -= WEIGHT_OPPONENT_CELL;
          }
        }
      }

      // check bottom cell
      if( bottom ){
        c = col;
        r = row + shift;
        currWeight = this.analyseCellVerticalTraversing( c, r );
        if( currWeight != WEIGHT_BLOCKED_CELL ){
          if( !bottomClean ){
            // increase weight only if clean cell was not found in this
            // direction previously to avoid counting several clean cells
            this.boardAnalysis[ BOTTOM_CELL ] += currWeight;
            // check if this cell is clean and if so than set flag indicating
            // that one clean cell already processed
            bottomClean = currWeight == WEIGHT_CLEAN_CELL;
          }
        }
        else if( r < this.engine.rowsNumber ){
          // stop any further traverse in this direction
          bottom = false;
          // check whether this is bomb and analyse it if so
          content = this.engine.getCell( c, r );
          if( content == GameEngine.BOMB_PLACE )
            this.boardAnalysis[ BOTTOM_CELL ] +=
                this.analyseBomb( this.engine.getBombAt( c, r ) );
          else if( content == GameEngine.PLAYER_PLACE ){
            // count opponent in order to decrease weight of this cell
            this.boardAnalysis[ BOTTOM_CELL ] -= WEIGHT_OPPONENT_CELL;
          }
        }
      }
    }

    if( logBestEvasionDirection ){
      Log.log( "BEST EVASION DIRECTION", "top: " +
          this.boardAnalysis[ TOP_CELL ] +
          " bottom: " + this.boardAnalysis[ BOTTOM_CELL ] + " left: " +
          this.boardAnalysis[ LEFT_CELL ] +
          " right: " + this.boardAnalysis[ RIGHT_CELL ] );
    }

    // return best direction
    return this.calculateBestDirection();
  }

  /**
   * Calculates best direction taking into account weights of all possible
   * directions stored in the board analysis array.
   * @return best direction or GameEngine.STOP if no such direction exist.
   */
  private final int calculateBestDirection()
  {
    int index = -1, w = 0;
    for( int i = 0; i < boardAnalysis.length; i++ ){
      if( boardAnalysis[ i ] > w ){
        w = boardAnalysis[ i ];
        index = i;
      }
      boardAnalysis[ i ] = 0;
    }
    int dir = GameEngine.STOP;
    switch( index ){
      case LEFT_CELL:
        dir = BombmanScreen.LEFT;
        break;
      case RIGHT_CELL:
        dir = BombmanScreen.RIGHT;
        break;
      case TOP_CELL:
        dir = BombmanScreen.UP;
        break;
      case BOTTOM_CELL:
        dir = BombmanScreen.DOWN;
        break;
    }
    if( logBestDirection ){
      Log.log( "BEST DIRECTION", "best direction: " + dir +
          " mode: " + this.mode );
    }
    return dir;
  }

  /**
   * Calculates bomb weight.
   * @param bomb the bomb to process.
   * @return weight of bomb, bigger weights is more desirable.
   */
  private final int analyseBomb( BombSprite bomb )
  {
    if( bomb != null )
      return bomb.affectCell( col, row ) * bomb.ticksToExplode * 10;
    else
      return 0;
  }

  /**
   * Performs analysis of cell when traversing in vertical direction and
   * returns its weight. If cell is blocked than WEIGHT_BLOCKED_CELL will be
   * returned.
   * @param col the column
   * @param row the row
   * @return weight of cell, bigger weights is more desirable.
   */
  private final int analyseCellVerticalTraversing( int col, int row )
  {
    int currWeight = this.analyseDirectCell( col, row );
    if( currWeight != WEIGHT_BLOCKED_CELL && currWeight != WEIGHT_CLEAN_CELL ){
      // check left/right cells
      int s1 = this.analyseSideCell( col - 1, row );
      int s2 = this.analyseSideCell( col + 1, row );
      if( s2 >= s1 )
        currWeight += s2;
      else
        currWeight += s1;
    }
    return currWeight;
  }


  /**
   * Performs analysis of cell when traversing in horizontal direction and
   * returns its weight. If cell is blocked than WEIGHT_BLOCKED_CELL will be
   * returned.
   * @param col the column
   * @param row the row
   * @return weight of cell, bigger weights is more desirable.
   */
  private final int analyseCellHorizontalTraversing( int col, int row )
  {
    int currWeight = this.analyseDirectCell( col, row );
    if( currWeight != WEIGHT_BLOCKED_CELL && currWeight != WEIGHT_CLEAN_CELL ){
      // check top/bottom cells
      int s1 = this.analyseSideCell( col, row - 1 );
      int s2 = this.analyseSideCell( col, row + 1 );
      if( s2 >= s1 )
        currWeight += s2;
      else
        currWeight += s1;
    }
    return currWeight;
  }

  /**
   * Performs analysis of specified side cell and returns its weight.
   * @param c the column
   * @param r the row
   * @return weight of cell, bigger weights is more desirable.
   */
  private final int analyseSideCell( int c, int r )
  {
    int res = this.analyseDirectCell( c, r );
    if( res == WEIGHT_DANGER_CELL )
      res = WEIGHT_SIDE_DANGER_CELL;
    else if( res == WEIGHT_BLOCKED_CELL )
      res = 0;

    return res;
  }

  /**
   * Perform analysis of specified direct cell and returns its weight.
   * @param c the column
   * @param r the row
   * @return weight of cell, bigger weights is more desirable.
   */
  private final int analyseDirectCell( int c, int r )
  {
    if( this.canMoveTo( c, r ) )
      return ( this.engine.isCellHazardous( c, r ) ? WEIGHT_DANGER_CELL : WEIGHT_CLEAN_CELL );
    else
      return WEIGHT_BLOCKED_CELL;
  }

  /**
   * Method to perform initialization of movement in interception mode.
   */
  private final void initInterception()
  {
    this.initPlayerInterception();

    // calculate bonus direction
    int bonusDir = this.lookForBonus();
    int dropBombFactor = this.calculateDropBombFactor();
    // If curent bomb drop factor bigger than previous one than it is desirable
    // to drop a bomb. But we should consider whether bonus is present in interest
    // range and if so than decide whether we should drop bomb or move to the bonus.
    // To make such decision clear we will compare drop bomb factor with maximal one
    // (when opponent is in range) and if calculated drop bomb factor bigger than
    // maximal that ultimate decision will be to drop the bomb and enter into
    // fire evasion mode.
    boolean dropBomb = false;
    if( dropBombFactor >= BOMB_FACTOR_OPPONENT ){
      // bingo! opponent found - blow him out.
      dropBomb = true;
    }
    else if( bonusDir != GameEngine.STOP ){
      // bonus found and opponent out of interest range - simply pick up the bonus
      this.movingDirection = bonusDir;
      this.startInterceptionMovement();
    }
    else if( dropBombFactor > this.prevDropBombFactor ){
      // bomb factor increased comparing to previous one, thus drop bomb here
      dropBomb = true;
    }
    else{
      // no bonus found and no reason to drop a bomb, so simply try to approach player
      this.initPlayerInterception();
      this.startInterceptionMovement();
    }
    // store drop bomb factor
    this.prevDropBombFactor = dropBombFactor;
    if( logInitInterception ){
      Log.log( "INIT INTERCEPTION", "bonusDir: " + bonusDir +
        " dropBombFactor: " + dropBombFactor + " dropBomb " + dropBomb );
    }

    // if drop bomb flag is set than try to drop the bomb
    if( dropBomb ){
      // check whether bomb can be dropped without danger of being damaged by it
      // make sure to not change order of statements to avoid uneccessary
      // bomb explosion simulations
      if( this.bombsInPocket > 0 && this.dropBombOk() ){
        // try to drop bomb
        if( this.tryToDropBomb() ){
          // set fire evasion mode to avoid danger from dropped bomb
          this.setMode( MODE_EVADE );
          this.initEvasion();
        }
      }
      else{
        // bomb was not dropped, so move
        this.initPlayerInterception();
        this.startInterceptionMovement();
      }
    }
  }

  /**
   * Simulates bomb dropping at current cell and finds out whether it is
   * possible to avoid damage.
   * @return <code>true</code> if bomb can be safely dropped without any danger of
   * being blowed up by it.
   */
  private final boolean dropBombOk()
  {
    // charge test bomb at current position
    this.testBomb.charge( this, this.col, this.row );
    // find evasion direction
    int evadeDir = this.calculateBestEvasionDirection();
    // blow bomb to clear hazzard info
    this.testBomb.blowUpDestructionRange( false );
    // clear cell when bomb was placed for testing purposes
    this.engine.setCell( GameEngine.ENEMY_PLACE, this.col, this.row );

    if( logDropBombOk ){
      Log.log( " DROP BOMB OK", "Drop bomb OK: " +
          ( evadeDir != GameEngine.STOP ) );
    }
    return evadeDir != GameEngine.STOP;
  }

  /**
   * Method to perform initialization of player interception.
   */
  private final void initPlayerInterception()
  {
    int dx = this.engine.player.x - x;
    int dy = this.engine.player.y - y;
    // calculating next moving direction for object inteception if not moving to bonus
    switch( nextMovingDirection ){
      case GameEngine.STOP: // undefined
        if( canMoveTo( col, row - 1 ) )
          movingDirection = BombmanScreen.UP;
        else if( canMoveTo( col, row + 1 ) )
          movingDirection = BombmanScreen.DOWN;
        else if( canMoveTo( col - 1, row ) )
          movingDirection = BombmanScreen.LEFT;
        else if( canMoveTo( col + 1, row ) )
          movingDirection = BombmanScreen.RIGHT;
        else{
          // dead end
          nextMovingDirection = GameEngine.STOP;
          movingDirection = GameEngine.STOP;
        }
        break;

      case BombmanScreen.UP: // Up
        if( dx > 0 ){
          // object at the right side
          if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( dx < 0 ){
          // object at the left side
          if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( canMoveTo( col, row - 1 ) )
          movingDirection = BombmanScreen.UP;
        else if( canMoveTo( col - 1, row ) )
          movingDirection = BombmanScreen.LEFT;
        else if( canMoveTo( col + 1, row ) )
          movingDirection = BombmanScreen.RIGHT;
        else if( canMoveTo( col, row + 1 ) )
          movingDirection = BombmanScreen.DOWN;
        else{
          // dead end
          nextMovingDirection = GameEngine.STOP;
          movingDirection = GameEngine.STOP;
        }
        break;

      case BombmanScreen.DOWN: // down
        if( dx > 0 ){
          // object at the rigth side
          if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( dx < 0 ){
          // object at the left side
          if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( canMoveTo( col, row + 1 ) )
          movingDirection = BombmanScreen.DOWN;
        else if( canMoveTo( col - 1, row ) )
          movingDirection = BombmanScreen.LEFT;
        else if( canMoveTo( col + 1, row ) )
          movingDirection = BombmanScreen.RIGHT;
        else if( canMoveTo( col, row - 1 ) )
          movingDirection = BombmanScreen.UP;
        else{
          // dead end
          nextMovingDirection = GameEngine.STOP;
          movingDirection = GameEngine.STOP;
        }
        break;

      case BombmanScreen.LEFT: // left
        if( dy > 0 ){
          // object below
          if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( dy < 0 ){
          // object above
          if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( canMoveTo( col - 1, row ) )
          movingDirection = BombmanScreen.LEFT;
        else if( canMoveTo( col, row - 1 ) )
          movingDirection = BombmanScreen.UP;
        else if( canMoveTo( col, row + 1 ) )
          movingDirection = BombmanScreen.DOWN;
        else if( canMoveTo( col + 1, row ) )
          movingDirection = BombmanScreen.RIGHT;
        else{
          // dead end
          nextMovingDirection = GameEngine.STOP;
          movingDirection = GameEngine.STOP;
        }
        break;

      case BombmanScreen.RIGHT: // right
        if( dy > 0 ){
          // object above
          if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( dy < 0 ){
          // object bellow
          if( canMoveTo( col, row - 1 ) )
            movingDirection = BombmanScreen.UP;
          else if( canMoveTo( col + 1, row ) )
            movingDirection = BombmanScreen.RIGHT;
          else if( canMoveTo( col, row + 1 ) )
            movingDirection = BombmanScreen.DOWN;
          else if( canMoveTo( col - 1, row ) )
            movingDirection = BombmanScreen.LEFT;
          else{
            // dead end
            nextMovingDirection = GameEngine.STOP;
            movingDirection = GameEngine.STOP;
          }
        }
        else if( canMoveTo( col + 1, row ) )
          movingDirection = BombmanScreen.RIGHT;
        else if( canMoveTo( col, row - 1 ) )
          movingDirection = BombmanScreen.UP;
        else if( canMoveTo( col, row + 1 ) )
          movingDirection = BombmanScreen.DOWN;
        else if( canMoveTo( col - 1, row ) )
          movingDirection = BombmanScreen.LEFT;
        else{
          // dead end
          nextMovingDirection = GameEngine.STOP;
          movingDirection = GameEngine.STOP;
        }
        break;
    }
  }

  /**
   * Starts movement in interception mode after moving direction found.
   */
  private final void startInterceptionMovement()
  {
    this.inDanger = false;
    // set appropriate frame index and move sprite
    switch( movingDirection ){
      case GameEngine.STOP:
        this.frameIndex = 3;
        break;

      case BombmanScreen.UP:
        this.frameIndex = 1;
        if( this.engine.isCellHazardous( col, row - 1 ) ){
          this.inDanger = true;
          movingDirection = GameEngine.STOP; // stop here
        }
        else
          this.move( 0, -this.movingSpeed );
        break;

      case BombmanScreen.DOWN:
        this.frameIndex = 4;
        if( this.engine.isCellHazardous( col, row + 1 ) ){
          this.inDanger = true;
          movingDirection = GameEngine.STOP; // stop here
        }
        else
          this.move( 0, this.movingSpeed );
        break;

      case BombmanScreen.LEFT:
        this.frameIndex = 7;
        if( this.engine.isCellHazardous( col - 1, row ) ){
          this.inDanger = true;
          movingDirection = GameEngine.STOP; // stop here
        }
        else
          this.move( -this.movingSpeed, 0 );
        break;

      case BombmanScreen.RIGHT:
        this.frameIndex = 10;
        if( this.engine.isCellHazardous( col + 1, row ) ){
          this.inDanger = true;
          movingDirection = GameEngine.STOP; // stop here
        }
        else
          this.move( this.movingSpeed, 0 );
        break;
    }

    if( logStartInterceptionMovement ){
      Log.log( "START INTERCEPTION", "inDanger: " + inDanger +
          " moving dir: " + movingDirection );
    }

    // if target cell dangerous than check current one and decide what to do
    if( inDanger && this.engine.isCellHazardous( col, row ) )
      this.setMode( MODE_EVADE );
  }

  /**
   * Examine bombman neighbours to decide whether bomb should be dropped or not.
   * This method calculates and returns weight of current bombman position in
   * accordance with number of soft walls around him and with relative position
   * of opponent. Bomb can be dropped only when at least one soft wall found
   * near bombman and at least one soft wall is in adjacent cell. Also bomb
   * must be dropped if opponent found in destruction range despite of number
   * of soft walls around.
   * @return weight of current bombman position. Bigger value means that bomb
   * dropping is more desirable.
   */
  private final int calculateDropBombFactor()
  {
    int c, r, currWeight = 0;
    byte content;
    boolean left = true, right = true, top = true, bottom = true;
    for( int i = GameConfig.aiLookRange - 1, shift; i >= 0; i-- ){
      shift = GameConfig.aiLookRange - i;
      // check left cell
      if( left ){
        c = col - shift;
        r = row;
        if( c >= 0 ){
          content = this.engine.getCell( c, r );
          if( content == GameEngine.HARD_WALL_PLACE )
            left = false;
          else if( content == GameEngine.SOFT_WALL_PLACE ){
            currWeight += BOMB_FACTOR_SOFT_WALL;
            left = false;
          }
          else if( content == GameEngine.PLAYER_PLACE ){
            currWeight += BOMB_FACTOR_OPPONENT;
            left = false;
          }
        }
        else
          left = false;
      }

      // check right cell
      if( right ){
        c = col + shift;
        r = row;
        if( c < this.engine.colNumber ){
          content = this.engine.getCell( c, r );
          if( content == GameEngine.HARD_WALL_PLACE )
            right = false;
          else if( content == GameEngine.SOFT_WALL_PLACE ){
            currWeight += BOMB_FACTOR_SOFT_WALL;
            right = false;
          }
          else if( content == GameEngine.PLAYER_PLACE ){
            currWeight += BOMB_FACTOR_OPPONENT;
            right = false;
          }
        }
      }
      else
        right = false;

      // check top cell
      if( top ){
        c = col;
        r = row - shift;
        if( r >= 0 ){
          content = this.engine.getCell( c, r );
          if( content == GameEngine.HARD_WALL_PLACE )
            top = false;
          else if( content == GameEngine.SOFT_WALL_PLACE ){
            currWeight += BOMB_FACTOR_SOFT_WALL;
            top = false;
          }
          else if( content == GameEngine.PLAYER_PLACE ){
            currWeight += BOMB_FACTOR_OPPONENT;
            top = false;
          }
        }
        else
          top = false;
      }

      // check bottom cell
      if( bottom ){
        c = col;
        r = row + shift;
        if( r < this.engine.rowsNumber ){
          content = this.engine.getCell( c, r );
          if( content == GameEngine.HARD_WALL_PLACE )
            bottom = false;
          else if( content == GameEngine.SOFT_WALL_PLACE ){
            currWeight += BOMB_FACTOR_SOFT_WALL;
            bottom = false;
          }
          else if( content == GameEngine.PLAYER_PLACE ){
            currWeight += BOMB_FACTOR_OPPONENT;
            bottom = false;
          }
        }
        else
          bottom = false;
      }
    }

    // if opponent not found near and some soft walls found than check if at least
    // one of them is in adjacent cell
    if( currWeight < BOMB_FACTOR_OPPONENT && currWeight > 0 ){
      boolean softWallLeft = false, softWallRight = false, softWallTop = false,
          softWallBottom = false;
      // check whether at least one soft wall present in adjacent cells
      if( this.col > 0 )
        softWallLeft =
            this.engine.getCell( this.col - 1, this.row ) == GameEngine.SOFT_WALL_PLACE;
      if( this.col < this.engine.colNumber - 1 )
        softWallRight =
            this.engine.getCell( this.col + 1, this.row ) == GameEngine.SOFT_WALL_PLACE;
      if( this.row > 0 )
        softWallTop =
            this.engine.getCell( this.col, this.row - 1 ) == GameEngine.SOFT_WALL_PLACE;
      if( this.row < this.engine.rowsNumber - 1 )
        softWallBottom =
            this.engine.getCell( this.col, this.row + 1 ) == GameEngine.SOFT_WALL_PLACE;

      if( !( softWallLeft | softWallRight | softWallTop | softWallBottom ) )
        currWeight = 0;// no soft walls found near - reset weight to avoid wrong assumptions
    }

    return currWeight;
  }

  /**
   * Perform check whether bonus present at one of the adjacent cells and if so
   * than return moving direction to the bonus. Additionally method calculates
   * which bonus is most desirable if several found.
   * @return moving direction to the bonus if any found or GameEngine.STOP if
   * no bonus was detected.
   */
  private final int lookForBonus()
  {
    int c, r, currWeight;
    boolean left = true, right = true, top = true, bottom = true;
    for( int i = GameConfig.aiLookRange - 1, shift; i >= 0; i-- ){
      shift = GameConfig.aiLookRange - i;
      // check left cell
      if( left ){
        c = col - shift;
        r = row;
        if( c < 0 || ( currWeight = checkCellForBonus( c, r ) ) == -1 )// N.B. check cell for bonus will be invoked each time when cell is in range
          left = false;// no need to look any further
        else
          this.boardAnalysis[ LEFT_CELL ] += currWeight;
      }

      // check right cell
      if( right ){
        c = col + shift;
        r = row;
        if( c >= this.engine.colNumber ||
            ( currWeight = checkCellForBonus( c, r ) ) == -1 )// N.B. check cell for bonus will be invoked each time when cell is in range
          right = false;
        else
          this.boardAnalysis[ RIGHT_CELL ] += currWeight;
      }

      // check top cell
      if( top ){
        c = col;
        r = row - shift;
        if( r < 0 || ( currWeight = checkCellForBonus( c, r ) ) == -1 )// N.B. check cell for bonus will be invoked each time when cell is in range
          top = false;
        else
          this.boardAnalysis[ TOP_CELL ] += currWeight;
      }

      // check bottom cell
      if( bottom ){
        c = col;
        r = row + shift;
        if( r >= this.engine.rowsNumber ||
            ( currWeight = checkCellForBonus( c, r ) ) == -1 ) // N.B. check cell for bonus will be invoked each time when cell is in range
          bottom = false;
        else
          this.boardAnalysis[ BOTTOM_CELL ] += currWeight;
      }
    }
    // return best direction
    return this.calculateBestDirection();
  }

  /**
   * Perform check whether bonus lives at specified cell. Than calculates and returns
   * cell weight in accordance with its content.
   * @param col the column of cell to check.
   * @param row the row of cell to check.
   * @return cell weight depending on its content (what bonus here), 0 if no
   * bonus found and cell is empty or -1 if cell is blocked or dangerous.
   */
  private final int checkCellForBonus( int col, int row )
  {
    if( this.engine.isCellHazardous( col, row ) )
      return -1;// danger cell found - stop looking in this direction
    else{
      int weight;
      switch( this.engine.getCell( col, row ) ){
        case GameEngine.BOMB_BONUS_PLACE:
          weight = 100;
          break;

        case GameEngine.FIRE_RANGE_BONUS_PLACE:
          weight = 50;
          break;

        case GameEngine.SPEED_BONUS_PLACE:
          weight = 10;
          break;

        case GameEngine.EMPTY_PLACE:
        case GameEngine.SHADOWED_EMPTY_PLACE:
          weight = 0;
          break;

        default:
          weight = -1;
          break;
      }
      return weight;
    }
  }

  /**
   * Finishes sprite moving to the destination cell
   * @param time the current game time.
   */
  private final void finishMove( int time )
  {
    int mod = 0;
    switch( this.movingDirection ){
      case BombmanScreen.UP:
        mod = y % GameConfig.tileHeight;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( 0, -this.movingSpeed );
          if( mod == GameConfig.tileHeight / 2 )
            frameIndex = 2;
        }
        else{
          // bombman in target cell
          this.nextMovingDirection = BombmanScreen.UP;
          this.processMove( time, col, row + 1 );
        }
        break;

      case BombmanScreen.DOWN: // DOWN
        mod = y % GameConfig.tileHeight;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( 0, this.movingSpeed );
          if( mod == GameConfig.tileHeight / 2 )
            frameIndex = 5;
        }
        else{
          // bombman in target cell
          this.nextMovingDirection = BombmanScreen.DOWN;
          this.processMove( time, col, row - 1 );
        }
        break;

      case BombmanScreen.LEFT: // LEFT
        mod = x % GameConfig.tileWidth;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( -this.movingSpeed, 0 );
          if( mod == GameConfig.tileWidth / 2 )
            frameIndex = 8;
        }
        else{
          // bombman in target cell
          this.nextMovingDirection = BombmanScreen.LEFT;
          this.processMove( time, col + 1, row );
        }
        break;

      case BombmanScreen.RIGHT: // RIGHT
        mod = x % GameConfig.tileWidth;
        if( mod != 0 ){
          // target cell doesn't reached yet
          this.move( this.movingSpeed, 0 );
          if( mod == GameConfig.tileWidth / 2 )
            frameIndex = 11;
        }
        else{
          // bombman in target cell
          this.nextMovingDirection = BombmanScreen.RIGHT;
          this.processMove( time, col - 1, row );
        }
        break;
    }
  }

  /**
   * Performs all actions in accordance to perform ultimate moving of bombman
   * to the destination cell.
   * @param time the current game time.
   * @param prevCol the previous column where bombman come from.
   * @param prevRow the previous row where bombman come from.
   */
  private final void processMove( int time, int prevCol, int prevRow )
  {
    this.engine.processBombmanMovement( this, prevCol, prevRow );
    this.movingDirection = GameEngine.STOP;
    this.checkSpeed( time );
  }

  /**
   * Should be implemented by subclasses in order to provide logic of death.
   */
  protected void processDeath()
  {
    this.engine.opponentDead();
  }

  /**
   * Should be implemented by subclasses in order to provide logic of win.
   */
  protected void processWin()
  {
    this.engine.opponentWin();
  }

  /**
   * Draws this sprite on the provided graphics context.
   * @param g the graphics context.
   */
  public void paint( Graphics g )
  {
    int xx = x + engine.boardX;
    int yy = y + engine.boardY;
    int h = GameConfig.tileHeight;
    int w = GameConfig.tileWidth;
    if( yy + GameConfig.tileHeight > GameConfig.gameScreenActiveHeight )
      h = GameConfig.gameScreenActiveHeight - yy;
    if( xx + GameConfig.tileWidth > GameConfig.screenWidth )
      w = GameConfig.screenWidth - xx;
    switch( this.state ){
      case STATE_ACTIVE:
      case STATE_FREEZE:
        g.setClip( xx, yy, w, h );
        g.drawImage( this.engine.bombermanImages[ 0 ],
            xx - frameIndex * GameConfig.tileWidth,
            yy - GameConfig.tileHeight, Graphics.TOP | Graphics.LEFT );
        break;

      case STATE_DEATH_ANIMATION:
        g.setClip( xx, yy, w, h );
        g.drawImage( this.engine.bombermanImages[ 1 ],
            xx - frameIndex * GameConfig.tileWidth,
            yy - GameConfig.tileHeight, Graphics.TOP | Graphics.LEFT );
        break;

      case STATE_WIN_ANIMATION:
        yy += GameConfig.tileHeight - GameConfig.bombmanWinAnimationHeight;
        g.setClip( xx, yy, GameConfig.bombmanWinAnimationWidth,
            GameConfig.bombmanWinAnimationHeight );
        g.drawImage( this.engine.bombermanImages[ 2 ],
            xx - frameIndex * GameConfig.bombmanWinAnimationWidth,
            yy - GameConfig.bombmanWinAnimationHeight, Graphics.TOP | Graphics.LEFT );
        break;
    }
  }

  /**
   * Returns <code>true</code> if player's sprite can move to the specified position.
   * @param col the column to move.
   * @param row the row to move.
   * @return <code>true</code> if player's sprite can move to the specified position.
   */
  protected final boolean canMoveTo( int col, int row )
  {
    boolean possible = super.canMoveTo( col, row );
    if( possible ){
      // check collision with opponent
      int yy = row * GameConfig.tileHeight;
      int xx = col * GameConfig.tileWidth;
      possible = !( yy < engine.player.y + GameConfig.tileHeight &&
        yy + GameConfig.tileHeight > engine.player.y &&
        xx < engine.player.x + GameConfig.tileWidth &&
        xx + GameConfig.tileWidth > engine.player.x );
    }
    return possible;
  }
}