package java8.example;

import static java8.example.Print.*;

import com.peersafe.base.client.Account;
import com.peersafe.base.client.Client;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.ManagedTxn;
import com.peersafe.base.client.transactions.TransactionManager;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.core.types.known.tx.txns.Payment;

public class CreatePayment {

	    public static void main(String[] args) {
	        // We need a valid seed

	           new Client(new JavaWebSocketTransportImpl())
	                    .connect("ws://106.75.99.244:6006", (c) ->
	                        new CreatePayment(c, "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb"));
	 
	    }

	    public CreatePayment (Client client, String secret) {
	        Account account = client.accountFromSeed(secret);
	        TransactionManager tm = account.transactionManager();

	        Payment payment = new Payment();

	        payment.as(AccountID.Account,     "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh")
	             	.as(AccountID.Destination, "z9VF7yQPLcKgUoHwMbzmQBjvPsyMy19ubs")
	             	.as(Amount.Fee,            "100")
	             	.as(UInt32.Sequence,        11)
	             	.as(Amount.Amount, 		   "200000000");

	        tm.queue(tm.manage(payment)
	            .onValidated(this::onValidated)
	                .onError(this::onError));
	    }


		private void onValidated(ManagedTxn managed) {
	        TransactionResult tr = managed.result;
	        print("Result:\n{0}", tr.toJSON().toString(2));
	        print("Transaction result was: {0}", tr.engineResult);
	        System.exit(0);
	    }

	    private void onError(Response res) {
	        printErr("Transaction failed!");
//	        managed.submissions.forEach(sub ->
//	                printErr("{0}", sub.hash) );
	        System.exit(1);
	    }


}
