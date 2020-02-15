package solver.util;

public enum Type {

	NORMAL("  "),
	START("S "),
	END("X "),
	WP_A("A "), 
	WP_B("B "),
	WP_C("C "),
	WP_D("D "),
	WP_E("E "),
	WP_F("F "),
	WP_G("G "),
	WP_H("H "),
	WP_I("I "),
	WP_J("J "),
	WP_K("K "),
	WP_L("L "),
	WP_M("M "),
	WP_N("N "),
	TP_1_IN("1i", 1),
	TP_1_OUT("1o"),
	TP_2_IN("2i", 2),
	TP_2_OUT("2o"),
	TP_3_IN("3i", 4),
	TP_3_OUT("3o"),
	TP_4_IN("4i", 8),
	TP_4_OUT("4o"),
	TP_5_IN("5i", 16),
	TP_5_OUT("5o"),
	TP_6_IN("6i", 32),
	TP_6_OUT("6o"),
	TP_7_IN("7i", 64),
	TP_7_OUT("7o"),
	PATH("__"),
	ICE("++"),
	BLOCKED_BY_USER("@ "),
	BLOCKED_BY_MAP("# "),
	GREEN_ONLY("GO"),
	RED_ONLY("RO"),
	START_RED("SR");
	
	private final String literal;
	public final int bitMask;
	
	private Type(String literal, int bitMask) {
		this.literal = literal;
		this.bitMask = bitMask;
	}
	
	private Type(String literal) {
		this.literal = literal;
		bitMask = 0;
	}

	public String getLiteral() {
		return literal;
	}
	public static boolean isTP_IN(Type type) {
		return type==TP_1_IN || type==TP_2_IN || type==TP_3_IN || type==TP_4_IN || type==TP_5_IN || type==TP_6_IN || type==TP_7_IN;
	}
	
	public static Type[] getWPs() {
		return new Type[] {WP_A, WP_B, WP_C, WP_D, WP_E, WP_F, WP_G, WP_H, WP_I, WP_J, WP_K, WP_L, WP_M, WP_N};
	}

}
