package snapcj;
import cjdom.*;
import snap.gfx.*;

/**
 * A snap Painter for rendering to a CheerpJ HTMLCanvasElement.
 */
public class CJPainter2 extends PainterDVR2 {

    /**
     * Constructor for given canvas.
     */
    public CJPainter2(HTMLCanvasElement aCnvs, int aScale)
    {
        super(new CJPainter(aCnvs, aScale));
    }

    /**
     * Override to have CJPainter paint stacks.
     */
    @Override
    public void flush()
    {
        CJPainter painter = (CJPainter) _pntr;
        for (int i = 0; i < _nativeStackSize; i++)
            _nativeStack[i] = toNative(_nativeStack[i]);
        painter.paintStacks(_instructionStack, _instructionStackSize, _intStack, _doubleStack, _stringStack, _nativeStack);
        clear();
    }

    /**
     * Converts objects in native stack to JavaScript friendly object.
     */
    private static Object toNative(Object anObj)
    {
        // Handle Paint: Convert to paint string
        if (anObj instanceof Paint)
            return CJ.get(((Paint) anObj).getColor());

        // Handle Font: Convert to font string
        if (anObj instanceof Font)
            return CJ.get((Font) anObj);

        // Handle image: Convert to Native.JS
        if (anObj instanceof Image) {
            CanvasImageSource imgSrc = (CanvasImageSource) ((Image) anObj).getNative();
            return ((HTMLElement) imgSrc).getJS();
        }

        return anObj;
    }
}