package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.WadoParams.CONTENT_DISPOSITION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter handles various audio format encodings.
 * 
 * @author bwallace
 * 
 */
public class EncodeAudio implements Filter<ServletResponseItem> {
   private static final Logger log = LoggerFactory.getLogger(EncodeAudio.class);

   /** Encodes the raw dicom object as XML */
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
      DicomObject ds = dicomFullHeader.filter(null, params);
      if (ds == null || !"AU".equals(ds.getString(Tag.Modality)))
         return filterItem.callNextFilter(params);
      if (!ds.contains(Tag.WaveformSequence)) {
         log.warn("Unable to get waveform data.");
         return filterItem.callNextFilter(params);
      }
      return new AudioServletResponseItem(ds, FilterUtil.getString(params, "contentType"));
   }

   private Filter<DicomObject> dicomFullHeader;

   /** Gets the filter that returns the dicom object image header */
   public Filter<DicomObject> getDicomFullHeader() {
      return dicomFullHeader;
   }

   @MetaData(out = "${ref:dicomFullHeader}")
   public void setDicomFullHeader(Filter<DicomObject> dicomFullHeader) {
      this.dicomFullHeader = dicomFullHeader;
   }

}

/** Return the encoded audio to the servlet response. */
class AudioServletResponseItem implements ServletResponseItem {
   private static final Logger log = LoggerFactory.getLogger(AudioServletResponseItem.class);
   DicomObject ds;
   String encoding;

   /** Get the audio information from the object and return it */
   public AudioServletResponseItem(DicomObject ds, String encoding) {
      this.ds = ds;
      this.encoding = encoding;
   }

   /**
    * Write a wave-encoded audio response - currently only handles basic audio
    * and audio/wave
    */
   public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
      DicomObject waveDs = ds.getNestedDicomObject(Tag.WaveformSequence);
      float rate = waveDs.getFloat(Tag.SamplingFrequency);
      int size = waveDs.getInt(Tag.NumberOfWaveformSamples);
      byte[] data = waveDs.getBytes(Tag.WaveformData);
      log.info("Writing approximately {} bytes of audio data - as WAVE", size);
      AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, rate, 8, 1, 1, 1 /rate, false);
      InputStream is = new ByteArrayInputStream(data);
      AudioInputStream stream = new AudioInputStream(is, af, size);
      response.setContentType("audio/wav");
      response.setHeader(CONTENT_DISPOSITION, "inline;filename="+ds.getString(Tag.SOPInstanceUID)+".wav");
      OutputStream os = response.getOutputStream();
      AudioSystem.write(stream, AudioFileFormat.Type.WAVE, os);
      os.close();
   }

}