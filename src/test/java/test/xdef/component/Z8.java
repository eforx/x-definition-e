// This file was generated by org.xdef.component.GenXComponent.
// XDPosition: "SouborD1A#ObjStrankaDN".
// Any modifications to this file will be lost upon recompilation.
package test.xdef.component;
public class Z8 implements org.xdef.component.XComponent{
  public org.xdef.sys.SDatetime getDatumCasDN() {return _DatumCasDN;}
  public java.util.Date dateOfDatumCasDN() {
    return org.xdef.sys.SDatetime.getDate(_DatumCasDN);
  }
  public java.sql.Timestamp timestampOfDatumCasDN() {
    return org.xdef.sys.SDatetime.getTimestamp(_DatumCasDN);
  }
  public java.util.Calendar calendarOfDatumCasDN() {
    return org.xdef.sys.SDatetime.getCalendar(_DatumCasDN);
  }
  public org.xdef.sys.SDatetime getDatumCasDoDN() {return _DatumCasDoDN;}
  public java.util.Date dateOfDatumCasDoDN() {
    return org.xdef.sys.SDatetime.getDate(_DatumCasDoDN);
  }
  public java.sql.Timestamp timestampOfDatumCasDoDN() {
    return org.xdef.sys.SDatetime.getTimestamp(_DatumCasDoDN);
  }
  public java.util.Calendar calendarOfDatumCasDoDN() {
    return org.xdef.sys.SDatetime.getCalendar(_DatumCasDoDN);
  }
  public String getKodOkresu() {return _KodOkresu;}
  public String getObec() {return _Obec;}
  public String getUlice() {return _Ulice;}
  public String getCisloPopisne() {return _CisloPopisne;}
  public String getSkodaTisKc() {return _SkodaTisKc;}
  public String getHlavniPricina() {return _HlavniPricina;}
  public String getPricina() {return _Pricina;}
  public Z8.Misto getMisto() {return _Misto;}
  public void setDatumCasDN(org.xdef.sys.SDatetime x) {_DatumCasDN = x;}
  public void setDatumCasDN(java.util.Date x) {
    _DatumCasDN=x==null ? null : new org.xdef.sys.SDatetime(x);
  }
  public void setDatumCasDN(java.sql.Timestamp x) {
    _DatumCasDN=x==null ? null : new org.xdef.sys.SDatetime(x);
  }
  public void setDatumCasDN(java.util.Calendar x) {
    _DatumCasDN=x==null ? null : new org.xdef.sys.SDatetime(x);
  }
  public void setDatumCasDoDN(org.xdef.sys.SDatetime x) {_DatumCasDoDN = x;}
  public void setDatumCasDoDN(java.util.Date x) {
    _DatumCasDoDN=x==null ? null : new org.xdef.sys.SDatetime(x);
  }
  public void setDatumCasDoDN(java.sql.Timestamp x) {
    _DatumCasDoDN=x==null ? null : new org.xdef.sys.SDatetime(x);
  }
  public void setDatumCasDoDN(java.util.Calendar x) {
    _DatumCasDoDN=x==null ? null : new org.xdef.sys.SDatetime(x);
  }
  public void setKodOkresu(String x) {_KodOkresu = x;}
  public void setObec(String x) {_Obec = x;}
  public void setUlice(String x) {_Ulice = x;}
  public void setCisloPopisne(String x) {_CisloPopisne = x;}
  public void setSkodaTisKc(String x) {_SkodaTisKc = x;}
  public void setHlavniPricina(String x) {_HlavniPricina = x;}
  public void setPricina(String x) {_Pricina = x;}
  public void setMisto(Z8.Misto x) {
    if (x!=null && x.xGetXPos() == null)
      x.xInit(this, "Misto", null, "SouborD1A#ObjStrankaDN/Misto");
    _Misto = x;
  }
  public String xposOfDatumCasDN(){return XD_XPos + "/@DatumCasDN";}
  public String xposOfDatumCasDoDN(){return XD_XPos + "/@DatumCasDoDN";}
  public String xposOfKodOkresu(){return XD_XPos + "/@KodOkresu";}
  public String xposOfObec(){return XD_XPos + "/@Obec";}
  public String xposOfUlice(){return XD_XPos + "/@Ulice";}
  public String xposOfCisloPopisne(){return XD_XPos + "/@CisloPopisne";}
  public String xposOfSkodaTisKc(){return XD_XPos + "/@SkodaTisKc";}
  public String xposOfHlavniPricina(){return XD_XPos + "/@HlavniPricina";}
  public String xposOfPricina(){return XD_XPos + "/@Pricina";}
//<editor-fold defaultstate="collapsed" desc="XComponent interface">
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
    if (getDatumCasDN() != null)
      el.setAttribute(XD_Name_DatumCasDN, getDatumCasDN().formatDate("d.M.yyyy H:mm"));
    if (getDatumCasDoDN() != null)
      el.setAttribute(XD_Name_DatumCasDoDN, getDatumCasDoDN().formatDate("d.M.yyyy H:mm"));
    if (getKodOkresu() != null)
      el.setAttribute(XD_Name_KodOkresu, getKodOkresu());
    if (getObec() != null)
      el.setAttribute(XD_Name_Obec, getObec());
    if (getUlice() != null)
      el.setAttribute(XD_Name_Ulice, getUlice());
    if (getCisloPopisne() != null)
      el.setAttribute(XD_Name_CisloPopisne, getCisloPopisne());
    if (getSkodaTisKc() != null)
      el.setAttribute(XD_Name_SkodaTisKc, getSkodaTisKc());
    if (getHlavniPricina() != null)
      el.setAttribute(XD_Name_HlavniPricina, getHlavniPricina());
    if (getPricina() != null)
      el.setAttribute(XD_Name_Pricina, getPricina());
    for (org.xdef.component.XComponent x: xGetNodeList())
      el.appendChild(x.toXml(doc));
    return el;
  }
  @Override
  public java.util.List<org.xdef.component.XComponent> xGetNodeList() {
    java.util.List<org.xdef.component.XComponent> a =
      new java.util.ArrayList<org.xdef.component.XComponent>();
    org.xdef.component.XComponentUtil.addXC(a, getMisto());
    return a;
  }
  public Z8() {}
  public Z8(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public Z8(org.xdef.component.XComponent p, org.xdef.proc.XXNode xx){
    org.w3c.dom.Element el=xx.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=xx.getXPos();
    XD_Model=xx.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"199AB9CCBBED043C8646204196B61E95".equals(
      xx.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_DatumCasDN="DatumCasDN";
  private org.xdef.sys.SDatetime _DatumCasDN;
  private String XD_Name_DatumCasDoDN="DatumCasDoDN";
  private org.xdef.sys.SDatetime _DatumCasDoDN;
  private String XD_Name_KodOkresu="KodOkresu";
  private String _KodOkresu;
  private String XD_Name_Obec="Obec";
  private String _Obec;
  private String XD_Name_Ulice="Ulice";
  private String _Ulice;
  private String XD_Name_CisloPopisne="CisloPopisne";
  private String _CisloPopisne;
  private String XD_Name_SkodaTisKc="SkodaTisKc";
  private String _SkodaTisKc;
  private String XD_Name_HlavniPricina="HlavniPricina";
  private String _HlavniPricina;
  private String XD_Name_Pricina="Pricina";
  private String _Pricina;
  private Z8.Misto _Misto;
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "ObjStrankaDN";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="SouborD1A#ObjStrankaDN";
  @Override
  public void xSetText(org.xdef.proc.XXNode xx,
    org.xdef.XDParseResult parseResult) {}
  @Override
  public void xSetAttr(org.xdef.proc.XXNode xx,
    org.xdef.XDParseResult parseResult) {
    if (xx.getXMNode().getXDPosition().endsWith("/@CisloPopisne")) {
      XD_Name_CisloPopisne = xx.getNodeName();
      setCisloPopisne(parseResult.getParsedValue().stringValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@DatumCasDN")) {
      XD_Name_DatumCasDN = xx.getNodeName();
      setDatumCasDN(parseResult.getParsedValue().datetimeValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@DatumCasDoDN")) {
      XD_Name_DatumCasDoDN = xx.getNodeName();
      setDatumCasDoDN(parseResult.getParsedValue().datetimeValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@HlavniPricina")) {
      XD_Name_HlavniPricina = xx.getNodeName();
      setHlavniPricina(parseResult.getParsedValue().stringValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@KodOkresu")) {
      XD_Name_KodOkresu = xx.getNodeName();
      setKodOkresu(parseResult.getParsedValue().stringValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@Obec")) {
      XD_Name_Obec = xx.getNodeName();
      setObec(parseResult.getParsedValue().stringValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@Pricina")) {
      XD_Name_Pricina = xx.getNodeName();
      setPricina(parseResult.getParsedValue().stringValue());
    } else if (xx.getXMNode().getXDPosition().endsWith("/@SkodaTisKc")) {
      XD_Name_SkodaTisKc = xx.getNodeName();
      setSkodaTisKc(parseResult.getParsedValue().stringValue());
    } else {
      XD_Name_Ulice = xx.getNodeName();
      setUlice(parseResult.getParsedValue().stringValue());
    }
  }
  @Override
  public org.xdef.component.XComponent xCreateXChild(org.xdef.proc.XXNode xx)
    {return new Misto(this, xx);}
  @Override
  public void xAddXChild(org.xdef.component.XComponent xc) {
    xc.xSetNodeIndex(XD_ndx++);
    setMisto((Misto) xc); //SouborD1A#ObjStrankaDN/Misto
  }
  @Override
  public void xSetAny(org.w3c.dom.Element el) {}
// </editor-fold>
public static class Misto implements org.xdef.component.XComponent{
  public String get$value() {return _$value;}
  public void set$value(String x) {_$value = x;}
  public String xposOf$value(){return XD_XPos + "/$text";}
//<editor-fold defaultstate="collapsed" desc="XComponent interface">
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
  public int xGetModelIndex() {return 0;}
  @Override
  public org.w3c.dom.Node toXml(org.w3c.dom.Document doc) {
    org.w3c.dom.Element el;
    if (doc==null) {
      doc = org.xdef.xml.KXmlUtils.newDocument(XD_NamespaceURI,
        XD_NodeName, null);
      el = doc.getDocumentElement();
    } else {
      el = doc.createElementNS(XD_NamespaceURI, XD_NodeName);
    }
    for (org.xdef.component.XComponent x: xGetNodeList())
      el.appendChild(x.toXml(doc));
    return el;
  }
  @Override
  public java.util.List<org.xdef.component.XComponent> xGetNodeList() {
    java.util.ArrayList<org.xdef.component.XComponent> a =
      new java.util.ArrayList<org.xdef.component.XComponent>();
    if (get$value() != null)
      org.xdef.component.XComponentUtil.addText(this,
        "SouborD1A#text/$text", a, get$value(), _$$value);
    return a;
  }
  public Misto() {}
  public Misto(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public Misto(org.xdef.component.XComponent p, org.xdef.proc.XXNode xx){
    org.w3c.dom.Element el=xx.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=xx.getXPos();
    XD_Model=xx.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"0BBC8E2A504A9E2D3C354DD465C51838".equals(
      xx.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String _$value;
  private char _$$value= (char) -1;
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "Misto";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="SouborD1A#ObjStrankaDN/Misto";
  @Override
  public void xSetText(org.xdef.proc.XXNode xx,
    org.xdef.XDParseResult parseResult) {
    _$$value=(char) XD_ndx++;
    set$value(parseResult.getParsedValue().stringValue());
  }
  @Override
  public void xSetAttr(org.xdef.proc.XXNode xx,
    org.xdef.XDParseResult parseResult) {}
  @Override
  public org.xdef.component.XComponent xCreateXChild(org.xdef.proc.XXNode xx)
    {return null;}
  @Override
  public void xAddXChild(org.xdef.component.XComponent xc) {}
  @Override
  public void xSetAny(org.w3c.dom.Element el) {}
// </editor-fold>
}
}