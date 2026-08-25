package com.nexus.backend.common;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class CsvUtil {

    private CsvUtil() {
    }

    public static String escape(Object value) {
        String s = value == null ? "" : value.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static String row(Object... values) {
        return Arrays.stream(values).map(CsvUtil::escape).collect(Collectors.joining(",")) + "\r\n";
    }
}
