package spring.java_lab10.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.java_lab10.Model.Department;
import spring.java_lab10.Model.Role;
import spring.java_lab10.Model.User;
import spring.java_lab10.Repository.DepartmentRepository;
import spring.java_lab10.Repository.RoleRepository;
import spring.java_lab10.Repository.UserRepository;
import spring.java_lab10.Security.InputValidation;
import spring.java_lab10.Service.KeyManagementService;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private KeyManagementService keyManagementService;

    @GetMapping("/user-list")
    public String userList(Model model) {
        List<User> users = userRepository.findAll();
        List<Role> roles = roleRepository.findAll();
        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("departments", departments);
        return "userList";
    }

    @PostMapping("/add-user")
    public String addUser(@RequestParam String username, @RequestParam String password, @RequestParam Long roleId, @RequestParam Long departmentId, Model model) throws Exception {
        if (!InputValidation.isValidText(username)) {
            model.addAttribute("error", "Неправильний формат імені користувача.");
            return userList(model);
        }
        if (!isValidPassword(password)) {
            model.addAttribute("error", "Неправильний формат паролю.");
            return userList(model);
        }

        String encodedPassword = new BCryptPasswordEncoder().encode(password);
        User user = new User(username, encodedPassword);
        Optional<Role> optionalRole = roleRepository.findById(roleId);

        Optional<Department> optionalDepartment = departmentRepository.findById(departmentId);


        if (optionalRole.isPresent() || optionalDepartment.isPresent()) {
            Role userRole = optionalRole.get();
            user.setRole(userRole);
            Department department = optionalDepartment.get();
            user.setDepartment(department);

            keyManagementService.generateAndStoreKeysForUser(user);
            return "redirect:/user-list";
        } else {
            model.addAttribute("error", " Виникла помилка при роботі з ролями/департаментами.");
            return userList(model);
        }
    }

    @GetMapping("/edit-user")
    public String editUser(@RequestParam Long userId, Model model) {
        Optional<User> optionalUser = userRepository.findById(userId);
        List<Role> roles = roleRepository.findAll();
        List<Department> departments = departmentRepository.findAll(); // Додано отримання списку відділів

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            model.addAttribute("user", user);
            model.addAttribute("roles", roles);
            model.addAttribute("departments", departments); // Додано до моделі
            return "editUser";
        } else {
            return "redirect:/user-list";
        }
    }

    @PostMapping("/edit-user")
    public String updateUser(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam Long roleId,
            @RequestParam Long departmentId, // Додано параметр departmentId
            Model model) {

        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        Optional<Department> optionalDepartment = departmentRepository.findById(departmentId); // Додано пошук відділу

        if (optionalUser.isPresent() && optionalRole.isPresent() && optionalDepartment.isPresent()) { // Перевірка наявності відділу
            User user = optionalUser.get();
            if (!InputValidation.isValidText(username)) {
                model.addAttribute("error", "Неправильний формат імені користувача.");
                return editUser(userId, model);
            }
            if (password != null && !password.isEmpty()) {
                if (!isValidPassword(password)) {
                    model.addAttribute("error", "Неправильний формат паролю.");
                    return editUser(userId, model);
                }
                String encodedPassword = new BCryptPasswordEncoder().encode(password);
                user.setPassword(encodedPassword);
            }
            user.setUsername(username);
            Role userRole = optionalRole.get();
            user.setRole(userRole);

            Department userDepartment = optionalDepartment.get();
            user.setDepartment(userDepartment);

            userRepository.save(user);
        }
        return "redirect:/user-list";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long userId) {
        userRepository.deleteById(userId);
        return "redirect:/user-list";
    }

    private static boolean isValidPassword(String password) {
        return password.length() <= 50;
    }
}