# Builtins

## B1
[//]: # (MAIN: global)
Test the use of console.log.

```json
{
  "directLinks": [
    ["<global>", "Native.print"]
  ],
  "indirectLinks": []
}
```
```python
# bi/B1.py

print("Hello World!")
```
[//]: # (END)

## B2
[//]: # (MAIN: global)
Test the use of map.

```json
{
  "directLinks": [
    ["<global>", "Native.map"],
    ["Native.map", "B2.foo"]
  ],
  "indirectLinks": []
}
```
```python
# bi/B2.py

def foo(x):
    return x+1

arr = [1, 2, 3]
arr2 = list(map(foo, arr))
```
[//]: # (END)

## B3
[//]: # (MAIN: global)
Test the use of filter.

```json
{
  "directLinks": [
    ["<global>", "Native.filter"],
    ["Native.filter", "B3.foo"]
  ],
  "indirectLinks": []
}
```
```python
# bi/B3.py

def foo(x):
    return x > 1

arr = [1, 2, 3]
arr2 = list(filter(foo, arr))
```
[//]: # (END)
