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
		imageTexture.setWidth(320);
		imageTexture.setHeight(180);
		imageTexture.setX(54);
		imageTexture.setY(30);
		imageTexture.setAnchor(WidgetAnchor.SCALE);
		attachWidget(plugin, imageTexture);
	}

}
