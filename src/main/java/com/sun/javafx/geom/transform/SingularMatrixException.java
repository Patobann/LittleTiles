package com.sun.javafx.geom.transform;

/**
 * Compatibility replacement for the JavaFX exception used by the archived
 * CreativeCore 1.16 matrix implementation. Modern OpenJDK 8 distributions do
 * not bundle JavaFX.
 */
public class SingularMatrixException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SingularMatrixException() {
        super("Matrix is singular and cannot be inverted");
    }
}
