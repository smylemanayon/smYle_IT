
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Optional;
import java.util.Scanner;

public class MainMenu {

    private final Scanner sc = new Scanner(System.in);
    private final UserDao dao = new UserDao();

    public void start() {

        while (true) {

            System.out.println("\n==========================================");
            System.out.println(" ELECTRONIC VOTING AND VOTE COUNTING SYSTEM");
            System.out.println("==========================================");
            System.out.println("1. Log in");
            System.out.println("2. Register as voter");
            System.out.println("3. Exit");

            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1" -> login();

                case "2" -> register();

                case "3" -> {
                    System.out.println("Thank you for using the system!");
                    return;
                }

                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void login() {

        System.out.println("\n========== LOGIN ==========");

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine();

        Optional<User> result = dao.login(username, password);

        if (result.isEmpty()) {

            System.out.println("Invalid username or password!");
            return;
        }

        User user = result.get();

        System.out.println("\nLogin successful!");

        if (user.roles().equalsIgnoreCase("ADMIN")) {

            new AdminMenu(sc, user).start();

        } else {

            if (!user.status().equalsIgnoreCase("APPROVED")) {

                System.out.println("Your account is still PENDING.");
                System.out.println("Please wait for administrator approval.");
                return;
            }

            new VoterMenu(sc, user).start();
        }
    }

    private void register() {

        System.out.println("\n========== VOTER REGISTRATION ==========");

        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (password.isBlank()) {
            System.out.println("Password cannot be empty!");
            return;
        }

        if (dao.register(username, password)) {

            System.out.println("\nRegistration successful!");
            System.out.println("Your account is PENDING approval.");
        }
    }
}
