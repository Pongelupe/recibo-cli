package com.prosegur.sol.recibocli.menu.options.mergeiro.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileSeparator {

	private static final byte[] SEPATARORS = new byte[]{29, 29, 29, 29};

	public List<byte[]> decodeBinaries(byte[] merged) {
		return split(SEPATARORS, merged);
	}

	public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] != input[pos + i]) {
				return false;
			}
		}
		return true;
	}

	public List<byte[]> split(byte[] pattern, byte[] input) {
		List<byte[]> bins = new LinkedList<>();
		int blockStart = 0;
		for (int i = 0; i < input.length; i++) {
			if (isMatch(pattern, input, i)) {
				bins.add(Arrays.copyOfRange(input, blockStart, i));
				blockStart = i + pattern.length;
				i = blockStart;
			}
		}
		bins.add(Arrays.copyOfRange(input, blockStart, input.length));
		return bins;
	}

	public byte[] merge(byte[] parcial, byte[] toAppend) {
		byte[] merged = new byte[parcial.length + toAppend.length
				+ SEPATARORS.length];

		System.arraycopy(parcial, 0, merged, 0, parcial.length);

		System.arraycopy(SEPATARORS, 0, merged, parcial.length,
				SEPATARORS.length);

		System.arraycopy(toAppend, 0, merged,
				parcial.length + SEPATARORS.length, toAppend.length);

		return merged;
	}

	public byte[] fileToByteArray(File file) {
		try {
			String name = file.getName().replace(".jasper", "");
			int isMaster = name.contains("Master") ? 1 : 0;

			byte[] bin = ByteArrayUtils.merge(
					FileUtils.readFileToByteArray(file),
					new byte[]{(byte) isMaster}, new byte[]{29, 29, 29, 29});

			return ByteArrayUtils.merge(bin, name.getBytes(),
					new byte[]{29, 29, 29, 29});

		} catch (IOException e) {
			return null;
		}
	}

}
