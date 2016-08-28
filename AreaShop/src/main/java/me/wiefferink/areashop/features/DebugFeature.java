package me.wiefferink.areashop.features;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.events.ask.*;
import me.wiefferink.areashop.events.askandnotify.AddFriendEvent;
import me.wiefferink.areashop.events.askandnotify.DeleteFriendEvent;
import me.wiefferink.areashop.events.notify.*;
import org.bukkit.event.EventHandler;

public class DebugFeature extends Feature {

	@EventHandler
	public void buyingRegion(BuyingRegionEvent event) {
		AreaShop.debug("BuyingRegionEvent: " + event.getPlayer().getName() + " is trying to buy " + event.getRegion().getName());
	}

	@EventHandler
	public void boughtRegion(BoughtRegionEvent event) {
		AreaShop.debug("BoughtRegionEvent: " + event.getRegion().getPlayerName() + " bought " + event.getRegion().getName());
	}

	@EventHandler
	public void sellingRegion(SellingRegionEvent event) {
		AreaShop.debug("SellingRegionEvent: " + event.getRegion().getName() + " is trying to sell " + event.getRegion().getName());
	}

	@EventHandler
	public void soldRegion(SoldRegionEvent event) {
		AreaShop.debug("SoldRegionEvent: "+Utils.toName(event.getOldBuyer())+" sold "+event.getRegion().getName());
	}

	@EventHandler
	public void resellingRegion(ResellingRegionEvent event) {
		AreaShop.debug("ResellingRegionEvent: " + event.getRegion().getName() + " is trying to resell " + event.getRegion().getName());
	}

	@EventHandler
	public void resoldRegion(ResoldRegionEvent event) {
		AreaShop.debug("ResoldRegionEvent: "+Utils.toName(event.getFromPlayer())+" resold "+event.getRegion().getName()+" to "+event.getRegion().getPlayerName());
	}

	@EventHandler
	public void rentingRegion(RentingRegionEvent event) {
		AreaShop.debug("RentingRegionEvent: " + event.getPlayer().getName() + " is trying to rent " + event.getRegion().getName() + ", extending=" + event.isExtending());
	}

	@EventHandler
	public void rentedRegion(RentedRegionEvent event) {
		AreaShop.debug("RentedRegionEvent: " + event.getRegion().getPlayerName() + " rented " + event.getRegion().getName() + ", extending=" + event.hasExtended());
	}

	@EventHandler
	public void unrentingRegion(UnrentingRegionEvent event) {
		AreaShop.debug("UnrentingRegionEvent: " + event.getRegion().getPlayerName() + " is trying to unrent " + event.getRegion().getName());
	}

	@EventHandler
	public void unrentedRegion(UnrentedRegionEvent event) {
		AreaShop.debug("UnrentedRegionEvent: "+Utils.toName(event.getOldRenter())+" unrented "+event.getRegion().getName());
	}

	@EventHandler
	public void regionUpdate(UpdateRegionEvent event) {
		//AreaShop.debug("UpdateRegionEvent: " + event.getRegion().getName() + " updated");
	}

	@EventHandler
	public void addedRegion(AddedRegionEvent event) {
		AreaShop.debug("AddedRegionEvent: "+event.getRegion().getName());
	}

	@EventHandler
	public void removedRegion(RemovedRegionEvent event) {
		AreaShop.debug("RemovedRegionEvent: "+event.getRegion().getName());
	}

	@EventHandler
	public void addedFriend(AddFriendEvent event) {
		AreaShop.debug("AddFriendEvent: region "+event.getRegion().getName()+", "+event.getFriend().getName()+" by "+(event.getBy() == null ? "<nobody>" : event.getBy().getName()));
	}

	@EventHandler
	public void deleteFriend(DeleteFriendEvent event) {
		AreaShop.debug("DeleteFriendEvent: region "+event.getRegion().getName()+", "+event.getFriend().getName()+" by "+(event.getBy() == null ? "<nobody>" : event.getBy().getName()));
	}
}
