# Dynamic Code Generation
Dynamic code generation allows the creation of new code at runtime. 
This can be done using `eval`, the `Function` constructor, or other methods.

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

var code = "var z = 1;";
code += "return";
code += " foo";
code += "()";

new Function(code)();
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
Test a more complex function call using eval.

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

var code = "var z = 1;";
code += "foo()";

eval(code);
```

[//]: # (END)

## DCG7
[//]: # (MAIN: global)
Test a more complex function call using setTimeout.

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

var code = "var z = 1;";
code += "foo()";

setTimeout(code, 1000);
```

[//]: # (END)

## DCG8
[//]: # (MAIN: global)
Test a more complex function call using setInterval.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG8.foo"]
}
```
```js
// dcg/DCG8.js

function foo() {
    return 2;
}

var code = "var z = 1;";
code += "foo()";

setInterval(code, 1000);
```

[//]: # (END)
