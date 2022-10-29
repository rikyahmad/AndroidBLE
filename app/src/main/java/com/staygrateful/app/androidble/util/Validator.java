package com.staygrateful.app.androidble.util;

public interface Validator<T> {
    boolean onValidate(T data);
}
