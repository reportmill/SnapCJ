package snapcj;

public class SnapCJ {

    static native void printString(String str);

    public static void main(String[] args)
    {
        printString("Hello World! xxx");

        Window window = Window.current();
        CJ.log(window);
        //window.open("http://abc.com", "_blank");

        byte[] bytes = { 1, 2, 3, 4, 5 };
        Int8Array int8Array = new Int8Array(bytes);
        CJ.log(int8Array);
        CJ.log("This is being logged");
    }
}