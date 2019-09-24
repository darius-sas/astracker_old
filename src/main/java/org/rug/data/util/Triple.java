package org.rug.data.util;

import java.io.Serializable;

/**
 * A class representing generic triple of objects
 * @param <A>
 * @param <B>
 * @param <C>
 */
public class Triple<A, B, C> implements Serializable {
    protected A a;
    protected B b;
    protected C c;

    public Triple(A a, B b, C c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }

    public void setC(C c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return String.format("a: %s\tb:\t%s\tc:%s", a, b, c);
    }
}
