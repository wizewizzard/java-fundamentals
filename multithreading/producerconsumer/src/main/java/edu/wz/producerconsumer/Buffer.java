package edu.wz.producerconsumer;

public interface Buffer <T> {
    void putValue(T value);
    T getValue();
}
