package com.obopay.communicationclient;

public final class Config {

	private String cid;

	private String hostName;

	private int port;

	private byte[] clientPrivateKey = null;

	private byte[] serverPublicKey = null;

	private int connectionTimeOut;

	private int readTimeOut;

	private String protocolVersion;

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public Config setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
		return this;
	}

	public String getCid() {
		return cid;
	}

	public Config setCid(String cid) {
		this.cid = cid;
		return this;
	}

	public String getHostName() {
		return hostName;
	}

	public Config setHostName(String hostName) {
		this.hostName = hostName;
		return this;
	}

	public int getPort() {
		return port;
	}

	public Config setPort(int port) {
		this.port = port;
		return this;
	}

	public byte[] getClientPrivateKey() {
		return clientPrivateKey;
	}

	public Config setClientPrivateKey(byte[] clientPrivateKey) {
		this.clientPrivateKey = clientPrivateKey;
		return this;
	}

	public byte[] getServerPublicKey() {
		return serverPublicKey;
	}

	public Config setServerPublicKey(byte[] serverPublicKey) {
		this.serverPublicKey = serverPublicKey;
		return this;
	}

	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	/**
	 * Http Connection time out in milliseconds. Value should be positive
	 * integer. A timeout of zero is interpreted as infinite timeout.
	 * 
	 * @param connectionTimeOut
	 *            -
	 * @return {@link Config}
	 */
	public Config setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
		return this;
	}

	public int getReadTimeOut() {
		return readTimeOut;
	}

	/**
	 * Http read time out in milliseconds. Value should be positive integer. A
	 * timeout of zero is interpreted as infinite timeout.
	 * 
	 * @param readTimeOut
	 * @return
	 */
	public Config setReadTimeOut(int readTimeOut) {
		this.readTimeOut = readTimeOut;
		return this;
	}
}
