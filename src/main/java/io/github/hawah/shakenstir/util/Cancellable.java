package io.github.hawah.shakenstir.util;

public class Cancellable {

    private final boolean cancel;

    private static final Cancellable CANCEL = new Cancellable(true);
    private static final Cancellable CONTINUE = new Cancellable(false);

    private Cancellable(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isCanceled() {
        return cancel;
    }

    public static Cancellable cancel() {
        return CANCEL;
    }

    public static Cancellable continua() {
        return CONTINUE;
    }
}
