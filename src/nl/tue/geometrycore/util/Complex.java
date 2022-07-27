/*
 * GeometryCore library   
 * Copyright (C) 2022   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

import nl.tue.geometrycore.geometry.Vector;

/**
 * A simple complex number class with double impression. Computation with
 * complex numbers is supported through static functions, in which arguments can
 * be either double or Complex numbers. This allows for more flexibility
 * changing parts of the computations from double to complex and vice versa.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Complex {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private double _re;
    private double _im;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a complex number for the given real and imaginary components.
     *
     * @param re The real component
     * @param im The imaginary component
     */
    public Complex(double re, double im) {
        _re = re;
        _im = im;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public double getRe() {
        return _re;
    }

    public void setRe(double _re) {
        this._re = _re;
    }

    public double getIm() {
        return _im;
    }

    public void setIm(double _im) {
        this._im = _im;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Tests whether the complex number is purely real
     *
     * @return true iff imaginary part is approximately zero
     */
    public boolean isReal() {
        return DoubleUtil.close(_im, 0);
    }

    /**
     * Tests whether the complex number is purely imaginary
     *
     * @return true iff real part is approximately zero
     */
    public boolean isPureIm() {
        return DoubleUtil.close(_re, 0);
    }

    /**
     * Returns true iff this number is approximately zero.
     *
     * @return
     */
    public boolean isZero() {
        return isReal() && isPureIm();
    }

    @Override
    public String toString() {
        return "[" + _re + " + " + _im + " i]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="STATIC ARITHMETIC">
    /**
     * Adds the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a+b
     */
    public static Complex add(Complex a, Complex b) {
        return new Complex(a._re + b._re, a._im + b._im);
    }

    /**
     * Adds the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a+b
     */
    public static Complex add(double a, Complex b) {
        return new Complex(a + b._re, b._im);
    }

    /**
     * Adds the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a+b
     */
    public static Complex add(Complex a, double b) {
        return new Complex(a._re + b, a._im);
    }

    /**
     * Adds the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a+b
     */
    public static Complex add(double a, double b) {
        return new Complex(a + b, 0);
    }

    /**
     * Subtracts the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a-b
     */
    public static Complex subtract(Complex a, Complex b) {
        return new Complex(a._re - b._re, a._im - b._im);
    }

    /**
     * Subtracts the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a-b
     */
    public static Complex subtract(double a, Complex b) {
        return new Complex(a - b._re, b._im);
    }

    /**
     * Subtracts the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a-b
     */
    public static Complex subtract(Complex a, double b) {
        return new Complex(a._re - b, a._im);
    }

    /**
     * Subtracts the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a-b
     */
    public static Complex subtract(double a, double b) {
        return new Complex(a - b, 0);
    }

    /**
     * Multiplies the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a*b
     */
    public static Complex multiply(Complex a, Complex b) {
        return new Complex(a._re * b._re - a._im * b._im, a._re * b._im + a._im * b._re);
    }

    /**
     * Multiplies the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a*b
     */
    public static Complex multiply(double a, Complex b) {
        return new Complex(a * b._re, a * b._im);
    }

    /**
     * Multiplies the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a*b
     */
    public static Complex multiply(Complex a, double b) {
        return new Complex(a._re * b, a._im * b);
    }

    /**
     * Multiplies the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a*b
     */
    public static Complex multiply(double a, double b) {
        return new Complex(a * b, 0);
    }

    /**
     * Computes the square of the given number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return a^2
     */
    public static Complex square(Complex a) {
        return multiply(a, a);
    }

    /**
     * Computes the square of the given number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return a^2
     */
    public static Complex square(double a) {
        return new Complex(a * a, 0);
    }

    /**
     * Computes the cube of the given number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return a^3
     */
    public static Complex cube(Complex a) {
        return multiply(a, multiply(a, a));
    }

    /**
     * Computes the given number raised to the power 4, returning the result as
     * a new complex number.
     *
     * @param a
     * @return a^4
     */
    public static Complex quart(Complex a) {
        Complex aa = multiply(a, a);
        return multiply(aa, aa);
    }

    /**
     * Computes the given number raised to the power 4, returning the result as
     * a new complex number.
     *
     * @param a
     * @return a^4
     */
    public static Complex quart(double a) {
        Complex aa = multiply(a, a);
        return multiply(aa, aa);
    }

    /**
     * Computes the square root of a number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return sqrt(a)
     */
    public static Complex squareroot(Complex a) {
        if (a.isReal()) {
            return squareroot(a._re);
        } else {
            double len = Math.sqrt(a._re * a._re + a._im * a._im);
            return new Complex(Math.sqrt((a._re + len) / 2),
                    Math.signum(a._im) * Math.sqrt((-a._re + len) / 2)
            );
        }
    }

    /**
     * Computes the square root of a number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return sqrt(a)
     */
    public static Complex squareroot(double a) {
        if (a > 0) {
            return new Complex(Math.sqrt(a), 0);
        } else {
            return new Complex(0, Math.sqrt(-a));
        }
    }

    /**
     * Computes the cuberoot root of a number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return a^(1/3)
     */
    public static Complex cuberoot(Complex a) {
        if (a.isReal()) {
            return cuberoot(a._re);
        }
        Vector v = new Vector(a._re, a._im);
        double r = v.length();
        double th = v.computeClockwiseAngleTo(Vector.right());
        double crr = Math.pow(r, 1 / 3.0);
        double crth_1 = th / 3.0;
        double crth_2 = (th + Math.PI * 2) / 3.0;
        double crth_3 = (th - Math.PI * 2) / 3.0;
        Vector cr_1 = Vector.right(crr);
        cr_1.rotate(crth_1);
        Vector cr_2 = Vector.right(crr);
        cr_2.rotate(crth_2);
        Vector cr_3 = Vector.right(crr);
        cr_3.rotate(crth_3);
        double max_re = Math.max(Math.max(cr_1.getX(), cr_2.getX()), cr_3.getX());
        if (cr_1.getX() >= max_re) {
            return new Complex(cr_1.getX(), cr_1.getY());
        } else if (cr_2.getX() >= max_re) {
            return new Complex(cr_2.getX(), cr_2.getY());
        } else {
            return new Complex(cr_3.getX(), cr_3.getY());
        }
    }

    /**
     * Computes the cuberoot root of a number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return a^(1/3)
     */
    public static Complex cuberoot(double a) {
        if (a > 0) {
            return new Complex(Math.pow(a, 1 / 3.0), 0);
        } else {
            return new Complex(-Math.pow(-a, 1 / 3.0), 0);
        }
    }

    /**
     * Computes the reciprocal of a number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return 1/a
     */
    public static Complex reciproc(Complex a) {
        double len = a._re * a._re + a._im * a._im;
        return new Complex(a._re / len, -a._im / len);
    }

    /**
     * Computes the reciprocal of a number, returning the result as a new
     * complex number.
     *
     * @param a
     * @return 1/a
     */
    public static Complex reciproc(double a) {
        return new Complex(1 / a, 0);
    }

    /**
     * Divides the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a/b
     */
    public static Complex divide(Complex a, Complex b) {
        return multiply(a, reciproc(b));
    }

    /**
     * Divides the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a/b
     */
    public static Complex divide(double a, Complex b) {
        return multiply(a, reciproc(b));
    }

    /**
     * Divides the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a/b
     */
    public static Complex divide(Complex a, double b) {
        return multiply(a, 1.0 / b);
    }

    /**
     * Divides the two numbers, returning the result as a new complex number.
     *
     * @param a
     * @param b
     * @return a/b
     */
    public static Complex divide(double a, double b) {
        return multiply(a, 1.0 / b);
    }
    //</editor-fold>
}
