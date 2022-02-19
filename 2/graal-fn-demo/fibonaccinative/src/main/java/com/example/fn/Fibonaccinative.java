/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fn;

import java.util.Comparator;
import java.util.stream.Stream;

public class Fibonaccinative {

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
            System.out.println("Invalid parameter received in Fibonaci Native " + input);
            nfe.printStackTrace();

        }
        // In case of error return -1l
        return -1l;
    }

}