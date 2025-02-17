# Dynamic Property Access

## DPA1
[//]: # (MAIN: global)
Test a simple function call using dynamic property access.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA1.foo"]
}
```
```python
# dpa/DPA1.py

def foo():
    return 2

class Bar:
    pass

bar = Bar()
setattr(bar, "a", foo)

# Call the function using getattr
getattr(bar, "a")()
```
[//]: # (END)

## DPA2
[//]: # (MAIN: global)
Test a simple function call using more complex dynamic property access.
Here static expression needs to be evaluated first to compute the property name.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA2.foo"]
}
```
```python
# dpa/DPA2.py

def foo():
    return 2

class Bar:
    pass

bar = Bar()
setattr(bar, "aa", foo)

property_name = "a" + "a"
getattr(bar, property_name)()
```
[//]: # (END)

## DPA3
[//]: # (MAIN: global)
Test property access depending on a function call.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA3.foo"]
}
```
```python
# dpa/DPA3.py

def foo():
    return 1

def get_property_name():
    return "a"

class Bar:
    pass

bar = Bar()
setattr(bar, get_property_name(), foo)

getattr(bar, "a")()
```
[//]: # (END)

## DPA4
[//]: # (MAIN: global)
Test property access depending on conditional.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA4.foo"]
}
```
```python
# dpa/DPA4.py

def foo():
    return 1

class Bar:
    pass

bar = Bar()
cond = False

if cond:
    setattr(bar, "a", foo)
else:
    setattr(bar, "b", foo)

getattr(bar, "b")()
```
[//]: # (END)

## DPA5
[//]: # (MAIN: global)
Test a more complex dynamic property access scenario.

```json
{
  "directLinks": [],
  "indirectLinks": ["DPA5.foo"]
}
```
```python
# dpa/DPA5.py

def foo():
    return 1

def get_name(n):
    if n == 1:
        return "a"
    else:
        return get_name(n - 1)

class Bar:
    pass

bar = Bar()
setattr(bar, get_name(100000), foo)

getattr(bar, "a")()
```
[//]: # (END)
