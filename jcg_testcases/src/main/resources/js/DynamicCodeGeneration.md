# Dynamic Code Generation

## DCG1
[//]: # (MAIN: global)
Test a simple function call using eval.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG1.foo"]
}
```
```js
// dcg/DCG1.js

function foo() {
    return 2;
}

eval("foo()");
```
[//]: # (END)

## DCG2
[//]: # (MAIN: global)
Test a simple function call using Function constructor.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG2.foo"]
}
```
```js
// dcg/DCG2.js

function foo() {
    return 2;
}

new Function("return foo()")();
```
[//]: # (END)

## DCG3
[//]: # (MAIN: global)
Test a more complex function call using Function constructor.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG3.foo"]
}
```
```js
// dcg/DCG3.js

function foo() {
    return 2;
}

Function("return foo()")();
```
[//]: # (END)

## DCG4
[//]: # (MAIN: global)
Test a simple function call using setTimeout.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG4.foo"]
}
```
```js
// dcg/DCG4.js

function foo() {
    return 2;
}

setTimeout("foo()", 1000);
```
[//]: # (END)

## DCG5
[//]: # (MAIN: global)
Test a simple function call using setInterval.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG5.foo"]
}
```
```js
// dcg/DCG5.js

function foo() {
    return 2;
}

setInterval("foo()", 1000);
```
[//]: # (END)