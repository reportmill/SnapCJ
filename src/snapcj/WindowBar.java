package snapcj;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.*;

/**
 * A View to paint a window bar.
 */
public class WindowBar extends ParentView {

    // The content
    private View  _content;
    
    // The title bar height
    private double _barHeight;
    
    // The buttons
    private Shape  _closeButton, _minButton, _maxButton;
    
    // The title bar font
    private Font  _font;
    
    // For dragging
    private Point  _mousePoint;
    
    // Colors
    private static final Color CLOSE_COLOR = new Color("#ED6B5F");
    private static final Color CLOSE_COLOR2 = CLOSE_COLOR.blend(Color.BLACK,.2);
    private static final Color MIN_COLOR = new Color("#F5BF4F");
    private static final Color MIN_COLOR2 = MIN_COLOR.blend(Color.BLACK,.2);
    private static final Color MAX_COLOR = new Color("#62C654");
    private static final Color MAX_COLOR2 = MAX_COLOR.blend(Color.BLACK,.2);
    
    /**
     * Creates a WindowBar.
     */
    public WindowBar(View aView)
    {
        WindowView win = aView.getWindow();
        double titleBarH = win.getType() == WindowView.TYPE_MAIN ? 24 : 18;
        setBarHeight(titleBarH);
        enableEvents(MousePress, MouseDrag, MouseRelease);
        setContent(aView);
    }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        _content = aView;
        addChild(_content);
    }

    /**
     * Returns the title bar height.
     */
    public double getBarHeight()  { return _barHeight; }

    /**
     * Sets the title bar height.
     */
    public void setBarHeight(double aValue)
    {
        _barHeight = aValue;
        setPadding(_barHeight,0,0,0);

        // Create buttons
        double buttonY = 6;
        double buttonW = 12;
        if (_barHeight != 24) {
            buttonY = 4; buttonW = 10;
        }

        // Create buttons
        _closeButton = new Arc(10, buttonY, buttonW, buttonW,0,360);
        _minButton = new Arc(30, buttonY, buttonW, buttonW,0,360);
        _maxButton = new Arc(50, buttonY, buttonW, buttonW,0,360);

        // Set font
        double fontSize = _barHeight == 24 ? 14 : 11;
        _font = Font.Arial10.copyForSize(fontSize);
    }

    /**
     * Override to paint titlebar.
     */
    protected void paintFront(Painter aPntr)
    {
        if (_barHeight == 0) return;

        // Paint title bar
        ButtonPainter buttonPainter = ViewTheme.getClassic().getButtonPainter();
        RoundRect buttonRect = new RoundRect(0, 0, getWidth(), _barHeight, 4).copyForPosition(Pos.TOP_CENTER);
        buttonPainter.paintButtonInShape(aPntr, buttonRect, 0, false);

        // Paint buttons
        aPntr.setStroke(Stroke.getStroke(.5));
        if (_closeButton != null) {
            aPntr.setColor(CLOSE_COLOR);
            aPntr.fill(_closeButton);
            aPntr.setColor(CLOSE_COLOR2);
            aPntr.draw(_closeButton);
        }
        if (_minButton != null) {
            aPntr.setColor(MIN_COLOR);
            aPntr.fill(_minButton);
            aPntr.setColor(MIN_COLOR2);
            aPntr.draw(_minButton);
        }
        if(_maxButton != null) {
            aPntr.setColor(MAX_COLOR);
            aPntr.fill(_maxButton);
            aPntr.setColor(MAX_COLOR2);
            aPntr.draw(_maxButton);
        }
        aPntr.setStroke(Stroke.getStroke(1));

        // Paint title
        String title = getWindow().getTitle();
        if (title != null) {
            double strY = _barHeight == 24 ? 4 : 3;
            double strW = _font.getStringAdvance(title);
            double strX = Math.round((getWidth() - strW) / 2);
            aPntr.setColor(Color.DARKGRAY);
            aPntr.setFont(_font);
            aPntr.drawString(title, strX, strY + _font.getAscent());
        }
    }

    /**
     * Override to handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        anEvent.consume();

        // Handle MousePress: Update MousePoint
        if (anEvent.isMousePress()) {
            _mousePoint = anEvent.getY() <= _barHeight ? anEvent.getPoint(null) : null;
            return;
        }

        // Handle MouseDrag
        if (anEvent.isMouseDrag())
            mouseDrag(anEvent);

        // Handle MouseRelease: Clear MousePoint
        if (anEvent.isMouseRelease())
            mouseRelease(anEvent);
    }

    /**
     * Called on MouseDrag event.
     */
    private void mouseDrag(ViewEvent anEvent)
    {
        // If Maximized, just return
        WindowView win = getWindow();
        if (win.isMaximized())
            return;

        // Update MousePoint
        Point mousePoint = _mousePoint;
        if (mousePoint == null) return;
        _mousePoint = anEvent.getPoint(null);

        // Update Window XY
        double x2 = win.getX() + (_mousePoint.x - mousePoint.x);
        double y2 = win.getY() + (_mousePoint.y - mousePoint.y);
        win.setXY(x2, y2);
    }

    /**
     * Called on MouseRelease event.
     */
    private void mouseRelease(ViewEvent anEvent)
    {
        // If click hit CloseButton, close window
        Point ppointt = anEvent.getPoint();
        if (_closeButton.contains(ppointt.x, ppointt.y)) {
            WindowView win = getWindow();
            win.setVisible(false);
        }

        _mousePoint = null;
    }

    /**
     * Override to return preferred width of content.
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, getContent(), aH);
    }

    /**
     * Override to return preferred height of content.
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, getContent(), aW);
    }

    /**
     * Override to layout content.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, getContent(), true, true);
    }

    /**
     * Attaches a WindowBar to given root view.
     * This is not great - should really probably be done in JavaScript and/or at WindowView level.
     */
    public static void attachWindowBar(View aView)
    {
        RootView rootView = aView.getRootView();
        View content = rootView.getContent();
        if (content instanceof WindowBar)
            return;

        // Get whether Window is currently at pref height
        WindowView window = rootView.getWindow();
        double windowH = window.getHeight();
        double windowPrefH = window.getPrefHeight();
        boolean windowAtPrefHeight = windowH == windowPrefH;

        // Create WindowBar
        WindowBar windowBar = new WindowBar(content);
        if (window.isMaximized())
            windowBar.setBarHeight(18);

        // Set WindowBar as RootView content
        rootView.setContent(windowBar);

        // If window was at PrefHeight, resize window for windowBar height
        if (windowAtPrefHeight)
            window.setHeight(windowH + windowBar.getBarHeight());

        // Register to repaint when Title changes
        window.addPropChangeListener(pc -> windowBar.repaint(), WindowView.Title_Prop);
    }

    /**
     * Detaches a WindowBar to a view.
     */
    public static void detachWindowBar(View aView)
    {
        RootView rvrootViewew = aView.getRootView();
        View content = rvrootViewew.getContent();
        WindowBar wbar = content instanceof WindowBar ? (WindowBar)content : null; if (wbar==null) return;
        rvrootViewew.setContent(wbar.getContent());
    }
}