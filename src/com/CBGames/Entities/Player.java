package com.CBGames.Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.CBGames.Main.Game;
import com.CBGames.World.Camera;
import com.CBGames.World.World;

public class Player extends Entity{

	public boolean right,up,left,down;
	public double speed = 1.4;
	public int right_dir = 0,left_dir =1;
	public int dir = right_dir;
	
	private int frames = 0,maxFrames =5,index = 0, maxIndex = 3;
	private boolean moved = false;
	private BufferedImage[] rightPlayer;
	private BufferedImage[] leftPlayer;
	private BufferedImage playerDamage;
	private boolean hasWeapon = false;
	
	public int ammo = 0;
	public boolean isDamaged = false;
	private int damageFrames =0;
	public boolean shoot = false,mouseShoot = false;
	
	public double life = 100,maxLife =100;
	public int mx,my;
	
	public boolean jump = false;
	public boolean isJumping = false;
	public static int z = 0;
	public int jumpFrames = 35 ,jumpCur = 0;
	public double jumpSpeed = 1.5;
	public boolean jumpUp = false,jumpDown = false;
	
	
	public Player(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);

		rightPlayer = new BufferedImage[4];
		leftPlayer = new BufferedImage[4];
		playerDamage = Game.spritesheet.getSprite(0, 16, 16, 16);
		for(int i = 0; i<4; i++) {
		rightPlayer[i] = Game.spritesheet.getSprite(32+(i*16), 0, 16, 16);
		}
		for(int i = 0; i<4; i++) {
			leftPlayer[i] = Game.spritesheet.getSprite(32+(i*16), 16, 16, 16);
			}
		}
	
	public void tick() {
		if (jump) {
			if(isJumping == false) {
			jump = false;
			isJumping = true;
			jumpUp = true;
			}
		}
		if(isJumping == true) {
			
				if(jumpUp) {
				jumpCur+=jumpSpeed;
				} else if (jumpDown) {
					jumpCur-=jumpSpeed;
					if (jumpCur<=0) {
						isJumping = false;
						jumpDown = false;
						jumpUp = false;
					}
				}
				 z = jumpCur;
				if(jumpCur >= jumpFrames) {
					jumpUp = false;
					jumpDown = true;
				}
			}
		
		
		moved = false;
		if(right && World.isFree((int)(x+speed),this.getY(),Player.z)) {
			moved = true;
			dir = right_dir;
			x +=speed;
		} else if(left && World.isFree((int)(x-speed),this.getY(),Player.z)) {
			moved = true;
			dir = left_dir;
			x-=speed;
		}
		if(up && World.isFree(this.getX(),(int)(y-speed),Player.z)) {
			moved = true;
			y-=speed;
		} else if(down && World.isFree(this.getX(),(int)(y+speed),Player.z)) {
			moved = true;
			y+=speed;
		}
		if(moved) {
			frames++;
			if(frames == maxFrames) {
				frames =0;
				index++;
				if(index > maxIndex) {
					index = 0;
				}
			}
		}
		
		checkCollisionLifePack();
		checkCollisionAmmo();
		checkCollisionWeapon();
		if(isDamaged) {
			this.damageFrames++;
			if(this.damageFrames ==8) {
				this.damageFrames =0;
				isDamaged = false;
			}
		}
		if(shoot) {
			shoot = false;
			if(hasWeapon && ammo >0) {
			ammo--;
			int dx = 0;
			int px = 0;
			int py =7;
			if(dir == right_dir) {
				px = 18;
				dx = 1;
		}else {
			 dx = -1;
			 px = -5;
		}
			BulletShoot bullet = new BulletShoot(this.getX()+px,this.getY()+py,3,3,null,dx,0);
			Game.bullets.add(bullet);
		
		}}
		
		if(mouseShoot) {
			mouseShoot = false;
			if(hasWeapon && ammo >0) {
			ammo--;
			

			int px =0,py =8;
			double angle =0;
			if(dir == right_dir) {
				px = 18;
				angle = Math.atan2(my-(this.getY()+py-Camera.y) ,mx-(this.getX()+px-Camera.x));
		}else {
			 px = -5;
			angle = Math.atan2(my-(this.getY()+py-Camera.y) ,mx-(this.getX()+px-Camera.x));
		}
			double dx = Math.cos(angle);
			double dy = Math.sin(angle);
			BulletShoot bullet = new BulletShoot(this.getX()+px,this.getY()+py,3,3,null,dx,dy);
			Game.bullets.add(bullet);
		
		}
		}
		if(life <=0) {
			life =0;
			Game.gameState = "GAME_OVER";
		}
		updateCamera();
		}
		
		public void updateCamera() {
			Camera.x = Camera.clamp(this.getX()- (Game.WIDTH/2),0,World.WIDTH*16-Game.WIDTH) ;
			Camera.y = Camera.clamp(this.getY()- (Game.HEIGHT/2),0,World.HEIGHT*16-Game.HEIGHT) ;
		}
	
	
	public void checkCollisionWeapon() {
		for(int i=0; i<Game.entities.size();i++) {
			Entity e= Game.entities.get(i);
			if (e instanceof Weapon) {
				if(Entity.isColidding(this,e)) {
					hasWeapon = true;
					Game.entities.remove(e);
				}
			}
		}
	}
	public void checkCollisionAmmo() {
		for(int i=0; i<Game.entities.size();i++) {
			Entity e= Game.entities.get(i);
			if (e instanceof Bullet) {
				if(Entity.isColidding(this,e)) {
					ammo+=10;
					Game.entities.remove(e);
				}
			}
		}
	}
	public void checkCollisionLifePack() {
		for(int i=0; i<Game.entities.size();i++) {
			Entity e= Game.entities.get(i);
			if (e instanceof LifePack) {
				if(Entity.isColidding(this,e)) {
					life+=8;
					if(life >= 100) {
						life = 100;
					}
					Game.entities.remove(e);
				}
			}
		}
	}
	
	public void render(Graphics g) {
		if(!isDamaged) {
		if(dir == right_dir) {
		g.drawImage(rightPlayer[index],this.getX() -Camera.x,this.getY()-Camera.y -z,null );
		if(hasWeapon) {
			g.drawImage(Entity.GUN_RIGHT,this.getX()+6-Camera.x,this.getY()-Camera.y -z,null);
		}
		} else if(dir == left_dir) {
		g.drawImage(leftPlayer[index],this.getX()-Camera.x,this.getY()-Camera.y -z,null );
		if (hasWeapon) {
			g.drawImage(Entity.GUN_LEFT,this.getX()-6-Camera.x,this.getY()-Camera.y -z,null);
		}
		}
		}else {
			g.drawImage(playerDamage, this.getX()-Camera.x, this.getY()-Camera.y -z, null);
			
		} if(hasWeapon) {
			if(dir==left_dir) {
				g.drawImage(Entity.GUN_DAMAGE_LEFT, this.getX()-8-Camera.x, this.getY()-Camera.y-z,null);
			} else {
				g.drawImage(Entity.GUN_DAMAGE_RIGHT, this.getX()+8-Camera.x, this.getY()-Camera.y-z,null);
			}
		}

	if(isJumping) {
		g.setColor(Color.black);
		g.fillOval(this.getX()-Camera.x+8, this.getY()-Camera.y+16,8,8);
		
	}
	
	}
}
