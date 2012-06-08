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
 * A code taken from a short list of codes that are not defined in a formal code system
 */
public class Choice extends Type {

    public class Value extends Element {
        /**
         * A possible code or value that the user could have chosen
         */
        private String code;

        /**
         * A set of words associated with the code to give it meaning, if any exist
         */
        private String display;

        public String getCode() { 
          return this.code;
        }

        public void setCode(String value) { 
          this.code = value;
        }

        public String getDisplay() { 
          return this.display;
        }

        public void setDisplay(String value) { 
          this.display = value;
        }

    }

    /**
     * The code or value that the user selected from the list of possible codes
     */
    private String code;

    /**
     * A list of possible values for the code
     */
    private List<Value> value = new ArrayList<Value>();

    /**
     * Whether the order of the values has an assigned meaning
     */
    private boolean isOrdered;

    public String getCode() { 
      return this.code;
    }

    public void setCode(String value) { 
      this.code = value;
    }

    public List<Value> getValue() { 
      return this.value;
    }

    public boolean getIsOrdered() { 
      return this.isOrdered;
    }

    public void setIsOrdered(boolean value) { 
      this.isOrdered = value;
    }


}

