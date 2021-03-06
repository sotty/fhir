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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.definitions.Config;
import org.hl7.fhir.definitions.model.BindingSpecification;
import org.hl7.fhir.definitions.model.DefinedCode;
import org.hl7.fhir.definitions.model.Definitions;
import org.hl7.fhir.definitions.model.ElementDefn;
import org.hl7.fhir.tools.implementations.GeneratorUtils;
import org.hl7.fhir.utilities.Utilities;

public class JavaResourceGenerator extends JavaBaseGenerator {

	public enum JavaGenClass { Structure, Type, Resource, BackboneElement, Constraint }
	private JavaGenClass clss;

	protected Definitions definitions;
	
	public JavaResourceGenerator(OutputStream out, Definitions definitions) throws UnsupportedEncodingException {
		super(out);
		this.definitions = definitions;
	}

	protected Map<ElementDefn, String> typeNames = new HashMap<ElementDefn, String>();
	protected List<String> typeNameStrings = new ArrayList<String>();

	protected List<ElementDefn> enums = new ArrayList<ElementDefn>();
	protected List<String> enumNames = new ArrayList<String>();
	protected List<ElementDefn> strucs  = new ArrayList<ElementDefn>();

  private String classname;


	public Map<ElementDefn, String> getTypeNames() {
		return typeNames;
	}
	
	public void generate(ElementDefn root, String packageName, String name, Map<String, BindingSpecification> conceptDomains, JavaGenClass clss, DefinedCode cd, Map<String,String> aliases, Date genDate, String version) throws Exception {
		typeNames.clear();
		typeNameStrings.clear();
		enums.clear();
		strucs.clear();
		enumNames.clear();
		this.clss = clss;

		write("package " + packageName + ";\r\n");
		write("\r\n/*\r\n"+Config.FULL_LICENSE_CODE+"*/\r\n\r\n");
		write("// Generated on "+Config.DATE_FORMAT().format(genDate)+" for FHIR v"+version+"\r\n\r\n");
    if (clss != JavaGenClass.Constraint) {
      boolean l = true; // hasList(root);
      boolean h = hasXhtml(root);
      boolean d = hasDecimal(root);
      if (l || h || d) {
        if (l)
          write("import java.util.*;\r\n");
        if (h)
          write("import org.hl7.fhir.utilities.xhtml.XhtmlNode;\r\n");
        write("\r\n");
        if (d)
          write("import java.math.*;\r\n");
      }
    }
		jdoc("", root.getDefinition());
		classname = upFirst(name);
		if (clss == JavaGenClass.Resource)
			write("public class "+upFirst(name)+" extends Resource implements " + packageName + ".intf." + classname + "Resource {\r\n");
    else if (clss == JavaGenClass.Structure)
      write("public class "+upFirst(name)+" extends Element {\r\n");
    else if (clss == JavaGenClass.BackboneElement)
      write("public class "+upFirst(name)+" extends BackboneElement {\r\n");
		else if (clss == JavaGenClass.Constraint)
			write("public class "+upFirst(cd.getCode())+" extends "+upFirst(root.getName())+" {\r\n");
	  else if (root.getName().equals("Quantity"))
			write("public class "+upFirst(name)+" extends Type {\r\n");
		else
			write("public " +
                  ( isAbstract( name ) ? " abstract " : " ") +
                  "class "+upFirst(name)+ genericParams(name) + " extends Type {\r\n");
		write("\r\n");

		if (clss != JavaGenClass.Constraint) {
			for (ElementDefn e : root.getElements()) {
				if (clss != JavaGenClass.Resource || (!e.getName().equals("extension") && !e.getName().equals("text")))
					scanNestedTypes(root, root.getName(), e, conceptDomains);
			}
			for (ElementDefn e : enums) {
				generateEnum(e, conceptDomains);
			}
			for (ElementDefn e : strucs) {
				generateType(e, clss == JavaGenClass.Resource ? JavaGenClass.BackboneElement : JavaGenClass.Structure, aliases);
			}

			for (ElementDefn e : root.getElements()) {
				if (clss != JavaGenClass.Resource || (!e.getName().equals("extension") && !e.getName().equals("text")))
					generateField(root, e, "    ", aliases);
			}

			List<ElementDefn> mandatory = new ArrayList<ElementDefn>();
			generateConstructor(upFirst(name), mandatory, "  ", aliases);
			for (ElementDefn e : root.getElements()) {
        if (clss != JavaGenClass.Resource || (!e.getName().equals("extension") && !e.getName().equals("text"))) {
          if (e.isMandatory())
            mandatory.add(e);
        }
      }
	    if (mandatory.size() > 0)
	      generateConstructor(upFirst(name), mandatory, "  ",aliases);

			for (ElementDefn e : root.getElements()) {
				if (clss != JavaGenClass.Resource || (!e.getName().equals("extension") && !e.getName().equals("text")))
					generateAccessors(root, e, "    ", upFirst(name), aliases);
			}
			generateChildrenRegister(root, "    ");
		}

        if ( isResource( classname ) ) {
            generateResourceReferenceMethods();
        } else {
		    generateCopy(root, classname, false, aliases);
        }
		if (clss == JavaGenClass.Resource) {
		  write("  @Override\r\n");
		  write("  public ResourceType getResourceType() {\r\n");
		  write("    return ResourceType."+root.getName()+";\r\n");
		  write("   }\r\n");

          write("  public boolean isReference() { return false; } \r\n");

          write("  public " + packageName + ".refs." + classname + "_Reference asReference() { \r\n" );
          write("     return new " + packageName + ".refs." + classname + "_Reference(); \r\n" );
          write("  }\r\n");

          write("  public " + classname + " asResource() { \r\n" );
          write("     return this; \r\n" );
          write("  }\r\n");

            write("\r\n");


		}
		write("\r\n");
		write("}\r\n");
		write("\r\n");
		flush();

	}

