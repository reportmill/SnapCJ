package snapcj;
import snap.view.*;

/**
 * A custom class.
 */
public class Test extends ViewOwner {


protected View createUI()
{
    Button btn = new Button("Hello World"); btn.setPrefSize(100,25);
    Box box = new Box(btn); box.setPrefSize(320,320);
    return box;
}

public static void main(String args[])
{
    CJ.set();
    Test test = new Test();
    test.getUI().setFill(snap.gfx.Color.WHITE);
    test.setWindowVisible(true);
}

}