# Prototypes

## P1
[//]: # (MAIN: global)
Test a basic prototype method call. 

```json
{
  "directLinks": [
    ["<global>", "P1.foo"]
  ],
  "indirectLinks": []
}
```
```js
// prototypes/P1.js
function foo() {}

function A() {}
A.prototype.m = foo;

const a = new A();
a.m();
```
[//]: # (END)

## P2
[//]: # (MAIN: global)
Test basic prototype inheritance.

```json
{
  "directLinks": [
    ["<global>", "P2.foo"]
  ],
  "indirectLinks": []
}
```
```js
// prototypes/P2.js
function foo() {
    return 'animal sound';
}
function Animal() {}
Animal.prototype.speak = foo;

function Dog() {}
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

const dog = new Dog();
dog.speak();

```
[//]: # (END)

## P3
[//]: # (MAIN: global)
Test prototype chain inheritance with method override (see if tool can follow the prototype chain correctly).
TODO: EXPECTED NOT RIGHT, TEST ALSO A BIT WEIRD

```json
{
  "directLinks": [
    ["<global>", "P3.woof"]
  ],
  "indirectLinks": []
}
```
```js
// prototypes/P3.js

function foo() {
    return 'animal sound';
}

function Animal() {}
Animal.prototype.speak = foo;

function Dog() {}
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;
function woof() {
    return 'woof';
}
Dog.prototype.speak = woof;

const dog = new Dog();
dog.speak();

```
[//]: # (END)

## P4
[//]: # (MAIN: global)
Test basic prototype inheritance using object syntax.

```json
{
  "directLinks": [
    ["<global>", "P4.foo:3"]
  ],
  "indirectLinks": []
}
```
```js
// prototypes/P4.js

var obj = {
    foo() {
        return 'bar';
    }
};

var obj2 = Object.create(obj);
obj2.foo();

```
[//]: # (END)

## P5
[//]: # (MAIN: global)
Set prototype using Object.setPrototypeOf.

```json
{
  "directLinks": [
    ["<global>", "P5.foo"]
  ],
  "indirectLinks": []
}
```
```js
// prototypes/P5.js

function foo() {
    return 'foo';
}

function Base() {}
Base.prototype = {
    foo: foo
};

function Derived() {}
Object.setPrototypeOf(Derived.prototype, Base.prototype);

const d = new Derived();
d.foo();

```
[//]: # (END)
