# Recursion
Recursion is a technique in programming where a function calls itself.

## R1
[//]: # (MAIN: global)
Test the use of recursion in the same module/file.

```json
{
  "directLinks": [
    ["<global>", "R1.factorial"],
    ["R1.factorial", "R1.factorial"]
  ],
  "indirectLinks": []
}
```
```python
# r/R1.py

def factorial(n):
    if n == 0:
        return 1
    return n * factorial(n - 1)

factorial(5)
```
[//]: # (END)

## R2
[//]: # (MAIN: global)
Test the use of a function with a high number of recursions.

```json
{
  "directLinks": [
    ["<global>", "R2.count_down"],
    ["R2.count_down", "R2.count_down"]
  ],
  "indirectLinks": []
}
```
```python
# r/R2.py

def count_down(n):
    if n == 0:
        return 0
    return count_down(n - 1)

count_down(1000000)
```
[//]: # (END)

## R3
[//]: # (MAIN: global)
Test endless recursion.

```json
{
  "directLinks": [
    ["<global>", "R3.endless_recursion"],
    ["R3.endless_recursion", "R3.endless_recursion"]
  ],
  "indirectLinks": []
}
```
```python
# r/R3.py

def endless_recursion():
    return endless_recursion()

endless_recursion()
```
[//]: # (END)
