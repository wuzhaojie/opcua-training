/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Reciprocal Community License ("RCL") Version 1.00
 * 
 * Unless explicitly acquired and licensed from Licensor under another 
 * license, the contents of this file are subject to the Reciprocal 
 * Community License ("RCL") Version 1.00, or subsequent versions as 
 * allowed by the RCL, and You may not copy or use this file in either 
 * source code or executable form, except in compliance with the terms and 
 * conditions of the RCL.
 * 
 * All software distributed under the RCL is provided strictly on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
 * AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
 * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RCL for specific 
 * language governing rights and limitations under the RCL.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/RCL/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.builtintypes;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.utils.ObjectUtils;


/**
 * This primitive DataType is specified as a string that is composed of a 
 * language component and a country/region component as specified by RFC 3066. 
 * The <country/region> component is always preceded by a hyphen. The format of 
 * the LocaleId string is shown below:
 * 
 *    <language>[-<country/region>], where <language> is the two letter ISO 639 
 *    code for a language, <country/region> is the two letter ISO 3166 code for 
 *    the country/region.
 * 
 * The rules for constructing LocaleIds defined by RFC 3066 are restricted for 
 * OPC UA as follows:
 *  d) OPC UA permits only zero or one <country/region> component to follow the 
 *    <language> component,
 *  e) OPC UA also permits the "-CHS" and "-CHT" three-letter <country/region> 
 *    codes for "Simplified" and "Traditional" Chinese locales.
 *  f) OPC UA also allows the use of other <country/region> codes as deemed 
 *    necessary by the client or the server.
 *    
 * Example:
 *  English				en
 *  English (US)		en-US
 *  German				de
 *  German (Germany)	de-DE
 *  German (Austrian)	de-AT
 * 
 * See Country Codes <code>http://www.iso.org/iso/english_country_names_and_code_elements</code>
 * See Language Codes <code>http://www.loc.gov/standards/iso639-2/php/English_list.php</code> 
 * @see <code>http://www.ietf.org/rfc/rfc3066.txt</code> 
 * @see Locale
 * @author Toni Kalajainen (toni.kalajainen@vtt.fi)
 */
public final class LocalizedText {
	
	/** The pattern of the locale part */
	public static final Pattern LOCALE_PATTERN = Pattern.compile("^(([a-z]{2})(-([A-Z]{2,3}){1})?)?$");
	
	public static final Locale NO_LOCALE = new Locale("", "");
	
	public static final Locale NULL_LOCALE = null;
	
	public static final NodeId ID = Identifiers.LocalizedText;

	public static final LocalizedText NULL = new LocalizedText(null, NULL_LOCALE);
	
	public static final LocalizedText EMPTY = new LocalizedText("", NULL_LOCALE);
	// Empty string in English locale
	public static final LocalizedText EMPTY_EN = english("");
	
	/** Localized text */
	private String text;
	
	/** Optional locale */
	private String locale;

	/**
	 * Convert UA LocateId to {@link Locale} 
	 * @param localeId or null
	 * @return locale or null
	 */
	public static Locale toLocale(String localeId) {
		if (localeId==null) return null;
		Matcher m = LOCALE_PATTERN.matcher(localeId);
		if (!m.matches()) 
			return NO_LOCALE;
			//throw new IllegalArgumentException("Invalid locale \""+localeId+"\""); // changes made 3.6. TODO

		String language = m.group(2);
		String country = m.group(4);
		if (language == null) language = "";
		if (country == null) country = "";
		
		return new Locale(language, country);
	}

	/**
	 * Convert {@link Locale} to UA LocaleId String
	 * @param locale locale or null
	 * @return LocaleId or null
	 */
	public static String toLocaleId(Locale locale) {
		if (locale==null) return null;
		return locale.getLanguage() + (!locale.getCountry().equals("")?"-"+locale.getCountry():"") ;		
	}

	/**
	 * Create new Localized Text
	 * 
	 * @param text Localized text or null
	 * @param localeId <language>[-<country/region>] or null
	 */
	public LocalizedText(String text, String localeId) {
		this.text = text;
		this.locale = localeId;
	}
	
	/**
	 * Create new Localized Text with locale NO_LOCALE
	 * @param text the text
	 */
	public LocalizedText(String text){
		this(text, NO_LOCALE);
	}
	
	/**
	 * Create a english text
	 * 
	 * @param text string
	 * @return english text
	 */
	public static LocalizedText english(String text) {
		return new LocalizedText(text, "en");
	}
	
	/**
	 * Create new localized text
	 * 
	 * @param text or null
	 * @param locale locale or null 
	 */
	public LocalizedText(String text, Locale locale) {
		this.text = text;
		this.locale = (locale == null ? null : locale.toString());
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(text) + 
				3*ObjectUtils.hashCode(locale);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return this.locale == null && this.text == null;
		if (!(obj instanceof LocalizedText)) return false;
		LocalizedText other = (LocalizedText) obj;
		return 
			ObjectUtils.objectEquals(text, other.text) &&
			ObjectUtils.objectEquals(locale, other.locale);
	}

	/**
	 * Get the whole locale string
	 * @return LocaleId
	 */
	public String getLocaleId() {
		return locale;
	}
	
	/**
	 * Get locale object
	 * @return locale or null
	 */
	public Locale getLocale() {
		return toLocale(locale);
	}

	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		if (getLocaleId() == null)
			  return getText();
		return "("+getLocaleId()+") "+getText();
	}
	
}
