/**
 * $Id: GameEffects.java 997 2006-08-30 16:14:09Z yaric $
 */
package ng.games.bombman.media;

import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import com.nokia.mid.sound.Sound;

import ng.games.bombman.BombmanDAO;
import ng.games.bombman.Settings;

/**
 * Nokia series 60 specific.
 *
 * Class to perform game effects.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class GameEffects
{
  /** <code>true</code> if device has sound support */
  public static final boolean hasSound = true;

  /**
   * Array to hold game sounds
   * <ul>
   * <li> 0 - Blow sound
   * <li> 1 - Bonus sound
   * <li> 2 - Scream sound
   * <li> 3 - Win sound
   * </ul>
   */
  private Sound[] gameSounds;

  /** Title sound */
  private Player titleSound;

  /** Previously playing sound */
  private int prevSound;


  /**
   * Default constructor.
   * @param dao the DAO to acquire resources.
   */
  public GameEffects( BombmanDAO dao )
  {
    InputStream is = null;
    try{
      // load sounds
      Object[] tmp = dao.loadBundle( "sound" );
      gameSounds = new Sound[ 4 ];
      for( int i = 3; i >= 0; i-- )
        gameSounds[ i ] = new Sound( (byte[])tmp[ i ], Sound.FORMAT_WAV );

      // load title music
      is = getClass().getResourceAsStream( "/title.mid" );
      titleSound = Manager.createPlayer( is, "audio/midi" );
      titleSound.prefetch();
      titleSound.setLoopCount( -1 );

      is.close();
    } catch( Exception ex ){
      ex.printStackTrace();
    }
  }


  /**
   * Starts playing title music.
   */
  public void startTitleMusic()
  {
    if( this.titleSound != null && Settings.music )
      try{
        this.titleSound.start();
      } catch( Exception ex ){}
  }

  /**
   * Stop title music.
   */
  public void stopTitleMusic()
  {
    if( this.titleSound != null )
      try{
        this.titleSound.stop();
      } catch( Exception ex ){}
  }


  /**
   * Bomb explosion
   */
  public void bombExplosion()
  {
    this.playSound( 0 );
  }

  /**
   * Pickup bonus
   */
  public void pickUpBonus()
  {
    this.playSound( 1 );
  }

  /**
   * Scream
   */
  public final void scream()
  {
    this.playSound( 2 );
  }

  /**
   * Win sound
   */
  public final void winSound()
  {
    this.playSound( 3 );
  }

  /**
   * Method to destroy all resources associated with this class.
   */
  public final void destroy()
  {
    if( this.titleSound != null ){
      try{
        this.titleSound.stop();
      } catch( Exception ex ){
      }
      this.titleSound.deallocate();
    }
  }

  /**
   * Play specific sound
   * @param sound the sound index to play.
   */
  private void playSound( int sound )
  {
    if( Settings.sound ){
      try{
        if( gameSounds[ prevSound ] != null &&
            gameSounds[ prevSound ].getState() == Sound.SOUND_PLAYING )
          gameSounds[ prevSound ].stop(); // stop previous sound

        if( gameSounds[ sound ] != null )
          gameSounds[ sound ].play( 1 );
      } catch( Exception e ){
        e.printStackTrace();
      }
    }
    this.prevSound = sound;
  }
}
