package org.hl7.fhir.tools.publisher.implementations;
/*
Copyright (c) 2011-2012, HL7, Inc
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this 
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation 
   and/or other materials provided with the distribution.
 * Neither the name of HL7 nor the names of its contributors may be used to 
   endorse or promote products derived from this software without specific 
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

*/
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.definitions.Config;
import org.hl7.fhir.definitions.model.BindingSpecification;
import org.hl7.fhir.definitions.model.DefinedCode;
import org.hl7.fhir.definitions.model.Definitions;
import org.hl7.fhir.definitions.model.ElementDefn;
import org.hl7.fhir.definitions.model.ResourceDefn;
import org.hl7.fhir.definitions.model.SearchParameter;
import org.hl7.fhir.definitions.model.TypeRef;
import org.hl7.fhir.tools.publisher.PlatformGenerator;
import org.hl7.fhir.utilities.Logger;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.ZipGenerator;

/**
 * Generates the delphi reference implementation
 * 
 * todo: the delphi reference implementation depends on too much HL7Connect infrastructure.
 * 
 * @author Grahame
 *
 */
public class DelphiGenerator extends BaseGenerator implements PlatformGenerator {

  public enum ClassCategory {
    Type, Component, Resource
  }

  private DelphiCodeGenerator defCodeType;
  private DelphiCodeGenerator defCodeComp;
  private DelphiCodeGenerator defCodeRes;
  private DelphiCodeGenerator prsrCode;
  private Definitions definitions;
  
  private Map<ElementDefn, String> typeNames = new HashMap<ElementDefn, String>();

  private List<ElementDefn> enums = new ArrayList<ElementDefn>();
  private List<String> enumNames = new ArrayList<String>();
  private List<ElementDefn> strucs  = new ArrayList<ElementDefn>();
  private List<String> lists = new ArrayList<String>();
  

  private StringBuilder workingParserX;
  private StringBuilder workingComposerX;
  private StringBuilder workingParserJ;
  private StringBuilder workingComposerJ;
  private StringBuilder factoryIntf;
  private StringBuilder factoryImpl;
  
  
  private void generate(ElementDefn root, String superClass, boolean listsAreWrapped, boolean resource, ClassCategory category) throws Exception {
    typeNames.clear();
    enums.clear();
    strucs.clear();
    enumNames.clear();
        
    for (ElementDefn e : root.getElements()) {
      if (!root.typeCode().equals("Resource") || (!e.getName().equals("extension") && !e.getName().equals("text")))
        scanNestedTypes(root, root.getName(), e);
    }

    for (ElementDefn e : enums) {
      generateEnum(e);
    }
    for (ElementDefn e : strucs) {
      generateType(e, listsAreWrapped, category == ClassCategory.Resource ? ClassCategory.Component : category);
    }
   
    if (root.getTypes().size() > 0 && root.getTypes().get(0).getName().equals("GenericType")) {
      for (TypeRef td : definitions.getKnownTypes()) {
        if (td.getName().equals(root.getName()) && td.hasParams()) {
          for (String pt : td.getParams()) {
            String tn = getTypeName(pt, false);
            if (tn.equals(pt))
              tn = "T"+tn;
            genGenericResource(root, "T"+root.getName()+"_"+getTitle(pt), tn, superClass, ClassCategory.Type);
          }
        }
      }
    } else if (!resource) {
      genType(root, "T"+root.getName(), superClass, listsAreWrapped, category);
    }
  }
    
