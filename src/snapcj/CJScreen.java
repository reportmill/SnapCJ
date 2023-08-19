package snapcj;
import cjdom.*;
import snap.view.PopupWindow;
import snap.view.View;
import snap.view.ViewEvent;
import snap.view.WindowView;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to work with the browser web page.
 */
public class CJScreen {

    // The Window hit by last MouseDown
    private WindowView  _mousePressWin;

    // The Window hit by last MouseMove (if mouse still down)
    private WindowView  _mouseDownWin;

    // Time of last mouse release
    private long  _lastReleaseTime;

    // Last number of clicks
    private int  _clicks;

    // The list of open windows
    private List <WindowView>  _windows = new ArrayList<>();

    // The current main window
    private WindowView  _win;

    // The shared screen object
    private static CJScreen _screen;

    /**
     * Creates a TVScreen.
     */
    private CJScreen()
    {
        // Get Doc and body
        HTMLDocument doc = HTMLDocument.getDocument();
        HTMLBodyElement body = doc.getBody();

        // Add Mouse listeners
        cjdom.EventListener lsnr = e -> handleEvent(e);
        body.addEventListener("mousedown", lsnr);
        body.addEventListener("mousemove", lsnr);
        body.addEventListener("mouseup", lsnr);
        body.addEventListener("wheel", lsnr);

        // Add Key Listeners
        body.addEventListener("keydown", lsnr);
        body.addEventListener("keyup", lsnr);

        // Add pointerdown: Used to keep getting events when mousedown goes outside window
        body.addEventListener("pointerdown", lsnr);

        // Add Touch Listeners
        body.addEventListener("touchstart", lsnr);
        body.addEventListener("touchmove", lsnr);
        body.addEventListener("touchend", lsnr);

        // Disable click, contextmenu events
        EventListener stopLsnr = e -> { };
        body.addEventListener("click", stopLsnr);
        body.addEventListener("contextmenu", stopLsnr);

        // Disable selection events on iOS
        body.addEventListener("select", stopLsnr);
        body.addEventListener("selectstart", stopLsnr);
        body.addEventListener("selectend", stopLsnr);
    }

    /**
     * Handles an event.
     */
    void handleEvent(Event e)
    {
        // Vars
        Runnable run = null;

        // Handle event types
        switch(e.getType()) {

            // Handle MouseDown
            case "mousedown":
                run = () -> mouseDown((MouseEvent) e);
                _mousePressWin = _mouseDownWin = getWindow((MouseEvent) e);
                if (_mousePressWin == null) return;
                break;

            // Handle MouseMove
            case "mousemove":
                if (_mouseDownWin != null)
                    run = () -> mouseDrag((MouseEvent) e);
                else run = () -> mouseMove((MouseEvent) e);
                break;

            // Handle MouseUp
            case "mouseup":
                run = () -> mouseUp((MouseEvent) e);
                if (_mousePressWin == null) return; //stopProp = prevDefault = true;
                break;

            // Handle Wheel
            case "wheel":
                if (getWindow((WheelEvent) e) == null) return;
                run = () -> mouseWheel((WheelEvent) e);
                break;

            // Handle KeyDown
            case "keydown":
                if (_mousePressWin == null) return;
                run = () -> keyDown((KeyboardEvent) e);
                break;

            // Handle KeyUp
            case "keyup":
                if (_mousePressWin == null) return;
                run = () -> keyUp((KeyboardEvent) e);
                break;

            // Handle TouchStart
            case "touchstart":
                run = () -> touchStart((TouchEvent) e);
                _mousePressWin = _mouseDownWin = getWindow((TouchEvent) e);
                if (_mousePressWin == null) return;
                break;

            // Handle TouchMove
            case "touchmove":
                if (_mousePressWin == null) return;
                run = () -> touchMove((TouchEvent) e);
                break;

            // Handle TouchEnd
            case "touchend":
                if (_mousePressWin == null) return;
                run = () -> touchEnd((TouchEvent) e);
                break;

            // Handle pointerDown
            case "pointerdown":
                setPointerCapture(e);
                break;

            // Unknown
            default: System.err.println("CJScreen.handleEvent: Not handled: " + e.getType()); return;
        }

        // Run event
        if (run != null)
            run.run(); // CJEnv.runOnAppThread(run);
    }

