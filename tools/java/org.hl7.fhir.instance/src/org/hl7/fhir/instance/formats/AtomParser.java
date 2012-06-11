package org.hl7.fhir.instance.formats;

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


import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.xmlpull.v1.XmlPullParser;

public class AtomParser extends XmlBase {
  public AtomFeed parse(InputStream input) throws Exception {
    XmlPullParser xpp = loadXml(input);
  
    if (!xpp.getNamespace().equals(ATOM_NS))
      throw new Exception("This does not appear to be an atom feed (wrong namespace '"+xpp.getNamespace()+"') (@ /)");
    return parseAtom(xpp);
  }
  
  public AtomFeed parse(XmlPullParser xpp) throws Exception {
    if (!xpp.getNamespace().equals(ATOM_NS))
      throw new Exception("This does not appear to be an atom feed (wrong namespace '"+xpp.getNamespace()+"') (@ /)");
    return parseAtom(xpp);
  }

  private String parseString(XmlPullParser xpp) throws Exception {
    if (xpp.next() != XmlPullParser.TEXT)
      throw new Exception("No text in String");
    String res = xpp.getText();
    if (xpp.next() != XmlPullParser.END_TAG)
      throw new Exception("Bad String Structure");
    xpp.next();
    return res;
  }
  
  private AtomFeed parseAtom(XmlPullParser xpp) throws Exception {
    AtomFeed res = new AtomFeed();

    if (!xpp.getName().equals("feed"))
      throw new Exception("This does not appear to be an atom feed (wrong name '"+xpp.getName()+"') (@ /)");
    
    int eventType = nextNoWhitespace(xpp);
    while (eventType != XmlPullParser.END_TAG) {
      if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("title")) {
        res.setTitle(parseString(xpp));
      } else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("id"))
        res.setId(parseString(xpp));
      else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("link"))
        res.setLink(parseString(xpp));
      else if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("updated"))
        res.setUpdated(parseDate(xpp));
      else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("entry"))
        res.getEntryList().add(parseEntry(xpp));
      else
        throw new Exception("Bad Xml parsing Agent");
      eventType = nextNoWhitespace(xpp);
    }

    return res;  
  }

  private AtomEntry parseEntry(XmlPullParser xpp) throws Exception {
    AtomEntry res = new AtomEntry();
    
    int eventType = nextNoWhitespace(xpp);
    while (eventType != XmlPullParser.END_TAG) {
      if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("title")) {
        res.setTitle(parseString(xpp));
      } else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("id"))
        res.setId(parseString(xpp));
      else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("link"))
        res.setLink(parseString(xpp));
      else if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("updated"))
        res.setUpdated(parseDate(xpp));
      else if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("published"))
        res.setPublished(parseDate(xpp));
      else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("category"))
        res.setCategory(parseString(xpp));
      else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("author")) {
        eventType = nextNoWhitespace(xpp);
        while (eventType != XmlPullParser.END_TAG) {
          if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("name")) {
            res.setAuthorName(parseString(xpp));
          } else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("uri"))
            res.setAuthorUri(parseString(xpp));
          else
            throw new Exception("Bad Xml parsing entry.author");
          eventType = nextNoWhitespace(xpp);
        }
      }
      else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("content")) {
        nextNoWhitespace(xpp);
        XmlParser p = new XmlParser();
        res.setResource(p.parse(xpp));
      } else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("summary")) {
        nextNoWhitespace(xpp);
        res.setSummary(new XhtmlParser().parseHtmlNode(xpp));
      } else
        throw new Exception("Bad Xml parsing entry");
      eventType = nextNoWhitespace(xpp);
    }

    return res;  
  }

  private Date parseDate(XmlPullParser xpp) throws Exception {
    return xmlToDate(parseString(xpp));    
  }

}