# Higher Order Functions
Higher Order Functions (HOF) are functions that take other functions as arguments or return functions as results.

## HOF1
[//]: # (MAIN: global)
Test the use of a function as an argument to another function.

```json
{
  "directLinks": [
    ["HOF1.foo", "HOF1.bar"]
  ],
  "indirectLinks": []
}
```
```python
# af/HOF1.py

def foo(f):
    f(1)

def bar(x):
    return x

foo(bar)
```
[//]: # (END)

## HOF2
[//]: # (MAIN: global)
Test the use of a function as a return value of another function.

```json
{
  "directLinks": [
    ["<global>", "HOF2.foo"],
    ["<global>", "HOF2.bar"]
  ],
  "indirectLinks": []
}
```
```python
# af/HOF2.py

def foo():
    return bar

def bar(x):
    return x

foo()(1)
```
[//]: # (END)

## HOF3
[//]: # (MAIN: global)
Test the use of a function as return value but stored in variable

```json
{
  "directLinks": [
    ["<global>", "HOF3.foo"],
    ["<global>", "HOF3.bar"]
  ],
  "indirectLinks": []
}
```
```python
# af/HOF3.py

def foo():
    return bar

def bar(x):
    return x

f = foo()
f(1)
```
[//]: # (END)


