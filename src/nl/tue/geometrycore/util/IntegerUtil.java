/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

/**
 * Utility class with convenience methods for dealing with integer values.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IntegerUtil {

    /**
     * Ensures that value A lies in range [min,max].
     *
     * @param A Value to clip to range
     * @param min Lower bound of range
     * @param max Upper bound of range
     * @return A if A in [min,max]; min if A &lt; min; max if A &gt; max
     */
    public static int clipValue(int A, int min, int max) {
        return Math.max(Math.min(A, max), min);
    }

    /**
     * Returns whether a value lies in a closed interval. Assumes x &lt;= y.
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @return a in [x,y].
     */
    public static boolean inClosedInterval(int a, int x, int y) {
        return x <= a && a <= y;
    }

    /**
     * Returns whether a value lies in an open interval. Assumes x &lt;= y.
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @return a in (x,y).
     */
    public static boolean inOpenInterval(int a, int x, int y) {
        return x < a && a < y;
    }

    /**
     * Convenience function to compute the maximum over multiple values. The
     * value will be Integer.MIN_VALUE if vs is empty.
     *
     * @param vs An array of numbers
     * @return The maximum value in vs
     */
    public static int max(int... vs) {
        int m = Integer.MIN_VALUE;
        for (int v : vs) {
            if (v > m) {
                m = v;
            }
        }
        return m;
    }

    /**
     * Convenience function to compute the minimum over multiple values. The
     * value will be Integer.MAX_VALUE if vs is empty.
     *
     * @param vs An array of numbers
     * @return The maximum value in vs
     */
    public static int min(int... vs) {
        int m = Integer.MAX_VALUE;
        for (int v : vs) {
            if (v < m) {
                m = v;
            }
        }
        return m;
    }
}
