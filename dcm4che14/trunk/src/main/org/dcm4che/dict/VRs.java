/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.dict;


/** 
 * Provides VR constants and VR related utility functions.
 *
 * <p>
 * Further Information regarding Value Representation (DICOM data types)
 * can be found at: <br>
 * <code>PS 3.5 - 2000 Section 6.2 Page 15</code>
 * </p>
 * 
 * @author gunter.zeilinger@tiani.com (Gunter Zeilinger)
 * @author hauer@psicode.com (Sebastian Hauer)
 * @version 1.0
 * @since version 0.1
 */
public class VRs  {
  
    /**
     * Private constructor.
     */
    private VRs() {}
  
    /**
     * NULL element for VRs. Use as VR value for Data Elements, 
     * Item (FFFE,E000), Item Delimitation Item (FFFE,E00D), and
     * Sequence Delimitation Item (FFFE,E0DD).
     */
    public static final int NONE = 0x0000;
    
    /**
     * Application Entity.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters with leading and trailing spaces (20H)
     *    being non-significant. The value made of 16 spaces, meaning
     *    "no application name specified", shall not be used.
     *   </dd>
     *
     *    <dt><b> Character Repertoire: </b></dt>
     *    <dd>
     *     Default Character Repertoire excluding control
     *     characters LF, FF, CR and ESC.
     *    </dd>
     *
     *    <dt><b> Length of Value: </b></dt>
     *    <dd>
     *     16 bytes maximum
     *    </dd>
     * </dl>
     * </p>
     */
    public static final int AE = 0x4145;
    
