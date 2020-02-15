package solver.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import solver.util.Type;

public class V1Parser {

	public static final String START_TOKEN = "code\":\"";
	
	private static Map<String, Type> map = null;
	
	private static Type getType(int x, int y, String substring) {
		if(map==null) {
			map = new HashMap<String, Type>();
			map.put("s", Type.START);
			map.put("f", Type.END);
			map.put("r", Type.BLOCKED_BY_MAP);
			map.put("a", Type.WP_A);
			map.put("b", Type.WP_B);
			map.put("c", Type.WP_C);
			map.put("d", Type.WP_D);
			map.put("e", Type.WP_E);
			map.put("t", Type.TP_1_IN);
			map.put("u", Type.TP_1_OUT);
			map.put("m", Type.TP_2_IN);
			map.put("n", Type.TP_2_OUT);
			map.put("g", Type.TP_3_IN);
			map.put("h", Type.TP_3_OUT);
			map.put("i", Type.TP_4_IN);
			map.put("j", Type.TP_4_OUT);
			map.put("k", Type.TP_5_IN);
			map.put("l", Type.TP_5_OUT);
			map.put("S", Type.START_RED);
			map.put("X", Type.GREEN_ONLY);
			map.put("p", Type.PATH);
			map.put("x", Type.RED_ONLY);
			map.put("q", Type.BLOCKED_BY_MAP);
			map.put("R", Type.BLOCKED_BY_MAP);
		}
		
		Type t = map.get(substring);
		
		if(t==null) {
			throw new RuntimeException("Unknown type at " + x + "/" + y + " : " + substring);
		}
		
		return t;
	}
	
	private static String getString(Iterator<String> iter, String tag) {
		while(iter.hasNext()) {
			String s = iter.next().replaceAll("\\\"", "");
			if(s.startsWith(tag)) {
				String[] result = s.split(":");
				if(result.length==2) {
					return result[1];
				} else {
					return "";
				}
			}
		}
		throw new RuntimeException(tag + " not found.");
	}
	
	
	public static MazeDescriptor createDescriptor(String site, String string) {
		System.out.println(string);
		String idString = string.substring(6, string.indexOf(","));
//		.
		// TODO BUG!!
//		int id = Integer.valueOf(idString);
		String name = "";
		if(string.contains("name")) {
			String[] a = string.split(",");
			List<String> omg = Arrays.asList(a);
			Iterator<String> iter = omg.iterator();
			name = getString(iter, "name");
		}
		string = string.substring(string.indexOf(START_TOKEN) + START_TOKEN.length());
		String mapCode = string;
		
		int width = Integer.valueOf(string.substring(0, string.indexOf("x")));
		int height = Integer.valueOf(string.substring(string.indexOf("x")+1, string.indexOf(".")));

		string = string.substring(string.indexOf(".w")+2);
		
		int blocks = Integer.valueOf(string.substring(0, string.indexOf(".")));
		
		string = string.substring(string.indexOf(":")+1, string.indexOf("\"")-1);
		
		int x = 0;
		int y = 0;
		
		String[] array = string.split("\\.");
		Type[][] types = new Type[height][];
		for(int i = 0; i<types.length; i++) {
			types[i] = new Type[width];
			for(int j = 0; j<width; j++) {
				types[i][j] = Type.NORMAL;
			}
		}
		for(String s : array) {
			int add = Integer.valueOf(s.substring(0, s.length()-1));
			
			x += add;
			if(x>=width) {
				y += x/width;
				x = x%width;
			}
			Type c = getType(x, y, s.substring(s.length()-1, s.length()));
			types[y][x] = c;
			
			x++;
			if(x>=width) {
				y += x/width;
				x = x%width;
			}
		}
		return new MazeDescriptor(site, idString, name, width, height, types, blocks, mapCode, true);
	}
}
