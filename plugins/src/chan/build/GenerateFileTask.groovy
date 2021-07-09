package chan.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class GenerateFileTask extends DefaultTask {
	@Input String inputText
	@OutputFile File outputFile

	@TaskAction
	def action() {
		outputFile.write(inputText)
	}
}
