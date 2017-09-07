package snapcj;
import cjdom.*;
import java.io.InputStream;
import snap.gfx.*;
import snap.web.WebURL;

/**
 * An Image implementation for CheerpJ.
 */
public class CJImage extends Image {
    
    // The source
    String                   _src;
    
    // The native object
    HTMLImageElement         _img;
    
    // The canvas object
    HTMLCanvasElement        _canvas;
    
    // The size
    int                      _pw = -1, _ph = -1;
    
/**
 * Creates a new CJImage from source.
 */
public CJImage(Object aSource)
{
    // Get Src URL string
    _src = getSourceURL(aSource);
    
    // Create image    
    _img = _img = (HTMLImageElement)HTMLDocument.current().createElement("img");
    _pw = _ph = 20;
    
    // Set src and wait till loaded
    setLoaded(false);
    _img.addEventListener("load", e -> didFinishLoad());
    _img.setSrc(_src);
}

/**
 * Returns a Source URL from source object.
 */
String getSourceURL(Object aSource)
{
    // Handle byte[] and InputStream
    if(aSource instanceof byte[] || aSource instanceof InputStream) {
        Blob blob = new Blob(aSource, null);
        return URL.createObjectURL(blob);
    }
    
    // Get URL
    WebURL url = WebURL.getURL(aSource);
    if(url==null)
        return null;
        
    // If URL can't be fetched by browser, load from bytes
    if(!isBrowsable(url))
        return getSourceURL(url.getBytes());
        
    // Return URL string
    return url.getString();
}

/**
 * Returns whether URL can be fetched by browser.
 */
boolean isBrowsable(WebURL aURL)
{
    String urls = aURL.getString();
    String scheme = aURL.getScheme();
    if(urls.contains("!")) return false;
    return scheme.equals("http") || scheme.equals("https") || scheme.equals("data") || scheme.equals("blob");
}

/** Called when image has finished load. */
synchronized void didFinishLoad()
{
    _pw = _img.getWidth(); _ph = _img.getHeight();  //_loaded = true; notifyAll();
    setLoaded(true);
}

/**
 * Creates a new TVImage for size.
 */
public CJImage(double aWidth, double aHeight, boolean hasAlpha)
{
    _pw = (int)aWidth; _ph = (int)aHeight;
    _canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
    _canvas.setSize(_pw, _ph);
}

/**
 * Returns the width of given image in pixels.
 */
public int getPixWidth()
{
    if(_pw>=0) return _pw;
    return _pw = _img.getWidth();
}

/**
 * Returns the height of given image in pixels.
 */
public int getPixHeight()
{
    if(_ph>=0) return _ph;
    return _ph = _img.getHeight();
}

/**
 * Returns whether image has alpha.
 */
public boolean hasAlpha()  { return false; }

/**
 * Returns number of components.
 */
public int getComponentCount()  { return hasAlpha()? 4 : 3; }

/**
 * Returns whether index color model.
 */
public boolean isIndexedColor()  { return false; }

/**
 * Returns an RGB integer for given x, y.
 */
public int getRGB(int aX, int aY)
{
    getPainter();
    //CanvasRenderingContext2D cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
    //ImageData idata = cntx.getImageData(aX, aY, 1, 1);
    //Uint8ClampedArray data = idata.getData();
    //int d1 = data.get(0), d2 = data.get(1), d3 = data.get(2), d4 = data.get(3);
    //return d4<<24 | d1<<16 | d2<<8 | d3;
    return 0;
}

/** Returns the ARGB array of this image. */
public int[] getArrayARGB()  { System.err.println("Image.getArrayARGB: Not implemented"); return null; }

/** Returns the ARGB array of this image. */
public byte[] getBytesRGBA()  { System.err.println("Image.getBytesRGBA: Not implemented"); return null; }

/** Returns the ARGB array of this image. */
public int getAlphaColorIndex()  { System.err.println("Image.getAlphaColorIndex: Not implemented"); return 0; }

/** Returns the ARGB array of this image. */
public byte[] getColorMap()  { System.err.println("Image.getColorMap: Not implemented"); return null; }

/** Returns the ARGB array of this image. */
public int getBitsPerSample()  { System.err.println("Image.getBitsPerSample: Not implemented"); return 0; }

/** Returns the ARGB array of this image. */
public int getSamplesPerPixel()  { System.err.println("Image.getSamplesPerPixel: Not implemented"); return 0; }

/** Returns the JPEG bytes for image. */
public byte[] getBytesJPEG()  { return null; }

/** Returns the PNG bytes for image. */
public byte[] getBytesPNG()  { return null; }

/**
 * Returns a painter to mark up image.
 */
public Painter getPainter()
{
    if(_img!=null) {
        _canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas").withAttr("width", String.valueOf(getPixWidth()))
            .withAttr("height", String.valueOf(getPixHeight()));
        Painter pntr = new CJPainter(_canvas);
        pntr.drawImage(this, 0, 0); _img = null;
        return pntr;
    }
    return new CJPainter(_canvas);
}

/**
 * Returns whether image data is premultiplied.
 */
public boolean isPremultiplied()  { return _pm; } boolean _pm;

/**
 * Sets whether image data is premultiplied.
 */
public void setPremultiplied(boolean aValue)  { _pm = aValue; }

/**
 * Returns the native object.
 */
public CanvasImageSource getNative()  { return _img!=null? _img : _canvas; }

}