  private void genGenericResource(ElementDefn root, String tn, String pt, String superClass, ClassCategory category) throws Exception {
    prsrdefX.append("    function Parse"+tn.substring(1)+"(element : IXmlDomElement) : "+tn+";\r\n");
    srlsdefX.append("    procedure Compose"+tn.substring(1)+"(xml : TMsXmlBuilder; name : string; elem : "+tn+");\r\n");
    prsrdefJ.append("    function Parse"+tn.substring(1)+" : "+tn+";\r\n");
    srlsdefJ.append("    procedure Compose"+tn.substring(1)+"(json : TJSONWriter; name : string; elem : "+tn+");\r\n");
    workingParserX = new StringBuilder();
    workingComposerX = new StringBuilder();
    workingParserJ = new StringBuilder();
    workingComposerJ = new StringBuilder();
    
    StringBuilder def = new StringBuilder();
    StringBuilder defPriv1 = new StringBuilder();
    StringBuilder defPriv2 = new StringBuilder();
    StringBuilder defPub = new StringBuilder();
    StringBuilder impl = new StringBuilder();
    StringBuilder create = new StringBuilder();
    StringBuilder destroy = new StringBuilder();
    StringBuilder assign = new StringBuilder();
    StringBuilder getkids = new StringBuilder();
    StringBuilder getprops = new StringBuilder();
      

    
    for (ElementDefn e : root.getElements()) {
      generateField(e, defPriv1, defPriv2, defPub, impl, create, destroy, assign, getkids, getprops, tn, pt, true, false, category);
    }

    def.append("  {@Class "+tn+" : "+superClass+"\r\n");
    def.append("    "+root.getDefinition()+"\r\n");
    def.append("  }\r\n");
    def.append("  {!.Net HL7Connect.Fhir."+tn.substring(1)+"}\r\n");
    def.append("  "+tn+" = class ("+superClass+")\r\n");
    factoryIntf.append("    {@member new"+tn.substring(1)+"\r\n      create a new "+root.getName()+"\r\n    }\r\n    {!script nolink}\r\n    function new"+tn.substring(1)+" : "+tn+";\r\n");
    factoryImpl.append("function TFHIRResourceFactory.new"+tn.substring(1)+" : "+tn+";\r\nbegin\r\n  result := "+tn+".create;\r\nend;\r\n\r\n");
    def.append("  private\r\n");
    def.append(defPriv1.toString());
    def.append(defPriv2.toString());
    def.append("  protected\r\n");
    def.append("    Procedure GetChildrenByName(child_name : string; list : TFHIRObjectList); override;\r\n");
    def.append("  public\r\n");
    def.append("    constructor Create; Override;\r\n");
    def.append("    destructor Destroy; override;\r\n");
    def.append("    {!script hide}\r\n");
    def.append("    procedure Assign(oSource : TAdvObject); override;\r\n");
    def.append("    function Link : "+tn+"; overload;\r\n");
    def.append("    function Clone : "+tn+"; overload;\r\n");
    def.append("    {!script show}\r\n");
    def.append("  published\r\n");
    def.append(defPub.toString());
    def.append("  end;\r\n");
    def.append("\r\n");

    StringBuilder impl2 = new StringBuilder();
    impl2.append("{ "+tn+" }\r\n\r\n");
    impl2.append("constructor "+tn+".Create;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(create.toString());
    impl2.append("end;\r\n\r\n");

    impl2.append("destructor "+tn+".Destroy;\r\n");
    impl2.append("begin\r\n");
    impl2.append(destroy.toString());
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n\r\n");
    
    impl2.append("procedure "+tn+".Assign(oSource : TAdvObject);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(assign.toString());
    impl2.append("end;\r\n\r\n");
    
    impl2.append("procedure "+tn+".GetChildrenByName(child_name : string; list : TFHIRObjectList);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getkids.toString());
    impl2.append("end;\r\n\r\n");
    
    impl2.append("function "+tn+".Link : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Link);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("function "+tn+".Clone : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Clone);\r\n");
    impl2.append("end;\r\n\r\n");
    
    getCode(category).classDefs.add(def.toString());
    getCode(category).classImpls.add(impl2.toString()+impl.toString());
    getCode(category).classFwds.add("  "+tn+" = class;\r\n");
    generateParser(tn, false);
  }
  
  private DelphiCodeGenerator getCode(ClassCategory category) {
    switch (category) {
    case Type:
      return defCodeType;
    case Component:
      return defCodeComp;
    case Resource:
      return defCodeRes;
    }
    return null;
  }

  private void genType(ElementDefn root, String tn, String superClass, boolean listsAreWrapped, ClassCategory category) throws Exception {
    prsrdefX.append("    function Parse"+root.getName()+"(element : IXmlDomElement) : T"+root.getName()+";\r\n");
    srlsdefX.append("    procedure Compose"+root.getName()+"(xml : TMsXmlBuilder; name : string; elem : T"+root.getName()+");\r\n");
    prsrdefJ.append("    function Parse"+root.getName()+" : T"+root.getName()+";\r\n");
    srlsdefJ.append("    procedure Compose"+root.getName()+"(json : TJSONWriter; name : string; elem : T"+root.getName()+");\r\n");
    workingParserX = new StringBuilder();
    workingComposerX = new StringBuilder();
    workingParserJ = new StringBuilder();
    workingComposerJ = new StringBuilder();

   
    StringBuilder def = new StringBuilder();
    StringBuilder defPriv1 = new StringBuilder();
    StringBuilder defPriv2 = new StringBuilder();
    StringBuilder defPub = new StringBuilder();
    StringBuilder impl = new StringBuilder();
    StringBuilder create = new StringBuilder();
    StringBuilder destroy = new StringBuilder();
    StringBuilder assign = new StringBuilder();
    StringBuilder getkids = new StringBuilder();
    StringBuilder getprops = new StringBuilder();
    impl.append("{ "+tn+" }\r\n\r\n");

    
    boolean isRes = superClass.equals("TFHIRResource");
    for (ElementDefn e : root.getElements()) {
      if (!isRes || (!e.getName().equals("extension") && !e.getName().equals("text")))
        generateField(e, defPriv1, defPriv2, defPub, impl, create, destroy, assign, getkids, getprops, tn, "", !isRes, listsAreWrapped, category);
    }

    def.append("  {@Class "+tn+" : "+superClass+"\r\n");
    def.append("    "+root.getDefinition()+"\r\n");
    def.append("  }\r\n");
    def.append("  {!.Net HL7Connect.Fhir."+tn.substring(1)+"}\r\n");
    def.append("  "+tn+" = class ("+superClass+")\r\n");
    factoryIntf.append("    {@member new"+tn.substring(1)+"\r\n      create a new "+root.getName()+"\r\n    }\r\n    {!script nolink}\r\n    function new"+tn.substring(1)+" : "+tn+";\r\n");    
    factoryImpl.append("function TFHIRResourceFactory.new"+tn.substring(1)+" : "+tn+";\r\nbegin\r\n  result := "+tn+".create;\r\nend;\r\n\r\n");
    def.append("  private\r\n");
    def.append(defPriv1.toString());
    def.append(defPriv2.toString());
    def.append("  protected\r\n");
    if (isRes) {
      def.append("    function GetResourceType : TFHIRResourceType; override;\r\n");      
    }
    def.append("    Procedure GetChildrenByName(child_name : string; list : TFHIRObjectList); override;\r\n");
    def.append("    Procedure ListProperties(oList : TFHIRPropertyList; bInheritedProperties : Boolean); Override;\r\n");
    def.append("  public\r\n");
    def.append("    constructor Create; Override;\r\n");
    def.append("    destructor Destroy; override;\r\n");
    def.append("    {!script hide}\r\n");
    def.append("    procedure Assign(oSource : TAdvObject); override;\r\n");
    def.append("    function Link : "+tn+"; overload;\r\n");
    def.append("    function Clone : "+tn+"; overload;\r\n");
    def.append("    {!script show}\r\n");
    def.append("  published\r\n");
    def.append(defPub.toString());
    def.append("  end;\r\n");
    def.append("\r\n");
    StringBuilder impl2 = new StringBuilder();
    impl2.append("{ "+tn+" }\r\n\r\n");
    impl2.append("constructor "+tn+".Create;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(create.toString());
    impl2.append("end;\r\n\r\n");

    impl2.append("destructor "+tn+".Destroy;\r\n");
    impl2.append("begin\r\n");
    impl2.append(destroy.toString());
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n\r\n");
    if (isRes) {
      impl2.append("function "+tn+".GetResourceType : TFHIRResourceType;\r\nbegin\r\n  result := frt"+root.getName()+";\r\nend;\r\n\r\n");       
    }
    
    impl2.append("procedure "+tn+".Assign(oSource : TAdvObject);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(assign.toString());
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure "+tn+".GetChildrenByName(child_name : string; list : TFHIRObjectList);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getkids.toString());
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure "+tn+".ListProperties(oList: TFHIRPropertyList; bInheritedProperties: Boolean);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getprops.toString());
    impl2.append("end;\r\n\r\n");
    
    impl2.append("function "+tn+".Link : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Link);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("function "+tn+".Clone : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Clone);\r\n");
    impl2.append("end;\r\n\r\n");
    getCode(category).classDefs.add(def.toString());
    getCode(category).classImpls.add(impl2.toString() + impl.toString());
    getCode(category).classFwds.add("  "+tn+" = class;\r\n");
    generateParser(tn, isRes);
  }

  private void genResource(ResourceDefn root, String tn, String superClass, boolean listsAreWrapped, ClassCategory category) throws Exception {
    prsrdefX.append("    function Parse"+root.getName()+"(element : IXmlDomElement) : T"+root.getName()+";\r\n");
    srlsdefX.append("    procedure Compose"+root.getName()+"(xml : TMsXmlBuilder; name : string; elem : T"+root.getName()+");\r\n");
    prsrdefJ.append("    function Parse"+root.getName()+" : T"+root.getName()+";\r\n");
    srlsdefJ.append("    procedure Compose"+root.getName()+"(json : TJSONWriter; name : string; elem : T"+root.getName()+");\r\n");
    workingParserX = new StringBuilder();
    workingComposerX = new StringBuilder();
    workingParserJ = new StringBuilder();
    workingComposerJ = new StringBuilder();

    generateSearchEnums(root);
    
    StringBuilder def = new StringBuilder();
    StringBuilder defPriv1 = new StringBuilder();
    StringBuilder defPriv2 = new StringBuilder();
    StringBuilder defPub = new StringBuilder();
    StringBuilder impl = new StringBuilder();
    StringBuilder create = new StringBuilder();
    StringBuilder destroy = new StringBuilder();
    StringBuilder assign = new StringBuilder();
    StringBuilder getkids = new StringBuilder();
    StringBuilder getprops = new StringBuilder();
    impl.append("{ "+tn+" }\r\n\r\n");

    
    boolean isRes = superClass.equals("TFHIRResource");
    for (ElementDefn e : root.getRoot().getElements()) {
      if (!isRes || (!e.getName().equals("extension") && !e.getName().equals("text")))
        generateField(e, defPriv1, defPriv2, defPub, impl, create, destroy, assign, getkids, getprops, tn, "", !isRes, listsAreWrapped, ClassCategory.Component);
    }

    def.append("  {@Class "+tn+" : "+superClass+"\r\n");
    def.append("    "+root.getDefinition()+"\r\n");
    def.append("  }\r\n");
    def.append("  {!.Net HL7Connect.Fhir."+tn.substring(1)+"}\r\n");
    def.append("  "+tn+" = class ("+superClass+")\r\n");
    factoryIntf.append("    {@member new"+tn.substring(1)+"\r\n      create a new "+root.getName()+"\r\n    }\r\n    {!script nolink}\r\n    function new"+tn.substring(1)+" : "+tn+";\r\n");    
    factoryImpl.append("function TFHIRResourceFactory.new"+tn.substring(1)+" : "+tn+";\r\nbegin\r\n  result := "+tn+".create;\r\nend;\r\n\r\n");
    def.append("  private\r\n");
    def.append(defPriv1.toString());
    def.append(defPriv2.toString());
    def.append("  protected\r\n");
    def.append("    Procedure GetChildrenByName(child_name : string; list : TFHIRObjectList); override;\r\n");
    def.append("    Procedure ListProperties(oList : TFHIRPropertyList; bInheritedProperties : Boolean); Override;\r\n");
    if (isRes) {
      def.append("    function GetResourceType : TFHIRResourceType; override;\r\n");      
    }
    def.append("  public\r\n");
    def.append("    constructor Create; Override;\r\n");
    def.append("    destructor Destroy; override;\r\n");
    def.append("    {!script hide}\r\n");
    def.append("    procedure Assign(oSource : TAdvObject); override;\r\n");
    def.append("    function Link : "+tn+"; overload;\r\n");
    def.append("    function Clone : "+tn+"; overload;\r\n");
    def.append("    {!script show}\r\n");
    def.append("  published\r\n");
    def.append(defPub.toString());
    def.append("  end;\r\n");
    def.append("\r\n");
    StringBuilder impl2 = new StringBuilder();
    impl2.append("{ "+tn+" }\r\n\r\n");
    impl2.append("constructor "+tn+".Create;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(create.toString());
    impl2.append("end;\r\n\r\n");

    impl2.append("destructor "+tn+".Destroy;\r\n");
    impl2.append("begin\r\n");
    impl2.append(destroy.toString());
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n\r\n");
    if (isRes) {
      impl2.append("function "+tn+".GetResourceType : TFHIRResourceType;\r\nbegin\r\n  result := frt"+root.getName()+";\r\nend;\r\n\r\n");       
    }
    
    impl2.append("procedure "+tn+".Assign(oSource : TAdvObject);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(assign.toString());
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure "+tn+".GetChildrenByName(child_name : string; list : TFHIRObjectList);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getkids.toString());
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure "+tn+".ListProperties(oList: TFHIRPropertyList; bInheritedProperties: Boolean);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getprops.toString());
    impl2.append("end;\r\n\r\n");
    
    impl2.append("function "+tn+".Link : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Link);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("function "+tn+".Clone : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Clone);\r\n");
    impl2.append("end;\r\n\r\n");
    getCode(category).classDefs.add(def.toString());
    getCode(category).classImpls.add(impl2.toString() + impl.toString());
    getCode(category).classFwds.add("  "+tn+" = class;\r\n");
    generateParser(tn, isRes);
  }

  private void generateSearchEnums(ResourceDefn r) throws Exception {
    StringBuilder def = new StringBuilder();
    StringBuilder con = new StringBuilder();
    StringBuilder con2 = new StringBuilder();
    StringBuilder con3 = new StringBuilder();
    StringBuilder con4 = new StringBuilder();
    StringBuilder con5 = new StringBuilder();

    String tn = "TSearchParams"+r.getName();
    String prefix = "sp"+r.getName()+"_";

    def.append("  {@Enum "+tn+"\r\n");
    def.append("    Search Parameters for "+r.getName()+"\r\n");
    def.append("  }\r\n");
    def.append("  "+tn+" = (\r\n");
    
    con3.append("  CODES_"+tn+" : Array["+tn+"] of String = (");
    con4.append("  TYPES_"+tn+" : Array["+tn+"] of TSearchParamType = (");
    con5.append("  REPEATS_"+tn+" : Array["+tn+"] of TSearchRepeatBehavior = (");
    con.append("  DESC_"+tn+" : Array["+tn+"] of String = (");
    con2.append("//  CHECK_"+tn+" : Array["+tn+"] of "+tn+" = (");

    int l = r.getSearchParams().size();
    int i = 0;

    for (SearchParameter p : r.getSearchParams()) {
      i++;
      String n = p.getCode().replace("$", "_");
      String d = p.getDescription();
      String nf = n.replace("-", "_");
      if (i == l) {
        def.append("    "+prefix+getTitle(nf)+"); {@enum.value "+prefix+getTitle(nf)+" "+d+" }\r\n");
        con.append("'"+defCodeType.escape(d)+"');");
        con2.append(" "+prefix+getTitle(nf)+");");
        con4.append(" tspt"+getTitle(p.getType().toString())+");");
        con3.append("'"+defCodeType.escape(n)+"');");
        con5.append(" tsrb"+getTitle(p.getRepeatMode().toString())+");");
      }
      else {
        def.append("    "+prefix+getTitle(nf)+", {@enum.value "+prefix+getTitle(nf)+" "+d+" }\r\n");
        con.append("'"+defCodeType.escape(d)+"', ");
        con2.append(" "+prefix+getTitle(nf)+", ");
        con4.append(" tspt"+getTitle(p.getType().toString())+", ");
        con3.append("'"+defCodeType.escape(n)+"', ");
        con5.append(" tsrb"+getTitle(p.getRepeatMode().toString())+", ");
      }
    }

    defCodeType.enumDefs.add(def.toString());
    defCodeType.enumConsts.add(con3.toString());
    defCodeType.enumConsts.add(con5.toString());
    defCodeType.enumConsts.add(con4.toString());
    defCodeType.enumConsts.add(con.toString());
    defCodeType.enumConsts.add(con2.toString());
    
  }
  
  private void generateEnum(ElementDefn e) throws Exception {
    String tn = typeNames.get(e);
    BindingSpecification cd = getConceptDomain(e.getBindingName());
    
    
    StringBuilder pfx = new StringBuilder();
    for (char c : tn.toCharArray()) {
      if (Character.isUpperCase(c))
        pfx.append(c);
    }
    String prefix = pfx.toString().toLowerCase();
    StringBuilder def = new StringBuilder();
    StringBuilder con = new StringBuilder();
    def.append("  {@Enum "+tn+"\r\n");
    def.append("    "+cd.getDefinition()+"\r\n");
    def.append("  }\r\n");
    def.append("  "+tn+" = (\r\n");
    con.append("  CODES_"+tn+" : Array["+tn+"] of String = (");
    
    int l = cd.getCodes().size();
    int i = 0;
    def.append("    "+prefix+"Unknown,  {@enum.value "+prefix+"Unknown Value is unknown }\r\n");
    con.append("'', ");
    for (DefinedCode c : cd.getCodes()) {
      i++;
      String cc = c.getCode();
      cc = cc.replace("-", "Minus").replace("+", "Plus").replace(">=", "greaterOrEquals").replace("<=", "lessOrEquals").replace("<", "lessThan").replace(">", "greaterThan").replace("=", "equal");

      cc = prefix + getTitle(cc);
      if (GeneratorUtils.isDelphiReservedWord(cc))
        cc = cc + "_";
      if (i == l) {
        def.append("    "+cc+"); {@enum.value "+cc+" "+c.getDefinition()+" }\r\n");
        con.append("'"+c.getCode()+"');");
      }
      else {
        def.append("    "+cc+", {@enum.value "+cc+" "+c.getDefinition()+" }\r\n");
        con.append("'"+c.getCode()+"', ");
      }
    }
    defCodeType.enumDefs.add(def.toString());
    defCodeType.enumConsts.add(con.toString());
  }

  private void generateType(ElementDefn e, boolean listsAreWrapped, ClassCategory category) throws Exception {
    String tn = typeNames.get(e);

    prsrdefX.append("    function Parse"+tn.substring(1)+"(element : IXmlDomElement) : "+tn+";\r\n");
    srlsdefX.append("    procedure Compose"+tn.substring(1)+"(xml : TMsXmlBuilder; name : string; elem : "+tn+");\r\n");
    prsrdefJ.append("    function Parse"+tn.substring(1)+" : "+tn+";\r\n");
    srlsdefJ.append("    procedure Compose"+tn.substring(1)+"(json : TJSONWriter; name : string; elem : "+tn+");\r\n");
    workingParserX = new StringBuilder();
    workingComposerX = new StringBuilder();
    workingParserJ = new StringBuilder();
    workingComposerJ = new StringBuilder();
    
    StringBuilder def = new StringBuilder();
    StringBuilder defPriv1 = new StringBuilder();
    StringBuilder defPriv2 = new StringBuilder();
    StringBuilder defPub = new StringBuilder();
    StringBuilder impl = new StringBuilder();
    StringBuilder create = new StringBuilder();
    StringBuilder destroy = new StringBuilder();
    StringBuilder assign = new StringBuilder();
    StringBuilder getkids = new StringBuilder();
    StringBuilder getprops = new StringBuilder();
    
    def.append("  {@Class "+tn+" : TFHIRElement\r\n");
    def.append("    "+e.getDefinition()+"\r\n");
    def.append("  }\r\n");
    def.append("  {!.Net HL7Connect.Fhir."+tn.substring(1)+"}\r\n");
    def.append("  "+tn+" = class (TFHIRElement)\r\n");
    factoryIntf.append("    {@member new"+tn.substring(1)+"\r\n      create a new "+e.getName()+"\r\n    }\r\n    {!script nolink}\r\n    function new"+tn.substring(1)+" : "+tn+";\r\n");    
    factoryImpl.append("function TFHIRResourceFactory.new"+tn.substring(1)+" : "+tn+";\r\nbegin\r\n  result := "+tn+".create;\r\nend;\r\n\r\n");
    impl.append("{ "+tn+" }\r\n\r\n");
    
//    if (hasLists(e)) {
//      s.append("      public "+tn+"()\r\n");
//      s.append("      {\r\n");
//      for (ElementDefn c : e.getElements()) {
//        if (c.unbounded()) {
//          s.append("        "+getElementName(c.getName())+" = new List<"+typeNames.get(c)+">();\r\n");         
//        }
//      }
//      s.append("      }\r\n");
//      s.append("\r\n");
//      
//    }
    for (ElementDefn c : e.getElements()) {
      generateField(c, defPriv1, defPriv2, defPub, impl, create, destroy, assign, getkids, getprops, tn, "", false, listsAreWrapped, category);
    }

    def.append("  private\r\n");
    def.append(defPriv1.toString());
    def.append(defPriv2.toString());
    def.append("  protected\r\n");
    def.append("    Procedure GetChildrenByName(child_name : string; list : TFHIRObjectList); override;\r\n");
    def.append("    Procedure ListProperties(oList : TFHIRPropertyList; bInheritedProperties : Boolean); Override;\r\n");
    def.append("  public\r\n");
    def.append("    constructor Create; Override;\r\n");
    def.append("    destructor Destroy; override;\r\n");
    def.append("    {!script hide}\r\n");
    def.append("    procedure Assign(oSource : TAdvObject); override;\r\n");
    def.append("    function Link : "+tn+"; overload;\r\n");
    def.append("    function Clone : "+tn+"; overload;\r\n");
    def.append("    {!script show}\r\n");
    def.append("  published\r\n");
    def.append(defPub.toString());
    def.append("  end;\r\n");
    def.append("\r\n");
    StringBuilder impl2 = new StringBuilder();
    impl2.append("{ "+tn+" }\r\n\r\n");
    impl2.append("constructor "+tn+".Create;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(create.toString());
    impl2.append("end;\r\n\r\n");

    impl2.append("destructor "+tn+".Destroy;\r\n");
    impl2.append("begin\r\n");
    impl2.append(destroy.toString());
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n\r\n");
    
    impl2.append("procedure "+tn+".Assign(oSource : TAdvObject);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(assign.toString());
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure "+tn+".GetChildrenByName(child_name : string; list : TFHIRObjectList);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getkids.toString());
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure "+tn+".ListProperties(oList: TFHIRPropertyList; bInheritedProperties: Boolean);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append(getprops.toString());
    impl2.append("end;\r\n\r\n");
    
    
    impl2.append("function "+tn+".Link : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Link);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("function "+tn+".Clone : "+tn+";\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := "+tn+"(inherited Clone);\r\n");
    impl2.append("end;\r\n\r\n");
    
    getCode(category).classDefs.add(def.toString());
    getCode(category).classImpls.add(impl2.toString() + impl.toString());
    getCode(category).classFwds.add("  "+tn+" = class;\r\n");
    generateParser(tn, false);
  }

  private void generateParser(String tn, boolean isResource) {
    String s = workingParserX.toString();
    prsrImpl.append(
            "function TFHIRXmlParser.Parse"+tn.substring(1)+"(element : IXmlDomElement) : "+tn+";\r\n"+
            "var\r\n"+
            "  child : IXMLDOMElement;\r\n");
    
    prsrImpl.append(s.contains("item") ? "  item : IXMLDOMElement;\r\n" : "");
    prsrImpl.append(
            "begin\r\n"+
            "  result := "+tn+".create;\r\n"+
            "  try\r\n"+
            "    takeComments(result);\r\n"+
            "    result.xmlId := GetAttribute(element, 'id');\r\n");
    prsrImpl.append(
            "    child := FirstChild(element);\r\n"+
            "    while (child <> nil) do\r\n"+
            "    begin\r\n");
    if (isResource)
      prsrImpl.append(
          "      if (child.baseName = 'text') then\r\n"+
          "        result.text := ParseNarrative(child)\r\n"+
          "      else if (child.baseName = 'extension') then\r\n"+
          "        result.extensionList.add(ParseExtension(child))\r\n"+
            s);
    else 
      prsrImpl.append("      "+s.substring(11));
    prsrImpl.append(
            "      else\r\n"+
            "         UnknownContent(child);\r\n"+
            "      child := NextSibling(child);\r\n"+
            "    end;\r\n"+
            "\r\n"+
            "    result.link;\r\n"+
            "  finally\r\n"+
            "    result.free;\r\n"+
            "  end;\r\n"+
            "end;\r\n\r\n"
        );

    s = workingComposerX.toString();
    prsrImpl.append(
            "procedure TFHIRXmlComposer.Compose"+tn.substring(1)+"(xml : TMsXmlBuilder; name : string; elem : "+tn+");\r\n");
    if (s.contains("for i := ") || isResource)
      prsrImpl.append("var\r\n  i : integer;\r\n");
    prsrImpl.append(
            "begin\r\n"+
            "  if (elem = nil) then\r\n    exit;\r\n"+
            "  comments(xml, elem);\r\n  attribute(xml, 'id', elem.xmlId);\r\n");
    prsrImpl.append(
            "  xml.open(name);\r\n\r\n");
    
    prsrImpl.append(s);
    if (isResource)
      prsrImpl.append(
          "  for i := 0 to elem.extensionList.count - 1 do\r\n"+
          "    ComposeExtension(xml, 'extension', elem.extensionList[i]);\r\n"+
          "  ComposeNarrative(xml, 'text', elem.text);\r\n");
    prsrImpl.append(
            "  xml.close(name);\r\n"+
            "end;\r\n\r\n"
        );

    s = workingParserJ.toString();
    prsrImpl.append(
            "function TFHIRJsonParser.Parse"+tn.substring(1)+" : "+tn+";\r\n"+
            "begin\r\n"+
            "  json.next;\r\n"+    
            "  result := "+tn+".create;\r\n"+
            "  try\r\n"+
            "    while (json.ItemType <> jpitEnd) do\r\n"+
            "    begin\r\n"+
            "      if (json.ItemName = '@id') then\r\n"+
            "        result.xmlId := json.itemValue\r\n"+
            "      else if (json.ItemName = '_xml_comments') then\r\n"+
            "        parseComments(result.xml_comments)\r\n");
    prsrImpl.append(s);
    if (isResource)
      prsrImpl.append(
          "      else if (json.ItemName = 'extension') then\r\n"+
          "      begin\r\n"+
          "        json.checkState(jpitArray);\r\n"+
          "        json.Next;\r\n"+
          "        while (json.ItemType <> jpitEnd) do\r\n"+
          "        begin\r\n"+
          "          result.extensionList.Add(ParseExtension);\r\n"+
          "          json.Next;\r\n"+
          "        end;\r\n"+
          "      end\r\n"+
          "      else if (json.ItemName = 'text') then\r\n"+
          "        result.text := ParseNarrative\r\n");
    prsrImpl.append(
            "      else\r\n"+
            "         UnknownContent;\r\n"+
            "      json.next;\r\n"+    
            "    end;\r\n"+
            "\r\n"+
            "    result.link;\r\n"+
            "  finally\r\n"+
            "    result.free;\r\n"+
            "  end;\r\n"+
            "end;\r\n\r\n"
        );

    s = workingComposerJ.toString();
    prsrImpl.append(
            "procedure TFHIRJsonComposer.Compose"+tn.substring(1)+"(json : TJSONWriter; name : string; elem : "+tn+");\r\n");
    if (s.contains("for i := "))
      prsrImpl.append("var\r\n  i : integer;\r\n");
    prsrImpl.append(
            "begin\r\n"+
            "  if (elem = nil) then\r\n    exit;\r\n"+
            "  json.valueObject(name);\r\n"+
            "  Comments(json, elem);\r\n"+
            "  Prop(json, '@id', elem.xmlId);\r\n");
    prsrImpl.append(s);
    if (isResource)
      prsrImpl.append(
          "  if elem.extensionList.Count > 0 then\r\n"+
          "  begin\r\n"+
          "    json.valueArray('extension');\r\n"+
          "    for i := 0 to elem.extensionList.count - 1 do\r\n"+
          "      ComposeExtension(json, '', elem.extensionList[i]);\r\n"+
          "    json.FinishArray;\r\n"+
          "  end;\r\n"+
          "  ComposeNarrative(json, 'text', elem.text);\r\n");
    prsrImpl.append(
            "  json.finishObject;\r\n"+
            "end;\r\n\r\n"
        );

  }

//  private boolean hasLists(ElementDefn e) {
//    for (ElementDefn c : e.getElements()) {
//      if (c.unbounded())
//        return true;
//    }
//    return false;
//  }

  private void scanNestedTypes(ElementDefn root, String path, ElementDefn e) throws Exception {
    String tn = null;
    if (e.typeCode().equals("code") && e.hasBinding()) {
      BindingSpecification cd = getConceptDomain(e.getBindingName());
      if (cd != null && cd.getBinding() == BindingSpecification.Binding.CodeList) {
        tn = "T"+getTitle(getCodeList(cd.getReference()).substring(1));
        if (!enumNames.contains(tn)) {
          enumNames.add(tn);
          enums.add(e);
        }
        typeNames.put(e,  tn);
      }
    }
    if (tn == null) {
      if (e.usesCompositeType()) {
        tn = typeNames.get(getElementForPath(root, e.typeCode().substring(1)));
        typeNames.put(e,  tn);
      } else if (e.getTypes().size() > 0) {
        boolean hasId = root.typeCode().equals("Resource") || e.unbounded();
        tn = getTypeName(e, hasId);
        typeNames.put(e,  tn);
      } else 
      {
        tn = "T"+path+getTitle(e.getName());
        strucs.add(e);
        typeNames.put(e,  tn);
        for (ElementDefn c : e.getElements()) {
          scanNestedTypes(root, path+getTitle(e.getName()), c);
        }
      }
    }
  }

  private Object getElementForPath(ElementDefn root, String pathname) throws Exception {
    String[] path = pathname.split("\\.");
    if (!path[0].equals(root.getName()))
      throw new Exception("Element Path '"+pathname+"' is not legal in this context");
    ElementDefn res = root;
    for (int i = 1; i < path.length; i++)
    {
      String en = path[i];
      if (en.length() == 0)
        throw new Exception("Improper path "+pathname);
      ElementDefn t = res.getElementByName(en);
      if (t == null) {
        throw new Exception("unable to resolve "+pathname);
      }
      res = t; 
    }
    return res;

  }

  private String getCodeList(String binding) {
    StringBuilder b = new StringBuilder();
    boolean up = true;
    for (char ch: binding.toCharArray()) {
      if (ch == '-')
        up = true;
      else if (up) {
        b.append(Character.toUpperCase(ch));
        up = false;
      }
      else        
        b.append(ch);
    }
    return b.toString();
  }

  private BindingSpecification getConceptDomain(String conceptDomain) {
    for (BindingSpecification cd : definitions.getBindings().values())
      if (cd.getName().equals(conceptDomain))
        return cd;
    return null;
  }

  private void generateField(ElementDefn e, StringBuilder defPriv1, StringBuilder defPriv2, StringBuilder defPub, StringBuilder impl, StringBuilder create, StringBuilder destroy, StringBuilder assign, StringBuilder getkids, StringBuilder getprops, String cn, String pt, Boolean isType, boolean listsAreWrapped, ClassCategory category) throws Exception {
    String tn;
    if (e.getTypes().size() > 0 && e.getTypes().get(0).isUnboundGenericParam())
      tn = pt;
    else
      tn = typeNames.get(e);
    if (tn == null) {
      if (e.getName().equals("extension"))
        tn = "TExtension";
      else
        tn = getTypeName(e, isType || e.unbounded());
    }
    

    String parse = null;
    String ttb = "";
    String tta = "";
    String propV = "F"+getTitle(getElementName(e.getName()));
    if (typeIsSimple(tn)) {
      if (enumNames.contains(tn)) {        
        parse = tn+"(ParseEnum(CODES_"+tn+", child))";
        ttb = "CODES_"+tn+"[";
        tta = "]";
        propV = "CODES_"+tn+"["+propV+ "]";
      } else if (tn.equals("Integer")) {
        parse = "StringToInteger32(child.text)";
        ttb = "inttostr(";
        tta = ")";
        propV = "inttostr("+propV+ ")";
      } else if (tn.equals("Boolean")) {
        parse = "StringToBoolean(child.text)";
        propV = "booleanToString("+propV+ ")";
      } else if (tn.equals("TDateTime")) {
        parse = "XMLDateTimeStringToDateTime(child.text)";
        propV = "DateTimeToXMLDateTimeString("+propV+")";
      } else if (tn.equals("TFhirXHtmlNode"))
        parse = "ParseXhtml(child)";
//      else if (tn.equals("TXmlIdReference"))
//        parse = "GetAttribute(child, 'idref')";
      else
        parse = "child.text";
    } else if (tn.equals("TSmartDecimal")) 
      propV = propV+".asString";
    else 
      propV = propV+".Link";
    
    String parseJ = null;
    if (typeIsSimple(tn)) {
      if (enumNames.contains(tn))
        parseJ = tn+"(ParseEnum(CODES_"+tn+"))";
      else if (tn.equals("Integer"))
        parseJ = "StringToInteger32(json.itemValue)";
      else if (tn.equals("Boolean"))
        parseJ = "StringToBoolean(json.itemValue)";
      else if (tn.equals("TDateTime"))
        parseJ = "XMLDateTimeStringToDateTime(json.itemValue)";
      else if (tn.equals("TFhirXHtmlNode"))
        parseJ = "ParseXhtml()";
      else
        parseJ = "json.itemValue";
    }
    String srlsd = "Text";
    String srlsdJ = "Prop";
    String srls = "#";
    if (typeIsSimple(tn)) {
      if (enumNames.contains(tn)) {
        srls = "CODES_"+tn+"[#]";
      } else if (tn.equals("Integer")) {
        srls = "IntegerToString(#)";
      } else if (tn.equals("Boolean")) {
        srls = "BooleanToString(#)";
      } else if (tn.equals("TDateTime")) {
        srls = "DateTimeToXMLDateTimeString(#)";
      };
    }
    
    
    String s = getElementName(e.getName()); 
    if (e.unbounded()) {
      String tnl;
      if (tn.contains("{"))
        tnl = tn.substring(0, tn.indexOf('{'))+"List"+tn.substring(tn.indexOf('{'));
      else if (tn.equals("String"))
        tnl = "TStringList";
      else
        tnl = tn+"List";
      s = s+"List";
      defPriv1.append("    F"+s+" : "+tnl+";\r\n");
      defPub.append("    {@member "+s+"\r\n");
      defPub.append("      "+e.getDefinition()+"\r\n");
      defPub.append("    }\r\n");
      defPub.append("    property "+s+" : "+tnl+" read F"+getTitle(s)+";\r\n");
      defPub.append("\r\n");
      create.append("  F"+getTitle(s)+" := "+tnl+".Create;\r\n");
      destroy.append("  F"+getTitle(s)+".Free;\r\n");
      assign.append("  F"+getTitle(s)+".Assign("+cn+"(oSource).F"+getTitle(s)+");\r\n");
      getkids.append("  if (child_name = '"+getElementName(e.getName())+"') Then\r\n     list.addAll(F"+getTitle(s)+");\r\n");
      getprops.append("  oList.add(TFHIRProperty.create(self, '"+e.getName()+"', '"+e.typeCode()+"', F"+getTitle(s)+".Link)){3};\r\n");
      
      defineList(tn, tnl, category);
      if (!typeIsSimple(tn)) {
        if (!e.getName().equals("[type]") && !e.getName().contains("[x]")) {
          parse = "Parse"+parseName(tn)+"(child)";
          parseJ = "Parse"+parseName(tn)+"";
          srlsd = "Compose"+parseName(tn);
          srlsdJ = "Compose"+parseName(tn);
        } else {
          throw new Exception("not supported");
        }
      };
        workingParserX.append("      else if (child.baseName = '"+e.getName()+"') then\r\n"+
            "        result."+s+".Add("+parse+")\r\n");
        workingComposerX.append("  for i := 0 to elem."+s+".Count - 1 do\r\n"+
            "    "+srlsd+"(xml, '"+e.getName()+"', "+srls.replace("#", "elem."+s+"[i]")+");\r\n");
      workingParserJ.append("      else if (json.ItemName = '"+e.getName()+"') then\r\n"+
          "      begin\r\n"+
          "        json.checkState(jpitArray);\r\n"+
          "        json.Next;\r\n"+
          "        while (json.ItemType <> jpitEnd) do\r\n"+
          "        begin\r\n"+
          "          result."+s+".Add("+parseJ+");\r\n"+
          "          json.Next;\r\n"+
          "        end;\r\n"+
          "      end\r\n");

      workingComposerJ.append("  if elem."+s+".Count > 0 then\r\n"+
          "  begin\r\n"+
          "    json.valueArray('"+e.getName()+"');\r\n"+
          "    for i := 0 to elem."+s+".Count - 1 do\r\n"+
          "      "+srlsdJ+"(json, '',"+srls.replace("#", "elem."+s+"[i]")+");\r\n"+
          "    json.FinishArray;\r\n"+
          "  end;\r\n");
    } else {
      defPriv1.append("    F"+getTitle(s)+" : "+tn+";\r\n");
      defPriv2.append("    Procedure Set"+getTitle(s)+"(value : "+tn+");\r\n");
      defPub.append("    {@member "+s+"\r\n");
      defPub.append("      "+e.getDefinition()+"\r\n");
      defPub.append("    }\r\n");
      defPub.append("    property "+s+" : "+tn+" read F"+getTitle(s)+" write Set"+getTitle(s)+";\r\n");
      defPub.append("\r\n");
      if (typeIsSimple(tn) && !tn.equals("TFhirXHtmlNode")) {
        impl.append("Procedure "+cn+".Set"+getTitle(s)+"(value : "+tn+");\r\nbegin\r\n  F"+getTitle(s)+" := value;\r\nend;\r\n\r\n");
        assign.append("  F"+getTitle(s)+" := "+cn+"(oSource).F"+getTitle(s)+";\r\n");
        getkids.append("  if (child_name = '"+getElementName(e.getName())+"') Then\r\n     list.add(TFHIRObjectText.create("+ttb+getTitle(s)+tta+"));\r\n");
        getprops.append("  oList.add(TFHIRProperty.create(self, '"+e.getName()+"', '"+e.typeCode()+"', "+propV+"));{1}\r\n");
        workingParserX.append("      else if (child.baseName = '"+e.getName()+"') then\r\n        result."+s+" := "+parse+"\r\n");
        workingParserJ.append("      else if (json.ItemName = '"+e.getName()+"') then\r\n        result."+s+" := "+parseJ+"\r\n");
//        if (tn.equals("TXmlIdReference")) {
//          workingComposerX.append("  if (elem."+e.getName()+" <> '') then\r\n");
//          workingComposerX.append("  begin\r\n");
//          workingComposerX.append("    attribute(xml, 'idref', elem."+e.getName()+");\r\n");
//          workingComposerX.append("    xml.Tag('"+e.getName()+"');\r\n");
//          workingComposerX.append("  end;\r\n");
//        } else
          workingComposerX.append("  Text(xml, '"+e.getName()+"',"+srls.replace("#", "elem."+s)+");\r\n");
        workingComposerJ.append("  Prop(json, '"+e.getName()+"',"+srls.replace("#", "elem."+s)+");\r\n");
      }
      else {
        impl.append("Procedure "+cn+".Set"+getTitle(s)+"(value : "+tn+");\r\nbegin\r\n  F"+getTitle(s)+".free;\r\n  F"+getTitle(s)+" := value;\r\nend;\r\n\r\n");
        destroy.append("  F"+getTitle(s)+".free;\r\n");
        assign.append("  "+s+" := "+cn+"(oSource)."+s+".Clone;\r\n");
        getkids.append("  if (child_name = '"+getElementName(e.getName())+"') Then\r\n     list.add("+getTitle(s)+".Link);\r\n");
        getprops.append("  oList.add(TFHIRProperty.create(self, '"+e.getName()+"', '"+e.typeCode()+"', "+propV+"));{2}\r\n");
        if (e.getName().contains("[x]") && e.getTypes().size() > 1) {
          String pfx = e.getName().replace("[x]", "");
          int t = e.getTypes().size();
          int i = 0;
          for (TypeRef td : e.getTypes()) {
            if (td.hasParams()) {
              for (String ptn : td.getParams()) {
                workingParserX.append("      else if (child.baseName = '"+pfx+getTitle(td.getName())+"_"+getTitle(ptn)+"') then\r\n        result."+s+" := Parse"+getTitle(td.getName())+"_"+getTitle(ptn)+"(child)\r\n");
                workingComposerX.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is T"+getTitle(td.getName())+"_"+getTitle(ptn)+" {2} then\r\n    Compose"+getTitle(td.getName())+"_"+getTitle(ptn)+"(xml, '"+pfx+getTitle(td.getName())+"_"+getTitle(ptn)+"', T"+getTitle(td.getName())+"_"+getTitle(ptn)+"(elem."+s+"))"+(i == t-1?";" : "")+"\r\n");
                workingParserJ.append("      else if (json.ItemName = '"+pfx+getTitle(td.getName())+"_"+getTitle(ptn)+"') then\r\n        result."+s+" := Parse"+getTitle(td.getName())+"_"+getTitle(ptn)+"\r\n");
                workingComposerJ.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is T"+getTitle(td.getName())+"_"+getTitle(ptn)+" then\r\n    Compose"+getTitle(td.getName())+"_"+getTitle(ptn)+"(json, '"+pfx+getTitle(td.getName())+"_"+getTitle(ptn)+"', T"+getTitle(td.getName())+"_"+getTitle(ptn)+"(elem."+s+"))"+(i == t-1?";" : "")+"\r\n");
              }
            }
            else { 
              workingParserX.append("      else if (child.baseName = '"+pfx+getTitle(td.getName())+"') then\r\n        result."+s+" := Parse"+getTitle(td.getName())+"(child)\r\n");
              if (td.getName().equalsIgnoreCase("string")) {
                workingComposerX.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is TFHIR"+getTitle(td.getName())+" {3}  then\r\n    Text(xml, '"+pfx+getTitle(td.getName())+"', TFHIR"+getTitle(td.getName())+"(elem."+s+").value)"+(i == t-1?";" : "")+"\r\n");
                workingComposerJ.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is TFHIR"+getTitle(td.getName())+" then\r\n    Prop(json, '"+pfx+getTitle(td.getName())+"', TFHIR"+getTitle(td.getName())+"(elem."+s+").value)"+(i == t-1?";" : "")+"\r\n");
              } else if (td.getName().equalsIgnoreCase("code")) {
                  workingComposerX.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is TFHIRString {4} then\r\n    Text(xml, '"+pfx+getTitle(td.getName())+"', TFHIRString(elem."+s+").value)"+(i == t-1?";" : "")+"\r\n");
                  workingComposerJ.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is TFHIRString then\r\n    Prop(json, '"+pfx+getTitle(td.getName())+"', TFHIRString(elem."+s+").value)"+(i == t-1?";" : "")+"\r\n");
              } else if (td.getName().equalsIgnoreCase("boolean") ) {
                  workingComposerX.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is TFHIR"+getTitle(td.getName())+" {5} then\r\n    Text(xml, '"+pfx+getTitle(td.getName())+"', BooleanToString(TFHIR"+getTitle(td.getName())+"(elem."+s+").value))"+(i == t-1?";" : "")+"\r\n");
                  workingComposerJ.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is TFHIR"+getTitle(td.getName())+" then\r\n    Prop(json, '"+pfx+getTitle(td.getName())+"', BooleanToString(TFHIR"+getTitle(td.getName())+"(elem."+s+").value))"+(i == t-1?";" : "")+"\r\n");
              } else {
                workingComposerX.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is "+getTypeName(td.getName(), true)+" {6} then\r\n    Compose"+getTitle(td.getName())+"(xml, '"+pfx+getTitle(td.getName())+"', "+getTypeName(td.getName(), true)+"(elem."+s+"))"+(i == t-1?";" : "")+"\r\n");
                workingComposerJ.append("  "+(i==0 ? "if" : "else if")+" elem."+s+" is "+getTypeName(td.getName(), true)+" then\r\n    Compose"+getTitle(td.getName())+"(json, '"+pfx+getTitle(td.getName())+"', "+getTypeName(td.getName(), true)+"(elem."+s+"))"+(i == t-1?";" : "")+"\r\n");
              }
              workingParserJ.append("      else if (json.ItemName = '"+pfx+getTitle(td.getName())+"') then\r\n        result."+s+" := Parse"+getTitle(td.getName())+"\r\n");
            }
            i++;
          }
          
        } else if (!e.getName().equals("[type]") && !e.getName().contains("[x]")) {
          workingParserX.append("      else if (child.baseName = '"+e.getName()+"') then\r\n        result."+s+" := Parse"+parseName(tn)+"(child)\r\n");
          workingComposerX.append("  Compose"+parseName(tn)+"(xml, '"+e.getName()+"', elem."+s+");\r\n");
          workingParserJ.append("      else if (json.ItemName = '"+e.getName()+"') then\r\n        result."+s+" := Parse"+parseName(tn)+"\r\n");
          workingComposerJ.append("  Compose"+parseName(tn)+"(json, '"+e.getName()+"', elem."+s+");\r\n");
        } else {
          String pfx = e.getName().contains("[x]") ? e.getName().replace("[x]", "") : "";
          int i = 0;
          for (DefinedCode cd : definitions.getPrimitives().values()) {
            workingParserX.append("      else if (child.baseName = '"+pfx+getTitle(cd.getCode())+"') then\r\n        result."+s+" := Parse"+getTitle(cd.getCode())+"(child)\r\n");
            String ptn = "TFHIR"+getTitle(cd.getCode());
            if (cd.getCode().equals("base64Binary"))
              ptn = "TFHIRBytes";
            workingComposerX.append("  "+(i > 0 ? "else " : "")+"if elem."+s+" is "+ptn+" {1} then\r\n    Compose"+ptn.substring(5)+"(xml, '"+pfx+getTitle(cd.getCode())+"', "+ptn+"(elem."+s+"))\r\n");
            workingParserJ.append("      else if (json.ItemName = '"+pfx+getTitle(cd.getCode())+"') then\r\n        result."+s+" := Parse"+getTitle(cd.getCode())+"\r\n");
            workingComposerJ.append("  "+(i > 0 ? "else " : "")+"if elem."+s+" is "+ptn+" then\r\n    Compose"+ptn.substring(5)+"(json, '"+pfx+getTitle(cd.getCode())+"', "+ptn+"(elem."+s+"))\r\n");
            i++;
          }
          for (ElementDefn ed : definitions.getTypes().values()) {
            if (ed.getTypes().get(0).getName().equals("GenericType")) {
              for (TypeRef td : definitions.getKnownTypes()) {
                if (td.getName().equals(ed.getName()) && td.hasParams()) {
                  for (String ptn : td.getParams()) {
                    workingParserX.append("      else if (child.baseName = '"+pfx+getTitle(ed.getName())+"_"+getTitle(ptn)+"')  then\r\n        result."+s+" := Parse"+getTitle(ed.getName())+"_"+getTitle(ptn)+"(child)\r\n");
                    workingComposerX.append("  else if elem."+s+" is T"+getTitle(ed.getName())+"_"+getTitle(ptn)+" {7} then\r\n   Compose"+getTitle(ed.getName())+"_"+getTitle(ptn)+"(xml, '"+pfx+getTitle(ed.getName())+"_"+getTitle(ptn)+"', T"+getTitle(ed.getName())+"_"+getTitle(ptn)+"(elem."+s+"))\r\n");
                    workingParserJ.append("      else if (json.ItemName = '"+pfx+getTitle(ed.getName())+"_"+getTitle(ptn)+"') then\r\n        result."+s+" := Parse"+getTitle(ed.getName())+"_"+getTitle(ptn)+"\r\n");
                    workingComposerJ.append("  else if elem."+s+" is T"+getTitle(ed.getName())+"_"+getTitle(ptn)+" then\r\n    Compose"+getTitle(ed.getName())+"_"+getTitle(ptn)+"(json, '"+pfx+getTitle(ed.getName())+"_"+getTitle(ptn)+"', T"+getTitle(ed.getName())+"_"+getTitle(ptn)+"(elem."+s+"))\r\n");
                  }
                }
              }
            } else {
              workingParserX.append("      else if (child.baseName = '"+pfx+getTitle(ed.getName())+"') then\r\n        result."+s+" := Parse"+getTitle(ed.getName())+"(child)\r\n");
              workingComposerX.append("  else if elem."+s+" is T"+getTitle(ed.getName())+" {8} then\r\n    Compose"+getTitle(ed.getName())+"(xml, '"+pfx+getTitle(ed.getName())+"', T"+getTitle(ed.getName())+"(elem."+s+"))\r\n");
              workingParserJ.append("      else if (json.ItemName = '"+pfx+getTitle(ed.getName())+"') then\r\n        result."+s+" := Parse"+getTitle(ed.getName())+"\r\n");
              workingComposerJ.append("  else if elem."+s+" is T"+getTitle(ed.getName())+" then\r\n    Compose"+getTitle(ed.getName())+"(json, '"+pfx+getTitle(ed.getName())+"', T"+getTitle(ed.getName())+"(elem."+s+"))\r\n");
            }
          }
          int t = definitions.getStructures().size();
          i = 0;
          for (ElementDefn ed : definitions.getStructures().values()) {
            workingParserX.append("      else if (child.baseName = '"+pfx+getTitle(ed.getName())+"') then\r\n        result."+s+" := Parse"+getTitle(ed.getName())+"(child)\r\n");
            workingComposerX.append("  else if elem."+s+" is T"+getTitle(ed.getName())+" {9} then\r\n    Compose"+getTitle(ed.getName())+"(xml, '"+pfx+getTitle(ed.getName())+"', T"+getTitle(ed.getName())+"(elem."+s+"))"+(i < t-1 ? "" : ";")+"\r\n");
            workingParserJ.append("      else if (json.ItemName = '"+pfx+getTitle(ed.getName())+"') then\r\n        result."+s+" := Parse"+getTitle(ed.getName())+"\r\n");
            workingComposerJ.append("  else if elem."+s+" is T"+getTitle(ed.getName())+" then\r\n    Compose"+getTitle(ed.getName())+"(json, '"+pfx+getTitle(ed.getName())+"', T"+getTitle(ed.getName())+"(elem."+s+"))"+(i < t-1 ? "" : ";")+"\r\n");
            i++;
          }
        }
      }
    }

  }

  private String parseName(String tn) {
    return tn.startsWith("TFHIR") ? tn.substring(5) : tn.substring(1);
  }

  private void defineList(String tn, String tnl, ClassCategory category) {
    if (tnl.contains("{"))
      tnl = tnl.substring(0, tnl.indexOf("{"));
    if (tn.contains("{"))
      tn = tn.substring(0, tn.indexOf("{"));
    if (!lists.contains(tnl) && !tnl.equals("TStringList")) {
      lists.add(tn+"List");
      String tt = tn.substring(1);
      getCode(category).classFwds.add("  "+tn+"List = class;\r\n");
      getCode(category).classDefs.add(
        "  {@Class "+tn+"List\r\n"+
        "    A list of "+tt+"\r\n"+
        "  }\r\n"+
        "  {!.Net HL7Connect.Fhir."+tn.substring(1)+"List}\r\n"+
        "  "+tn+"List = class (TFHIRObjectList)\r\n"+
        "  private\r\n"+
        "    function GetItemN(index : Integer) : "+tn+";\r\n"+
        "    procedure SetItemN(index : Integer; value : "+tn+");\r\n"+
        "  public\r\n"+
        "    {!script hide}\r\n"+
        "    function Link : "+tn+"List; Overload;\r\n"+
        "    function Clone : "+tn+"List; Overload;\r\n"+
        "    {!script show}\r\n"+
        "    \r\n"+
        "    {@member Append\r\n"+
        "      Add a "+tt+" to the end of the list.\r\n"+
        "    }\r\n"+
        "    function Append : "+tn+";\r\n"+
        "    \r\n"+
        "    {@member AddItem\r\n"+
        "      Add an already existing "+tt+" to the end of the list.\r\n"+
        "    }\r\n"+
        "    procedure AddItem(value : "+tn+");\r\n"+
        "    \r\n"+
        "    {@member IndexOf\r\n"+
        "      See if an item is already in the list. returns -1 if not in the list\r\n"+
        "    }\r\n"+
        "    \r\n"+
        "    {@member IndexOf\r\n"+
        "      See if an item is already in the list. returns -1 if not in the list\r\n"+
        "    }\r\n"+
        "    function IndexOf(value : "+tn+") : Integer;\r\n"+
        "    \r\n"+
        "    {@member Insert\r\n"+
        "      Insert "+tt+" before the designated index (0 = first item)\r\n"+
        "    }\r\n"+
        "    function Insert(index : Integer) : "+tn+";\r\n"+
        "    \r\n"+
        "    {@member InsertItem\r\n"+
        "       Insert an existing "+tt+" before the designated index (0 = first item)\r\n"+
        "    }\r\n"+
        "    procedure InsertItem(index : Integer; value : "+tn+");\r\n"+
        "    \r\n"+
        "    {@member Item\r\n"+
        "       Get the iIndexth "+tt+". (0 = first item)\r\n"+
        "    }\r\n"+
        "    \r\n"+
        "    {@member Item\r\n"+
        "       Get the iIndexth "+tt+". (0 = first item)\r\n"+
        "    }\r\n"+
        "    procedure SetItemByIndex(index : Integer; value : "+tn+");\r\n"+
        "    \r\n"+
        "    {@member Count\r\n"+
        "      The number of items in the collection\r\n"+
        "    }\r\n"+
        "    function Item(index : Integer) : "+tn+";\r\n"+
        "    \r\n"+
        "    {@member Count\r\n"+
        "      The number of items in the collection\r\n"+
        "    }\r\n"+
        "    function Count : Integer; Overload;\r\n"+
        "    \r\n"+
        "    {@member remove\r\n"+
        "      Remove the indexth item. The first item is index 0.\r\n"+
        "    }\r\n"+
        "    procedure Remove(index : Integer);\r\n"+
        "    {@member ClearItems\r\n"+
        "      Remove All Items from the list\r\n"+
        "    }\r\n"+
        "    procedure ClearItems;\r\n"+
        "    \r\n"+
        "    Property "+Utilities.pluralizeMe(tt)+"[index : Integer] : "+tn+" read GetItemN write SetItemN; default;\r\n"+
        "  End;\r\n"+
        "\r\n"  
          );
      getCode(category).classImpls.add(
        "{ "+tn+"List }\r\n"+
        "{ "+tn+"List }\r\n"+
        "procedure "+tn+"List.AddItem(value: "+tn+");\r\n"+
        "begin\r\n"+
        "  assert(value.ClassName = '"+tn+"', 'Attempt to add an item of type '+value.ClassName+' to a List of "+tn+"');\r\n"+
        "  add(value);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.Append: "+tn+";\r\n"+
        "begin\r\n"+
        "  result := "+tn+".create;\r\n"+
        "  try\r\n"+
        "    add(result.Link);\r\n"+
        "  finally\r\n"+
        "    result.free;\r\n"+
        "  end;\r\n"+
        "end;\r\n"+
        "\r\n"+
        "procedure "+tn+"List.ClearItems;\r\n"+
        "begin\r\n"+
        "  Clear;\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.Clone: "+tn+"List;\r\n"+
        "begin\r\n"+
        "  result := "+tn+"List(inherited Clone);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.Count: Integer;\r\n"+
        "begin\r\n"+
        "  result := Inherited Count;\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.GetItemN(index: Integer): "+tn+";\r\n"+
        "begin\r\n"+
        "  result := "+tn+"(ObjectByIndex[index]);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.IndexOf(value: "+tn+"): Integer;\r\n"+
        "begin\r\n"+
        "  result := IndexByReference(value);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.Insert(index: Integer): "+tn+";\r\n"+
        "begin\r\n"+
        "  result := "+tn+".create;\r\n"+
        "  try\r\n"+
        "    inherited insert(index, result);\r\n"+
        "  finally\r\n"+
        "    result.free;\r\n"+
        "  end;\r\n"+
        "end;\r\n"+
        "\r\n"+
        "procedure "+tn+"List.InsertItem(index: Integer; value: "+tn+");\r\n"+
        "begin\r\n"+
        "  assert(value is "+tn+");\r\n"+
        "  Inherited Insert(index, value);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.Item(index: Integer): "+tn+";\r\n"+
        "begin\r\n"+
        "  result := "+tn+"(ObjectByIndex[index]);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "function "+tn+"List.Link: "+tn+"List;\r\n"+
        "begin\r\n"+
        "  result := "+tn+"List(inherited Link);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "procedure "+tn+"List.Remove(index: Integer);\r\n"+
        "begin\r\n"+
        "  DeleteByIndex(index);\r\n"+
        "end;\r\n"+
        "\r\n"+
        "procedure "+tn+"List.SetItemByIndex(index: Integer; value: "+tn+");\r\n"+
        "begin\r\n"+
        "  assert(value is "+tn+");\r\n"+
        "  "+Utilities.pluralizeMe(tt)+"[index] := value;\r\n"+
        "end;\r\n"+
        "\r\n"+
        "procedure "+tn+"List.SetItemN(index: Integer; value: "+tn+");\r\n"+
        "begin\r\n"+
        "  assert(value is "+tn+");\r\n"+
        "  ObjectByIndex[index] := value;\r\n"+
        "end;\r\n"        
          ); 
    }
  }

  private boolean typeIsSimple(String tn) {
    if (tn == null)
      return false;
    return tn.equals("String") || tn.equals("Integer") || tn.equals("Boolean") || tn.equals("TDateTime") || tn.equals("TFhirXHtmlNode")  || tn.equals("TXmlIdReference") || enumNames.contains(tn);
  }

  private String getTitle(String name) {
    if (name.length() < 2)
      return name.toUpperCase();
    else
      return name.substring(0, 1).toUpperCase()+ name.substring(1);
  }

  private String getElementName(String name) {
    if (GeneratorUtils.isDelphiReservedWord(name))
      return name+"_";
    return name.replace("[x]", "").replace("[type]", "value");
  }

  private String getTypeName(ElementDefn e, boolean hasId) throws Exception {
    if (e.getTypes().size() > 1) {
      return "TFHIRType";
    } else if (e.getTypes().size() == 0) {
      throw new Exception("not supported");
    } else {
      return getTypename(e.getTypes().get(0), hasId);
    }
  }

  private String getTypename(TypeRef type, boolean complex) throws Exception {
    if (type.getParams().size() == 1) {     
      if (type.isResourceReference())
        return "TResourceReference{"+getTypeName(type.getParams().get(0), complex)+"}";
      else if (type.getName().equals("Interval"))
        return "TInterval_"+type.getParams().get(0);
      else
        throw new Exception("not supported: "+type.summary());
    } else if (type.getParams().size() > 1) {
      if (type.isResourceReference())
        return "TResourceReference{Resource}";
      else
        throw new Exception("not supported");
    } else {
      return getTypeName(type.getName(), complex);
    }
  }

  private String getTypeName(String tn, boolean complex) {
    if (tn == null) {
      return "";
    } else if (tn.equals("string")) {
      return complex ? "TFHIRString" : "String";
    } else if (tn.equals("id")) {
      return complex ? "TFHIRString" : "String";
    } else if (tn.equals("code")) {
      return complex ? "TFHIRString" : "String";
    } else if (tn.equals("integer")) {
      return complex ? "TFHIRInteger" : "Integer";
    } else if (tn.equals("instant")) {
      return complex ? "TFHIRInstant" : "TDateTime";
    } else if (tn.equals("boolean")) {
      return complex ? "TFHIRBoolean" : "Boolean";
    } else if (tn.equals("dateTime")) {
      return complex ? "TFHIRString" : "String";
    } else if (tn.equals("date")) {
      return complex ? "TFHIRDate" : "String";
    } else if (tn.equals("uri")) {
      return complex ? "TFHIRString" : "String";
    } else if (tn.equals("decimal")) {
      return complex ? "TFHIRDecimal" : "TSmartDecimal";      
    } else if (tn.equals("xhtml")) {
      return "TFhirXHtmlNode"; 
    } else if (tn.equals("idref")) {
      return "String";
    } else if (tn.equals("base64Binary")) {
      return "TFHIRBuffer";
    } else if (tn.equals("*")) {
      return "TFHIRType";
    } else if (tn.equals("Any")) {
      return "Resource";
    } else if (definitions.getConstraints().containsKey(tn)) {
      return getTypeName(definitions.getConstraints().get(tn).getComment(), complex);
    } else {
      return "T"+getTitle(tn);
    }
  }
  
  public String getName() {
    return "delphi";
  }

  public void genConstraint(DefinedCode c) {
    prsrdefX.append("    function Parse"+c.getCode()+"(element : IXmlDomElement) : T"+c.getCode()+";\r\n");
    srlsdefX.append("    procedure Compose"+c.getCode()+"(xml : TMsXmlBuilder; name : string; elem : T"+c.getCode()+");\r\n");
    prsrdefJ.append("    function Parse"+c.getCode()+" : T"+c.getCode()+";\r\n");
    srlsdefJ.append("    procedure Compose"+c.getCode()+"(json : TJSONWriter; name : string; elem : T"+c.getCode()+");\r\n");
    defCodeType.classDefs.add("  T"+c.getCode()+" = T"+c.getComment()+";\r\n");
    prsrImpl.append("function TFHIRXmlParser.Parse"+c.getCode()+"(element : IXmlDomElement) : T"+c.getCode()+";\r\nbegin\r\n  result := Parse"+c.getComment()+"(element);\r\nend;\r\n\r\n");
    prsrImpl.append("procedure TFHIRXmlComposer.Compose"+c.getCode()+"(xml : TMsXmlBuilder; name : string; elem : T"+c.getCode()+");\r\nbegin\r\n  Compose"+c.getComment()+"(xml, name, elem);\r\nend;\r\n\r\n");
    prsrImpl.append("function TFHIRJsonParser.Parse"+c.getCode()+" : T"+c.getCode()+";\r\nbegin\r\n  result := Parse"+c.getComment()+";\r\nend;\r\n\r\n");
    prsrImpl.append("procedure TFHIRJsonComposer.Compose"+c.getCode()+"(json : TJSONWriter; name : string; elem : T"+c.getCode()+");\r\nbegin\r\n  Compose"+c.getComment()+"(json, name, elem);\r\nend;\r\n\r\n");
  }
  
  
  public void generate(Definitions definitions, String destDir, String implDir, String version, Date genDate, Logger logger)  throws Exception {
    defCodeRes = new DelphiCodeGenerator(new FileOutputStream(implDir+"FHIRResources.pas"));
    defCodeRes.start();
    defCodeRes.name = "FHIRResources";
    defCodeRes.comments.add("FHIR v"+version+" generated "+Config.DATE_FORMAT().format(genDate));
    defCodeRes.precomments.add("!Wrapper uses FHIRBase, FHIRBase_Wrapper, FHIRTypes, FHIRTypes_Wrapper, FHIRComponents, FHIRComponents_Wrapper");
    defCodeRes.uses.add("FHIRBase");
    defCodeRes.uses.add("FHIRTypes");
    defCodeRes.uses.add("FHIRComponents");
    defCodeRes.uses.add("AdvBuffers");
    defCodeRes.uses.add("SysUtils");
    defCodeRes.uses.add("DecimalSupport");
    defCodeRes.uses.add("StringSupport");
    defCodeRes.uses.add("Classes");

    defCodeType = new DelphiCodeGenerator(new FileOutputStream(implDir+"FHIRTypes.pas"));
    defCodeType.start();
    defCodeType.name = "FHIRTypes";
    defCodeType.comments.add("FHIR v"+version+" generated "+Config.DATE_FORMAT().format(genDate));
    defCodeType.precomments.add("!Wrapper uses FHIRBase, FHIRBase_Wrapper");
    defCodeType.uses.add("FHIRBase");
    defCodeType.uses.add("AdvBuffers");
    defCodeType.uses.add("SysUtils");
    defCodeType.uses.add("DecimalSupport");
    defCodeType.uses.add("StringSupport");
    defCodeType.uses.add("Classes");

    defCodeComp = new DelphiCodeGenerator(new FileOutputStream(implDir+"FHIRComponents.pas"));
    defCodeComp.start();
    defCodeComp.name = "FHIRComponents";
    defCodeComp.comments.add("FHIR v"+version+" generated "+Config.DATE_FORMAT().format(genDate));
    defCodeComp.precomments.add("!Wrapper uses FHIRBase, FHIRBase_Wrapper, FHIRTypes, FHIRTypes_Wrapper");
    defCodeComp.uses.add("FHIRBase");
    defCodeComp.uses.add("FHIRTypes");
    defCodeComp.uses.add("AdvBuffers");
    defCodeComp.uses.add("SysUtils");
    defCodeComp.uses.add("DecimalSupport");
    defCodeComp.uses.add("StringSupport");
    defCodeComp.uses.add("Classes");

    factoryIntf = new StringBuilder();
    factoryImpl = new StringBuilder();

    
    prsrCode = new DelphiCodeGenerator(new FileOutputStream(implDir+"FHIRParser.pas"));
    prsrCode.start();
    prsrCode.name = "FHIRParser";
    initParser(version, genDate);
    
    this.definitions = definitions;
    
    generateResource();
    
    for (ElementDefn n : definitions.getInfrastructure().values()) {
      generate(n, "TFHIRElement", true, false, ClassCategory.Type);
    }
    for (ElementDefn n : definitions.getTypes().values()) {
      generate(n, "TFHIRType", false, false, ClassCategory.Type);
    }
    
    for (ElementDefn n : definitions.getStructures().values()) {
      generate(n, "TFHIRType", false, false, ClassCategory.Type);
    }
    
    for (DefinedCode c : definitions.getConstraints().values()) {
      genConstraint(c);
    }
    for (ResourceDefn n : definitions.getResources().values()) {
      generate(n.getRoot(), "TFHIRResource", true, true, ClassCategory.Resource);
      genResource(n, "T"+n.getName(), "TFHIRResource", true, ClassCategory.Resource);
      prsrRegX.append("  else if element.baseName = '"+n.getName()+"' Then\r\n    result := Parse"+n.getName()+"(element)\r\n");
      srlsRegX.append("    frt"+n.getName()+": Compose"+n.getName()+"(xml, '"+n.getName()+"', T"+n.getName()+"(resource));\r\n");
      prsrRegJ.append("  else if json.ItemName = '"+n.getName()+"' Then\r\n    result := Parse"+n.getName()+"\r\n");
      srlsRegJ.append("    frt"+n.getName()+": Compose"+n.getName()+"(json, '"+n.getName()+"', T"+n.getName()+"(resource));\r\n");
    }
    
//    for (String n : ini.getPropertyNames("future-resources")) {
//      ElementDefn e = new ElementDefn();
//      e.setName(ini.getStringProperty("future-resources", n));
//      generate(e, definitions.getConceptDomains());
//    }
    defCodeRes.enumConsts.add("  FHIR_GENERATED_VERSION = '"+version+"';\r\n");
    defCodeRes.enumConsts.add("  FHIR_GENERATED_DATE = '"+new SimpleDateFormat("yyyyMMddHHmmss").format(genDate)+"';\r\n");
    defCodeRes.classDefs.add(" TFHIRResourceFactory = class (TFHIRBaseFactory)\r\n  public\r\n"+factoryIntf.toString()+"  end;\r\n");
    defCodeRes.classImpls.add(factoryImpl.toString());
    defCodeComp.finish();
    defCodeType.finish();
    defCodeRes.finish();

    prsrCode.classDefs.add(buildParser());
    prsrCode.classImpls.add(prsrImpl.toString());
    prsrCode.finish();
    
    ZipGenerator zip = new ZipGenerator(destDir+"delphi.zip");
    zip.addFiles(implDir, "", ".pas");
    zip.close();    
  }

  private void generateResource() throws Exception {
    String prefix = "frt";
    StringBuilder def = new StringBuilder();
    StringBuilder con = new StringBuilder();
    def.append("  {@Enum TFHIRResourceType\r\n");
    def.append("    Enumeration of known resource types\r\n");
    def.append("  }\r\n");
    def.append("  TFHIRResourceType = (\r\n");
    con.append("  CODES_TFHIRResourceType : Array[TFHIRResourceType] of String = (");
    
    int l = definitions.getResources().size();
    int i = 0;
    for (String s : definitions.getResources().keySet()) {
      i++;
      String s2 = prefix + getTitle(s);
      if (GeneratorUtils.isDelphiReservedWord(s2))
        s2 = s2 + "_";
      def.append("    "+s2+", {@enum.value "+definitions.getResourceByName(s).getDefinition()+" }\r\n");
      con.append("'"+s+"', ");
    }
    def.append("    "+prefix+"Binary); {@enum.value Binary Resource }\r\n");
    con.append("'Binary');");

    con.append("\r\n  PLURAL_CODES_TFHIRResourceType : Array[TFHIRResourceType] of String = (");
    i = 0;
    for (String s : definitions.getResources().keySet()) {
      i++;
      con.append("'"+Utilities.pluralizeMe(s.toLowerCase())+"', ");
    }
    con.append("'binaries');");
    con.append("\r\n  LOWERCASE_CODES_TFHIRResourceType : Array[TFHIRResourceType] of String = (");
    i = 0;
    for (String s : definitions.getResources().keySet()) {
      i++;
        con.append("'"+s.toLowerCase()+"', ");
    }
  con.append("'binary');");

    con.append("\r\n  CLASSES_TFHIRResourceType : Array[TFHIRResourceType] of TFHIRResourceClass = (");
    i = 0;
    for (String s : definitions.getResources().keySet()) {
      i++;
      con.append("T"+getTitle(s)+", ");
    }
    con.append("TBinary);");


    defCodeRes.enumDefs.add(def.toString());
    defCodeRes.enumConsts.add(con.toString());

       

    def = new StringBuilder();
    

    def.append("  {@Class TFHIRResource : TFHIRElement\r\n");
    def.append("    Base Resource Definition - id, extension, narrative\r\n");
    def.append("  }\r\n");
    def.append("  {!.Net HL7Connect.Fhir.Resource}\r\n");
    def.append("  TFHIRResource = {abstract} class (TFHIRElement)\r\n");
    def.append("  private\r\n");
    def.append("    FText : TNarrative;\r\n");
    def.append("    FFormat : TFHIRFormat;\r\n");
    def.append("    FExtensionList : TExtensionList;\r\n");
    def.append("    procedure SetText(value : TNarrative);\r\n");
    def.append("  protected\r\n");
    def.append("    function GetResourceType : TFHIRResourceType; virtual; abstract;\r\n");
    def.append("  protected\r\n");
    def.append("    Procedure GetChildrenByName(child_name : string; list : TFHIRObjectList); override;\r\n");
    def.append("    Procedure ListProperties(oList : TFHIRPropertyList; bInheritedProperties : Boolean); Override;\r\n");
    def.append("  public\r\n");
    def.append("    constructor Create; override;\r\n");
    def.append("    destructor Destroy; override;\r\n");
    def.append("    {!script hide}\r\n");
    def.append("    procedure Assign(oSource : TAdvObject); override;\r\n");
    def.append("    function Link : TFHIRResource; overload;\r\n");
    def.append("    function Clone : TFHIRResource; overload;\r\n");
    def.append("    {!script show}\r\n");
    def.append("  published\r\n");
    def.append("    Property ResourceType : TFHIRResourceType read GetResourceType;\r\n\r\n");
    def.append("    {@member extensionList\r\n");
    def.append("      Resource Extensions\r\n");
    def.append("    }\r\n");
    def.append("    property extensionList : TExtensionList read FExtensionList;\r\n");
    def.append("    {@member text\r\n");
    def.append("      Text summary of resource, for human interpretation\r\n");
    def.append("    }\r\n");
    def.append("    property text : TNarrative read FText write SetText;\r\n");
    def.append("    {@member _source_format\r\n");
    def.append("      Whether the resource was first represented in XML or JSON\r\n");
    def.append("    }\r\n");
    def.append("    property _source_format : TFHIRFormat read FFormat write FFormat;\r\n");
    def.append("  end;\r\n");
    def.append("  \r\n");
    def.append("  TFHIRResourceClass = class of TFHIRResource;\r\n");
    def.append("  \r\n");
    def.append("  \r\n");
    def.append("  {@Class TBinary : TFHIRResource\r\n");
    def.append("    Special Binary Resource\r\n");
    def.append("  }\r\n");
    def.append("  {!.Net HL7Connect.Fhir.Binary}\r\n");
    def.append("  TBinary = class (TFHIRResource)\r\n");
    def.append("  private\r\n");
    def.append("    FContent : TAdvBuffer;\r\n");
    def.append("    FContentType : string;\r\n");
    def.append("  protected\r\n");
    def.append("    function GetResourceType : TFHIRResourceType; override;\r\n");
    def.append("  public\r\n");
    def.append("    Constructor create; Overload; Override;\r\n");
    def.append("    Destructor Destroy; Override;\r\n");
    def.append("    Property Content : TAdvBuffer read FContent;\r\n");
    def.append("    Property ContentType : string read FContentType write FContentType;\r\n");
    def.append("  end;\r\n");
    def.append("  \r\n");
    
    def.append("\r\n");
    StringBuilder impl2 = new StringBuilder();
    impl2.append("{ TFHIRResource }\r\n\r\n");
    impl2.append("constructor TFHIRResource.Create;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  FExtensionList := TExtensionList.create;\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n\r\n");

    impl2.append("destructor TFHIRResource.Destroy;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  FText.Free;\r\n");
    impl2.append("  FExtensionList.Free;\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n\r\n");
    
    impl2.append("procedure TFHIRResource.GetChildrenByName(child_name : string; list : TFHIRObjectList);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("  if (child_name = 'extension') then\r\n    list.addAll(FExtensionList);\r\n");
    impl2.append("  if (child_name = 'text') then\r\n    list.add(text.Link);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure TFHIRResource.ListProperties(oList: TFHIRPropertyList; bInheritedProperties: Boolean);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("  oList.add(TFHIRProperty.create(self, 'extension', 'Extension', FExtensionList.Link));\r\n");
    impl2.append("  oList.add(TFHIRProperty.create(self, 'text', 'Narrative', FText.Link));\r\n");
    impl2.append("end;\r\n\r\n");
    
    
    impl2.append("procedure TFHIRResource.Assign(oSource : TAdvObject);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("  FFormat := TFHIRResource(oSource).FFormat;\r\n");
    impl2.append("  extensionList.assign(TFHIRResource(oSource).extensionList);\r\n");
    impl2.append("  text := TFHIRResource(oSource).text.Clone;\r\n");
    impl2.append("end;\r\n\r\n");
    
    impl2.append("function TFHIRResource.Link : TFHIRResource;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := TFHIRResource(inherited Link);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("function TFHIRResource.Clone : TFHIRResource;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := TFHIRResource(inherited Clone);\r\n");
    impl2.append("end;\r\n\r\n");
    impl2.append("procedure TFHIRResource.SetText(value : TNarrative);\r\n");
    impl2.append("begin\r\n");
    impl2.append("  FText.Free;\r\n");
    impl2.append("  FText := value;\r\n");
    impl2.append("end;\r\n\r\n");

    impl2.append("constructor TBinary.create;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("  FContent := TAdvBuffer.create;\r\n");
    impl2.append("end;\r\n");
    impl2.append("\r\n");
    impl2.append("destructor TBinary.Destroy;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  FContent.free;\r\n");
    impl2.append("  inherited;\r\n");
    impl2.append("end;\r\n");
    impl2.append("\r\n");    
    impl2.append("function TBinary.GetResourceType : TFHIRResourceType;\r\n");
    impl2.append("begin\r\n");
    impl2.append("  result := frtBinary;\r\n");
    impl2.append("end;\r\n");
    impl2.append("\r\n");    
    defCodeRes.classDefs.add(def.toString());
    defCodeRes.classImpls.add(impl2.toString());
    defCodeRes.classFwds.add("  TFHIRResource = class;\r\n");
    
  }

  private StringBuilder prsrRegX = new StringBuilder();
  private StringBuilder srlsRegX = new StringBuilder();
  private StringBuilder prsrRegJ = new StringBuilder();
  private StringBuilder srlsRegJ = new StringBuilder();
  private StringBuilder prsrImpl = new StringBuilder();
  private StringBuilder prsrdefX = new StringBuilder();
  private StringBuilder srlsdefX = new StringBuilder();
  private StringBuilder prsrdefJ = new StringBuilder();
  private StringBuilder srlsdefJ = new StringBuilder();
  
  private void initParser(String version, Date genDate) {
    prsrCode.uses.add("SysUtils");
    prsrCode.uses.add("Classes");
    prsrCode.uses.add("ActiveX");
    prsrCode.uses.add("StringSupport");
    prsrCode.uses.add("DateSupport");
    prsrCode.uses.add("IdSoapMsXml");
    prsrCode.uses.add("FHIRParserBase");
    prsrCode.uses.add("FHIRBase");
    prsrCode.uses.add("FHIRResources");
    prsrCode.uses.add("FHIRComponents");
    prsrCode.uses.add("FHIRTypes");
    prsrCode.uses.add("MsXmlParser");
    prsrCode.uses.add("MsXmlBuilder");
    prsrCode.uses.add("JSON");
    prsrCode.comments.add("FHIR v"+version+" generated "+Config.DATE_FORMAT().format(genDate));
    
    prsrImpl.append(
        "{ TFHIRXmlParser }\r\n"+
        "\r\n"
        );
    
  }

  private String buildParser() {
    
    prsrImpl.append(
        "function TFHIRXmlParser.ParseResource(element : IXmlDomElement) : TFHIRResource;\r\n"+
        "begin\r\n"+
        "  if (element = nil) Then\r\n"+
        "    Raise Exception.Create('error - element is nil')\r\n"+
        prsrRegX.toString()+
        "  else\r\n"+
        "    raise Exception.create('Error: the element '+element.baseName+' is not recognised as a valid resource name');\r\n" +
        "end;\r\n\r\n"
        );

    prsrImpl.append(
        "procedure TFHIRXmlComposer.ComposeResource(xml : TMsXmlBuilder; id, ver : String; resource: TFHIRResource);\r\n"+
        "begin\r\n"+
        "  if (resource = nil) Then\r\n"+
        "    Raise Exception.Create('error - resource is nil');\r\n"+
        "  Case resource.ResourceType of\r\n"+
        srlsRegX.toString()+
        "  else\r\n"+
        "    raise Exception.create('Internal error: the resource type '+CODES_TFHIRResourceType[resource.ResourceType]+' is not a valid resource type');\r\n" +
        "  end;\r\n"+
        "end;\r\n\r\n"
        );

    prsrImpl.append(
        "function TFHIRJsonParser.ParseResource : TFHIRResource;\r\n"+
        "begin\r\n "+
        prsrRegJ.toString().substring(6)+
        "  else\r\n"+
        "    raise Exception.create('error: the element '+json.itemName+' is not a valid resource name');\r\n" +
        "end;\r\n\r\n"
        );

    prsrImpl.append(
        "procedure TFHIRJsonComposer.ComposeResource(json : TJSONWriter; id, ver : String; resource: TFHIRResource);\r\n"+
        "begin\r\n"+
        "  if (resource = nil) Then\r\n"+
        "    Raise Exception.Create('error - resource is nil');\r\n"+
        "  Case resource.ResourceType of\r\n"+
        srlsRegJ.toString()+
        "  else\r\n"+
        "    raise Exception.create('Internal error: the resource type '+CODES_TFHIRResourceType[resource.ResourceType]+' is not a valid resource type');\r\n" +
        "  end;\r\n"+
        "end;\r\n\r\n"
        );

    return
        "  TFHIRXmlParser = class (TFHIRXmlParserBase)\r\n"+
        "  protected\r\n"+
        prsrdefX.toString()+
        "    function ParseResource(element : IxmlDomElement) : TFHIRResource; override;\r\n"+
        "  end;\r\n\r\n"+
        "  TFHIRXmlComposer = class (TFHIRXmlComposerBase)\r\n"+
        "  protected\r\n"+
        srlsdefX.toString()+
        "    procedure ComposeResource(xml : TMsXmlBuilder; id, ver : String; resource : TFHIRResource); override;\r\n"+
        "  end;\r\n\r\n"+
        "  TFHIRJsonParser = class (TFHIRJsonParserBase)\r\n"+
        "  protected\r\n"+
        prsrdefJ.toString()+
        "    function ParseResource : TFHIRResource; override;\r\n"+
        "  end;\r\n\r\n"+
        "  TFHIRJsonComposer = class (TFHIRJsonComposerBase)\r\n"+
        "  protected\r\n"+
        srlsdefJ.toString()+
        "    procedure ComposeResource(json : TJSONWriter; id, ver : String; resource : TFHIRResource); override;\r\n"+
        "  end;\r\n\r\n";
  }

  public String getDescription() {
    return "Resource Definitions and XML & JSON parsers. D5+. TODO: remove dependencies on unpublished code.";
  }

  public String getTitle() {
    return "Delphi";
  }

  public boolean isECoreGenerator() {
    return false;
  }

  public void generate(org.hl7.fhir.definitions.ecore.fhir.Definitions definitions, String destDir,
      String implDir, Logger logger) throws Exception {

    throw new UnsupportedOperationException("Delphi generator uses ElementDefn-style definitions.");
  }

  public boolean doesCompile() {
    return false;
  }

  public boolean compile(String rootDir, List<String> errors) {
    return false;
  }

  public boolean doesTest() {
    return false;
  }

  public void loadAndSave(String rootDir, String sourceFile, String destFile) {
    
  }

}
