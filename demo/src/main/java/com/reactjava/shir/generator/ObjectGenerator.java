package com.reactjava.shir.generator;

import java.util.ArrayList;
import java.util.List;

public interface ObjectGenerator<T> {
    T next();

    default List<T> generate(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        List<T> objects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            objects.add(next());
        }
        return objects;
    }
}
