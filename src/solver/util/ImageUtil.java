package solver.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageUtil {

	private static final Map<Type, BufferedImage> cache = new HashMap<Type, BufferedImage>();
	
	public static BufferedImage getImageForType(Type type) {
		BufferedImage img = cache.get(type);
		if(img==null) {
			img = readImage(type);
			cache.put(type, img);
		}
		return img;
	}

	private static BufferedImage readImage(Type type) {
		String name = null;
		String color = null;
		switch(type) {

		// TODO mapping for new stuff
//		.
		case BLOCKED_BY_MAP: name = "OverlayTileFaceted50b.png"; color = "#b85555"; break;
		case BLOCKED_BY_USER: name = "OverlayTileFaceted50b.png"; color = "#666666"; break;
		case END: name = "OverlayFinish50c.png"; color = "#606060"; break;
		case NORMAL: name = "OverlayTile10.png"; color = "#F5FBFE"; break;
		case START: name = "OverlayStart50b.png"; color = "#fbfefb"; break;
		case START_RED: name = "OverlayStart2.png"; color = "#fbfefb"; break;
		case WP_A: name = "Waypoints_A.png"; color = "#F777FF"; break;
		case WP_B: name = "Waypoints_B.png"; color = "#FFFF11"; break;
		case WP_C: name = "Waypoints_C.png"; color = "#FF4466"; break;
		case WP_D: name = "Waypoints_D.png"; color = "#ff9911"; break;
		case WP_E: name = "Waypoints_E.png"; color = "#00FFFF"; break;
		case WP_F: name = "Waypoints_F.png"; color = "#a12ec4"; break;
		case WP_G: name = "Waypoints_G.png"; color = "#46c0a0"; break;
		case WP_H: name = "Waypoints_H.png"; color = "#33ff33"; break;
		case WP_I: name = "Waypoints_I.png"; color = "#f032e6"; break;
		case WP_J: name = "Waypoints_J.png"; color = "#d2f53c"; break;
		case WP_K: name = "Waypoints_K.png"; color = "#fabebe"; break;
		case WP_L: name = "Waypoints_L.png"; color = "#9090f4"; break;
		case WP_M: name = "Waypoints_M.png"; color = "#e6beff"; break;
		case WP_N: name = "Waypoints_N.png"; color = "#aa6e28"; break;
		case TP_1_IN: name = "TeleportInW.png"; color = "#3377AA"; break;
		case TP_2_IN: name = "TeleportIn.png"; color = "#44EE66"; break;
		case TP_3_IN: name = "TeleportInW.png"; color = "#992200"; break;
		case TP_4_IN: name = "TeleportIn.png"; color = "#55CCFF"; break;
		case TP_5_IN: name = "TeleportInW.png"; color = "#005533"; break;
		case TP_6_IN: name = "TeleportIn.png"; color = "#FFF"; break;
		case TP_7_IN: name = "TeleportInW.png"; color = "#000"; break;
		case TP_1_OUT: name = "TeleportOutW.png"; color = "#3377AA"; break;
		case TP_2_OUT: name = "TeleportOut.png"; color = "#44EE66"; break;
		case TP_3_OUT: name = "TeleportOutW.png"; color = "#992200"; break;
		case TP_4_OUT: name = "TeleportOut.png"; color = "#55CCFF"; break;
		case TP_5_OUT: name = "TeleportOutW.png"; color = "#005533"; break;
		case TP_6_OUT: name = "TeleportOut.png"; color = "#FFF"; break;
		case TP_7_OUT: name = "TeleportOutW.png"; color = "#000"; break;
		case GREEN_ONLY: name = "Path2xRock.png"; color = "#ffffff"; break;
		case RED_ONLY: name = "Path1xRock.png"; color = "#ffffff"; break;
		case ICE: name = "PathableOnly1.png"; color = "#44ffff"; break;
		case PATH: name = "PathableOnly1.png"; color = "#FFFFEE"; break;
		default: throw new RuntimeException("Unexpected type: " + type);
		}
		BufferedImage result = new BufferedImage(35, 35, BufferedImage.TYPE_INT_RGB);
		try {
			BufferedImage i = ImageIO.read(new File("images\\" + name));
			result.getGraphics().drawImage(i, 0, 0, Color.decode(color), null);
		} catch (IOException e) {
			throw new RuntimeException("Error reading " + "images\\" + name, e);
		}
		return result;
	}

}
