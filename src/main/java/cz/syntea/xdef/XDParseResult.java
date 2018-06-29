/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseResult.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.SParser;

/** Result of parsers in XScript.
 * @author Vaclav Trojan
 */
public interface XDParseResult extends XDValue, SParser {

	/** Return the parsed value of DefParseResult object.
	 * @return copy of DefParseResult.
	 */
	public XDValue getParsedValue();

	/** Set parsed value of DefParseResult object.
	 * @param value value of parsed object.
	 */
	public void setParsedValue(XDValue value);

	/** Set parsed value of DefParseResult object.
	 * @param value value of parsed object.
	 */
	public void setParsedValue(String value);

	/** Get text until current position and the first occurrence of a white
	 * space or the end of source. Set current position after the token.
	 * @return found token or <tt>null</tt>.
	 */
	public String nextToken();

	/** Check if given data matches parser (result is same as !errors()).
	 * @return <tt>true</tt> if and only if the data were parsed without error.
	 */
	public boolean matches();

	/** Get reporter from parsing.
	 * @return ArrayReporter containing parsing errors or <tt>null</tt>.
	 */
	public ArrayReporter getReporter();

	/** Add all reports from reporter.
	 * @param reporter array reporter (may be null).
	 */
	public void addReports(ArrayReporter reporter);

	/** Clear all reports. */
	public void clearReports();

	/** Replace parsed string from the position from argument upto parsed
	 * position by string from argument.
	 * @param from position from to replace.
	 * @param s replace string.
	 */
	public void replaceParsedBufferFrom(int from, String s);

}