    /**
     * Age String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters with one of the following formats --
     *    nnnD, nnnW, nnnM, nnnY; where nnn shall contain the number of
     *    days for D, weeks for W, months for M, or years for Y.
     *    Example:  018M  would represent an age of 18 months.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    0 - 9 ,  D ,  W ,  M ,  Y  of Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    4 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int AS = 0x4153;
    
    /**
     * Attribute Tag.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Ordered pair of 16-bit unsigned integers that is the value of a
     *    Data Element Tag. Example: A Data Element Tag of (0018,00FF) would be
     *    encoded as a series of 4 bytes in a Little-Endian Transfer Syntax as
     *    18H,00H,FFH,00H and in a Big-Endian Transfer Syntax 
     *    as 00H,18H,00H,FFH.
     *    Note: The encoding of an AT value is exactly the same as the encoding
     *    of a Data Element Tag as defined in Section 7.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    4 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int AT = 0x4154;
    
    /**
     * Code String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters with leading or trailing spaces (20H)
     *    being non-significant.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Uppercase characters,  0 -  9 , the SPACE character,
     *    and underscore  _ , of the Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    16 bytes maximum
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int CS = 0x4353;
    
    /**
     * Date.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters of the format yyyymmdd; where yyyy shall
     *    contain year, mm shall contain the month, and dd shall contain 
     *    the day. This conforms to the ANSI HISPP MSDS Date common data type.
     *    Example:  19930822  would represent August 22, 1993.
     *    Notes: 1. For reasons of backward compatibility with versions of this
     *    standard prior to V3.0, it is recommended that implementations also
     *    support a string of characters of the format yyyy.mm.dd for this
     *    VR. 2. See also DT VR in this table.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    0 - 9  of Default Character Repertoire Note: For reasons specified
     *    in the previous column, implementations may wish to support
     *    the  .  character as well.
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    8 bytes fixed
     *    Note: For reasons specified in the previous columns, implementations
     *    may also wish to support a 10 byte fixed length as well.
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int DA = 0x4441;
    
    /**
     * Decimal String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters representing either a fixed point number
     *    or a floating point number. A fixed point number shall contain only
     *    the characters 0-9 with an optional leading "+" or "-" and an
     *    optional "." to mark the decimal point. A floating point number shall
     *    be conveyed as defined in ANSI X3.9, with an "E" or "e" to indicate
     *    the start of the exponent. Decimal Strings may be padded with leading
     *    or trailing spaces. Embedded spaces are not allowed.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *     0 - 9 ,  + ,  - ,  E ,  e ,  ." of Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    16 bytes maximum
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int DS = 0x4453;
    
    /**
     * Date Time.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    The Date Time common data type. Indicates a concatenated date-time
     *    ASCII string in the format: YYYYMMDDHHMMSS.FFFFFF&ZZZZ The components
     *    of this string, from left to right, are YYYY = Year, MM = Month,
     *    DD = Day, HH = Hour, MM = Minute, SS = Second,
     *    FFFFFF = Fractional Second, & =  +  or  - ,
     *    and ZZZZ = Hours and Minutes of offset.
     *    &ZZZZ is an optional suffix for plus/minus offset from
     *    Coordinated Universal Time. A component that is omitted from the
     *    string is termed a null component. Trailing null components of
     *    Date Time are ignored. Non-trailing null components are prohibited,
     *    given that the optional suffix is not considered as a component.
     *    Note: For reasons of backward compatibility with versions of this
     *    standard prior to V3.0, many existing DICOM Data Elements use the
     *    separate DA and TM VRs. Standard and Private Data Elements defined in
     *    the future should use DT, when appropriate, to be more compliant with
     *    ANSI HISPP MSDS.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    "0"-"9", "+", "-", "." of Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    26 bytes maximum
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int DT = 0x4454;
    
    /**
     * Floating Point Single.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Single precision binary floating point number represented in
     *    IEEE 754:1985 32-bit Floating Point Number Format.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    4 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int FL = 0x464C;
    
    /**
     * Floating Point Double.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Double precision binary floating point number represented in
     *    IEEE 754:1985 64-bit Floating Point Number Format.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    8 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int FD = 0x4644;
    
    /**
     * Integer String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters representing an Integer in base-10 (decimal),
     *    shall contain only the characters 0 - 9, with an optional leading
     *    "+" or "-". It may be padded with leading and/or trailing spaces.
     *    Embedded spaces are not allowed. The integer, n, represented shall be
     *    in the range: -2 31 <= n <= (2 31 - 1).
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *     0 - 9 ,  + ,  -" of Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    12 bytes maximum
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int IS = 0x4953;
    
    /**
     * Long String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string that may be padded with leading and/or trailing
     *    spaces. The character code 5CH (the BACKSLASH  \  in ISO-IR 6) shall
     *    not be present, as it is used as the delimiter between values in
     *    multiple valued data elements. The string shall not have
     *    Control Characters except for ESC.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Default Character Repertoire and/or as defined by (0008,0005).
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    64 chars maximum (see NOTE in <code>PS 3.5 - 2000 Section 6.2</code>)
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int LO = 0x4C4F;
    
    /**
     * Long Text.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string that may contain one or more paragraphs.
     *    It may contain the Graphic Character set and the Control Characters,
     *    CR, LF, FF, and ESC. It may be padded with trailing spaces, which may
     *    be ignored, but leading spaces are considered to be significant.
     *    Data Elements with this VR shall not be multi-valued and therefore
     *    character code 5CH (the BACKSLASH  \  in ISO-IR 6) may be used.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Default Character Repertoire and/or as defined by (0008,0005).
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    10240 chars maximum
     *    (see NOTE in <code>PS 3.5 - 2000 Section 6.2</code>)
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int LT = 0x4C54;
    
    /**
     * Other Byte String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of bytes where the encoding of the contents is specified by
     *    the negotiated Transfer Syntax. OB is a VR which is insensitive to
     *    Little/Big Endian byte ordering (see Section 7.3).
     *    The string of bytes shall be padded with a single trailing
     *    NULL byte value (00H) when necessary to achieve even length.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    see Transfer Syntax definition
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int OB = 0x4F42;
    
    /**
     * Other Word String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of 16-bit words where the encoding of the contents is
     *    specified by the negotiated Transfer Syntax. OW is a VR which requires
     *    byte swapping within each word when changing between Little Endian and
     *    Big Endian byte ordering (see Section 7.3).
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    see Transfer Syntax definition
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int OW = 0x4F57;
    
    /**
     * Person Name.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string encoded using a 5 component convention.
     *    The character code 5CH (the BACKSLASH  \  in ISO-IR 6) shall not be
     *    present, as it is used as the delimiter between values in multiple
     *    valued data elements. The string may be padded with trailing spaces.
     *    The five components in their order of occurrence are: family name
     *    complex, given name complex, middle name, name prefix, name suffix.
     *    Any of the five components may be an empty string. The component
     *    delimiter shall be the caret  ^  character (5EH). Delimiters are
     *    required for interior null components. Trailing null components and
     *    their delimiters may be omitted. Multiple entries are permitted in 
     *    each component and are encoded as natural text strings, in the format
     *    preferred by the named person. This conforms to the ANSI HISPP MSDS
     *    Person Name common data type. This group of five components is 
     *    referred to as a Person Name component group. For the purpose of 
     *    writing names in ideographic characters and in phonetic characters, 
     *    up to 3 groups of components (see Annex H examples 1 and 2) may be 
     *    used. The delimiter for component groups shall be the equals 
     *    character  =  (3DH).
     *    The three component groups of components in their order of occurrence
     *    are: a single-byte character representation, an ideographic
     *    representation, and a phonetic representation. Any component group may
     *    be absent, including the first component group. In this case, the
     *    person name may start with one or more  =  delimiters. Delimiters are
     *    required for interior null component groups. Trailing null component
     *    groups and their delimiters may be omitted. Precise semantics are
     *    defined for each component group. See section 6.2.1.
     *
     *    <dl>
     *        <dt><b>Examples:</b></dt>
     *        <dd>
     *            <p>
     *            Rev. John Robert Quincy Adams, B.A. M.Div.<br>
     *            Adams^John Robert Quincy^^Rev.^B.A. M.Div.<br>
     *            [One family name; three given names; no middle name;
     *            one prefix; two suffixes.]
     *            </p>
     *            <p>
     *            Susan Morrison-Jones, Ph.D., Chief Executive Officer<br>
     *            Morrison-Jones^Susan^^^Ph.D., Chief Executive Officer<br>
     *            [Two family names; one given name; no middle name; no prefix;
     *            two suffixes.]
     *            </p>
     *            <p>
     *            John Doe<br>
     *            Doe^John<br>
     *            [One family name; one given name; no middle name,
     *            prefix, or suffix. Delimiters have been omitted for
     *            the three trailing null components.]
     *            </p>
     *            <p>
     *            (for examples of the encoding of Person Names using multi-byte
     *            character sets see Annex H)
     *        </dd>
     *        <dt><b>Notes:</b></dt>
     *        <dd>
     *            <p>
     *            1. This five component convention is also used by HL7 as
     *            defined in ASTM E-1238-91 and further specialized by the
     *            ANSI MSDS.
     *            </p>
     *            <p>
     *            2. In typical American and European usage the first occurrence
     *            of  given name would represent the  first name .
     *            The second and subsequent occurrences of the  given name
     *            would typically be treated as a middle name(s). The  middle
     *            name  component is retained for the purpose of backward
     *            compatibility with existing standards.
     *            </p>
     *            <p>
     *            3. The  Degree  component present in ASTM E-1238-91 is
     *            absorbed into the  Suffix  component.
     *            </p>
     *            <p>
     *            4. The implementor should remain mindful of earlier usage
     *            forms which represented  given names  as  first  and  middle
     *            and that translations to and from this previous typical usage
     *            may be required.
     *            </p>
     *            <p>
     *            5. For reasons of backward compatibility with versions of
     *            this standard prior to V3.0, person names might be considered
     *            a single family name complex (single component without
     *            "^" delimiters).
     *            </p>
     *        </dd>
     *    </dl>
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Default Character Repertoire and/or as defined by (0008,0005)
     *    excluding Control Characters LF, FF, and CR but allowing
     *    Control Character ESC.
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    64 chars maximum per component group
     *    (see NOTE in <code>PS 3.5 - 2000 Section 6.2</code>)
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int PN = 0x504E;
    
    /**
     * Short String.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string that may be padded with leading and/or
     *    trailing spaces. The character code 05CH
     *    (the BACKSLASH  \  in ISO-IR 6) shall not be present, as it is used as
     *    the delimiter between values for multiple data elements. The string
     *    shall not have Control Characters except ESC.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Default Character Repertoire and/or as defined by (0008,0005).
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    16 chars maximum (see NOTE in 6.2)
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int SH = 0x5348;
    
    /**
     * Signed Long.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Signed binary integer 32 bits long in 2's complement form.
     *    Represents an integer, n, in the range: - 2 31 <= n <= (2 31 - 1).
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    4 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int SL = 0x534C;
    
    /**
     * Sequence of Items.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Value is a Sequence of zero or more Items, as defined in Section 7.5.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable (see Section 7.5)
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    not applicable (see Section 7.5)
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int SQ = 0x5351;
    
    /**
     * Signed Short.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Signed binary integer 16 bits long in 2's complement form. Represents
     *    an integer n in the range: -2 15 <= n <= (2 15 - 1).
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    2 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int SS = 0x5353;
    
    /**
     * Short Text.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string that may contain one or more paragraphs. It may
     *    contain the Graphic Character set and the Control Characters, CR, LF,
     *    FF, and ESC. It may be padded with trailing spaces, which may be
     *    ignored, but leading spaces are considered to be significant.
     *    Data Elements with this VR shall not be multi-valued and therefore
     *    character code 5CH (the BACKSLASH  \  in ISO-IR 6) may be used.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Default Character Repertoire and/or as defined by (0008,0005).
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    1024 chars maximum (see NOTE in 6.2)
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int ST = 0x5354;
    
    /**
     * Time.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of characters of the format hhmmss.frac;
     *    where hh contains hours (range "00" - "23"),
     *    mm contains minutes (range "00" - "59"),
     *    ss contains seconds (range "00" - "59"), and
     *    frac contains a fractional part of a second as small
     *    as 1 millionth of a second (range  000000  -  999999 ).
     *    A 24 hour clock is assumed. Midnight can be represented by
     *    only  0000  since  2400  would violate the hour range.
     *    The string may be padded with trailing spaces. Leading and embedded
     *    spaces are not allowed. One or more of the components mm, ss, or frac
     *    may be unspecified as long as every component to the right of an
     *    unspecified component is also unspecified. If frac is unspecified the
     *    preceding  .  may not be included. Frac shall be held to six decimal
     *    places or less to ensure its format conforms to the ANSI HISPP MSDS
     *    Time common data type.
     *
     *    <dl>
     *        <dt>Examples:</dt>
     *        <dd>
     *            <p>
     *            1.  070907.0705   represents a time of 7 hours,
     *            9 minutes and 7.0705 seconds.
     *            </p>
     *            <p>
     *            2.  1010  represents a time of 10 hours, and 10 minutes.
     *            </p>
     *            <p>
     *            3.  021   is an invalid value.
     *            </p>
     *        </dd>
     *        <dt>Notes:</dt>
     *        <dd>
     *            <p>
     *            1. For reasons of backward compatibility with versions of
     *            this standard prior to V3.0, it is recommended that
     *            implementations also support a string of characters of the
     *            format hh:mm:ss.frac for this VR.
     *            </p>
     *            <p>
     *            2. See also DT VR in this table.
     *            </p>
     *        </dd>
     *    </dl>
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    "0 - 9 ,  ." of Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    16 bytes maximum
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int TM = 0x544D;
    
    /**
     * Unique Identifier (UID).
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string containing a UID that is used to uniquely identify
     *    a wide variety of items. The UID is a series of numeric components
     *    separated by the period "." character. If a Value Field containing
     *    one or more UIDs is an odd number of bytes in length, the Value Field
     *    shall be padded with a single trailing NULL (00H) character to ensure
     *    that the Value Field is an even number of bytes in length. See Section
     *    9 and Annex B for a complete specification and examples.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    "0 - 9 ,  ." of Default Character Repertoire
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    64 bytes maximum
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int UI = 0x5549;
    
    /**
     * Unsigned Long.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Unsigned binary integer 32 bits long. Represents an integer n in the
     *    range: 0 <= n < 2 32 .
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    4 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int UL = 0x554C;
    
    /**
     * Unknown.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A string of bytes where the encoding of the contents is
     *    unknown (see Section 6.2.2).
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    Any length valid for any of the other DICOM Value Representations
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int UN = 0x554E;
    
    /**
     * Unsigned Short.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    Unsigned binary integer 16 bits long. Represents integer n in
     *    the range: 0 <= n < 2 16 .
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    not applicable
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    2 bytes fixed
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int US = 0x5553;
    
    /**
     * Unlimited Text.
     * <p>
     * <dl>
     *   <dt><b> Definition: </b></dt>
     *   <dd>
     *    A character string that may contain one or more paragraphs.
     *    It may contain the Graphic Character set and the Control Characters,
     *    CR, LF, FF, and ESC. It may be padded with trailing spaces, which may
     *    be ignored, but leading spaces are considered to be significant.
     *    Data Elements with this VR shall not be multi-valued and therefore
     *    character code 5CH (the BACKSLASH  \  in ISO-IR 6) may be used.
     *   </dd>
     *
     *   <dt><b> Character Repertoire: </b></dt>
     *   <dd>
     *    Default Character Repertoire and/or as defined by (0008,0005).
     *   </dd>
     *
     *   <dt><b> Length of Value: </b></dt>
     *   <dd>
     *    2 32 -2
     *    <dl>
     *        <dt>Note:</dt>
     *        <dd>
     *            limited only by the size of the maximum unsigned integer
     *            representable in a 32 bit VL field minus one,
     *            since FFFFFFFFH is reserved.
     *        </dd>
     *    </dl>
     *   </dd>
     * </dl>
     * </p>
     */
    public static final int UT = 0x5554;
        
