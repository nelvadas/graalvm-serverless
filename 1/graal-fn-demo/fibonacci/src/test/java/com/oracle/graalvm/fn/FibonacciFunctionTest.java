package com.oracle.graalvm.fn;
import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class FibonacciFunctionTest {
    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();
    @Test
    public void shouldReturnFibonaciNumber() {
        testing.givenEvent()
                .withBody("8")
                .enqueue();
        testing.thenRun(FibonacciFunction.class, "handleRequest");
        FnResult result = testing.getOnlyResult();
        assertEquals("21", result.getBodyAsString());
    }
}