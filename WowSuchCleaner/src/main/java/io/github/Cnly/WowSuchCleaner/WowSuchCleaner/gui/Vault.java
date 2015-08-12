package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import io.github.Cnly.BusyInv.BusyInv.events.ItemClickEvent;
import io.github.Cnly.BusyInv.BusyInv.items.BusyItem;
import io.github.Cnly.BusyInv.BusyInv.menus.ChestMenu;
import io.github.Cnly.Crafter.Crafter.framework.locales.CrafterLocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

public class Vault extends ChestMenu
{
    
    private static HashSet<Player> opened = new HashSet<>();
    
    private Main main = Main.getInstance();
    private AuctionConfig auctionConfig = main.getAuctionConfig();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    public Vault(CrafterLocaleManager localeManager, AuctionConfig config)
    {
        super(localeManager.getLocalizedString("ui.vault"), null, ChestSize.fit(config.getVaultCapacity()));
    }
    
    @Override
    public ChestMenu openFor(Player p)
    {
        
        Map<UUID, ItemStack> itemMap = auctionDataManager.getVaultContents(p);
        int mapSize = itemMap.size();
        
        int capacity = auctionConfig.getVaultCapacity(p);
        
        if(mapSize > capacity)
        {
            this.size = ChestSize.fit(mapSize).getValue();
        }
        else
        {
            this.size = ChestSize.fit(capacity).getValue();
        }
        this.items = new BusyItem[this.size];
        
        int slot = 0;
        for(Entry<UUID, ItemStack> e : itemMap.entrySet())
        {
            setItem(slot, new VaultItem(slot, e.getKey(), e.getValue()));
            slot++;
        }
        
        opened.add(p);
        
        return super.openFor(p);
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e)
    {
        super.onMenuClose(e);
        opened.remove(e.getPlayer());
    }
    
    public static Collection<Player> closeAll()
    {
        
        Collection<Player> result = new HashSet<Player>(opened);
        
        for(Player p : opened)
        {
            p.closeInventory();
        }
        
        opened.clear();
        
        return result;
    }

    private class VaultItem extends BusyItem
    {
        
        private final int slot;
        private final UUID uuid;
        
        public VaultItem(int slot, UUID uuid, ItemStack look)
        {
            super(look);
            this.slot = slot;
            this.uuid = uuid;
        }

        @Override
        public void onClick(ItemClickEvent e)
        {
            
            Player p = e.getPlayer();
            
            if(p.getInventory().firstEmpty() == -1) return;
            
            setItem(slot, null);
            e.setReloadMenu(true);
            auctionDataManager.removeVaultItem(p, uuid);
            
            p.getInventory().addItem(look);
            
        }
        
    }
    
}
