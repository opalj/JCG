# Dynamic Property Access

## DPA1
[//]: # (MAIN: global)
Test a simple function call using dynamic property access.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA1.foo"]
}
```
```js
// dpa/DPA1.js

function foo() {
    return 2;
}

var bar = {};
bar["a"] = foo;

bar["a"]();
```
[//]: # (END)

## DPA2
[//]: # (MAIN: global)
Test a simple function call using more complex dynamic property access.
Here static expression needs to be evaluated first to compute the property name.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA2.foo"]
}
```
```js
// dpa/DPA2.js

function foo() {
    return 2;
}

var bar = {};
bar["aa"] = foo;

bar["a" + "a"]();
```
[//]: # (END)

## DPA3
[//]: # (MAIN: global)
Test property access depending on a function call.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA3.foo"]
}
```
```js
// dpa/DPA3.js

function foo() {
    return 1;
}

function getPropertyName() {
    return "a";
}

var bar = {};
bar[getPropertyName()] = foo;

bar["a"]();
```
[//]: # (END)

## DPA4
[//]: # (MAIN: global)
Test property access depending on conditional.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA4.foo"]
}
```
```js
// dpa/DPA4.js

function foo() {
    return 1;
}

var bar = {};
var cond = false;

if (cond) {
    bar["a"] = foo;
} else {
    bar["b"] = foo;
}

bar["b"]();
```
[//]: # (END)
