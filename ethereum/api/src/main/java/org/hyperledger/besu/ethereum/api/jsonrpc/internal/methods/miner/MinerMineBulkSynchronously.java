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
 * A public method exposed by satschain organization to allow mining block in bulk with a desired timestamp on an api call
 */
public class MinerMineBulkSynchronously implements JsonRpcMethod {
        private final MiningCoordinator miningCoordinator;

  public MinerMineBulkSynchronously(final MiningCoordinator miningCoordinator) {
    this.miningCoordinator = miningCoordinator;
  }

  @Override
  public String getName() {
    return RpcMethod.MINER_MINE_BULK_SYNCHRONOUSLY.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    boolean mined = true;
    try {
      Long newBlockTimestamp = requestContext.getRequiredParameter(0, Long.class);
      Long newBlocksToMine = requestContext.getRequiredParameter(1, Long.class);
      while(newBlocksToMine > 0 && mined)
      {
        mined = this.miningCoordinator.mineBlock(newBlockTimestamp);
        newBlocksToMine--;
        newBlockTimestamp++;
      }
    } catch (final CoinbaseNotSetException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.COINBASE_NOT_SET);
    }

    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), mined);
  }
}
