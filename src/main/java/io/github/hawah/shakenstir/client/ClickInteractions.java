package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.util.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ClickInteractions {
    public static final List<BiFunction<Double, Double, Result>> mouseMoves = new ArrayList<>();

    public static void registerMouseMove(BiFunction<Double, Double, Result> mouseMove) {
        mouseMoves.add(mouseMove);
    }
}
