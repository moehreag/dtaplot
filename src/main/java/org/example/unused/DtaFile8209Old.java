package org.example.unused;

import java.nio.ByteBuffer;
import java.util.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.DtaFile;
import org.example.LookUpTable;

public class DtaFile8209Old extends DtaFile {

	private static final LookUpTable[] lut = new LookUpTable[]{

			// LUT for TRL, TVL, TBW, TFB1, TRLext
			new LookUpTable(
					new int[]{1550, 1550, 1550, 1438, 1305, 1205, 1128,
							1063, 1007, 959, 916, 878, 843, 811, 783, 756, 732, 708,
							685, 664, 647, 625, 607, 590, 574, 558, 543, 529, 515, 501,
							487, 474, 461, 448, 436, 424, 412, 401, 390, 379, 368, 358,
							348, 338, 328, 318, 308, 299, 289, 279, 269, 260, 250, 241,
							232, 223, 214, 205, 196, 187, 178, 170, 161, 152, 144,
							135, 127, 118, 109, 100, 92, 83, 74, 65, 56, 47, 38, 29,
							20, 11, 1, -7, -17, -26, -37, -48, -58, -68, -78, -90,
							-102, -112, -124, -137, -150, -162, -175, -190, -205,
							-220, -237, -255, -273, -279, -279},
					0,  // offset
					10, // delta
					10  // precision
			),

			// LUT for TWQein, TWQaus, TA
			new LookUpTable(
					new int[]{1550, 1435, 1133, 971, 862, 781, 718,
							664, 618, 579, 545, 514, 486, 460, 435, 413, 392, 372, 354,
							337, 321, 306, 290, 276, 261, 248, 235, 223, 210, 199, 188,
							177, 166, 156, 146, 136, 127, 117, 108, 99, 90, 82, 73, 65,
							57, 48, 40, 32, 25, 17, 9, 1, -5, -12, -20, -27, -35, -42,
							-50, -57, -63, -70, -77, -85, -92, -100, -107, -113, -120,
							-127, -134, -142, -150, -156, -163, -169, -177, -184, -192,
							-200, -207, -214, -221, -229, -237, -246, -254, -261, -269,
							-277, -286, -296, -304, -313, -322, -331, -342, -352, -362,
							-372, -384, -396, -408, -411, -411},
					0,  // offset
					10, // delta
					10  // precision
			),

			// LUT for THG
			new LookUpTable(
					new int[]{1550, 1550, 1550, 1550, 1550, 1550, 1550,
							1537, 1468, 1409, 1357, 1311, 1268, 1229, 1193, 1160, 1130,
							1100, 1074, 1048, 1024, 1000, 978, 956, 936, 916, 896, 879,
							861, 843, 827, 811, 795, 780, 765, 750, 737, 723, 709, 695,
							681, 668, 655, 646, 630, 617, 605, 593, 581, 570, 558, 547,
							535, 524, 513, 502, 490, 479, 467, 456, 444, 433, 421, 410,
							398, 387, 376, 364, 353, 341, 330, 318, 306, 294, 282, 269,
							256, 243, 230, 217, 203, 189, 175, 161, 146, 131, 116, 99,
							83, 65, 47, 27, 7, -14, -37, -62, -90, -120, -155, -194,
							-240, -300, -378, -411, -411},
					0,  // offset
					10, // delta
					10  // precision
			),

			// LUT for TSS, TSK
			new LookUpTable(
					new int[]{1550, 1550, 1550, 1550, 1550, 1550, 1550, 1537, 1468, 1409, 1357,
							1311, 1268, 1229, 1193, 1160, 1130, 1100, 1074, 1048, 1024, 1000,
							978, 956, 936, 916, 896, 879, 861, 843, 827, 811, 795, 780, 765,
							750, 737, 723, 709, 695, 681, 668, 655, 646, 630, 617, 605, 593,
							581, 570, 558, 547, 535, 524, 513, 502, 490, 479, 467, 456, 444,
							433, 421, 410, 398, 387, 376, 365, 354, 343, 331, 319, 308, 296,
							283, 270, 257, 244, 231, 218, 204, 191, 177, 162, 148, 133, 117,
							101, 84, 67, 48, 29, 9, -12, -35, -60, -87, -117, -152, -189,
							-235, -292, -369, -411, -411},
					0,  // offset
					40, // delta
					10 // precision
			),

			// LUT for TFB2, TFB3, TEE
			new LookUpTable(
					new int[]{1550, 1550, 1550, 1438, 1305, 1205, 1128, 1063, 1007, 959, 916, 878,
							843, 811, 783, 756, 732, 708, 685, 664, 647, 625, 607, 590, 574,
							558, 543, 529, 515, 501, 487, 474, 461, 448, 436, 424, 412, 401,
							390, 379, 368, 358, 348, 338, 328, 318, 308, 299, 289, 279, 269,
							260, 250, 241, 232, 223, 214, 205, 196, 187, 178, 170, 161, 152,
							144, 135, 127, 119, 110, 101, 93, 84, 75, 66, 57, 48, 39, 30, 21,
							12, 2, -7, -16, -25, -36, -47, -57, -66, -77, -89, -101, -111,
							-123, -135, -149, -161, -174, -189, -204, -219, -235, -254, -271,
							-279, -279},
					0,  // offset
					40, // delta
					10 // precision
			)
	};
	private static final Map<String, LookUpTable> tables = new HashMap<>();

