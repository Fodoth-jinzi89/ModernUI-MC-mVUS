package icyllis.modernui.mc.text.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import icyllis.modernui.mc.MultiBufferSourceCompat;
import icyllis.modernui.mc.text.TextLayoutEngine;
import icyllis.modernui.mc.text.TextRenderType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;


    @Inject(
            method = "addMainPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"
            )
    )
    private void onEndOutlineBatch(
            FrameGraphBuilder frame, Frustum frustum, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, boolean renderOutline, LevelRenderState levelRenderState, DeltaTracker deltaTracker, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {

        if (TextLayoutEngine.sUseTextShadersInWorld) {
            Object firstSDFFillType = TextRenderType.getFirstSDFFillType();
            Object firstSDFStrokeType = TextRenderType.getFirstSDFStrokeType();

            // flush SDF batches after outline batch ends
            MultiBufferSourceCompat.endBatch(renderBuffers.bufferSource(), firstSDFFillType);
            MultiBufferSourceCompat.endBatch(renderBuffers.bufferSource(), firstSDFStrokeType);
        }
    }
}
