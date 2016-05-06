/**
 * $Id: ImageDataDecoder.java 209 2005-07-12 16:19:09Z yaric $
 */
package ng.mobile.game.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Image;

/**
 * <p>Utility class to operate with PNG image data encoded using custom format to reduce
 * total size of graphics data.</p>
 * <p><b>Data file format:</b></p>
 * <pre>
 * +- palette data length [ 2 bytes ] readShort()
 * +- palete data [ palette data length bytes ]
 * +- bit depth [ 1 byte ] readByte()
 * +- images count [ 1 byte ] readByte()
 *    for( i = 0; i < [ images count ]; i++ )
 *    +- [ image data ]
 *       +- transparency data length [ 1 byte ] readByte() (zero to specify that no transparency present)
 *       +- transparency data [ transparency data length ]
 *       +- image width [ 2 bytes ] readShort()
 *       +- image height [ 2 bytes ] readShort()
 *       +- image data length [ 2 bytes ] readShort()
 *       +- image data [ image data length ]
 * </pre>
 * <p>Company: NewGround</p>
 * @author Yaroslav Omelyanenko
 * @version 1.0 - initial creation
 * @version 1.1 - better pallette chunk construction (taking into account bit depth)
 * @version 1.2 - ability to extract exactly one image rather than array of images.
 */
public class ImageDataDecoder
{
  /** Table with CRC indexes */
  private int CRCTable[];
  /** Currently processed image data */
  private byte imgData[];
  /** Current position in the image data array during processing */
  private int imgDataPointer;

  /**
   * Clears internal storages.
   */
  public void release()
  {
    this.imgData = null;
    this.CRCTable = null;
  }

  /**
   * Calculates checksum for current chunk of data.
   * @param data array with chunk of data.
   * @param offset starting position.
   * @param length the length of chunck data to process including chunk data and
   * chunk identifier parts and excluding chunck data length part.
   * @return checksum for current chunk of data.
   */
  private int calculateCRC( byte data[], int offset, int length )
  {
    int k;
    int l;
    for( k = -1; length-- > 0; k = CRCTable[ ( k ^ l ) & 0xff ] ^ k >>> 8 )
      l = data[ offset++ ] & 0xff;

    return~k;
  }

  /**
   * Writes chunk data into image data array.
   * @param chunkType the chunk type.
   * @param chunkData the array with bytes of chunk data.
   * @param chunkDataLength the number of bytes to write from chunk data.
   */
  private void writeChunk( int chunkType, byte chunkData[], int chunkDataLength )
  {

    // write chunk data length
    writeInt4( imgData, imgDataPointer, chunkDataLength );
    imgDataPointer += 4;
    int k = imgDataPointer;
    // write chunk type
    writeInt4( imgData, imgDataPointer, chunkType );
    imgDataPointer += 4;
    // write chunk data
    if( chunkData != null ){
      System.arraycopy( chunkData, 0, imgData, imgDataPointer, chunkDataLength );
      imgDataPointer += chunkDataLength;
    }

    int crc = calculateCRC( imgData, k, chunkDataLength + 4 );
    // chunk crc
    writeInt4( imgData, imgDataPointer, crc );
    imgDataPointer += 4;
  }

  /**
   * Writes int value into provided array of bytes starting from specified offset
   * using big endian byte order (network byte order).
   * @param data the destination array.
   * @param offset the strating offset in destination array.
   * @param value the int value to write.
   */
  private void writeInt4( byte data[], int offset, int value )
  {
    data[ offset + 3 ] = ( byte ) ( value & 0xff );
    value >>>= 8;
    data[ offset + 2 ] = ( byte ) ( value & 0xff );
    value >>>= 8;
    data[ offset + 1 ] = ( byte ) ( value & 0xff );
    value >>>= 8;
    data[ offset ] = ( byte )value;
  }

  /**
   * <p>Extracks images from provided <code>InputStream</code>. Provided <code>InputStream</code>
   * will be closed upon method completion. Returns array of images or <code>null</code>
   * if no image was extrackted.</p>
   * <p>For data format srecification see class comments</p>.
   * @param is the <code>InputStream</code> to read images' data from.
   * @param palette the array of palette data to use instead of original stored in
   * <code>InputStream</code> or <code>null</code> if no substitution should be
   * performed. Method checks whether size of provided palette and original one is equal
   * and if so than swap them, otherwise original palette will be in use.
   * @return array of images or <code>null</code> if no image was extrackted.
   */
  public Image[] extractImages( InputStream is, byte[] palette )
  {
    return this.extractImages( is, palette, -1 );
  }


