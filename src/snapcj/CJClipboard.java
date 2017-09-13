package snapcj;
import cjdom.Element;
import java.util.ArrayList;
import java.util.List;
import snap.gfx.Image;
import snap.view.Clipboard;
import snap.view.View;
import snap.view.ViewEvent;
import cjdom.DragEvent;
import cjdom.DataTransfer;

/**
 * A snap Clipboard implementation for CheerpJ.
 */
public class CJClipboard extends Clipboard {
    
    // The view to initiate drag
    View             _view;
    
    // The view event
    ViewEvent        _viewEvent;
    
    // The DragEvent
    DragEvent        _dragEvent;
    
    // The DataTransfer
    DataTransfer     _dataTrans;
    
    // The shared clipboards for system and drag
    static CJClipboard  _shared = new CJClipboard();
    static CJClipboard  _sharedDrag = new CJClipboard();

/**
 * Returns the clipboard content.
 */
public boolean hasContent(String aName)
{
    // Handle String
    if(aName==STRING)
        return _dataTrans.hasType("text/plain");

    // Handle Files
    if(aName==FILES)
        return _dataTrans.getFiles().length>0;

    // Handle anything else
    return _dataTrans.hasType(aName);
}

/**
 * Returns the clipboard content.
 */
public Object getContent(String aName)
{
    // Handle String
    if(aName==STRING)
        return _dataTrans.getData("text/plain");
        
    // Handle Files
    if(aName==FILES) {
        List files = new ArrayList();
        return files;
    }
        
    // Handle anything else
    return _dataTrans.getData(aName);
}

/**
 * Sets the clipboard content.
 */
public void setContent(String aName, Object theData)
{
    // Handle String
    if(aName==STRING)
        _dataTrans.setData("text/plain", (String)theData);
}

/**
 * Sets the clipboard content.
 */
public void setContent(Object ... theContents)
{
    // If contents only one object, map to key
    if(theContents.length==1) {
        if(theContents[0] instanceof String) theContents = new Object[] { STRING, theContents[0] };
        //else if(theContents[0] instanceof java.io.File)
        //    theContents = new Object[] { FILES, Arrays.asList(theContents[0]) };
        else if(theContents[0] instanceof List) theContents = new Object[] { FILES, theContents[0] };
        //else if(theContents[0] instanceof Image) theContents = new Object[] { IMAGE, theContents[0] };
        //else if(theContents[0] instanceof Color) theContents = new Object[] { COLOR, theContents[0] };
    }

    // Set contents    
    for(int i=0;i<theContents.length;) {
        String name = (String)theContents[i++];
        Object data = theContents[i++];
        setContent(name, data);
    }
}

/**
 * Returns the data transfer.
 */
protected DataTransfer getDataTransfer()  { return _dataTrans; }

/**
 * Starts the drag.
 */
public void startDrag()
{
    // Set Dragging true and consume event
    isDragging = true;
    _viewEvent.consume();
    
    // Get DragSource and start Listening to drag events drag source
    //DragSource dragSource = _dge.getDragSource();
    //dragSource.removeDragSourceListener(this); dragSource.removeDragSourceMotionListener(this);
    //dragSource.addDragSourceListener(this); dragSource.addDragSourceMotionListener(this);
    
    // Check to see if image drag is supported by system. If not (ie, Windows), simulate image dragging with a window.
    //if(getDragImage()!=null && !DragSource.isDragImageSupported()) createDragWindow();

    // Get drag image and point and set in DataTransfer
    Image dimg = getDragImage(); if(dimg==null) dimg = Image.get(1,1,true);
    Element img = (Element)dimg.getNative();
    double dx = getDragImageOffset().x;
    double dy = getDragImageOffset().y;
    _dataTrans.setDragImage(img, dx, dy);
        
    // Add image element to canvas so browsers can generate image (then remove a short time later)
    cjdom.Element body = cjdom.Document.current().getBody();
    body.appendChild(img);
    CJViewEnv.get().runDelayed(() -> { isDragging = false; body.removeChild(img); }, 100, false);
}

public static boolean isDragging;

/**
 * Sets the current event.
 */
protected void setEvent(ViewEvent anEvent)
{
    _viewEvent = anEvent;
    _dragEvent = (DragEvent)anEvent.getEvent();
    _dataTrans = _dragEvent.getDataTransfer();
}

/**
 * Returns the shared SwingClipboard.
 */
public static CJClipboard get()  { return _shared; }

/**
 * Returns the shared SwingClipboard for drag and drop.
 */
public static CJClipboard getDrag(ViewEvent anEvent)
{
    if(anEvent!=null) _sharedDrag.setEvent(anEvent);
    return _sharedDrag;
}

}