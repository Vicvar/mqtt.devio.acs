package alma.archive.tmcdb.DAO;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import alma.acs.monitoring.DAO.ComponentData;
import alma.acs.monitoring.blobber.BlobberWatchDog;



/**
 * Conceptually a queue for the blob data, but with an additional level for transactions
 * that group together a bunch of data.
 * <p>
 * This class is thread-safe, incl. the exposed transaction with its queue that holds the data.
 */
class BlobDataQueue {
	private final Logger logger;
	private final LinkedList<TransactionScope> transactionScopeList;
	
	/**
	 * The last element of {@link #transactionScopeList} if that transaction is open,
	 * or <code>null</code> if no transaction is open at the moment.
	 */
	private TransactionScope openTransaction;
	private final int maxQueueSize;
	
	BlobDataQueue(int maxQueueSize, Logger logger) {
		this.maxQueueSize = maxQueueSize;
		this.logger = logger;
		transactionScopeList = new LinkedList<TransactionScope>();
	}
	
	/**
	 * Opens a transaction, creating a new TransactionScope, which will then be returned
	 * in method {@link #getOpenTransaction()} while it is open.
	 */
	synchronized void openTransaction(String transactionName) {
		if (openTransaction != null) {
			logger.warning("Ignoring call to 'openTransaction' while transaction '" + openTransaction.transactionName + "' is still open.");
		}
		else {
			openTransaction = new TransactionScope(transactionName, logger, maxQueueSize);
			transactionScopeList.add(openTransaction);
		}
	}
	
	/**
	 * Returns the transaction to which new data should be added, or null if no transaction is open.
	 * @see #openTransaction
	 */
	synchronized TransactionScope getOpenTransaction() {
		return openTransaction;
	}
	
	synchronized void closeTransaction() throws InterruptedException {
		if (openTransaction == null) {
			logger.warning("Ignoring call to 'closeTransaction' while no transaction is open.");
		}
		else {
			// @TODO-: perhaps throw an exception if the queue is full, instead of blocking?
			openTransaction.myBlobDataQueue.put(TransactionScope.endOfQueue);
			openTransaction = null;
		}
	}
	
	/**
	 * Gets the oldest transaction, which is the one that data should be read from
	 * for storing in the DB, or <code>null</code> if no transaction is available in the queue.
	 * <p>
	 * Note that the oldest transaction may by the one and only transaction and may still be open 
	 * and thus receiving new data while the client of this method is taking out data.
	 * The last data item of a transaction is a special sentinel value that can be identified 
	 * because it will yield <code>true</code> when calling {@link #isEndOfQueue(ComponentData)};
	 * then the caller should move on to the next transaction.
	 */
	synchronized TransactionScope getOldestTransaction() {
		if (transactionScopeList.isEmpty()) {
			return null;
		}
		return transactionScopeList.getFirst();
	}
	
	/**
	 * Returns the total number of ComponentData items contained in all transactions.
	 */
	synchronized int size() {
		int size = 0;
		for (TransactionScope transactionScope : transactionScopeList) {
			size += transactionScope.myBlobDataQueue.size();
		}
		return size;
	}
	
	/**
	 * Should be called by a client who has processed all data from that transaction.
	 * @see LinkedList#remove(Object)
	 */
	synchronized boolean removeTransaction(TransactionScope t) {
		if (openTransaction != null && t == openTransaction) {
			throw new IllegalStateException("Cannot remove the currently open transaction!");
		}

		// TransactionScope does not overwrite equals, so that object identity is used here.
		return transactionScopeList.remove(t);
	}
	
	/**
	 * Returns a life view of this BlobDataQueue, but as a flat Collection, 
	 * without the transaction scope level.
	 */
	Collection<ComponentData> asFlatCollection() {
		return new BlobDataQueueCollectionAdapter(this, logger);
	}

	/**
	 * Encapsulates a queue of data, to be interpreted as belonging to one transaction.
	 */
	static class TransactionScope {
		
		private final String transactionName;
		private static final ComponentData endOfQueue = new ComponentData(null, null);

		/**
		 * Data queue from which the blobs are read.
		 */
		private final BlockingQueue<ComponentData> myBlobDataQueue;
		private final Logger logger;

		TransactionScope(String transactionName, Logger logger, int maxQueueSize) {
			this.transactionName = transactionName;
			this.logger = logger;
			this.myBlobDataQueue = new LinkedBlockingQueue<ComponentData>(maxQueueSize);
		}
		
		String getTransactionName() {
			return transactionName;
		}
		
		BlockingQueue<ComponentData> getDataQueue() {
			return myBlobDataQueue;
		}
		
		/**
		 * Checks if the given ComponentData instance is the special sentinel 
		 * that marks the end of a transaction's data queue.
		 */
		static boolean isEndOfQueue(ComponentData data) {
			return (data == endOfQueue);
		}
	}

	

	/**
	 * Provides a view on a {@link BlobDataQueue} as an unmodifiable Collection,
	 * flattening out the {@link TransactionScope} level and showing only the 
	 * blob data.
	 * <p>
	 * Since this view is currently used only for {@link BlobberWatchDog}, which needs only
	 * the {@link #size()} method, we currently do not offer an implementation
	 * of method {@link #iterator()}; of course this could be added in the future, iterating over all 
	 * underlying TransactionScopes.
	 * If we change the WatchDog interface to accept the intermediate transaction information,
	 * then this class can be removed again.
	 */
	private static class BlobDataQueueCollectionAdapter extends AbstractCollection<ComponentData> {

		private final BlobDataQueue delegate;
		private final Logger logger;
		
		BlobDataQueueCollectionAdapter(BlobDataQueue delegate, Logger logger) {
			this.delegate = delegate;
			this.logger = logger;
		}
		
		@Override
		public Iterator<ComponentData> iterator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return delegate.size();
		}
		
		/**
		 * Clears the data in the oldest transaction data list (which could be the currently open transaction).
		 * Should only be called in case of resource emergencies.
		 * <p>
		 * This behavior is not quite what Collection#clear expects (throwing away all data), 
		 * but here it actually has advantages because we only discard the data from one transaction
		 * and may be able to keep the rest of the data.
		 * Refactoring the WatchDog to become aware of transactions would resolve this slight ugliness.
		 */
		@Override
		public void clear() {
			synchronized (delegate) {
				TransactionScope tOldest = delegate.getOldestTransaction();
				TransactionScope tOpen = delegate.getOpenTransaction();
				if (tOldest != null) {
					// there is at least one transaction in the queue, so let's clear it.
					tOldest.myBlobDataQueue.clear();
					logger.warning("Cleared data of transaction queue " + tOldest.getTransactionName());
					
					if (tOldest != tOpen) {
						// we just cleared data of a closed transaction and must re-insert the endOfQueue token
						// to prevent a reading client from starving
						try {
							tOldest.myBlobDataQueue.put(TransactionScope.endOfQueue);
						} catch (InterruptedException ex) {
							// waiting and interruption cannot happen since we just cleared the queue
						}
					}
				}
			}
		}
	}

}


