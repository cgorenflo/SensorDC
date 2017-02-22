package com.sensordc.sensors;

import rx.functions.Func1;

class Rule<T> {
    private final Func1<T, Boolean> rule;

    Rule(Func1<T, Boolean> rule) {

        this.rule = rule;
    }

    boolean validate(T input) {
        return rule.call(input);
    }

    void reset() {
        rule.call(null);
    }
}
