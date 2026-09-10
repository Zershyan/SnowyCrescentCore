package io.zershyan.sccore.compat.animation.registry.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.animation.registry.entity.AnimationRideEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AnimationRideEntityRenderer extends EntityRenderer<AnimationRideEntity> {
    public AnimationRideEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AnimationRideEntity pEntity) {
        return SCCore.id("");
    }

    @Override
    public boolean shouldRender(@NotNull AnimationRideEntity pLivingEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return false;
    }

    @Override
    public void render(@NotNull AnimationRideEntity pEntity, float pEntityYaw, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
    }

    @Override
    protected void renderNameTag(@NotNull AnimationRideEntity entity, @NotNull Component displayName, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, float partialTick) {
    }
}
