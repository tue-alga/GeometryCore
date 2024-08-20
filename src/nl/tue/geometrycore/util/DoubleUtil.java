/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

import java.util.Arrays;

/**
 * Utility class with convenience methods for dealing with double values, in
 * particular, focused on handling imprecision.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DoubleUtil {

    //<editor-fold defaultstate="collapsed" desc="STATIC FIELDS">
    /**
     * Default tolerance for imprecision.
     */
    public static final double EPS = 0.000001;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRECISION">
    /**
     * Returns whether the absolute value between A and B is at most EPS.
     *
     * @param A first value
     * @param B second value
     * @return |A-B| &lt;= DoubleUtil.EPS
     */
    public static boolean close(double A, double B) {
        return Math.abs(A - B) <= EPS;
    }

    /**
     * Returns whether the absolute value between A and B is at most the given
     * eps value.
     *
     * @param A first value
     * @param B second value
     * @param eps tolerance value
     * @return |A-B| &lt;= eps
     */
    public static boolean close(double A, double B, double eps) {
        return -eps <= A - B && A - B <= eps;
    }

    /**
     * Ensures that value A lies in range [min,max].
     *
     * @param A Value to clip to range
     * @param min Lower bound of range
     * @param max Upper bound of range
     * @return A if A in [min,max]; min if A &lt; min; max if A &gt; max
     */
    public static double clipValue(double A, double min, double max) {
        return Math.max(Math.min(A, max), min);
    }

    /**
     * Returns whether a value lies in a closed interval for a given precision.
     * Assumes x &lt;= y. Positive precision makes the check "enlarge" the
     * interval, negative values shrink the interval.
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @param prec precision
     * @return a in [x-prec,y+prec].
     */
    public static boolean inClosedInterval(double a, double x, double y, double prec) {
        return x - prec <= a && a <= y + prec;
    }

    /**
     * Checks whether a value lies in an closed interval.
     *
     * @param a value to check for
     * @param x lower value of the interval
     * @param y upper value of the interval
     * @return a in [x-EPS, y+EPS]
     */
    public static boolean inClosedInterval(double a, double x, double y) {
        return inClosedInterval(a, x, y, EPS);
    }

    /**
     * Checks whether a value lies in an open interval.
     *
     * @param a value to check for
     * @param x lower value of the interval
     * @param y upper value of the interval
     * @return a in (x+EPS, y-EPS)
     */
    public static boolean inOpenInterval(double a, double x, double y) {
        return inOpenInterval(a, x, y, -EPS);
    }

    /**
     * Returns whether a value lies in an open interval for a given precision.
     * Assumes x &lt;= y. Positive precision makes the check "enlarge" the
     * interval, negative values shrink the interval.
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @param prec precision
     * @return a in (x-prec,y+prec).
     */
    public static boolean inOpenInterval(double a, double x, double y, double prec) {
        return x - prec < a && a < y + prec;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="EQUATION SOLVING">
    /**
     * Returns the solutions of a*x^2 + b*x + c = 0.
     *
     * @param a factor for quadratic term
     * @param b factor for linear term
     * @param c factor for constant term
     * @return array with 0, 1, or 2 values for x that satisfy the equation, in
     * increasing order
     */
    public static double[] solveQuadraticEquation(double a, double b, double c) {
        if (close(a, 0)) {
            // b x + c = 0 --> x = -c/b
            return new double[]{-c / b};
        } else {
            double d = b * b - 4 * a * c;
            if (d < -EPS) {
                return new double[]{};
            } else if (d < EPS) {
                return new double[]{-b / (2 * a)};
            } else {
                double s1 = (-b + Math.sqrt(d)) / (2 * a);
                double s2 = (-b - Math.sqrt(d)) / (2 * a);
                if (s1 < s2) {
                    return new double[]{s1, s2};
                } else {
                    return new double[]{s2, s1};
                }

            }
        }
    }

    /**
     * Returns the smallest solution of a*x^2 + b*x + c = 0 strictly greater
     * than a given threshold value.
     *
     * @param a factor for quadratic term
     * @param b factor for linear term
     * @param c factor for constant term
     * @param threshold threshold value
     * @return the smallest positive solution; NaN if no positive solution
     * exists
     */
    public static double solveQuadraticEquationForSmallestPositive(double a, double b, double c, double threshold) {
        double[] sol = solveQuadraticEquation(a, b, c);

        switch (sol.length) {
            case 0:
                return Double.NaN;
            case 1:
                if (sol[0] > threshold) {
                    return sol[0];
                } else {
                    return Double.NaN;
                }
            default:
                if (sol[0] > threshold && sol[1] > threshold) {
                    return Math.min(sol[0], sol[1]);
                } else if (sol[0] > threshold) {
                    return sol[0];
                } else if (sol[1] > threshold) {
                    return sol[1];
                } else {
                    return Double.NaN;
                }
        }
    }

    /**
     * Returns the four possibly complex solutions of a*x^4 + b*x^3 + c*x^2 +d*x
     * + e = 0. This function implements Ferrari's method.
     *
     * @param a factor for quartic term
     * @param b factor for cubic term
     * @param c factor for quadratic term
     * @param d factor for linear term
     * @param e factor for constant term
     *
     * @return an array with the four complex solutions to the quartic equation
     */
    public static Complex[] solveQuarticEquation(double a, double b, double c, double d, double e) {

        Complex[] xs = new Complex[4];

        double alpha = -3 * Math.pow(b, 2) / (8 * Math.pow(a, 2)) + c / a;
        double beta = Math.pow(b, 3) / (8 * Math.pow(a, 3)) - b * c / (2 * Math.pow(a, 2)) + d / a;
        double gamma = -3 * Math.pow(b, 4) / (256 * Math.pow(a, 4)) + c * Math.pow(b, 2) / (16 * Math.pow(a, 3)) - b * d / (4 * Math.pow(a, 2)) + e / a;

        if (DoubleUtil.close(beta, 0)) {
            xs[0] = Complex.add(-b / (4 * a), Complex.squareroot((-alpha + Math.sqrt(Math.pow(alpha, 2) - 4 * gamma)) / 2.0));
            xs[1] = Complex.add(-b / (4 * a), Complex.squareroot((-alpha - Math.sqrt(Math.pow(alpha, 2) - 4 * gamma)) / 2.0));
            xs[2] = Complex.subtract(-b / (4 * a), Complex.squareroot((-alpha + Math.sqrt(Math.pow(alpha, 2) - 4 * gamma)) / 2.0));
            xs[3] = Complex.subtract(-b / (4 * a), Complex.squareroot((-alpha - Math.sqrt(Math.pow(alpha, 2) - 4 * gamma)) / 2.0));
        } else {

            double P = -Math.pow(alpha, 2) / 12 - gamma;
            double Q = -Math.pow(alpha, 3) / 108 + alpha * gamma / 3 - Math.pow(beta, 2) / 8;
            Complex R = Complex.add(
                    -Q / 2,
                    Complex.squareroot(Math.pow(Q, 2) / 4 + Math.pow(P, 3) / 27));

            Complex U = Complex.cuberoot(R);

            Complex y = Complex.add(
                    -5 * alpha / 6,
                    U.isZero()
                    ? Complex.subtract(0, Complex.cuberoot(Q))
                    : Complex.subtract(U, Complex.divide(P, Complex.multiply(3, U))));

            Complex W = Complex.squareroot(Complex.add(alpha, Complex.multiply(2, y)));

            xs[0] = Complex.add(
                    -b / (4 * a),
                    Complex.divide(
                            Complex.add(
                                    W,
                                    Complex.squareroot(
                                            Complex.subtract(0,
                                                    Complex.add(3 * alpha,
                                                            Complex.add(
                                                                    Complex.multiply(2, y),
                                                                    Complex.divide(2 * beta, W)
                                                            )
                                                    )
                                            )
                                    )
                            ),
                            2));
            xs[1] = Complex.add(
                    -b / (4 * a),
                    Complex.divide(
                            Complex.subtract(
                                    W,
                                    Complex.squareroot(
                                            Complex.subtract(0,
                                                    Complex.add(3 * alpha,
                                                            Complex.add(
                                                                    Complex.multiply(2, y),
                                                                    Complex.divide(2 * beta, W)
                                                            )
                                                    )
                                            )
                                    )
                            ),
                            2));
            xs[2] = Complex.add(
                    -b / (4 * a),
                    Complex.divide(
                            Complex.add(
                                    Complex.subtract(0, W),
                                    Complex.squareroot(
                                            Complex.subtract(0,
                                                    Complex.add(3 * alpha,
                                                            Complex.subtract(
                                                                    Complex.multiply(2, y),
                                                                    Complex.divide(2 * beta, W)
                                                            )
                                                    )
                                            )
                                    )
                            ),
                            2));
            xs[3] = Complex.add(
                    -b / (4 * a),
                    Complex.divide(
                            Complex.subtract(
                                    Complex.subtract(0, W),
                                    Complex.squareroot(
                                            Complex.subtract(0,
                                                    Complex.add(3 * alpha,
                                                            Complex.subtract(
                                                                    Complex.multiply(2, y),
                                                                    Complex.divide(2 * beta, W)
                                                            )
                                                    )
                                            )
                                    )
                            ),
                            2));
        }

        // DEBUG CODE: |Ftests the roots
//        for (Complex v : result) {
//            Complex r = Complex.add(
//                    Complex.multiply(a, Complex.quart(v)),
//                    Complex.add(Complex.multiply(b, Complex.cube(v)),
//                            Complex.add(Complex.multiply(c, Complex.square(v)),
//                                    Complex.add(Complex.multiply(d, v),
//                                            e))));
//            if (r.isZero()) {
//                System.out.println("x = " + v + " --> OK");
//            } else {
//                System.out.println("x = " + v + " --> " + r);
//            }
//        }
        return xs;
    }

    /**
     * Returns the real roots of a*x^4 + b*x^3 + c*x^2 +d*x + e = 0
     *
     * @param a factor for quartic term
     * @param b factor for cubic term
     * @param c factor for quadratic term
     * @param d factor for linear term
     * @param e factor for constant term
     *
     * @return an array with up to four real solutions to the quartic equation,
     * sorted by increasing values of x
     */
    public static double[] solveQuarticEquationReal(double a, double b, double c, double d, double e) {

        Complex[] cxs = solveQuarticEquation(a, b, c, d, e);

        int r = 0;
        for (Complex cx : cxs) {
            if (cx.isReal()) {
                r++;
            }
        }

        double[] xs = new double[r];
        r--;
        for (Complex cx : cxs) {
            if (cx.isReal()) {
                xs[r] = cx.getRe();
                r--;
            }
        }

        Arrays.sort(xs);

        return xs;
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="BASIC COMPUTATIONS">
    /**
     * Linearly interpolations between the two given values.
     *
     * @param a the first value
     * @param b the second value
     * @param lambda how far to interpolate from a to b
     * @return (1-lambda) * a + lambda * b
     */
    public static double interpolate(double a, double b, double lambda) {
        return (1 - lambda) * a + lambda * b;
    }

    /**
     * Convenience function to compute the maximum over multiple values. The
     * value will be negative infinity if vs is empty; if a value is NaN, then
     * the result will be NaN.
     *
     * @param vs An array of numbers
     * @return The maximum value in vs
     */
    public static double max(double... vs) {
        double m = Double.NEGATIVE_INFINITY;
        for (double v : vs) {
            m = Math.max(m, v);
        }
        return m;
    }
    
     /**
     * Convenience function to compute the minimum over multiple values. The
     * value will be positive infinity if vs is empty; if a value is NaN, then
     * the result will be NaN.
     *
     * @param vs An array of numbers
     * @return The minimum value in vs
     */
    public static double min(double... vs) {
        double m = Double.POSITIVE_INFINITY;
        for (double v : vs) {
            m = Math.min(m, v);
        }
        return m;
    }
    //</editor-fold>
}
