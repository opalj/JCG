# Closures

## CL1
[//]: # (MAIN: global)
Test the use of a closure depending on a function parameter.

```json
{
  "directLinks": [
    ["<global>", "CL1.inner"],
    ["CL1.inner", "CL1.foo"]
  ],
  "indirectLinks": []
}
```
```python
# cl/CL1.py

def outer(fn):
    def inner():
        return fn()
    return inner

def foo():
    return 1

f = outer(foo)
f()
```
[//]: # (END)

## CL2
[//]: # (MAIN: global)
Test closure scope chain calls.

```json
{
  "directLinks": [
    ["<global>", "CL2.sum_"],
    ["<global>", "CL2.sum2"],
    ["<global>", "CL2.sum3"],
    ["CL2.sum3", "CL2.outside"]
  ],
  "indirectLinks": []
}
```
```python
# cl/CL2.py

def outside():
    return 10

def sum_(a):
    def sum2(b):
        def sum3(c):
            return a + b + c + outside()
        return sum3
    return sum2

sum_(1)(2)(3)
```
[//]: # (END)