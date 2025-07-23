package com.example.common.util;

import java.math.BigDecimal;

public final class CommonUtils {

    /**
     * <code>CommonUtils</code> should not normally be instantiated.
     */
    private CommonUtils() {
    }

    public static boolean hasActualAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

}
