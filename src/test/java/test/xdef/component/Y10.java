// This file was generated by org.xdef.component.GenXComponent.
// XDPosition: "Y10#A".
// Any modifications to this file will be lost upon recompilation.
package test.xdef.component;
public class Y10 implements org.xdef.component.XComponent{
  public String geta() {return _a;}
  public String getb() {return _b;}
  public String getc() {return _c;}
  public Y10p getp() {return _p;}
  public Y10q getq() {return _q;}
  public void seta(String x){
_a=x;}
  public void setb(String x){
_b=x;}
  public void setc(String x){
_c=x;}
  public void setp(Y10p x){
_p=x;}
  public void setq(Y10q x){
_q=x;}
  public String xposOfa(){return XD_XPos+"/@a";}
  public String xposOfb(){return XD_XPos+"/@b";}
  public String xposOfc(){return XD_XPos+"/@c";}
//<editor-fold defaultstate="collapsed" desc="Implementation of XComponent interface">
  public final static byte JSON = 0;
  @Override
  public org.w3c.dom.Element toXml()
    {return (org.w3c.dom.Element) toXml((org.w3c.dom.Document) null);}
  @Override
  public String xGetNodeName() {return XD_NodeName;}
  @Override
  public void xInit(org.xdef.component.XComponent p,
    String name, String ns, String xdPos) {
    XD_Parent=p; XD_NodeName=name; XD_NamespaceURI=ns; XD_Model=xdPos;
  }
  @Override
  public String xGetNamespaceURI() {return XD_NamespaceURI;}
  @Override
  public String xGetXPos() {return XD_XPos;}
  @Override
  public void xSetXPos(String xpos){XD_XPos = xpos;}
  @Override
  public int xGetNodeIndex() {return XD_Index;}
  @Override
  public void xSetNodeIndex(int index) {XD_Index = index;}
  @Override
  public org.xdef.component.XComponent xGetParent() {return XD_Parent;}
  @Override
  public Object xGetObject() {return XD_Object;}
  @Override
  public void xSetObject(final Object obj) {XD_Object = obj;}
  @Override
  public String toString() {return "XComponent: "+xGetModelPosition();}
  @Override
  public String xGetModelPosition() {return XD_Model;}
  @Override
  public int xGetModelIndex() {return -1;}
  @Override
  public org.w3c.dom.Node toXml(org.w3c.dom.Document doc) {
    org.w3c.dom.Element el;
    if (doc==null) {
      doc = org.xdef.xml.KXmlUtils.newDocument(XD_NamespaceURI,
        XD_NodeName, null);
      el = doc.getDocumentElement();
    } else {
      el = doc.createElementNS(XD_NamespaceURI, XD_NodeName);
      if (doc.getDocumentElement()==null) doc.appendChild(el);
    }
    if (geta() != null)
      el.setAttribute(XD_Name_a, geta());
    if (getb() != null)
      el.setAttribute(XD_Name_b, getb());
    if (getc() != null)
      el.setAttribute(XD_Name_c, getc());
    for (org.xdef.component.XComponent x: xGetNodeList())
      el.appendChild(x.toXml(doc));
    return el;
  }
  @Override
  public Object toJson() {return org.xdef.json.JsonUtil.xmlToJson(toXml());}
  @Override
  public java.util.List<org.xdef.component.XComponent> xGetNodeList() {
    java.util.List<org.xdef.component.XComponent> a=
      new java.util.ArrayList<org.xdef.component.XComponent>();
    org.xdef.component.XComponentUtil.addXC(a, getp());
    org.xdef.component.XComponentUtil.addXC(a, getq());
    return a;
  }
  public Y10() {}
  public Y10(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public Y10(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"33377B941B1C2E1BD10C1867872051E5".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_a="a";
  private String _a;
  private String XD_Name_b="b";
  private String _b;
  private String XD_Name_c="c";
  private String _c;
  private Y10p _p;
  private Y10q _q;
  public static final String XD_NAME="A";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "A";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="Y10#A";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult) {
    if (x.getXMNode().getXDPosition().endsWith("/@a")) {
      XD_Name_a = x.getNodeName();
      seta(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@b")) {
      XD_Name_b = x.getNodeName();
      setb(parseResult.getParsedValue().toString());
    } else {
      XD_Name_c = x.getNodeName();
      setc(parseResult.getParsedValue().toString());
    }
  }
  @Override
  public org.xdef.component.XComponent xCreateXChild(
    org.xdef.proc.XXNode x) {
    String s = x.getXMElement().getXDPosition();
    if ("Y10#A/$mixed/a".equals(s))
      return new test.xdef.component.Y10p(this, x);
    return new test.xdef.component.Y10q(this, x); // Y10#A/$mixed/b
  }
  @Override
  public void xAddXChild(org.xdef.component.XComponent x){
    x.xSetNodeIndex(XD_ndx++);
    String s = x.xGetModelPosition();
    if ("Y10#A/$mixed/a".equals(s))
      setp((test.xdef.component.Y10p)x);
    else
      setq((test.xdef.component.Y10q)x); //Y10#A/$mixed/b
  }
  @Override
  public void xSetAny(org.w3c.dom.Element el) {}
// </editor-fold>
}