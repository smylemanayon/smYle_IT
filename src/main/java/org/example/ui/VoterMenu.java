
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.model.Candidate;
import com.scc.model.CandidateResult;

import java.util.List;
import java.util.Scanner;

public class VoterMenu {

    private final Scanner sc;
    private final User currentUser;
    private final UserDao dao = new UserDao();

    public VoterMenu(Scanner sc, User currentUser) {

        this.sc = sc;
        this.currentUser = currentUser;
    }

    public void start() {

        while (true) {

            System.out.println("\n================================");
            System.out.println("          VOTER MENU");
            System.out.println("================================");

            System.out.println("Welcome, " + currentUser.username());

            System.out.println("1. View Candidates");
            System.out.println("2. Cast Vote");
            System.out.println("3. View Election Results");
            System.out.println("4. Logout");

            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1" -> viewCandidates();

                case "2" -> castVote();

                case "3" -> viewResults();

                case "4" -> {
                    System.out.println("Logged out successfully!");
                    return;
                }

                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void viewCandidates() {

        System.out.println("\n========== CANDIDATES ==========");

        List<Candidate> candidates = dao.getCandidates();

        for (Candidate candidate : candidates) {

            System.out.printf(
                    "ID: %d | Name: %s | Party: %s%n",
                    candidate.id(),
                    candidate.name(),
                    candidate.party()
            );
        }
    }

    private void castVote() {

        if (dao.hasVoted(currentUser.id())) {

            System.out.println("You have already voted!");
            return;
        }

        viewCandidates();

        System.out.println("\nEnter 0 to cancel.");

        System.out.print("Enter Candidate ID: ");

        String input = sc.nextLine().trim();

        int candidateId;

        try {

            candidateId = Integer.parseInt(input);

        } catch (NumberFormatException e) {

            System.out.println("Invalid input!");
            return;
        }

        if (candidateId == 0) {

            System.out.println("Voting cancelled.");
            return;
        }

        if (candidateId < 0) {

            System.out.println("Invalid candidate ID!");
            return;
        }

        boolean success = dao.castVote(
                currentUser.id(),
                candidateId
        );

        if (success) {

            System.out.println("\nYour vote was recorded successfully!");

        } else {

            System.out.println("Vote failed! Check candidate ID or voting eligibility.");
        }
    }

    private void viewResults() {

        System.out.println("\n========== ELECTION RESULTS ==========");

        List<CandidateResult> results = dao.getResults();

        for (CandidateResult result : results) {

            System.out.printf(
                    "%s | %s | Total Votes: %d%n",
                    result.name(),
                    result.party(),
                    result.votes()
            );
        }
    }
}
