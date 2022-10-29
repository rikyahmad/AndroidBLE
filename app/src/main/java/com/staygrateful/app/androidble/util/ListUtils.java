package com.staygrateful.app.androidble.util;

import java.util.List;

public class ListUtils {
    public static<T> T find(List<T> coll, Validator<T> validator) {
        if (coll == null || coll.isEmpty()) {
            return null;
        }
        final int size = coll.size();;
        for (int i=0; i< size; i++) {
            final T data = coll.get(i);
            if (validator != null && validator.onValidate(data)) {
                return data;
            }
        }
        return null;
    }
}
