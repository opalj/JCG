# Modules
JS modules allow you to split your code into multiple files. This can help keep code organized and maintainable. 

## M1
[//]: # (MAIN: global)
Test the use of a module that exports a single function.

```json
{
  "nodes": [
    "<global>", "M1.foo"
  ],
  "directLinks": [
    ["<global>", "M1.foo"]
  ],
  "indirectLinks": []
}
```
```js
// modules/M1.js

export function foo(x) {
    return x + 1;
}
```
```js
//modules/Main.js
import { foo } from './M1.js';

foo(1);
```
[//]: # (END)

## M2
[//]: # (MAIN: global)
Test the use of a module that exports multiple functions.

```json
{
  "nodes": [
    "<global>", "M2.foo", "M2.bar"
  ],
  "directLinks": [
    ["<global>", "M2.foo"],
    ["<global>", "M2.bar"]
  ],
  "indirectLinks": []
}
```
```js
// modules/M2.js

export function foo(x) {
    return x + 1;
}

export function bar(x) {
    return x + 2;
}
```
```js
//modules/Main.js
import { foo, bar } from './M2.js';

foo(1);
bar(2);
```
[//]: # (END)

## M3
[//]: # (MAIN: global)
Test the use of CommonJS style modules.

```json
{
  "nodes": [
    "<global>", "M3.foo", "M3.bar"
  ],
  "directLinks": [
    ["<global>", "M3.foo"],
    ["<global>", "M3.bar"]
  ],
  "indirectLinks": []
}
```
```js
// modules/M3.js

function foo(x) {
    return x + 1;
}

function bar(x) {
    return x + 2;
}

exports.foo = foo;
exports.bar = bar;

```
```js
//modules/Main.js

const m3 = require('./M3.js');

m3.foo(1);
m3.bar(2);
```
[//]: # (END)