    private void generateResourceReferenceMethods() throws IOException {
//        write( "  public ResourceReference<T> copy(); \r\n" );
        write( "  protected Type typedCopy() { return copy(); } \r\n\r\n");
        write( "  public T resolveReference() { return null; } " );
    }

    private boolean isAbstract( String name ) {
//        return isResource( name );
        return false;
    }

    private String genericParams( String name ) {
        if ( isResource( name ) ) {
            return "<T>";
        } else {
            return "";
        }
    }

    protected void jdoc(String indent, String text) throws IOException {
    write(indent+"/**\r\n");
		write(indent+" * "+text+"\r\n");
		write(indent+" */\r\n");
  }

	private void generateChildrenRegister(ElementDefn p, String indent) throws IOException {
	  write(indent+"  protected void listChildren(List<Property> childrenList) {\r\n");
	  write(indent+"    super.listChildren(childrenList);\r\n");
	  for (ElementDefn e : p.getElements()) {
	    if (!e.typeCode().equals("xhtml"))
	      write(indent+"    childrenList.add(new Property(\""+e.getName()+"\", \""+e.typeCode()+"\", \""+Utilities.escapeJava(e.getDefinition())+"\", 0, java.lang.Integer.MAX_VALUE, "+ "(Element)" +getElementName(e.getName(), true)+"));\r\n");
	  }
	  write(indent+"  }\r\n\r\n");  
  }

  private void generateConstructor(String className, List<ElementDefn> params, String indent, Map<String,String> aliases ) throws IOException {
    write(indent+"  public "+className+"(");
    boolean first = true;
    for (ElementDefn e : params) {
      if (!first)
        write(", ");
      first = false;
      String tn = determineType( typeNames, e, aliases );
      String en = getElementName(e.getName(), true);
      write(tn +" "+en);
    }
    write(") {\r\n");
    write(indent+"    super();\r\n");
    for (ElementDefn e : params) {
      String en = getElementName(e.getName(), true);
      write(indent+"    this."+en+" = "+en+";\r\n");      
    }
    write(indent+"  }\r\n\r\n");
  }


