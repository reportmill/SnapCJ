package snapcj;
import cjdom.*;
import snap.util.ArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This Process subclass tries to implement Process for CheerpJ.
 */
public class CJProcess extends Process {

    // The main class name
    private String _mainClassName;

    // The class path
    private String _classPath;

    // Whether to use CJDom
    private boolean _useCJDom;

    // The iframe that holds new process
    private HTMLIFrameElement _iframe;

    // The div element that holds the console
    private HTMLDivElement _consoleDiv;

    // The input stream
    private ReadWriteInputStream _inputStream;

    // The error stream
    private ReadWriteInputStream _errorStream;

    /**
     * Constructor.
     */
    public CJProcess(String[] args)
    {
        super();

        // Get main class and class path
        int cpArgIndex = ArrayUtils.indexOf(args, "-cp");
        _classPath = args[cpArgIndex + 1];
        _mainClassName = args[cpArgIndex + 2];

        // If UseCJDom, add CJDom and SnapCJ
        _useCJDom = true;
        if (_useCJDom)
            _classPath = "/app/CJDom-2024.01.jar:/app/SnapCJ-2024.01.jar:" + _classPath;

        System.out.println("MainClass: " + _mainClassName);
        System.out.println("ClassPath: " + _classPath);

        execProcess(args);
    }

    /**
     * Executes a process.
     */
    public void execProcess(String[] args)
    {
        // Create Standard out/err input streams
        _inputStream = new ReadWriteInputStream();
        //_errorStream = new ReadWriteInputStream();

        // Create IFrame with source to launcher.html (just has script for cjloader in header)
        HTMLDocument doc = HTMLDocument.getDocument();
        _iframe = (HTMLIFrameElement) doc.createElement("iframe");
        _iframe.setSrc("launcher.html");
        _iframe.getStyle().setCssText("position: absolute; right: 36px; top: 30%; width: 50%; height: 60%; background-color: white; box-sizing: border-box; z-index: 0; box-shadow: grey 1px 1px 8px; ");

        // Add to doc body
        HTMLBodyElement body = doc.getBody();
        body.appendChild(_iframe);

        // Listen for iframe src load
        _iframe.addEventListener("load", e -> didFinishIFrameLoad());
    }

    /**
     * Called after iframe src is loaded.
     */
    private void didFinishIFrameLoad()
    {
        HTMLDocument iframeDoc = _iframe.getContentDocument();
        HTMLHtmlElement iframeHtml = iframeDoc.getDocumentElement();

        // Add SwingParent div
        if (!_useCJDom) {
            HTMLDivElement swingParentDiv = (HTMLDivElement) iframeDoc.createElement("div");
            swingParentDiv.setId("SwingParent");
            swingParentDiv.getStyle().setCssText("margin: 0; width: 100%; height: 100%;");
            HTMLBodyElement body = iframeDoc.getBody();
            body.appendChild(swingParentDiv);
        }

        // Create and add a console div
        _consoleDiv = (HTMLDivElement) iframeDoc.createElement("div");
        _consoleDiv.setId("console");
        _consoleDiv.getStyle().setProperty("display", "none");
        HTMLBodyElement body = iframeDoc.getBody();
        body.appendChild(_consoleDiv);

        // Register for mutations
        MutationObserver mutationObserver = new MutationObserver(this::handleConsoleDivChanges);
        mutationObserver.observe(_consoleDiv, MutationObserver.Option.childList);

        // If using CJDom, add cjdom.js
        if (_useCJDom) {
            HTMLScriptElement cjdomScript = (HTMLScriptElement) iframeDoc.createElement("script");
            cjdomScript.setSrc("cjdom.js");
            iframeHtml.appendChild(cjdomScript);

            // Listen for load then add main script, otherwise they will load at same time instead of in order
            cjdomScript.addEventListener("load", e -> addMainScript());
        }

        // Otherwise just add script
        else addMainScript();
    }

    /**
     * Adds the main script.
     */
    private void addMainScript()
    {
        // Create script to run main for new class and class path
        HTMLDocument iframeDoc = _iframe.getContentDocument();
        HTMLScriptElement mainScript = (HTMLScriptElement) iframeDoc.createElement("script");
        String scriptText = getScriptText();
        mainScript.setText(scriptText);

        // Add script
        HTMLHtmlElement iframeHtml = iframeDoc.getDocumentElement();
        iframeHtml.appendChild(mainScript);
    }

    /**
     * Returns the script text.
     */
    private String getScriptText()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("  async function myInit() {\n");
        if (_useCJDom)
            sb.append("    await cheerpjInit({ natives: cjdomNativeMethods });\n");
        else {
            sb.append("    await cheerpjInit();\n");
            sb.append("    cheerpjCreateDisplay(-1, -1, document.getElementById('SwingParent'));\n");
        }
        sb.append("    await cheerpjRunMain('" + _mainClassName + "', '" + _classPath + "');\n");
        sb.append("  }\n");
        sb.append("  myInit();\n");

