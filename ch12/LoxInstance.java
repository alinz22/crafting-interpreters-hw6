package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    private LoxClass klass;
    // protected so LoxClass (which extends LoxInstance) can write static methods in.
    protected final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Token name, Interpreter interpreter) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        if (klass != null) {
            LoxFunction method = klass.findMethod(name.lexeme);
            if (method != null) {
                LoxFunction bound = method.bind(this);
                // Challenge 2: if the method is a getter, call it immediately.
                // The caller sees the computed value, not the function object.
                if (method.isGetter) {
                    return bound.call(interpreter, new ArrayList<>());
                }
                return bound;
            }
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass != null ? klass.name + " instance" : "<class object>";
    }
}
