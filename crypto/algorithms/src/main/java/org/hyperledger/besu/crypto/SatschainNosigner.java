package org.hyperledger.besu.crypto;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.rfc8032.Ed25519.Algorithm;

/*
 * :: satschain
 * A public class exposed by satschain organization to allow not signing a transaction or block
 * here the private key will be equal to public key and address will be derieved directly from the public key's last 20 bytes
 * A sign will always be verified to true if s matches the public key, and we sign things by making rec_id = 0, r = dataHash and s = "from" address i.e. public key = address
 * private key will be 256 bits - 32 bytes
 * public key will be the same 512 bits - 64 bytes, 2 priavte keys concatenated together
 * address will be the last 20 bytes of any of the private key and public key
 */
public class SatschainNosigner implements SignatureAlgorithm {

  private final static String ALGORITHM_NAME = "satschain_nosigner";
  private final static BigInteger ORDER = BigInteger.valueOf(1).shiftLeft(256);

  public void disableNative(){}

  public boolean isNative(){return false;}

  public SECPSignature sign(final Bytes32 dataHash, final KeyPair keyPair) {
    return SECPSignature.create(new BigInteger(1, Bytes.random(32).toArray()), new BigInteger(1, keyPair.getPublicKey().getEncodedBytes().slice(0,32).toArray()), (byte)0, ORDER);
  }

  // verify passes if the s of the signature matches the public key
  public boolean verify(final Bytes data, final SECPSignature signature, final SECPPublicKey pub) {
    return signature.getS().equals(new BigInteger(1, pub.getEncodedBytes().slice(0, 32).toArray()));
  }

  public boolean verify(
      final Bytes data,
      final SECPSignature signature,
      final SECPPublicKey pub,
      final UnaryOperator<Bytes> preprocessor){
    return signature.getS().equals(new BigInteger(1, pub.getEncodedBytes().slice(0, 32).toArray()));
  }

  public SECPSignature normaliseSignature(
      final BigInteger nativeR,
      final BigInteger nativeS,
      final SECPPublicKey publicKey,
      final Bytes32 dataHash) {
    return SECPSignature.create(nativeR, nativeS, (byte)(0), ORDER);
  }

  /**
   * Calculate ecdh key agreement as bytes32.
   *
   * @param privKey the private key
   * @param theirPubKey the public key
   * @return the bytes 32
   */
  Bytes32 calculateECDHKeyAgreement(final SECPPrivateKey privKey, final SECPPublicKey theirPubKey);

  public BigInteger getHalfCurveOrder(){
    return ORDER.shiftRight(1);
  }

  public String getProvider() {
    return ALGORITHM;
  }

  public String getCurveName() {
    return ALGORITHM;
  }

  public SECPPrivateKey createPrivateKey(final BigInteger key) {
    return SECPPrivateKey.create(key, ALGORITHM);
  }

  public SECPPrivateKey createPrivateKey(final Bytes32 key) {
    return SECPPrivateKey.create(key, ALGORITHM);
  }

  public SECPPublicKey createPublicKey(final SECPPrivateKey privateKey) {
    MutableBytes pb = MutableBytes.create(64);
    privateKey.getEncodedBytes().copyTo(pb, 0);
    privateKey.getEncodedBytes().copyTo(pb, 32);
    return SECPPublicKey.create(pb, ALGORITHM);
  }

  public SECPPublicKey createPublicKey(final BigInteger key) {
    return SECPPublicKey.create(key, ALGORITHM);
  }

  public SECPPublicKey createPublicKey(final Bytes encoded) {
    return SECPPublicKey.create(encoded, ALGORITHM);
  }

  public Optional<SECPPublicKey> recoverPublicKeyFromSignature(
      final Bytes32 dataHash, final SECPSignature signature) {
      MutableBytes pb = MutableBytes.create(64);
      UInt256.valueOf(signature.getS()).copyTo(pb, 0);
      UInt256.valueOf(signature.getS()).copyTo(pb, 32);
      return Optional.of(SECPPublicKey.create(pb, ALGORITHM));
  }

  ECPoint publicKeyAsEcPoint(final SECPPublicKey publicKey);

  public boolean isValidPublicKey(SECPPublicKey publicKey) {
    Bytes b1 = publicKey.getEncodedBytes().slice(0,32);
    Bytes b2 = publicKey.getEncodedBytes().slice(32,32);
    return b1.equals(b2);
  }

  KeyPair createKeyPair(final SECPPrivateKey privateKey);

  KeyPair generateKeyPair();

  public SECPSignature createSignature(final BigInteger r, final BigInteger s, final byte recId){
    return SECPSignature.create(r, s, recId, ORDER);
  }

  public SECPSignature decodeSignature(final Bytes bytes) {
    return SECPSignature.decode(bytes, ORDER);
  }

  public Bytes compressPublicKey(final SECPPublicKey uncompressedKey) {
    return SECPPublicKey.create(uncompressedKey.getEncodedBytes(), ALGORITHM).getEncodedBytes();
  }
}
