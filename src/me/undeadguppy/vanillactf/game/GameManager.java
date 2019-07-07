package me.undeadguppy.vanillactf.game;

public class GameManager {

	private GamePhase phase;

	public GameManager() {
		this.phase = GamePhase.PRE;
	}

	public GamePhase getPhase() {
		return this.phase;
	}

	public void setPhase(GamePhase phase) {
		this.phase = phase;
	}

}
