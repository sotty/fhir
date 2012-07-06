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
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.definitions.ecore.fhir.CompositeTypeDefn;
import org.hl7.fhir.definitions.ecore.fhir.ConstrainedTypeDefn;
import org.hl7.fhir.definitions.ecore.fhir.ElementDefn;
import org.hl7.fhir.definitions.model.Definitions;
import org.hl7.fhir.tools.publisher.PlatformGenerator;
import org.hl7.fhir.utilities.Logger;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.ZipGenerator;

public class CSharpGenerator extends BaseGenerator implements PlatformGenerator {

	public void generate(Definitions definitions, String destDir,
			String implDir, String version, Date genDate, Logger logger)
			throws Exception {

		throw new UnsupportedOperationException("The C# generator uses eCore, not ElementDefn-style definition.");
	}

	public String getName() {
		return "csharp";
	}

	public String getDescription() {
		return "Resource definitions (+ more todo)";
	}

	public String getTitle() {
		return "C#";
	}

	public boolean isECoreGenerator() {
		return true;
	}

	public void generate(org.hl7.fhir.definitions.ecore.fhir.Definitions definitions, String destDir,
			String implDir, Logger logger) throws Exception {

		
		char sl = File.separatorChar;
		String modelGenerationDir =  implDir + "Model" + sl;
		
		File f = new File(modelGenerationDir);
		if( !f.exists() ) f.mkdir();
		
		List<String> filenames = new ArrayList<String>();
	
		{
			CSharpResourceGenerator gen = new CSharpResourceGenerator();
			String enumsFilename = modelGenerationDir + "Bindings.cs";
			TextFile.stringToFile(gen.generateGlobalEnums(definitions.getBindings(),definitions).toString(), enumsFilename);						 
			filenames.add(enumsFilename);
		}

		{
			CSharpPrimitiveGenerator gen = new CSharpPrimitiveGenerator();
			String primFilename = modelGenerationDir + "Primitives.cs";
			TextFile.stringToFile(gen.generatePrimitives(definitions.getPrimitives(),definitions).toString(), primFilename);						 
			filenames.add(primFilename);
		}
		
		List<CompositeTypeDefn> allComplexTypes = new ArrayList<CompositeTypeDefn>();
		allComplexTypes.addAll(definitions.getLocalCompositeTypes());
		allComplexTypes.addAll(definitions.getLocalResources());
		
		for( CompositeTypeDefn composite : allComplexTypes )
		{		
			CSharpResourceGenerator gen = new CSharpResourceGenerator();
			String compositeFilename = modelGenerationDir + composite.getName() + ".cs";			
			TextFile.stringToFile(gen.generateComposite(composite, definitions).toString(), compositeFilename);			
			filenames.add(compositeFilename);
		}

		
		for( ConstrainedTypeDefn constrained : definitions.getLocalConstrainedTypes() )
		{
			CSharpResourceGenerator gen = new CSharpResourceGenerator();
			String constrainedFilename = modelGenerationDir + constrained.getName() + ".cs";
			TextFile.stringToFile(gen.generateConstrained(constrained, definitions).toString(), constrainedFilename);						 
			filenames.add(constrainedFilename);
		}

		
/*
		// Generate a C# file for each Resource class
		for (ResourceDefn resource : definitions.getResources().values()) 
		{
			CSharpResourceGenerator cSharpGen = new CSharpResourceGenerator(
					new FileOutputStream(modelGenerationDir + resource.getName() + ".cs" ));
		
			filenames.add("HL7.Fhir.Instance.Model" + sl + resource.getName()+".cs" );
			cSharpGen.generate(resource.getRoot(), definitions.getBindings(), 
					GenClass.Resource, genDate, version, resource );
		}

		// Generate a C# file for each "future" Resource
	    for (ResourceDefn resource : definitions.getFutureResources().values()) 
	    {
	    	CSharpResourceGenerator cSharpGen = new CSharpResourceGenerator(
					new FileOutputStream(modelGenerationDir + resource.getName() + ".cs" ));
		
			filenames.add("HL7.Fhir.Instance.Model" + sl + resource.getName()+".cs" );
			cSharpGen.generateFutureResource(resource, genDate, version );
	    }
		
		// Generate infrastructure classes
		for (String n : definitions.getInfrastructure().keySet()) 
		{
		      ElementDefn root = definitions.getInfrastructure().get(n); 
		      new CSharpResourceGenerator(
		    	new FileOutputStream(modelGenerationDir+root.getName()+".cs"))
		      		.generate(root, definitions.getBindings(), 
		      				GenClass.Structure, genDate, version, null);
				filenames.add("HL7.Fhir.Instance.Model" + sl + root.getName()+".cs" );
		}

		
		// Generate a C# file for basic types
	    for (String n : definitions.getTypes().keySet())
	    {
	        ElementDefn root = definitions.getTypes().get(n); 
	     
	        GenClass generationType = GenClass.Type;
	        
	        if( root.getName().equals("Quantity"))
	        	generationType = GenClass.Ordered;

	        if( root.hasType("GenericType"))
	        	generationType = GenClass.Generic;

	        new CSharpResourceGenerator(
	        	new FileOutputStream(modelGenerationDir+root.getName()+".cs"))
	        		.generate(root, definitions.getBindings(), 
	        			generationType, genDate, version, null);

	        filenames.add("HL7.Fhir.Instance.Model" + sl + root.getName()+".cs" );
	    }

		// Generate a C# file for structured types (HumanName, Address)
	    for (String n : definitions.getStructures().keySet())
	    {
	        ElementDefn root = definitions.getStructures().get(n); 
	        new CSharpResourceGenerator(
	        	new FileOutputStream(modelGenerationDir+root.getName()+".cs"))
	        		.generate(root, definitions.getBindings(), 
	        				GenClass.Type, genDate, version, null);

	        filenames.add("HL7.Fhir.Instance.Model" + sl + root.getName()+".cs" );
	    }


		// Generate a C# file for inline-defined structured types 
	    // (these are resoure-locally defined structured types)
//	    for (ResourceDefn resource : definitions.getResources().values()) 
//	    {
//		    for (ElementDefn e : resource.getNestedTypes().values())
//		    {
//		        new CSharpResourceGenerator(
//		        	new FileOutputStream(modelGenerationDir+
//		        			e.getName()+".cs"))
//		        		.generate(e, definitions.getBindings(), 
//		        				GenClass.Type, genDate, version, null);
//	
//		        filenames.add("HL7.Fhir.Instance.Model" + sl + e.getName()+".cs" );
//		    }
//	    }

	    
	    // Generate a C# file for Constrained types (Money, Distance, ...)
	    for (DefinedCode cd : definitions.getConstraints().values()) {
	        ElementDefn root = definitions.getTypes().get(cd.getComment()); 
	        new CSharpResourceGenerator(
	        	new FileOutputStream(modelGenerationDir+cd.getCode()+".cs"))
	        		.generateConstraint(cd.getCode(), root.getName(),
	        				cd.getDefinition(), genDate, version);
			filenames.add("HL7.Fhir.Instance.Model" + sl + cd.getCode()+".cs" );  
	    }
*/
	    // Generate C# project file
	    CSharpProjectGenerator projGen = new CSharpProjectGenerator();
	    projGen.build(implDir, filenames);
	    
		ZipGenerator zip = new ZipGenerator(destDir + "CSharp.zip");
		zip.addFiles(modelGenerationDir, "Model" +sl, ".cs");
		zip.addFiles(implDir + sl + "Support" + sl, "Support" +sl, ".cs");
		zip.addFiles(implDir + sl + "Properties" + sl, "Properties"+sl, ".cs");
		zip.addFiles(implDir + sl, "", ".csproj");
		zip.addFiles(implDir + sl, "", ".sln");
		zip.close();		
	}

}
