// This file was generated by org.xdef.component.GenXComponent.
// XDPosition: "SouborD1A#ZaznamPDN".
// Any modifications to this file will be lost upon recompilation.
package test.xdef.component;
public class Z1 implements org.xdef.component.XComponent{
  public String getVerze() {return _Verze;}
  public Integer getSeqRec() {return _SeqRec;}
  public String getKrajPolicie() {return _KrajPolicie;}
  public org.xdef.sys.SDatetime getPlatnostOd() {return _PlatnostOd;}
  public java.util.Date dateOfPlatnostOd(){return org.xdef.sys.SDatetime.getDate(_PlatnostOd);}
  public java.sql.Timestamp timestampOfPlatnostOd(){return org.xdef.sys.SDatetime.getTimestamp(_PlatnostOd);}
  public java.util.Calendar calendarOfPlatnostOd(){return org.xdef.sys.SDatetime.getCalendar(_PlatnostOd);}
  public Z2 getProtokol() {return _Protokol;}
  public Z7 getRozhodnutiDN() {return _RozhodnutiDN;}
  public java.util.List<Z1.VyliceniDN> listOfVyliceniDN() {return _VyliceniDN;}
  public Z8 getObjStranka() {return _ObjStranka;}
  public java.util.List<FotoDN> listOfFoto() {return _Foto;}
  public java.util.List<VozidloDN> listOfVozidlo() {return _Vozidlo;}
  public java.util.List<TramvajDN> listOfTramvaj() {return _Tramvaj;}
  public java.util.List<TrolejbusDN> listOfTrolejbus() {return _Trolejbus;}
  public java.util.List<VlakDN> listOfVlak() {return _Vlak;}
  public java.util.List<PovozDN> listOfPovoz() {return _Povoz;}
  public java.util.List<PredmetDN> listOfPredmet() {return _Predmet;}
  public java.util.List<ZvireDN> listOfZvire() {return _Zvire;}
  public java.util.List<UcastnikDN> listOfUcastnik() {return _Ucastnik;}
  public void setVerze(String x){
_Verze=x;}
  public void setSeqRec(Integer x){
_SeqRec=x;}
  public void setKrajPolicie(String x){
_KrajPolicie=x;}
  public void setPlatnostOd(org.xdef.sys.SDatetime x){
_PlatnostOd=x;}
  public void setPlatnostOd(java.util.Date x){
_PlatnostOd=x==null?null:new org.xdef.sys.SDatetime(x);}
  public void setPlatnostOd(java.sql.Timestamp x){
_PlatnostOd=x==null?null:new org.xdef.sys.SDatetime(x);}
  public void setPlatnostOd(java.util.Calendar x){
_PlatnostOd=x==null?null:new org.xdef.sys.SDatetime(x);}
  public void setProtokol(Z2 x){
    if (x!=null && x.xGetXPos() == null)
      x.xInit(this, "Protokol", null, "SouborD1A#ZaznamPDN/$mixed/Protokol");
    _Protokol=x;
  }
  public void setRozhodnutiDN(Z7 x){
    if (x!=null && x.xGetXPos() == null)
      x.xInit(this, "RozhodnutiDN", null, "SouborD1A#ZaznamPDN/$mixed/RozhodnutiDN");
    _RozhodnutiDN=x;
  }
  public void addVyliceniDN(Z1.VyliceniDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "VyliceniDN", null, "SouborD1A#ZaznamPDN/$mixed/VyliceniDN");
      _VyliceniDN.add(x);
    }

  }
  public void setObjStranka(Z8 x){
    if (x!=null && x.xGetXPos() == null)
      x.xInit(this, "ObjStranka", null, "SouborD1A#ZaznamPDN/$mixed/ObjStranka");
    _ObjStranka=x;
  }
  public void addFoto(FotoDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Foto", null, "SouborD1A#ZaznamPDN/$mixed/Foto");
      _Foto.add(x);
    }

  }
  public void addVozidlo(VozidloDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Vozidlo", null, "SouborD1A#ZaznamPDN/$mixed/Vozidlo");
      _Vozidlo.add(x);
    }

  }
  public void addTramvaj(TramvajDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Tramvaj", null, "SouborD1A#ZaznamPDN/$mixed/Tramvaj");
      _Tramvaj.add(x);
    }

  }
  public void addTrolejbus(TrolejbusDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Trolejbus", null, "SouborD1A#ZaznamPDN/$mixed/Trolejbus");
      _Trolejbus.add(x);
    }

  }
  public void addVlak(VlakDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Vlak", null, "SouborD1A#ZaznamPDN/$mixed/Vlak");
      _Vlak.add(x);
    }

  }
  public void addPovoz(PovozDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Povoz", null, "SouborD1A#ZaznamPDN/$mixed/Povoz");
      _Povoz.add(x);
    }

  }
  public void addPredmet(PredmetDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Predmet", null, "SouborD1A#ZaznamPDN/$mixed/Predmet");
      _Predmet.add(x);
    }

  }
  public void addZvire(ZvireDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Zvire", null, "SouborD1A#ZaznamPDN/$mixed/Zvire");
      _Zvire.add(x);
    }

  }
  public void addUcastnik(UcastnikDN x) {
    if (x!=null) {
        if (x.xGetXPos()==null)
          x.xInit(this, "Ucastnik", null, "SouborD1A#ZaznamPDN/$mixed/Ucastnik");
      _Ucastnik.add(x);
    }

  }
  public String xposOfVerze(){return XD_XPos+"/@Verze";}
  public String xposOfSeqRec(){return XD_XPos+"/@SeqRec";}
  public String xposOfKrajPolicie(){return XD_XPos+"/@KrajPolicie";}
  public String xposOfPlatnostOd(){return XD_XPos+"/@PlatnostOd";}
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
    if (getVerze() != null)
      el.setAttribute(XD_Name_Verze, getVerze());
    if (getSeqRec() != null)
      el.setAttribute(XD_Name_SeqRec, String.valueOf(getSeqRec()));
    if (getKrajPolicie() != null)
      el.setAttribute(XD_Name_KrajPolicie, getKrajPolicie());
    if (getPlatnostOd() != null)
      el.setAttribute(XD_Name_PlatnostOd, getPlatnostOd().formatDate("d.M.yyyy H:mm"));
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
    org.xdef.component.XComponentUtil.addXC(a, getProtokol());
    org.xdef.component.XComponentUtil.addXC(a, getRozhodnutiDN());
    org.xdef.component.XComponentUtil.addXC(a, listOfVyliceniDN());
    org.xdef.component.XComponentUtil.addXC(a, getObjStranka());
    org.xdef.component.XComponentUtil.addXC(a, listOfFoto());
    org.xdef.component.XComponentUtil.addXC(a, listOfVozidlo());
    org.xdef.component.XComponentUtil.addXC(a, listOfTramvaj());
    org.xdef.component.XComponentUtil.addXC(a, listOfTrolejbus());
    org.xdef.component.XComponentUtil.addXC(a, listOfVlak());
    org.xdef.component.XComponentUtil.addXC(a, listOfPovoz());
    org.xdef.component.XComponentUtil.addXC(a, listOfPredmet());
    org.xdef.component.XComponentUtil.addXC(a, listOfZvire());
    org.xdef.component.XComponentUtil.addXC(a, listOfUcastnik());
    return a;
  }
  public Z1() {}
  public Z1(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public Z1(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"4584F2D55B3E3B6707313549D69DA5C8".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_Verze="Verze";
  private String _Verze;
  private String XD_Name_SeqRec="SeqRec";
  private Integer _SeqRec;
  private String XD_Name_KrajPolicie="KrajPolicie";
  private String _KrajPolicie;
  private String XD_Name_PlatnostOd="PlatnostOd";
  private org.xdef.sys.SDatetime _PlatnostOd;
  private Z2 _Protokol;
  private Z7 _RozhodnutiDN;
  private final java.util.List<Z1.VyliceniDN> _VyliceniDN = new java.util.ArrayList<Z1.VyliceniDN>();
  private Z8 _ObjStranka;
  private final java.util.List<FotoDN> _Foto = new java.util.ArrayList<FotoDN>();
  private final java.util.List<VozidloDN> _Vozidlo = new java.util.ArrayList<VozidloDN>();
  private final java.util.List<TramvajDN> _Tramvaj = new java.util.ArrayList<TramvajDN>();
  private final java.util.List<TrolejbusDN> _Trolejbus = new java.util.ArrayList<TrolejbusDN>();
  private final java.util.List<VlakDN> _Vlak = new java.util.ArrayList<VlakDN>();
  private final java.util.List<PovozDN> _Povoz = new java.util.ArrayList<PovozDN>();
  private final java.util.List<PredmetDN> _Predmet = new java.util.ArrayList<PredmetDN>();
  private final java.util.List<ZvireDN> _Zvire = new java.util.ArrayList<ZvireDN>();
  private final java.util.List<UcastnikDN> _Ucastnik = new java.util.ArrayList<UcastnikDN>();
  public static final String XD_NAME="ZaznamPDN";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "ZaznamPDN";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="SouborD1A#ZaznamPDN";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult) {
    if (x.getXMNode().getXDPosition().endsWith("/@Verze")) {
      XD_Name_Verze = x.getNodeName();
      setVerze(parseResult.getParsedValue().toString());
    } else if (x.getXMNode().getXDPosition().endsWith("/@SeqRec")) {
      XD_Name_SeqRec = x.getNodeName();
      setSeqRec(parseResult.getParsedValue().intValue());
    } else if (x.getXMNode().getXDPosition().endsWith("/@KrajPolicie")) {
      XD_Name_KrajPolicie = x.getNodeName();
      setKrajPolicie(parseResult.getParsedValue().toString());
    } else {
      XD_Name_PlatnostOd = x.getNodeName();
      setPlatnostOd(parseResult.getParsedValue().datetimeValue());
    }
  }
  @Override
  public org.xdef.component.XComponent xCreateXChild(
    org.xdef.proc.XXNode x) {
    String s = x.getXMElement().getXDPosition();
    if ("SouborD1A#ZaznamPDN/$mixed/Protokol".equals(s))
      return new test.xdef.component.Z2(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/RozhodnutiDN".equals(s))
      return new test.xdef.component.Z7(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/VyliceniDN".equals(s))
      return new VyliceniDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/ObjStranka".equals(s))
      return new test.xdef.component.Z8(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Foto".equals(s))
      return new test.xdef.component.FotoDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Vozidlo".equals(s))
      return new test.xdef.component.VozidloDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Tramvaj".equals(s))
      return new test.xdef.component.TramvajDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Trolejbus".equals(s))
      return new test.xdef.component.TrolejbusDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Vlak".equals(s))
      return new test.xdef.component.VlakDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Povoz".equals(s))
      return new test.xdef.component.PovozDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Predmet".equals(s))
      return new test.xdef.component.PredmetDN(this, x);
    if ("SouborD1A#ZaznamPDN/$mixed/Zvire".equals(s))
      return new test.xdef.component.ZvireDN(this, x);
    return new test.xdef.component.UcastnikDN(this, x); // SouborD1A#ZaznamPDN/$mixed/Ucastnik
  }
  @Override
  public void xAddXChild(org.xdef.component.XComponent x){
    x.xSetNodeIndex(XD_ndx++);
    String s = x.xGetModelPosition();
    if ("SouborD1A#ZaznamPDN/$mixed/Protokol".equals(s))
      setProtokol((test.xdef.component.Z2)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/RozhodnutiDN".equals(s))
      setRozhodnutiDN((test.xdef.component.Z7)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/VyliceniDN".equals(s))
      listOfVyliceniDN().add((VyliceniDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/ObjStranka".equals(s))
      setObjStranka((test.xdef.component.Z8)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Foto".equals(s))
      listOfFoto().add((test.xdef.component.FotoDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Vozidlo".equals(s))
      listOfVozidlo().add((test.xdef.component.VozidloDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Tramvaj".equals(s))
      listOfTramvaj().add((test.xdef.component.TramvajDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Trolejbus".equals(s))
      listOfTrolejbus().add((test.xdef.component.TrolejbusDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Vlak".equals(s))
      listOfVlak().add((test.xdef.component.VlakDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Povoz".equals(s))
      listOfPovoz().add((test.xdef.component.PovozDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Predmet".equals(s))
      listOfPredmet().add((test.xdef.component.PredmetDN)x);
    else if ("SouborD1A#ZaznamPDN/$mixed/Zvire".equals(s))
      listOfZvire().add((test.xdef.component.ZvireDN)x);
    else
      listOfUcastnik().add((test.xdef.component.UcastnikDN)x); //SouborD1A#ZaznamPDN/$mixed/Ucastnik
  }
  @Override
  public void xSetAny(org.w3c.dom.Element el) {}
// </editor-fold>
public static class VyliceniDN implements org.xdef.component.XComponent{
  public String get$value() {return _$value;}
  public void set$value(String x){
_$value=x;}
  public String xposOf$value(){return XD_XPos+"/$text";}
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
  public int xGetModelIndex() {return 3;}
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
  public Object toJson() {return org.xdef.json.JsonUtil.xmlToJson(toXml());}
  @Override
  public java.util.List<org.xdef.component.XComponent> xGetNodeList() {
    java.util.ArrayList<org.xdef.component.XComponent> a=
      new java.util.ArrayList<org.xdef.component.XComponent>();
    if (get$value() != null)
      org.xdef.component.XComponentUtil.addText(this,
        "SouborD1A#text/$text", a, get$value(), _$$value);
    return a;
  }
  public VyliceniDN() {}
  public VyliceniDN(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public VyliceniDN(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"0BBC8E2A504A9E2D3C354DD465C51838".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String _$value;
  private char _$$value= (char) -1;
  public static final String XD_NAME="VyliceniDN";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "VyliceniDN";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="SouborD1A#ZaznamPDN/$mixed/VyliceniDN";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    _$$value=(char) XD_ndx++;
    set$value(parseResult.getParsedValue().toString());
  }
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
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
}