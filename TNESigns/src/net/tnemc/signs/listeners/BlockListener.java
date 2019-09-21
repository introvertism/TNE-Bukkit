package net.tnemc.signs.listeners;

import net.tnemc.core.TNE;
import net.tnemc.core.common.api.IDFinder;
import net.tnemc.core.common.module.ModuleListener;
import net.tnemc.core.event.module.TNEModuleDataEvent;
import net.tnemc.signs.ChestHelper;
import net.tnemc.signs.SignsData;
import net.tnemc.signs.SignsManager;
import net.tnemc.signs.SignsModule;
import net.tnemc.signs.signs.SignType;
import net.tnemc.signs.signs.TNESign;
import net.tnemc.signs.signs.impl.ItemSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

/**
 * The New Economy Minecraft Server Plugin
 * <p>
 * Created by Daniel on 5/29/2018.
 * <p>
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to
 * Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 * Created by creatorfromhell on 06/30/2017.
 */
public class BlockListener implements ModuleListener {

  private TNE plugin;

  public BlockListener(TNE plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockBreakEvent(final BlockBreakEvent event) {
    try {

      final TNESign sign = (event.getBlock().getState() instanceof Sign) ?
          SignsData.loadSign(event.getBlock().getLocation())
          : SignsData.loadSignAttached(event.getBlock().getLocation());

      if (sign != null) {
        final Sign signBlock = (event.getBlock().getState() instanceof Sign) ?
            (Sign) event.getBlock().getState() : SignsManager.getAttachedSign(event.getBlock());
        if (signBlock != null && !SignsModule.manager().getType(sign.getType()).onSignDestroy(sign.getOwner(), event.getPlayer().getUniqueId())
            && !event.getPlayer().hasPermission("tne.sign.antiprotect")) {
          event.setCancelled(true);
        } else {
          if (signBlock != null) {
            SignsData.deleteSign(signBlock.getBlock().getLocation());
          }
        }
      }
    } catch(SQLException e) {
      e.printStackTrace();
    }

    if(!event.isCancelled()) {
      if(event.getBlock().getType().equals(Material.CHEST)) {
        ChestHelper helper = new ChestHelper((Chest)event.getBlock().getState());

        try {
          UUID chest = ItemSign.chest(event.getBlock().getLocation());

          if(chest != null &&!event.getPlayer().getUniqueId().equals(chest) && !event.getPlayer().hasPermission("tne.shop.override")) {
            event.setCancelled(true);
          }
        } catch (SQLException e) {
          TNE.debug(e);
        }

        if(!event.isCancelled() && helper.isDouble()) {

          try {
            UUID chest = ItemSign.chest(helper.getDoubleChest().getLocation());

            if(chest != null &&!event.getPlayer().getUniqueId().equals(chest) && !event.getPlayer().hasPermission("tne.shop.override")) {
              event.setCancelled(true);
            }
          } catch (SQLException e) {
            TNE.debug(e);
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onChange(SignChangeEvent event) {
    final Player player = event.getPlayer();
    final Block block = event.getBlock();
    final String[] lines = event.getLines();
    final Location location = event.getBlock().getLocation();
    Block basicAttached = (block != null && block.getState().getData() instanceof org.bukkit.material.Sign)?
        block.getRelative(((org.bukkit.material.Sign)block.getState().getData()).getAttachedFace()) : null;
    final Block attached = (basicAttached == null || basicAttached.getLocation().equals(block.getLocation()))? block.getWorld().getBlockAt(block.getLocation().add(0, -1, 0)) : basicAttached;

    Bukkit.getScheduler().runTaskAsynchronously(TNE.instance(), ()->{
      if(lines.length > 0 && SignsManager.validSign(lines[0])) {
        SignType type = SignsModule.manager().getType(lines[0]);
        if(type != null) {

          TNEModuleDataEvent dataEvent = new TNEModuleDataEvent("SignCreate", new HashMap<>(), !Bukkit.isPrimaryThread());
          dataEvent.addData("type", type.name());
          dataEvent.addData("location", location);
          dataEvent.addData("creator", player.getUniqueId());

          if(attached != null) {
            dataEvent.addData("attached", attached);
          }
          Bukkit.getPluginManager().callEvent(dataEvent);

          if (!dataEvent.isCancelled() && type.create(event, attached, IDFinder.getID(player))) {

            Bukkit.getScheduler().runTask(TNE.instance(), ()->{
              Sign sign = (Sign)block.getState();
              sign.setLine(0, type.success() + lines[0]);
              sign.update(true);
            });
          } else {

            Bukkit.getScheduler().runTask(TNE.instance(), ()->{
              Sign sign = (Sign)block.getState();
              sign.setLine(0, ChatColor.RED + "Failed");
              sign.update(true);
            });
          }
        }
      }
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockBurn(final BlockBurnEvent event) {
    final Sign sign = (event.getBlock().getState() instanceof Sign)?
        (Sign)event.getBlock().getState() : SignsManager.getAttachedSign(event.getBlock());

    if(sign != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockIgnite(final BlockIgniteEvent event) {
    final Sign sign = (event.getBlock().getState() instanceof Sign)?
        (Sign)event.getBlock().getState() : SignsManager.getAttachedSign(event.getBlock());

    if(sign != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPistonExtend(final BlockPistonExtendEvent event) {
    final Sign sign = (event.getBlock().getState() instanceof Sign)?
        (Sign)event.getBlock().getState() : SignsManager.getAttachedSign(event.getBlock());

    if(sign != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPistonRetract(final BlockPistonRetractEvent event) {
    final Sign sign = (event.getBlock().getState() instanceof Sign)?
        (Sign)event.getBlock().getState() : SignsManager.getAttachedSign(event.getBlock());

    if(sign != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockExplode(final BlockExplodeEvent event) {
    final Sign sign = (event.getBlock().getState() instanceof Sign)?
        (Sign)event.getBlock().getState() : SignsManager.getAttachedSign(event.getBlock());

    if(sign != null) {
      event.setCancelled(true);
    }
  }
}