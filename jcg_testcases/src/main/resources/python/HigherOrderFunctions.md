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
# hof/HOF1.py

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
# hof/HOF2.py

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
# hof/HOF3.py

def foo():
    return bar

def bar(x):
    return x

f = foo()
f(1)
```
[//]: # (END)

## HOF4
[//]: # (MAIN: global)
Test reassignment of a function to a variable

```json
{
  "directLinks": [
    ["<global>", "HOF4.bar"]
  ],
  "indirectLinks": []
}
```
```python
# hof/HOF4.py

def foo():
    return bar

def bar(x):
    return x

f = foo
f = bar
f(1)
```
[//]: # (END)

## HOF5
[//]: # (MAIN: global)
Test if chained assignments are correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "HOF5.bar"]
  ],
  "indirectLinks": []
}
```
```python
# hof/HOF5.py

def bar():
    return 1

f = g = bar
f()
```
[//]: # (END)

## HOF6
[//]: # (MAIN: global)
Test if multi assignments are correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "HOF6.foo"]
  ],
  "indirectLinks": []
}
```
```python
# hof/HOF6.py

def bar():
    return 1
    
def foo():
    return 2

f, g = bar, foo
g()
```
[//]: # (END)


