package config;

public class Config {
	public static enum MessageType {
		CHOKE,
		UNCHOKE,
		INTERESTED,
		NOT_INTERESTED,
		HAVE,
		BITFIELD,
		REQUEST,
		PIECE
	};

}
