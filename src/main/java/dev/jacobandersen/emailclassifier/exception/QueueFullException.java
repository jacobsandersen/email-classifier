package dev.jacobandersen.emailclassifier.exception;

public final class QueueFullException extends RuntimeException {
    public QueueFullException() {
        super("queue is full");
    }
}
