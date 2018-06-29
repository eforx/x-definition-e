/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: KEmptyNamedNodeMap.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.xml;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Implemetation of empty object org.w3c.dom.NamedNodeMap.
 *
 * @author Vaclav Trojan
 */
public final class KEmptyNamedNodeMap implements NamedNodeMap {
	@Override
	public final int getLength() {return 0;}
	@Override
	public final Node item(int index) {return null;}
	/** Creates a new instance of KEmptyNamedNodeMap */
	public KEmptyNamedNodeMap() {}
	@Override
	public final Node removeNamedItem(final String name) {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
			"Empty map");
	}
	@Override
	public final Node removeNamedItemNS(final String namespaceURI,
		final String localName)	{
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
			"Empty map");
	}
	@Override
	public final Node setNamedItem(Node arg) {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
			"Empty map");
	}
	@Override
	public final Node setNamedItemNS(Node arg) {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
			"Empty map");
	}
	@Override
	public final Node getNamedItemNS(final String namespaceURI,
		final String localName) {
		return null;
	}
	@Override
	public final Node getNamedItem(final String name) {return null;}

}