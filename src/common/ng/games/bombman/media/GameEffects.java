/**
 * $Id: GameEffects.java 204 2005-07-11 15:51:19Z yaric $
 */
package ng.games.bombman.media;

import ng.games.bombman.BombmanDAO;

/**
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
   * Default constructor.
   * @param dao the DAO to acquire resources.
   */
  public GameEffects( BombmanDAO dao ){}

  /**
   * Starts playing title music.
   */
  public void startTitleMusic(){}

  /**
   * Stops playing title music.
   */
  public void stopTitleMusic(){}

  /**
   * Bomb explosion
   */
  public void bombExplosion(){}

  /**
   * Pickup bonus
   */
  public void pickUpBonus(){}

  /**
   * Scream
   */
  public final void scream()
  {
  }

  /**
   * Win sound
   */
  public final void winSound()
  {
  }

  /**
   * Method to destroy all resources associated with this class.
   */
  public final void destroy(){}
}
