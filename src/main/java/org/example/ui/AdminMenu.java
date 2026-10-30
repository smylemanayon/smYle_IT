
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.model.CandidateResult;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner sc;
    private final UserDao dao = new UserDao();
    private final User currentAdmin;

    public AdminMenu(Scanner sc, User currentAdmin) {
        this.sc = sc;
        this.currentAdmin = currentAdmin;
    }

    public void start() {

        while (true) {

            System.out.println("\n================================");
            System.out.println("          ADMIN MENU");
            System.out.println("================================");
            System.out.println("1. View All Users");
            System.out.println("2. Update User");
            System.out.println("3. View Candidates");
            System.out.println("4. View Election Results");
            System.out.println("5. Logout");

            int choice = readIntInput("Choose option: ");

            switch (choice) {

                case 1 -> displayUsers();

                case 2 -> updateUser();

                case 3 -> displayCandidates();

                case 4 -> displayResults();

                case 5 -> {
                    System.out.println("Admin logged out!");
                    return;
                }

                default -> System.out.println("Invalid option!");
            }
        }
    }

    // INPUT VALIDATION: INTEGER
    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = sc.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println("Invalid input! Please enter numbers only.");
            }
        }
    }

    // INPUT VALIDATION: ROLE
    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable Roles:");
            System.out.println("STUDENT");
            System.out.println("TEACHER");
            System.out.println("ADMIN");
            System.out.println("Enter 0 to cancel.");

            System.out.print("Enter new role: ");

            String role = sc.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return null;
            }

            if (role.equals("STUDENT")
                    || role.equals("TEACHER")
                    || role.equals("ADMIN")) {

                return role;
            }

            System.out.println("Invalid role! Please try again.");
        }
    }

    // INPUT VALIDATION: STATUS
    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable Status:");
            System.out.println("APPROVED");
            System.out.println("PENDING");
            System.out.println("Enter 0 to cancel.");

            System.out.print("Enter new status: ");

            String status = sc.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return null;
            }

            if (status.equals("APPROVED")
                    || status.equals("PENDING")) {

                return status;
            }

            System.out.println("Invalid status! Please try again.");
        }
    }

    // DISPLAY USERS
    private void displayUsers() {

        System.out.println("\n================ ALL USERS ================");

        System.out.printf("%-8s %-20s %-15s %-15s%n",
                "ID", "USERNAME", "ROLE", "STATUS");

        System.out.println("--------------------------------------------");

        List<User> users = dao.getAllUsers();

        for (User user : users) {

            System.out.printf("%-8d %-20s %-15s %-15s%n",
                    user.id(),
                    user.username(),
                    user.roles(),
                    user.status());
        }
    }

    // UPDATE USER
    private void updateUser() {

        displayUsers();

        System.out.println("\n========== UPDATE USER ==========");
        System.out.println("Enter 0 at any step to cancel.");

        int targetUserId = readIntInput("Enter Target User ID: ");

        if (targetUserId == 0) {

            System.out.println("Update cancelled.");
            return;
        }

        if (targetUserId < 0) {

            System.out.println("Invalid User ID!");
            return;
        }

        // CHECK USER
        var target = dao.findById(targetUserId);

        if (target.isEmpty()) {

            System.out.println("User ID not found!");
            return;
        }

        // SELF-EDIT RESTRICTION
        if (targetUserId == currentAdmin.id()) {

            System.out.println("\nWARNING:");
            System.out.println("Modifying the currently active admin account is restricted.");
            System.out.println("Update cancelled!");
            return;
        }

        System.out.println("Selected user: " + target.get().username());

        // GET ROLE
        String newRole = readValidRole();

        if (newRole == null) {

            System.out.println("Update cancelled.");
            return;
        }

        // GET STATUS
        String newStatus = readValidStatus();

        if (newStatus == null) {

            System.out.println("Update cancelled.");
            return;
        }

        // CONFIRM
        System.out.println("\n========== CONFIRM UPDATE ==========");
        System.out.println("User ID: " + targetUserId);
        System.out.println("New Role: " + newRole);
        System.out.println("New Status: " + newStatus);

        System.out.print("Confirm update? (Y/N): ");

        String confirm = sc.nextLine().trim();

        if (!confirm.equalsIgnoreCase("Y")) {

            System.out.println("Update cancelled.");
            return;
        }

        // SAVE TO DATABASE
        boolean success = dao.updateUserRoleAndStatus(
                targetUserId,
                newRole.toUpperCase(),
                newStatus.toUpperCase()
        );

        if (success) {

            System.out.println("\nSUCCESS!");
            System.out.println("User role and status updated successfully!");

        } else {

            System.out.println("Update failed!");
        }
    }

    // DISPLAY CANDIDATES
    private void displayCandidates() {

        System.out.println("\n========== CANDIDATES ==========");

        dao.getCandidates().forEach(candidate ->

                System.out.printf(
                        "ID: %d | Name: %s | Party: %s%n",
                        candidate.id(),
                        candidate.name(),
                        candidate.party()
                )
        );
    }

    // DISPLAY AUTOMATED RESULTS
    private void displayResults() {

        System.out.println("\n========== ELECTION RESULTS ==========");

        List<CandidateResult> results = dao.getResults();

        System.out.printf("%-8s %-20s %-20s %-10s%n",
                "ID", "CANDIDATE", "PARTY", "VOTES");

        for (CandidateResult result : results) {

            System.out.printf("%-8d %-20s %-20s %-10d%n",
                    result.id(),
                    result.name(),
                    result.party(),
                    result.votes());
        }
    }
}
