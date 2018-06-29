/*
 * File: Tester.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test.xdef;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportPrinter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.xml.KXmlConstants;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.XDBuilder;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDOutput;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.model.XMDefinition;
import cz.syntea.xdef.model.XMElement;
import cz.syntea.xdef.impl.code.DefOutStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import org.w3c.dom.Element;
import cz.syntea.xdef.sys.ReportReader;
import cz.syntea.xdef.sys.ReportWriter;
import javax.xml.namespace.QName;

/** Support of tests.
 * @author Vaclav Trojan
 */
public abstract class Tester extends cz.syntea.xdef.sys.STester {
//	public static String XDEFNS = KXmlConstants.XDEF20_NS_URI;
	public static String XDEFNS = KXmlConstants.XDEF31_NS_URI;
	private static boolean _genObj = false;
	private final Properties _props = new Properties();
	public static XDPool _xdOfxd = null;
	public static boolean chkXDSyntax = false;
	/** Creates a new instance of TestAbstract */
	public Tester() {
		super();
		resetProperties();
	}

	public final void resetProperties() {
		_props.clear();
//		setProperty(XDConstants.XDPROPERTY_DISPLAY,
//			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);
//		setProperty(XDConstants.XDPROPERTY_DEBUG,
//			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
//		setProperty(XDConstants.XDPROPERTY_XINCLUDE,
//			XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
//		setProperty(XDConstants.XDPROPERTY_IGNOREUNRESOLVEDENTITIES,
//			XDConstants.XDPROPERTYVALUE_IGNOREUNRESOLVEDENTITIES_FALSE);
//		int yr = new java.util.GregorianCalendar().get(
//			java.util.GregorianCalendar.YEAR);
//		setProperty(XDConstants.XDPROPERTY_MINYEAR,	String.valueOf(yr - 200));
//		setProperty(XDConstants.XDPROPERTY_MAXYEAR,	String.valueOf(yr + 200));
		setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		setProperty(XDConstants.XDPROPERTY_MINYEAR, "1916");
		setProperty(XDConstants.XDPROPERTY_MAXYEAR, "2216");
		setProperty(XDConstants.XDPROPERTY_SPECDATES,
			"3000-12-31,3000-12-31T00:00:00,3000-12-31T23:59:59");
/*#if DEBUG*#/
		setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
			XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE);
/*#end*/
	}

	public final Properties getProperties() {return _props;}

	public final void setProperty(final String key, final String value) {
		if (value == null) {
			_props.remove(key);
		} else {
			_props.setProperty(key, value);
		}
	}

	final public static void setGenObjFile(final boolean genObj) {
		_genObj = genObj;
	}

	final public static boolean getGenObjFile() {return _genObj;}

	final public Element test(final String[] xdefs,
		final String data,
		final String name,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode,
		final Class[] exts) {
		System.out.flush();
		System.err.flush();
		try {
			InputStream[] xdin = new InputStream[xdefs.length];
			for (int i = 0; i <xdefs.length; i++) {
				xdin[i] = new ByteArrayInputStream(xdefs[i].getBytes("ASCII"));
			}
			InputStream in = null;
			if (data != null && data.length() > 0) {
				in = new java.io.ByteArrayInputStream(data.getBytes("ASCII"));
			}
			Element result = test(xdin, in, name, out, reporter, mode, exts);
			return result;
		} catch (Exception ex) {
			fail(ex);
		}
		System.err.flush();
		System.out.flush();
		return null;
	}

	final public Element test(final String xdef,
		final String data,
		final String name,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode,
		final Class[] exts) {
		return test(new String[] {xdef}, data, name, out, reporter, mode, exts);
	}

	/** Returns the available element model represented by given name or
	 * <i>null</i> if definition item is not available.
	 * @param xdef XDefinition.
	 * @param key The name of definition item used for search.
	 * @return The required XElement or null.
	 */
	private static XMElement getXElement(final XMDefinition xdef,
		final String key) {
		int ndx = key.lastIndexOf('#');
		String lockey;
		XMDefinition def;
		if (ndx < 0) { //reference to this set, element with the name from key.
			lockey = key;
			def = xdef;
		} else {
			def = xdef.getXDPool().getXMDefinition(key.substring(0,ndx));
			if (def == null) {
				return null;
			}
			lockey = key.substring(ndx + 1);
		}
		XMElement[] elems = def.getModels();
		for (int i = 0; i < elems.length; i++) {
			XMElement xel  = elems[i];
			if (lockey.equals(xel.getName())) {
				return xel;
			}
		}
		return null;
	}

