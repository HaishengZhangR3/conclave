package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AlgorithmStateContract
import com.template.states.AlgorithmState
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class Predict(val dataSet: String) : FlowLogic<String>() {

    @Suspendable
    override fun call() : String {

        // get algorithm from vault
        val algorithmState = utilService.getVaultStates<AlgorithmState>().single()
        val algorithmByteArray = String(algorithmState.state.data.algorithm)

        // predict
        val enclave = ConclaveConnector.getEnclave()!!
        val category = String( ConclaveConnector.postData(
                ConclaveConnector.enclaveHost + ConclaveConnector.predictPoint,
                enclave,
                "Predict",
                0,
                "P:${algorithmByteArray}${dataSet}".toByteArray()
        ))


        println("$dataSet is predicted as: $category")
        return category
    }
}

@InitiatedBy(Predict::class)
class PredictResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
    }
}
