# Closures

## CL1
[//]: # (MAIN: global)
Test the use of a closure depending on a function parameter.

```json
{
  "directLinks": [
    ["<global>", "CL1.inner"],
    ["CL1.inner", "CL1.foo"]
  ],
  "indirectLinks": []
}
```
```js
// cl/CL1.js

function outer(fn) {
    return function inner() {
        return fn();
    }
}

function foo() {
    return 1;
}

const f = outer(foo);
f();
```
[//]: # (END)

## CL2
[//]: # (MAIN: global)
Test closure scope chain calls.

```json
{
  "directLinks": [
    ["<global>", "CL2.sum"],
    ["<global>", "CL2.sum2"],
    ["<global>", "CL2.sum3"],
    ["CL2.sum3", "CL2.outside"]
  ],
  "indirectLinks": []
}
```
```js
// cl/CL2.js

function sum(a) {
    function outside() {
        return 10;
    }
    
    return function sum2(b) {
        return function sum3(c) {
            return a + b + c + outside();
        }
    }
}

sum(1)(2)(3);
```
[//]: # (END)