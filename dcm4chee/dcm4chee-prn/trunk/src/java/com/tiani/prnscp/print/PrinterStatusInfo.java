/*                                                                          *
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
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
 */
package com.tiani.prnscp.print;

import javax.print.attribute.EnumSyntax;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  February 6, 2003
 * @version  $Revision$
 */
public class PrinterStatusInfo extends EnumSyntax
{

    /**  Description of the Field */
    public final static PrinterStatusInfo NORMAL = new PrinterStatusInfo(0);
    /**  Description of the Field */
    public final static PrinterStatusInfo BAD_RECEIVE_MGZ = new PrinterStatusInfo(1);
    /**  Description of the Field */
    public final static PrinterStatusInfo BAD_SUPPLY_MGZ = new PrinterStatusInfo(2);
    /**  Description of the Field */
    public final static PrinterStatusInfo CALIBRATING = new PrinterStatusInfo(3);
    /**  Description of the Field */
    public final static PrinterStatusInfo CALIBRATION_ERR = new PrinterStatusInfo(4);
    /**  Description of the Field */
    public final static PrinterStatusInfo CHECK_CHEMISTRY = new PrinterStatusInfo(5);
    /**  Description of the Field */
    public final static PrinterStatusInfo CHECK_SORTER = new PrinterStatusInfo(6);
    /**  Description of the Field */
    public final static PrinterStatusInfo CHEMICALS_EMPTY = new PrinterStatusInfo(7);
    /**  Description of the Field */
    public final static PrinterStatusInfo CHEMICALS_LOW = new PrinterStatusInfo(8);
    /**  Description of the Field */
    public final static PrinterStatusInfo COVER_OPEN = new PrinterStatusInfo(9);
    /**  Description of the Field */
    public final static PrinterStatusInfo ELEC_CONFIG_ERR = new PrinterStatusInfo(10);
    /**  Description of the Field */
    public final static PrinterStatusInfo ELEC_DOWN = new PrinterStatusInfo(11);
    /**  Description of the Field */
    public final static PrinterStatusInfo ELEC_SW_ERROR = new PrinterStatusInfo(12);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_8X10 = new PrinterStatusInfo(13);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_8X10_BLUE = new PrinterStatusInfo(14);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_8X10_CLR = new PrinterStatusInfo(15);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_8X10_PAPR = new PrinterStatusInfo(16);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X12 = new PrinterStatusInfo(17);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X12_BLUE = new PrinterStatusInfo(18);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X12_CLR = new PrinterStatusInfo(19);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X12_PAPR = new PrinterStatusInfo(20);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X14 = new PrinterStatusInfo(21);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X14_BLUE = new PrinterStatusInfo(22);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X14_CLR = new PrinterStatusInfo(23);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_10X14_PAPR = new PrinterStatusInfo(24);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_11X14 = new PrinterStatusInfo(25);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_11X14_BLUE = new PrinterStatusInfo(26);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_11X14_CLR = new PrinterStatusInfo(27);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_11X14_PAPR = new PrinterStatusInfo(28);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X14 = new PrinterStatusInfo(29);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X14_BLUE = new PrinterStatusInfo(30);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X14_CLR = new PrinterStatusInfo(31);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X14_PAPR = new PrinterStatusInfo(32);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X17 = new PrinterStatusInfo(33);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X17_BLUE = new PrinterStatusInfo(34);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X17_CLR = new PrinterStatusInfo(35);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_14X17_PAPR = new PrinterStatusInfo(36);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X24 = new PrinterStatusInfo(37);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X24_BLUE = new PrinterStatusInfo(38);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X24_CLR = new PrinterStatusInfo(39);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X24_PAPR = new PrinterStatusInfo(40);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X30 = new PrinterStatusInfo(41);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X30_BLUE = new PrinterStatusInfo(42);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X30_CLR = new PrinterStatusInfo(43);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_24X30_PAPR = new PrinterStatusInfo(44);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_A4_PAPR = new PrinterStatusInfo(45);
    /**  Description of the Field */
    public final static PrinterStatusInfo EMPTY_A4_TRANS = new PrinterStatusInfo(46);
    /**  Description of the Field */
    public final static PrinterStatusInfo EXPOSURE_FAILURE = new PrinterStatusInfo(47);
    /**  Description of the Field */
    public final static PrinterStatusInfo FILM_JAM = new PrinterStatusInfo(48);
    /**  Description of the Field */
    public final static PrinterStatusInfo FILM_TRANSP_ERR = new PrinterStatusInfo(49);
    /**  Description of the Field */
    public final static PrinterStatusInfo FINISHER_EMPTY = new PrinterStatusInfo(50);
    /**  Description of the Field */
    public final static PrinterStatusInfo FINISHER_ERROR = new PrinterStatusInfo(51);
    /**  Description of the Field */
    public final static PrinterStatusInfo FINISHER_LOW = new PrinterStatusInfo(52);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_8X10 = new PrinterStatusInfo(53);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_8X10_BLUE = new PrinterStatusInfo(54);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_8X10_CLR = new PrinterStatusInfo(55);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_8X10_PAPR = new PrinterStatusInfo(56);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X12 = new PrinterStatusInfo(57);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X12_BLUE = new PrinterStatusInfo(58);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X12_CLR = new PrinterStatusInfo(59);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X12_PAPR = new PrinterStatusInfo(60);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X14 = new PrinterStatusInfo(61);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X14_BLUE = new PrinterStatusInfo(62);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X14_CLR = new PrinterStatusInfo(63);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_10X14_PAPR = new PrinterStatusInfo(64);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_11X14 = new PrinterStatusInfo(65);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_11X14_BLUE = new PrinterStatusInfo(66);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_11X14_CLR = new PrinterStatusInfo(67);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_11X14_PAPR = new PrinterStatusInfo(68);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X14 = new PrinterStatusInfo(69);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X14_BLUE = new PrinterStatusInfo(70);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X14_CLR = new PrinterStatusInfo(71);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X14_PAPR = new PrinterStatusInfo(72);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X17 = new PrinterStatusInfo(73);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X17_BLUE = new PrinterStatusInfo(74);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X17_CLR = new PrinterStatusInfo(75);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_14X17_PAPR = new PrinterStatusInfo(76);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X24 = new PrinterStatusInfo(77);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X24_BLUE = new PrinterStatusInfo(78);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X24_CLR = new PrinterStatusInfo(79);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X24_PAPR = new PrinterStatusInfo(80);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X30 = new PrinterStatusInfo(81);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X30_BLUE = new PrinterStatusInfo(82);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X30_CLR = new PrinterStatusInfo(83);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_24X30_PAPR = new PrinterStatusInfo(84);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_A4_PAPR = new PrinterStatusInfo(85);
    /**  Description of the Field */
    public final static PrinterStatusInfo LOW_A4_TRANS = new PrinterStatusInfo(86);
    /**  Description of the Field */
    public final static PrinterStatusInfo NO_RECEIVE_MGZ = new PrinterStatusInfo(87);
    /**  Description of the Field */
    public final static PrinterStatusInfo NO_RIBBON = new PrinterStatusInfo(88);
    /**  Description of the Field */
    public final static PrinterStatusInfo NO_SUPPLY_MGZ = new PrinterStatusInfo(89);
    /**  Description of the Field */
    public final static PrinterStatusInfo CHECK_PRINTER = new PrinterStatusInfo(90);
    /**  Description of the Field */
    public final static PrinterStatusInfo CHECK_PROC = new PrinterStatusInfo(91);
    /**  Description of the Field */
    public final static PrinterStatusInfo PRINTER_DOWN = new PrinterStatusInfo(92);
    /**  Description of the Field */
    public final static PrinterStatusInfo PRINTER_BUSY = new PrinterStatusInfo(93);
    /**  Description of the Field */
    public final static PrinterStatusInfo PRINTER_BUFFER_FULL = new PrinterStatusInfo(94);
    /**  Description of the Field */
    public final static PrinterStatusInfo PRINTER_INIT = new PrinterStatusInfo(95);
    /**  Description of the Field */
    public final static PrinterStatusInfo PRINTER_OFFLINE = new PrinterStatusInfo(96);
    /**  Description of the Field */
    public final static PrinterStatusInfo PROC_DOWN = new PrinterStatusInfo(97);
    /**  Description of the Field */
    public final static PrinterStatusInfo PROC_INIT = new PrinterStatusInfo(98);
    /**  Description of the Field */
    public final static PrinterStatusInfo PROC_OVERFLOW_FL = new PrinterStatusInfo(99);
    /**  Description of the Field */
    public final static PrinterStatusInfo PROC_OVERFLOW_HI = new PrinterStatusInfo(100);
    /**  Description of the Field */
    public final static PrinterStatusInfo QUEUED = new PrinterStatusInfo(101);
    /**  Description of the Field */
    public final static PrinterStatusInfo RECEIVER_FULL = new PrinterStatusInfo(102);
    /**  Description of the Field */
    public final static PrinterStatusInfo REQ_MED_NOT_INST = new PrinterStatusInfo(103);
    /**  Description of the Field */
    public final static PrinterStatusInfo REQ_MED_NOT_AVAI = new PrinterStatusInfo(104);
    /**  Description of the Field */
    public final static PrinterStatusInfo RIBBON_ERROR = new PrinterStatusInfo(105);
    /**  Description of the Field */
    public final static PrinterStatusInfo SUPPLY_EMPTY = new PrinterStatusInfo(106);
    /**  Description of the Field */
    public final static PrinterStatusInfo SUPPLY_LOW = new PrinterStatusInfo(107);
    /**  Description of the Field */
    public final static PrinterStatusInfo UNKNOWN = new PrinterStatusInfo(108);


