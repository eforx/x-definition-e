/*
 * File: DefObject.java
 *
 * Copyright 20 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

/** DefByte
 *
 * @author  Vaclav Trojan
 */
public final class DefObject extends XDValueAbstract {

	/** The value associated with this item. */
	private Object _value;

	/** Creates a new empty instance of DefInteger
	 */
	public DefObject() {_value = null;}

	/** Creates a new instance of DefInteger
	 * @param value The object.
	 */
	public DefObject(Object value) {_value = value;}

	/** Set object value.
	 * @param obj object to be set as value.
	 */
	public void setObject(final Object obj) {_value = obj;}

	@Override
	/** Get object representing value */
	public Object getObject() {return _value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_OBJECT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.OBJECT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return "" + _value;}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefObject(_value);}
	@Override
	public int hashCode() {return _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof DefObject) {
			return _value.equals(((DefObject) arg)._value);
		}
		return false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return always <tt>false</tt>.
	 */
	public boolean equals(final XDValue arg) {
		return arg.getItemId() == XDValueID.XD_OBJECT ?
			 equals((DefObject) arg) : false;
	}

	/** Check whether some other DefBigDecimal object is "equal to" this one.
	 * @param arg other DefBigDecimal object to which is to be compared.
	 * @return true if the object is identical to the argument otherwise return
	 * <tt>false</tt>.
	 */
	public boolean equals(final DefObject arg) {
		return _value.equals(arg._value);
	}

}