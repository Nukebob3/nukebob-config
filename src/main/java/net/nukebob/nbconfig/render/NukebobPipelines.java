package net.nukebob.nbconfig.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.nukebob.nbconfig.NukebobConfig;

public class NukebobPipelines {
    public static final RenderPipeline LITTLE_NUKEBOB_GUI;

    static {
        LITTLE_NUKEBOB_GUI = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation("pipeline/little_nukebob_gui")
                        .withFragmentShader(NukebobConfig.id("core/little_nukebob"))
                .build());
    }
}