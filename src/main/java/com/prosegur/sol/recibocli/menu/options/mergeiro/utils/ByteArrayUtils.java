package com.prosegur.sol.recibocli.menu.options.mergeiro.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ByteArrayUtils {

	private ByteArrayUtils() {
	}

	public static List<byte[]> splitBinaries(byte[] pattern, byte[] target) {
		List<byte[]> bins = new LinkedList<>();
		int blockStart = 0;
		for (int i = 0; i < target.length; i++) {
			if (isMatch(pattern, target, i)) {
				bins.add(Arrays.copyOfRange(target, blockStart, i));
				blockStart = i + pattern.length;
				i = blockStart;
			}
		}
		bins.add(Arrays.copyOfRange(target, blockStart, target.length));
		return bins;
	}

	private static boolean isMatch(byte[] pattern, byte[] input, int pos) {
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] != input[pos + i]) {
				return false;
			}
		}
		return true;
	}

	public static byte[] merge(byte[] parcial, byte[] toAppend) {
		return merge(parcial, toAppend, new byte[0]);
	}

	public static byte[] merge(byte[] parcial, byte[] toAppend,
			byte[] separators) {
		byte[] merged = new byte[parcial.length + toAppend.length
				+ separators.length];

		System.arraycopy(parcial, 0, merged, 0, parcial.length);

		if (separators.length != 0)
			System.arraycopy(separators, 0, merged, parcial.length,
					separators.length);

		System.arraycopy(toAppend, 0, merged,
				parcial.length + separators.length, toAppend.length);

		return merged;
	}

}
