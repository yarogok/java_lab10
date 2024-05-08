package spring.java_lab10.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring.java_lab10.Model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
