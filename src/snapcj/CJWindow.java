package snapcj;
import cjdom.*;
import cjdom.EventListener;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.view.*;

/**
 * A class to represent the WindowView in the browser page.
 */
public class CJWindow {

    // The Window View
    protected WindowView _win;

    // The RootView
    protected RootView _rootView;

    // A div element to hold window canvas
    protected HTMLElement _windowDiv;

    // The HTMLCanvas to paint/show window content
    protected HTMLCanvasElement _canvas;

    // The HTMLCanvas 'double-buffering' buffer - offscreen drawing to avoid flicker of async JNI
    protected HTMLCanvasElement _canvasBuffer;

    // The Painter for window content
    private Painter _painter;

    // The rendering context for canvas
    private CanvasRenderingContext2D _canvasContext;

    // The parent element holding window element when showing
    protected HTMLElement _parent;

    // A listener for hide
    protected PropChangeListener _hideLsnr;

    // A listener for browser window resize
    protected EventListener<?> _resizeLsnr = null;

    // The body margin value
    protected String _bodyMargin = "undefined";

    // The body overflow value
    private String _bodyOverflow;

    // The last top window
    protected static int _topWin;

    // The paint scale
    public static int PIXEL_SCALE = CJDom.getDevicePixelRatio() == 2 ? 2 : 1;

    /**
     * Constructor.
     */
    public CJWindow(WindowView snapWindow)
    {
        // Set Window and RootView
        _win = snapWindow;
        _rootView = _win.getRootView();

        // Set window and start listening to bounds, Maximized and ActiveCursor changes
        _win.addPropChangeListener(pc -> snapWindowMaximizedChanged(), WindowView.Maximized_Prop);
        _win.addPropChangeListener(pce -> snapWindowBoundsChanged(pce), View.X_Prop, View.Y_Prop,
                View.Width_Prop, View.Height_Prop);
        _win.addPropChangeListener(pc -> snapWindowActiveCursorChanged(), WindowView.ActiveCursor_Prop);

        // Create/configure WindowDiv, the HTMLElement to hold window and canvas
        HTMLDocument doc = HTMLDocument.getDocument();
        _windowDiv = doc.createElement("div");
        _windowDiv.getStyle().setProperty("box-sizing", "border-box");
        _windowDiv.getStyle().setProperty("background", "#F4F4F4CC");

        // Create canvas and configure to totally fill window element (minus padding insets)
        _canvas = (HTMLCanvasElement) HTMLDocument.getDocument().createElement("canvas");
        _canvas.getStyle().setProperty("width", "100%");
        _canvas.getStyle().setProperty("height", "100%");
        _canvas.getStyle().setProperty("box-sizing", "border-box");

        // Create Canvas Buffer - offscreen drawing to avoid flicker of async JNI
        // Needed because all canvas drawing (JNI) calls get evaluated (and displayed) before next drawing call
        _canvasBuffer = (HTMLCanvasElement) HTMLDocument.getDocument().createElement("canvas");
        _canvasBuffer.getStyle().setProperty("width", "100%");
        _canvasBuffer.getStyle().setProperty("height", "100%");
        _canvasBuffer.getStyle().setProperty("box-sizing", "border-box");

        // Add RootView listener to propagate size changes to canvas
        _rootView.addPropChangeListener(pc -> rootViewSizeChange(), View.Width_Prop, View.Height_Prop);
        rootViewSizeChange();

        // Have to do this so TouchEvent.preventDefault doesn't complain and iOS doesn't scroll doc
        _canvas.getStyle().setProperty("touch-action", "none");
        _canvas.setAttribute("touch-action", "none");
        //_canvas.addEventListener("touchstart", e -> e.preventDefault());
        //_canvas.addEventListener("touchmove", e -> e.preventDefault());
        //_canvas.addEventListener("touchend", e -> e.preventDefault());
        //_canvas.addEventListener("wheel", e -> e.preventDefault());

        // Create painter
        _painter = new CJPainter(_canvasBuffer, PIXEL_SCALE);
        _canvasContext = (CanvasRenderingContext2D) _canvas.getContext("2d");

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

        // Add canvas to WindowDiv
        _windowDiv.appendChild(_canvas);
    }

