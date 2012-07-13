package org.hl7.fhir.definitions.generators.specification;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.hl7.fhir.definitions.model.DefinedCode;
import org.hl7.fhir.definitions.model.ElementDefn;
import org.hl7.fhir.definitions.model.ExtensionDefn;
import org.hl7.fhir.definitions.model.ExtensionDefn.ContextType;
import org.hl7.fhir.definitions.model.BindingSpecification;
import org.hl7.fhir.definitions.model.ProfileDefn;
import org.hl7.fhir.definitions.model.ResourceDefn;
import org.hl7.fhir.definitions.model.TypeRef;
import org.hl7.fhir.instance.formats.XmlComposer;
import org.hl7.fhir.instance.formats.XmlParser;
//import org.hl7.fhir.instance.model.Factory;
import org.hl7.fhir.instance.model.Narrative;
import org.hl7.fhir.instance.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.Profile;
import org.hl7.fhir.instance.model.Profile.Binding;
import org.hl7.fhir.instance.model.Profile.BindingStrength;
import org.hl7.fhir.instance.model.Profile.BindingType;
import org.hl7.fhir.instance.model.Profile.Concept;
import org.hl7.fhir.instance.model.Profile.Definition;
import org.hl7.fhir.instance.model.Profile.ExtensionContextType;
import org.hl7.fhir.instance.model.Profile.Mapping;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;

public class ProfileGenerator {

  public Profile generate(ProfileDefn profile, OutputStream stream, String html) throws Exception {
    Profile p = new Profile();
    p.setId(profile.metadata("id"));
    p.setName(profile.metadata("name"));
    p.setAuthor(p.new Author());
    p.getAuthor().setName(profile.metadata("author.name"));
    if (profile.hasMetadata("author.reference"))
      p.getAuthor().getReference().add(new URI(profile.metadata("author.reference")));
//  <code> opt Zero+ Coding assist with indexing and finding</code>
    if (profile.hasMetadata("intention"))
      throw new Exception("profile intention is not supported any more ("+p.getName()+")");
    if (profile.hasMetadata("description"))
      p.setDescription(profile.metadata("description"));
    if (profile.hasMetadata("evidence"))
      throw new Exception("profile evidence is not supported any more ("+p.getName()+")");
    if (profile.hasMetadata("comments"))
      throw new Exception("profile comments is not supported any more ("+p.getName()+")");
    if (profile.hasMetadata("date"))
      p.setDate(profile.metadata("date").substring(0, 10));

    if (profile.hasMetadata("status")) 
      p.setStatus(Profile.ResourceProfileStatus.fromCode(profile.metadata("status")));
    
    for (ResourceDefn resource : profile.getResources()) {
      Profile.Resource c = p.new Resource();
      p.getResource().add(c);
      c.setType(resource.getRoot().typeCode());
      // we don't profile URI when we generate in this mode - we are generating an actual statement, not a re-reference
      if (!"".equals(resource.getRoot().getProfileName()))
        c.setName(resource.getRoot().getProfileName());
      // no purpose element here
      defineElement(p, c, resource.getRoot(), resource.getName());
    }
    for (ExtensionDefn ex : profile.getExtensions())
      p.getExtensionDefn().add(generateExtensionDefn(ex, p));
    for (BindingSpecification b : profile.getBindings()) 
      p.getBinding().add(generateBinding(b, p));
    XhtmlNode div = new XhtmlNode();
    div.setName("div");
    div.setNodeType(NodeType.Element);
    div.getChildNodes().add(new XhtmlParser().parseFragment(html));
    p.setText(new Narrative());
    p.getText().setStatus(NarrativeStatus.generated);
    p.getText().setDiv(div);
    XmlComposer comp = new XmlComposer();
    comp.compose(stream, p, true, false);
    
    return p;
  }

  private Binding generateBinding(BindingSpecification src, Profile p) throws Exception {
    Binding dst = p.new Binding();
    dst.setName(src.getName());
    dst.setDefinition(src.getDefinition());
    dst.setType(convert(src.getBinding()));
    dst.setStrength(convert(src.getBindingStrength()));
    dst.setReference(new URI(src.getReference()));
    for (DefinedCode dc : src.getCodes()) {
      Concept cd = p.new Concept();
      cd.setCode(dc.getCode());
      cd.setDisplay(dc.getDisplay());
      cd.setDefinition(dc.getDefinition());
      cd.setSystem(dc.hasSystem() ? new URI(dc.getSystem()) : null);
      dst.getConcept().add(cd);
   }
    
    return dst;
  }

  private BindingStrength convert(org.hl7.fhir.definitions.model.BindingSpecification.BindingStrength bindingStrength) throws Exception {
    if (bindingStrength == org.hl7.fhir.definitions.model.BindingSpecification.BindingStrength.Preferred)
      return BindingStrength.preferred;
    if (bindingStrength == org.hl7.fhir.definitions.model.BindingSpecification.BindingStrength.Required)
      return BindingStrength.required;
    if (bindingStrength == org.hl7.fhir.definitions.model.BindingSpecification.BindingStrength.Suggested)
      return BindingStrength.suggested;
    throw new Exception("unknown value BindingStrength."+bindingStrength.toString());
  }

