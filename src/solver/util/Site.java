package solver.util;

import java.awt.Color;

public enum Site {
	
	YELLOW("https://www.pathery.com", Color.YELLOW),
	RED("http://beta.pathery.net", Color.RED),
	BLUE("http://blue.pathery.net", Color.BLUE);
	
	public final String urlStub;
	public final Color color;

	private Site(String urlStub, Color color) {
		this.urlStub = urlStub;
		this.color = color;
	}
	
	public String getUrlStub() {
		return urlStub;
	}

	public Color getColor() {
		return color;
	}

	
}
