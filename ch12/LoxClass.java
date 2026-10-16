package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

// Challenge 1: LoxClass extends LoxInstance so the class object itself can hold
// static methods in its fields map, making Math.square(3) work via normal get dispatch.
class LoxClass extends LoxInstance implements LoxCallable {
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass,
             Map<String, LoxFunction> methods,
             Map<String, LoxFunction> staticMethods) {
        super(null);
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
        // Static methods live directly on the class-as-instance.
        // visitGetExpr already handles LoxInstance, so Math.square resolves for free.
        for (Map.Entry<String, LoxFunction> entry : staticMethods.entrySet()) {
            fields.put(entry.getKey(), entry.getValue());
        }
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if (superclass != null) {
            return superclass.findMethod(name);
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
    public String toString() {
        return name;
    }
}