  private BindingType convert(org.hl7.fhir.definitions.model.BindingSpecification.Binding binding) throws Exception {
    if (binding == org.hl7.fhir.definitions.model.BindingSpecification.Binding.CodeList)
      return BindingType.codelist;
    if (binding == org.hl7.fhir.definitions.model.BindingSpecification.Binding.Reference)
      return BindingType.reference;
    if (binding == org.hl7.fhir.definitions.model.BindingSpecification.Binding.Special)
      return BindingType.special;
    if (binding == org.hl7.fhir.definitions.model.BindingSpecification.Binding.ValueSet)
      return BindingType.valueset;

    throw new Exception("unknown value Binding."+binding.toString());
  }

  private org.hl7.fhir.instance.model.Profile.ExtensionDefn generateExtensionDefn(ExtensionDefn src, Profile p) throws Exception {
    org.hl7.fhir.instance.model.Profile.ExtensionDefn dst = p.new ExtensionDefn();
    dst.setCode(src.getCode());
    dst.setContext(src.getContext());
    dst.setContextType(convertContextType(src.getType()));
    
    ElementDefn dSrc = src.getDefinition();
    Definition dDst = p.new Definition();
    dst.setDefinition(dDst);
    
    dDst.setShort(dSrc.getShortDefn());
    dDst.setFormal(dSrc.getDefinition());
    dDst.setComments(dSrc.getComments());
    dDst.setDataAbsentReason(dSrc.isAllowDAR());
    if (dSrc.getMaxCardinality() == null)
      dDst.setMax("*");
    else
      dDst.setMax(dSrc.getMaxCardinality().toString());
    dDst.setMin(dSrc.getMinCardinality());
    dDst.setMustSupport(dSrc.isMustSupport());
    dDst.setMustUnderstand(dSrc.isMustUnderstand());
    // dDst.
    for (TypeRef t : dSrc.getTypes())
      dDst.getType().add(t.summary());
    if (dSrc.hasRimMapping()) {
      Mapping m = p.new Mapping();
      m.setMap("RIM");
      m.setTarget(dSrc.getRimMapping());
      dDst.getMapping().add(m);
    }
    dDst.setBinding(dSrc.getBindingName());
    return dst;
  }


  private ExtensionContextType convertContextType(ContextType type) throws Exception {
    if (type == ContextType.DataType)
      return ExtensionContextType.datatype;
    if (type == ContextType.Resource)
      return ExtensionContextType.resource;
    if (type == ContextType.Elements)
      return ExtensionContextType.elements;
    if (type == ContextType.Extension)
      return ExtensionContextType.extension;
    if (type == ContextType.Mapping)
      return ExtensionContextType.mapping;
    
    throw new Exception("unknown value ContextType."+type.toString());
  }

  private void defineElement(Profile p, Profile.Resource c, ElementDefn e, String path) throws Exception {
    Profile.Element_ ce = p.new Element_();
    c.getElement().add(ce);
    ce.setPath(path);
    if (!"".equals(e.getProfileName()))
      ce.setName(e.getProfileName());
    ce.setDefinition(p.new Definition());
    if (!"".equals(e.getComments()))
      ce.getDefinition().setComments(e.getComments());
    if (!"".equals(e.getShortDefn()))
      ce.getDefinition().setShort(e.getShortDefn());
    if (!"".equals(e.getDefinition()))
      ce.getDefinition().setFormal(e.getDefinition());
    
    
    // no purpose here
    ce.getDefinition().setMin(e.getMinCardinality());
    ce.getDefinition().setMax(e.getMaxCardinality() == null ? "*" : e.getMaxCardinality().toString());
    for (TypeRef t : e.getTypes())
      ce.getDefinition().getType().add(t.summaryFormal()); 
    // ce.setConformance(getType(e.getConformance()));
    if (!"".equals(e.getCondition()))
      ce.getDefinition().setCondition(e.getCondition());
    // we don't know mustSupport here
    ce.getDefinition().setMustUnderstand(e.isMustUnderstand());
    // todo: mappings
    // we don't have anything to say about constraints on resources
    if (!"".equals(e.getBindingName()))
      ce.getDefinition().setBinding(e.getBindingName());
    ce.getDefinition().setDataAbsentReason(e.isAllowDAR());
    
    if( e.hasAggregation() )
    {
    	Profile.ResourceA res = p.new ResourceA();
    	res.setBundled(true);
   		res.setProfile(new URI(e.getAggregation()) );
    	ce.setResource(res);
    }
    
    for (ElementDefn child : e.getElements()) {
      defineElement(p, c, child, path+"."+child.getName());
    }
  }

  
}
