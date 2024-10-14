# Modules

## M1
[//]: # (MAIN: global)
Test a basic function import from module.

```json
{
  "directLinks": [
    ["<global>", "M1.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M1.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

from M1 import foo

foo(1)
```
[//]: # (END)

## M2
[//]: # (MAIN: global)
Test a module import with an alias.

```json
{
  "directLinks": [
    ["<global>", "M2.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M2.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

import M1 as mod_a

mod_a.foo(1)
```
[//]: # (END)

## M3
[//]: # (MAIN: global)
Test multiple function imports from a module.

```json
{
  "directLinks": [
    ["<global>", "M3.foo"],
    ["<global>", "M3.bar"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M3.py

def foo(x):
    return x + 1

def bar(x):
    return x - 1
```
```python
# modules/Main.py

from M3 import foo, bar

foo(1)
bar(1)
```
[//]: # (END)

## M4
[//]: # (MAIN: global)
Test a module import import.

```json
{
  "directLinks": [
    ["<global>", "M4.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M4.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

import M4

M4.foo(1)
```
[//]: # (END)

## M5
[//]: # (MAIN: global)
Test module import with a wildcard.

```json
{
  "directLinks": [
    ["<global>", "M5.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M5.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

from M5 import *

foo(1)
```
[//]: # (END)

## M6
[//]: # (MAIN: global)
Test the use of a local import in a function.

```json
{
  "directLinks": [
    ["<global>", "M6.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M6.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

def bar(x):
    from M6 import foo
    return foo(x)

bar(1)
```
[//]: # (END)

## M7
[//]: # (MAIN: global)
Test importing a Submodule from a package.

```json
{
  "directLinks": [
    ["<global>", "M7.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/Main.py

from submodule.M7 import foo

foo(1)
```
```python
# modules/submodule/M7.py

def foo(x):
    return x + 1
```
```python
# modules/submodule/__init__.py

```
[//]: # (END)

## M8
[//]: # (MAIN: global)
Test dynamic import using ``__import__``.

```json
{
  "directLinks": [
    ["<global>", "M8.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M8.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

mod = __import__('M8')
mod.foo(1)
```
[//]: # (END)

## M9
[//]: # (MAIN: global)
Test conditional import that occurs during runtime.

```json
{
  "directLinks": [
    ["<global>", "M9.foo"]
  ],
  "indirectLinks": []
}
```
```python
# modules/M9.py

def foo(x):
    return x + 1
```
```python
# modules/Main.py

cond = False
cond = not cond

if cond:
    from M9 import foo

foo(1)
```
[//]: # (END)