    /**
     * Returns if Value Length is coded in 2 bytes with explicit VR.
     *
     * @param vr integer const
     * @return  <CODE>true</CODE>, if Value Length is coded in 2 bytes with
     *                             explicit VR<BR>
     *          <CODE>false</CODE>, if Value Length is coded in 4 bytes with
     *                              explicit VR
     */
    public static boolean isLengthField16Bit(int vr) {
        switch (vr) {
            case AE: case AS: case AT: case CS: case DA: case DS: case DT:
            case FL: case FD: case IS: case LO: case LT: case PN: case SH:
            case SL: case SS: case ST: case TM: case UI: case UL: case US:
                return true;
            default:
                return false;
        }
    }//end isLengthField16Bit()
  
    public static int getPadding(int vr) {
        switch (vr) {
            case AE: case AS: case CS: case DA: case DS: case DT: case IS:
            case LO: case LT: case PN: case SH: case SL: case ST: case TM:
            case UT:
                return ' ';
            default:
                return 0;
        }
    }
    
    public static boolean isStringValue(int vr) {
        switch (vr) {
            case AE: case AS: case CS: case DA: case DS: case DT: case IS:
            case LO: case LT: case PN: case SH: case ST: case TM: case UI:
            case UT:
                return true;
        }
        return false;
    }
    
}//end class VR
