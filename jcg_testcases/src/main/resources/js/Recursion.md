# Recursion
Recursion is a technique in programming where a function calls itself. 

## R1
[//]: # (MAIN: global)
Test the use of recursion in the same module/file.

```json
{
  "directLinks": [
    ["<global>", "R1.factorial"],
    ["R1.factorial", "R1.factorial"]
  ],
  "indirectLinks": []
}
```
```js
// r/R1.js

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
[//]: # (MAIN: global)
Test the use of a function with a high number of recursions.

```json
{
  "directLinks": [
    ["<global>", "R2.countDown"],
    ["R2.countDown", "R2.countDown"]
  ],
  "indirectLinks": []
}
```
```js
// r/R2.js

function countDown(n) {
    if (n === 0) {
        return 0;
    }
    return countDown(n - 1);
}

countDown(1000000);
```
[//]: # (END)

## R3
[//]: # (MAIN: global)
Test endless recursion.

```json
{
  "directLinks": [
    ["<global>", "R3.endlessRecursion"],
    ["R3.endlessRecursion", "R3.endlessRecursion"]
  ],
  "indirectLinks": []
}
```
```js
// r/R3.js

function endlessRecursion() {
    return endlessRecursion();
}

endlessRecursion();
```
[//]: # (END)

