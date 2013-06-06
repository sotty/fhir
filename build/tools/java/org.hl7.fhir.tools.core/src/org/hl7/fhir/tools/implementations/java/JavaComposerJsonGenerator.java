package org.hl7.fhir.tools.implementations.java;
/*
Copyright (c) 2011-2013, HL7, Inc
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
import org.hl7.fhir.definitions.model.TypeRef;
import org.hl7.fhir.tools.implementations.GeneratorUtils;

public class JavaComposerJsonGenerator extends OutputStreamWriter {
  public enum JavaGenClass { Structure, Type, Resource, Constraint }

  private Definitions definitions;
  private Map<ElementDefn, String> typeNames = new HashMap<ElementDefn, String>();
  private List<String> typeNameStrings = new ArrayList<String>();
  private List<ElementDefn> enums = new ArrayList<ElementDefn>();
  private List<String> enumNames = new ArrayList<String>();
  private List<ElementDefn> strucs  = new ArrayList<ElementDefn>();
//  private List<String> lists = new ArrayList<String>();

  private String context;

  private StringBuilder reg = new StringBuilder();
  private StringBuilder regn = new StringBuilder();
  private StringBuilder regt = new StringBuilder();
//  private StringBuilder regn = new StringBuilder();
  private String genparam;

  
  public JavaComposerJsonGenerator(OutputStream out) throws UnsupportedEncodingException {
    super(out, "UTF-8");
  }

  public void generate(Definitions definitions, String version, Date genDate) throws Exception {

    this.definitions = definitions;
    
    start(version, genDate);
    
    genElement();
    generateEnumComposer();
    for (DefinedCode dc : definitions.getPrimitives().values()) 
      generatePrimitive(dc);
    
    for (ElementDefn n : definitions.getInfrastructure().values()) {
      generate(n, JavaGenClass.Structure);
//      String t = upFirst(n.getName());
//      regt.append("    else if (type instanceof "+t+")\r\n       compose"+n.getName()+"(prefix+\""+n.getName()+"\", ("+t+") type);\r\n");
    }
    
    for (ElementDefn n : definitions.getTypes().values()) {
      generate(n, JavaGenClass.Type);
      if (n.getName().equals("ResourceReference"))
        regt.append("    else if (type instanceof "+n.getName()+")\r\n       compose"+n.getName()+"(prefix+\"Resource\", ("+n.getName()+") type);\r\n");
      else
        regt.append("    else if (type instanceof "+n.getName()+")\r\n       compose"+n.getName()+"(prefix+\""+n.getName()+"\", ("+n.getName()+") type);\r\n");
      //        regn.append("    if (xpp.getName().equals(prefix+\""+n.getName()+"\"))\r\n      return true;\r\n");
    }

    for (DefinedCode n : definitions.getConstraints().values()) {
      generateConstraint(n);
      regt.append("    else if (type instanceof "+n.getCode()+")\r\n       compose"+n.getCode()+"(prefix+\""+n.getCode()+"\", ("+n.getCode()+") type);\r\n");
//      regn.append("    if (xpp.getName().equals(prefix+\""+n.getCode()+"\"))\r\n      return true;\r\n");
    }
    for (ElementDefn n : definitions.getStructures().values()) {
      generate(n, JavaGenClass.Structure);
      regt.append("    else if (type instanceof "+n.getName()+")\r\n       compose"+n.getName()+"(prefix+\""+n.getName()+"\", ("+n.getName()+") type);\r\n");
//      regn.append("    if (xpp.getName().equals(prefix+\""+n.getName()+"\"))\r\n      return true;\r\n");
    }
    
    genResource();

    for (String s : definitions.sortedResourceNames()) {
      ResourceDefn n = definitions.getResources().get(s);
      generate(n.getRoot(), JavaGenClass.Resource);
      String nn = javaClassName(n.getName());
      reg.append("    else if (resource instanceof "+nn+")\r\n      compose"+nn+"(\""+n.getName()+"\", ("+nn+")resource);\r\n");
      regn.append("    else if (resource instanceof "+nn+")\r\n      compose"+nn+"(name, ("+nn+")resource);\r\n");
//      regn.append("    if (xpp.getName().equals(prefix+\""+n.getName()+"\"))\r\n      return true;\r\n");
    }
    
    for (DefinedCode cd : definitions.getPrimitives().values()) {
      String n = upFirst(cd.getCode());
      String t = n;
      if (n.equals("String")) 
        t = "String_";
      
//      if (n.equals("Uri"))
//        t = "Uri";
      regt.append("    else if (type instanceof "+t+")\r\n       compose"+upFirst(n)+"(prefix+\""+n+"\", ("+t+") type);\r\n");
//      regn.append("    if (xpp.getName().equals(prefix+\""+n+"\"))\r\n      return true;\r\n");
    }
    
    finish();
  }

  private void genElement() throws Exception {
    
    write("  private void composeElement(Element element) throws Exception {\r\n");
    write("    if (element.getXmlId() != null)\r\n");
    write("      prop(\"_id\", element.getXmlId());\r\n");
    write("    if (element.getExtensions().size() > 0) {\r\n");
    write("      openArray(\"extension\");\r\n");
    write("      for (Extension ex : element.getExtensions())\r\n");
    write("        composeExtension(null, ex);\r\n");
    write("      closeArray();\r\n");
    write("    }\r\n");
    write("  }\r\n");
    write("\r\n");
  }

  private void genResource() throws Exception {    
    write("  private void composeResourceElements(Resource element) throws Exception {\r\n");
    write("    composeElement(element);\r\n");
    write("    if (element.getText() != null)\r\n");
    write("      composeNarrative(\"text\", element.getText());\r\n");
    write("    if (element.getContained().size() > 0) {\r\n");
    write("      openArray(\"contained\");\r\n");
    write("      for (Resource r : element.getContained()) {\r\n");
    write("        if (r.getXmlId() == null)\r\n");
    write("          throw new Exception(\"Contained Resource has no id - one must be assigned\"); // we can't assign one here - what points to it?\r\n");
    write("        open(null);\r\n");
    write("        composeResource(r);\r\n");
    write("        close();\r\n");
    write("      }\r\n");
    write("      closeArray();\r\n");
    write("    }\r\n");
    write("  }\r\n");
    write("\r\n");		
  }

  private String javaClassName(String name) {
    if (name.equals("List"))
      return "List_";
    else 
      return name;
  }

  private String upFirst(String n) {
    return n.substring(0,1).toUpperCase() + n.substring(1);
  }

  private void start(String version, Date genDate) throws Exception {

    write("package org.hl7.fhir.instance.formats;\r\n");
    write("\r\n/*\r\n"+Config.FULL_LICENSE_CODE+"*/\r\n\r\n");
    write("// Generated on "+Config.DATE_FORMAT().format(genDate)+" for FHIR v"+version+"\r\n\r\n");
    write("import org.hl7.fhir.instance.model.*;\r\n");
    write("import org.hl7.fhir.instance.model.Integer;\r\n");
    write("import org.hl7.fhir.instance.model.Boolean;\r\n");
    write("import org.hl7.fhir.utilities.Utilities;\r\n");
    write("import java.net.*;\r\n");
    write("import java.math.*;\r\n");
    write("\r\n");
    write("public class JsonComposer extends JsonComposerBase {\r\n");
    write("\r\n");
  }


  private void generateEnumComposer() throws Exception {
    write("  private <E extends Enum<E>> void composeEnumeration(String name, Enumeration<E> value, EnumFactory e) throws Exception {\r\n");
    write("    if (value != null && (!Utilities.noString(value.getXmlId()) || value.hasExtensions() || value.getValue() != null)) {\r\n");
    write("      open(name);\r\n");
    write("      composeElement(value);\r\n");
    write("      if (value.getValue() != null) \r\n");
    write("        prop(\"value\", e.toCode(value.getValue()));\r\n");
    write("      close();\r\n");
    write("    }    \r\n");
    write("  }    \r\n");
    write("\r\n");
  }

  private String getPrimitiveTypeModelName(String code) {
    if (code.equals("string"))
      return "String_";
    return upFirst(code);
  }

  private void generatePrimitive(DefinedCode dc) throws Exception {
    String tn = getPrimitiveTypeModelName(dc.getCode());

    write("  private void compose"+upFirst(dc.getCode())+"(String name, "+tn+" value) throws Exception {\r\n");
    if (dc.getCode().equals("integer")  || dc.getCode().equals("boolean"))
      write("    if (value != null) {\r\n");
    else if (dc.getCode().equals("decimal") || dc.getCode().equals("uri") || dc.getCode().equals("base64Binary") || dc.getCode().equals("instant"))
      write("    if (value != null && (!Utilities.noString(value.getXmlId()) || value.hasExtensions() || value.getValue() != null)) {\r\n");
    else
      write("    if (value != null && (!Utilities.noString(value.getXmlId()) || value.hasExtensions() || !Utilities.noString(value.getValue()))) {\r\n");
    write("      open(name);\r\n");
    write("      composeElement(value);\r\n");
    if (!dc.getCode().equals("integer") && !dc.getCode().equals("boolean"))
      write("      if (value.getValue() != null) \r\n");
    write("        prop(\"value\", toString(value.getValue()));\r\n");
    write("      close();\r\n");
    write("    }    \r\n");
    write("  }    \r\n");
    write("\r\n");
  }


  private void generate(ElementDefn n, JavaGenClass clss) throws Exception {
    typeNames.clear();
    typeNameStrings.clear();
    enums.clear();
    strucs.clear();
    enumNames.clear();
    String nn = javaClassName(n.getName());
    for (ElementDefn e : n.getElements()) {
        scanNestedTypes(n, nn, e);
    }
    context = nn;

    genInner(n, clss);
    
    for (ElementDefn e : strucs) {
      genInner(e, JavaGenClass.Structure);
    }

  }

  private void generateConstraint(DefinedCode cd) throws Exception {
    typeNames.clear();
    typeNameStrings.clear();
    enums.clear();
    strucs.clear();
    enumNames.clear();
    context = cd.getCode();
    ElementDefn n = definitions.getTypes().get(cd.getComment());
    
    typeNames.put(n, cd.getCode());
    for (ElementDefn e : n.getElements()) {
        scanNestedTypes(n, n.getName(), e);
    }

    genInner(n, JavaGenClass.Constraint);
    
    for (ElementDefn e : strucs) {
      genInner(e, JavaGenClass.Structure);
    }

  }

  private void genInner(ElementDefn n, JavaGenClass clss) throws IOException, Exception {
    String tn = typeNames.containsKey(n) ? typeNames.get(n) : javaClassName(n.getName());
    
    write("  private void compose"+upFirst(tn).replace(".", "").replace("<", "_").replace(">", "")+"(String name, "+tn+" element) throws Exception {\r\n");
    write("    if (element != null) {\r\n");
    write("      open(name);\r\n");
    if (clss == JavaGenClass.Resource) 
      write("      composeResourceElements(element);\r\n");
    else
      write("      composeElement(element);\r\n");
    for (ElementDefn e : n.getElements()) 
      genElement(n, e, clss);
    write("      close();\r\n");
    write("    }\r\n");    
    write("  }\r\n\r\n");    
  }

  private String pathClass(String tn) {
    return tn.substring(0, tn.indexOf('.'));
  }

  private String pathNode(String tn) {
    return tn.substring(tn.indexOf('.')+1);
  }

  private void genElement(ElementDefn root, ElementDefn e, JavaGenClass clss) throws Exception {
    String name = e.getName();
    if (name.endsWith("[x]") || name.equals("[type]")) {
      String en = name.endsWith("[x]") ? name.replace("[x]", "") : "value";
      String pfx = name.endsWith("[x]") ? name.replace("[x]", "") : "";
      write("      composeType(\""+pfx+"\", element.get"+upFirst(en)+"());\r\n");
    } else {
      String comp = null;
      String en = null;
      BindingSpecification cd = definitions.getBindingByName(e.getBindingName());
      String tn = typeName(root, e, false);
      if (e.typeCode().equals("code") && cd != null && cd.getBinding() == BindingSpecification.Binding.CodeList) {
        en = typeNames.get(e); // getCodeListType(cd.getBinding());
        comp = null;
      } else {   
        if (name.equals("extension")) {
          name = "extension";
          tn = "Extension";
        }
        if (tn.equals("char[]"))
          tn = "xhtml";
        else if (tn.equals("code")) {
          tn = "Code";
          comp = "composeCode";
        } else if (tn.equalsIgnoreCase("uri"))
          tn = "Uri";
        else if (tn.equals("instant"))
          tn = "Instant";
        if (tn.contains("Resource(")) {
          comp = "composeResourceReference";
          tn = "ResourceReference";
        } else if (tn.contains("("))
          comp = "compose"+PrepGenericName(tn);
        else if (tn.startsWith(context) && !tn.equals(context)) {
          comp = "compose"+upFirst(leaf(tn)).replace(".", "");
        } else
          comp = "compose"+upFirst(leaf(tn)).replace(".", "");
      }
//      if ((!contentsHaveId && typeIsSimple(e)) || e.typeCode().equals("xml:lang")) 
//        comp = comp+"Simple";
      
      if (name.equals("extension")) {
        String s = "Extension"; 
        write("      if (element.get"+s+"().size() > 0) {\r\n");
        write("        openArray(\"extension\");\r\n");
        write("        for (Extension e : element.get"+s+"()) \r\n");
        write("          composeExtension(null, e);\r\n");
        write("        closeArray();\r\n");
        write("      };\r\n");
      } else if (e.unbounded()) {
        tn = typeName(root, e, true);
        if (tn.contains("Resource(")) {
          comp = "composeResourceReference";
          tn = "ResourceReference";
        }
        write("      if (element.get"+upFirst(name)+"().size() > 0) {\r\n");
        write("        openArray(\""+name+"\");\r\n");
  	    if (en == null) {
          if (tn.equals("String"))
              tn = "String_";
          write("        for ("+(tn.contains("(") ? PrepGenericTypeName(tn) : upFirst(tn))+" e : element.get"+upFirst(getElementName(name, false))+"()) \r\n");
          write("          "+comp+"(null, e);\r\n");
  	    } else {
            write("        for (Enumeration<"+prepEnumName(en)+"> e : element.get"+upFirst(getElementName(name, false))+"()) \r\n");
            write("          composeEnumeration(null, e, new "+context+"().new "+upFirst(en.substring(en.indexOf(".")+2))+"EnumFactory());\r\n");
  	    }
  	  write("        closeArray();\r\n");
        write("      };\r\n");
      } else if (en != null) {
        write("      if (element.get"+upFirst(name)+"() != null)\r\n");
        write("        composeEnumeration(\""+name+"\", element.get"+upFirst(getElementName(name, false))+"(), new "+context+"().new "+upFirst(en.substring(en.indexOf(".")+2))+"EnumFactory());\r\n");
        //write("        composeString(\""+name+"\", element.get"+upFirst(getElementName(name, false))+"().toCode());\r\n");        
      } else {
        write("      "+comp+"(\""+name+"\", element.get"+upFirst(getElementName(name, false))+"());\r\n");
      }
    }
  }

  private String prepEnumName(String en) {
	String[] parts = en.split("\\.");
	if (parts.length == 1)
		return upFirst(parts[0]);
	else
		return upFirst(parts[0])+'.'+upFirst(parts[1].substring(1));
}

