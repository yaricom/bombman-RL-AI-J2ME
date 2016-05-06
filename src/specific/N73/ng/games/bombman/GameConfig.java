/**
 * $Id: GameConfig.java 1015 2006-09-12 12:29:33Z yaric $
 */
package ng.games.bombman;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Nokia N73 (240x320) specific.
 * Game constants holder.
 *
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0
 */
public interface GameConfig
{
  /** Screen width */
  public static final int screenWidth = 240;
  /** Screen height */
  public static final int screenHeight = 320;

  /** Game speed factor */
  public static final int speedFactor = 50;
  /** Text scrolling speed */
  public static final int scrollingDelay = speedFactor * 2;
  /** Levels count */
  public static final int levels = 10;

  /** Game background color */
  public static final int gameBackgroundColor = 0x8833AA;
  /** Screen bounds color */
  public static final int screenBoundsColor = 0;

  /** The X coordinate of top left corner for progress bar drawing */
  public static final int loadProgressBarX = 20;
  /** The Y coordinate of top left corner for progress bar drawing */
  public static final int loadProgressBarY = 300;
  /** Load progress bar width */
  public static final int loadProgressBarW = 200;
  /** Load progress bar height */
  public static final int loadProgressBarH = 5;
  /** Progress bar color */
  public static final int progressBarColor = 0xFFFFFF;

  /** Vertical offset for 'press any key' caption */
  public static final int pressAnyKeyOffset = 305;
  /** Fade in background color */
  public static final int pressAnyKeyFadeInColor = 0xFFDD00;
  /** Fade out background color */
  public static final int pressAnyKeyFadeOutColor = 0xFF9900;

  /** Loading background color */
  public static final int loadingBackgroundColor = gameBackgroundColor;

  /** Tile height */
  public static final int tileHeight = 24;
  /** Tile width */
  public static final int tileWidth = 24;
  /** Flame segments count */
  public static final int flameSegments = 4;
  /** Flame vertical segments start index */
  public static final int flameVertical = 0;
  /** Flame horizontal segments start index */
  public static final int flameHorizontal = 1 * flameSegments;
  /** Flame up segments start index */
  public static final int flameUp = 2 * flameSegments;
  /** Flame left segments start index */
  public static final int flameLeft = 3 * flameSegments;
  /** Flame down segments start index */
  public static final int flameDown = 4 * flameSegments;
  /** Flame right segments start index */
  public static final int flameRight = 5 * flameSegments;
  /** Flame center segments start index */
  public static final int flameCenter = 6 * flameSegments;

  /** Scroll mark width */
  public static final int scrollMarkWidth = 7;
  /** Scroll mark height */
  public static final int scrollMarkHeight = 7;
  /** Scroll bar color */
  public static final int scrollBarColor = 0xBB0000;
  /** Scroll pointer height */
  public static final int scrollPointerHeight = scrollMarkHeight;

  /** Number of supported languages */
  public static final int maxSupportedLangCount = 6;
  /** Scroll texts aligment */
  public static final int scrollTextAligment = Graphics.HCENTER;
  /** Top scores count */
  public static final int topScoresCount = 12;

  //
  // Main menu configurations
  //
  /** Menu title top offset */
  public static final int menuTitleTopY = 4;
  /** Menu title left offset */
  public static final int menuTitleLeftX = 42;

  /** Main menu first option left X coordinate */
  public static final int mainMenuOptionX = 66;
  /** Main menu first option top Y coordinate */
  public static final int mainMenuOptionY = 72;
  /** Main menu highlighted option height */
  public static final int mainMenuOptionHeight = 26;
  /** Main menu interoption distance */
  public static final int mainMenuInterval = 5;

  /** Number of menu options to draw simultaneously */
  public static final int mainMenuOptionDrawCount = 6;

  /** Main menu decor cross left X */
  public static final int mainMenuCrossX = 11;
  /** Main menu decor cross top Y */
  public static final int mainMenuCrossY = 40;
  /** Main menu decor cross index (index of flame segment) */
  public static final int mainMenuCrossIndex = 1;
  /** Horizontal segments count */
  public static final int mainMenuFlameHorizCount = 8;
  /** Vertical segments count */
  public static final int mainMenuFlameVertCount = 9;

