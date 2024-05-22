package spring.java_lab10.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import spring.java_lab10.Model.Department;
import spring.java_lab10.Model.Role;
import spring.java_lab10.Model.User;
import spring.java_lab10.Repository.DepartmentRepository;
import spring.java_lab10.Repository.RoleRepository;
import spring.java_lab10.Repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private KeyManagementService keyManagementService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public void checkAndCreateDefaultUsers() throws Exception {
        if (userRepository.count() == 0) {
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
            Role roleUser = roleRepository.findByName("ROLE_USER");

            if (roleAdmin == null) {
                roleAdmin = new Role("ROLE_ADMIN");
                roleRepository.save(roleAdmin);
            }

            if (roleUser == null) {
                roleUser = new Role("ROLE_USER");
                roleRepository.save(roleUser);
            }

            Department department1 = departmentRepository.findByName("IT Department");
            Department department2 = departmentRepository.findByName("HR Department");

            if (department1 == null) {
                department1 = new Department();
                department1.setName("IT Department");
                departmentRepository.save(department1);
            }

            if (department2 == null) {
                department2 = new Department();
                department2.setName("HR Department");
                departmentRepository.save(department2);
            }

            String encodedPassword = new BCryptPasswordEncoder().encode("adminpassword1");
            User admin1 = new User("admin1", encodedPassword);
            admin1.setRole(roleAdmin);
            admin1.setDepartment(department1);

            encodedPassword = new BCryptPasswordEncoder().encode("password1");
            User user1 = new User("user1", encodedPassword);
            user1.setRole(roleUser);
            user1.setDepartment(department2);

            keyManagementService.generateAndStoreKeysForUser(admin1);
            keyManagementService.generateAndStoreKeysForUser(user1);
        }
    }

}