    /**
     * This is used to keep getting events even when mousedown goes outside window.
     */
    public void setPointerCapture(Event anEvent)
    {
        // document.body.setPointerCapture(anEvent.pointerId)
        HTMLDocument doc = HTMLDocument.getDocument();
        HTMLBodyElement body = doc.getBody();
        int id = anEvent.getMemberInt("pointerId");
        body.callWithDouble("setPointerCapture", id);
    }

    /**
     * Returns the list of visible windows.
     */
    public List <WindowView> getWindows()  { return _windows; }

    /**
     * Called when a window is ordered onscreen.
     */
    public void addWindow(WindowView aWin)
    {
        // Add to list
        _windows.add(aWin);

        // If not Popup, make window main window
        if (!(aWin instanceof PopupWindow))
            _win = _mousePressWin = aWin;
    }

    /**
     * Called when a window is hidden.
     */
    public void removeWindow(WindowView aWin)
    {
        // Remove window from list
        _windows.remove(aWin);

        // Make next window in list main window
        _win = null;
        for (int i = _windows.size() - 1; i >= 0; i--) {
            WindowView win = _windows.get(i);
            if (!(win instanceof PopupWindow)) {
                _win = win;
                break;
            }
        }
    }

    /**
     * Called when body gets mouseMove.
     */
    public void mouseMove(MouseEvent anEvent)
    {
        // Get window for MouseEvent
        WindowView win = getWindow(anEvent);
        if (win == null) win = _win;
        if (win == null) return;

        // Dispatch MouseMove event
        ViewEvent event = createEvent(win, anEvent, View.MouseMove, null);
        event.setClickCount(_clicks);
        win.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets MouseDown.
     */
    public void mouseDown(MouseEvent anEvent)
    {
        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime < 400 ? (_clicks + 1) : 1;
        _lastReleaseTime = time;

        // Get MouseDownWin for event
        _mouseDownWin = getWindow(anEvent);
        if (_mouseDownWin == null) return;

        // Dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MousePress, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets mouseMove with MouseDown.
     */
    public void mouseDrag(MouseEvent anEvent)
    {
        if (_mouseDownWin == null) return;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets mouseUp.
     */
    public void mouseUp(MouseEvent anEvent)
    {
        if (_mouseDownWin == null) return;
        WindowView mouseDownWin = _mouseDownWin;
        _mouseDownWin = null;

        // Create and dispatch MouseRelease event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);
    }

    /* Only Y Axis Scrolling has been implemented */
    public void mouseWheel(WheelEvent anEvent)
    {
        // Get window for WheelEvent and dispatch WheelEvent event
        WindowView win = getWindow(anEvent); if (win == null) return;
        ViewEvent event = createEvent(win, anEvent, View.Scroll, null);
        win.dispatchEventToWindow(event);

        // if (event.isConsumed()) { anEvent.stopPropagation(); anEvent.preventDefault(); }
    }

    /**
     * Called when body gets keyDown.
     */
    public void keyDown(KeyboardEvent anEvent)
    {
        ViewEvent event = createEvent(_win, anEvent, View.KeyPress, null);
        _win.dispatchEventToWindow(event); //anEvent.stopPropagation();

        String str = anEvent.getKey();
        if (str == null || str.length() == 0) return;
        if (str.equals("Control") || str.equals("Alt") || str.equals("Meta") || str.equals("Shift")) return;
        if (str.equals("ArrowUp") || str.equals("ArrowDown") || str.equals("ArrowLeft") || str.equals("ArrowRight")) return;
        if (str.equals("Enter") || str.equals("Backspace") || str.equals("Escape")) return;
        keyPress(anEvent);
    }

    /**
     * Called when body gets keyPress.
     */
    public void keyPress(KeyboardEvent anEvent)
    {
        ViewEvent event = createEvent(_win, anEvent, View.KeyType, null);
        _win.dispatchEventToWindow(event); //anEvent.stopPropagation();
    }

    /**
     * Called when body gets keyUp.
     */
    public void keyUp(KeyboardEvent anEvent)
    {
        ViewEvent event = createEvent(_win, anEvent, View.KeyRelease, null);
        _win.dispatchEventToWindow(event); //anEvent.stopPropagation();
    }

    /**
     * Called when body gets TouchStart.
     */
    public void touchStart(TouchEvent anEvent)
    {
        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        // Get Click count and set MouseDown
        long time = System.currentTimeMillis();
        _clicks = time - _lastReleaseTime < 400 ? (_clicks + 1) : 1; _lastReleaseTime = time;

        // Get MouseDownWin for event
        _mouseDownWin = getWindow(anEvent);
        if (_mouseDownWin == null) return;
        anEvent.preventDefault();

        // Create and dispatch MousePress event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MousePress, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets touchMove.
     */
    public void touchMove(TouchEvent anEvent)
    {
        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        if (_mouseDownWin == null) return;
        anEvent.preventDefault();

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(_mouseDownWin, anEvent, View.MouseDrag, null);
        event.setClickCount(_clicks);
        _mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets touchEnd.
     */
    public void touchEnd(TouchEvent anEvent)
    {
        // Don't think this can happen
        if (anEvent.getTouch() == null) return;

        if (_mouseDownWin == null) return;
        anEvent.preventDefault();

        WindowView mouseDownWin = _mouseDownWin;
        _mouseDownWin = null;

        // Create and dispatch MouseDrag event
        ViewEvent event = createEvent(mouseDownWin, anEvent, View.MouseRelease, null);
        event.setClickCount(_clicks);
        mouseDownWin.dispatchEventToWindow(event);
    }

    /**
     * Called when body gets cut/copy/paste.
     */
    /*public void cutCopyPaste(ClipboardEvent anEvent)
    {
        String type = anEvent.getType();
        CJClipboard cb = (CJClipboard)Clipboard.get();
        DataTransfer dtrans = anEvent.getClipboardData();

        // Handle cut/copy: Load DataTransfer from Clipboard.ClipboardDatas
        if (type.equals("cut") || type.equals("copy")) {
            dtrans.clearData(null);
            for (ClipboardData cdata : cb.getClipboardDatas().values())
                if (cdata.isString())
                    dtrans.setData(cdata.getMIMEType(), cdata.getString());
        }

        // Handle paste: Update Clipboard.ClipboardDatas from DataTransfer
        else if (type.equals("paste")) {
            cb.clearData();
            for (String typ : dtrans.getTypes())
                cb.addData(typ,dtrans.getData(typ));
        }

        // Needed to push changes to system clipboard
        anEvent.preventDefault();
    }*/

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(MouseEvent anEvent)
    {
        int x = anEvent.getPageX();
        int y = anEvent.getPageY();
        return getWindow(x, y);
    }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(TouchEvent anEvent)
    {
        int x = anEvent.getPageX();
        int y = anEvent.getPageY();
        return getWindow(x, y);
    }

    /**
     * Returns the WindowView for an event.
     */
    public WindowView getWindow(int aX, int aY)
    {
        for (int i = _windows.size() - 1; i >= 0; i--) {
            WindowView win = _windows.get(i);
            if (win.isMaximized() || win.contains(aX - win.getX(), aY - win.getY()))
                return win;
        }
        return null;
    }

    /**
     * Creates an Event.
     */
    ViewEvent createEvent(WindowView aWin, Object anEvent, ViewEvent.Type aType, String aName)
    {
        View rootView = aWin.getRootView();
        ViewEvent event = ViewEvent.createEvent(rootView, anEvent, aType, aName);
        return event;
    }

    /**
     * Returns the shared screen.
     */
    public static CJScreen getScreen()
    {
        if (_screen != null) return _screen;
        return _screen = new CJScreen();
    }
}