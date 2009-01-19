package org.dcm4chee.web.wicket.folder;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public class StudyListHeader extends Panel {

    private int headerExpandLevel = 1;
 
    private final class Row extends WebMarkupContainer {

        private final int entityLevel;

        public Row(String id, int entityLevel) {
            super(id);
            this.entityLevel = entityLevel;
        }

        @Override
        public boolean isVisible() {
            return StudyListHeader.this.headerExpandLevel >= Row.this.entityLevel;
        }

    }

    private final class Cell extends WebMarkupContainer {

        private final int entityLevel;

        public Cell(String id, int entityLevel) {
            super(id);
            this.entityLevel = entityLevel;
            add(new AjaxFallbackLink("collapse"){

                @Override
                public void onClick(AjaxRequestTarget target) {
                    StudyListHeader.this.headerExpandLevel =
                            Cell.this.entityLevel;
                    if (target != null) {
                        target.addComponent(StudyListHeader.this);
                    }
                }

                @Override
                public boolean isVisible() {
                    return StudyListHeader.this.headerExpandLevel
                            > Cell.this.entityLevel;
                }

            });

            add(new AjaxFallbackLink("expand"){

                @Override
                public void onClick(AjaxRequestTarget target) {
                    StudyListHeader.this.headerExpandLevel =
                            Cell.this.entityLevel + 1;
                    if (target != null) {
                        target.addComponent(StudyListHeader.this);
                    }
                }

                @Override
                public boolean isVisible() {
                    return StudyListHeader.this.headerExpandLevel
                            == Cell.this.entityLevel;
                }

            });
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
           super.onComponentTag(tag);
           tag.put("rowspan", 1 + headerExpandLevel - entityLevel);
        }

    }


    public StudyListHeader(String id) {
        super(id);
        setOutputMarkupId(true);
        add(new Row("patient", 0).add(new Cell("cell", 0)));
        add(new Row("study", 1).add(new Cell("cell", 1)));
        add(new Row("pps", 2).add(new Cell("cell", 2)));
        add(new Row("series", 3).add(new Cell("cell", 3)));
        add(new Row("instance", 4).add(new Cell("cell", 4)));
        add(new Row("file", 5));
    }

}
