package com.staygrateful.app.androidble.util;

import java.util.List;

public class StringUtils {
    public static<T> String joinToString(List<T> coll,
                                      String separator,
                                      String prefix,
                                      String postfix,
                                      Transform<T> transform) {
        if (coll == null || coll.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(prefix);
        final int size = coll.size();;
        for (int i=0; i< size; i++) {
            final T data = coll.get(i);
            if (transform != null) {
                sb.append(transform.onTransform(data));
            } else {
                sb.append(data);
            }
            if (i < size - 1) {
             sb.append(separator);
            }
        }
        return sb.append(postfix).toString();
    }
}
