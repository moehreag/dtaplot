package io.github.moehreag.dtaplot.versioned;

import java.nio.ByteBuffer;
import java.util.*;

import io.github.moehreag.dtaplot.DataField;
import io.github.moehreag.dtaplot.DtaFile;
import io.github.moehreag.dtaplot.DataFieldContainer;
import io.github.moehreag.dtaplot.LookUpTable;

public class DtaFile8209 extends DtaFile {

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

	public DtaFile8209(ByteBuffer data) {
		super(data);

		data.position(8);

		System.out.println("File contains " + (data.limit() / getEntryLength()) + " data points!");

		List<Map<String, Value<?>>> entries = new ArrayList<>();
		for (int i = 0; i < data.limit() / getEntryLength(); i++) {
			entries.add(readEntry());
		}

		setDatapoints(entries);
	}

	public static DataFieldContainer lut(LookUpTable table, String category, String name, ByteBuffer buffer){
		int value = buffer.getShort();

		// Position in Tabelle
		int idx = (value - table.getOffset()) / table.getDelta();
		int size = (table.getData().length*4) / 2;
		if (idx > (size - 2)) idx = size - 2;

		// linear approximation
		int x1 = idx * table.getDelta() + table.getOffset();
		int x2 = (idx + 1) * table.getDelta() + table.getOffset();
		int y1 = table.getData()[idx];
		int y2 = table.getData()[idx + 1];

		double m = (float)(y2 - y1) / (x2 - x1);
		double n = y1 - m * x1;

		// calc value
		double res = m * value + n;
		Value<Number> val = Value.of((res) / (table.getPrecision()));
		return DataFieldContainer.single(category, name, val);
	}

	private DataFieldContainer analogue(String category, String name, ByteBuffer buffer){
		return analogue(category, name, 10, 10, buffer);
	}

	private DataFieldContainer analogue(String category, String name, double factor, int precision, ByteBuffer buffer){
		return analogue(category, name, factor, precision, buffer, false);
	}

	private DataFieldContainer analogue(String category, String name, double factor, int precision, ByteBuffer buffer, boolean highbytes){

		int val = highbytes ? buffer.getInt() : buffer.getShort();
		double res = (val / factor * precision) / precision;

		Value<Number> value = Value.of(res);
		return DataFieldContainer.single(category, name, value);
	}

	private int getEntryLength() {
		return getVersion() == 0x2011 ? 168 : 188;
	}

