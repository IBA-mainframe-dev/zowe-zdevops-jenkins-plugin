package eu.ibagroup.zdevops.declarative.jobs

import eu.ibagroup.r2z.zowe.client.sdk.core.ZOSConnection
import eu.ibagroup.r2z.zowe.client.sdk.zosfiles.ZosDsn
import eu.ibagroup.zdevops.declarative.AbstractZosmfAction
import hudson.*
import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor

class WriteToDatasetDeclarative @DataBoundConstructor constructor(private val dsn: String,
                                                                  private val text: String) :
    AbstractZosmfAction() {

    override val exceptionMessage: String = zMessages.zdevops_declarative_writing_DS_fail(dsn)

    override fun perform(
        run: Run<*, *>,
        workspace: FilePath,
        env: EnvVars,
        launcher: Launcher,
        listener: TaskListener,
        zosConnection: ZOSConnection
    ) {
        if (text != "") {
            listener.logger.println(zMessages.zdevops_declarative_writing_DS_from_input(dsn, zosConnection.host, zosConnection.zosmfPort))

            val stringList = text.split('\n')
            val targetDS = ZosDsn(zosConnection).getDatasetInfo(dsn)
            if (targetDS.recordLength == null) {
                throw AbortException(zMessages.zdevops_declarative_writing_DS_no_info(dsn))
            }
            var ineligibleStrings = 0
            stringList.forEach {
                if (it.length > targetDS.recordLength!!) {
                    ineligibleStrings++
                }
            }
            if (ineligibleStrings > 0) {
                throw AbortException(zMessages.zdevops_declarative_writing_DS_ineligible_strings(ineligibleStrings,dsn))
            } else {
                val textByteArray = text.replace("\r","").toByteArray()
                val writeToDS = ZosDsn(zosConnection).writeDsn(dsn, textByteArray)
                listener.logger.println(zMessages.zdevops_declarative_writing_DS_success(dsn))
            }
        } else {
            listener.logger.println(zMessages.zdevops_declarative_writing_skip())
        }
    }


    @Symbol("writeToDS")
    @Extension
    class DescriptorImpl : Companion.DefaultBuildDescriptor("Write to Dataset Declarative")
}

