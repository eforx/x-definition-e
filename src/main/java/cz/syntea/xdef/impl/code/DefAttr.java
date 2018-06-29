/*
 * File: DefAttr.java
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
package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.XDNamedValue;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

/** Implementation of script value with org.w3c.dom.Attr.
 *
 * @author  Vaclav Trojan
 */
class DefAttr extends XDValueAbstract implements XDNamedValue {

	/** The attribute as value of this item. */
	private final Attr _value;

	/** Creates a new instance of DefAttr. */
	DefAttr() {_value = null;}

	/** Creates a new instance of DefAttr.
	 * @param value attribute which to be set as value of this object.
	 */
	public DefAttr(final Attr value) {_value = value;}

	/** Creates a new instance of DefAttr and create an attribute to be set as
	 * value of this object.
	 * @param doc Document where attribute is to be created.
	 * @param name name of attribute.
	 * @param value value of the attribute.
	 */
	public DefAttr(final Document doc, final String name, final String value) {
		_value = doc.createAttribute(name);
		_value.setValue(value);
	}

	/** Return the value of org.w3c.dom.Attr object.
	 * @return the value of the node as org.w3c.dom.Attr object.
	 */
	public Attr attrValue() {return _value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_ATTR;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.ATTR;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value==null? null : _value.getValue();}

	@Override
	/** Get string value of this object or throw SRuntimeException.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return _value==null? null : _value.getValue();}

	@Override
	public String getName() {return _value==null ? null : _value.getName();}

	@Override
	public XDValue getValue() {return new DefString(stringValue());}

	@Override
	public XDValue setValue(XDValue newValue) {
		XDValue result = getValue();
		_value.setValue(newValue.toString());
		return result;
	}

}