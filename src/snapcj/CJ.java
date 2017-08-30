package snapcj;
import cheerpj.*;
import snap.gfx.*;

/**
 * Utility methods.
 */
public class CJ {

/**
 * Returns HTML color for snap color.
 */
public static String get(Color aColor)
{
    if(aColor==null) return null;
    int r = aColor.getRedInt(), g = aColor.getGreenInt(), b = aColor.getBlueInt(), a = aColor.getAlphaInt();
    StringBuffer sb = new StringBuffer(a==255? "rgb(" : "rgba(");
    sb.append(r).append(',').append(g).append(',').append(b);
    if(a==255) sb.append(')'); else sb.append(',').append(a/255d).append(')');
    return sb.toString();
}

/**
 * Returns HTML color for snap color.
 */
public static CanvasGradient get(GradientPaint aGP, CanvasRenderingContext2D aRC)
{
    CanvasGradient cg = aRC.createLinearGradient(aGP.getStartX(), aGP.getStartY(), aGP.getEndX(), aGP.getEndY());
    for(int i=0,iMax=aGP.getStopCount();i<iMax;i++)
        cg.addColorStop(aGP.getStopOffset(i), get(aGP.getStopColor(i)));
    return cg;
}

/**
 * Returns HTML font for snap font.
 */
public static String get(Font aFont)
{
    String str = ""; if(aFont.isBold()) str += "Bold "; if(aFont.isItalic()) str += "Italic ";
    str += ((int)aFont.getSize()) + "px "; str += aFont.getFamily();
    return str;
}

/**
 * Sets the CheerpJ environment.
 */
public static void set()  { CJViewEnv.set(); }

}