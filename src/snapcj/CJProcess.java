package snapcj;
import cjdom.*;
import snap.util.ArrayUtils;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This Process subclass tries to implement Process for CheerpJ.
 */
public class CJProcess extends Process {

    // The main class name
    private String _mainClassName;

    // The class path
    private String _classPath;

    // The iframe that holds new process
    private HTMLIFrameElement _iframe;

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
        System.out.println("java -cp " + _classPath + ' ' + _mainClassName);

        execProcess(args);
    }

    /**
     * Executes a process.
     */
    public void execProcess(String[] args)
    {
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
        // Create script to run main for new class and class path
        HTMLDocument iframeDoc = _iframe.getContentDocument();
        HTMLScriptElement mainScript = (HTMLScriptElement) iframeDoc.createElement("script");
        String scriptText =
            "  async function myInit() {\n" +
            "    await cheerpjInit();\n" +
            "    cheerpjCreateDisplay(-1, -1, document.getElementById('SwingParent'));\n" +
            "    await cheerpjRunMain('" + _mainClassName + "', '" + _classPath + "');\n" +
            "  }\n" +
            "  myInit();\n";
        mainScript.setText(scriptText);

        // Add script to iframe doc element
        HTMLHtmlElement iframeHtml = iframeDoc.getDocumentElement();
        iframeHtml.appendChild(mainScript);
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
    public InputStream getInputStream()  { return null; }

    @Override
    public InputStream getErrorStream()  { return null; }

    @Override
    public int waitFor() { return 0; }

    @Override
    public int exitValue()  { return 0; }
}
