package io.github.hawah.shakenstir.foundation.datapack;

import io.github.hawah.shakenstir.foundation.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.foundation.datapack.spirit.SpiritData;

public record DrinkData(CocktailType type, SpiritData base) {
}