	static {
		tables.put("TRL", lut[0]);
		tables.put("TVL", lut[0]);
		tables.put("TBW", lut[0]);
		tables.put("TFB1", lut[0]);
		tables.put("TRLext", lut[0]);
		tables.put("TWQein", lut[1]);
		tables.put("TWQaus", lut[1]);
		tables.put("TA", lut[1]);
		tables.put("THG", lut[2]);
		tables.put("TSS", lut[3]);
		tables.put("TSK", lut[3]);
		tables.put("TFB2", lut[4]);
		tables.put("TFB3", lut[4]);
		tables.put("TEE", lut[4]);
	}

	public DtaFile8209Old(ByteBuffer data) {
		super(data);
		data.position(8);

		System.out.println("File contains " + (data.limit() / getEntryLength()) + " data points!");

		List<Entry> entries = new ArrayList<>();
		for (int i = 0; i < data.limit() / getEntryLength(); i++) {
			entries.add(readEntry());
		}
		entries.sort(Comparator.comparingInt(Entry::getTime));
		setDatapoints(entries.stream().map(Entry::calculateValues).toList());
	}

	private Entry readEntry() {
		int time = data.getInt();
		skip(4);
		short statusA = data.getShort();
		skip(34);
		short statusE = data.getShort();
		skip(6);
		short tFB1 = data.getShort();
		short tBW = data.getShort();
		short tA = data.getShort();
		short tRLext = data.getShort();
		short tRL = data.getShort();
		short tVL = data.getShort();
		short tHG = data.getShort();
		short tWQaus = data.getShort();
		skip(2);
		short tWQein = data.getShort();
		skip(8);
		short tRLsoll = data.getShort();
		short tRLsoll_highbytes = data.getShort();
		short tMK1soll = data.getShort();
		short tMK1soll_highbytes = data.getShort();
		skip(40);
		short comfortPlatine = data.getShort();
		skip(2);
		short statusA_CP = data.getShort();
		skip(2);
		short aO1 = data.getShort();
		short aO2 = data.getShort();
		short statusE_CP = data.getShort();
		skip(2);
		short tSS = data.getShort();
		short tSK = data.getShort();
		short tFB2 = data.getShort();
		short tFB3 = data.getShort();
		short tEE = data.getShort();
		skip(4);
		short aI1 = data.getShort();
		short tMK2soll = data.getShort();
		short tMK2soll_highbytes = data.getShort();
		short tMK3soll = data.getShort();
		short tMK3soll_highbytes = data.getShort();
		if (getVersion() != 0x2011) {
			skip(20);
		}
		return new Entry(time, readBits(statusA), readBits(statusE, 0, 1, 2, 3),
				tFB1, tBW, tA, tRLext, tRL, tVL, tHG, tWQaus, tWQein, combine(tRLsoll, tRLsoll_highbytes),
				combine(tMK1soll, tMK1soll_highbytes), comfortPlatine, readBits(statusA_CP), aO1, aO2, readBits(statusE_CP, 4),
				tSS, tSK, tFB2, tFB3, tEE, aI1, combine(tMK2soll, tMK2soll_highbytes),
				combine(tMK3soll, tMK3soll_highbytes));
	}



	private int getEntryLength() {
		return getVersion() == 0x2011 ? 168 : 188;
	}

	@EqualsAndHashCode(callSuper = true)
	@Getter
	@RequiredArgsConstructor
	public static class Entry extends DtaFile.Entry {
		private final int time;
		private final boolean[] statusOut, statusIn;
		private final short tempFB1, tBW, tA, tRLext, tRL, tVL, tHG, tWQaus, tWQein;
		private final int tRLsoll, tMK1soll;
		private final short comfortPlatine;
		private final boolean[] statusA_CP;
		private final short aO1, aO2;
		private final boolean[] statusE_CP;
		private final short tSS, tSK, tFB2, tFB3, tEE, aI1;
		private final int tMK2soll, tMK3soll;

		public Map<String, Value<?>> calculateValues() {

			Map<String, Value<?>> values = new HashMap<>();

			values.put("time", of(time));

			Object[] a = new Object[]{statusOut, statusIn, tempFB1, tBW, tA, tRLext,
					tRL, tVL, tHG, tWQaus, tWQein, tRLsoll, tMK1soll, comfortPlatine,
					statusA_CP, aO1, aO2, statusE_CP, tSS, tSK, tFB2, tFB3, tEE, aI1,
					tMK2soll, tMK3soll
			};
			for (int i = 0; i < Math.min(a.length, Entries.values().length); i++) {
				String id = Entries.values()[i].getShortName();
				values.put(id, getLookUpValue(id, of(a[i])));
			}

			return values;
		}

		private Value<?> getLookUpValue(String id, Value<?> v) {
			if (v.get() instanceof Integer || v.get() instanceof Short) {
				int value;
				if (v.get() instanceof Short){
					value = (int)(short)v.get();
				} else {
					value = (int) v.get();
				}

				LookUpTable m_lutInfo = tables.get(id);
				if (m_lutInfo == null){
					return v;
				}
				// Position in Tabelle
				int idx = (value - m_lutInfo.getOffset()) / m_lutInfo.getDelta();
				int size = (m_lutInfo.getData().length*4) / 2;
				if (idx > (size - 2)) idx = size - 2;

				// linear approximation
				int x1 = idx * m_lutInfo.getDelta() + m_lutInfo.getOffset();
				int x2 = (idx + 1) * m_lutInfo.getDelta() + m_lutInfo.getOffset();
				int y1 = m_lutInfo.getData()[idx];
				int y2 = m_lutInfo.getData()[idx + 1];

				double m = (float)(y2 - y1) / (x2 - x1);
				double n = y1 - m * x1;

				// calc value
				double res = m * value + n;
				return of(Math.round(res) / (m_lutInfo.getPrecision()));
			}
			return v;
		}
	}
}
