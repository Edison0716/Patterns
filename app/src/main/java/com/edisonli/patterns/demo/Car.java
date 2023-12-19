package com.edisonli.patterns.demo;

import com.edisonli.patterns.anno.BuilderAnno;

@BuilderAnno
public class Car {

    private final String color;
    private final String brand;
    private final String model;

    public Car(String color, String brand, String model) {
        this.color = color;
        this.brand = brand;
        this.model = model;
    }
}
