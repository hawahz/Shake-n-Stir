package io.github.hawah.shakenstir.foundation.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;

public abstract class AutoUpdateBlockEntity extends BlockEntity {
    // TODO: 人工审查 - 2026-09-03 - 新增静态 Logger 字段,替换原内联 LOGGER 调用。
    //  原代码每次写日志都会重新解析调用类并创建 Logger,改为类级静态常量后仅创建一次。
    private static final Logger LOGGER = LogUtils.getLogger();
    public AutoUpdateBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            tag = output.buildResult();
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void markChanged() {
        setChanged();
        if (getLevel() instanceof ServerLevel serverLevel){
            serverLevel.players().forEach(
                    player -> player.connection.send(getUpdatePacket())
            );
            // FIXME
            //noinspection UnstableApiUsage
            net.neoforged.neoforge.attachment.AttachmentSync.syncBlockEntityUpdates(this, serverLevel.players());
        }
    }
}
