package com.hbm.blocks.generic;

import com.hbm.inventory.gui.GUIScreenBobble;
import com.hbm.items.special.ItemPlasticScrap.ScrapType;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.world.gen.INBTTileEntityTransformable;
import com.hbm.world.gen.INBTTransformable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockBobble extends BlockContainer implements IGUIProvider, INBTTransformable {

    public BlockBobble() {
        super(Material.IRON);
    }

    //@Override
    public int getRenderType() {
        return -1;
    }

    //@Override
    public boolean isOpaqueCube() {
        return false;
    }

    //@Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    //@Override
    public Item getItemDropped(int i, Random rand, int j) {
        return null;
    }

    //@Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos blockPos, EntityPlayer player) {

        TileEntityBobble entity = (TileEntityBobble) world.getTileEntity(blockPos);

        if(entity != null) {
            return new ItemStack(this, 1, entity.type.ordinal());
        }

        return super.getPickBlock(state,target, world, blockPos, player);
    }

    //@Override
    public void onBlockHarvested(World world, BlockPos blockPos, int meta, EntityPlayer player) {

        if(!player.capabilities.isCreativeMode) {
            harvesters.set(player);
            if(!world.isRemote) {
                TileEntityBobble entity = (TileEntityBobble) world.getTileEntity(blockPos);
                if(entity != null) {
                    EntityItem item = new EntityItem(world);
                    item.motionX = 0;
                    item.motionY = 0;
                    item.motionZ = 0;
                    world.spawnEntity(item);
                }
            }
            harvesters.set(null);
        }
    }

    //@Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        player.addExhaustion(0.025F);
    }

    //@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

        if(world.isRemote) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
            return true;

        } else {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    //@Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {

        for(int i = 1; i < BobbleType.values().length; i++)
            list.add(new ItemStack(item, 1, i));
    }


    public void onBlockPlacedBy(World world, BlockPos blockPos, EntityLivingBase player, ItemStack stack) {
        //int meta = MathHelper.floor((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
        IBlockState meta = getDefaultState().withRotation(Rotation.valueOf(String.valueOf(player.rotationYaw)));
        world.setBlockState(blockPos, meta,2);

        TileEntityBobble bobble = (TileEntityBobble) world.getTileEntity(blockPos);
        bobble.type = BobbleType.values()[Math.abs(stack.getItemDamage()) % BobbleType.values().length];
        bobble.markDirty();
    }

    //@Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        float f = 0.0625F;
        this.setBlockBoundsBasedOnState(world, x, y, z);
    }

    //@Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        //return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
        return null;
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return (meta + coordBaseMode * 4) % 16;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBobble();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public static class TileEntityBobble extends TileEntity implements INBTTileEntityTransformable {

        public BobbleType type = BobbleType.NONE;

        //@Override
        public boolean canUpdate() {
            return false;
        }

        //@Override
        public Packet getDescriptionPacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            //this.readFromNBT(pkt.func_148857_g());
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.type = BobbleType.values()[Math.abs(nbt.getByte("type")) % BobbleType.values().length];
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setByte("type", (byte) type.ordinal());
            return super.writeToNBT(nbt);
        }

        @Override
        public void transformTE(World world, int coordBaseMode) {
            type = BobbleType.values()[world.rand.nextInt(BobbleType.values().length - 1) + 1];
        }
    }

    public static enum BobbleType {

        NONE(			"null",								"null",				null,														null,																								false,	ScrapType.BOARD_BLANK),
        STRENGTH(		"Strength",							"Strength",			null,														"It's essential to give your arguments impact.",													false,	ScrapType.BRIDGE_BIOS),
        PERCEPTION(		"Perception",						"Perception",		null,														"Only through observation will you perceive weakness.",												false,	ScrapType.BRIDGE_NORTH),
        ENDURANCE(		"Endurance",						"Endurance",		null,														"Always be ready to take one for the team.",														false,	ScrapType.BRIDGE_SOUTH),
        CHARISMA(		"Charisma",							"Charisma",			null,														"Nothing says pizzaz like a winning smile.",														false,	ScrapType.BRIDGE_IO),
        INTELLIGENCE(	"Intelligence",						"Intelligence",		null,														"It takes the smartest individuals to realize$there's always more to learn.",						false,	ScrapType.BRIDGE_BUS),
        AGILITY(		"Agility",							"Agility",			null,														"Never be afraid to dodge the sensitive issues.",													false,	ScrapType.BRIDGE_CHIPSET),
        LUCK(			"Luck",								"Luck",				null,														"There's only one way to give 110%.",																false,	ScrapType.BRIDGE_CMOS),
        BOB(			"Robert \"The Bobcat\" Katzinsky",	"HbMinecraft",		"Hbm's Nuclear Tech Mod",									"I know where you live, " + System.getProperty("user.name"),										false,	ScrapType.CPU_SOCKET),
        FRIZZLE(		"Frooz",							"Frooz",			"Weapon models",											"BLOOD IS FUEL",																					true,	ScrapType.CPU_CLOCK),
        PU238(			"Pu-238",							"Pu-238",			"Improved Tom impact mechanics",							null,																								false,	ScrapType.CPU_REGISTER),
        VT(				"VT-6/24",							"VT-6/24",			"Balefire warhead model and general texturework",			"You cannot unfuck a horse.",																		true,	ScrapType.CPU_EXT),
        DOC(			"The Doctor",						"Doctor17PH",		"Russian localization, lunar miner",						"Perhaps the moon rocks were too expensive",														true,	ScrapType.CPU_CACHE),
        BLUEHAT(		"The Blue Hat",						"The Blue Hat",		"Textures",													"payday 2's deagle freeaim champ of the year 2022",													true,	ScrapType.MEM_16K_A),
        PHEO(			"Pheo",								"Pheonix",			"Deuterium machines, tantalium textures, Reliant Rocket",	"RUN TO THE BEDROOM, ON THE SUITCASE ON THE LEFT,$YOU'LL FIND MY FAVORITE AXE",						true,	ScrapType.MEM_16K_B),
        ADAM29(			"Adam29",							"Adam29",			"Ethanol, liquid petroleum gas",							"You know, nukes are really quite beatiful.$It's like watching a star be born for a split second.",	true,	ScrapType.MEM_16K_C),
        UFFR(			"UFFR",								"UFFR",				"All sorts of things from his PR",							"fried shrimp",																						false,	ScrapType.MEM_SOCKET),
        VAER(			"vaer",								"vaer",				"ZIRNOX",													"taken de family out to the weekend cigarette festival",											true,	ScrapType.MEM_16K_D),
        NOS(			"Dr Nostalgia",						"Dr Nostalgia",		"SSG and Vortex models",									"Take a picture, I'ma pose, paparazzi$I've been drinking, moving like a zombie",					true,	ScrapType.BOARD_TRANSISTOR),
        DRILLGON(		"Drillgon200",						"Drillgon200",		"1.12 Port",												null,																								false,	ScrapType.CPU_LOGIC),
        CIRNO(			"Cirno",							"Cirno",			"the only multi layered skin i had",						"No brain. Head empty.",																			true,	ScrapType.BOARD_BLANK),
        MICROWAVE(		"Microwave",						"Microwave",		"OC Compatibility and massive RBMK/packet optimizations",		"they call me the food heater$john optimization",												true,	ScrapType.BOARD_CONVERTER),
        PEEP(			"Peep",								"LePeeperSauvage",	"Coilgun, Leadburster and Congo Lake models, BDCL QC",		"Fluffy ears can't hide in ash, nor snow.",															true,	ScrapType.CARD_BOARD),
        MELLOW(			"MELLOWARPEGGIATION",				"Mellow",			"NBT Structures, industrial lighting, animation tools",				"Make something cool now, ask for permission later.",												true,	ScrapType.CARD_PROCESSOR);

        public String name;			//the title of the tooltip
        public String label;		//the name engraved in the socket
        public String contribution;	//what contributions this person has made, if applicable
        public String inscription;	//the flavor text
        public boolean skinLayers;
        public ScrapType scrap;

        private BobbleType(String name, String label, String contribution, String inscription, boolean layers, ScrapType scrap) {
            this.name = name;
            this.label = label;
            this.contribution = contribution;
            this.inscription = inscription;
            this.skinLayers = layers;
            this.scrap = scrap;
        }
    }

    //@Override
    public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos blockPos) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    //@Override
    public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos blockPos) {
        return new GUIScreenBobble((TileEntityBobble) world.getTileEntity(blockPos));
    }
}