package org.hl7.fhir.instance.model;

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

// Generated on Sat, Jun 9, 2012 09:07+1000 for FHIR v0.03

import java.util.*;

/**
 * A person who is involved in the healthcare process
 */
public class Person extends Resource {

    public enum LanguageUse {
        none, // The person does not speak the language at all
        poor, // The person has minimal functional capability in the language
        useable, // The person can use the language, but may not be full conversant, particularly with regards to health concepts
        fluent; // The person is fully capable of using the language
        public static LanguageUse fromCode(String code) throws Exception {
            if (code == null || "".equals(code))
                return null;
        if ("none".equals(code))
          return none;
        if ("poor".equals(code))
          return poor;
        if ("useable".equals(code))
          return useable;
        if ("fluent".equals(code))
          return fluent;
        throw new Exception("Unknown LanguageUse code '"+code+"'");
        }
        public String toCode() {
          switch (this) {
            case none: return "none";
            case poor: return "poor";
            case useable: return "useable";
            case fluent: return "fluent";
            default: return "?";
          }
        }
    }

    public class Qualification extends Element {
        /**
         * The identifier of a qualification
         */
        private Identifier id;

        /**
         * The type of the qualification
         */
        private CodeableConcept code;

        /**
         * The organisation that confered/confers the qualification
         */
        private ResourceReference institution;

        /**
         * The period for which a qualification is held
         */
        private Interval<Date> period;

        public Identifier getId() { 
          return this.id;
        }

        public void setId(Identifier value) { 
          this.id = value;
        }

        public CodeableConcept getCode() { 
          return this.code;
        }

        public void setCode(CodeableConcept value) { 
          this.code = value;
        }

        public ResourceReference getInstitution() { 
          return this.institution;
        }

        public void setInstitution(ResourceReference value) { 
          this.institution = value;
        }

        public Interval<Date> getPeriod() { 
          return this.period;
        }

        public void setPeriod(Interval<Date> value) { 
          this.period = value;
        }

    }

    public class Language extends Element {
        /**
         * A code that identifies the language
         */
        private String code;

        /**
         * A code the describes how well the language is spoken
         */
        private LanguageUse use;

        public String getCode() { 
          return this.code;
        }

        public void setCode(String value) { 
          this.code = value;
        }

        public LanguageUse getUse() { 
          return this.use;
        }

        public void setUse(LanguageUse value) { 
          this.use = value;
        }

    }

    public class RelatedPerson extends Element {
        /**
         * Identifier the related person - may be a full link to a Person resource, or some other kind of identifier
         */
        private HumanId id;

        /**
         * Code that specifies how this person is related to the subject. A code is required.
         */
        private CodeableConcept role;

        /**
         * A name should be specified for the related person
         */
        private HumanName name;

        /**
         * Contact details (phone, email etc) should be provided for the person
         */
        private List<Contact> contact = new ArrayList<Contact>();

        public HumanId getId() { 
          return this.id;
        }

        public void setId(HumanId value) { 
          this.id = value;
        }

        public CodeableConcept getRole() { 
          return this.role;
        }

        public void setRole(CodeableConcept value) { 
          this.role = value;
        }

        public HumanName getName() { 
          return this.name;
        }

        public void setName(HumanName value) { 
          this.name = value;
        }

        public List<Contact> getContact() { 
          return this.contact;
        }

    }

    /**
     * Identifier for the person that is used to identify the person across multiple disparate systems and also for face to face identification of the person
     */
    private List<HumanId> identifier = new ArrayList<HumanId>();

    /**
     * A name associated with the person
     */
    private List<HumanName> name = new ArrayList<HumanName>();

    /**
     * An address for the person
     */
    private List<Address> address = new ArrayList<Address>();

    /**
     * A contact detail for the person
     */
    private List<Contact> contact = new ArrayList<Contact>();

    /**
     * The birth date for the person
     */
    private DateTime dob;

    /**
     * Administrative Gender
     */
    private CodeableConcept gender;

    /**
     * The religious denomination to which a person professes affiliation
     */
    private CodeableConcept religion;

    /**
     * blah balh
     */
    private List<CodeableConcept> race = new ArrayList<CodeableConcept>();

    /**
     * The qualifications a person has, including formal educational achievements, accreditations, and current certifications. All these qualifications may be used to determine what roles a person may play in a healthcare environment
     */
    private List<Qualification> qualification = new ArrayList<Qualification>();

    /**
     * A language spoken by the person, with proficiency
     */
    private List<Language> language = new ArrayList<Language>();

    /**
     * Other persons who are related to this person. The relationship might be one of several types: kin (familial or marital), financial or legal (such as guardian), biological (e.g. donor, donation-recipient) or casual (i.e. friend).
     */
    private List<RelatedPerson> relatedPerson = new ArrayList<RelatedPerson>();

    public List<HumanId> getIdentifier() { 
      return this.identifier;
    }

    public List<HumanName> getName() { 
      return this.name;
    }

    public List<Address> getAddress() { 
      return this.address;
    }

    public List<Contact> getContact() { 
      return this.contact;
    }

    public DateTime getDob() { 
      return this.dob;
    }

    public void setDob(DateTime value) { 
      this.dob = value;
    }

    public CodeableConcept getGender() { 
      return this.gender;
    }

    public void setGender(CodeableConcept value) { 
      this.gender = value;
    }

    public CodeableConcept getReligion() { 
      return this.religion;
    }

    public void setReligion(CodeableConcept value) { 
      this.religion = value;
    }

    public List<CodeableConcept> getRace() { 
      return this.race;
    }

    public List<Qualification> getQualification() { 
      return this.qualification;
    }

    public List<Language> getLanguage() { 
      return this.language;
    }

    public List<RelatedPerson> getRelatedPerson() { 
      return this.relatedPerson;
    }


}

