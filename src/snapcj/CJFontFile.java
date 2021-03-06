package snapcj;
import snap.gfx.*;

/**
 * A custom class.
 */
public class CJFontFile extends FontFile {
    
    // The name
    String  _name = "Arial";
    
    // The Family name
    String  _fname = "Arial";
    
    // The advance array
    double  _adv[];
    
    // The char advance for Arial, from 32 - 255
    static final double _advA[]  = { 0.278, 0.278, 0.355, 0.556, 0.556, 0.889, 0.667, 0.191, 0.333, 0.333, 0.389, 0.584,
        0.278, 0.333, 0.278, 0.278, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.278, 0.278,
        0.584, 0.584, 0.584, 0.556, 1.015, 0.667, 0.667, 0.722, 0.722, 0.667, 0.611, 0.778, 0.722, 0.278, 0.5, 0.667,
        0.556, 0.833, 0.722, 0.778, 0.667, 0.778, 0.722, 0.667, 0.611, 0.722, 0.667, 0.944, 0.667, 0.667, 0.611, 0.278,
        0.278, 0.278, 0.469, 0.556, 0.333, 0.556, 0.556, 0.5, 0.556, 0.556, 0.278, 0.556, 0.556, 0.222, 0.222, 0.5,
        0.222, 0.833, 0.556, 0.556, 0.556, 0.556, 0.333, 0.5, 0.278, 0.556, 0.5, 0.722, 0.5, 0.5, 0.5, 0.334, 0.26,
        0.334, 0.584, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 0.278, 0.333, 0.556, 0.556, 0.556, 0.556, 0.26, 0.556, 0.333, 0.737, 0.37, 0.556, 0.584, 0.333, 0.737,
        0.552, 0.4, 0.549, 0.333, 0.333, 0.333, 0.576, 0.537, 0.333, 0.333, 0.333, 0.365, 0.556, 0.834, 0.834, 0.834,
        0.611, 0.667, 0.667, 0.667, 0.667, 0.667, 0.667, 1, 0.722, 0.667, 0.667, 0.667, 0.667, 0.278, 0.278, 0.278,
        0.278, 0.722, 0.722, 0.778, 0.778, 0.778, 0.778, 0.778, 0.584, 0.778, 0.722, 0.722, 0.722, 0.722, 0.667, 0.667,
        0.611, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.889, 0.5, 0.556, 0.556, 0.556, 0.556, 0.278, 0.278, 0.278,
        0.278, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.549, 0.611, 0.556, 0.556,0.556,0.556,0.5,0.556,0.5 };
        
    // The char advance for Arial Bold, from 32 - 255
    static final double _advAB[]  = { 0.278, 0.333, 0.474, 0.556, 0.556, 0.889, 0.722, 0.238, 0.333, 0.333, 0.389,0.584,
        0.278, 0.333, 0.278, 0.278, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.333, 0.333,
        0.584, 0.584, 0.584, 0.611, 0.975, 0.722, 0.722, 0.722, 0.722, 0.667, 0.611, 0.778, 0.722, 0.278, 0.556, 0.722,
        0.611, 0.833, 0.722, 0.778, 0.667, 0.778, 0.722, 0.667, 0.611, 0.722, 0.667, 0.944, 0.667, 0.667, 0.611, 0.333,
        0.278, 0.333, 0.584, 0.556, 0.333, 0.556, 0.611, 0.556, 0.611, 0.556, 0.333, 0.611, 0.611, 0.278, 0.278, 0.556,
        0.278, 0.889, 0.611, 0.611, 0.611, 0.611, 0.389, 0.556, 0.333, 0.611, 0.556, 0.778, 0.556, 0.556, 0.5, 0.389,
        0.28, 0.389, 0.584, 0, 0.556, 0.286, 0.536, 0.5, 0.193, 0.397, 0.183, 0.542, 0.548, 0.675, 0.563, 0.5, 0.998,
        0.195, 0.215, 0.214, 0.218, 0.216, 0.2, 0.42, 0.42, 0, 0.531, 0.994, 0.338, 0.931, 0.364, 0.25, 0.556, 0, 0,
        0.699, 0.278, 0.333, 0.556, 0.556, 0.556, 0.556, 0.28, 0.556, 0.333, 0.737, 0.37, 0.556, 0.584, 0.333, 0.737,
        0.552, 0.4, 0.549, 0.333, 0.333, 0.333, 0.576, 0.556, 0.333, 0.333, 0.333, 0.365, 0.556, 0.834, 0.834, 0.834,
        0.611, 0.722, 0.722, 0.722, 0.722, 0.722, 0.722, 1, 0.722, 0.667, 0.667, 0.667, 0.667, 0.278, 0.278, 0.278,
        0.278, 0.722, 0.722, 0.778, 0.778, 0.778, 0.778, 0.778, 0.584, 0.778, 0.722, 0.722, 0.722, 0.722, 0.667, 0.667,
        0.611, 0.556, 0.556, 0.556, 0.556, 0.556, 0.556, 0.889, 0.556, 0.556, 0.556, 0.556, 0.556, 0.278, 0.278, 0.278,
        0.278, 0.611, 0.611, 0.611, 0.611, 0.611, 0.611, 0.611, 0.549, 0.611, 0.611, 0.611, 0.611, 0.611, 0.556, 0.611,
        0.556 };
 
/**
 * Creates a new CJFontFile for given name.
 */
public CJFontFile(String aName)
{
    _name = aName;
    _fname = _name.replace("Bold","").replace("Italic","").trim();
    _adv = _name.contains("Bold")? _advAB : _advA;
}

/**
 * Returns the name of this font.
 */
public String getName()  { return _name; }

/**
 * Returns the name of this font in English.
 */
public String getNameEnglish()  { return _name; }

/**
 * Returns the family name of this font.
 */
public String getFamily()  { return _fname; }

/**
 * Returns the PostScript name of this font.
 */
public String getPSName()  { return _name; }

/**
 * Returns the family name of this font in English.
 */
public String getFamilyEnglish()  { return _fname; }

/**
 * Returns the char advance for the given char.
 */
protected double charAdvanceImpl(char aChar)
{
    if(aChar<32) return 0;
    if(aChar>255) return charAdvance('X');
    return _adv[aChar-32];
}

/**
 * Returns the path for a given char (does the real work, but doesn't cache).
 */
protected Shape getCharPathImpl(char c)  { return new Rect(0,0,1000,1000); }

/**
 * Returns the path for given string with character spacing.
 */
public Shape getOutline(CharSequence aStr, double aSize, double aX, double aY, double aCharSpacing)
{
    return new Rect(0,0,1000,1000);
}

/**
 * Returns the max distance above the baseline that this font goes.
 */
public double getAscent()  { return .906; }

/**
 * Returns the max distance below the baseline that this font goes.
 */
public double getDescent()  { return .212; }

/**
 * Returns the default distance between lines for this font.
 */
public double getLeading()  { return .033; }

/**
 * Returns the max advance of characters in this font.
 */
public double getMaxAdvance()  { return 2; }

/** Returns if this font can display the given char. */
protected boolean canDisplayImpl(char aChar)  { return true; }

/** Override to return CheerpJ font. */
public Object getNative()  { return getName() + ' ' + 1000; }

/** Override to return CheerpJ font. */
public Object getNative(double aSize)  { return getName() + ' ' + aSize; }

}