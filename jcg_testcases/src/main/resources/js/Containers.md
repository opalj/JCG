# Containers
In JavaScript, containers may hold functions as values. These tests check how the functions are tracked when they are stored in different types of containers.

## CO1
[//]: # (MAIN: global)
Test if a list of functions is correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "CO1.foo"]
  ],
  "indirectLinks": []
}
```
```js
// co/CO1.js

function bar() {
    return 1;
}

function foo() {
    return 2;
}

var f = [bar, foo];
f[1]();
```
[//]: # (END)

## CO2
[//]: # (MAIN: global)
Test if a Map of functions is correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "CO2.foo"]
  ],
  "indirectLinks": []
}
```
```js
// co/CO2.js

function bar() {
    return 1;
}

function foo() {
    return 2;
}

var m = new Map();
m.set("a", bar);
m.set("b", foo);

m.get("b")();
```
[//]: # (END)

## CO3
[//]: # (MAIN: global)
Test functions in an object.

```json
{
  "directLinks": [
    ["<global>", "CO3.foo"]
  ],
  "indirectLinks": []
}
```
```js
// co/CO3.js

function bar() {
    return 1;
}

function foo() {
    return 2;
}

var o = {
    a: bar,
    b: foo
};

o.b();
```
[//]: # (END)

## CO4
[//]: # (MAIN: global)
Test handling of push and shift.

```json
{
  "directLinks": [
    ["<global>", "CO4.foo"]
  ],
  "indirectLinks": []
}
```
```js
// co/CO4.js

function bar() {
    return 1;
}

function foo() {
    return 2;
}

var f = [];
f.push(foo);
f.push(bar);
f.shift()();
```
[//]: # (END)

## CO5
[//]: # (MAIN: global)
Test handling of push and pop.

```json
{
  "directLinks": [
    ["<global>", "CO5.bar"]
  ],
  "indirectLinks": []
}
```
```js
// co/CO5.js

function bar() {
    return 1;
}

function foo() {
    return 2;
}

var f = [];
f.push(foo);
f.push(bar);

f.pop()();
```
[//]: # (END)
