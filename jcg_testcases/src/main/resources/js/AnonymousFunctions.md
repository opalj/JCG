# Anonymous Functions
Anonymous functions, also called Lambda functions, are functions that are not bound to an identifier.

## AF1
[//]: # (MAIN: global)
Test the use of an anonymous function as an argument to another function.

```json
{
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

## AF3
[//]: # (MAIN: global)
Test the use of anonymous function as argument using arrow function syntax.

```json
{
  "directLinks": [
    ["AF3.foo", "AF3.<anonymous:7>"]
  ],
  "indirectLinks": []
}
```
```js
// af/AF3.js

function foo(f) {
    return f(1);
}

foo(
    x => x + 1
);
```
[//]: # (END)

## AF4
[//]: # (MAIN: global)
Test an anonymous function calling another anonymous function.

```json
{
  "directLinks": [
    ["<global>", "AF4.foo"],
    ["AF4.foo", "AF4.<anonymous:11>"],
    ["AF4.<anonymous:12>", "AF4.<anonymous:4>"]
  ],
  "indirectLinks": []
}
```
```js
// af/AF4.js

function foo(f) {
    f(
        function(x) {
            return x + 1;
        }
    );
}

foo(
    function(f) {
        return f(1);
    }
);
```
[//]: # (END)
