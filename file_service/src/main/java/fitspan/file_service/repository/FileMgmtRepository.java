package fitspan.file_service.repository;

import fitspan.file_service.entity.FileMgmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMgmtRepository extends JpaRepository<FileMgmt,String> {
}
