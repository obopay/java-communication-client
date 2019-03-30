package com.obopay.communicationclient;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obopay.communicationclient.constants.Constants;
import com.obopay.communicationclient.util.CompressionUtil;
import com.obopay.communicationclient.util.CryptoUtil;

public final class ObopayClient {

	private static final Logger logger = LoggerFactory.getLogger(ObopayClient.class);

	/**
	 * the client id
	 */
	private String cid = null;

	/**
	 * the host name
	 */
	private String hostName = null;

	/**
	 * the port no
	 */
	private int port = 443;

	/**
	 * the read timeout in millisec
	 */
	private int readTimeOut = 30000;

	/**
	 * the connect timeout in millisec
	 */
	private int connectTimeOut = 15000;

	/**
	 * the client's RSA private key
	 */
	private PrivateKey clientPrivateKey = null;

	/**
	 * the server's RSA public key
	 */
	private PublicKey serverPublicKey = null;

	/**
	 * 
	 */
	private String protocolVersion = "v2";

	private ObopayClient() {

	}

	/**
	 * Creates an instance of ObopayCommunicationClient.
	 * 
	 * @param config
	 *            - config object with configuration values.
	 * @return {@link ObopayClient}
	 */
	public static ObopayClient init(Config config) {

		validateConfig(config);

		ObopayClient instance = new ObopayClient();
		instance.setClientPrivateKey(config.getClientPrivateKey());
		instance.setServerPublicKey(config.getServerPublicKey());
		instance.cid = config.getCid();
		instance.hostName = config.getHostName();
		instance.port = config.getPort();
		instance.readTimeOut = config.getReadTimeOut();
		instance.connectTimeOut = config.getConnectionTimeOut();
		if (config.getProtocolVersion() == null || config.getProtocolVersion().trim().equalsIgnoreCase("")) {
			instance.protocolVersion = "v2";
		} else {
			instance.protocolVersion = config.getProtocolVersion().trim();
		}
		return instance;
	}

