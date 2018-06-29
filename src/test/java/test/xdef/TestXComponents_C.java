package test.xdef;

import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.proc.XXData;

public class TestXComponents_C {

	private int _n = 0;

	public static void test(XXData a) {
		if ("Novak".equals(a.getTextValue())) {
			XComponent x = a.getRootXXElement().getXComponent();
			if (x instanceof TestXComponents_C) {
				((TestXComponents_C) x)._n++;
			}
		}
	}

	public int getTest() {return _n;}

}