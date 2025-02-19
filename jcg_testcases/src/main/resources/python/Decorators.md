# Decorators

## D1
[//]: # (MAIN: global)
Test the use of simple decorator.

```json
{
  "directLinks": [
    ["<global>", "D1.dec"],
    ["<global>", "D1.foo"]
  ],
  "indirectLinks": []
}
```
```python
# d/D1.py

def dec(func): 
    return func

@dec
def foo():
    return 1

foo()
```
[//]: # (END)

## D2
[//]: # (MAIN: global)
Test the use of multiple decorators.

```json
{
  "directLinks": [
    ["<global>", "D2.dec1"],
    ["<global>", "D2.dec2"],
    ["<global>", "D2.foo"]
  ],
  "indirectLinks": []
}
```
```python
# d/D2.py

def dec1(func): 
    return func

def dec2(func):
    return func

@dec1
@dec2
def foo():
    return 1

foo()
```
[//]: # (END)

## D3
[//]: # (MAIN: global)
Test the use of simple decorator.

```json
{
  "directLinks": [
    ["<global>", "D3.foo"],
    ["D3.wrapper", "D3.foo"]
  ],
  "indirectLinks": []
}
```
```python
# d/D3.py

def dec(func):
    def wrapper():
        foo()
        
    return wrapper

@dec
def foo():
    pass

foo()
```
[//]: # (END)

## D4
[//]: # (MAIN: global)
Test the use of nested decorators calling each other.

```json
{
  "directLinks": [
    ["<global>", "D4.dec1"],
    ["D4.wrapper1", "D4.wrapper2"],
    ["D4.wrapper2", "D4.foo"]
  ],
  "indirectLinks": []
}
```
```python
# d/D4.py

def dec1(func): 
    def wrapper1():
        return func()
    return wrapper1

def dec2(func):
    def wrapper2():
        return func()
    return wrapper2

@dec1
@dec2
def foo():
    return 1

foo()

```
[//]: # (END)


