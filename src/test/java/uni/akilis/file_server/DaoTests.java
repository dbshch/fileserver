package uni.akilis.file_server;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.entity.UploadFile;


import static org.junit.Assert.*;

/**
 * Created by leo on 11/24/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:config/application-test.properties")
public class DaoTests {

    private static final Logger logger = LoggerFactory.getLogger(DaoTests.class);

    @Autowired
    private IDao iDao;

    @Autowired
    private ApplicationContext appContxt;

    long time = System.currentTimeMillis();
    String originName = "file.pdf";
    String filename = time + "_" + originName;
    long size = 1;

    @Before
    public void before() {
       this.iDao.clearFileDb();
    }

    @Test
    public void testSaveFile() {
        UploadFile uploadFile = this.iDao.saveFile(time++, originName, filename, size);
        assertNotNull(uploadFile.getId());
        this.iDao.saveFile(time++, originName, filename, size);
        this.iDao.saveFile(time++, originName, filename, size);
        assertEquals(3, this.iDao.countFiles());
    }

    @Test
    public void testGetFilenameById() {
        UploadFile uploadFile = this.iDao.saveFile(time++, originName, filename, size);
        assertEquals(filename, this.iDao.getFilenameById(uploadFile.getId()));
    }

    @Test
    public void testFindAllFiles(){
        this.iDao.saveFile(time++, originName, filename, size);
        this.iDao.saveFile(time++, originName, filename, size);
        this.iDao.saveFile(time++, originName, filename, size);
        assertEquals(3, this.iDao.findAllFiles().size());
    }
}
