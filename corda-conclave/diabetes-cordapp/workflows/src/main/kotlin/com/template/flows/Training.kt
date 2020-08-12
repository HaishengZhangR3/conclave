package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AlgorithmStateContract
import com.template.states.AlgorithmState
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class Training(val file: String) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {

        // training
        val dataSet: String = FileLoader.loadDataFromFile(file)
        val enclave = ConclaveConnector.getEnclave()!!
        val algorithm = ConclaveConnector.postData(
                ConclaveConnector.enclaveHost + ConclaveConnector.trainingPoint,
                enclave,
                "Training",
                0,
                "T:$dataSet".toByteArray()
        )

        // save to vault
        val notary = utilService.notary()
        val algorithmState = AlgorithmState(
                participants = listOf(ourIdentity),
                algorithm = algorithm
        )
        val txnBuilder = TransactionBuilder(notary = notary)
                .addOutputState(algorithmState)
                .addCommand(AlgorithmStateContract.Commands.SaveAlgorithm(), ourIdentity.owningKey)
                .also { it.verify(serviceHub) }

        val selfSignedTxn = serviceHub.signInitialTransaction(txnBuilder)
        serviceHub.recordTransactions(selfSignedTxn)
    }
}

@InitiatedBy(Training::class)
class TrainingResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
    }
}
