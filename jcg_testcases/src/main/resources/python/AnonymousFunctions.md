# Anonymous Functions
Anonymous functions, also called Lambda functions, are functions that are not bound to an identifier.

## AF1
[//]: # (MAIN: global)
Test the use of an anonymous function as an argument to another function.

```json
{
  "directLinks": [
    ["AF1.foo", "AF1.<anonymous:5>"]
  ],
  "indirectLinks": []
}
```
```python
# af/AF1.py

def foo(f):
    return f(1)
    
foo(
    lambda x: x+1
)
```
[//]: # (END)

## AF2
[//]: # (MAIN: global)
Test the use of multiple anonymous functions as arguments to another function.

```json
{
  "directLinks": [
    ["AF2.foo", "AF2.<anonymous:7>"],
    ["AF2.foo", "AF2.<anonymous:8>"]
  ],
  "indirectLinks": []
}
```
```python
# af/AF2.py

def foo(f1, f2):
    x = f1(1)
    y = f2(1)
    return x + y

foo(
    lambda x: x, 
    lambda y: y
)
```
[//]: # (END)

## AF3
[//]: # (MAIN: global)
Test the use of an anonymous function calling another anonymous function.

```json
{
  "directLinks": [
    ["AF4.foo", "AF4.<anonymous:8>"],
    ["AF4.<anonymous:8>", "AF4.<anonymous:4>"]
  ],
  "indirectLinks": []
}
```
```python
# af/AF3.py

def foo(f):
    f(
        lambda x: x + 1
    )

foo(
    lambda x: x(1)
)
```
[//]: # (END)