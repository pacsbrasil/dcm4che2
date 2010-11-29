/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chex.archive.common;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since May 8, 2009
 */
public class PatientMatching implements Serializable{

    private static final long serialVersionUID = -5066423063497788483L;

    private static final String PID = "pid";
    private static final String ISSUER = "issuer";
    private static final String FAMILYNAME = "familyname";
    private static final String GIVENNAME = "givenname";
    private static final String MIDDLENAME = "middlename";
    private static final String NAMEPREFIX = "nameprefix";
    private static final String NAMESUFFIX = "namesuffix";
    private static final String BIRTHDATE = "birthdate";
    private static final String SEX = "sex";
    private static final String INITIAL = "(1)";
    private static final String IGNORE = "ignore";

    public static final PatientMatching BY_ID = 
        new PatientMatching(
                true,   // trustPatientIDWithIssuer
                false,  // unknownPatientIDAlwaysMatch
                true,   // unknownIssuerAlwaysMatch
                false,  // familyNameMustMatch
                false,  // familyNameInitialMatch
                true,  // unknownFamilyNameAlwaysMatch
                false, // givenNameMustMatch
                false, // givenNameInitialMatch
                true,  // unknownGivenNameAlwaysMatch
                false, // middleNameMustMatch,
                false, // middleNameInitialMatch
                true,  // unknownMiddleNameAlwaysMatch,
                false, // namePrefixMustMatch,
                false, // namePrefixInitialMatch
                true,  // unknownnNamePrefixAlwaysMatch,
                false, // nameSuffixMustMatch,
                false, // nameSuffixInitialMatch
                true,  // unknownnNameSuffixAlwaysMatch,
                false, // birthDateMustMatch,
                true,  // unknownBirthDateAlwaysMatch
                false, // sexMustMatch,
                true,  // unknownSexAlwaysMatch
                null   // ignore
                );

    public final boolean trustPatientIDWithIssuer;
    public final boolean unknownPatientIDAlwaysMatch;
    public final boolean unknownIssuerAlwaysMatch;
    public final boolean familyNameMustMatch;
    public final boolean familyNameInitialMatch;
    public final boolean unknownFamilyNameAlwaysMatch;
    public final boolean givenNameMustMatch;
    public final boolean givenNameInitialMatch;
    public final boolean unknownGivenNameAlwaysMatch;
    public final boolean middleNameMustMatch;
    public final boolean middleNameInitialMatch;
    public final boolean unknownMiddleNameAlwaysMatch;
    public final boolean namePrefixMustMatch;
    public final boolean namePrefixInitialMatch;
    public final boolean unknownNamePrefixAlwaysMatch;
    public final boolean nameSuffixMustMatch;
    public final boolean nameSuffixInitialMatch;
    public final boolean unknownNameSuffixAlwaysMatch;
    public final boolean birthDateMustMatch;
    public final boolean unknownBirthDateAlwaysMatch;
    public final boolean sexMustMatch;
    public final boolean unknownSexAlwaysMatch;
    public final char[] ignoreChars;
    
