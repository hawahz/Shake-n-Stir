package io.github.hawah.shakenstir.client.render.item.thirdPerson;

import net.minecraft.client.model.HumanoidModel;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class SnsArmPoses {
    public static final IArmPoseTransformer TEST = (model, entity, arm) -> {
        System.out.println("test");
    };
    public static final EnumProxy<HumanoidModel.ArmPose> GLASSWARE_ARM_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, false, false, TEST);

    public static Object getArmPose(int idx, Class<?> type) {
        return type.cast(switch (idx){
            case 0 -> false;
            case 1 -> false;
            case 2 -> TEST;
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        });
    }
}
