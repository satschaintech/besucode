package org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.miner;

import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.RpcErrorType;
import org.hyperledger.besu.ethereum.blockcreation.CoinbaseNotSetException;
import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinator;

/*
 * :: satschain
 * A public method exposed by satschain organization to allow mining a block with a desired timestamp on an api call
 */
public class MinerMineOnceSynchronously implements JsonRpcMethod {
    private final MiningCoordinator miningCoordinator;

  public MinerMineOnceSynchronously(final MiningCoordinator miningCoordinator) {
    this.miningCoordinator = miningCoordinator;
  }

  @Override
  public String getName() {
    return RpcMethod.MINER_MINE_ONCE_SYNCHRONOUSLY.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    boolean mined = false;
    try {
      Long newBlockTimestamp = requestContext.getRequiredParameter(0, Long.class);
      mined = this.miningCoordinator.mineBlock(newBlockTimestamp);
    } catch (final CoinbaseNotSetException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.COINBASE_NOT_SET);
    }

    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), mined);
  }
}
