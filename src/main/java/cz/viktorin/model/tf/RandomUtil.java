package cz.viktorin.model.tf;

import java.util.Random;

public class RandomUtil {

    private static Random rnd = new Random();

    /*
    Class not to be instantiated
     */
    private RandomUtil() {
    }
    
    public static Double nextNormal() {
        return rnd.nextGaussian();
    }
    
    public static Double nextDouble() {
        return rnd.nextDouble();
    }

    public static int nextInt(int bound) {
        return rnd.nextInt(bound);
    }

    public static double nextDouble(double min, double max) {
        return (nextDouble() * (max - min) + min);
    }

}
