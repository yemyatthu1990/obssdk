package io.github.yemyatthu1990.apm;

import java.math.BigInteger;

public class Utils {
    public static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }
}
