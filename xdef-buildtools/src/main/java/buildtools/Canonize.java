package buildtools;

import java.io.File;

/** Canonize sources.
 * <p>1. Remove all white spaces after last nonblank character
 * at the end of line and replace leading spaces by tabs.</p>
 * <p>2. Check and generate message report classes.</p>
 * @author  Vaclav Trojan
 */
public class Canonize {

	private Canonize() {}

	/** if true the header (copyright) info text is generated from the file. */
	static boolean _hdr = false;
	/** if true the _tail (modification) info text is generated from the file. */
	static boolean _tail = false;

	/** Canonize sources. Remove all trailing white spaces on all lines and
	 * handle with leading spaces an all lines according to argument
	 * <tt>tabs</tt>.
	 * Insert or update header or _tail information to sources
	 * according to value of arguments <tt>_hdr</tt> and <tt>_tail</tt>. If the
	 * argument <tt>recurse</tt> is true, do it with all specified files in
	 * child directories.
	 * @param filename The name of file (wildcards are possible).
	 * @param recurse If <tt>true<tt> then recurse process in child
	 * subdirectories.
	 * @param tabs If <tt>true</tt> then leading spaces are replaced
	 * by the tabelator (4 spaces for 1 tab).
	 * @param hdr If <tt>true</tt> then leading standard copyright information
	 * is inserted before the first line of Java source or it replaces the
	 * existing one. The template for the copyright information is taken from
	 * the file <tt>hdrinfo.txt</tt>the root directory <tt>java</tt> (under
	 * which are projects).If the argument's value is <tt>false</tt> then
	 * the top of source remains unchanged.
	 * @param tail If <tt>true</tt> then log information is added after the last
	 * line of Java source or it replaces the existing one.The template used for
	 * the log information is taken from the file <tt>tailinfo.txt</tt> in the
	 * root directory <tt>java</tt> (under which are projects). If the value
	 * of this argument is <tt>false</tt> then the end source remains unchanged.
	 */
	private static void doSources(final String filename,
		final boolean recurse,
		final boolean tabs) {
		try {
			File f = new File(filename).getCanonicalFile();
			String home = f.getAbsolutePath().replace('\\', '/');
			if (!home.endsWith("/")) {
				home += '/';
			}
			if (home.endsWith("/data/")) {
				return; //do not process data directories
			}

			String hdrTemplate = null;
			String tailTemplate = null;
			System.out.println("Directory: " + home);
			CanonizeSource.canonize(home + "*.java",
				recurse,
				tabs,
				tabs ? 4 : 2,
				hdrTemplate, tailTemplate, GenConstants.JAVA_SOURCE_CHARSET);
			CanonizeSource.canonize(home + "*.xml",
				recurse,
				false,
				tabs ? 4 : 2,
				null, null, GenConstants.JAVA_SOURCE_CHARSET);
			CanonizeSource.canonize(home + "*.html",
				recurse,
				false,
				tabs ? 4 : 2,
				null, null, GenConstants.JAVA_SOURCE_CHARSET);
			CanonizeSource.canonize(home + "*.xdef",
				recurse,
				false,
				tabs ? 4 : 2,
				null, null, GenConstants.JAVA_SOURCE_CHARSET);
			CanonizeSource.canonize(home + "*.properties",
				recurse,
				false,
				tabs ? 4 : 2,
				null, null, GenConstants.JAVA_SOURCE_CHARSET);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Canonize sources.
	 * @param args array with command line parameters.
	 */
	public static void main(String[] args) {
		_hdr = false;
		_tail = false;
		String projectBase;
		try {
			projectBase = new File(".").getCanonicalPath().replace('\\', '/');
			
		} catch (Exception ex) {
			throw new RuntimeException("Can't find project base directory");
		}
		int i = projectBase.lastIndexOf('/');
		if (i < 0) {
			throw new RuntimeException("Unknown build structure");
		}
		// Java source files: recurse directories, the second parameter is true.
		doSources("../xdef/src/main/java/org", true, true);
		doSources("../xdef/src/main/resources/org", true, true);
		doSources("../xdef-test/src/test/java", false, true);
		doSources("../xdef-test/src/test/resources", false, true);
		doSources("src/main/java/buildtools", true, true); //this directory
		doSources("../xdef-example/examples", true, true); //this directory
		GenReportTables.main();
	}

}