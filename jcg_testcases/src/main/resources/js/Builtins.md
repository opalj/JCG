# Builtins

## B1
[//]: # (MAIN: global)
Test the use of console.log.

```json
{
  "directLinks": [
    ["<global>", "Native.console_log"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B1.js

console.log("Hello World!");
```
[//]: # (END)

## B2
[//]: # (MAIN: global)
Test the use of map.

```json
{
  "directLinks": [
    ["<global>", "Native.Array_prototype_map"],
    ["Native.Array_prototype_map", "B2.foo"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B2.js

function foo(x) {
    return x+1;
}

const arr = [1, 2, 3];
const arr2 = arr.map(foo);
```
[//]: # (END)

## B3
[//]: # (MAIN: global)
Test the use of forEach.

```json
{
  "directLinks": [
    ["<global>", "Native.Array_prototype_foreach"],
    ["Native.Array_prototype_foreach", "B3.foo"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B3.js

function foo(x) {
    return x+1;
}

const arr = [1, 2, 3];
arr.forEach(foo);
```
[//]: # (END)

## B4
[//]: # (MAIN: global)
Test the use of reduce.

```json
{
  "directLinks": [
    ["<global>", "Native.Array_prototype_reduce"],
    ["Native.Array_prototype_reduce", "B4.foo"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B4.js

function foo(x, y) {
    return x + y;
}

const arr = [1, 2, 3];
const sum = arr.reduce(foo);
```
[//]: # (END)

## B5
[//]: # (MAIN: global)
Test the use of filter.

```json
{
  "directLinks": [
    ["<global>", "Native.Array_prototype_filter"],
    ["Native.Array_prototype_filter", "B5.foo"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B5.js

function foo(x) {
    return x > 1;
}

const arr = [1, 2, 3];
const filtered = arr.filter(foo);
```
[//]: # (END)

## B6
[//]: # (MAIN: global)
Test the use of every.

```json
{
  "directLinks": [
    ["<global>", "Native.Array_prototype_every"],
    ["Native.Array_prototype_every", "B6.foo"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B6.js

function foo(x) {
    return x > 1;
}

const arr = [1, 2, 3];
const result = arr.every(foo);
```
[//]: # (END)

## B7
[//]: # (MAIN: global)
Test the use of some.

```json
{
  "directLinks": [
    ["<global>", "Native.Array_prototype_some"],
    ["Native.Array_prototype_some", "B7.foo"]
  ],
  "indirectLinks": []
}
```
```js
// bi/B7.js

function foo(x) {
    return x > 1;
}

const arr = [1, 2, 3];
const result = arr.some(foo);
```
[//]: # (END)
