package com.github.serezhka.mzdlink.socket;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class MinicapHeader {

    private int version;
    private int size;
    private int pid;
    private int realWidth;
    private int realHeight;
    private int virtualWidth;
    private int virtualHeight;
    private int orientation;
    private int quirk;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getRealWidth() {
        return realWidth;
    }

    public void setRealWidth(int realWidth) {
        this.realWidth = realWidth;
    }

    public int getRealHeight() {
        return realHeight;
    }

    public void setRealHeight(int realHeight) {
        this.realHeight = realHeight;
    }

    public int getVirtualWidth() {
        return virtualWidth;
    }

    public void setVirtualWidth(int virtualWidth) {
        this.virtualWidth = virtualWidth;
    }

    public int getVirtualHeight() {
        return virtualHeight;
    }

    public void setVirtualHeight(int virtualHeight) {
        this.virtualHeight = virtualHeight;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getQuirk() {
        return quirk;
    }

    public void setQuirk(int quirk) {
        this.quirk = quirk;
    }

    @Override
    public String toString() {
        return "Header{" +
                "version=" + version +
                ", size=" + size +
                ", pid=" + pid +
                ", realWidth=" + realWidth +
                ", realHeight=" + realHeight +
                ", virtualWidth=" + virtualWidth +
                ", virtualHeight=" + virtualHeight +
                ", orientation=" + orientation +
                ", quirk=" + quirk +
                '}';
    }
}