  /**
   * <p>Extracks images from provided <code>InputStream</code>. Provided
   * <code>InputStream</code> will be closed upon method completion. Returns
   * array with one image with specified index from array of encoded images or
   * <code>null</code> if no image was extrackted. If index equal to -1 than
   * array with all extracted images will be returned.</p>
   * <p>For data format srecification see class comments</p>.
   * @param index the index of image to return or -1 if all images should
   * be returned.
   * @param is the <code>InputStream</code> to read images' data from.
   * @param palette the array of palette data to use instead of original stored in
   * <code>InputStream</code> or <code>null</code> if no substitution should be
   * performed. Method checks whether size of provided palette and original one
   * is equal and if so than swap them, otherwise original palette will be in use.
   * @return array of images or <code>null</code> if no image was extrackted.
   */
  public Image[] extractImages( InputStream is, byte[] palette, int index )
  {
    DataInputStream dis = null;
    Image images[] = null;
    int i, j, k;
    try{
      // make crc table
      if( CRCTable == null ){
        CRCTable = new int[ 256 ];
        for( i = 0; i < 256; i++ ){
          j = i;
          for( k = 0; k < 8; k++ ){
            if( ( j & 1 ) == 1 )
              j = 0xedb88320 ^ j >>> 1;
            else
              j >>>= 1;
          }
          CRCTable[ i ] = j;
        }
      }
      dis = new DataInputStream( is );

      // palette data
      short paletteDataLength = dis.readShort();
      byte paletteData[] = new byte[ paletteDataLength ];
      for( i = 0; i < paletteDataLength; i++ )
        paletteData[ i ] = dis.readByte();

      // substitute original palette if appropriate
      if( palette != null ){
        paletteDataLength = ( short )palette.length;
        paletteData = palette;
      }

      // read images count
      int imagesCount = dis.readByte() & 0xFF;

      if( index == -1 )
        images = new Image[ imagesCount ];// all images should be returned
      else
        images = new Image[ 1 ];// only one image should be returned

      int transparencyDataLength = 0;
      byte[] transparencyData, imageHeaderData, imageData;
      int imageWidth, imageHeight, imageDataLength, totalImageSize;
      byte bitDepth;
      for( i = 0; i < imagesCount; i++ ){
        // read bit depth
        bitDepth = dis.readByte();
        // transparency data
        transparencyDataLength = dis.readByte() & 0xFF;
        transparencyData = new byte[ transparencyDataLength ];
        for( j = 0; j < transparencyDataLength; j++ )
          transparencyData[ j ] = dis.readByte();

        imageWidth = dis.readShort() & 0xffff;
        imageHeight = dis.readShort() & 0xffff;

        imageDataLength = dis.readShort() & 0xffff;

        totalImageSize = 33; // PNG signature + header chunk: signature(8) + data length specification(4) + chunk type(4) + header data( 13 ) + header CRC(4)
        totalImageSize += ( 8 + paletteDataLength + 4 ); // palette data length specification(4) + chunk type(4) + palette data + CRC(4)
        if( transparencyData.length > 0 )
          totalImageSize += ( 8 + transparencyDataLength + 4 ); // transparency data length specification(4) + chunk type(4) + transparency data + CRC(4)
        totalImageSize += ( 8 + imageDataLength + 4 ); // palette data length specification(4) + chunk type(4) + palette data length specification(4) + image data + CRC(4)
        totalImageSize += 12; // end mark data length specification(4) + chunk type(4) + end mark CRC(4)

        imgData = new byte[ totalImageSize ];

        imgDataPointer = 0;

        // Write file signature (PNG...)
        writeInt4( imgData, imgDataPointer, 0x89504E47 );
        imgDataPointer += 4;
        writeInt4( imgData, imgDataPointer, 0x0D0A1A0A );
        imgDataPointer += 4;

        // Image header
        //
        // Width:              4 bytes
        // Height:             4 bytes
        // Bit depth:          1 byte
        // Color type:         1 byte
        // Compression method: 1 byte
        // Filter method:      1 byte
        // Interlace method:   1 byte
        // ----------------------------
        //                     13 bytes
        imageHeaderData = new byte[ 13 ];
        writeInt4( imageHeaderData, 0, imageWidth );
        writeInt4( imageHeaderData, 4, imageHeight );
        imageHeaderData[ 8 ] = bitDepth; // number of bits per pixel
        imageHeaderData[ 9 ] = 3; // color type: each pixel is a palette index

        // IHDR
        writeChunk( 0x49484452 , imageHeaderData, 13 );

        // PLTE
        writeChunk( 0x504C5445 , paletteData, Math.min( paletteData.length,
            ( 2 << bitDepth ) * 3 ) );// skip unnecessary entries

        // tRNS
        if( transparencyData.length > 0 )
          writeChunk( 0x74524E53, transparencyData, transparencyData.length );

        imageData = new byte[ imageDataLength ];
        for( j = 0; j < imageDataLength; j++ )
          imageData[ j ] = dis.readByte();

          // IDAT
        writeChunk( 0x49444154, imageData, imageData.length );

        // IEND
        writeChunk( 0x49454E44, null, 0 );

        // create image
        if( index == -1 )
          images[ i ] = Image.createImage( imgData, 0, imgData.length );
        else if( index == i ){
          images[ 0 ] = Image.createImage( imgData, 0, imgData.length );
          break;// no need to process any further
        }
      }
    } catch( Exception ex ){
      ex.printStackTrace();
    } finally{
      try{ dis.close(); } catch( IOException ex ){ }
      this.imgData = null;
    }
    return images;
  }
}