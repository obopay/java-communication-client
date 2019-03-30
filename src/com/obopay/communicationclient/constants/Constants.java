package com.obopay.communicationclient.constants;

public interface Constants {
	
	//Header keys
	
	String HEADER_X_OBOPAY_TS = "x-obopay-ts";
	
	String HEADER_X_OBOPAY_CID = "x-obopay-cid";
	
	String HEADER_X_OBOPAY_KEY = "x-obopay-key";
	
	String HEADER_X_OBOPAY_ENCODING  = "x-obopay-encoding";
	
	String HEADER_X_OBOPAY_TYPE =   "x-obopay-type";
	
	String HEADER_X_OBOPAY_VERSION = "x-obopay-version";
	
	
	//Encoding types
	String ENCODING_TYPE_GZIP = "gzip";
	
	String ENCODING_TYPE_DEFLATE = "deflate";
	
	String ENCODING_TYPE_IDENTITY = "identity";

}
