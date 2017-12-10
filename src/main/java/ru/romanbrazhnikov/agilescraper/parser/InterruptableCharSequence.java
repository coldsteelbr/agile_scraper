package ru.romanbrazhnikov.agilescraper.parser;


/**
 * CharSequence that noticed thread interrupts -- as might be necessary
 * to recover from a loose regex on unexpected challenging input.
 *
 * @author gojomo
 */
public class InterruptableCharSequence implements CharSequence {
    CharSequence inner;
    // public long counter = 0;

    public InterruptableCharSequence(CharSequence inner) {
        super();
        this.inner = inner;
    }

    public char charAt(int index) {
        if (Thread.interrupted()) { // clears flag if set
            throw new RuntimeException(new InterruptedException());
        }
        // counter++;
        return inner.charAt(index);
    }

    public int length() {
        return inner.length();
    }

    public CharSequence subSequence(int start, int end) {
        return new InterruptableCharSequence(inner.subSequence(start, end));
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
