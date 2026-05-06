package io.github.hawah.shakenstir.util;

public record Result(boolean cancelled) {
    private static final Result EMPTY = new Result(false);
    public static Result empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Result otherEvent = (Result) obj;
        return otherEvent.cancelled == this.cancelled;
    }
}
