# Higher Order Functions
Higher Order Functions (HOF) are functions that take other functions as arguments or return functions as results. 

## HOF1
[//]: # (MAIN: global)
Test the use of a function as an argument to another function.

```json
{
  "nodes": [
    "<global>", "HOF1.foo", "HOF1.bar"
  ],
  "directLinks": [
    ["<global>", "HOF1.foo"],
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
  "nodes": [
    "<global>", "HOF2.foo", "HOF2.bar", "HOF2.main"
  ],
  "directLinks": [
    ["<global>", "HOF2.main"],
    ["HOF2.main", "HOF2.foo"],
    ["HOF2.main", "HOF2.bar"]
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

function main() {
    foo()(1);
}

main();
```
[//]: # (END)

## HOF3
[//]: # (MAIN: global)
Test the use of multiple functions as arguments to another function.

```json
{
  "nodes": [
    "<global>", "HOF3.foo", "HOF3.bar", "HOF3.main"
  ],
  "directLinks": [
    ["<global>", "HOF3.foo"],
    ["HOF3.foo", "HOF3.bar"],
    ["HOF3.foo", "HOF3.baz"]
  ],
  "indirectLinks": []
}
```
```js
// af/HOF3.js

function foo(f1, f2) {
    f1(1);
    f2(1);
}

function bar(x) {
    return x;
}

function baz(x) {
    return x;
}

foo(bar, baz);
```
[//]: # (END)