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
Test a simple function call using new Function.

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
Test a simple function call using Function constructor.

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

## DCG6
[//]: # (MAIN: global)
Test a simple function call using dynamic property access.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG6.foo"]
}
```
```js
// dcg/DCG6.js

function foo() {
    return 2;
}

var bar = {};
bar["a"] = foo;

bar["a"]();
```
[//]: # (END)

## DCG7
[//]: # (MAIN: global)
Test a simple function call using more complex dynamic property access. 
Here static expression needs to be evaluated first to compute the property name.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG7.foo"]
}
```
```js
// dcg/DCG7.js

function foo() {
    return 2;
}

var bar = {};
bar["aa"] = foo;

bar["a" + "a"]();
```
[//]: # (END)

## DCG8
[//]: # (MAIN: global)
Test property access depending on a function call.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG8.foo"]
}
```
```js
// dcg/DCG8.js

function foo() {
    return 1;
}

function getPropertyName() {
    return "a";
}

var bar = {};
bar[getPropertyName()] = foo;

bar["a"];
```
[//]: # (END)