  /** Main menu pointer left X coordinate */
  public static final int mainMenuPointerX = 38;
  /** ain menu pointer area height */
  public static final int mainMenuPointerAreaHeight = mainMenuOptionHeight * 6 +
      mainMenuInterval * 5;

  /** Main menu area width */
  public static final int mainMenuAreaWidth = 176;
  /** Main menu area height */
  public static final int mainMenuAreaHeight = 202;
  /** Main menu area left X coordinate */
  public static final int mainMenuAreaX = 40;
  /** Main menu area top Y coordinate */
  public static final int mainMenuAreaY = 70;
  /** Main menu background color */
  public static final int mainMenuBackground = 0xFF9900;
  /** Main menu foreground color */
  public static final int mainMenuForeground = 0xBB0000;
  /** Text padding */
  public static final int mainMenuTextPadding = 2;
  /** Main menu text area (area available for text drawing) left X coordinate */
  public static final int mainMenuTextAreaX = mainMenuAreaX + mainMenuTextPadding;
  /** Main menu text area (area available for text drawing) top Y coordinate */
  public static final int mainMenuTextAreaY = mainMenuAreaY + 6;
  /** Main menu active text area (area available for text drawing) width */
  public static final int mainMenuTextAreaWidth =
      mainMenuAreaWidth - mainMenuTextPadding - mainMenuTextPadding -
      scrollMarkWidth - 2;
  /** Number of text lines to display */
  public static final int mainMenuTextLinesCount = 12;

  /** Scroll bar height in main menu area */
  public static final int mainMenuScrollBarHeight = mainMenuAreaHeight - 8;
  /** Top Y coordinate for menu scroll bar drawing */
  public static final int mainMenuScrollBarY = mainMenuAreaY + 4;
  /** Center X coordinate for scroll marks drawing in main menu area */
  public static final int mainMenuScrollMarkX = mainMenuAreaX + mainMenuAreaWidth - 7;


  /** Settings menu option left X coordinate */
  public static final int settMenuOptionX = mainMenuAreaX + 3;
  /** Settings menu option start Y coordinate */
  public static final int settMenuOptionY = mainMenuAreaY + 4;
  /** Settings menu option width */
  public static final int settMenuOptionWidth = mainMenuAreaWidth - 6;
  /** Settings menu option height */
  public static final int settMenuOptionHeight = 23;
  /** Settings menu interoption distance */
  public static final int settMenuInterval = 3;
  /** Setting menu icon left coordinate */
  public static final int settMenuIconX = settMenuOptionX + 3;
  /** Setting menu icon top offset */
  public static final int settMenuIconYOffset = 5;
  /** Settings menu checkbox right X coordinate */
  public static final int settMenuCheckboxX = settMenuOptionX +
      settMenuOptionWidth - 6;
  /** Settings menu checkbox top offset */
  public static final int settMenuCheckboxYOffset = settMenuIconYOffset;
  /** Settings menu text left coordinate */
  public static final int settMenuTextX = settMenuOptionX + 20;
  /** Settings menu text top offset */
  public static final int settMenuTextYOffset = 4;
  /** Settings menu pointer background color */
  public static final int settMenuPointerBackground = 0xFFD800;
  /** Settings menu pointer foreground color */
  public static final int settMenuPointerForeground = mainMenuForeground;
  /** Flag icon right X coordinate */
  public static final int settMenuFlagIconX= settMenuOptionX +
                                     settMenuOptionWidth - 2;
  /** Flag icon width */
  public static final int settMenuFlagIconWidth = 20;
  /** Flag icon height */
  public static final int settMenuFlagIconHeight = 16;


