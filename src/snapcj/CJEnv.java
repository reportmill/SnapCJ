package snapcj;
import java.io.*;
import java.util.*;
import java.net.*;
import cheerpj.*;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class CJEnv extends GFXEnv {

    // The shared CJEnv
    static CJEnv     _shared = new CJEnv();
    
    // Map of sites
    //Map <WebURL,WebSite> _sites = new HashMap();

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
public Image getImage(Object aSource)
{
    if(aSource instanceof byte[]) {
        System.err.println("TVEnv.getImage: Trying to load from bytes");
        return null;
    }
    
    WebURL url = getURL(aSource);
    if(url==null)
        return null;
        
    return new CJImage(url);
}

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
 * Returns a URL for given source.
 */
public WebURL getURL(Object anObj)
{
    // Handle null, WebURL, WebFile
    if(anObj==null || anObj instanceof WebURL) return (WebURL)anObj;
    if(anObj instanceof WebFile) return ((WebFile)anObj).getURL();
    
    // Handle String; 
    if(anObj instanceof String) { String str = (String)anObj;
    
        // If it's our silly "Jar:/com/rm" format, return class resource URL
        if(str.startsWith("Jar:/com/reportmill")) 
            return getURL(WebURL.class.getResource(str.substring(4)));
            
        // If string is Windows/Unix file path, make it a file URL
        if(str.indexOf('\\')>=0) { String strlc = str.toLowerCase();
            str = str.replace('\\', '/'); if(!str.startsWith("/") || !strlc.startsWith("file:")) str = '/' + str; }
        if(str.startsWith("/")) str = "file://" + str;
        
        // Get protocol for URL
        int ind = str.indexOf(':'); if(ind<0) throw new RuntimeException("Missing protocol in URL: " + str);
        String scheme = str.substring(0, ind).toLowerCase();
            
        // Get URL for string
        try { 
            if(scheme.equals("class") || scheme.equals("local") || scheme.equals("git"))
                anObj = new URL(null, str, new BogusURLStreamHandler());
            else anObj = new URL(str);
        }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    
    // Handle File: Convert to Canonical URL to normalize path
    if(anObj instanceof File) { File file = (File)anObj;
        try { anObj = file.getCanonicalFile().toURI().toURL(); } catch(Exception e) { } }
    
    // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that)
    if(anObj instanceof URL) { URL url = (URL)anObj;
        String urls = url.toExternalForm(); try { urls = URLDecoder.decode(urls, "UTF-8"); } catch(Exception e) { }
        if(url.getProtocol().equals("jar")) urls = urls.substring(4);
        else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
        return new WebURL(url, urls);
    }
    
    // Handle Class
    if(anObj instanceof Class) return getURL((Class)anObj, null);
    throw new RuntimeException("No URL found for: " + anObj);
}

/**
 * Returns a URL for given class and name/path string.
 */
public WebURL getURL(Class aClass, String aName)
{
    // Get absolute path to class/resource
    String path = '/' + aClass.getName().replace('.', '/') + ".class";
    if(aName!=null) {
        if(aName.startsWith("/")) path = aName;
        else { int sep = path.lastIndexOf('/'); path = path.substring(0, sep+1) + aName; }
    }
    
    // If class loader is DataClassLoader, have it return URL
    ClassLoader cldr = aClass.getClassLoader();
    if(cldr instanceof WebClassLoader)
        return ((WebClassLoader)cldr).getURL(path);
    
    // Get URL string for class and resource (decoded)
    URL url = aClass.getResource(path); if(url==null) return null;
    
    // Handle URL: Get string, decode and strip "jar:" prefix if found (we don't use that) and install path separator
    String urls = url.toExternalForm(); try { urls = URLDecoder.decode(urls, "UTF-8"); } catch(Exception e) { }
    if(url.getProtocol().equals("jar")) urls = urls.substring(4);
    else if(url.getProtocol().equals("wsjar")) urls = urls.substring(6);
    else urls = urls.replace(path, '!' + path);
    return new WebURL(url, urls);
}

/**
 * A URLStreamHandlerFactory.
 */
private static class BogusURLStreamHandler extends URLStreamHandler {
    protected URLConnection openConnection(URL u) throws IOException  { return null; }}

// A map of existing WebSites
Map <WebURL, WebSite>  _sites = Collections.synchronizedMap(new HashMap());

/**
 * Returns a site for given source URL.
 */
public synchronized WebSite getSite(WebURL aSiteURL)
{
    WebSite site = _sites.get(aSiteURL);
    if(site==null) _sites.put(aSiteURL, site = createSite(aSiteURL));
    return site;
}

/**
 * Creates a site for given URL.
 */
protected WebSite createSite(WebURL aSiteURL)
{
    WebURL parentSiteURL = aSiteURL.getSiteURL();
    String scheme = aSiteURL.getScheme(), path = aSiteURL.getPath(); if(path==null) path = "";
    String type = FilePathUtils.getExtension(path).toLowerCase();
    WebSite site = null;
    
    // If url has path, see if it's jar or zip
    if(type.equals("jar") || path.endsWith(".jar.pack.gz")) site = new JarFileSite();
    else if(type.equals("zip") || type.equals("gfar")) site = new ZipFileSite();
    else if(parentSiteURL!=null && parentSiteURL.getPath()!=null) site = new DirSite();
    else if(scheme.equals("file")) site = new FileSite();
    else if(scheme.equals("http") || scheme.equals("https")) site = new HTTPSite();
    else if(scheme.equals("ftp")) site = new FTPSite();
    else if(scheme.equals("class")) site = new ClassSite();
    else if(scheme.equals("local")) site = new LocalSite();
    if(site!=null) WebUtils.setSiteURL(site, aSiteURL);
    return site;
}

/**
 * Returns the screen resolution.
 */
public double getScreenResolution()  { return 72; }

/**
 * Tries to open the given file name with the platform reader.
 */
public void openFile(Object aSource)  { }

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
 * Sets this JVM to be headless.
 */
public void setHeadless()  { }

/**
 * Returns the platform.
 */
public SnapUtils.Platform getPlatform()  { return SnapUtils.Platform.UNKNOWN; }

/**
 * Returns a key value.
 */
public Object getKeyValue(Object anObj, String aKey)  { return null; }

/**
 * Sets a key value.
 */
public void setKeyValue(Object anObj, String aKey, Object aValue)  { }

/**
 * Returns a key chain value.
 */
public Object getKeyChainValue(Object anObj, String aKeyChain)  { return null; }

/**
 * Sets a key chain value.
 */
public void setKeyChainValue(Object anObj, String aKC, Object aValue)  { }

/**
 * Returns a key list value.
 */
public Object getKeyListValue(Object anObj, String aKey, int anIndex)  { return null; }

/**
 * Adds a key list value.
 */
public void setKeyListValue(Object anObj, String aKey, Object aValue, int anIndex)  { }

/**
 * Returns a shared instance.
 */
public static CJEnv get()  { return _shared; }

}