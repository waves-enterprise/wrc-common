package com.wavesenterprise.wrc.wrc10.impl

import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.getValue
import com.wavesenterprise.wrc.wrc10.DefaultPermissions
import com.wavesenterprise.wrc.wrc10.StateMappings
import com.wavesenterprise.wrc.wrc10.WRC10RoleBasedAccessControl

class WRC10RoleBasedAccessControlImpl(
    internal val state: ContractState,
    private val call: ContractCall,
) : WRC10RoleBasedAccessControl {

    internal val permissions: Mapping<Set<String>> by state
    internal val roleAdmins: Mapping<Set<String>> by state

    fun init() {
        permissions.put(call.caller, setOf())
        roleAdmins.put(call.caller, setOf())
        state.put(StateMappings.OWNER, call.caller)
    }

    override fun grant(to: String, role: String) {
        require(canManage(call.caller, role))
        modifyRole(to) { it.add(role) }
    }

    override fun revoke(from: String, role: String) {
        require(canManage(call.caller, role))
        modifyRole(from) { it.remove(role) }
    }

    override fun grantRoleAdmin(to: String, role: String) {
        require(canManage(call.caller, role))
        modifyRoleAdmin(to) { it.add(role) }
    }

    override fun revokeRoleAdmin(from: String, role: String) {
        require(canManage(call.caller, role))
        modifyRoleAdmin(from) { it.remove(role) }
    }

    private fun modifyRole(address: String, fn: (MutableSet<String>) -> Unit) {
        permissionsOf(address).toMutableSet().apply {
            fn(this)
            permissions.put(address, this.toSet())
        }
    }

    private fun modifyRoleAdmin(address: String, fn: (MutableSet<String>) -> Unit) {
        rolesAdministratedBy(address).toMutableSet().apply {
            fn(this)
            roleAdmins.put(address, this.toSet())
        }
    }
}

fun WRC10RoleBasedAccessControlImpl.hasPermission(address: String, permission: String): Boolean =
    this.permissions.tryGet(address).map {
        it.contains(permission)
    }.orElse(false)

fun WRC10RoleBasedAccessControlImpl.isRoleAdmin(address: String, role: String): Boolean =

    this.roleAdmins.tryGet(address).map {
        it.contains(role)
    }.orElse(false)

fun WRC10RoleBasedAccessControlImpl.permissionsOf(address: String): Set<String> =
    this.permissions.tryGet(address).orElse(setOf())

fun WRC10RoleBasedAccessControlImpl.rolesAdministratedBy(address: String): Set<String> =
    this.roleAdmins.tryGet(address).orElse(setOf())

fun WRC10RoleBasedAccessControlImpl.isOwner(address: String) =
    this.state[StateMappings.OWNER, String::class.java] == address

fun WRC10RoleBasedAccessControlImpl.canManage(address: String, role: String) =
    isOwner(address) ||
        hasPermission(address, DefaultPermissions.ADMIN) ||
        isRoleAdmin(address, role)
