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

    public record MouseMove(boolean cancelled, double mouseX, double mouseY) {
        public static MouseMove empty() {
            return new MouseMove(false, 0, 0);
        }
    }
}
