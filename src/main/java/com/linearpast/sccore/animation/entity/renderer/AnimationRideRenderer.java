package com.linearpast.sccore.animation.entity.renderer;

import com.linearpast.sccore.animation.entity.AnimationRideEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AnimationRideRenderer extends EntityRenderer<AnimationRideEntity> {
    public AnimationRideRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AnimationRideEntity pEntity) {
        return new ResourceLocation("");
    }

    @Override
    public boolean shouldRender(@NotNull AnimationRideEntity pLivingEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return false;
    }

    @Override
    public void render(@NotNull AnimationRideEntity pEntity, float pEntityYaw, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {}

    @Override
    protected void renderNameTag(@NotNull AnimationRideEntity pEntity, @NotNull Component pDisplayName, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {}
}
