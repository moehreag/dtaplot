package io.github.moehreag.dtaplot.socket.tcp;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import io.github.moehreag.dtaplot.Translations;
import io.github.moehreag.dtaplot.Value;
import lombok.Getter;

@SuppressWarnings("SameParameterValue")
@Getter
public abstract class DataVector {

	private final Collection<Map<String, Value<?>>> values = new ArrayList<>();
	protected final List<Datatype> data;

	protected DataVector(List<Datatype> data) {
		this.data = data;
	}

	public void read(int[] data) {
		Map<String, Value<?>> map = new HashMap<>();
		int size = this.data.size();
		for (int i = 0; i < data.length; i++) {
			Datatype type = i >= size ? unknown("Unknown_"+this.getClass().getSimpleName()+"_"+i) : this.data.get(i);
			if (!type.getUnit().isEmpty()){
				map.put(type.getName(), type.read(data[i]));
			} else {
				map.put(type.getName(), type.read(data[i]));
			}
		}
		values.add(map);
	}

	public Datatype get(String name){
		for (Datatype type : data){
			if (name.equals(type.getName())){
				return type;
			}
		}
		return null;
	}

	public Datatype get(int index){
		if (index < 0 || index >= data.size()){
			return null;
		}
		return data.get(index);
	}

	protected static Datatype unknown(String name) {
		return Datatype.base(name, false);
	}

	protected static Datatype celsius(String name, boolean writeable) {
		return Datatype.scaling(name, writeable, 0.1f).unit("°C");
	}

	protected static Datatype celsius(String name) {
		return celsius(name, false);
	}


