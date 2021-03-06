package snapcj;
import cjdom.*;
import snap.gfx.*;
import snap.util.FilePathUtils;
import snap.web.*;

/**
 * A GFXEnv implementation for CheerpJ.
 */
public class CJEnv extends GFXEnv {

    // The shared CJEnv
    static CJEnv     _shared = new CJEnv();
    
/**
 * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
 */
public String[] getFontNames()  { return _fontNames; }
private static String _fontNames[] = { "Arial", "Arial Bold" };

/**
 * Returns a list of all system family names.
 */
public String[] getFamilyNames()  { return _famNames; }
private static String _famNames[] = { "Arial" };

/**
 * Returns a list of all font names for a given family name.
 */
public String[] getFontNames(String aFamilyName)  { return _fontNames; }

/**
 * Returns a font file for given name.
 */
public FontFile getFontFile(String aName)  { return new CJFontFile(aName); }

/**
 * Creates a new image from source.
 */
public Image getImage(Object aSource)  { return new CJImage(aSource); }

/**
 * Creates a new image for width, height and alpha.
 */
public Image getImage(int aWidth, int aHeight, boolean hasAlpha)  { return new CJImage(aWidth,aHeight,hasAlpha); }

/**
 * Returns a sound for given source.
 */
public SoundClip getSound(Object aSource)  { return new CJSoundClip(aSource); }

/**
 * Creates a sound for given source.
 */
public SoundClip createSound()  { return null; }

/**
 * Returns the screen resolution.
 */
public double getScreenResolution()  { return 72; }

/**
 * Tries to open the given file name with the platform reader.
 */
public void openFile(Object aSource)
{
    WebURL url = WebURL.getURL(aSource);
    String ext = FilePathUtils.getExtension(url.getPath()).toLowerCase();
    String type = ext.equals("pdf")? "application/pdf" : ext.equals("html")? "text/html" : null;
    byte bytes[] = url.getBytes();
    System.out.println("Got bytes: " + bytes.length + " and type " + type);
    Blob blob = new Blob(bytes, type);
    String src = URL.createObjectURL(blob);
    System.out.println("Open src: " + src);
    Window.current().open(src, "_blank", "menubar=no");
}

/**
 * Tries to open the given URL with the platform reader.
 */
public void openURL(Object aSource)
{
    WebURL url = WebURL.getURL(aSource);
    String urls = url!=null? url.getString() : null; if(urls!=null) urls = urls.replace("!", "");
    System.out.println("Open URL: " + urls);
    Window.current().open(urls, "_blank", "menubar=no");
}

/**
 * Plays a beep.
 */
public void beep()  { }

/**
 * Returns a shared instance.
 */
public static CJEnv get()  { return _shared; }

/**
 * Sets TVViewEnv as the ViewEnv.
 */
public static void set()  { GFXEnv.setEnv(get()); }

}