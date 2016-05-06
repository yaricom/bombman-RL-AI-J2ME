/**
 * $Id: BombmanDAO.java 997 2006-08-30 16:14:09Z yaric $
 */
package ng.games.bombman;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;

import ng.mobile.game.util.ImageDataDecoder;
import ng.mobile.game.util.Log;

/**
 * Less resources cashing enabled.
 * This class encapsulates all data access logic.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class BombmanDAO
{
  /** Number of resources to preload */
  public static final int resToPreload = 3;

  /** Instance of image decoder */
  private ImageDataDecoder decoder;

  /** Array of textual game resources */
  private String resources[][];
  /** Array of help resources */
  private String help[][];

  /**
   * Menu images
   * <ul>
   * <li> 0 - background fill
   * <li> 1 - title
   * </ul>
   */
  private Image[] menuImages;

  /** Counter to check number of loaded resources */
  private int resCounter;

  //
  // Logging definition
  //
  private final static boolean logInitialLoad = Log.enabled & true;

  /**
   * Default constructor.
   */
  public BombmanDAO()
  {
    this.decoder = new ImageDataDecoder();
  }

  /**
   * Releases all holded resources
   */
  public final void cleanIntermediate()
  {
    this.decoder.release();
    this.resCounter = 0;
  }

  /**
   * Returns menu images. (Should be cached when possible)
   * <ul>
   * <li> 0 - background fill
   * <li> 1 - title
   * </ul>
   * @return menu images.
   */
  public final Image[] getMenuImages()
  {
    return this.menuImages;
  }

  /**
   * Returns menu captions.
   * @param lang the captions language.
   * @return menu captions.
   */
  public final Image getMenuCaptions( int lang )
  {
    return this.getImage( "mc", lang );
  }

  /**
   * Returns common images
   * <ul>
   * <li> 0 - flame
   * <li> 1 - game tiles
   * <li> 2 - scroller mark
   * <li> 3 - scroller pointer
   * </ul>
   * @return common images
   */
  public Image[] getCommonImages()
  {
    return this.getImages( "cm" );
  }

  /**
   * Returns options' menu images.
   * <ul>
   * <li> 0 - flags
   * <li> 1 - checkbox OFF
   * <li> 2 - checkbox ON
   * <li> 3 - icon language
   * <li> 4 - icon music
   * <li> 5 - icon sound
   * </ul>
   * @return options' menu images.
   */
  public Image[] getOptionsMenuImages()
  {
    return this.getImages( "om" );
  }

  /**
   * Returns sprites images.
   * <ul>
   * <li> 0 - Walk
   * <li> 1 - Death
   * <li> 2 - Wain
   * </ul>
   * @return sprites images.
   */
  public Image[] getSprites()
  {
    return this.getImages( "b" );
  }

  /**
   * Returns clock sprites.
   * <ul>
   * <li> 0 - bottom
   * <li> 1 - top
   * </ul>
   * @return clock sprites.
   */
  public Image[] getClockSprites()
  {
    return this.getImages( "c" );
  }

  /**
   * Returns score image.
   * @return score image.
   */
  public Image getScoreImage()
  {
    return this.getImage( "sc" );
  }

  /**
   * Returns help message.
   * @param lang the language.
   * @return help message.
   */
  public final String getHelpMessage( final int lang )
  {
    return this.help[ lang ][ 0 ];
  }

  /**
   * Returns about message.
   * @param lang the language.
   * @return about message.
   */
  public final String getAboutMessage( final int lang )
  {
    return this.help[ lang ][ 1 ];
  }

  /**
   * Preloads and cache common resources before game starts.
   * @return <code>true</code> if all resources already loaded.
   */
  public final boolean preloadResources()
  {
    if( logInitialLoad ){
      Log.log( "PRELOAD RESOURCES", "Step: " + this.resCounter );
    }

    boolean finished = false;
    switch( this.resCounter ){
      case 0:
        // mining menu images
        this.menuImages = this.getImages( "m" );
        break;

      case 1:
        // load resources
        this.resources = this.getTextsFromBundle( "res" );
        break;

      case 2:
        // load help
        this.help = this.getTextsFromBundle( "res.hlp" );
        break;

      default:
        finished = true;
        break;
    }

    // increment resources counter
    this.resCounter++;
    return finished;
  }

  /**
   * Represents all resources from specified bundle as two dimensions string array.
   * @param name the bundle name.
   * @return two dimensional string array with resources.
   */
  private final String[][] getTextsFromBundle( final String name )
  {
    Object[] tmp = this.loadBundle( name );
    String[][]res = new String[ tmp.length ][];
    for( int i = 0; i < tmp.length; i++ ){
      res[ i ] = (String[])tmp[ i ];
    }
    return res;
  }

  /**
   * Returns text resource under specified index in specified language.
   * @param index the resource index
   * @param lang the language.
   * @return text resource
   */
  public final String getResource( final int index, final int lang )
  {
    return this.resources[ lang ][ index ];
  }

  /**
   * Returns level description.
   * @param level the level index.
   * @return level description as strings array.
   */
  public final String[] getLevelData( int level )
  {
    return this.getTextsFromBundle( "l" )[ level ];
  }

  /**
   * Loads <code>Image</code> in accordance with game specific requirements.
   * @param name the <code>Image</code> name.
   * @return requested <code>Image</code> in accordance with specified name or
   * <code>null</code> if image not found.
   */
  public final Image getImage( final String name )
  {
    Image tmp = null;
    try {
      tmp = Image.createImage( "/" + name + ".png" );
    } catch (Exception ex) { throw new IllegalArgumentException( ex.toString() ); }
    return tmp;
  }

  /**
   * Loads array of images from specified resource file.
   * @param res the resource file name.
   * @return array of images.
   */
  public final Image[] getImages( final String res )
  {
    return this.getImages(
        this.getClass().getResourceAsStream( "/" + res + ".img" ), null, -1 );
  }

  /**
   * Loads array of images from specified input stream.
   * @param is the input stream to load image data from.
   * @param plt the palette to use for substitution or <code>null</code> if default
   * palette will be in use.
   * @param index the index of image to return or -1 if all images should be
   * returned.
   * @return array of images.
   */
  private final Image[] getImages( InputStream is, byte[] plt, int index )
  {
    Image[] img = null;
    try{
      img = decoder.extractImages( is, plt, index );
      this.decoder.release();
    } catch( Exception ex ){
      throw new IllegalStateException( ex.toString() );
    }
    return img;
  }

  /**
   * Loads images bundle with specified name and extracts from it image with
   * specified index.
   * @param res the resource file name.
   * @param index the image index.
   * @return image with specified index.
   */
  private final Image getImage( final String res, int index )
  {
    return this.getImages(
        this.getClass().getResourceAsStream( "/" + res + ".img" ),
        null, index )[ 0 ];
  }

  /**
   * Method to unpack specified resource bundle.
   * @param name the resource bundle name.
   * @return content of resource bundle.
   */
  public final Object[] loadBundle( final String name )
  {
    Object[] res = null;
    DataInputStream dis = null;
    try{
      dis = new DataInputStream( getClass().getResourceAsStream( "/" + name ) );
      int count = dis.readByte();
      res = new Object[ count ];
      for( int i = 0, size, type, j; i < count; i++ ){
        size = dis.readInt();
        type = dis.readByte();
        switch( type ){
          case 0:
            // utf strings
            String[]tmpStr = new String[ size ];
            res[ i ] = tmpStr;
            for( j = 0; j < size; j++ ){
              tmpStr[ j ] = dis.readUTF();
            }
            break;

          default:
            byte[]tmp = new byte[ size ];
            res[ i ] = tmp;
            for( j = 0; j < size; j++ )
              tmp[ j ] = dis.readByte();
            break;
        }
      }

    } catch( Exception ex ){
      throw new IllegalStateException( ex.toString() );
    } finally{
      try{
        dis.close();
      } catch( Exception ex ){}
    }
    dis = null;
    return res;
  }
}