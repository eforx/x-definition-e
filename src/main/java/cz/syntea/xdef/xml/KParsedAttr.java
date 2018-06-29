/*
 * Copyright 2007 Syntea software group a.s.
 *
 * File: KParsedAttr.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.xml;

import cz.syntea.xdef.sys.SPosition;

/** Container for attribute name, value and source position of value.
 * @author Vaclav Trojan
 */
public class KParsedAttr {

	private String _nsURI;
	private final String _name;
	private String _value;
	private final SPosition _pos;

	/** Creates a new instance of KParsedAttr.
	 * @param name the name of attribute.
	 * @param value value of attribute.
	 * @param pos SPosition of attribute.
	 */
	public KParsedAttr(String name, String value, SPosition pos) {
		_name = name.intern();
		_value = value;
		_pos = pos;
	}

	public KParsedAttr(String nsURI,
		String name,
		String value,
		SPosition pos) {
		_nsURI = nsURI;
		_name = name;
		_value = value;
		_pos = pos;
	}

	/* Get parsed attribute name (may be qualified name). */
	public String getName() {return _name;}

	/* Get namespace URI.
	 * @return namespace URI or <tt>null</tt>.
	 */
	public String getNamespaceURI() {return _nsURI;}

	/* Set namespace URI.
	 * @param namespace URI.
	 */
	public void setNamespaceURI(String nsURI) {_nsURI = nsURI;}

	/* Get string with value of attribute. */
	public String getValue() {return _value;}

	/* Set value of attribute. */
	public void setValue(String value) {_value = value;}

	/* Get source position of parset value of attribute. */
	public SPosition getPosition() {return _pos;}

	@Override
	public String toString() {
		return _name + " = \"" + _value + '"' +
			(_nsURI == null ? "" : "; URI = " + _nsURI);
	}

}
