package snapcj;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import cjdom.*;
import snap.gfx.*;
import snap.util.FileUtils;
import snap.util.Prefs;
import snap.web.*;

/**
 * A GFXEnv implementation for TeaVM.
 */
public class CJEnv extends GFXEnv {
    
    // The shared Env
    private static CJEnv  _shared;

    // Font names, Family names
    private static String[]  _fontNames = {
        "Arial", "Arial Bold", "Arial Italic", "Arial Bold Italic",
        "Times New Roman", "Times New Roman Bold", "Times New Roman Italic", "Times New Roman Bold Italic",
    };
    private static String[]  _famNames = { "Arial", "Times New Roman" };

    /**
     * Creates a CJEnv.
     */
    public CJEnv()
    {
        if (_env == null) {
            _env = _shared = this;
        }
    }

    /**
     * Returns resource for class and path.
     */
    public URL getResource(Class<?> aClass, String aPath)
    {
//        CJWebSite site = CJWebSite.get();
//        return site.getJavaURL(aClass, aPath);
        return aClass.getResource(aPath);
    }

    /**
     * Returns the root URL classes in Snap Jar as string.
     */
    public String getClassRoot()
    {
        return CJViewEnv.getScriptRoot();
    }

    /**
     * Returns a list of all system fontnames (excludes any that don't start with capital A-Z).
     */
    public String[] getFontNames()  { return _fontNames; }

    /**
     * Returns a list of all system family names.
     */
    public String[] getFamilyNames()  { return _famNames; }

    /**
     * Returns a list of all font names for a given family name.
     */
    public String[] getFontNames(String aFamilyName)
    {
        // Get system fonts and create new list for font family
        String[] fonts = getFontNames();
        List<String> familyNames = new ArrayList<>();

        // Iterate over fonts
        for(String name : fonts) {

            // If family name is equal to given family name, add font name
            if(name.contains(aFamilyName) && !familyNames.contains(name))
                familyNames.add(name);
        }

        // Get font names as array and sort
        String[] familyArray = familyNames.toArray(new String[0]);
        Arrays.sort(familyArray);

        // Return
        return familyArray;
    }

    /**
     * Returns a font file for given name.
     */
    public FontFile getFontFile(String aName)
    {
        return new CJFontFile(aName);
    }

    /**
     * Creates image from source.
     */
    public Image getImageForSource(Object aSource)
    {
        return new CJImage(aSource);
    }

    /**
     * Creates image for width, height and alpha and dpi scale (0 = screen dpi, 1 = 72 dpi, 2 = 144 dpi).
     */
    public Image getImageForSizeAndDpiScale(double aWidth, double aHeight, boolean hasAlpha, double dpiScale)
    {
        if (dpiScale <= 0)
            dpiScale = getScreenScale();
        return new CJImage(aWidth, aHeight, hasAlpha, dpiScale);
    }

    /**
     * Returns a sound for given source.
     */
    public SoundClip getSound(Object aSource)
    {
        return new CJSoundClip(aSource);
    }

    /**
     * Creates a sound for given source.
     */
    public SoundClip createSound()  { return null; }

    /**
     * Returns prefs.
     */
    @Override
    public Prefs getPrefs(String aName)
    {
        return new CJPrefs(aName);
    }

    /**
     * Returns the screen resolution.
     */
    public double getScreenResolution()  { return 72; }

    /**
     * Returns the screen scale. Usually 1, but could be 2 for HiDPI/Retina displays.
     */
    public double getScreenScale()  { return CJDom.getDevicePixelRatio(); }

    /**
     * Tries to open the given file name with the platform reader.
     */
    public void openFile(Object aSource)
    {
        // Get Java File for source
        if (aSource instanceof WebFile)
            aSource = ((WebFile) aSource).getJavaFile();
        if (aSource instanceof WebURL)
            aSource = ((WebURL) aSource).getJavaURL();
        java.io.File file = FileUtils.getFile(aSource);

        // Get file name, type, bytes
        String name = file.getName().toLowerCase();
        String type = name.endsWith("pdf") ? "application/pdf" : name.endsWith("html") ? "text/html" : null;
        byte[] bytes = FileUtils.getBytes(file);

        // Create file and URL string
        File fileJS = new File(name, type, bytes);
        String urls = fileJS.createURL();

        // Open
        Window.current().open(urls, "_blank");
    }

    /**
     * Tries to open the given URL with the platform reader.
     */
    public void openURL(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        String urls = url != null ? url.getString() : null;
        if (urls != null)
            urls = urls.replace("!", "");
        System.out.println("Open URL: " + urls);
        Window.current().open(urls, "_blank", "menubar=no");
    }

    /**
     * Plays a beep.
     */
    public void beep()  { }

    /**
     * This is really just here to help with TeaVM.
     */
    public Method getMethod(Class<?> aClass, String aName, Class<?>... theClasses) throws NoSuchMethodException
    {
        return aClass.getMethod(aName, theClasses);
    }

    /**
     * This is really just here to help with TeaVM.
     */
    public void exit(int aValue)  { }

    /**
     * Sets the Browser window.location.hash.
     */
    @Override
    public void setBrowserWindowLocationHash(String aString)
    {
        Window window = Window.current();
        window.setWindowLocationHash(aString);
    }

    /**
     * Executes a process.
     */
    @Override
    public Process execProcess(String[] args)
    {
        return new CJProcess(args);
    }

    /**
     * Returns a shared instance.
     */
    public static CJEnv get()
    {
        if (_shared != null) return _shared;
        return _shared = new CJEnv();
    }
}