	protected static Datatype heatingMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Automatic", "Second heatsource", "Party", "Holidays", "Off");
	}

	protected static Datatype hotWaterMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Automatic", "Second heatsource", "Party", "Holidays", "Off");
	}

	protected static Datatype kelvin(String name, boolean writeable) {
		return Datatype.scaling(name, writeable, 0.1f).unit("K");
	}

	protected static Datatype kelvin(String name) {
		return kelvin(name, false);
	}

	protected static Datatype accessLevel(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "user", "after sales service", "manufacturer", "installer");
	}

	protected static Datatype coolingMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Off", "Automatic");
	}

	protected static Datatype poolMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Automatic", "Party", "Holidays", "Off");
	}

	protected static Datatype timeOfDay(String name, boolean writeable) {
		return Datatype.custom(name, writeable, value -> {
			int hours = Math.floorDiv(value, 3600);
			int minutes = Math.floorDiv(value, 60) % 60;
			int seconds = value % 60;

			return String.format("%s:%s", hours, timeFormat.format(minutes)) +
					(seconds > 0 ? String.format(":%s", timeFormat.format(seconds)) : "");
		}, value -> {
			if (value.get() instanceof String s) {
				Integer[] d = Arrays.stream(s.split(":")).map(Integer::parseInt).toArray(Integer[]::new);

				int val = d[0] * 3600 + d[1] * 60;
				if (d.length == 3) {
					val += d[2];
				}

				return val;
			}
			return -1;
		});
	}

	protected static Datatype timeOfDay(String name) {
		return timeOfDay(name, false);
	}

	protected static Datatype timerProgram(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "week", "5+2", "days");
	}

	protected static Datatype timerProgram(String name) {
		return timerProgram(name, false);
	}

	protected static Datatype seconds(String name) {
		return Datatype.base(name, false).unit("s");
	}

	protected static Datatype count(String name) {
		return Datatype.base(name, false);
	}

	protected static Datatype mixedCircuitMode(String name){
		return mixedCircuitMode(name, false);
	}

	protected static Datatype mixedCircuitMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Automatic", "Party", "Holidays", "Off");
	}

	protected static Datatype hours(String name, boolean writeable) {
		return Datatype.scaling(name, writeable, 0.1f).unit("h");
	}

	protected static Datatype hours2(String name, boolean writeable) {
		return Datatype.custom(name, writeable, value -> 1 + value / 2, value -> {
			if (value.get() instanceof Number num) {
				return Math.round((num.floatValue() - 1) * 2);
			}
			return -1;
		});
	}

	protected static Datatype minutes(String name, boolean writeable) {
		return Datatype.base(name, writeable).unit("min");
	}

	protected static Datatype energy(String name) {
		return Datatype.scaling(name, false, 0.1f).unit("kWh");
	}

	protected static Datatype timestamp(String name) {
		return Datatype.custom(name, val -> {
			ZonedDateTime zTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(val),
					ZoneId.systemDefault());
			return Translations.translate("date.format",
					timeFormat.format(zTime.getHour()),
					timeFormat.format(zTime.getMinute()),
					timeFormat.format(zTime.getDayOfMonth()),
					timeFormat.format(zTime.getMonthValue()),
					timeFormat.format(zTime.getYear())
			);
		});
	}

	protected static Datatype solarMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Automatic", "Second heatsource", "Party", "Holidays", "Off");
	}

	protected static Datatype ventilationMode(String name, boolean writeable) {
		return Datatype.selection(name, writeable, "Automatic", "Party", "Holidays", "Off");
	}

	private static final NumberFormat timeFormat = new DecimalFormat("00");

	protected static Datatype timeOfDay2(String name) {
		return Datatype.custom(name, false,
				value -> {
					int value_low = value & 0xFFFF;
					int value_high = value >> 16;
					int hours1 = Math.floorDiv(value_low, 60);
					int minutes1 = value_low % 60;
					int hours2 = Math.floorDiv(value_high, 60);
					int minutes2 = value_high % 60;

					return String.format(
							"%s:%s-%s:%s",
							hours1, timeFormat.format(minutes1), hours2, timeFormat.format(minutes2)
					);
				}, value -> {
					if (value.get() instanceof String s) {
						String[] d = s.split("-");
						Integer[] low = Arrays.stream(d[0].split(":")).map(Integer::parseInt).toArray(Integer[]::new);
						Integer[] high = Arrays.stream(d[1].split(":")).map(Integer::parseInt).toArray(Integer[]::new);

						return ((high[0] * 60 + high[1]) << 16) + low[0] * 60 + low[1];
					}
					return -1;
				}
		);
	}

	protected static Datatype bool(String name) {
		return Datatype.bool(name, false);
	}

	protected static Datatype bool(String name, boolean writeable) {
		return Datatype.bool(name, writeable);
	}

	protected static Datatype heatpumpCode(String name) {
		return heatpumpCode(name, false);
	}

	protected static Datatype heatpumpCode(String name, boolean writeable) {
		return Datatype.selection(name, writeable,
				"ERC",
				"SW1",
				"SW2",
				"WW1",
				"WW2",
				"L1I",
				"L2I",
				"L1A",
				"L2A",
				"KSW",
				"KLW",
				"SWC",
				"LWC",
				"L2G",
				"WZS",
				"L1I407",
				"L2I407",
				"L1A407",
				"L2A407",
				"L2G407",
				"LWC407",
				"L1AREV",
				"L2AREV",
				"WWC1",
				"WWC2",
				"L2G404",
				"WZW",
				"L1S",
				"L1H",
				"L2H",
				"WZWD",
				"ERC",
				"ERC",
				"ERC",
				"ERC",
				"ERC",
				"ERC",
				"ERC",
				"ERC",
				"ERC",
				"WWB_20",
				"LD5",
				"LD7",
				"SW 37_45",
				"SW 58_69",
				"SW 29_56",
				"LD5 (230V)",
				"LD7 (230 V)",
				"LD9",
				"LD5 REV",
				"LD7 REV",
				"LD5 REV 230V",
				"LD7 REV 230V",
				"LD9 REV 230V",
				"SW 291",
				"LW SEC",
				"HMD 2",
				"MSW 4",
				"MSW 6",
				"MSW 8",
				"MSW 10",
				"MSW 12",
				"MSW 14",
				"MSW 17",
				"MSW 19",
				"MSW 23",
				"MSW 26",
				"MSW 30",
				"MSW 4S",
				"MSW 6S",
				"MSW 8S",
				"MSW 10S",
				"MSW 12S",
				"MSW 16S",
				"MSW2-6S",
				"MSW4-16",
				"LD2AG",
				"LD9V",
				"MSW3-12",
				"MSW3-12S",
				"MSW2-9S",
				"LW 8",
				"LW 12",
				"HZ_HMD",
				"LW V4",
				"LW SEC 2",
				"MSW1-4S",
				"LP5V",
				"LP8V"
		);
	}

	protected static Datatype bivalenceLevel(String name) {
		return bivalenceLevel(name, false);
	}

	protected static Datatype bivalenceLevel(String name, boolean writeable) {
		return Datatype.selection(name, writeable,
				"one compressor allowed to run",
				"two compressors allowed to run",
				"additional heat generator allowed to run"
		);
	}

	protected static Datatype operationMode(String name) {
		return Datatype.selection(name, false,
				"heating",
				"hot water",
				"swimming pool/solar",
				"evu",
				"defrost",
				"no request",
				"heating external source",
				"cooling"
		);
	}

	protected static Datatype character(String name) {
		return Datatype.custom(name, integer -> (char)(int)integer);
	}

	protected static Datatype ipv4Address(String name) {
		return Datatype.custom(name, false, value -> {
			ByteBuffer buf = ByteBuffer.wrap(new byte[4]);
			buf.putInt(value);
			try {
				return Inet4Address.getByAddress(buf.array()).getHostAddress();
			} catch (UnknownHostException e) {
				return "";
			}
		}, value -> {
			if (value.get() instanceof String s && !s.isBlank()) {
				try {
					return ByteBuffer.wrap(Inet4Address.getByName(s).getAddress()).getInt();
				} catch (UnknownHostException ignored) {
				}
			}
			return -1;
		});
	}

	protected static Datatype errorcode(String name) {
		return Datatype.custom(name, v ->
				switch (v){
			case 718 -> "Max. Aussentemp. (718)";
			default -> v;
		});
	}

	protected static Datatype switchoffFile(String name) {
		return Datatype.selection(name, false,
				"heatpump error",
				"system error",
				"evu lock",
				"operation mode second heat generator",
				"air defrost",
				"maximal usage temperature",
				"minimal usage temperature",
				"lower usage limit",
				"no request",
				"flow rate",
				"PV max"
		);
	}

	protected static Datatype mainMenuStatusLine1(String name) {
		return Datatype.selection(name, false, "heatpump running",
				"heatpump idle",
				"heatpump coming",
				"errorcode slot 0",
				"defrost",
				"waiting on LIN connection",
				"compressor heating up",
				"pump forerun"
		);
	}

	protected static Datatype mainMenuStatusLine2(String name) {
		return Datatype.selection(name, false,
				"since", "in");
	}

	protected static Datatype mainMenuStatusLine3(String name) {
		return Datatype.selection(name, false,
				"heating",
				"no request",
				"grid switch on delay",
				"cycle lock",
				"lock time",
				"domestic water",
				"info bake out program",
				"defrost",
				"pump forerun",
				"thermal desinfection",
				"cooling",
				"swimming pool/solar",
				"heating external energy source",
				"domestic water external energy source",
				"flow monitoring",
				"second heat generator 1 active"
		);
	}

	protected static Datatype level(String name) {
		return Datatype.base(name, false);
	}

	protected static Datatype icon(String name) {
		return Datatype.base(name, false);
	}

	protected static Datatype voltage(String name) {
		return Datatype.scaling(name, false, 0.1f).unit("V");
	}

	protected static Datatype flow(String name) {
		return Datatype.base(name, false).unit("l/h");
	}

	protected static Datatype pressure(String name) {
		return Datatype.scaling(name, false, 0.1f).unit("%");
	}

	protected static Datatype percent2(String name) {
		return Datatype.base(name, false).unit("%");
	}

	protected static Datatype speed(String name) {
		return Datatype.base(name, false).unit("rpm");
	}

	protected static Datatype secOperationMode(String name) {
		return Datatype.selection(name, false,
				"off",
				"cooling",
				"heating",
				"fault",
				"transition",
				"defrost",
				"waiting",
				"waiting",
				"transition",
				"stop",
				"manual",
				"simulation start",
				"evu lock"
	);
	}

	protected static Datatype frequency(String name) {
		return Datatype.base(name, false).unit("Hz");
	}

	protected static Datatype power(String name) {
		return Datatype.base(name, false).unit("W");
	}

	protected static Datatype majorMinorVersion(String name) {
		return Datatype.custom(name, value -> {
			if (value > 0) {
				int major = Math.floorDiv(value, 100);
				int minor = value % 100;
				return String.format("%s.%s", major, minor);
			}
			return "0";
		});
	}
}