private String leaf(String tn) {
    return tn.startsWith("java.lang.") ? tn.substring(10) : tn;
  }
  
  private String PrepGenericTypeName(String tn) {
    int i = tn.indexOf('(');
    return tn.substring(0, i)+"<"+upFirst(tn.substring(i+1).replace(")", ">"));
  }

  private String PrepGenericName(String tn) {
    int i = tn.indexOf('(');
    return tn.substring(0, i)+"_"+upFirst(tn.substring(i+1).replace(")", ""));
  }

  private boolean typeIsSimple(ElementDefn e) {
    String t = e.typeCode();
    return definitions.getPrimitives().containsKey(t);
  }

  private String typeName(ElementDefn root, ElementDefn elem, boolean formal) throws Exception {
    String t = elem.typeCode();
//    if (usePrimitive && definitions.getPrimitives().containsKey(t)) {
//      if (t.equals("boolean"))
//        return formal ? "boolean" : "java.lang.Boolean";
//      else if (t.equals("integer"))
//        return "int";
//      else if (t.equals("decimal"))
//        return formal ? "BigDecimal" : "BigDecimal";
//      else if (t.equals("base64Binary"))
//        return formal ? "byte[]" : "bytes";
//      else if (t.equals("instant"))
//        return formal ? "java.util.Calendar" : "Date";
//      else if (t.equals("uri"))
//        return formal ? "java.net.URI" : "Uri";
//      else if (t.equals("dateTime"))
//        return "DateTime";
//      else if (t.equals("date"))
//        return "Date";
//      else 
//        return "String";
////      if (t.equals("idref"))
////        return "String";
////      else if (t.equals("string"))
////        return "String_";
////      else
////        return upFirst(t);
//    }  else if (t.equals("xml:lang"))
//        return formal ? "string" : "Code";
//    else 
    if (elem.usesCompositeType()) { 
      if (typeNames.containsKey(elem) && typeNames.get(elem) != null)
        return typeNames.get(elem);
      else  
        return root.getName();      
    } else if (elem.getTypes().size() == 0) {
        return typeNames.get(elem);
    } else if (typeNames.containsKey(elem))
      return typeNames.get(elem);
    else
      return upFirst(t);
  }
  
  private void finish() throws Exception {
    write("  @Override\r\n");
    write("  protected void composeResource(Resource resource) throws Exception {\r\n");
    write("    "+reg.toString().substring(9));
    write("    else if (resource instanceof Binary)\r\n");
    write("      composeBinary(\"Binary\", (Binary)resource);\r\n");
    write("    else\r\n");
    write("      throw new Exception(\"Unhanded resource type \"+resource.getClass().getName());\r\n");
    write("  }\r\n\r\n");
    write("  protected void composeNamedResource(String name, Resource resource) throws Exception {\r\n");
    write("    "+regn.toString().substring(9));
    write("    else if (resource instanceof Binary)\r\n");
    write("      composeBinary(name, (Binary)resource);\r\n");
    write("    else\r\n");
    write("      throw new Exception(\"Unhanded resource type \"+resource.getClass().getName());\r\n");
    write("  }\r\n\r\n");
    write("  @SuppressWarnings(\"unchecked\")\r\n");
    write("  protected void composeType(String prefix, Type type) throws Exception {\r\n");
    write("    if (type == null)\r\n");
    write("      ;\r\n");
    write(regt.toString());
    write("    else\r\n");
    write("      throw new Exception(\"Unhanded type\");\r\n");
    write("  }\r\n\r\n");
//
//    write("  private boolean nameIsTypeName(XmlPullParser xpp, String prefix) {\r\n");
//    write("    "+regn.toString());
//    write("    return false;\r\n");
//    write("  }\r\n");
//    
    
    write("}\r\n");
    write("\r\n");
    flush();
  }

  private String getCodeListType(String binding) {
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

  private void scanNestedTypes(ElementDefn root, String path, ElementDefn e) throws Exception {
    String tn = null;
    if (e.typeCode().equals("code") && e.hasBinding()) {
      BindingSpecification cd = definitions.getBindingByName(e.getBindingName());
      if (cd != null && cd.getBinding() == BindingSpecification.Binding.CodeList) {
        tn = getCodeListType(cd.getReference());
        if (!enumNames.contains(tn)) {
          enumNames.add(tn);
          enums.add(e);
        }
        typeNames.put(e,  rootOf(path)+"."+tn);
      }
    }
    if (tn == null) {
      if (e.usesCompositeType()) {
        tn = typeNames.get(getElementForPath(root, e.typeCode().substring(1)));
        typeNames.put(e,  tn);
      } else if (e.getTypes().size() > 0) {
        tn = e.typeCode();
        TypeRef tr = e.getTypes().get(0);
        
        if (tr.isUnboundGenericParam())
          tn = genparam;
        else if (tr.isIdRef())
          tn ="String";
        else if (tr.isXhtml()) 
          tn = "char[]";
        else if (tr.isWildcardType())
          tn ="Type";
        else if (tn.equals("xml:lang"))
          tn = "Code";
        else if (tn.equals("string"))
          tn = "String";
        if (tn.contains("<"))
          tn = tn.substring(0, tn.indexOf('<')+1)+tn.substring(tn.indexOf('<')+1, tn.indexOf('<')+2).toUpperCase()+tn.substring(tn.indexOf('<')+2);
        typeNames.put(e,  tn);
      } else {
    	  if (e.getDeclaredTypeName() != null) 
				tn = e.getDeclaredTypeName();
			else
				tn = upFirst(e.getName());
        if (tn.equals("Element"))
          tn = "Element_";
        if (!e.getName().equals("extension"))
          strucs.add(e);
        if (typeNameStrings.contains(tn)) {
          char i = 'A';
          while (typeNameStrings.contains(tn+i))
            i++;
          tn = tn + i;
        }
        typeNameStrings.add(tn);
        tn = path+"."+tn;
        typeNames.put(e,  tn);
        for (ElementDefn c : e.getElements()) {
          scanNestedTypes(root, path, c);
        }
      }
    } 
  }

  private String rootOf(String path) {
    int i = path.indexOf('.');
    return i == -1 ? path : path.substring(0, i);
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

  protected String getElementName(String name, boolean alone) {
    if (name.equals("[type]"))
      return "value";
    else if ((alone && GeneratorUtils.isJavaReservedWord(name)) || (!alone && name.equals("class")))
      return name+"_";
    else
      return name.replace("[x]", "");
  }

  
}