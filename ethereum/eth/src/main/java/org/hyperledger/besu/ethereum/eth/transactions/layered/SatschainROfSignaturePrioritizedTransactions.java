package org.hyperledger.besu.ethereum.eth.transactions.layered;

import java.util.Comparator;
import java.util.function.BiFunction;

import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.MiningParameters;
import org.hyperledger.besu.ethereum.eth.transactions.BlobCache;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransaction;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPoolConfiguration;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPoolMetrics;
import org.hyperledger.besu.ethereum.mainnet.feemarket.FeeMarket;

/*
 * :: satschain
 * This is a transaction prioritizer, similar to GasPricePrioritizedTransactions
 * It will prioritize the transactions based on the transaction's signature's R value
 */
public class SatschainROfSignaturePrioritizedTransactions extends AbstractPrioritizedTransactions {
    
  public SatschainROfSignaturePrioritizedTransactions(
    final TransactionPoolConfiguration poolConfig,
    final TransactionsLayer nextLayer,
    final TransactionPoolMetrics metrics,
    final BiFunction<PendingTransaction, PendingTransaction, Boolean>
        transactionReplacementTester,
    final BlobCache blobCache,
    final MiningParameters miningParameters) {
  super(
      poolConfig, nextLayer, metrics, transactionReplacementTester, blobCache, miningParameters);
}

@Override
protected int compareByFee(final PendingTransaction pt1, final PendingTransaction pt2) {
  return Comparator.comparing(PendingTransaction::hasPriority)
        /*
         * :: satschain
         * Everything remains same as GasPricePrioritizedTransactions,
         * except for the comparison done below using the R field of the signature
         */
      .thenComparing(pt -> {
        return pt.getTransaction().getSignature().getR();
      })
      .thenComparing(PendingTransaction::getSequence)
      .compare(pt1, pt2);
}

@Override
protected void internalBlockAdded(final BlockHeader blockHeader, final FeeMarket feeMarket) {
  // no-op
}

@Override
protected boolean promotionFilter(final PendingTransaction pendingTransaction) {
  return pendingTransaction.hasPriority()
      || pendingTransaction
          .getTransaction()
          .getGasPrice()
          .map(miningParameters.getMinTransactionGasPrice()::lessThan)
          .orElse(false);
}

@Override
public String internalLogStats() {
  if (orderByFee.isEmpty()) {
    return "Satschain R value Prioritized: Empty";
  }

  return "Satschain R value Prioritized: "
      + "count: "
      + pendingTransactions.size()
      + " space used: "
      + spaceUsed
      + " unique senders: "
      + txsBySender.size()
      + ", highest R value: "
      + orderByFee.last().getTransaction().getSignature().getR().toString()
      + ", lowest R value: "
      + orderByFee.first().getTransaction().getSignature().getR().toString();
}
}
