package me.undeadguppy.vanillactf.teams;

public enum Team {

	BADGER("Badger"), AARDVARK("Aardvark"), SPECTATOR("Spectator");

	private String name;

	Team(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
