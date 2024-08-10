# Higher Order Functions
Higher Order Functions (HOF) are functions that take other functions as arguments or return functions as results. 

## HOF1
[//]: # (MAIN: global)
Test the use of a function as an argument to another function.

```json
{
  "directLinks": [
    ["HOF1.foo", "HOF1.bar"]
  ],
  "indirectLinks": []
}
```
```js
// af/HOF1.js

function foo(f) {
    f(1)
}

function bar(x) {
    return x;
}

foo(bar);
```
[//]: # (END)

## HOF2
[//]: # (MAIN: global)
Test the use of a function as a return value of another function.

```json
{
  "directLinks": [
    ["<global>", "HOF2.foo"],
    ["<global>", "HOF2.bar"]
  ],
  "indirectLinks": []
}
```
```js
// af/HOF2.js

function foo() {
    return bar;
}

function bar(x) {
    return x;
}

foo()(1);
```
[//]: # (END)

## HOF3
[//]: # (MAIN: global)
Test the use of a function as return value but stored in variable

```json
{
  "directLinks": [
    ["<global>", "HOF2.foo"],
    ["<global>", "HOF2.bar"]
  ],
  "indirectLinks": []
}
```
```js
// af/HOF3.js

function foo() {
    return bar;
}

function bar(x) {
    return x;
}

var x = foo()
x(1);
```
[//]: # (END)
