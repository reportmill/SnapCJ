package snapcj;
import cjdom.*;
import snap.gfx.Image;
import snap.view.ViewEvent;
import snap.view.ViewUtils;

/**
 * A TVClipboard subclass to support drag and drop.
 */
public class CJDragboard extends CJClipboard {

    // The view event
    private ViewEvent  _viewEvent;

    // The shared clipboard for system drag/drop
    private static CJDragboard  _sharedDrag;

    /**
     * Starts the drag.
     */
    public void startDrag()
    {
        // Set Dragging true and consume event
        _viewEvent.consume();

        // Get drag image
        Image dragImage = getDragImage();
        if (dragImage == null)
            dragImage = Image.getImageForSize(1,1,true);

        // Get native HTML element for image (set style to hide when added on screen for drag)
        HTMLElement img = (HTMLElement) dragImage.getNative();
        double dx = getDragImageOffset().x;
        double dy = getDragImageOffset().y;
        img.getStyle().setProperty("position", "absolute");
        img.getStyle().setProperty("left", "-100%");

        // Start drag
        _dataTrans.startDrag(img, dx, dy);
    }

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
        if (anEvent.isDragGesture())
            _dataTrans = new DataTransfer();
        else {
            DragEvent dragEvent = (DragEvent) anEvent.getEvent();
            _dataTrans = dragEvent.getDataTransfer();
        }
    }

    /**
     * Returns the shared TVClipboard for drag and drop.
     */
    public static CJClipboard getDrag(ViewEvent anEvent)
    {
        if (_sharedDrag == null)
            _sharedDrag = new CJDragboard();
        if (anEvent != null)
            _sharedDrag.setEvent(anEvent);
        return _sharedDrag;
    }
}
