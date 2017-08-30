package snapcj;
import java.text.DecimalFormat;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A simple test view that animates a bunch of silly drawing and has a Start button, time slider and time label.
 */
public class BusyBox extends ViewOwner {
    
    // Border
    public static final Border VIEW_BORDER = Border.createLineBorder(Color.WHITE, 5);
    public final Font VIEW_FONT = new Font("Arial", 14);

/**
 * Creates a new Test view.
 */
public View createUI()
{
    ListView lview = new ListView(); lview.setName("ListView"); lview.setPrefWidth(200); lview.setPrefHeight(500);
    lview.setFont(new Font("Arial", 15)); lview.setRowHeight(30);
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,40,20); vbox.setPrefWidth(550);
    vbox.setFill(ViewUtils.getBackFill()); vbox.setSpacing(30); vbox.setName("VBox");
    List items = new ArrayList();
    
    items.add("Buttons"); vbox.addChild(createButtonsView());
    items.add("Toggle Buttons"); vbox.addChild(createToggleButtonsView());
    items.add("CheckBoxes"); vbox.addChild(createCheckBoxsView());
    items.add("RadioButtons"); vbox.addChild(createRadioButtonsView());
    items.add("Sliders"); vbox.addChild(createSlidersView());
    items.add("TextFields"); vbox.addChild(createTextFieldsView());
    items.add("ProgressBar"); vbox.addChild(createProgressBarView());
    items.add("Spinner"); vbox.addChild(createSpinnerView());
    items.add("TabView"); vbox.addChild(createTabView());

    lview.setItems(items); lview.setSelectedIndex(0);
    ScrollView sview1 = new ScrollView(lview);
    ScrollView sview2 = new ScrollView(vbox); sview2.setGrowWidth(true); sview2.setGrowHeight(true);
    HBox top = new HBox(); top.addChild(new Label("SnapKit BusyBox - with CheerpJ")); top.setPrefHeight(40); top.setPadding(4,4,4,14);
    Color c1 = new Color("#DDDDDD"), c2 = new Color("#EEEEEE"), c3 = new Color("#CCCCCC");
    GradientPaint.Stop stops[] = GradientPaint.getStops(0, c1, .5, c2, 1, c3);
    GradientPaint gpaint = new GradientPaint(90, stops); top.setFill(gpaint); top.setFont(new Font("Arial-Bold",25));
    top.setBorder(Border.createLineBorder(Color.DARKGRAY, 1));
    
    BorderView bview = new BorderView(); bview.setPrefSize(800,600);
    bview.setCenter(sview2); bview.setLeft(sview1); bview.setTop(top);
    return bview;
}

/**
 * Creates Buttons view.
 */
