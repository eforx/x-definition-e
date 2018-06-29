/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XMText.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.model;

import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDContainer;

/** Interface of model of XML text data (attributes, text nodes).
 * @author Vaclav Trojan
 */
public interface XMData extends XMNode {

	public XDParseResult validate(String value);

	/** Get value specified as default.
	 * @return string with value specified as default or return <tt>null</tt>
	 * if there was not specified a default value.
	 */
	public String getDefaultValue();

	/** Get value specified as fixed.
	 * @return string with value specified as fixed or return <tt>null</tt>
	 * if there was not specified a default value.
	 */
	public String getFixedValue();

	/** Get values of enumeration type.
	 * @return string array with values specified as enumeration or return
	 * <tt>null</tt> if specified type is not enumeration of string values.
	 */
	public String[] getEnumerationValues();

	/** Get base type ID of value.
	 * @return type ID of data value.
	 */
	public short getBaseType();

	/** Get type name of value.
	 * @return type name of data value.
	 */
	public String getValueTypeName();

	/** Get parser used for parsing of value.
	 * @return XDParser or null if parser is not available.
	 */
	public XDValue getParseMethod();

	/** Get parameters of parsing method.
	 * @return XDParser or null if parser is not available.
	 */
	public XDContainer getParseParams();

	/** Get type of parsed value.
	 * @return value from cz.syntea.xdef.XDValueTypes.
	 */
	public short getParserType();

	/** Get datetime mask from the model parser.
	 * @return mask of datetime type or <tt>null</tt>.
	 */
	public String getDateMask();

	/** Get name parser (i.e. string, "base64Binary", "hexBinary", float etc.).
	 * @return name of parser or empty string.
	 */
	public String getParserName();

}