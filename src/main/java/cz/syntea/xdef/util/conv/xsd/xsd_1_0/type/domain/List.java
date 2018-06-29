/*
 * File: List.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.util.conv.xsd.xsd_1_0.type.domain;

import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.util.conv.xsd2xd.schema_1_0.Utils;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.Element;

/** Represents list of XML schema data type.
 * @author Alexandrov
 */
public class List extends Specification {

	/** Type of list items. */
	private final Type _itemType;

	/** Creates instance of XML schema list construction.
	 * @param listElement list declaration element.
	 * @param schemaURL URL of schema containing list.
	 * @param schemaElements all schema elements.
	 */
	public List(Element listElement, URL schemaURL,
		Map<URL, Element> schemaElements) {
		String itemType = listElement.getAttribute("itemType");
		if (!"".equals(itemType)) {
			_itemType = Type.getType(
				itemType, listElement, schemaURL, schemaElements);
		} else {
			Element simpleTypeElement = KXmlUtils.firstElementChildNS(
				listElement, Utils.NSURI_SCHEMA, "simpleType");
			_itemType =
				new SimpleType(simpleTypeElement, schemaURL, schemaElements);
		}
	}

	/** Item type getter.
	 * @return  list item type.
	 */
	public Type getItemType() {return _itemType;}

	@Override
	public String getTypeMethod() {
		return "list(%item=" + _itemType.getTypeMethod() + ")";
	}

	@Override
	public String toString() {
		return "List [itemtype=" + _itemType.toString() + "]";
	}
}
