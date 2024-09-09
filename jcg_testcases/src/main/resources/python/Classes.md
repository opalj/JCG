# Classes

## C1
[//]: # (MAIN: global)
Test the call to a class method.

```json
{
  "directLinks": [
    ["<global>", "C1.Foo.foo"]
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
    ["C2.Foo.bar", "C2.Foo.foo"]
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
Test inheritance

```json
{
  "directLinks": [
    ["<global>", "C3.Bar.foo"]
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
    ["<global>", "C4.Three.foo"]
  ],
  "indirectLinks": []
}
```
```python
# classes/C3.py

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
    ["C5.Foo.bar", "C5.Foo.foo"]
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
