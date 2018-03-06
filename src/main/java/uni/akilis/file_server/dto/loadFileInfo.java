package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing file's size and content info
 * @author dbshch 
 */
public class loadFileInfo implements Serializable {
    private float x, y;
    private String pdf;

    // setter and getter

    public float getwidth() { return x; }

    public float getheight() { return y; }

    public String getPdf() { return pdf; }

    public loadFileInfo() {}

    public loadFileInfo(float x, float y, String pdf) {
        this.x = x;
        this.y = y;
        this.pdf = pdf;
    }
}
