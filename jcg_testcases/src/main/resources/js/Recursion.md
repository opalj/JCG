# Recursion
Recursion is a technique in programming where a function calls itself. 

## R1
[//]: # (MAIN: global)
Test the use of recursion in the same module/file.

```json
{
  "nodes": [
    "<global>", "R1.factorial"
  ],
  "directLinks": [
    ["<global>", "R1.factorial"],
    ["R1.factorial", "R1.factorial"]
  ],
  "indirectLinks": []
}
```
```js
// dt/R1.js

// calculate factorial
function factorial(n) {
    if (n === 0) {
        return 1;
    }
    return n * factorial(n - 1);
}

factorial(5);
```
[//]: # (END)

## R2
[//]: # (MAIN: Main.Main)
Test the use of recursion in another module/file.

```json
{
  "nodes": [
    "<global>", "Main.Main", "R2.fibonacci"
  ],
  "directLinks": [
    ["Main.Main", "R2.fibonacci"],
    ["R2.fibonacci", "R2.fibonacci"]
  ],
  "indirectLinks": []
}
```
```js
// dt/R2.js

// calculate fibonnaci
export function fibonacci(n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}
```
```js
//dt/Main.js

import { fibonacci } from "./R2.js";

function Main() {
    fibonacci(10);
}
```
[//]: # (END)
```