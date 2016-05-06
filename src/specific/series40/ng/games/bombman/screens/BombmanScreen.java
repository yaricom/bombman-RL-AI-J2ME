/**
 * $Id: BombmanScreen.java 197 2005-07-08 16:00:57Z yaric $
 */
package ng.games.bombman.screens;

import javax.microedition.lcdui.*;

import com.nokia.mid.ui.FullCanvas;

import ng.games.bombman.BombmanGame;
import ng.games.bombman.GameConfig;
import ng.mobile.game.util.Log;

/**
 * Nokia specific (Full screen consumption).
 *
 * This class represents drawing and events recieving facilities. It recieves
 * such low level events from underlying system and forwards it to the game controller.
 * Used to encapsulate all device specific details from the game.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class BombmanScreen extends FullCanvas
{
  /** Canvas listener to delegate game events to */
  public final BombmanGame game;

  /** Positive command */
  private Command positiveCommand;
  /** Negative command */
  private Command negativeCommand;

  /**
   * Command buttons' images
   * <ul>
   * <li> 0 - positive
   * <li> 1 - negative
   * </ul>
   */
  private Image[] images;

  /** Flag to indicate whether commands should be drawn in full screen mode */
  private boolean showCommands;

  //
  // Loging definitions
  //
  private static final boolean logAddCommand = Log.enabled & false;
  private static final boolean logRemoveCommand = Log.enabled & false;


  /**
   * Constructs new instance.
   * @param parent the parent game.
   */
  public BombmanScreen( BombmanGame parent )
  {
    this.game = parent;
  }

  /**
   * Method to perform initialization steps if required.
   */
  public void init()
  {
    // set appropriate font
    this.images = this.game.dao.getImages( "fs" );
    this.showCommands = true;
  }

  /**
   * Adds a command to the Displayable.
   * @param cmd the command to be added.
   */
  public void addCommand( Command cmd )
  {
    int type = cmd.getCommandType();
    String label = cmd.getLabel();
    if( type == Command.OK ){
      this.positiveCommand = cmd;
    }
    else{
      this.negativeCommand = cmd;
    }

    if( logAddCommand ){
      Log.log( "ADD COMMAND", cmd.getLabel() );
    }
  }

  /**
   * Removes a command from the Displayable.
   * @param cmd the command to be removed.
   */
  public void removeCommand( Command cmd )
  {
    if( this.positiveCommand != null && this.positiveCommand == cmd ){
      this.positiveCommand = null;
    }
    else if( this.negativeCommand != null && this.negativeCommand == cmd ){
      this.negativeCommand = null;
    }

    if( logAddCommand ){
      Log.log( "REMOVE COMMAND", cmd.getLabel() );
    }
  }

  /**
   * Sets a listener for Commands to this Displayable, replacing any
   * previous CommandListener.
   * @param l the new listener, or <code>null</code>.
   */
  public void setCommandListener( CommandListener l )
  {
    // just not to throw exception as standard Nokia UI implementation do
  }

  /**
   * Hides commands in full screen mode.
   * @param hide if <code>true</code> than commands will not be drawn in full
   * screen mode, otherwise its will be drawn.
   */
  public void hideCommands( boolean hide )
  {
    this.showCommands = !hide;
  }

  /**
   * Called when a key is pressed.
   * @param keyCode The key code of the key that was repeated
   */
  protected void keyPressed( int keyCode )
  {
    // filter soft buttons
    if( this.game.gameState != BombmanGame.STATE_SHOW_SPLASH ){
      if( keyCode == FullCanvas.KEY_SOFTKEY1 )
        this.fireCommandAction( this.positiveCommand );
      else if( keyCode == FullCanvas.KEY_SOFTKEY2 )
        this.fireCommandAction( this.negativeCommand );
      else
        this.game.keyPressed( keyCode );
    }
    else
      this.game.keyPressed( keyCode );
  }

  /**
   * Called when a key is released.
   * @param keyCode The key code of the key that was released
   */
  protected void keyReleased( int keyCode )
  {
    this.game.keyReleased( keyCode );
  }

  /**
   * Gets the game action associated with the given key code of the device.
   * @param keyCode the key code.
   * @return the game action corresponding to this key, or 0 if none.
   */
  public int getGameAction( int keyCode )
  {
    int action = super.getGameAction( keyCode );
    if( action == Canvas.UP || keyCode == ActionKeysMappings.GAME_KEY_UP )
      action = Canvas.UP;
    else if( action == Canvas.DOWN || keyCode == ActionKeysMappings.GAME_KEY_DOWN )
      action = Canvas.DOWN;
    else if( action == Canvas.LEFT || keyCode == ActionKeysMappings.GAME_KEY_LEFT )
      action = Canvas.LEFT;
    else if( action == Canvas.RIGHT || keyCode == ActionKeysMappings.GAME_KEY_RIGHT )
      action = Canvas.RIGHT;
    else if( action == Canvas.FIRE || keyCode == ActionKeysMappings.GAME_KEY_FIRE )
      action = Canvas.FIRE;

    return action;
  }

  /**
   * Method to propagate command action to registered <code>CommandListener</code>
   * @param command the command to propagate.
   */
  private final void fireCommandAction( Command command )
  {
    if( command != null )
      this.game.commandAction( command, this );
  }

  /**
   * Method to render current game.
   * @param g The Graphics context.
   */
  protected void paint( Graphics g )
  {
    this.game.paint( g );

    if( this.showCommands ){
      // draw commands
      g.setClip( 0, GameConfig.commandY,
          GameConfig.screenWidth, GameConfig.commandHeight );

      if( this.positiveCommand != null )
        g.drawImage( this.images[ 0 ], GameConfig.commandPositiveX,
            GameConfig.commandY, Graphics.LEFT | Graphics.TOP );

      if( this.negativeCommand != null )
        g.drawImage( this.images[ 1 ], GameConfig.commandNegativeX,
            GameConfig.commandY, Graphics.RIGHT | Graphics.TOP );
    }
  }

  /**
   * Invoked shortly after the Canvas has been removed from the display.
   * Used to pause game execution for Nokia phones.
   */
  protected void hideNotify()
  {
    this.game.hideNotify();
  }
}