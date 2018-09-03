/*
 * Copyright 2015 Syntea software group a.s. All rights reserved.
 *
 * File: Aaa.java, created 2015-03-16.
 * Package: mytest.xdef
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package test.xdef;

import test.utils.XDTester;
import cz.syntea.xdef.sys.SDatetime;
import cz.syntea.xdef.util.XDChecker;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXData;
import java.util.Arrays;

/**
 * @author Vaclav Trojan
 */
public class TestXDChecker extends XDTester {

	public TestXDChecker() {super();}

	public static boolean typX(XXData xdata) {
		return "xxx".equals(xdata .getTextValue());
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String methods = "boolean test.xdef.TestXDChecker.typX(XXData);";
		String declarations =
"boolean typY() {\n"+
" return 'yyy'.equals(getText());\n"+
"}";
		String options = "ignoreEmptyAttributes";
		XDChecker chk = new XDChecker(
			new Class<?>[]{test.xdef.TestXDChecker.class},
			methods,
			declarations,
			options);
		XDParseResult x;
		assertTrue(chk.checkType("string(2, 3)", "2").errors());
		assertTrue(chk.checkType("string(2, 3)", "abcd").errors());
		assertTrue(chk.checkType("string(2, 3)", "ab").matches());
		assertTrue(chk.checkType("string(2, 3)", "abc").matches());
		assertTrue(chk.checkType("string()", "a").matches());
		assertTrue(chk.checkType("string()", "aaaaaaaa").matches());
		assertTrue(chk.checkType("string(1)", "a").matches());
		assertTrue(chk.checkType("string(1)", "").matches());
		assertTrue(chk.checkType("string(1)", "aaaaaaaa").errors());
		assertTrue(chk.checkType("enum('ab','efg')", "ab").matches());
		assertTrue(chk.checkType("enum('ab','cdef')", "cdef").matches());
		assertTrue(chk.checkType("enum('ab','efg')", "efg").matches());
		assertTrue(chk.checkType("enum('ab','efg')", "cde").errors());
		x = chk.checkType("hex()", "cde0");
		assertTrue(x.matches());
		assertTrue(Arrays.equals(new byte[]{(byte) 205, (byte) 224},
			x.getParsedValue().getBytes()));
		assertTrue(chk.checkType("hex()", "0").matches());
		assertTrue(chk.checkType("hex()", " .").errors());
		assertTrue(chk.checkType("tokens('||||Q|XY|A')", "||Q").matches());
		assertTrue(chk.checkType("typX", "xxx").matches()); //external method
		assertTrue(chk.checkType("typX", "aa").errors());
		assertTrue(chk.checkType("typY", "yyy").matches()); //declared method
		assertTrue(chk.checkType("typY", "aa").errors());
		x = chk.checkType("datetime('d.M yyyy')", "7.12 2015");
		assertTrue(x.matches());
		SDatetime d = new SDatetime("2015-12-07");
		assertTrue(d.equals(x.getParsedValue().datetimeValue()));
		x = chk.checkType("datetime('d.M yyyy')", "7.21 2015");
		assertTrue(x.errors());
		assertNull(x.getParsedValue());
		x = chk.checkType("int()", "10");
		assertTrue(10 == x.getParsedValue().intValue());
		x = chk.checkType("int()", null);
		assertTrue(x.errors(), "Error not recognized");
		x = chk.checkType("? int()", null);
		assertFalse(x.errors());

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}