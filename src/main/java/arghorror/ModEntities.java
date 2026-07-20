package arghorror;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static EntityType<TheArchitectEntity> THE_ARCHITECT;

    public static void register() {
        THE_ARCHITECT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Arghorror.id("the_architect"),
            FabricEntityTypeBuilder.<TheArchitectEntity>create(MobCategory.MONSTER, TheArchitectEntity::new)
                .dimensions(EntityDimensions.scalable(0.6f, 1.95f))
                .trackRangeBlocks(128)
                .trackedUpdateRate(1)
                .build()
        );
    }
}
