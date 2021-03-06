package snapcj;
import cjdom.*;
import snap.gfx.Point;
import snap.view.*;

/**
 * A ViewEvent implementation for CheerpJ.
 */
public class CJEvent extends ViewEvent {

    // The mouse location
    double            _mx = Float.MIN_VALUE, _my = Float.MIN_VALUE;
    
    // The click count
    int               _ccount = -1;

/** Returns the mouse event x. */
public double getX()  { if(_mx==Float.MIN_VALUE) setXY(); return _mx; }

/** Returns the mouse event y. */
public double getY()  { if(_my==Float.MIN_VALUE) setXY(); return _my; }

/** Sets the event point from browser mouse event. */
void setXY()
{
    if(isTouch())  { setXYTouch(); return; }
    MouseEvent event = getEvent(MouseEvent.class); if(event==null) { _mx = _my = 0; return; }
    double x = event.getClientX();
    double y = event.getClientY();
    Point pt = getView().parentToLocal(x,y,null);
    _mx = pt.x; _my = pt.y;
}

/** Sets the event point from browser mouse event. */
void setXYTouch()
{
    Touch touch = (Touch)getEvent();
    double x = touch.getClientX();
    double y = touch.getClientY();
    Point pt = getView().parentToLocal(x,y,null);
    _mx = pt.x; _my = pt.y;
}

/** Returns the click count for a mouse event. */
public int getClickCount()  { return _ccount; }

/** Returns the scroll amount for a wheel event. */
public double getScrollX()
{
    WheelEvent event = (WheelEvent)getEvent();
    _mx = event.getDeltaX();
    return _mx;
}

/** Returns the scroll amount for a wheel event. */
public double getScrollY()
{
    WheelEvent event = (WheelEvent)getEvent();
    _my = event.getDeltaY();
    return _my;
}

/** Returns whether event is touch event. */
boolean isTouch()
{
    if(_touch<0) _touch = getEvent() instanceof Touch? 1 : 0;
    return _touch==1;
}
int _touch = -1;

/** Returns the event keycode. */
public int getKeyCode()
{
    KeyboardEvent kev = (KeyboardEvent)getEvent();
    int kcode = kev.getKeyCode(); if(kcode==13) kcode = 10;
    return kcode;
}

/** Returns the event key char. */
public char getKeyChar()  { return (char)getKeyCode(); }

/** Returns whether shift key is down. */
public boolean isShiftDown()
{
    if(isTouch()) return false;
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isShiftKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).isShiftKey();
    return false;
}

/** Returns whether control key is down. */
public boolean isControlDown()
{
    if(isTouch()) return false;
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isCtrlKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).isCtrlKey();
    return false;
}

/** Returns whether alt key is down. */
public boolean isAltDown()
{
    if(isTouch()) return false;
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isAltKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).isAltKey();
    return false;
}

/** Returns whether "meta" key is down (the command key on Mac with no equivalent on Windows). */
public boolean isMetaDown()
{
    if(isTouch()) return false;
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isMetaKey();
    if(isMouseEvent()) return ((MouseEvent)getEvent()).isMetaKey();
    return false;
}

/** Returns whether shortcut key is pressed. */
public boolean isShortcutDown()
{
    if(isTouch()) return false;
    if(isKeyEvent()) return ((KeyboardEvent)getEvent()).isMetaKey();
    if(isMouseEvent()) return isMetaDown() || isControlDown();
    return false;
}

/**
 * Returns the drag Clipboard for this event.
 */
public Clipboard getClipboard()  { return CJClipboard.getDrag(this); }

/** Called to indicate that drop is accepted. */
public void acceptDrag()  { CJClipboard.getDrag(this).acceptDrag(); }

/** Called to indicate that drop is complete. */
public void dropComplete()  { CJClipboard.getDrag(this).dropComplete(); }

/**
 * Returns a view event at new point.
 */
public ViewEvent copyForViewPoint(View aView, double aX, double aY, int aClickCount)
{
    String name = getName(); if(name!=null && (name.length()==0 || name.equals(getView().getName()))) name = null;
    CJEvent copy = (CJEvent)CJViewEnv.get().createEvent(aView, getEvent(), getType(), name);
    copy._mx = aX; copy._my = aY; copy._ccount = aClickCount>0? aClickCount : _ccount;
    return copy;
}

/**
 * Returns the event type.
 */
protected Type getTypeImpl()
{
    Event event = (Event)getEvent();
    String type = event.getType();
    switch(type) {
        case "dragstart": return Type.DragGesture;
        case "dragend": return Type.DragSourceEnd;
        case "dragenter": return Type.DragEnter;
        case "dragexit": return Type.DragExit;
        case "dragover": return Type.DragOver;
        case "drop": return Type.DragDrop;
        default: return null;
    }
}

}