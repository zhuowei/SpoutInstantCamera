package net.zhuoweizhang.spoutinstantcamera;

import org.bukkit.Bukkit;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

import org.getspout.spoutapi.*;
import org.getspout.spoutapi.inventory.*;
import org.getspout.spoutapi.material.*;
import org.getspout.spoutapi.material.item.*;
import org.getspout.spoutapi.player.*;
import org.getspout.spoutapi.event.screen.*;
import org.getspout.spoutapi.sound.*;

import org.getspout.commons.io.CRCStore;
import org.getspout.commons.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.getspout.spoutapi.packet.PacketPreCacheFile;

import java.io.*;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SpoutInstantCameraPlugin extends JavaPlugin {

	public CameraItem cameraItem;

	public String cameraItemTextureUrl, cameraShutterSoundUrl, cameraItemName;

	private SpoutInstantCameraScreenListener screenListener = new SpoutInstantCameraScreenListener();
	private SpoutInstantCameraPlayerListener playerListener = new SpoutInstantCameraPlayerListener();

	private Set<SpoutPlayer> playersTakingPictures = new HashSet<SpoutPlayer>();
	private FileConfiguration config;

	public static SpoutInstantCameraPlugin thePlugin;

	public short nextPhotoId;

	public SpoutShapedRecipe cameraRecipe;

	public void onDisable() {
		config.set("cameraItemTextureUrl", cameraItemTextureUrl);
		config.set("cameraShutterSoundUrl", cameraShutterSoundUrl);
		config.set("cameraItemName", cameraItemName);
		config.set("nextPhotoId", nextPhotoId);
		this.saveConfig();
		System.out.println(this + " is now disabled!");
	}

	public void onEnable() {
		thePlugin = this;
		config = this.getConfig();
		this.cameraItemTextureUrl = config.getString("cameraItemTextureUrl", "http://cloud.github.com/downloads/zhuowei/SpoutInstantCamera/camera.png");
		this.cameraShutterSoundUrl = config.getString("cameraShutterSoundUrl", "http://cloud.github.com/downloads/zhuowei/SpoutInstantCamera/shutter.ogg");
		this.cameraItemName = config.getString("cameraItemName", "Camera");
		this.nextPhotoId = (short) config.getInt("nextPhotoId", 1);
		this.saveConfig();
		FileManager fileManager = SpoutManager.getFileManager();
		fileManager.addToCache(this, cameraItemTextureUrl);
		fileManager.addToCache(this, cameraShutterSoundUrl);
		cameraItem = new CameraItem(this);
		cameraRecipe = new SpoutShapedRecipe(new SpoutItemStack(cameraItem, 1)).shape("iii", "ird", "iii").
			setIngredient('i', MaterialData.ironIngot).setIngredient('r', MaterialData.redstone).setIngredient('d', MaterialData.diamondBlock);
		SpoutManager.getMaterialManager().registerSpoutRecipe(cameraRecipe);
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.CUSTOM_EVENT, screenListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		System.out.println(this + " is now enabled!");
		System.out.println(this + " Item id: " + cameraItem.getCustomId());
		System.out.println(this + " next photo damage val: " + nextPhotoId);
	}

	public boolean onTakePicture(SpoutPlayer player) {
		if (!player.hasPermission("spoutinstantcamera.use")) {
			player.sendMessage("You don't have permission to use the " + cameraItemName);
			return false;
		}
		playersTakingPictures.add(player);
		player.sendScreenshotRequest();
		SpoutManager.getSoundManager().playGlobalCustomSoundEffect(this, cameraShutterSoundUrl, false, player.getLocation());
		return true;
	}

	private void processScreenshot(SpoutPlayer player, BufferedImage image) {
		try {
			short imageId = nextPhotoId;
			File imageFolder = new File(this.getDataFolder(), "pictures");
			if (!imageFolder.exists())
				imageFolder.mkdir();
			File imageFile = new File(imageFolder, "instantcam_" + imageId + ".png");
			ImageIO.write(image, "png", imageFile);
			nextPhotoId++;
			PlayerInventory inventory = player.getInventory();
			ItemStack stack = new ItemStack(org.bukkit.Material.SIGN, 1, imageId);
			player.getWorld().dropItemNaturally(player.getLocation(), stack);
			player.sendMessage("The camera ejects a photograph.");
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage("The film got jammed in the camera.");
		}
	}

	private void cachePictureFor(SpoutPlayer player, short id) {
		File imageFolder = new File(this.getDataFolder(), "pictures");
		File file = new File(imageFolder, "instantcam_" + id + ".png");
		String fileName = FileUtil.getFileName(file.getPath());
		long crc = -1;
		try {
			crc = FileUtil.getCRC(file, FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (crc !=-1) {
			player.sendPacket(new PacketPreCacheFile(this.getDescription().getName(), file.getPath(), crc, false));
		}

	}

	public boolean openPictureScreen(SpoutPlayer player, short id) {
		if (!player.isSpoutCraftEnabled()) {
			return false;
		}
		cachePictureFor(player, id);
		PhotoDisplayPopup popup = new PhotoDisplayPopup(this, id);
		player.getMainScreen().attachPopupScreen(popup);
		return true;
	}


	private class SpoutInstantCameraScreenListener extends ScreenListener {
		public void onScreenshotReceived(ScreenshotReceivedEvent event) {
			SpoutPlayer player = event.getPlayer();
			if (!playersTakingPictures.contains(player)) {
				return;
			}
			playersTakingPictures.remove(player);
			processScreenshot(player, event.getScreenshot());
		}
	}

	private class SpoutInstantCameraPlayerListener extends PlayerListener {
		public void onItemHeldChange(PlayerItemHeldEvent event) {
			SpoutPlayer player = (SpoutPlayer) event.getPlayer();
			ItemStack newStack = player.getInventory().getItem(event.getNewSlot());
			if (newStack.getType().equals(org.bukkit.Material.SIGN) && newStack.getDurability() != 0) {
				player.sendMessage("This sign has a picture painted on one side.");
				if (player.isSpoutCraftEnabled()) {
					player.sendMessage("Right-click to show the picture.");
				} else {
					player.sendMessage("You will need Spoutcraft to see the picture.");
				}
			}
		}
		public void onPlayerInteract(PlayerInteractEvent event) {
			SpoutPlayer player = (SpoutPlayer) event.getPlayer();
			ItemStack stack = player.getItemInHand();
			if (event.isCancelled() || stack.getType() != org.bukkit.Material.SIGN || stack.getDurability() == 0 ||
				!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				return;
			}
			event.setCancelled(true);
			if (player.isSpoutCraftEnabled()) {
				openPictureScreen(player, stack.getDurability());
			} else {
				player.sendMessage("You will need Spoutcraft to see this picture.");
			}
		}
	}

}
