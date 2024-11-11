# Dynamic Code Generation

## DCG1
[//]: # (MAIN: global)
Test a simple function call using eval.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG1.foo"]
}
```
```python
# dcg/DCG1.py

def foo():
    return 2


eval("foo()")
```
[//]: # (END)

## DCG2
[//]: # (MAIN: global)
Test a constructed function call using eval

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG2.foo"]
}
```
```python
# dcg/DCG2.py

def foo():
    return 2

x = "foo"
eval(f"{x}()")
```
[//]: # (END)

## DCG3
[//]: # (MAIN: global)
Test a function call using exec

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG3.foo"]
}
```
```python
# dcg/DCG3.py

def foo():
    return 2

code = """
x = "test"
x = x + " test2"

foo()
"""

exec(code)
```
[//]: # (END)

## DCG4
[//]: # (MAIN: global)
Test a more complex function call using exec.

```json
{
  "directLinks": [],
  "indirectLinks": ["DCG4.foo"]
}
```
```python
# dcg/DCG4.py

def foo():
    return 2

code = """
x = "test"
"""

code += """
foo()
"""

exec(code)
```
[//]: # (END)

