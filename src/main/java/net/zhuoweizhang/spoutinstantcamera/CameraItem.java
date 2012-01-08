package net.zhuoweizhang.spoutinstantcamera;

import org.bukkit.block.*;

import org.getspout.spoutapi.block.SpoutBlock;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.material.item.GenericCustomItem;
import org.bukkit.Material;

public class CameraItem extends GenericCustomItem {

	private final SpoutInstantCameraPlugin plugin;

	protected CameraItem(SpoutInstantCameraPlugin plugin) {
		super(plugin, plugin.cameraItemName, plugin.cameraItemTextureUrl);
		this.plugin = plugin;
	}

	@Override
	public boolean onItemInteract(SpoutPlayer player, SpoutBlock block, BlockFace face) {
		if (plugin.ignoreRightClickOnContainers && block != null &&
			(block.getState() instanceof ContainerBlock || block.getState() instanceof NoteBlock || 
				block.getState() instanceof Jukebox || block.getType() == Material.WOODEN_DOOR ||
				block.getType() == Material.IRON_DOOR_BLOCK || block.getType() == Material.TRAP_DOOR ||
				block.getType() == Material.DRAGON_EGG))
			return false; //Did I miss anything?
		return plugin.onTakePicture(player);
	}

}
