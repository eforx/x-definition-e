// This file was generated by org.xdef.component.GenXComponent.
// XDPosition: "SouborD1A#Adresa".
// Any modifications to this file will be lost upon recompilation.
package test.xdef.component;
public class Z9 implements org.xdef.component.XComponent{
  public String getUlice() {return _Ulice;}
  public String getCisloOrientacni() {return _CisloOrientacni;}
  public String getCisloPopisne() {return _CisloPopisne;}
  public String getObec() {return _Obec;}
  public String getPSC() {return _PSC;}
  public String getOkres() {return _Okres;}
  public String getStat() {return _Stat;}
  public String getTelefon1() {return _Telefon1;}
  public String getTelefon2() {return _Telefon2;}
  public void setUlice(String x){_Ulice=x;}
  public void setCisloOrientacni(String x){_CisloOrientacni=x;}
  public void setCisloPopisne(String x){_CisloPopisne=x;}
  public void setObec(String x){_Obec=x;}
  public void setPSC(String x){_PSC=x;}
  public void setOkres(String x){_Okres=x;}
  public void setStat(String x){_Stat=x;}
  public void setTelefon1(String x){_Telefon1=x;}
  public void setTelefon2(String x){_Telefon2=x;}
  public String xposOfUlice(){return XD_XPos+"/@Ulice";}
  public String xposOfCisloOrientacni(){return XD_XPos+"/@CisloOrientacni";}
  public String xposOfCisloPopisne(){return XD_XPos+"/@CisloPopisne";}
  public String xposOfObec(){return XD_XPos+"/@Obec";}
  public String xposOfPSC(){return XD_XPos+"/@PSC";}
  public String xposOfOkres(){return XD_XPos+"/@Okres";}
  public String xposOfStat(){return XD_XPos+"/@Stat";}
  public String xposOfTelefon1(){return XD_XPos+"/@Telefon1";}
  public String xposOfTelefon2(){return XD_XPos+"/@Telefon2";}
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
    if (getUlice() != null)
      el.setAttribute(XD_Name_Ulice, getUlice());
    if (getCisloOrientacni() != null)
      el.setAttribute(XD_Name_CisloOrientacni, getCisloOrientacni());
    if (getCisloPopisne() != null)
      el.setAttribute(XD_Name_CisloPopisne, getCisloPopisne());
    if (getObec() != null)
      el.setAttribute(XD_Name_Obec, getObec());
    if (getPSC() != null)
      el.setAttribute(XD_Name_PSC, getPSC());
    if (getOkres() != null)
      el.setAttribute(XD_Name_Okres, getOkres());
    if (getStat() != null)
      el.setAttribute(XD_Name_Stat, getStat());
    if (getTelefon1() != null)
      el.setAttribute(XD_Name_Telefon1, getTelefon1());
    if (getTelefon2() != null)
      el.setAttribute(XD_Name_Telefon2, getTelefon2());
    return el;
  }
  @Override
  public Object toJson() {return org.xdef.json.JsonUtil.xmlToJson(toXml());}
  @Override
  public java.util.List<org.xdef.component.XComponent> xGetNodeList() {
    return new java.util.ArrayList<org.xdef.component.XComponent>();
  }
  public Z9() {}
  public Z9(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public Z9(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"7070EC0A858206E367DABF38B96DB80C".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_Ulice="Ulice";
  private String _Ulice;
  private String XD_Name_CisloOrientacni="CisloOrientacni";
  private String _CisloOrientacni;
  private String XD_Name_CisloPopisne="CisloPopisne";
  private String _CisloPopisne;
  private String XD_Name_Obec="Obec";
  private String _Obec;
  private String XD_Name_PSC="PSC";
  private String _PSC;
  private String XD_Name_Okres="Okres";
  private String _Okres;
  private String XD_Name_Stat="Stat";
  private String _Stat;
  private String XD_Name_Telefon1="Telefon1";
  private String _Telefon1;
  private String XD_Name_Telefon2="Telefon2";
  private String _Telefon2;
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "Adresa";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private String XD_XPos;
  private String XD_Model="SouborD1A#Adresa";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult) {
    if (x.getXMNode().getXDPosition().endsWith("/@Ulice")) {
      XD_Name_Ulice = x.getNodeName();
      setUlice(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@CisloOrientacni")) {
      XD_Name_CisloOrientacni = x.getNodeName();
      setCisloOrientacni(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@CisloPopisne")) {
      XD_Name_CisloPopisne = x.getNodeName();
      setCisloPopisne(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@Obec")) {
      XD_Name_Obec = x.getNodeName();
      setObec(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@PSC")) {
      XD_Name_PSC = x.getNodeName();
      setPSC(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@Okres")) {
      XD_Name_Okres = x.getNodeName();
      setOkres(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@Stat")) {
      XD_Name_Stat = x.getNodeName();
      setStat(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@Telefon1")) {
      XD_Name_Telefon1 = x.getNodeName();
      setTelefon1(parseResult.getParsedValue().toString());
    } else {
      XD_Name_Telefon2 = x.getNodeName();
      setTelefon2(parseResult.getParsedValue().toString());
    }
  }
  @Override
  public org.xdef.component.XComponent xCreateXChild(
    org.xdef.proc.XXNode x)
    {return null;}
  @Override
  public void xAddXChild(org.xdef.component.XComponent x){}
  @Override
  public void xSetAny(org.w3c.dom.Element el) {}
// </editor-fold>
}