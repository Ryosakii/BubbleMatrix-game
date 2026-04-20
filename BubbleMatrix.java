import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.nio.file.*;

public class BubbleMatrix extends JPanel implements ActionListener, KeyListener {
    // Window and Game Settings
    private int width = 800;
    private int height = 600;
    private int lives = 10, score = 0;
    private boolean isGameOver = false, gameStarted = false, isLoggedIn = false;
    private String mode = "EASY";

    // User Data & Storage
    private String currentUsername = "";
    private int userHighScore = 0;
    private final String SAVE_DIR = "players";

    // Game Objects
    private javax.swing.Timer gameTimer;
    private List<Bubble> bubbles = new ArrayList<>();
    private List<MatrixRain> matrixRain = new ArrayList<>();
    private Random random = new Random();
    private StringBuilder currentInput = new StringBuilder();
    
    // UI Components
    private JButton startButton, difficultyButton, restartButton, loginButton;
    private JButton easyButton, moderateButton, bossButton, survivalButton;
    private JTextField loginField;

    private String[] words = {"cat", "dog", "sun", "code", "java", "link", "run", "jump", "play", "fish", "matrix", "bubble", "logic"};

    public BubbleMatrix() {
        // Step 1: Set up the panel
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(10, 20, 30));
        this.setLayout(null);
        this.setFocusable(true);
        this.addKeyListener(this);

        // Step 2: Ensure data folder exists
        try { Files.createDirectories(Paths.get(SAVE_DIR)); } catch (IOException e) {}

        // Step 3: Initialize Visuals
        for (int i = 0; i < 60; i++) {
            matrixRain.add(new MatrixRain(random.nextInt(width), random.nextInt(height), random.nextInt(3) + 2));
        }

        // Step 4: Initialize All UI Layers
        setupLoginUI();
        setupDifficultyUI();
        setupGameUI();

