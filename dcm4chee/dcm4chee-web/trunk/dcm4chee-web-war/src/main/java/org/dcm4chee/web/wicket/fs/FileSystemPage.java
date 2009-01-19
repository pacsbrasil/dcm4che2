package org.dcm4chee.web.wicket.fs;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.web.dao.FileSystemHomeLocal;
import org.dcm4chee.web.wicket.util.JNDIUtils;

public class FileSystemPage extends WebPage {

    private static final long serialVersionUID = 1L;

    private List<FileSystem> list;

    @SuppressWarnings("serial")
    public FileSystemPage(final PageParameters parameters) {
        try {
            FileSystemHomeLocal dao = (FileSystemHomeLocal)
                    JNDIUtils.lookup(FileSystemHomeLocal.JNDI_NAME);
            list = dao.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            list = Collections.emptyList();
        }
        
        add(new PropertyListView("list", list) {
            protected void populateItem(ListItem item) {
                FileSystem fs = (FileSystem) item.getModelObject();
                item.add(new Label("pk"));
                item.add(new Label("directoryPath"));
                item.add(new Label("groupID"));
                item.add(new Label("retrieveAET", fs.getRetrieveAET()));
                item.add(new Label("availability"));
                item.add(new Label("status"));
                item.add(new Label("nextFileSystem.directoryPath"));
            }
        });
    }

}
