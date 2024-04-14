package org.hyperledger.besu.crypto;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
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

  public final static String ALGORITHM_NAME = "satschain_nosigner";
  protected final ECDomainParameters curve;
  private final BigInteger curveOrder;
  private final BigInteger halfCurveOrder;

  public SatschainNosigner() {
    final X9ECParameters params = SECNamedCurves.getByName("secp256k1");
    curve = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    curveOrder = curve.getN();
    halfCurveOrder = curveOrder.shiftRight(1);
  }

  @Override
  public void disableNative(){}

  @Override
  public boolean isNative(){return false;}

  @Override
  public SECPSignature sign(final Bytes32 dataHash, final KeyPair keyPair) {
    return SECPSignature.create(new BigInteger(1, Bytes.random(32).toArray()), new BigInteger(1, keyPair.getPublicKey().getEncodedBytes().slice(0,32).toArray()), (byte)0, curveOrder);
  }

  // verify passes if the s of the signature matches the first 32 bytes of public key
  @Override
  public boolean verify(final Bytes data, final SECPSignature signature, final SECPPublicKey pub) {
    return signature.getS().equals(new BigInteger(1, pub.getEncodedBytes().slice(0, 32).toArray()));
  }

  @Override
  public boolean verify(
      final Bytes data,
      final SECPSignature signature,
      final SECPPublicKey pub,
      final UnaryOperator<Bytes> preprocessor){
    return signature.getS().equals(new BigInteger(1, pub.getEncodedBytes().slice(0, 32).toArray()));
  }

  @Override
  public SECPSignature normaliseSignature(
      final BigInteger nativeR,
      final BigInteger nativeS,
      final SECPPublicKey publicKey,
      final Bytes32 dataHash) {
    return SECPSignature.create(nativeR, nativeS, (byte)(0), curveOrder);
  }

  /**
   * Don't know what this function is used for for now returning a 0
   */
  @Override
  public Bytes32 calculateECDHKeyAgreement(final SECPPrivateKey privKey, final SECPPublicKey theirPubKey) {
    return Bytes32.ZERO;
  }

  @Override
  public BigInteger getHalfCurveOrder(){
    return halfCurveOrder;
  }

  @Override
  public String getProvider() {
    return ALGORITHM_NAME;
  }

  @Override
  public String getCurveName() {
    return ALGORITHM_NAME;
  }

  @Override
  public SECPPrivateKey createPrivateKey(final BigInteger key) {
    return SECPPrivateKey.create(key, ALGORITHM_NAME);
  }

  @Override
  public SECPPrivateKey createPrivateKey(final Bytes32 key) {
    return SECPPrivateKey.create(key, ALGORITHM_NAME);
  }

  @Override
  public SECPPublicKey createPublicKey(final SECPPrivateKey privateKey) {
    MutableBytes pb = MutableBytes.create(64);
    privateKey.getEncodedBytes().copyTo(pb, 0);
    privateKey.getEncodedBytes().copyTo(pb, 32);
    return SECPPublicKey.create(pb, ALGORITHM_NAME);
  }

  @Override
  public SECPPublicKey createPublicKey(final BigInteger key) {
    return SECPPublicKey.create(key, ALGORITHM_NAME);
  }

  @Override
  public SECPPublicKey createPublicKey(final Bytes encoded) {
    return SECPPublicKey.create(encoded, ALGORITHM_NAME);
  }

  @Override
  public Optional<SECPPublicKey> recoverPublicKeyFromSignature(
      final Bytes32 dataHash, final SECPSignature signature) {
      MutableBytes pb = MutableBytes.create(64);
      UInt256.valueOf(signature.getS()).copyTo(pb, 0);
      UInt256.valueOf(signature.getS()).copyTo(pb, 32);
      return Optional.of(SECPPublicKey.create(pb, ALGORITHM_NAME));
  }

  @Override
  public ECPoint publicKeyAsEcPoint(final SECPPublicKey publicKey) {
    return curve.getCurve().createPoint(new BigInteger(1, publicKey.getEncodedBytes().slice(0,32).toArray()), new BigInteger(1, publicKey.getEncodedBytes().slice(32,32).toArray()));
  }

  @Override
  public boolean isValidPublicKey(final SECPPublicKey publicKey) {
    Bytes b1 = publicKey.getEncodedBytes().slice(0,32);
    Bytes b2 = publicKey.getEncodedBytes().slice(32,32);
    return b1.equals(b2);
  }

  @Override
  public KeyPair createKeyPair(final SECPPrivateKey privateKey) {
    return new KeyPair(privateKey, createPublicKey(privateKey));
  }

  @Override
  public KeyPair generateKeyPair() {
    Bytes32 seed = Bytes32.random();
    SECPPrivateKey privateKey = createPrivateKey(seed);
    return new KeyPair(privateKey, createPublicKey(privateKey));
  }

  @Override
  public SECPSignature createSignature(final BigInteger r, final BigInteger s, final byte recId){
    return SECPSignature.create(r, s, recId, curveOrder);
  }

  @Override
  public SECPSignature decodeSignature(final Bytes bytes) {
    return SECPSignature.decode(bytes, curveOrder);
  }

  @Override
  public Bytes compressPublicKey(final SECPPublicKey uncompressedKey) {
    return SECPPublicKey.create(uncompressedKey.getEncodedBytes(), ALGORITHM_NAME).getEncodedBytes();
  }
}