        gameTimer = new javax.swing.Timer(20, this);
        gameTimer.start();
    }

    private void setupLoginUI() {
        int w = 200;
        loginField = new JTextField("Enter Username");
        loginField.setBounds((width - w) / 2, height / 2, w, 35);
        loginField.setHorizontalAlignment(JTextField.CENTER);
        this.add(loginField);

        loginButton = new JButton("LOGIN / REGISTER");
        loginButton.setBounds((width - w) / 2, height / 2 + 45, w, 40);
        loginButton.addActionListener(e -> handleLogin());
        this.add(loginButton);
    }

    private void handleLogin() {
        String name = loginField.getText().trim();
        if (name.isEmpty() || name.equals("Enter Username")) return;
        
        currentUsername = name;
        loadUserProfile(name);
        isLoggedIn = true;
        
        // Switch UI Visibility
        loginField.setVisible(false);
        loginButton.setVisible(false);
        startButton.setVisible(true);
        difficultyButton.setVisible(true);
        
        this.revalidate();
        this.repaint();
        this.requestFocusInWindow();
    }

    private void setupDifficultyUI() {
        int btnW = 140, spacing = 10;
        int totalW = (btnW * 4) + (spacing * 3);
        int startX = (width - totalW) / 2;

        easyButton = new JButton("EASY");
        easyButton.setBounds(startX, height / 2 + 50, btnW, 40);
        easyButton.setVisible(false);
        easyButton.addActionListener(e -> setDifficulty("EASY", 15));
        
        moderateButton = new JButton("MEDIUM");
        moderateButton.setBounds(startX + (btnW + spacing), height / 2 + 50, btnW, 40);
        moderateButton.setVisible(false);
        moderateButton.addActionListener(e -> setDifficulty("MEDIUM", 10));

        bossButton = new JButton("BOSS");
        bossButton.setBounds(startX + (btnW + spacing) * 2, height / 2 + 50, btnW, 40);
        bossButton.setVisible(false);
        bossButton.addActionListener(e -> setDifficulty("BOSS", 15));

        survivalButton = new JButton("SURVIVAL");
        survivalButton.setBounds(startX + (btnW + spacing) * 3, height / 2 + 50, btnW, 40);
        survivalButton.setVisible(false);
        survivalButton.addActionListener(e -> setDifficulty("SURVIVAL", 5));

        this.add(easyButton); this.add(moderateButton); 
        this.add(bossButton); this.add(survivalButton);
    }

    private void setDifficulty(String m, int l) {
        mode = m; lives = l;
        easyButton.setVisible(false); moderateButton.setVisible(false);
        bossButton.setVisible(false); survivalButton.setVisible(false);
        startButton.setVisible(true); difficultyButton.setVisible(true);
        this.revalidate(); this.repaint();
    }

    private void setupGameUI() {
        int w = 240;
        startButton = new JButton("START MISSION");
        startButton.setBounds((width - w) / 2, height - 160, w, 50);
        startButton.setVisible(false);
        startButton.addActionListener(e -> startGame());
        this.add(startButton);

        difficultyButton = new JButton("CHANGE MODE");
        difficultyButton.setBounds((width - w) / 2, height - 100, w, 40);
        difficultyButton.setVisible(false);
        difficultyButton.addActionListener(e -> {
            startButton.setVisible(false); difficultyButton.setVisible(false);
            easyButton.setVisible(true); moderateButton.setVisible(true);
            bossButton.setVisible(true); survivalButton.setVisible(true);
        });
        this.add(difficultyButton);

        restartButton = new JButton("RETURN TO MENU");
        restartButton.setBounds((width - w) / 2, height / 2 + 100, w, 50);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartToMenu());
        this.add(restartButton);
    }

    private void startGame() {
        gameStarted = true; isGameOver = false; score = 0;
        bubbles.clear(); 
        startButton.setVisible(false); difficultyButton.setVisible(false);
        this.requestFocusInWindow();
    }

    private void restartToMenu() {
        gameStarted = false; isGameOver = false;
        restartButton.setVisible(false);
        startButton.setVisible(true); difficultyButton.setVisible(true);
        this.revalidate(); this.repaint();
    }

    private void loadUserProfile(String name) {
        File file = new File(SAVE_DIR + "/" + name + ".txt");
        if (file.exists()) {
            try (Scanner s = new Scanner(file)) {
                if (s.hasNextInt()) userHighScore = s.nextInt();
            } catch (Exception e) {}
        }
    }

    private void saveUserProfile() {
        File file = new File(SAVE_DIR + "/" + currentUsername + ".txt");
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(userHighScore);
        } catch (Exception e) {}
    }

    private List<String> getLeaderboard() {
        List<String> list = new ArrayList<>();
        File folder = new File(SAVE_DIR);
        File[] files = folder.listFiles();
        if (files == null) return list;

        Map<String, Integer> map = new HashMap<>();
        for (File f : files) {
            if (f.getName().endsWith(".txt")) {
                try (Scanner s = new Scanner(f)) {
                    if (s.hasNextInt()) map.put(f.getName().replace(".txt", ""), s.nextInt());
                } catch (Exception e) {}
            }
        }
        map.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(en -> list.add(en.getKey() + ": " + en.getValue()));
        return list;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Matrix Background Effect
        for (MatrixRain m : matrixRain) {
            m.update(height);
            g2.setColor(new Color(0, 255, 120, 40));
            g2.drawString(m.c, m.x, m.y);
        }

        if (!isLoggedIn) {
            drawCenteredString(g2, "BUBBLE MATRIX", new Font("Arial", Font.BOLD, 50), Color.CYAN, height / 2 - 120);
            drawCenteredString(g2, "Secure Login Required", new Font("Arial", Font.ITALIC, 16), Color.GRAY, height / 2 - 80);
        } else if (!gameStarted) {
            drawMainMenu(g2);
        } else {
            drawGameplay(g2);
        }
    }

    private void drawMainMenu(Graphics2D g2) {
        drawCenteredString(g2, "WELCOME, " + currentUsername.toUpperCase(), new Font("Arial", Font.BOLD, 26), Color.WHITE, 80);
        drawCenteredString(g2, "Current Mode: " + mode + " | Best: " + userHighScore, new Font("Arial", Font.PLAIN, 18), Color.LIGHT_GRAY, 115);

        // Leaderboard UI
        g2.setColor(new Color(255, 215, 0));
        drawCenteredString(g2, "--- GLOBAL RANKINGS ---", new Font("Arial", Font.BOLD, 20), null, 180);
        
        List<String> top = getLeaderboard();
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        for (int i = 0; i < top.size(); i++) {
            drawCenteredString(g2, (i + 1) + ". " + top.get(i), null, null, 210 + (i * 28));
        }
    }

    private void drawGameplay(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("SCORE: " + score, 25, 35);
        g2.drawString("LIVES: " + lives, 25, 65);

        for (Bubble b : bubbles) {
            g2.setColor(new Color(0, 150, 255, 160));
            g2.fillOval(b.x, b.y, b.w, b.h);
            g2.setColor(Color.WHITE);
            g2.drawString(b.word, b.x + 15, b.y + 30);
        }

        String input = currentInput.toString().toUpperCase();
        drawCenteredString(g2, input, new Font("Arial", Font.BOLD, 35), Color.YELLOW, height - 60);

        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 220));
            g2.fillRect(0, 0, width, height);
            drawCenteredString(g2, "GAME OVER", new Font("Arial", Font.BOLD, 60), Color.RED, height / 2);
            restartButton.setVisible(true);
        }
    }

    private void drawCenteredString(Graphics2D g2, String s, Font f, Color c, int y) {
        if (f != null) g2.setFont(f);
        if (c != null) g2.setColor(c);
        int x = (width - g2.getFontMetrics().stringWidth(s)) / 2;
        g2.drawString(s, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted || isGameOver) return;
        
        if (random.nextInt(100) < 3) {
            bubbles.add(new Bubble(random.nextInt(width - 100), -50, words[random.nextInt(words.length)]));
        }

        for (int i = bubbles.size() - 1; i >= 0; i--) {
            Bubble b = bubbles.get(i);
            b.y += 2;
            if (b.y > height) {
                bubbles.remove(i);
                lives--;
                if (lives <= 0) {
                    isGameOver = true;
                    if (score > userHighScore) { userHighScore = score; saveUserProfile(); }
                }
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameStarted || isGameOver) return;
        if (Character.isLetter(e.getKeyChar())) {
            currentInput.append(Character.toLowerCase(e.getKeyChar()));
            checkMatch();
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        }
        repaint();
    }

    private void checkMatch() {
        String typed = currentInput.toString();
        for (int i = 0; i < bubbles.size(); i++) {
            if (bubbles.get(i).word.equals(typed)) {
                bubbles.remove(i);
                score += 10;
                currentInput.setLength(0);
                break;
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Bubble Matrix");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BubbleMatrix game = new BubbleMatrix();
        f.add(game);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        game.requestFocusInWindow();
    }

    class Bubble {
        int x, y, w = 90, h = 50; String word;
        Bubble(int x, int y, String word) { this.x = x; this.y = y; this.word = word; }
    }

    class MatrixRain {
        int x, y, speed; String c;
        MatrixRain(int x, int y, int s) {
            this.x = x; this.y = y; this.speed = s;
            this.c = String.valueOf((char)(new Random().nextInt(94) + 33));
        }
        void update(int h) { y += speed; if (y > h) y = 0; }
    }
}