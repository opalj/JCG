# Nested Definitions

## ND1
[//]: # (MAIN: global)
Test the use of a function definition inside another function definition.

```json
{
  "directLinks": [
    ["ND1.foo", "ND1.bar"]
  ],
  "indirectLinks": []
}
```
```python
# af/ND1.py

def foo():
    def bar(x):
        return x
    bar()

foo()
```
[//]: # (END)

## ND2
[//]: # (MAIN: global)
Test a nested function calling an outside function.

```json
{
  "directLinks": [
    ["<global>", "ND2.foo"],
    ["ND2.foo", "ND2.bar"],
    ["ND2.bar", "ND2.baz"]
  ],
  "indirectLinks": []
}
```
```python
# af/ND2.py

def baz():
    return 0

def foo():
    def bar(x):
        baz()
    bar()

foo()
```
[//]: # (END)

## ND3
[//]: # (MAIN: global)
Test the use of nested functions with overlapping names.

```json
{
  "directLinks": [
    ["ND3.foo", "ND3.bar:9"],
    ["ND3.baz", "ND3.bar:3"]
  ],
  "indirectLinks": []
}
```
```python
# af/ND3.py

def baz():
    def bar(x):
        return x
    
    bar()

def foo():
    def bar(x):
        return x + 1
    
    bar()

foo()
baz()
```
[//]: # (END)


