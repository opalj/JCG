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