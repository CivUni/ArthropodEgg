package co.civcraft.arthropodegg;

import static co.civcraft.arthropodegg.ArthropodEggPlugin.log;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;

/**
 * @author Randy
 * @since 1.0
 * <p>
 * All the event listeners related to ArthropodEggPlugin plugin. Handles
 * each case that is currently configured for handling.
 */
public class ArthropodEggEntityListener implements Listener {

	private ArthropodEggPlugin plugin;

	public ArthropodEggEntityListener(ArthropodEggPlugin instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Player targetPlayer = event.getEntity().getKiller();
		if (null == targetPlayer) {
			return;
		}
		// Check for a baby animal
		if (event.getEntity() instanceof Ageable) {
			Ageable ageableEntity = (Ageable) event.getEntity();
			if (!ageableEntity.isAdult()) {
				if(!plugin.getConfig().getBoolean("babyDrop")){
					// babies can't drop spawn eggs
					return;
				}
			}
		}

		// Check the player's currently equipped weapon
		ItemStack handstack = targetPlayer.getItemInHand();
		// Get the map of enchantments on that item
		Map<Enchantment, Integer> itemEnchants = handstack.getEnchantments();
		if (itemEnchants.isEmpty()) {
			return;
		}
		boolean arthropodsOn = true;
		boolean smiteOn = true;
		// Check if one enchantment is BaneOfArthropods
		if (null == itemEnchants.get(org.bukkit.enchantments.Enchantment.DAMAGE_ARTHROPODS)) {
			arthropodsOn = false;
		}

		if(null == itemEnchants.get(Enchantment.DAMAGE_UNDEAD)) {
			smiteOn = false;
		}

		if(!arthropodsOn && !smiteOn) {
			return;
		}
		//declorations
		double randomNum = Math.random();
		double eggArthropodPercentage = plugin.getConfig().getDouble("eggArthropodPercentage");
		double eggLootingPercentage = plugin.getConfig().getDouble("eggLootingPercentage");
		double targetPercentage;
		//egg spawning
		if (arthropodsOn) {
			Short currentEntityID = event.getEntity().getType().getTypeId();
			//check if id list is on and if so blacklist certain mobs
			if(!plugin.getConfig().getBoolean("ignoreIDList")) {
				if (!plugin.getConfig().getShortList("eggEntityIDList").contains(currentEntityID)) {
					return;
				}
			}
			double levelOfArthropod = handstack.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
			double levelOfLooting = handstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

			targetPercentage = (eggArthropodPercentage * levelOfArthropod) + (eggLootingPercentage *
					levelOfLooting);
			if (plugin.getConfig().getBoolean("eggDebug")) {
				targetPlayer.sendMessage("Arth[" + levelOfArthropod + "], Loot[" + levelOfLooting + "]");
				targetPlayer.sendMessage("Total =" + targetPercentage * 100 + "%, random% is " + randomNum * 100);
			}
			if (randomNum < targetPercentage) {
				// Figure out the right item type to drop
				ItemStack item = new ItemStack(Material.MONSTER_EGG, 1);
				SpawnEggMeta spawnMeta = (SpawnEggMeta) item.getItemMeta();
				spawnMeta.setSpawnedType(event.getEntityType());
				item.setItemMeta(spawnMeta);
				if (plugin.getConfig().getBoolean("eggRemoveDrops")) {
					event.getDrops().clear();
					event.setDroppedExp(0);
				}
				event.getDrops().add(item);
				if (plugin.getConfig().getBoolean("eggDebug")) {
					targetPlayer.sendMessage("Egg generated.");
				}
			}
		}
		//we need a new random number.
		randomNum = Math.random();
		double mobSmitePercentage = plugin.getConfig().getDouble("mobSmitePercentage");
		double mobLootingPercentage = plugin.getConfig().getDouble("mobLootingPercentage");
		//Mob head spawning
		if (smiteOn) {
			double levelOfSmite = handstack.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
			double levelOfLooting = handstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

			targetPercentage = (mobSmitePercentage * levelOfSmite) + (mobLootingPercentage *
					levelOfLooting);
			if (plugin.getConfig().getBoolean("mobDebug")) {
				targetPlayer.sendMessage("Arth[" + levelOfSmite + "], Loot[" + levelOfLooting + "]");
				targetPlayer.sendMessage("Total =" + targetPercentage * 100 + "%, random% is " + randomNum * 100);
			}
			if (randomNum < targetPercentage) {
				// Figure out the right item type to drop
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
				if (plugin.getConfig().getBoolean("mobRemoveDrops")) {
					event.getDrops().clear();
					event.setDroppedExp(0);
				}
				boolean animalOnList = true;
				switch(event.getEntityType()) {
					case CHICKEN:
						skullMeta.setOwner("MHF_Chicken");
						skullMeta.setDisplayName("Chicken Head");
						break;
					case COW:
						skullMeta.setOwner("MHF_Cow");
						skullMeta.setDisplayName("Cow Head");
						break;
					case ENDERMAN:
						skullMeta.setOwner("MHF_Enderman");
						skullMeta.setDisplayName("Enderman Head");
						break;
					case GHAST:
						skullMeta.setOwner("MHF_Ghast");
						skullMeta.setDisplayName("Ghast Head");
						break;
					case MAGMA_CUBE:
						skullMeta.setOwner("MHF_LavaSlime");
						skullMeta.setDisplayName("Magma Cube Head");
						break;
					case MUSHROOM_COW:
						skullMeta.setOwner("MHF_MushroomCow");
						skullMeta.setDisplayName("Mooshroom Head");
						break;
					case PIG:
						skullMeta.setOwner("MHF_Pig");
						skullMeta.setDisplayName("Pig Head");
						break;
					case PIG_ZOMBIE:
						skullMeta.setOwner("MHF_PigZombie");
						skullMeta.setDisplayName("Zombie Pigman Head");
						break;
					case SHEEP:
						skullMeta.setOwner("MHF_Sheep");
						skullMeta.setDisplayName("Sheep Head");
						break;
					case SLIME:
						skullMeta.setOwner("MHF_Slime");
						skullMeta.setDisplayName("Slime Head");
						break;
					case SPIDER:
						skullMeta.setOwner("MHF_Spider");
						skullMeta.setDisplayName("Spider Head");
						break;
					case SQUID:
						skullMeta.setOwner("MHF_Squid");
						skullMeta.setDisplayName("Squid Head");
						break;
					case CREEPER:
						skullMeta.setOwner("MHF_Creeper");
						skullMeta.setDisplayName("Creeper Head");
						break;
					case SKELETON:
						skullMeta.setOwner("MHF_Skeleton");
						skullMeta.setDisplayName("Skeleton Head");
						break;
					default:
						animalOnList = false;
						break;
				}
				skull.setItemMeta(skullMeta);
				if (animalOnList) {
					event.getDrops().add(skull);
					if (plugin.getConfig().getBoolean("mobDebug")) {
						targetPlayer.sendMessage("Head generated.");
					}
				}
				else
				{
					if (plugin.getConfig().getBoolean("mobDebug")) {
						targetPlayer.sendMessage("Animal not on head list.");
					}
				}
			}
		}
	}
}
