package main

import (
	"context"
	"encoding/json"
	"io"
	"log"

	fdk "github.com/fnproject/fdk-go"
)

func main() {
	fdk.Handle(fdk.HandlerFunc(myHandler))
}

type Fib struct {
	Input int `json:"input"`;
        Output int `json:"output"`
}


func fibonacci(n int) int {
	first, second := 0, 1
	for i:=0;i<n;i++{
		first, second = second, first+second
         }
	return first
}

func myHandler(ctx context.Context, in io.Reader, out io.Writer) {
	f := &Fib{Input: 10}
	json.NewDecoder(in).Decode(f)
	f.Output  = fibonacci(f.Input)
        log.Print("Inside Go Fibonacci function")
	json.NewEncoder(out).Encode(&f)
}
