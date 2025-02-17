# Operator Overloading
Python allows to define custom behavior for operators, this is known as operator overloading.

## OO1
[//]: # (MAIN: global)
Test overloading the `[]` operator.

```json
{
  "directLinks": [
    ["<global>", "OO1.Foo.__getitem__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO1.py

class Foo:
    def __init__(self, x):
        self.x = [x]

    def __getitem__(self, key):
        return self.x[key]

f1 = Foo(1)
f1[0]
```
[//]: # (END)

## OO2
[//]: # (MAIN: global)
Test overloading the `==` operator.

```json
{
  "directLinks": [
    ["<global>", "OO2.Foo.__eq__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO2.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __eq__(self, other):
        return self.x == other.x

f1 = Foo(1)
f2 = Foo(2)
f1 == f2
```
[//]: # (END)

## OO3
[//]: # (MAIN: global)
Test overloading the `in` operator.

```json
{
  "directLinks": [
    ["<global>", "OO3.Foo.__contains__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO3.py

class Foo:
    def __init__(self, items):
        self.items = items

    def __contains__(self, item):
        return item in self.items

f1 = Foo([1, 2, 3])
2 in f1
```
[//]: # (END)

## OO4
[//]: # (MAIN: global)
Test overloading the `setitem` operator.

```json
{
  "directLinks": [
    ["<global>", "OO4.Foo.__setitem__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO4.py

class Foo:
    def __init__(self):
        self.data = {}

    def __setitem__(self, key, value):
        self.data[key] = value

f1 = Foo()
f1['a'] = 1
```
[//]: # (END)

## OO5
[//]: # (MAIN: global)
Test overloading the `!=` operator.

```json
{
  "directLinks": [
    ["<global>", "OO5.Foo.__ne__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO5.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __ne__(self, other):
        return self.x != other.x

f1 = Foo(1)
f2 = Foo(2)
f1 != f2
```
[//]: # (END)

## OO6
[//]: # (MAIN: global)
Test overloading the `<` operator.

```json
{
  "directLinks": [
    ["<global>", "OO6.Foo.__lt__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO6.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __lt__(self, other):
        return self.x < other.x

f1 = Foo(1)
f2 = Foo(2)
f1 < f2
```
[//]: # (END)

## OO7
[//]: # (MAIN: global)
Test overloading the `+` operator.

```json
{
  "directLinks": [
    ["<global>", "OO7.Foo.__add__"]
  ],
  "indirectLinks": []
}
```
```python
# oo/OO7.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __add__(self, other):
        return self.x + other.x

f1 = Foo(1)
f2 = Foo(2)
f1 + f2
```
[//]: # (END)
