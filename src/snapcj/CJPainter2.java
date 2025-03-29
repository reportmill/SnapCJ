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

    @Override
    public void flush()
    {
        System.out.println("CJPainter2 flush, instruction count: " + _instructionStackSize);
        CJPainter painter = (CJPainter) _pntr;
        for (int i = 0; i < _nativeStackSize; i++)
            _nativeStack[i] = toNative(_nativeStack[i]);
        painter.paintStacks(_instructionStack, _instructionStackSize, _intStack, _doubleStack, _stringStack, _nativeStack);
        clear();
    }

    private static Object toNative(Object anObj)
    {
        if (anObj instanceof Paint)
            return CJ.get(((Paint) anObj).getColor());
        if (anObj instanceof Image) {
            CanvasImageSource imgSrc = (CanvasImageSource) ((Image) anObj).getNative();
            return ((HTMLElement) imgSrc).getJS();
        }
        if (anObj instanceof Font)
            return CJ.get((Font) anObj);
        return anObj;
    }
}