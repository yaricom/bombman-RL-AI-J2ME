package ng.mobile.game.util;

/**
 * Handles loging. Static field <code>enableAll</code> controls whether loging
 * enabled or disabled in application at all. In all places where loging should
 * occur you should check whether this flag is set and only then invoke
 * <code>log</code> method of this class. In such way we will consume conditional
 * compilation, i.e. if this flag unset than compiller simply removes all such
 * blocks from source code. After all such blocks removal there will be no more
 * references to this class/method from any place in application and it will
 * be removed by obfuscator during further processing.
 * <p>Simple usage:</p>
 * <pre>
 * if( Log.enabled ){
 *   Log.log( "Key", "Message" );
 * }
 * </pre>
 * <p>Advanced usage:</p>
 * It is possible to have fine grained control over loging, namely you can
 * separate loging in accordance with your requirements for particullar class
 * by defining additional static conditions, which depends on global
 * <code>Log.enabled</code> flag. Than you can use such a custom conditions
 * in your code to control logging facilities for particullar class.
 * <pre>
 * // Definition of custom logging level
 * private static final boolean customLog = Log.enabled & true;
 *
 * // somwhere in code. Note that this block will be included into compilled
 * // code only if global <code>Log.enabled</code> flag set to <code>true</code>.
 * if( customLog ){
 *   Log.log( "Key", "Message" );
 * }
 * </pre>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public class Log
{
  /** Flag to indicate whether loging globaly enabled */
  public static final boolean enabled = false;

  /**
   * Log specified message to the standard output.
   * @param key the message key.
   * @param mess the message to process.
   */
  public static final void log( String key, String mess )
  {
    System.out.println( key + " : " + mess );
  }
}