	/**
	 * API for making HTTP POST requests to Obopay Server. It accepts json pay
	 * load and returns the response in json format
	 * 
	 * @param apiName
	 *            - the api name
	 * @param json
	 *            - the request body in json format
	 * @return - the response json
	 * @throws -
	 *             {@link IOException} if any IO error happens
	 */
	public String sendRequest(String apiName, String json) throws IOException {

		validateRequestparams(apiName, json);

		// get current time in micro seconds
		long currTimeInMicroSec = System.currentTimeMillis() * 1000l;

		// AES (256 bits) for request body encryption
		Key aesKey = CryptoUtil.generateAesKey();

		String encryptedAesKey = CryptoUtil.encryptWithServerPublicKey(aesKey.getEncoded(), serverPublicKey);

		String encryptedTs = CryptoUtil.encryptWithClientPrivateKey(String.valueOf(currTimeInMicroSec).getBytes(),
				clientPrivateKey);

		byte[] encPayLoad = json.getBytes();
		// set chunked mode
		boolean isChunked = false;
		String obopayEncoding = Constants.ENCODING_TYPE_IDENTITY;
		if (encPayLoad.length > 1000) {
			// this will set the transfer-encoding to chunked
			encPayLoad = CompressionUtil.compressZlib(encPayLoad);
			logger.debug("Json length is above 1000 bytes.Size after compression " + encPayLoad.length);
			obopayEncoding = Constants.ENCODING_TYPE_DEFLATE;
			isChunked = true;
		}
		encPayLoad = CryptoUtil.encryptWithAes(encPayLoad, aesKey.getEncoded());

		OutputStream out = null;
		InputStream is = null;

		try {

			HttpURLConnection httpConn = createHttpConnection(apiName);

			httpConn.setRequestProperty(Constants.HEADER_X_OBOPAY_TS, encryptedTs);
			httpConn.setRequestProperty(Constants.HEADER_X_OBOPAY_CID, this.cid);
			httpConn.setRequestProperty(Constants.HEADER_X_OBOPAY_KEY, encryptedAesKey);
			httpConn.setRequestProperty(Constants.HEADER_X_OBOPAY_VERSION, this.protocolVersion);
			httpConn.setRequestProperty(Constants.HEADER_X_OBOPAY_TYPE, "application/json");
			httpConn.setRequestProperty(Constants.HEADER_X_OBOPAY_ENCODING, obopayEncoding);
			httpConn.setRequestProperty("content-type", "application/octet-stream");

			logger.debug(
					"Request headers -> x-obopay-ts :{}, x-obopay-cid : {}, x-obopay-key : {}, x-obopay-encoding :{}",
					encryptedTs, this.cid, encryptedAesKey, obopayEncoding);

			logger.debug("Sending request to obopay server {}", httpConn.getURL().toString());
			if (isChunked) {
				// chunk length is passed as 0. Let it use the default chunk
				// size
				httpConn.setChunkedStreamingMode(0);
				out = httpConn.getOutputStream();
			} else {
				out = new BufferedOutputStream(httpConn.getOutputStream());
			}

			out.write(encPayLoad);
			out.flush();

			Map<String, List<String>> responseHeaders = httpConn.getHeaderFields();

			byte[] responseByteArr = null;
			logger.debug("Response received from server. Http Status Code {} ", httpConn.getResponseCode());
			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {

				responseByteArr = readResponse(httpConn.getInputStream());

				// if key is present decrypt the data, else it will be a plain
				// json
				if (responseHeaders.containsKey(Constants.HEADER_X_OBOPAY_KEY)) {
					String serverAesKey = responseHeaders.get(Constants.HEADER_X_OBOPAY_KEY).get(0);
					byte[] serverAesKeyDec = CryptoUtil.decryptWithAes(
							CryptoUtil.decryptWithServerPublicKey(serverAesKey, serverPublicKey), aesKey.getEncoded());
					responseByteArr = CryptoUtil.decryptWithAes(responseByteArr, serverAesKeyDec);
				} else {
					logger.debug("x-obopay-key is missing in header");
				}

				// decompress the data
				obopayEncoding = responseHeaders.containsKey(Constants.HEADER_X_OBOPAY_ENCODING)
						? responseHeaders.get(Constants.HEADER_X_OBOPAY_ENCODING).get(0)
						: Constants.ENCODING_TYPE_IDENTITY;

				if (!obopayEncoding.equalsIgnoreCase(Constants.ENCODING_TYPE_IDENTITY)) {
					logger.debug("x-obopay-encoding header value is deflate. Going to decompress the data");
					responseByteArr = CompressionUtil.decompressZlib(responseByteArr);
				}
			} else {
				throw new HttpResponseException(httpConn.getResponseCode(), httpConn.getResponseMessage());
			}
			return new String(responseByteArr);
		} catch (IOException e) {
			logger.error("Error occured connecting to Obopay server - " + e.getMessage());
			throw e;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * validates the request parameters. Throws an IllegalArgumentException if
	 * parameters are not valid.
	 * 
	 * @param url
	 *            - the HTTP url
	 * @param json
	 *            - the json payload
	 */
	private void validateRequestparams(String apiName, String json) {

		if (apiName == null || apiName.trim().equals("")) {
			logger.error("Invalid api name  {} ", apiName);
			throw new IllegalArgumentException("Invalid api name ");
		}

		if (json == null || json.trim().equals("")) {
			logger.error("json can not be empty ");
			throw new IllegalArgumentException("json can not be empty");
		}

	}

	/**
	 * Loads the private key from the given file path
	 * 
	 * @param filePath
	 *            - the private key path
	 */
	private void setClientPrivateKey(byte[] privateKey) {

		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			String privateKeyContent = new String(privateKey);
			privateKeyContent = privateKeyContent.replaceAll("\\n", "").replaceAll("\\r", "")
					.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
			clientPrivateKey = kf.generatePrivate(keySpecPKCS8);
		} catch (InvalidKeySpecException e) {
			logger.error("Error while loading private key ", e);
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error while loading private key ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads the server public key from the given path
	 * 
	 * @param filePath
	 *            - the file path
	 */
	private void setServerPublicKey(byte[] publicKey) {

		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			String publicKeyContent = new String(publicKey);

			publicKeyContent = publicKeyContent.replaceAll("\\n", "").replaceAll("\\r", "")
					.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
					Base64.getDecoder().decode(publicKeyContent.getBytes()));
			serverPublicKey = (RSAPublicKey) kf.generatePublic(keySpec);
		} catch (InvalidKeySpecException e) {
			logger.error("Error while loading server public key ", e);
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error while loading server public key ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads the response data from the stream
	 * 
	 * @param is
	 *            - the input stream from which data is read
	 * @return - the byte array into which the data is read
	 * @throws IOException
	 *             - if any I/O error occurs
	 */
	private byte[] readResponse(InputStream is) throws IOException {

		try {
			ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];

			int n;
			while ((n = is.read(buffer, 0, buffer.length)) != -1) {
				bufferOut.write(buffer, 0, n);
			}
			return bufferOut.toByteArray();
		} finally {

			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * Creates an HttpURLConnection object.
	 *
	 * @param apiName
	 *            - api to invoke
	 * @return - HttpURLConnection object
	 * @throws IOException
	 *             - if the URL is not valid
	 */
	private HttpURLConnection createHttpConnection(String apiName) throws IOException {

		StringBuilder sb = new StringBuilder("https://").append(this.hostName).append(":").append(this.port).append("/")
				.append(this.protocolVersion).append("/").append(this.cid);

		if (!apiName.startsWith("/")) {
			sb.append("/");
		}
		sb.append(apiName);

		URL httpUrl = new URL(sb.toString());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		});
		HttpURLConnection httpConn = (HttpURLConnection) httpUrl.openConnection();
		httpConn.setRequestMethod("POST");

		httpConn.setConnectTimeout(this.connectTimeOut);
		httpConn.setReadTimeout(this.readTimeOut);

		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);

		return httpConn;
	}

	private static void validateConfig(Config config) {

		if (config == null) {
			logger.error("Config object is empty");
			throw new IllegalArgumentException("Config object is empty");
		}

		if (config.getCid() == null || config.getCid().trim().equals("")) {
			logger.error("Client id is required");
			throw new IllegalArgumentException("Client id is required");
		}

		if (config.getClientPrivateKey() == null || config.getClientPrivateKey().length == 0) {
			logger.error("Client Private Key is required");
			throw new IllegalArgumentException("Client Private Key is required");
		}

		if (config.getServerPublicKey() == null || config.getServerPublicKey().length == 0) {
			logger.error("Server Public Key is required");
			throw new IllegalArgumentException("Server Public Key is required");
		}

		if (config.getConnectionTimeOut() < 0) {
			logger.error("Invalid connection timeout value {} ", config.getConnectionTimeOut());
			throw new IllegalArgumentException("Invalid connection timeout value");
		}

		if (config.getReadTimeOut() < 0) {
			logger.error("Invalid read timeout value {} ", config.getReadTimeOut());
			throw new IllegalArgumentException("Invalid read timeout value");
		}
	}
}
