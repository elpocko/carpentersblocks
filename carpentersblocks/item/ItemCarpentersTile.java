package carpentersblocks.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import carpentersblocks.CarpentersBlocks;
import carpentersblocks.entity.item.EntityCarpentersTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCarpentersTile extends Item {

    public ItemCarpentersTile()
    {
        setCreativeTab(CarpentersBlocks.creativeTab);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon("carpentersblocks:canvas");
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote) {
            
            return true;
            
        } else {
            
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            int x_offset = x + dir.offsetX;
            int y_offset = y + dir.offsetY;
            int z_offset = z + dir.offsetZ;
    
            if (!entityPlayer.canPlayerEdit(x_offset, y_offset, z_offset, side, itemStack)) {
                
                return false;
                
            } else {
                
                /* Get nearby tile properties. */
                
                String dye = "dyeWhite";
                int rotation = 0;
                
                List<EntityCarpentersTile> list = getNearbyTiles(world, x_offset, y_offset, z_offset);
                
                /* Set dye using non-weighted code. */
                for (EntityCarpentersTile tile : list)
                {
                    if (!tile.getDye().equals("dyeWhite")) {
                        dye = tile.getDye();
                    }
                    if (tile.getRotation() != 0) {
                        rotation = tile.getRotation();
                    }
                }
                
                EntityCarpentersTile entity = new EntityCarpentersTile(world, x_offset, y_offset, z_offset, side, rotation, dye);
    
                if (entity != null && entity.onValidSurface()) {
    
                    if (!world.isRemote) {
                        world.spawnEntityInWorld(entity);
                    }
                    
                    --itemStack.stackSize;
                    
                }
                
                return true;
            }
        }
    }
    
    /**
     * Returns list of nearby tiles.
     */
    private List<EntityCarpentersTile> getNearbyTiles(World world, int x, int y, int z)
    {
        return world.getEntitiesWithinAABB(EntityCarpentersTile.class, AxisAlignedBB.getBoundingBox(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1));
    }

}