	//	private void generateSetters(ElementDefn root, String indent) throws Exception {
	//		boolean first = true;
	//		if (root.getElements().size() == 0)
	//			return;
	//		
	//		boolean generics = false;
	//		for (ElementDefn e : root.getElements()) 
	//			if (!e.getName().equals("id") && !e.getName().equals("extension") && !e.getName().equals("text")) 
	//				if (typeNames.get(e).contains("<"))
	//					generics = true;
	//
	//		if (generics)
	//			write(indent+"@SuppressWarnings(\"unchecked\")\r\n");
	//		write(indent+"public void setProperty(String name, Object value) throws Exception {\r\n");
	//		for (ElementDefn e : root.getElements()) {
	//			if (!e.getName().equals("id") && !e.getName().equals("extension") && !e.getName().equals("text")) {
	//				if (first)
	//					write(indent+"    if (\""+e.getName()+"\".equals(name))\r\n");
	//				else
	//					write(indent+"    else if (\""+e.getName()+"\".equals(name))\r\n");
	//				first = false;
	//				if (e.unbounded())
	//					write(indent+"        get"+getTitle(getElementName(e.getName()))+"().add(("+typeNames.get(e)+") value);\r\n");
	//				else
	//					write(indent+"        set"+getTitle(getElementName(e.getName()))+"(("+typeNames.get(e)+") value);\r\n");
	//			}
	//		}
	//		write(indent+"    else\r\n");
	//		write(indent+"        super.setProperty(name, value);\r\n");
	//		write(indent+"}\r\n");
	//		write("\r\n");
	//	}



	protected boolean hasList(ElementDefn root) {
		for (ElementDefn e : root.getElements()) {
			if (!e.getName().equals("text")) {
				if (e.unbounded() || hasListInner(e))
					return true;
			}
		}
		return false;
	}

  protected boolean hasDecimal(ElementDefn root) {
    for (ElementDefn e : root.getElements()) {
      if (e.typeCode().equals("decimal") || hasDecimalInner(e))
        return true;
    }
    return false;
  }

	protected boolean hasXhtml(ElementDefn root) {
		for (ElementDefn e : root.getElements()) {
			if (e.isXhtmlElement() || hasXhtmlInner(e))
				return true;
		}
		return false;
	}

	protected boolean hasListInner(ElementDefn e) {
		for (ElementDefn c : e.getElements()) {
			if (c.unbounded() || hasListInner(c))
				return true;
		}

		return false;
	}

	protected boolean hasXhtmlInner(ElementDefn e) {
		for (ElementDefn c : e.getElements()) {
			if (c.isXhtmlElement() || hasXhtmlInner(c))
				return true;
		}

		return false;
	}

  protected boolean hasDecimalInner(ElementDefn e) {
    for (ElementDefn c : e.getElements()) {
      if (c.typeCode().equals("decimal") || hasDecimalInner(c))
        return true;
    }

    return false;
  }

