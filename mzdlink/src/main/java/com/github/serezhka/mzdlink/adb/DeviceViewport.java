package com.github.serezhka.mzdlink.adb;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class DeviceViewport {

    private final int width;
    private final int height;

    private final boolean landscape;

    public DeviceViewport(int width, int height) {
        this.width = width;
        this.height = height;
        landscape = width > height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isLandscape() {
        return landscape;
    }

    @Override
    public String toString() {
        return "DeviceViewport{" + "width=" + width + ", height=" + height + ", landscape=" + landscape + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceViewport that = (DeviceViewport) o;

        if (width != that.width) return false;
        //noinspection SimplifiableIfStatement
        if (height != that.height) return false;
        return landscape == that.landscape;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + (landscape ? 1 : 0);
        return result;
    }
}
