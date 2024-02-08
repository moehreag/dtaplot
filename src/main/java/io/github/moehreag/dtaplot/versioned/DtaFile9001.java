package io.github.moehreag.dtaplot.versioned;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.moehreag.dtaplot.DataField;
import io.github.moehreag.dtaplot.DataFieldContainer;
import io.github.moehreag.dtaplot.DtaFile;

public class DtaFile9001 extends DtaFile {

	private final int subVer;

	public DtaFile9001(ByteBuffer data) {
		super(data);

		data.position(4);
		subVer = data.getInt();
		short count = data.getShort();

		List<Map<String, Value<?>>> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Map<String, Value<?>> map = new HashMap<>();
			List<DataFieldContainer> fields = readEntry();
			fields.stream().filter(f -> !f.isVoid())
					.forEach(c -> c.get()//.stream().filter(DataField::isNumeric)
							.forEach(d -> map.put(d.getName(), d.getValue())));
			list.add(map);
		}
		setDatapoints(list);
	}

	private List<DataFieldContainer> readEntry() {
		List<DataFieldContainer> list = new ArrayList<>(List.of(
				DataField.time(data),
				analogue("Heizkreis", "TVL", 10.0, 10, data),  // [4 :5 ] TVL
				analogue("Heizkreis", "TRL", 10.0, 10, data),  // [6 :7 ] TRL
				analogue("Heizkreis", "TWQein", 10.0, 10, data),  // [8 :9 ] TWQein
				analogue("Heizkreis", "TWQaus", 10.0, 10, data),  // [10:11] TWQaus
				analogue("Heizkreis", "THG", 10.0, 10, data),  // [12:13] THG
				analogue("Heizkreis", "TBW", 10.0, 10, data),  // [14:15] TBW
				analogue("Heizkreis", "TFB1", 10.0, 10, data),  // [16:17] TFB1
				analogue("Heizkreis", "TA", 10.0, 10, data),  // [18:19] TA
				analogue("Heizkreis", "TRLext", 10.0, 10, data),  // [20:21] TRLext
				analogue("Heizkreis", "TRLsoll", 10.0, 10, data),  // [22:23] TRLsoll
				analogue("Heizkreis", "TMK1soll", 10.0, 10, data),  // [24:25] TMK1soll
				DataField.digital("Digitale Eingänge", data, // [26:27] StatusE = Status der Eingaenge (die Bits sind invertiert zur Funktion)
						DataField.bit("HD", 0),//   bit 0:  HD_  = Hochdruckpressostat
						DataField.bit("ND", 1),//   bit 1:  ND_  = Niederdruckpressostat
						DataField.bit("MOT", 2),//   bit 2:  MOT_ = Motorschutz			/* Die Angaben zur Invertierung sind wiedersprüchlich zur Dokumentation! */
						DataField.bit("ASD", 3),//   bit 3:  ASD_ = Abtau/Soledruck/Durchfluss
						DataField.bit("EVU", 4, true)//   bit 4:  EVU  = EVU Sperre
						),
				DataField.digital("Digitale Ausgänge", data,// [28:29]   StatusA = Status der Ausgaenge
						DataField.bit("HUP", 0),//   bit 0:  HUP  = Heizungsumwaelzpumpe
						DataField.bit("ZUP", 1),//   bit 1:  ZUP  = Zusatzumwaelzpumpe
						DataField.bit("BUP", 2),//   bit 2:  BUP  = Brauswarmwasserumwaelzpumpe oder Drei-Wege-Ventil auf Brauchwassererwaermung
						DataField.bit("ZW2", 3),//   bit 3:  ZW2  = Zusaetzlicher Waermeerzeuger 2 / Sammelstoerung
						DataField.bit("MA1", 4),//   bit 4:  MA1  = Mischer 1 auf
						DataField.bit("MZ1", 5),//   bit 5:  MZ1  = Mischer 1 zu
						DataField.bit("ZIP", 6),//   bit 6:  ZIP  = Zirkulationspumpe
						DataField.bit("VD1", 7),//   bit 7:  VD1  = Verdichter 1
						DataField.bit("VD2", 8),//   bit 8:  VD2  = Verdichter 2
						DataField.bit("VENT", 9),//   bit 9:  VENT = Ventilation des WP Gehaeses / 2. Stufe des Ventilators
						DataField.bit("AV", 10),//   bit 10: AV   = Abtauventil (Kreislaufumkehr)
						DataField.bit("VBS", 11),//   bit 11: VBS  = Ventilator, Brunnen- oder Soleumwaelzpumpe
						DataField.bit("ZW1", 12)//   bit 12: ZW1  = Zusaetzlicher Waermeerzeuger 1
				),
				DataField.unknown(2, data),                                // [30:31]
				analogue("Heizkreis", "TSS", 10.0, 10, data),  // [32:33] TSS
				analogue("Heizkreis", "TSK", 10.0, 10, data),  // [34:35] TSK
				analogue("Heizkreis", "TFB2", 10.0, 10, data),  // [36:37] TFB2
				analogue("Heizkreis", "TFB3", 10.0, 10, data),  // [38:39] TFB3
				analogue("Heizkreis", "TEE", 10.0, 10, data),  // [40:41] TEE
				DataField.unknown(4, data),                                // [42:45]
				analogue("Heizkreis", "TMK2soll", 10.0, 10, data),  // [46:47] TMK2soll
				analogue("Heizkreis", "TMK3soll", 10.0, 10, data),  // [48:49] TMK3soll
				analogue("Heizkreis", "AI1", 1000.0, 1000, data),  // [50:51] AI
				analogue("Heizkreis", "AO1", 1000.0, 1000, data)  // [52:53] AO1
		));

		if (subVer > 0 && subVer <= 3) {
			list.addAll(List.of(
					analogue("Heizkreis", "AO2", 1000.0, 1000, data),  // [54:55] AO2
					DataField.unknown(2, data),                                  // [56:57]
					analogue("Heizkreis", "Asg.VDi", 10.0, 10, data),  // [58:59] Ansaug Verdichter
					analogue("Heizkreis", "Asg.VDa", 10.0, 10, data),  // [60:61] Ansaug Verdampfer
					analogue("Heizkreis", "VDHz", 10.0, 10, data),  // [62:63] VD Heizung
					DataField.unknown(8, data)                           // [64:71]
			));
		}

		if (subVer == 1 || subVer == 3) {
			list.addAll(List.of(
					DataField.unknown(2, data),                                // [72:73]
					analogue("Heizkreis", "UeHz", 10.0, 10, data),  // [74:75] Ueberhitzung
					analogue("Heizkreis", "UeHzsoll", 10.0, 10, data),  // [76:77] Ueberhiztung Sollwert
					DataField.unknown(2, data)                               // [78:79]
			));
		}

		if (subVer == 3) {
			list.add(DataField.unknown(18, data));
		}

		return list;
	}

	private DataFieldContainer analogue(String category, String name, double factor, int precision, ByteBuffer buffer) {
		return analogue(category, name, factor, precision, buffer, false);
	}

	private DataFieldContainer analogue(String category, String name, double factor, int precision, ByteBuffer buffer, boolean highbytes) {

		int val = highbytes ? buffer.getInt() : buffer.getShort();
		double res = (val / factor * precision) / precision;

		Value<Number> value = Value.of(res/10);
		return DataFieldContainer.single(category, name, value);
	}


}
