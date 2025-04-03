package snapcj;
import cjdom.*;
import snap.gfx.*;

/**
 * A snap Painter for rendering to a CheerpJ HTMLCanvasElement.
 */
public class CJPainter2 extends PainterDVR2 {

    // The RenderContext2D
    protected CanvasRenderingContext2D _cntx;

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
        _cntx = painter._cntx;

        // Convert Native stack objects to JS (where applicable)
        for (int i = 0; i < _nativeStackSize; i++)
            _nativeStack[i] = toNative(_nativeStack[i]);

        // Paint stacks
        painter.paintStacks(_instructionStack, _instructionStackSize, _intStack, _doubleStack, _stringStack, _nativeStack);
        clear(); _cntx = null;
    }

    /**
     * Converts objects in native stack to JavaScript friendly object.
     */
    private Object toNative(Object anObj)
    {
        // Handle Color: Convert to color string
        if (anObj instanceof Color)
            return CJ.getColorJS((Color) anObj);

        // Handle texture, gradient: Convert to canvas versions
        if (anObj instanceof Paint) {
            if (anObj instanceof ImagePaint)
                return CJ.getTextureJS((ImagePaint) anObj, _cntx);
            if (anObj instanceof GradientPaint)
                return CJ.getGradientJS((GradientPaint) anObj, _cntx);
            return CJ.getColorJS(((Paint) anObj).getColor());
        }

        // Handle Font: Convert to font string
        if (anObj instanceof Font)
            return CJ.getFontJS((Font) anObj);

        // Handle image: Convert to Native.JS
        if (anObj instanceof Image) {
            CanvasImageSource imgSrc = (CanvasImageSource) ((Image) anObj).getNative();
            return ((HTMLElement) imgSrc).getJS();
        }

        return anObj;
    }
}