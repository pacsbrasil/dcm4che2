/*****************************************************************************
 *                                                                           *
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
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import javax.print.attribute.EnumSyntax;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since February 6, 2003
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class PrinterStatusInfo extends EnumSyntax {
   
   public static final PrinterStatusInfo NORMAL = new PrinterStatusInfo(0);
   public static final PrinterStatusInfo BAD_RECEIVE_MGZ = new PrinterStatusInfo(1);
   public static final PrinterStatusInfo BAD_SUPPLY_MGZ = new PrinterStatusInfo(2);
   public static final PrinterStatusInfo CALIBRATING = new PrinterStatusInfo(3);
   public static final PrinterStatusInfo CALIBRATION_ERR = new PrinterStatusInfo(4);
   public static final PrinterStatusInfo CHECK_CHEMISTRY = new PrinterStatusInfo(5);
   public static final PrinterStatusInfo CHECK_SORTER = new PrinterStatusInfo(6);
   public static final PrinterStatusInfo CHEMICALS_EMPTY = new PrinterStatusInfo(7);
   public static final PrinterStatusInfo CHEMICALS_LOW = new PrinterStatusInfo(8);
   public static final PrinterStatusInfo COVER_OPEN = new PrinterStatusInfo(9);
   public static final PrinterStatusInfo ELEC_CONFIG_ERR = new PrinterStatusInfo(10);
   public static final PrinterStatusInfo ELEC_DOWN = new PrinterStatusInfo(11);
   public static final PrinterStatusInfo ELEC_SW_ERROR = new PrinterStatusInfo(12);
   public static final PrinterStatusInfo EMPTY_8X10 = new PrinterStatusInfo(13);
   public static final PrinterStatusInfo EMPTY_8X10_BLUE = new PrinterStatusInfo(14);
   public static final PrinterStatusInfo EMPTY_8X10_CLR = new PrinterStatusInfo(15);
   public static final PrinterStatusInfo EMPTY_8X10_PAPR = new PrinterStatusInfo(16);
   public static final PrinterStatusInfo EMPTY_10X12 = new PrinterStatusInfo(17);
   public static final PrinterStatusInfo EMPTY_10X12_BLUE = new PrinterStatusInfo(18);
   public static final PrinterStatusInfo EMPTY_10X12_CLR = new PrinterStatusInfo(19);
   public static final PrinterStatusInfo EMPTY_10X12_PAPR = new PrinterStatusInfo(20);
   public static final PrinterStatusInfo EMPTY_10X14 = new PrinterStatusInfo(21);
   public static final PrinterStatusInfo EMPTY_10X14_BLUE = new PrinterStatusInfo(22);
   public static final PrinterStatusInfo EMPTY_10X14_CLR = new PrinterStatusInfo(23);
   public static final PrinterStatusInfo EMPTY_10X14_PAPR = new PrinterStatusInfo(24);
   public static final PrinterStatusInfo EMPTY_11X14 = new PrinterStatusInfo(25);
   public static final PrinterStatusInfo EMPTY_11X14_BLUE = new PrinterStatusInfo(26);
   public static final PrinterStatusInfo EMPTY_11X14_CLR = new PrinterStatusInfo(27);
   public static final PrinterStatusInfo EMPTY_11X14_PAPR = new PrinterStatusInfo(28);
   public static final PrinterStatusInfo EMPTY_14X14 = new PrinterStatusInfo(29);
   public static final PrinterStatusInfo EMPTY_14X14_BLUE = new PrinterStatusInfo(30);
   public static final PrinterStatusInfo EMPTY_14X14_CLR = new PrinterStatusInfo(31);
   public static final PrinterStatusInfo EMPTY_14X14_PAPR = new PrinterStatusInfo(32);
   public static final PrinterStatusInfo EMPTY_14X17 = new PrinterStatusInfo(33);
   public static final PrinterStatusInfo EMPTY_14X17_BLUE = new PrinterStatusInfo(34);
   public static final PrinterStatusInfo EMPTY_14X17_CLR = new PrinterStatusInfo(35);
   public static final PrinterStatusInfo EMPTY_14X17_PAPR = new PrinterStatusInfo(36);
   public static final PrinterStatusInfo EMPTY_24X24 = new PrinterStatusInfo(37);
   public static final PrinterStatusInfo EMPTY_24X24_BLUE = new PrinterStatusInfo(38);
   public static final PrinterStatusInfo EMPTY_24X24_CLR = new PrinterStatusInfo(39);
   public static final PrinterStatusInfo EMPTY_24X24_PAPR = new PrinterStatusInfo(40);
   public static final PrinterStatusInfo EMPTY_24X30 = new PrinterStatusInfo(41);
   public static final PrinterStatusInfo EMPTY_24X30_BLUE = new PrinterStatusInfo(42);
   public static final PrinterStatusInfo EMPTY_24X30_CLR = new PrinterStatusInfo(43);
   public static final PrinterStatusInfo EMPTY_24X30_PAPR = new PrinterStatusInfo(44);
   public static final PrinterStatusInfo EMPTY_A4_PAPR = new PrinterStatusInfo(45);
   public static final PrinterStatusInfo EMPTY_A4_TRANS = new PrinterStatusInfo(46);
   public static final PrinterStatusInfo EXPOSURE_FAILURE = new PrinterStatusInfo(47);
   public static final PrinterStatusInfo FILM_JAM = new PrinterStatusInfo(48);
   public static final PrinterStatusInfo FILM_TRANSP_ERR = new PrinterStatusInfo(49);
   public static final PrinterStatusInfo FINISHER_EMPTY = new PrinterStatusInfo(50);
   public static final PrinterStatusInfo FINISHER_ERROR = new PrinterStatusInfo(51);
   public static final PrinterStatusInfo FINISHER_LOW = new PrinterStatusInfo(52);
   public static final PrinterStatusInfo LOW_8X10 = new PrinterStatusInfo(53);
   public static final PrinterStatusInfo LOW_8X10_BLUE = new PrinterStatusInfo(54);
   public static final PrinterStatusInfo LOW_8X10_CLR = new PrinterStatusInfo(55);
   public static final PrinterStatusInfo LOW_8X10_PAPR = new PrinterStatusInfo(56);
   public static final PrinterStatusInfo LOW_10X12 = new PrinterStatusInfo(57);
   public static final PrinterStatusInfo LOW_10X12_BLUE = new PrinterStatusInfo(58);
   public static final PrinterStatusInfo LOW_10X12_CLR = new PrinterStatusInfo(59);
   public static final PrinterStatusInfo LOW_10X12_PAPR = new PrinterStatusInfo(60);
   public static final PrinterStatusInfo LOW_10X14 = new PrinterStatusInfo(61);
   public static final PrinterStatusInfo LOW_10X14_BLUE = new PrinterStatusInfo(62);
   public static final PrinterStatusInfo LOW_10X14_CLR = new PrinterStatusInfo(63);
   public static final PrinterStatusInfo LOW_10X14_PAPR = new PrinterStatusInfo(64);
   public static final PrinterStatusInfo LOW_11X14 = new PrinterStatusInfo(65);
   public static final PrinterStatusInfo LOW_11X14_BLUE = new PrinterStatusInfo(66);
   public static final PrinterStatusInfo LOW_11X14_CLR = new PrinterStatusInfo(67);
   public static final PrinterStatusInfo LOW_11X14_PAPR = new PrinterStatusInfo(68);
   public static final PrinterStatusInfo LOW_14X14 = new PrinterStatusInfo(69);
   public static final PrinterStatusInfo LOW_14X14_BLUE = new PrinterStatusInfo(70);
   public static final PrinterStatusInfo LOW_14X14_CLR = new PrinterStatusInfo(71);
   public static final PrinterStatusInfo LOW_14X14_PAPR = new PrinterStatusInfo(72);
   public static final PrinterStatusInfo LOW_14X17 = new PrinterStatusInfo(73);
   public static final PrinterStatusInfo LOW_14X17_BLUE = new PrinterStatusInfo(74);
   public static final PrinterStatusInfo LOW_14X17_CLR = new PrinterStatusInfo(75);
   public static final PrinterStatusInfo LOW_14X17_PAPR = new PrinterStatusInfo(76);
   public static final PrinterStatusInfo LOW_24X24 = new PrinterStatusInfo(77);
   public static final PrinterStatusInfo LOW_24X24_BLUE = new PrinterStatusInfo(78);
   public static final PrinterStatusInfo LOW_24X24_CLR = new PrinterStatusInfo(79);
   public static final PrinterStatusInfo LOW_24X24_PAPR = new PrinterStatusInfo(80);
   public static final PrinterStatusInfo LOW_24X30 = new PrinterStatusInfo(81);
   public static final PrinterStatusInfo LOW_24X30_BLUE = new PrinterStatusInfo(82);
   public static final PrinterStatusInfo LOW_24X30_CLR = new PrinterStatusInfo(83);
   public static final PrinterStatusInfo LOW_24X30_PAPR = new PrinterStatusInfo(84);
   public static final PrinterStatusInfo LOW_A4_PAPR = new PrinterStatusInfo(85);
   public static final PrinterStatusInfo LOW_A4_TRANS = new PrinterStatusInfo(86);
   public static final PrinterStatusInfo NO_RECEIVE_MGZ = new PrinterStatusInfo(87);
   public static final PrinterStatusInfo NO_RIBBON = new PrinterStatusInfo(88);
   public static final PrinterStatusInfo NO_SUPPLY_MGZ = new PrinterStatusInfo(89);
   public static final PrinterStatusInfo CHECK_PRINTER = new PrinterStatusInfo(90);
   public static final PrinterStatusInfo CHECK_PROC = new PrinterStatusInfo(91);
   public static final PrinterStatusInfo PRINTER_DOWN = new PrinterStatusInfo(92);
   public static final PrinterStatusInfo PRINTER_BUSY = new PrinterStatusInfo(93);
   public static final PrinterStatusInfo PRINTER_BUFFER_FULL = new PrinterStatusInfo(94);
   public static final PrinterStatusInfo PRINTER_INIT = new PrinterStatusInfo(95);
   public static final PrinterStatusInfo PRINTER_OFFLINE = new PrinterStatusInfo(96);
   public static final PrinterStatusInfo PROC_DOWN = new PrinterStatusInfo(97);
   public static final PrinterStatusInfo PROC_INIT = new PrinterStatusInfo(98);
   public static final PrinterStatusInfo PROC_OVERFLOW_FL = new PrinterStatusInfo(99);
   public static final PrinterStatusInfo PROC_OVERFLOW_HI = new PrinterStatusInfo(100);
   public static final PrinterStatusInfo QUEUED = new PrinterStatusInfo(101);
   public static final PrinterStatusInfo RECEIVER_FULL = new PrinterStatusInfo(102);
   public static final PrinterStatusInfo REQ_MED_NOT_INST = new PrinterStatusInfo(103);
   public static final PrinterStatusInfo REQ_MED_NOT_AVAI = new PrinterStatusInfo(104);
   public static final PrinterStatusInfo RIBBON_ERROR = new PrinterStatusInfo(105);
   public static final PrinterStatusInfo SUPPLY_EMPTY = new PrinterStatusInfo(106);
   public static final PrinterStatusInfo SUPPLY_LOW = new PrinterStatusInfo(107);
   public static final PrinterStatusInfo UNKNOWN = new PrinterStatusInfo(108);
   
   // Constructors --------------------------------------------------
   protected PrinterStatusInfo(int value) {
      super(value);
   }
   
   // Public --------------------------------------------------------
   
   // EnumSyntax overrides ---------------------------------------------------
    private static final String[] myStringTable = {
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

    private static final PrinterStatusInfo[] myEnumValueTable = {
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
     * Returns the string table for class PrinterStatusInfo.
     */
    protected String[] getStringTable() {
	return myStringTable;
    }

    /**
     * Returns the enumeration value table for class PrinterStatusInfo.
     */
    protected EnumSyntax[] getEnumValueTable() {
	return myEnumValueTable;
    }
   
   // Y implementation ----------------------------------------------
}
