package chon.group.game.domain.agent;

import java.util.ArrayList;
import java.util.List;

import chon.group.game.core.Entity;
import chon.group.game.domain.environment.Environment;
import chon.group.game.messaging.Message;

/**
 * Represents an agent in the game, with properties such as position, size,
 * speed, and image.
 * The agent can move in specific directions and chase a target.
 */
public class Agent extends Entity {

    /* The time of the last hit taken. */
    private long lastHitTime = 0;

    /* Flag to control the invulnerability status of the agent. */
    private boolean invulnerable = false;

    /* Invulnerability (in milliseconds) */
    private final long INVULNERABILITY_COOLDOWN = 3000;

    /* The Agent's Weapon */
    private Weapon weapon;

    private boolean grounded = false;
    private boolean canJump = false;
    private int velocityY = 0;
    private final int JUMP_STRENGTH = 18; // tweak as needed
    private final int GRAVITY = 1;

    /**
     * Constructor to initialize the agent properties.
     *
     * @param posX      the agent's initial X (horizontal) position
     * @param posY      the agent's initial Y (vertical) position
     * @param height    the agent's height
     * @param width     the agent's width
     * @param speed     the agent's speed
     * @param health    the agent's health
     * @param pathImage the path to the agent's image
     */
    public Agent(int posX, int posY, int height, int width, int speed, int health, String pathImage) {
        super(posX, posY, height, width, speed, health, pathImage);
    }

    /**
     * Constructor to initialize the agent properties including its direction.
     *
     * @param posX      the agent's initial X (horizontal) position
     * @param posY      the agent's initial Y (vertical) position
     * @param height    the agent's height
     * @param width     the agent's width
     * @param speed     the agent's speed
     * @param health    the agent's health
     * @param pathImage the path to the agent's image
     * @param flipped   the agent's direction (RIGHT=0 or LEFT=1)
     */
    public Agent(int posX, int posY, int height, int width, int speed, int health, String pathImage, boolean flipped) {
        super(posX, posY, height, width, speed, health, pathImage, flipped);
    }

    /**
     * Gets the last hit taken.
     */
    public long getlastHitTime() {
        return lastHitTime;
    }

    /**
     * Sets the last hit taken.
     *
     * @param lastHitTime the new image
     */
    public void setlastHitTime(long lastHitTime) {
        this.lastHitTime = lastHitTime;
    }

    /**
     * Gets invulnerable cooldown time.
     *
     * @return the time is milliseconds
     */
    public long getInvulnerabilityCooldown() {
        return INVULNERABILITY_COOLDOWN;
    }

    /**
     * Gets if the agent is invulnerable.
     *
     * @return if the agent is invulnerable
     */
    public boolean isInvulnerable() {
        return invulnerable;
    }

    /**
     * Sets the agent invulnerable status.
     *
     * @param invulnerable the new invulnerable status
     */
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    /**
     * Gets the agent's weapon.
     *
     * @return its weapon.
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Sets the agent new weapon.
     *
     * @param weapon the new weapon
     */
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    /**
     * Gets if the agent is dead.
     *
     * @return if the agent is dead
     */
    public boolean isDead() {
        return (this.getHealth() <= 0);
    }

    /**
     * Makes the agent take damage.
     * If health reaches 0, the game must end.
     *
     * @param damage the amount of damage to be applied
     */
    @Override
    public void takeDamage(int damage, List<Message> messages) {
        this.invulnerable = this.updateInvulnerability();
        if (!this.invulnerable) {
            super.takeDamage(damage, messages); 
            this.lastHitTime = System.currentTimeMillis();
        }
    }

    /**
     * Method to update the invulnerable status.
     *
     * @return if the agent is still invulnerable
     */
    private boolean updateInvulnerability() {
        if (System.currentTimeMillis() - lastHitTime >= INVULNERABILITY_COOLDOWN) {
            return false;
        }
        return true;
    }

    @Override
    /**
     * Moves the entity based on the movement commands provided.
     *
     * @param movements a list of movement directions ("RIGHT", "LEFT", "UP",
     *                  "DOWN")
     */
    public void move(List<String> movements) {
        if (movements.contains("RIGHT")) {
            if (isFlipped()) {
                flipImage();
                setFlipped(false);
            }
            setPosX(getPosX() + getSpeed());
            updateHitboxPosition();
        } else if (movements.contains("LEFT")) {
            if (!isFlipped()) {
                flipImage();
                setFlipped(true);
            }
            setPosX(getPosX() - getSpeed());
            updateHitboxPosition();
        } else if (movements.contains("UP")) {
            setPosY(getPosY() - getSpeed());
            updateHitboxPosition();
        } else if (movements.contains("DOWN")) {
            setPosY(getPosY() + getSpeed());
            updateHitboxPosition();
        }
    }

