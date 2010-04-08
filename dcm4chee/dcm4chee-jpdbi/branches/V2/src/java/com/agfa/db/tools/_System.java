// $Id: jpdbi2.java,v 1.2 2010-04-01 16:13:17 kianusch Exp $

package com.agfa.db.tools;

public class _System {
   static void exit(int ExitCode) {
       System.out.flush();
       System.out.close();
       System.err.flush();
       System.err.close();
       System.exit(ExitCode);
   }

   static void exit(int ExitCode, String ErrorText) {
       System.err.println(ErrorText);
       exit(ExitCode);
   }
}
