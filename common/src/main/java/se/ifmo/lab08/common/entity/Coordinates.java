package se.ifmo.lab08.common.entity;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Coordinates implements Serializable {
    private long x;
    private Float y;

    private static final long LIMIT_X = 540;
    private static final float LIMIT_Y = 498;


    public Coordinates(long x, Float y) {
        this.x = x;
        this.y = y;
    }

    public static boolean validateX(Long x) {
        return (x != null && x >= 0 && x <= LIMIT_X);
    }

    public static boolean validateY(Float y) {
        return (y != null && y >= 0 && y <= LIMIT_Y);
    }

    public boolean validate() {
        return (validateX(this.x) && validateY(this.y));
    }

    public Coordinates update(Coordinates coordinates) {
        this.x = coordinates.x;
        this.y = coordinates.y;
        return this;
    }

    @Override
    public String toString() {
        return String.format("---- X: %s\n---- Y: %s", x, y);
    }
}