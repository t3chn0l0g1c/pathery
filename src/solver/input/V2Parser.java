package solver.input;

import java.util.HashMap;
import java.util.Map;

import solver.util.Type;
import solver.util.Util;

public class V2Parser {

	public static final String START_TOKEN = "code\":\"";
	
	private static Map<String, Type> map = null;
	
	private static Type getType(int x, int y, String substring) {
		if(map==null) {
			map = new HashMap<String, Type>();
			map.put("s1", Type.START);
			map.put("s2", Type.START_RED);
			map.put("f1", Type.END);
			map.put("p1", Type.PATH);
			map.put("x1", Type.RED_ONLY);
			map.put("x2", Type.GREEN_ONLY);
			map.put("z5", Type.ICE);
			map.put("r1", Type.BLOCKED_BY_MAP);
			map.put("r2", Type.BLOCKED_BY_MAP);
			map.put("r3", Type.BLOCKED_BY_MAP);
			map.put("c1", Type.WP_A);
			map.put("c2", Type.WP_B);
			map.put("c3", Type.WP_C);
			map.put("c4", Type.WP_D);
			map.put("c5", Type.WP_E);
			map.put("c6", Type.WP_F);
			map.put("c7", Type.WP_G);
			map.put("c8", Type.WP_H);
			map.put("c9", Type.WP_I);
			map.put("c10", Type.WP_J);
			map.put("c11", Type.WP_K);
			map.put("c12", Type.WP_L);
			map.put("c13", Type.WP_M);
			map.put("c14", Type.WP_N);
			map.put("t1", Type.TP_1_IN);
			map.put("t2", Type.TP_2_IN);
			map.put("t3", Type.TP_3_IN);
			map.put("t4", Type.TP_4_IN);
			map.put("t5", Type.TP_5_IN);
			map.put("t6", Type.TP_6_IN);
			map.put("t7", Type.TP_7_IN);
			map.put("u1", Type.TP_1_OUT);
			map.put("u2", Type.TP_2_OUT);
			map.put("u3", Type.TP_3_OUT);
			map.put("u4", Type.TP_4_OUT);
			map.put("u5", Type.TP_5_OUT);
			map.put("u6", Type.TP_6_OUT);
			map.put("u7", Type.TP_7_OUT);
		}
		
		Type t = map.get(substring);
		
		if(t==null) {
			throw new RuntimeException("Unknown type at " + x + "/" + y + " : " + substring);
		}
		
		return t;
	}
	
	// 11.14.16.Testmap...:,s1.1,s2.1,f1.1,p1.1,x1.1,x2.11,z5.1,r1.1,r2.1,r3.3,c1.11,c2.1,c3.1,c4.1,c5.1,c6.1,c7.11,c8.1,c9.1,c10.1,c11.1,c12.1,c13.11,c14.1,t1.1,t2.1,t3.1,t4.1,t5.11,t6.1,t7.1,u1.1,u2.1,u3.1,u4.11,u5.1,u6.1,u7.
	
		//,"code":"27.19.999.Ultra Complex Unlimited...:,s1.25,r1.,r1.14,u1.7,z5.,r1.1,f1.,s1.9,r1.1,u2.,t4.1,r1.3,z5.6,r1.,r1.3,r1.3,u4.17,f1.,s1.5,r1.3,c5.3,r1.11,r1.,r1.5,r1.,r1.8,c3.3,u3.2,z5.2,f1.,s1.4,c2.14,c3.5,r1.,r1.12,c1.4,r1.,r1.6,f1.,s1.2,c9.6,z5.15,r1.,r1.25,f1.,s1.15,r1.9,r1.,r1.22,z5.,r1.1,f1.,s1.9,z5.15,r1.,r1.25,f1.,s1.2,r1.,c6.5,r1.2,t1.9,r1.2,r1.,r1.11,t2.2,r1.6,r1.3,f1.,s1.8,t3.1,r1.1,z5.,c7.11,r1.,r1.3,r1.2,c4.15,z5.,c8.1,f1.,s1.25,r1.","oldCode":null}
		
	public static MazeDescriptor createDescriptor(String site, String string) {
		System.out.println(string);
		String idString = string.substring(6, string.indexOf(","));
		string = string.substring(string.indexOf(START_TOKEN) + START_TOKEN.length());
		
		String mapCode = string;
		
		int idx = string.indexOf(".");
		
		int width = Integer.valueOf(string.substring(0, idx));
		string = string.substring(idx+1);
		idx = string.indexOf(".");
		
		int height = Integer.valueOf(string.substring(0, idx));
		string = string.substring(idx+1);
		idx = string.indexOf(".");
		
		int blocks = Integer.valueOf(string.substring(0, idx));
		string = string.substring(idx+1);
		
		idx = string.indexOf(":");
		String name = string.substring(0, idx);
		string = string.substring(idx+1, string.indexOf("\""));

		
		String[] array = string.split(",");
		Type[][] types = new Type[height][];
		for(int i = 0; i<types.length; i++) {
			types[i] = new Type[width];
			for(int j = 0; j<width; j++) {
				types[i][j] = Type.NORMAL;
			}
		}
		
		int x = 0;
		int y = 0;
		
		for(String s : array) {
			if(Util.isNullOrEmpty(s)) {
				continue;
			}
			
			int i = s.indexOf(".");
			if(i>1) {
				types[y][x] = getType(x, y, s.substring(0, i));
				x++;
				if(x>=width) {
					y += x/width;
					x = x%width;
				}
			}
			s = s.substring(i+1);
			if(!Util.isNullOrEmpty(s)) {
				int add = Integer.valueOf(s);
				x += add;
			}
			if(x>=width) {
				y += x/width;
				x = x%width;
			}
		}
		return new MazeDescriptor(site, idString, name, width, height, types, blocks, mapCode, true);
	}
}
