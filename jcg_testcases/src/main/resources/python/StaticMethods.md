# Static Methods

## SM1
[//]: # (MAIN: global)
Test the use of static methods defined in a class.

```json
{
  "directLinks": [
    ["<global>", "SM1.foo"]
  ],
  "indirectLinks": []
}
```
```python
# sm/SM1.py

class C1:
    @staticmethod
    def foo():
        return 1

C1.foo()
```
[//]: # (END)

## SM2
[//]: # (MAIN: global)
Test the use of a static method defined using staticmethod().

```json
{
  "directLinks": [
    ["<global>", "SM2.foo"]
  ],
  "indirectLinks": []
}
```
```python
# bi/SM2.py

class C1:
    def foo():
        return 1

C1.foo = staticmethod(C1.foo)
C1.foo()
```
[//]: # (END) 


