package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class Landmine extends BlockContainer implements IBomb {


    public double range;
    public double height;
    public static final float f = 0.0625F;
    public static final AxisAlignedBB AP_BOX = new AxisAlignedBB(5 * f, 0.0F, 5 * f, 11 * f, 1 * f, 11 * f);
    public static final AxisAlignedBB HE_BOX = new AxisAlignedBB(4 * f, 0.0F, 4 * f, 12 * f, 2 * f, 12 * f);
    public static final AxisAlignedBB SHRAP_BOX = new AxisAlignedBB(5 * f, 0.0F, 5 * f, 11 * f, 1 * f, 11 * f);
    public static final AxisAlignedBB FAT_BOX = new AxisAlignedBB(5 * f, 0.0F, 4 * f, 11 * f, 6 * f, 12 * f);
    public static boolean safeMode = false;
    private static Random rand = new Random();

    public Landmine(Material materialIn, String s, double range, double height) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.range = range;
        this.height = height;
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLandmine();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (this == ModBlocks.mine_ap) {
            return AP_BOX;
        } else if (this == ModBlocks.mine_he) {
            return HE_BOX;
        } else if (this == ModBlocks.mine_shrap) {
            return SHRAP_BOX;
        } else if (this == ModBlocks.mine_fat) {
            return FAT_BOX;
        } else {
            return FULL_BLOCK_AABB;
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) || worldIn.getBlockState(pos.down()).getBlock() instanceof BlockFence;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.getStrongPower(pos) > 0) {
            explode(world, pos);
        }

        boolean flag = false;

        if (!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP) && !(world.getBlockState(pos.down()).getBlock() instanceof BlockFence)) {
            flag = true;
        }

        if (flag) {
            this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!safeMode) {
            explode(worldIn, pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.getHeldItemMainhand().getItem() == ModItems.defuser || player.getHeldItemOffhand().getItem() == ModItems.defuser || player.getHeldItemMainhand().getItem() == ModItems.defuser_desh || player.getHeldItemOffhand().getItem() == ModItems.defuser_desh) {

            safeMode = true;
            world.setBlockToAir(pos);

            ItemStack itemstack = new ItemStack(this, 1);
            float f = world.rand.nextFloat() * 0.6F + 0.2F;
            float f1 = world.rand.nextFloat() * 0.2F;
            float f2 = world.rand.nextFloat() * 0.6F + 0.2F;

            EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1 + 1, pos.getZ() + f2, itemstack);

            float f3 = 0.05F;
            entityitem.motionX = (float) world.rand.nextGaussian() * f3;
            entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
            entityitem.motionZ = (float) world.rand.nextGaussian() * f3;

            if (!world.isRemote)
                world.spawnEntity(entityitem);

            safeMode = false;
            return true;
        }

        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        if (this == ModBlocks.mine_fat) {
            tooltip.add("§2[Nuclear Mine]§r");
            tooltip.add(" §eRadius: " + BombConfig.fatmanRadius + "m§r");
            tooltip.add("§2[Fallout]§r");
            tooltip.add(" §aRadius: " + (int) BombConfig.fatmanRadius * (1 + BombConfig.falloutRange / 100) + "m§r");
        }
    }

    @Override
    public BombReturnCode explode(World world, BlockPos pos) {
        if (!world.isRemote) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            Landmine.safeMode = true;
            world.destroyBlock(pos, false);
            Landmine.safeMode = false;

            if(this == ModBlocks.mine_ap) {
                ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 3F);
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, 10F).setupPiercing(5F, 0.2F));
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F));
                vnt.explode();
            } else if(this == ModBlocks.mine_he) {
                ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 4F);
                vnt.setBlockAllocator(new BlockAllocatorStandard());
                vnt.setBlockProcessor(new BlockProcessorStandard());
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 35).setupPiercing(15F, 0.2F));
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(15, 3.5F, 1.25F));
                vnt.explode();
            } else if(this == ModBlocks.mine_shrap) {
                ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 3F);
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, 7.5F));
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F));
                vnt.explode();
                ExplosionLarge.spawnShrapnelShower(world, x + 0.5, y + 0.5, z + 0.5, 0, 1D, 0, 45, 0.2D);
                ExplosionLarge.spawnShrapnels(world, x + 0.5, y + 0.5, z + 0.5, 5);
            }
            else if (this == ModBlocks.mine_fat) {

                world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.fatmanRadius, x + 0.5, y + 0.5, z + 0.5));
                if (rand.nextInt(100) == 0 || MainRegistry.polaroidID == 11) {
                    EntityNukeTorex.statFacBale(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.fatmanRadius);
                } else {
                    EntityNukeTorex.statFac(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.fatmanRadius);
                }
            }
        }

        return BombReturnCode.DETONATED;
    }
}
