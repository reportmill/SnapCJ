package snapcj;
import cjdom.CanvasRenderingContext2D;
import cjdom.HTMLCanvasElement;
import cjdom.HTMLDocument;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.view.*;

/**
 * A class to manage RootView canvas.
 */
public class CJRootView {

    // The RootView
    private RootView  _rootView;
    
    // The HTMLCanvas
    protected HTMLCanvasElement _canvas;
    
    // The HTMLCanvas 'double-buffering' buffer - offscreen drawing to avoid flicker of async JNI
    protected HTMLCanvasElement _canvasBuffer;

    // The image dpi scale (1 = normal, 2 for retina/hidpi)
    private int  _scale = CJWindow.scale;
    
    // Painter
    private Painter  _pntr;
    
    /**
     * Sets the view.
     */
    public void setView(RootView aView)
    {
        // Set RootView
        _rootView = aView;

        // Create canvas and configure to totally fill window element (minus padding insets)
        _canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        _canvas.getStyle().setProperty("width", "100%");
        _canvas.getStyle().setProperty("height", "100%");
        _canvas.getStyle().setProperty("box-sizing", "border-box");

        // Create Canvas Buffer - offscreen drawing to avoid flicker of async JNI
        // Needed because all canvas drawing (JNI) calls get evaluated (and displayed) before next drawing call
        _canvasBuffer = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        _canvasBuffer.getStyle().setProperty("width", "100%");
        _canvasBuffer.getStyle().setProperty("height", "100%");
        _canvasBuffer.getStyle().setProperty("box-sizing", "border-box");

        // Add RootView listener to propagate size changes to canvas
        _rootView.addPropChangeListener(pc -> rootViewSizeChange(), View.Width_Prop, View.Height_Prop);
        rootViewSizeChange();

        // Have to do this so TouchEvent.preventDefault doesn't complain and iOS doesn't scroll doc
        _canvas.getStyle().setProperty("touch-action", "none");
        _canvas.setAttribute("touch-action", "none");
        _canvas.addEventListener("touchstart", e -> e.preventDefault());
        _canvas.addEventListener("touchmove", e -> e.preventDefault());
        _canvas.addEventListener("touchend", e -> e.preventDefault());
        _canvas.addEventListener("wheel", e -> e.preventDefault());

        // Create painer
        _pntr = new CJPainter(_canvasBuffer, _scale);

        // Register for drop events
//        _canvas.setAttribute("draggable", "true");
//        EventListener dragLsnr = e -> handleDragEvent((DragEvent)e);
//        _canvas.addEventListener("dragenter", dragLsnr);
//        _canvas.addEventListener("dragover", dragLsnr);
//        _canvas.addEventListener("dragexit", dragLsnr);
//        _canvas.addEventListener("drop", dragLsnr);

        // Register for drag start event
//        _canvas.addEventListener("dragstart", e -> handleDragGesture((DragEvent)e));
//        _canvas.addEventListener("dragend", e -> handleDragEnd((DragEvent)e));
    }

    /**
     * Called to register for repaint.
     */
    public void paintViews(Rect aRect)
    {
        _pntr.setTransform(1,0,0,1,0,0); // I don't know why I need this!
        ViewUpdater updater = _rootView.getUpdater();
        updater.paintViews(_pntr, aRect);

        // Copy buffer to canvas
        CanvasRenderingContext2D context = (CanvasRenderingContext2D) _canvas.getContext("2d");
        double rectX = aRect.x * _scale;
        double rectY = aRect.y * _scale;
        double rectW = aRect.width * _scale;
        double rectH = aRect.height * _scale;
        context.drawImage(_canvasBuffer, rectX, rectY, rectW, rectH, rectX, rectY, rectW, rectH);
    }

    /**
     * Called when root view size changes.
     */
    void rootViewSizeChange()
    {
        int rootW = (int) Math.ceil(_rootView.getWidth());
        int rootH = (int) Math.ceil(_rootView.getHeight());
        _canvas.setWidth(rootW * _scale);
        _canvas.setHeight(rootH * _scale);
        _canvasBuffer.setWidth(rootW * _scale);
        _canvasBuffer.setHeight(rootH * _scale);
    }

    /**
     * Called to handle a drag event.
     * Not called on app thread, because drop data must be processed when event is issued.
     * TVEnv.runOnAppThread(() -> handleDragEvent(anEvent));
     */
//    public void handleDragEvent(DragEvent anEvent)
//    {
//        anEvent.preventDefault();
//        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
//        _rootView.getWindow().dispatchEventToWindow(event);
//    }

    /** Called to handle a drag event. */
//    public void handleDragGesture(DragEvent anEvent)
//    {
//        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
//        _rootView.getWindow().dispatchEventToWindow(event);
//        if (!TVDragboard.isDragging) {
//            anEvent.preventDefault();
//            anEvent.stopPropagation();
//        }
//    }

    /** Called to handle dragend event. */
//    public void handleDragEnd(DragEvent anEvent)
//    {
//        ViewEvent nevent = ViewEvent.createEvent(_rootView, anEvent, null, null);
//        _rootView.getWindow().dispatchEventToWindow(nevent);
//    }
}