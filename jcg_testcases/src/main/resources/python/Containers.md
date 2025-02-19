# Containers
In Python, containers may hold functions as values. These tests check how the functions are tracked when they are stored in different types of containers.

## CO1
[//]: # (MAIN: global)
Test if a list of functions is correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "CO1.foo"]
  ],
  "indirectLinks": []
}
```
```python
# co/CO1.py

def bar():
    return 1

def foo():
    return 2

f = [bar, foo]
f[1]()
```
[//]: # (END)

## CO2
[//]: # (MAIN: global)
Test if a dictionary of functions is correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "CO2.foo"]
  ],
  "indirectLinks": []
}
```
```python
# co/CO2.py

def bar():
    return 1

def foo():
    return 2

m = {"a": bar, "b": foo}
m["b"]()
```
[//]: # (END)

## CO3
[//]: # (MAIN: global)
Test if pop function is correctly handled.

```json
{
  "directLinks": [
    ["<global>", "CO3.foo"]
  ],
  "indirectLinks": []
}
```
```python
# co/CO3.py

def bar():
    return 1

def foo():
    return 2

s = [bar, foo]
s.pop()()
```
[//]: # (END)

## CO4
[//]: # (MAIN: global)
Test if a tuple of functions is correctly tracked.

```json
{
  "directLinks": [
    ["<global>", "CO4.bar"]
  ],
  "indirectLinks": []
}
```
```python
# co/CO4.py

def bar():
    return 1

def foo():
    return 2

t = (bar, foo)
t[0]()
```
[//]: # (END)