	private Map<String, Value<?>> readEntry(){
		List<DataFieldContainer> fields = List.of(
				DataField.time(data),																					// [0  :3  ] - Datum
				DataField.unknown(4, data),																		// [4  :7  ]
				DataField.digital("Digitale Ausgänge", data,													// [8  :9  ] - Status Ausgaenge
					DataField.bit("HUP", 0),																	//   bit 0:  HUP  = Heizungsumwaelzpumpe
					DataField.bit("ZUP", 1),																	//   bit 1:  ZUP  = Zusatzumwaelzpumpe
					DataField.bit("BUP", 2),																	//   bit 2:  BUP  = Brauswarmwasserumwaelzpumpe oder Drei-Wege-Ventil auf Brauchwassererwaermung
					DataField.bit("ZW2", 3),																	//   bit 3:  ZW2  = Zusaetzlicher Waermeerzeuger 2 / Sammelstoerung
					DataField.bit("MA1", 4),																	//   bit 4:  MA1  = Mischer 1 auf
					DataField.bit("MZ1", 5),																	//   bit 5:  MZ1  = Mischer 1 zu
					DataField.bit("ZIP", 6),																	//   bit 6:  ZIP  = Zirkulationspumpe
					DataField.bit("VD1", 7),																	//   bit 7:  VD1  = Verdichter 1
					DataField.bit("VD2", 8),																	//   bit 8:  VD2  = Verdichter 2
					DataField.bit("VENT", 9),																	//   bit 9:  VENT = Ventilation des WP Gehaeses / 2. Stufe des Ventilators
					DataField.bit("AV", 10),																	//   bit 10: AV   = Abtauventil (Kreislaufumkehr)
					DataField.bit("VBS", 11),																	//   bit 11: VBS  = Ventilator, Brunnen- oder Soleumwaelzpumpe
					DataField.bit("ZW1", 12)																	//   bit 12: ZW1  = Zusaetzlicher Waermeerzeuger 1
				),
				DataField.unknown(34, data),																		// [10 :43 ]
				DataField.digital("Digitale Eingänge", data,													// [44 :45 ] - Status Eingaenge
						DataField.bit("HD", 0, true),													//   bit 0:  HD_  = Hochdruckpressostat
						DataField.bit("ND", 1, true),													//   bit 1:  ND_  = Niederdruckpressostat
						DataField.bit("MOT", 2, true),													//   bit 2:  MOT_ = Motorschutz
						DataField.bit("ASD", 3, true),													//   bit 3:  ASD_ = Abtau/Soledruck/Durchfluss
						DataField.bit("EVU", 4)																//   bit 4:  EVU  = EVU Sperre
				),
				DataField.unknown(6, data),																		// [46 :51 ]
				lut(lut[0], "Heizkreis",   "TFB1", data),													// [52 :53 ] - TFB1
				lut(lut[0], "Heizkreis",    "TBW", data),													// [54 :55 ] - TBW
				lut(lut[1], "Heizkreis",     "TA", data),													// [56 :57 ] - TA
				lut(lut[0], "Heizkreis", "TRLext", data),													// [58 :59 ] - TRLext
				lut(lut[0], "Heizkreis",    "TRL", data),													// [60 :61 ] - TRL
				lut(lut[0], "Heizkreis",    "TVL", data),													// [62 :63 ] - TVL
				lut(lut[2], "Heizkreis",    "THG", data),													// [64 :65 ] - THG
				lut(lut[1], "Heizkreis", "TWQaus", data),													// [66 :67 ] - TWQaus
				DataField.unknown(2, data),																		// [68 :69 ]
				lut(lut[1], "Heizkreis", "TWQein", data),													// [70 :71 ] - TWQein
				DataField.unknown(8, data),																		// [72 :79 ]
				analogue("Heizkreis",  "TRLsoll", 10.0, 10, data, true),			// [80 :83 ] - TRLsoll
				analogue("Heizkreis", "TMK1soll", 10.0, 10, data, true),			// [84 :87 ] - TMK1soll
				DataField.unknown(40, data),																		// [88 :127]
				DataField.unknown(2, data),																		// [128:129] - ComfortPlatine indikator
				DataField.unknown(2, data),																		// [88 :131]
				DataField.digital("Comfort-Platine EA", data,													// [132:133] - Status Ausgaenge ComfortPlatine
						DataField.bit("AI1DIV", 6),															//    bit 6:  AI1DIV = Spannungsteiler an AI1: wann AI1DIV dann AI1 = AI1/2
						DataField.bit("SUP", 7),																//    bit 7:  SUP = Schwimmbadumwaelzpumpe
						DataField.bit("FUP2", 8),																//    bit 8:  FUP2 = Mischkreispumpe 2 / Kuehlsignal 2
						DataField.bit("MA2", 9),																//    bit 9:  MA2 = Mischer 2 auf
						DataField.bit("MZ2", 10),																//   bit 10:  MZ2 = Mischer 2 zu
						DataField.bit("MA3", 11),																//   bit 11:  MA3 = Mischer 3 auf
						DataField.bit("MZ3", 11),																//   bit 11:  MZ3 = Mischer 3 zu
						DataField.bit("FUP3", 12),															//   bit 12:  FUP3 = Mischkreispumpe 3 / Kuehlsignal 3
						DataField.bit("ZW3", 14),																//   bit 14:  ZW3 = Zusaetzlicher Waermeerzeuger 3
						DataField.bit("SLP", 15)																//   bit 15:  SLP = Solarladepumpe
				),
				DataField.unknown(2, data),																		// [134:135]
				analogue("Comfort-Platine", "AO1", 381.825, 100, data), 					// [136:137] - AO1
				analogue("Comfort-Platine", "AO2", 381.825, 100, data),  					// [138:139] - AO2
				DataField.digital("Comfort-Platine EA", data,													// [140:141] - Status Eingaenge ComfortPlatine
						DataField.bit("SWT", 4, true)													//    bit 4:  SWT_ = Schwimmbadthermostat
				),
				DataField.unknown(2, data),																		// [142:143]
				lut(lut[3], "Comfort-Platine",   "TSS", data),											// [144:145] - TSS
				lut(lut[3], "Comfort-Platine",   "TSK", data),											// [146:147] - TSK
				lut(lut[4], "Comfort-Platine",  "TFB2", data),											// [148:149] - TFB2
				lut(lut[4], "Comfort-Platine",  "TFB3", data),											// [150:151] - TFB3
				lut(lut[4], "Comfort-Platine",   "TEE", data),											// [152:153] - TEE
				DataField.unknown(4, data),																		// [154:157]
				analogue("Comfort-Platine",  "AI1", 275.406, 100, data), 					// [158:159] - AI1
				analogue("Comfort-Platine", "TMK2soll", 10.0, 10, data, true), 	// [160:163] - TMK2soll
				analogue("Comfort-Platine", "TMK3soll", 10.0, 10, data, true) 		// [164:167] - TMK3soll
		);
		if (getVersion() == 0x2010){
			skip(20);
		}

		Map<String, Value<?>> map = new HashMap<>();
		fields.stream().filter(f -> !f.isVoid())
						.forEach(c -> c.get().forEach(d -> map.put(d.getName(), d.getValue())));
		return map;
	}
}
