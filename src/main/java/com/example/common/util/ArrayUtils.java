package com.example.common.util;

import java.lang.reflect.Array;

public final class ArrayUtils {

    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * <code>ArrayUtils</code> should not normally be instantiated.
     */
    private ArrayUtils() {
    }

    public static String[] stringToArray(String input) {
        return stringToArray(input, StringUtils.COMMA);
    }

    public static String[] stringToArray(String input, String delimiter) {
        if (StringUtils.isBlank(input)) {
            return EMPTY_STRING_ARRAY;
        }
        return input.trim().split("\\s*" + delimiter + "\\s*");
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     */
    public static int getLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    /**
     * <p>Checks if an array of Objects is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     */
    public static boolean isEmpty(final Object[] array) {
        return getLength(array) == 0;
    }
}
