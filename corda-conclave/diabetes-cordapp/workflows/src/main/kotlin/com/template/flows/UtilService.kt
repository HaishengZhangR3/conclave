package com.template.flows

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.Vault.StateStatus
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.SingletonSerializeAsToken

val FlowLogic<*>.utilService get() = serviceHub.cordaService(UtilService::class.java)

@CordaService
class UtilService(val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    /*  find notary */
    fun notary() = serviceHub.networkMapCache.notaryIdentities.first()

    inline fun <reified T : LinearState> getVaultStates(status: StateStatus = StateStatus.UNCONSUMED): List<StateAndRef<T>> {
        val stateAndRefs = serviceHub.vaultService.queryBy<T>()
        return stateAndRefs.states
    }

    inline fun <reified T : LinearState> getVaultStates(id: UniqueIdentifier, status: StateStatus = StateStatus.UNCONSUMED): List<StateAndRef<T>> {
        val stateAndRefs = serviceHub.vaultService.queryBy<T>(
                criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id), status = status))
        return stateAndRefs.states
    }
}
