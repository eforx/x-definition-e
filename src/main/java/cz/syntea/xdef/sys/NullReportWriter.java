/*
 * Copyright 2007 Syntea software group a.s.
 *
 * File: NullReportWriter.java
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

/** Implementation of ReportWriter to null stream.
 *
 * @author  Vaclav Trojan
 */
public class NullReportWriter implements ReportWriter {

	/** number of warning messages. */
	private int _warnings;
	/** Number of light errors reports. */
	private int _lightErrors;
	/** number of error messages. */
	private int _errors;
	/** number of fatal error messages. */
	private int _fatals;
	/** total number of messages. */
	private int _size;
	/** Last error report which has been written by put method. */
	private Report _lastErrorReport;
	/** Switch whether errors, fatal errors or light errors throws exception.*/
	public boolean _throwException;
	/** Switch if report messages will be included to a thrown message. */

	/** Creates a new instance of NulReportWriter
	 * @param throwException if true the <tt>putReport</tt> method will
	 * throw this exception if the message is light error, error or fatal error.
	 * Otherwise the last error message is just stored to memory.
	 */
	public NullReportWriter(final boolean throwException) {
		_throwException = throwException;
	}

	@Override
	/** Set language (ISO-639 or ISO-639-2). This method has no effect here.
	 * @param language language id (ISO-639).
	 */
	public void setLanguage(final String language) {}

	@Override
	/** Clear the report file - ignored for this implementation. */
	public void clear() {
		clearCounters();
		_size = 0;
		_lastErrorReport = null;
	}

	@Override
	/** Clear counters of fatal errors, errors and warnings. */
	public void clearCounters() {
		_errors = 0;
		_lightErrors = 0;
		_warnings = 0;
		_fatals = 0;
	}

	@Override
	/** Check if warnings and/or errors and/or fatal errors were generated.
	 * @return true is warnings or errors reports are present.
	 */
	public boolean errorWarnings() {
		return _fatals + _errors + _lightErrors + _warnings != 0;
	}

	@Override
	/** Check if errors and/or fatal errors were generated.
	 * @return true is errors reports are present.
	 */
	public boolean errors() {return _fatals + _errors + _lightErrors != 0;}

	@Override
	/** Check if fatal errors were generated.
	 * @return true is errors reports are present.
	 */
	public boolean fatals() {return _fatals != 0;}

	@Override
	/** Get number of error items.
	 * @return The number of generated errors.
	 */
	public int getErrorCount() {return _lightErrors + _errors;}

	@Override
	/** Get number of light error items.
	 * @return The number of light errors.
	 */
	public int getLightErrorCount() {return _lightErrors;}

	@Override
	/** Get number of fatal items.
	 * @return The number of generated fatal errors.
	 */
	public int getFatalErrorCount() {return _fatals;}

	@Override
	/** Get last error report.
	 * @return last error report (or <tt>null</tt> if last report is not
	 * available).
	 */
   public Report getLastErrorReport() {return _lastErrorReport;}

	@Override
	/** Clear last error report. If last report has been available it will be
	 * erased (i.e. result of <tt>getLastReport()</tt> will be null. However,
	 * the report has already been written to the report file.
	 */
	public void clearLastErrorReport() {_lastErrorReport = null;}

	@Override
	/** Returns <tt>null</tt> for this implementation.
	 *
	 * @return The value <tt>null</tt>.
	 */
	public ReportReader getReportReader() {return null;}

	@Override
	/** Get number of warning items.
	 * @return The number of generated warnings.
	 */
	public int getWarningCount() {return _warnings;}

	@Override
	/** Put the report - ignored if the type of msg argument is not error,
	 * fatal error, light error or warning. If the type of msg argument is one
	 * of error, fatal error or light error the msg is stored to memory as
	 * the last error and if the switch _throwException is set on.
	 * @param msg The report.
	 * @throws SRuntimeException with message if msg is ERROR, LIGHT or FATAL.
	 */
	public void putReport(final Report msg) {
		_size++;
		switch (msg.getType()) {
			case Report.FATAL:
				_fatals++;
				_lastErrorReport = msg;
				if (_throwException) {
					throw new SRuntimeException(msg);
				} else {
				   return;
				}
			case Report.ERROR:
				_errors++;
				_lastErrorReport = msg;
				if (_throwException) {
					throw new SRuntimeException(msg);
				} else {
					return;
				}
			case Report.LIGHTERROR:
				_lightErrors++;
				_lastErrorReport = msg;
				return;
			case Report.WARNING:
				_warnings++;
		}
	}

	@Override
	/** Get total number of reports.
	 * @return The number of generated reports.
	 */
	public int size() {return _size;}

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
	 * @param mod Message modification parameters.
	 */
	public void string(long registeredID, Object... mod) {
		putReport(Report.string(registeredID, mod));
	}

	@Override
	/** Write string to reporter  - ignored for this implementation.
	 * @param str String to be written.
	 */
	public void writeString(final String str) {}

	@Override
	/** Check error reports are present in the report writer. Return normally if
	 * in no errors are found, otherwise throw exception with list of
	 * error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors has been generated.
	 */
	public void checkAndThrowErrors() throws SRuntimeException {
		if (errors()) {
			throwReports(false);
		}
	}

	@Override
	/** Check if error and/or warning reports  are present in the report writer.
	 * Return normally if in no errors or warnings are found, otherwise throw
	 * exception with the  list of error messages (max. MAX_REPORTS messages).
	 * @throws SRuntimeException if errors or warnings has been generated.
	 */
	public void checkAndThrowErrorWarnings() throws SRuntimeException {
		if (errorWarnings()) {
			throwReports(true);
		}
	}

	/** Throw runtime exception if reports with errors and (or even warnings)
	 * are present in the report writer.
	 * @param display all warnings messages if this argument is true,
	 * otherwise display only errors.
	 * @throws SRuntimeException with reports.
	 */
	private void throwReports(boolean warnings) throws SRuntimeException {
		ArrayReporter r = new ArrayReporter();
		r.putReport(getLastErrorReport());
		if (r.isEmpty()) {
			//Can't get report reader from this report writer
			r.putReport(Report.error(SYS.SYS045));
		}
		ReportReader reader = r.getReportReader();
		StringBuilder sb = new StringBuilder();
		Report rep;
		for (int i = 0; (rep=reader.getReport()) != null; i++) {
			if (i >= MAX_REPORTS) {
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(
					Report.text(null, "...").toXmlString(),'"',true));
				sb.append("&}");
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(
					//Too many errors*
					Report.error(SYS.SYS013).toXmlString(),'"',true));
				sb.append("&}");
				break;
			} else {
				sb.append("\n&{&&");
				sb.append(KXmlUtils.toXmlText(rep.toXmlString(),'"',true));
				sb.append("&}");
			}
		}
		//Errors detected: &{0}
		throw new SRuntimeException(SYS.SYS012, sb.toString());
	}

	@Override
	/** Close report writer - ignored for this implementation. */
	public void close() {}

	@Override
	/** Flush report writer - ignored for this implementation. */
	public void flush() {}

}