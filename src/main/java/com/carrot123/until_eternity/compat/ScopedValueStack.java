package com.carrot123.until_eternity.compat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

public final class ScopedValueStack<T> {
    private final ThreadLocal<Deque<T>> values =
            ThreadLocal.withInitial(ArrayDeque::new);

    public T current(T fallback) {
        Deque<T> stack = values.get();
        return stack.isEmpty() ? fallback : stack.peek();
    }

    public <R> R withValue(T value, Supplier<R> action) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(action, "action");

        Deque<T> stack = values.get();
        stack.push(value);
        try {
            return action.get();
        } finally {
            stack.pop();
            if (stack.isEmpty()) {
                values.remove();
            }
        }
    }
}