        // Return
        return sb.toString();
    }

    /**
     * Override to remove iframe from parent.
     */
    @Override
    public void destroy()
    {
        if (_iframe == null) return;

        // Remove iframe from parent
        Node parentNode = _iframe.getParentNode();
        if (parentNode != null)
            parentNode.removeChild(_iframe);
        _iframe = null;
    }

    @Override
    public OutputStream getOutputStream()  { return null; }

    @Override
    public InputStream getInputStream()  { return _inputStream; }

    @Override
    public InputStream getErrorStream()  { return _errorStream; }

    @Override
    public int waitFor() { return 0; }

    @Override
    public int exitValue()  { return 0; }

    /**
     * Called to handle changes to console div.
     */
    private void handleConsoleDivChanges(MutationRecord[] mutationRecords)
    {
        String consoleDivText = _consoleDiv.getInnerText();
        if (consoleDivText.length() > _inputStream._writeBytesLength) {
            String newText = consoleDivText.substring(_inputStream._writeBytesLength);
            _inputStream.addString(newText);
        }

        // Iterate over mutation records
        /*for (MutationRecord mutationRecord : mutationRecords) {
            Node[] addedNodes = mutationRecord.getAddedNodes();
            for (Node addedNode : addedNodes) {
                if (addedNode instanceof Text) { Text text = (Text) addedNode;
                    _inputStream.addString(text.getData());
                }
            }
        }*/
    }

    /**
     * An InputStream that lets you add bytes on the fly.
     */
    private static class ReadWriteInputStream extends InputStream {

        // The byte array to write to
        private byte[] _writeBytesBuffer = new byte[0];

        // The byte array to read from
        private byte[] _readBytesBuffer = new byte[1];

        // The index of the next character to read
        private int _readBytesIndex;

        // The currently marked position
        private int _markedIndex;

        // The number of bytes write bytes.
        private int _writeBytesLength;

        // Whether waiting for more input
        private boolean  _waiting;

        /** Constructor */
        public ReadWriteInputStream()
        {
            super();
        }

        /** Adds string to stream. */
        public void addString(String aStr)
        {
            byte[] bytes = aStr.getBytes();
            addBytes(bytes);
        }

        /** Adds bytes to stream. */
        public void addBytes(byte[] addBytes)
        {
            // Add new bytes to write buffer
            int oldLength = _writeBytesBuffer.length;
            _writeBytesBuffer = Arrays.copyOf(_writeBytesBuffer, oldLength + addBytes.length);
            System.arraycopy(addBytes, 0, _writeBytesBuffer, oldLength, addBytes.length);
            _writeBytesLength = _writeBytesBuffer.length;

            // If waiting, wake up
            if (_waiting) {
                synchronized (this) {
                    try { notifyAll(); _waiting = false; }
                    catch(Exception e) { throw new RuntimeException(e); }
                }
            }
        }

        /** Reads the next byte of data from this input stream. */
        @Override
        public int read()
        {
            int len = read(_readBytesBuffer, 0, 1);
            return len > 0 ? _readBytesBuffer[0] : -1;
        }

        /** Reads up to <code>len</code> bytes of data into an array of bytes from this input stream. */
        @Override
        public int read(byte[] theBytes, int offset, int length)
        {
            while (_readBytesIndex >= _writeBytesLength) {
                synchronized (this) {
                    try { _waiting = true; wait(); }
                    catch(Exception ignore) { }
                }
            }

            int availableBytesCount = _writeBytesLength - _readBytesIndex;
            if (length > availableBytesCount)
                length = availableBytesCount;
            if (length <= 0)
                return 0;
            System.arraycopy(_writeBytesBuffer, _readBytesIndex, theBytes, offset, length);
            _readBytesIndex += length;
            return length;
        }

        /** Skips <code>n</code> bytes of input from this input stream. */
        @Override
        public synchronized long skip(long n)
        {
            long k = _writeBytesLength - _readBytesIndex;
            if (n < k) {
                k = n < 0 ? 0 : n;
            }
            _readBytesIndex += k;
            return k;
        }

        /** Returns the number of remaining bytes that can be read (or skipped over) from this input stream. */
        public synchronized int available() { return _writeBytesLength - _readBytesIndex; }

        /** Tests if this <code>InputStream</code> supports mark/reset. */
        public boolean markSupported() { return true; }

        /** Set the current marked position in the stream. */
        public void mark(int readAheadLimit) { _markedIndex = _readBytesIndex; }

        /** Resets the buffer to the marked position. */
        public synchronized void reset() { _readBytesIndex = _markedIndex; }

        /** Closing a <tt>BytesArrayInputStream</tt> has no effect. */
        public void close() throws IOException  { }
    }
}
