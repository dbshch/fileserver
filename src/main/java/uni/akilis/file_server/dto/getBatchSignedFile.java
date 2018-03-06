package uni.akilis.file_server.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Object containing batch of files after signed from CA
 * @author dbshch
 */
public class getBatchSignedFile implements Serializable {
    private String ret_msg;
    private int ret_code;
    private String sign;
    private List<signPdf> sign_pdf;

    public class signPdf{
        private String document_no;
        private String pdf;

        public String get_pdf() { return pdf; }
        public String getdoc_no() { return document_no; }
    }

    // setter and getter

    public List<signPdf> getPdfList() { return sign_pdf; }

    public int getRet_code() { return ret_code; }
    public String getRet_msg() { return ret_msg; }

    // public getBatchSignedFile() {}

    // public getBatchSignedFile(int ret_code, String ret_msg, String document_no,
    //                           String pdf, String sign) {
    //     this.ret_code = ret_code;
    //     this.ret_msg = ret_msg;
    //     this.document_no = document_no;
    //     this.pdf = pdf;
    //     this.sign = sign;
    // }
}
