
import java.util.Random;
import java.util.Scanner;

// javac Example5.java --release 8 && jar cvf Example5.jar *.class && rm *.class
class Example5 {
    private static String beta(String a) {
        return a;            // c6
    }

    private static String alpha(String x) {
        return beta(x);      // c5
    }

    public static void main(String[] args) throws Exception {
        String a = new Scanner(System.in).next();    // c1
        String b = alpha(a);                // c2
        String c = alpha("hi");             // c3
        System.out.println(c);               // c4
    }
}