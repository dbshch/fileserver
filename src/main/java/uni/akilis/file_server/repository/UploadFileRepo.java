package uni.akilis.file_server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uni.akilis.file_server.entity.UploadFile;

/**
 * Created by leo on 12/25/17.
 */

@Repository
public interface UploadFileRepo extends CrudRepository<UploadFile, Integer> {
    String findFilenameById(Integer id);
}
