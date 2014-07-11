/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package in.raster.mayam.form;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalSliderUI;

/**
 *
 * @author devishree
 */
public class SliderUI extends MetalSliderUI {

    private static final float[] fractions = {0.0f, 0.5f};
//    private static final Color[] fillColors = {
//        new Color(0x2687AE),
//        new Color(0x1658AE)
//    };      
    private static final Color[] backColors = {
        new Color(0x7C818D),
        new Color(0x575C68)
    };
//    private static final Color[] fillColors = {
//        new Color(15, 15, 15),
//        new Color(45, 45, 45)
//    };
//    private static final Color[] fillColors = {
//        new Color(102, 51, 0),
//        new Color(102, 51, 0)
//    };
    
    private static final Color[] fillColors = {
        new Color(175, 125, 105),
        new Color(102, 51, 0)
    };
    private static final Paint hFillGradient = new LinearGradientPaint(0, 0, 0, 11,
            fractions, fillColors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    private static final Paint hBackGradient = new LinearGradientPaint(0, 0, 0, 11,
            fractions, backColors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    private static final Paint vFillGradient = new LinearGradientPaint(0, 0, 11, 0,
            fractions, fillColors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    private static final Paint vBackGradient = new LinearGradientPaint(0, 0, 11, 0,
            fractions, backColors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    private static final Stroke roundEndStroke = new BasicStroke(8,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final float[] thumbFractions = {0.1f, 0.35f};
//    private static final Color[] colors = {
//        new Color(0xBFE9FF),
//        new Color(0x1658AE)
//    };
//    private static final Color[] colors = {
//        new Color(75, 150, 75),
//        new Color(200, 135, 100)
//    };
//    private static final Color[] colors = {
//        new Color(204, 102, 0),
//        new Color(230, 110, 50)
//    };
//    private static final Color[] colors = {
//        new Color(153, 76, 0),
//        new Color(153, 76, 0)
//    };
    private static final Color[] colors = {
        new Color(100, 75, 75),
        new Color(100, 75, 75)
    };
    private static final Paint thumbGradient = new RadialGradientPaint(5, 3, 15,
            thumbFractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);

    public static ComponentUI createUI(JComponent c) {
        return new SliderUI();
    }   

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            d.height += 10;
        } else {
            d.width += 10;
        }
        return d;
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int cy = (trackRect.height / 2) - 2;
            g.translate(trackRect.x, trackRect.y + cy);

            g2.setStroke(roundEndStroke);
            g2.setPaint(hBackGradient);
            g2.drawLine(thumbRect.x, 2, trackRect.width, 2);
            g2.setPaint(hFillGradient);
            g2.drawLine(0, 2, thumbRect.x, 2);

            g.translate(-trackRect.x, -(trackRect.y + cy));
        } else {
            int cx = (trackRect.width / 2) - 2;
            g.translate(trackRect.x + cx, trackRect.y);

            g2.setStroke(roundEndStroke);
            g2.setPaint(vBackGradient);
            g2.drawLine(2, 0, 2, thumbRect.y);
            g2.setPaint(vFillGradient);
            g2.drawLine(2, thumbRect.y, 2, trackRect.height);

            g.translate(-(trackRect.x + cx), -trackRect.y);
        }
    }

    public void paintThumb(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(thumbGradient);
        g.fillRoundRect(thumbRect.x, thumbRect.y + 2, 15, 15, 7, 7);
    }
}