    public PatientMatching(String s) {
        int pid = indexOf(s, PID);
        int issuer = indexOf(s, ISSUER);
        int familyName = indexOf(s, FAMILYNAME);
        int givenName = indexOf(s, GIVENNAME);
        int middleName = indexOf(s, MIDDLENAME);
        int namePrefix = indexOf(s, NAMEPREFIX);
        int nameSuffix = indexOf(s, NAMESUFFIX);
        int birthdate = indexOf(s, BIRTHDATE);
        int sex = indexOf(s, SEX);
        int ignore = indexOf(s, IGNORE);
        int trust = s.indexOf("[");
        if (pid == -1 || issuer == -1
                || initialMatch(s, pid, PID)
                || initialMatch(s, issuer, ISSUER)
                || initialMatch(s, birthdate, BIRTHDATE)
                || initialMatch(s, sex, SEX)) {
            throw new IllegalArgumentException(s);
        }
        familyNameMustMatch = familyName != -1;
        givenNameMustMatch = givenName != -1;
        middleNameMustMatch = middleName != -1;
        namePrefixMustMatch = namePrefix != -1;
        nameSuffixMustMatch = nameSuffix != -1;
        birthDateMustMatch = birthdate != -1;
        sexMustMatch = sex != -1;
        unknownPatientIDAlwaysMatch = unknownAlwaysMatch(s, pid, PID, false);
        unknownIssuerAlwaysMatch = unknownAlwaysMatch(s, issuer, ISSUER, false);
        familyNameInitialMatch = initialMatch(s, familyName, FAMILYNAME);
        unknownFamilyNameAlwaysMatch = 
                unknownAlwaysMatch(s, familyName, FAMILYNAME, familyNameInitialMatch);
        givenNameInitialMatch = initialMatch(s, givenName, GIVENNAME);
        unknownGivenNameAlwaysMatch =
                unknownAlwaysMatch(s, givenName, GIVENNAME, givenNameInitialMatch);
        middleNameInitialMatch = initialMatch(s, middleName, MIDDLENAME);
        unknownMiddleNameAlwaysMatch =
                unknownAlwaysMatch(s, middleName, MIDDLENAME, middleNameInitialMatch);
        namePrefixInitialMatch = initialMatch(s, namePrefix, NAMEPREFIX);
        unknownNamePrefixAlwaysMatch =
                unknownAlwaysMatch(s, namePrefix, NAMEPREFIX, namePrefixInitialMatch);
        nameSuffixInitialMatch = initialMatch(s, nameSuffix, NAMESUFFIX);
        unknownNameSuffixAlwaysMatch =
                unknownAlwaysMatch(s, nameSuffix, NAMESUFFIX, nameSuffixInitialMatch);
        unknownBirthDateAlwaysMatch =
                unknownAlwaysMatch(s, birthdate, BIRTHDATE, false);
        unknownSexAlwaysMatch =
                unknownAlwaysMatch(s, sex, SEX, false);
        ignoreChars = ignoreChars(s, ignore);
        if (trust != -1) {
            if (trust < issuer || s.indexOf("]") != s.length()-1
                    || familyNameMustMatch && trust > familyName
                    || givenNameMustMatch && trust > givenName
                    || middleNameMustMatch && trust > middleName
                    || namePrefixMustMatch && trust > namePrefix
                    || nameSuffixMustMatch && trust > nameSuffix
                    || birthDateMustMatch && trust > birthdate
                    || sexMustMatch && trust > sex) {
                throw new IllegalArgumentException(s);
            }
            trustPatientIDWithIssuer = true;
        } else {
            trustPatientIDWithIssuer = !familyNameMustMatch
                    && !givenNameMustMatch && !middleNameMustMatch
                    && !namePrefixMustMatch && !nameSuffixMustMatch
                    && !birthDateMustMatch && !sexMustMatch;
        }
        if (unknownPatientIDAlwaysMatch && !familyNameMustMatch) {
            throw new IllegalArgumentException(s);
        }
    }

    private PatientMatching(boolean trustPatientIDWithIssuer, 
            boolean unknownPatientIDAlwaysMatch,
            boolean unknownIssuerAlwaysMatch,
            boolean familyNameMustMatch,
            boolean familyNameInitialMatch,
            boolean unknownFamilyNameAlwaysMatch,
            boolean givenNameMustMatch,
            boolean givenNameInitialMatch,
            boolean unknownGivenNameAlwaysMatch,
            boolean middleNameMustMatch,
            boolean middleNameInitialMatch,
            boolean unknownMiddleNameAlwaysMatch,
            boolean namePrefixMustMatch,
            boolean namePrefixInitialMatch,
            boolean unknownNamePrefixAlwaysMatch,
            boolean nameSuffixMustMatch,
            boolean nameSuffixInitialMatch,
            boolean unknownNameSuffixAlwaysMatch,
            boolean birthDateMustMatch,
            boolean unknownBirthDateAlwaysMatch,
            boolean sexMustMatch,
            boolean unknownSexAlwaysMatch,
            String ignore) {
        this.trustPatientIDWithIssuer = trustPatientIDWithIssuer;
        this.unknownPatientIDAlwaysMatch = unknownPatientIDAlwaysMatch;
        this.unknownIssuerAlwaysMatch = unknownIssuerAlwaysMatch;
        this.familyNameMustMatch = familyNameMustMatch;
        this.familyNameInitialMatch = familyNameInitialMatch;
        this.unknownFamilyNameAlwaysMatch = unknownFamilyNameAlwaysMatch;
        this.givenNameMustMatch = givenNameMustMatch;
        this.givenNameInitialMatch = givenNameInitialMatch;
        this.unknownGivenNameAlwaysMatch = unknownGivenNameAlwaysMatch;
        this.middleNameMustMatch = middleNameMustMatch;
        this.middleNameInitialMatch = middleNameInitialMatch;
        this.unknownMiddleNameAlwaysMatch = unknownMiddleNameAlwaysMatch;
        this.namePrefixMustMatch = namePrefixMustMatch;
        this.namePrefixInitialMatch = namePrefixInitialMatch;
        this.unknownNamePrefixAlwaysMatch = unknownNamePrefixAlwaysMatch;
        this.nameSuffixMustMatch = nameSuffixMustMatch;
        this.nameSuffixInitialMatch = nameSuffixInitialMatch;
        this.unknownNameSuffixAlwaysMatch = unknownNameSuffixAlwaysMatch;
        this.birthDateMustMatch = birthDateMustMatch;
        this.unknownBirthDateAlwaysMatch = unknownBirthDateAlwaysMatch;
        this.sexMustMatch = sexMustMatch;
        this.unknownSexAlwaysMatch = unknownSexAlwaysMatch;
        this.ignoreChars = (ignore != null && ignore.length() != 0) 
                ? ignore.toCharArray() : null;
    }

