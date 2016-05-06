/**
 * $Id: TextManager.java 223 2005-07-14 15:42:03Z yaric $
 */
package ng.games.bombman;

import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ng.mobile.game.util.Log;

/**
 * System font only specific.
 * Text manager to draw all captions using custom font bitmaps and to operate with
 * strings.
 *
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 2.0
 */
public class TextManager
{
  /** Space character definition */
  private final static char SPACE = ' ';

  /** Constant to define text entries font */
  public static final byte FONT_TEXT = 1;
  /** Constant to define menu entries font */
  public static final byte FONT_MENU = (byte)( 1 << 1 );
  /** Constant to define white font */
  public static final byte FONT_TYPE_WHITE = (byte)( 1 << 2 );
  /** Constant to define blck font */
  public static final byte FONT_TYPE_BLACK = (byte)( 1 << 3 );

  /** Height of the characters */
  public int charHeight;

  /** Resource factory instance */
  private final BombmanDAO resourceFactory;
  /** Fonts in use */
  private Font[] fonts;
  /** Current font */
  private Font currentFont;
  /** Current font type */
  private int font;

  //
  // Logging definitions
  //
  private static final boolean logSetFont = Log.enabled & true;

  /**
   * Constructs new instance.
   * @param factory the resource factory.
   */
  public TextManager( BombmanDAO factory )
  {
    this.resourceFactory = factory;
    this.fonts = new Font[ 2 ];
    // text font
    this.fonts[ 0 ] = Font.getFont( Font.FACE_SYSTEM,
                              GameConfig.FONT_TEXT_STYLE, GameConfig.FONT_TEXT_SIZE );
    // menu font
    this.fonts[ 1 ] = Font.getFont( Font.FACE_PROPORTIONAL,
                              GameConfig.FONT_MENU_STYLE, GameConfig.FONT_MENU_SIZE );

    this.setFont( FONT_TEXT | FONT_TYPE_WHITE );
  }

  /**
   * Method to switch between fonts.
   * @param font the font definition.
   */
  public final void setFont( int font )
  {
    if( this.font == font )
      return;// no need to process twice

    this.font = font;
    if( ( font & FONT_TEXT ) != 0 ){
      this.currentFont = this.fonts[ 0 ];
      if( GameConfig.FONT_TEXT_HEIGHT == -1 )
        this.charHeight = this.fonts[ 0 ].getHeight();
      else
        this.charHeight = GameConfig.FONT_TEXT_HEIGHT;
    }
    else if( ( font & FONT_MENU ) != 0 ){
      this.currentFont = this.fonts[ 1 ];
      if( GameConfig.FONT_MENU_HEIGHT == -1 )
        this.charHeight = this.fonts[ 1 ].getHeight();
      else
        this.charHeight = GameConfig.FONT_MENU_HEIGHT;
    }

    if( logSetFont ){
      Log.log( "SET FONT", "Char height: " + charHeight);
    }
  }

  /**
   * Sets appropriate font into provided graphics context in accordance with current
   * font type.
   * @param g the graphics context.
   */
  private final void setFontIntoGraphics( Graphics g )
  {
    if( ( this.font & FONT_TYPE_WHITE ) != 0 ){
      g.setColor( 0xFFFFFF );
    }
    else if( ( this.font & FONT_TYPE_BLACK ) != 0 ){
      g.setColor( 0 );
    }
    g.setFont( this.currentFont );
  }


  /**
   * Converts text string to image
   * @param text Text string
   * @param backgroundColor Background color of new image
   * @param width the width of image if -1 than string width will be used.
   * @param height the height of image if -1 than font height will be used.
   * @param x the horizontal offset for text drawing.
   * @return Text converted to image with specified background and location.
   */
  public Image textToImage( String text, int backgroundColor, int width,
      int height, int x )
  {
    int y = 0;
    if( width == -1 )
      width = this.currentFont.stringWidth( text );
    if( height == -1 )
      height = charHeight;
    else
      y = ( height - charHeight ) / 2;

    Image result = Image.createImage( width, height );
    Graphics g = result.getGraphics();
    g.setColor( backgroundColor );
    g.fillRect( 0, 0, width, height );
    char ch;
    int charsCount = text.length();
    this.setFontIntoGraphics( g );
    g.drawString( text, x, y, Graphics.TOP | Graphics.LEFT );
    return result;
  }

