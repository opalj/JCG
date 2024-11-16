# Classes

## C1
[//]: # (MAIN: global)
Test the call to a class method.

```json
{
  "directLinks": [
    ["<global>", "C1.foo"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C1.py

class Foo:
    def foo(self, x):
        return x + 1

f = Foo()
f.foo(1)
```
[//]: # (END)

## C2
[//]: # (MAIN: global)
Test a class method calling another class method on self.

```json
{
  "directLinks": [
    ["C2.bar", "C2.foo"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C2.py

class Foo:
    def foo(self):
        pass

    def bar(self):
        self.foo()
        pass

f = Foo()
f.bar()
```
[//]: # (END)

## C3
[//]: # (MAIN: global)
Test inheritance.

```json
{
  "directLinks": [
    ["<global>", "C3.foo"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C3.py

class Foo:
    def foo(self):
        pass
    
class Bar(Foo):
    pass

b = Bar()
b.foo()
```
[//]: # (END)

## C4
[//]: # (MAIN: global)
Test if diamond problem is correctly resolved, i.e. the MRO is correctly followed.

```json
{
  "directLinks": [
    ["<global>", "C4.foo:11"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C4.py

class One:
    def foo(self):
        pass

class Two(One):
    def foo(self):
        pass

class Three(One):
    def foo(self):
        pass

class Four(Three, Two):
    pass

f = Four()
f.foo()
```
[//]: # (END)

## C5
[//]: # (MAIN: global)
Test method calling method of superclass.

```json
{
  "directLinks": [
    ["C5.bar", "C5.foo"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C5.py

class Foo:
    def foo(self):
        pass

class Bar(Foo):
    def bar(self):
        self.foo()
        pass

b = Bar()
b.bar()
```
[//]: # (END)

## C6
[//]: # (MAIN: global)
Test the use of super() to access overridden method.

```json
{
  "directLinks": [
    ["<global>", "C6.foo:7"],
    ["C6.foo:8", "C6.foo:3"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C6.py

class Foo:
    def foo(self):
        pass

class Bar(Foo):
    def foo(self):
        super().foo()
        pass

b = Bar()
b.foo()
```
[//]: # (END)

## C7
[//]: # (MAIN: global)
Test the use of a constructor.

```json
{
  "directLinks": [
    ["<global>", "C7.__init__"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C7.py

class Foo:
    def __init__(self):
        pass

f = Foo()
```
[//]: # (END)

## C8
[//]: # (MAIN: global)
Test call to super constructor

```json
{
  "directLinks": [
    ["<global>", "C8.__init__:7"],
    ["C8.__init__:8", "C8.__init__:3"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C8.py

class Foo:
    def __init__(self):
        pass

class Bar(Foo):
    def __init__(self):
        super().__init__()
        pass

b = Bar()
```
[//]: # (END)