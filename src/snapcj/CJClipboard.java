package snapcj;
import cjdom.Element;
import java.util.ArrayList;
import java.util.List;
import snap.gfx.Image;
import snap.view.*;
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
protected boolean hasDataImpl(String aMimeType)
{
    if(aMimeType==FILE_LIST)
        return _dataTrans.getFiles().length>0;
    return _dataTrans.hasType(aMimeType);
}

/**
 * Returns the clipboard content.
 */
protected ClipboardData getDataImpl(String aMimeType)
{
    Object data = null;
    
    // Handle Files
    if(aMimeType==FILE_LIST) {
        cjdom.File cjfiles[] = _dataTrans.getFiles(); if(cjfiles==null) return null;
        List <ClipboardData> cfiles = new ArrayList(cjfiles.length);
        for(cjdom.File cjfile : cjfiles) {
            String type = cjfile.getType();
            byte bytes[] = cjfile.getBytes();
            ClipboardData cbfile = new ClipboardData(cjfile.getType(), cjfile.getBytes());
            cfiles.add(cbfile);
        }
        data = cfiles;
    }
        
    // Handle anything else (String data)
    else data = _dataTrans.getData(aMimeType);
    
    // Return ClipboardData for data
    return new ClipboardData(aMimeType, data);
}

/**
 * Adds clipboard content.
 */
protected void addDataImpl(String aMimeType, ClipboardData aData)
{
    // Do normal implementation to populate ClipboardDatas map
    super.addDataImpl(aMimeType, aData);
    
    // Handle string data
    if(aData.isString())
        _dataTrans.setData(aMimeType, aData.getString());
        
    // Otherwise complain
    else System.err.println("CJClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
}

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

/** Called to indicate that drop is accepted. */
public void acceptDrag()  { }

/** Called to indicate that drop is complete. */
public void dropComplete()  { }

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