  /**
   * Draw text on graphics
   * @param text Text to draw
   * @param x the left x position
   * @param y the top y position
   * @param keepClipRect If true, clip rect is resumed, otherwise it
   * remains modified by method call
   * @param g Graphics to draw on
   */
  public void drawText( String text, int x, int y,
      boolean keepClipRect, Graphics g )
  {
    // set font
    this.setFontIntoGraphics( g );
    g.drawString( text, x, y, Graphics.TOP | Graphics.LEFT );
  }


  /**
   * Returns width of specified string.
   * @param s the string to check.
   * @return string width in current font.
   */
  public int stringWidth( String s )
  {
    return this.currentFont.stringWidth( s );
  }

  /**
   * Breaks a string into lines no longer than the number of pixels available.
   * Line breaks are placed at word boundaries; if a string contains no spaces
   * and is wider than the available pixels the break will occur within the string.
   * @param text The text to break into lines
   * @param availablePixels The number of pixels available width-wise.
   * @param font the font used to draw.
   * @return Array of strings, each string is a line.
   */
  public String[] breakIntoLines( String text, int availablePixels, int font )
  {
    this.setFont( font );
    Vector lines = new Vector();
    StringBuffer buffer = new StringBuffer();
    int length = 0, lastWord = 0, index = 0, size = text.length();
    char current = 0;
    while( index <= size ){
      length = currentFont.stringWidth( buffer.toString() );
      if( length >= availablePixels ){
        if( lastWord > 0 ){
          // truncate sentence
          lines.addElement( buffer.toString().
              substring( 0, buffer.length() - index + lastWord ).
              trim() );
          index = lastWord + 1;// just to skip empty space
        }
        else{
          // truncate word
          index--;
          lines.addElement( buffer.toString().
              substring( 0, buffer.length() - 1 ).trim() );
        }
        // clear buffer
        buffer.delete( 0, buffer.length() );
        lastWord = 0;
      }
      else if( index < size ){
        // append
        current = text.charAt( index );
        if( current == '\n' ){
          // new line starting
          lines.addElement( buffer.toString().trim() );
          // clear buffer
          buffer.delete( 0, buffer.length() );
          lastWord = 0;
        }
        else{
          // append character
          buffer.append( current );
          if( current == SPACE ) // store word position for subsequent truncation
            lastWord = index;
        }
        index ++;
      }
      else
        index ++;// just to take into account last letter and properly exit while cycle

    } // while

    // add last line if appropriate
    if( buffer.length() > 0 )
      lines.addElement( buffer.toString().trim() );

    String [] returnValue = null;
    if( lines.size() > 0 ){
      // make an array from the vector of lines
      returnValue = new String[ lines.size() ];
      lines.copyInto( returnValue );
    }
    return returnValue;
  }

  /**
   * Method to insert dots to fit screen area width.
   * @param start the first part of the string.
   * @param end the ending part of the string.
   * @param availablePixels avaliable pixels.
   * @param font The font used for text rendering.
   * @param charVal the char to insert.
   * @return String with inserted dots between first and second parts.
   */
  public final String insertChars( String start, String end,
      int availablePixels, int font, char charVal )
  {
    this.setFont( font );
    int endWidth = this.stringWidth( end );
    StringBuffer buf = new StringBuffer( start );
    int length;
    while( ( length = this.stringWidth( buf.toString() ) + endWidth ) < availablePixels )
      buf.append( charVal );

    if( length > availablePixels ){
      buf.deleteCharAt( buf.length() - 1 );
    }

    buf.append( end );
    return buf.toString();
  }
}