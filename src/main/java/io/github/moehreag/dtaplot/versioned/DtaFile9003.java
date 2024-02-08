package io.github.moehreag.dtaplot.versioned;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import io.github.moehreag.dtaplot.DataField;
import io.github.moehreag.dtaplot.DataFieldContainer;
import io.github.moehreag.dtaplot.DtaFile;
import lombok.AllArgsConstructor;

public class DtaFile9003 extends DtaFile {

	public DtaFile9003(ByteBuffer data) {
		super(data);

		data.position(4);
		int defSize = data.getInt();
		short count = data.getShort();
		short length = data.getShort();

		List<FieldDef> list = new ArrayList<>();
		readDef(list, defSize);

		List<Map<String, Value<?>>> values = new ArrayList<>();
		for (FieldDef d : list){
			DataFieldContainer c = d.read();
			if (!c.isVoid()){
				Map<String, Value<?>> map = new HashMap<>();
				c.get()//.stream().filter(DataField::isNumeric)
						.forEach(val -> map.put(val.getName(), val.getValue()));
				if (!map.isEmpty()){
					values.add(map);
				}
			}
		}
		setDatapoints(values);
	}

	private void readDef(List<FieldDef> list, int defSize) {

		int end = data.position() + defSize -2;
		String category = "";
		while (data.position() < end){
			byte id = data.get();
			int type = id & 0x0F;

			switch (type){
				case 0: {
					// Gruppe / Kategorie
					category = readString();
					list.add(new Category(category));
					//_log(QString("0x%1, %2").arg(id, 2, 16, QChar('0')).arg(category));
					break;
				}
				case 1: {
					// analoges Feld
					String name = readString();
					int color = readColor();
					//fcfg->setColor(name, color);
					short factor = 10;
					if ((id & 0x80) == 0)
						factor = data.getShort();
					list.add(new Analogue(category, name, color, factor));
					//DataFieldAnalog *f = new DataFieldAnalog(category, name, factor, 10, this);
					//m_fields.append(f);
					//_log(QString("0x%1, %2").arg(id, 2, 16, QChar('0')).arg(QString(*f)));
					//dsLen += 2;
					break;
				}
				case 2:
				case 4: {
					//DataFieldDigital *dig = new DataFieldDigital(this);
					byte count = data.get();

					// Sichtbarkeit fuer jedes Feld extra?
					short visibility = (short) 0xFFFF;
					if ((id & 0x40) == 0)
						visibility = data.getShort();

					// factoryOnly fuer jedes Feld extra?
					short factoryOnlyAll = 0;
					if ((id & 0x20) == 0)
						factoryOnlyAll = data.getShort();

					// in/out fuer jedes Feld extra
					short ios = 0;
					if ((id & 0x04) == 0)
						ios = data.getShort();
					else if ((id & 0x80) == 0)
						ios = (short) 0xFFFF;

					/*_log(QString("0x%1, IO with %2 items, visibility=0x%3, factoryOnly=0x%4, ios=0x%5")
							/.arg(id, 2, 16, QChar('0'))
							.arg(count)
							.arg(visibility, 4, 16, QChar('0'))
							.arg(factoryOnlyAll, 4, 16, QChar('0'))
							.arg(ios, 4, 16, QChar('0')));*/
					Digital.Bit[] bits = new Digital.Bit[count];
					for (int i = 0; i < count; ++i) {
						String name = readString();
						int color = readColor();
						//fcfg->setColor(name, color);
						bits[i] = new Digital.Bit(name, color);
						//DataFieldDigitalBit *bit = new DataFieldDigitalBit(category + " EA", name, i, DataField::Input, dig);
//                bit->set_visible(visibility);
//                bit->set_factoryOnly(factoryOnlyAll);
						//bit->set_direction(ios);
						//dig->addItem(bit);
						//_log(QString("   %1, RGB=%2").arg(name).arg(DataField::color2str(color)));
					}
					list.add(new Digital(category, bits, visibility, factoryOnlyAll, ios));

					//_log(QString("IO end"));
					//m_fields.append(dig);
					//dsLen += 2;
					break;
				}
				case 3: {
					// ENUM Feld
					String name = readString();
					byte count = data.get();

					//DataFieldEnum *f = new DataFieldEnum(category, name, this);
					//m_fields.append(f);
					//_log(QString("0x%1, ENUM '%2' with %3 items").arg(id, 2, 16, QChar('0')).arg(name).arg(count));
					String[] values = new String[count];
					for (int i=0; i<count; ++i) {
						String itemText = readString();
						values[i] = itemText;
						//_log(QString("   %1 = %2").arg(i).arg(itemText));
					}
					list.add(new Enum(category, name, values));
					break;
				}
				// LCOV_EXCL_START
				default: {
					//_log(QString("DTA v9003 - unknown field type 0x%1!").arg(fieldType, 2, 16, QChar('0')));
					throw new IllegalStateException(String.format("DTA v9003 - unknown field type 0x%08X!", type));
				}
			}


		}
	}

	private String readString(){
		StringBuilder builder = new StringBuilder();
		while (true){
			byte b = data.get();
			if (b == 0){
				break;
			}
			builder.append(b);
		}
		data.position(data.position()-1);
		return builder.toString().replace("Text_", "");
	}

	private int readColor(){
		return 0xFF000000 | (data.get() << 16) | (data.get() << 8) | data.get();
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

	private interface FieldDef {

		DataFieldContainer read();
	}

	@AllArgsConstructor
	private static class Category implements FieldDef {

		private final String name;
		@Override
		public DataFieldContainer read() {
			return DataFieldContainer.empty();
		}
	}

	@AllArgsConstructor
	private class Analogue implements FieldDef {

		private final String category;
		private final String name;
		private final int color;
		private final short factor;

		@Override
		public DataFieldContainer read() {
			return analogue(category, name, factor, 10, data);
		}
	}

	@AllArgsConstructor
	private class Digital implements FieldDef {

		private final String category;
		private final Bit[] bits;

		private final short visibility;
		private final short factoryOnlyAll;
		private final short ios;

		@Override
		public DataFieldContainer read() {
			DataField.DataFieldBit[] bits = new DataField.DataFieldBit[this.bits.length];
			for (int i=0;i<bits.length;i++){
				bits[i] = this.bits[i].read(i, readBit(ios, i));
			}
			return DataField.digital(category, data, bits);
		}

		@AllArgsConstructor
		public static class Bit {
			private final String name;
			private final int color;

			public DataField.DataFieldBit read(int bit, boolean inverted){
				return DataField.bit(name, bit, inverted);
			}
		}
	}

	@AllArgsConstructor
	private class Enum implements FieldDef {

		private final String category;
		private final String name;
		private final String[] values;

		@Override
		public DataFieldContainer read() {
			return DataFieldContainer.multi(Arrays.stream(values).map(s -> new DataField<String>(category, name) {
				@Override
				public Value<String> getValue() {
					return of(s);
				}

				@Override
				public boolean isNumeric() {
					return false;
				}
			}).collect(Collectors.toCollection(ArrayList::new)));
		}

	}
}
