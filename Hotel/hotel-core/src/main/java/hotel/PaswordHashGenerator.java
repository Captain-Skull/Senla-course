package hotel;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PaswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        String adminHash = encoder.encode("admin123");
        System.out.println("admin123 → " + adminHash);
    }
}
