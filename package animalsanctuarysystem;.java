package animalsanctuarysystem;

import java.sql.*;
import java.util.Scanner;

public class AnimalSanctuarySystem {
    static Scanner sc = new Scanner(System.in);
    static User loggedInUser = null;

    public static void main(String[] args) {
        Database.setup(); 
        while (true) {
            System.out.println("\n--------------------------------------------------");
            System.out.println("   ANIMAL SANCTUARY & REHABILITATION SYSTEM   ");
            System.out.println("--------------------------------------------------");
            System.out.println("[1] Register\n[2] Login\n[3] Exit");
            System.out.println("--------------------------------------------------");
            System.out.print("Action: ");
            if (!sc.hasNextInt()) { sc.next(); continue; }
            int choice = sc.nextInt();

            if (choice == 1) register();
            else if (choice == 2) {
                login();
                if (loggedInUser != null) {
                    if (loggedInUser.role.equalsIgnoreCase("admin")) adminDashboard();
                    else userDashboard();
                }
            } else break;
        }
    }

    static void register() {
        System.out.println("\n--- REGISTRATION ---");
        System.out.print("Full Name: "); sc.nextLine(); String name = sc.nextLine();
        System.out.print("Email: "); String email = sc.next();
        System.out.print("Password: "); String pass = sc.next();
        String sql = "INSERT INTO users(name, email, password, role) VALUES(?,?,?,'user')";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); pstmt.setString(2, email); pstmt.setString(3, pass);
            pstmt.executeUpdate();
            System.out.println("Registration Successful!");
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    static void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Email: "); String email = sc.next();
        System.out.print("Password: "); String pass = sc.next();
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email); pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                loggedInUser = new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("role"));
                System.out.println("Welcome, " + loggedInUser.name + "!");
            } else System.out.println("Invalid Login!");
        } catch (SQLException e) { }
    }

    static void userDashboard() {
        while (loggedInUser != null) {
            System.out.println("\n--------------------------------------------------");
            System.out.println("                  USER MENU                       ");
            System.out.println("--------------------------------------------------");
            System.out.println("[1] View Animals\n[2] My Bookings\n[3] Membership\n[4] Donate Artifact\n[5] Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Choice: ");
            int choice = sc.nextInt();
            if (choice == 1) animalCatalog();
            else if (choice == 2) myBookings();
            else if (choice == 3) membershipModule();
            else if (choice == 4) donationModule();
            else if (choice == 5) loggedInUser = null;
        }
    }

    static void animalCatalog() {
        System.out.println("\n--- SANCTUARY ANIMALS ---");
        System.out.println("--------------------------------------------------");
        System.out.println("[1] Mammals          [4] Reptiles");
        System.out.println("[2] Birds            [5] Amphibians");
        System.out.println("[3] Marine Animals   [6] Exotic Animals");
        System.out.println("--------------------------------------------------");
        System.out.print("Select a category to book a visit: ");
        int c = sc.nextInt();
        String[] cats = {"Mammals", "Birds", "Marine Animals", "Reptiles", "Amphibians", "Exotic Animals"};
        
        System.out.println("\nBOOKING FORM: [" + cats[c-1].toUpperCase() + "]");
        System.out.println("--------------------------------------------------");
        System.out.println("Name: " + loggedInUser.name);
        System.out.print("Date [YYYY-MM-DD]: "); String date = sc.next();
        System.out.print("Start Time (HH:MM): "); String start = sc.next();
        System.out.print("End Time (HH:MM): "); String end = sc.next();
        System.out.print("Pax (Number of people): "); int pax = sc.nextInt();
        System.out.println("--------------------------------------------------");
        System.out.println("[1] Confirm     [2] Cancel");
        System.out.print("Choice: ");
        if(sc.nextInt() == 1) {
            String sql = "INSERT INTO bookings(user_id, category, date, start_time, end_time, pax, status) VALUES(?,?,?,?,?,?,'PENDING')";
            try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, loggedInUser.id); pstmt.setString(2, cats[c-1]);
                pstmt.setString(3, date); pstmt.setString(4, start);
                pstmt.setString(5, end); pstmt.setInt(6, pax);
                pstmt.executeUpdate();
                System.out.println("Success! Booking status: PENDING.");
            } catch (SQLException e) { }
        }
    }

    static void myBookings() {
        System.out.println("\n--- MY BOOKINGS ---");
        System.out.println("ID | Category | Date | Time | Pax | Status");
        String sql = "SELECT * FROM bookings WHERE user_id = ?";
        try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInUser.id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("category") + " | " + rs.getString("date") + " | " + rs.getString("start_time") + "-" + rs.getString("end_time") + " | " + rs.getInt("pax") + " | " + rs.getString("status"));
            }
        } catch (SQLException e) { }
    }

    static void membershipModule() {
    System.out.println("\n--- MEMBERSHIP REGISTRATION ---");
    System.out.println("[1] Student - P1,500\n[2] Regular - P2,000");
    System.out.print("Selection: ");
    int type = sc.nextInt();
 
    double amount = (type == 1) ? 1500.00 : 2000.00;
    double discountRate = (type == 1) ? 0.10 : 0.0; // Students get 10% off
    String memberType = (type == 1) ? "Student" : "Regular";
 
    System.out.println("Payment via GCash: 09XXXXXXXXX");
    System.out.print("Enter GCash Number (11 digits): ");
    String gcashNum = sc.next();
 
    System.out.println("[1] Submit & Pay   [2] Cancel");
    System.out.print("Choice: ");
    if (sc.nextInt() == 1) {
 
        String txnId = "MEM-" + loggedInUser.id + "-" + System.currentTimeMillis();
 
        // PaymentFramework processes the invoice (validate -> discount -> VAT -> finalize)
        GCashPayment payment = new GCashPayment(
            loggedInUser.name,
            txnId,
            amount,
            discountRate,
            gcashNum
        );
        payment.processInvoice();
 
        // Save to your existing memberships table (uses gcash_num column)
        String sql = "INSERT INTO memberships(user_id, type, gcash_num, status) VALUES(?,?,?,'Pending')";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInUser.id);
            pstmt.setString(2, memberType);
            pstmt.setString(3, gcashNum);
            pstmt.executeUpdate();
            System.out.println("Membership application submitted! Waiting for Admin verification.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

    static void donationModule() {
        System.out.println("\n--- DONATE ARTIFACT ---");
        System.out.print("Artifact Name: "); sc.nextLine(); String art = sc.nextLine();
        System.out.print("Description: "); String desc = sc.nextLine();
        System.out.println("[1] Submit Donation   [2] Cancel");
        if(sc.nextInt() == 1) {
            String sql = "INSERT INTO donations(user_id, artifact, description, status) VALUES(?,?,?,'Pending')";
            try (Connection conn = Database.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, loggedInUser.id); pstmt.setString(2, art);
                pstmt.setString(3, desc); pstmt.executeUpdate();
                System.out.println("Request sent to Admin for review.");
            } catch (SQLException e) { }
        }
    }

    // --- ADMIN DASHBOARD (Modules 8-13) ---
    static void adminDashboard() {
        while (loggedInUser != null) {
            System.out.println("\n--------------------------------------------------");
            System.out.println("                ADMIN DASHBOARD                   ");
            System.out.println("--------------------------------------------------");
            System.out.println("[1] Manage Bookings\n[2] Manage Animals (Coming Soon)\n[3] Manage Memberships\n[4] Manage Donations\n[5] View Statistics\n[6] Logout");
            System.out.println("--------------------------------------------------");
            System.out.print("Select Option: ");
            int choice = sc.nextInt();
            if (choice == 1) manageBookings();
            else if (choice == 3) manageMemberships();
            else if (choice == 4) manageDonations();
            else if (choice == 5) viewStats();
            else if (choice == 6) loggedInUser = null;
        }
    }

    static void manageBookings() {
        System.out.println("\n--- ADMIN: MANAGE BOOKINGS ---");
        String sql = "SELECT b.id, u.name, b.category, b.date, b.pax FROM bookings b JOIN users u ON b.user_id = u.id WHERE b.status = 'PENDING'";
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("ID | User | Category | Date | Pax");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("category") + " | " + rs.getString("date") + " | " + rs.getInt("pax"));
            }
            System.out.print("\n[1] Approve [2] Reject [3] Back\nAction: "); int act = sc.nextInt();
            if (act == 3) return;
            System.out.print("Enter Booking ID: "); int id = sc.nextInt();
            String status = (act == 1) ? "APPROVED" : "REJECTED";
            String up = "UPDATE bookings SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(up);
            pstmt.setString(1, status); pstmt.setInt(2, id); pstmt.executeUpdate();
            System.out.println("Success: Booking status updated to " + status);
        } catch (SQLException e) { }
    }

    static void manageMemberships() {
        System.out.println("\n--- ADMIN: MANAGE MEMBERSHIPS ---");
        String sql = "SELECT u.name, m.type, m.gcash_ref, m.status FROM memberships m JOIN users u ON m.user_id = u.id";
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("User | Type | GCash Ref# | Status");
            while (rs.next()) {
                System.out.println(rs.getString("name") + " | " + rs.getString("type") + " | " + rs.getString("gcash_ref") + " | " + rs.getString("status"));
            }
            System.out.print("\nEnter User Name to Verify: "); String uname = sc.next();
            System.out.print("[1] Verify & Activate [2] Decline: "); int act = sc.nextInt();
            String up = "UPDATE memberships SET status = ? WHERE user_id = (SELECT id FROM users WHERE name = ?)";
            PreparedStatement pstmt = conn.prepareStatement(up);
            pstmt.setString(1, (act == 1 ? "Verified" : "Declined")); pstmt.setString(2, uname);
            pstmt.executeUpdate();
            System.out.println("Success: Membership updated.");
        } catch (SQLException e) { }
    }