	private void generateEnum(ElementDefn e, Map<String, BindingSpecification> conceptDomains) throws Exception {
		String tn = typeNames.get(e);
		String tns = tn.substring(tn.indexOf("<")+1);
		tns = tns.substring(0, tns.length()-1);
		BindingSpecification cd = getConceptDomain(conceptDomains, e.getBindingName());

		write("    public enum "+tns+" {\r\n");
		int l = cd.getCodes().size();
		int i = 0;
		for (DefinedCode c : cd.getCodes()) {
			i++;
			String cc = Utilities.camelCase(c.getCode());
			if (GeneratorUtils.isJavaReservedWord(cc))
				cc = cc + "_";
			if (Utilities.IsInteger(cc))
			  cc = "_"+cc;
			if (cc.equals("<"))
				cc = "lessThan";
			else if (cc.equals("<="))
				cc = "lessOrEqual";
			else if (cc.equals(">"))
				cc = "greaterThan";
			else if (cc.equals(">="))
				cc = "greaterOrEqual";
      else if (cc.equals("="))
        cc = "equal";
			else if (allPlusMinus(cc))
			  cc = cc.replace("-", "Minus").replace("+", "Plus");
			else
			  cc = cc.replace("-", "").replace("+", "");
			write("        "+cc+", // "+c.getDefinition()+"\r\n");
		}
    write("        Null; // added to help the parsers\r\n");


		write("        public static "+tns+" fromCode(String codeString) throws Exception {\r\n");
		write("            if (codeString == null || \"\".equals(codeString))\r\n");
		write("                return null;\r\n");
		for (DefinedCode c : cd.getCodes()) {
			String cc = Utilities.camelCase(c.getCode());
			if (GeneratorUtils.isJavaReservedWord(cc))
				cc = cc + "_";
      if (Utilities.IsInteger(cc))
        cc = "_"+cc;
			if (cc.equals("<"))
				cc = "lessThan";
			else if (cc.equals("<="))
				cc = "lessOrEqual";
			else if (cc.equals(">"))
				cc = "greaterThan";
			else if (cc.equals(">="))
				cc = "greaterOrEqual";
      else if (cc.equals("="))
        cc = "equal";
      else if (allPlusMinus(cc))
        cc = cc.replace("-", "Minus").replace("+", "Plus");
			else
				cc = cc.replace("-", "").replace("+", "");
			write("        if (\""+c.getCode()+"\".equals(codeString))\r\n");
			write("          return "+cc+";\r\n");
		}		
		write("        throw new Exception(\"Unknown "+tns+" code '\"+codeString+\"'\");\r\n");
		write("        }\r\n");	

		write("        public String toCode() {\r\n");
		write("          switch (this) {\r\n");
		for (DefinedCode c : cd.getCodes()) {
			String cc = Utilities.camelCase(c.getCode());
			if (GeneratorUtils.isJavaReservedWord(cc))
				cc = cc + "_";
      if (Utilities.IsInteger(cc))
        cc = "_"+cc;
			if (cc.equals("<"))
				cc = "lessThan";
			else if (cc.equals("<="))
				cc = "lessOrEqual";
			else if (cc.equals(">"))
				cc = "greaterThan";
			else if (cc.equals(">="))
				cc = "greaterOrEqual";
      else if (cc.equals("="))
        cc = "equal";
      else if (allPlusMinus(cc))
        cc = cc.replace("-", "Minus").replace("+", "Plus");
			else
				cc = cc.replace("-", "").replace("+", "");
			write("            case "+cc+": return \""+c.getCode()+"\";\r\n");
		}   
		write("            default: return \"?\";\r\n");
		write("          }\r\n"); 
		write("        }\r\n"); 

		write("    }\r\n");
		write("\r\n");

		
		write("  public static class "+tns+"EnumFactory implements EnumFactory {\r\n");
		write("    public Enum<?> fromCode(String codeString) throws Exception {\r\n");
		
		write("      if (codeString == null || \"\".equals(codeString))\r\n");
    write("            if (codeString == null || \"\".equals(codeString))\r\n");
    write("                return null;\r\n");
    for (DefinedCode c : cd.getCodes()) {
      String cc = Utilities.camelCase(c.getCode());
      if (GeneratorUtils.isJavaReservedWord(cc))
        cc = cc + "_";
      if (Utilities.IsInteger(cc))
        cc = "_"+cc;
      if (cc.equals("<"))
        cc = "lessThan";
      else if (cc.equals("<="))
        cc = "lessOrEqual";
      else if (cc.equals(">"))
        cc = "greaterThan";
      else if (cc.equals(">="))
        cc = "greaterOrEqual";
      else if (cc.equals("="))
        cc = "equal";
      else if (allPlusMinus(cc))
        cc = cc.replace("-", "Minus").replace("+", "Plus");
      else
        cc = cc.replace("-", "").replace("+", "");
      write("        if (\""+c.getCode()+"\".equals(codeString))\r\n");
      write("          return "+tns+"."+cc+";\r\n");
    }   
    write("        throw new Exception(\"Unknown "+tns+" code '\"+codeString+\"'\");\r\n");
    write("        }\r\n"); 
    write("    public String toCode(Enum<?> code) throws Exception {\r\n");
    for (DefinedCode c : cd.getCodes()) {
      String cc = Utilities.camelCase(c.getCode());
      if (GeneratorUtils.isJavaReservedWord(cc))
        cc = cc + "_";
      if (Utilities.IsInteger(cc))
        cc = "_"+cc;
      if (cc.equals("<"))
        cc = "lessThan";
      else if (cc.equals("<="))
        cc = "lessOrEqual";
      else if (cc.equals(">"))
        cc = "greaterThan";
      else if (cc.equals(">="))
        cc = "greaterOrEqual";
      else if (cc.equals("="))
        cc = "equal";
      else if (allPlusMinus(cc))
        cc = cc.replace("-", "Minus").replace("+", "Plus");
      else
        cc = cc.replace("-", "").replace("+", "");
      write("      if (code == "+tns+"."+cc+")\r\n        return \""+c.getCode()+"\";\r\n");
    }
    write("      return \"?\";\r\n"); 
    write("      }\r\n"); 
    write("    }\r\n"); 
    write("\r\n");
	}

