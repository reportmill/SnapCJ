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
     * Sets the TeaVM environment.
     */
    public static void set()
    {
        if (SnapUtils.isWebVM) CJViewEnv.set();
    }
}