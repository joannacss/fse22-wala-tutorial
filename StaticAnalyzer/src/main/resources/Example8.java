
// javac Example8.java --release 8 && jar cvf Example8.jar *.class && rm *.class
public class Example8 {
    private static String SAMPLE_FIELD;
    public static void main(String[] args) {
        SAMPLE_FIELD = "Init static field";
        Shape s;
        // is even?
        if (Integer.parseInt(args[0]) % 2 == 0) {
            s = new Circle();
        } else {
            s = new Rectangle();
        }
        s.draw();
    }

    public static String getSampleField() {
        return SAMPLE_FIELD;
    }

    public static void setSampleField(String sampleField) {
        SAMPLE_FIELD = sampleField;
    }
}


interface Shape {
    void draw();
}
class Circle implements Shape{
    public void draw(){
        System.out.println("Draw Circle " + Example8.getSampleField());

    }
}
class Rectangle implements Shape{
    public void draw(){
        System.out.println("Draw Rectangle");
        Example8.setSampleField("Changed static field");
    }
}

class Triangle implements Shape{
    public void draw(){
        System.out.println("Draw Triangle");
    }
}