	private boolean allPlusMinus(String cc) {
	  for (char c : cc.toCharArray())
	    if (!(c == '-' || c == '+'))
	      return false;
    return true;
  }

  private void generateType(ElementDefn e, JavaGenClass clss, Map<String,String> aliases ) throws Exception {
		String tn = typeNames.get(e);

		if (clss == JavaGenClass.BackboneElement)
	    write("    public static class "+tn+" extends BackboneElement {\r\n");
		else
		  write("    public static class "+tn+" extends Element {\r\n");
		for (ElementDefn c : e.getElements()) {
			generateField(e, c, "        ", aliases);
		}
    List<ElementDefn> mandatory = new ArrayList<ElementDefn>();
    generateConstructor(tn, mandatory, "    ", aliases);
    for (ElementDefn c : e.getElements()) {
      if (clss != JavaGenClass.Resource || (!c.getName().equals("extension") && !c.getName().equals("text"))) {
        if (c.isMandatory())
          mandatory.add(c);
      }
    }
    if (mandatory.size() > 0)
      generateConstructor(tn, mandatory, "    ", aliases);
		
		for (ElementDefn c : e.getElements()) {
			generateAccessors(e, c, "        ", tn, aliases);
		}
    generateChildrenRegister(e, "      ");
		generateCopy(e, tn, true, aliases );
    write("  }\r\n");
		write("\r\n");

	}

