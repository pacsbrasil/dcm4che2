package org.dcm4chee.xero.wado;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class keeps the url for the original stream and re-opens it if the stream has been closed, and then
 * is attempted to be re-used.  This prevents re-parsing of the header etc. 
 * 
 * @author bwallace
 */
public class ReopenableImageInputStream extends ImageInputStreamImpl {
   private static final Logger log = LoggerFactory.getLogger(ReopenableImageInputStream.class);

   URL location;
   String fileName;
   ImageInputStream stream;
   File file = null;
   
   static long filesOpened = 0;
   static long filesClosed = 0;
   
   /** Sets the location, and initially opens the stream */
   public ReopenableImageInputStream(URL location) throws IOException {
	  this.location = location;
	  String surl = location.toString();
	  log.debug("String url={}",surl);
	  if( surl.startsWith("file:") ) {
		 this.fileName = location.getFile();
		 location = null;
	  }
	  reopen(true);
   }
   
   /** Sets initially opens the stream */
   public ReopenableImageInputStream(File file) throws IOException {
	   this.file = file;
	   this.fileName = file.getName();
	   location = null;	  
	   reopen(true);
   }

   public void reopen() throws IOException {
	  reopen(false);
   }
   
   static synchronized long openCount() {
	  filesOpened++;
	  return filesOpened-filesClosed;
   }
   
   static synchronized long closeCount() {
	  filesClosed++;
	  return filesOpened-filesClosed;
   }
   
   /** Re-opens the stream, restoring the byte order and position */
   public void reopen(boolean first) throws IOException {
	  if( stream!=null ) return;
	  if (fileName!=null) {
		 if( !first ) log.debug("Re-opening DICOM image from local cache file " + fileName);
		 if (!(file == null)){
			 stream = new FileImageInputStream(file);
		 }else{
			 stream = new FileImageInputStream(new File(fileName));
		 }
	  } else {
		 if( !first ) log.debug("Re-opening DICOM image from remote url {}",location);
		 stream = new FileCacheImageInputStream(location.openStream(),null);
	  }
	  if( getStreamPosition()!=0 ) {
		 stream.seek(getStreamPosition());
		 bitOffset = 0;
	  }
	  log.debug("Open "+filesOpened+" close count "+filesClosed+" delta "+openCount());
   }
   
   public void close() throws IOException {
	  if( stream==null ) return;
	  log.debug("Close file open "+filesOpened+" close count "+filesClosed+" delta "+closeCount());
	  stream.close();
	  stream = null;
   }
   
   public void flushBefore(long pos) throws IOException {
	  if( stream!=null ) stream.flushBefore(pos);
	  super.flushBefore(pos);
   }

   public boolean isCached() {
	  if( stream==null ) return false;
	  return stream.isCached();
   }

   public boolean isCachedFile() {
	  if( stream==null ) return false;
	  return stream.isCachedFile();
   }

   public boolean isCachedMemory() {
	  if( stream==null ) return false;
	  return stream.isCachedMemory();
   }

   public int read() throws IOException {
	  if( stream==null ) reopen();
	  bitOffset = 0;
	  int val = stream.read();
	  if( val!=-1 ) {
		 ++streamPos;
	  }
	  return val;
   }
   
   @Override
   public long length() {
	  if( stream==null ) return -1L;
	  try {
		 return stream.length();
	  }
	  catch(IOException e) {
		 throw new RuntimeException(e);
	  }
   }

   public int read(byte[] b, int off, int len) throws IOException {
	  if( stream==null ) reopen();
	  bitOffset = 0;
	  int nbytes = stream.read(b,off,len);
	  if( nbytes!=-1 ) {
		 streamPos += nbytes;
	  }
	  return nbytes;
   }
   
   @Override
   public void seek(long pos) throws IOException {
	  streamPos = pos;
	  bitOffset = 0;
	  if( stream!=null ) {
		 stream.seek(pos);
		 streamPos = stream.getStreamPosition();
	  }
   }
}
