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
    System.out.println("Start Test");
    CJ.set();
    System.out.println("CJ.set()");
    Test test = new Test();
    System.out.println("new Test()");
    test.getUI().setFill(snap.gfx.Color.WHITE);
    System.out.println("test.getUI()");
    test.setWindowVisible(true);
    System.out.println("setWindowVisible(): " + test.getWindow().getSize());
}

}