package com.example.common.util;

import java.util.*;
import java.util.stream.Collectors;

public final class CollectionUtils {

    /**
     * <code>CollectionUtils</code> should not normally be instantiated.
     */
    private CollectionUtils() {
    }

    /**
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     * </p>
     *
     * @param coll the collection to check, may be null
     * @return true if empty or null
     */
    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * <p>
     * Null returns false.
     * </p>
     *
     * @param coll the collection to check, may be null
     * @return true if non-null and non-empty
     */
    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }

    public static List<String> stringToList(String input) {
        return stringToList(input, StringUtils.COMMA);
    }

    public static List<String> stringToList(String input, boolean distinct) {
        return stringToList(input, StringUtils.COMMA, distinct);
    }

    public static List<String> stringToList(String input, String delimiter) {
        return stringToList(input, delimiter, false);
    }

    private static List<String> stringToList(String input, String delimiter, boolean distinct) {
        String[] arr = ArrayUtils.stringToArray(input, delimiter);
        if (ArrayUtils.isEmpty(arr)) {
            return new ArrayList<>();
        }
        if (distinct) {
            return Arrays.stream(arr).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        }
        return Arrays.stream(arr).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    public static Set<String> stringToSet(String input) {
        return stringToSet(input, StringUtils.COMMA);
    }

    public static Set<String> stringToSet(String input, String delimiter) {
        List<String> lst = stringToList(input, delimiter);
        if (CollectionUtils.isEmpty(lst)) {
            return new HashSet<>();
        }
        return new HashSet<>(lst);
    }
}
