// This file was generated by org.xdef.component.GenXComponent.
// XDPosition: "#A".
// Any modifications to this file will be lost upon recompilation.
package mytest.component;
public class XA implements org.xdef.component.XComponent{
  public String jgeta$string(){
    return _js$string==null?null:org.xdef.json.JsonUtil.jstringFromSource(_js$string.get$value());
  }
  public XA.js$string getjs$string() {return _js$string;}
  public Number jgeta$number(){
    return _js$number==null?null:_js$number.get$value();
  }
  public XA.js$number getjs$number() {return _js$number;}
  public Boolean jgeta$boolean(){
    return _js$boolean==null?null:_js$boolean.get$value();
  }
  public XA.js$boolean getjs$boolean() {return _js$boolean;}
  public org.xdef.json.JNull jgeta$null(){
    return _js$null==null?null:org.xdef.json.JNull.JNULL;
  }
  public XA.js$null getjs$null() {return _js$null;}
  public void jseta$string(String x){
    if(x==null) _js$string=null;
    else {
      if(_js$string==null) setjs$string(new XA.js$string());
      _js$string.set$value(x);
    }
  }
  public void setjs$string(XA.js$string x){
    _js$string=null;_js$number=null;_js$boolean=null;_js$null=null;
    _js$string=x;
    }
  public void jseta$number(Number x){
    if(x==null) _js$number=null;
    else {
      if(_js$number==null) setjs$number(new XA.js$number());
      _js$number.set$value(x);
    }
  }
  public void setjs$number(XA.js$number x){
    _js$string=null;_js$number=null;_js$boolean=null;_js$null=null;
    _js$number=x;
    }
  public void jseta$boolean(Boolean x){
    if(x==null) _js$boolean=null;
    else {
      if(_js$boolean==null) setjs$boolean(new XA.js$boolean());
      _js$boolean.set$value(x);
    }
  }
  public void setjs$boolean(XA.js$boolean x){
    _js$string=null;_js$number=null;_js$boolean=null;_js$null=null;
    _js$boolean=x;
    }
  public void jseta$null(org.xdef.json.JNull x){
    if(_js$null==null)setjs$null(x==null?null:new XA.js$null());
  }
  public void setjs$null(XA.js$null x){
    _js$string=null;_js$number=null;_js$boolean=null;_js$null=null;
    _js$null=x;
    }
//<editor-fold defaultstate="collapsed" desc="Implementation of XComponent interface">
  public final static byte JSON = 1;
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
    el.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
      "xmlns:js", "http://www.w3.org/2005/xpath-functions");
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
    org.xdef.component.XComponentUtil.addXC(a, getjs$null());
    org.xdef.component.XComponentUtil.addXC(a, getjs$boolean());
    org.xdef.component.XComponentUtil.addXC(a, getjs$number());
    org.xdef.component.XComponentUtil.addXC(a, getjs$string());
    return a;
  }
  public XA() {}
  public XA(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public XA(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"5DDFADB542D989485A9864D0985FF61E".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private XA.js$null _js$null;
  private XA.js$boolean _js$boolean;
  private XA.js$number _js$number;
  private XA.js$string _js$string;
  public static final String XD_NAME="A";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "js:map";
  private String XD_NamespaceURI = "http://www.w3.org/2005/xpath-functions";
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="#A/js:map";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
  @Override
  public org.xdef.component.XComponent xCreateXChild(
    org.xdef.proc.XXNode x) {
    String s = x.getXMElement().getXDPosition();
    if ("#A/js:map/$choice/js:null".equals(s))
      return new js$null(this, x);
    if ("#A/js:map/$choice/js:boolean".equals(s))
      return new js$boolean(this, x);
    if ("#A/js:map/$choice/js:number".equals(s))
      return new js$number(this, x);
    return new js$string(this, x); // #A/js:map/$choice/js:string
  }
  @Override
  public void xAddXChild(org.xdef.component.XComponent x){
    x.xSetNodeIndex(XD_ndx++);
    String s = x.xGetModelPosition();
    if ("#A/js:map/$choice/js:null".equals(s))
      setjs$null((js$null)x);
    else if ("#A/js:map/$choice/js:boolean".equals(s))
      setjs$boolean((js$boolean)x);
    else if ("#A/js:map/$choice/js:number".equals(s))
      setjs$number((js$number)x);
    else
      setjs$string((js$string)x); //#A/js:map/$choice/js:string
  }
  @Override
  public void xSetAny(org.w3c.dom.Element el) {}
// </editor-fold>
public static class js$null implements org.xdef.component.XComponent{
  public String getkey() {return _key;}
  public void setkey(String x){_key=x;}
  public String xposOfkey(){return XD_XPos+"/@key";}
//<editor-fold defaultstate="collapsed" desc="Implementation of XComponent interface">
  public final static byte JSON = 1;
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
  public int xGetModelIndex() {return 1;}
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
    if (getkey() != null)
      el.setAttribute(XD_Name_key, getkey());
    el.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
      "xmlns:js", "http://www.w3.org/2005/xpath-functions");
    return el;
  }
  @Override
  public Object toJson() {return org.xdef.json.JsonUtil.xmlToJson(toXml());}
  @Override
  public java.util.List<org.xdef.component.XComponent> xGetNodeList() {
    return new java.util.ArrayList<org.xdef.component.XComponent>();
  }
  public js$null() {}
  public js$null(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public js$null(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"6C666925345C41A40D8883A3EED72820".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_key="key";
  private String _key;
  public static final String XD_NAME="js:null";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "js:null";
  private String XD_NamespaceURI = "http://www.w3.org/2005/xpath-functions";
  private int XD_Index = -1;
  private String XD_XPos;
  private String XD_Model="#A/js:map/$choice/js:null";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){}
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    XD_Name_key = x.getNodeName();
    setkey(parseResult.getParsedValue().toString());
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
public static class js$boolean implements org.xdef.component.XComponent{
  public String getkey() {return _key;}
  public Boolean get$value() {return _$value;}
  public void setkey(String x){_key=x;}
  public void set$value(Boolean x){_$value=x;}
  public String xposOfkey(){return XD_XPos+"/@key";}
  public String xposOf$value(){return XD_XPos+"/$text";}
//<editor-fold defaultstate="collapsed" desc="Implementation of XComponent interface">
  public final static byte JSON = 1;
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
  public int xGetModelIndex() {return 2;}
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
    if (getkey() != null)
      el.setAttribute(XD_Name_key, getkey());
    el.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
      "xmlns:js", "http://www.w3.org/2005/xpath-functions");
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
        "#A/js:map/$choice/js:boolean/$text", a, get$value().toString(), _$$value);
    return a;
  }
  public js$boolean() {}
  public js$boolean(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public js$boolean(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"BBC1E2D70081D1481B9C571674E3145A".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_key="key";
  private String _key;
  private Boolean _$value;
  private char _$$value= (char) -1;
  public static final String XD_NAME="js:boolean";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "js:boolean";
  private String XD_NamespaceURI = "http://www.w3.org/2005/xpath-functions";
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="#A/js:map/$choice/js:boolean";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    _$$value=(char) XD_ndx++;
    set$value(parseResult.getParsedValue().booleanValue());
  }
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    XD_Name_key = x.getNodeName();
    setkey(parseResult.getParsedValue().toString());
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
public static class js$number implements org.xdef.component.XComponent{
  public String getkey() {return _key;}
  public Number get$value() {return _$value;}
  public void setkey(String x){_key=x;}
  public void set$value(Number x){_$value=x;}
  public String xposOfkey(){return XD_XPos+"/@key";}
  public String xposOf$value(){return XD_XPos+"/$text";}
//<editor-fold defaultstate="collapsed" desc="Implementation of XComponent interface">
  public final static byte JSON = 1;
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
    if (getkey() != null)
      el.setAttribute(XD_Name_key, getkey());
    el.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
      "xmlns:js", "http://www.w3.org/2005/xpath-functions");
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
        "#A/js:map/$choice/js:number/$text", a, get$value().toString(), _$$value);
    return a;
  }
  public js$number() {}
  public js$number(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public js$number(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"51B34686437C777A7A8D6A2F80AC36A4".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_key="key";
  private String _key;
  private Number _$value;
  private char _$$value= (char) -1;
  public static final String XD_NAME="js:number";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "js:number";
  private String XD_NamespaceURI = "http://www.w3.org/2005/xpath-functions";
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="#A/js:map/$choice/js:number";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    _$$value=(char) XD_ndx++;
    set$value((Number)parseResult.getParsedValue().getObject());
  }
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    XD_Name_key = x.getNodeName();
    setkey(parseResult.getParsedValue().toString());
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
public static class js$string implements org.xdef.component.XComponent{
  public String getkey() {return _key;}
  public String get$value() {return _$value;}
  public void setkey(String x){_key=x;}
  public void set$value(String x){_$value=x;}
  public String xposOfkey(){return XD_XPos+"/@key";}
  public String xposOf$value(){return XD_XPos+"/$text";}
//<editor-fold defaultstate="collapsed" desc="Implementation of XComponent interface">
  public final static byte JSON = 1;
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
  public int xGetModelIndex() {return 4;}
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
    if (getkey() != null)
      el.setAttribute(XD_Name_key, getkey());
    el.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
      "xmlns:js", "http://www.w3.org/2005/xpath-functions");
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
        "#A/js:map/$choice/js:string/$text", a, get$value(), _$$value);
    return a;
  }
  public js$string() {}
  public js$string(org.xdef.component.XComponent p,
    String name, String ns, String xPos, String XDPos) {
    XD_NodeName=name; XD_NamespaceURI=ns;
    XD_XPos=xPos;
    XD_Model=XDPos;
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
  }
  public js$string(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){
    org.w3c.dom.Element el=x.getElement();
    XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
    XD_XPos=x.getXPos();
    XD_Model=x.getXMElement().getXDPosition();
    XD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;
    if (!"3797C61A76CABF48D931604341A0BDC1".equals(
      x.getXMElement().getDigest())) { //incompatible element model
      throw new org.xdef.sys.SRuntimeException(
        org.xdef.msg.XDEF.XDEF374);
    }
  }
  private String XD_Name_key="key";
  private String _key;
  private String _$value;
  private char _$$value= (char) -1;
  public static final String XD_NAME="js:string";
  private org.xdef.component.XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "js:string";
  private String XD_NamespaceURI = "http://www.w3.org/2005/xpath-functions";
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private String XD_Model="#A/js:map/$choice/js:string";
  @Override
  public void xSetText(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    _$$value=(char) XD_ndx++;
    set$value((String)parseResult.getParsedValue().getObject());
  }
  @Override
  public void xSetAttr(org.xdef.proc.XXNode x,
    org.xdef.XDParseResult parseResult){
    XD_Name_key = x.getNodeName();
    setkey(parseResult.getParsedValue().toString());
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
}