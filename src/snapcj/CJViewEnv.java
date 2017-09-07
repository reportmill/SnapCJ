package snapcj;
import java.util.*;
import cjdom.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A ViewEnv implementation for TeaVM.
 */
public class CJViewEnv extends ViewEnv {
    
    // The clipboard
    //SwingClipboard       _clipboard;
    
    // The timer for runIntervals and runDelayed
    //java.util.Timer           _timer = new java.util.Timer();
    
    // A map of timer tasks
    //Map <Runnable,TimerTask>  _timerTasks = new HashMap();
    Map <Runnable,Integer>  _intervalIds = new HashMap();

    // List of run later runnables
    static List <Runnable>    _runLaters = new ArrayList();
    
    // A shared instance.
    static CJViewEnv          _shared = new CJViewEnv();

/**
 * Returns whether current thread is event thread.
 */
public boolean isEventThread()  { return true; }

/**
 * Run later.
 */
public void runLater(Runnable aRunnable)
{
    _runLaters.add(aRunnable);
    if(_runLaters.size()==1)
        Window.setTimeout(() -> sendEvents(), 10);
}

void sendEvents()
{
    while(_runLaters.size()>0) {
        Runnable run = _runLaters.remove(0);
        run.run();
    }
}

/**
 * Runs given runnable after delay.
 */
public void runDelayed(Runnable aRun, int aDelay, boolean inAppThread)
{
    Window.setTimeout(aRun, aDelay);
    //TimerTask task = new TimerTask() { public void run() { if(inAppThread) runLater(aRun); else aRun.run(); }};
    //_timer.schedule(task, aDelay);
}

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public void runIntervals(Runnable aRun, int aPeriod, int aDelay, boolean doAll, boolean inAppThread)
{
    int id = Window.setInterval(aRun, aPeriod);
    _intervalIds.put(aRun, id);
    //TimerTask task = new TimerTask() { public void run()  { aRun.run(); }}; //if(inAppThread) runLaterAndWait(aRun);else 
    //_timerTasks.put(aRun, task);
    //if(doAll) _timer.scheduleAtFixedRate(task, aDelay, aPeriod); else
    //_timer.schedule(task, aDelay, aPeriod); // Why is this running fast?
}

/**
 * Runs given runnable for given period after given delay with option to run once for every interval, even under load.
 */
public void stopIntervals(Runnable aRun)
{
    Integer id = _intervalIds.get(aRun);
    if(id!=null)
        Window.clearInterval(id);
    //TimerTask task = _timerTasks.get(aRun);
    //if(task!=null) task.cancel();
}

/**
 * Returns the system clipboard.
 */
public Clipboard getClipboard()  { return null; } //_clipboard!=null? _clipboard : (_clipboard=SwingClipboard.get()); }

/**
 * Returns a FileChooser.
 */
public FileChooser getFileChooser()  { return null; }//new SwingFileChooser(); }

/**
 * Returns a property for given view.
 */
public Object getProp(Object anObj, String aKey)  { return super.getProp(anObj, aKey); }

/**
 * Sets a property for a given native.
 */
public void setProp(Object anObj, String aKey, Object aValue)  { super.setProp(anObj, aKey, aValue); }

/**
 * Creates the top level properties map.
 */
protected Map createPropsMap()  { return new HashMap(); }

/**
 * Returns a new ViewHelper for given native component.
 */
public ViewHelper createHelper(View aView)
{
    if(aView instanceof RootView) return new CJRootViewHpr();
    if(aView instanceof WindowView) return new CJWindowHpr();
    return null;
}

/**
 * Creates an event for a UI view.
 */
public ViewEvent createEvent(View aView, Object anEvent, ViewEvent.Type aType, String aName)
{
    Event eobj = (Event)anEvent;
    if(eobj==null && aType==null) aType = View.Action; //eobj=new ActionEvent(this,ACTION_PERFORMED,"DefAct")
    
    // Create event, configure and send
    ViewEvent event = new CJEvent(); event.setView(aView); event.setEvent(eobj); event.setType(aType);
    event.setName(aName!=null? aName : aView!=null? aView.getName() : null);
    return event;
}

/**
 * Returns the screen bounds inset to usable area.
 */
public Rect getScreenBoundsInset()  { return CJScreen.get().getBounds(); }
    
/**
 * Returns a shared instance.
 */
public static CJViewEnv get()  { return _shared; }

/**
 * Sets TVViewEnv as the ViewEnv.
 */
public static void set()  { snap.gfx.GFXEnv.setEnv(CJEnv.get()); ViewEnv.setEnv(get()); }

/**
 * A ViewHelper for RootView + CJRootView.
 */
public static class CJRootViewHpr <T extends CJRootView> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new CJRootView(); }

    /** Override to set view in RootView. */
    public void setView(View aView)  { super.setView(aView); get().setView(aView); }
    
    /** Sets the cursor. */
    public void setCursor(Cursor aCursor)  { get().setCursor(aCursor); }
    
    /** Registers a view for repaint. */
    public void requestPaint(Rect aRect)  { get().repaint(aRect); }
}

/**
 * A ViewHelper for WindowView + TVWindow.
 */
public static class CJWindowHpr <T extends CJWindow> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new CJWindow(); }
    
    /** Override to get view as WindowView. */
    public WindowView getView()  { return (WindowView)super.getView(); }
        
    /** Override to set view in RootView. */
    public void setView(View aView)  { super.setView(aView); get().setView((WindowView)aView); }
        
    /** Window/Popup method: Shows the window at given point relative to given view. */
    public void show(View aView, double aX, double aY)  { get().show(aView, aX, aY); }
    
    /** Window/Popup method: Hides the window. */
    public void hide()  { get().hide(); }
}

}