	private Element createElement(final XDPool xp,
		final String xdname,
		final ReportWriter reporter,
		final Element el,
		final DefOutStream stdout) {
		XDDocument xd = xp.createXDDocument(xdname);
		xd.setProperties(_props);
		xd.setStdOut(stdout);
		String qname;
		String nsURI;
		if (el != null) {
			xd.setXDContext(el);
			nsURI = el.getNamespaceURI();
			qname = el.getTagName();
		} else {
			XMElement xe;
			XMDefinition xdf = xp.getXMDefinition(xdname);
			if ((xe = getXElement(xdf, (qname = xdf.getName()))) == null) {
				//Model of element '&{0}' is missing in XDefinition&{1}{ }
				throw new SRuntimeException(XDEF.XDEF601,
					qname,xdf.getName());
			}
			nsURI = xe.getNSUri();
		}
		xd.xcreate(new QName(nsURI, qname), reporter);
		Element result = xd.getElement();
		return result;
	}

	public final static XDPool checkExtObjects(final XDPool xp) {
		if (!_genObj) {return xp;}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			xp.writeXDPool(baos);
			baos.close();
			return XDFactory.readXDPool(
				new ByteArrayInputStream(baos.toByteArray()));
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch(Error e) {
			throw new RuntimeException(e);
		}
	}

	public final Element test(final InputStream[] xdefs,
		final InputStream data,
		final String name,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode,
		final Class<?>... exts) {
		try {
			if (reporter != null) {
				reporter.clear();
			}
			XDBuilder xb = XDFactory.getXDBuilder(_props);
			xb.setExternals(exts);
			xb.setSource(xdefs, null);
			XDPool xp = checkExtObjects(xb.compileXD());
			if (out != null) {
				out.flush();
			}
			if (xdefs == null) {
				throw new Exception("XDefinition " + name + " doesn't exist");
			}
			DefOutStream stdout;
			if (out == null) {
				stdout = null;
			} else {
				stdout =new DefOutStream(new OutputStreamWriter(out), false);
			}
			if (mode == 'C') {
				Element el = null;
				if (data != null) {
					el = KXmlUtils.parseXml(data, true).getDocumentElement();
				}
				return createElement(xp, name , reporter, el, stdout);
			} else {
				XDDocument xd = xp.createXDDocument(name);
				xd.setProperties(_props);
				if (stdout != null) {
					xd.setStdOut(stdout);
				}
				xd.xparse(data, "", reporter);
				Element result = xd.getElement();
				return result;
			}
		} catch (Exception ex) {
			fail(ex);
		}
		return null;
	}

	/* if reporter is not null checking of result of data proecessin is
	 * skipped! */
	final public Element test(final InputStream xn,
		final InputStream data,
		final String name,
		final OutputStream os,
		final ReportWriter reporter,
		final Class[] exts) {
		return test(new InputStream[]{xn}, data, name, os, reporter, exts);
	}

	// if reporter is not null checking of result of data proecessin is
	// skipped!
	final public Element test(final InputStream[] xdefs,
		final InputStream data,
		final String name,
		final OutputStream os,
		final ReportWriter reporter,
		final Class[] exts) {
		try {
			ReportWriter myreporter = reporter;
			if (reporter == null) {
				myreporter = new ArrayReporter();
			}
			Element result = test(xdefs, data, name, os, reporter, 'P', exts);
			if (reporter == null) {
				if (myreporter.errors()) {
					StringWriter sw = new StringWriter();
					data.reset();
					ReportReader rri = myreporter.getReportReader();
					ReportPrinter.printListing(sw,
						new java.io.InputStreamReader(data), rri, true);
					fail(sw.toString());
				}
				if (result == null) {
					fail("got null result");
				}
			}
			return result;
		} catch (Exception ex) {
			fail(ex);
		}
		return null;
	}

	final public void checkResult(final Element el, final String expected) {
		if (expected == null) {
			return;
		}
		checkResult(el, KXmlUtils.parseXml(expected).getDocumentElement());
	}

	final public void checkResult(final Element el, final Element expected) {
		ReportWriter rw = KXmlUtils.compareElements(el, expected);
		if (!rw.errorWarnings()) {
			return;
		}
		System.err.flush();
		System.out.flush();
		StringWriter swr = new StringWriter();
		Report rep;
		ReportReader rri = rw.getReportReader();
		while((rep = rri.getReport()) != null) {
			swr.write(rep.toString());
			swr.write('\n');
		}
		fail(swr.toString());
	}

