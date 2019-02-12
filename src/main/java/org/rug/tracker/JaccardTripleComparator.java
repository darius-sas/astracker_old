package org.rug.tracker;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class JaccardTripleComparator implements Comparator<LinkScoreTriple> {

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     * <p>
     * The implementor must ensure that {@code sgn(compare(x, y)) ==
     * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
     * implies that {@code compare(x, y)} must throw an exception if and only
     * if {@code compare(y, x)} throws an exception.)<p>
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
     * {@code compare(x, z)>0}.<p>
     * <p>
     * Finally, the implementor must ensure that {@code compare(x, y)==0}
     * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
     * {@code z}.<p>
     * <p>
     * It is generally the case, but <i>not</i> strictly required that
     * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."<p>
     * <p>
     * In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(LinkScoreTriple o1, LinkScoreTriple o2) {
        return o1.compareTo(o2);
    }

    /**
     * Returns a comparator that imposes the reverse ordering of this
     * comparator.
     *
     * @return a comparator that imposes the reverse ordering of this
     * comparator.
     * @since 1.8
     */
    @Override
    public Comparator<LinkScoreTriple> reversed() {
        return null;
    }

    /**
     * Returns a lexicographic-order comparator with another comparator.
     * If this {@code Comparator} considers two elements equal, i.e.
     * {@code compare(a, b) == 0}, {@code other} is used to determine the order.
     *
     * <p>The returned comparator is serializable if the specified comparator
     * is also serializable.
     *
     * @param other the other comparator to be used when this comparator
     *              compares two objects that are equal.
     * @return a lexicographic-order comparator composed of this and then the
     * other comparator
     * @throws NullPointerException if the argument is null.
     * @apiNote For example, to sort a collection of {@code String} based on the length
     * and then case-insensitive natural ordering, the comparator can be
     * composed using following code,
     *
     * <pre>{@code
     *     Comparator<String> cmp = Comparator.comparingInt(String::length)
     *             .thenComparing(String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     * @since 1.8
     */
    @Override
    public Comparator<LinkScoreTriple> thenComparing(Comparator<? super LinkScoreTriple> other) {
        return null;
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a key to be compared with the given {@code Comparator}.
     *
     * @param keyExtractor  the function used to extract the sort key
     * @param keyComparator the {@code Comparator} used to compare the sort key
     * @return a lexicographic-order comparator composed of this comparator
     * and then comparing on the key extracted by the keyExtractor function
     * @throws NullPointerException if either argument is null.
     * @implSpec This default implementation behaves as if {@code
     * thenComparing(comparing(keyExtractor, cmp))}.
     * @see #comparing(Function, Comparator)
     * @see #thenComparing(Comparator)
     * @since 1.8
     */
    @Override
    public <U> Comparator<LinkScoreTriple> thenComparing(Function<? super LinkScoreTriple, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        return null;
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code Comparable} sort key.
     *
     * @param keyExtractor the function used to extract the {@link
     *                     Comparable} sort key
     * @return a lexicographic-order comparator composed of this and then the
     * {@link Comparable} sort key.
     * @throws NullPointerException if the argument is null.
     * @implSpec This default implementation behaves as if {@code
     * thenComparing(comparing(keyExtractor))}.
     * @see #comparing(Function)
     * @see #thenComparing(Comparator)
     * @since 1.8
     */
    @Override
    public <U extends Comparable<? super U>> Comparator<LinkScoreTriple> thenComparing(Function<? super LinkScoreTriple, ? extends U> keyExtractor) {
        return null;
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts an {@code int} sort key.
     *
     * @param keyExtractor the function used to extract the integer sort key
     * @return a lexicographic-order comparator composed of this and then the
     * {@code int} sort key
     * @throws NullPointerException if the argument is null.
     * @implSpec This default implementation behaves as if {@code
     * thenComparing(comparingInt(keyExtractor))}.
     * @see #comparingInt(ToIntFunction)
     * @see #thenComparing(Comparator)
     * @since 1.8
     */
    @Override
    public Comparator<LinkScoreTriple> thenComparingInt(ToIntFunction<? super LinkScoreTriple> keyExtractor) {
        return null;
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code long} sort key.
     *
     * @param keyExtractor the function used to extract the long sort key
     * @return a lexicographic-order comparator composed of this and then the
     * {@code long} sort key
     * @throws NullPointerException if the argument is null.
     * @implSpec This default implementation behaves as if {@code
     * thenComparing(comparingLong(keyExtractor))}.
     * @see #comparingLong(ToLongFunction)
     * @see #thenComparing(Comparator)
     * @since 1.8
     */
    @Override
    public Comparator<LinkScoreTriple> thenComparingLong(ToLongFunction<? super LinkScoreTriple> keyExtractor) {
        return null;
    }

    /**
     * Returns a lexicographic-order comparator with a function that
     * extracts a {@code double} sort key.
     *
     * @param keyExtractor the function used to extract the double sort key
     * @return a lexicographic-order comparator composed of this and then the
     * {@code double} sort key
     * @throws NullPointerException if the argument is null.
     * @implSpec This default implementation behaves as if {@code
     * thenComparing(comparingDouble(keyExtractor))}.
     * @see #comparingDouble(ToDoubleFunction)
     * @see #thenComparing(Comparator)
     * @since 1.8
     */
    @Override
    public Comparator<LinkScoreTriple> thenComparingDouble(ToDoubleFunction<? super LinkScoreTriple> keyExtractor) {
        return null;
    }
}