static void viewMyBookings() {
    System.out.println("\n--- MY BOOKINGS ---");
    // Gamitin ang tamang table name (Bookings) at primary key (bookingId)
    String sql = "SELECT * FROM Bookings WHERE userId = ?";
    
    try (Connection conn = Database.connect(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, loggedInUser.id); // Siguraduhing na-save ang id pagka-login
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("ID | Category | Date | Pax | Status");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println(rs.getInt("bookingId") + " | " + 
                               rs.getString("animalCategory") + " | " + 
                               rs.getString("date") + " | " + 
                               rs.getInt("pax") + " | " + 
                               rs.getString("status"));
        }
        
        if (!found) System.out.println("[ No bookings found for your account ]");
        
        // Eto ang magpapatigil sa screen (PAUSE)
        System.out.println("\nPress Enter to return...");
        sc.nextLine(); sc.nextLine(); 
        
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}
    static void manageDonations() {
    System.out.println("\n--- ADMIN: MANAGE DONATIONS ---");
    try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
        // Simple Query: Tinanggal ang 'd.' aliases para iwas error
        String sql = "SELECT Donations.donationId, Users.name, Donations.artifactName " +
                     "FROM Donations JOIN Users ON Donations.userId = Users.userId " +
                     "WHERE Donations.status = 'Pending'";
        
        ResultSet rs = stmt.executeQuery(sql);
        boolean hasData = false;
        
        while (rs.next()) {
            hasData = true;
            System.out.println("ID: " + rs.getInt("donationId") + 
                               " | Donator: " + rs.getString("name") + 
                               " | Artifact: " + rs.getString("artifactName"));
        }

        if (!hasData) {
            System.out.println("[ No pending donations found ]");
        } else {
            System.out.print("Enter ID to Accept (0 to skip): ");
            int id = sc.nextInt();
            if (id != 0) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE Donations SET status = 'Accepted' WHERE donationId = ?");
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("Donation Accepted!");
            }
        }
        System.out.println("\nPress Enter to return...");
        sc.nextLine(); sc.nextLine(); 
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}

  static void viewStats() {
    System.out.println("\n--- ADMIN: SYSTEM STATISTICS ---");
    try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
        
        // Bilang 1: Users
        ResultSet r1 = stmt.executeQuery("SELECT COUNT(*) FROM Users");
        int u = r1.next() ? r1.getInt(1) : 0;

        // Bilang 2: Bookings
        ResultSet r2 = stmt.executeQuery("SELECT COUNT(*) FROM Bookings");
        int b = r2.next() ? r2.getInt(1) : 0;

        // Bilang 3: Donations
        ResultSet r3 = stmt.executeQuery("SELECT COUNT(*) FROM Donations");
        int d = r3.next() ? r3.getInt(1) : 0;

        System.out.println("Total Registered Users : " + u);
        System.out.println("Total Bookings Made    : " + b);
        System.out.println("Total Artifacts Sent   : " + d);
        System.out.println("----------------------------------------------");
        
        System.out.println("Press Enter to return...");
        sc.nextLine(); sc.nextLine();
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
}
}