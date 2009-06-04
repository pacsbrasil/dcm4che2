package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.WadoParams.CONTENT_DISPOSITION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.CloseUtils;
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
   //private static final Logger log = LoggerFactory.getLogger(EncodeAudio.class);

   /** Encodes the raw dicom object as XML */
   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
      DicomObject ds = dicomFullHeader.filter(null, params);
      if (ds == null || !ds.contains(Tag.WaveformSequence))
         return filterItem.callNextFilter(params);
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
   
   static Map<String,AudioFormat.Encoding> ENCODINGS = new HashMap<String,AudioFormat.Encoding>();
   static {
      ENCODINGS.put("UB", AudioFormat.Encoding.PCM_UNSIGNED);
      ENCODINGS.put("US", AudioFormat.Encoding.PCM_UNSIGNED);
      ENCODINGS.put("SB", AudioFormat.Encoding.PCM_SIGNED);
      ENCODINGS.put("SS", AudioFormat.Encoding.PCM_SIGNED);
      ENCODINGS.put("MB", AudioFormat.Encoding.ULAW);
      ENCODINGS.put("AB", AudioFormat.Encoding.ALAW);
   };
   
   static HashMap<String, AudioFileFormat.Type> CONTENT_TYPES = new HashMap<String,AudioFileFormat.Type>();
   static {
      CONTENT_TYPES.put("audio/wav", AudioFileFormat.Type.WAVE);
      CONTENT_TYPES.put("audio/snd", AudioFileFormat.Type.SND);
      CONTENT_TYPES.put("audio/basic", AudioFileFormat.Type.AU);
      CONTENT_TYPES.put("audio/x-aiff", AudioFileFormat.Type.AIFF);
      CONTENT_TYPES.put("audio/x-aifc", AudioFileFormat.Type.AIFC);
   }

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
      AudioFormat.Encoding interpretation = ENCODINGS.get(waveDs.getString(Tag.WaveformSampleInterpretation));
      int bits = waveDs.getInt(Tag.WaveformBitsStored,8);
      byte[] data = waveDs.getBytes(Tag.WaveformData);
      int size = data.length;

      InputStream is = new ByteArrayInputStream(data);
      OutputStream os = null;
      try {
            AudioFileFormat.Type type = CONTENT_TYPES.get(encoding);
            AudioFormat af = new AudioFormat(interpretation, rate, bits, 1, 1, 1 / rate, false);
            AudioInputStream stream = new AudioInputStream(is, af, size);
            if (type == null) {
                log.info("Content type {} not found - using audio/wav", encoding);
                encoding = "audio/wav";
                type = AudioFileFormat.Type.WAVE;
            }
            log.info("Encoding response as {}", encoding);
            response.setContentType("audio/wav");
            response.setHeader(CONTENT_DISPOSITION, "inline;filename=" + ds.getString(Tag.SOPInstanceUID) + "."
                    + type.getExtension());
            os = response.getOutputStream();
            AudioSystem.write(stream, type, os);
            os.close();
        } finally {
            CloseUtils.safeClose(is);
            CloseUtils.safeClose(os);
        }
   }

}