public View createButtonsView()
{
    Label label = new Label("Buttons");
    Button btn1 = new Button("Button One");
    Button btn2 = new Button("Button Two");
    Button btn3 = new Button("Button Three");
    HBox hbox = new HBox(); hbox.setAlign(Pos.CENTER); hbox.setSpacing(20);
    hbox.setChildren(btn1, btn2, btn3);
    
    Button btn4 = new Button("Rotate");
    btn4.addEventHandler(e -> btn4.getAnimCleared(800).setRotate(360).getAnim(1600).setRotate(0).play(), Action);
    Button btn5 = new Button("Scale");
    btn5.addEventHandler(e -> btn5.getAnimCleared(800).setScale(2).getAnim(1600).setScale(1).play(), Action);
    Button btn6 = new Button("Translate");
    btn6.addEventHandler(e -> btn6.getAnimCleared(800).setTransX(-150).getAnim(1600).setTransX(0).play(), Action);
    HBox hbox2 = new HBox(); hbox2.setAlign(Pos.CENTER); hbox2.setSpacing(20);
    hbox2.setChildren(btn4, btn5, btn6);
    
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(30);
    vbox.setChildren(label, hbox, hbox2); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates ToggleButtons view.
 */
public View createToggleButtonsView()
{
    Label label = new Label("Toggle Buttons");
    ToggleButton btn1 = new ToggleButton("Align Left"); btn1.setToggleGroupName("TG1"); btn1.setPrefWidth(100);
    ToggleButton btn2 = new ToggleButton("Align Center"); btn2.setToggleGroupName("TG1"); btn2.setPrefWidth(100);
    ToggleButton btn3 = new ToggleButton("Align Right"); btn3.setToggleGroupName("TG1"); btn3.setPrefWidth(100);
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(20);
    vbox.setChildren(label, btn1, btn2, btn3); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    vbox.setPrefWidth(360);
    btn1.addEventHandler(e -> ViewAnim.setAlign(vbox, Pos.CENTER_LEFT, 500), Action);
    btn2.addEventHandler(e -> ViewAnim.setAlign(vbox, Pos.CENTER, 500), Action);
    btn3.addEventHandler(e -> ViewAnim.setAlign(vbox, Pos.CENTER_RIGHT, 500), Action);
    return vbox;
}

/**
 * Creates CheckBox view.
 */
public View createCheckBoxsView()
{
    Label label = new Label("CheckBoxes");
    CheckBox btn1 = new CheckBox("CheckBox One");
    CheckBox btn2 = new CheckBox("CheckBox Two");
    CheckBox btn3 = new CheckBox("CheckBox Three");
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(20);
    vbox.setChildren(label, btn1, btn2, btn3); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates RadioButton view.
 */
public View createRadioButtonsView()
{
    Label label = new Label("RadioButtons");
    RadioButton btn1 = new RadioButton("RadioButton One"); btn1.setToggleGroupName("TG2");
    RadioButton btn2 = new RadioButton("RadioButton Two"); btn2.setToggleGroupName("TG2");
    RadioButton btn3 = new RadioButton("RadioButton Three"); btn3.setToggleGroupName("TG2");
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(20);
    vbox.setChildren(label, btn1, btn2, btn3); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates Slider view.
 */
public View createSlidersView()
{
    Label label = new Label("Sliders");
    DecimalFormat fmt = new DecimalFormat("0.###");
    
    Label lbl1 = new Label("Slider One:");
    Slider sldr1 = new Slider(); sldr1.setPrefWidth(200);
    TextField text1 = new TextField(); text1.setText("0"); text1.setPrefWidth(50); text1.setAlign(Pos.CENTER);
    sldr1.addEventHandler(e -> text1.setText(fmt.format(sldr1.getValue())), Action);
    HBox hbox1 = new HBox(); hbox1.setChildren(lbl1, sldr1, text1);
    
    Label lbl2 = new Label("Slider Two:");
    Slider sldr2 = new Slider(); sldr2.setPrefWidth(200);
    TextField text2 = new TextField(); text2.setText("0"); text2.setPrefWidth(50); text2.setAlign(Pos.CENTER);
    sldr2.addEventHandler(e -> { double v = sldr2.getValue(); text2.setText(fmt.format(v)); text2.setScale(1+v); }, Action);
    HBox hbox2 = new HBox(); hbox2.setChildren(lbl2, sldr2, text2);
    
    Label lbl3 = new Label("Slider Three:");
    Slider sldr3 = new Slider(); sldr3.setPrefWidth(200);
    TextField text3 = new TextField(); text3.setText("0"); text3.setPrefWidth(50); text3.setAlign(Pos.CENTER);
    sldr3.addEventHandler(e -> { double v = sldr3.getValue(); text3.setText(fmt.format(v)); text3.setRotate(v*360); }, Action);
    HBox hbox3 = new HBox(); hbox3.setChildren(lbl3, sldr3, text3);
    
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(30,30,30,30); vbox.setSpacing(30);
    vbox.setChildren(label, hbox1, hbox2, hbox3); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates TextField view.
 */
public View createTextFieldsView()
{
    Label label = new Label("TextFields");
    Label lbl1 = new Label("TextField One:");
    TextField sldr1 = new TextField(); sldr1.setPrefWidth(200);
    HBox hbox1 = new HBox(); hbox1.setChildren(lbl1, sldr1);
    Label lbl2 = new Label("TextField Two:");
    TextField sldr2 = new TextField(); sldr2.setPrefWidth(200);
    HBox hbox2 = new HBox(); hbox2.setChildren(lbl2, sldr2);
    Label lbl3 = new Label("TextField Three:");
    TextField sldr3 = new TextField(); sldr3.setPrefWidth(200);
    HBox hbox3 = new HBox(); hbox3.setChildren(lbl3, sldr3);
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(30,30,30,30); vbox.setSpacing(30);
    vbox.setChildren(label, hbox1, hbox2, hbox3); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates ProgressBar view.
 */
public View createProgressBarView()
{
    Label label = new Label("ProgressBar");
    ProgressBar pbar = new ProgressBar(); pbar.setPrefSize(200, 24);
    Slider sldr = new Slider(); sldr.setPrefWidth(180);
    sldr.addEventHandler(e -> pbar.setProgress(sldr.getValue()), Action);
    CheckBox cbox = new CheckBox(); cbox.setText("Set Indeterminate");
    cbox.addEventHandler(e -> pbar.setIndeterminate(cbox.isSelected()), Action);
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(20);
    vbox.setChildren(label, pbar, sldr, cbox); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates a Spinner View.
 */
public View createSpinnerView()
{
    Label label = new Label("Spinner");
    Spinner spnr = new Spinner(); spnr.setPrefSize(160,24);
    spnr.setValue(100);
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(20);
    vbox.setChildren(label, spnr); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Creates a TabView.
 */
public View createTabView()
{
    Label label = new Label("TabView");
    TabView tview = new TabView(); tview.setPrefSize(400,220);
    tview.addTab("Primary", new Label("This stuff comes first")); tview.getTabContent(0).setAlign(Pos.CENTER);
    tview.addTab("Secondary", new Label("This stuff comes next")); tview.getTabContent(1).setAlign(Pos.CENTER);
    tview.addTab("Tertiary", new Label("This stuff comes last")); tview.getTabContent(2).setAlign(Pos.CENTER);
    VBox vbox = new VBox(); vbox.setAlign(Pos.CENTER); vbox.setPadding(20,20,20,20); vbox.setSpacing(20);
    vbox.setChildren(label, tview); vbox.setBorder(VIEW_BORDER); vbox.setFont(VIEW_FONT);
    return vbox;
}

/**
 * Responds to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    if(anEvent.equals("ListView")) {
        int ind = getViewSelectedIndex("ListView");
        VBox vbox = getView("VBox", VBox.class);
        View child = vbox.getChild(ind);
        Scroller scroller = (Scroller)vbox.getParent();
        double sv = child.getMaxY() - scroller.getHeight() + Math.round((scroller.getHeight()-child.getHeight())/2);
        sv = Math.max(sv,0);
        scroller.getAnimCleared(500).setValue(Scroller.ScrollV_Prop, sv).play();
    }
}

/**
 * Main.
 */
public static void main(String args[])
{
    snapcj.CJ.set();
    BusyBox bbox = new BusyBox(); bbox.getUI().setGrowWidth(true);
    bbox.setWindowVisible(true);
}

}