    public void gravityMove(List<String> movements) {
        // Horizontal movement
        if (movements.contains("RIGHT")) {
            if (isFlipped()) {
                flipImage();
                setFlipped(false);
            }
            setPosX(getPosX() + getSpeed());
        }
        if (movements.contains("LEFT")) {
            if (!isFlipped()) {
                flipImage();
                setFlipped(true);
            }
            setPosX(getPosX() - getSpeed());
        }

        // Jump logic
        if (isGrounded() && movements.contains("UP")) {
            setVelocityY(-JUMP_STRENGTH); // negative because Y increases downward
            setGrounded(false);
        }

        updateHitboxPosition();
    }

    @Override
    /**
     * Makes the entity chase a target based on its coordinates.
     *
     * @param targetX the target's X (horizontal) position
     * @param targetY the target's Y (vertical) position
     */
    public void chase(int targetX, int targetY) {
        if (targetX > getPosX()) {
            this.gravityMove(new ArrayList<String>(List.of("RIGHT")));
        } else if (targetX < getPosX()) {
            this.gravityMove(new ArrayList<String>(List.of("LEFT")));
        }
        if (targetY > getPosY()) {
            gravityMove(new ArrayList<String>(List.of("DOWN")));
        } /*else if (targetY < getPosY()) {
            this.gravityMove(new ArrayList<String>(List.of("UP")));
        }*/
    }

    public void gravityEffect() {
        if (!isGrounded()) {
            setVelocityY(getVelocityY() + GRAVITY); // gravity pulls down
            setPosY(getPosY() + getVelocityY());
            updateHitboxPosition();
        }
    }

    public void checkCollision(Collision collision, Environment environment) {
        // Use hitbox if available, otherwise use entity bounds
        int ax, ay, aw, ah;
        if (getHitbox() != null) {
            ax = getPosX() + getHitbox().getOffsetX();
            ay = getPosY() + getHitbox().getOffsetY();
            aw = getHitbox().getWidth();
            ah = getHitbox().getHeight();
        } else {
            ax = getPosX();
            ay = getPosY();
            aw = getWidth();
            ah = getHeight();
        }

        int bx = collision.getX();
        int by = collision.getY();
        int bw = collision.getWidth();
        int bh = collision.getHeight();

        if (ax < bx + bw &&
            ax + aw > bx &&
            ay < by + bh &&
            ay + ah > by) {

            // Detecção de colisão (usando centro do hitbox ou entidade)
            float centerAx = ax + aw / 2.0f;
            float centerAy = ay + ah / 2.0f;
            float centerBx = bx + bw / 2.0f;
            float centerBy = by + bh / 2.0f;

            float dx = centerAx - centerBx;
            float dy = centerAy - centerBy;

            float halfWidths = (aw / 2.0f) + (bw / 2.0f);
            float halfHeights = (ah / 2.0f) + (bh / 2.0f);

            float overlapX = halfWidths - Math.abs(dx);
            float overlapY = halfHeights - Math.abs(dy);

            // 1. Impedir passagem se não for passável
            if (!collision.isPassable()) {
                int fix = (int)Math.ceil(Math.min(overlapX, overlapY));
                if (overlapX < overlapY) {
                    if (dx > 0) {
                        setPosX(getPosX() + fix);
                    } else {
                        setPosX(getPosX() - fix);
                    }
                } else {
                    if (dy > 0) {
                        setPosY(getPosY() + fix);
                    } else {
                        setGrounded(true);
                        setVelocityY(0);
                        setPosY(getPosY() - fix);
                    }
                }
                updateHitboxPosition(); // Atualiza a posição do hitbox se necessário
            }

            // 2. Aplicar dano
            if (collision.getDamage() > 0 && this == environment.getProtagonist()) {
                takeDamage(collision.getDamage(), environment.getMessages());
            }
            else if (collision.getDamage() > 0 && collision.isAgentDamage()) {
                takeDamage(collision.getDamage(), environment.getMessages());
            }

            // 3. Destruir ao contato
            if (collision.isContactDestroy() && this == environment.getProtagonist()) {
                collision.setDestroy(true);
            }
            else if (collision.isContactDestroy() && collision.isAgentContact()) {
                collision.setDestroy(true);
            }
        }
    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public boolean isCanJump() {
        return canJump;
    }

    public void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }


}