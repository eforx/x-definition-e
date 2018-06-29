/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XSParseIDREFS.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefContainer;
import java.util.Map;

/** Parser of Schema "IDREFS" type.
 * @author Vaclav Trojan
 */
public class XSParseIDREFS extends XSParseENTITIES {
	private static final String ROOTBASENAME = "IDREFS";

	public XSParseIDREFS() {
		super();
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITY.check(parser, xnode);");
			return;
		}
		Map<Object, ArrayReporter> tab = xnode.getIdRefTable();
		DefContainer val = (DefContainer) result.getParsedValue();
		for (int i = 0; i < val.getXDItemsNumber(); i++) {
			String id = val.getXDItem(i).toString();
			ArrayReporter a = tab.get(id);
			boolean err = false;
			if (a == null) {
				tab.put(id, a = new ArrayReporter()); //new item
				err = true;
			} else if (a.size() > 0) {
				err = true;
			}
			if (err) {
				// Missing element with identifier &{0}
				Report rep=Report.error(XDEF.XDEF522,
					id + "&{xpath}"+xnode.getXPos()
					+ "&{xdpos}" + xnode.getXDPosition());
				xnode.getReporter().genPositionInfo(rep);
				a.putReport(rep);
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
