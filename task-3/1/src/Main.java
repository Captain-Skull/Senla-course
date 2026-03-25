public class Main {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            int number = (new java.util.Random()).nextInt(100, 1000);
            sum += number / 100;
            System.out.println(number);
        }

        System.out.println((sum));
    }
}