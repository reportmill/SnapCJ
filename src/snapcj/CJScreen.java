package snapcj;
import java.util.*;
import snap.gfx.Rect;
import snap.view.*;
import cjdom.*;

/**
 * A class to work with the browser web page.
 */
public class CJScreen {

    // The HTMLDocument
    HTMLDocument          _doc = HTMLDocument.current();
    
    // The HTMLDocument
    HTMLBodyElement       _body = _doc.getBody();
    
    // The RootView hit by last MouseDown (if mouse still down)
    RootView              _mouseDownView;
    
    // Time of last mouse release
    long                  _lastReleaseTime;
    
    // Last number of clicks
    int                   _clicks;
    
    // The list of open windows
    List <WindowView>     _windows = new ArrayList();
    
    // The current main window
    WindowView            _win;
    
    // The focused root view
    RootView              _rview;
    
    // The shared screen object
    static CJScreen       _screen = new CJScreen();
    
/**
 * Creates a new CJScreen.
 */
private CJScreen()
{
    // Add Mouse listeners
    _body.addEventListener("mousedown", e -> mouseDown((MouseEvent)e));
    _body.addEventListener("mousemove", e -> mouseMove((MouseEvent)e));
    _body.addEventListener("mouseup", e -> mouseUp((MouseEvent)e));
    _body.addEventListener("wheel", e -> mouseWheel((WheelEvent)e));
    
    // Add Key Listeners
    _body.addEventListener("keydown", e -> keyDown((KeyboardEvent)e));
    _body.addEventListener("keypress", e -> keyPress((KeyboardEvent)e));
    _body.addEventListener("keyup", e -> keyUp((KeyboardEvent)e));
    
    // Add Touch Listeners
    _body.addEventListener("touchstart", e -> touchStart((TouchEvent)e));
    _body.addEventListener("touchmove", e -> touchMove((TouchEvent)e));
    _body.addEventListener("touchend", e -> touchEnd((TouchEvent)e));
    
    // Add cut, copy, paste listeners
    cjdom.EventListener cutCopyPasteLsnr = e -> cutCopyPaste((ClipboardEvent)e);
    _body.addEventListener("cut", cutCopyPasteLsnr);
    _body.addEventListener("copy", cutCopyPasteLsnr);
    _body.addEventListener("paste", cutCopyPasteLsnr);
    
    // Add bounds listener
    Window.current().addEventListener("resize", e -> boundsChanged());
}

/**
 * Called when a window is ordered onscreen.
 */
public void showWindow(WindowView aWin)
{
    _windows.add(aWin);
    if(!(aWin instanceof PopupWindow)) {
        _win = aWin; _rview = aWin.getRootView(); }
    if(aWin.isGrowWidth())
        boundsChanged();
}

/**
 * Called when a window is hidden.
 */
public void hideWindow(WindowView aWin)
{
    _windows.remove(aWin);
    _win = null;
    for(int i=_windows.size()-1;i>=0;i--) { WindowView win = _windows.get(i);
        if(!(win instanceof PopupWindow)) {
            _win = win; break; }}
    _rview = _win!=null? _win.getRootView() : null;
}

/**
 * Returns the screen (browser window) bounds.
 */
public Rect getBounds()
{
    int w = Window.current().getInnerWidth();
    int h = Window.current().getInnerHeight();
    return new Rect(0, 0, w, h);
}

/**
 * Called when body gets mouseMove.
 */
public void mouseMove(MouseEvent anEvent)
{
    // If MouseDown, forward to mouseDrag()
    if(_mouseDownView!=null) { mouseDrag(anEvent); return; }
    
    // Get RootView for MouseEvent
    RootView rview = getRootView(anEvent);

    // Dispatch MouseMove event
    ViewEvent event = CJViewEnv.get().createEvent(rview, anEvent, View.MouseMove, null);
    ((CJEvent)event)._ccount = _clicks;
    rview.dispatchEvent(event);
}

/**
 * Called when body gets MouseDown.
 */
public void mouseDown(MouseEvent anEvent)
{
    // Get Click count and set MouseDown
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    
    // Get MouseDownView for event
    _mouseDownView = getRootView(anEvent);
    
    // Dispatch MousePress event
    ViewEvent event = CJViewEnv.get().createEvent(_mouseDownView, anEvent, View.MousePress, null);
    ((CJEvent)event)._ccount = _clicks;
    _mouseDownView.dispatchEvent(event);
}

/**
 * Called when body gets mouseMove with MouseDown.
 */
public void mouseDrag(MouseEvent anEvent)
{
    ViewEvent event = CJViewEnv.get().createEvent(_mouseDownView, anEvent, View.MouseDrag, null);
    ((CJEvent)event)._ccount = _clicks;
    _mouseDownView.dispatchEvent(event);
}

/**
 * Called when body gets mouseUp.
 */
public void mouseUp(MouseEvent anEvent)
{
    RootView mouseDownView = _mouseDownView; _mouseDownView = null;
    ViewEvent event = CJViewEnv.get().createEvent(mouseDownView, anEvent, View.MouseRelease, null);
    ((CJEvent)event)._ccount = _clicks;
    mouseDownView.dispatchEvent(event);
}

/* Only Y Axis Scrolling has been implemented */
public void mouseWheel(WheelEvent anEvent)
{
    // Get RootView for WheelEvent
    RootView rview = getRootView(anEvent);

    // Dispatch WheelEvent event
    ViewEvent event = CJViewEnv.get().createEvent(rview, anEvent, View.Scroll, null);
    rview.dispatchEvent(event);
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets keyDown.
 */
public void keyDown(KeyboardEvent anEvent)
{
    ViewEvent event = CJViewEnv.get().createEvent(_rview, anEvent, View.KeyPress, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets keyPress.
 */
public void keyPress(KeyboardEvent anEvent)
{
    ViewEvent event = CJViewEnv.get().createEvent(_rview, anEvent, View.KeyType, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets keyUp.
 */
public void keyUp(KeyboardEvent anEvent)
{
    ViewEvent event = CJViewEnv.get().createEvent(_rview, anEvent, View.KeyRelease, null);
    _rview.dispatchEvent(event);
    anEvent.stopPropagation();
}

/**
 * Called when body gets TouchStart.
 */
public void touchStart(TouchEvent anEvent)
{
    // Get event touches and first touch
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    // Get Click count and set MouseDown
    long time = System.currentTimeMillis();
    _clicks = time - _lastReleaseTime<400? (_clicks+1) : 1; _lastReleaseTime = time;
    
    // Get MouseDownView for event
    _mouseDownView = getRootView(touch);

    // Dispatch MousePress event
    ViewEvent event = CJViewEnv.get().createEvent(_mouseDownView, touch, View.MousePress, null);
    ((CJEvent)event)._ccount = _clicks;
    _mouseDownView.dispatchEvent(event);
    
    // Suppress other actions
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets touchMove.
 */
public void touchMove(TouchEvent anEvent)
{
    // Get event touches and first touch
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    // Dispatch MouseDrag event
    ViewEvent event = CJViewEnv.get().createEvent(_mouseDownView, touch, View.MouseDrag, null);
    ((CJEvent)event)._ccount = _clicks;
    _mouseDownView.dispatchEvent(event);
    
    // Suppress other actions
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets touchEnd.
 */
public void touchEnd(TouchEvent anEvent)
{
    // Get event touches and first touch
    Touch touches[] = anEvent.getChangedTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    
    // Dispatch MouseRelease event
    RootView mouseDownView = _mouseDownView; _mouseDownView = null;
    ViewEvent event = CJViewEnv.get().createEvent(mouseDownView, touch, View.MouseRelease, null);
    ((CJEvent)event)._ccount = _clicks;
    mouseDownView.dispatchEvent(event);
    
    // Suppress other actions
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets cut/copy/paste.
 */
public void cutCopyPaste(ClipboardEvent anEvent)
{
    String type = anEvent.getType();
    CJClipboard cb = (CJClipboard)Clipboard.get();
    DataTransfer dtrans = anEvent.getClipboardData();
    
    // Handle cut/copy: Load DataTransfer from Clipboard.ClipboardDatas
    if(type.equals("cut") || type.equals("copy")) {
        dtrans.clearData(null);
        for(ClipboardData cdata : cb.getClipboardDatas().values())
            if(cdata.isString())
                dtrans.setData(cdata.getMIMEType(), cdata.getString());
    }
    
    // Handle paste: Update Clipboard.ClipboardDatas from DataTransfer
    else if(type.equals("paste")) {
        cb.clearData();
        for(String typ : dtrans.getTypes())
            cb.addData(typ,dtrans.getData(typ));
    }
    
    // Needed to push changes to system clipboard
    anEvent.preventDefault();
}

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(MouseEvent anEvent)  { return getRootView(anEvent.getClientX(), anEvent.getClientY()); }

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(Touch anEvent)  { return getRootView(anEvent.getClientX(), anEvent.getClientY()); }

/**
 * Returns the RootView for an event.
 */
public RootView getRootView(int aX, int aY)
{
    for(int i=_windows.size()-1;i>=0;i--) { WindowView wview = _windows.get(i);
        if(wview.contains(aX - wview.getX(), aY - wview.getY()))
            return wview.getRootView(); }
    return _rview;
}

/**
 * Called when screen (browser window) size changes.
 */
public void boundsChanged()
{
    for(WindowView win : _windows)
        if(win.isGrowWidth())
            win.setBounds(getBounds());
}

/**
 * Returns the shared screen.
 */
public static CJScreen get()  { return _screen; }

}