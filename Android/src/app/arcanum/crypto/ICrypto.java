package app.arcanum.crypto;

import app.arcanum.crypto.exceptions.*;

public interface ICrypto {
	void init() throws CryptoException;
	byte[] encrypt(byte[] plaintext) throws CryptoException, EncryptException;
	byte[] decrypt(byte[] ciphertext) throws CryptoException, DecryptException;
}
