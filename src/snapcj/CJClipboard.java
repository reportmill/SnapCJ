package snapcj;
import cjdom.*;
import snap.view.Clipboard;
import snap.view.ClipboardData;
import snap.view.ViewUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A snap Clipboard implementation for TeaVM.
 */
public class CJClipboard extends Clipboard {
    
    // The DataTransfer
    protected DataTransfer _dataTrans;

    // The runnable to call addAllDatas()
    private Runnable  _addAllDatasRun, ADD_ALL_DATAS_RUN = () -> addAllDataToClipboard();

    // Whether clipboard is loaded
    private boolean  _loaded;

    // A LoadListener to handle async browser clipboard
    private static Runnable  _loadListener;

    // The shared clipboard for system copy/paste
    private static CJClipboard  _shared;

    /**
     * Returns the clipboard content.
     */
    protected boolean hasDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.hasDataImpl(aMimeType);

        // Handle FILE_LIST: Return true if at least one file
        if (aMimeType == FILE_LIST)
            return _dataTrans.getFileCount() > 0;

        // Forward to DataTrans
        return _dataTrans.hasType(aMimeType);
    }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMimeType)
    {
        // If no DataTransfer, just return normal version
        if (_dataTrans == null)
            return super.getDataImpl(aMimeType);

        // Handle Files
        if (aMimeType == FILE_LIST) {

            // Get files
            File[] jsfiles = _dataTrans.getFiles();
            if (jsfiles == null)
                return null;

            // Iterate over jsFiles and create clipbard data
            List<ClipboardData> cfiles = new ArrayList<>(jsfiles.length);
            for (File jsfile : jsfiles) {
                ClipboardData cbfile = new CJClipboardData(jsfile);
                cfiles.add(cbfile);
            }

            // Return ClipboardData for files array
            return new ClipboardData(aMimeType, cfiles);
        }

        // Handle anything else (String data)
        Object data = _dataTrans.getData(aMimeType);
        return new ClipboardData(aMimeType, data);
    }

    /**
     * Adds clipboard content.
     */
    protected void addDataImpl(String aMimeType, ClipboardData aData)
    {
        // Do normal implementation to populate ClipboardDatas map
        super.addDataImpl(aMimeType, aData);

        // Handle DragDrop case
        if (_dataTrans != null) {

            // Handle string data
            if (aData.isString())
                _dataTrans.setData(aMimeType, aData.getString());

                // Otherwise complain
            else System.err.println("CJClipboard.addDataImpl: Unsupported data type: " + aMimeType + ", " + aData.getSource());
        }

        // Handle system clipboard copy: Wait till all types added, then update clipboard
        else {
            if (_addAllDatasRun == null)
                ViewUtils.runLater(_addAllDatasRun = ADD_ALL_DATAS_RUN);
        }
    }

    /**
     * Load datas into system clipboard
     */
    private void addAllDataToClipboard()
    {
        // Clear run
        _addAllDatasRun = null;

        // Get list of ClipbardData
        Map<String,ClipboardData> clipDataMap = getClipboardDatas();
        Collection<ClipboardData> clipDataList = clipDataMap.values();

        // Convert to list of ClipboardItem
        List<ClipboardItem> clipItemsList = new ArrayList<>();
        for (ClipboardData clipboardData : clipDataList) {
            ClipboardItem clipboardItem = getClipboardItemForClipboardData(clipboardData);
            if (clipboardItem != null)
                clipItemsList.add(clipboardItem);
        }

        // Convert to JSArray of ClipboardItem
        ClipboardItem[] clipItems = clipItemsList.toArray(new ClipboardItem[0]);
        Array<ClipboardItem> clipItemsJS = new Array(clipItems);

        // Write to system clipboard
        Promise<?> writePromise = null;
        try {
            writePromise = cjdom.Clipboard.getClipboardWriteItemsPromise(clipItemsJS);
        }
        catch (Exception e) {
            System.err.println("CJClipboard.addAllDataToClipboard: Failed to do navigator.clipboard.write");
        }

        // Handle/configure promise
        if (writePromise != null) {

            // On success, log
            writePromise.then(aJSObj -> {
                System.out.println("CJClipboard.write: Successfully did copy");
                return null;
            });

            // On failure, complain and return
            writePromise.catch_(aJSObj -> {
                System.err.println("CJClipboard.addAllDataToClipboard failed:");
                CJDom.log(aJSObj);
                return null;
            });
        }

        // Clear datas
        clearData();
    }

    /**
     * Returns a ClipboardItem for given ClipboardData.
     */
    private ClipboardItem getClipboardItemForClipboardData(ClipboardData aData)
    {
        // Handle image
        if (aData.isImage()) {

            // Get image as PNG blob
            CJImage image = (CJImage) aData.getImage();
            byte[] bytes = image.getBytesPNG();
            Blob blob = new Blob(bytes, "image/png");

            // Get ClipboardItem array for blob
            return new ClipboardItem(blob);
        }

        // Handle anything else
        else {

            // Get type and bytes
            String type = aData.getMIMEType();
            byte[] bytes = aData.getBytes();

            // If valid, just wrap in ClipboardItem
            if (type != null && bytes != null && bytes.length > 0) {
                Blob blob = new Blob(bytes, type);
                return new ClipboardItem(blob);
            }
        }

        // Complain and return null
        System.err.println("CJClipboard.getClipboardItemForClipboardData: Had problem with " + aData);
        return null;
    }

    /**
     * Starts the drag.
     */
    public void startDrag()  { System.err.println("CJClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is accepted. */
    public void acceptDrag()  { System.err.println("CJClipboard.startDrag: Not implemented"); }

    /** Called to indicate that drop is complete. */
    public void dropComplete()  { System.err.println("CJClipboard.startDrag: Not implemented");  }

    /**
     * Override to clear DataTrans.
     */
    @Override
    public void clearData()
    {
        super.clearData();
        _dataTrans = null;
    }

    /**
     * Returns the shared CJClipboard.
     */
    public static CJClipboard get()
    {
        if (_shared != null) return _shared;
        return _shared = new CJClipboard();
    }

    /**
     * Returns whether clipboard is loaded.
     */
    public boolean isLoaded()  { return _loaded; }

    /**
     * Adds a callback to be triggered when resources loaded.
     * If Clipboard needs to be 'approved', get approved and call given load listener.
     */
    public void addLoadListener(Runnable aRun)
    {
        // Set LoadListener
        _loadListener = aRun;

        // Get PermissionsPromise
        Promise<Object> permissionsPromise = cjdom.Clipboard.getReadPermissionsPromise();

        // If returned, handle
        if (permissionsPromise != null) {

            // On success, forward to didGetClipboardReadText()
            permissionsPromise.then(perm -> didGetPermissions(perm));

            // On failure, complain and return
            permissionsPromise.catch_(anObjJS -> {

                // Complain
                System.err.println("CJClipboard.addLoadListener: failed:");
                CJDom.log(anObjJS);

                // Actually, try anyway - works on Safari
                didGetPermissions(null);
                return null;
            });
        }

        // Otherwise, return
        else {
            System.out.println("CJClipboard.addLoadListener: No read permissions promise?");
            didGetPermissions(null);
        }
    }

    /**
     * Returns a readText promise
     */
    private static Promise<String> didGetPermissions(Object aPermResult)
    {
        // Print result of permissions
        if (aPermResult != null) {
            String state = cjdom.Clipboard.getPermissionStatusState(aPermResult);
            System.out.println("CJClipboard.didGetPermissions: Got Read Permissions: " + state);
        }

        // Get readText promise to call didGetClipboardReadText
        Promise<String> readTextPromise = cjdom.Clipboard.getClipboardReadTextPromise();

        // On success, forward to didGetClipboardReadText()
        readTextPromise.then(str -> didGetClipboardReadText(str));

        // On failure, complain and return
        readTextPromise.catch_(aJSO -> {
            System.err.println("CJClipboard.didGetPermissions: failed:");
            CJDom.log(aJSO);
            return null;
        });

        // Return promise
        return readTextPromise;
    }

    /**
     * Returns the system DataTransfer.
     */
    private static Promise<String> didGetClipboardReadText(String str)
    {
        // Log string
        String msg = str.replace("\n", "\\n");
        if (str.length() > 50) msg = str.substring(0, 50) + "...";
        System.out.println("CJClipboard.didGetClipboardReadText: Read clipboard string: " + msg);

        // Create/set DataTransfer for string
        _shared._dataTrans = DataTransfer.getDataTrasferForString(str);

        // Trigger LoadListener
        _shared.notifyLoaded();
        return null;
    }

    /**
     * Notify loaded.
     */
    private void notifyLoaded()
    {
        ViewUtils.runLater(() -> {
            _loaded = true;
            _loadListener.run();
            _loaded = false;
            _loadListener = null;
        });
    }
}