	private void generateCopy(ElementDefn e, String tn, boolean owner, Map<String,String> aliases ) throws IOException {
	  if (owner) {
      write("      public "+tn+" copy("+classname+" e) {\r\n");
      write("        "+tn+" dst = new "+tn+"();\r\n");
	  } else {
      write("      public "+tn+" copy() {\r\n");
      write("        "+tn+" dst = new "+tn+"();\r\n");
	  }
    for (ElementDefn c : e.getElements()) {
      String params = "";
      if (((c.getElements().size() > 0) || c.typeCode().startsWith("@")) && !definitions.dataTypeIsSharedInfo(c.typeCode()))
        params = owner ? "e" : "dst";
      String name = getElementName(c.getName(), true);
      if (c.unbounded()) {
        write("        dst."+name+" = new ArrayList<"+determineType( typeNames, c, aliases )+">();\r\n");
        write("        for ("+determineType( typeNames, c, aliases )+" i : "+name+")\r\n");
          if ( isResource( c, definitions ) ) {
            write("          dst."+name+".add( (" + determineType( typeNames, c, aliases ) + ") ((Type)i).copy("+params+"));\r\n");
          } else {
            write("          dst."+name+".add(i.copy("+params+"));\r\n");
          }
      } else {
        if (name.endsWith("[x]")) {
          name = name.substring(0, name.length()-3);
        }
        if ( isResource( c, definitions ) ) {
            write("        dst."+name+" = "+name+" == null ? null : ( (" +determineType( typeNames, c, aliases)+ ") ((Type)" +name+").copy("+params+") );\r\n");
        } else {
            write("        dst."+name+" = "+name+" == null ? null : "+name+".copy("+params+");\r\n");
        }
      }
    }
    write("        return dst;\r\n");
    write("      }\r\n\r\n");
    if (!owner) {
      write("      protected "+tn+" typedCopy() {\r\n");
      write("        return copy();\r\n");
      write("      }\r\n\r\n");
      
    }
  }

    private boolean isResource( ElementDefn c, Definitions definitions ) {
        if ( c.getTypes().isEmpty() ) {
            return false;
        }
        String name = c.getTypes().get( 0 ).getName();
        return "Resource".equals( name );
    }