  //
  // In-Game menu configuration
  //
  /** Title left X coordinate */
  public static final int gameMenuTitleX = 33;
  /** Title top Y coordinate */
  public static final int gameMenuTitleY = 4;
  /** Game menu area left X coordinate */
  public static final int gameMenuAreaX = 12;
  /** Game menu area top Y coordinate */
  public static final int gameMenuAreaY = 45;
  /** Game menu area width */
  public static final int gameMenuAreaWidth = 216;
  /** Game menu area height */
  public static final int gameMenuAreaHeight = 230;
  /** Text padding */
  public static final int gameMenuTextPadding = 5;
  /** Game menu text area left X coordinate */
  public static final int gameMenuTextAreaX = gameMenuAreaX + gameMenuTextPadding;
  /** Game menu text area top Y coordinate */
  public static final int gameMenuTextAreaY = gameMenuAreaY + gameMenuTextPadding;
  /** Game menu text area width */
  public static final int gameMenuTextAreaWidth = gameMenuAreaWidth -
      gameMenuTextPadding - gameMenuTextPadding - scrollMarkWidth - 2;
  /** Number of text lines on game menu screens */
  public static final int gameMenuTextLines = mainMenuTextLinesCount + 1;
  /** Load bar left X coordinate */
  public static final int gameMenuLoadBarX = gameMenuAreaX + 18;
  /** Load bar width */
  public static final int gameMenuLoadBarWidth = gameMenuAreaWidth - 36;
  /** Load bar width */
  public static final int gameMenuLoadBarHeight = 5;
  /** Load bar vertical offset */
  public static final int gameMenuLoadBarYOffset = 5;
  /** Load bar color */
  public static final int gameMenuLoadBarColor = 0;

  /** Scroll bar height in game menu area */
  public static final int gameMenuScrollBarHeight = gameMenuAreaHeight - 8;
  /** Top Y coordinate for game menu scroll bar drawing */
  public static final int gameMenuScrollBarY = gameMenuAreaY + 4;
  /** Center X coordinate for scroll marks drawing in game menu area */
  public static final int gameMenuScrollMarkX = gameMenuAreaX + gameMenuAreaWidth - 7;


  /** The height of level box */
  public static final int gameMenuLevelBoxHeight = 22;
  /** Top Y coordinate for level text box drawing */
  public static final int gameMenuLevelBoxY = gameMenuAreaY +
                                                ( gameMenuAreaHeight -
                                                gameMenuLevelBoxHeight ) / 2;
  /** Text vertical offset for level caption */
  public static final int gameMenuLevelBoxTextYOffset = 5;
  /** Text horizontal offset for level caption */
  public static final int gameMenuLevelBoxTextXOffset = 4;


  //
  // Game screen configuration
  //
  /** Bottom area height */
  public static final int gameScreenIndicatorsAreaHeight = 32;
  /** Background color for indicators area */
  public static final int gameScreenIndicatorsBackground = 0xFF9900;
  /** Foreground color for indicators area */
  public static final int gameScreenIndicatorsForeground = 0xBB0000;

  /** Active area for game board drawing */
  public static final int gameScreenActiveHeight = screenHeight -
      gameScreenIndicatorsAreaHeight;

  /** Width of number for score displaying */
  public static final int gameScreenScoreNumWidth = 10;
  /** Height of number for score displaying */
  public static final int gameScreenScoreNumHeight = 16;
  /** Total width of numbers image */
  public static final int gameScreenScoreNumWidthTotal =
      gameScreenScoreNumWidth * 11;
  /** Interval between score numbers */
  public static final int gameScreenScoreInterwal = 2;
  /** Left X coordinate for score drawing */
  public static final int gameScreenScoreX =
      ( screenWidth - gameScreenScoreNumWidth * 5 ) / 2;
  /** The score divider define minimal number of numbers to display, including padding zeroes */
  public static final int gameScreenScoreDivider = 100000;
  /** The width of lives mark */
  public static final int gameScreenLivesMarkWidth = 20;
  /** The width of bombs mark */
  public static final int gameScreenBombsMarkWidth = 16;
  /** The horizontal offset of indicators */
  public static final int gameScreenIndicatorHorizontalOffset = 4;
  /** The top Y coordinate for indicators drawing */
  public static final int gameScreenIndicatorY = gameScreenActiveHeight + 9;

  /** Enter name interwal between letters*/
  public static final int gameScreenEnterNameInterwal = 10;
  /** Enter name carret blink interval */
  public static final int gameScreenEnterNameBlinkDelay = 200;
  /** Enter name bottom Y coordinate */
  public static final int gameScreenEnterNameY = gameMenuAreaY +
                                                 gameMenuAreaHeight - 5;
  /** Enter name carret color */
  public static final int gameScreenEnterNameCarretColor = 0xBB0000;


