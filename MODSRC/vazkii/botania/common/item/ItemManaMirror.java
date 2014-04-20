/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Apr 13, 2014, 5:39:24 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.block.tile.TilePool;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibItemNames;

import java.awt.*;
import java.util.List;

public class ItemManaMirror extends ItemMod implements IManaItem {

	IIcon[] icons;

	private static final String TAG_MANA_VISUAL = "manaVisual";
	private static final String TAG_POS_X = "posX";
	private static final String TAG_POS_Y = "posY";
	private static final String TAG_POS_Z = "posZ";
	private static final String TAG_DIM = "dim";

	public ItemManaMirror() {
		super();
		setMaxStackSize(1);
		setMaxDamage(1000);
		setUnlocalizedName(LibItemNames.MANA_MIRROR);
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		float mana = getManaForDisplay(par1ItemStack);
		return par2 == 1 ? Color.HSBtoRGB(0.528F,  mana / TilePool.MAX_MANA, 1F) : 0xFFFFFF;
	}

	@Override
	public int getDamage(ItemStack stack) {
		float mana = getManaForDisplay(stack);
		return 1000 - (int) (mana / TilePool.MAX_MANA * 1000);
	}

	@Override
	public int getDisplayDamage(ItemStack stack) {
		return getDamage(stack);
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		icons = new IIcon[2];
		for(int i = 0; i < icons.length; i++)
			icons[i] = IconHelper.forItem(par1IconRegister, this, i);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		return icons[Math.min(1, pass)];
	}
	
	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		IManaPool pool = getManaPool(par1ItemStack);
		if(!(pool instanceof DummyPool)) {
			if(pool == null) {
				bindPool(par1ItemStack, null);
				setManaForDisplay(par1ItemStack, 0);
			} else setManaForDisplay(par1ItemStack, pool.getCurrentMana());
		}
	}
	
	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if(par2EntityPlayer.isSneaking() && !par3World.isRemote) {
			TileEntity tile = par3World.getTileEntity(par4, par5, par6);
			if(tile != null && tile instanceof IManaPool) {
				bindPool(par1ItemStack, tile);
				par3World.playSoundAtEntity(par2EntityPlayer, "random.orb", 1F, 1F);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		ChunkCoordinates coords = getPoolCoords(par1ItemStack);
		if(coords.posY != -1) {
			par3List.add("X: " + coords.posX);
			par3List.add("Y: " + coords.posY);
			par3List.add("Z: " + coords.posZ);
		}
	}

	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public int getMana(ItemStack stack) {
		IManaPool pool = getManaPool(stack);
		return pool == null ? 0 : pool.getCurrentMana();
	}
	
	public int getManaForDisplay(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_MANA_VISUAL, 0);
	}
	
	public void setManaForDisplay(ItemStack stack, int mana) {
		ItemNBTHelper.setInt(stack, TAG_MANA_VISUAL, mana);
	}

	@Override
	public int getMaxMana(ItemStack stack) {
		return TilePool.MAX_MANA;
	}

	@Override
	public void addMana(ItemStack stack, int mana) {
		IManaPool pool = getManaPool(stack);
		if(pool != null)
			pool.recieveMana(mana);
	}
	
	public void bindPool(ItemStack stack, TileEntity pool) {
		ItemNBTHelper.setInt(stack, TAG_POS_X, pool == null ? 0 : pool.xCoord);
		ItemNBTHelper.setInt(stack, TAG_POS_Y, pool == null ? -1 : pool.yCoord);
		ItemNBTHelper.setInt(stack, TAG_POS_Z, pool == null ? 0 : pool.zCoord);
		ItemNBTHelper.setInt(stack, TAG_DIM, pool == null ? 0 : pool.getWorldObj().provider.dimensionId);
	}
	
	public ChunkCoordinates getPoolCoords(ItemStack stack) {
		int x = ItemNBTHelper.getInt(stack, TAG_POS_X, 0);
		int y = ItemNBTHelper.getInt(stack, TAG_POS_Y, -1);
		int z = ItemNBTHelper.getInt(stack, TAG_POS_Z, 0);
		return new ChunkCoordinates(x, y, z);
	}
	
	public int getDimension(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_DIM, 0);
	}
	
	public IManaPool getManaPool(ItemStack stack) {
		MinecraftServer server = MinecraftServer.getServer();
		if(server == null)
			return new DummyPool();
		
		ChunkCoordinates coords = getPoolCoords(stack);
		if(coords.posY == -1)
			return null;
		
		int dim = getDimension(stack);
		if(server.worldServers.length > dim && server.worldServers[dim] != null) {
			TileEntity tile = server.worldServers[dim].getTileEntity(coords.posX, coords.posY, coords.posZ);
			if(tile != null && tile instanceof IManaPool)
				return (IManaPool) tile;
		}
		
		return null;
	}

	@Override
	public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
		return false;
	}

	@Override
	public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
		return false;
	}

	@Override
	public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
		return false;
	}

	@Override
	public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
		return true;
	}
	
	private static class DummyPool implements IManaPool {

		@Override
		public boolean isFull() {
			return false;
		}

		@Override
		public void recieveMana(int mana) {
			// NO-OP
		}

		@Override
		public boolean canRecieveManaFromBursts() {
			return false;
		}

		@Override
		public int getCurrentMana() {
			return 0;
		}

		@Override
		public boolean isOutputtingPower() {
			return false;
		}
		
	}

}
