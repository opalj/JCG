# Operator Overloading
Python allows to define custom behavior for operators, this is known as operator overloading.

## OO1
[//]: # (MAIN: global)
Test overloading the `+` operator.

```json
{
  "directLinks": [
    ["<global>", "OO1.Foo.__add__"]
  ],
  "indirectLinks": []
}
```
```python
# classes/OO1.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __add__(self, other):
        return self.x + other.x

f1 = Foo(1)
f2 = Foo(2)
f1 + f2
```

## OO2
[//]: # (MAIN: global)
Test overloading the `*` operator.

```json
{
  "directLinks": [
    ["<global>", "OO2.Foo.__mul__"]
  ],
  "indirectLinks": []
}
```
```python
# classes/OO2.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __mul__(self, other):
        return self.x * other.x

f1 = Foo(1)
f2 = Foo(2)
f1 * f2
```

## OO3
[//]: # (MAIN: global)
Test overloading the `-` operator.

```json
{
  "directLinks": [
    ["<global>", "OO3.Foo.__sub__"]
  ],
  "indirectLinks": []
}
```
```python
# classes/OO3.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __sub__(self, other):
        return self.x - other.x

f1 = Foo(1)
f2 = Foo(2)
f1 - f2
```

## OO4
[//]: # (MAIN: global)
Test overloading the `/` operator.

```json
{
  "directLinks": [
    ["<global>", "OO4.Foo.__truediv__"]
  ],
  "indirectLinks": []
}
```
```python
# classes/OO4.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __truediv__(self, other):
        return self.x / other.x

f1 = Foo(1)
f2 = Foo(2)
f1 / f2
```

## OO5
[//]: # (MAIN: global)
Test overloading the `==` operator.

```json
{
  "directLinks": [
    ["<global>", "OO5.Foo.__eq__"]
  ],
  "indirectLinks": []
}
```
```python
# classes/OO5.py

class Foo:
    def __init__(self, x):
        self.x = x

    def __eq__(self, other):
        return self.x == other.x

f1 = Foo(1)
f2 = Foo(2)
f1 == f2
```