    // Constructors --------------------------------------------------
    /**
     *  Constructor for the PrinterStatusInfo object
     *
     * @param  value Description of the Parameter
     */
    protected PrinterStatusInfo(int value)
    {
        super(value);
    }


    // Public --------------------------------------------------------

    // EnumSyntax overrides ---------------------------------------------------
    private final static String[] myStringTable = {
            "NORMAL",
            "BAD RECEIVE MGZ",
            "BAD SUPPLY MGZ",
            "CALIBRATING",
            "CALIBRATION ERR",
            "CHECK CHEMISTRY",
            "CHECK SORTER",
            "CHEMICALS EMPTY",
            "CHEMICALS LOW",
            "COVER OPEN",
            "ELEC CONFIG ERR",
            "ELEC DOWN",
            "ELEC SW ERROR",
            "EMPTY 8X10",
            "EMPTY 8X10 BLUE",
            "EMPTY 8X10 CLR",
            "EMPTY 8X10 PAPR",
            "EMPTY 10X12",
            "EMPTY 10X12 BLUE",
            "EMPTY 10X12 CLR",
            "EMPTY 10X12 PAPR",
            "EMPTY 10X14",
            "EMPTY 10X14 BLUE",
            "EMPTY 10X14 CLR",
            "EMPTY 10X14 PAPR",
            "EMPTY 11X14",
            "EMPTY 11X14 BLUE",
            "EMPTY 11X14 CLR",
            "EMPTY 11X14 PAPR",
            "EMPTY 14X14",
            "EMPTY 14X14 BLUE",
            "EMPTY 14X14 CLR",
            "EMPTY 14X14 PAPR",
            "EMPTY 14X17",
            "EMPTY 14X17 BLUE",
            "EMPTY 14X17 CLR",
            "EMPTY 14X17 PAPR",
            "EMPTY 24X24",
            "EMPTY 24X24 BLUE",
            "EMPTY 24X24 CLR",
            "EMPTY 24X24 PAPR",
            "EMPTY 24X30",
            "EMPTY 24X30 BLUE",
            "EMPTY 24X30 CLR",
            "EMPTY 24X30 PAPR",
            "EMPTY A4 PAPR",
            "EMPTY A4 TRANS",
            "EXPOSURE FAILURE",
            "FILM JAM",
            "FILM TRANSP ERR",
            "FINISHER EMPTY",
            "FINISHER ERROR",
            "FINISHER LOW",
            "LOW 8X10",
            "LOW 8X10 BLUE",
            "LOW 8X10 CLR",
            "LOW 8X10 PAPR",
            "LOW 10X12",
            "LOW 10X12 BLUE",
            "LOW 10X12 CLR",
            "LOW 10X12 PAPR",
            "LOW 10X14",
            "LOW 10X14 BLUE",
            "LOW 10X14 CLR",
            "LOW 10X14 PAPR",
            "LOW 11X14",
            "LOW 11X14 BLUE",
            "LOW 11X14 CLR",
            "LOW 11X14 PAPR",
            "LOW 14X14",
            "LOW 14X14 BLUE",
            "LOW 14X14 CLR",
            "LOW 14X14 PAPR",
            "LOW 14X17",
            "LOW 14X17 BLUE",
            "LOW 14X17 CLR",
            "LOW 14X17 PAPR",
            "LOW 24X24",
            "LOW 24X24 BLUE",
            "LOW 24X24 CLR",
            "LOW 24X24 PAPR",
            "LOW 24X30",
            "LOW 24X30 BLUE",
            "LOW 24X30 CLR",
            "LOW 24X30 PAPR",
            "LOW A4 PAPR",
            "LOW A4 TRANS",
            "NO RECEIVE MGZ",
            "NO RIBBON",
            "NO SUPPLY MGZ",
            "CHECK PRINTER",
            "CHECK PROC",
            "PRINTER DOWN",
            "PRINTER BUSY",
            "PRINTER BUFFER FULL",
            "PRINTER INIT",
            "PRINTER OFFLINE",
            "PROC DOWN",
            "PROC INIT",
            "PROC OVERFLOW FL",
            "PROC OVERFLOW HI",
            "QUEUED",
            "RECEIVER FULL",
            "REQ MED NOT INST",
            "REQ MED NOT AVAI",
            "RIBBON ERROR",
            "SUPPLY EMPTY",
            "SUPPLY LOW",
            "UNKNOWN"
            };

