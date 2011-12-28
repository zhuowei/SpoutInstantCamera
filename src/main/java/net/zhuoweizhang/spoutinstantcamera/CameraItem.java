package net.zhuoweizhang.spoutinstantcamera;

import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;

import org.getspout.spoutapi.block.SpoutBlock;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.material.item.GenericCustomItem;

public class CameraItem extends GenericCustomItem {

	private final SpoutInstantCameraPlugin plugin;

	protected CameraItem(SpoutInstantCameraPlugin plugin) {
		super(plugin, plugin.cameraItemName, plugin.cameraItemTextureUrl);
		this.plugin = plugin;
	}

	@Override
	public boolean onItemInteract(SpoutPlayer player, SpoutBlock block, BlockFace face) {
		if (plugin.ignoreRightClickOnContainers && block.getState() instanceof ContainerBlock) return false;
		return plugin.onTakePicture(player);
	}

}
