package snapcj;
import snap.geom.Rect;
import snap.view.*;

/**
 * A WindowHpr to map WindowView to CJWindow.
 */
public class CJWindowHpr extends WindowView.WindowHpr {

    // The snap Window
    protected WindowView  _win;

    // The snap CJWindow
    protected CJWindow  _winNtv;

    /**
     * Returns the window.
     */
    @Override
    public WindowView getWindow()  { return _win; }

    /**
     * Sets the window and creates native.
     */
    @Override
    public void setWindow(WindowView aWin)
    {
        _win = aWin;
        _winNtv = new CJWindow(aWin);
    }

    /**
     * Returns the native.
     */
    public CJWindow getNative()  { return _winNtv; }

    /**
     * Window method: initializes native window.
     */
    public void initWindow()
    {
        _winNtv.initWindow();
    }

    /**
     * Window/Popup method: Shows the window.
     */
    public void show()
    {
        _winNtv.show();
    }

    /**
     * Window/Popup method: Hides the window.
     */
    public void hide()
    {
        _winNtv.hide();
    }

    /**
     * Window/Popup method: Order window to front.
     */
    public void toFront()
    {
        _winNtv.toFront();
    }

    /**
     * Registers a view for repaint.
     */
    public void requestPaint(Rect aRect)
    {
        _winNtv.paintViews(aRect);
    }
}