    private final static PrinterStatusInfo[] myEnumValueTable = {
            NORMAL,
            BAD_RECEIVE_MGZ,
            BAD_SUPPLY_MGZ,
            CALIBRATING,
            CALIBRATION_ERR,
            CHECK_CHEMISTRY,
            CHECK_SORTER,
            CHEMICALS_EMPTY,
            CHEMICALS_LOW,
            COVER_OPEN,
            ELEC_CONFIG_ERR,
            ELEC_DOWN,
            ELEC_SW_ERROR,
            EMPTY_8X10,
            EMPTY_8X10_BLUE,
            EMPTY_8X10_CLR,
            EMPTY_8X10_PAPR,
            EMPTY_10X12,
            EMPTY_10X12_BLUE,
            EMPTY_10X12_CLR,
            EMPTY_10X12_PAPR,
            EMPTY_10X14,
            EMPTY_10X14_BLUE,
            EMPTY_10X14_CLR,
            EMPTY_10X14_PAPR,
            EMPTY_11X14,
            EMPTY_11X14_BLUE,
            EMPTY_11X14_CLR,
            EMPTY_11X14_PAPR,
            EMPTY_14X14,
            EMPTY_14X14_BLUE,
            EMPTY_14X14_CLR,
            EMPTY_14X14_PAPR,
            EMPTY_14X17,
            EMPTY_14X17_BLUE,
            EMPTY_14X17_CLR,
            EMPTY_14X17_PAPR,
            EMPTY_24X24,
            EMPTY_24X24_BLUE,
            EMPTY_24X24_CLR,
            EMPTY_24X24_PAPR,
            EMPTY_24X30,
            EMPTY_24X30_BLUE,
            EMPTY_24X30_CLR,
            EMPTY_24X30_PAPR,
            EMPTY_A4_PAPR,
            EMPTY_A4_TRANS,
            EXPOSURE_FAILURE,
            FILM_JAM,
            FILM_TRANSP_ERR,
            FINISHER_EMPTY,
            FINISHER_ERROR,
            FINISHER_LOW,
            LOW_8X10,
            LOW_8X10_BLUE,
            LOW_8X10_CLR,
            LOW_8X10_PAPR,
            LOW_10X12,
            LOW_10X12_BLUE,
            LOW_10X12_CLR,
            LOW_10X12_PAPR,
            LOW_10X14,
            LOW_10X14_BLUE,
            LOW_10X14_CLR,
            LOW_10X14_PAPR,
            LOW_11X14,
            LOW_11X14_BLUE,
            LOW_11X14_CLR,
            LOW_11X14_PAPR,
            LOW_14X14,
            LOW_14X14_BLUE,
            LOW_14X14_CLR,
            LOW_14X14_PAPR,
            LOW_14X17,
            LOW_14X17_BLUE,
            LOW_14X17_CLR,
            LOW_14X17_PAPR,
            LOW_24X24,
            LOW_24X24_BLUE,
            LOW_24X24_CLR,
            LOW_24X24_PAPR,
            LOW_24X30,
            LOW_24X30_BLUE,
            LOW_24X30_CLR,
            LOW_24X30_PAPR,
            LOW_A4_PAPR,
            LOW_A4_TRANS,
            NO_RECEIVE_MGZ,
            NO_RIBBON,
            NO_SUPPLY_MGZ,
            CHECK_PRINTER,
            CHECK_PROC,
            PRINTER_DOWN,
            PRINTER_BUSY,
            PRINTER_BUFFER_FULL,
            PRINTER_INIT,
            PRINTER_OFFLINE,
            PROC_DOWN,
            PROC_INIT,
            PROC_OVERFLOW_FL,
            PROC_OVERFLOW_HI,
            QUEUED,
            RECEIVER_FULL,
            REQ_MED_NOT_INST,
            REQ_MED_NOT_AVAI,
            RIBBON_ERROR,
            SUPPLY_EMPTY,
            SUPPLY_LOW,
            UNKNOWN
            };


    /**
     *  Returns the string table for class PrinterStatusInfo.
     *
     * @return  The stringTable value
     */
    protected String[] getStringTable()
    {
        return myStringTable;
    }


    /**
     *  Returns the enumeration value table for class PrinterStatusInfo.
     *
     * @return  The enumValueTable value
     */
    protected EnumSyntax[] getEnumValueTable()
    {
        return myEnumValueTable;
    }

    // Y implementation ----------------------------------------------
}

