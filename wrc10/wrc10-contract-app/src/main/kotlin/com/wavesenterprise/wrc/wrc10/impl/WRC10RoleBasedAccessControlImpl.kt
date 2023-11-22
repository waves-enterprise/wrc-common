package com.wavesenterprise.wrc.wrc10.impl

import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.wrc.wrc10.DefaultPermissions
import com.wavesenterprise.wrc.wrc10.StateMappings
import com.wavesenterprise.wrc.wrc10.WRC10RoleBasedAccessControl

class WRC10RoleBasedAccessControlImpl(
    internal val state: ContractState,
    private val call: ContractCall,
) : WRC10RoleBasedAccessControl {

    internal val permissions: Mapping<Set<String>> = state.getMapping(
        object : TypeReference<Set<String>>() {},
        StateMappings.PERMISSIONS,
    )
    internal val roleAdmins: Mapping<Set<String>> = state.getMapping(
        object : TypeReference<Set<String>>() {},
        StateMappings.ROLE_ADMINS
    )

    fun init() {
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

fun WRC10RoleBasedAccessControlImpl.hasPermission(address: String, permission: String) =
    this.permissions.tryGet(address).map {
        it.contains(permission)
    }.orElse(false)!!

fun WRC10RoleBasedAccessControlImpl.isRoleAdmin(address: String, role: String) =
    this.roleAdmins.tryGet(address).map {
        it.contains(role)
    }.orElse(false)!!

fun WRC10RoleBasedAccessControlImpl.permissionsOf(address: String) =
    this.permissions.tryGet(address).orElse(setOf())!!

fun WRC10RoleBasedAccessControlImpl.rolesAdministratedBy(address: String) =
    this.roleAdmins.tryGet(address).orElse(setOf())!!

fun WRC10RoleBasedAccessControlImpl.isOwner(address: String) =
    this.state[StateMappings.OWNER, String::class.java] == address

fun WRC10RoleBasedAccessControlImpl.canManage(address: String, role: String) =
    isOwner(address) ||
        hasPermission(address, DefaultPermissions.ADMIN) ||
        isRoleAdmin(address, role)
