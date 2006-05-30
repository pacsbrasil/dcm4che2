/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.dcm4che2.tool.logger;


import java.io.Writer;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;

import org.apache.log4j.helpers.LogLog;

/**
   SyslogWriter is a wrapper around the java.net.DatagramSocket class
   so that it behaves like a java.io.Writer.

   @since 0.7.3
*/
public class SyslogWriter extends Writer {

  final static int SYSLOG_PORT = 514;
  static String syslogHost;
  private int syslogPort;
  private InetAddress address;
  private DatagramSocket ds;

  public
  SyslogWriter(String syslogHost) {
      this(syslogHost, SYSLOG_PORT);  
  }
  
  public
  SyslogWriter(String syslogHost, int syslogPort) {
    this.syslogHost = syslogHost;
    this.syslogPort = syslogPort;

    try {      
      this.address = InetAddress.getByName(syslogHost);
    }
    catch (UnknownHostException e) {
      LogLog.error("Could not find " + syslogHost +
			 ". All logging will FAIL.", e);
    }

    try {
      this.ds = new DatagramSocket();
    }
    catch (SocketException e) {
      e.printStackTrace(); 
      LogLog.error("Could not instantiate DatagramSocket to " + syslogHost +
			 ". All logging will FAIL.", e);
    }
  }


  public
  void write(char[] buf, int off, int len) throws IOException {
    this.write(new String(buf, off, len));
  }
  
  public
  void write(String string) throws IOException {
    byte[] bytes = string.getBytes();
    DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
					       address, syslogPort);

    if(this.ds != null)
      ds.send(packet);
    
  }

  public
  void flush() {}

  public
  void close() {}
}
