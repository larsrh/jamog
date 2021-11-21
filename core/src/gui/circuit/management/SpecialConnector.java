/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.circuit.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sylvester
 */
public class SpecialConnector extends Connector {

	public static enum Type {

		CLOCK,
		OUT;

		int existingAutoConnectors = 0;

		@Override
		public String toString() {
			switch (this) {
				case CLOCK:
					return "Clock connector";
				case OUT:
					return "Outside connector";
			}
			return "";
		}
	}

	private static Map<Type, List<SpecialConnector>> connectors = new HashMap<Type, List<SpecialConnector>>();

	public static SpecialConnector giveAutoConnector(Type specialType) {
		String name = specialType.toString() + " (" + String.valueOf(++specialType.existingAutoConnectors) + ")";
		Connector.Type type = Connector.Type.UNDEFINED;
		switch (specialType) {
			case CLOCK:
				type = Connector.Type.OUTPUT;
				break;
		}
		return SpecialConnector.addConnector(specialType, name, type, 1, 1);
	}

	public static SpecialConnector addConnector(Type specialType, String name, Connector.Type type, int lineCount, int bitWidth) {
		SpecialConnector connector = new SpecialConnector(specialType, name, type, lineCount, bitWidth);
		List<SpecialConnector> connectorList;
		if ((connectorList = SpecialConnector.connectors.get(specialType)) == null) {
			connectorList = new LinkedList<SpecialConnector>();
			SpecialConnector.connectors.put(specialType, connectorList);
		}
		connectorList.add(connector);
		return connector;
	}

	public static SpecialConnector addConnector(Type specialType, String name, Connector.Type type, int lineCount, int[] bitWidths) {
		SpecialConnector connector = new SpecialConnector(specialType, name, type, lineCount, bitWidths);
		List<SpecialConnector> connectorList;
		if ((connectorList = SpecialConnector.connectors.get(specialType)) == null) {
			connectorList = new LinkedList<SpecialConnector>();
			SpecialConnector.connectors.put(specialType, connectorList);
		}
		connectorList.add(connector);
		return connector;
	}

	public static List<SpecialConnector> getConnectors(Type type) {
		List<SpecialConnector> list;
		if ((list = SpecialConnector.connectors.get(type)) != null)
			return list;
		return Collections.emptyList();
	}

	public static List<SpecialConnector> getConnectors() {
		List<SpecialConnector> list = new LinkedList<SpecialConnector>();
		for (Type type : Type.values())
			list.addAll(SpecialConnector.getConnectors(type));
		return list;
	}

	static {
		SpecialConnector.addConnector(SpecialConnector.Type.CLOCK, "Default Clock", Connector.Type.OUTPUT, 1, 1);
	}

	private Type type;

	private SpecialConnector(Type specialType, String name, Connector.Type type, int lineCount, int[] bitWidths) {
		super(name, type, lineCount, bitWidths);
		this.type = specialType;
	}

	private SpecialConnector(Type specialType, String name, Connector.Type type, int lineCount, int bitWidth) {
		super(name, type, lineCount, bitWidth);
		this.type = specialType;
	}

	public Type getSpecialType() {
		return type;
	}

}
