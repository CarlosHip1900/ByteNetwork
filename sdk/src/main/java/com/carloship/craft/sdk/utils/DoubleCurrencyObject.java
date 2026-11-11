package com.carloship.craft.sdk.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.codecs.pojo.annotations.BsonIgnore;

public class DoubleCurrencyObject {

    private double value;

    public void add(double i) {
        set(get() + i);
    }

    public void set(double value) {
        this.value = value;
    }

    @JsonIgnore
    @BsonIgnore
    public double get() {
        return value;
    }

    public void remove(double i){
        value -= i;
        if (value < 0) { value = 0; }

        set(value);
    }
}
