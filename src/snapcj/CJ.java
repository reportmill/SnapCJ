package snapcj;
import cjdom.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.GradientPaint;
import snap.util.SnapUtils;

/**
 * Utility methods for SnapKit+TeaVM.
 */
public class CJ {

    /**
     * Returns TVM color for snap color.
     */
    public static String get(Color aColor)
    {
        if (aColor == null) return null;
        int r = aColor.getRedInt(), g = aColor.getGreenInt(), b = aColor.getBlueInt(), a = aColor.getAlphaInt();
        StringBuilder sb = new StringBuilder(a == 255 ? "rgb(" : "rgba(");
        sb.append(r).append(',').append(g).append(',').append(b);
        if (a == 255) sb.append(')');
        else sb.append(',').append(a / 255d).append(')');
        return sb.toString();
    }

    /**
     * Returns TVM color for snap color.
     */
//    public static CanvasGradient get(GradientPaint aGP, CanvasRenderingContext2D aRC)
//    {
//        CanvasGradient cg = aRC.createLinearGradient(aGP.getStartX(), aGP.getStartY(), aGP.getEndX(), aGP.getEndY());
//        for (int i = 0, iMax = aGP.getStopCount(); i < iMax; i++)
//            cg.addColorStop(aGP.getStopOffset(i), get(aGP.getStopColor(i)));
//        return cg;
//    }

    /**
     * Returns TVM font for snap font.
     */
    public static String get(Font aFont)
    {
        String str = "";
        if (aFont.isBold()) str += "Bold ";
        if (aFont.isItalic()) str += "Italic ";
        str += ((int) aFont.getSize()) + "px ";
        str += aFont.getFamily();
        return str;
    }

    /**
     * Returns the offset.
     */
    public static Point getOffsetAll(HTMLElement anEmt)
    {
        // Update window location
        int top = 0;
        int left = 0;
        HTMLDocument doc = HTMLDocument.current();
        for (Node emt = anEmt; emt != null && emt.getJS() != doc.getJS(); emt = emt.getParentNode()) {
            top += ((HTMLElement) emt).getOffsetTop();
            left += ((HTMLElement) emt).getOffsetLeft();
        }

        // Return point
        return new Point(left, top);
    }

    /**
     * Viewport size.
     */
    public static Rect getViewportBounds()
    {
        double x = 0; // double x = getViewportX();
        double y = 0; // double y = getViewportY();
        double w = CJDom.getViewportWidth();
        double h = CJDom.getViewportHeight();
        return new Rect(x, y, w, h);
    }

    /**
     * Creates a JavaScript File from given bytes in Java.
     */
    public static File createFile(byte[] theBytes, String aName, String aType)
    {
        Int8Array bytesJS = new Int8Array(theBytes);
        File file = createFile(bytesJS, aName, aType);
        return file;
    }

    /**
     * Creates a File from given bytes in JS.
     */
    //@JSBody(params = {"theBytes", "aName", "aType"}, script = "return new File([theBytes], aName, aType? { type:aType } : null);")
    static native File createFile(Int8Array theBytes, String aName, String aType);

    /**
     * Creates a Blob from given bytes in Java.
     */
//    public static Blob createBlob(byte[] theBytes, String aType)
//    {
//        Int8Array bytesJS = getBytesJS(theBytes);
//        Blob blob = createBlob(bytesJS, aType);
//        return blob;
//    }

    /**
     * Creates a Blob from given bytes in JS.
     */
    //@JSBody(params = {"theBytes", "aType"}, script = "return new Blob([theBytes], aType? { type:aType } : null);")
    //static native Blob createBlob(Int8Array theBytes, String aType);

    /**
     * Creates a URL from given blob.
     */
    //@JSBody(params = {"theBlob"}, script = "return URL.createObjectURL(theBlob);")
    //static native String createURL(Blob theBlob);

    /**
     * Creates a URL from given blob.
     */
    //@JSBody(params = {"htmlElement", "aValue"}, script = "htmlElement.contentEditable = aValue; htmlElement.tabIndex = 0;")
    //static native String setContentEditable(HTMLElement htmlElement, boolean aValue);

    /**
     * Sets the TeaVM environment.
     */
    public static void set()
    {
        if (SnapUtils.isWebVM) CJViewEnv.set();
    }
}