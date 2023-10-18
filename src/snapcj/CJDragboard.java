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

    // Whether dragging is in progress
    public static boolean isDragging;

    // The shared clipboard for system drag/drop
    private static CJDragboard  _sharedDrag;

    /**
     * Starts the drag.
     */
    public void startDrag()
    {
        // Set Dragging true and consume event
        isDragging = true;
        _viewEvent.consume();

        // Get drag image
        Image dragImage = getDragImage();
        if (dragImage == null)
            dragImage = Image.getImageForSize(1,1,true);

        // Get native HTML element for image
        HTMLElement img = (HTMLElement) dragImage.getNative();
        double dx = getDragImageOffset().x;
        double dy = getDragImageOffset().y;

        // Start Drag
        _dataTrans.setDragImage(img, dx, dy);

        // Add image element to screenDiv so browsers can generate image
        HTMLElement screenDiv = CJScreen.getScreenDiv();
        screenDiv.appendChild(img);

        // Register to remove element a short time later
        ViewUtils.runDelayed(() -> {
            isDragging = false;
            screenDiv.removeChild(img);
        }, 100);
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
        DragEvent dragEvent = (DragEvent) anEvent.getEvent();
        _dataTrans = dragEvent.getDataTransfer();
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
