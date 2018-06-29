/*
 * File: CodeExtMethod.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.XDValue;
import java.lang.reflect.Method;

/** Implementation of code of an external methods.
 * @author Vaclav Trojan
 */
public class CodeExtMethod extends CodeI1 {

	/** Method name. */
	private final String _name;
	/** External method pointer. */
	private final Method _extMethod;

	/** Creates a new instance of CodeExtMethod
	 * @param name The name of method.
	 * @param resultType The type of result.
	 * @param code The code.
	 * @param numParam The number of parameters.
	 * @param extMethod The method.
	 */
	public CodeExtMethod(final String name,
		final short resultType,
		final short code,
		final int numParam,
		final Method extMethod) {
		super(resultType, code, numParam);
		_name = name.intern();
		_extMethod = extMethod;
	}

	public final String getName() {return _name;}

	public final Method getExtMethod() {return _extMethod;}

	@Override
	/** Get string representation of the object.
	 * @return The string representation of the object.
	 */
	public String toString() {return _name + "(" + _param + ")";}

	@Override
	/** Compare this object with other CodeItem.
	 * @param o other object to be compared.
	 * @return true if both objects are equal.
	 */
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof CodeExtMethod)) {
			return false;
		}
		CodeExtMethod x = (CodeExtMethod) o;
		if (getCode() != x.getCode() ||	getParam() != x.getParam() ||
			_resultType != x.getItemId()) {
			return false;
		}
		if (!_name.equals(x._name)) {
			return false;
		}
		return _extMethod.equals(x._extMethod);
	}
}
