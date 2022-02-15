package io.github.yemyatthu1990.apm;

import java.math.BigInteger;

class Utils {
    static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }
    static String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuilder.append(stackTraceElement).append("\n");
        }
        return stringBuilder.toString();
    }
}
