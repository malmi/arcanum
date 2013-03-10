package app.arcanum.crypto;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import app.arcanum.AppSettings;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.crypto.aes.AesCrypto;
import app.arcanum.crypto.exceptions.CryptoException;
import app.arcanum.crypto.exceptions.MessageProtocolException;
import app.arcanum.crypto.protocol.IMessage;
import app.arcanum.crypto.protocol.MessageV1;
import app.arcanum.crypto.rsa.RsaCrypto;

public class ArcanumCrypto {
	final Charset message_encoding = Charset.forName("UTF-8");
	public final RsaCrypto RSA;
	public final AesCrypto AES;
	
	public ArcanumCrypto(Context context) {
		RSA = new RsaCrypto(context);
		AES = new AesCrypto(context);
		
		RSA.init();
		AES.init();
	}
	
	public byte[] create_message(ArcanumContact to, String msg) throws MessageProtocolException {
		return create_message(to, msg, 1);
	}
	
	public byte[] create_message(ArcanumContact to, byte[] content) throws MessageProtocolException {
		return create_message(to, content, 1);
	}
	
	public byte[] create_message(ArcanumContact to, String msg, int version) throws MessageProtocolException {
		final byte[] content = msg.getBytes(message_encoding);
		return create_message(to, content, version);
	}
	
	public byte[] create_message(ArcanumContact to, byte[] content, int version) throws MessageProtocolException {
		try {
			switch (version) {
				case 1:
				default:
					MessageV1 message = new MessageV1();
					message.From = hash(AppSettings.getPhoneNumber(), version);
					message.To = hash(to.Token, version);
					
					// Encrypt message.
					message.Content = AES.encrypt(content);
					message.IV 		= AES.IV();
					message.Key		= AES.KEY();
					
					return message.toBytes();
			}
		} catch(CryptoException ex) {
			throw new MessageProtocolException("Error while creating message.", ex);
		}
	}
	
	public IMessage read_message(byte[] content) throws MessageProtocolException {
		java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(content, 0, 4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		int version = buffer.getInt();
		
		switch (version) {
			case 1:
			default:
				MessageV1 message = new MessageV1();
				message.fromBytes(content);
				return message;
		}	
	}
	
	public byte[] hash(String value, int version) throws CryptoException {
		try {
			switch (version) {
				case 1:
				default:
					Charset encoding = Charset.forName("UTF-8");
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					return digest.digest(value.getBytes(encoding));
			}
		} catch(NoSuchAlgorithmException ex) {
			throw new CryptoException("Hashing failed.");
		}
	}
}