    public boolean isUnknownPersonNameAlwaysMatch() {
        return unknownFamilyNameAlwaysMatch && unknownGivenNameAlwaysMatch
                && unknownMiddleNameAlwaysMatch && unknownNamePrefixAlwaysMatch
                && unknownNameSuffixAlwaysMatch;
    }

    private boolean initialMatch(String s, int index, String substr) {
        return index != -1 && s.startsWith(INITIAL, index + substr.length());
    }

    private boolean unknownAlwaysMatch(String s, int index, String substr,
            boolean initialMatch) {
        if (index == -1)
            return true;
        int after = index + substr.length();
        if (initialMatch)
            after += INITIAL.length();
        return after < s.length() && s.charAt(after) == '?';
    }

    private char[] ignoreChars(String s, int index) {
        if (index == -1)
            return null;
        int after = index + IGNORE.length();
        int end = s.indexOf(')', after+2);
        if (end == -1 || s.charAt(after) != '(')
            throw new IllegalArgumentException(s);
        char[] ignore = new char[end-after+1];
        s.getChars(after+1, end, ignore, 0);
        return ignore;
    }


    private int indexOf(String str, String substr) {
        int index = str.indexOf(substr);
        if (index != -1) {
            int after;
            if (index > 0 
                    && " ,[".indexOf(str.charAt(index-1)) == -1
                    || (after = index + substr.length()) < str.length() 
                    && " ,]?(".indexOf(str.charAt(after)) == -1) {
                throw new IllegalArgumentException(str);
            }
        }
        return index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PID);
        if (unknownPatientIDAlwaysMatch) {
            sb.append('?');
        }
        sb.append(',').append(ISSUER);
        if (unknownIssuerAlwaysMatch) {
            sb.append('?');
        }
        if (familyNameMustMatch || givenNameMustMatch || middleNameMustMatch
                || namePrefixMustMatch || nameSuffixMustMatch 
                || birthDateMustMatch || sexMustMatch) {
            sb.append(',');
            if (trustPatientIDWithIssuer) {
                sb.append('[');
            }
            int count = 0;
            if (ignoreChars != null) {
                count++;
                sb.append(IGNORE).append('(').append(ignoreChars).append(')');
            }
            if (familyNameMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(FAMILYNAME);
                if (familyNameInitialMatch) {
                    sb.append(INITIAL);
                }
                if (unknownFamilyNameAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (givenNameMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(GIVENNAME);
                if (givenNameInitialMatch) {
                    sb.append(INITIAL);
                }
                if (unknownGivenNameAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (middleNameMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(MIDDLENAME);
                if (middleNameInitialMatch) {
                    sb.append(INITIAL);
                }
                if (unknownMiddleNameAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (namePrefixMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(NAMEPREFIX);
                if (namePrefixInitialMatch) {
                    sb.append(INITIAL);
                }
                if (unknownNamePrefixAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (nameSuffixMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(NAMESUFFIX);
                if (nameSuffixInitialMatch) {
                    sb.append(INITIAL);
                }
                if (unknownNameSuffixAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (birthDateMustMatch) {
                if (count > 0) {
                    sb.append(',');
                }
                sb.append(BIRTHDATE);
                if (unknownBirthDateAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (sexMustMatch) {
                if (count > 0) {
                    sb.append(',');
                }
                sb.append(SEX);
                if (unknownSexAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (trustPatientIDWithIssuer) {
                sb.append(']');
            }
        }
        return sb.toString();
    }

    public Pattern compilePNPattern(String familyName, String givenName,
            String middleName, String namePrefix, String nameSuffix) {
        if (allMatchesFor(familyName, givenName, middleName, namePrefix,
                namePrefix)) {
            return null;
        }
        boolean appendNameSuffix =
                nameSuffixMustMatch && nameSuffix != null;
        boolean appendNamePrefix = appendNameSuffix
                || namePrefixMustMatch && namePrefix != null;
        boolean appendMiddleName = appendNamePrefix
                || middleNameMustMatch && middleName != null;
        boolean appendGivenName = appendMiddleName
                || givenNameMustMatch && givenName != null;
        boolean appendFamilyName = appendGivenName
                || familyNameMustMatch && familyName != null;
        StringBuilder regex = new StringBuilder();
        if (appendFamilyName) {
            appendRegex(regex, ignoreChars(familyName), familyNameMustMatch,
                    familyNameInitialMatch, unknownFamilyNameAlwaysMatch);
            if (appendGivenName) {
                appendRegex(regex, ignoreChars(givenName), givenNameMustMatch,
                        givenNameInitialMatch, unknownGivenNameAlwaysMatch);
                if (appendMiddleName) {
                    appendRegex(regex, ignoreChars(middleName), middleNameMustMatch,
                            middleNameInitialMatch, unknownMiddleNameAlwaysMatch);
                    if (appendNamePrefix) {
                        appendRegex(regex, ignoreChars(namePrefix), namePrefixMustMatch,
                                namePrefixInitialMatch, unknownNamePrefixAlwaysMatch);
                        if (appendNameSuffix) {
                            appendRegex(regex, ignoreChars(nameSuffix), nameSuffixMustMatch,
                                    nameSuffixInitialMatch, unknownNameSuffixAlwaysMatch);
                        }
                    }
                }
            }
        }
        regex.append(".*");
        return Pattern.compile(regex.toString());
    }

    public String ignoreChars(String s) {
        if (ignoreChars == null || s == null)
            return s;

        char[] a = s.toCharArray();
        int rpos = 0;
        int wpos = 0;
        while (rpos < a.length) {
            char c = a[rpos];
            if (!ignore(c))
                a[wpos++] = c;
            rpos++;
        }
        return wpos < rpos ? new String(a, 0, wpos) : s;
    }

    private boolean ignore(char c1) {
        for (char c2 : ignoreChars)
            if (c1 == c2)
                return true;
        return false;
    }

    private static void appendRegex(StringBuilder regex, String value,
            boolean mustMatch, boolean initialMatch,
            boolean unknownAlwaysMatch) {
        if (!mustMatch || value == null) {
            regex.append("[^\\^]*");
        } else if (!initialMatch) {
            regex.append(unknownAlwaysMatch ? "(\\Q" : "\\Q")
                 .append(value)
                 .append(unknownAlwaysMatch ? "\\E)?" : "\\E");
        } else if (value.length() == 1) {
            regex.append(unknownAlwaysMatch ? "(\\Q" : "\\Q")
                 .append(value)
                 .append(unknownAlwaysMatch ? "\\E[^\\^]*)?" : "\\E[^\\^]*");
        } else {
            regex.append("(\\Q")
                 .append(value)
                 .append("\\E|\\Q")
                 .append(value.charAt(0))
                 .append(unknownAlwaysMatch ? "\\E)?" : "\\E)");
        }
        regex.append("\\^");
    }

    public boolean noMatchesFor(String pid, String issuer, String familyName,
            String givenName, String middleName, String namePrefix,
            String nameSuffix, String birthdate, String sex) {
        return !unknownPatientIDAlwaysMatch && pid == null
                || !unknownIssuerAlwaysMatch && issuer == null
                || !(trustPatientIDWithIssuer && pid != null && issuer != null)
                && noMatchesFor(familyName, givenName, middleName, namePrefix,
                        nameSuffix, birthdate, sex);
    }

    public boolean noMatchesFor(String familyName, String givenName,
            String middleName, String namePrefix, String nameSuffix,
            String birthdate, String sex) {
        return !unknownFamilyNameAlwaysMatch && familyName == null
                || !unknownGivenNameAlwaysMatch && givenName == null
                || !unknownMiddleNameAlwaysMatch && middleName == null
                || !unknownNamePrefixAlwaysMatch && namePrefix == null
                || !unknownNameSuffixAlwaysMatch && nameSuffix == null
                || !unknownBirthDateAlwaysMatch && birthdate == null
                || !unknownSexAlwaysMatch && sex == null;
    }

    public boolean allMatchesFor(String familyName, String givenName,
            String middleName, String namePrefix, String nameSuffix) {
        return (!familyNameMustMatch 
                        || unknownFamilyNameAlwaysMatch && familyName == null)
            && (!givenNameMustMatch
                        || unknownGivenNameAlwaysMatch && givenName == null)
            && (!middleNameMustMatch
                        || unknownMiddleNameAlwaysMatch && middleName == null)
            && (!namePrefixMustMatch
                        || unknownNamePrefixAlwaysMatch && namePrefix == null)
            && (!nameSuffixMustMatch
                        || unknownNameSuffixAlwaysMatch && nameSuffix == null);
    }

    public boolean allMatchesFor(String familyName, String givenName,
            String middleName, String namePrefix, String nameSuffix,
            String birthdate, String sex) {
        return allMatchesFor(familyName, givenName, middleName, namePrefix, nameSuffix)
                && (!birthDateMustMatch
                        || unknownBirthDateAlwaysMatch && birthdate == null)
                && (!sexMustMatch
                        || unknownSexAlwaysMatch && sex == null);
    }
}
