package com.resto.reservation.config;

public class ZoneConfig {
    private String name;
    private Bounds bounds;

    public boolean contains(int x, int y) {
        return (x >= bounds.xMin && x <= bounds.xMax) && (y >= bounds.yMin && y <= bounds.yMax);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public static class Bounds {
        private int xMin, xMax, yMin, yMax;

        public int getxMin() {
            return xMin;
        }

        public void setxMin(int xMin) {
            this.xMin = xMin;
        }

        public int getxMax() {
            return xMax;
        }

        public void setxMax(int xMax) {
            this.xMax = xMax;
        }

        public int getyMin() {
            return yMin;
        }

        public void setyMin(int yMin) {
            this.yMin = yMin;
        }

        public int getyMax() {
            return yMax;
        }

        public void setyMax(int yMax) {
            this.yMax = yMax;
        }
    }
}