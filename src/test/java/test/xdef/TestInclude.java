/*
 * File: TestInclude.java
 * Copyright 2006 Syntea.
 *
 * This file may be copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt kopirovan, modifikovan a siren pouze v souladu
 * s textem prilozeneho souboru LICENCE.TXT, ktery obsahuje specifikaci
 * prislusnych prav.
 */
package test.xdef;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportPrinter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDPool;
import java.io.File;
import java.io.StringWriter;

/** Test of attribute "include" in header of XDefinition and of "xi:include"
 * in XML data.
 * @author Vaclav Trojan
 */
public final class TestInclude extends Tester {

	public TestInclude() {super();}

	@Override
	public void test() {
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp;
		StringWriter strw;
		StringWriter sw;
		String s;
		Report report;
		String dataDir = new File(getDataDir(), "test").getAbsolutePath()
			.replace('\\', '/');
		if (!dataDir.endsWith("/")) {
			dataDir += '/';
		}
		boolean chkSyntax = getChkSyntax();
		try {
			//xd:include in XDefinition header
			xdef =
"<xd:def xmlns:xd = 'http://www.syntea.cz/xdef/2.0' name = 'a' root = 'foo'"+
"   xd:include = \"" + dataDir +"TestInclude_1.xdef\">\n"+
"  <foo xd:script = \"finally out('f')\">\n"+
"    <bar xd:script = '*; ref b#bar'/>\n"+ // b is xdefinition from include
"  </foo>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("f", strw.toString());
			xml = "<foo><bar><foo/></bar></foo>";
			strw = new StringWriter();
			parse(xp,"a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("bf", strw.toString());
			// the same from file
			xdef = dataDir + "TestInclude.xdef";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("f", strw.toString());
			xml = "<foo><bar><foo/></bar></foo>";
			strw = new StringWriter();
			parse(xp,"a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("bf", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			//xi:include in collection
			xdef =
"<xd:collection xmlns:xd='http://www.syntea.cz/xdef/2.0'>\n"+
"<xd:def name = 'a' root = 'foo'>\n"+
"  <foo xd:script = \"finally out('f')\">\n"+
"    <bar xd:script = '*; ref b#bar'/>\n"+ // b is xdefinition from include
"  </foo>\n"+
"</xd:def>\n"+
"<xi:include xmlns:xi = 'http://www.w3.org/2001/XInclude'\n" +
"   href = '" + dataDir + "TestInclude_1.xdef" + "' />\n" +
"</xd:collection>";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("f", strw.toString());
			xml = "<foo><bar><foo/></bar></foo>";
			strw = new StringWriter();
			parse(xp,"a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("bf", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			//include attribute in collection
			xdef =
"<xd:collection xmlns:xd='http://www.syntea.cz/xdef/2.0'\n"+
	"include='" + dataDir + "TestInclude.xdef'/>";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = "<foo><bar><foo/></bar></foo>";
			parse(xp,"a", xml, reporter, strw, null, null);
			if (reporter.errors()) {
				sw = new StringWriter();
				ReportPrinter.printListing(sw, xml, reporter, true);
				fail(sw.toString());
			}
			assertEq("bf", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {//xinclude
			xdef =
"<xdef:def xmlns:xdef='" + XDEFNS + "' name='test' root='a'>\n"+
"  <a>\n"+
"    <b>\n"+
"      required string()\n"+
"    </b>\n"+
"  </a>\n"+
"</xdef:def>";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_1_1.xml";
			assertEq("<a><b>&lt;c>text&lt;/c></b></a>",
				parse(xp, "test", xml, reporter));
			assertNoErrors(reporter);
			xml = dataDir + "TestInclude_2_1.xml";
			parse(xp, "test", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				report = reporter.getReport();
				if (report == null) {
					fail("Error not reported");
				} else {
					while (report != null && ("XML308".equals(report.getMsgID())
						|| "XDEF539".equals(report.getMsgID()))) {
						report = reporter.getReport();
					}
				}
				while (report != null) {
					fail("Unexpected error: " + report.toString());
				}
			}
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !s.contains("Not_Exists.File")) {
				fail(ex);
			}
		}
		try {// xinclude
			xdef =
"<xdef:def xmlns:xdef='" + XDEFNS + "'\n"+
"          xdef:name   =\"test\"\n"+
"          xdef:root   =\"a\">\n"+
"  <a>\n"+
"    <b>\n"+
"      required string()\n"+
"    </b>\n"+
"  </a>\n"+
"</xdef:def>";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_3_1.xml";
			parse(xp, "test", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				while (!"XML308".equals(
					(report=reporter.getReport()).getMsgID()) &&
					!"XML306".equals(report.getMsgID()) &&
					!"XDEF527".equals(report.getMsgID()) &&
					!"SYS036".equals(report.getMsgID()) &&
					!"SYS033".equals(report.getMsgID())) {
					fail(report.toString());
				}
			}
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !(s.contains("SYS033")
				|| s.contains("XML306"))) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xdef:def xmlns:xdef='" + XDEFNS + "'\n"+
"          xdef:name   =\"test\"\n"+
"          xdef:root   =\"a\">\n"+
"  <a>\n"+
"    <b>\n"+
"      required string()\n"+
"    </b>\n"+
"  </a>\n"+
"</xdef:def>";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_4_1.xml";
			assertEq("<a><b>&lt;c>text&lt;/c></b></a>",
				parse(xp, "test", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			setChkSyntax(false);
			xdef = dataDir + "TestInclude_5.xdef";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_5.xml";
			parse(xp, "wsdefinitions", xml);
		} catch (Exception ex) {fail(ex);}
		setChkSyntax(chkSyntax);
		try {
			xdef = dataDir + "TestInclude_6.xdef";
			try {
				setChkSyntax(false);
				compile(xdef);
				fail("Exception not thrown");
			} catch (SRuntimeException ex) {
				s = ex.getMessage();
				if (s == null ||
					s.indexOf("XML308") < 0 && s.indexOf("XML075") < 0) {
					fail(ex);
				}
			}
		} catch (Exception ex) {fail(ex);}
		setChkSyntax(chkSyntax);
		try {// once more with ignoreEntities
			xdef = dataDir + "TestInclude_7.xdef";
			try {
				setChkSyntax(false);
				compile(xdef);
				fail("Exception not thrown");
			} catch (SRuntimeException ex) {
				s = ex.getReport().getModification();
				if (s == null ||
					s.indexOf("XML308") < 0 && s.indexOf("XML075") < 0) {
					fail(ex);
				}
			}
		} catch (Exception ex) {fail(ex);}
		setChkSyntax(chkSyntax);
		try {
			setProperty(XDConstants.XDPROPERTY_VALIDATE,
				XDConstants.XDPROPERTYVALUE_VALIDATE_TRUE);
			xp = compile(dataDir + "TestInclude_8.xdef");
			xml = dataDir + "TestInclude_8.xml";
			parse(xp, "A", xml, reporter);
			assertNoErrors(reporter);
			xml = dataDir + "TestInclude_8_6.xml";
			parse(xp, "A", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		setProperty(XDConstants.XDPROPERTY_VALIDATE,
				XDConstants.XDPROPERTYVALUE_VALIDATE_FALSE);
		try {
			//test Include default (not allowed)
			xdef =
"<xd:def xmlns:xd = '" + XDEFNS + "' xd:root ='A'>\n"+
"  <A><b/></A>\n"+
"</xd:def>";
			xp = compile(xdef); //here is default (not allowed)
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);
//			xp.setResolveIncludes(false);
			xml = dataDir + "TestInclude_9.xml";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
			// include allowed from properties
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
			xp = compile(xdef);
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xp = compile(xdef);
			 // from program
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
			xdef = dataDir + "TestInclude_9_1.xdef";
			// not allowed from properties
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
			// allowed from properties
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
			//allowed
			xp = compile(xdef);
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xp = compile(xdef);
			 // from program
//			xp.setResolveIncludes(true); //resove
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);
//			xp.setResolveIncludes(false);  //not resove
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}