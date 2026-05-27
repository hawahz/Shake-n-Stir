package io.github.hawah.shakenstir.content.entity.ai.sensor;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Sensors {
    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(Registries.SENSOR_TYPE, ShakenStir.MODID);

    public static final Supplier<SensorType<BarCounterSensor>> BAR_COUNTER_SENSOR = register("bar_counter_sensor", BarCounterSensor::new);
    public static final Supplier<SensorType<UnservedCustomerSensor>> UNSERVED_CUSTOMER_SENSOR = register("unserved_customer_sensor", UnservedCustomerSensor::new);


    public static <T extends Sensor<?>> DeferredHolder<SensorType<?>, SensorType<T>> register(String name, Supplier<T> sensor) {
        return SENSORS.register(name, () -> new SensorType<>(sensor));
    }

    public static void register(IEventBus modEventBus) {
        SENSORS.register(modEventBus);
    }
}
