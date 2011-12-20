package net.zhuoweizhang.spoutinstantcamera;

import org.getspout.spoutapi.gui.*;

public class PhotoDisplayPopup extends GenericPopup {

	private SpoutInstantCameraPlugin plugin;
	private short imageId;
	private Texture imageTexture;

	public PhotoDisplayPopup(SpoutInstantCameraPlugin plugin, short imageId) {
		super();
		this.plugin = plugin;
		this.imageId = imageId;
		imageTexture = new GenericTexture("instantcam_" + imageId + ".png");
		attachWidget(plugin, imageTexture);
	}

}
