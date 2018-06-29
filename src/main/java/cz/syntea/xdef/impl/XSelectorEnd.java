/*
 * File: XSelectorEnd.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.model.XMDefinition;
import java.io.IOException;
import java.util.ArrayList;

/** End mark of selector.
 *  deprecated - will be not public in future versions
 * @author Vaclav Trojan
 */
public class XSelectorEnd extends XNode {

	/* Create the new instance of XSelectorEnd object. */
	public XSelectorEnd() {
		super(null, "$selector_end", null, XNode.XMSELECTOR_END);
	}

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return root XMDefinition node.
	 */
	public XMDefinition getXMDefinition() {return null;}

	@Override
	public int getInitCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getFinallyCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getMatchCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getComposeCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getCheckCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnTrueCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnFalseCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getDefltCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnStartElementCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnAbsenceCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnExcessCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnIllegalAttrCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnIllegalTextCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getOnIllegalElementCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
	@Override
	public int getVarinitCode() {
		throw new UnsupportedOperationException("Not supported here.");
	}
////////////////////////////////////////////////////////////////////////////////

	@Override
	void writeXNode(XDWriter xw, ArrayList<XNode> list) throws IOException {
		xw.writeShort(getKind());
	}

	static XSelectorEnd readXSelectorEnd(XDReader xr) throws IOException {
		xr.readShort();
		return new XSelectorEnd();
	}
}