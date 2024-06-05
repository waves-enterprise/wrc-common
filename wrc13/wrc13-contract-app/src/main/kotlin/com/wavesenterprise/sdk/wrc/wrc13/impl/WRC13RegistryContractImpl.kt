package com.wavesenterprise.sdk.wrc.wrc13.impl

import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.getValue
import com.wavesenterprise.sdk.contract.core.state.set
import com.wavesenterprise.sdk.wrc.wrc10.DefaultPermissions
import com.wavesenterprise.sdk.wrc.wrc10.WRC10RoleBasedAccessControl
import com.wavesenterprise.sdk.wrc.wrc10.impl.WRC10RoleBasedAccessControlImpl
import com.wavesenterprise.sdk.wrc.wrc10.impl.hasPermission
import com.wavesenterprise.sdk.wrc.wrc13.WRC13RegistryContract

@ContractHandler
class WRC13RegistryContractImpl(
    private val state: ContractState,
    private val call: ContractCall,
    internal val rbac: WRC10RoleBasedAccessControlImpl = WRC10RoleBasedAccessControlImpl(state, call)
) : WRC13RegistryContract, WRC10RoleBasedAccessControl by rbac {

    internal val data: Mapping<String> by state

    override fun create() {
        rbac.init()
        rbac.grant(call.sender.asBase58String(), DefaultPermissions.ADMIN)
    }

    override fun setValue(key: String, value: String) {
        require(rbac.hasPermission(call.sender.asBase58String(), DefaultPermissions.ADMIN))
        data[key] = value
    }
}
