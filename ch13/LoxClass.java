package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

// Challenge 2: BETA-style inheritance — superclass method wins, inner() calls down.
// Key change is in findMethod: walk to the TOP of the hierarchy first,
// then return the highest method found instead of the lowest.
class LoxClass extends LoxInstance implements LoxCallable {
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        super(null);
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    // Normal (jlox) lookup — returns FIRST match from the bottom up.
    // Used for regular dispatch in the original interpreter.
    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    // Challenge 2: BETA-style lookup — returns the method from the TOP of the hierarchy.
    // Walk all the way up, then return whichever level defines the method highest.
    LoxFunction findMethodBeta(String name) {
        LoxFunction candidate = null;
        LoxClass current = this;
        while (current != null) {
            if (current.methods.containsKey(name)) {
                candidate = current.methods.get(name);
            }
            current = current.superclass;
        }
        return candidate;
    }

    // Challenge 2: find the next method DOWN from 'fromClass' in the hierarchy
    // rooted at 'this'. Used to implement inner() — the inverse of super.
    LoxFunction findInner(String name, LoxClass fromClass) {
        // Collect the class chain from top to bottom.
        java.util.List<LoxClass> chain = new java.util.ArrayList<>();
        LoxClass c = this;
        while (c != null) { chain.add(c); c = c.superclass; }
        java.util.Collections.reverse(chain); // now bottom-to-top, i.e. subclass first

        // Find where fromClass sits, then look below it for the next method.
        boolean found = false;
        for (LoxClass klass : chain) {
            if (found && klass.methods.containsKey(name)) {
                return klass.methods.get(name);
            }
            if (klass == fromClass) found = true;
        }
        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public String toString() { return name; }
}
