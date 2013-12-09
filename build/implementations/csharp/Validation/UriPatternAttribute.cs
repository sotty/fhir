﻿using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace Hl7.Fhir.Validation
{
    [AttributeUsage(AttributeTargets.Property, Inherited = false, AllowMultiple = false)]
    public class UriPatternAttribute : ValidationAttribute
    {
        protected override ValidationResult IsValid(object value, ValidationContext validationContext)
        {
            if (validationContext.ObjectType != typeof(Uri))
                throw new ArgumentException("UriPatternAttribute can only be applied to .NET Uri properties");

            if (value == null) return ValidationResult.Success;

            var uri = (Uri)value;

            if (uri.IsAbsoluteUri)
            {
                var uris = uri.ToString();
                
                if (uris.StartsWith("urn:oid:") && !OidPatternAttribute.IsValid(uris))
                    return new ValidationResult(String.Format("Uri uses an urn:oid scheme, but the oid {0} is incorrect", uris));
                else if (uris.StartsWith("urn:uuid:") && !UuidPatternAttribute.IsValid(uris))
                    return new ValidationResult(String.Format("Uri uses an urn:uuid schema, but the uuid {0} is incorrect", uris));
            }

            return ValidationResult.Success;
        }
    }
}