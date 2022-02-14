package com.oracle.graalvm.fn;
import java.util.Comparator;
import java.util.stream.Stream;

public class FibonacciFunction {
    public Long handleRequest(String input) {
        try {
            // Convert the received string input to Integer Type
            Integer inputInt = Integer.valueOf(input);
            long result = 0;
            //Compute the associated Fibonacci Number
            result = Stream.iterate( new int[]{0,1}, fib-> new int[]{fib[1], fib[0]+fib[1]} )
                    .limit(inputInt)
                    .map(x->x[1])
                    .max(Comparator.naturalOrder())
                    .get()
                    .longValue();
            return result;
        }catch ( NumberFormatException nfe ){
            System.out.println("Invalid parameter received " + input);
            nfe.printStackTrace();

        }
        // In case of error return -1l
        return -1l;
    }
}