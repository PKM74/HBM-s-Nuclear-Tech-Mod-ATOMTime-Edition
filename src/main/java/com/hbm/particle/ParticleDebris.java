package com.hbm.particle;

import com.hbm.wiaj.WorldInAJar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ParticleDebris extends Particle {

    public static Random rng = new Random();
    public WorldInAJar worldInAJar;
    private final BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
    private double prevRotationPitch;
    private double prevRotationYaw;
    private double rotationPitch;
    private double rotationYaw;

    public ParticleDebris(World world, double x, double y, double z, double mX, double mY, double mZ) {
        super(world, x, y, z);
        double mult = 3;
        this.motionX = mX * mult;
        this.motionY = mY * mult;
        this.motionZ = mZ * mult;
        this.particleMaxAge = 100;
        this.particleGravity = 0.15F;
        this.canCollide = false;
    }

    public ParticleDebris(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public int getFXLayer() {
        return 3;
    }


    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge > 5) this.canCollide = true;

        rng.setSeed(this.hashCode());
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch += rng.nextFloat() * 10;
        this.rotationYaw += rng.nextFloat() * 10;

        if (this.hashCode() % 3 == 0) {
            ParticleRocketFlame fx = new ParticleRocketFlame(world, posX, posY, posZ).setScale(1F * Math.max(worldInAJar.sizeY, 6) / 16F);
            fx.updateInterpPos();
            fx.setMaxAge(50);
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        }

        this.motionY -= this.particleGravity;

        this.move(this.motionX, this.motionY, this.motionZ);

        this.particleAge++;
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        IBlockState state = worldInAJar.getBlockState(pos);

        if (this.onGround || state.getBlock() == Blocks.WEB) this.setExpired();
    }

    @Override
    public void renderParticle(BufferBuilder _buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (worldInAJar == null) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - dX);
        float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - dY);
        float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - dZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(pX, pY, pZ);

        GlStateManager.rotate((float) (prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks), 0, 1, 0);
        GlStateManager.rotate((float) (prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks), 0, 0, 1);

        GlStateManager.translate(-worldInAJar.sizeX / 2.0, -worldInAJar.sizeY / 2.0, -worldInAJar.sizeZ / 2.0);

        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (int x = 0; x < worldInAJar.sizeX; x++) {
            for (int y = 0; y < worldInAJar.sizeY; y++) {
                for (int z = 0; z < worldInAJar.sizeZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = worldInAJar.getBlockState(pos);

                    try {
                        IBakedModel model = renderer.getModelForState(state);
                        renderer.getBlockModelRenderer().renderModel(
                                worldInAJar, model, state, pos, buffer, false
                        );
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        tessellator.draw();
        GlStateManager.popMatrix();
    }

}