	final public Element test(final Class[] objs,
		final String xdef,
		final String data,
		final String name,
		final OutputStream out,
		final ArrayReporter reporter,
		final char mode) throws Exception { // 'P' => parse, 'C' => create
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			_xdOfxd.createXDDocument().xparse(xdef, null);
		}
		XDBuilder xb = XDFactory.getXDBuilder(_props);
		xb.setExternals(objs);
		xb.setSource(xdef);
		XDPool xp = checkExtObjects(xb.compileXD());
		XDDocument xd = xp.createXDDocument(name);
		xd.setProperties(_props);
		DefOutStream stdout =
			new DefOutStream(new OutputStreamWriter(out), false);
		if (mode == 'C') {
			Element el = null;
			if (data != null && data.length() > 0) {
				el = KXmlUtils.parseXml(data).getDocumentElement();
			}
			Element result = createElement(xp, name, reporter, el, stdout);
			return result;
		} else {
			xd.setStdOut(stdout);
			xd.xparse(data, reporter);
			return xd.getElement();
		}
	}

	final public boolean test(final String xdef,
		final String data,
		final String name,
		final char mode) {  // 'P' => parse, 'C' => create
		return test(xdef, data, name, mode, data, "");
	}

	final public boolean test(final String xdef,
		final String data,
		final String name,
		final char mode,  // 'P' => parse, 'C' => create
		final String result,
		final String stdout,
		final Class<?>... exts) {
		return test(new String[]{xdef}, data, name, mode, result, stdout, exts);
	}

	final public boolean test(final String[] xdef,
		final String data,
		final String name,
		final char mode,  // 'P' => parse, 'C' => create
		final String result,
		final String stdout,
		final Class<?>... exts) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			for (String x: xdef) {
				_xdOfxd.createXDDocument().xparse(x, null);
			}
		}
		boolean error = false;
		System.err.flush();
		System.out.flush();
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ArrayReporter reporter = new ArrayReporter();
			Element el = test(xdef, data, name, bos, reporter, mode, exts);
			if (reporter.errors()) {
				error = true;
				ReportPrinter.printListing(System.out, data, reporter, true);
			}
			if (result == null) {
				if (el != null) {
					error = true;
					System.err.println("Fails expected null, got:");
					System.err.println(KXmlUtils.nodeToString(el, true));
				}
			} else {
				if (el == null) {
					error = true;
					System.err.println("Fails! Expected:\n"
						+ result + "\n got null");
				} else {
					Element expected =
						KXmlUtils.parseXml(result).getDocumentElement();
					ReportWriter rw =
						KXmlUtils.compareElements(el, expected);
					if (rw.errorWarnings()) {
						error = true;
						rw.getReportReader().printReports(System.err);
						System.err.println("Fails! Expected:\n"
							+ result + "\n got:\n"
							+ KXmlUtils.nodeToString(el, false));
					}
				}
			}
			String s = bos.toString("UTF-8");
			if (!s.equals(stdout)) {
				error = true;
				System.err.println("========== Standard output ===========\n");
				System.err.println("Len expected: " + stdout.length()
					 + ", len returned: " + s.length());
				System.err.println("Incorrect Output! Expected:\n" + stdout
					+ "\ngot:\n" + s);
			}
		} catch (Exception ex) {
			error = true;
			fail(ex);
		}
		System.err.flush();
		System.out.flush();
		return error;
	}

	final public String getListing(final ReportReader r,
		final String xdef) {
		if (xdef.charAt(0) == '<') {
			return ReportPrinter.printListing(xdef, r);
		} else {
			try {
				return "File: " + xdef + "\n" + ReportPrinter.printListing(
					FUtils.readString(new File(xdef)), r);
			} catch (Exception ex) {
				return "File: " + xdef + "\n" + ex;
			}
		}
	}

	final public XDPool compile(final InputStream source,
		final String pathname,
		final Class<?>... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			_xdOfxd.createXDDocument().xparse(source, null);
		}
		return checkExtObjects(XDFactory.compileXD(
			_props, source, pathname, obj));
	}

	final public XDPool compile(final InputStream[] sources,
		final String[] pathnames,
		final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(
			_props, sources, pathnames, obj));
	}

	final public XDPool compile(final URL[] source, final Class<?>... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			for (URL xdef: source) {
				_xdOfxd.createXDDocument().xparse(xdef, null);
			}
		}
		return checkExtObjects(XDFactory.compileXD(_props, source, obj));
	}

	final public XDPool compile(final URL source, final Class<?>... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			_xdOfxd.createXDDocument().xparse(source, null);
		}
		return checkExtObjects(XDFactory.compileXD(	_props, source, obj));
	}


	final public XDPool compile(final File[] sources, final Class... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			for (File xdef: sources) {
				_xdOfxd.createXDDocument().xparse(xdef, null);
			}
		}
		return checkExtObjects(XDFactory.compileXD(_props, sources, obj));
	}

	final public XDPool compile(final File source, final Class... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef", obj);
			}
			_xdOfxd.createXDDocument().xparse(source, null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, source, obj));
	}

	final public XDPool compile(final String xdef, final Class<?>... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			_xdOfxd.createXDDocument().xparse(xdef, null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdef, obj));
	}


	final public XDPool compile(String[] xdefs, final Class<?>... obj) {
		if (chkXDSyntax) {
			if (_xdOfxd == null) {
				_xdOfxd = XDFactory.compileXD(null,
					"test/test/xdef/data/test/TestXdefOfXdef.xdef");
			}
			for (String xdef: xdefs) {
				_xdOfxd.createXDDocument().xparse(xdef, null);
			}
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdefs, obj));
	}

	final public String createListnig(final String data,
		final ArrayReporter reporter) {
		if (!reporter.errorWarnings()) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		ReportPrinter.printListing(out, data, reporter, true);
		out.close();
		return sw.toString();
	}

	final public Element create(final XDPool xp,
		final String defName,
		final ReportWriter reporter,
		final String xml) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element el = KXmlUtils.parseXml(xml).getDocumentElement();
		xd.setXDContext(el);
		Element result = xd.xcreate(
			new QName(el.getNamespaceURI(), el.getNodeName()), reporter);
		return result;
	}

	final public Element create(final XDPool xp,
		final String defName,
		final String name,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xcreate(name, reporter);
		return result;
	}

	final public Element create(final XDPool xp,
		final String defName,
		final QName qname,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xcreate(qname, reporter);
		return result;
	}

	final public Element create(final String xdef,
		final String defName,
		final QName qname,
		final ReportWriter reporter) {
		return create(compile(xdef), defName, qname, reporter);
	}

	final public Element create(final XDPool xp,
		final String defName,
		final Element el,
		final String name) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (el != null) {
			xd.setXDContext(el);
		}
		Element result = (el != null && (name == null || name.length() == 0))
			? xd.xcreate(
				new QName(el.getNamespaceURI(), el.getTagName()), null)
			: xd.xcreate(name, null);
		return result;
	}

	final public Element create(final String xdef,
		final String defName,
		final Element el,
		final String name) {
		return create(compile(xdef), defName, el, name);
	}

	/**
	 Compose a new XML document from the specified data.
	 * @param xdef the X-Definition as source for data construction.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as X-Definition context or null.
	 * @param name root element name of the constructed XML document.
	 * @param param global parameter in X-Script or null.
	 * @param obj the value of the global parameter or null.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef,
		final String defName,
		final Element el,
		final String name,
		final String param,
		final Object obj,
		final ArrayReporter reporter) {
		return create(compile(xdef), defName, el, name, param, obj, reporter);
	}

	/**
	 Compose a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as X-Definition context or null.
	 * @param name root element name of the constructed XML document.
	 * @param param global parameter in X-Script or null.
	 * @param obj the value of the global parameter or null.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final Element el,
		final String name,
		final String param,
		final Object obj,
		final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (el != null) {
			xd.setXDContext(el);
		}
		if(param != null) {
			xd.setVariable(param, obj);
		}
		Element result = (el != null &&
			(name == null || name.length() == 0) && reporter != null)
			? xd.xcreate(
				new QName(el.getNamespaceURI(), el.getTagName()), reporter)
			: xd.xcreate(name, null);
		return result;
	}
	final public Element create(final XDPool xp,
		final String xdName,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter strw,
		final Object userObj) {
		XDDocument xd = xp.createXDDocument(xdName);
		xd.setProperties(_props);
		return create(xd, qname, reporter, xml, strw, userObj);
	}
	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter strw,
		final Object userObj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDOutput out = null;
		if (strw != null) {
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
		}
		if (xml != null && xml.length() > 0) {
			xd.setXDContext(xml);
		}
		if (userObj != null) {
			xd.setUserObject(userObj);
		}
		Element result = xd.xcreate(qname, reporter);
		if (out != null) {
			out.close();
		}
		return result;
	}

	final public Element create(final String xdef,
		final String xdName,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter strw,
		final Object userObj) {
		return create(compile(xdef),
			xdName, qname, reporter, xml, strw, userObj);
	}

	final public Element create(final XDPool xp,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter strw,
		final Object userObj) {
		return create(xp, xdName, new QName(name),reporter, xml, strw, userObj);
	}

	final public Element create(final String xdef,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter strw,
		final Object userObj) {
		return create(compile(xdef), xdName, name, reporter, xml, strw,userObj);
	}

	final public Element create(final XDPool xp,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xp, xdName, name, reporter, xml, null, null);
	}

	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter) {
		return create(xd, new QName(name), reporter, null, null, null);
	}

	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter) {
		return create(xd, qname, reporter, null, null, null);
	}

	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xd, new QName(name), reporter, xml, null, null);
	}

	final public Element create(final String xdef,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xdef, xdName, name, reporter, xml, null, null);
	}

	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter,
		final String xml) {
		return create(xd, qname, reporter, xml, null, null);
	}

	final public Element parse(final XDPool xp,
		final String defName,
		final String xml,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(xml, reporter);
		return result;
	}

	final public Element parse(final String xdef,
		final String defName,
		final String xml,
		final ReportWriter reporter) {
		return parse(compile(xdef), defName, xml, reporter);
	}

	final public Element parse(final XDPool xp,
		final String defName,
		final Element el,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(el, reporter);
		return result;
	}

	final public Element parse(final String xdef,
		final String defName,
		final Element el,
		final ReportWriter reporter) {
		return parse(compile(xdef), defName, el, reporter);
	}

	final public Element parse(final XDPool xp,
		final String defName,
		final Element el) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(el, null);
		return result;
	}

	final public Element parse(final String xdef,
		final String defName,
		final Element el) {
		return parse(compile(xdef), defName, el);
	}

	final public Element parse(final XDPool xp,
		final String defName,
		final String xml) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(xml, null);
		return result;
	}

	final public Element parse(final String xdef,
		final String defName,
		final String xml) {
		return parse(compile(xdef), defName, xml);
	}

	final public Element parse(final XDPool xp,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final Object obj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (obj != null) {
			xd.setUserObject(obj);
		}
		Element result = xd.xparse(xml, reporter);
		return result;
	}

	final public Element parse(final String xdef,
		final String defName,
		final ArrayReporter reporter,
		final String xml,
		final Object obj) {
		return parse(compile(xdef), defName, xml, reporter, obj);
	}

	final public Element parse(final XDPool xp,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter strw,
		final Object input,
		final Object obj) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (strw != null) {
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
		}
		return parse(xd, xml, reporter, strw, input, obj);
	}

	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter) {
		return parse(xd, xml, reporter, null, null, null);
	}

	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter strw) {
		return parse(xd, xml, reporter, strw, null, null);
	}

	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter strw,
		final Object obj) {
		return parse(xd, xml, reporter, strw, null, obj);
	}

	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter strw,
		final Object input,
		final Object obj) {
		if (reporter != null) {
			reporter.clear();
		}
		if (input != null) {
			if (input instanceof String) {
				xd.setStdIn(XDFactory.createXDInput(
					new ByteArrayInputStream(((String)input).getBytes()),
					false));
			} else if (input instanceof InputStreamReader) {
				xd.setStdIn(XDFactory.createXDInput(
					(InputStreamReader) input, false));
			} else if (input instanceof InputStream) {
				xd.setStdIn(XDFactory.createXDInput(
					(InputStream) input, false));
			}
		}
		if (obj != null) {
			xd.setUserObject(obj);
		}
		xd.xparse(xml, reporter);
		if (strw != null) {
			try {
				strw.close();
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}
		Element result = xd.getElement();
		return result;
	}

	final public Element parse(final String xdef,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter strw,
		final Object input,
		final Object obj) {
		return parse(compile(xdef), defName, xml, reporter, strw, input, obj);
	}

	final public void printReports(final ReportReader reporter,
		final String data) {
		System.out.flush();
		if (data.charAt(0) == '<') {
			ReportPrinter.printListing(System.err, data, reporter, true);
		} else {
			try {
				ReportPrinter.printListing(System.err,
					new FileReader(data), reporter, true);
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
		System.err.flush();
	}

////////////////////////////////////////////////////////////////////////////////

	public final XComponent parseXC(final XDPool xp,
		final String name,
		final String xml,
		final Class clazz,
		final ArrayReporter reporter) {
		return parseXC(xp.createXDDocument(name), xml, clazz, reporter);
	}

	public final XComponent parseXC(final XDPool xp,
		final String name,
		final Element el,
		final Class clazz,
		final ArrayReporter reporter) {
		return parseXC(xp.createXDDocument(name), el, clazz, reporter);
	}

	public final XComponent parseXC(final XDDocument xd,
		final String xml,
		final Class clazz,
		final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		xd.setProperties(_props);
		return xd.parseXComponent(xml, clazz, reporter);
	}

	public final static XComponent parseXC(final XDDocument xd,
		final Element el,
		final Class clazz,
		final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		return xd.parseXComponent(el, clazz, reporter);
	}

}
