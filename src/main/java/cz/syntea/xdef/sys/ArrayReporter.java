/*
 * Copyright 2007 Syntea software group a.s.
 *
 * File: ArrayReporter.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.sys;

import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

/** Implements both (ReportWriter and ReportReader interfaces) using ArrayList.
 * @author Vaclav Trojan
 */
public class ArrayReporter extends ArrayList<Report>
	implements ReportWriter, ReportReader {

	/** Number of warning reports. */
	private int _warnings;
	/** Number of light errors reports. */
	private int _lightErrors;
	/** Number of error reports. */
	private int _errors;
	/** Number of fatal error reports. */
	private int _fatals;
	/** Index of actual report. */
	private int _index;
	/** Last error report which has been written by put method. */
	private int _lastErrorReport;

	/** Create new empty ArrayReporter. */
	public ArrayReporter() {
		super();
//		_errors = 0;
//		_lastErrors = 0;
//		_warnings = 0;
//		_fatals = 0;
//		_index = 0;
		_lastErrorReport = -1;
	}

	@Override
	/** Set language (ISO-639 or ISO-639-2). This method has no effect here.
	 * @param language language id (ISO-639).
	 */
	public void setLanguage(final String language) {}

	@Override
	/** Appends the specified report to the end of this list.
	 * @param report report to be appended to this list.
	 * @return <tt>true</tt> (as per the general contract of Collection.add).
	 */
	public boolean add(final Report report) {
		putReport(report);
		return true;
	}

	@Override
	/** Appends the specified collection of reports to the end of this list.
	 * @param c collection of reports.
	 * @return <tt>true</tt> if something was added.
	 */
	public boolean addAll(final Collection<? extends Report> c) {
		int len = c.size();
		for (Report r: c) {
			putReport(r);
		}
		return len > 0;
	}

	/** Appends reports from the specified ArrayReporter to this list.
	 * @param ar ArrayReporter with reports.
	 */
	public void addReports(ArrayReporter ar) {
		for (Report r: ar) {
			putReport(r);
		}
	}

	@Override
	/** Put the report to the list.
	 * @param report The report.
	 */
	public void putReport(final Report report) {
		super.add(report);
		switch (report.getType()) {
			case Report.ERROR:
				_errors++;
				_lastErrorReport = _index;
				return;
			case Report.LIGHTERROR:
				_lightErrors++;
				_lastErrorReport = _index;
				return;
			case Report.FATAL:
				_fatals++;
				_lastErrorReport = _index;
				return;
			case Report.WARNING:
				_warnings++;
		}
	}

	/** Remove the report from the list.
	 * @param msg The report.
	 * @return <tt>true</tt> if the object was found and removed, otherwise
	 * return false.
	 */
	public boolean removeReport(final Report msg) {
		if (msg == null || super.isEmpty()) {
			return true;
		}
		int ndx = super.lastIndexOf(msg);
		if (ndx < 0) {
			return false;
		}
		super.remove(ndx);
		Report rep;
		byte type;
		switch (msg.getType()) {
			case Report.ERROR:
				_errors--;
				break;
			case Report.LIGHTERROR:
				_lightErrors--;
				break;
			case Report.FATAL:
				_fatals--;
				break;
			case Report.WARNING:
				_warnings--;
				break;
		}
		if (_lastErrorReport == ndx) {
			while (--ndx >= 0) {
				rep = super.get(ndx);
				type = rep.getType();
				if (type == Report.FATAL || type == Report.ERROR ||
					type == Report.LIGHTERROR) {
					_lastErrorReport = ndx;
					break;
				}
			}
		}
		return true;
	}

	@Override
	/** Write string to reporter.
	 * @param str String to be written.
	 */
	public void writeString(final String str) {
		super.add(Report.string(null, str));
	}

	@Override
	/** Get last error report.
	 * @return last error report (or <tt>null</tt> if last report is not
	 * available).
	 */
	public Report getLastErrorReport() {
		return _lastErrorReport < 0 ? null : get(_lastErrorReport);
	}

	@Override
	/** Clear last error report. If last report has been available it will be
	 * erased (i.e. result of <tt>getLastReport()</tt> will be null. However,
	 * the report has already been written to the report file.
	 */
	public void clearLastErrorReport() {_lastErrorReport = -1;}

	@Override
	/** Clear counters of fatal errors, errors and warnings.
	 */
	public void clearCounters() {
		_errors = 0;
		_warnings = 0;
		_fatals = 0;
	}

	/** Get report from position.
	 * @param index index of report to return.
	 * @return the report at the specified position in this list.
	 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
	 * &lt; 0 || index &gt;= size())</tt>.
	 */
	@Override
	public Report get(int index) {return super.get(index);}

	@Override
	/** Get next report from the list or null.
	 * @return The report or null.
	 */
	public Report getReport() {
		if (_index < super.size()) {
			return super.get(_index++);
		}
		return null;
	}

	@Override
	/** Returns report reader for reading created report file. If reader
	 * can't be created <tt>null</tt> is returned. It is implementation
	 * dependent if writing of reports can continue, rather it closes the
	 * report file and resets it to read mode.
	 * @return The report reader or <tt>null</tt>.
	 */
	public ReportReader getReportReader() {
		_index = 0;
		return this;
	}

	/** Returns report reader for reading from the start point. */
	public void reset() {_index = 0;}

	@Override
	/** Clear all report items. */
	public void clear() {
		super.clear();
		_warnings = 0;
		_errors = 0;
		_lightErrors = 0;
		_fatals = 0;
		_index = 0;
		_lastErrorReport = -1;
	}

	@Override
	/** Get size of the list of reports.
	 * @return Number of report items.
	 */
	public int size() {return super.size();}

	@Override
	/** Put fatal item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final String id, final String msg, final Object... mod) {
		putReport(Report.fatal(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void fatal(long registeredID, Object... mod) {
		putReport(Report.fatal(registeredID, mod));
	}

	@Override
	/** Put error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void error(final String id, final String msg, final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}

	@Override
	/** Put error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(long registeredID, Object... mod) {
		putReport(Report.error(registeredID, mod));
	}

	@Override
	/** Put light error item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void lighterror(final String id,
		final String msg, final Object... mod) {
		putReport(Report.lightError(id, msg, mod));
	}

	@Override
	/** Put light error item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void lightError(long registeredID, Object... mod) {
		putReport(Report.lightError(registeredID, mod));
	}

	@Override
	/** Put warning item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void warning(final String id, final String msg, final Object... mod){
		putReport(Report.warning(id, msg, mod));
	}

	@Override
	/** Put warning item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void warning(long registeredID, Object... mod) {
		putReport(Report.warning(registeredID, mod));
	}

	@Override
	/** Put audit item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void audit(final String id, final String msg, final Object... mod) {
		putReport(Report.audit(id, msg, mod));
	}

	@Override
	/** Put audit item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void audit(long registeredID, Object... mod) {
		putReport(Report.audit(registeredID, mod));
	}

	@Override
	/** Put message item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void message(final String id, final String msg, final Object... mod){
		putReport(Report.message(id, msg, mod));
	}

	@Override
	/** Put message item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void mesage(long registeredID, Object... mod) {
		putReport(Report.message(registeredID, mod));
	}

	@Override
	/** Put info item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void info(final String id, final String msg, final Object... mod) {
		putReport(Report.info(id, msg, mod));
	}

	@Override
	/** Put info item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void info(long registeredID, Object... mod) {
		putReport(Report.info(registeredID, mod));
	}

	@Override
	/** Put text item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void text(final String id, final String msg, final Object... mod) {
		putReport(Report.text(id, msg, mod));
	}

	@Override
	/** Put text item.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void text(long registeredID, Object... mod) {
		putReport(Report.text(registeredID, mod));
	}

	@Override
	/** Put string item.
	 * @param id The report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 */
	public void string(final String id, final String msg, final Object... mod) {
		putReport(Report.string(id, msg, mod));
	}

	@Override
	/** Put string item.
	 * @param registeredID registered report id.
	 * @param mod The modification string of report text.
	 */
	public void string(long registeredID, Object... mod) {
		putReport(Report.string(registeredID, mod));
	}

	@Override
	/** Get number of fatal items.
	 * @return Number of fatal errors.
	 */
	public int getFatalErrorCount() {return _fatals;}

	@Override
	/** Get number of error items.
	 * @return Number of errors.
	 */
	public int getErrorCount() {return _errors + _lightErrors;}

	@Override
	/** Get number of light error items.
	 * @return The number of light errors.
	 */
	public int getLightErrorCount() {return _lightErrors;}

	@Override
	/** Get number of warning items.
	 * @return Number of warnings.
	 */
	public int getWarningCount() {return _warnings;}

	@Override
	/** Check if fatal errors were generated.
	 * @return true is errors reports are present.
	 */
	public boolean fatals() {return _fatals != 0;}

	@Override
	/** Check if errors and/or fatal errors were generated.
	 * @return <i>true</i> if and only if exists fatal errors.
	 */
	public boolean errors() {return (_fatals + _errors + _lightErrors) != 0;}

	@Override
	/** return true if warnings or errors reports are present.
	 * @return <i>true</i> if fatals, errors or warning exists.
	 */
	public boolean errorWarnings() {
		return (_warnings + _lightErrors + _errors + _fatals) != 0;
	}

	@Override
	/** Check if error and warning reports were stored in report writer. Return
	 * normally if in no errors or warnings are found, otherwise throw
	 * exception with the  list of error reports (max. MAX_REPORTS reports).
	 * @throws SRuntimeException if errors or warnings has been generated.
	 */
	public void checkAndThrowErrorWarnings() throws SRuntimeException {
		if (errorWarnings()) {
			throwReports(true);
		}
	}

	@Override
	/** Check error reports stored in report writer. Return normally if
	 * in no errors are found, otherwise throw exception with list of
	 * error reports (max. MAX_REPORTS reports).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public void checkAndThrowErrors() throws SRuntimeException {
		if (errors()) {
			throwReports(false);
		}
	}

	/** Throw runtime exception if reports with errors and (or even warnings)
	 * are present in the report writer.
	 * @param warnings display all warnings messages if this argument is true,
	 * otherwise only errors.
	 * @throws SRuntimeException with reports.
	 */
	private void throwReports(boolean warnings) throws SRuntimeException {
		_index = 0;
		StringBuilder sb = new StringBuilder();
		Report rep;
		for (int i = 0; (rep = getReport()) != null;) {
			if (i >= MAX_REPORTS) {
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(
					Report.text(null, "...").toXmlString(),'"',true));
				sb.append("&}");
				sb.append("\n&{&&");
				// Too many errors
				sb.append(KXmlUtils.toXmlText(Report.error(SYS.SYS013)
					.toXmlString(),'"',true));
				sb.append("&}");
				break;
			} else if (warnings || rep.getType() == Report.ERROR ||
				rep.getType() == Report.LIGHTERROR ||
				rep.getType() == Report.FATAL) {
				i++;
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(rep.toXmlString(),'"',true));
				sb.append("&}");
			}
		}
		_index = 0;
		//Errors detected: &{0}
		throw new SRuntimeException(SYS.SYS012, sb.toString());
	}

	@Override
	/* Close stream - resets intput to start. */
	public void close() {_index = 0;}

	@Override
	/** Flush report writer - nothing to do for ArrayReporter. */
	public void flush() {}

	@Override
	/** Write reports to String (in actual language).
	 * @return the String with reports.
	 */
	public String printToString() {
		return printToString(null);
	}

	@Override
	/** Write reports to String in specified language.
	 * @param language language id (ISO-639).
	 * @return the String with reports.
	 */
	public String printToString(final String language) {
		StringBuilder result = new StringBuilder();
		_index = 0;
		Report rep;
		boolean wasFirst = false;
		while ((rep = getReport()) != null) {
			if (wasFirst) {
				result.append('\n');
			} else {
				wasFirst = true;
			}
			result.append(language == null ?
				rep.toString() : rep.toString(language));
		}
		_index = 0;
		return result.toString();
	}

	@Override
	/** Write reports to output stream.
	 * @param language language id (ISO-639).
	 * @param out The PrintStream where reports are printed.
	 */
	public void printReports(PrintStream out, String language) {
		Report rep;
		_index = 0;
		while ((rep = getReport()) != null) {
			out.println(rep.toString(language));
		}
		_index = 0;
	}

	@Override
	/** Write reports to output stream.
	 * @param out The PrintWriter where reports are printed.
	 */
	public void printReports(final PrintStream out) {
		Report rep;
		_index = 0;
		while ((rep = getReport()) != null) {
			out.println(rep.toString());
		}
		_index = 0;
	}

	@Override
	public String toString() {return printToString();}

}