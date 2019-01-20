package org.xdef.impl.compile;

import org.xdef.impl.code.CodeI1;
import org.xdef.XDValueID;

/** This class holds boolean jump vector generated by expression. It can be
 * converted to boolean value.
 * @author Vaclav Trojan <vaclav.trojan@syntea.cz>
 */
final class CompileJumpVector {
	private CodeI1[] _falseJumpList;
	private CodeI1[] _trueJumpList;

	/** Check if both the true list and the false list are empty.
	 * @return true if both the true list and the false list are empty.
	 */
	boolean isEmpty() {return _trueJumpList == null && _falseJumpList == null;}

	/** Check if the true list is empty.
	 * @return true if the true list is empty.
	 */
	boolean isTrueJumpListEmpty() {return _trueJumpList == null;}

	/** Check if the false list is empty.
	 * @return true if the false list is empty.
	 */
	boolean isFalseJumpListEmpty() {return _falseJumpList == null;}

	/** Add jump item created from given code to true jump list.
	 * @param code The code of jump item.
	 * @return the CodeI1 added to false list.
	 */
	CodeI1 addJumpItemToTrueList(final short code) {
		CodeI1 item = new CodeI1(XDValueID.XD_VOID, code);
		if (_trueJumpList == null) {
			_trueJumpList = new CodeI1[1];
			_trueJumpList[0] = item;
		} else {
			int len = _trueJumpList.length;
			CodeI1[] x = _trueJumpList;
			_trueJumpList = new CodeI1[len + 1];
			System.arraycopy(x, 0, _trueJumpList, 0, len);
			_trueJumpList[len] = item;
		}
		return item;
	}

	/** Add jump item created from given code to false jump list.
	 * @param code The code of jump item.
	 * @return the CodeI1 added to false list.
	 */
	CodeI1 addJumpItemToFalseList(final short code) {
		CodeI1 item = new CodeI1(XDValueID.XD_VOID, code);
		if (_falseJumpList == null) {
			_falseJumpList = new CodeI1[1];
			_falseJumpList[0] = item;
		} else {
			CodeI1[] x = _falseJumpList;
			int len = x.length;
			_falseJumpList = new CodeI1[len + 1];
			System.arraycopy(x, 0, _falseJumpList, 0, len);
			_falseJumpList[len] = item;
		}
		return item;
	}

	/** Resolve all jump addresses in the true list and clear the list.
	 * @param addr the address to be set to jumps.
	 */
	void resoveTrueJumps(final int addr) {
		if (isTrueJumpListEmpty()) {
			return;
		}
		for (int i = _trueJumpList.length - 1; i >= 0; i--) {
			_trueJumpList[i].setParam(addr);
		}
		_trueJumpList = null;
	}

	/** Resolve all jump addresses in the false list and clear the list.
	 * @param addr the address to be set to jumps.
	 */
	void resoveFalseJumps(final int addr) {
		if (isFalseJumpListEmpty()) {
			return;
		}
		for (int i = _falseJumpList.length - 1; i >= 0; i--) {
			_falseJumpList[i].setParam(addr);
		}
		_falseJumpList = null;
	}

}