    /**
     * Initializes window.
     */
    public void initWindow()
    {
        if (_rootView.getFill() == null)
            _rootView.setFill(ViewUtils.getBackFill());
        if (_rootView.getBorder() == null)
            _rootView.setBorder(Color.GRAY, 1);
    }

    /**
     * Returns the canvas for the window.
     */
    public HTMLCanvasElement getCanvas()  { return _canvas; }

    /**
     * Returns the parent DOM element of this window (WindowDiv).
     */
    public HTMLElement getParent()  { return _parent; }

    /**
     * Sets the parent DOM element of this window (WindowDiv).
     */
    protected void setParent(HTMLElement aNode)
    {
        // If already set, just return
        if (aNode == _parent) return;

        // Set new value
        HTMLElement parent = _parent;
        _parent = aNode;

        // If null, just remove from old parent and return
        if (aNode == null) {
            parent.removeChild(_windowDiv);
            return;
        }

        // Add WindowDiv to given node
        aNode.appendChild(_windowDiv);

        // If body, configure special
        HTMLBodyElement body = HTMLBodyElement.getBody();
        if (aNode == body) {

            // Set body and html height so that document covers the whole browser page
            HTMLDocument doc = HTMLDocument.getDocument();
            HTMLHtmlElement html = doc.getDocumentElement();
            html.getStyle().setProperty("height", "100%");
            body.getStyle().setProperty("min-height", "100%");
            _bodyMargin = body.getStyle().getPropertyValue("margin");
            body.getStyle().setProperty("margin", "0");

            // Configure WindowDiv for body
            _windowDiv.getStyle().setProperty("position", _win.isMaximized() ? "fixed" : "absolute");
            _windowDiv.getStyle().setProperty("z-index", String.valueOf(_topWin++));

            // If not maximized, clear background and add drop shadow
            if (!_win.isMaximized()) {
                _windowDiv.getStyle().setProperty("background", null);
                _windowDiv.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
            }

            // If Window.Type not PLAIN, attach WindowBar
            if (_win.getType() != WindowView.TYPE_PLAIN) {
                WindowBar windowBar = WindowBar.attachWindowBar(_rootView);
                if (_win.isMaximized())
                    windowBar.setTitlebarHeight(18);
            }
        }

        // If arbitrary element
        else {
            if (_bodyMargin != "undefined")
                body.getStyle().setProperty("margin", _bodyMargin);
            _windowDiv.getStyle().setProperty("position", "static");
            _windowDiv.getStyle().setProperty("width", "100%");
            _windowDiv.getStyle().setProperty("height", "100%");
        }
    }

    /**
     * Returns the parent DOM element of this window.
     */
    private HTMLElement getParentForWin()
    {
        // Get body
        HTMLBodyElement body = HTMLBodyElement.getBody();

        // If window is maximized, parent should always be body
        if (_win.isMaximized())
            return body;

        // If window has named element, return that
        String parentName = _win.getName();
        if (parentName != null) {
            HTMLDocument doc = HTMLDocument.getDocument();
            HTMLElement parent = doc.getElementById(parentName);
            if (parent != null)
                return parent;
        }

        // Default to body
        return body;
    }

    /**
     * Returns whether window is child of body.
     */
    private boolean isChildOfBody()
    {
        HTMLDocument doc = HTMLDocument.getDocument();
        HTMLBodyElement body = doc.getBody();
        return getParent() == body;
    }

    /**
     * Resets the parent DOM element and Window/WindowDiv bounds.
     */
    protected void resetParentAndBounds()
    {
        // Get proper parent node and set
        HTMLElement parent = getParentForWin();
        setParent(parent);

        // If window floating in body, set WindowDiv bounds from Window
        HTMLBodyElement body = HTMLBodyElement.getBody();
        if (parent == body) {
            if (_win.isMaximized())
                _win.setBounds(CJ.getViewportBounds());
            snapWindowBoundsChanged(null);
        }

        // If window in DOM container element
        else browserWindowSizeChanged();
    }

    /**
     * Shows window.
     */
    public void show()
    {
        if (_win.isModal())
            showModal();
        else showImpl();
    }

