
// javac Example7.java --release 8 && jar cvf Example7.jar *.class && rm *.class
public class Example7 {

    public static void drawCircle(Shape c){
        c.draw();
    }
    public static void drawRectangle(Shape r){
        r.draw();
    }

    public static void main(String[] args) {
        // is even?
        if (Integer.parseInt(args[0]) % 2 == 0) {
            drawCircle(new Circle());
        } else {
            drawRectangle(new Rectangle());
        }
    }
}

interface Shape {
    void draw();
}
class Circle implements Shape{
    public void draw(){
        System.out.println("Draw Circle");
    }
}
class Rectangle implements Shape{
    public void draw(){
        System.out.println("Draw Rectangle");
    }
}

class Triangle implements Shape{
    public void draw(){
        System.out.println("Draw Triangle");
    }
}
