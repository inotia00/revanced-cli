package app.revanced.cli.command

import app.revanced.patcher.patch.Package
import app.revanced.patcher.patch.loadPatchesFromJar
import com.google.gson.GsonBuilder
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Visibility.ALWAYS
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger

@Command(
    name = "patches",
    description = ["Generate patches file from RVP files."],
)
internal object PathesCommand : Runnable {
    private val logger = Logger.getLogger(PathesCommand::class.java.name)

    @Parameters(
        description = ["Paths to RVP files."],
        arity = "1..*",
    )
    private lateinit var patchesFiles: Set<File>

    @Option(
        names = ["-p", "--path"],
        description = ["Path to patches JSON file."],
        showDefaultValue = ALWAYS,
    )
    private var filePath: File = File("patches.json")

    override fun run() {
        try {
            if (!filePath.exists()) {
                Files.createFile(Paths.get(filePath.path))
            }
            loadPatchesFromJar(patchesFiles).let { patches ->
                patches.sortedBy { it.name }.map {
                    JsonPatch(
                        it.name!!,
                        it.description,
                        it.compatiblePackages,
                        it.use,
                        it.options.values.map { option ->
                            JsonPatch.Option(
                                option.key,
                                option.default,
                                option.values,
                                option.title,
                                option.description,
                                option.required,
                            )
                        },
                    )
                }.let {
                    filePath.writeText(GsonBuilder().serializeNulls().create().toJson(it))
                }
            }
            filePath.writeText(
                filePath.readText()
                    .replace(
                        "\"first\":",
                        "\"name\":"
                    ).replace(
                        "\"second\":",
                        "\"versions\":"
                    )
            )
        } catch (ex: PatchesFileAlreadyExistsException) {
            logger.severe("Patches file already exists, use --overwrite to override it")
        }
    }

    @Suppress("unused")
    class JsonPatch(
        val name: String? = null,
        val description: String? = null,
        val compatiblePackages: Set<Package>? = null,
        val use: Boolean = true,
        val options: List<Option>,
    ) {
        class Option(
            val key: String,
            val default: Any?,
            val values: Map<String, Any?>?,
            val title: String?,
            val description: String?,
            val required: Boolean,
        )
    }

    class PatchesFileAlreadyExistsException : Exception()
}
