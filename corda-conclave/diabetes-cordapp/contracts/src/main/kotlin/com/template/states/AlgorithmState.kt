package com.template.states

import com.template.contracts.AlgorithmStateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import java.time.Instant
import java.util.*

@BelongsToContract(AlgorithmStateContract::class)
data class AlgorithmState(
        override val linearId: UniqueIdentifier = UniqueIdentifier.fromString(UUID.randomUUID().toString()),
        override val participants: List<AbstractParty>,
        val created: Instant = Instant.now(),
        val algorithm: ByteArray
) : LinearState {}