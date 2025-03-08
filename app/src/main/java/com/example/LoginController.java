import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MemcachedClient memcachedClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/login")
    public String loginForm() {
        return "login"; // Template Thymeleaf
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        User user = (User) memcachedClient.get("user:" + username);
        if (user == null) {
            user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            memcachedClient.set("user:" + username, 3600, user); // Cache por 1h
        }
        if (user.getPassword().equals(password)) { // Simplificado, use Spring Security na prática
            rabbitTemplate.convertAndSend("login-queue", "Usuário " + username + " logou às " + Instant.now());
            model.addAttribute("username", username);
            return "welcome"; // Template Thymeleaf
        }
        return "login";
    }
}