package solver.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import solver.input.MazeDescriptor;
import solver.util.Coordinate;
import solver.util.IceKey;
import solver.util.Type;

public final class Maze {

	public final Field[][] field;
	private final Field[] wp_A;
	private final Field[] wp_B;
	private final Field[] wp_C;
	private final Field[] wp_D;
	private final Field[] wp_E;
	private final Field[] wp_F;
	private final Field[] wp_G;
	private final Field[] wp_H;
	private final Field[] wp_I;
	private final Field[] wp_J;
	private final Field[] wp_K;
	private final Field[] wp_L;
	private final Field[] wp_M;
	private final Field[] wp_N;
	private final Field[] starts;
	private final Field[] ends;
	private final Field[] green_only;
	private final Field[] red_only;
	private final Field[] start_red;

	private final int tp_count;
	private final Field[] tp_1_out;
	private final Field[] tp_2_out;
	private final Field[] tp_3_out;
	private final Field[] tp_4_out;
	private final Field[] tp_5_out;
	private final Field[] tp_6_out;
	private final Field[] tp_7_out;
	
	private final int height;
	private final int width;
	
	private final Field[] resettableFields;
	public final Field[] allFieldsInclNull;
	private final Field[] allTPs;
	
	private int usedTPCount = 0;
	private int tpMask = 0;
	private int blocked_offset = 1000;
	
	public final int numberOfAllFields;
	private final char[] compressedIDs;
	
	public Maze(final MazeDescriptor md, final Field[][] field, Map<IceKey, Field> iceTiles) {
		height = md.getHeight();
		width = md.getWidth();

		if(width*height>=1024) {
			throw new RuntimeException("Too large for compressed IDs!");
		}
		
		this.field = field;
		wp_A = getFieldsForType(Type.WP_A);
		wp_B = getFieldsForType(Type.WP_B);
		wp_C = getFieldsForType(Type.WP_C);
		wp_D = getFieldsForType(Type.WP_D);
		wp_E = getFieldsForType(Type.WP_E);
		wp_F = getFieldsForType(Type.WP_F);
		wp_G = getFieldsForType(Type.WP_G);
		wp_H = getFieldsForType(Type.WP_H);
		wp_I = getFieldsForType(Type.WP_I);
		wp_J = getFieldsForType(Type.WP_J);
		wp_K = getFieldsForType(Type.WP_K);
		wp_L = getFieldsForType(Type.WP_L);
		wp_M = getFieldsForType(Type.WP_M);
		wp_N = getFieldsForType(Type.WP_N);
		starts = getFieldsForType(Type.START);
		ends = getFieldsForType(Type.END);
		green_only = getFieldsForType(Type.GREEN_ONLY);
		red_only = getFieldsForType(Type.RED_ONLY);
		start_red = getFieldsForType(Type.START_RED);
		tp_1_out = getFieldsForType(Type.TP_1_OUT);
		tp_2_out = getFieldsForType(Type.TP_2_OUT);
		tp_3_out = getFieldsForType(Type.TP_3_OUT);
		tp_4_out = getFieldsForType(Type.TP_4_OUT);
		tp_5_out = getFieldsForType(Type.TP_5_OUT);
		tp_6_out = getFieldsForType(Type.TP_6_OUT);
		tp_7_out = getFieldsForType(Type.TP_7_OUT);
		
		int tc = 0;
		if(tp_1_out!=null) {
			tc++;
		}
		if(tp_2_out!=null) {
			tc++;
		}
		if(tp_3_out!=null) {
			tc++;
		}
		if(tp_4_out!=null) {
			tc++;
		}
		if(tp_5_out!=null) {
			tc++;
		}
		if(tp_6_out!=null) {
			tc++;
		}
		if(tp_7_out!=null) {
			tc++;
		}
		tp_count = tc;
		
		List<Field> list = new ArrayList<Field>();
		List<Field> all = new ArrayList<Field>();
		int highestID = 0;
		
		for(Field[] fa : field) {
			for(Field f : fa) {
				all.add(f);
				if(f.getType()!=Type.BLOCKED_BY_MAP) {
					list.add(f);
				}
				if(f.getType()==Type.NORMAL && f.id>highestID) {
					highestID = f.id;
				}
			}
		}
		for(Map.Entry<IceKey, Field> e : iceTiles.entrySet()) {
			all.add(e.getValue());
			list.add(e.getValue());
		}
		
		numberOfAllFields = all.size();

		allFieldsInclNull = new Field[all.size()+1];
		for(int i = 0; i<all.size(); i++) {
			Field f = all.get(i);
			allFieldsInclNull[f.id] = f;
//			extendedIDFields[f.extendedID] = f;
		}
		allTPs = new Field[tp_count];
		int c = 0;
		for(Field f : all) {
			switch(f.getType()) {
			case TP_1_IN:
			case TP_2_IN:
			case TP_3_IN:
			case TP_4_IN:
			case TP_5_IN: 
			case TP_6_IN:
			case TP_7_IN:
				allTPs[c++] = f;
			default: // nothing
			}
		}
		
		resettableFields = list.toArray(new Field[list.size()]);
		compressedIDs = new char[highestID/16 +2];
	}
	
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	// NOTE: returns TP_OUTs for TP_INs!
	// TODO consider [][]?
	public final Field[] get(final Type type) {
		switch (type) {
		case START:
			return starts;
		case END:
			return ends;
		case WP_A:
			return wp_A;
		case WP_B:
			return wp_B;
		case WP_C:
			return wp_C;
		case WP_D:
			return wp_D;
		case WP_E:
			return wp_E;
		case WP_F:
			return wp_F;
		case WP_G:
			return wp_G;
		case WP_H:
			return wp_H;
		case WP_I:
			return wp_I;
		case WP_J:
			return wp_J;
		case WP_K:
			return wp_K;
		case WP_L:
			return wp_L;
		case WP_M:
			return wp_M;
		case WP_N:
			return wp_N;
		case GREEN_ONLY:
			return green_only;
		case RED_ONLY:
			return red_only;
		case START_RED:
			return start_red;
		case TP_1_IN:
			return tp_1_out;
		case TP_1_OUT:
			return tp_1_out;
		case TP_2_IN:
			return tp_2_out;
		case TP_2_OUT:
			return tp_2_out;
		case TP_3_IN:
			return tp_3_out;
		case TP_3_OUT:
			return tp_3_out;
		case TP_4_IN:
			return tp_4_out;
		case TP_4_OUT:
			return tp_4_out;
		case TP_5_IN:
			return tp_5_out;
		case TP_5_OUT:
			return tp_5_out;
		case TP_6_IN:
			return tp_6_out;
		case TP_6_OUT:
			return tp_6_out;
		case TP_7_IN:
			return tp_7_out;
		case TP_7_OUT:
			return tp_7_out;
		default:
			return null;
		}
	}
	
