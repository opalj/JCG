# Nested Definitions

## ND1
[//]: # (MAIN: global)
Test the use of a function definition inside another function definition.

```json
{
  "directLinks": [
    ["ND1.foo", "ND1.bar"]
  ],
  "indirectLinks": []
}
```
```js
// af/ND1.js

function foo(f) {
    function bar(x) {
        return x;
    }
    
    bar();
}

foo();
```
[//]: # (END)

## ND2
[//]: # (MAIN: global)
Test a nested function calling an outside function.

```json
{
  "directLinks": [
    ["<global>", "ND2.foo"],
    ["ND2.foo", "ND2.bar"],
    ["ND2.bar", "ND2.baz"]
  ],
  "indirectLinks": []
}
```
```js
// af/ND2.js

function baz() {
    return 0;
}

function foo() {
    function bar(x) {
        baz();
    }
    bar();
}

foo();
```
[//]: # (END)

## ND3
[//]: # (MAIN: global)
Test the use of nested function definitions with overlapping names.

```json
{
  "directLinks": [
    ["ND3.foo", "ND3.bar:11"],
    ["ND3.baz", "ND3.bar:3"]
  ],
  "indirectLinks": []
}
```
```js
// af/ND3.js

function baz() {
    function bar(x) {
        return x;
    }
    
    bar();
}

function foo() {
    function bar(x) {
        return x + 1;
    }
    
    bar();
}

foo();
baz();
```
[//]: # (END)