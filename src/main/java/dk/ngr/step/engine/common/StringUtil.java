package dk.ngr.step.engine.common;

import java.util.Objects;

/**
 * Adding this class with simple string utils thus avoiding
 * the Apache commons-lang3 dependency.
 *
 */
public class StringUtil {

    public static boolean isEmpty(String value) {

        if (Objects.isNull(value))
            throw new IllegalArgumentException("Value is null");

        if ( ! value.isEmpty())
            return false;

        return true;
    }

    public static boolean isNotEmpty(String value) {

        if (Objects.isNull(value))
            throw new IllegalArgumentException("Value is null");

        if (value.isEmpty())
            return false;

        return true;
    }
}