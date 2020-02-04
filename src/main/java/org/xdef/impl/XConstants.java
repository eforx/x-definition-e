package org.xdef.impl;

/** Internally used constants
 * @author Vaclav Trojan
 */
public interface XConstants {

	/** X-definition version 2.0 ID. */
	public static final byte XD20 = 20;
	/** X-definition version 3.1 ID. */
	public static final byte XD31 = 31;
	/** X-definition version 3.2 ID. */
	public static final byte XD32 = 32;
	/** X-definition version 4.0 ID. */
	public static final byte XD40 = 40;

	/** XML version 1.0. */
	public static final byte XML10 = 10;
	/** XML version 1.1. */
	public static final byte XML11 = 11;

	/** JSON to XML transformation according to W3C specification. */
	public static byte JSON_W3C = 1;
	/** JSON to XML transformation according to X-definition implementation. */
	public static byte JSON_XD = 2;

}