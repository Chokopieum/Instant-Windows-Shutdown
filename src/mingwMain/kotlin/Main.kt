@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import platform.windows.*

fun enableShutdownPrivilege(): Boolean {
    var success = false

    memScoped {
        val hToken = alloc<HANDLEVar>()
        val currentProcess = GetCurrentProcess()

        val accessFlags = (TOKEN_ADJUST_PRIVILEGES or TOKEN_QUERY).toUInt()
        if (OpenProcessToken(currentProcess, accessFlags, hToken.ptr) != 0) {

            val luid = alloc<LUID>()
            if (LookupPrivilegeValueW(null, "SeShutdownPrivilege", luid.ptr) != 0) {

                val tp = alloc<TOKEN_PRIVILEGES>()
                tp.PrivilegeCount = 1u

                val privilege = tp.Privileges.pointed
                privilege.Luid.LowPart = luid.LowPart
                privilege.Luid.HighPart = luid.HighPart
                privilege.Attributes = SE_PRIVILEGE_ENABLED.toUInt()

                if (AdjustTokenPrivileges(hToken.value, 0, tp.ptr, 0u, null, null) != 0) {
                    if (GetLastError() == 0u) {
                        success = true
                    }
                }
            }
            CloseHandle(hToken.value)
        }
    }
    return success
}

fun main() {


    if (!enableShutdownPrivilege()) {

        return
    }



    val hNtDll = GetModuleHandleW("ntdll.dll") ?: LoadLibraryW("ntdll.dll")
    if (hNtDll == null) {

        return
    }

    val ntShutdownSystemPtr = GetProcAddress(hNtDll, "NtShutdownSystem")
    if (ntShutdownSystemPtr == null) {

        return
    }



    val ntShutdownSystem = ntShutdownSystemPtr.reinterpret<CFunction<(Int) -> Int>>()

    ntShutdownSystem(2)

    println("Если вы это видите, значит выключение сорвалось.")
}