    protected void scanNestedTypes(ElementDefn root, String path, ElementDefn e, Map<String, BindingSpecification> conceptDomains) throws Exception {
		String tn = null;
		if (e.typeCode().equals("code") && e.hasBinding()) {
			BindingSpecification cd = getConceptDomain(conceptDomains, e.getBindingName());
			if (cd != null && cd.getBinding() == BindingSpecification.Binding.CodeList) {
				tn = getCodeListType(cd.getReference().substring(1));
				if (!enumNames.contains(tn)) {
					enumNames.add(tn);
					enums.add(e);
				}
				typeNames.put(e,  "Enumeration<"+tn+">");
			}
		}
		if (tn == null) {
			if (e.getTypes().size() > 0 && !e.usesCompositeType()) {
				tn = e.typeCode();
//				if (clss != JavaGenClass.Resource) {
//					if (tn.equals("boolean")) tn = "Boolean";
//					else if (tn.equals("integer")) tn = "Integer";
//					else if (tn.equals("decimal")) tn = "Decimal";
//					else if (tn.equals("base64Binary")) tn = "Base64Binary";
//					else if (tn.equals("instant")) tn = "Instant";
//					else if (tn.equals("string")) tn = "String_";
//          else if (tn.equals("uri")) tn = "Uri";
//          else if (tn.equals("xml:lang")) tn = "Code";
//					else if (tn.equals("code")) tn = "Code";
//					else if (tn.equals("oid")) tn = "Oid";
//          else if (tn.equals("uuid")) tn = "Uuid";
//          else if (tn.equals("idref")) tn = "String";
//					else if (tn.equals("sid")) tn = "Sid";
//					else if (tn.equals("id")) tn = "Id";
//					else if (tn.equals("date")) tn = "Date";
//					else if (tn.equals("dateTime")) tn = "DateTime";
//					else 
//						tn = getTypeName(e);
//				} else 
				tn = getTypeName(e);
				if (e.typeCode().equals("xml:lang"))
				  tn = "Code";
				if (e.getTypes().get(0).isUnboundGenericParam())
					tn = "T";
				else if (e.getTypes().get(0).isIdRef())
					tn ="String_";
				else if (e.isXhtmlElement()) 
					tn = "XhtmlNode";
				else if (e.getTypes().get(0).isWildcardType())
					tn ="org.hl7.fhir.instance.model.Type";

				typeNames.put(e,  tn);
			} else {
				if (e.usesCompositeType()) {
					tn = typeNames.get(getElementForPath(root, e.typeCode().substring(1)));
					typeNames.put(e,  tn);
//				} else if (e.getDeclaredTypeName() != null) {
//					tn = e.getDeclaredTypeName();
//					typeNames.put(e,  tn);
//					System.out.println(tn);
				} else {
					if (e.getDeclaredTypeName() != null) 
						tn = e.getDeclaredTypeName();
					else
						tn = getTitle(e.getName());
					if (tn.equals("Element"))
						tn = "Element_";
					strucs.add(e);
					if (typeNameStrings.contains(tn)) {
						char i = 'A';
						while (typeNameStrings.contains(tn+i))
							i++;
						tn = tn + i;
					}
					typeNames.put(e,  tn);
					typeNameStrings.add(tn);
					for (ElementDefn c : e.getElements()) {
						scanNestedTypes(root, path+getTitle(e.getName()), c, conceptDomains);
					}
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

	private BindingSpecification getConceptDomain(Map<String, BindingSpecification> conceptDomains, String conceptDomain) {
		for (BindingSpecification cd : conceptDomains.values())
			if (cd.getName().equals(conceptDomain))
				return cd;
		return null;
	}

	private void generateField(ElementDefn root, ElementDefn e, String indent, Map<String,String> aliases ) throws Exception {
		String tn = determineType( typeNames, e, aliases );

		if (e.unbounded()) {
		  jdoc(indent, e.getDefinition());
			if (tn == null && e.usesCompositeType())
				write(indent+"protected List<"+root.getName()+"> "+getElementName(e.getName(), true)+" = new ArrayList<"+root.getName()+">();\r\n");
			else
				write(indent+"protected List<"+tn+"> "+getElementName(e.getName(), true)+" = new ArrayList<"+tn+">();\r\n");
			write("\r\n");
		} else {
      jdoc(indent, e.getDefinition());
			write(indent+"protected "+tn+" "+getElementName(e.getName(), true)+";\r\n");
			write("\r\n");
		}

	}


  protected String getSimpleType(String n) {
    if (n.equals("String_"))
      return "String";
    if (n.equals("Code"))
      return "String";
    if (n.equals("Base64Binary"))
      return "byte[]";
    if (n.equals("Uri"))
      return "String";
    if (n.equals("Oid"))
      return "String";
    if (n.equals("Integer"))
      return "int";
    if (n.equals("Boolean"))
      return "boolean";
    if (n.equals("Decimal"))
      return "BigDecimal";
    if (n.equals("DateTime"))
      return "DateAndTime";
    if (n.equals("Date"))
      return "DateAndTime";
    if (n.equals("Id"))
      return "String";
    if (n.equals("Instant"))
      return "DateAndTime";
    
    String tns = null;
    if (n.indexOf("<") > 0) {
      tns = n.substring(n.indexOf("<")+1);
      tns = tns.substring(0, tns.length()-1);
    }

    if (tns != null && enumNames.contains(tns))
      return tns;
    
    return "??";
  }
	private void generateAccessors(ElementDefn root, ElementDefn e, String indent, String className, Map<String,String> aliases ) throws Exception {
		String tn = determineType( typeNames, e, aliases );


		if (e.unbounded()) {
		  jdoc(indent, "@return {@link #"+getElementName(e.getName(), true)+"} ("+e.getDefinition()+")");
			if (tn == null && e.usesCompositeType())
				write(indent+"public List<"+root.getName()+"> get"+getTitle(getElementName(e.getName(), false))+"() { \r\n");
			else
				write(indent+"public List<"+tn+"> get"+getTitle(getElementName(e.getName(), false))+"() { \r\n");
			write(indent+"  return this."+getElementName(e.getName(), true)+";\r\n");
			write(indent+"}\r\n");
      write("\r\n");
      write("    // syntactic sugar\r\n");
      jdoc(indent, "@return {@link #"+getElementName(e.getName(), true)+"} ("+e.getDefinition()+")");
			write(indent+"public "+tn+" add"+getTitle(getElementName(e.getName(), false))+"() { \r\n");
      write(indent+"  "+tn+" t = new "+determineConcreteType( typeNames, e, aliases )+"();\r\n");
      write(indent+"  this."+getElementName(e.getName(), true)+".add(t);\r\n");
      write(indent+"  return t;\r\n");
			write(indent+"}\r\n");
			write("\r\n");
      if (e.getTypes().size() == 1 && (definitions.getPrimitives().containsKey(e.typeCode()) || e.getTypes().get(0).isIdRef() || e.typeCode().equals("xml:lang"))) {
        jdoc(indent, "@param value {@link #"+getElementName(e.getName(), true)+"} ("+e.getDefinition()+")");
        write(indent+"public "+tn+" add"+getTitle(getElementName(e.getName(), false))+"Simple("+getSimpleType(tn)+" value) { \r\n");
        write(indent+"  "+tn+" t = new "+ determineConcreteType( typeNames, e, aliases ) +"();\r\n");
        write(indent+"  t.setValue(value);\r\n");
        write(indent+"  this."+getElementName(e.getName(), true)+".add(t);\r\n");
        write(indent+"  return t;\r\n");
        write(indent+"}\r\n");
        write("\r\n");
      }
		} else {
      jdoc(indent, "@return {@link #"+getElementName(e.getName(), true)+"} ("+e.getDefinition()+")");
			write(indent+"public "+tn+" get"+getTitle(getElementName(e.getName(), false))+"() { \r\n");
			write(indent+"  return this."+getElementName(e.getName(), true)+";\r\n");
			write(indent+"}\r\n");
			write("\r\n");
      jdoc(indent, "@param value {@link #"+getElementName(e.getName(), true)+"} ("+e.getDefinition()+")");
			write(indent+"public "+className+" set"+getTitle(getElementName(e.getName(), false))+"("+tn+" value) { \r\n");
      write(indent+"  this."+getElementName(e.getName(), true)+" = value;\r\n");
      write(indent+"  return this;\r\n");
			write(indent+"}\r\n");
			write("\r\n");
			if (e.getTypes().size() == 1 && (definitions.getPrimitives().containsKey(e.typeCode()) || e.getTypes().get(0).isIdRef() || e.typeCode().equals("xml:lang"))) {
	      jdoc(indent, "@return "+e.getDefinition());
	      write(indent+"public "+getSimpleType(tn)+" get"+getTitle(getElementName(e.getName(), false))+"Simple() { \r\n");
	      write(indent+"  return this."+getElementName(e.getName(), true)+" == null ? "+(e.typeCode().equals("boolean") ? "false" : "null")+" : this."+getElementName(e.getName(), true)+".getValue();\r\n");
	      write(indent+"}\r\n");
	      write("\r\n");
	      jdoc(indent, "@param value "+e.getDefinition());
	      write(indent+"public "+className+" set"+getTitle(getElementName(e.getName(), false))+"Simple("+getSimpleType(tn)+" value) { \r\n");
	      if (e.getMinCardinality() == 0) {
	        if (tn.equals("Integer"))
	          write(indent+"  if (value == -1)\r\n");
	        else if (tn.equals("Boolean"))
	          write(indent+"  if (value == false)\r\n");
	        else
	          write(indent+"  if (value == null)\r\n");
	        write(indent+"    this."+getElementName(e.getName(), true)+" = null;\r\n");
	        write(indent+"  else {\r\n");
	      }
	      write(indent+"    if (this."+getElementName(e.getName(), true)+" == null)\r\n");
	      write(indent+"      this."+getElementName(e.getName(), true)+" = new "+tn+"();\r\n");
        write(indent+"    this."+getElementName(e.getName(), true)+".setValue(value);\r\n");
        if (e.getMinCardinality() == 0) {
          write(indent+"  }\r\n");
        }
        write(indent+"  return this;\r\n");
        write(indent+"}\r\n");
	      write("\r\n");
			  
			}
		}

	}


}
