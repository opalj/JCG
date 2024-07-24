# Anonymous Functions
Anonymous functions, also called Lambda functions, are functions that are not bound to an identifier.

## AF1
[//]: # (MAIN: global)
Test the use of an anonymous function as an argument to another function.

```json
{
  "nodes": [
    "<global>", "AF1.foo", "AF1.<anonymous:7>"
  ],
  "directLinks": [
    ["AF1.foo", "AF1.<anonymous:7>"]
  ],
  "indirectLinks": []
}
```
```js
// af/AF1.js

function foo(f) {
    return f(1);
}

foo(
    function(x) {
        return x + 1;
    }
);
```
[//]: # (END)

## AF2
[//]: # (MAIN: global)
Test the use of multiple anonymous functions as arguments to another function.

```json
{
  "nodes": [
    "AF2.foo", "AF2.<anonymous:9>", "AF2.<anonymous:12>"
  ],
  "directLinks": [
    ["AF2.foo", "AF2.<anonymous:9>"],
    ["AF2.foo", "AF2.<anonymous:12>"]
  ],
  "indirectLinks": []
}
```
```js
// af/AF2.js

function foo(f1, f2) {
    const x = f1(1);
    const y = f2(1);
    return x + y;
}

foo(
    function(x) {
        return x;
    }, 
    function(x) {
        return x;
    }
);
```
[//]: # (END)