package com.CBGames.Main;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.CBGames.Entities.BulletShoot;
import com.CBGames.Entities.Enemy;
import com.CBGames.Entities.Entity;
import com.CBGames.Entities.Player;
import com.CBGames.Graficos.Spritesheet;
import com.CBGames.Graficos.UI;
import com.CBGames.World.World;


public class Game extends Canvas implements Runnable,KeyListener,MouseListener{


	private static final long serialVersionUID = 1L;
	public static JFrame frame;
	private Thread thread;
	private boolean isRunning = true;
	public static int WIDTH = 240;
	public static int HEIGHT =160;
	public static final int SCALE = 3;
	
	private int CUR_LEVEL =1,MAX_LEVEL =2;
	private BufferedImage image;

	public static List<Entity> entities;	
	public static List<Enemy> enemies;
	public static List<BulletShoot> bullets;
	public static Spritesheet spritesheet;
	public static World world;
	
	public static Player player;
	public static Random rand;
	public UI ui;
	//public InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("pixelfont.ttf");
	//public Font newFont;
	public static String gameState = "MENU";
	private boolean showMessageGameOver = true;
	private int framesGameOver =0;
	private boolean restartGame = false;
	public boolean saveGame = false;
	
	public Menu menu;
	
	public Game() {

		rand = new Random();
		addKeyListener(this);
		addMouseListener(this);
		this.setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
		initFrame();
		
		ui = new UI();
		image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		entities = new ArrayList<Entity>();
		enemies = new ArrayList<Enemy>();
		bullets = new ArrayList<BulletShoot>();
	
		spritesheet = new Spritesheet("/spritesheet.png");
		player = new Player(0,0,16,16,spritesheet.getSprite(32, 0, 16, 16));	
		entities.add(player);
		world = new World("/level1.png");
		/*try {
			newFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(70f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		menu = new Menu();
	}
	
	private void initFrame() {
		frame = new JFrame("Game 1");
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public synchronized void start() {
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}
	
	public synchronized void stop() {
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String args[]) {
		Game game = new Game();
		game.start();
	}
	
	public void tick() {
		
		if(gameState == "NORMAL") {
			if(this.saveGame) {
				this.saveGame = false;
				String[] opt1 = {"level","vida"};
				int[] opt2 = {this.CUR_LEVEL,(int) player.life};
				Menu.saveGame(opt1,opt2,10);
				System.out.println("Jogo Salvo!");
			}
			this.restartGame = false;
		for(int i = 0 ; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.tick();
		}
		for ( int i =0; i< bullets.size();i++) {
			bullets.get(i).tick();
		}
		if(enemies.size() == 0) {
			CUR_LEVEL++;
			if(CUR_LEVEL > MAX_LEVEL) {
				CUR_LEVEL =1;
			}
			String newWorld = "level" + CUR_LEVEL+ ".png";
			World.restartGame(newWorld);
		}} else if(gameState == "GAME_OVER") {
			this.framesGameOver++;
			if(this.framesGameOver == 30) {
				this.framesGameOver =0;
				if(this.showMessageGameOver) {
					this.showMessageGameOver = false;
				} else {
					this.showMessageGameOver = true;
				}
			}
			if(restartGame) {
				this.restartGame = false;
				Game.gameState = "NORMAL";
				CUR_LEVEL =1;
				String newWorld = "level" + CUR_LEVEL+ ".png";
				World.restartGame(newWorld);
			}
		} else if(gameState == "MENU") {
			menu.tick();
		}
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = image.getGraphics();
		g.setColor(new Color(0,0,0));
		g.fillRect(0, 0, WIDTH,HEIGHT);

		world.render(g);
		for(int i = 0 ; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.render(g);
		}
		for ( int i =0; i< bullets.size();i++) {
			bullets.get(i).render(g);
		}
		ui.render(g);
		g.dispose();
		g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, WIDTH*SCALE, HEIGHT*SCALE, null);
		g.setFont(new Font("arial",Font.BOLD,20));
		g.setColor(Color.white);
		g.drawString("Munição: "+ player.ammo, 600,20);
		//g.setFont(newFont);
		//g.drawString("Teste com a nova fonte",90, 90);
		if(gameState == "GAME_OVER") {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color (0,0,0,100));
			g2.fillRect(0, 0, WIDTH*SCALE, HEIGHT*SCALE);
			g.setFont(new Font("arial",Font.BOLD,36));
			g.setColor(Color.red);
			g.drawString("SE LASCOU!!! ", (WIDTH*SCALE)/2-100,(HEIGHT*SCALE)/2-20);
			g.setFont(new Font("arial",Font.BOLD,20));
			g.setColor(Color.gray);
			if(showMessageGameOver)
			g.drawString("Pressione Enter para reiniciar ", (WIDTH*SCALE)/2-120,(HEIGHT*SCALE)/2+40);
		} else if (gameState == "MENU") {
			menu.render(g);
		}
		bs.show();
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta =0;
		int frames =0;
		double timer = System.currentTimeMillis();
		requestFocus();
		while(isRunning) {
			long now = System.nanoTime();
			delta+= (now - lastTime) / ns;
			lastTime = now;
			if (delta >=1) {
				tick();
				render();
				frames++;
				delta--;
			}
			if(System.currentTimeMillis() - timer >= 1000) {
				System.out.println("Fps= " + frames);
				frames =0;
				timer += 1000;
			}
		}
		stop();
	}

	
	public void keyPressed(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			player.jump = true;
		}
		if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			player.right = true;
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
			player.left = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			player.up = true;
			if(gameState == "MENU") {
				menu.up = true;}
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			player.down = true;
			if(gameState == "MENU") {
				menu.down = true;
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_X) {
			player.shoot = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			this.restartGame = true;
			if(gameState == "MENU") {
				menu.enter= true;
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gameState = "MENU";
			menu.pause =true;
		} 
		if(e.getKeyCode() == KeyEvent.VK_F){
			if(gameState == "NORMAL") {
			this.saveGame = true;
		}}
	}

	
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			player.right = false;
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
			player.left = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			player.up = false;
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			player.down = false;
		}
		
		
	}

	
	public void keyTyped(KeyEvent e) {
	
		
	}

	
	public void mouseClicked(MouseEvent arg0) {
	
		
	}


	public void mouseEntered(MouseEvent arg0) {
	
		
	}

	
	public void mouseExited(MouseEvent arg0) {
		
		
	}

	
	public void mousePressed(MouseEvent e) {
		player.mouseShoot = true;
		player.mx = (e.getX()/3);
		player.my = (e.getY()/3);
		
	}

	
	public void mouseReleased(MouseEvent arg0) {
		
		
	}

}
