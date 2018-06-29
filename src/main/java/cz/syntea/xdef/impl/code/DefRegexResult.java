/*
 * File: DefRegexResult.java
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

import cz.syntea.xdef.XDRegexResult;
import cz.syntea.xdef.XDValueAbstract;
import java.util.regex.Matcher;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

/** DefRegexResult.
 * @author Vaclav Trojan
 */
public final class DefRegexResult extends XDValueAbstract
	implements XDRegexResult {

	/** Matcher object generated from the source expression. */
	private Matcher _value;

	/** Creates a new instance of DefRegexResult. */
	public DefRegexResult() {}

	/** Creates a new instance of DefRegexResult.
	 * @param matcher The matcher object.
	 */
	public DefRegexResult(final Matcher matcher) {_value = matcher;}

	@Override
	/** Check if given data matches the regular expression.
	 * @return <tt>true</tt> if and only if the data matches regular expression.
	 */
	public boolean matches() {return _value.matches();}

	@Override
	public final int groupCount() {return _value.groupCount() + 1;}

	@Override
	/** Get the input subsequence captured by the given group during the
	 * previous match operation.
	 * @param index index of group.
	 * @return the input subsequence captured by the given group.
	 */
	public final String group(final int index) {
		if (_value == null) return "";
		try {
			return _value.group(index);
		} catch (Exception ex) {
			return "";
		}
	}

	@Override
	public final int groupStart(final int index) {
		try {
			return _value.start(index);
		} catch (Exception ex) {
			return -1;
		}
	}

	@Override
	public final int groupEnd(final int index) {
		try {
			return _value.end(index);
		} catch (Exception ex) {
			return -1;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_REGEXRESULT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.REGEXRESULT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		if (_value == null) return "";
		if (_value.matches()) return _value.group();
		return "";
	}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return String.valueOf(matches());}
}