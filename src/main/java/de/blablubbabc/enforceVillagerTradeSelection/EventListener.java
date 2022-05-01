package de.blablubbabc.enforceVillagerTradeSelection;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MerchantInventory;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	void onInventoryClick(InventoryClickEvent event) {
		HumanEntity whoClicked = event.getWhoClicked();
		Log.debug(() -> "Inventory click: whoClicked=" + whoClicked.getName()
				+ ", inventoryType=" + event.getClickedInventory().getType()
				+ ", rawSlot=" + event.getRawSlot()
				+ ", slotType=" + event.getSlotType()
				+ ", action=" + event.getAction()
				+ ", cancelled=" + event.isCancelled());

		if (ignoreCancelledEvent(event)) {
			return;
		}

		if (ignoreNonMerchantInventory(event.getClickedInventory())) {
			return;
		}

		// Ignore interactions with non-input slots:
		if (event.getSlotType() != SlotType.CRAFTING) {
			Log.debug("  Ignoring non-input slot click.");
			return;
		}

		// Ignore certain inventory actions (e.g. pick ups):
		if (isInventoryActionIgnored(event.getAction())) {
			Log.debug("  Ignoring inventory action.");
			return;
		}

		// TODO Ignore if a trading recipe is already explicitly selected?

		if (ignorePlayerWithBypassPermission(whoClicked)) {
			return;
		}

		cancel(event);
	}

	private boolean ignoreCancelledEvent(InventoryInteractEvent event) {
		if (event.isCancelled()) {
			Log.debug("  Ignoring already cancelled event.");
			return true;
		}
		return false;
	}

	private boolean ignoreNonMerchantInventory(Inventory inventory) {
		if (!(inventory instanceof MerchantInventory)) {
			Log.debug("  Ignoring non-merchant inventory.");
			return true;
		}
		return false;
	}

	private boolean ignorePlayerWithBypassPermission(HumanEntity whoClicked) {
		if (whoClicked instanceof Player) {
			Player player = (Player) whoClicked;
			if (player.hasPermission(Permissions.BYPASS)) {
				Log.debug("  Ignoring player with bypass permission.");
				return true;
			}
		}
		return false;
	}

	private void cancel(InventoryInteractEvent event) {
		event.setCancelled(true);
		Log.debug("  Cancelled!");
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	void onInventoryDrag(InventoryDragEvent event) {
		HumanEntity whoClicked = event.getWhoClicked();
		Log.debug(() -> "Inventory dragging: whoClicked=" + whoClicked.getName()
				+ ", inventoryType=" + event.getInventory().getType()
				+ ", rawSlots=" + event.getRawSlots().stream()
						.map(String::valueOf)
						.collect(Collectors.joining(",", "[", "]"))
				+ ", cancelled=" + event.isCancelled());

		if (ignoreCancelledEvent(event)) {
			return;
		}

		if (ignoreNonMerchantInventory(event.getInventory())) {
			return;
		}

		// Ignore interactions with non-input slots:
		if (!containsInputSlot(event.getView(), event.getRawSlots())) {
			Log.debug(() -> "  Ignoring non-input slot drag.");
			return;
		}

		// TODO Ignore if a trading recipe is already explicitly selected?

		if (ignorePlayerWithBypassPermission(whoClicked)) {
			return;
		}

		cancel(event);
	}

	private boolean containsInputSlot(InventoryView inventoryView, Set<Integer> rawSlots) {
		for (int rawSlot : rawSlots) {
			SlotType slotType = inventoryView.getSlotType(rawSlot);
			if (slotType == SlotType.CRAFTING) {
				return true;
			}
		}
		return false;
	}

	private boolean isInventoryActionIgnored(InventoryAction action) {
		switch (action) {
		case PICKUP_ALL:
		case PICKUP_SOME:
		case PICKUP_HALF:
		case PICKUP_ONE:
		case DROP_ALL_CURSOR:
		case DROP_ONE_CURSOR:
		case DROP_ALL_SLOT:
		case DROP_ONE_SLOT:
		case MOVE_TO_OTHER_INVENTORY:
		case HOTBAR_MOVE_AND_READD:
		case CLONE_STACK:
		case COLLECT_TO_CURSOR:
			return true;
		case NOTHING: // No effect, so there is no harm in canceling it just in case
		case PLACE_ALL:
		case PLACE_SOME:
		case PLACE_ONE:
		case SWAP_WITH_CURSOR:
		case HOTBAR_SWAP:
		case UNKNOWN:
		default:
			// Intentionally: If new inventory actions are added to Minecraft, they are initially
			// cancelled by default and need to be explicitly allowed.
			return false;
		}
	}
}
