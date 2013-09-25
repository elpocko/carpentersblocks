package carpentersblocks.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import carpentersblocks.CarpentersBlocks;
import carpentersblocks.data.Ladder;
import carpentersblocks.tileentity.TECarpentersBlock;
import carpentersblocks.util.handler.BlockHandler;
import carpentersblocks.util.handler.IconHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCarpentersLadder extends BlockBase
{
	
	public BlockCarpentersLadder(int blockID)
	{
		super(blockID, Material.wood);
		this.setHardness(Block.ladder.blockHardness);
		this.setUnlocalizedName("blockCarpentersLadder");
		this.setCreativeTab(CarpentersBlocks.tabCarpentersBlocks);
		this.setStepSound(soundLadderFootstep);
	}

	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * When this method is called, your block should register all the icons it needs with the given IconRegister. This
	 * is the only chance you get to register icons.
	 */
	public void registerIcons(IconRegister iconRegister)
	{
		this.blockIcon = IconHandler.icon_generic;
	}

	@Override
	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		TECarpentersBlock TE = (TECarpentersBlock) world.getBlockTileEntity(x, y, z);

		int metadata = world.getBlockMetadata(x, y, z);

		float[] bounds;

		switch (metadata) {
		case Ladder.FACING_NORTH: // Ladder on +Z
			bounds = new float[] { 0.0F, 0.0F, 0.8125F, 1.0F, 1.0F, 1.0F };
			break;
		case Ladder.FACING_SOUTH: // Ladder on -Z
			bounds = new float[] { 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.1875F };
			break;
		case Ladder.FACING_WEST: // Ladder on +X
			bounds = new float[] { 0.8125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F };
			break;
		case Ladder.FACING_EAST: // Ladder on -X
			bounds = new float[] { 0.0F, 0.0F, 0.0F, 0.1875F, 1.0F, 1.0F };
			break;
		case Ladder.FACING_ON_X:
			bounds = new float[] { 0.0F, 0.0F, 0.375F, 1.0F, 1.0F, 0.625F };
			break;
		default: // Ladder.FACING_ON_Z
			bounds = new float[] { 0.375F, 0.0F, 0.0F, 0.625F, 1.0F, 1.0F };
			break;
		}

		this.setBlockBounds(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
	}

	@Override
	/**
	 * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
	 * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
	 */
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB, List list, Entity entity)
	{
		this.setBlockBoundsBasedOnState(world, x, y, z);
		super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
	}
	
	@Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side)
    {
		switch (ForgeDirection.getOrientation(side)) {
		case NORTH:
			return world.isBlockSolidOnSide(x, y, z + 1, ForgeDirection.SOUTH);
		case SOUTH:
			return world.isBlockSolidOnSide(x, y, z - 1, ForgeDirection.NORTH);
		case WEST:
			return world.isBlockSolidOnSide(x + 1, y, z, ForgeDirection.EAST);
		case EAST:
			return world.isBlockSolidOnSide(x - 1, y, z, ForgeDirection.WEST);
		default:
			return true;
		}
    }
	
	@Override
    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int initData)
    {
        return side;
    }	

    @Override
	/**
	 * Called when the block is placed in the world.
	 * Uses cardinal direction to adjust metadata if player clicks top or bottom face of block.
	 */
	public void auxiliaryOnBlockPlacedBy(TECarpentersBlock TE, World world, int x, int y, int z, EntityLiving entityLiving, ItemStack itemStack)
	{
    	if (world.getBlockMetadata(x, y, z) < 2)
    	{    		
    		/*
    		 * Try to match ladder facing above or below.
    		 */
    		if (world.getBlockId(x, y - 1, z) == blockID) {
        		world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y - 1, z), 2);
    		} else if (world.getBlockId(x, y + 1, z) == blockID) {
        		world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y + 1, z), 2);
    		} else {
        		int facing = MathHelper.floor_double(entityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        		world.setBlockMetadataWithNotify(x, y, z, (facing % 2) == 0 ? Ladder.FACING_ON_X : Ladder.FACING_ON_Z, 2);
    		}
    	}
    	
    	/*
    	 * Force ladder to check whether it can stay.
    	 * This is done to correct misplacements due
    	 * to ladder option to be free-standing.
    	 */
		this.onNeighborBlockChange(world, x, y, z, blockID);
	}
    
    @Override
    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    public void auxiliaryOnNeighborBlockChange(TECarpentersBlock TE, World world, int x, int y, int z, int blockID)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        
        if (metadata > 1)
        {
        	if (
		    		metadata == 2 && !world.isBlockSolidOnSide(x, y, z + 1, ForgeDirection.NORTH) ||
		    		metadata == 3 && !world.isBlockSolidOnSide(x, y, z - 1, ForgeDirection.SOUTH) ||
		    		metadata == 4 && !world.isBlockSolidOnSide(x + 1, y, z, ForgeDirection.WEST) ||
		    		metadata == 5 && !world.isBlockSolidOnSide(x - 1, y, z, ForgeDirection.EAST)
        		)
	        {
	            this.dropBlockAsItem(world, x, y, z, metadata, 0);
	            world.setBlockToAir(x, y, z);
	        }
        }
    }

	@Override
	public boolean isLadder(World world, int x, int y, int z, EntityLiving entityLiving)
	{
		return true;
	}

	@Override
	/**
	 * The type of render function that is called for this block
	 */
	public int getRenderType()
	{
		return BlockHandler.carpentersLadderRenderID;
	}

}