package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing files after signed from CA
 * @author dbshch
 */
public class getSignedFile implements Serializable {
    private String ret_msg;
    private String document_no;
    private String pdf;
    private String sign;
    private int ret_code;

    // setter and getter

    public String getpdf() { return pdf; }

    public String getdoc_no() { return document_no; }

    public int getRet_code() { return ret_code; }

    public getSignedFile() {}

    public getSignedFile(int ret_code, String ret_msg, String document_no,
                         String pdf, String sign) {
        this.ret_code = ret_code;
        this.ret_msg = ret_msg;
        this.document_no = document_no;
        this.pdf = pdf;
        this.sign = sign;
    }
}