    /**
     * Shows window.
     */
    public void showImpl()
    {
        // Make sure WindowDiv is in proper parent node with proper bounds
        resetParentAndBounds();

        // Add to Screen.Windows
        CJScreen screen = CJScreen.getScreen();
        screen.addWindow(_win);

        // Set Window showing
        ViewUtils.setShowing(_win, true);
        ViewUtils.setFocused(_win, true);

        // Start listening to browser window resizes
        if (_resizeLsnr == null)
            _resizeLsnr = e -> browserWindowSizeChanged();
        Window.current().addEventListener("resize", _resizeLsnr);
    }

    /**
     * Shows modal window.
     */
    protected synchronized void showModal()
    {
        // Do normal show
        showImpl();

        // Register listener to activate current thread on window not showing
        _win.addPropChangeListener(_hideLsnr = pc -> snapWindowShowingChanged(), View.Showing_Prop);

        // Start new app thread, since this thread is now tied up until window closes
//        CJEnv.get().startNewAppThread();

        // Wait until window is hidden
        try { wait(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Hides window.
     */
    public void hide()
    {
        // Remove WindowDiv from parent
        setParent(null);

        // Remove Window from screen
        CJScreen screen = CJScreen.getScreen();
        screen.removeWindow(_win);

        // Set Window not showing
        ViewUtils.setShowing(_win, false);
        ViewUtils.setFocused(_win, false);

        // Stop listening to browser window resizes
        Window.current().removeEventListener("resize", _resizeLsnr);
        _resizeLsnr = null;

        // Send WinClose event
        sendWinEvent(ViewEvent.Type.WinClose);
    }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()
    {
        _windowDiv.getStyle().setProperty("z-index", String.valueOf(_topWin++));
    }

    /**
     * Called to register for repaint.
     */
    public void paintViews(Rect aRect)
    {
        _painter.setTransform(1,0,0,1,0,0); // I don't know why I need this!
        ViewUpdater updater = _rootView.getUpdater();
        updater.paintViews(_painter, aRect);

        // Copy buffer to canvas
        double rectX = aRect.x * PIXEL_SCALE;
        double rectY = aRect.y * PIXEL_SCALE;
        double rectW = aRect.width * PIXEL_SCALE;
        double rectH = aRect.height * PIXEL_SCALE;
        _canvasContext.drawImage(_canvasBuffer, rectX, rectY, rectW, rectH, rectX, rectY, rectW, rectH);
    }

    /**
     * Called when window changes showing.
     */
    private synchronized void snapWindowShowingChanged()
    {
        _win.removePropChangeListener(_hideLsnr);
        _hideLsnr = null;
        notify();
    }

    /**
     * Called when browser window resizes.
     */
    private void browserWindowSizeChanged()
    {
        // If Window is child of body, just return
        if (isChildOfBody()) {
            if (_win.isMaximized())
                _win.setBounds(CJ.getViewportBounds());
            return;
        }

        // Reset window location
        HTMLElement parent = getParent();
        Point off = CJ.getOffsetAll(parent);
        _win.setXY(off.x, off.y);

        // Reset window size
        int parW = parent.getClientWidth();
        int parH = parent.getClientHeight();
        _win.setSize(parW, parH);
        _win.repaint();
    }

    /**
     * Called when WindowView has bounds change to sync to WindowDiv.
     */
    private void snapWindowBoundsChanged(PropChange aPC)
    {
        // If Window not child of body, just return (parent node changes go to win, not win to parent)
        if (!isChildOfBody()) return;

        // Get bounds x, y, width, height and PropChange name
        int x = (int) Math.round(_win.getX());
        int y = (int) Math.round(_win.getY());
        int w = (int) Math.round(_win.getWidth());
        int h = (int) Math.round(_win.getHeight());
        String propName = aPC != null ? aPC.getPropName() : null;

        // Handle changes
        if (propName == null || propName == View.X_Prop)
            _windowDiv.getStyle().setProperty("left", x + "px");
        if (propName == null || propName == View.Y_Prop)
            _windowDiv.getStyle().setProperty("top", y + "px");
        if (propName == null || propName == View.Width_Prop)
            _windowDiv.getStyle().setProperty("width", w + "px");
        if (propName == null || propName == View.Height_Prop)
            _windowDiv.getStyle().setProperty("height", h + "px");
    }

    /**
     * Called when root view size changes.
     */
    private void rootViewSizeChange()
    {
        int rootW = (int) Math.ceil(_rootView.getWidth());
        int rootH = (int) Math.ceil(_rootView.getHeight());
        _canvas.setWidth(rootW * PIXEL_SCALE);
        _canvas.setHeight(rootH * PIXEL_SCALE);
        _canvasBuffer.setWidth(rootW * PIXEL_SCALE);
        _canvasBuffer.setHeight(rootH * PIXEL_SCALE);
    }

    /**
     * Called to handle a drag event.
     * Not called on app thread, because drop data must be processed when event is issued.
     * TVEnv.runOnAppThread(() -> handleDragEvent(anEvent));
     */
    public void handleDragEvent(DragEvent anEvent)
    {
        anEvent.preventDefault();
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
    }

    /** Called to handle a drag event. */
    public void handleDragGesture(DragEvent anEvent)
    {
        ViewEvent event = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(event);
        if (!CJDragboard.isDragging) {
            anEvent.preventDefault();
            anEvent.stopPropagation();
        }
    }

    /** Called to handle dragend event. */
    public void handleDragEnd(DragEvent anEvent)
    {
        ViewEvent nevent = ViewEvent.createEvent(_rootView, anEvent, null, null);
        _rootView.getWindow().dispatchEventToWindow(nevent);
    }

    /**
     * Called when WindowView.Maximized is changed.
     */
    private void snapWindowMaximizedChanged()
    {
        // Get body and canvas
        HTMLBodyElement body = HTMLBodyElement.getBody();
        HTMLCanvasElement canvas = getCanvas();

        // Handle Maximized on
        if (_win.isMaximized()) {

            // Set body overflow to hidden (to get rid of scrollbars)
            _bodyOverflow = body.getStyle().getPropertyValue("overflow");
            body.getStyle().setProperty("overflow", "hidden");

            // Set Window/WindowDiv padding
            _win.setPadding(5, 5, 5, 5);
            _windowDiv.getStyle().setProperty("padding", "5px");

            // Add a shadow to canvas
            canvas.getStyle().setProperty("box-shadow", "1px 1px 8px grey");
        }

        // Handle Maximized off
        else {

            // Restore body overflow
            body.getStyle().setProperty("overflow", _bodyOverflow);

            // Clear Window/WindowDiv padding
            _win.setPadding(0, 0, 0, 0);
            _windowDiv.getStyle().setProperty("padding", null);

            // Remove shadow from canvas
            canvas.getStyle().setProperty("box-shadow", null);
        }

        // Reset parent and Window/WindowDiv bounds
        resetParentAndBounds();
    }

    /**
     * Sets the cursor.
     */
    private void snapWindowActiveCursorChanged()
    {
        Cursor aCursor = _win.getActiveCursor();
        String cstr = "default";
        if (aCursor == Cursor.DEFAULT) cstr = "default";
        if (aCursor == Cursor.CROSSHAIR) cstr = "crosshair";
        if (aCursor == Cursor.HAND) cstr = "pointer";
        if (aCursor == Cursor.MOVE) cstr = "move";
        if (aCursor == Cursor.TEXT) cstr = "text";
        if (aCursor == Cursor.NONE) cstr = "none";
        if (aCursor == Cursor.N_RESIZE) cstr = "n-resize";
        if (aCursor == Cursor.S_RESIZE) cstr = "s-resize";
        if (aCursor == Cursor.E_RESIZE) cstr = "e-resize";
        if (aCursor == Cursor.W_RESIZE) cstr = "w-resize";
        if (aCursor == Cursor.NE_RESIZE) cstr = "ne-resize";
        if (aCursor == Cursor.NW_RESIZE) cstr = "nw-resize";
        if (aCursor == Cursor.SE_RESIZE) cstr = "se-resize";
        if (aCursor == Cursor.SW_RESIZE) cstr = "sw-resize";
        getCanvas().getStyle().setProperty("cursor", cstr);
    }

    /**
     * Sends the given event.
     */
    private void sendWinEvent(ViewEvent.Type aType)
    {
        // If no listener for event type, just return
        if (!_win.getEventAdapter().isEnabled(aType))
            return;

        // Create ViewEvent and dispatch
        ViewEvent event = ViewEvent.createEvent(_win, null, aType, null);
        _win.dispatchEventToView(event);
    }
}