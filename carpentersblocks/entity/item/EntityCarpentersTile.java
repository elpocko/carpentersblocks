package carpentersblocks.entity.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import carpentersblocks.api.ICarpentersHammer;
import carpentersblocks.util.BlockProperties;
import carpentersblocks.util.handler.DyeHandler;
import carpentersblocks.util.handler.TileHandler;
import carpentersblocks.util.registry.IconRegistry;
import carpentersblocks.util.registry.ItemRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityCarpentersTile extends Entity {

    private int ticks;

    private final static byte ID_DIRECTION = 12;
    private final static byte ID_DYE       = 13;
    private final static byte ID_ICON_NAME = 14;
    private final static byte ID_ROTATION  = 15;

    private final static double depth = 0.0625D;

    private final static double[][] bounds =
    {
            {         0.0D, 1.0D - depth,         0.0D,  1.0D,  1.0D,  1.0D },
            {         0.0D,         0.0D,         0.0D,  1.0D, depth,  1.0D },
            {         0.0D,         0.0D, 1.0D - depth,  1.0D,  1.0D,  1.0D },
            {         0.0D,         0.0D,         0.0D,  1.0D,  1.0D, depth },
            { 1.0D - depth,         0.0D,         0.0D,  1.0D,  1.0D,  1.0D },
            {         0.0D,         0.0D,         0.0D, depth,  1.0D,  1.0D }
    };

    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public EntityCarpentersTile(World world)
    {
        super(world);
    }

    public EntityCarpentersTile(World world, int x, int y, int z, int dir, int rotation, String dye)
    {
        super(world);
        posX = x;
        posY = y;
        posZ = z;
        setDirection(dir);
        setRotation(rotation);
        setDye(dye);     
    }
    
    public double[] getBounds()
    {
        return bounds[dataWatcher.getWatchableObjectInt(ID_DIRECTION)];
    }

    public void setBoundingBox()
    {
        double bounds[] = getBounds();
        boundingBox.setBounds(posX + bounds[0], posY + bounds[1], posZ + bounds[2], posX + bounds[3], posY + bounds[4], posZ + bounds[5]);
    }

    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(dataWatcher.getWatchableObjectInt(ID_DIRECTION));
    }
    
    public void setDirection(int dir)
    {
        dataWatcher.updateObject(ID_DIRECTION, new Integer(dir));
    }

    public void setRotation(int rotation)
    {
        dataWatcher.updateObject(ID_ROTATION, new Integer(rotation));
    }
    
    public void rotate()
    {
        int rotation = getRotation();
        setRotation(++rotation & 3);
    }
    
    public int getRotation()
    {
        return dataWatcher.getWatchableObjectInt(ID_ROTATION);
    }
    
    public void setDye(String dye)
    {
        dataWatcher.updateObject(ID_DYE, new String(dye));
    }
    
    public String getDye()
    {
        return dataWatcher.getWatchableObjectString(ID_DYE);
    }

    private void setIconName(String tile)
    {
        dataWatcher.updateObject(ID_ICON_NAME, new String(tile));
        
        if (!tile.equals("blank")) {
            icon = IconRegistry.icon_tile.get(TileHandler.tileList.indexOf(tile));
        } else {
            icon = IconRegistry.icon_blank_tile;
        }
    }
    
    /**
     * Sets next tile design.
     */
    private void setNextIcon()
    {
        setIconName(TileHandler.getNext(getIconName()));
    }
    
    /**
     * Sets previous tile design.
     */
    private void setPrevIcon()
    {
        setIconName(TileHandler.getPrev(getIconName()));
    }
    
    public String getIconName()
    {
        return dataWatcher.getWatchableObjectString(ID_ICON_NAME);
    }

    public IIcon getIcon()
    {
        if (icon == null) {
            icon = IconRegistry.icon_blank_tile;
        }

        return icon;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound)
    {
        nbtTagCompound.setString("iconName", dataWatcher.getWatchableObjectString(ID_ICON_NAME));
        nbtTagCompound.setString("dye", dataWatcher.getWatchableObjectString(ID_DYE));
        nbtTagCompound.setInteger("direction", dataWatcher.getWatchableObjectInt(ID_DIRECTION));
        nbtTagCompound.setInteger("rotation", dataWatcher.getWatchableObjectInt(ID_ROTATION));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTagCompound)
    {
        dataWatcher.updateObject(ID_ICON_NAME, String.valueOf(nbtTagCompound.getString("iconName")));
        dataWatcher.updateObject(ID_DYE, String.valueOf(nbtTagCompound.getString("dye")));
        dataWatcher.updateObject(ID_DIRECTION, Integer.valueOf(nbtTagCompound.getInteger("direction")));
        dataWatcher.updateObject(ID_ROTATION, Integer.valueOf(nbtTagCompound.getInteger("rotation")));
    }

    /**
     * Called when this entity is broken. Entity parameter may be null.
     */
    public void onBroken(Entity entity)
    {
        if (entity instanceof EntityPlayer) {

            EntityPlayer entityPlayer = (EntityPlayer) entity;
            ItemStack itemStack = entityPlayer.getHeldItem();
            
            boolean hasHammer = false;
            
            if (itemStack != null) {
                Item item = itemStack.getItem();
                
                if (item instanceof ICarpentersHammer) {
                    hasHammer = true;
                }
            }
            
            if (entityPlayer.capabilities.isCreativeMode && !hasHammer) {
                return;
            }

        }

        entityDropItem(getItemDrop(), 0.0F);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        if (!worldObj.isRemote) {

            if (ticks++ >= 20) {
                ticks = 0;

                if (!isDead && !onValidSurface())
                {
                    setDead();
                    onBroken((Entity)null);
                }
            }

        }
    }

    /**
     * Returns representative ItemStack for entity.
     */
    private ItemStack getItemDrop()
    {
        return new ItemStack(ItemRegistry.itemCarpentersTile);
    }

    /**
     * Called when a user uses the creative pick block button on this entity.
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing should be added.
     */
    @Override
    public ItemStack getPickedResult(MovingObjectPosition target)
    {
        return getItemDrop();
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        // TODO: Switch to pass 1 when alpha rendering is fixed.
        return pass == 0;
    }

    /**
     * checks to make sure painting can be placed there
     */
    public boolean onValidSurface()
    {
        ForgeDirection dir = getDirection();

        int x_offset = MathHelper.floor_double(posX) - dir.offsetX;
        int y_offset = MathHelper.floor_double(posY) - dir.offsetY;
        int z_offset = MathHelper.floor_double(posZ) - dir.offsetZ;

        Block block = worldObj.getBlock(x_offset, y_offset, z_offset);

        return !(block != null && !block.isSideSolid(worldObj, x_offset, y_offset, z_offset, dir));
    }

    /**
     * Called when a player attacks an entity. If this returns true the attack will not happen.
     */
    @Override
    public boolean hitByEntity(Entity entity)
    {
        return entity instanceof EntityPlayer ? attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)entity), 0.0F) : false;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float par2)
    {
        Entity entity = damageSource.getEntity();

        boolean dropItem = false;
        
        if (entity instanceof EntityPlayer) {
                        
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            ItemStack itemStack = entityPlayer.getHeldItem();

            if (itemStack != null) {

                if (itemStack.getItem() instanceof ICarpentersHammer) {
                    if (entity.isSneaking()) {
                        if (!this.isDead && !this.worldObj.isRemote) {
                            dropItem = true;
                        }                        
                    } else {
                        setNextIcon();
                    }
                } else {
                    if (!this.isDead && !this.worldObj.isRemote) {
                        dropItem = true;
                    }   
                }
                
            } else if (entityPlayer.capabilities.isCreativeMode) {
                
                dropItem = true; 
                
            }

        }
        
        if (dropItem)
        {
            this.setDead();
            this.setBeenAttacked();
            this.onBroken(damageSource.getEntity());
            return true;
        }
        
        return false;
    }
    
    @Override
    /**
     * First layer of player interaction.
     */
    public boolean interactFirst(EntityPlayer entityPlayer)
    {
        ItemStack itemStack = entityPlayer.getHeldItem();
        
        if (itemStack != null) {
            if (itemStack.getItem() instanceof ICarpentersHammer) {
                
                if (entityPlayer.isSneaking()) {
                    rotate();
                } else {
                    setPrevIcon();
                }
                
            } else if (BlockProperties.isDye(itemStack)) {
                
                setDye(DyeHandler.getDyeName(itemStack));
                
            }
            
            return true;
        }

        return false;
    }

    /**
     * Tries to moves the entity by the passed in displacement. Args: x, y, z
     */
    @Override
    public void moveEntity(double x, double y, double z)
    {
        if (!worldObj.isRemote && !isDead && x * x + y * y + z * z > 0.0D)
        {
            setDead();
            onBroken((Entity)null);
        }
    }

    /**
     * Adds to the current velocity of the entity. Args: x, y, z
     */
    @Override
    public void addVelocity(double x, double y, double z)
    {
        if (!worldObj.isRemote && !isDead && x * x + y * y + z * z > 0.0D)
        {
            setDead();
            onBroken((Entity)null);
        }
    }

    @Override
    protected void entityInit()
    {
        dataWatcher.addObject(ID_ICON_NAME, new String("blank"));
        dataWatcher.addObject(ID_DYE, new String("dyeWhite"));
        dataWatcher.addObject(ID_DIRECTION, new Integer(0));
        dataWatcher.addObject(ID_ROTATION, new Integer(0));
    }

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double posX, double posY, double posZ, float yaw, float pitch, int par9)
    {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * returns the bounding box for this entity
     */
    @Override
    public AxisAlignedBB getBoundingBox()
    {
        setBoundingBox();
        return boundingBox;
    }

    @Override
    public float getCollisionBorderSize()
    {
        return 0.0F;
    }

    @Override
    protected boolean shouldSetPosAfterLoading()
    {
        return false;
    }
        
}
