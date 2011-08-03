/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.ui.graphic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.weasis.core.ui.Messages;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.util.MouseEventDouble;

/**
 * The Class SelectGraphic.
 * 
 * @author Nicolas Roduit ,Benoit Jacquemoud
 */
public class SelectGraphic extends RectangleGraphic {

    public static final Icon ICON = new ImageIcon(SelectGraphic.class.getResource("/icon/22x22/draw-selection.png")); //$NON-NLS-1$

    public SelectGraphic(float lineThickness, Color paint) {
        super(lineThickness, paint, false);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.sel"); //$NON-NLS-1$
    }

    @Override
    public void paint(Graphics2D g2d, AffineTransform transform) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();
        g2d.setPaint(Color.white);

        float dash[] = { 5F };
        Shape transformedShape = transform == null ? shape : transform.createTransformedShape(shape);

        g2d.setStroke(new BasicStroke(1.0F, 0, 0, 5F, dash, 0));
        g2d.draw(transformedShape);

        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(1.0F, 0, 0, 5F, dash, 5F));
        g2d.draw(transformedShape);

        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    @Override
    public DragSequence createResizeDrag(int i) {
        return new SelectedDragSequence();
    }

    protected class SelectedDragSequence extends AbstractDragGraphic.DefaultDragSequence {

        @Override
        public boolean completeDrag(MouseEventDouble mouseEvent) {
            fireRemoveAndRepaintAction();
            return true;
        }
    }

    @Override
    public boolean isGraphicComplete() {
        return handlePointList.size() > 1;
    }

    @Override
    public boolean isSelected() {
        return true;
    }

    // Overriding is necessary not to compute any inherited mesureTools
    @Override
    public void updateLabel(Object source, DefaultView2d view2d) {
    }

    @Override
    public List<MeasureItem> getMeasurements(org.weasis.core.api.media.data.ImageElement imageElement,
        boolean releaseEvent) {
        return null;
    };

}