	public final boolean unusedTPsLeft() {
		return usedTPCount<tp_count;
	}
	
	public final Field[] getFieldsForType(final Type t) {
		List<Field> list = getFieldsForTypeAsCollection(t, new ArrayList<>());
		return list.toArray(new Field[list.size()]);
	}

	public final <T extends Collection<Field>> T getFieldsForTypeAsCollection(final Type t, T list ) {
		for(int i = 0; i<field.length; i++) {
			for(int j = 0; j<field[i].length; j++) {
				if(field[i][j].getType() == t) {
					list.add(field[i][j]);
				}
			}
		}
		return list;
	}
	

	public final Field[] getTP_OUTForField(final Type type) {
		usedTPCount++;
		tpMask = tpMask | type.bitMask;
		switch(type) {
		case TP_1_IN:
			return tp_1_out;
		case TP_2_IN:
			return tp_2_out;
		case TP_3_IN:
			return tp_3_out;
		case TP_4_IN:
			return tp_4_out;
		case TP_5_IN:
			return tp_5_out;
		case TP_6_IN:
			return tp_6_out;
		case TP_7_IN:
			return tp_7_out;
		default: return null;
		}
	}
	

	protected final void resetTPUsage() {
		usedTPCount = 0;
		tpMask = 0;
	}
	
	public final void resetFields() {
		for(final Field f : resettableFields) {
			if(f.getType()==Type.BLOCKED_BY_USER) {
				f.precedessor = Field.pred_constant;
			} else {
				f.precedessor = 0;
			}
		}
		blocked_offset = 0;
	}
	
	public final int getAndIncrementOffset() {
		blocked_offset += 5000;
		if(blocked_offset>=2000000000) {
			blocked_offset = 5000;
			resetFields();
		}
		return blocked_offset;
	}
	
	public final Field[] getResettableFields() {
		return resettableFields;
	}



	public final int getTPMask() {
		return tpMask;
	}
	
	public final void setTPMask(final int tpMask) {
		this.tpMask = tpMask;
	}

	// char because Array.equals intrinsics
	public char[] getCompressedIDsOfWalls() {
		return Arrays.copyOf(compressedIDs, compressedIDs.length);
	}
	
	public final List<Coordinate> createCoords() {
		List<Coordinate> result = new ArrayList<Coordinate>();
		for(int i = 0; i<field.length; i++) {
			for(int j = 0; j<field[i].length; j++) {
				if(field[i][j].getType()==Type.BLOCKED_BY_USER) {
					result.add(new Coordinate(j, i));
				}
			}
		}
		return result;
	}

	
	public void setWallBits(int id, boolean wall) {
		int i = id/16;
		int ii = id%16;
		if(wall) {
			compressedIDs[i] |= 1<<ii;
		} else {
			compressedIDs[i] &= ~(1 << ii);
		}
	}

}
