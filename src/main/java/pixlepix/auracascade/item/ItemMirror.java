package pixlepix.auracascade.item;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import pixlepix.auracascade.AuraCascade;
import pixlepix.auracascade.data.EnumAura;
import pixlepix.auracascade.network.PacketBurst;
import pixlepix.auracascade.registry.CraftingBenchRecipe;
import pixlepix.auracascade.registry.ITTinkererItem;
import pixlepix.auracascade.registry.ThaumicTinkererRecipe;

import java.util.ArrayList;

/**
 * Created by localmacaccount on 5/30/15.
 */
public class ItemMirror extends Item implements ITTinkererItem {
    public ItemMirror() {
        super();
    }

    @Override
    public ArrayList<Object> getSpecialParameters() {
        return null;
    }

    @Override
    public String getItemName() {
        return "mirror";
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("aura:mirror");
    }

    @Override
    public boolean shouldRegister() {
        return true;
    }

    @Override
    public boolean shouldDisplayInTab() {
        return true;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     *
     * @param stack
     * @param world
     * @param player
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        AxisAlignedBB axisAlignedBB = AxisAlignedBB.getBoundingBox(player.posX - 6, player.posY - 6, player.posZ - 6, player.posX + 6, player.posY + 6, player.posZ + 6);
        ArrayList<EntityFireball> fireballs = (ArrayList<EntityFireball>) world.getEntitiesWithinAABB(EntityFireball.class, axisAlignedBB);
        for (EntityFireball fireball : fireballs) {
            if (fireball.getDistanceSqToEntity(player) <= 25) {
                redirect(fireball);
            }
        }
        AuraCascade.proxy.networkWrapper.sendToAllAround(new PacketBurst(7, player.posX, player.posY, player.posZ), new NetworkRegistry.TargetPoint(player.worldObj.provider.dimensionId, player.posX, player.posY, player.posZ, 32));

        return stack;
    }

    public void redirect(EntityFireball entity) {

        if (!entity.worldObj.isRemote && !(entity instanceof EntityWitherSkull)) {
            AxisAlignedBB axisAlignedBB = AxisAlignedBB.getBoundingBox(entity.posX - 100, entity.posY - 100, entity.posZ - 100, entity.posX + 100, entity.posY + 100, entity.posZ + 100);
            ArrayList<EntityFireball> targets = (ArrayList<EntityFireball>) entity.worldObj.getEntitiesWithinAABB(EntityBlaze.class, axisAlignedBB);
            targets.addAll((ArrayList<EntityFireball>) entity.worldObj.getEntitiesWithinAABB(EntityGhast.class, axisAlignedBB));
            if (targets.size() > 0) {

                //Check to make sure the fireball is traveling towards the player
                Entity target = targets.get(0);
                entity.motionX = (target.posX - entity.posX) / 15;
                entity.motionY = (target.posY - entity.posY) / 15;
                entity.motionZ = (target.posZ - entity.posZ) / 15;
                entity.accelerationX = entity.motionX * .3;
                entity.accelerationY = entity.motionY * .3;
                entity.accelerationZ = entity.motionZ * .3;
                AuraCascade.proxy.networkWrapper.sendToAllAround(new PacketBurst(1, entity.posX, entity.posY, entity.posZ), new NetworkRegistry.TargetPoint(entity.worldObj.provider.dimensionId, entity.posX, entity.posY, entity.posZ, 32));
            }
        }
    }


    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        boolean result = super.onLeftClickEntity(stack, player, entity);
        if (entity instanceof EntityFireball) {
            EntityFireball fireball = (EntityFireball) entity;
            redirect(fireball);
            return true;
        }
        return result;
    }

    @Override
    public ThaumicTinkererRecipe getRecipeItem() {
        return new CraftingBenchRecipe(new ItemStack(this), " G ", "GIG", " G ", 'G', new ItemStack(Blocks.glass), 'I', ItemMaterial.getIngot(EnumAura.RED_AURA));
    }

    @Override
    public int getCreativeTabPriority() {
        return -50;
    }
}
