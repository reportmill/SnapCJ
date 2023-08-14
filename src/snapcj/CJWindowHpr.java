package snapcj;
import cjdom.HTMLBodyElement;
import cjdom.HTMLDocument;
import snap.geom.Rect;
import snap.view.TextArea;
import snap.view.TextField;
import snap.view.View;
import snap.view.WindowView;

/**
 * A WindowHpr to map WindowView to TVWindow.
 */
public class CJWindowHpr extends WindowView.WindowHpr<CJWindow> {

    // The snap Window
    protected WindowView  _win;

    // The snap CJWindow
    protected CJWindow  _winNtv;

    // Whether content is editable
    private boolean _contentEditable;

    /**
     * Creates the native.
     */
    public WindowView getWindow()
    {
        return _win;
    }

    /**
     * Override to set snap Window in TVWindow.
     */
    public void setWindow(WindowView aWin)
    {
        _win = aWin;
        _winNtv = new CJWindow();
        _winNtv.setWindow(aWin);
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
        _winNtv._rootViewNtv.paintViews(aRect);
    }

    /**
     * Notifies that focus changed.
     */
    public void focusDidChange(View aView)
    {
        boolean isText = aView instanceof TextArea || aView instanceof TextField;
        setContentEditable(isText);
    }

    /**
     * Sets ContentEditable on canvas.
     */
    public void setContentEditable(boolean aValue)
    {
        // If already set, just return
        if (aValue == _contentEditable) return;

        // Set value
        _contentEditable = aValue;

        // Update Body.ContentEditable and TabIndex
        HTMLDocument doc = HTMLDocument.current();
        HTMLBodyElement body = doc.getBody();
        body.setContentEditable(aValue);

        // Focus element
        body.focus();
    }
}
