/**
 * $Id: GameEffects.java 997 2006-08-30 16:14:09Z yaric $
 */
package ng.games.bombman.media;

import com.samsung.util.AudioClip;

import ng.games.bombman.Settings;
import ng.games.bombman.BombmanDAO;

/**
 * Samsung MMF sound specific.
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
   * <li> 4 - Title music sound
   * </ul>
   */
  private AudioClip[] gameSounds;

  /** Previously playing sound */
  private int prevSound;

  /**
   * Default constructor
   * @param dao the resources factory.
   */
  public GameEffects( BombmanDAO dao )
  {
    try{
      gameSounds = new AudioClip[ 5 ];
      gameSounds[ 0 ] = new AudioClip( 1, "/blow.mmf" );
      gameSounds[ 1 ] = new AudioClip( 1, "/bonus.mmf" );
      gameSounds[ 2 ] = new AudioClip( 1, "/scream.mmf" );
      gameSounds[ 3 ] = new AudioClip( 1, "/hallelujah.mmf" );
      // load title music
      gameSounds[ 4 ] = new AudioClip( 1, "/title.mmf" );
    } catch( Exception ex ){
      ex.printStackTrace();
    }
  }

  /**
   * Play title music.
   */
  public void startTitleMusic()
  {
    if( this.gameSounds[ 4 ] != null && Settings.music ){
      try{
        gameSounds[ prevSound ].stop(); // stop previous sound
        gameSounds[ 4 ].play( 20, 3 );
      } catch( Exception e ){
        e.printStackTrace();
      }
    }
    this.prevSound = 4;
  }

  /**
   * Stop title music.
   */
  public void stopTitleMusic()
  {
    if( this.gameSounds[ 4 ] != null )
      this.gameSounds[ 4 ].stop();
  }


  /**
   * Bomb explosion
   */
  public void bombExplosion()
  {
    this.playSound( 0, 5, 1 );
  }

  /**
   * Pickup bonus
   */
  public void pickUpBonus()
  {
    this.playSound( 1, 4, 1 );
  }

  /**
   * Scream
   */
  public final void scream()
  {
    this.playSound( 2, 4, 1 );
  }

  /**
   * Win sound
   */
  public final void winSound()
  {
    this.playSound( 3, 5, 1 );
  }

  /**
   * Method to destroy all resources associated with this class.
   */
  public final void destroy()
  {
    if( this.gameSounds != null && this.gameSounds[ 4 ] != null )
      this.gameSounds[ 4 ].stop();
  }

  /**
   * Method to play specified sound.
   * @param sound the sound to play.
   * @param vol the sound volume.
   * @param loop the number of loops to play.
   */
  private void playSound( int sound, int vol, int loop )
  {
    if( Settings.sound ){
      try{
        gameSounds[ prevSound ].stop(); // stop previous sound
        gameSounds[ sound ].play( loop, vol );
      } catch( Exception e ){
        e.printStackTrace();
      }
    }
    this.prevSound = sound;
  }
}
