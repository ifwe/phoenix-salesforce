package com.salesforce.phoenix.map.reduce;

import org.junit.Test;
import static org.junit.Assert.*;

public class CStringUnescaperTest {

    @Test
    public void test() {
        String[][] tests = new String[][] {
                {"\\\'", "\'"},
                {"\\\"", "\""},
                {"\\f", "\f"},
                {"\\b", "\b"},
                {"\\n", "\n"},
                {"\\t", "\t"},
                {"\\r", "\r"},
                {"\\s", "s"},  // escape not needed
                {"\\", "\\"},  // trailing escape not needed (but left?!)
                {"\"\"", "\"\""},
                {"\\x00", "\u0000"},
                {"\\x27", "\u0027"},
                {"\\xF0\\x9F\\x92\\xA9", "\uD83D\uDCA9"},  // pile of poo test
                {"\\xf0\\x9f\\x92\\xa9", "\uD83D\uDCA9"},
                {"\\Xf0\\X9f\\X92\\Xa9", "\uD83D\uDCA9"}
        };
        for(String[] test: tests) {
            String in = test[0];
            String expected = test[1];
            String out = CStringUnescaper.unescape(in);
            assertEquals(in, expected, out);
        }
    }

}
