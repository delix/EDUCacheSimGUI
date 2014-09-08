package edu.fcse.cachesim.gui.construction;

import edu.fcse.cachesim.interfaces.CPUCore;
import edu.fcse.cachesim.interfaces.CacheLevel;
import edu.fcse.cachesim.interfaces.Referable;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.swing.JPanel;

public class DrawArchitecturePanel extends JPanel {

    private final Map<String, Shape> drawnComponents;
    private Set<String> coreTags;
    private Map<String, Referable> elements;
    private Map<String, Set<String>> l3tol2;
    private Map<String, Set<String>> l2tol1;
    private Map<String, Integer> elementToNumCores;
    private int numCores;

    public DrawArchitecturePanel(Map<String, CPUCore> createdCores) {
        this.drawnComponents = new HashMap<>();
        coreTags = new HashSet<>();
        elements = new HashMap<>();
        l3tol2 = new HashMap<>();
        l2tol1 = new HashMap<>();
        elementToNumCores = new HashMap<>();
        this.updateStuff(createdCores);
    }

    public final void updateStuff(Map<String, CPUCore> createdCores) {
        this.numCores = createdCores.size();
        for (String coreTag : createdCores.keySet()) {
            coreTags.add(coreTag);
            CPUCore current = createdCores.get(coreTag);
            elements.put(coreTag, current);
            CacheLevel lvl = current.getLevel(1);
            if (elementToNumCores.containsKey(lvl.getTag())) {
                elementToNumCores.put(lvl.getTag(), elementToNumCores.get(lvl.getTag()) + 1);
            } else {
                elementToNumCores.put(lvl.getTag(), 1);
            }
            String lvl1Tag = lvl.getTag();
            elements.put(lvl.getTag(), lvl);

            lvl = current.getLevel(2);
            if (elementToNumCores.containsKey(lvl.getTag())) {
                elementToNumCores.put(lvl.getTag(), elementToNumCores.get(lvl.getTag()) + 1);
            } else {
                elementToNumCores.put(lvl.getTag(), 1);
            }
            if (!l2tol1.containsKey(lvl.getTag())) {
                l2tol1.put(lvl.getTag(), new HashSet<String>());
            }
            l2tol1.get(lvl.getTag()).add(lvl1Tag);
            String lvl2Tag = lvl.getTag();
            elements.put(lvl.getTag(), lvl);

            lvl = current.getLevel(3);
            if (elementToNumCores.containsKey(lvl.getTag())) {
                elementToNumCores.put(lvl.getTag(), elementToNumCores.get(lvl.getTag()) + 1);
            } else {
                elementToNumCores.put(lvl.getTag(), 1);
            }
            elements.put(lvl.getTag(), lvl);
            if (!l3tol2.containsKey(lvl.getTag())) {
                l3tol2.put(lvl.getTag(), new HashSet<String>());
            }
            l3tol2.get(lvl.getTag()).add(lvl2Tag);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension panelSize = this.getSize();
        int coreWidth = (int) (0.8 * (panelSize.getWidth() / numCores));
        int coreHeight = (int) (0.98 * panelSize.getHeight());
        if (coreWidth > 1.2 * coreHeight) {
            coreWidth = (int) 1.2 * coreHeight;
        }
        if (coreWidth < 50 || coreHeight < 50) {
            g.drawString("Expand window", ((int) panelSize.getWidth() / 2) - 25, 10);
            return;
        }
        drawnComponents.clear();
        // height total = 25 parts
        // core = l1 = 4parts = 16%
        //l2 = 5 = 20%, l3 = 6 = 24%
        //lines = 2parts = 8%
        double coreCircleRadius = 0.16 * coreHeight;
        double lineHeight = 0.08 * coreHeight;
//        drawCores(g, coreWidth, coreCircleRadius);
//        drawLinesFromCores(g, lineHeight);
        drawAllL3s(g, coreWidth, coreHeight);
    }

    private void drawCores(Graphics g, int coreWidth, double coreCircleRadius) {
        double centerOfCore = coreWidth / 2;
//        for (String coreTag : cores.keySet()) {
//            int tagWidth = (int) g.getFontMetrics().getStringBounds(coreTag, g).getWidth();
//            g.drawString(coreTag, (int) (centerOfCore - (tagWidth / 2.0)), 10);
//            Shape newCircle = new Shape(centerOfCore - (coreCircleRadius / 2), 15, coreCircleRadius, coreCircleRadius);
//            drawnComponents.put(coreTag, newCircle);
//            g.drawOval((int) newCircle.x, (int) newCircle.y, (int) coreCircleRadius, (int) coreCircleRadius);
//            centerOfCore += coreWidth;
//        }
    }
    private Map<String, Shape> l3s;

    private void drawAllL3s(Graphics g, int coreWidth, int coreHeight) {
        PriorityQueue<CacheLevel> podredeni = new PriorityQueue<>(new Comparator<CacheLevel>() {

            @Override
            public int compare(CacheLevel o1, CacheLevel o2) {
                return Integer.compare(elementToNumCores.get(o2.getTag()), elementToNumCores.get(o1.getTag()));
            }

        });
        for (String l3tag : l3tol2.keySet()) {
            CacheLevel l3 = (CacheLevel) elements.get(l3tag);
            podredeni.add(l3);
        }
        int currentX = 0;
        int l3Height = (int) (0.24 * coreHeight);
        int currentY = coreHeight - l3Height;
        Queue<String> l3DrawOrder=new LinkedList<String>();
        while (!podredeni.isEmpty()) {
            CacheLevel current = podredeni.poll();
            int numTimes = elementToNumCores.get(current.getTag());
            g.drawRect(currentX + 5, currentY, (numTimes * coreWidth) - 5, l3Height);
            int tagWidth = (int) g.getFontMetrics().getStringBounds(current.getTag(), g).getWidth();
            int centerOfRect = currentX + 5 + ((numTimes * coreWidth - 5) / 2);
            g.drawString(current.getTag(), (int) (centerOfRect - (tagWidth / 2.0)), (int) (currentY + (l3Height / 2)));
            currentX += numTimes * coreWidth;
            l3DrawOrder.add(current.getTag());
        }
        drawAllL2s(g,coreWidth, coreHeight,l3DrawOrder);
    }

    private void drawAllL2s(Graphics g, int coreWidth, int coreHeight, Queue<String> l3DrawOrder) {
        
            int currentX = 0;
        while (!l3DrawOrder.isEmpty()) {
            String l3Tag = l3DrawOrder.poll();
            Set<String> l2tags = l3tol2.get(l3Tag);
            PriorityQueue<CacheLevel> podredeni = new PriorityQueue<>(new Comparator<CacheLevel>() {

                @Override
                public int compare(CacheLevel o1, CacheLevel o2) {
                    return Integer.compare(elementToNumCores.get(o2.getTag()), elementToNumCores.get(o1.getTag()));
                }

            });
            for (String l2 : l2tags) {
                podredeni.add((CacheLevel) elements.get(l2));
            }
            int l3Height = (int) (0.24 * coreHeight);
            int lineHeight = (int) (0.08 * coreHeight);
            int l2Height = (int) (0.2 * coreHeight);
            int currentY = coreHeight - l3Height - lineHeight - l2Height;
            while (!podredeni.isEmpty()) {
                CacheLevel currentL2 = podredeni.poll();
                int numTimes = elementToNumCores.get(currentL2.getTag());
                g.drawRect(currentX + 5, currentY, (numTimes * coreWidth) - 5, l2Height);
                int tagWidth = (int) g.getFontMetrics().getStringBounds(currentL2.getTag(), g).getWidth();
                int centerOfRect = currentX + 5 + ((numTimes * coreWidth - 5) / 2);
                g.drawString(currentL2.getTag(), (int) (centerOfRect - (tagWidth / 2.0)), (int) (currentY + (l2Height / 2)));
                currentX += numTimes * coreWidth;
            }
        }
    }

    private class Shape {

        public Shape(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        public double x, y, width, height;
    }

    private void drawLinesFromCores(Graphics g, double lineHeight) {

    }
}
