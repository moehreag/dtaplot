package io.github.moehreag.dtaplot.dta.versioned;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import io.github.moehreag.dtaplot.dta.DataFieldContainer;
import io.github.moehreag.dtaplot.dta.DtaFile;
import io.github.moehreag.dtaplot.Value;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.github.moehreag.dtaplot.dta.DataField.*;

public class DtaFile9000 extends DtaFile {

	private final int subVer;

	public DtaFile9000(ByteBuffer data) {
		super(data);

		subVer = data.getInt(4);

		data.position(8);

		List<Map<String, Value<?>>> list = new ArrayList<>();
		while (data.remaining()>0){
			list.add(readEntry());
		}
		setDatapoints(list);
	}

	private Map<String, Value<?>> readEntry() {
		List<DataFieldContainer> fields = new ArrayList<>(List.of(
				time(data),
				analogue("Heizkreis", "TVL"),																// [ 0] TVL
				analogue("Heizkreis", "TRL"),																// [ 1] TRL
				analogue("Heizkreis", "TWQein"),															// [ 2] TWQein
				analogue("Heizkreis", "TWQaus"),															// [ 3] TWQaus
				analogue("Heizkreis", "THG"),																// [ 4] THG
				analogue("Heizkreis", "TBW"),																// [ 5] TBW
				analogue("Heizkreis", "TFB1"),															// [ 6] TFB1?
				analogue("Heizkreis", "TA"),																// [ 7] TA
				analogue("Heizkreis", "TRLext"),															// [ 8] TRLext
				analogue("Heizkreis", "TRLsoll"),															// [ 9] TRLsoll
				unknown(1, data),																					// [10] ?
				unknown(1, data),																					// [11] ?
				digital("Digitale Ausgänge", data,																// [12]   StatusA = Status der Ausgaenge
						bit("HUP", 0),																		//   bit 0:  HUP  = Heizungsumwaelzpumpe
						bit("ZUP", 1),																		//   bit 1:  ZUP  = Zusatzumwaelzpumpe
						bit("BUP", 2),																		//   bit 2:  BUP  = Brauswarmwasserumwaelzpumpe oder Drei-Wege-Ventil auf Brauchwassererwaermung
						bit("ZW2", 3),																		//   bit 3:  ZW2  = Zusaetzlicher Waermeerzeuger 2 / Sammelstoerung
						bit("MA1", 4),																		//   bit 4:  MA1  = Mischer 1 auf
						bit("MZ1", 5),																		//   bit 5:  MZ1  = Mischer 1 zu
						bit("ZIP", 6),																		//   bit 6:  ZIP  = Zirkulationspumpe
						bit("VD1", 7),																		//   bit 7:  VD1  = Verdichter 1
						bit("VD2", 8),																		//   bit 8:  VD2  = Verdichter 2
						bit("VENT", 9),																		//   bit 9:  VENT = Ventilation des WP Gehaeses / 2. Stufe des Ventilators
						bit("AV", 10),																		//   bit 10: AV   = Abtauventil (Kreislaufumkehr)
						bit("VBS", 11),																		//   bit 11: VBS  = Ventilator, Brunnen- oder Soleumwaelzpumpe
						bit("ZW1", 12)																		//   bit 12: ZW1  = Zusaetzlicher Waermeerzeuger 1
				),
				digital("Digitale Eingänge", data,																// [13] StatusE = Status der Eingaenge (die Bits sind invertiert zur Funktion)
						bit("HD", 0, true),															//   bit 0:  HD_  = Hochdruckpressostat
						bit("ND", 1, true),															//   bit 1:  ND_  = Niederdruckpressostat
						bit("MOT", 2, true),															//   bit 2:  MOT_ = Motorschutz
						bit("ASD", 3, true),															//   bit 3:  ASD_ = Abtau/Soledruck/Durchfluss
						bit("EVU", 4, true)															//   bit 4:  EVU  = EVU Sperre
				),

				unknown(1, data),																					// [14] ?
				unknown(1, data),																					// [15] ?
				unknown(1, data),																					// [16] ?
				unknown(1, data),																					// [17] ?
				unknown(1, data),																					// [18] ?
				unknown(1, data),																					// [19] ?
				unknown(1, data),																					// [20] ?
				unknown(1, data),																					// [21] ?
				unknown(1, data),																					// [22] ?
				analogue("Heizkreis", "Durchfluss"),														// [23] TRLsoll
				unknown(1, data)																					// [24] ?
		));
		if (subVer < 676) {
			fields.addAll(List.of(
					unknown(1, data),																				// [25] ?
					unknown(1, data),																				// [26] ?
					analogue("Heizkreis", "Asg.VDi"),														// [27] Ansaug Verdichter
					analogue("Heizkreis", "Asg.VDa"),														// [28] Ansaug Verdampfer
					analogue("Heizkreis", "VDHz"),														// [29] VD Heizung
					unknown(1, data),																				// [30] ?
					unknown(1, data),																				// [31] ?
					unknown(1, data),																				// [32] ?
					unknown(1, data),																				// [33] ?
					unknown(1, data),																				// [34] ?
					analogue("Heizkreis", "UeHz"),														// [35] Ueberhitzung
					analogue("Heizkreis", "UeHzsoll"),													// [36] Ueberhiztung Sollwert
					unknown(1, data)																				// [37] ?
			));
		}

		Map<String, Value<?>> map = new HashMap<>();
		fields.stream().filter(f -> !f.isVoid())
				.forEach(c -> c.get().forEach(d -> map.put(d.getName(), d.getValue())));
		return map;
	}

	private DataFieldContainer analogue(String category, String name) {
		FieldType type = FieldType.of(data.get());
		float value = type.read(data)/10f;
		return DataFieldContainer.single(category, name, value);
	}


	@RequiredArgsConstructor
	@Getter
	private enum FieldType {
		POS_1(0, false, 1),
		POS_2(1, false, 2) {
			@Override
			public int read(ByteBuffer buf) {
				return buf.getShort();
			}
		},
		NEG_1(4, true, 1) {
			@Override
			public int read(ByteBuffer buf) {
				return -super.read(buf);
			}
		},
		NEG_2(5, true, 2) {
			@Override
			public int read(ByteBuffer buf) {
				return -buf.getShort();
			}
		};
		private final int value;
		private final boolean negative;
		private final int length;

		static final Map<Integer, FieldType> types = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(f -> f.value, f -> f));

		static FieldType of(byte type) {
			return types.get((int) type);
		}

		public int read(ByteBuffer buf) {
			return buf.get();
		}
	}
}
