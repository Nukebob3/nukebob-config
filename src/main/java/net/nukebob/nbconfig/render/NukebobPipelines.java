package net.nukebob.nbconfig.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.nukebob.nbconfig.NukebobConfig;

public class NukebobPipelines {
    public static final RenderPipeline LITTLE_NUKEBOB_GUI;
    public static final RenderPipeline BIG_NUKEBOB_HITBOX;

    static {
        LITTLE_NUKEBOB_GUI = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation("pipeline/little_nukebob_gui")
                        .withFragmentShader(NukebobConfig.id("core/little_nukebob"))
                .build());
        BIG_NUKEBOB_HITBOX = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation("pipeline/big_nukebob_hitbox")
                        .withFragmentShader(NukebobConfig.id("core/big_nukebob_hitbox"))
                .build());
    }
}