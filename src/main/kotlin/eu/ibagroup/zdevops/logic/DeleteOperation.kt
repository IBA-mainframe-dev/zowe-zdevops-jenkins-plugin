package eu.ibagroup.zdevops.logic

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsn
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsnList
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.input.ListParams
import eu.ibagroup.zdevops.declarative.jobs.zMessages
import eu.ibagroup.zdevops.utils.runMFTryCatchWrappedQuery
import hudson.AbortException
import hudson.model.TaskListener

/**
 * This class contains logic for mainframe datasets deletion
 */
class DeleteOperation {

    companion object {
        private val successMessage: String = zMessages.zdevops_deleting_ds_success()

        fun deleteDatasetsByMask(mask: String, zosConnection: ZOSConnection, listener: TaskListener) {
            if (mask.isEmpty()) {
                throw AbortException(zMessages.zdevops_deleting_datasets_by_mask_but_mask_is_empty())
            }
            listener.logger.println(zMessages.zdevops_deleting_ds_by_mask(mask))
            val dsnList = ZosDsnList(zosConnection).listDsn(mask, ListParams())
            if (dsnList.items.isEmpty()) {
                throw AbortException(zMessages.zdevops_deleting_ds_fail_no_matching_mask())
            }
            dsnList.items.forEach {
                runMFTryCatchWrappedQuery(listener) {
                    listener.logger.println(zMessages.zdevops_deleting_ds(it.name, zosConnection.host, zosConnection.zosmfPort))
                    ZosDsn(zosConnection).deleteDsn(it.name)
                }
            }
            listener.logger.println(successMessage)
        }

        fun deleteDatasetOrMember(dsn: String, member: String, zosConnection: ZOSConnection, listener: TaskListener) {
            if (dsn.isEmpty() && member.isEmpty()) {
                throw AbortException(zMessages.zdevops_deleting_ds_fail_none_params())
            }
            val memberNotEmpty = member.isNotEmpty()
            val logMessage = if (memberNotEmpty) zMessages.zdevops_deleting_ds_member(member, dsn, zosConnection.host, zosConnection.zosmfPort)
                             else zMessages.zdevops_deleting_ds(dsn, zosConnection.host, zosConnection.zosmfPort)
            listener.logger.println(logMessage)
            runMFTryCatchWrappedQuery(listener) {
                val response = if (memberNotEmpty) ZosDsn(zosConnection).deleteDsn(dsn, member)
                               else ZosDsn(zosConnection).deleteDsn(dsn)
            }
            listener.logger.println(successMessage)
        }

    }
}