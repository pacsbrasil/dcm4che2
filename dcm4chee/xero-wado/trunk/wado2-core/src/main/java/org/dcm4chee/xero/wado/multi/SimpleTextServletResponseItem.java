package org.dcm4chee.xero.wado.multi;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;

/** Just write a simple text response */
public class SimpleTextServletResponseItem implements ServletResponseItem {

    String text;
    public SimpleTextServletResponseItem(String text) {
        this.text = text;
    }
    
    public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter w= response.getWriter();
        w.print(text);
        w.close();
    }

}