  //
  // Game screen pause menu configuration
  //
  /** The width of pause menu area */
  public static final int gameScreenPauseMenuWidth = 186;
  /** The height of pause menu area */
  public static final int gameScreenPauseMenuHeight = 180;
  /** The left X coordinate of pause menu area */
  public static final int gameScreenPauseMenuX =
      ( screenWidth - gameScreenPauseMenuWidth ) / 2;
  /** The top Y coordinate of pause menu area */
  public static final int gameScreenPauseMenuY =
      ( gameScreenActiveHeight - gameScreenPauseMenuHeight ) / 2;
  /** The pause menu option Y start */
  public static final int gameScreenPauseMenuOptionY = gameScreenPauseMenuY + 30;
  /** The pause menu interoption distance */
  public static final int gameScreenPauseMenuInterval = 10;
  /** The pointer horizontal offset regarding option */
  public static final int gameScreenPauseMenuPointerXOffset = 5;
  /** The pointer vertical offset regarding option top */
  public static final int gameScreenPauseMenuPointerYOffset = -3;

  //
  // Game engine configuration
  //
  /** Normal bombman move offset */
  public static final int bombmanMoveOffset = tileWidth / 4;
  /** Accelerated bombman move offset */
  public static final int bombmanAcceleratedMoveOffset = tileWidth / 2;
  /** Bombman scceleration time in ms */
  public static final int bombmanAccelerationTime = 3000;//3 sec
  /** Bombman animation interval */
  public static final int bombmanAnimationInterval = 100;
  /** Number of animation frames for death */
  public static final int bombmanDeathAnimationFrames = 7;
  /** Number of animation frames for win animation */
  public static final int bombmanWinAnimationFrames = 11;
  /** Number of animation cycles for win animation */
  public static final int bombmanWinAnimationCycles = 3;
  /** Bombman win animation sprite width */
  public static final int bombmanWinAnimationWidth = tileWidth;
  /** Bombman win animation sprite height */
  public static final int bombmanWinAnimationHeight = 48;
  /** Maximal number of bombs in the bombman pocket */
  public static final int maxBombsInPocket = 3;
  /** Normal explosion range in cells */
  public static final int normalEplosionRange = 3;
  /** Extended explosion range in cells */
  public static final int extendedEplosionRange = normalEplosionRange * 2;
  /** Interval between bomb step changes in ms */
  public static final int bombStepChangeInterval = speedFactor * 2;

  /** Clock count down interval in ms */
  public static final int clockCountDownInterval = 100;
  /** Number of beep steps */
  public static final int clockBeepSteps = 10;
  /** Clock area left X coordinate */
  public static final int clockAreaX = 3;
  /** Clock area top Y coordinate */
  public static final int clockAreaY = 3;
  /** Clock area height */
  public static final int clockAreaHeight = 32;
  /** Clock area width */
  public static final int clockAreaWidth = 23;

  /** Board panning speed */
  public static final int boardPanningSpeed = 2;

  /** Level fading animation segments count */
  public static final int levelFadingSegments = 16;
  /** Level fading animation segment height */
  public static final int levelFadingSegmentHeight = screenHeight / levelFadingSegments;
  /** Level fading animation step */
  public static final int levelFadingStep = 2;

  /** Final animation step */
  public static final int finalAnimationStep = 4;
  /** Final caption display delay in ms */
  public static final int finalCaptionDelay = 1500;

  /** AI look range */
  public static final int aiLookRange = normalEplosionRange;

  //
  // Fonts definitions
  //
  public static final int FONT_TEXT_STYLE = Font.STYLE_BOLD;
  public static final int FONT_TEXT_SIZE = Font.SIZE_SMALL;
  public static final int FONT_TEXT_HEIGHT = 16;

  public static final int FONT_MENU_STYLE = Font.STYLE_BOLD;
  public static final int FONT_MENU_SIZE = Font.SIZE_MEDIUM;
  public static final int FONT_MENU_HEIGHT = 19;

  public static final boolean fullScreen = true;
  //
  // Full screen specific
  //
  /** Command height */
  public static final int commandHeight = 22;
  /** Bottom Y coordinate for commands drawing */
  public static final int commandY = 292;
  /** Left X coordinate for positive command drawing */
  public static final int commandPositiveX = 8;
  /** Right X coordinate for negative command drawing */
  public static final int commandNegativeX = screenWidth - 8;
}
