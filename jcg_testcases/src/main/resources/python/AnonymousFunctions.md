# Anonymous Functions
Anonymous functions, also called Lambda functions, are functions that are not bound to an identifier.

## AF1
[//]: # (MAIN: global)
Test the use of an anonymous function as an argument to another function.

```json
{
  "nodes": [
    "<global>", "AF1.foo", "AF1.<anonymous:5>"
  ],
  "directLinks": [
    ["<global>", "AF1.foo"],
    ["AF1.foo", "AF1.<anonymous:5>"]
  ],
  "indirectLinks": []
}
```
```python
# af/AF1.py

def foo(f):
    f(1)
    
foo(lambda x: x+1)
```
[//]: # (END)

## AF2
[//]: # (MAIN: global)
Test the use of an anonymous function stored in a variable.

```json
{
  "nodes": [
    "<global>", "AF2.foo", "AF2.<anonymous:2>"
  ],
  "directLinks": [
    ["<global>", "AF2.<anonymous:2>"]
  ],
  "indirectLinks": []
}
```
```python
# af/AF2.py

f = lambda x: x+1
f(1)
```
[//]: # (END)

## AF3
[//]: # (MAIN: global)
Test the use of multiple anonymous functions as arguments to another function.

```json
{
  "nodes": [
    "<global>", "AF3.foo", "AF3.<anonymous:5>", "AF3.<anonymous:8>"
  ],
  "directLinks": [
    ["<global>", "AF3.foo"],
    ["AF3.foo", "AF3.<anonymous:7>"],
    ["AF3.foo", "AF3.<anonymous:8>"]
  ],
  "indirectLinks": []
}
```
```python
# af/AF3.py

def foo(f1, f2):
    x = f1(1)
    y = f2(1)
    return x + y

foo(lambda x: x, 
    lambda y: y)
```
[//]: # (END)