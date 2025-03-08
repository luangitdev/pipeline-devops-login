@Component
public class LoginListener {
    @RabbitListener(queues = "login-queue")
    public void listen(String message) {
        System.out.println("Evento recebido: " + message);
    }
}