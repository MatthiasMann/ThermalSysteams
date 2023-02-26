package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.block_entities.GourmandBoilerBlockEntity;
import cofh.core.util.ProxyUtils;
import cofh.lib.inventory.container.slot.SlotCoFH;
import cofh.lib.inventory.wrapper.InvWrapperCoFH;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GourmandBoilerContainer extends BoilerContainerBase<GourmandBoilerBlockEntity> {

	public GourmandBoilerContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
		this(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer());
	}

	public GourmandBoilerContainer(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
		super(SysteamsRegistry.Boilers.GOURMAND.menu(), windowId, level, pos, inventory, player);
		BoilerBlockEntityBase blockEntity = (BoilerBlockEntityBase) level.getBlockEntity(pos);
        InvWrapperCoFH blockEntityInv = new InvWrapperCoFH(blockEntity.getItemInv());

		addSlot(new SlotCoFH(blockEntityInv, 0, 44, 35));

        bindAugmentSlots(blockEntityInv, 1, blockEntity.augSize());
        bindPlayerInventory(inventory);
	}
}
