package nl.evolutioncoding.areashop.features;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.events.ask.*;
import nl.evolutioncoding.areashop.events.notify.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DebugFeature extends Feature implements Listener {

	public DebugFeature(AreaShop plugin) {
		super(plugin);
	}

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
	public void regionUpdate(RegionUpdateEvent event) {
		//AreaShop.debug("RegionUpdateEvent: " + event.getRegion().getName() + " updated");
	}
}
